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
import java.text.ParsePosition;

/**
 * A UFormat that performs (dummy) formatting and parsing of text (string) values.
 *
 * This class is required only to be consistent with the UFormat API, its methods do nothing.
 *
 * @author Hillel M.
 */
public class TextFormat extends UFormat {

  /**
   * Formats a TextValue and appends the result to a StringBuffer.
   *
   * @param obj The object to format.
   * @param appendTo The StringBuffer to which the formatted string is appended.
   * @param pos A FieldPosition parameter not used in this case.
   *
   * @return A StringBuffer with the formatted string for this object.
   */
  @Override
  public StringBuffer format(Object obj, StringBuffer appendTo, FieldPosition pos) {
    if ((null == obj) || !(obj instanceof String)) {
      throw new IllegalArgumentException();
    }
    String text = (String) obj;
    appendTo.append(text);
    pos.setBeginIndex(0);
    if (0 == text.length()) {
      pos.setEndIndex(0);
    } else {
      pos.setEndIndex(text.length() - 1);
    }
    return appendTo;
  }

  /**
   * Parse a string into a TextValue.
   *
   * If this method is used to parse an empty string and it is called via
   * Format.parseObject(Object) a ParseException is thrown.
   *
   * @param source The string to parse from.
   * @param pos Marks the end of the parsing or 0 if the parsing failed.
   *
   * @return A BooleanValue for the parsed string.
   *
   * @throws NullPointerException if pos is null or source is null.
   */
  @Override
  public Object parseObject(String source, ParsePosition pos) {
    if ((null == pos) || (null == source)) {
      throw new NullPointerException();
    }
    pos.setIndex(source.length());
    return source;
  }
}
