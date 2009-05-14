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
import com.google.visualization.datasource.base.DataSourceParameters;
import com.google.visualization.datasource.base.InvalidQueryException;
import com.google.visualization.datasource.base.OutputType;
import com.google.visualization.datasource.query.Query;

import com.ibm.icu.util.ULocale;

import javax.servlet.http.HttpServletRequest;

/**
 * This class contains all information concerning a data source request. The information in this
 * class is used to create a data table and return a corresponding response to the user.
 *
 * @author Yaniv S.
 *
 */
public class DataSourceRequest {

  /**
   * The query (based on the "tq" request parameter).
   */
  private Query query;

  /**
   * The data source parameters (based on the "tqx" request parameter).
   */
  private DataSourceParameters dsParams;

  /**
   * The user locale.
   */
  private ULocale userLocale;

  /**
   * Indicates whether the request is from the same origin.
   */
  private boolean sameOrigin;

  /**
   * A request header name. Used to determine if the request was sent from the same domain as the
   * server.
   */
  public static final String SAME_ORIGIN_HEADER = "X-DataSource-Auth";

  /**
   * The name of the request parameter that specifies the query to execute.
   */
  public static final String QUERY_REQUEST_PARAMETER = "tq";

  /**
   * The name of the request parameter that specifies the data source parameters.
   */
  public static final String DATASOURCE_REQUEST_PARAMETER = "tqx";

  /**
   * A private constructor.
   */
  private DataSourceRequest() {}

  /**
   * Constructor.
   * Useful for debugging and testing.
   * Do not use it for production.
   *
   * @param query The query.
   * @param dsParams The data source parameters.
   * @param userLocale The user locale.
   */
  public DataSourceRequest(Query query, DataSourceParameters dsParams, ULocale userLocale) {
    setUserLocale(userLocale);
    this.dsParams = dsParams;
    this.query = query;
  }

  /**
   * Builds a DataSource request from an <code>HttpServletRequest</code>.
   *
   * @param req The HttpServletRequest.
   *
   * @throws DataSourceException In case of an invalid 'tq' or 'tqx' parameter.
   */
  public DataSourceRequest(HttpServletRequest req) throws DataSourceException {
    inferLocaleFromRequest(req);
    sameOrigin = determineSameOrigin(req);
    createDataSourceParametersFromRequest(req);
    createQueryFromRequest(req);
  }

  /**
   * Returns a default data source request.
   *
   * Used in case no data source request is available (i.e., because of invalid input).
   * Parses the input 'tq' and 'tqx' strings and, if it fails, uses default values.
   *
   * @param req The http servlet request.
   *
   * @return A default data source request.
   */
  public static DataSourceRequest getDefaultDataSourceRequest(HttpServletRequest req) {
    DataSourceRequest dataSourceRequest = new DataSourceRequest();
    dataSourceRequest.inferLocaleFromRequest(req);
    dataSourceRequest.sameOrigin = determineSameOrigin(req);
    try {
      dataSourceRequest.createDataSourceParametersFromRequest(req);
    } catch (DataSourceException e) {
      if (dataSourceRequest.dsParams == null) {
        dataSourceRequest.dsParams = DataSourceParameters.getDefaultDataSourceParameters();
      }
      if ((dataSourceRequest.dsParams.getOutputType() == OutputType.JSON)
          && (!dataSourceRequest.sameOrigin)) {
        dataSourceRequest.dsParams.setOutputType(OutputType.JSONP);
      }
    }
    try {
      dataSourceRequest.createQueryFromRequest(req);
    } catch (InvalidQueryException e) {
      // If we can't parse the 'tq' parameter, a null query is set.
    }
    return dataSourceRequest;
  }

  /**
   * Determines whether the given request is from the same origin (based on
   * request headers set by the ).
   *
   * @param req The http servlet request.
   *
   * @return True if this is a same origin request false otherwise.
   */
  public static boolean determineSameOrigin(HttpServletRequest req) {
    // We conclude that the request is sent from the same origin if it contains a predefined
    // header that the client application inserted. This is a known way of verifying that
    // a request is xhr and hence was sent from the same domain.
    return (req.getHeader(SAME_ORIGIN_HEADER) != null);
  }

  /**
   * Creates the <code>Query</code> based on the 'tq' parameter on the given request.
   *
   * @param req The http servlet request.
   *
   * @throws InvalidQueryException if the 'tq' string is invalid or missing on the request.
   */
  private void createQueryFromRequest(HttpServletRequest req) throws InvalidQueryException {
    String queryString = req.getParameter(QUERY_REQUEST_PARAMETER);
    query = DataSourceHelper.parseQuery(queryString);
  }

  /**
   * Creates the <code>DataSourceParameters</code> based on the 'tqx' parameter on the given
   * request.
   *
   * @param req The http servlet request.
   *
   * @throws DataSourceException Thrown if the data source parameters or query could not be parsed.
   */
  private void createDataSourceParametersFromRequest(HttpServletRequest req)
      throws DataSourceException {
    // Set the data source parameters.
    String dataSourceParamsString = req.getParameter(DATASOURCE_REQUEST_PARAMETER);

    dsParams = new DataSourceParameters(dataSourceParamsString);
    // For backward compatibility, set the OutputType to be 'jsonp' in case the request is cross
    // domain and the 'out' property in the 'tqx' parameter is 'json'.
    if (dsParams.getOutputType() == OutputType.JSON && !sameOrigin) {
      dsParams.setOutputType(OutputType.JSONP);
    }
  }


  /**
   * Infers the locale from the http servlet request.
   *
   * @param req The http servlet request.
   */
  private void inferLocaleFromRequest(HttpServletRequest req) {
    userLocale = DataSourceHelper.getLocaleFromRequest(req);
  }

  /**
   * Returns the query.
   *
   * @return The query.
   */
  public Query getQuery() {
    return query;
  }

  /**
   * Returns the data source parameters.
   *
   * @return The data source parameters.
   */
  public DataSourceParameters getDataSourceParameters() {
    return dsParams;
  }

  /**
   * Sets the user locale.
   *
   * @param userLocale The user locale.
   */
  public void setUserLocale(ULocale userLocale) {
    this.userLocale = userLocale;
  }

  /**
   * Returns the user locale.
   *
   * @return The user locale.
   */
  public ULocale getUserLocale() {
    return userLocale;
  }

  /**
   * Returns true if the request is same-origin.
   *
   * @return True if the request is same-origin.
   */
  public boolean isSameOrigin() {
    return sameOrigin;
  }
}
