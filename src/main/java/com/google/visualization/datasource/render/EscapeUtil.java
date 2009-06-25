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

package com.google.visualization.datasource.render;


import org.apache.commons.lang.StringEscapeUtils;

/**
 * A utility to escape strings.
 *
 * @author Hillel M.
 */
public class EscapeUtil {

  /**
   * This helper lookup array is used for json escaping. It enables fast lookup of unicode
   * characters.
   * {@link #jsonEscape}.
   */
  private static final char[] HEX_DIGITS = {
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
  };

  /**
   * Private constructor.
   */
  private EscapeUtil() {
  }

  /**
   * This method is used to escape strings embedded in the json response. The method is based on
   * org.apache.shindig.common.JsonSerializer.appendString().
   * The method escapes the following in order to enable safe parsing of the json string:
   * 1) single and double quotes - ' and "
   * 2) backslash - /
   * 3) html brackets - <>
   * 4) control characters - \n \t \r ..
   * 5) special characters - out of range unicode characters (formatted to the uxxxx format)
   *
   * @param str The original string to escape.
   *
   * @return The escaped string.
   */
  public static String jsonEscape(String str) {
    if (str == null || str.length() == 0) {
      return "";
    }
    StringBuffer sb = new StringBuffer();
    char current;
    for (int i = 0, j = str.length(); i < j; ++i) {
      current = str.charAt(i);
      switch (current) {
        case '\'':
          sb.append("\\u0027");
          break;
        case '\"':
          sb.append("\\u0022");
          break;
        case '\\':
          sb.append('\\');
          sb.append(current);
          break;
          // We escape angle brackets in order to prevent content sniffing in user agents like IE.
          // This content sniffing can potentially be used to bypass other security restrictions.
        case '<':
          sb.append("\\u003c");
          break;
        case '>':
          sb.append("\\u003e");
          break;
        default:
          if (current < ' ' || (current >= '\u0080' && current < '\u00a0') ||
              (current >= '\u2000' && current < '\u2100')) {
            sb.append('\\');
            switch (current) {
              case '\b':
                sb.append('b');
                break;
              case '\t':
                sb.append('t');
                break;
              case '\n':
                sb.append('n');
                break;
              case '\f':
                sb.append('f');
                break;
              case '\r':
                sb.append('r');
                break;
              default:
                // The possible alternative approaches for dealing with unicode characters are
                // as follows:
                // Method 1 (from json.org.JSONObject)
                // 1. Append "000" + Integer.toHexString(current)
                // 2. Truncate this value to 4 digits by using value.substring(value.length() - 4)
                //
                // Method 2 (from net.sf.json.JSONObject)
                // This method is fairly unique because the entire thing uses an intermediate fixed
                // size buffer of 1KB. It's an interesting approach, but overall performs worse than
                // org.json
                // 1. Append "000" + Integer.toHexString(current)
                // 2. Append value.charAt(value.length() - 4)
                // 2. Append value.charAt(value.length() - 3)
                // 2. Append value.charAt(value.length() - 2)
                // 2. Append value.charAt(value.length() - 1)
                //
                // Method 3 (previous experiment)
                // 1. Calculate Integer.hexString(current)
                // 2. for (int i = 0; i < 4 - value.length(); ++i) { buf.append('0'); }
                // 3. buf.append(value)
                //
                // Method 4 (Sun conversion from java.util.Properties)
                // 1. Append '\'
                // 2. Append 'u'
                // 3. Append each of 4 octets by indexing into a hex array.
                //
                // Method 5
                // Index into a single lookup table of all relevant lookup values.
                sb.append('u');
                sb.append(HEX_DIGITS[(current >> 12) & 0xF]);
                sb.append(HEX_DIGITS[(current >>  8) & 0xF]);
                sb.append(HEX_DIGITS[(current >>  4) & 0xF]);
                sb.append(HEX_DIGITS[current & 0xF]);
            }
          } else {
            sb.append(current);
          }
      }
    }
    return sb.toString();
  }

  /**
   * Escapes the string for embedding in html.
   *
   * @param str The string to escape.
   *
   * @return The escaped string.
   */
  public static String htmlEscape(String str) {
    return StringEscapeUtils.escapeHtml(str);
  }

}
