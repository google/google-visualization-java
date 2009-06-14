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
 * Unit test for DataSourceParameters.
 *
 * @author Nimrod T.
 */
public class DataSourceParametersTest extends TestCase {

  /**
   * Test request parsing.
   */
  public void testRequestParsing() throws DataSourceException {
    // Test null and default values.
    String tqxValue = null;
    DataSourceParameters dsParams = new DataSourceParameters(tqxValue);
    assertEquals(null, dsParams.getTqxValue());
    assertEquals(null, dsParams.getRequestId());
    assertEquals("JSON", dsParams.getOutputType().toString());
    assertEquals("data.csv", dsParams.getOutFileName());
    assertEquals("google.visualization.Query.setResponse", dsParams.getResponseHandler());

    // Test empty string and default values.
    tqxValue = "";
    dsParams = new DataSourceParameters(tqxValue);
    assertEquals(null, dsParams.getTqxValue());
    assertEquals(null, dsParams.getRequestId());
    assertEquals("JSON", dsParams.getOutputType().toString());
    assertEquals("data.csv", dsParams.getOutFileName());
    assertEquals("google.visualization.Query.setResponse", dsParams.getResponseHandler());

    // Test double ::.
    boolean caught = false;
    try {
      tqxValue = "reqId::7;sig:666";
      dsParams = new DataSourceParameters(tqxValue);
    } catch (DataSourceException e) {
      caught = true;
      assertEquals("Internal error(malformed)", e.getMessageToUser());
      assertEquals(ReasonType.INVALID_REQUEST, e.getReasonType());
    }
    assertTrue(caught);


    // Test double ;;.
    caught = false;
    try {
      tqxValue = "reqId:7;;sig:666";
      dsParams = new DataSourceParameters(tqxValue);
    } catch (DataSourceException e) {
      caught = true;
      assertEquals("Internal error(malformed)", e.getMessageToUser());
      assertEquals(ReasonType.INVALID_REQUEST, e.getReasonType());
    }
    assertTrue(caught);

    // Test no value for a key.
    caught = false;
    try {
      tqxValue = "reqId:7;sig";
      dsParams = new DataSourceParameters(tqxValue);
    } catch (DataSourceException e) {
      caught = true;
      assertEquals("Internal error(malformed)", e.getMessageToUser());
      assertEquals(ReasonType.INVALID_REQUEST, e.getReasonType());
    }
    assertTrue(caught);

    // Test empty value for a key.
    caught = false;
    try {
      tqxValue = "reqId:7;sig:";
      dsParams = new DataSourceParameters(tqxValue);
    } catch (DataSourceException e) {
      caught = true;
      assertEquals("Internal error(malformed)", e.getMessageToUser());
      assertEquals(ReasonType.INVALID_REQUEST, e.getReasonType());
    }
    assertTrue(caught);

    // Test leniently.
    tqxValue = "blahblah:bluhbluh;reqId:251ddd;ganja:sensi;";
    dsParams = new DataSourceParameters(tqxValue);
    assertEquals("251ddd", dsParams.getRequestId());

    // Test signature.
    tqxValue = "sig:1234567";
    dsParams = new DataSourceParameters(tqxValue);
    assertEquals("1234567", dsParams.getSignature());

    // Test out and outFilename when out is html.
    tqxValue = "out:html;outFileName:babylon.burn";
    dsParams = new DataSourceParameters(tqxValue);
    assertEquals("HTML", dsParams.getOutputType().toString());
    assertEquals("babylon.burn", dsParams.getOutFileName());

    // Test out and outFilename when out is csv.
    tqxValue = "out:csv;outFileName:babylon.burn";
    dsParams = new DataSourceParameters(tqxValue);
    assertEquals("CSV", dsParams.getOutputType().toString());
    assertEquals("babylon.burn", dsParams.getOutFileName());

    // Test out and outFilename when outFilename is without .csv.
    tqxValue = "out:csv;outFileName:babylon";
    dsParams = new DataSourceParameters(tqxValue);
    assertEquals("CSV", dsParams.getOutputType().toString());
    assertEquals("babylon.csv", dsParams.getOutFileName());
  }
}
