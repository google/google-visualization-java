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
 * A warning generated while processing a request.
 *
 * @author Yonatan B.Y.
 */
public class Warning {

  /**
   * The reason for this warning.
   */
  private ReasonType reasonType;

  /**
   * The warning message to return to the user.
   */
  private String messageToUser;

  /**
   * Constructs a new exception with a reason type and a message for the user.
   *
   * @param reasonType The reason type of the exception.
   * @param messageToUser The message to the user.
   */
  public Warning(ReasonType reasonType, String messageToUser) {
    this.messageToUser = messageToUser;
    this.reasonType = reasonType;
  }

  /**
   * Returns the reason.
   *
   * @return The reason.
   */
  public ReasonType getReasonType() {
    return reasonType;
  }

  /**
   * Returns the message.
   * 
   * @return The message.
   */
  public String getMessage() {
    return messageToUser;
  }
}
