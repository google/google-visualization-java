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

import java.util.ListResourceBundle;

/**
 * A resource bundle that contains all the error messages for a datasource in the en-US locale.
 * This is the default locale used in this library.
 *
 * @author Yaniv S.
 */
public class ErrorMessages extends ListResourceBundle {

  /**
   * The contents of this bundle. A key to message map.
   */
  static final Object[][] CONTENTS = {
    {"UNKNOWN_DATA_SOURCE_ID", "Unknown data source ID"},
    {"ACCESS_DENIED", "Access denied"},
    {"USER_NOT_AUTHENTICATED", "User not signed in"},
    {"UNSUPPORTED_QUERY_OPERATION", "Unsupported query operation"},
    {"INVALID_QUERY", "Invalid query"},
    {"INVALID_REQUEST", "Invalid request"},
    {"INTERNAL_ERROR", "Internal error"},
    {"NOT_SUPPORTED", "Operation not supported"},
    {"DATA_TRUNCATED", "Retrieved data was truncated"},
    {"NOT_MODIFIED", "Data not modified"},
    {"TIMEOUT", "Request timeout"},
    {"ILLEGAL_FORMATTING_PATTERNS", "Illegal formatting patterns"},
    {"OTHER", "Could not complete request"},
    {"SIGN_IN", "Sign in"}  
  };

  /**
   * Returns the error messages.
   * Note that this method exposes the inner array. This means it can be changed by the outside
   * world. We are not cloning here to avoid the computation time hit. Please do not change the
   * inner values unless you know what you are doing. 
   */
  @Override
  public Object[][] getContents() {
    return CONTENTS;
  }
}
