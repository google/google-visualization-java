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
import com.google.visualization.datasource.datatable.TableRow;

import java.util.List;
import java.util.Set;

/**
 * A query filter.
 * Any class that implements this interface can act as a filter, i.e., be the
 * part of a query that decides for a given TableRow if that row is part of the
 * result rows or not.
 *
 * @author Yonatan B.Y.
 */
public interface QueryFilter {

  /**
   * Checks if this row should be part of the result set or not.
   *
   * @param table The table containing this row.
   * @param row The row to check.
   *
   * @return true if this row should be part of the result set, false otherwise.
   */
  boolean isMatch(DataTable table, TableRow row);

  /**
   * Returns all the columnIds this filter uses.
   *
   * @return All the columnIds this filter uses.
   */
  Set<String> getAllColumnIds();

  /**
   * Returns a list of all scalarFunctionColumns this filter uses.
   *
   * @return A list of all scalarFunctionColumns this filter uses.
   */
  List<ScalarFunctionColumn> getScalarFunctionColumns();

  /**
   * Returns a string that when parsed by the query parser, should return an
   * identical filter.
   *
   * @return A string form of this filter.
   */
  String toQueryString();
}
