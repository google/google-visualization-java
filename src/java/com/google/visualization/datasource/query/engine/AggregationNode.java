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

import com.google.common.collect.Maps;
import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.value.Value;
import com.google.visualization.datasource.query.AggregationType;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An aggregation node is a node in an aggregation tree. This node holds a value, equal to the
 * value in the corresponding group-by column. It also holds: value aggregators (one for each
 * aggregation column), a reference to its parent in the tree, and a set of references to its
 * children. Each child is associated with a unique value, which corresponds to the value held
 * in the child node. See {@link AggregationTree} for more details.
 *
 * @author Yoav G.
 */
public class AggregationNode {

  /**
   * The parent of this node in the aggregation tree.
   */
   private AggregationNode parent;

  /**
   * The value of this node. This value is unique among the siblings of this
   * node (in the aggregation tree), and is used for navigation. Note that the value is the same
   * as the value used as a key to point to this AggregationNode in the parent's {@link #children}
   * map. 
   */
   private Value value;

  /**
   * Maps a column id to its aggregator. The column id should belong to the list of aggregation
   * columns.
   */
  private Map<String, ValueAggregator> columnAggregators = Maps.newHashMap();

  /**
   * Maps a value to a child of this node (which is also an aggregation node). ‎The value is the
   * same as the {@link #value} that will be stored in the child, i.e.,
   * <code>children.get(X).getValue()</code> should equal <code>X</code>.
   */
  private Map<Value, AggregationNode> children = Maps.newHashMap();

  /**
   * Construct a new aggregation node.
   *
   * @param columnsToAggregate A set of ids of the columns to aggregate (aggregation columns).
   * @param table The table.
   */
  public AggregationNode(Set<String> columnsToAggregate, DataTable table) {
    // Add a column value aggregator for each aggregation column.
    for (String columnId : columnsToAggregate) {
      columnAggregators.put(columnId, new ValueAggregator(
          table.getColumnDescription(columnId).getType()));
    }
  }

  /**
   * Aggregates values using the value aggregators of this node.
   *
   * @param valuesByColumn Maps a column id to the value that needs be aggregated (for that column).
   */
  public void aggregate(Map<String, Value> valuesByColumn) {
    for (String columnId : valuesByColumn.keySet()) {
      columnAggregators.get(columnId).aggregate(valuesByColumn.get(columnId));
    }
  }

  /**
   * Returns the aggregation value of a specific column and type.
   *
   * @param columnId The requested column id.
   * @param type The requested aggregation type.
   *
   * @return The aggregation values of a specific column.
   */
  public Value getAggregationValue(String columnId, AggregationType type) {
    ValueAggregator valuesAggregator = columnAggregators.get(columnId);
    if (valuesAggregator == null) {
      throw new IllegalArgumentException("Column " + columnId +
          " is not aggregated");
    }
    return valuesAggregator.getValue(type);
  }

  /**
   * Returns the child of this node defined by a specific value.
   *
   * @param v The value.
   *
   * @return The child of this node defined by a specific value.
   */
  public AggregationNode getChild(Value v) {
    AggregationNode result = children.get(v);
    if (result == null) {
      throw new NoSuchElementException("Value " + v + " is not a child.");
    }
    return result;
  }

  /**
   * Returns true if a node contains a child (identified by value) and false
   * otherwise.
   *
   * @param v The value of the child.
   *
   * @return True if this node contains a child (identified by value) and false
   * otherwise.
   */
  public boolean containsChild(Value v) {
    return children.containsKey(v);
  }

  /**
   * Adds a new child.
   *
   * @param key The value defining the new child.
   * @param columnsToAggregate The ids of the columns to aggregate.
   * @param table The table.
   */
  public void addChild(Value key, Set<String> columnsToAggregate, DataTable table) {

    if (children.containsKey(key)) {
      throw new IllegalArgumentException("A child with key: " + key +
          " already exists.");
    }
    AggregationNode node = new AggregationNode(columnsToAggregate, table);
    node.parent = this;
    node.value = key;
    children.put(key, node);
  }

  /**
   * Returns a copy of the map of children of this node.
   *
   * @return A copy of the map of children of this node.
   */
  public Map<Value, AggregationNode> getChildren() {
    return Maps.newHashMap(children);
  }

  /**
   * Returns the value of this node. This is also the key of this node in the
   * children set of this parent.
   *
   * @return The value of this node.
   */
  protected Value getValue() {
    return value;
  }

  /**
   * Returns the parent of this node in the aggregation tree.
   *
   * @return The parent of this node.
   */
  protected AggregationNode getParent() {
    return parent;
  }
}
