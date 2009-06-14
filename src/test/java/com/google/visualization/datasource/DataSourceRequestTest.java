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

package com.google.visualization.datasource;

import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.base.OutputType;

import com.ibm.icu.util.ULocale;

import junit.framework.TestCase;

import org.easymock.EasyMock;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

/**
 * Unit test for data source request.
 *
 * @author Nimrod T.
 */
public class DataSourceRequestTest extends TestCase {

  /**
   * Test simple constructor.
   */
  public void testConstructor() {
    // Test with hl == fr, out:csv, same-domain.
    DataSourceRequest dataSourceRequest = null;
    boolean caught = false;
    try {
      HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
      EasyMock.expect(req.getParameter("hl")).andReturn("fr");
      EasyMock.expect(req.getHeader("X-DataSource-Auth")).andReturn("a");
      EasyMock.expect(req.getParameter("tqx")).andReturn("out:csv;");
      EasyMock.expect(req.getParameter("tq")).andReturn(null);
      EasyMock.replay(req);
      dataSourceRequest = new DataSourceRequest(req);
      EasyMock.verify(req);
    } catch (DataSourceException e) {
      caught = true;
    }
    assertFalse(caught);
    assertEquals(new ULocale("fr"), dataSourceRequest.getUserLocale());
    assertTrue(dataSourceRequest.isSameOrigin());
    assertEquals(OutputType.CSV, dataSourceRequest.getDataSourceParameters().getOutputType());

    // Test with hl == it, not same-domain.
    dataSourceRequest = null;
    caught = false;
    try {
      HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
      EasyMock.expect(req.getParameter("hl")).andReturn("it");
      EasyMock.expect(req.getHeader("X-DataSource-Auth")).andReturn(null);
      EasyMock.expect(req.getParameter("tqx")).andReturn(null);
      EasyMock.expect(req.getParameter("tq")).andReturn(null);
      EasyMock.replay(req);
      dataSourceRequest = new DataSourceRequest(req);
      EasyMock.verify(req);
    } catch (DataSourceException e) {
      caught = true;
    }
    assertFalse(caught);
    assertEquals(new ULocale("it"), dataSourceRequest.getUserLocale());
    assertFalse(dataSourceRequest.isSameOrigin());
    assertEquals(OutputType.JSONP, dataSourceRequest.getDataSourceParameters().getOutputType());
  }

  /**
   * Test simple constructor.
   */
  public void testGetDefaultDataSourceRequest() {
    // Test with hl == fr, out:csv, same-domain.
    DataSourceRequest dataSourceRequest;
    HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(req.getParameter("hl")).andReturn("fr");
    EasyMock.expect(req.getHeader("X-DataSource-Auth")).andReturn("a");
    EasyMock.expect(req.getParameter("tqx")).andReturn("out:csv;");
    EasyMock.expect(req.getParameter("tq")).andReturn(null);
    EasyMock.replay(req);
    dataSourceRequest = DataSourceRequest.getDefaultDataSourceRequest(req);
    EasyMock.verify(req);
    assertEquals(new ULocale("fr"), dataSourceRequest.getUserLocale());
    assertTrue(dataSourceRequest.isSameOrigin());
    assertEquals(OutputType.CSV, dataSourceRequest.getDataSourceParameters().getOutputType());

    // Test with empty request (only locale).
    dataSourceRequest = null;
    req = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(req.getParameter("hl")).andReturn(null);
    EasyMock.expect(req.getLocale()).andReturn(new Locale("qq"));
    EasyMock.expect(req.getHeader("X-DataSource-Auth")).andReturn(null);
    EasyMock.expect(req.getParameter("tqx")).andReturn(null);
    EasyMock.expect(req.getParameter("tq")).andReturn(null);
    EasyMock.replay(req);
    dataSourceRequest = DataSourceRequest.getDefaultDataSourceRequest(req);
    EasyMock.verify(req);
    assertEquals(new ULocale("qq"), dataSourceRequest.getUserLocale());
    assertFalse(dataSourceRequest.isSameOrigin());
    assertEquals(OutputType.JSONP, dataSourceRequest.getDataSourceParameters().getOutputType());

    // Test with hl == it, not same-domain.
    dataSourceRequest = null;
    req = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(req.getParameter("hl")).andReturn("it");
    EasyMock.expect(req.getHeader("X-DataSource-Auth")).andReturn(null);
    EasyMock.expect(req.getParameter("tqx")).andReturn(null);
    EasyMock.expect(req.getParameter("tq")).andReturn(null);
    EasyMock.replay(req);
    dataSourceRequest = DataSourceRequest.getDefaultDataSourceRequest(req);
    EasyMock.verify(req);
    assertEquals(new ULocale("it"), dataSourceRequest.getUserLocale());
    assertFalse(dataSourceRequest.isSameOrigin());
    assertEquals(OutputType.JSONP, dataSourceRequest.getDataSourceParameters().getOutputType());
  }

  public void testDetermineSameOrigin() {
    HttpServletRequest req = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(req.getHeader(
        DataSourceRequest.SAME_ORIGIN_HEADER)).andReturn("not null");
    EasyMock.replay(req);
    assertTrue("Request should be same origin",
        DataSourceRequest.determineSameOrigin(req));
    EasyMock.verify(req);

    req = EasyMock.createMock(HttpServletRequest.class);
    EasyMock.expect(req.getHeader(
        DataSourceRequest.SAME_ORIGIN_HEADER)).andReturn(null);
    EasyMock.replay(req);
    assertFalse("Request should not be same origin", 
        DataSourceRequest.determineSameOrigin(req));
    EasyMock.verify(req);
  }
}
