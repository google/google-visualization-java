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

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An abstract class for data source servlet implementations.
 *
 * @author Yaniv S.
 */
public abstract class DataSourceServlet extends HttpServlet implements DataTableGenerator {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    DataSourceHelper.executeDataSourceServletFlow(req, resp, this, isRestrictedAccessMode());
  }

  /**
   * Returns a flag that indicates whether the servlet is in restricted-access mode.
   * In restricted-access mode the server serves only requests coming from the same domain as the
   * server domain (i.e., same origin policy).
   * This protects the server from XSRF attacks while limiting the requests to which the server
   * can respond.
   *
   * @return True if this servlet operates in restricted-access mode, false otherwise.
   */
  protected boolean isRestrictedAccessMode() {
    return true;
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    doGet(req, resp);
  }

  @Override
  public Capabilities getCapabilities() {
    return Capabilities.NONE;
  }
}
