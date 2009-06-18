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

/**
 * A column lookup.
 * Maps columns to their index in a column container (e.g., DateTable).
 *
 * @author Liron L.
 */
public interface ColumnLookup {

  /**
   * Returns the index of the given column.
   *
   * @param column The given AbstractColumn.
   *
   * @return The index of the given column.
   */
  public int getColumnIndex(AbstractColumn column);

  /**
   * Returns whether or not this ColumnLookup contains the given column.
   *
   * @param column The column to check.
   *
   * @return True if column exists, false otherwise.
   */
  public boolean containsColumn(AbstractColumn column);
}
