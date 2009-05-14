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

package com.google.visualization.datasource.query.engine;

import com.google.common.collect.Sets;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.value.Value;

import java.util.Map;
import java.util.Set;

/**
 * An aggregation tree is the logical data structure that represents table grouping information.
 * Each level of the tree (besides the root-level) is associated with an aggregation column.
 * Each node of the tree is associated with a value (of some cell in the column defining its level).
 * Each node contains aggregation information, i.e., information about the minimum, maximum,
 * count, average and sum on the aggregated columns. A leaf node holds this aggregation information
 * for the corresponding "path" of values. For example, if the group-by columns are {Name, Revenue},
 * then the tree consists of 3 levels: the root level, holding only the root, then a level with
 * nodes representing different values of Name, and then a level with nodes holding different
 * values of Revenue. Then the leaf node which is at the path {"Joe", 100} from the root will hold
 * the aggregation information for all rows in the table for which the value of Name is "Joe" and
 * the value of Revenue is 100. The non-leaf node which is at the path {"Joe"} will contain the
 * aggregation information for all rows in which the name is "Joe", without any consideration
 * of the value of the Revenue column. The root node contains all aggregation information
 * for the entire table. 
 *
 * @author Yoav G.
 */
public class AggregationTree {

  /**
   * The root of the tree is associated with the empty path and aggregates
   * all data rows.
   */
  private AggregationNode root;

  /**
   * A set of ids of the columns to aggregate. This set is shared by all the
   * nodes in this tree.
   */
  private Set<String> columnsToAggregate;

  /**
   * The table. Used only for columns information.
   */
  private DataTable table;

  /**
   * Construct an aggregation tree.
   *
   * @param columnsToAggregate A set of ids of the columns to aggregate.
   * @param table The table.
   */
  public AggregationTree(Set<String> columnsToAggregate, DataTable table) {
    this.columnsToAggregate = columnsToAggregate;
    this.table = table;
    root = new AggregationNode(columnsToAggregate, table);
  }

  /**
   * Aggregates values to all the nodes on a path. The nodes of the path that
   * are not in the tree are created and inserted into the tree.
   *
   * @param valuesToAggregate Maps column ids to values (to aggregate).
   * @param path The aggregation path.
   */
  public void aggregate(AggregationPath path, Map<String, Value> valuesToAggregate) {
    AggregationNode curNode = root;
    root.aggregate(valuesToAggregate);

    for (Value curValue : path.getValues()) {
      // Add a new child if needed.
      if (!curNode.containsChild(curValue)) {
        curNode.addChild(curValue, columnsToAggregate, table);
      }
      curNode = curNode.getChild(curValue);
      curNode.aggregate(valuesToAggregate);
    }
  }

  /**
   * Returns the aggregation node at the end of a path.
   *
   * @param path The aggregation path.
   *
   * @return The aggregation node at the end of a path.
   *
   * @throws java.util.NoSuchElementException In case no node lies at the end of the path.
   */
  public AggregationNode getNode(AggregationPath path) {
    AggregationNode curNode = root;
    for (Value curValue : path.getValues()) {
      curNode = curNode.getChild(curValue);
    }
    return curNode;
  }

  /**
   * Returns a set containing a path for each leaf in the tree.
   *
   * @return A set containing a path for each leaf in the tree.
   */
  public Set<AggregationPath> getPathsToLeaves() {
    Set<AggregationPath> result = Sets.newHashSet();
    getPathsToLeavesInternal(root, result);
    return result;
  }

  /**
   * Fills a set with the paths to all leaves in the tree.
   */
  private void getPathsToLeavesInternal(AggregationNode node, Set<AggregationPath> result) {
    // Perform DFS.
    Map<Value, AggregationNode> children = node.getChildren();
    if (children.isEmpty()) {
      // Reached a leaf.
      result.add(getPathToNode(node));
    } else {
      for (AggregationNode curNode : children.values()) {
        getPathsToLeavesInternal(curNode, result);
      }
    }
  }

  /**
   * Returns the path in the aggregation tree from the root to an aggregation node.
   *
   * @param node The aggregation node.
   *
   * @return The path in the aggregation tree an aggregation node.
   */
  private static final AggregationPath getPathToNode(AggregationNode node) {
    AggregationPath result = new AggregationPath();
    AggregationNode curNode = node;
    while (curNode.getValue() != null) { // Did not reach the root.
      result.add(curNode.getValue());
      curNode = curNode.getParent();
    }
    result.reverse();
    return result;
  }
}
