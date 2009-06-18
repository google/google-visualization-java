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
 * A response status holds three parameters:
 * 1) The response type (Ok, Warning or Error).
 * 2) The response reason type.
 * 3) A string with an error message to the user.
 *
 * @author Hillel M.
 */
public class ResponseStatus {

  /**
   * The response status type.
   */
  private StatusType statusType;

  /**
   * The response reason type (if OK or ERROR).
   */
  private ReasonType reasonType;

  /**
   * A message to be passed to the user (if ERROR).
   */
  private String description;

  /**
   * The sign in message key in the <code>ResourceBundle</code>
   */
  public static final String SIGN_IN_MESSAGE_KEY = "SIGN_IN";

  /**
   * Constructs a response status object.
   * This object contains a status type, reason type,
   * and a message that is sent to the user.
   *
   * @param statusType The response status type.
   * @param reasonType The response reason type.
   * @param description A message to be passed to the user.
   */
  public ResponseStatus(StatusType statusType, ReasonType reasonType, String description) {
    this.statusType = statusType;
    this.reasonType = reasonType;
    this.description = description;
  }

  /**
   * Creates a ResponseStatus for the given DataSourceException.
   *
   * @param dse The data source exception.
   *
   * @return A response status object for the given data source exception.
   */
  public static ResponseStatus createResponseStatus(DataSourceException dse) {
    return new ResponseStatus(StatusType.ERROR, dse.getReasonType(), dse.getMessageToUser());
  }

  /**
   * Gets a modified response status in case of <code>ReasonType</code>#USER_NOT_AUTHENTICATED
   * by adding a sign in html link for the given url. If no url is provided in the
   * <code>ResponseStatus</code># no change is made.
   *
   * @param responseStatus The response status.
   *
   * @return The modified response status if modified, or else the original response status.
   */
  public static ResponseStatus getModifiedResponseStatus(ResponseStatus responseStatus) {

    String signInString = LocaleUtil.getLocalizedMessageFromBundle(
        "com.google.visualization.datasource.base.ErrorMessages", SIGN_IN_MESSAGE_KEY, null);
    if (responseStatus.getReasonType() == ReasonType.USER_NOT_AUTHENTICATED) {
      String msg = responseStatus.getDescription();
      if (!msg.contains(" ")
          && (msg.startsWith("http://")
              || msg.startsWith("https://"))) {
        // The description is assumed to be a link to sign in page, transform the message into
        // an html snippet of a link.
        StringBuilder sb = new StringBuilder("<a target=\"_blank\" href=\"")
            .append(msg).append("\">")
            .append(signInString)
            .append("</a>");
        responseStatus = new ResponseStatus(responseStatus.getStatusType(),
            responseStatus.getReasonType(), sb.toString());
      }
    }
    return responseStatus;
  }

  public ResponseStatus(StatusType statusType) {
    this(statusType, null, null);
  }

  /**
   * Returns the response status type.
   * 
   * @return the response status type.
   */
  public StatusType getStatusType() {
    return statusType;
  }

  /**
   * Returns the response reason type.
   * 
   * @return the response reason type.
   */
  public ReasonType getReasonType() {
    return reasonType;
  }

  /**
   * Returns the message to pass to the user.
   * 
   * @return The message to pass to the user.
   */
  public String getDescription() {
    return description;
  }
}
