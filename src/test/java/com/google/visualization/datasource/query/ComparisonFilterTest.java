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

import com.google.visualization.datasource.datatable.DataTable;
import com.google.visualization.datasource.datatable.TableRow;
import com.google.visualization.datasource.datatable.value.BooleanValue;
import com.google.visualization.datasource.datatable.value.DateValue;
import com.google.visualization.datasource.datatable.value.NumberValue;
import com.google.visualization.datasource.datatable.value.TextValue;
import com.google.visualization.datasource.datatable.value.Value;

import junit.framework.TestCase;

import java.util.List;
import java.util.Set;


/**
 * Test for ComparisonFilter
 *
 * @author Yonatan B.Y.
 */
public class ComparisonFilterTest extends TestCase {

  /**
   * This is a concrete class deriving from ComparisonFilter.
   * It is here for two reasons:
   * 1. So we can test the abstract class ComparisonFilter without explicitly
   *    using one of its known subclasses.
   */
  public static class ConcreteComparisonFilter extends ComparisonFilter {

    public ConcreteComparisonFilter(Operator op) {
      super(op);
    }

    @Override
    public boolean isMatch(DataTable table, TableRow row) {
      return false;
    }

    @Override
    public Set<String> getAllColumnIds() {
      return null;
    }

    @Override
    public List<ScalarFunctionColumn> getScalarFunctionColumns() {
      return null;
    }

    @Override
    public List<AggregationColumn> getAggregationColumns() {
      return null;
    }

    protected String getInnerCanonicalString() {
      return null;
    }

    public String getToken() {
      return null;
    }

    /**
     * This is here to test the protected method isOperatorMatch.
     * @param v1 The first value.
     * @param v2 The second value.
     * @return Whether or not v1 operator v2.
     */
    @Override
    public boolean isOperatorMatch(Value v1, Value v2) {
      return super.isOperatorMatch(v1, v2);
    }

    @Override
    public String toQueryString() {
      return null;
    }

    @Override
    public boolean equals(Object obj) {
      throw new UnsupportedOperationException(
          "Equals is not implemented in ComparisonFilterTest");
    }

    @Override
    public int hashCode() {
      throw new UnsupportedOperationException(
          "hashCode is not implemented in ComparisonFilterTest");
    }
  }

  /**
   * Tests the regular equality and order operators.
   */
  public void testCheck() {
    ConcreteComparisonFilter equalsFilter = new ConcreteComparisonFilter(
        ComparisonFilter.Operator.EQ);
    ConcreteComparisonFilter notEqualsFilter = new ConcreteComparisonFilter(
        ComparisonFilter.Operator.NE);
    ConcreteComparisonFilter lessThanFilter = new ConcreteComparisonFilter(
        ComparisonFilter.Operator.LT);
    ConcreteComparisonFilter greaterThanFilter = new ConcreteComparisonFilter(
        ComparisonFilter.Operator.GT);
    ConcreteComparisonFilter lessOrEqualsFilter = new ConcreteComparisonFilter(
        ComparisonFilter.Operator.LE);
    ConcreteComparisonFilter greaterOrEqualsFilter =
        new ConcreteComparisonFilter(ComparisonFilter.Operator.GE);

    assertTrue(equalsFilter.isOperatorMatch(new NumberValue(123),
        new NumberValue(123)));
    assertTrue(equalsFilter.isOperatorMatch(new TextValue("abc"),
        new TextValue("abc")));
    assertTrue(equalsFilter.isOperatorMatch(BooleanValue.TRUE,
        BooleanValue.TRUE));
    assertFalse(equalsFilter.isOperatorMatch(new NumberValue(123),
        new NumberValue(345)));
    assertFalse(equalsFilter.isOperatorMatch(new TextValue("ab"),
        new TextValue("abc")));

    assertTrue(lessThanFilter.isOperatorMatch(new NumberValue(123.123),
        new NumberValue(123.124)));
    assertTrue(lessThanFilter.isOperatorMatch(new TextValue("abc"),
        new TextValue("abd")));
    assertTrue(lessThanFilter.isOperatorMatch(BooleanValue.FALSE,
        BooleanValue.TRUE));
    assertFalse(lessThanFilter.isOperatorMatch(new NumberValue(7),
        new NumberValue(-14)));
    assertFalse(lessThanFilter.isOperatorMatch(new TextValue("bbb"),
        new TextValue("aaa")));
    assertFalse(lessThanFilter.isOperatorMatch(BooleanValue.TRUE,
        BooleanValue.FALSE));

    assertTrue(lessOrEqualsFilter.isOperatorMatch(new NumberValue(123),
        new NumberValue(123)));
    assertTrue(lessOrEqualsFilter.isOperatorMatch(new TextValue("abc"),
        new TextValue("abc")));
    assertTrue(lessOrEqualsFilter.isOperatorMatch(BooleanValue.TRUE,
        BooleanValue.TRUE));
    assertTrue(lessOrEqualsFilter.isOperatorMatch(new NumberValue(123.123),
        new NumberValue(123.124)));
    assertTrue(lessOrEqualsFilter.isOperatorMatch(new TextValue("abc"),
        new TextValue("abd")));
    assertTrue(lessOrEqualsFilter.isOperatorMatch(BooleanValue.FALSE,
        BooleanValue.TRUE));
    assertFalse(lessOrEqualsFilter.isOperatorMatch(new NumberValue(123),
        new NumberValue(122.2)));
    assertFalse(lessOrEqualsFilter.isOperatorMatch(new TextValue("ab"),
        new TextValue("aa")));
    assertFalse(lessOrEqualsFilter.isOperatorMatch(new NumberValue(7),
        new NumberValue(-14)));
    assertFalse(lessOrEqualsFilter.isOperatorMatch(new TextValue("bbb"),
        new TextValue("aaa")));
    assertFalse(lessOrEqualsFilter.isOperatorMatch(BooleanValue.TRUE,
        BooleanValue.FALSE));

    assertFalse(notEqualsFilter.isOperatorMatch(new NumberValue(123),
        new NumberValue(123)));
    assertFalse(notEqualsFilter.isOperatorMatch(new TextValue("abc"),
        new TextValue("abc")));
    assertFalse(notEqualsFilter.isOperatorMatch(BooleanValue.TRUE,
        BooleanValue.TRUE));
    assertTrue(notEqualsFilter.isOperatorMatch(new NumberValue(123),
        new NumberValue(345)));
    assertTrue(notEqualsFilter.isOperatorMatch(new TextValue("ab"),
        new TextValue("abc")));

    assertFalse(greaterOrEqualsFilter.isOperatorMatch(new NumberValue(123.123),
        new NumberValue(123.124)));
    assertFalse(greaterOrEqualsFilter.isOperatorMatch(new TextValue("abc"),
        new TextValue("abd")));
    assertFalse(greaterOrEqualsFilter.isOperatorMatch(BooleanValue.FALSE,
        BooleanValue.TRUE));
    assertTrue(greaterOrEqualsFilter.isOperatorMatch(new NumberValue(7),
        new NumberValue(-14)));
    assertTrue(greaterOrEqualsFilter.isOperatorMatch(new TextValue("bbb"),
        new TextValue("aaa")));
    assertTrue(greaterOrEqualsFilter.isOperatorMatch(BooleanValue.TRUE,
        BooleanValue.FALSE));

    assertFalse(greaterThanFilter.isOperatorMatch(new NumberValue(123),
        new NumberValue(123)));
    assertFalse(greaterThanFilter.isOperatorMatch(new TextValue("abc"),
        new TextValue("abc")));
    assertFalse(greaterThanFilter.isOperatorMatch(BooleanValue.TRUE,
        BooleanValue.TRUE));
    assertFalse(greaterThanFilter.isOperatorMatch(new NumberValue(123.123),
        new NumberValue(123.124)));
    assertFalse(greaterThanFilter.isOperatorMatch(new TextValue("abc"),
        new TextValue("abd")));
    assertFalse(greaterThanFilter.isOperatorMatch(BooleanValue.FALSE,
        BooleanValue.TRUE));
    assertTrue(greaterThanFilter.isOperatorMatch(new NumberValue(123),
        new NumberValue(122.2)));
    assertTrue(greaterThanFilter.isOperatorMatch(new TextValue("ab"),
        new TextValue("aa")));
    assertTrue(greaterThanFilter.isOperatorMatch(new NumberValue(7),
        new NumberValue(-14)));
    assertTrue(greaterThanFilter.isOperatorMatch(new TextValue("bbb"),
        new TextValue("aaa")));
    assertTrue(greaterThanFilter.isOperatorMatch(BooleanValue.TRUE,
        BooleanValue.FALSE));

    // Test type mismatch:
    assertFalse(greaterThanFilter.isOperatorMatch(new TextValue("blah"),
        new NumberValue(123)));
    assertFalse(equalsFilter.isOperatorMatch(new DateValue(2000, 3, 3),
        BooleanValue.TRUE));
  }

  /**
   * Tests the string-related operators.
   */
  public void testStringOperators() {
    ConcreteComparisonFilter containsFilter = new ConcreteComparisonFilter(
        ComparisonFilter.Operator.CONTAINS);
    ConcreteComparisonFilter startsWithFilter = new ConcreteComparisonFilter(
        ComparisonFilter.Operator.STARTS_WITH);
    ConcreteComparisonFilter endsWithFilter = new ConcreteComparisonFilter(
        ComparisonFilter.Operator.ENDS_WITH);
    ConcreteComparisonFilter matchesFilter = new ConcreteComparisonFilter(
        ComparisonFilter.Operator.MATCHES);
    ConcreteComparisonFilter likeFilter = new ConcreteComparisonFilter(
        ComparisonFilter.Operator.LIKE);

    assertTrue(containsFilter.isOperatorMatch(new TextValue("blah blah"),
        new TextValue("ah bl")));
    assertTrue(containsFilter.isOperatorMatch(new TextValue("foo bar"),
        new TextValue("foo ")));
    assertTrue(containsFilter.isOperatorMatch(new TextValue("foo bar"),
        new TextValue(" bar")));
    assertFalse(containsFilter.isOperatorMatch(new TextValue("abc123"),
        new TextValue("bd")));

    assertTrue(startsWithFilter.isOperatorMatch(new TextValue("this is a " +
        "text value"), new TextValue("this is")));
    assertFalse(startsWithFilter.isOperatorMatch(new TextValue("foo bar"),
        new TextValue("oo bar")));

    assertTrue(endsWithFilter.isOperatorMatch(new TextValue("this is a " +
        "text value"), new TextValue("text value")));
    assertFalse(endsWithFilter.isOperatorMatch(new TextValue("foo bar"),
        new TextValue("foo ba")));

    assertTrue(matchesFilter.isOperatorMatch(new TextValue("aaaaaa123"),
        new TextValue("a*123")));
    assertTrue(matchesFilter.isOperatorMatch(new TextValue("abcdefghijk"),
        new TextValue("[a-k]+")));
    assertFalse(matchesFilter.isOperatorMatch(new TextValue("abc1234"),
        new TextValue("a*123")));
    assertFalse(matchesFilter.isOperatorMatch(new TextValue("blah blah"),
        new TextValue("%^@%!%$#^*&*$!@#$((((")));
    // Test partial
    assertFalse(matchesFilter.isOperatorMatch(new TextValue("aaaaaaa123"),
        new TextValue("a*")));

    assertTrue(likeFilter.isOperatorMatch(new TextValue("aaaabbbccc"),
        new TextValue("a%b_b%c")));
    assertFalse(likeFilter.isOperatorMatch(new TextValue("aaaabbbccc"),
        new TextValue("aaqqb_c%c")));
    assertTrue(likeFilter.isOperatorMatch(new TextValue("aaaabbbccc"),
        new TextValue("a%%b_b%c")));
    assertTrue(likeFilter.isOperatorMatch(new TextValue("aaaabbbccc"),
        new TextValue("%")));
    assertTrue(likeFilter.isOperatorMatch(new TextValue("foo bar"),
        new TextValue("foo_bar")));
    assertTrue(likeFilter.isOperatorMatch(new TextValue("foo%bar"),
        new TextValue("foo_bar")));
    assertFalse(likeFilter.isOperatorMatch(new TextValue("blah blah"),
        new TextValue("bla%b%aa")));
    assertTrue(likeFilter.isOperatorMatch(new TextValue("foobar"),
        new TextValue("foo__r")));
    assertFalse(likeFilter.isOperatorMatch(new TextValue("foobr"),
        new TextValue("foo__r")));
    assertFalse(likeFilter.isOperatorMatch(new TextValue("foobaar"),
        new TextValue("foo__r")));
    assertTrue(likeFilter.isOperatorMatch(new TextValue("foobaar"),
        new TextValue("foo__%_r")));
    assertTrue(likeFilter.isOperatorMatch(new TextValue("foobaaar"),
        new TextValue("foo__%_r")));
    assertTrue(likeFilter.isOperatorMatch(new TextValue("foobaaaar"),
        new TextValue("foo__%_r")));
    assertFalse(likeFilter.isOperatorMatch(new TextValue("foobar"),
        new TextValue("foo__%_r")));
    assertFalse(likeFilter.isOperatorMatch(new TextValue("foobr"),
        new TextValue("foo__%_r")));

    // Partial match:
    assertFalse(likeFilter.isOperatorMatch(new TextValue("blah blah"),
        new TextValue("bla%b%a")));

    // Test non-strings:
    assertTrue(containsFilter.isOperatorMatch(new NumberValue(123),
        new NumberValue(3)));
    assertTrue(containsFilter.isOperatorMatch(new NumberValue(123.45),
        new TextValue("23.4")));
    assertFalse(containsFilter.isOperatorMatch(new NumberValue(23423),
        BooleanValue.TRUE));

    assertTrue(startsWithFilter.isOperatorMatch(BooleanValue.FALSE,
        new TextValue("fa")));

    assertTrue(endsWithFilter.isOperatorMatch(new NumberValue(123.456),
        new TextValue("456")));

    assertTrue(matchesFilter.isOperatorMatch(new NumberValue(123),
        new TextValue(".*")));

    assertTrue(likeFilter.isOperatorMatch(new NumberValue(123),
        new TextValue("_2%")));
  }

}
