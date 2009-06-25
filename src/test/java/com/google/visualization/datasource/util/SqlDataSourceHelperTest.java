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
import com.google.visualization.datasource.base.InvalidQueryException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.value.BooleanValue;
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.TimeOfDayValue;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.AggregationColumn;
import com.google.visualization.datasource.query.AggregationType;
import com.google.visualization.datasource.query.ColumnColumnFilter;
import com.google.visualization.datasource.query.ColumnIsNullFilter;
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

import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;
import junit.framework.TestCase;

import org.apache.commons.lang.text.StrBuilder;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

/**
 * Tests for the SqlDataSourceTest class.
 * The DB connection is not being tested, only the building of the query and
 * the creation of the data table from the jdbc result set.
 * For this purpose result set and result set meta data mocks are used.
 *
 * @author Liron L.
 */
public class SqlDataSourceHelperTest extends TestCase {

  /**
   * Number of columns in the sql Table.
   */
  private static final int NUM_OF_COLS = 9;

  /**
   * The table's columns labels.
   */
  private List<String> labels;

  /**
   * The table's columns types.
   */
  private List<Integer> types;

  /**
   * A list of lists holding the table data.
   */
  private List<List<Object>> rows;

  /**
   * Sets the information of the table columns: labels and types. Creates empty
   * list for the table rows as well.
   *
   * This method is called before a test is executed.
   */
  @Override
  protected void setUp() {
    labels = Lists.newArrayList("ID", "Fname", "Lname", "Gender", "Salary",
        "IsMarried", "StartDate", "TimeStamp", "Time");
    // Use the JDBC type constants as defined in java.sql.Types.
    types = Lists.newArrayList(Types.INTEGER, Types.VARCHAR, Types.VARCHAR,
        Types.CHAR, Types.INTEGER, Types.BOOLEAN, Types.DATE, Types.TIMESTAMP,
        Types.TIME);
    rows = Lists.newArrayList();
  }

  /**
   * Clears the data of the table columns and rows.
   * This method is called after a test is executed.
   */
  @Override
  protected void tearDown() {
    labels.clear();
    types.clear();
    rows.clear();
  }

  /**
   * Tests the building of the SQL query SELECT clause from the Gviz query.
   */
  public void testBuildSelectClause() {
    Query query = new Query();

    QuerySelection querySelection = new QuerySelection();
    querySelection.addColumn(new SimpleColumn("ID"));
    querySelection.addColumn(
        new AggregationColumn(new SimpleColumn("Salary"), AggregationType.MIN));
    query.setSelection(querySelection);
    StrBuilder queryStringBuilder = new StrBuilder();
    SqlDataSourceHelper.appendSelectClause(query, queryStringBuilder);
    assertEquals(queryStringBuilder.toString(), "SELECT `ID`, min(`Salary`) ",
        queryStringBuilder.toString());
  }


  /**
   * Tests the building of the SQL query FROM clause from the Gviz query.
   */
  public void testBuildFromClause() throws DataSourceException {
    Query query = new Query();

    StrBuilder queryStringBuilder = new StrBuilder();
    SqlDataSourceHelper.appendFromClause(query, queryStringBuilder, "Employee");
    assertEquals(queryStringBuilder.toString(), "FROM Employee ",
        queryStringBuilder.toString());
  }

  /**
   * Tests the building of the SQL query WHERE clause from the Gviz query.
   */
  public void testBuildWhereClause() {
    Query query = new Query();

    QueryFilter queryFilter1 = new ColumnColumnFilter(new SimpleColumn("ID"),
        new SimpleColumn("Salary"), ComparisonFilter.Operator.EQ);
    QueryFilter queryFilter2 = new ColumnValueFilter(new SimpleColumn("ID"),
        new NumberValue(1), ComparisonFilter.Operator.GE);
    QueryFilter queryFilter3 = new ColumnValueFilter(new SimpleColumn("Fname"),
        new TextValue("Mi"), ComparisonFilter.Operator.STARTS_WITH);
    QueryFilter queryFilter4 = new ColumnValueFilter(new SimpleColumn("Lname"),
        new TextValue("SH"), ComparisonFilter.Operator.CONTAINS);
     QueryFilter queryFilter5 = new ColumnValueFilter(new SimpleColumn("Lname"),
        new TextValue("tz"), ComparisonFilter.Operator.ENDS_WITH);
    List<QueryFilter> subFiltersList1 = Lists.newArrayList();
    subFiltersList1.add(queryFilter1);
    subFiltersList1.add(queryFilter2);
    QueryFilter queryCompoundFilter1 =
        new CompoundFilter(CompoundFilter.LogicalOperator.AND, subFiltersList1);
    List<QueryFilter> subFiltersList2 = Lists.newArrayList();
    subFiltersList2.add(queryFilter3);
    subFiltersList2.add(queryFilter4);
    subFiltersList2.add(queryFilter5);
    QueryFilter queryCompoundFilter2 =
        new CompoundFilter(CompoundFilter.LogicalOperator.AND, subFiltersList2);
    List<QueryFilter> subFiltersList3 = Lists.newArrayList();
    subFiltersList3.add(queryCompoundFilter1);
    subFiltersList3.add(queryCompoundFilter2);
    QueryFilter queryCompoundFilter3 =
        new CompoundFilter(CompoundFilter.LogicalOperator.OR, subFiltersList3);
    query.setFilter(queryCompoundFilter3);
    StrBuilder queryStringBuilder = new StrBuilder();
    SqlDataSourceHelper.appendWhereClause(query, queryStringBuilder);
    assertEquals(queryStringBuilder.toString(),
        "WHERE (((ID=Salary) AND (ID>=1.0)) OR ((Fname LIKE \"Mi%\") "
        + "AND (Lname LIKE \"%SH%\") AND (Lname LIKE \"%tz\"))) ",
        queryStringBuilder.toString());

    // Check empty compound filters.
    List<QueryFilter> subFiltersList4 = Lists.newArrayList();
    QueryFilter queryCompoundFilter4 =
        new CompoundFilter(CompoundFilter.LogicalOperator.OR, subFiltersList4);
    query.setFilter(queryCompoundFilter4);
    queryStringBuilder = new StrBuilder();
    SqlDataSourceHelper.appendWhereClause(query, queryStringBuilder);
    assertEquals(queryStringBuilder.toString(), "WHERE false ");
    QueryFilter queryCompoundFilter5 =
        new CompoundFilter(CompoundFilter.LogicalOperator.AND, subFiltersList4);
    query.setFilter(queryCompoundFilter5);
    queryStringBuilder = new StrBuilder();
    SqlDataSourceHelper.appendWhereClause(query, queryStringBuilder);
    assertEquals(queryStringBuilder.toString(), "WHERE true ");

    // Check compound filter with one sub-filter.
    List<QueryFilter> subFiltersList6 = Lists.newArrayList(
        (QueryFilter) new ColumnColumnFilter(new SimpleColumn("ID"),
        new SimpleColumn("Salary"), ComparisonFilter.Operator.EQ));
    QueryFilter queryCompoundFilter6 =
        new CompoundFilter(CompoundFilter.LogicalOperator.OR, subFiltersList6);
    query.setFilter(queryCompoundFilter6);
    queryStringBuilder = new StrBuilder();
    SqlDataSourceHelper.appendWhereClause(query, queryStringBuilder);
    assertEquals(queryStringBuilder.toString(), "WHERE ((ID=Salary)) ",
        queryStringBuilder.toString());
    
    // Check "is null".
    ColumnIsNullFilter isNullFilter = new ColumnIsNullFilter(new SimpleColumn("ID"));
    query.setFilter(isNullFilter);
    queryStringBuilder = new StrBuilder();
    SqlDataSourceHelper.appendWhereClause(query, queryStringBuilder);
    assertEquals("WHERE (ID IS NULL) ", queryStringBuilder.toString());

    // Check negation.
    NegationFilter negationFilter =
        new NegationFilter(new ColumnColumnFilter(new SimpleColumn("ID"),
        new SimpleColumn("Salary"), ComparisonFilter.Operator.EQ));
    query.setFilter(negationFilter);
    queryStringBuilder = new StrBuilder();
    SqlDataSourceHelper.appendWhereClause(query, queryStringBuilder);
    assertEquals("WHERE (NOT (ID=Salary)) ", queryStringBuilder.toString());
  }

  /**
   * Tests the building of the SQL query GROUP BY clause from the Gviz query.
   */
  public void testBuildGroupByClause() {
    Query query =  new Query();
    QueryGroup queryGroup = new QueryGroup();
    queryGroup.addColumn(new SimpleColumn("ID"));
    queryGroup.addColumn(new SimpleColumn("FNAME"));
    query.setGroup(queryGroup);
    StrBuilder queryStringBuilder = new StrBuilder();
    SqlDataSourceHelper.appendGroupByClause(query, queryStringBuilder);
    assertEquals("GROUP BY `ID`, `FNAME` ", queryStringBuilder.toString());
  }

  /**
   * Tests the building of the SQL query ORDER BY clause from the Gviz query.
   */
  public void testBuildOrderByClause() {
    Query query = new Query();
    QuerySort querySort = new QuerySort();
    AggregationColumn column1 =
        new AggregationColumn(new SimpleColumn("ID"), AggregationType.COUNT);
    SimpleColumn column2 = new SimpleColumn("FNAME");
    querySort.addSort(column1, SortOrder.DESCENDING);
    querySort.addSort(column2, SortOrder.ASCENDING);
    query.setSort(querySort);
    StrBuilder queryStringBuilder = new StrBuilder();
    SqlDataSourceHelper.appendOrderByClause(query, queryStringBuilder);
    assertEquals(queryStringBuilder.toString(),
        "ORDER BY count(`ID`) DESC, `FNAME` ",
        queryStringBuilder.toString());
  }

  /**
   * Tests the building of the SQL query LIMIT and OFFSET clauses from the Gviz
   * query.
   *
   * @throws InvalidQueryException When there is an error in the query.
   */
  public void testBuildLimitAndOffsetClauses() throws InvalidQueryException {
    Query query = new Query();
    int limit = 2;
    int offset = 3;

    // Only offset, default limit.
    query.setRowOffset(offset);
    StrBuilder queryStringBuilder = new StrBuilder();
    SqlDataSourceHelper.appendLimitAndOffsetClause(query, queryStringBuilder);
    assertEquals(" OFFSET "  + offset, queryStringBuilder.toString());

    // Both limit and offset.
    query.setRowLimit(limit);
    queryStringBuilder.delete(0, queryStringBuilder.length());
    SqlDataSourceHelper.appendLimitAndOffsetClause(query, queryStringBuilder);
    assertEquals("LIMIT " + limit + " OFFSET " + offset,
        queryStringBuilder.toString());

    // Only limit.
    query = new Query();
    queryStringBuilder.delete(0, queryStringBuilder.length());
    query.setRowLimit(limit);
    SqlDataSourceHelper.appendLimitAndOffsetClause(query, queryStringBuilder);
    assertEquals("LIMIT " + limit, queryStringBuilder.toString());

    // No limit and no offset.
    query = new Query();
    queryStringBuilder.delete(0, queryStringBuilder.length());
    SqlDataSourceHelper.appendLimitAndOffsetClause(query, queryStringBuilder);
    assertEquals("", queryStringBuilder.toString());
  }

  /**
   * Tests the method buildDataTableRows.
   *
   * @throws SQLException Thrown when the connection to the database failed.
   */
  public void testBuildDataTableRows() throws SQLException {
    // Build Timestamp, Date ant Time objects for the date February 8, 2008
    // 09:01:10. Set their values in the table.
    GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    gc.set(2008, 1, 8, 9, 1, 10);
    // Set the milliseconds explicitly, otherwise the milliseconds from the
    // time the gc was initialized are used.
    gc.set(GregorianCalendar.MILLISECOND, 0);

    Date date = new Date(gc.getTimeInMillis());
    Time time = new Time(gc.getTimeInMillis());
    Timestamp timestamp = new Timestamp(gc.getTimeInMillis());

    // Create the table rows.
    List<Object> row1 =
        Lists.<Object>newArrayList(100, "Yaron", null, 'M', 1000, false, date,
            timestamp, time);
    List<Object> row2 =
        Lists.<Object>newArrayList(200, "Moran", "Bar", 'F', 2000, null, null,
            null, null);
    List<Object> row3 =
        Lists.<Object>newArrayList(300, "Shir", "Gal", 'F', null, true, null,
            null, null);
    rows.add(row1);
    rows.add(row2);
    rows.add(row3);

    // Get the mock result set for the table.
    ResultSet rs = new MockResultSet(rows, NUM_OF_COLS, labels, types);

    List<String> columnIdsList = Lists.newArrayList("id", "fname", "lname",
        "gender", "salary", "ismarried", "startdate", "timestamp", "time");

    // Get the table description using the mock result set.
    DataTable dataTable = SqlDataSourceHelper.buildColumns(rs, columnIdsList);
    assertNotNull(dataTable);

    // Make sure the number of columns in the table description is correct.
    assertEquals(NUM_OF_COLS, dataTable.getNumberOfColumns());

    // Get the columns description list.
    List <ColumnDescription> columnsDescriptionList = dataTable.getColumnDescriptions();

    // Make sure the type of each column is correct.
    assertEquals(ValueType.NUMBER, columnsDescriptionList.get(0).getType());
    assertEquals(ValueType.TEXT, columnsDescriptionList.get(1).getType());
    assertEquals(ValueType.TEXT, columnsDescriptionList.get(2).getType());
    assertEquals(ValueType.TEXT, columnsDescriptionList.get(3).getType());
    assertEquals(ValueType.NUMBER, columnsDescriptionList.get(4).getType());
    assertEquals(ValueType.BOOLEAN, columnsDescriptionList.get(5).getType());
    assertEquals(ValueType.DATE, columnsDescriptionList.get(6).getType());
    assertEquals(ValueType.DATETIME, columnsDescriptionList.get(7).getType());
    assertEquals(ValueType.TIMEOFDAY, columnsDescriptionList.get(8).getType());

    // Make sure the label of each column is correct.
    assertEquals("ID", columnsDescriptionList.get(0).getLabel());
    assertEquals("Fname", columnsDescriptionList.get(1).getLabel());
    assertEquals("Lname", columnsDescriptionList.get(2).getLabel());
    assertEquals("Gender", columnsDescriptionList.get(3).getLabel());
    assertEquals("Salary", columnsDescriptionList.get(4).getLabel());
    assertEquals("IsMarried", columnsDescriptionList.get(5).getLabel());
    assertEquals("StartDate", columnsDescriptionList.get(6).getLabel());
    assertEquals("TimeStamp", columnsDescriptionList.get(7).getLabel());
    assertEquals("Time", columnsDescriptionList.get(8).getLabel());

    // Build the table rows.
    SqlDataSourceHelper.buildRows(dataTable, rs);

    assertNotNull(dataTable);

    // Make sure the number of rows int the table is correct.
    assertEquals(3, dataTable.getNumberOfRows());

    // Make sure the type of the data table cells is correct.
    for (int i = 0; i < dataTable.getNumberOfRows(); i++) {
      assertEquals(ValueType.NUMBER,
          dataTable.getRow(i).getCell(0).getValue().getType());
      assertEquals(ValueType.TEXT,
          dataTable.getRow(i).getCell(1).getValue().getType());
      assertEquals(ValueType.TEXT,
          dataTable.getRow(i).getCell(2).getValue().getType());
      assertEquals(ValueType.TEXT,
          dataTable.getRow(i).getCell(3).getValue().getType());
      assertEquals(ValueType.NUMBER,
          dataTable.getRow(i).getCell(4).getValue().getType());
      assertEquals(ValueType.BOOLEAN,
          dataTable.getRow(i).getCell(5).getValue().getType());
      assertEquals(ValueType.DATE,
          dataTable.getRow(i).getCell(6).getValue().getType());
      assertEquals(ValueType.DATETIME,
          dataTable.getRow(i).getCell(7).getValue().getType());
      assertEquals(ValueType.TIMEOFDAY,
          dataTable.getRow(i).getCell(8).getValue().getType());
    }

    // Make sure the value of the data table cells is correct. For cells with
    // null value, check that the value equals to the null value format of the
    // specific type value.
    assertEquals(new NumberValue(100.0),
        dataTable.getRow(0).getCell(0).getValue());
    assertEquals("Yaron", dataTable.getRow(0).getCell(1).getValue().toString());
    assertEquals(new TextValue(""), dataTable.getRow(0).getCell(2).getValue());
    assertEquals("Bar", dataTable.getRow(1).getCell(2).getValue().toString());
    assertEquals("F", dataTable.getRow(1).getCell(3).getValue().toString());
    assertEquals(BooleanValue.getNullValue(),
        dataTable.getRow(1).getCell(5).getValue());
    assertEquals(NumberValue.getNullValue(),
        dataTable.getRow(2).getCell(4).getValue());
    assertEquals("true", dataTable.getRow(2).getCell(5).getValue().toString());
    assertEquals(dataTable.getRow(0).getCell(6).getValue().toString(),
        new DateValue(gc), dataTable.getRow(0).getCell(6).getValue());
    assertEquals(new DateTimeValue(gc),
        dataTable.getRow(0).getCell(7).getValue());
    assertEquals(new TimeOfDayValue(gc),
        dataTable.getRow(0).getCell(8).getValue());
    assertEquals(DateValue.getNullValue(),
        dataTable.getRow(1).getCell(6).getValue());
    assertEquals(DateTimeValue.getNullValue(),
        dataTable.getRow(2).getCell(7).getValue());
    assertEquals(TimeOfDayValue.getNullValue(),
        dataTable.getRow(2).getCell(8).getValue());
  }

  /**
   * Tests the method buildDataTableRows with en empty table.
   *
   * @throws SQLException Thrown when the connection to the database failed.
   */
  public void testBuildDataTableRowsWithEmptyTable() throws SQLException {
    // Get the mock result set for the table.
    ResultSet rs = new MockResultSet(rows, NUM_OF_COLS, labels, types);

    List<String> columnIdsList = null;

    // Get the table description using the mock result set.
    DataTable dataTable = SqlDataSourceHelper.buildColumns(rs, columnIdsList);

    assertNotNull(dataTable);

    // Build the table rows.
    SqlDataSourceHelper.buildRows(dataTable, rs);

    // Make sure there aren't any rows in the data table.
    assertEquals(0, dataTable.getNumberOfRows());
    assertEquals(0, dataTable.getRows().size());
  }
}
