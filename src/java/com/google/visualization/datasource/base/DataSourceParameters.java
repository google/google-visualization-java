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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class contains the data source parameters of the request.
 * The data source parameters are extracted from the "tqx" URL parameter.
 *
 * @author Nimrod T.
 */
public class DataSourceParameters {

  /**
   * Log.
   */
  private static final Log log = LogFactory.getLog(DataSourceParameters.class.getName());

  /**
   * The name of the request id parameter as it appears in the "tqx" URL parameter.
   */
  private static final String REQUEST_ID_PARAM_NAME = "reqId";

  /**
   * The name of the data signature parameter as it appears in the "tqx" URL parameter.
   */
  private static final String SIGNATURE_PARAM_NAME = "sig";

  /**
   * The name of the output type parameter as it appears in the "tqx" URL parameter.
   */
  private static final String OUTPUT_TYPE_PARAM_NAME = "out";

  /**
   * The name of the response handler parameter as it appears in the "tqx" URL parameter.
   */
  private static final String RESPONSE_HANDLER_PARAM_NAME = "responseHandler";

  /**
   * The name of the filename parameter as it appears in the "tqx" URL parameter.
   * This is relevant only if the "out" parameter is "csv".
   * In that case, if the outFileName is given,
   * this is the name of the csv that the datasource will output.
   * Otherwise, if missing, the default filename (data.csv) is used.
   */
  private static final String REQUEST_OUTFILENAME_PARAM_NAME = "outFileName";

  /**
   * A default error message.
   */
  private static final String DEFAULT_ERROR_MSG = "Internal error";

  /**
   * The original string passed as the value of the "tqx" parameter.
   */
  private String tqxValue = null;

  /**
   * The request id.
   */
  private String requestId = null;

  /**
   * The signature.
   */
  private String signature = null;

  /**
   * The output type.
   */
  private OutputType outputType = OutputType.defaultValue();

  /**
   * The response handler.
   */
  private String responseHandler = "google.visualization.Query.setResponse";

  /**
   * The out filename.
   */
  private String outFileName = "data.csv";

  /**
   * Returns a default DataSourceParameters object.
   *
   * @return A default DataSourceParameters object.
   */
  public static DataSourceParameters getDefaultDataSourceParameters() {
    DataSourceParameters dsParams = null;
    try {
      dsParams = new DataSourceParameters(null);
    } catch (DataSourceException e) {
      // Shouldn't be here.
    }
    return dsParams;
  }

  /**
   * Constructs a new instance of this class, with the given "tqx" string and parses it.
   *
   * Note: the "tqx" parameter is generated internally by the client side
   * libraries. Thus, both the user and the developer will receive only short
   * error messages regarding it. E.g., "Internal Error (code)".
   * 
   * @param tqxValue The "tqx" string to parse.
   * @throws DataSourceException Thrown if parsing of request parameters fails.
   */
  public DataSourceParameters(String tqxValue) throws DataSourceException {
    if (StringUtils.isEmpty(tqxValue)) {
      // Return the default values in case tqx is empty.
      return;
    }

    this.tqxValue = tqxValue;

    // Split the tqx value to it's pair parts.
    String[] parts = tqxValue.split(";");

    // Loop over the parts.
    for (String part : parts) {
      // Each part should be a pair of key value.
      String[] nameValuePair = part.split(":");
      if (nameValuePair.length != 2) {
        log.error("Invalid name-value pair: " + part);
        throw new DataSourceException(ReasonType.INVALID_REQUEST,
            DEFAULT_ERROR_MSG + "(malformed)");
      }

      // Get the name and value.
      String name = nameValuePair[0];
      String value = nameValuePair[1];

      // Parse each part.
      if (name.equals(REQUEST_ID_PARAM_NAME)) {
        requestId = value;
      } else if (name.equals(SIGNATURE_PARAM_NAME)) {
        signature = value;
      } else if (name.equals(OUTPUT_TYPE_PARAM_NAME)) {
        outputType = OutputType.findByCode(value);
        if (outputType == null) {
          outputType = OutputType.defaultValue();
        }
      } else if (name.equals(RESPONSE_HANDLER_PARAM_NAME)) {
        responseHandler = value;
      } else if (name.equals(REQUEST_OUTFILENAME_PARAM_NAME)) {
        outFileName = value;

        // A heuristic that adds ".csv" if no . is provided.
        if (!outFileName.contains(".")) {
          outFileName += ".csv";
        }
      } else {
        // The server is tolerant to keys that it isn't
        // aware of, to allow "Forward Compatibility"
        // to later version.
      }
    }
  }

  /**
   * Returns the request id.
   * 
   * @return The request id.
   */
  public String getRequestId() {
    return requestId;
  }

  /**
   * Returns the signature.
   * 
   * @return The signature.
   */
  public String getSignature() {
    return signature;
  }

  /**
   * Sets the signature.
   * 
   * @param signature The signature.
   */
  public void setSignature(String signature) {
    this.signature = signature;
  }

  /**
   * Returns the output type.
   * 
   * @return The output type.
   */
  public OutputType getOutputType() {
    return outputType;
  }

  /**
   * Sets the output type.
   *
   * @param outputType The output type.
   */
  public void setOutputType(OutputType outputType) {
    this.outputType = outputType;
  }

  /**
   * Returns the response handler.
   * 
   * @return The response handler.
   */
  public String getResponseHandler() {
    return responseHandler;
  }

  /**
   * Returns the out file name.
   * 
   * @return The out file name.
   */
  public String getOutFileName() {
    return outFileName;
  }

  /**
   * Returns the "tqx" value.
   * 
   * @return The "tqx" value.
   */
  public String getTqxValue() {
    return tqxValue;
  }
}
