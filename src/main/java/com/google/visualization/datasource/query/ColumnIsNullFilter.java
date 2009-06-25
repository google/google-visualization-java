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

import java.util.List;
import java.util.Set;

/**
 * A filter that matches null values. Its isMatch function returns true if
 * the value at the column is a null value (as defined by TableCell.isNull()).
 *
 * @author Yonatan B.Y.
 */
public class ColumnIsNullFilter extends QueryFilter {

  /**
   * The ID of the column that should be null.
   */
  private AbstractColumn column;

  /**
   * Constructs a new instance of this class with the given column ID.
   *
   * @param column The column that should be null.
   */
  public ColumnIsNullFilter(AbstractColumn column) {
    this.column = column;
  }

  /**
   * Returns the column that should be null.
   *
   * @return The column that should be null.
   */
  public AbstractColumn getColumn() {
    return column;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<String> getAllColumnIds() {
    return Sets.newHashSet(column.getAllSimpleColumnIds());
  }

  /**
   * Returns a list of all scalarFunctionColumns this filter uses, in this case
   * the scalarFunctionColumns in column (e.g, in the filter 'year(a) is null'
   * it will return year(a).
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
   * {@inheritDoc}
   */
  @Override
  public boolean isMatch(DataTable table, TableRow row) {
    DataTableColumnLookup lookup = new DataTableColumnLookup(table);
    return column.getValue(lookup, row).isNull();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toQueryString() {
    return column.toQueryString() + " IS NULL";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((column == null) ? 0 : column.hashCode());
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
    ColumnIsNullFilter other = (ColumnIsNullFilter) obj;
    if (column == null) {
      if (other.column != null) {
        return false;
      }
    } else if (!column.equals(other.column)) {
      return false;
    }
    return true;
  }
}
