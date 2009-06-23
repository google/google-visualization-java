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

import com.google.visualization.datasource.base.ReasonType;
import com.google.visualization.datasource.base.ResponseStatus;
import com.google.visualization.datasource.base.StatusType;
import com.google.visualization.datasource.base.Warning;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.ValueFormatter;
import com.google.visualization.datasource.datatable.value.BooleanValue;
import com.google.visualization.datasource.datatable.value.ValueType;

import com.ibm.icu.util.ULocale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Takes a data table and returns an html string.
 *
 * @author Nimrod T.
 */
public class HtmlRenderer {
  
  /**
   * Log.
   */
  private static final Log log = LogFactory.getLog("HtmlRenderer");  

  /**
   * Private constructor.
   */
  private HtmlRenderer() {}

  /**
   * Pattern for matching against &lt;a&gt; tags with hrefs.
   * Used in sanitizeDetailedMessage.
   */
  private static final Pattern DETAILED_MESSAGE_A_TAG_REGEXP = Pattern.compile(
      "([^<]*<a(( )*target=\"_blank\")*(( )*target='_blank')*"
      + "(( )*href=\"[^\"]*\")*(( )*href='[^']*')*>[^<]*</a>)+[^<]*");

  /**
   * Pattern for matching against "javascript:".
   * Used in sanitizeDetailedMessage.
   */
  private static final Pattern BAD_JAVASCRIPT_REGEXP = Pattern.compile("javascript(( )*):");

  /**
   * Generates an HTML string representation of a data table.
   * 
   * @param dataTable The data table to render.
   * @param locale The locale. If null, uses the default from
   *     {@code LocaleUtil#getDefaultLocale}.
   *
   * @return The char sequence with the html string.
   */
  public static CharSequence renderDataTable(DataTable dataTable, ULocale locale) {
    // Create an xml document with head and an empty body.
    Document document = createDocument();
    Element bodyElement = appendHeadAndBody(document);

    // Populate the xml document.
    Element tableElement = document.createElement("table");
    bodyElement.appendChild(tableElement);
    tableElement.setAttribute("border", "1");
    tableElement.setAttribute("cellpadding", "2");
    tableElement.setAttribute("cellspacing", "0");

    // Labels tr element.
    List<ColumnDescription> columnDescriptions = dataTable.getColumnDescriptions();
    Element trElement = document.createElement("tr");
    trElement.setAttribute("style", "font-weight: bold; background-color: #aaa;");
    for (ColumnDescription columnDescription : columnDescriptions) {
      Element tdElement = document.createElement("td");
      tdElement.setTextContent(columnDescription.getLabel());
      trElement.appendChild(tdElement);
    }
    tableElement.appendChild(trElement);

    Map<ValueType, ValueFormatter> formatters = ValueFormatter.createDefaultFormatters(locale);
    // Table tr elements.
    int rowCount = 0;
    for (TableRow row : dataTable.getRows()) {
      rowCount++;
      trElement = document.createElement("tr");
      String backgroundColor = (rowCount % 2 != 0) ? "#f0f0f0" : "#ffffff";
      trElement.setAttribute("style", "background-color: " + backgroundColor);

      List<TableCell> cells = row.getCells();
      for (int c = 0; c < cells.size(); c++) {
        ValueType valueType = columnDescriptions.get(c).getType();
        TableCell cell = cells.get(c);
        String cellFormattedText = cell.getFormattedValue();
        if (cellFormattedText == null) {
          cellFormattedText = formatters.get(cell.getType()).format(cell.getValue());
        }

        Element tdElement = document.createElement("td");
        if (cell.isNull()) {
          tdElement.setTextContent("\u00a0");
        } else {
          switch (valueType) {
            case NUMBER:
              tdElement.setAttribute("align", "right");
              tdElement.setTextContent(cellFormattedText);
              break;
            case BOOLEAN:
              BooleanValue booleanValue = (BooleanValue) cell.getValue();
              tdElement.setAttribute("align", "center");
              if (booleanValue.getValue()) {
                tdElement.setTextContent("\u2714"); // Check mark.
              } else {
                tdElement.setTextContent("\u2717"); // X mark.
              }
              break;
            default:
              if (StringUtils.isEmpty(cellFormattedText)) {
                tdElement.setTextContent("\u00a0"); // nbsp.
              } else {
                tdElement.setTextContent(cellFormattedText);
              }
          }
        }
        trElement.appendChild(tdElement);
      }
      tableElement.appendChild(trElement);
    }
    bodyElement.appendChild(tableElement);

    // Warnings:
    for (Warning warning : dataTable.getWarnings()) {
      bodyElement.appendChild(document.createElement("br"));
      bodyElement.appendChild(document.createElement("br"));
      Element messageElement = document.createElement("div");
      messageElement.setTextContent(warning.getReasonType().getMessageForReasonType() + ". " +
          warning.getMessage());
      bodyElement.appendChild(messageElement);
    }

    return transformDocumentToHtmlString(document);
  }

  /**
   * Transforms a document to a valid html string.
   *
   * @param document The document to transform
   *
   * @return A string representation of a valid html.
   */
  private static String transformDocumentToHtmlString(Document document) {
    // Generate a CharSequence from the xml document.
    Transformer transformer = null;
    try {
      transformer = TransformerFactory.newInstance().newTransformer();
    } catch (TransformerConfigurationException e) {
      log.error("Couldn't create a transformer", e);
      throw new RuntimeException("Couldn't create a transformer. This should never happen.", e);
    }
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//W3C//DTD HTML 4.01//EN");
    transformer.setOutputProperty(OutputKeys.METHOD, "html");
    transformer.setOutputProperty(OutputKeys.VERSION, "4.01");
    
    DOMSource source = new DOMSource(document);
    Writer writer = new StringWriter();
    StreamResult result = new StreamResult(writer);
    try {
      transformer.transform(source, result);
    } catch (TransformerException e) {
      log.error("Couldn't transform", e);
      throw new RuntimeException("Couldn't transform. This should never happen.", e);
    }

    return writer.toString();
  }

  /**
   * Sanitizes the html in the detailedMessage, to allow only href inside &lt;a&gt; tags.
   *
   * @param detailedMessage The detailedMessage.
   *
   * @return The sanitized detailedMessage.
   */
  static String sanitizeDetailedMessage(String detailedMessage) {
    if (StringUtils.isEmpty(detailedMessage)) {
      return "";
    }

    if (DETAILED_MESSAGE_A_TAG_REGEXP.matcher(detailedMessage).matches()
        && (!BAD_JAVASCRIPT_REGEXP.matcher(detailedMessage).find())) {
      // No need to escape.
      return detailedMessage;
    } else {
      // Need to html escape.
      return EscapeUtil.htmlEscape(detailedMessage);
    }
  }

  /**
   * Renders a simple html for the given responseStatus.
   *
   * @param responseStatus The response message.
   *
   * @return A simple html for the given responseStatus.
   */
  public static CharSequence renderHtmlError(ResponseStatus responseStatus) {
    // Get the responseStatus details.
    StatusType status = responseStatus.getStatusType();
    ReasonType reason = responseStatus.getReasonType();
    String detailedMessage = responseStatus.getDescription();

    // Create an xml document with head and an empty body.
    Document document = createDocument();
    Element bodyElement = appendHeadAndBody(document);

    // Populate the xml document.
    Element oopsElement = document.createElement("h3");
    oopsElement.setTextContent("Oops, an error occured.");
    bodyElement.appendChild(oopsElement);

    if (status != null) {
      String text = "Status: " + status.lowerCaseString();
      appendSimpleText(document, bodyElement, text);
    }

    if (reason != null) {
      String text = "Reason: " + reason.getMessageForReasonType(null);
      appendSimpleText(document, bodyElement, text);
    }

    if (detailedMessage != null) {
      String text = "Description: " + sanitizeDetailedMessage(detailedMessage);
      appendSimpleText(document, bodyElement, text);
    }

    return transformDocumentToHtmlString(document);
  }

  /**
   * Creates a document element.
   *
   * @return A document element.
   */
  private static Document createDocument() {
    DocumentBuilder documentBuilder = null;
    try {
      documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      log.error("Couldn't create a document builder", e);
      throw new RuntimeException(
          "Couldn't create a document builder. This should never happen.", e);
    }
    Document document = documentBuilder.newDocument();
    return document;
  }

  /**
   * Appends &lt;html&gt;, &lt;head&gt;, &lt;title&gt;, and &lt;body&gt; elements to the document.
   *
   * @param document The containing document.
   *
   * @return The &lt;body&gt; element.
   */
  private static Element appendHeadAndBody(Document document) {
    Element htmlElement = document.createElement("html");
    document.appendChild(htmlElement);
    Element headElement = document.createElement("head");
    htmlElement.appendChild(headElement);
    Element titleElement = document.createElement("title");
    titleElement.setTextContent("Google Visualization");
    headElement.appendChild(titleElement);
    Element bodyElement = document.createElement("body");
    htmlElement.appendChild(bodyElement);
    return bodyElement;
  }

  /**
   * Appends a simple text line to the body of the document.
   *
   * @param document The containing document.
   * @param bodyElement The body of the document.
   * @param text The text to append.
   */
  private static void appendSimpleText(Document document, Element bodyElement, String text) {
    Element statusElement = document.createElement("div");
    statusElement.setTextContent(text);
    bodyElement.appendChild(statusElement);
  }
}
