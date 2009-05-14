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

package com.google.visualization.datasource.datatable.value;

import com.google.common.collect.Maps;
import com.google.visualization.datasource.base.TypeMismatchException;

import com.ibm.icu.util.GregorianCalendar;

import java.util.Map;

/**
 * Represents a supported value type for a table.
 *
 * @author Yoah B.D.
 */
public enum ValueType {
  BOOLEAN("BOOLEAN"),
  NUMBER("NUMBER"),
  TEXT("STRING"),
  DATE("DATE"),
  TIMEOFDAY("TIMEOFDAY"),
  DATETIME("DATETIME");

  /**
   * The type code string for this ValueType.
   */
  private String typeCode;

  /**
   * Constructs a ValueType with a given type code string.
   *
   * @param typeCode The type code string for the ValueType.
   */
  ValueType(String typeCode) {
    this.typeCode = typeCode;
  }

  /**
   * Returns the type code string for this ValueType.
   *
   * @return The type code string for this ValueType.
   */
  String getTypeCode() {
    return typeCode;
  }

  /**
   * Returns the type code string for this ValueType as a lower case string.
   *
   * @return The type code string for this ValueType as a lower case string.
   */
  public String getTypeCodeLowerCase() {
    return typeCode.toLowerCase();
  }

  /**
   * Returns the correct ValueType for a given type code string.
   *
   * @param string The type code string.
   *
   * @return The correct ValueType for the type code string.
   */
  static ValueType getByTypeCode(String string) {
    return typeCodeToValueType.get(string);
  }

  /**
   * Holds the mapping of type code to an instance of this class.
   */
  private static Map<String, ValueType> typeCodeToValueType;

  /**
   * Creates a value of this ValueType. The value must be of an appropriate
   * class, otherwise an exception is thrown.
   * Java native types/classes are translated to Value in the following way:
   * String -> TextValue
   * Number (inc. all derivatives such as Integer, Double) -> NumberValue
   * Boolean -> BooleanValue
   * com.ibm.icu.util.GregorianCalendar -> DateValue, DateTimeValue or
   * TimeOfDayValue
   *
   * @param value The value to create.
   * @return A Value of this ValueType.
   * @throws TypeMismatchException When the type of value does not match this.
   */
  public Value createValue(Object value) throws TypeMismatchException {
    Value ret = null;

    if (value == null) {
      ret = Value.getNullValueFromValueType(this);
    } else if ((this == TEXT) &&
               ((value instanceof String) || value == null)) {
      ret = new TextValue((String) value);
    } else if ((this == NUMBER) && (value instanceof Number)) {
        ret = new NumberValue(((Number) value).doubleValue());
    } else if ((this == BOOLEAN) && (value instanceof Boolean)) {
        ret = ((Boolean) value).booleanValue() ? BooleanValue.TRUE
                : BooleanValue.FALSE;
    } else if ((this == DATE) && (value instanceof GregorianCalendar)) {
        ret = new DateValue((GregorianCalendar) value);
    } else if ((this == DATETIME) && (value instanceof GregorianCalendar)) {
        ret = new DateTimeValue((GregorianCalendar) value);
    } else if ((this == TIMEOFDAY) && (value instanceof GregorianCalendar)) {
        ret = new TimeOfDayValue((GregorianCalendar) value);
    }

    // If none of the above hold, we have a type mismatch.
    if (ret == null) {
      throw new TypeMismatchException("Value type mismatch.");
    }

    return ret;
  }

  /**
   * Initializes the static map.
   */
  static {
    typeCodeToValueType = Maps.newHashMap();
    for (ValueType type : ValueType.values()) {
      typeCodeToValueType.put(type.typeCode, type);
    }
  }
}
