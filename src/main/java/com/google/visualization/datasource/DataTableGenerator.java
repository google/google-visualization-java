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
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.query.Query;

import javax.servlet.http.HttpServletRequest;

/**
 * An interface for a class that can generate a <code>DataTable</code>.
 * 
 * @author Yaniv S.
 */
public interface DataTableGenerator {

  /**
   * Generates the data table.
   *
   * @param query The query to execute on the underlying data. Ignore this parameter for
   *     a data source that does not support any capabilities.
   * @param request The http request. May contain information that is relevant to generating
   *     the data table.
   *
   * @return The generated data table.
   *
   * @throws DataSourceException If the data could not be generated for any reason.
   */
  public DataTable generateDataTable(Query query, HttpServletRequest request)
      throws DataSourceException;

  /**
   * Returns the capabilities supported by this data table generator.
   *
   * The query that <code>generateDataTable</code> accepts will only contain clauses 
   * corresponding to these capabilities. 
   * (see {@link com.google.visualization.datasource.Capabilities}).
   *
   * @return The capabilities supported by this datasource.
   */
  public Capabilities getCapabilities();
}
