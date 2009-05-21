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

package com.google.visualization.datasource.query.parser;

import com.google.visualization.datasource.query.QueryOptions;

/**
 * Helper enum for the auto-generated parser. Holds a single query option (a single element of the
 * OPTIONS clause). This is used by the parser, i.e., referenced from the QueryParser.jj file.
 * It is needed because queryOptions uses boolean setX() functions, and does not have a datatype
 * to hold a single value, and such a datatype is needed for convenience of parsing purposes in
 * the .jj parser. You can use the setInQueryOptions() method of this enum, to set the option to
 * true in a QueryOptions instance.
 *
 * @author Yonatan B.Y.
 */
/* package */ enum QueryOptionEnum {
  NO_VALUES,
  NO_FORMAT;

  /**
   * Sets this option to true in the given QueryOptions, leaving its other
   * options intact.
   *
   * @param queryOptions The QueryOptions in which to set the option to true.
   */
  public void setInQueryOptions(QueryOptions queryOptions) {
    switch (this) {
      case NO_VALUES:
        queryOptions.setNoValues(true);
        break;
      case NO_FORMAT:
        queryOptions.setNoFormat(true);
        break;
    }
  }
}
