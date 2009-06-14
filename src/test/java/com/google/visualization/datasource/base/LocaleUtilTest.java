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

import junit.framework.TestCase;

import java.util.Locale;

/**
 * Unit test for LocaleUtil.
 *
 * @author Hillel M.
 */
public class LocaleUtilTest extends TestCase {


  public void testGetLocalizedMessageFromBundle() {
    assertEquals("Sign in", LocaleUtil.getLocalizedMessageFromBundle(
        "com.google.visualization.datasource.base.ErrorMessages", "SIGN_IN", null));

    assertEquals("Sign in", LocaleUtil.getLocalizedMessageFromBundle(
        "com.google.visualization.datasource.base.ErrorMessages", "SIGN_IN", Locale.CANADA_FRENCH));

    assertEquals("Access denied", LocaleUtil.getLocalizedMessageFromBundle(
        "com.google.visualization.datasource.base.ErrorMessages", "ACCESS_DENIED", Locale.GERMAN));
  }

}