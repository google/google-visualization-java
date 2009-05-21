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
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;

import java.util.List;

/**
 * A constant function, i.e., a 0-ary function that always returns the same value, given at
 * construction time. The value can be of any supported {@link ValueType}.
 *
 * @author Liron L.
 */
public class Constant implements ScalarFunction {

  /**
   * The value of this constant.
   */
  private Value value;

  /**
   * Constructs a new constant function that always returns the given value.
   * 
   * @param value The value to constantly return.
   */
  public Constant(Value value) {
    this.value = value;
  }

  /**
   * {@inheritDoc}
   */
  public String getFunctionName() {
    return value.toQueryString();
  }

  /**
   * Executes the scalar function constant(). The <code>values</code> parameter is ignored.
   * Returns the value supplied at construction time.
   *
   * @param values Ignored.
   *
   * @return The value supplied at construction time.
   */
  public Value evaluate(List<Value> values) {
    return value;
  }

  /**
   * Returns the return type of the function. This matches the type of the value
   * supplied at construction time. The <code>types</code> parameter is ignored.
   *
   * @param types Ignored.
   *
   * @return The return type of this function.
   */
  public ValueType getReturnType(List<ValueType> types) {
    return value.getType();
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
      throw new InvalidQueryException("The constant function should not get "
          + "any parameters");
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof Constant) {
      Constant other = (Constant) o;
      return value.equals(other.value);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return (value == null) ? 0 : value.hashCode(); 
  }
  
  /**
   * {@inheritDoc}
   */
  public String toQueryString(List<String> argumentsQueryStrings) {
    return value.toQueryString(); 
  }
}
