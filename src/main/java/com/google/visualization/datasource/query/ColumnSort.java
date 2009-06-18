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
 * A sort definition for a single column.
 * This class is immutable.
 *
 * @author Yoah B.D.
 */
public class ColumnSort {

  /**
   * The column by which to sort.
   */
  private AbstractColumn column;

  /**
   * The requested ordering.
   */
  private SortOrder order;

  /**
   * Construct and new column sort condition.
   * @param column The column by which to sort.
   * @param order The requested ordering.
   */
  public ColumnSort(AbstractColumn column, SortOrder order) {
    this.column = column;
    this.order = order;
  }

  /**
   * Returns the column by which to sort.
   * @return The column by which to sort.
   */
  public AbstractColumn getColumn() {
    return column;
  }

  /**
   * Returns the requested ordering.
   * @return The requested ordering.
   */
  public SortOrder getOrder() {
    return order;
  }
  
  /**
   * Creates a string that when fed to the query parser should return a ColumnSort equal to this
   * one. Used mainly for debugging purposes.
   * @return A query string.
   */
  public String toQueryString() {
    return column.toQueryString() + (order == SortOrder.DESCENDING ? " DESC" : "");   
  }
}
