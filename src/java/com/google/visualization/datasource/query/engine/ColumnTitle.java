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

package com.google.visualization.datasource.query.engine;

import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.AggregationColumn;
import com.google.visualization.datasource.query.AggregationType;

import org.apache.commons.lang.text.StrBuilder;

import java.util.List;

/**
 * A "title" (identification) of a column in a pivoted and/or grouped table.
 * What identifies a column is the list of values, one value from each of the pivot columns,
 * and a ColumnAggregation denoting on which column (of the original table columns) the aggregation
 * is performed and what type of aggregation (min, max, average, ...) is performed.
 *
 * @author Yonatan B.Y.
 */
/* package */ class ColumnTitle {

  /**
   * The list of values from the pivot-by columns.
   */
  private List<Value> values;

  /**
   * The column aggregation denoting this column.
   */
  public AggregationColumn aggregation;

  /**
   * Indication whether this table comes from a query that has more than one
   * aggregation.
   */
  private boolean isMultiAggregationQuery;

  /**
   * When creating the id of the column, this is used as a separator between the values
   * in the pivot columns.
   */
  public static final String PIVOT_COLUMNS_SEPARATOR = ",";

  /**
   * When creating the id of the column, this is used as a separator between the list of
   * values and the column aggregation.
   */
  public static final String PIVOT_AGGREGATION_SEPARATOR = " ";

  /**
   * Creates a new instance of this class, with the given values and column
   * aggregation.
   *
   * @param values The list of values.
   * @param aggregationColumn The aggregation column.
   * @param isMultiAggregationQuery Whether this table comes from a
   *     query with more than one aggregation.
   */
  public ColumnTitle(List<Value> values,
      AggregationColumn aggregationColumn, boolean isMultiAggregationQuery) {
    this.values = values;
    this.aggregation = aggregationColumn;
    this.isMultiAggregationQuery = isMultiAggregationQuery;
  }

  /**
   * Returns the values in this ColumnTitle.
   * @return The values in this ColumnTitle.
   */
  public List<Value> getValues() {
    return values;
  }

  /**
   * Creates a ColumnDescription for this column. The id is created by
   * concatenating the values, and the column aggregation, using the separators
   * PIVOT_COLUMNS_SEPARATOR, PIVOT_AGGREGATION_SEPARATOR,
   * COLUMN_AGGRGATION_TYPE_SEPARATOR.
   *
   * @param originalTable The original table, from which the original column description
   *     of the column under aggregation is taken.
   *
   * @return The ColumnDescription for this column.
   */
  public ColumnDescription createColumnDescription(DataTable originalTable) {
    ColumnDescription colDesc = originalTable.getColumnDescription(
        aggregation.getAggregatedColumn().getId());
    return createAggregationColumnDescription(colDesc);
  }

  /**
   * Creates a prefix for a pivoted column id, containing all the values of the
   * pivoted columns. Returns an empty string if no pivoting was used.
   *
   * @return A prefix for the pivoted column id.
   */
  private String createIdPivotPrefix() {
    if (!isPivot()) {
      return "";
    }
    return new StrBuilder().appendWithSeparators(values, PIVOT_COLUMNS_SEPARATOR)
        .append(PIVOT_AGGREGATION_SEPARATOR).toString();
  }

  /**
   * Creates a prefix for a pivoted column label, containing all the values of
   * the pivoted columns. Returns an empty string if no pivoting was used.
   *
   * @return A prefix for the pivoted column label.
   */
  private String createLabelPivotPart() {
    if (!isPivot()) {
      return "";
    }
    return new StrBuilder().appendWithSeparators(values, PIVOT_COLUMNS_SEPARATOR).toString();
  }

  /**
   * Returns true if pivoting was used.
   *
   * @return True if pivoting was used.
   */
  private boolean isPivot() {
    return (!values.isEmpty());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ColumnTitle)) {
      return false;
    }
    ColumnTitle other = (ColumnTitle) o;
    return values.equals(other.values) &&
        aggregation.equals(other.aggregation);
  }

  @Override
  public int hashCode() {
    int hash  = 1279; // Some arbitrary prime number.
    hash = (hash * 17) + values.hashCode();
    hash = (hash * 17) + aggregation.hashCode();
    return hash;
  }

  /**
   * Creates an aggregation column description.
   *
   * @param originalColumnDescription The original column description.
   *
   * @return A column description for the aggregation column.
   */
  /* package */ ColumnDescription createAggregationColumnDescription(
      ColumnDescription originalColumnDescription) {
    AggregationType aggregationType = aggregation.getAggregationType();
    String columnId = createIdPivotPrefix() + aggregation.getId();
    ValueType type = originalColumnDescription.getType();
    String aggregationLabelPart = aggregation.getAggregationType().getCode()
        + " " + originalColumnDescription.getLabel();
    String pivotLabelPart = createLabelPivotPart();
    String label;
    if (isPivot()) {
      if (isMultiAggregationQuery) {
        label = pivotLabelPart + " " + aggregationLabelPart;
      } else {
        label = pivotLabelPart;
      }
    } else {
      label = aggregationLabelPart;
    }

    ColumnDescription result;
    if (canUseSameTypeForAggregation(type, aggregationType)) {
      // Create a new column description and copy original formatters.
      result = new ColumnDescription(columnId, type, label);
    } else {
      // Create a new column description if this is a non numeric column and the aggregation 
      // type is COUNT.
      result = new ColumnDescription(columnId, ValueType.NUMBER, label);
    }

    return result;
  }

  /**
   * Checks whether the aggregation values and column values are of the same type.
   *
   * @param valueType The type of values in the aggregated column.
   * @param aggregationType The aggregation type.
   *
   * @return True if the aggregation values and column values are of the same
   *     type.
   */
  private boolean canUseSameTypeForAggregation(ValueType valueType,
      AggregationType aggregationType) {
    boolean ans;
    if (valueType == ValueType.NUMBER) {
      ans = true;
    } else {
      switch (aggregationType) {
        case MIN:
        case MAX:
          ans = true;
          break;
        case SUM:
        case AVG:
        case COUNT:
          ans = false;
          break;
        default:
          // Not supposed to be here.
          throw new IllegalArgumentException();
      }
    }
    return ans;
  }
}
