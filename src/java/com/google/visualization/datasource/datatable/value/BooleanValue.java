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
 * A value of type boolean. Valid values are TRUE, FALSE and NULL_VALUE.
 *
 * @author Yoah B.D.
 */
public class BooleanValue extends Value {

  /**
   * A single static null value.
   */
  private static final BooleanValue NULL_VALUE = new BooleanValue(false);

  /**
   * A single static TRUE value.
   */
  public static final BooleanValue TRUE = new BooleanValue(true);

  /**
   * A single static FALSE value.
   */
  public static final BooleanValue FALSE = new BooleanValue(false);

  /**
   * Static method to return the null value (same one for all calls)
   *
   * @return Null value.
   */
  public static BooleanValue getNullValue() {
    return NULL_VALUE;
  }

  /**
   * Static method to return a BooleanValue based on a given java boolean.
   * If the parameter is null, returns NULL_VALUE.
   *
   * @param value The java Boolean value to be represented.
   *
   * @return The static predefined instance of the given value.
   */
  public static BooleanValue getInstance(Boolean value) {
    if (value == null) {
      return NULL_VALUE;
    }
    return value ? TRUE : FALSE;
  }

  /**
   * The underlying value
   */
  private boolean value;

  /**
   * Create a new boolean value.
   * This is private so users must use the three predefined values here (Null,
   * true, and false), like an enum.
   *
   * @param value The underlying value.
   */
  private BooleanValue(boolean value) {
    this.value = value;
  }

  @Override
  public ValueType getType() {
    return ValueType.BOOLEAN;
  }

  /**
   * Returns the underlying value.
   *
   * @return The underlying value.
   */
  public boolean getValue() {
    if (this == NULL_VALUE) {
      throw new NullValueException("This null boolean has no value");
    }
    return value;
  }

  /**
   * Returns the value with default formatting.
   *
   * @return The value with default formatting.
   */
  @Override
  public String toString() {
    if (this == NULL_VALUE) {
      return "null";
    }
    return Boolean.toString(value);
  }

  /**
   * Tests whether this cell's value is a logical null.
   *
   * @return Indication if the call's value is null.
   */
  @Override
  public boolean isNull() {
    return (this == NULL_VALUE);
 }

  /**
   * Compares this cell to another cell of the same type.
   *
   * @param other Other cell.
   *
   * @return 0 if equal, negative if this is smaller, positive if larger.
   */
  @Override
  public int compareTo(Value other) {
    if (this == other) {
      return 0; // If same cell, or both are null.
    }
    BooleanValue otherBoolean = (BooleanValue) other;
    if (isNull()) {
      return -1;
    }
    if (otherBoolean.isNull()) {
      return 1;
    }
    return (value == otherBoolean.value ? 0 : (value ? 1 : -1));
  }

  /**
   * Returns a hash code for this boolean value.
   *
   * @return A hash code for this boolean value. Null gets -1, false gets 0,
   *     true gets 1.
   */
  @Override
  public int hashCode() {
    return (isNull() ? -1 : (value ? 1 : 0));
  }


  @Override
  public Boolean getObjectToFormat() {
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
    return value ? "true" : "false";
  }
}
