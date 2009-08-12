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

import com.google.visualization.datasource.DataSourceHelper;
import com.google.visualization.datasource.DataSourceServlet;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.base.ReasonType;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.query.Query;
import com.google.visualization.datasource.util.CsvDataSourceHelper;

import com.ibm.icu.util.ULocale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

/**
 * A demo servlet for serving a simple, constant data table.
 * This servlet extends DataSourceServlet, but does not override the default
 * empty implementation of method getCapabilities(). This servlet therefore ignores the
 * user query (as passed in the 'tq' url parameter), leaving the
 * query engine to apply it to the data table created here.
 *
 * @author Nimrod T.
 */
public class CsvDataSourceServlet extends DataSourceServlet {

  /**
   * Log.
   */
  private static final Log log = LogFactory.getLog(CsvDataSourceServlet.class.getName());

  /**
   * The name of the parameter that contains the url of the CSV to load.
   */
  private static final String URL_PARAM_NAME = "url";

  /**
   * Generates the data table.
   * This servlet assumes a special parameter that contains the CSV URL from which to load
   * the data.
   */
  @Override
  public DataTable generateDataTable(Query query, HttpServletRequest request)
      throws DataSourceException {
    String url = request.getParameter(URL_PARAM_NAME);
    if (StringUtils.isEmpty(url)) {
      log.error("url parameter not provided.");
      throw new DataSourceException(ReasonType.INVALID_REQUEST, "url parameter not provided");
    }

    Reader reader;
    try {
      reader = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
    } catch (MalformedURLException e) {
      log.error("url is malformed: " + url);
      throw new DataSourceException(ReasonType.INVALID_REQUEST, "url is malformed: " + url);
    } catch (IOException e) {
      log.error("Couldn't read from url: " + url, e);
      throw new DataSourceException(ReasonType.INVALID_REQUEST, "Couldn't read from url: " + url);
    }
    DataTable dataTable = null;
    ULocale requestLocale = DataSourceHelper.getLocaleFromRequest(request);
    try {
      // Note: We assume that all the columns in the CSV file are text columns. In cases where the
      // column types are known in advance, this behavior can be overridden by passing a list of
      // ColumnDescription objects specifying the column types. See CsvDataSourceHelper.read() for
      // more details.
      dataTable = CsvDataSourceHelper.read(reader, null, true, requestLocale);
    } catch (IOException e) {
      log.error("Couldn't read from url: " + url, e);
      throw new DataSourceException(ReasonType.INVALID_REQUEST, "Couldn't read from url: " + url);
    }
    return dataTable;
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
