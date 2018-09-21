/*
 * Copyright 2016 The Closure Compiler Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.javascript.jscomp.parsing.parser.util.format;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Basic tests for {@code SimpleFormat}
 *
 * @author zhoumotongxue008@gmail.com (Michael Zhou)
 */
@RunWith(JUnit4.class)
public final class SimpleFormatTest extends TestCase {

  @Test
  public void testCharacter() {
    assertEquals("c is a character", SimpleFormat.format("%c is a character", 'c'));
  }

  @Test
  public void testInteger() {
    assertEquals("1 + 41 = 42", SimpleFormat.format("%d + %d = %d", 1, 41, 42));
  }

  @Test
  public void testString() {
    assertEquals("This is foo", SimpleFormat.format("This is %s", "foo"));
  }

  @Test
  public void testArgumentIndex() {
    assertEquals("one 2 3.0", SimpleFormat.format("%1$s %2$d %3$f", "one", 2, 3.0));
  }

  @Test
  public void testPrecision() {
    assertEquals("003", SimpleFormat.format("%03d", 3));
    assertEquals("+3", SimpleFormat.format("%+d", 3));
    assertEquals("0.1", SimpleFormat.format("%.1f", 0.123456));
    assertEquals("0.12", SimpleFormat.format("%.2f", 0.123456));
    assertEquals("0.123", SimpleFormat.format("%.3f", 0.123456));
  }
}
