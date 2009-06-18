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
import com.google.visualization.datasource.datatable.value.ValueType;

import java.util.Collections;
import java.util.Map;

/**
 * Holds all the information we keep for a single column in a {@link DataTable}. 
 * This does not include all the values, which are kept in {@link TableCell}s. 
 * The information that is included is:
 * <ul>
 * <li>Data type (see {@link ValueType})</li>
 * <li>Id - used mainly for referencing columns in queries (see
 * {@link com.google.visualization.datasource.query.Query}))</li>
 * <li>Label - used mainly for display purposes</li>
 * <li>Custom properties - can be used for any purpose.</li>
 * </ul>
 *
 * @author Yoah B.D.
 */
public class ColumnDescription {

  /**
   * The column's identifier.
   * This Id is used in query sort, filter and other query elements that
   * need to refer to specific columns.
   */
  private String id;

  /**
   * The column's data type.
   */
  private ValueType type;

  /**
   * The column's displayed name.
   */
  private String label;

  /**
   * The column's formatting pattern. The pattern may be empty, otherwise indicates
   * the formatting pattern in which the column is already, or should be, formatted.
   */
  private String pattern;

  /**
   * Custom properties for this column.
   */
  private Map<String, String> customProperties = null;

  /**
   * Creates a new column description.
   *
   * @param id The column's identifier.
   * @param type The column's data type.
   * @param label The column's displayed name (label).
   */
  public ColumnDescription(String id, ValueType type, String label) {
    this.id = id;
    this.type = type;
    this.label = label;
    this.pattern = ""; //empty by default
  }

  /**
   * Returns the column id.
   *
   * @return The column id.
   */
  public String getId() {
    return id;
  }

  /**
   * Returns the column type.
   *
   * @return The column type.
   */
  public ValueType getType() {
    return type;
  }

  /**
   * Returns the column label.
   *
   * @return The column label.
   */
  public String getLabel() {
    return label;
  }

  /**
   * Returns the column pattern.
   *
   * @return The column pattern.
   */
  public String getPattern() {
    return pattern;
  }

  /**
   * Sets a column pattern.
   *
   * @param pattern A pattern to set for this column.
   */
  public void setPattern(String pattern) {
    this.pattern = pattern;
  }

  /**
   * Sets a column label.
   *
   * @param label A label to set for this column.
   */
  public void setLabel(String label) {
    this.label = label;
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
   * Returns a clone of the ColumnDescription, meaning a column descriptor with the exact
   * same properties. This is a deep clone as it copies all custom properties.
   *
   * @return The cloned ColumnDescription.
   */
  @Override
  public ColumnDescription clone() {
    ColumnDescription result = new ColumnDescription(id, type, label);
    result.setPattern(pattern);
    
    if (customProperties != null) {
      result.customProperties = Maps.newHashMap();
      for (Map.Entry<String, String> entry : customProperties.entrySet()) {
        result.customProperties.put(entry.getKey(), entry.getValue());
      }
    }
    return result;
  }
}
