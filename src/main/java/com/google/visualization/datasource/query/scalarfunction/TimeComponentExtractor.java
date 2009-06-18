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

import com.google.common.collect.Maps;
import com.google.visualization.datasource.base.InvalidQueryException;
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TimeOfDayValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.ibm.icu.util.GregorianCalendar;

import java.util.List;
import java.util.Map;

/**
 * A time component extractor. This class encompasses all the unary scalar functions
 * that extract a time component from a date/datetime/timeofday value. e.g.,
 * year, month, second.
 *
 * @author Liron L.
 */
public class TimeComponentExtractor implements ScalarFunction {

  /**
   * An enum of time components that can be extracted from a
   * date/datetime/timeofday value.
   */
  public static enum TimeComponent {
    YEAR("year"),
    MONTH("month"),
    WEEK("week"),
    DAY("day"),
    HOUR("hour"),
    MINUTE("minute"),
    SECOND("second"),
    MILLISECOND("millisecond"),
    QUARTER("quarter"),
    // Returns 1 for Sunday, 2 for Monday, etc.
    DAY_OF_WEEK("dayofweek");

    /**
     * The name of this TimeComponent.
     */
    private String name;

    /**
     * Constructor for the enum. Initializes the name member.
     *
     * @param name The name of this TimeComponent.
     */
    private TimeComponent(String name) {
      this.name = name;
    }

    /**
     * Returns the name of this TimeComponent.
     *
     * @return The name of this TimeComponent.
     */
    public String getName() {
      return name;
    }
  }

  /**
   * A mapping of a TimeComponent to a TimeComponentExtractor that extracts this TimeComponent.
   * This is used to keep a single instance of every type of extractor.
   */
  private static Map<TimeComponent, TimeComponentExtractor> timeComponentsPool;

  /**
    * Returns an instance of a TimeComponentExtractor that extracts the given
    * TimeComponent.
    *
    * @param timeComponent The TimeComponent to extract.
    *
    * @return Returns an instance of a TimeComponentExtractor.
    */
   public static TimeComponentExtractor getInstance(TimeComponent timeComponent)
   {
     return timeComponentsPool.get(timeComponent);
   }

  /**
   * This static block builds the timeComponentsPool that maps TimeComponents to
   * TimeComponentExtractors that extract this TimeComponent.
   */
  static {
    timeComponentsPool = Maps.newHashMap();
    for (TimeComponent component : TimeComponent.values()) {
      timeComponentsPool.put(component, new TimeComponentExtractor(component));
    }
  }

  /**
   * The TimeComponent that this TimeComponentExtractor extracts.
   */
  private TimeComponent timeComponent;

  /**
   * A private constructor.
   *
   * @param timeComponent
   */
  private TimeComponentExtractor(TimeComponent timeComponent) {
    this.timeComponent = timeComponent;

  }

  /**
   * Returns the name of this scalar function, in this case the name of the TimeComponentExtractor.
   *
   * @return the name of this scalar function, in this case the name of the TimeComponentExtractor.
   */
  public String getFunctionName() {
    return timeComponent.getName();
  }

  /**
   * Executes the scalar function that extracts the timeComponent on the given
   * values. Returns a NumberValue with the timeComponent of the given
   * Date/DateTime/TimeOfDay value. The method does not validate the parameters,
   * the user must check the parameters before calling this method.
   *
   * @param values A list of the values on which the scalar function is performed.
   *
   * @return Value with the timeComponent value of the given value, or number
   *     null value if value is null.
   */
  public Value evaluate(List<Value> values) {
    Value value = values.get(0);
    ValueType valueType = value.getType();
    int component;

    // If the value is null, return a null number value.
    if (value.isNull()) {
      return NumberValue.getNullValue();
    }

    switch(timeComponent) {
      case YEAR:
        if (valueType == ValueType.DATE) {
          component = ((DateValue) value).getYear();
        } else { // DATETIME
          component = ((DateTimeValue) value).getYear();
        }
        break;
      case MONTH:
        if (valueType == ValueType.DATE) {
          component = ((DateValue) value).getMonth();
        } else { // DATETIME
          component = ((DateTimeValue) value).getMonth();
        }
        break;
      case DAY:
        if (valueType == ValueType.DATE) {
          component = ((DateValue) value).getDayOfMonth();
        } else { // DATETIME
          component = ((DateTimeValue) value).getDayOfMonth();
        }
        break;
      case HOUR:
        if (valueType == ValueType.TIMEOFDAY) {
          component = ((TimeOfDayValue) value).getHours();
        } else { // DATETIME
          component = ((DateTimeValue) value).getHourOfDay();
        }
        break;
      case MINUTE:
        if (valueType == ValueType.TIMEOFDAY) {
          component = ((TimeOfDayValue) value).getMinutes();
        } else { // DATETIME
          component = ((DateTimeValue) value).getMinute();
        }
        break;
      case SECOND:
        if (valueType == ValueType.TIMEOFDAY) {
          component = ((TimeOfDayValue) value).getSeconds();
        } else { // DATETIME
          component = ((DateTimeValue) value).getSecond();
        }
        break;
      case MILLISECOND:
        if (valueType == ValueType.TIMEOFDAY) {
          component = ((TimeOfDayValue) value).getMilliseconds();
        } else { // DATETIME
          component = ((DateTimeValue) value).getMillisecond();
        }
        break;
      case QUARTER:
        if (valueType == ValueType.DATE) {
          component = ((DateValue) value).getMonth();
        } else { // DATETIME
          component = ((DateTimeValue) value).getMonth();
        }
        component = component / 3 + 1; // Add 1 to get 1-4 instead of 0-3. 
        break;
      case DAY_OF_WEEK:
        GregorianCalendar calendar =
            (GregorianCalendar) ((valueType == ValueType.DATE) ?
                ((DateValue) value).getObjectToFormat() :
                ((DateTimeValue) value).getObjectToFormat());
        component = calendar.get(GregorianCalendar.DAY_OF_WEEK);
        break;
      default:
        // should not get here since we assume that the given values are valid.
        throw new RuntimeException("An invalid time component.");
    }

    return new NumberValue(component);
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
   * Validates that there is only one parameter given for the function, and
   * that its type is DATE or DATETIME if the timeComponent to extract is year,
   * month or day, or that it is DATETIME or TIMEOFDAY if the timeComponent is
   * hour, minute, second or millisecond. Throws a ScalarFunctionException if
   * the parameters are invalid.
   *
   * @param types A list of parameter types.
   *
   * @throws InvalidQueryException Thrown if the parameters are invalid.
   */
  public void validateParameters(List<ValueType> types) throws InvalidQueryException {
    if (types.size() != 1) {
      throw new InvalidQueryException("Number of parameters for "
          + timeComponent.getName() + "function is wrong: " + types.size());
    }
    switch (timeComponent) {
      case YEAR:
      case MONTH:
      case DAY:
      case QUARTER:
      case DAY_OF_WEEK:
        if ((types.get(0) != ValueType.DATE)
            && (types.get(0) != ValueType.DATETIME)) {
          throw new InvalidQueryException("Can't perform the function "
              + timeComponent.getName() + " on a column that is not a Date or"
              + " a DateTime column");
        }
        break;
      case HOUR:
      case MINUTE:
      case SECOND:
      case MILLISECOND:
        if ((types.get(0) != ValueType.TIMEOFDAY)
            && (types.get(0) != ValueType.DATETIME)) {
          throw new InvalidQueryException("Can't perform the function "
              + timeComponent.getName() + " on a column that is not a "
              + "TimeOfDay or a DateTime column");
        }
        break;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof TimeComponentExtractor) {
      TimeComponentExtractor other = (TimeComponentExtractor) o;
      return timeComponent.equals(other.timeComponent);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return (timeComponent == null) ? 0 : timeComponent.hashCode();
  }
  
  /**
   * {@inheritDoc}
   */
  public String toQueryString(List<String> argumentsQueryStrings) {
    return getFunctionName() + "(" + argumentsQueryStrings.get(0) + ")"; 
  }
}
