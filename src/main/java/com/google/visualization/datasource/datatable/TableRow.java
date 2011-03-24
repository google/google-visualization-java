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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.visualization.datasource.datatable.value.Value;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A single row in a {@link DataTable}.
 *
 * The number of cells is expected to match the number of columns in the table, and the type
 * of the value held in each cell is expected to match the type defined for the corresponding
 * column.
 *
 * @author Yoah B.D.
 */
public class TableRow {

  /**
   * A list of the cells in the row.
   */
  private List<TableCell> cells = Lists.newArrayList();

  /**
   * Custom properties for the row.
   */
  private Map<String, String> customProperties = null;

  /**
   * Create an empty row list.
   */
  public TableRow() {
  }

  /**
   * Adds a single cell to the end of the row.
   *
   * @param cell The cell's value.
   */
  public void addCell(TableCell cell) {
    cells.add(cell);
  }

  /**
   * Adds a value of a single cell to the end of the row.
   *
   * @param value The inner value of the cell to add.
   */
  public void addCell(Value value) {
    addCell(new TableCell(value));
  }

  /**
   * Adds a numeric value of a single cell to the end of the row.
   *
   * @param value The inner numeric value of the cell to add.
   */
  public void addCell(double value) {
    addCell(new TableCell(value));
  }

  /**
   * Adds a boolean value of a single cell to the end of the row.
   *
   * @param value The inner boolean value of the cell to add.
   */
  public void addCell(boolean value) {
    addCell(new TableCell(value));
  }

  /**
   * Adds a text value of a single cell to the end of the row.
   *
   * @param value The inner text value of the cell to add.
   */
  public void addCell(String value) {
    addCell(new TableCell(value));
  }

  /**
   * Returns the list of all cell values.
   *
   * @return The list of all cell values. The returned list is
   *     immutable.
   */
  public List<TableCell> getCells() {
    return ImmutableList.copyOf(cells);
  }

  /**
   * Returns a single cell by its index.
   *
   * @param index The index of the cell to get.
   *
   * @return A single cell by it's index.
   */
  public TableCell getCell(int index) {
    return cells.get(index);
  }
  
  /**
   * Package protected function.
   * Replaces the cell at the specified position in this row with the specified cell.
   * The value type of the new cell must match the that of the replaced one.
   * 
   * @param index The index of the cell to replace.
   * @param cell The cell to be stored at the specified position.
   * 
   * @return The cell that was replaced.
   * 
   * @throws IndexOutOfBoundsException Thrown if the index out of range.
   */
  TableCell setCell(int index, TableCell cell) throws IndexOutOfBoundsException {
    return cells.set(index, cell);
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
   * Returns a clone of this TableRow. This is a deep clone.
   *
   * @return A clone of this TableRow.
   */
  @Override
  public TableRow clone() {
    TableRow result = new TableRow();
    for (TableCell cell : cells) {
      result.addCell(cell.clone());
    }
    if (customProperties != null) {
      result.customProperties = Maps.newHashMap();
      for (Map.Entry<String, String> entry : customProperties.entrySet()) {
        result.customProperties.put(entry.getKey(), entry.getValue());
      }
    }
    return result;
  }
}
