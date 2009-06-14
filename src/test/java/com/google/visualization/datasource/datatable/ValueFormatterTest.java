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

import com.google.visualization.datasource.base.LocaleUtil;
import com.google.visualization.datasource.datatable.value.BooleanValue;
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.TimeOfDayValue;
import com.google.visualization.datasource.datatable.value.ValueType;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.ULocale;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;

import java.util.Map;

/**
 * ValueFormatter tests.
 *
 * There is a test per each value type. Each test has three parts as follows: A test of the default
 * pattern, a test of a provided pattern, and a test that fails because the provided differs from
 * the pattern of the actual text to parse.
 *
 * @author Hillel M.
 */
public class ValueFormatterTest extends TestCase {

  private ULocale[] ulocales;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    ulocales = ULocale.getAvailableLocales();
  }

  /**
   * This test checks the creation of ValueFormatters for each
   * ValueType and for each ULocale. The output strings are very different
   * since they are locale dependent thus the checks are quite primitive.
   * Nevertheless it is (almost) impossible to create better tests since
   * the output strings are so diverse (usually not all fonts are available
   * on a single machine to even display all of them correctly).
   *
   * ULocale is a class analogous to java.util.Locale that provides additional
   * support for ICU protocol.
   */
  public void testValueFormatterFromPatternAndLocale() {
    for (ULocale ulocale : ulocales) {
      // Create a ValueFormatter for each ValueType.
      ValueFormatter booleanFormatter = ValueFormatter.createFromPattern(
          ValueType.BOOLEAN, "true:false", ulocale);
      ValueFormatter numberFormatter = ValueFormatter.createFromPattern(
          ValueType.NUMBER, "#.##", ulocale);
      ValueFormatter textFormatter = ValueFormatter.createFromPattern(
          ValueType.TEXT, "TEXT-PATTERN", ulocale);
      ValueFormatter dateFormatter = ValueFormatter.createFromPattern(
          ValueType.DATE, "dd/mm/YYYY", ulocale);
      ValueFormatter timeOfDayFormatter = ValueFormatter.createFromPattern(
          ValueType.TIMEOFDAY, "HH:mm:ss", ulocale);
      ValueFormatter dateTimeFormatter = ValueFormatter.createFromPattern(
          ValueType.DATETIME, "YYYY/mm/dd HH:mm:ss", ulocale);

      // Boolean
      String formattedValue = booleanFormatter.format(BooleanValue.TRUE);
      assertEquals("true", formattedValue);

      // Number
      formattedValue = numberFormatter.format(new NumberValue(-12.23));
      assertFalse(StringUtils.isEmpty(formattedValue));
      String language = ulocale.getDisplayLanguage();
      if (isReadableLetters(language)) {
        assertTrue(formattedValue.contains("12"));
      }

      // Text
      formattedValue = textFormatter.format(new TextValue("this is a test"));
      assertEquals("this is a test", formattedValue);

      // Date
      formattedValue = dateFormatter.format(new DateValue(1988, 11, 24));
      assertFalse(StringUtils.isEmpty(formattedValue));
      language = ulocale.getDisplayLanguage();
      if (isReadableLetters(language)) {
        assertTrue(formattedValue.contains("88"));
      }

      // Time
      formattedValue = timeOfDayFormatter.format(new TimeOfDayValue(2, 24, 6));
      assertFalse(StringUtils.isEmpty(formattedValue));
      language = ulocale.getDisplayLanguage();
      if (isReadableLetters(language)) {
        assertTrue(formattedValue.contains("24"));
      }

      // Timestamp
      formattedValue = dateTimeFormatter.format(new DateTimeValue(1597, 9, 29, 1, 2, 33, 142));
      assertFalse(StringUtils.isEmpty(formattedValue));
      language = ulocale.getDisplayLanguage();
      if (isReadableLetters(language)) {
        assertTrue(formattedValue.contains("29"));
      }

      // test nulls:
      assertEquals("", booleanFormatter.format(BooleanValue.getNullValue()));
      assertEquals("", numberFormatter.format(NumberValue.getNullValue()));
      assertEquals("", dateFormatter.format(DateValue.getNullValue()));
      assertEquals("", dateTimeFormatter.format(DateTimeValue.getNullValue()));
      assertEquals("", timeOfDayFormatter.format(TimeOfDayValue.getNullValue()));
    }
  }

  public void testDefaultAndNullCases() {
    ValueFormatter booleanFormatter = ValueFormatter.createFromPattern(
        ValueType.BOOLEAN, null, null);
    ValueFormatter numberFormatter = ValueFormatter.createFromPattern(
        ValueType.NUMBER, null, null);
    ValueFormatter textFormatter = ValueFormatter.createFromPattern(
        ValueType.TEXT, null, null);
    ValueFormatter dateFormatter = ValueFormatter.createFromPattern(
        ValueType.DATE, null, null);
    ValueFormatter timeOfDayFormatter = ValueFormatter.createFromPattern(
        ValueType.TIMEOFDAY, null, null);
    ValueFormatter dateTimeFormatter = ValueFormatter.createFromPattern(
        ValueType.DATETIME, null, null);

    assertNotNull(booleanFormatter);
    assertNotNull(numberFormatter);
    assertNotNull(dateFormatter);
    assertNotNull(dateTimeFormatter);
    assertNotNull(timeOfDayFormatter);
    assertNotNull(textFormatter);

    assertNotNull(booleanFormatter.getUFormat());
    assertNotNull(numberFormatter.getUFormat());
    assertNotNull(dateFormatter.getUFormat());
    assertNotNull(dateTimeFormatter.getUFormat());
    assertNotNull(timeOfDayFormatter.getUFormat());
    assertNotNull(textFormatter.getUFormat());

    assertEquals(LocaleUtil.getDefaultLocale(), booleanFormatter.getLocale());
    assertEquals(LocaleUtil.getDefaultLocale(), numberFormatter.getLocale());
    assertEquals(LocaleUtil.getDefaultLocale(), dateFormatter.getLocale());
    assertEquals(LocaleUtil.getDefaultLocale(), dateTimeFormatter.getLocale());
    assertEquals(LocaleUtil.getDefaultLocale(), timeOfDayFormatter.getLocale());
    assertEquals(LocaleUtil.getDefaultLocale(), textFormatter.getLocale());

    assertEquals(ValueType.BOOLEAN, booleanFormatter.getType());
    assertEquals(ValueType.NUMBER, numberFormatter.getType());
    assertEquals(ValueType.DATE, dateFormatter.getType());
    assertEquals(ValueType.DATETIME, dateTimeFormatter.getType());
    assertEquals(ValueType.TIMEOFDAY, timeOfDayFormatter.getType());
    assertEquals(ValueType.TEXT, textFormatter.getType());

    assertNotNull(ValueFormatter.createDefault(ValueType.BOOLEAN, null));
    assertNotNull(ValueFormatter.createDefault(ValueType.NUMBER, null));
    assertNotNull(ValueFormatter.createDefault(ValueType.DATE, null));
    assertNotNull(ValueFormatter.createDefault(ValueType.DATETIME, null));
    assertNotNull(ValueFormatter.createDefault(ValueType.TIMEOFDAY, null));
    assertNotNull(ValueFormatter.createDefault(ValueType.TEXT, null));

    ULocale ulocale = ULocale.ENGLISH;
    ULocale ulocale1 = ULocale.CANADA_FRENCH;

    assertEquals(ulocale,
        ValueFormatter.createFromPattern(ValueType.NUMBER, "", ulocale).getLocale());
    assertEquals(ulocale1,
        ValueFormatter.createDefault(ValueType.NUMBER, ulocale1).getLocale());
  }

  public void testAFewMoreCases() {
    ULocale ulocale = ULocale.ENGLISH;

    // Create a ValueFormatter for each type
    ValueFormatter booleanFormatter = ValueFormatter.createFromPattern(
        ValueType.BOOLEAN, null, ulocale);
    assertNotNull(booleanFormatter);

    ValueFormatter numberFormatter = ValueFormatter.createFromPattern(
        ValueType.NUMBER, null, ulocale);
    assertNotNull(numberFormatter);

    ValueFormatter dateFormatter = ValueFormatter.createFromPattern(
        ValueType.DATE, "YYYY/mm", null);
    assertNotNull(dateFormatter);

    ValueFormatter textFormatter = ValueFormatter.createFromPattern(ValueType.TEXT, "", ulocale);
    assertNotNull(textFormatter);
  }

  /**
   * Since not all locales use greek letters, this is a simple way to filter
   * SOME of the greek letter using languages.
   */
  private boolean isReadableLetters(String language) {
    return ((language.equals("English")) || (language.equals("Spanish"))
        || (language.equals("German")) || (language.equals("Greek")));
  }

  /**
   * This test checks formatting by aq specific pattern in the US locale.
   */
  public void testSpecificPatternFormattingInUSLocale() {
    // Create a ColumnFormatter for each pattern
    ValueFormatter booleanFormatter = ValueFormatter.createFromPattern(
        ValueType.BOOLEAN, "yep:na", ULocale.US);
    ValueFormatter numberFormatter = ValueFormatter.createFromPattern(
        ValueType.NUMBER, "#,##0.000", ULocale.US);
    ValueFormatter textFormatter = ValueFormatter.createFromPattern(
        ValueType.TEXT, "", ULocale.US);
    ValueFormatter dateFormatter = ValueFormatter.createFromPattern(
        ValueType.DATE, "MM | dd | yy", ULocale.US);
    ValueFormatter timeFormatter = ValueFormatter.createFromPattern(
        ValueType.TIMEOFDAY, "HH-mm", ULocale.US);
    ValueFormatter timestampFormatter = ValueFormatter.createFromPattern(
        ValueType.DATETIME, "dd_MM_yy HH:mm", ULocale.US);

    // Boolean
    String formattedValue = booleanFormatter.format(BooleanValue.TRUE);
    assertEquals("yep", formattedValue);

    // Number
    formattedValue = numberFormatter.format(new NumberValue(-12000.23));
    assertEquals("-12,000.230", formattedValue);

    // Text
    formattedValue = textFormatter.format(new TextValue("this is another test"));
    assertEquals("this is another test", formattedValue);

    // Date
    formattedValue = dateFormatter.format(new DateValue(1988, 11, 24));
    assertEquals("12 | 24 | 88", formattedValue);

    // Time
    formattedValue =
        timeFormatter.format(new TimeOfDayValue(2, 24, 6));
    assertEquals("02-24", formattedValue);

    // Timestamp
    formattedValue = timestampFormatter.format(new DateTimeValue(1597, 9, 29, 1, 2, 33, 142));
    assertEquals("29_10_97 01:02", formattedValue);
  }

  // Some locales use localized digits and so even a simple pattern will be formatted
  // differently than in English.
  // The following is a simple example for Hindi.
  public void testHindiLocale() {
    ValueFormatter dateFormatter = ValueFormatter.createFromPattern(
        ValueType.DATE, "MM | dd | yyyy", new ULocale("hi_IN"));
    DateValue dateValue = new DateValue(2009, 1, 2);
    String dateString =
        "\u0966\u0968\u0020\u007c\u0020\u0966\u0968\u0020\u007c\u0020\u0968\u0966\u0966\u096f";
    assertEquals(dateString, dateFormatter.format(dateValue));
    assertEquals(dateValue, dateFormatter.parse(dateString));
  }

  public void testGetDefaultValueForamtters() {
    Map<ValueType, ValueFormatter> formatters = ValueFormatter.createDefaultFormatters(null);
    assertNotNull(formatters);
    assertEquals(6, formatters.size());
    for (ValueType type : ValueType.values()) {
      ValueFormatter valueFormatter = ValueFormatter.createDefault(type, null);
      assertEquals(valueFormatter.getPattern(), formatters.get(type).getPattern());
      assertEquals(valueFormatter.getLocale(), formatters.get(type).getLocale());
    }
  }

  public void testParseAll() {
    assertEquals(new NumberValue(22),
        ValueFormatter.createDefault(ValueType.NUMBER, null).parse("22"));

    assertEquals(new TextValue("1a"),
        ValueFormatter.createDefault(ValueType.TEXT, null).parse("1a"));

    assertEquals(BooleanValue.TRUE,
        ValueFormatter.createDefault(ValueType.BOOLEAN, null).parse("true"));

    assertEquals(new DateValue(2009, Calendar.JANUARY, 1),
        ValueFormatter.createDefault(ValueType.DATE, null).parse("2009-1-1"));

    assertEquals(new DateTimeValue(2009, Calendar.JANUARY, 1, 12, 13, 14, 0),
        ValueFormatter.createDefault(ValueType.DATETIME, null).parse("2009-1-1 12:13:14"));

    assertEquals(new TimeOfDayValue(12, 13, 14, 0),
        ValueFormatter.createDefault(ValueType.TIMEOFDAY, null).parse("12:13:14"));

  }
  public void testParseNumber() {
    ValueFormatter numberFormatter = ValueFormatter.createDefault(ValueType.NUMBER, null);
    assertEquals("22.0", numberFormatter.parse("22%").toString());
    assertEquals("22.0", numberFormatter.parse("22.0").toString());
    assertEquals("null", numberFormatter.parse("#0.0%").toString());
  }

  public void testParseBoolean() {
    ValueFormatter booleanFormatter = ValueFormatter.createFromPattern(
        ValueType.BOOLEAN, "1:0", null);

    assertEquals(BooleanValue.getNullValue(), booleanFormatter.parse("false"));
    assertEquals(BooleanValue.TRUE, booleanFormatter.parse("1"));
  }

  public void testParseText() {
    ValueFormatter textFormatter = ValueFormatter.createDefault(ValueType.TEXT, null);
    assertEquals(new TextValue("text"), textFormatter.parse("text"));
  }

  public void testParseDate() {
    ValueFormatter dateFormatter = ValueFormatter.createDefault(ValueType.DATE, null);

    assertEquals(new DateValue(2004, Calendar.FEBRUARY, 15),
        dateFormatter.parse("2004-02-15"));
    dateFormatter = ValueFormatter.createFromPattern(ValueType.DATE, "MM/dd/yyyy", null);
    assertEquals(new DateValue(2004, Calendar.JANUARY, 15), dateFormatter.parse("01/15/2004"));
    assertEquals(DateValue.getNullValue(), dateFormatter.parse("01.15.2004"));
  }

  public void testParseTimeOfDay() {
    ValueFormatter timeOfDayFormatter = ValueFormatter.createDefault(ValueType.TIMEOFDAY, null);
    assertEquals(new TimeOfDayValue(7, 22, 44), timeOfDayFormatter.parse("7:22:44"));

    timeOfDayFormatter = ValueFormatter.createFromPattern(ValueType.TIMEOFDAY, "ss:mm:HH", null);

    assertEquals(new TimeOfDayValue(7, 22, 44), timeOfDayFormatter.parse("44:22:7"));

    assertEquals(TimeOfDayValue.getNullValue(), timeOfDayFormatter.parse("01.15.2004"));
  }

  public void testParseDateTime() {
    ValueFormatter dateTimeFormatter = ValueFormatter.createDefault(ValueType.DATETIME, null);
    assertEquals(new DateTimeValue(2004, Calendar.FEBRUARY, 15, 7, 22, 44, 0),
        dateTimeFormatter.parse("2004-02-15 7:22:44"));
    dateTimeFormatter =
        ValueFormatter.createFromPattern(ValueType.DATETIME, "MM/dd/yyyy ss:mm:HH", null);
    assertEquals(new DateTimeValue(2004, Calendar.JANUARY, 15, 7, 22, 44, 0),
        dateTimeFormatter.parse("01/15/2004 44:22:7"));
    assertEquals(DateTimeValue.getNullValue(), dateTimeFormatter.parse("01.15.2004"));
  }
}
