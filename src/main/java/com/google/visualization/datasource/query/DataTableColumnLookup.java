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

import com.google.visualization.datasource.datatable.DataTable;

/**
 * An adapter between a data table and a column lookup.
 * The table does not have to contain rows; only the columns data is used.
 *
 * @author Liron L.
 */
public class DataTableColumnLookup implements ColumnLookup {

  /**
   * The table.
   */
  private DataTable table;

  /**
   * Creates a new DataTableColumnLookup with the given data table.
   *
   * @param table The given TableDescription.
   */
  public DataTableColumnLookup(DataTable table) {
    this.table = table;
  }

  @Override
  public int getColumnIndex(AbstractColumn column) {
    return table.getColumnIndex(column.getId());
  }

  @Override
  public boolean containsColumn(AbstractColumn column) {
    return table.containsColumn(column.getId());
  }
}
