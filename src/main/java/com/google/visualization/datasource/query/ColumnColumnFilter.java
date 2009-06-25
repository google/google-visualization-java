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
import com.google.common.collect.Sets;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.Value;

import java.util.List;
import java.util.Set;

/**
 * A filter that compares two column values.
 *
 * @author Yonatan B.Y.
 */
public class ColumnColumnFilter extends ComparisonFilter {

  /**
   * The first column.
   */
  private AbstractColumn firstColumn;

  /**
   * The second column. This is the column with which the first column is compared.
   */
  private AbstractColumn secondColumn;

  /**
   * Constructs a new ColumnColumnFilter on two given columns, and an operator.
   *
   * @param firstColumn The first column.
   * @param secondColumn The second column.
   * @param operator The comparison operator to use.
   */
  public ColumnColumnFilter(AbstractColumn firstColumn,
      AbstractColumn secondColumn, Operator operator) {
    super(operator);
    this.firstColumn = firstColumn;
    this.secondColumn = secondColumn;
  }

  /**
   * Implements isMatch from the QueryFilter interface. This implementation
   * retrieves from the row, the data in the required columns, and compares
   * the first against the second, using the operator given in the
   * constructor.
   *
   * @param table The table containing this row.
   * @param row The row to check.
   *
   * @return true if this row should be part of the result set, i.e., if the
   *     operator is true when run on the values in the two columns; false
   *     otherwise.
   */
  @Override
  public boolean isMatch(DataTable table, TableRow row) {
    DataTableColumnLookup lookup = new DataTableColumnLookup(table);
    Value firstValue = firstColumn.getValue(lookup, row);
    Value secondValue = secondColumn.getValue(lookup, row);
    return isOperatorMatch(firstValue, secondValue);
  }

  /**
   * Returns all the simple column IDs this filter uses, in this case
   * the simple column IDs of firstColumn and secondColumn.
   *
   * @return All the column IDs this filter uses.
   */
  @Override
  public Set<String> getAllColumnIds() {
    Set<String> columnIds = Sets.newHashSet(firstColumn.getAllSimpleColumnIds());
    columnIds.addAll(secondColumn.getAllSimpleColumnIds());
    return columnIds;
  }

  /**
   * Returns a list of all scalarFunctionColumns this filter uses, in this case
   * the scalarFunctionColumns in the first and second columns (e.g, in the
   * filter year(a) = year(date(b)) it will return year(a), year(date(b)) and
   * date(b)).
   *
   * @return A list of all scalarFunctionColumns this filter uses.
   */
  @Override
  public List<ScalarFunctionColumn> getScalarFunctionColumns() {
    List<ScalarFunctionColumn> scalarFunctionColumns = Lists.newArrayList();
    scalarFunctionColumns.addAll(firstColumn.getAllScalarFunctionColumns());
    scalarFunctionColumns.addAll(secondColumn.getAllScalarFunctionColumns());
    return scalarFunctionColumns;
  }

  /**
   * {@InheritDoc}
   */
  @Override
  protected List<AggregationColumn> getAggregationColumns() {
    List<AggregationColumn> aggregationColumns = Lists.newArrayList();
    aggregationColumns.addAll(firstColumn.getAllAggregationColumns());
    aggregationColumns.addAll(secondColumn.getAllAggregationColumns());
    return aggregationColumns;
  }

  /**
   * Returns the first column associated with this ColumnColumnFilter.
   *
   * @return The first column associated with this ColumnColumnFilter.
   */
  public AbstractColumn getFirstColumn() {
    return firstColumn;
  }

  /**
   * Returns the second column associated with this ColumnColumnFilter.
   *
   * @return The second column associated with this ColumnColumnFilter.
   */
  public AbstractColumn getSecondColumn() {
    return secondColumn;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toQueryString() {
    return firstColumn.toQueryString() + " " + operator.toQueryString()
    + " " + secondColumn.toQueryString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((firstColumn == null) ? 0 : firstColumn.hashCode());
    result = prime * result + ((secondColumn == null) ? 0 : secondColumn.hashCode());
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
    ColumnColumnFilter other = (ColumnColumnFilter) obj;
    if (firstColumn == null) {
      if (other.firstColumn != null) {
        return false;
      }
    } else if (!firstColumn.equals(other.firstColumn)) {
      return false;
    }
    if (secondColumn == null) {
      if (other.secondColumn != null) {
        return false;
      }
    } else if (!secondColumn.equals(other.secondColumn)) {
      return false;
    }
    return true;
  }
}
