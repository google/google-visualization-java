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

package com.google.visualization.datasource.query;

import com.google.visualization.datasource.datatable.value.Value;

import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A filter that decides upon a row by comparing two values. The values can be
 * either column values or constant values, depending on the concrete class
 * extending this one.
 *
 * @author Yonatan B.Y.
 */
public abstract class ComparisonFilter extends QueryFilter {

  /**
   * The set of possible comparison operators for such filters.
   */
  public static enum Operator {
    EQ("=", true),
    NE("!=", true),
    LT("<", true),
    GT(">", true),
    LE("<=", true),
    GE(">=", true),
    CONTAINS("CONTAINS", false),
    STARTS_WITH("STARTS WITH", false),
    ENDS_WITH("ENDS WITH", false),
    // Note that the operator syntax is: val MATCHES regexp.
    MATCHES("MATCHES", false),
    LIKE("LIKE", false);

    /**
     * Initializes a new instance of this class, with the given boolean
     * saying whether or not this instance requires the types to be equal.
     *
     * @param requiresEqualTypes True if this instance requires the types
     *     to be equal, false otherwise.
     * @param queryStringForm The query string form of this operator.
     */
    Operator(String queryStringForm, boolean requiresEqualTypes) {
      this.queryStringForm = queryStringForm;
      this.requiresEqualTypes = requiresEqualTypes;
    }

    /**
     * Returns whether or not this instance requires the two types to be equal.
     *
     * @return Whether or not this instance requires the two types to be equal.
     */
    public boolean areEqualTypesRequired() {
      return requiresEqualTypes;
    }

    /**
     * Returns the query string form of this operator.
     *
     * @return The query string form of this operator.
     */
    public String toQueryString() {
      return queryStringForm;
    }

    /**
     * True if this instance requires the two types to be equal.
     */
    private boolean requiresEqualTypes;

    /**
     * The query string form of this operator, i.e., how this operator should
     * appear in a query string, to be parsed by the query parser.
     */
    private String queryStringForm;
  }

  /**
   * The comparison operator for this filter.
   */
  protected Operator operator;

  /**
   * Constructs a new ComparisonFilter with the given operator.
   *
   * @param operator The comparison operator for this filter.
   */
  protected ComparisonFilter(Operator operator) {
    this.operator = operator;
  }

  /**
   * Returns true if s1 is "like" s2, in the sql-sense, i.e., if s2 has any
   * %'s or _'s, they are treated as special characters corresponding to an
   * arbitrary sequence of characters in s1 or to an abitrary character in s1
   * respectively. All other characters in s2 need to match exactly to
   * characters in s1. You cannot escape these characters, so that you cannot
   * match an explicit '%' or '_'.
   * @param s1 The first string.
   * @param s2 The second string.
   * @return True if s1 is "like" s2, in the sql-sense.
   */
  private boolean isLike(String s1, String s2) {
    StringTokenizer tokenizer = new StringTokenizer(s2, "%_", true);
    StringBuilder regexp = new StringBuilder();
    while (tokenizer.hasMoreTokens()) {
      String s = tokenizer.nextToken();
      if (s.equals("%")) {
        regexp.append(".*");
      } else if (s.equals("_")) {
        regexp.append(".");
      } else {
        regexp.append(Pattern.quote(s));
      }
    }
    return s1.matches(regexp.toString());
  }

  /**
   * Matches the given two values against the operator. E.g., if the operator is
   * GT, returns true if v1 > v2. This implementation uses the
   * compareTo() method.
   *
   * @param v1 The first value.
   * @param v2 The second value.
   *
   * @return true if v1 op v2, false otherwise.
   */
  protected boolean isOperatorMatch(Value v1, Value v2) {
    if (operator.areEqualTypesRequired()) {
      if (!v1.getType().equals(v2.getType())) {
        return false;
      }
    }

    switch (operator) {
      case EQ:
        return (v1.compareTo(v2) == 0);
      case NE:
        return (v1.compareTo(v2) != 0);
      case LT:
        return (v1.compareTo(v2) < 0);
      case GT:
        return (v1.compareTo(v2) > 0);
      case LE:
        return (v1.compareTo(v2) <= 0);
      case GE:
        return (v1.compareTo(v2) >= 0);
      case CONTAINS:
        return v1.toString().contains(v2.toString());
      case STARTS_WITH:
        return v1.toString().startsWith(v2.toString());
      case ENDS_WITH:
        return v1.toString().endsWith(v2.toString());
      case MATCHES:
        try {
          return v1.toString().matches(v2.toString());
        } catch (PatternSyntaxException ex) {
          return false; // a match against an illegal expression is false
        }
      case LIKE:
        return isLike(v1.toString(), v2.toString());
    }
    return false; // should never get here
  }

  /**
   * Returns the operator associated with this CompoundFilter.
   *
   * @return The operator associated with this CompoundFilter.
   */
  public Operator getOperator() {
    return operator;
  }

  @Override
  public abstract int hashCode();

  @Override
  public abstract boolean equals(Object obj);
}
