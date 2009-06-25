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

package com.google.visualization.datasource.query;

/**
 * Options definition for a query.
 * Holds options for the query that fall under the options clause.
 *
 * @author Yonatan B.Y.
 */
public class QueryOptions {

  /**
   * Should be set to indicate not to return the values, but only the column
   * labels and types (table description).
   */
  private boolean noValues;

  /**
   * Should be set to indicate not to format the values, but to return only the
   * raw data. By default returns both raw and formatted values.
   */
  private boolean noFormat;

  /**
   * Constructs query options with all boolean options set to false.
   */
  public QueryOptions() {
    noValues = false;
    noFormat = false;
  }

  /**
   * Returns the value of the noValues option.
   *
   * @return The value of the noValues option.
   */
  public boolean isNoValues() {
    return noValues;
  }

  /**
   * Sets the value of the noValues option.
   *
   * @param noValues The new value of the noValues option.
   */
  public void setNoValues(boolean noValues) {
    this.noValues = noValues;
  }

  /**
   * Returns the value of the noFormat option.
   *
   * @return The value of the noFormat option.
   */
  public boolean isNoFormat() {
    return noFormat;
  }

  /**
   * Sets the value of the noFormat option.
   *
   * @param noFormat The new value of the noFormat option.
   */
  public void setNoFormat(boolean noFormat) {
    this.noFormat = noFormat;
  }

  /**
   * Returns true if all options are set to their default values.
   *
   * @return True if all options are set to their default values.
   */
  public boolean isDefault() {
    return !noFormat && !noValues;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (noFormat ? 1231 : 1237);
    result = prime * result + (noValues ? 1231 : 1237);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    QueryOptions other = (QueryOptions) obj;
    if (noFormat != other.noFormat) {
      return false;
    }
    if (noValues != other.noValues) {
      return false;
    }
    return true;
  }
  
  /**
   * Returns a string that when fed to the query parser should return a QueryOptions equal to this
   * one. Used mainly for debugging purposes. The string returned does not contain the
   * OPTIONS keyword.
   * 
   * @return The query string.
   */
  public String toQueryString() {
     return (noValues ? "NO_VALUES" : "") + (noFormat ? "NO_FORMAT" : "");    
  }
}
