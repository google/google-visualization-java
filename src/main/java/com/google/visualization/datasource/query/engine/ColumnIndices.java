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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.visualization.datasource.query.AbstractColumn;

import java.util.List;

/**
 * Holds a mapping between {@link AbstractColumn}s and lists of indices, i.e., holds a list of
 * indices for each column. The indices for a column are the indices in the new datatable, generated
 * by a grouping/pivoting operation. There may be many indices for one column because a pivoting
 * operation creates a few columns for a single original column.
 *
 * @author Yonatan B.Y.
 */
/* package */ class ColumnIndices {

  /**
   * The indices of the columns.
   */
  private ArrayListMultimap<AbstractColumn, Integer> columnToIndices;
  /**
   * Creates an empty instance of this class.
   */
  public ColumnIndices() {
    columnToIndices = ArrayListMultimap.create();
  }

  /**
   * Sets the index of the given column to the given number.
   *
   * @param col The column to set the index of.
   * @param index The index to set for the column.
   */
  public void put(AbstractColumn col, int index) {
    columnToIndices.put(col, index);
  }

  /**
   * Returns the index of the given column. If there is more than one index for
   * the column, a runtime exception is thrown.
   *
   * @param col The column to look for.
   *
   * @return The index of the column.
   */
  public int getColumnIndex(AbstractColumn col) {
    List<Integer> indices = columnToIndices.get(col);
    if (indices.size() != 1) {
      throw new RuntimeException("Invalid use of ColumnIndices.");
    }
    return indices.get(0);
  }

  /**
   * Returns the indices of the given column.
   *
   * @param col The column to look for.
   *
   * @return The indeices of the column.
   */
  public List<Integer> getColumnIndices(AbstractColumn col) {
    return ImmutableList.copyOf(columnToIndices.get(col));
  }

  /**
   * Clears the entire map.
   */
  public void clear() {
    columnToIndices.clear();
  }
}
