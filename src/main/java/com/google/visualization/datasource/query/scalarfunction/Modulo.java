// Copyright 2010 Google Inc.
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
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;

import java.util.List;

/**
 * The binary scalar function modulo().
 * Returns the modulo between two number values.
 *
 * @author Roee E.
 */
public class Modulo implements ScalarFunction {
  
  /**
   * The name of this function.
   */
  private static final String FUNCTION_NAME = "modulo";

  /**
   *  A singleton instance of this class.
   */
  private static final Modulo INSTANCE = new Modulo();

  /**
   * A private constructor, to prevent instantiation other than by the singleton.
   */
  private Modulo() {}

  /**
   * Returns the singleton instance of this class.
   *
   * @return The singleton instance of this class.
   */
  public static Modulo getInstance() {
    return INSTANCE;
  }

  /**
   * {@inheritDoc}
   */
  public String getFunctionName() {
    return FUNCTION_NAME;
  }

  /**
   * Executes a binary scalar function modulo() between the first and the second
   * values in the list. Returns the modulo between the given values. All values
   * are number values. The method does not validate the parameters,
   * the user must check the parameters before calling this method.
   *
   * @param values A list of values on which the scalar function is performed.
   *
   *
   * @return Value with the modulo between two given values, or number null value
   *     if one of the values is null.
   */
  public Value evaluate(List<Value> values) {
    if (values.get(0).isNull() || values.get(1).isNull()) {
      return NumberValue.getNullValue();
    }
    double modulo = ((NumberValue) values.get(0)).getValue() %
        ((NumberValue) values.get(1)).getValue();
    return new NumberValue(modulo);
  }

  /**
   * Returns the return type of the function. In this case, NUMBER. The method
   * does not validate the parameters, the user must check the parameters
   * before calling this method.
   *
   * @param types A list of the types of the scalar function parameters.
   *
   * @return The type of the returned value: Number.
   */
  public ValueType getReturnType(List<ValueType> types) {
    return ValueType.NUMBER;
  }

  /**
   * Validates that all function parameters are of type NUMBER, and that there
   * are exactly 2 parameters. Throws a ScalarFunctionException otherwise.
   *
   * @param types A list with parameters types.
   *
   * @throws InvalidQueryException Thrown if the parameters are invalid.
   */
  public void validateParameters(List<ValueType> types) throws InvalidQueryException {
    if (types.size() != 2) {
      throw new InvalidQueryException("The function " + FUNCTION_NAME
          + " requires 2 parmaeters ");
    }
    for (ValueType type : types) {
      if (type != ValueType.NUMBER) {
        throw new InvalidQueryException("Can't perform the function "
            + FUNCTION_NAME + " on values that are not numbers");
      }
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public String toQueryString(List<String> argumentsQueryStrings) {
    return "(" + argumentsQueryStrings.get(0) + " % " + argumentsQueryStrings.get(1) + ")"; 
  }
}