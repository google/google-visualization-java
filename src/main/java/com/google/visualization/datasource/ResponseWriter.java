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

import com.google.visualization.datasource.base.DataSourceParameters;
import com.google.visualization.datasource.base.OutputType;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

/**
 * A helper class responsible for writing a response message on a <code>HttpServletResponse</code>.
 *
 * @author Nimrod T.
 */
public class ResponseWriter {

  /**
   * UTF-16LE byte-order mark required by TSV_EXCEL output type.
   * @see OutputType#TSV_EXCEL
   */
  private static final byte[] UTF_16LE_BOM = new byte[] {(byte) 0xff, (byte) 0xfe};
  
  /**
   * A private constructor.
   */
  private ResponseWriter() {}

  /**
   * Sets the specified responseMessage on the given <code>HttpServletResponse</code>.
   * This method assumes the <code>StatusType</code> is 'OK'.
   *
   * @param responseMessage The response message.
   * @param dataSourceParameters The datasource parameters.
   * @param res The HTTP response.
   *
   * @throws IOException In case of a I/O error.
   */
  public static void setServletResponse(String responseMessage,
      DataSourceParameters dataSourceParameters, HttpServletResponse res) throws IOException {
    OutputType type = dataSourceParameters.getOutputType();
    switch (type) {
      case CSV:
        setServletResponseCSV(dataSourceParameters, res);
        writeServletResponse(responseMessage, res);
        break;
      case TSV_EXCEL:
        setServletResponseTSVExcel(dataSourceParameters, res);
        writeServletResponse(responseMessage, res, "UTF-16LE", UTF_16LE_BOM);
        break;
      case HTML:
        setServletResponseHTML(res);
        writeServletResponse(responseMessage, res);
        break;
      case JSONP:
        setServletResponseJSONP(res);
        writeServletResponse(responseMessage, res);
        break;
      case JSON:
        setServletResponseJSON(res);
        writeServletResponse(responseMessage, res);
        break;
      default:
        // This should never happen.
        throw new RuntimeException("Unhandled output type.");
    }
  }

  /**
   * Sets the specified responseMessage on the given <code>HttpServletResponse</code> if
   * the <code>OutputType</code> is CSV.
   * This method assumes the <code>StatusType</code> is 'OK'.
   *
   * @param responseMessage The response message.
   * @param dataSourceParameters The data source parameters.
   * @param res The HTTP response.
   *
   * @throws IOException In case of a I/O error.
   */
  static void setServletResponseCSV(DataSourceParameters dataSourceParameters,
      HttpServletResponse res) {
    res.setContentType("text/csv; charset=UTF-8");
    String outFileName = dataSourceParameters.getOutFileName();
    
    // For security reasons, make sure the file extension is ".csv".
    if (!outFileName.toLowerCase().endsWith(".csv")) {
      outFileName = outFileName + ".csv";
    }
    
    res.setHeader("Content-Disposition", "attachment; filename=" + outFileName);
  }
  
  /**
   * Sets the HTTP servlet response for a TSV_EXCEL output type.
   * This method assumes the <code>StatusType</code> is 'OK'.
   *
   * @param responseMessage The response message.
   * @param dsParams The data source parameters.
   * @param res The HTTP response.
   *
   * @throws IOException In case of a I/O error.
   */
  static void setServletResponseTSVExcel(DataSourceParameters dsParams,
      HttpServletResponse res) {
    res.setContentType("text/csv; charset=UTF-16LE");
    String outFileName = dsParams.getOutFileName();
    res.setHeader("Content-Disposition", "attachment; filename=" + outFileName);
  }
  
  /**
   * Sets the HTTP servlet response for a HTML output type.
   * This method assumes the <code>StatusType</code> is 'OK'.
   *
   * @param responseMessage The response message.
   * @param res The HTTP response.
   *
   * @throws IOException In case of a I/O error.
   */
  static void setServletResponseHTML(HttpServletResponse res) {
    res.setContentType("text/html; charset=UTF-8");
  }
  
  /**
   * Sets the HTTP servlet response for a JSONP output type.
   * This method assumes the <code>StatusType</code> is 'OK'.
   *
   * @param responseMessage The response char sequence.
   * @param res The HTTP response.
   *
   * @throws IOException In case of a I/O error.
   */
  static void setServletResponseJSONP(HttpServletResponse res) {
    res.setContentType("text/javascript; charset=UTF-8");
  }
  
  /**
   * Sets the HTTP servlet response for a JSON output type.
   * This method assumes the <code>StatusType</code> is 'OK'.
   *
   * @param responseMessage The response char sequence.
   * @param res The HTTP response.
   *
   * @throws IOException In case of a I/O error.
   */
  static void setServletResponseJSON(HttpServletResponse res) {
    res.setContentType("application/json; charset=UTF-8");
  }

  /**
   * Writes the response to the servlet response using UTF-8 charset without
   * byte-order mark.
   *
   * @param responseMessage A charSequence to write to the servlet response.
   * @param res The servlet response.
   *
   * @throws IOException In case of a I/O error.
   */
  private static void writeServletResponse(CharSequence responseMessage, HttpServletResponse res) 
      throws IOException {
    writeServletResponse(responseMessage, res, "UTF-8", null);
  }

  /**
   * Writes the response to the servlet response using specified charset and an
   * optional byte-order mark.
   *
   * @param charSequence A charSequence to write to the servlet response.
   * @param res The servlet response.
   * @param charset A {@code String} specifying one of the character sets
   *        defined by IANA Character Sets
   *        (http://www.iana.org/assignments/character-sets).
   * @param byteOrderMark An optional byte-order mark.
   *
   * @throws IOException In case of a I/O error.
   */
  private static void writeServletResponse(CharSequence charSequence, HttpServletResponse res,
      String charset, byte[] byteOrderMark) throws IOException {
    ServletOutputStream outputStream = res.getOutputStream();
    if (byteOrderMark != null) {
      outputStream.write(byteOrderMark);
    }
    outputStream.write(charSequence.toString().getBytes(charset));
  }
}
