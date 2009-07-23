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

import com.google.common.collect.Lists;
import com.google.visualization.datasource.Capabilities;
import com.google.visualization.datasource.DataSourceHelper;
import com.google.visualization.datasource.DataSourceRequest;
import com.google.visualization.datasource.QueryPair;
import com.google.visualization.datasource.base.DataSourceException;
import com.google.visualization.datasource.base.ReasonType;
import com.google.visualization.datasource.base.ResponseStatus;
import com.google.visualization.datasource.base.StatusType;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.AbstractColumn;
import com.google.visualization.datasource.query.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * A demo servlet for serving a data table from a data source that has query capabilities.
 * This data source has a native 'select' capability.
 * Other parts of the query are handled by the completion query.
 *
 * The data source can return two different data tables, depending on the
 * tableId parameter. By default the data source returns the 'animals' data table, unless the
 * tableId parameter is set to 'planets' in which case the data source returns the 'planets' data 
 * table.
 *
 * For the sake of a complete example this servlet extends HttpServlet.
 * DataSourceServlet is an abstract class that provides a template behavior for
 * serving data source requests. Consider extending DataSourceServlet for
 * easier implementation.
 *
 * @see com.google.visualization.datasource.DataSourceServlet
 * @see com.google.visualization.datasource.example.AdvancedExampleServlet
 *
 * @author Eran W.
 */
public class AdvancedExampleServlet2 extends HttpServlet {

  /**
   * The log used throughout the data source library.
   */
  private static final Log log = LogFactory.getLog(AdvancedExampleServlet2.class.getName());
  
  /**
   * A Map of animal names to link in wikipedia describing them.
   */
  private static Map<String, String> animalLinksByName = new TreeMap<String, String>();
  static {
    animalLinksByName.put("Fish", "http://en.wikipedia.org/wiki/Fish");
    animalLinksByName.put("Dog", "http://en.wikipedia.org/wiki/Dog");
    animalLinksByName.put("Cat", "http://en.wikipedia.org/wiki/Cat");
    animalLinksByName.put("Cow", "http://en.wikipedia.org/wiki/Cow");
    animalLinksByName.put("Tiger", "http://en.wikipedia.org/wiki/Tiger");
    animalLinksByName.put("Elephant", "http://en.wikipedia.org/wiki/Elephant");
    animalLinksByName.put("Goat", "http://en.wikipedia.org/wiki/Goat");
    animalLinksByName.put("Aye-aye", "http://en.wikipedia.org/wiki/Aye-aye");
    animalLinksByName.put("Sloth", "http://en.wikipedia.org/wiki/Sloth");
  }

  private static final String ANIMAL_COLUMN = "animal";
  private static final String ARTICLE_COLUMN = "article";

  private static final ColumnDescription[] ANIMAL_TABLE_COLUMNS =
      new ColumnDescription[] {
        new ColumnDescription(ANIMAL_COLUMN, ValueType.TEXT, "Animal"),
        new ColumnDescription(ARTICLE_COLUMN, ValueType.TEXT, "Link to wikipedia")
  };

  /**
   * An enum of planets. Each planet has a name, surface gravity and 
   * number of moons.
   */
  private enum Planet {
    JUPITER (317.8, 11.2, 63),
    SATURN  (95.2, 9.4, 60),
    URANUS  (14.5, 4, 27),
    NEPTUNE (17.2, 3.9, 13);

    private final double mass;
    private final double surfaceGravity;
    private final int numberOfMoons;

    private Planet(double mass, double surfaceGravity, int numberOfMoons) {
      this.mass = mass;
      this.surfaceGravity = surfaceGravity;
      this.numberOfMoons = numberOfMoons;
    }

    public double getMass() {
      return mass;
    }

    public double getSurfaceGravity() {
      return surfaceGravity;
    }

    public int getNumberOfMoons() {
      return numberOfMoons;
    }
  }

  private static final String PLANET_COLUMN = "planet";
  private static final String MASS_COLUMN = "mass";
  private static final String GRAVITY_COLUMN = "gravity";
  private static final String MOONS_COLUMN = "moons";

  private static final ColumnDescription[] planetTableColumns =
      new ColumnDescription[] {
        new ColumnDescription(PLANET_COLUMN, ValueType.TEXT, "Planet"),
        new ColumnDescription(MASS_COLUMN, ValueType.NUMBER, "Mass"),
        new ColumnDescription(GRAVITY_COLUMN, ValueType.NUMBER, "Surface Gravity"),
        new ColumnDescription(MOONS_COLUMN, ValueType.NUMBER, "Number of Moons")
  };

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    DataSourceRequest dsRequest = null;

    try {
      // Extract the request parameters.
      dsRequest = new DataSourceRequest(req);

      // NOTE: If you want to work in restricted mode, which means that only
      // requests from the same domain can access the data source, you should
      // uncomment the following call.
      //
      // DataSourceHelper.verifyAccessApproved(dsRequest);

      // Split the query.
      QueryPair query = DataSourceHelper.splitQuery(dsRequest.getQuery(), Capabilities.SELECT);

      // Generate the data table.
      DataTable data = generateMyDataTable(query.getDataSourceQuery(), req);

      // Apply the completion query to the data table.
      DataTable newData = DataSourceHelper.applyQuery(query.getCompletionQuery(), data,
          dsRequest.getUserLocale());

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

  /**
   * Returns true if the given column name is requested in the given query.
   * If the query is empty, all columnNames returns true.
   *
   * @param query The given query.
   * @param columnName The requested column name.
   *
   * @return True if the given column name is requested in the given query.
   */
  private boolean isColumnRequested(Query query, String columnName) {
    // If the query is empty return true.
    if (query.isEmpty()) {
      return true;
    }

    List<AbstractColumn> columns = query.getSelection().getColumns();
    for (AbstractColumn column : columns) {
      if (column.getId().equalsIgnoreCase(columnName)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Generates a data table - according to the provided tableId url parameter.
   *
   * @param query The query to operate on the underlying data.
   * @param req The HttpServeltRequest.
   *
   * @return The generated data table.
   */
  private DataTable generateMyDataTable(Query query, HttpServletRequest req)
      throws TypeMismatchException {
    String tableID = req.getParameter("tableId");
    if ((tableID != null) && (tableID.equalsIgnoreCase("planets"))) {
      return generatePlanetsTable(query);
    }
    return generateAnimalsTable(query);
  }

  /**
   * Returns an animals data table. The table contains a list of animals and
   * links to wikipedia pages.
   *
   * @param query The selection query.
   *
   * @return A data table of animals.
   */
  private DataTable generateAnimalsTable(Query query) throws TypeMismatchException {
    DataTable data = new DataTable();
    List<ColumnDescription> requiredColumns = getRequiredColumns(query,
        ANIMAL_TABLE_COLUMNS);
    data.addColumns(requiredColumns);

    // Populate the data table
    for (String key : animalLinksByName.keySet()) {
      TableRow row = new TableRow();
      for (ColumnDescription selectionColumn : requiredColumns) {
        String columnName = selectionColumn.getId();
        if (columnName.equals(ANIMAL_COLUMN)) {
          row.addCell(key);
        } else if (columnName.equals(ARTICLE_COLUMN)) {
          row.addCell(animalLinksByName.get(key));
        }
      }
      data.addRow(row);
    }
    return data;
  }

  /**
   * Returns the planets data table.
   *
   * @param query The selection query.
   *
   * @return planets data table.
   */
  private DataTable generatePlanetsTable(Query query) throws TypeMismatchException {
    DataTable data = new DataTable();
    List<ColumnDescription> requiredColumns = getRequiredColumns(
        query, planetTableColumns);
    data.addColumns(requiredColumns);

    // Populate data table
    for (Planet planet : Planet.values()) {
      TableRow row = new TableRow();
      for (ColumnDescription selectionColumn : requiredColumns) {
        String columnName = selectionColumn.getId();
        if (columnName.equals(PLANET_COLUMN)) {
          row.addCell(planet.name());
        } else if (columnName.equals(MASS_COLUMN)) {
          row.addCell(planet.getMass());
        } else if (columnName.equals(GRAVITY_COLUMN)) {
          row.addCell(planet.getSurfaceGravity());
        } else if (columnName.equals(MOONS_COLUMN)) {
          row.addCell(planet.getNumberOfMoons());
        }
      }
      data.addRow(row);
    }
    return data;
  }

  /**
   * Returns a list of required columns based on the query and the actual
   * columns.
   *
   * @param query The user selection query.
   * @param availableColumns The list of possible columns.
   *
   * @return A List of required columns for the requested data table.
   */
  private List<ColumnDescription> getRequiredColumns(Query query,
      ColumnDescription[] availableColumns) {
    // Required columns
    List<ColumnDescription> requiredColumns = Lists.newArrayList();
    for (ColumnDescription column : availableColumns) {
      if (isColumnRequested(query, column.getId())) {
        requiredColumns.add(column);
      }
    }
    return requiredColumns;
  }
}
