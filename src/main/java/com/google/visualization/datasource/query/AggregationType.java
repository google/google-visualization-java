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

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * Enumeration of all possible aggregation types.
 *
 * @author Yoav G.
 * @author Yonatan B.Y.
 */
public enum AggregationType {
  SUM("sum"),
  COUNT("count"),
  MIN("min"),
  MAX("max"),
  AVG("avg");

  /**
   * The code for this AggregationType.
   */
  private String code;

  /**
   * Constructor
   *
   * @param code The code for this AggregationType.
   */
  private AggregationType(String code) {
    this.code = code;
  }

  /**
   * Get this AggregationType code.
   *
   * @return The AggregationType code.
   */
  public String getCode() {
    return code;
  }

  /**
   * Returns the correct AggregationType for a given code.
   *
   * @param code The code for a type.
   *
   * @return The correct AggregationType for the given code.
   */
  public static AggregationType getByCode(String code) {
    return codeToAggregationType.get(code);
  }

  /**
   * Holds the mapping of type code to instances of this class.
   */
  private static Map<String, AggregationType> codeToAggregationType;

  /**
   * Initializes the static maps.
   */
  static {
    codeToAggregationType = Maps.newHashMap();
    for (AggregationType type : AggregationType.values()) {
      codeToAggregationType.put(type.code, type);
    }
  }
}
