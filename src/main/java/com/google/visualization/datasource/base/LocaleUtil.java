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

import com.ibm.icu.text.MessageFormat;
import com.ibm.icu.util.ULocale;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A utility class for locale handling.
 * 
 * @author Yaniv S.
 *
 */
public class LocaleUtil {
  
  /**
   * Make the default constructor private.
   */
  private LocaleUtil() {}
  
  /**
   * A regular expression for extracting the language, country and variant from a locale string.
   */
  private static final Pattern LOCALE_PATTERN =
    Pattern.compile("(^[^_-]*)(?:[_-]([^_-]*)(?:[_-]([^_-]*))?)?");

  /**
   * The default locale. Used as a fall-back locale throughout the system. 
   */
  private static ULocale defaultLocale = ULocale.US;

  /**
   * Converts a locale string from the RFC 3066 standard format to the Java locale format.
   * You can call this on any locale string obtained from an external source
   * (cookie, URL parameter, header, etc.). This method accepts more than just the standard
   * format and will also tolerate capitalization discrepancies and the use of an underscore
   * in place of a hyphen.
   * 
   * @param s The locale string.
   *
   * @return The locale for the given locale string.
   * 
   */
  public static Locale getLocaleFromLocaleString(String s) {
    if (s == null) {
      return null;
    }

    Matcher matcher = LOCALE_PATTERN.matcher(s);

    // LOCALE_PATTERN will match any string, though it may not match the whole string.  
    // Specifically, it will not match a third _ or - or any subsequent text.
    matcher.find();

    String language = matcher.group(1);
    language = (language == null) ? "" : language;
    String country = matcher.group(2);
    country = (country == null) ? "" : country;
    String variant = matcher.group(3);
    variant = (variant == null) ? "" : variant;

    return new Locale(language, country, variant);
  }
  
  /**
   * Sets the default locale.
   * 
   * @param defaultLocale The default locale.
   */
  public static void setDefaultLocale(ULocale defaultLocale) {
    LocaleUtil.defaultLocale = defaultLocale;
  }

  /**
   * Returns the default locale.
   * 
   * @return The default locale.
   */
  public static ULocale getDefaultLocale() {
    return defaultLocale;
  }

  /**
   * Returns a localized message from the specified <code>ResourceBundle</code> for the given key.
   * In case the locale is null, uses the default locale.
   * If locale is null, the default <code>ResourceBundle</code> is used.
   *
   * @param bundleName The name of the resource bundle.
   * @param key The key of the requested string.
   * @param locale The locale.
   *
   * @return A localized message from the bundle based on the given locale.
   */
  public static String getLocalizedMessageFromBundle(String bundleName, String key, Locale locale) {
    if (locale == null) {
      // If no locale is specified, return the message in the default ResourceBundle.
      return ResourceBundle.getBundle(bundleName).getString(key);
    }
    return ResourceBundle.getBundle(bundleName, locale).getString(key);
  }
  
  /**
   * Returns a localized message from the specified <code>ResourceBundle</code> for the given key
   * with the given arguments inserted to the message in the specified locations.
   * In case the locale is null, uses the default locale.
   * If locale is null, the default <code>ResourceBundle</code> is used.
   * 
   *
   * @param bundleName The name of the resource bundle.
   * @param key The key of the requested string.
   * @param args Arguments to place in the error message.
   * @param locale The locale.
   *
   * @return A localized message from the bundle based on the given locale.
   */
  public static String getLocalizedMessageFromBundleWithArguments(String bundleName, String key,
      String[] args, Locale locale) {
    String rawMesage = getLocalizedMessageFromBundle(bundleName, key, locale);
    if (args != null && args.length > 0) {
      return MessageFormat.format(rawMesage, args);
    }
    return rawMesage;
  }
}
