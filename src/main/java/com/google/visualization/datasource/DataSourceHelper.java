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
import com.google.visualization.datasource.base.LocaleUtil;
import com.google.visualization.datasource.base.MessagesEnum;
import com.google.visualization.datasource.base.OutputType;
import com.google.visualization.datasource.base.ReasonType;
import com.google.visualization.datasource.base.ResponseStatus;
import com.google.visualization.datasource.base.StatusType;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.query.AggregationColumn;
import com.google.visualization.datasource.query.Query;
import com.google.visualization.datasource.query.ScalarFunctionColumn;
import com.google.visualization.datasource.query.engine.QueryEngine;
import com.google.visualization.datasource.query.parser.QueryBuilder;
import com.google.visualization.datasource.render.CsvRenderer;
import com.google.visualization.datasource.render.HtmlRenderer;
import com.google.visualization.datasource.render.JsonRenderer;

import com.ibm.icu.util.ULocale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A Helper class providing convenience functions for serving data source requests.
 *
 * The class enables replying to a data source request with a single method which encompasses
 * all the request processing - <code>executeDataSourceServletFlow</code>.
 * To enable users to change the default flow all the basic operations (such as: query parsing,
 * data table creation, query execution, and response creation) are also exposed.
 *
 * @author Yaniv S.
 */
public class DataSourceHelper {

  /**
   * The log used throughout the data source library.
   */
  private static final Log log = LogFactory.getLog(DataSourceHelper.class.getName());

  /**
   * The name of the http request parameter that indicates the requested locale.
   */
  /* package */ static final String LOCALE_REQUEST_PARAMETER = "hl";

  /**
   * A private constructor for this Singleton.
   */
  private DataSourceHelper() {}

  /**
   * Executes the default data source servlet flow.
   * Assumes restricted access mode.
   * @see <code>executeDataSourceServletFlow(HttpServletRequest req, HttpServletResponse resp,
   *     DataTableGenerator dtGenerator, boolean isRestrictedAccessMode)</code>
   *
   * @param req The HttpServletRequest.
   * @param resp The HttpServletResponse.
   * @param dtGenerator An implementation of {@link DataTableGenerator} interface.
   *
   * @throws IOException In case of I/O errors.
   */
  public static void executeDataSourceServletFlow(HttpServletRequest req, HttpServletResponse resp,
      DataTableGenerator dtGenerator) throws IOException {
    executeDataSourceServletFlow(req, resp, dtGenerator, true);
  }

  /**
   * Executes the default data source servlet flow.
   *
   * The default flow is as follows:
   * - Parse the request parameters.
   * - Verify access is approved (for restricted access mode only).
   * - Split the query.
   * - Generate the data-table using the data-table generator.
   * - Run the completion query.
   * - Set the servlet response.
   *
   * Usage note : this function executes the same flow provided to Servlets that inherit
   * <code>DataSourceServlet</code>.
   * Use this function when the default flow is required but <code>DataSourceServlet</code>
   * cannot be inherited (e.g., your servlet already inherits from anther class, or not in a
   * servlet context).
   *
   * @param req The HttpServletRequest.
   * @param resp The HttpServletResponse.
   * @param dtGenerator An implementation of {@link DataTableGenerator} interface.
   * @param isRestrictedAccessMode Indicates whether the server should serve trusted domains only.
   *     Currently this translates to serving only requests from the same domain.
   *
   * @throws IOException In case of I/O errors.
   */
  public static void executeDataSourceServletFlow(HttpServletRequest req, HttpServletResponse resp,
      DataTableGenerator dtGenerator, boolean isRestrictedAccessMode) throws IOException {
    // Extract the data source request parameters.
    DataSourceRequest dsRequest = null;
    try {
      dsRequest = new DataSourceRequest(req);

      if (isRestrictedAccessMode) {
        // Verify that the request is approved for access.
        DataSourceHelper.verifyAccessApproved(dsRequest);
      }

      // Split the query.
      QueryPair query = DataSourceHelper.splitQuery(dsRequest.getQuery(),
          dtGenerator.getCapabilities());

      // Generate the data table.
      DataTable dataTable = dtGenerator.generateDataTable(query.getDataSourceQuery(), req);

      // Apply the completion query to the data table.
      DataTable newDataTable = DataSourceHelper.applyQuery(query.getCompletionQuery(), dataTable,
          dsRequest.getUserLocale());

      // Set the response.
      setServletResponse(newDataTable, dsRequest, resp);
    } catch (DataSourceException e) {
      if (dsRequest != null) {
        setServletErrorResponse(e, dsRequest, resp);
      } else {
        DataSourceHelper.setServletErrorResponse(e, req, resp);
      }
    } catch (RuntimeException e) {
      log.error("A runtime exception has occured", e);
      ResponseStatus status = new ResponseStatus(StatusType.ERROR, ReasonType.INTERNAL_ERROR,
          e.getMessage());
      if (dsRequest == null) {
        dsRequest = DataSourceRequest.getDefaultDataSourceRequest(req);
      }
      DataSourceHelper.setServletErrorResponse(status, dsRequest, resp);
    }
  }

  /**
   * Checks that the given request is sent from the same domain as that of the server.
   *
   * @param req The data source request.
   *
   * @throws DataSourceException If the access for this request is denied.
   */
  public static void verifyAccessApproved(DataSourceRequest req) throws DataSourceException {
    // The library requires the request to be same origin for JSON and JSONP.
    // Check for (!csv && !html && !tsv-excel) to make sure any output type
    // added in the future will be restricted to the same domain by default.
    OutputType outType = req.getDataSourceParameters().getOutputType();
    if (outType != OutputType.CSV && outType != OutputType.TSV_EXCEL
        && outType != OutputType.HTML && !req.isSameOrigin()) {
      throw new DataSourceException(ReasonType.ACCESS_DENIED,
          "Unauthorized request. Cross domain requests are not supported.");
    }
  }

  // -------------------------- Servlet helper methods --------------------------------------------

  /**
   * Sets the response on the <code>HttpServletResponse</code> by creating a response message
   * for the given <code>DataTable</code> and sets it on the <code>HttpServletResponse</code>.
   *
   * @param dataTable The data table.
   * @param dataSourceRequest The data source request.
   * @param res The http servlet response.
   *
   * @throws IOException In case an error happened trying to write the response to the servlet.
   */
  public static void setServletResponse(DataTable dataTable, DataSourceRequest dataSourceRequest,
      HttpServletResponse res) throws IOException {
    String responseMessage = generateResponse(dataTable, dataSourceRequest);
    setServletResponse(responseMessage, dataSourceRequest, res);
  }

  /**
   * Sets the given response string on the <code>HttpServletResponse</code>.
   *
   * @param responseMessage The response message.
   * @param dataSourceRequest The data source request.
   * @param res The HTTP response.
   *
   * @throws IOException In case an error happened trying to write to the servlet response.
   */
  public static void setServletResponse(String responseMessage,
      DataSourceRequest dataSourceRequest, HttpServletResponse res) throws IOException {
    DataSourceParameters dataSourceParameters = dataSourceRequest.getDataSourceParameters();
    ResponseWriter.setServletResponse(responseMessage, dataSourceParameters, res);
  }


  /**
   * Sets the HTTP servlet response in case of an error.
   *
   * @param dataSourceException The data source exception.
   * @param dataSourceRequest The data source request.
   * @param res The http servlet response.
   *
   * @throws IOException In case an error happened trying to write the response to the servlet.
   */
  public static void setServletErrorResponse(DataSourceException dataSourceException,
      DataSourceRequest dataSourceRequest, HttpServletResponse res) throws IOException {
    String responseMessage = generateErrorResponse(dataSourceException, dataSourceRequest);
    setServletResponse(responseMessage, dataSourceRequest, res);
  }

  /**
   * Sets the HTTP servlet response in case of an error.
   *
   * @param responseStatus The response status.
   * @param dataSourceRequest The data source request.
   * @param res The http servlet response.
   *
   * @throws IOException In case an error happened trying to write the response to the servlet.
   */
  public static void setServletErrorResponse(ResponseStatus responseStatus,
      DataSourceRequest dataSourceRequest, HttpServletResponse res) throws IOException {
    String responseMessage = generateErrorResponse(responseStatus, dataSourceRequest);
    setServletResponse(responseMessage, dataSourceRequest, res);
  }

  /**
   * Sets the HTTP servlet response in case of an error.
   *
   * Gets an <code>HttpRequest</code> parameter instead of a <code>DataSourceRequest</code>.
   * Use this when <code>DataSourceRequest</code> is not available, for example, if
   * <code>DataSourceRequest</code> constructor failed.
   *
   * @param dataSourceException The data source exception.
   * @param req The http servlet request.
   * @param res The http servlet response.
   *
   * @throws IOException In case an error happened trying to write the response to the servlet.
   */
  public static void setServletErrorResponse(DataSourceException dataSourceException,
      HttpServletRequest req, HttpServletResponse res) throws IOException {
    DataSourceRequest dataSourceRequest = DataSourceRequest.getDefaultDataSourceRequest(req);
    setServletErrorResponse(dataSourceException, dataSourceRequest, res);
  }

  // -------------------- Response message helper methods. ----------------------------------------

  /**
   * Generates a string response for the given <code>DataTable</code>.
   *
   * @param dataTable The data table.
   * @param dataSourceRequest The data source request.
   *
   * @return The response string.
   */
  public static String generateResponse(DataTable dataTable, DataSourceRequest dataSourceRequest) {
    CharSequence response;
    ResponseStatus responseStatus = null;
    if (!dataTable.getWarnings().isEmpty()) {
      responseStatus = new ResponseStatus(StatusType.WARNING);
    }
    switch (dataSourceRequest.getDataSourceParameters().getOutputType()) {
      case CSV:
        response = CsvRenderer.renderDataTable(dataTable, dataSourceRequest.getUserLocale(), ",");
        break;
      case TSV_EXCEL:
        response = CsvRenderer.renderDataTable(dataTable, dataSourceRequest.getUserLocale(), "\t");
        break;
      case HTML:
        response = HtmlRenderer.renderDataTable(dataTable, dataSourceRequest.getUserLocale());
        break;
      case JSONP:
        response = JsonRenderer.renderJsonResponse(
            dataSourceRequest.getDataSourceParameters(), responseStatus, dataTable);
        break;
      case JSON:
        response = JsonRenderer.renderJsonResponse(
            dataSourceRequest.getDataSourceParameters(), responseStatus, dataTable);
        break;
      default:
        // This should never happen.
        throw new RuntimeException("Unhandled output type.");
    }

    return response.toString();
  }

  /**
   * Generates an error response string for the given {@link DataSourceException}.
   * Receives an exception, and renders it to an error response according to the
   *{@link OutputType} specified in the {@link DataSourceRequest}.
   *
   * Note: modifies the response status to make links clickable in cases where the reason type is
   * {@link ReasonType#USER_NOT_AUTHENTICATED}. If this is not required call generateErrorResponse
   * directly with a {@link ResponseStatus}.
   *
   * @param dse The data source exception.
   * @param dsRequest The DataSourceRequest.
   *
   * @return The error response string.
   *
   * @throws IOException In case if I/O errors.
   */
  public static String generateErrorResponse(DataSourceException dse, DataSourceRequest dsRequest)
      throws IOException {
    ResponseStatus responseStatus = ResponseStatus.createResponseStatus(dse);
    responseStatus = ResponseStatus.getModifiedResponseStatus(responseStatus);
    return generateErrorResponse(responseStatus, dsRequest);
  }

  /**
   * Generates an error response string for the given <code>ResponseStatus</code>.
   * Render the <code>ResponseStatus</code> to an error response according to the
   * <code>OutputType</code> specified in the <code>DataSourceRequest</code>.
   *
   * @param responseStatus The response status.
   * @param dsRequest The DataSourceRequest.
   *
   * @return The error response string.
   *
   * @throws IOException In case if I/O errors.
   */
  public static String generateErrorResponse(ResponseStatus responseStatus,
      DataSourceRequest dsRequest) throws IOException {
    DataSourceParameters dsParameters = dsRequest.getDataSourceParameters();
    CharSequence response;
    switch (dsParameters.getOutputType()) {
      case CSV:
      case TSV_EXCEL:
        response = CsvRenderer.renderCsvError(responseStatus);
        break;
      case HTML:
        response = HtmlRenderer.renderHtmlError(responseStatus);
        break;
      case JSONP:
        response = JsonRenderer.renderJsonResponse(dsParameters, responseStatus, null);
        break;
      case JSON:
        response = JsonRenderer.renderJsonResponse(dsParameters, responseStatus, null);
        break;
      default:
        // This should never happen.
        throw new RuntimeException("Unhandled output type.");
    }
    return response.toString();
  }

  // -------------------------- Query helper methods ----------------------------------------------

  /** @see #parseQuery(String, ULocale)*/
  public static Query parseQuery(String queryString) throws InvalidQueryException {
    return parseQuery(queryString, null);
  }
  
  /**
   * Parses a query string (e.g., 'select A,B pivot B') and creates a Query object.
   * Throws an exception if the query is invalid.
   *
   * @param queryString The query string.
   * @param locale The user locale.
   *
   * @return The parsed query object.
   *
   * @throws InvalidQueryException If the query is invalid.
   */
  public static Query parseQuery(String queryString, ULocale userLocale) 
      throws InvalidQueryException {
    QueryBuilder queryBuilder = QueryBuilder.getInstance();
    Query query = queryBuilder.parseQuery(queryString, userLocale);

    return query;
  }

  /**
   * Applies the given <code>Query</code> on the given <code>DataTable</code> and returns the
   * resulting <code>DataTable</code>. This method may change the given DataTable.
   * Error messages produced by this method will be localized according to the passed locale 
   * unless the specified {@code DataTable} has a non null locale. 
   *
   * @param query The query object.
   * @param dataTable The data table on which to apply the query.
   * @param locale The user locale for the current request.
   *
   * @return The data table result of the query execution over the given data table.
   *
   * @throws InvalidQueryException If the query is invalid.
   * @throws DataSourceException If the data source cannot execute the query.
   */
  public static DataTable applyQuery(Query query, DataTable dataTable, ULocale locale)
      throws InvalidQueryException, DataSourceException {
    dataTable.setLocaleForUserMessages(locale);
    validateQueryAgainstColumnStructure(query, dataTable);
    dataTable = QueryEngine.executeQuery(query, dataTable, locale);
    dataTable.setLocaleForUserMessages(locale);
    return dataTable;
  }

  /**
   * Splits the <code>Query</code> object into two queries according to the declared data source
   * capabilities: data source query and completion query.
   *
   * The data source query is executed first by the data source itself. Afterward, the
   * <code>QueryEngine</code> executes the completion query over the resulting data table.
   *
   * @param query The query to split.
   * @param capabilities The declared capabilities of the data source.
   *
   * @return A QueryPair object.
   *
   * @throws DataSourceException If the query cannot be split.
   */
  public static QueryPair splitQuery(Query query, Capabilities capabilities)
      throws DataSourceException {
    return QuerySplitter.splitQuery(query, capabilities);
  }

  /**
   * Checks that the query is valid against the structure of the data table.
   * A query is invalid if:
   * <ol>
   * <li> The query references column ids that don't exist in the data table.
   * <li> The query contains calculated columns operations (i.e., scalar function, aggregations)
   * that do not match the relevant columns type.
   * </ol>
   *
   * Note: does NOT validate the query itself, i.e. errors like "SELECT a, a" or
   * "SELECT a GROUP BY a" will not be caught. These kind of errors should be checked elsewhere
   * (preferably by the <code>Query.validate()</code> method).
   *
   * @param query The query to check for validity.
   * @param dataTable The data table against which to validate. Only the columns are used.
   *
   * @throws InvalidQueryException Thrown if the query is found to be invalid
   *     against the given data table.
   */
  public static void validateQueryAgainstColumnStructure(Query query, DataTable dataTable)
      throws InvalidQueryException {
    // Check that all the simple columns exist in the table (including the
    // simple columns inside aggregation and scalar-function columns)
    Set<String> mentionedColumnIds = query.getAllColumnIds();
    for (String columnId : mentionedColumnIds) {
      if (!dataTable.containsColumn(columnId)) {
        String messageToLogAndUser = MessagesEnum.NO_COLUMN.getMessageWithArgs(
            dataTable.getLocaleForUserMessages(), columnId);
        log.error(messageToLogAndUser);
        throw new InvalidQueryException(messageToLogAndUser);
      }
    }

    // Check that all aggregation columns are valid (i.e., the aggregation type
    // matches the columns type).
    Set<AggregationColumn> mentionedAggregations = query.getAllAggregations();
    for (AggregationColumn agg : mentionedAggregations) {
      try {
        agg.validateColumn(dataTable);
      } catch (RuntimeException e) {
        log.error("A runtime exception has occured", e);
        throw new InvalidQueryException(e.getMessage());
      }
    }

    // Check that all scalar function columns are valid. (i.e., the scalar
    // function matches the columns types).
    Set<ScalarFunctionColumn> mentionedScalarFunctionColumns =
        query.getAllScalarFunctionsColumns();
    for (ScalarFunctionColumn col : mentionedScalarFunctionColumns) {
      col.validateColumn(dataTable);
    }
  }

  /**
   * Get the locale from the given request.
   *
   * @param req The http serlvet request
   *
   * @return The locale for the given request.
   */
  public static ULocale getLocaleFromRequest(HttpServletRequest req) {
    Locale locale;
    String requestLocale = req.getParameter(LOCALE_REQUEST_PARAMETER);
    if (requestLocale != null) {
      // Try to take the locale from the 'hl' parameter in the request.
      locale = LocaleUtil.getLocaleFromLocaleString(requestLocale);
    } else {
      // Else, take the browser locale.
      locale = req.getLocale();
    }
    return ULocale.forLocale(locale);
  }
}
