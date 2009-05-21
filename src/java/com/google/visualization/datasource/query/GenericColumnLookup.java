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

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * This class represents a generic column lookup. It maps an abstract
 * column to its index in a table description.
 *
 * @author Liron L.
 */
public class GenericColumnLookup implements ColumnLookup {

  /**
   * A map from the column to its index in the table description.
   */
  private Map<AbstractColumn, Integer> columnIndexByColumn;

  /**
   * Create a GenericColumnLookup (with an empty map).
   */
  public GenericColumnLookup() {
    columnIndexByColumn = Maps.newHashMap();
  }

  /**
   * Clear all data of this GenericColumnLookup.
   */
  public void clear() {
    columnIndexByColumn.clear();
  }

  /**
   * Sets the index of the given column to the given number.
   *
   * @param col The column to set the index of.
   * @param index The index to set for the column.
   */
  public void put(AbstractColumn col, int index) {
    columnIndexByColumn.put(col, index);
  }

  /**
   * Returns the column index in the columns of a row (first is zero).
   *
   * @param column The column.
   *
   * @return The column index in the columns of a row (first is zero).
   */
  public int getColumnIndex(AbstractColumn column) {
    return columnIndexByColumn.get(column);
  }

  @Override
  public boolean containsColumn(AbstractColumn column) {
    return columnIndexByColumn.containsKey(column);
  }
}
