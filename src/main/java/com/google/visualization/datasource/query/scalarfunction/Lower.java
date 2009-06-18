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

package com.google.visualization.datasource.query.scalarfunction;

import com.google.visualization.datasource.base.InvalidQueryException;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;

import java.util.List;

/**
 * A unary scalar function that changes text to lower case.
 *
 * @author Yaniv S.
 */
public class Lower implements ScalarFunction {

  /**
   * The name of this function.
   */
  private static final String FUNCTION_NAME = "lower";

  /**
   *  A singleton instance of this class.
   */
  private static final Lower INSTANCE = new Lower();

  /**
   * A private constructor, to prevent instantiation other than by the singleton.
   */
  private Lower() {}
  
  /**
   * @return The singleton instance of this class.
   */
  public static Lower getInstance() {
    return INSTANCE;
  }

  /**
   * {@inheritDoc}
   */
  public String getFunctionName() {
    return FUNCTION_NAME;
  }

  /**
   * @param values A list that contains one text value.
   *
   * @return A lower-case version of the input text value.
   */
  public Value evaluate(List<Value> values) {
    return new TextValue(((TextValue) values.get(0)).getValue().toLowerCase());
  }

  /**
   * @return The return type of this function - TEXT.
   */
  public ValueType getReturnType(List<ValueType> types) {
    return ValueType.TEXT;
  }

  /**
    * {@inheritDoc}
    */
  public void validateParameters(List<ValueType> types) throws InvalidQueryException {
    if (types.size() != 1) {
      throw new InvalidQueryException(FUNCTION_NAME +
              " requires 1 parmaeter");
    }
    if (types.get(0) != ValueType.TEXT) {
      throw new InvalidQueryException(FUNCTION_NAME +
              " takes a text parameter");
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public String toQueryString(List<String> argumentsQueryStrings) {
    return FUNCTION_NAME + "(" + argumentsQueryStrings.get(0) + ")"; 
  }
}
