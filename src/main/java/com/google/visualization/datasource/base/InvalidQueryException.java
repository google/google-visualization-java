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
 * An exception that is thrown when trying to serve an invalid query.
 * A query can fail at parsing time, or at later validations.
 *
 * @author Yonatan B.Y.
 * @author Hillel M.
 */
public class InvalidQueryException extends DataSourceException {

  /**
   * Construct a data source exception with an InvalidQuery reason type
   * and a message to the user.
   *
   * @param messageToUser The message for the user.
   */
  public InvalidQueryException(String messageToUser) {
    super(ReasonType.INVALID_QUERY, messageToUser);
  }
}
