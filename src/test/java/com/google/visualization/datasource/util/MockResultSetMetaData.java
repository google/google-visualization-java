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

package com.google.visualization.datasource.util;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import java.util.List;

/**
 * A mock result set meta data for SqlDataSource unit tests.
 *
 * @author Liron L.
 */
public class MockResultSetMetaData implements ResultSetMetaData {

  /**
   * The number of columns in the table.
   */
  int columnCount;

  /**
   * Column labels.
   * */
  List<String> labels;

  /**
   * Column types.
   */
  List<Integer> types;

  /**
   * Constructor.
   *
   * @param columnCount The number of columns in the table.
   * @param labels The column labels.
   * @param types The column Types.
   */
  public MockResultSetMetaData(int columnCount, List<String> labels,
      List<Integer> types) {
    this.columnCount = columnCount;
    this.labels = labels;
    this.types = types;
  }

  /**
   * Returns the number of columns in the table.
   *
   * @return the number of columns in the table.
   */
  public int getColumnCount() {
    return columnCount;
  }

  public boolean isAutoIncrement(int column) {
    throw new UnsupportedOperationException("This operation is unsupported.");
  }

  public boolean isCaseSensitive(int column) {
    throw new UnsupportedOperationException("This operation is unsupported.");
  }

  public boolean isSearchable(int column) {
    throw new UnsupportedOperationException("This operation is unsupported.");
  }

  public boolean isCurrency(int column) {
    throw new UnsupportedOperationException("This operation is unsupported.");
  }

  public int isNullable(int column) {
    throw new UnsupportedOperationException("This operation is unsupported.");
  }

  public boolean isSigned(int column) {
    throw new UnsupportedOperationException("This operation is unsupported.");
  }

  public int getColumnDisplaySize(int column) {
    throw new UnsupportedOperationException("This operation is unsupported.");
  }

  /**
   * Returns the column label.
   *
   * @param column The column index. SQL indexes are 1-based.
   *
   * @return the column label.
   *
   * @throws SQLException Thrown when the column index is out of bounds.
   */
  public String getColumnLabel(int column) throws SQLException {
    if (column > columnCount) {
      throw new SQLException("The index column is out of bounds. Index = "
          + column + ", number of rows = " + columnCount);
    }
    return labels.get(column - 1);
  }

  public String getColumnName(int column) {
    throw new UnsupportedOperationException("This operation is unsupported.");
  }

  public String getSchemaName(int column) {
    throw new UnsupportedOperationException("This operation is unsupported.");
  }

  public int getPrecision(int column) {
    throw new UnsupportedOperationException("This operation is unsupported.");
  }

  public int getScale(int column) {
    throw new UnsupportedOperationException("This operation is unsupported.");
  }

  public String getTableName(int column) {
    throw new UnsupportedOperationException("This operation is unsupported.");
  }

  public String getCatalogName(int column) {
    throw new UnsupportedOperationException("This operation is unsupported.");
  }

  /**
   * Returns the column type.
   *
   * @param column The column index.
   *
   * @return the column type.
   *
   * @throws SQLException Thrown when the column index is out of bounds.
   */
  public int getColumnType(int column) throws SQLException {
    if (column > columnCount) {
      throw new SQLException("The index column is out of bounds. Index = " +
          column + ", number of rows = " + columnCount);
    }
    return types.get(column - 1);
  }

  public String getColumnTypeName(int column) {
    throw new UnsupportedOperationException("This operation is unsupported.");
  }

  public boolean isReadOnly(int column) {
    throw new UnsupportedOperationException("This operation is unsupported.");
  }

  public boolean isWritable(int column) {
    throw new UnsupportedOperationException("This operation is unsupported.");
  }

  public boolean isDefinitelyWritable(int column) {
    throw new UnsupportedOperationException("This operation is unsupported.");
  }

  public String getColumnClassName(int column) {
    throw new UnsupportedOperationException("This operation is unsupported.");
  }

  public <T> T unwrap(Class<T> iface) {
    throw new UnsupportedOperationException("This operation is unsupported.");
  }

  public boolean isWrapperFor(Class<?> iface) {
    throw new UnsupportedOperationException("This operation is unsupported.");
  }
}
