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

import com.google.common.collect.Maps;
import com.google.visualization.datasource.datatable.TableCell;

import java.util.Map;

/**
 * Holds the data of a table after pivoting and grouping, indexed by {@link RowTitle} and
 * {@link ColumnTitle}.
 *
 * @author Yonatan B.Y.
 */
/* package */ class MetaTable {

  /**
   * The data. A map that gives, for each row and column, a map of the cell 
   * associated with that row and that column.
   */
  private Map<RowTitle, Map<ColumnTitle, TableCell>> data;

  /**
   * Creates an empty instance.
   */
  public MetaTable() {
    data = Maps.newHashMap();
  }

  /**
   * Puts a new value in the MetaTable.
   *
   * @param rowTitle The row into which the cell should be inserted.
   * @param columnTitle The column into which the cell should be inserted.
   * @param cell The cell to insert.
   */
  public void put(RowTitle rowTitle, ColumnTitle columnTitle,
      TableCell cell) {
    Map<ColumnTitle, TableCell> rowData = data.get(rowTitle);
    if (rowData == null) {
      rowData = Maps.newHashMap();
      data.put(rowTitle, rowData);
    }
    rowData.put(columnTitle, cell);
  }

  /**
   * Retrieves a cell from the MetaTable.
   *
   * @param rowTitle The row from which to retrieve the cell.
   * @param columnTitle The column from which to retrieve the cell.
   *
   * @return The cell that is at row rowTitle and column columnTitle or null
   *     if no such cell exists.
   */
  public TableCell getCell(RowTitle rowTitle, ColumnTitle columnTitle) {
    Map<ColumnTitle, TableCell> rowData = data.get(rowTitle);
    if (rowData == null) {
      return null;
    }
    return rowData.get(columnTitle);
  }

  /**
   * Retrieves an entire row, in the form of a hashtable that maps a ColumnTitle
   * to a TableCell.
   *
   * @param rowTitle The title of the row to retrieve.
   *
   * @return The row.
   */
  public Map<ColumnTitle, TableCell> getRow(RowTitle rowTitle) {
    return data.get(rowTitle);
  }

  /**
   * Returns true if this MetaTable is empty, i.e., contains no rows.
   *
   * @return True if this MetaTable is empty.
   */
  public boolean isEmpty() {
    return data.isEmpty();
  }
}
