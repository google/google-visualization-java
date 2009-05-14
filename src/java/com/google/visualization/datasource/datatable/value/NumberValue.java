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

package com.google.visualization.datasource.datatable.value;

/**
 * A value of type number. Valid values are double-precision floating-point numbers
 * (including integers) or NULL_VALUE.
 *
 * @author Yoah B.D.
 */
public class NumberValue extends Value {

  /**
   * A single static null value.
   */
  private static final NumberValue NULL_VALUE = new NumberValue(-9999);

  /**
   * Static method to return the null value (same one for all calls).
   * @return Null value.
   */
  public static NumberValue getNullValue() {
    return NULL_VALUE;
  }

  /**
   * The underlying value.
   */
  private double value;

  /**
   * Create a new number value.
   *
   * @param value The underlying value.
   */
  public NumberValue(double value) {
    this.value = value;
  }

  @Override
  public ValueType getType() {
    return ValueType.NUMBER;
  }

  /**
   * Returns the underlying value.
   *
   * @return The underlying value.
   */
  public double getValue() {
    if (this == NULL_VALUE) {
      throw new NullValueException("This null number has no value");
    }
    return value;
  }

  /**
   * Returns the number as a String using default formatting.
   *
   * @return The number as a String using default formatting.
   */
  @Override
  public String toString() {
    if (this == NULL_VALUE) {
      return "null";
    }
    return Double.toString(value);
  }

  /**
   * Tests whether this value is a logical null.
   *
   * @return Indication of whether the value is null.
   */
  @Override
  public boolean isNull() {
    return (this == NULL_VALUE);
  }

  /**
   * Compares this value to another value of the same type.
   *
   * @param other Other value.
   *
   * @return 0 if equal, negative if this is smaller, positive if larger.
   */
  @Override
  public int compareTo(Value other) {
    if (this == other) {
      return 0; // If same value, or both are null.
    }
    NumberValue otherNumber = (NumberValue) other;
    if (isNull()) {
      return -1;
    }
    if (otherNumber.isNull()) {
      return 1;
    }
    return Double.compare(value, otherNumber.value);
  }

  /**
   * Returns a hash code for this number value.
   *
   * @return A hash code for this number value.
   */
  @Override
  public int hashCode() {
    if (isNull()) {
      return 0;
    }
    return new Double(value).hashCode();
  }

  @Override
  public Number getObjectToFormat() {
    if (isNull()) {
      return null;
    }
    return value;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String innerToQueryString() {
    return Double.toString(value);
  }
}
