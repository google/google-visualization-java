// Copyright 2009 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.visualization.datasource.util;

import com.google.common.collect.Lists;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.base.ReasonType;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.BooleanValue;
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.TimeOfDayValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.AbstractColumn;
import com.google.visualization.datasource.query.AggregationColumn;
import com.google.visualization.datasource.query.AggregationType;
import com.google.visualization.datasource.query.ColumnColumnFilter;
import com.google.visualization.datasource.query.ColumnIsNullFilter;
import com.google.visualization.datasource.query.ColumnSort;
import com.google.visualization.datasource.query.ColumnValueFilter;
import com.google.visualization.datasource.query.ComparisonFilter;
import com.google.visualization.datasource.query.CompoundFilter;
import com.google.visualization.datasource.query.NegationFilter;
import com.google.visualization.datasource.query.Query;
import com.google.visualization.datasource.query.QueryFilter;
import com.google.visualization.datasource.query.QueryGroup;
import com.google.visualization.datasource.query.QuerySelection;
import com.google.visualization.datasource.query.QuerySort;
import com.google.visualization.datasource.query.SimpleColumn;
import com.google.visualization.datasource.query.SortOrder;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;


/**
 * A utility class, with static methods that are specific for creating a
 * data source based on a SQL database table.
 * For now, it can be based on mysql database only.
 *
 * @author Liron L.
 */
public class SqlDataSourceHelper {

  /**
   * Log.
   */
  private static final Log log = LogFactory.getLog(SqlDataSourceHelper.class.getName());

  /**
   * A private constructor - all methods are static.
   */
  private SqlDataSourceHelper() {}

  /**
   * Executes the given query on the given SQL database table, and returns the
   * result as a DataTable.
   *
   * @param query The query.
   * @param databaseDescription The information needed to connect to the SQL database and table.
   *
   * @return DataTable A data table with the data from the specified sql table,
   *     after applying the specified query on it.
   *
   * @throws DataSourceException Thrown when the data source fails to perform the action.
   */
  public static DataTable executeQuery(Query query, SqlDatabaseDescription databaseDescription)
      throws DataSourceException {
    Connection con = getDatabaseConnection(databaseDescription);
    String tableName = databaseDescription.getTableName();

    // Build the sql query.
    StrBuilder queryStringBuilder = new StrBuilder();
    buildSqlQuery(query, queryStringBuilder, tableName);
    List<String> columnIdsList = null;
    if (query.hasSelection()) {
      columnIdsList = getColumnIdsList(query.getSelection());
    }
    Statement stmt = null;
    try {
      // Execute the sql query.
      stmt = con.createStatement();
      ResultSet rs = stmt.executeQuery(queryStringBuilder.toString());

      DataTable table = buildColumns(rs, columnIdsList);

      // Fill the data in the data table.
      buildRows(table, rs);
      return table;
    } catch (SQLException e) {
      String messageToUser = "Failed to execute SQL query. mySQL error message:"
          + " " + e.getMessage();
      throw new DataSourceException(
          ReasonType.INTERNAL_ERROR, messageToUser);
    } finally {
      if (stmt != null) {
        try {
          stmt.close();
        } catch (SQLException e) { /* ignore close errors */ }
      }
      if (con != null) {
        try {
          con.close();
        } catch (SQLException e) { /* ignore close errors */ }
      }
    }
  }

  /**
   * Returns a connection to the SQL database.
   *
   * @param databaseDescription The database description.
   *
   * @return The SQL database connection.
   *
   * @throws DataSourceException Thrown when the database connection is failed.
   */
  private static Connection getDatabaseConnection(
      SqlDatabaseDescription databaseDescription) throws DataSourceException{
    Connection con;
    // Set the connection's parameters.
    String userName = databaseDescription.getUser();
    String password = databaseDescription.getPassword();
    String url = databaseDescription.getUrl();
    try {
      // Connect to the database.
      // We should add connection pooling to avoid heavy creation of connections.
      con = DriverManager.getConnection(url, userName, password);
    } catch (SQLException e) {
      log.error("Failed to connect to database server.", e);
      throw new DataSourceException(
          ReasonType.INTERNAL_ERROR, "Failed to connect to database server.");
    }
    return con;
  }

  /**
   * Builds the sql query.
   *
   * @param query The query.
   * @param queryStringBuilder A string builder to build the sql query.
   * @param tableName The sql table name.
   *
   * @throws DataSourceException On errors to create the data table.
   */
  private static void buildSqlQuery(
      Query query, StrBuilder queryStringBuilder, String tableName)
      throws DataSourceException {
    appendSelectClause(query, queryStringBuilder);
    appendFromClause(query, queryStringBuilder, tableName);
    appendWhereClause(query, queryStringBuilder);
    appendGroupByClause(query, queryStringBuilder);
    appendOrderByClause(query, queryStringBuilder);
    appendLimitAndOffsetClause(query, queryStringBuilder);
  }

  /**
   * Appends the LIMIT and OFFSET clause of the sql query to the given string
   * builder. If there is no LIMIT on the number of rows, uses the system row
   * limit.
   *
   * @param query The query.
   * @param queryStringBuilder The string builder holding the string query.
   */
  static void appendLimitAndOffsetClause(
      Query query, StrBuilder queryStringBuilder) {
    if (query.hasRowLimit()) {
      queryStringBuilder.append("LIMIT ");
      queryStringBuilder.append(query.getRowLimit());
    }
    if (query.hasRowOffset()) {
      queryStringBuilder.append(" OFFSET ").append(query.getRowOffset());
    }
  }

  /**
   * Appends the GROUP BY clause of the sql query to the given string builder.
   *
   * @param query The query.
   * @param queryStringBuilder The string builder holding the string query.
   */
  static void appendGroupByClause(Query query, StrBuilder queryStringBuilder) {
    if (!query.hasGroup()) {
      return;
    }
    queryStringBuilder.append("GROUP BY ");
    QueryGroup queryGroup = query.getGroup();
    List<String> groupColumnIds = queryGroup.getColumnIds();
    List<String> newColumnIds = Lists.newArrayList();
    for (String groupColumnId : groupColumnIds) {
      newColumnIds.add('`' + groupColumnId + '`');
    }
    queryStringBuilder.appendWithSeparators(newColumnIds, ", ");
    queryStringBuilder.append(" ");
  }

  /**
   * Appends the ORDER BY clause of the sql query to the given string builder.
   *
   * @param query The query.
   * @param queryStringBuilder The string builder holding the string query.
   */
  static void appendOrderByClause(Query query, StrBuilder queryStringBuilder) {
    if (!query.hasSort()) {
      return;
    }
    queryStringBuilder.append("ORDER BY ");
    QuerySort querySort = query.getSort();
    List<ColumnSort> sortColumns = querySort.getSortColumns();
    int numOfSortColumns = sortColumns.size();
    for (int col = 0; col < numOfSortColumns; col++) {
      ColumnSort columnSort = sortColumns.get(col);
      queryStringBuilder.append(getColumnId(columnSort.getColumn()));
      if (columnSort.getOrder() == SortOrder.DESCENDING) {
        queryStringBuilder.append(" DESC");
      }
      if (col < numOfSortColumns - 1) {
        queryStringBuilder.append(", ");
      }
    }
    queryStringBuilder.append(" ");
  }

  /**
   * Appends the WHERE clause of the sql query to the given string builder.
   *
   * @param query The query.
   * @param queryStringBuilder The string builder holding the string query.
   */
  static void appendWhereClause(Query query, StrBuilder queryStringBuilder) {
    if (query.hasFilter()) {
      QueryFilter queryFilter = query.getFilter();
      queryStringBuilder.append("WHERE ")
          .append(buildWhereClauseRecursively(queryFilter)).append(" ");
    }
  }

  /**
   * Builds the sql WHERE clause recursively from the given query filter.
   * The WHERE clause structure is a tree where the leafs are comparison
   * filters and the internal nodes are compound filters. The recursion builds
   * a string like an in-order walk on the tree. Each filter (i.e. a node in
   * the tree) has parenthesis around it.
   *
   * @param queryFilter The query filter.
   *
   * @return The sql query WHERE clause as a StrBuilder.
   */
  private static StrBuilder buildWhereClauseRecursively(QueryFilter queryFilter) {
    StrBuilder whereClause = new StrBuilder();

    // Base case of the recursion: the filter is not a compound filter.
    if (queryFilter instanceof ColumnIsNullFilter) {
      buildWhereClauseForIsNullFilter(whereClause, queryFilter);
    } else if (queryFilter instanceof ComparisonFilter) {
      buildWhereCluaseForComparisonFilter(whereClause, queryFilter);
    } else if (queryFilter instanceof NegationFilter) {
      whereClause.append("(NOT ");
      whereClause.append(buildWhereClauseRecursively(
          ((NegationFilter) queryFilter).getSubFilter()));
      whereClause.append(")");
    } else {
      // queryFilter is a CompoundFilter.
      CompoundFilter compoundFilter = (CompoundFilter) queryFilter;

      int numberOfSubFilters = compoundFilter.getSubFilters().size();

      // If the compound filter is empty, build a where clause according to the
      // logical operator: nothing AND nothing -> WHERE "true", nothing OR
      // nothing -> WHERE "false" (match the query language rules).
      if (numberOfSubFilters == 0) {
        if (compoundFilter.getOperator() == CompoundFilter.LogicalOperator.AND) {
          whereClause.append("true");
        } else {// OR
          whereClause.append("false");
        }
      } else {
        List<String> filterComponents = Lists.newArrayList();
        for (QueryFilter filter : compoundFilter.getSubFilters()) {
          filterComponents.add(buildWhereClauseRecursively(filter).toString());
        }
        String logicalOperator = getSqlLogicalOperator(compoundFilter.getOperator());
        whereClause.append("(").appendWithSeparators(filterComponents, " " + logicalOperator + " ")
            .append(")");
      }
    }
    return whereClause;
  }

  /**
   * Builds a WHERE clause for is-null filter.
   * 
   * @param whereClause A string builder representing the WHERE clause of the SQL query.
   * @param queryFilter The query filter.
   */
  private static void buildWhereClauseForIsNullFilter(StrBuilder whereClause,
      QueryFilter queryFilter) {
    ColumnIsNullFilter filter = (ColumnIsNullFilter) queryFilter;
 
    whereClause.append("(").append(getColumnId(filter.getColumn())).append(" IS NULL)");
  }

  /**
   * Builds the WHERE clause for comparison filter. This is the base case of
   * the recursive building of the WHERE clause of the sql query.
   *
   * @param whereClause A string builder representing the WHERE clause of the SQL query.
   * @param queryFilter The query filter.
   */
  private static void buildWhereCluaseForComparisonFilter(
      StrBuilder whereClause, QueryFilter queryFilter) {
    StrBuilder first = new StrBuilder();
    StrBuilder second = new StrBuilder();

    // Build the left part and the right part of the clause according to the filter's type.
    if (queryFilter instanceof ColumnColumnFilter) {
      ColumnColumnFilter filter = (ColumnColumnFilter) queryFilter;
      first.append(getColumnId(filter.getFirstColumn()));
      second.append(getColumnId(filter.getSecondColumn()));
    } else { // The filter is a ColumnValueFilter
      ColumnValueFilter filter = (ColumnValueFilter) queryFilter;
      first.append(getColumnId(filter.getColumn()));
      second.append(filter.getValue().toString());
      if ((filter.getValue().getType() == ValueType.TEXT)
          || (filter.getValue().getType() == ValueType.DATE)
          || (filter.getValue().getType() == ValueType.DATETIME)
          || (filter.getValue().getType() == ValueType.TIMEOFDAY)) {
        second.insert(0, "\"");
        second.insert(second.length(), "\"");
      }
    }
    whereClause.append(buildWhereClauseFromRightAndLeftParts(
        first, second, ((ComparisonFilter) queryFilter).getOperator()));
  }

  /**
   * Returns the sql operator of the given CompoundFilter.LogicalOperator as a string.
   *
   * @param operator The CompoundFilter.LogicalOperator.
   *
   * @return A string representation of the SQL operator.
   */
  private static String getSqlLogicalOperator(CompoundFilter.LogicalOperator operator) {
    String stringOperator;
    switch (operator) {
      case AND:
        stringOperator = "AND";
        break;
      case OR:
        stringOperator = "OR";
        break;
      default:// Should never get here.
        throw new RuntimeException("Logical operator was not found: " + operator);
    }
    return stringOperator;
  }

  /**
   * Builds the where clause of the SQL query sql from the two given values and
   * the operator between these two values.
   *
   * @param value1 The first value in the where clause (either column id or value)
   * @param value2 The second value in the where clause (either column id or value)
   * @param operator The ComparisonFilter.Operator.
   *
   * @return A string builder representing the where clause of the SQL query.
   */
  private static StrBuilder buildWhereClauseFromRightAndLeftParts(
      StrBuilder value1, StrBuilder value2, ComparisonFilter.Operator operator) {
    StrBuilder clause;
    switch (operator) {
      case EQ:
        clause = value1.append("=").append(value2);
        break;
      case NE:
        clause = value1.append("<>").append(value2);
        break;
      case LT:
        clause = value1.append("<").append(value2);
        break;
      case GT:
        clause = value1.append(">").append(value2);
        break;
      case LE:
        clause = value1.append("<=").append(value2);
        break;
      case GE:
        clause = value1.append(">=").append(value2);
        break;
      case CONTAINS:
        value2 = new StrBuilder(value2.toString().replace("\"", ""));
        clause = value1.append(" LIKE ").append("\"%").append(value2).append("%\"");
        break;
      case STARTS_WITH:
        value2 = new StrBuilder(value2.toString().replace("\"", ""));
        clause = value1.append(" LIKE ").append("\"").append(value2).append("%\"");
        break;
      case ENDS_WITH:
        value2 = new StrBuilder(value2.toString().replace("\"", ""));
        clause = value1.append(" LIKE ").append("\"%").append(value2).append("\"");
        break;
      case MATCHES:
        throw new RuntimeException("SQL does not support regular expression");
      case LIKE:
        value2 = new StrBuilder(value2.toString().replace("\"", ""));
        clause = value1.append(" LIKE ").append("\"").append(value2).append("\"");
        break;
      default:// Should never get here.
        throw new RuntimeException("Operator was not found: " + operator);
    }
    clause.insert(0, "(").append(")");
    return clause;
  }

  /**
   * Appends the SELECT clause of the sql query to the given string builder.
   *
   * @param query The query.
   * @param queryStringBuilder The string builder holding the string query.
   */

  static void appendSelectClause(Query query,
      StrBuilder queryStringBuilder) {
    queryStringBuilder.append("SELECT ");

    // If it's a selectAll query, build "select *" clause.
    if (!query.hasSelection()) {
      queryStringBuilder.append("* ");
      return;
    }

    List<AbstractColumn> columns = query.getSelection().getColumns();
    int numOfColsInQuery = columns.size();

    // Add the Ids of the columns to the select clause
    for (int col = 0; col < numOfColsInQuery; col++) {
      queryStringBuilder.append(getColumnId(columns.get(col)));
      if (col < numOfColsInQuery - 1) {
        queryStringBuilder.append(", ");
      }
    }
    queryStringBuilder.append(" ");
  }

  /**
   * Returns the column id in SQL.
   *
   * @param abstractColumn The column.
   *
   * @return The column id for the data table.
   */
  private static StrBuilder getColumnId(AbstractColumn abstractColumn) {
    StrBuilder columnId = new StrBuilder();

    // For simple column the id is simply the column id.
    if (abstractColumn instanceof SimpleColumn) {
      columnId.append("`").append(abstractColumn.getId()).append("`");
    } else {
      // For aggregation column build the id from the aggregation type and the
      // column id (e.g. for aggregation type 'min' and column id 'salary', the
      // sql column id will be: min(`salary`);
      AggregationColumn aggregationColumn = (AggregationColumn) abstractColumn;
      columnId.append(getAggregationFunction(
          aggregationColumn.getAggregationType())).append("(`").
          append(aggregationColumn.getAggregatedColumn()).append("`)");
    }
    return columnId;
  }

  /**
   * Returns a list with the selected column ids in the table description.
   *
   * @param selection The query selection.
   *
   * @return a list with the selected column ids.
   */
  private static List<String> getColumnIdsList(QuerySelection selection) {
    List<String> columnIds =
        Lists.newArrayListWithCapacity(selection.getColumns().size());
    for (AbstractColumn column : selection.getColumns()) {
      columnIds.add(column.getId());
    }
    return columnIds;
  }

  /**
   * Returns a string representation of the given aggregation type.
   *
   * @param type The aggregation type.
   *
   * @return The aggragation type's string representation.
   */
  private static String getAggregationFunction(AggregationType type) {
    return type.getCode();
  }

  /**
   * Appends the FROM clause of the sql query to the given string builder. Takes
   * the table name from the configuration file. If no table name is given,
   * takes the table name from the query.
   *
   * @param query The query.
   * @param queryStringBuilder The string builder holding the string query.
   * @param tableName The database table name.
   *
   * @throws DataSourceException Thrown when no table name provided, or when the
   *     table name in the data source doesn't match the table name in the
   *     query.
   */
  static void appendFromClause(Query query,
      StrBuilder queryStringBuilder, String tableName)
      throws DataSourceException {
    if (StringUtils.isEmpty(tableName)) {
      log.error("No table name provided.");
      throw new DataSourceException(ReasonType.OTHER, "No table name provided.");
    }
    queryStringBuilder.append("FROM ");
    queryStringBuilder.append(tableName);
    queryStringBuilder.append(" ");
  }

  /**
   * Returns the table description which includes the ids, labels and types of
   * the table columns.
   *
   * @param rs The result set holding the data from the sql table.
   * @param columnIdsList The list of the column ids in the data table.
   *
   * @return The table description.
   *
   * @throws SQLException Thrown when the connection to the database failed.
   */
  static DataTable buildColumns(ResultSet rs, List<String> columnIdsList) throws SQLException {
    DataTable result = new DataTable();
    ResultSetMetaData metaData = rs.getMetaData();
    int numOfCols = metaData.getColumnCount();
    // For each column in the table, create the column description. SQL indexes
    // are 1-based.
    for (int i = 1; i <= numOfCols; i++) {
      String id = (columnIdsList == null) ? metaData.getColumnLabel(i) :
          columnIdsList.get(i - 1);
      ColumnDescription columnDescription =
          new ColumnDescription(id,
              sqlTypeToValueType(metaData.getColumnType(i)),
              metaData.getColumnLabel(i));
      result.addColumn(columnDescription);
    }
    return result;
  }

  /**
   * Converts the given SQL type to a value type.
   *
   * @param sqlType The sql type to be converted.
   *
   * @return The value type that fits the given sql type.
   */
  private static ValueType sqlTypeToValueType(int sqlType) {
    ValueType valueType;
    switch (sqlType) {
      case Types.BOOLEAN:
      case Types.BIT: {
        valueType =  ValueType.BOOLEAN;
        break;
      }
      case Types.CHAR:
      case Types.VARCHAR:
        valueType =  ValueType.TEXT;
        break;
      case Types.INTEGER:
      case Types.SMALLINT:
      case Types.BIGINT:
      case Types.TINYINT:
      case Types.REAL:
      case Types.NUMERIC:
      case Types.DOUBLE:
      case Types.FLOAT:
      case Types.DECIMAL:
        valueType = ValueType.NUMBER;
        break;
      case Types.DATE:
        valueType = ValueType.DATE;
        break;
      case Types.TIME:
        valueType = ValueType.TIMEOFDAY;
        break;
      case Types.TIMESTAMP:
        valueType = ValueType.DATETIME;
        break;
      default:
        valueType = ValueType.TEXT;
        break;
    }
    return valueType;
  }

  /**
   * Populates the data table and returns it.
   *
   * @param dataTable The data table to populates, that should already contains the
   *     column descriptions.
   * @param rs The result set holding the results of running the query on the
   *     relevant sql database table. The result set's data required for
   *     building the rows of the data table.
   *
   * @throws SQLException Thrown when the connection to the database failed.
   */
  static void buildRows(DataTable dataTable, ResultSet rs) throws SQLException {
    List <ColumnDescription> columnsDescriptionList = dataTable.getColumnDescriptions();
    int numOfCols = dataTable.getNumberOfColumns();

    // Get the value types of the columns.
    ValueType[] columnsTypeArray = new ValueType[numOfCols];
    for (int c = 0; c < numOfCols; c++) {
      columnsTypeArray[c] = columnsDescriptionList.get(c).getType();
    }

    // Build the data table rows, and in each row create the table cells with
    // the information in the result set.
    while (rs.next()) {
      TableRow tableRow = new TableRow();
      for (int c = 0; c < numOfCols; c++) {
        tableRow.addCell(buildTableCell(rs, columnsTypeArray[c], c));
      }
      try {
        dataTable.addRow(tableRow);
      } catch (TypeMismatchException e) {
        // Should not happen. An SQLException would already have been thrown if there was such a
        // problem.
      }
    }
  }

  /**
   * Creates a table cell from the value in the current row of the given result
   * set and the given column index. The type of the value is determined by the
   * given value type.
   *
   * @param rs The result set holding the data from the sql table. The result
   *     points to the current row.
   * @param valueType The value type of the column that the cell belongs to.
   * @param column The column index. Indexes are 0-based.
   *
   * @return The table cell.
   *
   * @throws SQLException Thrown when the connection to the database failed.
   */
  private static TableCell buildTableCell(ResultSet rs, ValueType valueType,
      int column) throws SQLException {
    Value value = null;

    // SQL indexes are 1- based.
    column = column + 1;

    switch (valueType) {
      case BOOLEAN:
        value = BooleanValue.getInstance(rs.getBoolean(column));
        break;
      case NUMBER:
        value = new NumberValue(rs.getDouble(column));
        break;
      case DATE:
        Date date = rs.getDate(column);
        // If date is null it is handled later.
        if (date != null) {
          GregorianCalendar gc =
              new GregorianCalendar(TimeZone.getTimeZone("GMT"));
          // Set the year, month and date in the gregorian calendar.
          // Use the 'set' method with those parameters, and not the 'setTime'
          // method with the date parameter, since the Date object contains the
          // current time zone and it's impossible to change it to 'GMT'.
          gc.set(date.getYear() + 1900, date.getMonth(), date.getDate());
          value = new DateValue(gc);
        }
        break;
      case DATETIME:
        Timestamp timestamp = rs.getTimestamp(column);
        // If timestamp is null it is handled later.
        if (timestamp != null) {
          GregorianCalendar gc =
              new GregorianCalendar(TimeZone.getTimeZone("GMT"));
          // Set the year, month, date, hours, minutes and seconds in the
          // gregorian calendar. Use the 'set' method with those parameters,
          // and not the 'setTime' method with the timestamp parameter, since
          // the Timestamp object contains the current time zone and it's
          // impossible to change it to 'GMT'.
          gc.set(timestamp.getYear() + 1900, timestamp.getMonth(),
                 timestamp.getDate(), timestamp.getHours(), timestamp.getMinutes(),
                 timestamp.getSeconds());
          // Set the milliseconds explicitly, as they are not saved in the
          // underlying date.
          gc.set(Calendar.MILLISECOND, timestamp.getNanos() / 1000000);
          value = new DateTimeValue(gc);
        }
        break;
      case TIMEOFDAY:
        Time time = rs.getTime(column);
        // If time is null it is handled later.
        if (time != null) {
          GregorianCalendar gc =
              new GregorianCalendar(TimeZone.getTimeZone("GMT"));
          // Set the hours, minutes and seconds of the time in the gregorian
          // calendar. Set the year, month and date to be January 1 1970 like
          // in the Time object.
          // Use the 'set' method with those parameters,
          // and not the 'setTime' method with the time parameter, since
          // the Time object contains the current time zone and it's
          // impossible to change it to 'GMT'.
          gc.set(1970, Calendar.JANUARY, 1, time.getHours(), time.getMinutes(),
                 time.getSeconds());
          // Set the milliseconds explicitly, otherwise the milliseconds from
          // the time the gc was initialized are used.
          gc.set(GregorianCalendar.MILLISECOND, 0);
          value = new TimeOfDayValue(gc);
        }
        break;
      default:
        String colValue = rs.getString(column);
        if (colValue == null) {
          value = TextValue.getNullValue();
        } else {
          value = new TextValue(rs.getString(column));
        }
        break;
    }
    // Handle null values.
    if (rs.wasNull()) {
      return new TableCell(Value.getNullValueFromValueType(valueType));
    } else {
      return new TableCell(value);
    }
  }
}
