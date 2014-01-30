/*
 * Copyright 2014 The Closure Compiler Authors.
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

package com.google.javascript.jscomp.parsing;

import com.google.common.collect.ImmutableList;
import com.google.javascript.jscomp.parsing.Config.LanguageMode;
import com.google.javascript.jscomp.testing.TestErrorReporter;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.head.ScriptRuntime;
import com.google.javascript.rhino.jstype.SimpleSourceFile;
import com.google.javascript.rhino.jstype.StaticSourceFile;
import com.google.javascript.rhino.testing.BaseJSTypeTestCase;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class NewParserTest extends BaseJSTypeTestCase {
  private static final String SUSPICIOUS_COMMENT_WARNING =
      IRFactory.SUSPICIOUS_COMMENT_WARNING;

  private static final String TRAILING_COMMA_MESSAGE =
      ScriptRuntime.getMessage0("msg.extra.trailing.comma");

  private static final String MISSING_GT_MESSAGE =
      "Bad type annotation. " +
      com.google.javascript.rhino.SimpleErrorReporter.getMessage0(
          "msg.jsdoc.missing.gt");

  private static final String MISPLACED_TYPE_ANNOTATION =
      IRFactory.MISPLACED_TYPE_ANNOTATION;

  private Config.LanguageMode mode;
  private boolean isIdeMode = false;

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    mode = LanguageMode.ECMASCRIPT3;
    isIdeMode = false;
  }

  public void testLinenoCharnoAssign1() throws Exception {
    Node assign = parse("a = b").getFirstChild().getFirstChild();

    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals(1, assign.getLineno());
    assertEquals(0, assign.getCharno());
  }

  public void testLinenoCharnoAssign2() throws Exception {
    Node assign = parse("\n a.g.h.k    =  45").getFirstChild().getFirstChild();

    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals(2, assign.getLineno());
    assertEquals(1, assign.getCharno());
  }

  public void testLinenoCharnoCall() throws Exception {
    Node call = parse("\n foo(123);").getFirstChild().getFirstChild();

    assertEquals(Token.CALL, call.getType());
    assertEquals(2, call.getLineno());
    assertEquals(1, call.getCharno());
  }

  public void testLinenoCharnoGetProp1() throws Exception {
    Node getprop = parse("\n foo.bar").getFirstChild().getFirstChild();

    assertEquals(Token.GETPROP, getprop.getType());
    assertEquals(2, getprop.getLineno());
    assertEquals(1, getprop.getCharno());

    Node name = getprop.getFirstChild().getNext();
    assertEquals(Token.STRING, name.getType());
    assertEquals(2, name.getLineno());
    assertEquals(5, name.getCharno());
  }

  public void testLinenoCharnoGetProp2() throws Exception {
    Node getprop = parse("\n foo.\nbar").getFirstChild().getFirstChild();

    assertEquals(Token.GETPROP, getprop.getType());
    assertEquals(2, getprop.getLineno());
    assertEquals(1, getprop.getCharno());

    Node name = getprop.getFirstChild().getNext();
    assertEquals(Token.STRING, name.getType());
    assertEquals(3, name.getLineno());
    assertEquals(0, name.getCharno());
  }

  public void testLinenoCharnoGetelem1() throws Exception {
    Node call = parse("\n foo[123]").getFirstChild().getFirstChild();

    assertEquals(Token.GETELEM, call.getType());
    assertEquals(2, call.getLineno());
    assertEquals(1, call.getCharno());
  }

  public void testLinenoCharnoGetelem2() throws Exception {
    Node call = parse("\n   \n foo()[123]").getFirstChild().getFirstChild();

    assertEquals(Token.GETELEM, call.getType());
    assertEquals(3, call.getLineno());
    assertEquals(1, call.getCharno());
  }

  public void testLinenoCharnoGetelem3() throws Exception {
    Node call = parse("\n   \n (8 + kl)[123]").getFirstChild().getFirstChild();

    assertEquals(Token.GETELEM, call.getType());
    assertEquals(3, call.getLineno());
    assertEquals(1, call.getCharno());
  }

  public void testLinenoCharnoForComparison() throws Exception {
    Node lt =
      parse("for (; i < j;){}").getFirstChild().getFirstChild().getNext();

    assertEquals(Token.LT, lt.getType());
    assertEquals(1, lt.getLineno());
    assertEquals(7, lt.getCharno());
  }

  public void testLinenoCharnoHook() throws Exception {
    Node n = parse("\n a ? 9 : 0").getFirstChild().getFirstChild();

    assertEquals(Token.HOOK, n.getType());
    assertEquals(2, n.getLineno());
    assertEquals(1, n.getCharno());
  }

  public void testLinenoCharnoArrayLiteral() throws Exception {
    Node n = parse("\n  [8, 9]").getFirstChild().getFirstChild();

    assertEquals(Token.ARRAYLIT, n.getType());
    assertEquals(2, n.getLineno());
    assertEquals(2, n.getCharno());

    n = n.getFirstChild();

    assertEquals(Token.NUMBER, n.getType());
    assertEquals(2, n.getLineno());
    assertEquals(3, n.getCharno());

    n = n.getNext();

    assertEquals(Token.NUMBER, n.getType());
    assertEquals(2, n.getLineno());
    assertEquals(6, n.getCharno());
  }

  public void testLinenoCharnoObjectLiteral() throws Exception {
    Node n = parse("\n\n var a = {a:0\n,b :1};")
        .getFirstChild().getFirstChild().getFirstChild();

    assertEquals(Token.OBJECTLIT, n.getType());
    assertEquals(3, n.getLineno());
    assertEquals(9, n.getCharno());

    Node key = n.getFirstChild();

    assertEquals(Token.STRING_KEY, key.getType());
    assertEquals(3, key.getLineno());
    assertEquals(10, key.getCharno());

    Node value = key.getFirstChild();

    assertEquals(Token.NUMBER, value.getType());
    assertEquals(3, value.getLineno());
    assertEquals(12, value.getCharno());

    key = key.getNext();

    assertEquals(Token.STRING_KEY, key.getType());
    assertEquals(4, key.getLineno());
    assertEquals(1, key.getCharno());

    value = key.getFirstChild();

    assertEquals(Token.NUMBER, value.getType());
    assertEquals(4, value.getLineno());
    assertEquals(4, value.getCharno());
  }

  public void testLinenoCharnoAdd() throws Exception {
    testLinenoCharnoBinop("+");
  }

  public void testLinenoCharnoSub() throws Exception {
    testLinenoCharnoBinop("-");
  }

  public void testLinenoCharnoMul() throws Exception {
    testLinenoCharnoBinop("*");
  }

  public void testLinenoCharnoDiv() throws Exception {
    testLinenoCharnoBinop("/");
  }

  public void testLinenoCharnoMod() throws Exception {
    testLinenoCharnoBinop("%");
  }

  public void testLinenoCharnoShift() throws Exception {
    testLinenoCharnoBinop("<<");
  }

  public void testLinenoCharnoBinaryAnd() throws Exception {
    testLinenoCharnoBinop("&");
  }

  public void testLinenoCharnoAnd() throws Exception {
    testLinenoCharnoBinop("&&");
  }

  public void testLinenoCharnoBinaryOr() throws Exception {
    testLinenoCharnoBinop("|");
  }

  public void testLinenoCharnoOr() throws Exception {
    testLinenoCharnoBinop("||");
  }

  public void testLinenoCharnoLt() throws Exception {
    testLinenoCharnoBinop("<");
  }

  public void testLinenoCharnoLe() throws Exception {
    testLinenoCharnoBinop("<=");
  }

  public void testLinenoCharnoGt() throws Exception {
    testLinenoCharnoBinop(">");
  }

  public void testLinenoCharnoGe() throws Exception {
    testLinenoCharnoBinop(">=");
  }

  private void testLinenoCharnoBinop(String binop) {
    Node op = parse("var a = 89 " + binop + " 76;").getFirstChild().
        getFirstChild().getFirstChild();

    assertEquals(1, op.getLineno());
    assertEquals(8, op.getCharno());
  }

  public void testJSDocAttachment1() {
    Node varNode = parse("/** @type number */var a;").getFirstChild();

    // VAR
    assertEquals(Token.VAR, varNode.getType());
    JSDocInfo info = varNode.getJSDocInfo();
    assertNotNull(info);
    assertTypeEquals(NUMBER_TYPE, info.getType());

    // NAME
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

  public void testJSDocAttachment2() {
    Node varNode = parse("/** @type number */var a,b;").getFirstChild();

    // VAR
    assertEquals(Token.VAR, varNode.getType());
    JSDocInfo info = varNode.getJSDocInfo();
    assertNotNull(info);
    assertTypeEquals(NUMBER_TYPE, info.getType());

    // First NAME
    Node nameNode1 = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode1.getType());
    assertNull(nameNode1.getJSDocInfo());

    // Second NAME
    Node nameNode2 = nameNode1.getNext();
    assertEquals(Token.NAME, nameNode2.getType());
    assertNull(nameNode2.getJSDocInfo());
  }

  public void testJSDocAttachment3() {
    Node assignNode = parse(
        "/** @type number */goog.FOO = 5;").getFirstChild().getFirstChild();
    assertEquals(Token.ASSIGN, assignNode.getType());
    JSDocInfo info = assignNode.getJSDocInfo();
    assertNotNull(info);
    assertTypeEquals(NUMBER_TYPE, info.getType());
  }

  public void testJSDocAttachment4() {
    Node varNode = parse(
        "var a, /** @define {number} */ b = 5;").getFirstChild();

    // ASSIGN
    assertEquals(Token.VAR, varNode.getType());
    assertNull(varNode.getJSDocInfo());

    // a
    Node a = varNode.getFirstChild();
    assertNull(a.getJSDocInfo());

    // b
    Node b = a.getNext();
    JSDocInfo info = b.getJSDocInfo();
    assertNotNull(info);
    assertTrue(info.isDefine());
    assertTypeEquals(NUMBER_TYPE, info.getType());
  }

  public void testJSDocAttachment5() {
    Node varNode = parse(
        "var /** @type number */a, /** @define {number} */b = 5;")
        .getFirstChild();

    // ASSIGN
    assertEquals(Token.VAR, varNode.getType());
    assertNull(varNode.getJSDocInfo());

    // a
    Node a = varNode.getFirstChild();
    assertNotNull(a.getJSDocInfo());
    JSDocInfo info = a.getJSDocInfo();
    assertNotNull(info);
    assertFalse(info.isDefine());
    assertTypeEquals(NUMBER_TYPE, info.getType());

    // b
    Node b = a.getNext();
    info = b.getJSDocInfo();
    assertNotNull(info);
    assertTrue(info.isDefine());
    assertTypeEquals(NUMBER_TYPE, info.getType());
  }

  /**
   * Tests that a JSDoc comment in an unexpected place of the code does not
   * propagate to following code due to {@link JSDocInfo} aggregation.
   */
  public void testJSDocAttachment6() throws Exception {
    Node functionNode = parse(
        "var a = /** @param {number} index */5;" +
        "/** @return boolean */function f(index){}")
        .getFirstChild().getNext();

    assertEquals(Token.FUNCTION, functionNode.getType());
    JSDocInfo info = functionNode.getJSDocInfo();
    assertNotNull(info);
    assertFalse(info.hasParameter("index"));
    assertTrue(info.hasReturnType());
    assertTypeEquals(UNKNOWN_TYPE, info.getReturnType());
  }

  public void testJSDocAttachment7() {
    Node varNode = parse("/** */var a;").getFirstChild();

    // VAR
    assertEquals(Token.VAR, varNode.getType());

    // NAME
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

  public void testJSDocAttachment8() {
    Node varNode = parse("/** x */var a;").getFirstChild();

    // VAR
    assertEquals(Token.VAR, varNode.getType());

    // NAME
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

  public void testJSDocAttachment9() {
    Node varNode = parse("/** \n x */var a;").getFirstChild();

    // VAR
    assertEquals(Token.VAR, varNode.getType());

    // NAME
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

  public void testJSDocAttachment10() {
    Node varNode = parse("/** x\n */var a;").getFirstChild();

    // VAR
    assertEquals(Token.VAR, varNode.getType());

    // NAME
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

  public void testJSDocAttachment11() {
    Node varNode =
       parse("/** @type {{x : number, 'y' : string, z}} */var a;")
        .getFirstChild();

    // VAR
    assertEquals(Token.VAR, varNode.getType());
    JSDocInfo info = varNode.getJSDocInfo();
    assertNotNull(info);

    assertTypeEquals(createRecordTypeBuilder().
                     addProperty("x", NUMBER_TYPE, null).
                     addProperty("y", STRING_TYPE, null).
                     addProperty("z", UNKNOWN_TYPE, null).
                     build(),
                     info.getType());

    // NAME
    Node nameNode = varNode.getFirstChild();
    assertEquals(Token.NAME, nameNode.getType());
    assertNull(nameNode.getJSDocInfo());
  }

  public void testJSDocAttachment12() {
    Node varNode =
       parse("var a = {/** @type {Object} */ b: c};")
        .getFirstChild();
    Node objectLitNode = varNode.getFirstChild().getFirstChild();
    assertEquals(Token.OBJECTLIT, objectLitNode.getType());
    assertNotNull(objectLitNode.getFirstChild().getJSDocInfo());
  }

  public void testJSDocAttachment13() {
    Node varNode = parse("/** foo */ var a;").getFirstChild();
    assertNotNull(varNode.getJSDocInfo());
  }

  public void testJSDocAttachment14() {
    Node varNode = parse("/** */ var a;").getFirstChild();
    assertNull(varNode.getJSDocInfo());
  }

  public void testJSDocAttachment15() {
    Node varNode = parse("/** \n * \n */ var a;").getFirstChild();
    assertNull(varNode.getJSDocInfo());
  }

  public void testJSDocAttachment16() {
    Node exprCall =
        parse("/** @private */ x(); function f() {};").getFirstChild();
    assertEquals(Token.EXPR_RESULT, exprCall.getType());
    assertNull(exprCall.getNext().getJSDocInfo());
    assertNotNull(exprCall.getFirstChild().getJSDocInfo());
  }

  public void testJSDocAttachment17() {
    Node fn =
        parse(
            "function f() { " +
            "  return /** @type {string} */ (g(1 /** @desc x */));" +
            "};").getFirstChild();
    assertEquals(Token.FUNCTION, fn.getType());
    Node cast = fn.getLastChild().getFirstChild().getFirstChild();
    assertEquals(Token.CAST, cast.getType());
  }

  public void testJSDocAttachment18() {
    Node fn =
        parse(
            "function f() { " +
            "  var x = /** @type {string} */ (y);" +
            "};").getFirstChild();
    assertEquals(Token.FUNCTION, fn.getType());
    Node cast =
        fn.getLastChild().getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.CAST, cast.getType());
  }

  public void testJSDocAttachment19() {
    Node fn =
        parse(
            "function f() { " +
            "  /** @type {string} */" +
            "  return;" +
            "};",
            MISPLACED_TYPE_ANNOTATION).getFirstChild();
    assertEquals(Token.FUNCTION, fn.getType());

    Node ret = fn.getLastChild().getFirstChild();
    assertEquals(Token.RETURN, ret.getType());
    assertNotNull(ret.getJSDocInfo());
  }

  public void testJSDocAttachment20() {
    Node fn =
        parse(
            "function f() { " +
            "  /** @type {string} */" +
            "  if (true) return;" +
            "};", MISPLACED_TYPE_ANNOTATION).getFirstChild();
    assertEquals(Token.FUNCTION, fn.getType());

    Node ret = fn.getLastChild().getFirstChild();
    assertEquals(Token.IF, ret.getType());
    assertNotNull(ret.getJSDocInfo());
  }

  public void testInlineJSDocAttachment1() {
    Node fn = parse("function f(/** string */ x) {}").getFirstChild();
    assertTrue(fn.isFunction());

    JSDocInfo info =
        fn.getFirstChild().getNext().getFirstChild().getJSDocInfo();
    assertNotNull(info);
    assertTypeEquals(STRING_TYPE, info.getType());
  }

  public void testInlineJSDocAttachment2() {
    Node fn = parse(
        "function f(/** ? */ x) {}").getFirstChild();
    assertTrue(fn.isFunction());

    JSDocInfo info =
        fn.getFirstChild().getNext().getFirstChild().getJSDocInfo();
    assertNotNull(info);
    assertTypeEquals(UNKNOWN_TYPE, info.getType());
  }

  public void testInlineJSDocAttachment3() {
    parse(
        "function f(/** @type {string} */ x) {}",
        "Bad type annotation. type not recognized due to syntax error");
  }

  public void testInlineJSDocAttachment4() {
    parse(
        "function f(/**\n" +
        " * @type {string}\n" +
        " */ x) {}",
        "Bad type annotation. type not recognized due to syntax error");
  }

  public void testInlineJSDocAttachment5() {
    Node vardecl = parse("var /** string */ x = 'asdf';").getFirstChild();
    JSDocInfo info = vardecl.getFirstChild().getJSDocInfo();
    assertNotNull(info);
    assertTrue(info.hasType());
    assertTypeEquals(STRING_TYPE, info.getType());
  }

  public void testInlineJSDocAttachment6() {
    Node fn = parse("function f(/** {attr: number} */ x) {}").getFirstChild();
    assertTrue(fn.isFunction());

    JSDocInfo info =
        fn.getFirstChild().getNext().getFirstChild().getJSDocInfo();
    assertNotNull(info);
    assertTypeEquals(createRecordTypeBuilder().
        addProperty("attr", NUMBER_TYPE, null).
        build(),
        info.getType());
  }

  public void testIncorrectJSDocDoesNotAlterJSParsing1() throws Exception {
    assertNodeEquality(
        parse("var a = [1,2]"),
        parse("/** @type Array.<number*/var a = [1,2]",
            MISSING_GT_MESSAGE));
  }

  public void testIncorrectJSDocDoesNotAlterJSParsing2() throws Exception {
    assertNodeEquality(
        parse("var a = [1,2]"),
        parse("/** @type {Array.<number}*/var a = [1,2]",
            MISSING_GT_MESSAGE));
  }

  public void testIncorrectJSDocDoesNotAlterJSParsing3() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("/** @param {Array.<number} nums */" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};",
            MISSING_GT_MESSAGE));
  }

  public void testIncorrectJSDocDoesNotAlterJSParsing4() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("/** @return boolean */" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};"));
  }

  public void testIncorrectJSDocDoesNotAlterJSParsing5() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("/** @param boolean this is some string*/" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};"));
  }

  public void testIncorrectJSDocDoesNotAlterJSParsing6() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("/** @param {bool!*%E$} */" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};",
            "Bad type annotation. expected closing }",
            "Bad type annotation. expecting a variable name in a @param tag"));
  }

  public void testIncorrectJSDocDoesNotAlterJSParsing7() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("/** @see */" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};",
              "@see tag missing description"));
  }

  public void testIncorrectJSDocDoesNotAlterJSParsing8() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("/** @author */" +
            "C.prototype.say=function(nums) {alert(nums.join(','));};",
              "@author tag missing author"));
  }

  public void testIncorrectJSDocDoesNotAlterJSParsing9() throws Exception {
    assertNodeEquality(
        parse("C.prototype.say=function(nums) {alert(nums.join(','));};"),
        parse("/** @someillegaltag */" +
              "C.prototype.say=function(nums) {alert(nums.join(','));};",
              "illegal use of unknown JSDoc tag \"someillegaltag\";"
              + " ignoring it"));
  }

  public void testUnescapedSlashInRegexpCharClass() throws Exception {
    // The tokenizer without the fix for this bug throws an error.
    parse("var foo = /[/]/;");
    parse("var foo = /[hi there/]/;");
    parse("var foo = /[/yo dude]/;");
    parse("var foo = /\\/[@#$/watashi/wa/suteevu/desu]/;");
  }

  private void assertNodeEquality(Node expected, Node found) {
    String message = expected.checkTreeEquals(found);
    if (message != null) {
      fail(message);
    }
  }

  @SuppressWarnings("unchecked")
  public void testParse() {
    mode = LanguageMode.ECMASCRIPT5;
    Node a = Node.newString(Token.NAME, "a");
    a.addChildToFront(Node.newString(Token.NAME, "b"));
    List<ParserResult> testCases = ImmutableList.of(
        new ParserResult(
            "3;",
            createScript(new Node(Token.EXPR_RESULT, Node.newNumber(3.0)))),
        new ParserResult(
            "var a = b;",
             createScript(new Node(Token.VAR, a))),
        new ParserResult(
            "\"hell\\\no\\ world\\\n\\\n!\"",
             createScript(new Node(Token.EXPR_RESULT,
             Node.newString(Token.STRING, "hello world!"))))
        );

    for (ParserResult testCase : testCases) {
      assertNodeEquality(testCase.node, parse(testCase.code));
    }
  }

  private Node createScript(Node n) {
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(n);
    return script;
  }

  public void testTrailingCommaWarning1() {
    parse("var a = ['foo', 'bar'];");
  }

  public void testTrailingCommaWarning2() {
    parse("var a = ['foo',,'bar'];");
  }

  public void testTrailingCommaWarning3() {
    parse("var a = ['foo', 'bar',];", TRAILING_COMMA_MESSAGE);
    mode = LanguageMode.ECMASCRIPT5;
    parse("var a = ['foo', 'bar',];");
  }

  public void testTrailingCommaWarning4() {
    parse("var a = [,];", TRAILING_COMMA_MESSAGE);
    mode = LanguageMode.ECMASCRIPT5;
    parse("var a = [,];");
  }

  public void testTrailingCommaWarning5() {
    parse("var a = {'foo': 'bar'};");
  }

  public void testTrailingCommaWarning6() {
    parse("var a = {'foo': 'bar',};", TRAILING_COMMA_MESSAGE);
    mode = LanguageMode.ECMASCRIPT5;
    parse("var a = {'foo': 'bar',};");
  }

  public void testTrailingCommaWarning7() {
    parseError("var a = {,};",
        "'}' expected");
  }

  public void testSuspiciousBlockCommentWarning1() {
    parse("/* @type {number} */ var x = 3;", SUSPICIOUS_COMMENT_WARNING);
  }

  public void testSuspiciousBlockCommentWarning2() {
    parse("/* \n * @type {number} */ var x = 3;", SUSPICIOUS_COMMENT_WARNING);
  }

  public void testSuspiciousBlockCommentWarning3() {
    parse("/* \n *@type {number} */ var x = 3;", SUSPICIOUS_COMMENT_WARNING);
  }

  public void testSuspiciousBlockCommentWarning4() {
    parse(
        "  /*\n" +
        "   * @type {number}\n" +
        "   */\n" +
        "  var x = 3;",
        SUSPICIOUS_COMMENT_WARNING);
  }

  public void testSuspiciousBlockCommentWarning5() {
    parse(
        "  /*\n" +
        "   * some random text here\n" +
        "   * @type {number}\n" +
        "   */\n" +
        "  var x = 3;",
        SUSPICIOUS_COMMENT_WARNING);
  }

  public void testSuspiciousBlockCommentWarning6() {
    parse("/* @type{number} */ var x = 3;", SUSPICIOUS_COMMENT_WARNING);
  }

  public void testSuspiciousBlockCommentWarning7() {
    // jsdoc tags contain letters only, no underscores etc.
    parse("/* @cc_on */ var x = 3;");
  }

  public void testSuspiciousBlockCommentWarning8() {
    // a jsdoc tag can't be immediately followed by a paren
    parse("/* @TODO(username) */ var x = 3;");
  }

  public void testCatchClauseForbidden() {
    parseError("try { } catch (e if true) {}",
        "')' expected");
  }

  public void testConstForbidden() {
    parseError("const x = 3;",
        "unsupported language feature: const declarations");
  }

  public void testDestructuringAssignForbidden() {
    parseError("var [x, y] = foo();",
        "unsupported language feature: destructuring");
  }

  public void testDestructuringAssignForbidden2() {
    parseError("var {x, y} = foo();",
        "unsupported language feature: destructuring");
  }

  public void testDestructuringAssignForbidden3() {
    parseError("var {x: x, y: y} = foo();",
        "unsupported language feature: destructuring");
  }

  public void testDestructuringAssignForbidden4() {
    parseError("[x, y] = foo();",
        "unsupported language feature: destructuring");
  }

  public void testLetForbidden1() {
    parseError("let x = 3;", "unsupported language feature: let declarations");
  }

  public void testLetForbidden2() {
    parseError("function f() { let x = 3; };",
        "unsupported language feature: let declarations");
  }

  public void testLetForbidden3() {
    mode = LanguageMode.ECMASCRIPT5_STRICT;
    parseError("function f() { var let = 3; }",
        "'identifier' expected");
  }

  public void testYieldForbidden() {
    parseError("function f() { yield 3; }",
        "unsupported language feature: generators");
  }

  public void testBracelessFunctionForbidden() {
    parseError("var sq = function(x) x * x;",
        "'{' expected");
  }

  public void testGeneratorsForbidden() {
    parseError("var i = (x for (x in obj));",
        "')' expected");
  }

  public void testGettersForbidden1() {
    parseError("var x = {get foo() { return 3; }};",
        IRFactory.GETTER_ERROR_MESSAGE);
  }

  public void testGettersForbidden2() {
    parseError("var x = {get foo bar() { return 3; }};",
        "'(' expected");
  }

  public void testGettersForbidden3() {
    parseError("var x = {a getter:function b() { return 3; }};",
        "':' expected");
  }

  public void testGettersForbidden4() {
    parseError("var x = {\"a\" getter:function b() { return 3; }};",
        "':' expected");
  }

  public void testGettersForbidden5() {
    parseError("var x = {a: 2, get foo() { return 3; }};",
        IRFactory.GETTER_ERROR_MESSAGE);
  }

  public void testGettersForbidden6() {
    parseError("var x = {get 'foo'() { return 3; }};",
        IRFactory.GETTER_ERROR_MESSAGE);
  }

  public void testSettersForbidden() {
    parseError("var x = {set foo(a) { y = 3; }};",
        IRFactory.SETTER_ERROR_MESSAGE);
  }

  public void testSettersForbidden2() {
    // TODO(johnlenz): maybe just report the first error, when not in IDE mode?
    parseError("var x = {a setter:function b() { return 3; }};",
        "':' expected");
  }

  public void testFileOverviewJSDoc1() {
    Node n = parse("/** @fileoverview Hi mom! */ function Foo() {}");
    assertEquals(Token.FUNCTION, n.getFirstChild().getType());
    assertTrue(n.getJSDocInfo() != null);
    assertNull(n.getFirstChild().getJSDocInfo());
    assertEquals("Hi mom!",
        n.getJSDocInfo().getFileOverview());
  }

  public void testFileOverviewJSDocDoesNotHoseParsing() {
    assertEquals(
        Token.FUNCTION,
        parse("/** @fileoverview Hi mom! \n */ function Foo() {}")
            .getFirstChild().getType());
    assertEquals(
        Token.FUNCTION,
        parse("/** @fileoverview Hi mom! \n * * * */ function Foo() {}")
            .getFirstChild().getType());
    assertEquals(
        Token.FUNCTION,
        parse("/** @fileoverview \n * x */ function Foo() {}")
            .getFirstChild().getType());
    assertEquals(
        Token.FUNCTION,
        parse("/** @fileoverview \n * x \n */ function Foo() {}")
            .getFirstChild().getType());
  }

  public void testFileOverviewJSDoc2() {
    Node n = parse("/** @fileoverview Hi mom! */ " +
        "/** @constructor */ function Foo() {}");
    assertTrue(n.getJSDocInfo() != null);
    assertEquals("Hi mom!", n.getJSDocInfo().getFileOverview());
    assertTrue(n.getFirstChild().getJSDocInfo() != null);
    assertFalse(n.getFirstChild().getJSDocInfo().hasFileOverview());
    assertTrue(n.getFirstChild().getJSDocInfo().isConstructor());
  }

  public void testObjectLiteralDoc1() {
    Node n = parse("var x = {/** @type {number} */ 1: 2};");

    Node objectLit = n.getFirstChild().getFirstChild().getFirstChild();
    assertEquals(Token.OBJECTLIT, objectLit.getType());

    Node number = objectLit.getFirstChild();
    assertEquals(Token.STRING_KEY, number.getType());
    assertNotNull(number.getJSDocInfo());
  }

  public void testDuplicatedParam() {
    parse("function foo(x, x) {}", "Duplicate parameter name \"x\".");
  }

  public void testLet() {
    mode = LanguageMode.ECMASCRIPT3;
    parse("var let");

    mode = LanguageMode.ECMASCRIPT5;
    parse("var let");

    mode = LanguageMode.ECMASCRIPT5_STRICT;
    parseError("var let", "'identifier' expected");

    mode = LanguageMode.ECMASCRIPT6;
    parse("var let");

    mode = LanguageMode.ECMASCRIPT6_STRICT;
    parseError("var let", "'identifier' expected");
  }

  public void testYield() {
    mode = LanguageMode.ECMASCRIPT3;
    parse("var yield");

    mode = LanguageMode.ECMASCRIPT5;
    parse("var yield");

    mode = LanguageMode.ECMASCRIPT5_STRICT;
    parseError("var yield", "'identifier' expected");

    mode = LanguageMode.ECMASCRIPT6;
    parse("var yield");

    mode = LanguageMode.ECMASCRIPT6_STRICT;
    parseError("var yield", "'identifier' expected");
  }

  public void testStringContinuations() {
    mode = LanguageMode.ECMASCRIPT3;
    parse("'\\\n';",
        "String continuations are not supported in this language mode.");
    mode = LanguageMode.ECMASCRIPT5;
    parse("'\\\n';");
    mode = LanguageMode.ECMASCRIPT6;
    parse("'\\\n';");
  }

  public void testBinaryLiterals() {
    mode = LanguageMode.ECMASCRIPT3;
    parse("0b0001;",
        "Binary integer literals are not supported in this language mode.");
    mode = LanguageMode.ECMASCRIPT5;
    parse("0b0001;",
        "Binary integer literals are not supported in this language mode.");
    mode = LanguageMode.ECMASCRIPT6;
    parse("0b0001;");
  }

  public void testOctalLiterals() {
    mode = LanguageMode.ECMASCRIPT3;
    parse("0o0001;",
        "Octal integer literals are not supported in this language mode.");
    mode = LanguageMode.ECMASCRIPT5;
    parse("0o0001;",
        "Octal integer literals are not supported in this language mode.");
    mode = LanguageMode.ECMASCRIPT6;
    parse("0o0001;");
  }


  public void testGetter() {
    mode = LanguageMode.ECMASCRIPT3;
    parseError("var x = {get 1(){}};",
        IRFactory.GETTER_ERROR_MESSAGE);
    parseError("var x = {get 'a'(){}};",
        IRFactory.GETTER_ERROR_MESSAGE);
    parseError("var x = {get a(){}};",
        IRFactory.GETTER_ERROR_MESSAGE);
    mode = LanguageMode.ECMASCRIPT5;
    parse("var x = {get 1(){}};");
    parse("var x = {get 'a'(){}};");
    parse("var x = {get a(){}};");
    parseError("var x = {get a(b){}};", "')' expected");
  }

  public void testSetter() {
    mode = LanguageMode.ECMASCRIPT3;
    parseError("var x = {set 1(x){}};",
        IRFactory.SETTER_ERROR_MESSAGE);
    parseError("var x = {set 'a'(x){}};",
        IRFactory.SETTER_ERROR_MESSAGE);
    parseError("var x = {set a(x){}};",
        IRFactory.SETTER_ERROR_MESSAGE);
    mode = LanguageMode.ECMASCRIPT5;
    parse("var x = {set 1(x){}};");
    parse("var x = {set 'a'(x){}};");
    parse("var x = {set a(x){}};");
    parseError("var x = {set a(){}};",
        "'identifier' expected");
  }

  public void testLamestWarningEver() {
    // This used to be a warning.
    parse("var x = /** @type {undefined} */ (y);");
    parse("var x = /** @type {void} */ (y);");
  }

  public void testUnfinishedComment() {
    parseError("/** this is a comment ", "unterminated comment");
  }

  public void testParseBlockDescription() {
    Node n = parse("/** This is a variable. */ var x;");
    Node var = n.getFirstChild();
    assertNotNull(var.getJSDocInfo());
    assertEquals("This is a variable.",
        var.getJSDocInfo().getBlockDescription());
  }

  public void testUnnamedFunctionStatement() {
    // Statements
    parseError("function() {};", "'identifier' expected");
    parseError("if (true) { function() {}; }", "'identifier' expected");
    parse("function f() {};");
    // Expressions
    parse("(function f() {});");
    parse("(function () {});");
  }

  public void testReservedKeywords() {
    mode = LanguageMode.ECMASCRIPT3;

    parseError("var boolean;", "identifier is a reserved word");
    parseError("function boolean() {};",
        "identifier is a reserved word");
    parseError("boolean = 1;", "identifier is a reserved word");

    // TODO(johnlenz): reenable
    // parseError("class = 1;", "identifier is a reserved word");
    // parseError("public = 2;", "identifier is a reserved word");

    mode = LanguageMode.ECMASCRIPT5;

    parse("var boolean;");
    parse("function boolean() {};");
    parse("boolean = 1;");

    // TODO(johnlenz): reenable
    // parseError("class = 1;", "identifier is a reserved word");
    // parse("public = 2;");

    mode = LanguageMode.ECMASCRIPT5_STRICT;

    parse("var boolean;");
    parse("function boolean() {};");
    parse("boolean = 1;");
    parseError("public = 2;", "primary expression expected");

    // TODO(johnlenz): reenable
    // parseError("class = 1;", "'identifier' expected");
  }

  public void testKeywordsAsProperties() {
    mode = LanguageMode.ECMASCRIPT3;

    // parse("var x = {function: 1};", IRFactory.INVALID_ES3_PROP_NAME);
    parse("x.function;", IRFactory.INVALID_ES3_PROP_NAME);
    parseError("var x = {get x(){} };",
        IRFactory.GETTER_ERROR_MESSAGE);
    parseError("var x = {get function(){} };", IRFactory.GETTER_ERROR_MESSAGE);
    parseError("var x = {get 'function'(){} };",
        IRFactory.GETTER_ERROR_MESSAGE);
    parseError("var x = {get 1(){} };",
        IRFactory.GETTER_ERROR_MESSAGE);
    parseError("var x = {set function(a){} };", IRFactory.SETTER_ERROR_MESSAGE);
    parseError("var x = {set 'function'(a){} };",
        IRFactory.SETTER_ERROR_MESSAGE);
    parseError("var x = {set 1(a){} };",
        IRFactory.SETTER_ERROR_MESSAGE);
    parse("var x = {class: 1};", IRFactory.INVALID_ES3_PROP_NAME);
    parse("var x = {'class': 1};");
    parse("x.class;", IRFactory.INVALID_ES3_PROP_NAME);
    parse("x['class'];");
    parse("var x = {let: 1};");  // 'let' is not reserved in ES3
    parse("x.let;");
    parse("var x = {yield: 1};"); // 'yield' is not reserved in ES3
    parse("x.yield;");

    mode = LanguageMode.ECMASCRIPT5;

    parse("var x = {function: 1};");
    parse("x.function;");
    parse("var x = {get function(){} };");
    parse("var x = {get 'function'(){} };");
    parse("var x = {get 1(){} };");
    parse("var x = {set function(a){} };");
    parse("var x = {set 'function'(a){} };");
    parse("var x = {set 1(a){} };");
    parse("var x = {class: 1};");
    parse("x.class;");
    parse("var x = {let: 1};");
    parse("x.let;");
    parse("var x = {yield: 1};");
    parse("x.yield;");

    mode = LanguageMode.ECMASCRIPT5_STRICT;

    parse("var x = {function: 1};");
    parse("x.function;");
    parse("var x = {get function(){} };");
    parse("var x = {get 'function'(){} };");
    parse("var x = {get 1(){} };");
    parse("var x = {set function(a){} };");
    parse("var x = {set 'function'(a){} };");
    parse("var x = {set 1(a){} };");
    parse("var x = {class: 1};");
    parse("x.class;");
    parse("var x = {let: 1};");
    parse("x.let;");
    parse("var x = {yield: 1};");
    parse("x.yield;");
  }

  public void testGetPropFunctionName() {
    parseError("function a.b() {}",
        "'(' expected");
    parseError("var x = function a.b() {}",
        "'(' expected");
  }

  public void testGetPropFunctionNameIdeMode() {
    // In IDE mode, we try to fix up the tree, but sometimes
    // this leads to even more errors.
    isIdeMode = true;
    parseError("function a.b() {}",
        "'(' expected",
        "',' expected");
    parseError("var x = function a.b() {}",
        "'(' expected",
        "',' expected");
  }

  public void testIdeModePartialTree() {
    Node partialTree = parseError("function Foo() {} f.",
        "'identifier' expected");
    assertNull(partialTree);

    isIdeMode = true;
    partialTree = parseError("function Foo() {} f.",
        "'identifier' expected");
    assertNotNull(partialTree);
  }

  public void testForEach() {
    parseError(
        "function f(stamp, status) {\n" +
        "  for each ( var curTiming in this.timeLog.timings ) {\n" +
        "    if ( curTiming.callId == stamp ) {\n" +
        "      curTiming.flag = status;\n" +
        "      break;\n" +
        "    }\n" +
        "  }\n" +
        "};",
        "'(' expected");
  }

  public void testMisplacedTypeAnnotation1() {
    // misuse with COMMA
    parse(
        "var o = {};" +
        "/** @type {string} */ o.prop1 = 1, o.prop2 = 2;",
        MISPLACED_TYPE_ANNOTATION);
  }

  public void testMisplacedTypeAnnotation2() {
    // missing parentheses for the cast.
    parse(
        "var o = /** @type {string} */ getValue();",
        MISPLACED_TYPE_ANNOTATION);
  }

  public void testMisplacedTypeAnnotation3() {
    // missing parentheses for the cast.
    parse(
        "var o = 1 + /** @type {string} */ value;",
        MISPLACED_TYPE_ANNOTATION);
  }

  public void testMisplacedTypeAnnotation4() {
    // missing parentheses for the cast.
    parse(
        "var o = /** @type {!Array.<string>} */ ['hello', 'you'];",
        MISPLACED_TYPE_ANNOTATION);
  }

  public void testMisplacedTypeAnnotation5() {
    // missing parentheses for the cast.
    parse(
        "var o = (/** @type {!Foo} */ {});",
        MISPLACED_TYPE_ANNOTATION);
  }

  public void testMisplacedTypeAnnotation6() {
    parse("var o = /** @type {function():string} */ function() {return 'str';}",
        MISPLACED_TYPE_ANNOTATION);
  }

  public void testValidTypeAnnotation1() {
    parse("/** @type {string} */ var o = 'str';");
    parse("var /** @type {string} */ o = 'str', /** @type {number} */ p = 0;");
    parse("/** @type {function():string} */ function o() { return 'str'; }");
    parse("var o = {}; /** @type {string} */ o.prop = 'str';");
    parse("var o = {}; /** @type {string} */ o['prop'] = 'str';");
    parse("var o = { /** @type {string} */ prop : 'str' };");
    parse("var o = { /** @type {string} */ 'prop' : 'str' };");
    parse("var o = { /** @type {string} */ 1 : 'str' };");
  }

  public void testValidTypeAnnotation2() {
    mode = LanguageMode.ECMASCRIPT5;
    parse("var o = { /** @type {string} */ get prop() { return 'str' }};");
    parse("var o = { /** @type {string} */ set prop(s) {}};");
  }

  public void testValidTypeAnnotation3() {
    // This one we don't currently support in the type checker but
    // we would like to.
    parse("try {} catch (/** @type {Error} */ e) {}");
  }

  public void testParsingAssociativity() {
    assertNodeEquality(parse("x * y * z"), parse("(x * y) * z"));
    assertNodeEquality(parse("x + y + z"), parse("(x + y) + z"));
    assertNodeEquality(parse("x | y | z"), parse("(x | y) | z"));
    assertNodeEquality(parse("x & y & z"), parse("(x & y) & z"));
    assertNodeEquality(parse("x ^ y ^ z"), parse("(x ^ y) ^ z"));
    assertNodeEquality(parse("x || y || z"), parse("(x || y) || z"));
    assertNodeEquality(parse("x && y && z"), parse("(x && y) && z"));
  }

  public void testIssue1116() {
    parse("/**/");
  }

  public void testRegExp() {
    assertNodeEquality(parse("/a/"), script(expr(regex("a"))));
    assertNodeEquality(parse("/\\\\/"), script(expr(regex("\\\\"))));
    assertNodeEquality(parse("/\\s/"), script(expr(regex("\\s"))));
    assertNodeEquality(parse("/\\u000A/"), script(expr(regex("\\u000A"))));
    assertNodeEquality(parse("/[\\]]/"), script(expr(regex("[\\]]"))));
  }

  private Node script(Node stmt) {
    Node n = new Node(Token.SCRIPT, stmt);
    n.setIsSyntheticBlock(true);
    return n;
  }

  private Node expr(Node n) {
    return new Node(Token.EXPR_RESULT, n);
  }

  private Node regex(String regex) {
    return new Node(Token.REGEXP, Node.newString(regex));
  }

  private Node regex(String regex, String flags) {
    return new Node(Token.REGEXP, Node.newString(regex), Node.newString(flags));
  }

  /**
   * Verify that the given code has the given parse errors.
   * @return If in IDE mode, returns a partial tree.
   */
  private Node parseError(String string, String... errors) {
    TestErrorReporter testErrorReporter = new TestErrorReporter(errors, null);
    Node script = null;
    try {
      StaticSourceFile file = new SimpleSourceFile("input", false);
      script = ParserRunner.parseEs6(
          file, string,
          ParserRunner.createConfig(isIdeMode, mode, false),
          testErrorReporter, Logger.getAnonymousLogger()).ast;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // verifying that all warnings were seen
    assertTrue(testErrorReporter.hasEncounteredAllErrors());
    assertTrue(testErrorReporter.hasEncounteredAllWarnings());

    return script;
  }

  private Node parse(String string, String... warnings) {
    TestErrorReporter testErrorReporter = new TestErrorReporter(null, warnings);
    Node script = null;
    try {
      StaticSourceFile file = new SimpleSourceFile("input", false);
      script = ParserRunner.parseEs6(
          file, string, ParserRunner.createConfig(true, mode, false),
          testErrorReporter, Logger.getAnonymousLogger()).ast;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // verifying that all warnings were seen
    assertTrue(testErrorReporter.hasEncounteredAllErrors());
    assertTrue(testErrorReporter.hasEncounteredAllWarnings());

    return script;
  }

  private static class ParserResult {
    private final String code;
    private final Node node;

    private ParserResult(String code, Node node) {
      this.code = code;
      this.node = node;
    }
  }
}
