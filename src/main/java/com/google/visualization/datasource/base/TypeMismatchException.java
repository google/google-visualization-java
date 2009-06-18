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
 * This exception is thrown when there is a mismatch between a value type and a column type,
 * for example when trying to assign a boolean value to a column of type number.
 *
 * @author Yaniv S.
 */
public class TypeMismatchException extends DataSourceException {

  /**
   * Constructs a new exception with OTHER reason type and a message for the user.
   *
   * @param message The message for the user.
   */
  public TypeMismatchException(String message) {
    super(ReasonType.OTHER, message);
  }
}
