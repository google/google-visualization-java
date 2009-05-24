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

import com.google.common.collect.Sets;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.Value;

import java.util.List;
import java.util.Set;

/**
 * A filter that compares a column value to a constant value.
 *
 * @author Yonatan B.Y.
 */
public class ColumnValueFilter extends ComparisonFilter {

  /**
   * The column to compare.
   */
  private AbstractColumn column;

  /**
   * The constant value the column value will be compared to.
   */
  private Value value;

  /**
   * Whether the column and the value should be reversed, i.e., instead of
   * column op value, this will be value op column.
   */
  private boolean isComparisonOrderReversed;

  /**
   * Constructs a new ColumnValueFilter on a given column, constant value,
   * operator, and isComparisonOrderReversed.
   *
   * @param column The column to compare.
   * @param value The constant value the column value is compared to.
   * @param operator The comparison operator to use.
   * @param isComparisonOrderReversed Whether to reverse the comparison order
   *     of the operator.
   */
  public ColumnValueFilter(AbstractColumn column, Value value,
      Operator operator, boolean isComparisonOrderReversed) {
    super(operator);
    this.column = column;
    this.value = value;
    this.isComparisonOrderReversed = isComparisonOrderReversed;
  }

  /**
   * Constructs a new ColumnValueFilter on a given column, constant value and
   * operator.
   *
   * @param column The column to compare.
   * @param value The constant value the column value is compared to.
   * @param operator The comparison operator to use.
   */
  public ColumnValueFilter(AbstractColumn column, Value value,
      Operator operator) {
    this(column, value, operator, false);
  }

  /**
   * Implements isMatch from the QueryFilter interface. This implementation
   * retrieves from the row, the data in the required column, and compares
   * it against the constant value, using the operator given in the
   * constructor.
   *
   * @param table The table containing this row.
   * @param row The row to check.
   *
   * @return true if this row should be part of the result set, i.e., if the
   *     operator returns true when run on the value in the column, and the
   *     constant value; false otherwise.
   */
  @Override
  public boolean isMatch(DataTable table, TableRow row) {
    DataTableColumnLookup lookup = new DataTableColumnLookup(table);
    Value columnValue = column.getValue(lookup, row);
    return isComparisonOrderReversed ? isOperatorMatch(value, columnValue) :
        isOperatorMatch(columnValue, value);
  }

  /**
   * Returns all the columnIds this filter uses, in this case the simple column
   * IDs of the filter's column.
   *
   * @return All the columnIds this filter uses.
   */
  @Override
  public Set<String> getAllColumnIds() {
    return Sets.newHashSet(column.getAllSimpleColumnIds());
  }

  /**
   * Returns a list of all scalarFunctionColumns this filter uses, in this case
   * the scalarFunctionColumns in the filter's column (e.g, in thefilter
   * 'year(a)=2008' it will return year(a).
   *
   * @return A list of all scalarFunctionColumns this filter uses.
   */
  @Override
  public List<ScalarFunctionColumn> getScalarFunctionColumns() {
    return column.getAllScalarFunctionColumns();
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  protected List<AggregationColumn> getAggregationColumns() {
    return column.getAllAggregationColumns();
  }

  /**
   * Returns the column ID associated with this ColumnValueFilter.
   *
   * @return The column ID associated with this ColumnValueFilter.
   */
  public AbstractColumn getColumn() {
    return column;
  }

  /**
   * Returns the value associated with this ColumnValueFilter.
   *
   * @return The value associated with this ColumnValueFilter.
   */
  public Value getValue() {
    return value;
  }

  /**
   * Returns whether or not this operator's comparison order is reversed. This
   * means whether the column and the value should be reversed, i.e., instead of
   * column op value, this will be value op column.
   *
   * @return Whether or not the operator's comparison order is reversed.
   */
  public boolean isComparisonOrderReversed() {
    return isComparisonOrderReversed;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toQueryString() {
    if (isComparisonOrderReversed) {
      return value.toQueryString() + " " + operator.toQueryString() + " "
          + column.toQueryString();
    } else {
      return column.toQueryString() + " " + operator.toQueryString() + " "
          + value.toQueryString();
    }
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((column == null) ? 0 : column.hashCode());
    result = prime * result + (isComparisonOrderReversed ? 1231 : 1237);
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ColumnValueFilter other = (ColumnValueFilter) obj;
    if (column == null) {
      if (other.column != null) {
        return false;
      }
    } else if (!column.equals(other.column)) {
      return false;
    }
    if (isComparisonOrderReversed != other.isComparisonOrderReversed) {
      return false;
    }
    if (value == null) {
      if (other.value != null) {
        return false;
      }
    } else if (!value.equals(other.value)) {
      return false;
    }
    return true;
  }
}
