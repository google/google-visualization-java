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

import com.google.visualization.datasource.Capabilities;
import com.google.visualization.datasource.DataSourceServlet;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.query.Query;
import com.google.visualization.datasource.util.SqlDataSourceHelper;
import com.google.visualization.datasource.util.SqlDatabaseDescription;

import javax.servlet.http.HttpServletRequest;

/**
 * An example data source servlet that can query an SQL database. 
 * This makes use of the SqlDataSourceHelper class that is part of this library.
 *
 * Query language operations affect the result. This includes operations
 * that are not part of SQL itself, such as pivoting, which is executed by
 * the query engine.
 *
 * Four URL parameters must be specified, these are then passed to
 * the underlying SQL DB. The parameters are as follows:
 * url - The url of the sql DB.
 * user - The username to use when accessing the DB.
 * password - The password to use when accessing the DB.
 * table - The SQL table name.
 *  
 * @author Yaniv S.
 */
public class SqlDataSourceServlet extends DataSourceServlet {

  /**
   * The SQL predefined capabilities set is a special custom set for SQL
   * databases. This implements most of the data source capabilities more 
   * efficiently.
   */
  @Override
  public Capabilities getCapabilities() {
    return Capabilities.SQL;
  }

  @Override
  public DataTable generateDataTable(Query query, HttpServletRequest request)
      throws DataSourceException {
    SqlDatabaseDescription dbDescription = new SqlDatabaseDescription(
        request.getParameter("url"),
        request.getParameter("user"),
        request.getParameter("password"),
        request.getParameter("table"));
    return SqlDataSourceHelper.executeQuery(query, dbDescription);
  }

  /**
   * NOTE: By default, this function returns true, which means that cross
   * domain requests are rejected.
   * This check is disabled here so examples can be used directly from the
   * address bar of the browser. Bear in mind that this exposes your
   * data source to xsrf attacks.
   * If the only use of the data source url is from your application,
   * that runs on the same domain, it is better to remain in restricted mode.
   */
  @Override
  protected boolean isRestrictedAccessMode() {
    return false;
  }
}
