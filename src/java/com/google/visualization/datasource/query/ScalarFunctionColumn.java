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

package com.google.visualization.datasource.query;

import com.google.common.collect.Lists;
import com.google.visualization.datasource.base.InvalidQueryException;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.scalarfunction.ScalarFunction;

import org.apache.commons.lang.text.StrBuilder;

import java.util.List;

/**
 * A scalar function column (e.g. year(Date1)). The values in a scalar function column are the
 * result of executing the function on the columns that are given as parameters, so for example
 * year(Date1) column values will have the year of the corresponding value in Date1 column.
 *
 * @author Liron L.
 */
public class ScalarFunctionColumn extends AbstractColumn {

  /**
   * A separator between function type and the columns on which it operates. 
   * Used for creating the column ID.
   */
  public static final String COLUMN_FUNCTION_TYPE_SEPARATOR = "_";

  /**
   * When creating the ID of the column, this is used as a separator between the columns
   * on which the function is performed.
   */
  public static final String COLUMN_COLUMN_SEPARATOR = ",";

  /**
   * A list of the columns on which the function is performed.
   */
  private List<AbstractColumn> columns;

  /**
   * The function performed.
   */
  private ScalarFunction scalarFunction;

  /**
   * Creates a new instance of this class with the given columns list and
   * function type.
   *
   * @param columns The list of columns on which the function is performed.
   * @param scalarFunction The function type.
   */
  public ScalarFunctionColumn(List<AbstractColumn> columns,
      ScalarFunction scalarFunction) {
    this.columns = columns;
    this.scalarFunction = scalarFunction;
  }

  /**
   * Returns the ID of the scalar function column. THe ID is constructed from the
   * function's name and the IDs of the inner columns on which the function is
   * performed.
   *
   * @return The ID of the scalar function column.
   */
  @Override
  public String getId() {
    List<String> colIds = Lists.newArrayList();
    for (AbstractColumn col : columns) {
      colIds.add(col.getId());
    }
    return new StrBuilder(scalarFunction.getFunctionName()).append(COLUMN_FUNCTION_TYPE_SEPARATOR)
        .appendWithSeparators(colIds,  COLUMN_COLUMN_SEPARATOR).toString();
  }

  /**
   * Returns a list of the inner simple column IDs of the scalar function
   * column (i.e., the columns on which the function is performed).
   *
   * @return A list of the inner simple column IDs.
   */
  @Override
  public List<String> getAllSimpleColumnIds() {
    List<String> columnIds = Lists.newArrayList();
    for (AbstractColumn column : columns) {
      columnIds.addAll(column.getAllSimpleColumnIds());
    }
    return columnIds;
  }

  /**
   * Returns the function of the scalar function column.
   *
   * @return The function of the scalar function column.
   */
  public ScalarFunction getFunction() {
    return scalarFunction;
  }

  /**
   * Returns a list of the columns on which the function is performed.
   *
   * @return A list of the columns on which the function is performed.
   */
  public List<AbstractColumn> getColumns() {
    return columns;
  }

  /**
   * Returns the cell of the column in the given row. If the given column
   * lookup contains this column, returns the cell in the given row using the
   * lookup. Otherwise, recursively gets the inner column values and uses them to
   * evaluate the value and create a cell based on that value. The base of the
   * recursion is a simple column, aggregation column or another scalar function
   * column that exists in the column lookup (i.e., its value was already calculated).
   *
   * @param row The given row.
   * @param lookup The column lookup.
   *
   * @return The cell of the column in the given row.
   */
  @Override
  public TableCell getCell(ColumnLookup lookup, TableRow row) {
    if (lookup.containsColumn(this)) {
      int columnIndex = lookup.getColumnIndex(this);
      return row.getCells().get(columnIndex);
    }
    // If the given column lookup does not contain this column, get the inner
    // column values of this column and use them as parameters to evaluate the
    // scalar function value in the given row.
    List<Value> functionParameters = Lists.newArrayListWithCapacity(columns.size());
    for (AbstractColumn column : columns) {
      functionParameters.add(column.getValue(lookup, row));
    }
    return new TableCell(scalarFunction.evaluate(functionParameters));
  }

  /**
   * Returns a list of all simple columns. This includes simple columns that are
   * inside scalar function columns (e.g, year(a1)), but does not include simple
   * columns that are inside aggregation columns (e.g., sum(a1)).
   *
   * @return A list of all simple columns.
   */
  @Override
  public List<SimpleColumn> getAllSimpleColumns() {
    List<SimpleColumn> simpleColumns =
        Lists.newArrayListWithCapacity(columns.size());
    for (AbstractColumn column : columns) {
      simpleColumns.addAll(column.getAllSimpleColumns());
    }
    return simpleColumns;
  }

  /**
   * Returns a list of all aggregation columns including the columns that are
   * inside scalar function columns (e.g., the column year(date(max(d1))) will
   * return max(d1)).
   *
   * @return A list of all aggregation columns.
   */
  @Override
  public List<AggregationColumn> getAllAggregationColumns() {
    List<AggregationColumn> aggregationColumns =
        Lists.newArrayListWithCapacity(columns.size());
    for (AbstractColumn column : columns) {
      aggregationColumns.addAll(column.getAllAggregationColumns());
    }
    return aggregationColumns;
  }

  /**
   * Returns a list of all scalar function columns. Returns itself and
   * other inner scalar function columns (if there are any).
   * e.g., the column max(year(a1), year(a2)) will return the 3 columns:
   * max(year(a1), year(a2)), year(a1), year(a2).
   *
   * @return A list of all scalar function columns.
   */
  @Override
  public List<ScalarFunctionColumn> getAllScalarFunctionColumns() {
    List<ScalarFunctionColumn> scalarFunctionColumns = Lists.newArrayList(this);
    for (AbstractColumn column : columns) {
      scalarFunctionColumns.addAll(column.getAllScalarFunctionColumns());
    }
    return scalarFunctionColumns;
  }

  /**
   * Checks that the column is valid. Checks the scalar function matches 
   * its arguments (inner columns) and all its inner columns are valid too. 
   * Throws a ColumnException if the scalar function has invalid arguments.
   *
   * @param dataTable The table description.
   *
   * @throws InvalidQueryException Thrown when the column is invalid.
   */
  @Override
  public void validateColumn(DataTable dataTable) throws InvalidQueryException {
    List<ValueType> types = Lists.newArrayListWithCapacity(columns.size());
    for (AbstractColumn column : columns) {
      column.validateColumn(dataTable);
      types.add(column.getValueType(dataTable));
    }
    // Throws an InvalidColumnException when the function arguments types are
    // invalid.
    scalarFunction.validateParameters(types);
  }

  /**
   * Returns the value type of the column after evaluating the scalar function.
   * e.g., the value type of year(date1) is NUMBER.
   *
   * @param dataTable The table description.
   *
   * @return the value type of the column.
   */
  @Override
  public ValueType getValueType(DataTable dataTable) {
    if (dataTable.containsColumn(this.getId())) {
      return dataTable.getColumnDescription(this.getId()).getType();
    }
    List<ValueType> types = Lists.newArrayListWithCapacity(columns.size());
    for (AbstractColumn column : columns) {
      types.add(column.getValueType(dataTable));
    }
    return scalarFunction.getReturnType(types);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof ScalarFunctionColumn) {
      ScalarFunctionColumn other = (ScalarFunctionColumn) o;
      return columns.equals(other.columns)
          && scalarFunction.equals(other.scalarFunction);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash  = 1279; // Some arbitrary prime number.
    for (AbstractColumn column : columns) {
      hash = (hash * 17) + column.hashCode();
    }
    hash = (hash * 17) + scalarFunction.hashCode();
    return hash;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toQueryString() {
    List<String> columnQueryStrings = Lists.newArrayList();
    for (AbstractColumn column : columns) {
      columnQueryStrings.add(column.toQueryString());
    }
    return scalarFunction.toQueryString(columnQueryStrings);
  }

  /**
   * This is for debug and error messages, not for ID generation.
   *
   * @return A string describing this AggregationColumn.
   */
  @Override
  public String toString() {
    List<String> colNames = Lists.newArrayList();
    for (AbstractColumn col : columns) {
      colNames.add(col.toString());
    }
    return new StrBuilder(scalarFunction.getFunctionName()).append("(")
        .appendWithSeparators(colNames, COLUMN_COLUMN_SEPARATOR).append(")").toString();
  }
}
