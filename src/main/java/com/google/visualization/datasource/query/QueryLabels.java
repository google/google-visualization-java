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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.visualization.datasource.base.InvalidQueryException;

import org.apache.commons.lang.text.StrBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Describes the labels of a Query, as passed in the query under
 * the "label" clause.
 *
 * A label can optionally be specified for each column.
 * If not specified, each column type has a default label.
 *
 * @author Itai R.
 */
public class QueryLabels {
  /**
   * Log.
   */
  private static final Log log = LogFactory.getLog(QueryLabels.class.getName());

  /**
   * A map of all of the columns that require non-default labels, and the
   * label specified for them.
   * Columns with default labels are not in the map.
   */
  private Map<AbstractColumn, String> columnLabels;

  /**
   * Default empty constructor, with no labels. Labels can be added later.
   */
  public QueryLabels() {
    columnLabels = Maps.newHashMap();
  }

  /**
   * Adds a column label.
   * Validates that the column ID is not already specified.
   *
   * @param column The column to which the label is assigned.
   * @param label The assigned label.
   *
   * @throws InvalidQueryException Thrown if the column is already specified.
   */
  public void addLabel(AbstractColumn column, String label) throws InvalidQueryException {
    if (columnLabels.keySet().contains(column)) {
      String messageToLogAndUser = "Column [" + column.toString() + "] is "
          + "specified more than once in LABEL.";
      log.error(messageToLogAndUser);
      throw new InvalidQueryException(messageToLogAndUser);
    }
    columnLabels.put(column, label);
  }

  /**
   * Returns the label of the specified column, or null if no label was
   * specified.
   *
   * @param column The column for which the label is required.
   *
   * @return The label, or null if no label was specified for this column.
   */
  public String getLabel(AbstractColumn column) {
    return columnLabels.get(column);
  }

  /**
   * Returns an immutable set of the columns for which a label was specified.
   *
   * @return An immutable set of the columns for which a label was specified.
   */
  public Set<AbstractColumn> getColumns() {
    return ImmutableSet.copyOf(columnLabels.keySet());
  }

  /**
   * Returns all the columns that are ScalarFunctionColumns including scalar
   * functions columns that are inside other scalar function columns
   * (e.g., sum(year(a), year(b))).
   *
   * @return all the columns that are ScalarFunctionColumns.
   */
  public List<ScalarFunctionColumn> getScalarFunctionColumns() {
    List<ScalarFunctionColumn> result = Lists.newArrayList();
    for (AbstractColumn col : columnLabels.keySet()) {
      for (ScalarFunctionColumn innerCol : col.getAllScalarFunctionColumns()) {
        if (!result.contains(innerCol)) {
          result.add(innerCol);
        }
      }
    }
    return result;
  }
  
  /**
   * Returns all the columns that are AggregationColumns.
   *
   * @return All the columns that are AggregationColumns.
   */
  public List<AggregationColumn> getAggregationColumns() {
    List<AggregationColumn> result = Lists.newArrayList();
    for (AbstractColumn col : columnLabels.keySet()) {
      result.addAll(col.getAllAggregationColumns());
    }
    return result;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((columnLabels == null) ? 0 : columnLabels.hashCode());
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
    QueryLabels other = (QueryLabels) obj;
    if (columnLabels == null) {
      if (other.columnLabels != null) {
        return false;
      }
    } else if (!columnLabels.equals(other.columnLabels)) {
      return false;
    }
    return true;
  }
  
  /**
   * Returns a string that when fed into the query parser, produces a QueryLabels equal to this one.
   * The string returned does not contain the LABEL keyword.
   * 
   * @return The query string.
   */
  public String toQueryString() {
    StrBuilder builder = new StrBuilder();
    List<String> stringList = Lists.newArrayList();
    for (AbstractColumn col : columnLabels.keySet()) {
      String label = columnLabels.get(col);
      stringList.add(col.toQueryString() + " " + Query.stringToQueryStringLiteral(label));
    }
    builder.appendWithSeparators(stringList, ", ");
    return builder.toString(); 
  }
}
