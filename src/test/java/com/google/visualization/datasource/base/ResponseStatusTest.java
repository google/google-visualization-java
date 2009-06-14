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
 * Unit test for ResponseStatus.
 *
 * @author Nimrod T.
 */
public class ResponseStatusTest extends TestCase {
  public void testBasic() {
    // Test getMessageForReasonType with default locale.
    assertEquals("Invalid query", ReasonType.INVALID_QUERY.getMessageForReasonType());

    // Test getMessageForReasonType with specified locale.
    assertEquals("Retrieved data was truncated", 
        ReasonType.DATA_TRUNCATED.getMessageForReasonType(new Locale("en", "US")));
  }

  public void testGetModifiedResponseStatus() {
      String urlMessage = "http://www.google.com";
      ResponseStatus responseStatusAccessDenied = new ResponseStatus(StatusType.ERROR,
          ReasonType.ACCESS_DENIED, urlMessage);
      ResponseStatus responseStatusUserNotAuthenticated = new ResponseStatus(StatusType.ERROR,
          ReasonType.USER_NOT_AUTHENTICATED, urlMessage);
      ResponseStatus responseStatusUserNotAuthenticatedNoUrl = new ResponseStatus(StatusType.ERROR,
          ReasonType.USER_NOT_AUTHENTICATED, "123");

      // no modification
      assertEquals(responseStatusAccessDenied,
          ResponseStatus.getModifiedResponseStatus(responseStatusAccessDenied));

      assertEquals(responseStatusUserNotAuthenticatedNoUrl,
          ResponseStatus.getModifiedResponseStatus(responseStatusUserNotAuthenticatedNoUrl));

      // modified
      ResponseStatus modifiedResponse =
          ResponseStatus.getModifiedResponseStatus(responseStatusUserNotAuthenticated);
      assertEquals(StatusType.ERROR, modifiedResponse.getStatusType());
      assertEquals(ReasonType.USER_NOT_AUTHENTICATED, modifiedResponse.getReasonType());
      String htmlSignInString = "<a target=\"_blank\" href=\"" + urlMessage + "\">Sign in</a>";
      assertEquals(htmlSignInString, modifiedResponse.getDescription());
    }

    public void testCreateResponseStatus() {
      DataSourceException dse = new DataSourceException(ReasonType.INTERNAL_ERROR, "123");
      ResponseStatus responseStatus = ResponseStatus.createResponseStatus(dse);

      assertEquals(StatusType.ERROR, responseStatus.getStatusType());
      assertEquals(ReasonType.INTERNAL_ERROR, responseStatus.getReasonType());
      assertEquals("123", responseStatus.getDescription());
    }
}
