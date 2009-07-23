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
import com.google.visualization.datasource.DataSourceRequest;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.base.ReasonType;
import com.google.visualization.datasource.base.ResponseStatus;
import com.google.visualization.datasource.base.StatusType;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.value.ValueType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * A demo servlet for serving a simple, constant data-table.
 *
 * For the sake of a complete example this servlet extends HttpServlet.
 * DataSourceServlet is an abstract class that provides a template behavior for
 * serving data source requests. Consider extending DataSourceServlet for
 * easier implementation.
 *
 * @see com.google.visualization.datasource.DataSourceServlet
 * @see com.google.visualization.datasource.example.SimpleExampleServlet
 */
public class SimpleExampleServlet2 extends HttpServlet {

  /**
   * The log used throughout the data source library.
   */
  private static final Log log = LogFactory.getLog(SimpleExampleServlet2.class.getName());
  
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    DataTable data = generateMyDataTable();
    DataSourceRequest dsRequest = null;

    try {
      // Extract the datasource request parameters.
      dsRequest = new DataSourceRequest(req);

      // NOTE: If you want to work in restricted mode, which means that only
      // requests from the same domain can access the data source, uncomment the following call.
      //
      // DataSourceHelper.verifyAccessApproved(dsRequest);

      // Apply the query to the data table.
      DataTable newData = DataSourceHelper.applyQuery(dsRequest.getQuery(), data,
          dsRequest.getUserLocale());

      // Set the response.
      DataSourceHelper.setServletResponse(newData, dsRequest, resp);
    } catch (RuntimeException rte) {
      log.error("A runtime exception has occured", rte);
      ResponseStatus status = new ResponseStatus(StatusType.ERROR, ReasonType.INTERNAL_ERROR,
          rte.getMessage());
      if (dsRequest == null) {
        dsRequest = DataSourceRequest.getDefaultDataSourceRequest(req);
      }
      DataSourceHelper.setServletErrorResponse(status, dsRequest, resp);
    } catch (DataSourceException e) {
      if (dsRequest != null) {
        DataSourceHelper.setServletErrorResponse(e, dsRequest, resp);
      } else {
        DataSourceHelper.setServletErrorResponse(e, req, resp);
      }
    }
  }

  private DataTable generateMyDataTable() {
    // Create a data table,
    DataTable data = new DataTable();
    ArrayList<ColumnDescription> cd = new ArrayList<ColumnDescription>();
    cd.add(new ColumnDescription("name", ValueType.TEXT, "Animal name"));
    cd.add(new ColumnDescription("link", ValueType.TEXT, "Link to wikipedia"));
    cd.add(new ColumnDescription("population", ValueType.NUMBER, "Population size"));
    cd.add(new ColumnDescription("vegeterian", ValueType.BOOLEAN, "Vegetarian?"));

    data.addColumns(cd);

    // Fill the data table.
    try {
      data.addRowFromValues("Aye-aye", "http://en.wikipedia.org/wiki/Aye-aye", 100, true);
      data.addRowFromValues("Sloth", "http://en.wikipedia.org/wiki/Sloth", 300, true);
      data.addRowFromValues("Leopard", "http://en.wikipedia.org/wiki/Leopard", 50, false);
      data.addRowFromValues("Tiger", "http://en.wikipedia.org/wiki/Tiger", 80, false);
    } catch (TypeMismatchException e) {
      System.out.println("Invalid type!");
    }
    return data;

  }
}
