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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;

import org.apache.commons.lang.text.StrBuilder;

import java.util.List;
import java.util.Set;

/**
 * A compound filter.
 * This filter is a logical aggregation of other filters. Currently, 
 * union (OR) and intersection (AND) are supported. An OR complex filter matches if
 * any of its sub-filters match. An AND complex filter matches if all of
 * its sub-filters match.
 *
 * @author Yonatan B.Y.
 */
public class CompoundFilter extends QueryFilter {

  /**
   * The available types of CompoundFilter. Currently supported are
   * AndFilter and OrFilter.
   */
  public static enum LogicalOperator {
    AND,
    OR
  }

  /**
   * The collection of all sub-filters of this compound filter.
   */
  private List<QueryFilter> subFilters;

  /**
   * The type of this compound filter ('and' or 'or').
   */
  private LogicalOperator operator;

  /**
   * Constructs a compound filter of the given type, with the given sub-filters.
   *
   * @param operator The type of this compound filter.
   * @param subFilters The collection of all sub-filters of this compound filter.
   */
  public CompoundFilter(LogicalOperator operator,
      List<QueryFilter> subFilters) {
    this.subFilters = subFilters;
    this.operator = operator;
  }

  /**
   * Implements isMatch (from the QueryFilter interface) by recursively calling
   * isMatch on each of the sub-filters, and using the compound filter type to
   * determine the result.
   * Note:
   * 1. This uses short-circuit evaluation, i.e., an OR filter decides on a true
   *    result the moment it finds a true result among its sub-filters, without
   *    continuing to check the rest of the sub-filters; and dually for an AND
   *    filter.
   * 2. The sub-filters are evaluated in order.
   * 3. If the collection of sub-filters is empty, a RuntimeException is
   *    thrown.
   *
   * @param table The table containing this row.
   * @param row The row to check.
   *
   * @return true if this row should be part of the result set, false otherwise.
   */
  @Override
  public boolean isMatch(DataTable table, TableRow row) {
    if (subFilters.isEmpty()) {
      throw new RuntimeException("Compound filter with empty subFilters "
          + "list");
    }
    for (QueryFilter subFilter : subFilters) {
      boolean result = subFilter.isMatch(table, row);
      if (((operator == LogicalOperator.AND) && !result) ||
          ((operator == LogicalOperator.OR) && result)) {
        return result;
      }
    }
    return (operator == LogicalOperator.AND);
  }

  /**
   * Returns all the columnIds this filter uses, in this case the union of all
   * the results of getAllColumnIds() of all its subfilters.
   *
   * @return All the columnIds this filter uses.
   */
  @Override
  public Set<String> getAllColumnIds() {
    Set<String> result = Sets.newHashSet();
    for (QueryFilter subFilter : subFilters) {
      result.addAll(subFilter.getAllColumnIds());
    }
    return result;
  }

  /**
   * Returns a list of all scalarFunctionColumns this filter uses, in this case
   * the union of all the results of getScalarFunctionColumns() of all its
   * sub-filters.
   *
   * @return A list of all scalarFunctionColumns this filter uses.
   */
  @Override
  public List<ScalarFunctionColumn> getScalarFunctionColumns() {
    List<ScalarFunctionColumn> result = Lists.newArrayList();
    for (QueryFilter subFilter : subFilters) {
      result.addAll(subFilter.getScalarFunctionColumns());
    }
    return result;
  }
  
  /**
   * {@InheritDoc}
   */
  @Override
  protected List<AggregationColumn> getAggregationColumns() {
    List<AggregationColumn> result = Lists.newArrayList();
    for (QueryFilter subFilter : subFilters) {
      result.addAll(subFilter.getAggregationColumns());
    }
    return result;
  }

  /**
   * Returns the list of sub-filters associated with this CompoundFilter.
   *
   * @return The list of sub-filters associated with this CompoundFilter.
   */
  public List<QueryFilter> getSubFilters() {
    return ImmutableList.copyOf(subFilters);
  }

  /**
   * Returns the logical operator associated with this CompoundFilter.
   *
   * @return The logical operator associated with this CompoundFilter.
   */
  public LogicalOperator getOperator() {
    return operator;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toQueryString() {
    List<String> subFilterStrings = Lists.newArrayList();
    for (QueryFilter filter : subFilters) {
      subFilterStrings.add("(" + filter.toQueryString() + ")");
    }
    // This works because the names in LogicalOperator are exactly the same as
    // in the query language.
    return new StrBuilder()
        .appendWithSeparators(subFilterStrings, " " + operator.name() + " ").toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((operator == null) ? 0 : operator.hashCode());
    result = prime * result + ((subFilters == null) ? 0 : subFilters.hashCode());
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
    CompoundFilter other = (CompoundFilter) obj;
    if (operator == null) {
      if (other.operator != null) {
        return false;
      }
    } else if (!operator.equals(other.operator)) {
      return false;
    }
    if (subFilters == null) {
      if (other.subFilters != null) {
        return false;
      }
    } else if (!subFilters.equals(other.subFilters)) {
      return false;
    }
    return true;
  }
}
