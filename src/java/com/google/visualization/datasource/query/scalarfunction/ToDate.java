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
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;

import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;

import java.util.Date;
import java.util.List;

/**
 * The unary scalar function toDate().
 * Transforms the given value to date value.
 * If the given value is already a date, it's trivial.
 * If the given value is a date-time, it extracts its date part.
 * If the given value is number, it treats that number as the number of milliseconds since the
 * Epoch, and returns the corresponding date.
 *
 * @author Liron L.
 */
public class ToDate implements ScalarFunction {

  /**
   * The name of the function.
   */
  private static final String FUNCTION_NAME = "toDate";

  /**
   *  A singleton instance of this class.
   */
  private static final ToDate INSTANCE = new ToDate();

  /**
   * A private constructor.
   */
  private ToDate() {}

  /**
   * Returns the singleton instance of this class.
   *
   * @return The singleton instance of this class.
   */
  public static ToDate getInstance() {
    return INSTANCE;
  }

  /**
   * {@inheritDoc}
   */
  public String getFunctionName() {
    return FUNCTION_NAME;
  }

  /**
   * Executes the scalar function toDate() on the given values. The list is expected to be
   * of length 1, and the value in it should be of an appropiate type.
   * The method does not validate the parameters, the user must check the
   * parameters before calling this method.
   *
   * @param values A list with the values that the scalar function is performed on them.
   *
   * @return A date value with the appropriate date.
   */
  public Value evaluate(List<Value> values) {
    Value value = values.get(0);
    Date date;
    GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("GMT"));

    // If the given value is null, return a null date value.
    if (value.isNull()) {
      return DateValue.getNullValue();
    }
    DateValue dateValue;
    switch(value.getType()) {
      case DATE:
        dateValue = (DateValue) value;
        break;
      case DATETIME:
        dateValue = new DateValue((GregorianCalendar)
            (((DateTimeValue) value).getObjectToFormat()));
        break;
      case NUMBER:
        date = new Date((long) ((NumberValue) value).getValue());
        gc.setTime(date);
        dateValue = new DateValue(gc);
        break;
      default:// Should never get here.
        throw new RuntimeException("Value type was not found: " + value.getType());
    }
    return dateValue;
  }

  /**
   * Returns the return type of the function. In this case, DATE. The method
   * does not validate the parameters, the user must check the parameters
   * before calling this method.
   *
   * @param types A list of the types of the scalar function parameters.
   *
   * @return The type of the returned value: DATE.
   */
  public ValueType getReturnType(List<ValueType> types) {
    return ValueType.DATE;
  }

  /**
   * Validates that there is only 1 parameter given for this function, and
   * that its type is either DATE, DATETIME or NUMBER. Throws a
   * ScalarFunctionException if the parameters are invalid.
   *
   * @param types A list with parameters types.
   *
   * @throws InvalidQueryException Thrown if the parameters are invalid.
   */
  public void validateParameters(List<ValueType> types) throws InvalidQueryException {
    if (types.size() != 1) {
      throw new InvalidQueryException("Number of parameters for the date "
          + "function is wrong: " + types.size());
    } else if ((types.get(0) != ValueType.DATETIME)
        && (types.get(0) != ValueType.DATE)
        && (types.get(0) != ValueType.NUMBER)) {
      throw new InvalidQueryException("Can't perform the function 'date' "
          + "on values that are not date, dateTime or number values");
    }
  }
  
  /**
   * {@inheritDoc}
   */
  public String toQueryString(List<String> argumentsQueryStrings) {
    return FUNCTION_NAME + "(" + argumentsQueryStrings.get(0) + ")"; 
  }
}
