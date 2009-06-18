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
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;

import java.util.Date;
import java.util.List;

/**
 * The binary scalar function datediff().
 * Returns the difference in days between two dates or date time values.
 *
 * @author Liron L.
 */
public class DateDiff implements ScalarFunction {

  /**
   * The name of the function.
   */
  private static final String FUNCTION_NAME = "dateDiff";

  /**
   *  A singleton instance of this class.
   */
  private static final DateDiff INSTANCE = new DateDiff();

  /**
   * A private constructor, to prevent instantiation other than by the singleton.
   */
  private DateDiff() {}

  /**
   * Returns the singleton instance of this class.
   *
   * @return The singleton instance of this class.
   */
  public static DateDiff getInstance() {
    return INSTANCE;
  }

  /**
   * {@inheritDoc}
   */
  public String getFunctionName() {
    return FUNCTION_NAME;
  }

  /**
   * Executes this scalar function on the given values. Returns values[0] - values[1] expressed
   * as a number value denoting the number of days from one date to the other. Both values can be
   * of type date or date-time. Only the date parts of date-time values are used in the calculation.
   * Thus the returned number is always an integer.
   * The method does not validate the parameters, the user must check the
   * parameters before calling this method.
   *
   * @param values A list of values on which the scalar function will be performed.
   *
   * @return Value holding the difference, in whole days, between the two given Date/DateTime
   *     values, or a null value (of type number) if one of the values is null.
   */
  public Value evaluate(List<Value> values) {
    Value firstValue = values.get(0);
    Value secondValue = values.get(1);

    // If one of the values is null, return a null number value.
    if (firstValue.isNull() || secondValue.isNull()) {
      return NumberValue.getNullValue();
    }
    Date firstDate = getDateFromValue(firstValue);
    Date secondDate = getDateFromValue(secondValue);

    GregorianCalendar calendar =
        new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    calendar.setTime(secondDate);
    return new NumberValue(calendar.fieldDifference(firstDate, Calendar.DATE));
  }

  /**
   * Converts the given value to date. The value must be of type date or datetime.
   *
   * @param value The given value.
   *
   * @return Date object with the same value as the given value.
   */
  private Date getDateFromValue(Value value) {
    Calendar calendar;
    if (value.getType() == ValueType.DATE) {
      calendar = ((DateValue) value).getObjectToFormat();
    } else { // datetime
      calendar = ((DateTimeValue) value).getObjectToFormat();
    }
    return calendar.getTime();
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
   * Validates that there are only 2 parameters given for this function, and that their types are
   * either DATE or DATETIME. Throws a ScalarFunctionException if the parameters are invalid.
   *
   * @param types A list with parameters types.
   *
   * @throws InvalidQueryException Thrown if the parameters are invalid.
   */
  public void validateParameters(List<ValueType> types) throws InvalidQueryException {
    if (types.size() != 2) {
      throw new InvalidQueryException("Number of parameters for the dateDiff "
          + "function is wrong: " + types.size());
    } else if ((!isDateOrDateTimeValue(types.get(0)))
        || (!isDateOrDateTimeValue(types.get(1)))) {
      throw new InvalidQueryException("Can't perform the function 'dateDiff' "
          + "on values that are not a Date or a DateTime values");
    }
  }

  /**
   * Returns true if the given type is Date or DateTime.
   *
   * @param type The given type.
   *
   * @return True if the given type is Date or DateTime.
   */
  private boolean isDateOrDateTimeValue(ValueType type) {
    return ((type == ValueType.DATE) || (type == ValueType.DATETIME));
  }
  
  
  /**
   * {@inheritDoc}
   */
  public String toQueryString(List<String> argumentsQueryStrings) {
    return FUNCTION_NAME + "(" + argumentsQueryStrings.get(0) + ", " + argumentsQueryStrings.get(1)
        + ")";
  }
}
