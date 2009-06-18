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

package com.google.visualization.datasource.base;

/**
 * An enum value to represent the status of a response.
 *
 * @author Hillel M.
 */
public enum StatusType {

  /**
   * The query completed successfully and the data can be returned.
   */
  OK,

  /**
   * The query failed to complete. In this case, no data table is passed in the response.
   */
  ERROR,

  /**
   * The query completed with a warning. In some cases, part of the data is returned.
   */
  WARNING;

  /**
   * Returns a lower case string of this enum.
   *
   * @return a lower case string of this enum.
   */
  public String lowerCaseString() {
    return this.toString().toLowerCase();
  }
}
