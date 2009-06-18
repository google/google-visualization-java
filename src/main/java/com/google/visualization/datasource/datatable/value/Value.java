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

import com.ibm.icu.util.ULocale;

import java.util.Comparator;

/**
 * An abstract value of a single cell in a table.
 * Acts as a base class for all the typed value classes.
 * A value should be an immutable class, because it is reference-copied when cloning complex
 * objects like rows or whole tables.
 *
 * @author Itai R.
 */
public abstract class Value implements Comparable<Value> {

  /**
   * Returns the type of this cell.
   *
   * @return The type of this cell.
   */
  public abstract ValueType getType();

  /**
   * Returns whether or not this cell's value is a logical null.
   *
   * @return true iff this cell's value is a logical null.
   */
  public abstract boolean isNull();

  /**
   * Checks if this object is equal to the given object.
   * Note that no instance can be equal to null and two
   * instances of different classes are not equal.
   *
   * @param o The object with which to compare.
   *
   * @return true if this equals o and false otherwise.
   */
  @Override
  public boolean equals(Object o) {
    if ((null == o) || (this.getClass() != o.getClass())) {
      return false;
    }
    // Does not throw exception since the classes match.
    return (this.compareTo((Value) o) == 0);
  }

  /**
   * Returns the hashcode of this value. This is introduced here to force the
   * subclasses to define this function.
   *
   * @return The hashcode of this value.
   */
  @Override
  public abstract int hashCode();

  /**
   * Uses an ibm.icu.text.UFormat instance to format Values.
   * The following method returns an object representing this Value that a
   * UFormat can format.
   *
   * @return An object representing this value that can be formatted by a
   *     UFormat formatter, null for a NULL_VALUE.
   */
  public abstract Object getObjectToFormat();


  /**
   * Returns the NullValue for the given type.
   *
   * @param type The value type.
   *
   * @return The NullValue for the given type.
   */
  public static Value getNullValueFromValueType(ValueType type) {
    switch (type) {
      case BOOLEAN:
        return BooleanValue.getNullValue();
      case TEXT:
        return TextValue.getNullValue();
      case NUMBER:
        return NumberValue.getNullValue();
      case TIMEOFDAY:
        return TimeOfDayValue.getNullValue();
      case DATE:
        return DateValue.getNullValue();
      case DATETIME:
        return DateTimeValue.getNullValue();
    }
    return null; // shouldn't get here
  }

  /**
   * Returns a string that, when parsed by the query parser, should return an
   * identical value. Throws an exception when called on a null value.
   *
   * @return A string form of this value.
   */
  public final String toQueryString() {
    if (isNull()) {
      throw new RuntimeException("Cannot run toQueryString() on a null value.");
    }
    return innerToQueryString();
  }

  /**
   * Returns a string that, when parsed by the query parser, should return an
   * identical value. Assumes this value is not null.
   *
   * @return A string form of this value.
   */
  protected abstract String innerToQueryString();

  /**
   * Returns a comparator that compares values according to a given locale
   * (in case of text values).
   *
   * @param ulocale The ULocale defining the order relation of text values.
   *
   * @return A comparator that compares values according to a given locale
   *     (in case of text values).
   */
  public static Comparator<Value> getLocalizedComparator(
      final ULocale ulocale) {
    return new Comparator<Value>() {
      private Comparator<TextValue> textValueComparator =
          TextValue.getTextLocalizedComparator(ulocale);

      @Override
      public int compare(Value value1, Value value2) {
        if (value1 == value2) {
          return 0;
        }
        if (value1.getType() == ValueType.TEXT) {
          return textValueComparator.compare((TextValue) value1,
              (TextValue) value2);
        } else {
          return value1.compareTo(value2);
        }
      }
    };
  }
}
