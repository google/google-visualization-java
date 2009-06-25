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
 * Describes the formatting options of a Query, as passed on the query
 * in the "format" clause.
 *
 * Format can be specified for each column, but does not have to be.
 * If not specified, each column type has a default format.
 *
 * Each format is a string pattern, as used in the ICU UFormat class.
 * @see com.ibm.icu.text.UFormat
 *
 * @author Itai R.
 */
public class QueryFormat {

  /**
   * Log.
   */
  private static final Log log = LogFactory.getLog(QueryFormat.class.getName());

  /**
   * A map of all of the columns that require non-default patterns, and the
   * pattern specified for them.
   * Columns with default patterns are not in the map.
   */
  private Map<AbstractColumn, String> columnPatterns;

  /**
   * Default empty constructor, with no patterns (patterns can be added later).
   */
  public QueryFormat() {
    columnPatterns = Maps.newHashMap();
  }

  /**
   * Adds a column pattern.
   * Validates that the column ID is not already specified.
   *
   * @param column The column to which the pattern is assigned.
   * @param pattern The assigned pattern.
   *
   * @throws InvalidQueryException Thrown if the column is already specified.
   */
  public void addPattern(AbstractColumn column, String pattern) throws InvalidQueryException {
    if (columnPatterns.keySet().contains(column)) {
      String messageToLogAndUser = "Column [" + column.toString() + "] is "
          + "specified more than once in FORMAT.";
      log.error(messageToLogAndUser);
      throw new InvalidQueryException(messageToLogAndUser);
    }
    columnPatterns.put(column, pattern);
  }

  /**
   * Returns the pattern of the specified column, or null if no pattern was
   * specified.
   *
   * @param column The column for which the pattern is required.
   *
   * @return The pattern, or null if no pattern was specified for this column.
   */
  public String getPattern(AbstractColumn column) {
    return columnPatterns.get(column);
  }

  /**
   * Returns an immutable set of the column IDs for which a pattern was
   * specified.
   *
   * @return An immutable set of the column IDs for which a pattern was
   *     specified.
   */
  public Set<AbstractColumn> getColumns() {
    return ImmutableSet.copyOf(columnPatterns.keySet());
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
    for (AbstractColumn col : columnPatterns.keySet()) {
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
    for (AbstractColumn col : columnPatterns.keySet()) {
      result.addAll(col.getAllAggregationColumns());
    }
    return result;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((columnPatterns == null) ? 0 : columnPatterns.hashCode());
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
    QueryFormat other = (QueryFormat) obj;
    if (columnPatterns == null) {
      if (other.columnPatterns != null) {
        return false;
      }
    } else if (!columnPatterns.equals(other.columnPatterns)) {
      return false;
    }
    return true;
  }
  
  /**
   * Returns a string that when fed into the query parser, produces a QueryFormat equal to this one.
   * The string returned does not contain the FORMAT keyword.
   * 
   * @return The query string.
   */
  public String toQueryString() {
    StrBuilder builder = new StrBuilder();
    List<String> stringList = Lists.newArrayList();
    for (AbstractColumn col : columnPatterns.keySet()) {
      String pattern = columnPatterns.get(col);
      stringList.add(col.toQueryString() + " " + Query.stringToQueryStringLiteral(pattern));
    }
    builder.appendWithSeparators(stringList, ", ");
    return builder.toString(); 
  }
}
