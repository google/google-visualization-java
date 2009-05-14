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
 * Enumeration of the supported output formats for the data source.
 * This enumeration almost exactly correlates to the "out" parameter of the wire protocol, except
 * JSONP which is never explicitly specified in the "out" parameter.
 * 
 * @author Nimrod T.
 */
public enum OutputType {
  HTML("html"),
  JSON("json"),
  JSONP("jsonp"),
  CSV("csv"),
  
  /**
   * Output type value for tab-separated values encoded using UTF-16
   * little-endian with byte-order mark supported by Microsoft Excel.
   * 
   * UTF-8 encoding used by {@link #CSV} output type is not supported by Excel.
   * The Unicode encoding understood by Excel is UTF-16 little-endian with
   * byte-order mark. Excel also does not support comma delimiter in UTF-16
   * encoded files, however, it supports tab delimiter.
   * 
   * The default output filename for Excel should have a {@code .csv} extension in
   * spite of containing tab-separated values because Excel does not
   * automatically associate with files having {@code .tsv} extension.
   */
  TSV_EXCEL("tsv-excel");

  /**
   * The code used to encode the output type in the tqx parameter.
   */
  private String code;

  /**
   * Constructs a new instance of this class with the given code.
   * 
   * @param code Used to encode the output type in the tqx parameter.
   */
  OutputType(String code) {
    this.code = code;
  }

  /**
   * Returns the code for this OutputType.
   * 
   * @return The code for this OutputType.
   */
  public String getCode() {
    return code;
  }

  /**
   * Finds the OutputType that matches the given code.
   *
   * @param code The code to search for.
   *
   * @return The OutputType that matches the given code or null if none are found.
   */
  public static OutputType findByCode(String code) {
    for (OutputType t : values()) {
      if (t.code.equals(code)) {
        return t;
      }
    }
    return null;
  }

  /**
   * Returns the default OutputType.
   * 
   * @return The default OutputType.
   */
  public static OutputType defaultValue() {
    return JSON;
  }
}
