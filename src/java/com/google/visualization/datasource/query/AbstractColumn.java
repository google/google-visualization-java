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

import com.google.visualization.datasource.base.InvalidQueryException;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;

import java.util.List;

/**
 * A column.
 * This can be either a column by ID or an aggregation column (like min(c1)),
 * etc.
 *
 * @author Yonatan B.Y.
 */
public abstract class AbstractColumn {

  /**
   * Returns a string ID for this column. It can be already existing in the
   * instance, or generated upon request, depending on the specific class
   * implementation.
   *
   * @return The string ID for this column.
   */
  public abstract String getId();

  /**
   * Returns a list of all simple (primitive) column IDs mentioned in this
   * AbstractColumn. It is a list to support calculated columns in the future.
   * For example, a simple column would just return a list containing its own
   * ID. An aggregation column would just return a list containing the ID of
   * its aggregated column. In the future, when calculated columns are
   * introduced, a calculated column would return a list with more than one
   * element.
   *
   * @return A list of all simple column IDs mentioned in this AbstractColumn.
   */
  public abstract List<String> getAllSimpleColumnIds();

  /**
   * Returns the value of the column in the given row.
   *
   * @param row The given row.
   * @param lookup The column lookup.
   *
   * @return The value of the column in the given row.
   */
  public Value getValue(ColumnLookup lookup, TableRow row) {
    return getCell(lookup, row).getValue();
  }
  
  /**
   * Returns the cell of the column in the given row.
   *
   * @param row The given row.
   * @param lookup The column lookup.
   *
   * @return The cell of the column in the given row.
   */
  public TableCell getCell(ColumnLookup lookup, TableRow row) {
    int columnIndex = lookup.getColumnIndex(this);
    return row.getCells().get(columnIndex);
  }

  /**
   * Returns a list of all simple columns mentioned in this abstract column.
   *
   * @return A list of all simple columns mentioned in this abstract column.
   */
  public abstract List<SimpleColumn> getAllSimpleColumns();

  /**
   * Returns a list of all aggregation columns mentioned in this abstract
   * column.
   *
   * @return A list of all aggregation columns mentioned in this abstract
   *     column.
   */
  public abstract List<AggregationColumn> getAllAggregationColumns();

  /**
   * Returns a list of all scalar function columns mentioned in this abstract
   * column.
   *
   * @return A list of all scalar function columns mentioned in this abstract
   *     column.
   */
  public abstract List<ScalarFunctionColumn> getAllScalarFunctionColumns();

  /**
   * Check whether the column is valid. i.e., aggregation columns and
   * scalar function column are valid if the aggregation or scalar function
   * respectively matches its arguments (inner columns).
   *
   * @param dataTable The data table.
   *
   * @throws InvalidQueryException Thrown when the column is not valid.
   */
  public abstract void validateColumn(DataTable dataTable) throws InvalidQueryException;

  /**
   * Returns the value type of the column. If it's a simple column returns the
   * value type of the column itself, if it's an aggregation or scalar function
   * column, returns the value type of the column after evaluating the function.
   * e.g., the value type of year(a1) is NUMBER.
   *
   * @param dataTable The data table.
   *
   * @return the value type of the column.
   */
  public abstract ValueType getValueType(DataTable dataTable);

  /**
   * Returns a string that when parsed by the query parser, should return an
   * identical column.
   *
   * @return A string form of this column.
   */
  public abstract String toQueryString();

  @Override
  public abstract boolean equals(Object o);

  @Override
  public abstract int hashCode();
}
