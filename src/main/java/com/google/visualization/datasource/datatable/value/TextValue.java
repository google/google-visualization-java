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

import com.ibm.icu.text.Collator;
import com.ibm.icu.util.ULocale;

import java.util.Comparator;

/**
 * A value of type text (string).
 *
 * @author Yoah B.D.
 */
public class TextValue extends Value {

  /**
   * A single static null value.
   */
  private static final TextValue NULL_VALUE = new TextValue("");

  /**
   * The underlying value.
   */
  private String value;

  /**
   * Creates a new text cell.
   *
   * @param value The cell's value.
   */
  public TextValue(String value) {
    if (value == null) {
      throw new NullPointerException("Cannot create a text value from null.");
    }
    this.value = value;
  }

  @Override
  public ValueType getType() {
    return ValueType.TEXT;
  }

  /**
   * Returns the text.
   *
   * @return The underlying text.
   */
  @Override
  public String toString() {
    return value;
  }

  /**
   * Static method to return the null value (same one for all calls).
   *
   * @return Null value.
   */
  public static TextValue getNullValue() {
    return NULL_VALUE;
  }

  /**
   * Tests whether this cell's value is logical null.
   *
   * @return Indication of whether the cell's value is null.
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
      return 0;
    }
    // TextValue has no NULL_VALUE.
    return value.compareTo(((TextValue) other).value);
  }

  /**
   * Returns a hash code for this string value.
   *
   * @return A hash code for this string value.
   */
  @Override
  public int hashCode() {
    // TextValue has no NULL_VALUE.
    return value.hashCode();
  }

  @Override
  public String getObjectToFormat() {
    return value;
  }

  /**
   * Returns a comparator that compares text values according to a given locale.
   *
   * @param ulocale The ulocale defining the order relation for text values.
   *
   * @return A comparator that compares text values according to a given locale.
   */
  public static Comparator<TextValue> getTextLocalizedComparator(final ULocale ulocale) {
    return new Comparator<TextValue>() {
      Collator collator = Collator.getInstance(ulocale);

      @Override
      public int compare(TextValue tv1, TextValue tv2) {
        if (tv1 == tv2) {
          return 0;
        }
        return collator.compare(tv1.value, tv2.value);
      }
    };
  }

  /**
   * Returns the text value.
   *
   * @return The underlying text value.
   */
  public String getValue() {
    return value;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected String innerToQueryString() {
    if (value.contains("\"")) {
      if (value.contains("'")) {
        throw new RuntimeException("Cannot run toQueryString() on string"
            + " values that contain both \" and '.");
      } else {
        return "'" + value + "'";
      }
    } else {
      return "\"" + value + "\"";
    }
  }
}
