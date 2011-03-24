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

package com.google.visualization.datasource.datatable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.base.Warning;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;

import com.ibm.icu.util.ULocale;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A table of data, arranged in typed columns.
 *
 * An instance of this class is the result of a request to a data source. A <code>DataTable</code>
 * can be rendered in many ways: JSON, HTML, CSV (see
 * {@link com.google.visualization.datasource.render}), and can be manipulated using queries (see
 * {@link com.google.visualization.datasource.query.Query} and
 * {@link com.google.visualization.datasource.query.engine.QueryEngine}).
 * 
 * A table contains any number of typed columns (see (@link ColumnDescription}) each with an id and
 * a label, and any number of rows. Each row must have as many cells as there are columns
 * in the table, and the types of values in the cells must match the types of the columns.
 * Each cell contains, as well as the typed value, a formatted value of type string, used for
 * display purposes.
 * Also, you can use the custom properties mechanism to hold any other data you require. There are
 * custom properties on every cell, row, column, and on the entire table.
 *
 * @author Yoah B.D.
 */
public class DataTable {

  /**
   * Column descriptions.
   */
  private List<ColumnDescription> columns;

  /**
   * Map from a column to its index in the columns list.
   */
  private Map<String, Integer> columnIndexById;

  /**
   * The list of returned rows.
   */
  private List<TableRow> rows;

  /**
   * Custom properties for this table.
   */
  private Map<String, String> customProperties = null;

  /**
   * A list of warnings.
   */
  private List<Warning> warnings;
  
  /**
   * The user locale, used to create localized messages.
   */
  private ULocale localeForUserMessages = null;

  /**
   * Create a new empty result.
   */
  public DataTable() {
    columns = Lists.newArrayList();
    columnIndexById = Maps.newHashMap();
    rows = Lists.newArrayList();
    warnings = Lists.newArrayList();
  }

  /**
   * Adds a single row to the end of the result. Throws a TypeMismatchException if the row's cells
   * do not match the current columns. If the row is too short, i.e., has too few cells, then the
   * remaining columns are filled with null values (the given row is changed).
   *
   * @param row The row of values.
   *
   * @throws TypeMismatchException Thrown if the values in the cells do not match the columns.
   */
  public void addRow(TableRow row) throws TypeMismatchException {
    List<TableCell> cells = row.getCells();
    if (cells.size() > columns.size()) {
      throw new TypeMismatchException("Row has too many cells. Should be at most of size: " +
          columns.size());
    }
    for (int i = 0; i < cells.size(); i++) {
      if (cells.get(i).getType() != columns.get(i).getType()) {
        throw new TypeMismatchException("Cell type does not match column type, at index: " + i +
            ". Should be of type: " + columns.get(i).getType().toString());
      }
    }
    for (int i = cells.size(); i < columns.size(); i++) {
      row.addCell(new TableCell(Value.getNullValueFromValueType(columns.get(i).getType())));
    }

    rows.add(row);
  }

  /**
   * A convenience method for creating a row directly from its cell values and
   * adding it to the data table.
   *
   * @param values The row values.
   * @throws TypeMismatchException Thrown if a value does not match its
   * corresponding column.
   */
  public void addRowFromValues(Object... values) throws TypeMismatchException {
    Iterator<ColumnDescription> columnIt = columns.listIterator();
    int i = 0;
    TableRow row = new TableRow();

    while (i < values.length && columnIt.hasNext()) {
      ColumnDescription colDesc = columnIt.next();
      row.addCell(colDesc.getType().createValue(values[i]));
      i++;
    }
    addRow(row);
  }

  /**
   * Adds a collection of rows to the end of the result.
   *
   * @param rowsToAdd The row collection.
   */
  public void addRows(Collection<TableRow> rowsToAdd) throws TypeMismatchException {
    for (TableRow row : rowsToAdd) {
      addRow(row);
    }
  }

  /**
   * Sets a collection of rows after clearing any current rows.
   *
   * @param rows The row collection.
   */
  public void setRows(Collection<TableRow> rows) throws TypeMismatchException {
    this.rows.clear();
    addRows(rows);
  }

  /**
   * Returns the list of all table rows.
   *
   * @return The list of all table rows.
   */
  public List<TableRow> getRows() {
    return rows;
  }

  /**
   * Returns the row at the given index.
   *
   * @param rowIndex the index of the requested row.
   *
   * @return The row at the given index.
   */
  public TableRow getRow(int rowIndex) {
    return rows.get(rowIndex);
  }

  /**
   * Returns the number of rows in this data table.
   *
   * @return The number of rows.
   */
  public int getNumberOfRows() {
    return rows.size();
  }

  /**
   * Returns the number of columns in this data table.
   *
   * @return The number of columns.
   */
  public int getNumberOfColumns() {
    return columns.size();
  }

  /**
   * Returns the list of all column descriptions.
   *
   * @return The list of all column descriptions. The returned list is
   *     immutable.
   */
  public List<ColumnDescription> getColumnDescriptions() {
    return ImmutableList.copyOf(columns);
  }

  /**
   * Returns the column description of a column by its index.
   *
   * @param colIndex The column index.
   *
   * @return The column description.
   */
  public ColumnDescription getColumnDescription(int colIndex) {
    return columns.get(colIndex);
  }

  /**
   * Returns the column description of a column by it's id.
   *
   * @param columnId The id of the column.
   *
   * @return The column description of the specified column.
   */
  public ColumnDescription getColumnDescription(String columnId) {
    return columns.get(getColumnIndex(columnId));
  }

  /**
   * Returns the list of all cells of a certain column, by the column index.
   * Note: This is the most naive implementation, that for each request
   * to this method just creates a new List of the needed cells.
   *
   * @param columnIndex The index of the requested column.
   *
   * @return The list of all cells of the requested column.
   */
  public List<TableCell> getColumnCells(int columnIndex) {
    List<TableCell> colCells =
        Lists.newArrayListWithCapacity(getNumberOfRows());

    for (TableRow row : getRows()) {
      colCells.add(row.getCell(columnIndex));
    }
    return colCells;
  }

  /**
   * Add a column to the table.
   *
   * @param columnDescription The column's description.
   */
  public void addColumn(ColumnDescription columnDescription) {
    String columnId = columnDescription.getId();
    if (columnIndexById.containsKey(columnId)) {
      throw new RuntimeException("Column Id [" + columnId + "] already in table description");
    }

    columnIndexById.put(columnId, columns.size());
    columns.add(columnDescription);
    for (TableRow row : rows) {
      row.addCell(new TableCell(Value.getNullValueFromValueType(columnDescription.getType())));
    }
  }

  /**
   * Adds columns to the table.
   *
   * @param columnsToAdd The columns to add.
   */
  public void addColumns(Collection<ColumnDescription> columnsToAdd) {
    for (ColumnDescription column : columnsToAdd) {
      addColumn(column);
    }
  }

  /**
   * Returns the column index in the columns of a row (first is zero).
   *
   * @param columnId The id of the column.
   *
   * @return The column index in the columns of a row (first is zero).
   */
  public int getColumnIndex(String columnId) {
    return columnIndexById.get(columnId);
  }

  /**
   * Returns the list of all cells of a certain column, by the column Id.
   *
   * @param columnId The id of the requested column.
   *
   * @return The list of all cells of the requested column.
   */
  public List<TableCell> getColumnCells(String columnId) {
    return getColumnCells(getColumnIndex(columnId));
  }

  /**
   * Returns the cell at the specified row and column indexes.
   *
   * @param rowIndex The row index.
   * @param colIndex The column index.
   *
   * @return The cell.
   */
  public TableCell getCell(int rowIndex, int colIndex) {
    return getRow(rowIndex).getCell(colIndex);
  }
  
  /**
   * Replaces an existing cell at a specified position in this table with the specified cell.
   * The value type of the new cell must match the type of the existing one.
   *  
   * @param rowIndex The row index.
   * @param colIndex The column index.
   * @param cell The cell to be stored at the specified position.
   * 
   * @return The cell that was replaced.
   * 
   * @throws TypeMismatchException Thrown if the new cell value type doesn't match
   *    the table column value type.
   * @throws IndexOutOfBoundsException Thrown if the position is out of range.
   */
  public TableCell setCell(int rowIndex, int colIndex, TableCell cell)
      throws TypeMismatchException, IndexOutOfBoundsException {
    TableRow row = rows.get(rowIndex);
    if (!row.getCell(colIndex).getType().equals(cell.getType())) {
      throw new TypeMismatchException("New cell value type does not match expected value type." +
          " Expected type: " + row.getCell(colIndex).getType() +
          " but was: " + cell.getType().toString());
    }
    return row.setCell(colIndex, cell);
  }

  /**
   * Returns the value in the cell at the specified row and column indexes.
   *
   * @param rowIndex The row index.
   * @param colIndex The column index.
   *
   * @return The value in the cell.
   */
  public Value getValue(int rowIndex, int colIndex) {
    return getCell(rowIndex, colIndex).getValue();
  }

  /**
   * Returns the list of warnings in this table. The list returned is immutable.
   *
   * @return The list of warnings in this table.
   */
  public List<Warning> getWarnings() {
    return ImmutableList.copyOf(warnings);
  }

  /**
   * Returns a sorted list of distinct table cells in the specified column.
   * The cells are sorted according to the given comparator in ascending order.
   *
   * @param columnIndex The index of the required column.
   * @param comparator A Comparator for TableCells.
   *
   * @return A sorted list of distinct table cells in the specified column.
   */
  public List<TableCell> getColumnDistinctCellsSorted(int columnIndex,
      Comparator<TableCell> comparator) {
    // Get only the distinct Table cells (based on the comparator)
    Set<TableCell> colCells = Sets.newTreeSet(comparator);
    for (TableCell cell : getColumnCells(columnIndex)) {
      colCells.add(cell);
    }

    // Return the list of distinct Table cells sorted (based on the comparator).
    return Ordering.from(comparator).sortedCopy(colCells);
  }

  /**
   * Returns an ordered list of all the distinct values of a single column.
   *
   * @param columnIndex The index of the requested column.
   *
   * @return An ordered list of all the distinct values of a single
   *     column.
   */
  List<Value> getColumnDistinctValues(int columnIndex) {
    Set<Value> values = Sets.newTreeSet();
    for (TableRow row : getRows()) {
      values.add(row.getCell(columnIndex).getValue());
    }
    return Lists.newArrayList(values);
  }

  /**
   * Adds a warning.
   *
   * @param warning The warning to add.
   */
  public void addWarning(Warning warning) {
    warnings.add(warning);
  }

  /**
   * Returns an ordered list of all the distinct values of a single column.
   *
   * @param columnId The id of the requested column.
   *
   * @return An ordered list of all the distinct values of a single
   * column.
   */
  List<Value> getColumnDistinctValues(String columnId) {
    return getColumnDistinctValues(getColumnIndex(columnId));
  }

  /**
   * Returns whether or not the table contains a column named columnId.
   *
   * @param columnId The column id to check.
   *
   * @return True if columnId exists in this table, false otherwise.
   */
  public boolean containsColumn(String columnId) {
    return columnIndexById.containsKey(columnId);
  }

  /**
   * Check that all the cols in colIds are in the data.
   *
   * @param colIds A list of column ids.
   *
   * @return True if all the columns are in the data table, false otherwise.
   */
  public boolean containsAllColumnIds(Collection<String> colIds) {

    for (String id : colIds) {
      if (!containsColumn(id)) {
        return false;
      }
    }
    return true;

  }

  /**
   * Returns a data table with str as the content of its single cell.
   *
   * @param str The cell's content
   * @return A data table with str as the content of its single cell.
   */
  public static DataTable createSingleCellTable(String str) {
    DataTable dataTable = new DataTable();
    ColumnDescription colDesc = new ColumnDescription("SingleCellTable",
        ValueType.TEXT, "");
    dataTable.addColumn(colDesc);
    TableRow row = new TableRow();
    row.addCell(new TableCell(str));

    try {
      dataTable.addRow(row);
    } catch (TypeMismatchException e) {
      // Should not happen. We control the column description.
    }

    return dataTable;
  }

  /**
   * Returns a new data table, with the same data and metadata as this one.
   * Any change to the returned table should not change this table and vice
   * versa. This is a deep clone.
   *
   * @return The cloned data table.
   */
  @Override
  public DataTable clone() {
    DataTable result = new DataTable();

    for (ColumnDescription column : columns) {
      result.addColumn(column.clone());
    }
    try {
      for (TableRow row : rows) {
        result.addRow(row.clone());
      }
    } catch (TypeMismatchException e) {
      // Should not happen. We assume this table is valid.
    }
    if (customProperties != null) {
      result.customProperties = Maps.newHashMap();
      for (Map.Entry<String, String> entry : customProperties.entrySet()) {
        result.customProperties.put(entry.getKey(), entry.getValue());
      }
    }
    result.warnings = Lists.newArrayList();
    for (Warning warning : warnings) {
      result.warnings.add(warning);
    }
    result.setLocaleForUserMessages(localeForUserMessages);

    return result;
  }

  /**
   * Retrieves a custom property. Returns null if it does not exist.
   *
   * @param key The property key.
   *
   * @return The property value, or null if it does not exist.
   */
  public String getCustomProperty(String key) {
    if (customProperties == null) {
      return null;
    }
    if (key == null) {
      throw new RuntimeException("Null keys are not allowed.");
    }
    return customProperties.get(key);
  }

  /**
   * Sets a custom property.
   *
   * @param propertyKey The property key.
   * @param propertyValue The property value.
   */
  public void setCustomProperty(String propertyKey, String propertyValue) {
    if (customProperties == null) {
      customProperties = Maps.newHashMap();
    }
    if ((propertyKey == null) || (propertyValue == null)) {
      throw new RuntimeException("Null keys/values are not allowed.");
    }
    customProperties.put(propertyKey, propertyValue);
  }

  /**
   * Returns an immutable map of the custom properties.
   *
   * @return An immutable map of the custom properties.
   */
  public Map<String, String> getCustomProperties() {
    if (customProperties == null) {
      return Collections.emptyMap();
    }
    return Collections.unmodifiableMap(customProperties);
  }

  /**
   * Returns a string representation of the data table.
   * Useful mainly for debugging.
   *
   * @return A string representation of the data table.
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();

    for (int rowIndex = 0; rowIndex < rows.size(); rowIndex++) {
      TableRow tableRow = rows.get(rowIndex);
      for (int cellIndex = 0; cellIndex < tableRow.getCells().size(); cellIndex++) {
        TableCell tableCell = tableRow.getCells().get(cellIndex);
        sb.append(tableCell.toString());
        if (cellIndex < tableRow.getCells().size() - 1) {
          sb.append(",");
        }
      }
      if (rowIndex < rows.size() - 1) {
        sb.append("\n");
      }
    }

    return sb.toString();
  }

  /**
   * Sets the user locale for creating localized messages.
   * @param userLocale the user locale.
   */
  public void setLocaleForUserMessages(ULocale localeForUserMessges) {
    this.localeForUserMessages = localeForUserMessges;
  }
  
  /**
   * Returns the locale to use to create localized user messages.
   * @return The locale for user messages.
   */
  public ULocale getLocaleForUserMessages() {
    return localeForUserMessages;
  }
}
