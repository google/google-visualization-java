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


/**
 * Unit test for Helper.
 *
 * @author Yaniv S.
 */
public class ReasonTypeTest extends TestCase {

  public void testBasic() {
    // Test getMessageForReasonType with default locale.
    assertEquals(ReasonType.INVALID_QUERY.getMessageForReasonType(), "Invalid query");
  }
  
  public void testLowerString() {
    ReasonType reasonType = ReasonType.ACCESS_DENIED;
    assertEquals("access_denied", reasonType.lowerCaseString());
  }
}
