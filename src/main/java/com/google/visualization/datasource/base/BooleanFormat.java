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

package com.google.visualization.datasource.base;

import com.ibm.icu.text.UFormat;

import java.text.FieldPosition;
import java.text.ParseException;
import java.text.ParsePosition;

/**
 * A UFormat that performs formatting and parsing for boolean values.
 *
 * The string representation of boolean values is determined by two strings passed
 * to the constructor of this class; a string for <code>TRUE</code> and a string for
 * <code>FALSE</code>.
 * 
 * Examples for text strings representing a BooleanValue that can be
 * used in construction are:
 * 1) true, false
 * 2) TRUE, FALSE
 * 2) t, f
 * 3) yes, no
 * 4) YES, NO
 * 5) check, uncheck
 * 6) Green, Red
 *
 * @author Hillel M.
 */

public class BooleanFormat extends UFormat {

  /**
   * A string representing the boolean value true.
   */
  private String trueString;

  /**
   * A string representing the boolean value false.
   */
  private String falseString;

  /**
   * Creates a BooleanFormat with default true/false formatting.
   */
  public BooleanFormat() {
    this("true", "false");
  }

  /**
   * Creates a BooleanFormat.
   *
   * @param trueString A string representing true.
   * @param falseString A string representing false.
   */
  public BooleanFormat(String trueString, String falseString) {
    if (trueString == null || falseString == null) {
      throw new NullPointerException();
    }
    this.trueString = trueString;
    this.falseString = falseString;
  }

  /**
   * Constructs a boolean format from a pattern. The pattern must contain two
   * strings separated by colon, for example: "true:false".
   *
   * @param pattern The pattern from which to construct.
   */
  public BooleanFormat(String pattern) {
    String[] valuePatterns = pattern.split(":");
    if (valuePatterns.length != 2) {
      throw new IllegalArgumentException("Cannot construct a boolean format "
          + "from " + pattern + ". The pattern must contain a single ':' "
          + "character");
    }
    this.trueString = valuePatterns[0];
    this.falseString = valuePatterns[1];
  }

  /**
   * Formats a Boolean and appends the result to a StringBuffer.
   *
   * @param obj The object to format.
   * @param appendTo The StringBuffer to which the formatted string will be appended.
   * @param pos A FieldPosition param (not used in this class).
   *
   * @return A StringBuffer with the formatted string for this object.
   */
  @Override
  public StringBuffer format(Object obj, StringBuffer appendTo, FieldPosition pos) {
    if ((null != obj) && !(obj instanceof Boolean)) {
      throw new IllegalArgumentException();
    }
    Boolean val = (Boolean) obj;
    if (val == null) {
      // nothing to append.
      pos.setBeginIndex(0);
      pos.setEndIndex(0);
    } else if (val) {
      appendTo.append(trueString);
      pos.setBeginIndex(0);
      pos.setEndIndex(trueString.length() - 1);
    } else {
      // val == false
      appendTo.append(falseString);
      pos.setBeginIndex(0);
      pos.setEndIndex(falseString.length() - 1);
    }
    return appendTo;
  }

  /**
   * Parses a string into a {@code Boolean}. A string can be either a trueString
   * or a falseString (non-case sensitive).
   *
   * @param source The string from which to parse.
   * @param pos Marks the end of the parsing, or 0 if the parsing failed.
   *
   * @return A {@code Boolean} for the parsed string.
   */
  @Override
  public Boolean parseObject(String source, ParsePosition pos) {
    if (source == null) {
      throw new NullPointerException();
    }
    Boolean value = null;
    if (trueString.equalsIgnoreCase(source.trim())) {
      value = Boolean.TRUE;
      pos.setIndex(trueString.length());
    } else if (falseString.equalsIgnoreCase(source.trim())) {
      value = Boolean.FALSE;
      pos.setIndex(falseString.length());
    }
    if (null == value) {
      pos.setErrorIndex(0);
    }
    return value;
  }

  /**
   * Parses text from the beginning of the given string to produce a boolean.
   * The method may not use the entire text of the given string.
   *
   * @param text A String that should be parsed from it's start.
   *
   * @return A {@code Boolean} parsed from the string.
   *
   * @exception ParseException If the string cannot be parsed.
   */
  public Boolean parse(String text) throws ParseException {
    ParsePosition parsePosition = new ParsePosition(0);
    Boolean result = parseObject(text, parsePosition);
    if (parsePosition.getIndex() == 0) {
      throw new ParseException("Unparseable boolean: \"" + text + '"',
          parsePosition.getErrorIndex());
    }
    return result;
  }
}
