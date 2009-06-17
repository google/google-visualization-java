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

package com.google.visualization.datasource.query.mocks;

import com.google.visualization.datasource.base.TypeMismatchException;
import com.google.visualization.datasource.datatable.ColumnDescription;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableCell;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.BooleanValue;
import com.google.visualization.datasource.datatable.value.DateTimeValue;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.TimeOfDayValue;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.datatable.value.ValueType;


import java.util.List;
import java.util.Random;


/**
 * A mock data source for unit tests.
 * This data source always caches it's tables. This helps see if at some point
 * we ruin the datasource's data table.
 *
 * @author Yonatan B.Y.
 */
public class MockDataSource {

  /**
   * Private constructor, to prevent initialization.
   */
  private MockDataSource() {}

  /**
   * A random number generator.
   */
  private static Random random = null;

  private static final Object[][][] columnData =
  {
    {
      {"name", ValueType.TEXT, "Pet Name"},
      {"weight", ValueType.NUMBER, "Pet Weight"},
      {"isAlive", ValueType.BOOLEAN, "Is Still Alive"},
      {"height", ValueType.NUMBER, "Pet height"}
    }, {
      {"Year", ValueType.TEXT, "Just year"},
      {"Band", ValueType.TEXT, "Name of Band"},
      {"Songs", ValueType.NUMBER, "Number of new songs"},
      {"Sales", ValueType.NUMBER, "Number of new sales"},
      {"Fans", ValueType.NUMBER, "Number of fans worldwide"}
    }, {
      {"Year", ValueType.TEXT, "Just year"},
      {"Band", ValueType.TEXT, "Name of Band"},
      {"Songs", ValueType.NUMBER, "Number of new songs"},
      {"Sales", ValueType.NUMBER, "Number of new sales"},
      {"Fans", ValueType.NUMBER, "Number of fans worldwide"}
    }, {
      {"name", ValueType.TEXT, "Name of employee"},
      {"dept", ValueType.TEXT, "Department"},
      {"lunchTime", ValueType.TIMEOFDAY, "Lunch hour"},
      {"salary", ValueType.NUMBER, "Salary"},
      {"hireDate", ValueType.DATE, "Date commencing employment"},
      {"age", ValueType.NUMBER, "Age"},
      {"isSenior", ValueType.BOOLEAN, "Is the employee senior?"},
      {"seniorityStartTime", ValueType.DATETIME, "Seniority start time"}
    }
  };

  private static final String[][][] ROW_DATA =
  {
    {
      {"aaa", "222", "true", "222"},
      {"ccc", "111", "true", "222"},
      {"bbb", "333", "false", "222"},
      {"ddd", "222", "false", "1234"},
      {"eee", "1234", "true", "1234"},
      {"eee", "1234", "false", "1234"},
      {"bbb", "222", "false", "1234"}
    }, {
      {"1994", "Contraband", "2", "4", "3000"},
      {"1994", "Contraband", "2", "4", "300"},
      {"1994", "Contraband", "4", "4", "3300"},
      {"1994", "Contraband", "4", "4", "500"},
      {"1994", "Contraband", "2", "4", "3600"},
      {"1994", "Contraband", "2", "4", "3400"},
      {"1994", "Youthanasia", "2", "4", "3200"},
      {"1994", "Youthanasia", "2", "4", "10000"},
      {"1994", "Youthanasia", "2", "4", "400"},
      {"1994", "Youthanasia", "4", "4", "20"},
      {"1994", "Youthanasia", "2", "4", "340"},
      {"1994", "Youthanasia", "2", "4", "3060"},
      {"1994", "Youthanasia", "2", "4", "334"},
      {"1994", "Youthanasia", "2", "4", "360"},
      {"1994", "Youthanasia", "2", "4", "3800"},
      {"1996", "Contraband", "2", "4", "3099"},
      {"1996", "Contraband", "2", "4", "390"},
      {"1996", "Contraband", "2", "4", "36700"},
      {"1996", "Contraband", "2", "4", "3230"},
      {"1996", "Contraband", "2", "4", "304"},
      {"1996", "Contraband", "2", "4", "30540"},
      {"1996", "Contraband", "2", "46", "120"},
      {"1996", "Youthanasia", "2", "4", "40"},
      {"1996", "Youthanasia", "2", "4", "7"},
      {"1996", "Youthanasia", "2", "4", "3000"},
      {"1996", "Youthanasia", "4", "4", "3500"},
      {"1996", "Youthanasia", "4", "4", "3000"},
      {"1996", "Youthanasia", "2", "4", "320"},
      {"1996", "Youthanasia", "2", "4", "3000"},
      {"1996", "Youthanasia", "2", "4", "370"},
      {"2003", "Collection", "2", "5", "3000"},
      {"2003", "Collection", "2", "4", "370"},
      {"2003", "Collection", "2", "4", "3000"},
      {"2003", "Collection", "2", "8", "33"},
      {"2003", "Collection", "2", "4", "3000"},
      {"2003", "Collection", "2", "4", "800"},
      {"2003", "Collection", "2", "4", "3000"},
      {"2003", "Collection", "2", "4", "660"},
      {"2003", "Collection", "2", "4", "3000"},
      {"2003", "Collection", "2", "4", "456"},
      {"2003", "Collection", "2", "4", "3000"},
      {"2003", "Collection", "2", "4", "3987"},
      {"2003", "Collection", "2", "4", "3000"},
      {"2003", "Collection", "2", "6", "3440"},
      {"2003", "Collection", "2", "4", "3000"}
    }, {
      {"1994", "Contraband", "2", "4", "100"},
      {"1994", "Contraband", null, "0", null},
      {"2003", "Collection", "2", null, null},
    }, {
      {"John", "Eng", "12:00:00", "1000", "2004-09-08", "35", "true", "2007-12-02 15:56:00"},
      {"Dave", "Eng", "12:00:00", "500", "2005-10-10", "27", "false", null},
      {"Sally", "Eng", "13:00:00", "600", "2005-10-10", "30", "false", null},
      {"Ben", "Sales", "12:00:00", "400", "2002-10-10", "32", "true", "2005-03-19 12:30:00"},
      {"Dana", "Sales", "12:00:00", "350", "2004-09-08", "25", "false", null},
      {"Mike", "Marketing", "13:00:00", "800", "2005-01-10", "24", "true", "2007-12-30 14:40:00"}
    }
  };

  private static DataTable[] dataTables;

  /**
   * Static constructor to initialize the TableDescriptions and DataTables
   */
  static {
    if (columnData.length != ROW_DATA.length) {
      throw new RuntimeException("Cannot happen. Fix tests.");
    }
    dataTables = new DataTable[ROW_DATA.length];
    for (int i = 0; i < columnData.length; i++) {
      dataTables[i] = new DataTable();
      for (int j = 0; j < columnData[i].length; j++) {
        if (columnData[i][j].length != 3) {
          throw new RuntimeException("Cannot happen. Fix tests.");
        }
        String id = (String) columnData[i][j][0];
        ValueType type = (ValueType) columnData[i][j][1];
        String label = (String) columnData[i][j][2];
        ColumnDescription colDesc = new ColumnDescription(id, type, label);
        dataTables[i].addColumn(colDesc);
      }
    }

    for (int i = 0; i < ROW_DATA.length; i++) {
      buildTable(dataTables[i], ROW_DATA[i]);
    }
  }

  public static DataTable getData(int tableNum) {
    return dataTables[tableNum].clone();
  }

  public static DataTable getRandomDataTable(int tableNum, int numRows,
      long randomSeed) {
    random = new Random(randomSeed);
    DataTable res = new DataTable();
    res.addColumns(dataTables[tableNum].getColumnDescriptions());
    res = res.clone(); // Clones the column descriptions.
    try {
      for (int i = 0; i < numRows; i++) {
        TableRow row = new TableRow();
        for (ColumnDescription colDesc : res.getColumnDescriptions()) {
          Value value = toRandomValue(colDesc.getType());
          row.addCell(new TableCell(value));
        }
        res.addRow(row);
      }
    } catch (TypeMismatchException e) {
        // Should not happen, as we control the types.
    }
    return res;
  }

  /**
   * Converts a string to a value.
   *
   * @param content The content.
   * @param type The column Type.
   *
   * @return A value.
   */
  public static Value toValue (String content, ValueType type){
    if (content == null) {
      return Value.getNullValueFromValueType(type);
    }
    Value value;
    switch (type) {
      case NUMBER:
        return new NumberValue(Double.parseDouble(content));
      case BOOLEAN:
        return BooleanValue.getInstance(Boolean.parseBoolean(content));
      case TEXT:
        return new TextValue(content);
      case DATETIME:
        String[] mainSplit = content.split(" ");
        String[] dateSplit = mainSplit[0].split("-");
        String[] timeSplit = mainSplit[1].split(":");
        int year = Integer.parseInt(dateSplit[0]);
        int month = Integer.parseInt(dateSplit[1]);
        month--; // normalize 1-12 to 0-11.
        int day = Integer.parseInt(dateSplit[2]);
        int hour = Integer.parseInt(timeSplit[0]);
        int minute = Integer.parseInt(timeSplit[1]);
        int second;
        int milli = 0;
        if (timeSplit[2].contains(".")) {
          String[] secondMilliSplit = timeSplit[2].split("\\.");
          second = Integer.parseInt(secondMilliSplit[0]);
          milli = Integer.parseInt(secondMilliSplit[1]);
        } else {
          second = Integer.parseInt(timeSplit[2]);
        }
        return new DateTimeValue(year, month, day, hour, minute, second, milli);
      case DATE:
        String[] split = content.split("-");
        year = Integer.parseInt(split[0]);
        month = Integer.parseInt(split[1]);
        month--; // normalize 1-12 to 0-11.
        day = Integer.parseInt(split[2]);
        return new DateValue(year, month, day);
      case TIMEOFDAY:
        split = content.split(":");
        hour = Integer.parseInt(split[0]);
        minute = Integer.parseInt(split[1]);
        if (split[2].contains(".")) {
          String[] secondMilliSplit = split[2].split("\\.");
          second = Integer.parseInt(secondMilliSplit[0]);
          milli = Integer.parseInt(secondMilliSplit[1]);
          return new TimeOfDayValue(hour, minute, second, milli);
        } else {
          second = Integer.parseInt(split[2]);
          return new TimeOfDayValue(hour, minute, second);
        }

      default:
        throw new RuntimeException("We do not support other types "
            + "for now");
    }
  }

  public static Value toRandomValue(ValueType type) {
    if (random.nextInt(10) == 0) {
      return Value.getNullValueFromValueType(type);
    }
    switch (type) {
      case NUMBER:
        return new NumberValue(random.nextDouble() * 10000 *
            (random.nextInt(2) * 2 - 1));
      case BOOLEAN:
        return BooleanValue.getInstance(random.nextBoolean());
      case TEXT:
        StringBuilder builder = new StringBuilder();
        int numLetters = random.nextInt(100) + 1;
        for (int i = 0; i < numLetters; i++) {
          builder.append('a' + random.nextInt(26));
        }
        return new TextValue(builder.toString());
      case DATETIME:
        return new DateTimeValue(random.nextInt(200) + 1900, random.nextInt(12),
            random.nextInt(28) + 1, random.nextInt(24), random.nextInt(60),
            random.nextInt(60), random.nextInt(999));
      case DATE:
        return new DateValue(random.nextInt(200) + 1900, random.nextInt(12),
            random.nextInt(28) + 1);
      case TIMEOFDAY:
        return new TimeOfDayValue(random.nextInt(24), random.nextInt(60),
            random.nextInt(60), random.nextInt(999));
      default:
        throw new RuntimeException("Invalid type");
    }
  }

  /**
   * Adds all content to the query result.
   *
   * @param res The query result.
   * @param content A double string array for the content of the table.
   */
  private static void buildTable (DataTable res, String[][] content) {
    List<ColumnDescription> descriptions = res.getColumnDescriptions();
    try {
      for (String[] curRow : content) {
        res.addRow(createNewTableRow(curRow, descriptions));
      }
    } catch (TypeMismatchException e) {
      // Shouldn't happen, as we control the types.
    }
  }

  /**
   * Constructs a table row according to string input.
   *
   * @param content A string array containing the content.
   * @param descriptors The column descriptors.
   *
   * @return A table row.
   */
  public static TableRow createNewTableRow(String[] content,
      List<ColumnDescription> descriptors) {
    TableRow result = new TableRow();
    for (int i = 0; i < content.length; i++) {
      Value value = toValue(content[i], descriptors.get(i).getType());
      result.addCell(new TableCell(value));
    }
    return result;
  }

  public static String[][] queryResultToStringMatrix(DataTable table) {
    int numRows = table.getNumberOfRows();
    int numCols = table.getNumberOfColumns();
    String[][] result = new String[numRows][];
    for (int i = 0; i < numRows; i++) {
      result[i] = new String[numCols];
      for (int j = 0; j < numCols; j++) {
        result[i][j] = table.getRows().get(i).getCells().get(j).toString();
      }
    }
    return result;
  }
}
