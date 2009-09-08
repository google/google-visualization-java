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
import com.google.visualization.datasource.base.MessagesEnum;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.value.ValueType;

import com.ibm.icu.util.ULocale;

import java.util.List;

/**
 * A column that is referred to by an aggregation, for example, min(c1).
 *
 * @author Yonatan B.Y.
 */
public class AggregationColumn extends AbstractColumn {
  /**
   * When creating the ID of the column, this separates the column
   * on which aggregation is performed and the type of aggregation performed
   * on it.
   */
  public static final String COLUMN_AGGRGATION_TYPE_SEPARATOR = "-";

  /**
   * The simple column on which the aggregation is performed, e.g., c1 in
   * min(c1).
   */
  private SimpleColumn aggregatedColumn;

  /**
   * The type of aggregation that is performed, e.g., min in min(c1).
   */
  private AggregationType aggregationType;

  /**
   * Creates a new instance of this class with the given column and
   * aggregation type.
   *
   * @param aggregatedColumn The column.
   * @param aggregationType The aggregation type.
   */
  public AggregationColumn(SimpleColumn aggregatedColumn,
      AggregationType aggregationType) {
    this.aggregatedColumn = aggregatedColumn;
    this.aggregationType = aggregationType;
  }

  /**
   * Creates a string to act as ID for this column. Constructed from the
   * aggregation type and the column ID, separated by a separator.
   *
   * @return A string to act as ID for this column.
   */
  @Override
  public String getId() {
    return aggregationType.getCode() + COLUMN_AGGRGATION_TYPE_SEPARATOR
        + aggregatedColumn.getId();
  }

  /**
   * Returns the column to aggregate.
   *
   * @return The column to aggregate.
   */
  public SimpleColumn getAggregatedColumn() {
    return aggregatedColumn;
  }

  @Override
  public List<String> getAllSimpleColumnIds() {
    return Lists.newArrayList(aggregatedColumn.getId());
  }

  /**
   * Returns a list of all simple columns. In this case, returns an empty list.
   *
   * @return A list of all simple columns.
   */
  @Override
  public List<SimpleColumn> getAllSimpleColumns() {
    return Lists.newArrayList();
  }

  /**
   * Returns a list of all aggregation columns. In this case, returns only
   * itself.
   *
   * @return A list of all aggregation columns.
   */
  @Override
  public List<AggregationColumn> getAllAggregationColumns() {
    return Lists.newArrayList(this);
  }

  /**
   * Returns a list of all scalar function columns. In this case, returns an
   * empty list.
   *
   * @return A list of all scalar function columns.
   */
  @Override
  public List<ScalarFunctionColumn> getAllScalarFunctionColumns() {
    return Lists.newArrayList();
  }

  /**
   * Returns the requested aggregation type.
   *
   * @return The requested aggregation type.
   */
  public AggregationType getAggregationType() {
    return aggregationType;
  }

  /**
   * Checks whether it makes sense to have the aggregation type on
   * the aggregated column. The type of the column is taken from the given
   * table description. Throws a column exception if the column is invalid.
   *
   * @param dataTable The data table.
   *
   * @throws InvalidQueryException Thrown if the column is invalid.
   */
  @Override
  public void validateColumn(DataTable dataTable) throws InvalidQueryException {
    ValueType valueType = dataTable.getColumnDescription(aggregatedColumn.getId()).getType();
    ULocale userLocale = dataTable.getLocaleForUserMessages();
    switch (aggregationType) {
      case COUNT: case MAX: case MIN: break;
      case AVG: case SUM:
      if (valueType != ValueType.NUMBER) {
        throw new InvalidQueryException(MessagesEnum.AVG_SUM_ONLY_NUMERIC.getMessage(userLocale));
      }
      break;
      default: throw new RuntimeException(MessagesEnum.INVALID_AGG_TYPE.getMessageWithArgs(
          userLocale, aggregationType.toString()));
    }
  }

  /**
   * Returns the value type of the column. In this case returns the value type
   * of the column after evaluating the aggregation function.
   *
   * @param dataTable The data table.
   *
   * @return The value type of the column.
   */
  @Override
  public ValueType getValueType(DataTable dataTable) {
    ValueType valueType;
    ValueType originalValueType =
        dataTable.getColumnDescription(aggregatedColumn.getId()).getType();
    switch (aggregationType) {
      case COUNT:
        valueType = ValueType.NUMBER;
        break;
      case AVG: case SUM: case MAX: case MIN:
      valueType = originalValueType;
      break;
      default: throw new RuntimeException(MessagesEnum.INVALID_AGG_TYPE.getMessageWithArgs(
          dataTable.getLocaleForUserMessages(), aggregationType.toString()));
    }
    return valueType;
  }


  @Override
  public boolean equals(Object o) {
    if (o instanceof AggregationColumn) {
      AggregationColumn other = (AggregationColumn) o;
      return aggregatedColumn.equals(other.aggregatedColumn)
          && aggregationType.equals(other.aggregationType);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash  = 1279; // Some arbitrary prime number.
    hash = (hash * 17) + aggregatedColumn.hashCode();
    hash = (hash * 17) + aggregationType.hashCode();
    return hash;
  }

  /**
   * This is for debug and error messages, not for ID generation.
   *
   * @return A string describing this AggregationColumn.
   */
  @Override
  public String toString() {
    return aggregationType.getCode() + "(" + aggregatedColumn.getId() + ")";
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toQueryString() {
    // This works because the codes in AggregationType are the same as the
    // keywords in the query parser.
    return aggregationType.getCode().toUpperCase() + "("
        + aggregatedColumn.toQueryString() + ")";
  }
}
