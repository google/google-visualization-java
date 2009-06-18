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

import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;

import java.util.List;
import java.util.Set;

/**
 * A negation filter.
 * Holds a sub-filter and negates its result, acting like the NOT operator.
 *
 * @author Yonatan B.Y.
 */
public class NegationFilter extends QueryFilter {

  /**
   * The sub-filter of this negation filter.
   */
  private QueryFilter subFilter;

  /**
   * Constructs a negation filter, with the given sub-filter.
   *
   * @param subFilter The sub-filter of this negation filter.
   */
  public NegationFilter(QueryFilter subFilter) {
    this.subFilter = subFilter;
  }

  /**
   * Implements isMatch (from the QueryFilter interface) by recursively calling
   * isMatch on the sub-filter and negating the result.
   *
   * @param table The table containing this row.
   * @param row The row to check.
   *
   * @return true if this row should be part of the result set, false otherwise.
   */
  @Override
  public boolean isMatch(DataTable table, TableRow row) {
    return !subFilter.isMatch(table, row);
  }

  /**
   * Returns all the columnIds this filter uses, in this case exactly all the
   * columnIds that the sub-filter uses.
   *
   * @return All the columnIds this filter uses.
   */
  @Override
  public Set<String> getAllColumnIds() {
    return subFilter.getAllColumnIds();
  }

  /**
   * Returns a list of all scalarFunctionColumns this filter uses, in this case
   * the scalarFunctionColumns its sub-filter uses.
   *
   * @return A list of all scalarFunctionColumns this filter uses.
   */
  @Override
  public List<ScalarFunctionColumn> getScalarFunctionColumns() {
    return subFilter.getScalarFunctionColumns();
  }

  /**
   * {@InheritDoc}
   */
  @Override
  protected List<AggregationColumn> getAggregationColumns() {
    return subFilter.getAggregationColumns();
  }

  /**
   * Returns the sub-filter associated with this NegationFilter.
   *
   * @return The sub-filter associated with this NegationFilter.
   */
  public QueryFilter getSubFilter() {
    return subFilter;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toQueryString() {
    return "NOT (" + subFilter.toQueryString() + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((subFilter == null) ? 0 : subFilter.hashCode());
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
    NegationFilter other = (NegationFilter) obj;
    if (subFilter == null) {
      if (other.subFilter != null) {
        return false;
      }
    } else if (!subFilter.equals(other.subFilter)) {
      return false;
    }
    return true;
  }
}
