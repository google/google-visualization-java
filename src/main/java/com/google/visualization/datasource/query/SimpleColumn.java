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

import com.google.common.collect.Lists;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.value.ValueType;

import java.util.List;

/**
 * A column referred to by an explicit string ID.
 *
 * @author Yonatan B.Y.
 */
public class SimpleColumn extends AbstractColumn {

  /**
   * The explicit string ID of the column.
   */
  private String columnId;

  /**
   * Creates a new instance of this class, with the given column ID.
   *
   * @param columnId The column ID.
   */
  public SimpleColumn(String columnId) {
    this.columnId = columnId;
  }

  /**
   * Returns the column ID.
   *
   * @return The column ID.
   */
  public String getColumnId() {
    return columnId;
  }

  @Override
  public String getId() {
    return columnId;
  }

  @Override
  public List<String> getAllSimpleColumnIds() {
    return Lists.newArrayList(columnId);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof SimpleColumn) {
      SimpleColumn other = (SimpleColumn) o;
      return columnId.equals(other.columnId);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash  = 1279; // Some arbitrary prime number.
    hash = (hash * 17) + columnId.hashCode();
    return hash;
  }

  @Override
  public String toString() {
    return columnId;
  }

   /**
   * Returns a list of all simple columns. In this case, returns only itself.
   *
   * @return A list of all simple columns.
   */
  @Override
  public List<SimpleColumn> getAllSimpleColumns() {
    return Lists.newArrayList(this);
  }

  /**
   * Returns a list of all aggregation columns. In this case, returns an empty
   * list.
   *
   * @return A list of all aggregation columns.
   */
  @Override
  public List<AggregationColumn> getAllAggregationColumns() {
    return Lists.newArrayList();
  }

  /**
   * Returns a list of all scalar function columns. In this case, returns an
   * empty list.
   *
   * @return A list of all scalar function columns.
   */
  @Override
  public List<ScalarFunctionColumn> getAllScalarFunctionColumns() {
    return Lists.newArrayList();
  }

  /**
   * Checks if the column is valid. In this case always does nothing.
   *
   * @param dataTable The data table.
   */
  @Override
  public void validateColumn(DataTable dataTable) {
  }

  /**
   * Returns the value type of the column. In this case returns the value type
   * of the column itself.
   *
   * @param dataTable The data table.
   *
   * @return the value type of the column.
   */
  @Override
  public ValueType getValueType(DataTable dataTable) {
    return dataTable.getColumnDescription(columnId).getType();
  }

  @Override
  public String toQueryString() {
    if (columnId.contains("`")) {
      throw new RuntimeException("Column ID cannot contain backtick (`)");
    }
    return "`" + columnId + "`";
  }
}
