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

import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.AggregationType;

/**
 * Aggregates a set of values. Adds one value at a time to the aggregated set.
 * This allows getting the values of: minimum, maximum, sum, count and average for the aggregated
 * set. Each one of these values is available only where appropriate (for instance, you cannot
 * average on text values).
 * The set of values itself is not stored.
 * Only non-null values are considered for aggregation.
 *
 * @author Yoav G.
 */

/*package*/ class ValueAggregator {

  /**
   * The column type of the values to be aggregated.
   */
  private ValueType valueType;

  /**
   * The maximum value found so far.
   */
  private Value max;

  /**
   * The minimum value found so far.
   */
  private Value min;

  /**
   * The sum of all aggregated values. Updated only for NumberValue.
   */
  private double sum = 0;

  /**
   * The number of non null values aggregated.
   */
  private int count = 0;

  /**
   * Constructs a new column value aggregator.
   *
   * @param valueType The column type of this aggregator. This type defines
   *     the type of all values to be aggregated.
   */
  public ValueAggregator(ValueType valueType) {
    this.valueType = valueType;
    min = max = Value.getNullValueFromValueType(valueType);
  }

  /**
   * Aggregates an additional value. If this value is not null it is counted,
   * summed, and compared against the current maximum and minimum values to
   * consider replacing them.
   *
   * @param value The value to aggregate.
   */
  public void aggregate(Value value) {
    if (!value.isNull()) {
      count++;
      if (valueType == ValueType.NUMBER) {
        sum += ((NumberValue) value).getValue();
      }
      if (count == 1) { // First non null element.
        max = min = value;
      } else {
        max = max.compareTo(value) >= 0 ? max : value;
        min = min.compareTo(value) <= 0 ? min : value;
      }
    } else if (count == 0) {
      min = max = value;
    }
  }

  /**
   * Returns the sum of all (non null) aggregated values.
   *
   * @return The sum of all (non null) aggregated values.
   *
   * @throws UnsupportedOperationException In case the column type does not
   *     support sum.
   */
  private double getSum() {
    if (valueType != ValueType.NUMBER) {
      throw new UnsupportedOperationException();
    }
    return sum;
  }

  /**
   * Returns the average (or null if no non-null values were aggregated).
   *
   * @return The average (or null if no non-null values were aggregated).
   *
   * @throws UnsupportedOperationException If the column type does not support average.
   */
  private Double getAverage() {
    if (valueType != ValueType.NUMBER) {
      throw new UnsupportedOperationException();
    }
    return count > 0 ? sum / count : null;
  }

  /**
   * Returns a single value.
   * Note: The aggregation of a zero number of rows returns a null value for
   * all aggregation types except from count. The type of Null value is numeric
   * for sum and average and identical to its column values for min and max.
   *
   * @param type The type of aggregation requested.
   *
   * @return The requested value.
   */
  public Value getValue(AggregationType type) {
    Value v;
    switch (type) {
      case AVG:
        v = (count != 0) ? new NumberValue(getAverage()) : NumberValue.getNullValue();
        break;
      case COUNT:
        v = new NumberValue(count);
        break;
      case MAX:
        v = max;
        // If there are zero rows replace with the same type null value.
        if (count == 0) {
          v = Value.getNullValueFromValueType(v.getType());
        }
        break;
      case MIN:
        v = min;
        // If there are zero rows replace with the same type null value.
        if (count == 0) {
          v = Value.getNullValueFromValueType(v.getType());
        }
        break;
      case SUM:
        v = (count != 0) ? new NumberValue(getSum()) : NumberValue.getNullValue();
        break;
      default:
        throw new RuntimeException("Invalid AggregationType");
    }
    return v;
  }
}
