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

package com.google.visualization.datasource;

/**
 * An enumeration of the capabilities provided by a data source. A data source declares the
 * capabilities it supports and this information determines how a query is split.
 * For example if a data source declares Capibilities.SELECT then the following
 * query <code>select A,B sort A limit 20</code>
 * will be split as follows:
 *  query for data source - <code>'select A,B'</code>
 *  completion query - <code>'sort A limit 20' </code>
 *
 * @author Yonatan B.Y.
 */
public enum Capabilities {

  /**
   * Supports: filter, sort, group, limit, and offset.
   * Does not support: pivot, options, labels, format or scalar functions.
   */
  SQL,

  /**
   * Supports: sort, limit, and offset over simple columns.
   * Simple columns are those that are not aggregation columns (such as max(a),
   * count(b), avg(c)) or scalar function columns (like a+b, c*2, year(d)).
   * If calculated columns are created, through scalar functions or aggregation for example,
   * the completion query handles the SORT_AND_PAGINATION over the newly created columns.
   */
  SORT_AND_PAGINATION,

  /**
   * Supports: select over simple columns.
   *
   * If calculated columns are created, through scalar functions or aggregation for example,
   * the completion query handles the SELECT over the newly created columns.
   */
  SELECT,

  /**
   * Supports all the above capabilities: SQL. SORT_AND_PAGINATION, and SELECT.
   */
  ALL,

  /**
   * Supports no capabilities.
   */
  NONE
}
