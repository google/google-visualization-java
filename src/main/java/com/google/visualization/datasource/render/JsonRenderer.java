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

package com.google.visualization.datasource.render;

import com.google.common.collect.Lists;
import com.google.visualization.datasource.base.DataSourceParameters;
import com.google.visualization.datasource.base.OutputType;
import com.google.visualization.datasource.base.ReasonType;
import com.google.visualization.datasource.base.ResponseStatus;
import com.google.visualization.datasource.base.StatusType;
import com.google.visualization.datasource.base.Warning;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.BooleanValue;
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TimeOfDayValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;

import com.ibm.icu.util.GregorianCalendar;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;

import java.util.List;
import java.util.Map;

/**
 * Takes a data table and returns a json string.
 * 
 * The renderer renders a response which primarily contains the serializing of a data table.
 * The response type can be either json or jsonp (a json format wrapped inside a callback method).
 * The main difference regarding data table serialization between the two types, is the way dates
 * are serialized. Dates have no standard representation in json thus they are rendered according
 * to the future use of the response. When the response type is jsonp, the data table is likely to
 * be evaluated with standard browser 'eval' and therefore date represented by Date constructor
 * will be transformed into javaScript dates (as expected), e.g new Date(2011,1,1).
 * When the response type is json, Date constructors can not be used (not defined in the json
 * standard) and so custom date strings are used instead. In this case, custom parsing of the string
 * is required to construct dates objects from their respective strings, e.g "Date(2011,1,1)".
 * 
 * @author Nimrod T.
 */
public class JsonRenderer {

  /**
   * Private constructor.
   */
  private JsonRenderer() {}

  /**
   * Returns a String-form 32-bit hash of this table's json.
   * Note: the signature ignores formatting.
   *
   * @param data The data table.
   *
   * @return a String-form 64-bit hash of this table.
   */
  public static String getSignature(DataTable data) {
    String tableAsString = renderDataTable(data, true, false, true).toString();
    // Casting to long to avoid bug with abs(Integer.MIN_VALUE) being negative.
    long longHashCode = tableAsString.hashCode();
    return String.valueOf(Math.abs(longHashCode));
  }

  /**
   * Returns the part of the json string that describes a single warning or error.
   *
   * @param reasonType The reason for the warning or error.
   * @param description The detailed description for this warning or error.
   *
   * @return The part of the json string.
   */
  private static String getFaultString(ReasonType reasonType, String description) {
    List<String> objectParts = Lists.newArrayList();
    if (reasonType != null) {
      objectParts.add("\"reason\":\"" + reasonType.lowerCaseString() + "\"");
      objectParts.add("\"message\":\"" + EscapeUtil.jsonEscape(
              reasonType.getMessageForReasonType(null)) + "\"");
    }

    if (description != null) {
      objectParts.add("\"detailed_message\":\"" + EscapeUtil.jsonEscape(description)
          + "\"");
    }
    return new StrBuilder("{").appendWithSeparators(objectParts, ",").append("}").toString();
  }

  /**
   * @deprecated As of version 1.1.1, isJsonp is removed and instead is inferred from dsParams.
   *    Please use:
   *    renderJsonResponse(DataSourceParameters dsParams, ResponseStatus responseStatus,
   *        DataTable data)
   *    
   * Returns the json response for the given data table.
   *
   * @param dsParams The datasource parameters.
   * @param responseStatus The response status.
   * @param data The data table.
   * @param True if response should be rendered as jsonp, otherwise False.
   *
   * @return The json response for the given data table and parameters.
   */
  @Deprecated public static CharSequence renderJsonResponse(
      DataSourceParameters dsParams,
      ResponseStatus responseStatus,
      DataTable data,
      boolean isJsonp) {
    dsParams.setOutputType(OutputType.JSONP);
    return renderJsonResponse(dsParams, responseStatus, data);
  }
  
  /**
   * Returns the json response for the given data table.
   *
   * @param dsParams The datasource parameters. If the OutputType parameter is set to
   *     JSONP the response will be rendered as JSONP. Otherwise a plain JSON string will
   *     be returned.
   * @param responseStatus The response status.
   * @param data The data table.
   *
   * @return The json response for the given data table and parameters.
   */
  public static CharSequence renderJsonResponse(
      DataSourceParameters dsParams,
      ResponseStatus responseStatus,
      DataTable data) {
    StrBuilder sb = new StrBuilder();
    boolean isJsonp = dsParams.getOutputType() == OutputType.JSONP;
    if (isJsonp) {
      sb.append(dsParams.getResponseHandler()).append("(");
    }
    sb.append("{\"version\":\"0.6\"");

    // If no reqId found in the request, do not return reqId in the response.
    String requestId = dsParams.getRequestId();
    if (requestId != null) {
      sb.append(",\"reqId\":\"").append(EscapeUtil.jsonEscape(requestId)).append("\"");
    }

    // Check signature.
    String previousSignature = dsParams.getSignature();
    if (responseStatus == null) {
      if (!StringUtils.isEmpty(previousSignature) && (data != null)
          && (JsonRenderer.getSignature(data).equals(previousSignature))) {
        responseStatus = new ResponseStatus(StatusType.ERROR, ReasonType.NOT_MODIFIED, null);
      } else {
        responseStatus = new ResponseStatus(StatusType.OK, null, null);
      }
    }

    StatusType statusType = responseStatus.getStatusType();
    sb.append(",\"status\":\"").append(statusType.lowerCaseString()).append("\"");

    // There are reason and messages if the status is WARNING/ERROR.
    if (statusType != StatusType.OK) {
      // Status is warning or error.
      if (statusType == StatusType.WARNING) {
        List<Warning> warnings = data.getWarnings();
        List<String> warningJsonStrings = Lists.newArrayList();
        if (warnings != null) {
          for (Warning warning : warnings) {
            warningJsonStrings.add(getFaultString(warning.getReasonType(), warning.getMessage()));
          }
        }
        sb.append(",\"warnings\":[").appendWithSeparators(warningJsonStrings, ",").append("]");

      } else { // Status is error.
        sb.append(",\"errors\":[");
        sb.append(getFaultString(responseStatus.getReasonType(), responseStatus.getDescription()));
        sb.append("]");
      }
    }
    
    if ((statusType != StatusType.ERROR) && (data != null)) {
      // MessageType OK or WARNING,
      // so need to attach a data table (and a signature).
      sb.append(",\"sig\":\"").append(JsonRenderer.getSignature(data)).append("\"");
      sb.append(",\"table\":").append(JsonRenderer.renderDataTable(data, true, true, isJsonp));
    }
    
    sb.append("}");
    if (isJsonp) {
      sb.append(");");
    }
    
    return sb.toString();
  }

  /**
   * @deprecated As of version 1.1.1, renderDateAsDateConstructor parameter added. Please use:
   *    renderDataTable(DataTable dataTable, boolean includeValues, boolean includeFormatting,
   *        boolean renderDateAsDateConstructor)
   * 
   * Generates a JSON representation of the data table object.
   *
   * @param includeValues False if the json should contain just meta-data and column descriptions
   *     but without the data rows.
   * @param includeFormatting False if formatting information should be omitted from the
   *     generated json.
   *       
   * @return The char sequence with the Json string.
   */
  @Deprecated public static CharSequence renderDataTable(DataTable dataTable, boolean includeValues,
      boolean includeFormatting) {
    return renderDataTable(dataTable, includeValues, includeFormatting, true);
  }
  
  /**
   * Generates a JSON representation of the data table object.
   *
   * @param includeValues False if the json should contain just meta-data and column descriptions
   *     but without the data rows.
   * @param includeFormatting False if formatting information should be omitted from the
   *     generated json.
   * @param renderDateAsDateConstructor True -> date constructor, False -> date string.
   *     True if date values should be rendered into the json string as a call to
   *     Date object constructor (usually used when rendering jsonp string). 
   *     False if it should should be rendered as string.
   *     For example, when rendering the date 1/1/2011 as Date object constructor its value
   *     in the json string will be new Date(2011,1,1), and when rendered as string
   *     will be "Date(2011,1,1)". For further explanation, see class comment.
   *       
   * @return The char sequence with the Json string.
   */
  public static CharSequence renderDataTable(DataTable dataTable, boolean includeValues, 
      boolean includeFormatting, boolean renderDateAsDateConstructor) {
    if (dataTable.getColumnDescriptions().isEmpty()) {
      return "";
    }

    List<ColumnDescription> columnDescriptions = dataTable.getColumnDescriptions();

    StringBuilder sb = new StringBuilder();
    sb.append("{");
    sb.append("\"cols\":["); // column descriptions.

    ColumnDescription col;
    for (int colId = 0; colId < columnDescriptions.size(); colId++) {
      col = columnDescriptions.get(colId);
      appendColumnDescriptionJson(col, sb);
      if (colId != (columnDescriptions.size() - 1)) {
        sb.append(",");
      }
    }
    sb.append("]"); // columns.

    if (includeValues) {
      sb.append(",\"rows\":[");
      List<TableCell> cells;
      TableCell cell;
      ColumnDescription columnDescription;

      List<TableRow> rows = dataTable.getRows();
      for (int rowId = 0; rowId < rows.size(); rowId++) {
        TableRow tableRow = rows.get(rowId);
        cells = tableRow.getCells();
        sb.append("{\"c\":[");
        for (int cellId = 0; cellId < cells.size(); cellId++) {
          cell = cells.get(cellId);
          if (cellId < (cells.size() - 1)) {
            appendCellJson(cell, sb, includeFormatting, false, renderDateAsDateConstructor);
            sb.append(",");
          } else {
            // Last column in the row.
            appendCellJson(cell, sb, includeFormatting, true, renderDateAsDateConstructor);
          }
        }
        sb.append("]");

        // Row properties.
        String customPropertiesString = getPropertiesMapString(tableRow.getCustomProperties());
        if (customPropertiesString != null) {
          sb.append(",\"p\":").append(customPropertiesString);
        }

        sb.append("}"); // cells.
        if ((rows.size() - 1) > rowId) {
          sb.append(",");
        }
      }

      sb.append("]"); // rows.
    }

    // Table properties.
    String customPropertiesString = getPropertiesMapString(dataTable.getCustomProperties());
    if (customPropertiesString != null) {
      sb.append(",\"p\":").append(customPropertiesString);
    }

    sb.append("}"); // table.
    return sb;
  }
  
  /**
   * @deprecated As of version 1.1.1, changed visibility to private.
   *    
   * Appends a Json representing a cell to the string buffer.
   *
   * @param cell The cell to write Json for.
   * @param sb The string buffer to append to.
   * @param includeFormatting Flase if formatting information should be omitted from the json.
   * @param isLastColumn Is this the last column in the row.
   *
   * @return The input string builder.
   */
  @Deprecated public static StringBuilder appendCellJson(TableCell cell,
      StringBuilder sb, boolean includeFormatting, boolean isLastColumn) {
    return appendCellJson(cell, sb, includeFormatting, isLastColumn, true);
  }

  /**
   * Appends a Json representing a cell to the string buffer.
   *
   * @param cell The cell to write Json for.
   * @param sb The string buffer to append to.
   * @param includeFormatting Flase if formatting information should be omitted from the json.
   * @param isLastColumn Is this the last column in the row.
   * @param renderDateAsDateConstructor True -> date constructor, False -> date string.
   *     True if date values should be rendered into the json string as a call to
   *     Date object constructor (usually used when rendering jsonp string). 
   *     False if it should should be rendered as string.
   *     For example, when rendering the date 1/1/2011 as Date object constructor its value
   *     in the json string will be new Date(2011,1,1), and when rendered as string
   *     will be "Date(2011,1,1)". For further explanation, see class comment.
   *
   * @return The input string builder.
   */
  static StringBuilder appendCellJson(TableCell cell, 
      StringBuilder sb, boolean includeFormatting, boolean isLastColumn,
      boolean renderDateAsDateConstructor) {
    Value value = cell.getValue();
    ValueType type = cell.getType();
    StringBuilder valueJson = new StringBuilder();
    GregorianCalendar calendar;
    String escapedFormattedString = "";
    boolean isJsonNull = false;

    // Prepare a Json string representing the current value.
    DateValue dateValue;
    TimeOfDayValue timeOfDayValue;
    if ((value == null) || (value.isNull())) {
      valueJson.append("null");
      isJsonNull = true;
    } else {
      switch (type) {
        case BOOLEAN:
          valueJson.append(((BooleanValue) value).getValue());
          break;
        case DATE:
          valueJson.append("Date(");
          dateValue = (DateValue) value;
          valueJson.append(dateValue.getYear()).append(",");
          valueJson.append(dateValue.getMonth()).append(",");
          valueJson.append(dateValue.getDayOfMonth());
          valueJson.append(")");
          if (renderDateAsDateConstructor) {
            // Rendering date as a call to Date constructor, e.g new Date(2011,1,1)
            valueJson.insert(0, "new ");
          } else {
            // Rendering date in string format, e.g "Date(2011,1,1)"
            valueJson.insert(0, "\"");
            valueJson.append("\"");           
          }
          break;
        case NUMBER:
          valueJson.append(((NumberValue) value).getValue());
          break;
        case TEXT:
          valueJson.append("\"");
          valueJson.append(EscapeUtil.jsonEscape(value.toString()));
          valueJson.append("\"");
          break;
        case TIMEOFDAY:
          valueJson.append("[");
          timeOfDayValue = (TimeOfDayValue) value;
          valueJson.append(timeOfDayValue.getHours()).append(",");
          valueJson.append(timeOfDayValue.getMinutes()).append(",");
          valueJson.append(timeOfDayValue.getSeconds()).append(",");
          valueJson.append(timeOfDayValue.getMilliseconds());
          valueJson.append("]");
          break;
        case DATETIME:
          calendar = ((DateTimeValue) value).getCalendar();
          valueJson.append("Date(");
          valueJson.append(calendar.get(GregorianCalendar.YEAR)).append(",");
          valueJson.append(calendar.get(GregorianCalendar.MONTH)).append(",");
          valueJson.append(calendar.get(GregorianCalendar.DAY_OF_MONTH));
          valueJson.append(",");
          valueJson.append(calendar.get(GregorianCalendar.HOUR_OF_DAY));
          valueJson.append(",");
          valueJson.append(calendar.get(GregorianCalendar.MINUTE)).append(",");
          valueJson.append(calendar.get(GregorianCalendar.SECOND));
          valueJson.append(")");
          if (renderDateAsDateConstructor) {
            // Rendering date as a call to Date constructor, e.g new Date(2011,1,1,0,0,0)
            valueJson.insert(0, "new ");
          } else {
            // Rendering date in string format, e.g "Date(2011,1,1,0,0,0)"
            valueJson.insert(0, "\"");
            valueJson.append("\"");           
          }
          break;
        default:
          throw new IllegalArgumentException("Illegal value Type " + type);
      }
    }

    // Prepare an escaped string representing the current formatted value.
    String formattedValue = cell.getFormattedValue();
    if ((value != null) && !value.isNull() && (formattedValue != null)) {
      escapedFormattedString = EscapeUtil.jsonEscape(formattedValue);
      // Check for a value of type TEXT if the formatted value equals
      // its ordinary toString.
      if ((type == ValueType.TEXT) && value.toString().equals(formattedValue)) {
        escapedFormattedString = "";
      }
    }

    // Add a Json for this cell. And,
    // 1) If the formatted value is empty drop it.
    // 2) If the value is null, and it is not the last column in the row drop the entire Json.
    if ((isLastColumn) || (!isJsonNull)) {
      sb.append("{");
      // Value
      sb.append("\"v\":").append(valueJson);
      // Formatted value
      if ((includeFormatting) && (!escapedFormattedString.equals(""))) {
        sb.append(",\"f\":\"").append(escapedFormattedString).append("\"");
      }
      String customPropertiesString = getPropertiesMapString(cell.getCustomProperties());
      if (customPropertiesString != null) {
        sb.append(",\"p\":").append(customPropertiesString);
      }
      sb.append("}");
    }
    return sb;
  }

  /**
   * Appends a Json representing a column description to the string buffer.
   *
   * @param col The column description to write Json for.
   * @param sb The string builder to append to.
   *
   * @return The input string builder.
   */
  public static StringBuilder appendColumnDescriptionJson(
      ColumnDescription col, StringBuilder sb) {
    sb.append("{");
    sb.append("\"id\":\"").append(EscapeUtil.jsonEscape(col.getId())).append("\",");
    sb.append("\"label\":\"").append(EscapeUtil.jsonEscape(col.getLabel())).append("\",");
    sb.append("\"type\":\"").append(col.getType().getTypeCodeLowerCase()).append("\",");
    sb.append("\"pattern\":\"").append(EscapeUtil.jsonEscape(col.getPattern())).append("\"");

    String customPropertiesString = getPropertiesMapString(col.getCustomProperties());
    if (customPropertiesString != null) {
      sb.append(",\"p\":").append(customPropertiesString);
    }

    sb.append("}");
    return sb;
  }

  /**
   * Makes a string from a properties map.
   *
   * @param propertiesMap The properties map.
   *
   * @return A json string.
   */
  private static String getPropertiesMapString(Map<String, String> propertiesMap) {
    String customPropertiesString = null;
    if ((propertiesMap != null) && (!propertiesMap.isEmpty())) {
      List<String> customPropertiesStrings = Lists.newArrayList();
      for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
        customPropertiesStrings.add("\""
            + EscapeUtil.jsonEscape(entry.getKey()) + "\":\""
            + EscapeUtil.jsonEscape(entry.getValue()) + "\"");
      }
      customPropertiesString = new StrBuilder("{")
          .appendWithSeparators(customPropertiesStrings, ",").append("}").toString();
    }
    return customPropertiesString;
  }
}
