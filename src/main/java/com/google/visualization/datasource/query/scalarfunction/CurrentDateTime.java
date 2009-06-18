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
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;

import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;

import java.util.List;

/**
 * A 0-ary function that returns the current datetime. The return type is always DateTime.
 *
 * @author Liron L.
 */
public class CurrentDateTime implements ScalarFunction {

  /**
   * The name of this function.
   */
  private static final String FUNCTION_NAME = "now";

  /**
   *  A singleton instance of this class.
   */
  private static final CurrentDateTime INSTANCE = new CurrentDateTime();

  /**
   * Private constructor, to prevent instantiation other than by the singleton.
   */
  private CurrentDateTime() {}

  /**
   * Returns the singleton instance of this class.
   *
   * @return The singleton instance of this class.
   */
  public static CurrentDateTime getInstance() {
    return INSTANCE;
  }

  /**
   * {@inheritDoc}
   */
  public String getFunctionName() {
    return FUNCTION_NAME;
  }

  /**
   * Evaluates this scalar function. Returns a DateTime with the current time.
   *
   * @param values Ignored.
   *
   * @return A DateTime value with the current time.
   */
  public Value evaluate(List<Value> values) {
    return new DateTimeValue(new GregorianCalendar(
        TimeZone.getTimeZone("GMT")));
  }

  /**
   * Returns the return type of the function. In this case, DATETIME.
   *
   * @param types Ignored.
   *
   * @return The type of the returned value: DATETIME.
   */
  public ValueType getReturnType(List<ValueType> types) {
    return ValueType.DATETIME;
  }

  /**
   * Validates that there are no parameters given for the function. Throws a
   * ScalarFunctionException otherwise.
   *
   * @param types A list with parameters types. Should be empty for this type of function.
   *
   * @throws InvalidQueryException Thrown if the parameters are invalid.
   */
  public void validateParameters(List<ValueType> types)
      throws InvalidQueryException {
    if (types.size() != 0) {
      throw new InvalidQueryException("The " + FUNCTION_NAME + " function should not get "
          + "any parameters");
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public String toQueryString(List<String> argumentsQueryStrings) {
    return FUNCTION_NAME + "()"; 
  }
}
