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
import com.google.visualization.datasource.DataSourceServlet;
import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.ValueType;
import com.google.visualization.datasource.query.AbstractColumn;
import com.google.visualization.datasource.query.Query;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;


/**
 * An example servlet for serving a data table from a data source that has query capabilities.
 * This data source has native 'select' capability.
 * Other parts of the query are handled by the completion query.
 *
 * The data source can return two different data tables, depending on the
 * tableId parameter. By default it returns the 'animals' data table, unless the
 * tableId parameter is set to 'planets'.
 */
public class AdvancedExampleServlet extends DataSourceServlet {

  /**
   * A map of animal names to links in wikipedia describing them.
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

  /**
   * Specifies that this data source can select and return specified columns instead of passing
   * all the columns and leaving the library's query engine to select the columns. 
   * If the number of columns is large, and a query specifies only a few of them, this can 
   * significantly improve performance.
   * 
   *
   * @return The "Select" capability in this case.
   */
  @Override
  public Capabilities getCapabilities() {
    return Capabilities.SELECT;
  }

  /**
   * NOTE: By default, this function returns true, which means that cross
   * domain requests are rejected.
   * This check is disabled here so that this example can be used directly from the
   * address bar of the browser. Bear in mind that this exposes your
   * data source to xsrf attacks.
   * If the only use of the data source url is from your application,
   * that runs on the same domain, it is better to remain in restricted mode.
   */
  @Override
  protected boolean isRestrictedAccessMode() {
    return false;
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

    // If the query is empty returns true, as if all columns were specified.
    if (query.isEmpty()) {
      return true;
    }

    // Returns true if the requested column id was specified (not case sensitive).
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
  @Override
  public DataTable generateDataTable(Query query, HttpServletRequest req)
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

    // Populate the planets data table
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
    List<ColumnDescription> requiredColumns = Lists.newArrayList();
    for (ColumnDescription column : availableColumns) {
      if (isColumnRequested(query, column.getId())) {
        requiredColumns.add(column);
      }
    }
    return requiredColumns;
  }
}
