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
 * An exception to be used by callers and callees of the library.
 * Each exception has a type taken from <code>ReasonType</code>, and a
 * message that can be used to output an appropriate message to the user.
 *
 * Example:
 * new DataSourceException(ReasonType.InvalidQuery, "The query cannot be empty")
 *
 * @author Hillel M.
 */

public class DataSourceException extends Exception {

  /**
   * The reason for this exception. Used to set the reason type of this
   * execution response by the thrower of this exception.
   */
  private ReasonType reasonType;
  
  /**
   * The error message to return to the user.
   */
  private String messageToUser = null;
  
  /**
   * A private constructor to prevent using this exception with no message.
   */
  private DataSourceException() {}

  /**
   * Constructs a new exception with a single message for the user.
   *
   * @param reasonType The reason type of the exception.
   * @param messageToUser The message for the user.
   */
  public DataSourceException(ReasonType reasonType, String messageToUser) {
    super(messageToUser);
    this.messageToUser = messageToUser;
    this.reasonType = reasonType;
  }

  /**
   * Returns the message for the user.
   *
   * @return The message for the user.
   */
  public String getMessageToUser() {
    return messageToUser;
  }

  /**
   * Returns the reason type of this exception.
   *
   * @return The reason type of this exception.
   */
  public ReasonType getReasonType() {
    return reasonType;
  }

  @Override @Deprecated
  public String getMessage() {
    return super.getMessage();
  }
}
