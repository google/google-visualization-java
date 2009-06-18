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

package com.google.visualization.datasource.datatable;

import com.google.common.collect.Maps;
import com.google.visualization.datasource.datatable.value.BooleanValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.ibm.icu.util.ULocale;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/**
 * A single cell in a {@link DataTable}.
 *
 * Contains a value, a formatted value, and a dictionary of custom properties.
 * The value should match the type of the column in which it resides. See {@link ColumnDescription}.
 * The formatted value is used for display purposes. The custom properties can be used for any
 * purpose.
 *
 * @author Yoah B.D.
 */
public class TableCell {

  /**
   * The internal value of this cell.
   */
  private Value value;

  /**
   * The formatted value for this cell.
   */
  private String formattedValue = null;

  /**
   * Custom properties for this cell.
   */
  private Map<String, String> customProperties = null;

  /**
   * Returns a comparator that compares table cells according to their inner
   * values and, in the case of text values, according to a given locale.
   *
   * @param ulocale The ULocale defining the order relation of text values.
   *
   * @return A comparator that compares table cells according to their inner
   *     values and, in the case of text values, according to a given locale.
   */
  public static Comparator<TableCell> getLocalizedComparator(
      final ULocale ulocale) {
    return new Comparator<TableCell>() {
      private Comparator<TextValue> textValueComparator =
          TextValue.getTextLocalizedComparator(ulocale);

      @Override
      public int compare(TableCell cell1, TableCell cell2) {
        if (cell1 == cell2) {
          return 0;
        }
        if (cell1.getType() == ValueType.TEXT) {
          return textValueComparator.compare((TextValue) cell1.value,
              (TextValue) cell2.value);
        } else {
          return cell1.getValue().compareTo(cell2.getValue());
        }
      }
    };
  }

  /**
   * Returns the formatted value of this cell.
   *
   * @return The formatted value of this cell.
   */
  public String getFormattedValue() {
    return formattedValue;
  }

  /**
   * Set a formatted value for the value in this table cell.
   *
   * @param formattedValue The formatted value to set.
   */
  public void setFormattedValue(String formattedValue) {
    this.formattedValue = formattedValue;
  }

  /**
   * Construct a new TableCell with the parameter as the inner value.
   *
   * @param value The inner value of this cell.
   */
  public TableCell(Value value) {
    this.value = value;
  }


  /**
   * Construct a new TableCell with the parameter as the inner value and with
   * a FormattedValue representing this.value.
   *
   * @param value The inner value of this cell.
   * @param formattedValue The formatted form of this value.
   */
  public TableCell(Value value, String formattedValue) {
    this.value = value;
    this.formattedValue = formattedValue;
  }

  /**
   * Copy constructor.
   *
   * @param other The other table cell to construct from.
   */
  public TableCell(TableCell other) {
    this(other.value, other.formattedValue);
  }
  /**
   * Constructs a new TableCell with a text value.
   *
   * @param value The inner text value of this cell.
   */
  public TableCell(String value) {
    this.value = new TextValue(value);
  }

  /**
   * Constructs a new TableCell with a boolean value.
   *
   * @param value The inner boolean value of this cell.
   */
  public TableCell(boolean value) {
    this.value = BooleanValue.getInstance(value);
  }

  /**
   * Constructs a new TableCell with a number value.
   *
   * @param value The inner number value of this cell.
   */
  public TableCell(double value) {
    this.value = new NumberValue(value);
  }

  /**
   * Returns the inner value of this cell.
   *
   * @return The inner value of this cell.
   */
  public Value getValue() {
    return value;
  }

  /**
   * Returns the type of this cell.
   *
   * @return The type of this cell.
   */
  public ValueType getType() {
    return value.getType();
  }

  /**
   * Tests whether this cell's value is a logical null.
   *
   * @return Indication whether the call's value is null.
   */
  public boolean isNull() {
    return value.isNull();
  }

  /**
   * Returns the String representation of the inner value.
   *
   * @return The String representation of the inner value.
   */
  @Override
  public String toString() {
    return value.toString();
  }

  /**
   * Retrieves a custom property. Returns null if it does not exist.
   *
   * @param key The property key.
   *
   * @return The property value, or null if it does not exist.
   */
  public String getCustomProperty(String key) {
    if (customProperties == null) {
      return null;
    }
    if (key == null) {
      throw new RuntimeException("Null keys are not allowed.");
    }
    return customProperties.get(key);
  }

  /**
   * Sets a custom property.
   *
   * @param propertyKey The property key.
   * @param propertyValue The property value.
   */
  public void setCustomProperty(String propertyKey, String propertyValue) {
    if (customProperties == null) {
      customProperties = Maps.newHashMap();
    }
    if ((propertyKey == null) || (propertyValue == null)) {
      throw new RuntimeException("Null keys/values are not allowed.");
    }
    customProperties.put(propertyKey, propertyValue);
  }

  /**
   * Returns an immutable map of the custom properties.
   *
   * @return An immutable map of the custom properties.
   */
  public Map<String, String> getCustomProperties() {
    if (customProperties == null) {
      return Collections.emptyMap();
    }
    return Collections.unmodifiableMap(customProperties);
  }

  /**
   * Returns a copy of this TableCell.
   *
   * @return A copy of this TableCell.
   */
  @Override
  public TableCell clone() {
    TableCell result = new TableCell(value, formattedValue);
    if (customProperties != null) {
      result.customProperties = Maps.newHashMap();
      for (Map.Entry<String, String> entry : customProperties.entrySet()) {
        result.customProperties.put(entry.getKey(), entry.getValue());
      }
    }
    return result;
  }
}
