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

package com.google.visualization.datasource.datatable;

import com.google.common.collect.Maps;
import com.google.visualization.datasource.base.BooleanFormat;
import com.google.visualization.datasource.base.LocaleUtil;
import com.google.visualization.datasource.base.TextFormat;
import com.google.visualization.datasource.datatable.value.BooleanValue;
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.TimeOfDayValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;

import com.ibm.icu.text.DecimalFormat;
import com.ibm.icu.text.DecimalFormatSymbols;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.SimpleDateFormat;
import com.ibm.icu.text.UFormat;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;


/**
 * Formats a {@link Value}, or parses a string to create a {@link Value}.
 * An instance of this class can be created using the
 * {@link #createFromPattern(ValueType, String, ULocale)} method, and can then use the format
 * and/or parse.
 *
 * The class also supplies a set of default patterns per {@link ValueType}. The default patterns
 * can be used for parsing/formatting values when there is no specified pattern.
 * Otherwise, create a class instance by specifying a pattern and locale.
 *
 * Note: This class is not thread safe since it uses {@code UFormat}. 
 *
 * @author Yonatan B.Y.
 */

public class ValueFormatter {

  /**
   * A uFormat that does the actual formatting.
   */
  private UFormat uFormat;

  /**
   * The underlying pattern for the {@code UFormat}.
   */
  private String pattern;

  /**
   * The locale used for the UFormatter.
   */
  private ULocale locale;

  /**
   * The {@code ValueType}.
   */
  private ValueType type;

  /**
   * The default pattern for parsing a string to a text value.
   *
   * @see com.google.visualization.datasource.datatable.value.TextValue
   */
  private static final String DEFAULT_TEXT_DUMMY_PATTERN = "dummy";

  /**
   * The default pattern for parsing a string to a date time value.
   *
   * @see DateTimeValue
   */
  private static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

  /**
   * The default pattern for parsing a string to a date value.
   *
   * @see DateValue
   */
  private static final String DEFAULT_DATE_PATTERNS = "yyyy-MM-dd";

  /**
   * The default pattern for parsing a string to a time of day value.
   *
   * @see TimeOfDayValue
   */
  private static final String DEFAULT_TIMEOFDAY_PATTERN = "HH:mm:ss";

  /**
   * The default pattern for parsing a string to a boolean value.
   *
   * @see BooleanValue
   */
  private static final String DEFAULT_BOOLEAN_PATTERN = "true:false";

  /**
   * The default pattern for parsing a string to a number value.
   *
   * @see NumberValue
   */
  private static final String DEFAULT_NUMBER_PATTERN = "";

  /**
   * Private constructor that constructs an instance of this class from a UFormat.
   * Use {@link #createFromPattern(ValueType, String, ULocale)} to create an instance.
   */
  private ValueFormatter(String pattern, UFormat uFormat, ValueType type, ULocale locale) {
    this.pattern = pattern;
    this.uFormat = uFormat;
    this.type = type;
    this.locale = locale;
  }

  /**
   * Creates a formatter for the given value type with the given pattern string and locale.
   * If the pattern is illegal returns null.
   * If pattern is null, uses the first default pattern for the given type.
   * If ulocale is null, uses the default locale return by {@code LocaleUtil#getDefaultLocale}.
   *
   * @param type The column value type.
   * @param pattern The string pattern representing the formatter pattern.
   * @param locale The ULocale of the formatter.
   *
   * @return A formatter for the given type, pattern and locale, or null if the pattern is illegal.
   */
  public static ValueFormatter createFromPattern(ValueType type, String pattern, ULocale locale) {
    UFormat uFormat = null;
    if (pattern == null) {
      pattern = getDefaultPatternByType(type);
    }

    if (locale == null) {
      locale = LocaleUtil.getDefaultLocale();
    }

    // For whichever formatter is created, try to format some arbitrary value, and see if an
    // exception was thrown. If it was thrown, conclude the pattern was illegal, and return null.
    try {
      switch (type) {
        case BOOLEAN:
          uFormat = new BooleanFormat(pattern);
          uFormat.format(BooleanValue.TRUE.getObjectToFormat());
          break;
        case TEXT:
          // Dummy format so no need to check it for problems.
          uFormat = new TextFormat();
          break;
        case DATE:
          uFormat = new SimpleDateFormat(pattern, locale);
          ((SimpleDateFormat) uFormat).setTimeZone(TimeZone.getTimeZone("GMT"));
          uFormat.format(new DateValue(1995, 7, 3).getObjectToFormat());
          break;
        case TIMEOFDAY:
          uFormat = new SimpleDateFormat(pattern, locale);
          ((SimpleDateFormat) uFormat).setTimeZone(TimeZone.getTimeZone("GMT"));
          uFormat.format(new TimeOfDayValue(2, 59, 12, 123).getObjectToFormat());
          break;
        case DATETIME:
          uFormat = new SimpleDateFormat(pattern, locale);
          ((SimpleDateFormat) uFormat).setTimeZone(TimeZone.getTimeZone("GMT"));
          uFormat.format(new DateTimeValue(1995, 7, 3, 2, 59, 12, 123).getObjectToFormat());
          break;
        case NUMBER:
          DecimalFormatSymbols symbols = new DecimalFormatSymbols(locale);
          uFormat = new DecimalFormat(pattern, symbols);
          uFormat.format(new NumberValue(-12.3).getObjectToFormat());
          break;
      }
    } catch (RuntimeException e) {
      // The formatter is illegal return null.
      return null;
    }
    return new ValueFormatter(pattern, uFormat, type, locale);
  }

  /**
   * Creates a default formatter for the specified value type and locale.
   * If locale is null, uses the default locale returned by {@code LocaleUtil#getDefaultLocale}.
   *
   * @param type The value type.
   * @param locale The data table locale.
   *
   * @return A default formatter for the given type and locale.
   */
  public static ValueFormatter createDefault(ValueType type, ULocale locale) {
    String pattern = getDefaultPatternByType(type);
    return createFromPattern(type, pattern, locale);
  }

  /**
   * Creates default formatters for all the value types for the specified locale.
   * Returns a map of default formatters by type.
   * The map can be used for iterating over a data table and parsing/formatting its values.
   *
   * @param locale The data table locale.
   *
   * @return A map of default formatters by type with the given locale.
   */
  public static Map<ValueType, ValueFormatter> createDefaultFormatters(ULocale locale) {
    Map<ValueType, ValueFormatter> foramtters = Maps.newHashMap();
    for (ValueType type : ValueType.values()) {
      foramtters.put(type, createDefault(type, locale));
    }
    return foramtters;
  }

  /**
   * Formats a value to a string, using the given pattern.
   *
   * @param value The value to format.
   *
   * @return The formatted value.
   */
  public String format(Value value) {
    if (value.isNull()) {
      return "";
    }
    return uFormat.format(value.getObjectToFormat());
  }


  /**
   * Creates the corresponding {@code Value} from the given string.
   * If parsing fails, returns a NULL_VALUE for the specified type.
   * For example, if val="3" and type=ValueType.Number, then after successfully parsing
   * the string "3" into the double 3.0 a new NumberValue would
   * be returned with an internal double value = 3.
   *
   * @param val The string to parse.
   *
   * @return A corresponding {@code Value} for the given string. If parsing fails the value would
   * be a NULL_VALUE of the correct {@code ValueType}.
   */
  public Value parse(String val) {
    Value value = null;
    try {
      switch(type) {
        case DATE:
          value = parseDate(val);
          break;
        case TIMEOFDAY:
          value = parseTimeOfDay(val);
          break;
        case DATETIME:
          value = parseDateTime(val);
          break;
        case NUMBER:
          value = parseNumber(val);
          break;
        case BOOLEAN:
          value = parseBoolean(val);
          break;
        case TEXT:
          // do nothing
          value = new TextValue(val);
          break;
      }
    } catch (ParseException pe) {
      value = Value.getNullValueFromValueType(type);
    }
    return value;
  }

  /**
   * Returns the default pattern for the specified value type and index.
   *
   * @param type The value type.
   *
   * @return The default pattern for the specified value type and index.
   */
  private static String getDefaultPatternByType(ValueType type) {
    String defaultPattern;
    switch (type) {
      case TEXT:
        defaultPattern = DEFAULT_TEXT_DUMMY_PATTERN;
        break;
      case DATE:
        defaultPattern = DEFAULT_DATE_PATTERNS;
        break;
      case DATETIME:
        defaultPattern = DEFAULT_DATETIME_PATTERN;
        break;
      case TIMEOFDAY:
        defaultPattern = DEFAULT_TIMEOFDAY_PATTERN;
        break;
      case BOOLEAN:
        defaultPattern = DEFAULT_BOOLEAN_PATTERN;
        break;
      case NUMBER:
        defaultPattern = DEFAULT_NUMBER_PATTERN;
        break;
      default:
        defaultPattern = null;
    }
    return defaultPattern;
  }



  /**
   * Parses a string to a boolean value.
   *
   * @param val The string to parse.
   *
   * @return A boolean value based on the given string.
   *
   * @throws ParseException if val cannot be parsed into a boolean value.
   */
  private BooleanValue parseBoolean(String val) throws ParseException {
    Boolean bool = ((BooleanFormat) uFormat).parse(val);
    return BooleanValue.getInstance(bool);
  }

  /**
   * Parses a string to a number value.
   *
   * @param val The string to parse.
   *
   * @return A number value based on the given string.
   *
   * @throws ParseException If val cannot be parsed into a number value.
   */
  private NumberValue parseNumber(String val) throws ParseException {
    Number n = ((NumberFormat) uFormat).parse(val);
    return new NumberValue(n.doubleValue());
  }

  /**
   * Parses a string to a date time value.
   *
   * @param val The string to parse.
   *
   * @return A date time value based on the given string.
   *
   * @throws ParseException If val cannot be parsed into a date.
   */
  private DateTimeValue parseDateTime(String val) throws ParseException {
    Date date = ((SimpleDateFormat) uFormat).parse(val);
    GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    gc.setTime(date);
    return new DateTimeValue(gc);
  }

  /**
   * Parses a string to a date value.
   *
   * @param val The string to parse.
   *
   * @return A date value based on the given string.
   *
   * @throws ParseException If val cannot be parsed into a date.
   */
  private DateValue parseDate(String val) throws ParseException {
    Date date = ((SimpleDateFormat) uFormat).parse(val);
    GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    gc.setTime(date);
    return new DateValue(gc);
  }

  /**
   * Parses a string to a time of day value.
   *
   * @param val The string to parse.
   *
   * @return A time of day value based on the given string.
   *
   * @throws ParseException If val cannot be parsed into a date.
   */
  private TimeOfDayValue parseTimeOfDay(String val) throws ParseException {
    Date date = ((SimpleDateFormat) uFormat).parse(val);
    GregorianCalendar gc = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    gc.setTime(date);
    return new TimeOfDayValue(gc);
  }



  /**
   * Returns the internal <code>UFormat</code> object.
   * 
   * @return The internal <code>UFormat</code> object.
   */
  public UFormat getUFormat() {
    return uFormat;
  }

  /**
   * Returns the pattern for this formatter.
   * 
   * @return The pattern for this formatter.
   */
  public String getPattern() {
  return pattern;
}

  /**
   * Returns the ulocale.
   * 
   * @return The ulocale.
   */
  public ULocale getLocale() {
    return locale;
  }

  /**
   * Returns the type.
   * 
   * @return The type.
   */
  public ValueType getType() {
    return type;
  }
}
