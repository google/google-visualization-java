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

import com.google.common.collect.Maps;

import java.util.Locale;
import java.util.Map;

/**
 * Possible reasons for errors or warnings.
 *
 * @author Hillel M.
 */
public enum ReasonType {

  /**
   * The user has no access to the requested data.
   */
  ACCESS_DENIED,

  /**
   * The user cannot be authenticated. Used when the data requires
   * authentication. Enable the data source to distinguish between 
   * 'no user' and 'user has no access' scenarios.
   */
  USER_NOT_AUTHENTICATED,

  /**
   * The query sent to the data source contains an operation that
   * the data source does not support.
   */
  UNSUPPORTED_QUERY_OPERATION,

  /**
   * The query sent to the data source contains invalid data.
   */
  INVALID_QUERY,

  /**
   * The request from the client is invalid.
   */
  INVALID_REQUEST,

  /**
   * An internal error occured.
   */
  INTERNAL_ERROR,

  /**
   * This operation is not supported.
   */
  NOT_SUPPORTED,

  /**
   * Not all data is retrieved.
   */
  DATA_TRUNCATED,

  /**
   * The data hasn't been changed (signatures are the same).
   */
  NOT_MODIFIED,

  /**
   * The request has timed out. This is used only in the client, it is defined here for
   * completeness.
   */
  TIMEOUT,

  /**
   * Illegal user given formatting patterns.
   */
  ILLEGAL_FORMATTING_PATTERNS,

  /**
   * Any other error that occured and prevented the data source from completing the action.
   */
  OTHER;

  /**
   * A mapping from reason type to message.
   */
  private static final Map<ReasonType, String>
      REASON_TYPE_TO_MESSAGE = Maps.newEnumMap(ReasonType.class);

  static {
    REASON_TYPE_TO_MESSAGE.put(ReasonType.ACCESS_DENIED,
        "ACCESS_DENIED");
    REASON_TYPE_TO_MESSAGE.put(ReasonType.USER_NOT_AUTHENTICATED,
        "USER_NOT_AUTHENTICATED");
    REASON_TYPE_TO_MESSAGE.put(ReasonType.UNSUPPORTED_QUERY_OPERATION,
        "UNSUPPORTED_QUERY_OPERATION");
    REASON_TYPE_TO_MESSAGE.put(ReasonType.INVALID_QUERY,
        "INVALID_QUERY");
    REASON_TYPE_TO_MESSAGE.put(ReasonType.INVALID_REQUEST,
        "INVALID_REQUEST");
    REASON_TYPE_TO_MESSAGE.put(ReasonType.INTERNAL_ERROR,
        "INTERNAL_ERROR");
    REASON_TYPE_TO_MESSAGE.put(ReasonType.NOT_SUPPORTED,
        "NOT_SUPPORTED");
    REASON_TYPE_TO_MESSAGE.put(ReasonType.DATA_TRUNCATED,
        "DATA_TRUNCATED");
    REASON_TYPE_TO_MESSAGE.put(ReasonType.NOT_MODIFIED,
        "NOT_MODIFIED");
    REASON_TYPE_TO_MESSAGE.put(ReasonType.TIMEOUT,
        "TIMEOUT");
    REASON_TYPE_TO_MESSAGE.put(ReasonType.ILLEGAL_FORMATTING_PATTERNS,
        "ILLEGAL_FORMATTING_PATTERNS");
    REASON_TYPE_TO_MESSAGE.put(ReasonType.OTHER,
        "OTHER");
  }

  /**
   * Returns a localized message for this reason type and locale.
   *
   * @param locale The locale.
   *
   * @return A localized message given a reason type and locale.
   */
  public String getMessageForReasonType(Locale locale) {
    return LocaleUtil.getLocalizedMessageFromBundle(
        "com.google.visualization.datasource.base.ErrorMessages", REASON_TYPE_TO_MESSAGE.get(this),
        locale);
  }

  /**
   * Returns a message for this reason type in the default locale.
   *
   * @return A message message for this reason type in the default locale.
   */
  public String getMessageForReasonType() {
    return getMessageForReasonType(null);
  }
  
  /**
   * Returns a lower case string of this enum.
   *
   * @return a lower case string of this enum
   */
  public String lowerCaseString() {
    return this.toString().toLowerCase();
  }  
}
