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

package com.google.visualization.datasource.query.engine;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.visualization.datasource.base.ReasonType;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.base.Warning;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.ValueFormatter;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.query.AbstractColumn;
import com.google.visualization.datasource.query.AggregationColumn;
import com.google.visualization.datasource.query.ColumnLookup;
import com.google.visualization.datasource.query.DataTableColumnLookup;
import com.google.visualization.datasource.query.GenericColumnLookup;
import com.google.visualization.datasource.query.Query;
import com.google.visualization.datasource.query.QueryFilter;
import com.google.visualization.datasource.query.QueryFormat;
import com.google.visualization.datasource.query.QueryGroup;
import com.google.visualization.datasource.query.QueryLabels;
import com.google.visualization.datasource.query.QueryPivot;
import com.google.visualization.datasource.query.QuerySelection;
import com.google.visualization.datasource.query.QuerySort;
import com.google.visualization.datasource.query.ScalarFunctionColumn;
import com.google.visualization.datasource.query.SimpleColumn;

import com.ibm.icu.util.ULocale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A collection of static methods that perform the operations involved in executing a query,
 * i.e., selection, sorting, paging (limit and offset), grouping and pivoting, filtering, skipping,
 * applying labels, and custom formatting. This also takes care of calculated columns.
 * This also takes care of scalar function columns.
 *
 * @author Yoah B.D.
 * @author Yonatan B.Y.
 * @author Liron L.
 */
public final class QueryEngine {

  /**
   * Empty private constructor, to prevent initialization.
   */
  private QueryEngine() {}

  /**
   * Creates the columns structure for a new table after pivoting and grouping.
   *
   * @param groupByColumnIds The column ids to group by. This is used for the
   *     ColumnDescriptions of the first columns in the table.
   * @param columnTitles The ColumnTitles of the aggregation columns in the table, i.e., columns
   *     that are composed of pivot values and aggregations.
   * @param original The original table, from which to get the original ColumnDescriptions.
   * @param scalarFunctionColumnTitles The scalar function column titles. i.e.,
   *     columns that are composed of pivot values and scalar function column.
   *
   * @return The new TableDescription.
   */
  private static DataTable createDataTable(
      List<String> groupByColumnIds, SortedSet<ColumnTitle> columnTitles,
      DataTable original, List<ScalarFunctionColumnTitle> scalarFunctionColumnTitles) {
    DataTable result = new DataTable();
    for (String groupById : groupByColumnIds) {
      result.addColumn(original.getColumnDescription(groupById));
    }
    for (ColumnTitle colTitle : columnTitles) {
      result.addColumn(colTitle.createColumnDescription(original));
    }

    for (ScalarFunctionColumnTitle scalarFunctionColumnTitle : scalarFunctionColumnTitles) {
      result.addColumn(scalarFunctionColumnTitle.createColumnDescription(original));
    }
    return result;
  }

  /**
   * Returns the data that is the result of executing the query. The query is validated against the
   * data table before execution and an InvalidQueryException is thrown if it is invalid.
   * This function may change the given DataTable.
   *
   * @param query The query.
   * @param table The table to execute the query on.
   *
   * @return The data that is the result of executing the query.
   */
  public static DataTable executeQuery(Query query, DataTable table, ULocale locale) {
    ColumnIndices columnIndices = new ColumnIndices();
    List<ColumnDescription> columnsDescription = table.getColumnDescriptions();
    for (int i = 0; i < columnsDescription.size(); i++) {
      columnIndices.put(new SimpleColumn(columnsDescription.get(i).getId()), i);
    }

    // A map of column lookups by their list of pivot values. This is utilized in
    // the grouping and pivoting queries.
    TreeMap<List<Value>, ColumnLookup> columnLookups =
        new TreeMap<List<Value>, ColumnLookup>(GroupingComparators.VALUE_LIST_COMPARATOR);
    try {
      table = performFilter(table, query);
      table = performGroupingAndPivoting(table, query, columnIndices, columnLookups);
      table = performSort(table, query, locale);
      table = performSkipping(table, query);
      table = performPagination(table, query);

      AtomicReference<ColumnIndices> columnIndicesReference =
        new AtomicReference<ColumnIndices>(columnIndices);
      table = performSelection(table, query, columnIndicesReference, columnLookups);
      columnIndices = columnIndicesReference.get();

      table = performLabels(table, query, columnIndices);
      table = performFormatting(table, query, columnIndices, locale);
    } catch (TypeMismatchException e) {
      // Should not happen.
    }
    return table;
  }

  /**
   * Returns a table consisted of a subset of rows of the input table. 
   * We select the first out of every k rows in the table according to the 
   * skipping value in the query.
   * If there is no need to do anything, returns the original table.
   * 
   * @param table The original table.
   * @param query The query.
   *
   * @return The skipped table, or the original if no skipping is needed. 
   */
  private static DataTable performSkipping(DataTable table, Query query)
      throws TypeMismatchException {
    int rowSkipping = query.getRowSkipping();
    
    // Return the original table if no skipping is needed
    if (rowSkipping <= 1) {
      return table;
    }

    // Add the first out of every k rows of the original table to TableRow
    int numRows = table.getNumberOfRows();
    List<TableRow> relevantRows = new ArrayList<TableRow>();
    for (int rowIndex = 0; rowIndex < numRows; rowIndex += rowSkipping) {
        relevantRows.add(table.getRows().get(rowIndex));
    }
    
    // Create a table out of the TableRow array
    DataTable newTable = new DataTable();
    newTable.addColumns(table.getColumnDescriptions());
    newTable.addRows(relevantRows);
    
    return newTable;
  }

  /**
   * Returns a paginated table, based on the row limit and offset parameters.
   * If there is no need to do anything, returns the original table.
   *
   * @param table The original table.
   * @param query The query.
   *
   * @return The paginated table, or the original if no pagination is needed.
   */
  private static DataTable performPagination(DataTable table, Query query)
      throws TypeMismatchException {
    int rowOffset = query.getRowOffset();
    int rowLimit = query.getRowLimit();

    // Return the original table if no pagination is needed
    if (((rowLimit == -1) || (table.getRows().size() <= rowLimit)) && (rowOffset == 0)) {
      return table;
    }
    int numRows = table.getNumberOfRows();
    int fromIndex = Math.max(0, rowOffset);
    int toIndex = (rowLimit == -1) ? numRows : Math.min(numRows, rowOffset + rowLimit);

    List<TableRow> relevantRows = table.getRows().subList(fromIndex, toIndex);
    DataTable newTable = new DataTable();
    newTable.addColumns(table.getColumnDescriptions());
    newTable.addRows(relevantRows);

    if (toIndex < numRows) { // Data truncated
      Warning warning = new Warning(ReasonType.DATA_TRUNCATED, "Data has been truncated due to user"
          + "request (LIMIT in query)");
      newTable.addWarning(warning);
    }

    return newTable;
  }

  /**
   * Returns a table sorted according to the query's sort.
   * The returned table has the same rows as the original table.
   *
   * @param table The table to sort.
   * @param query The query.
   *
   * @return The sorted table.
   */
  private static DataTable performSort(DataTable table, Query query, ULocale locale) {
    if (!query.hasSort()) {
      return table;
    }
    QuerySort sortBy = query.getSort();
    // A table description column lookup is enough because sorting by a column
    // that has multiple matching columns after pivoting is impossible. For example,
    // it is impossible to sort by an aggregation column when there is a pivot.
    DataTableColumnLookup columnLookup = new DataTableColumnLookup(table);
    TableRowComparator comparator = new TableRowComparator(sortBy, locale, columnLookup);
    Collections.sort(table.getRows(), comparator);
    return table;
  }

  /**
   * Returns a table that has only the rows from the given table that match the filter
   * provided by a query.
   *
   * @param table The table to filter.
   * @param query The query.
   *
   * @return The filtered table.
   */
  private static DataTable performFilter(DataTable table, Query query)
      throws TypeMismatchException {
    if (!query.hasFilter()) {
      return table;
    }

    List<TableRow> newRowList = Lists.newArrayList();
    QueryFilter filter = query.getFilter();
    for (TableRow inputRow : table.getRows()) {
      if (filter.isMatch(table, inputRow)) {
        newRowList.add(inputRow);
      }
    }
    table.setRows(newRowList);
    return table;
  }

  /**
   * Returns a table that has only the columns from the given table that are specified by
   * the query.
   *
   * @param table The table from which to select.
   * @param query The query.
   * @param columnIndicesReference A reference to a ColumnIndices instance, so that
   *     this function can change the internal ColumnIndices.
   * @param columnLookups A map of column lookups by their list of pivot values.
   *
   * @return The table with selected columns only.
   */
  private static DataTable performSelection(DataTable table, Query query,
      AtomicReference<ColumnIndices> columnIndicesReference,
      Map<List<Value>, ColumnLookup> columnLookups) throws TypeMismatchException {
    if (!query.hasSelection()) {
      return table;
    }

    ColumnIndices columnIndices = columnIndicesReference.get();

    List<AbstractColumn> selectedColumns = query.getSelection().getColumns();
    List<Integer> selectedIndices = Lists.newArrayList();

    // Build the new table description, and update columnIndices
    List<ColumnDescription> oldColumnDescriptions = table.getColumnDescriptions();
    List<ColumnDescription> newColumnDescriptions = Lists.newArrayList();
    ColumnIndices newColumnIndices = new ColumnIndices();
    int currIndex = 0;
    for (AbstractColumn col : selectedColumns) {
      // If the query has pivoting, then AggregationColumns in the SELECT are
      // discarded, since they are only there to control the pivoting.
      List<Integer> colIndices = columnIndices.getColumnIndices(col);
      selectedIndices.addAll(colIndices);
      // If the selected column does not exist in the columnIndices, then it is
      // a scalar function column that was not in the original table, and was not
      // calculated in the grouping and pivoting stage.
      if (colIndices.size() == 0) {
        newColumnDescriptions.add(new ColumnDescription(col.getId(),
            col.getValueType(table),
            ScalarFunctionColumnTitle.getColumnDescriptionLabel(table, col)));
        newColumnIndices.put(col, currIndex++);
      } else {
        for (int colIndex : colIndices) {
          newColumnDescriptions.add(oldColumnDescriptions.get(colIndex));
          newColumnIndices.put(col, currIndex++);
        }
      }
    }
    columnIndices = newColumnIndices;
    columnIndicesReference.set(columnIndices);

    DataTable result = new DataTable();
    result.addColumns(newColumnDescriptions);

    // Calculate the values in the data table rows.
    for (TableRow sourceRow : table.getRows()) {
      TableRow newRow = new TableRow();
      for (AbstractColumn col : selectedColumns) {
        boolean wasFound = false;
        Set<List<Value>> pivotValuesSet = columnLookups.keySet();
        for (List<Value> values : pivotValuesSet) {
          // If the current column-lookup contains the current column and it is
          // either a column that contains aggregations or a column that
          // contains only group-by columns and was not yet found, get its value
          // in the current row. Otherwise continue. If the column contains
          // only group-by columns it should appear only once, even though
          // it may appear in many column lookups.
          if (columnLookups.get(values).containsColumn(col)
              && ((col.getAllAggregationColumns().size() != 0) || !wasFound)) {
            wasFound = true;
            newRow.addCell(sourceRow.getCell(columnLookups.get(values).getColumnIndex(col)));
          }
        }
        // If the column was not found in any of the column lookups
        // calculate its value (e.g., scalar function column that was not
        // calculated in a previous stage).
        if (!wasFound) {
          DataTableColumnLookup lookup = new DataTableColumnLookup(table);
          newRow.addCell(col.getCell(lookup, sourceRow));
        }
      }
      result.addRow(newRow);
    }
    return result;
  }

  /**
   * Returns true if the query has aggregation columns and the table is not
   * empty.
   *
   * @param query The given query.
   *
   * @return true if the query has aggregation columns and the table is not
   *     empty.
   */
  private static boolean queryHasAggregation(Query query) {
    return (query.hasSelection()
        && !query.getSelection().getAggregationColumns().isEmpty());
  }

  /**
   * Returns the result of performing the grouping (and pivoting) operations
   * on the given table, using the information provided in the query's group
   * and pivot.
   *
   * The new table generated has columns as follows where A is the number of group-by columns,
   * B is the number of combinations of values of pivot-by columns, and X is the number of 
   * aggregations requested:
   * - Columns 1..A are the original group-by columns, in the order they are
   * given in the group-by list.
   * - Columns (A+1)..B are pivot and aggregation columns, where each
   * column's id is composed of values of the pivot-by columns in the
   * original table, and an aggregation column, with separators between them.
   * 
   * Note that the aggregations requested can be all on the same
   * aggregation column or on different aggregation columns. To this
   * mechanism, it doesn't matter.
   *
   * There is a row for each combination of the values of the group-by columns.
   *
   * The value in the cell at row X and column Y is the result of the requested
   * aggregation type described in Y on the set of values in the aggregation
   * column (also described in Y) for which the values of the group-by columns
   * are as determined by X and the values of the pivot-by columns are as
   * determined by the column.
   *
   * @param table The original table.
   * @param query The query.
   * @param columnIndices A map, in which this method sets the indices
   *     of the new columns, if grouping is performed, and then any
   *     previous values in it are cleared. If grouping is not performed, it is
   *     left as is.
   *
   * @return The new table, after grouping and pivoting was performed.
   */
  private static DataTable performGroupingAndPivoting(DataTable table, Query query,
      ColumnIndices columnIndices, TreeMap<List<Value>, ColumnLookup> columnLookups)
      throws TypeMismatchException {
    if (!queryHasAggregation(query) || (table.getNumberOfRows() == 0)) {
      return table;
    }
    QueryGroup group = query.getGroup();
    QueryPivot pivot = query.getPivot();
    QuerySelection selection = query.getSelection();

    List<String> groupByIds = Lists.newArrayList();
    if (group != null) {
      groupByIds = group.getColumnIds();
    }

    List<String> pivotByIds = Lists.newArrayList();
    if (pivot != null) {
      pivotByIds = pivot.getColumnIds(); // contained in groupByIds
    }

    List<String> groupAndPivotIds = Lists.newArrayList(groupByIds);
    groupAndPivotIds.addAll(pivotByIds);

    List<AggregationColumn> tmpColumnAggregations = selection.getAggregationColumns();
    List<ScalarFunctionColumn> selectedScalarFunctionColumns = selection.getScalarFunctionColumns();
    
    // Remove duplicates from tmpColumnAggregations, creating columnAggregations:
    List<AggregationColumn> columnAggregations =
      Lists.newArrayListWithExpectedSize(tmpColumnAggregations.size());
    for (AggregationColumn aggCol : tmpColumnAggregations) {
      if (!columnAggregations.contains(aggCol)) {
        columnAggregations.add(aggCol);
      }
    }
    
    List<String> aggregationIds = Lists.newArrayList();
    for (AggregationColumn col : columnAggregations) {
      aggregationIds.add(col.getAggregatedColumn().getId());
    }

    List<ScalarFunctionColumn> groupAndPivotScalarFunctionColumns = Lists.newArrayList();
    if (group != null) {
      groupAndPivotScalarFunctionColumns.addAll(group.getScalarFunctionColumns());
    }
    if (pivot != null) {
      groupAndPivotScalarFunctionColumns.addAll(pivot.getScalarFunctionColumns());
    }

    List<ColumnDescription> newColumnDescriptions = Lists.newArrayList();
    newColumnDescriptions.addAll(table.getColumnDescriptions());

    // Add to the table description the scalar function columns included in the
    // group and pivot. The groups of rows are defined according to the
    // values of those columns, and so it is necessary to add them before the
    // calculations of the groups, pivots and aggregations.
    for (ScalarFunctionColumn column : groupAndPivotScalarFunctionColumns) {
      newColumnDescriptions.add(new ColumnDescription(column.getId(),
          column.getValueType(table),
          ScalarFunctionColumnTitle.getColumnDescriptionLabel(table, column)));
    }

    DataTable tempTable = new DataTable();
    tempTable.addColumns(newColumnDescriptions);

    // Calculate the values of the added scalar function columns in each row.
    DataTableColumnLookup lookup = new DataTableColumnLookup(table);
    for (TableRow sourceRow : table.getRows()) {
      TableRow newRow = new TableRow();
      for (TableCell sourceCell : sourceRow.getCells()) {
        newRow.addCell(sourceCell);
      }
      for (ScalarFunctionColumn column : groupAndPivotScalarFunctionColumns) {
        newRow.addCell(new TableCell(column.getValue(lookup, sourceRow)));
      }
      try {
        tempTable.addRow(newRow);
      } catch (TypeMismatchException e) {
        // Should not happen, given that the original table is OK.
      }
    }
    table = tempTable;

    // Calculate the aggregations.
    TableAggregator aggregator = new TableAggregator(groupAndPivotIds,
        Sets.newHashSet(aggregationIds), table);
    Set<AggregationPath> paths = aggregator.getPathsToLeaves();

    // These variables will hold the "titles" of the rows and columns.
    // They are TreeSets because their order matters.
    SortedSet<RowTitle> rowTitles =
        Sets.newTreeSet(GroupingComparators.ROW_TITLE_COMPARATOR);
    SortedSet<ColumnTitle> columnTitles = Sets.newTreeSet(
        GroupingComparators.getColumnTitleDynamicComparator(columnAggregations));

    // A tree set containing all pivot value lists (the set is for the
    // uniqueness and the tree for the order).
    TreeSet<List<Value>> pivotValuesSet =
        Sets.newTreeSet(GroupingComparators.VALUE_LIST_COMPARATOR);
    // This MetaTable holds all the data in the table, this data is then
    // dumped into the real table.
    MetaTable metaTable = new MetaTable();
    for (AggregationColumn columnAggregation : columnAggregations) {
      for (AggregationPath path : paths) {

        // A ColumnTitle is composed of all the values for the pivot-by
        // columns, and a ColumnAggregation. That is why it is necessary to iterate over all
        // ColumnAggregations and create a ColumnTitle for each one.
        List<Value> originalValues = path.getValues();

        // Separate originalValues into the rowValues and columnValues. The
        // rowValues are the values of the group-by columns and the columnValues
        // are the values of the pivot-by columns.
        List<Value> rowValues = originalValues.subList(0, groupByIds.size());
        RowTitle rowTitle = new RowTitle(rowValues);
        rowTitles.add(rowTitle);

        List<Value> columnValues = originalValues.subList(groupByIds.size(), originalValues.size());
        pivotValuesSet.add(columnValues);

        ColumnTitle columnTitle = new ColumnTitle(columnValues,
            columnAggregation, (columnAggregations.size() > 1));
        columnTitles.add(columnTitle);
        metaTable.put(rowTitle, columnTitle, new TableCell(aggregator.getAggregationValue(path,
            columnAggregation.getAggregatedColumn().getId(),
            columnAggregation.getAggregationType())));
      }
    }

    // Create the scalar function column titles for the scalar function columns
    // that contain aggregations.
    List<ScalarFunctionColumnTitle> scalarFunctionColumnTitles =
        Lists.newArrayList();
    for (ScalarFunctionColumn scalarFunctionColumn :
        selectedScalarFunctionColumns) {
      if (scalarFunctionColumn.getAllAggregationColumns().size() != 0) {
        for (List<Value> columnValues : pivotValuesSet) {
          scalarFunctionColumnTitles.add(new ScalarFunctionColumnTitle(columnValues,
              scalarFunctionColumn));
        }
      }
    }

    // Create the new table description.
    DataTable result = createDataTable(groupByIds, columnTitles, table, scalarFunctionColumnTitles);
    List<ColumnDescription> colDescs = result.getColumnDescriptions();

    // Fill the columnIndices and columnLookups parameters for the group-by
    // columns and the aggregation columns.
    columnIndices.clear();
    int columnIndex = 0;
    if (group != null) {
      List<Value> empytListOfValues = Lists.newArrayList();
      columnLookups.put(empytListOfValues, new GenericColumnLookup());
      for (AbstractColumn column : group.getColumns()) {
        columnIndices.put(column, columnIndex);
        if (!(column instanceof ScalarFunctionColumn)) {
          ((GenericColumnLookup) columnLookups.get(empytListOfValues)).put(column, columnIndex);
          for (List<Value> columnValues : pivotValuesSet) {
            if (!columnLookups.containsKey(columnValues)) {
              columnLookups.put(columnValues, new GenericColumnLookup());
            }
            ((GenericColumnLookup) columnLookups.get(columnValues)).put(column, columnIndex);
          }
        }
        columnIndex++;
      }
    }

    for (ColumnTitle title : columnTitles) {
      columnIndices.put(title.aggregation, columnIndex);
      List<Value> values = title.getValues();
      if (!columnLookups.containsKey(values)) {
        columnLookups.put(values, new GenericColumnLookup());
      }
      ((GenericColumnLookup) columnLookups.get(values)).put(title.aggregation, columnIndex);
      columnIndex++;
    }

    // Dump the data from the metaTable to the result DataTable.
    for (RowTitle rowTitle : rowTitles) {
      TableRow curRow = new TableRow();
      // Add the group-by columns cells.
      for (Value v : rowTitle.values) {
        curRow.addCell(new TableCell(v));
      }
      Map<ColumnTitle, TableCell> rowData = metaTable.getRow(rowTitle);
      int i = 0;
      // Add the aggregation columns cells.
      for (ColumnTitle colTitle : columnTitles) {
        TableCell cell = rowData.get(colTitle);
        curRow.addCell((cell != null) ? cell : new TableCell(
            Value.getNullValueFromValueType(colDescs.get(i + rowTitle.values.size()).getType())));
        i++;
      }
      // Add the scalar function columns cells.
      for (ScalarFunctionColumnTitle columnTitle : scalarFunctionColumnTitles) {
        curRow.addCell(new TableCell(columnTitle.scalarFunctionColumn.
            getValue(columnLookups.get(columnTitle.getValues()), curRow)));
      }
      result.addRow(curRow);
    }

    // Fill the columnIndices and columnLookups parameters for the scalar
    // function column titles. This must be done after the calculation of the values
    // in the scalar function column cells, or else the scalar function columns
    // will not calculate their value recursively, but return the current value.
    // See the logic of the getValue() method in ScalarFunctionColumn.
    for (ScalarFunctionColumnTitle scalarFunctionColumnTitle
        : scalarFunctionColumnTitles) {
      columnIndices.put(scalarFunctionColumnTitle.scalarFunctionColumn,
          columnIndex);
      List<Value> values = scalarFunctionColumnTitle.getValues();
      if (!columnLookups.containsKey(values)) {
        columnLookups.put(values, new GenericColumnLookup());
      }
      ((GenericColumnLookup) columnLookups.get(values)).put(
          scalarFunctionColumnTitle.scalarFunctionColumn, columnIndex);
      columnIndex++;
    }

    return result;
  }

  /**
   * Apply labels to columns as specified in the user query.
   * If a column is specified in the query, but is not part of the data table,
   * this is still a valid situation, and the "invalid" column id is ignored.
   *
   * @param table The original table.
   * @param query The query.
   * @param columnIndices The map of columns to indices in the table.
   *
   * @return The table with labels applied.
   */
  private static DataTable performLabels(DataTable table, Query query,
      ColumnIndices columnIndices) {

    if (!query.hasLabels()) {
      return table;
    }

    QueryLabels labels = query.getLabels();

    List<ColumnDescription> columnDescriptions = table.getColumnDescriptions();

    for (AbstractColumn column : labels.getColumns()) {
      String label = labels.getLabel(column);
      List<Integer> indices = columnIndices.getColumnIndices(column);
      if (indices.size() == 1) {
        columnDescriptions.get(indices.get(0)).setLabel(label);
      } else {
        String columnId = column.getId(); // Without pivot values.
        for (int i : indices) {
          ColumnDescription colDesc = columnDescriptions.get(i);
          String colDescId = colDesc.getId(); // Includes pivot values.
          String specificLabel =
              colDescId.substring(0, colDescId.length() - columnId.length()) + label;
          columnDescriptions.get(i).setLabel(specificLabel);
        }

      }
    }
    return table;
  }

  /**
   * Add column formatters according to a given patterns list. Namely,
   * a visualization gadget can send a map of patterns by column ids. The following
   * method builds the appropriate formatters for these patterns.
   * An illegal pattern is recorded for later sending of a warning.
   *
   * @param table The original table.
   * @param query The query.
   * @param columnIndices The map of columns to indices in the table.
   * @param locale The locale by which to format.
   *
   * @return The table with formatting applied.
   */
  private static DataTable performFormatting(DataTable table, Query query,
      ColumnIndices columnIndices, ULocale locale) {
    if (!query.hasUserFormatOptions()) {
      return table;
    }

    QueryFormat queryFormat = query.getUserFormatOptions();
    List<ColumnDescription> columnDescriptions = table.getColumnDescriptions();
    Map<Integer, ValueFormatter> indexToFormatter = Maps.newHashMap();
    for (AbstractColumn col : queryFormat.getColumns()) {
      String pattern = queryFormat.getPattern(col);
      List<Integer> indices = columnIndices.getColumnIndices(col);
      boolean allSucceeded = true;
      for (int i : indices) {
        ColumnDescription colDesc = columnDescriptions.get(i);
        ValueFormatter f = ValueFormatter.createFromPattern(colDesc.getType(), pattern, locale);
        if (f == null) {
          allSucceeded = false;
        } else {
          indexToFormatter.put(i, f);
          table.getColumnDescription(i).setPattern(pattern); // May override datasource pattern.
        }
      }
      if (!allSucceeded) {
        Warning warning = new Warning(ReasonType.ILLEGAL_FORMATTING_PATTERNS,
            "Illegal formatting pattern: " + pattern + " requested on column: " + col.getId());
        table.addWarning(warning);
      }
    }

    for (TableRow row : table.getRows()) {
      for (int col : indexToFormatter.keySet()) {
        TableCell cell = row.getCell(col);
        Value value = cell.getValue();
        ValueFormatter formatter = indexToFormatter.get(col);
        String formattedValue = formatter.format(value);
        cell.setFormattedValue(formattedValue);
      }
    }
    return table;
  }
}
