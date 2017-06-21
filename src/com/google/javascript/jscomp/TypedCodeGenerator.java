/*
 * Copyright 2009 The Closure Compiler Authors.
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

package com.google.javascript.jscomp;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.javascript.rhino.FunctionTypeI;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.ObjectTypeI;
import com.google.javascript.rhino.TypeI;
import com.google.javascript.rhino.TypeIRegistry;
import com.google.javascript.rhino.jstype.JSTypeNative;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * A code generator that outputs type annotations for functions and
 * constructors.
 */
class TypedCodeGenerator extends CodeGenerator {
  private final TypeIRegistry registry;
  private final JSDocInfoPrinter jsDocInfoPrinter;

  TypedCodeGenerator(
      CodeConsumer consumer, CompilerOptions options, TypeIRegistry registry) {
    super(consumer, options);
    Preconditions.checkNotNull(registry);
    this.registry = registry;
    this.jsDocInfoPrinter = new JSDocInfoPrinter(options.getUseOriginalNamesInOutput());
  }

  @Override
  protected void add(Node n, Context context) {
    Node parent = n.getParent();
    if (parent != null && (parent.isNormalBlock() || parent.isScript())) {
      if (n.isFunction()) {
        add(getFunctionAnnotation(n));
      } else if (n.isExprResult()
          && n.getFirstChild().isAssign()) {
        Node assign = n.getFirstChild();
        if (NodeUtil.isNamespaceDecl(assign.getFirstChild())) {
          add(jsDocInfoPrinter.print(assign.getJSDocInfo()));
        } else {
          Node rhs = assign.getLastChild();
          add(getTypeAnnotation(rhs));
        }
      } else if (n.isVar()
          && n.getFirstFirstChild() != null) {
        if (NodeUtil.isNamespaceDecl(n.getFirstChild())) {
          add(jsDocInfoPrinter.print(n.getJSDocInfo()));
        } else {
          add(getTypeAnnotation(n.getFirstFirstChild()));
        }
      }
    }

    super.add(n, context);
  }

  private String getTypeAnnotation(Node node) {
    // Only add annotations for things with JSDoc, or function literals.
    JSDocInfo jsdoc = NodeUtil.getBestJSDocInfo(node);
    if (jsdoc == null && !node.isFunction()) {
      return "";
    }

    TypeI type = node.getTypeI();
    if (type == null) {
      return "";
    } else if (type.isFunctionType()) {
      return getFunctionAnnotation(node);
    } else if (type.isEnumObject()) {
      return "/** @enum {"
          + type.toMaybeObjectType().getElementsType().toNonNullAnnotationString()
          + "} */\n";
    } else if (!type.isUnknownType()
        && !type.isBottom()
        && !type.isVoidType()
        && !type.isPrototypeObject()) {
      return "/** @type {" + node.getTypeI().toNonNullAnnotationString() + "} */\n";
    } else {
      return "";
    }
  }

  /**
   * @param fnNode A node for a function for which to generate a type annotation
   */
  private String getFunctionAnnotation(Node fnNode) {
    TypeI type = fnNode.getTypeI();
    Preconditions.checkState(fnNode.isFunction() || type.isFunctionType());

    if (type == null || type.isUnknownType()) {
      return "";
    }

    FunctionTypeI funType = type.toMaybeFunctionType();

    if (type.equals(registry.getNativeType(JSTypeNative.FUNCTION_INSTANCE_TYPE))) {
      return "/** @type {!Function} */\n";
    }

    StringBuilder sb = new StringBuilder("/**\n");


    Node paramNode = null;
    // We need to use the child nodes of the function as the nodes for the
    // parameters of the function type do not have the real parameter names.
    // FUNCTION
    //   NAME
    //   PARAM_LIST
    //     NAME param1
    //     NAME param2
    if (fnNode != null && fnNode.isFunction()) {
      paramNode = NodeUtil.getFunctionParameters(fnNode).getFirstChild();
    }

    // Param types
    int minArgs = funType.getMinArguments();
    int maxArgs = funType.getMaxArguments();
    List<TypeI> args = ImmutableList.copyOf(funType.getParameterTypes());
    for (int i = 0; i < args.size(); i++) {
      sb.append(" * ");
      appendAnnotation(sb, "param", getParameterJSDocType(args, i, minArgs, maxArgs));
      sb.append(" ")
          .append(paramNode == null ? "p" + i : paramNode.getString())
          .append("\n");
      if (paramNode != null) {
        paramNode = paramNode.getNext();
      }
    }

    // Return type
    TypeI retType = funType.getReturnType();
    if (retType != null
        && !retType.isBottom() // There is no annotation for the empty type.
        && !funType.isInterface() // Interfaces never return a value.
        && !(funType.isConstructor()
        && retType.isVoidType())) {
      sb.append(" * ");
      appendAnnotation(sb, "return", retType.toNonNullAnnotationString());
      sb.append("\n");
    }

    // Constructor/interface
    if (funType.isConstructor() || funType.isInterface()) {

      if (funType.isInterface()) {
        Set<String> interfaces = new TreeSet<>();
        for (ObjectTypeI interfaceType : funType.getAncestorInterfaces()) {
          interfaces.add(interfaceType.toAnnotationString());
        }
        for (String interfaze : interfaces) {
          sb.append(" * ");
          appendAnnotation(sb, "extends", interfaze);
          sb.append("\n");
        }
      }

      if (funType.isConstructor()) {
        FunctionTypeI superConstructor = funType.getInstanceType().getSuperClassConstructor();
        if (superConstructor != null) {
          ObjectTypeI superInstance = superConstructor.getInstanceType();
          if (!superInstance.toString().equals("Object")) {
            sb.append(" * ");
            appendAnnotation(sb, "extends", superInstance.toAnnotationString());
            sb.append("\n");
          }
        }
        // Avoid duplicates, add implemented type to a set first
        Set<String> interfaces = new TreeSet<>();
        for (ObjectTypeI interfaze : funType.getAncestorInterfaces()) {
          interfaces.add(interfaze.toAnnotationString());
        }
        for (String interfaze : interfaces) {
          sb.append(" * ");
          appendAnnotation(sb, "implements", interfaze);
          sb.append("\n");
        }
        sb.append(" * @constructor\n");
      } else if (funType.isStructuralInterface()) {
        sb.append(" * @record\n");
      } else if (funType.isInterface()) {
        sb.append(" * @interface\n");
      }
    } else {
      TypeI thisType = funType.getTypeOfThis();
      if (thisType != null && !thisType.isUnknownType() && !thisType.isVoidType()) {
        if (fnNode == null || !thisType.equals(findMethodOwner(fnNode))) {
          sb.append(" * ");
          appendAnnotation(sb, "this", thisType.toNonNullAnnotationString());
          sb.append("\n");
        }
      }
    }

    Collection<String> typeParams = funType.getTypeParameters();
    if (!typeParams.isEmpty()) {
      sb.append(" * @template ");
      Joiner.on(",").appendTo(sb, typeParams);
      sb.append("\n");
    }

    sb.append(" */\n");
    return sb.toString();
  }

  // TODO(sdh): This whole method could be deleted if we don't mind adding
  // additional @this annotations where they're not actually necessary.
  /**
   * Given a method definition node, returns the {@link ObjectTypeI} corresponding
   * to the class the method is defined on, or null if it is not a prototype method.
   */
  private static ObjectTypeI findMethodOwner(Node n) {
    if (n == null) {
      return null;
    }
    Node parent = n.getParent();
    FunctionTypeI ctor = null;
    if (parent.isAssign()) {
      Node target = parent.getFirstChild();
      if (NodeUtil.isPrototypeProperty(target)) {
        TypeI type = target.getFirstFirstChild().getTypeI();
        ctor = type != null ? type.toMaybeFunctionType() : null;
      }
    } else if (parent.isClass()) {
      // TODO(sdh): test this case once NTI understands ES6 classes
      ctor = parent.getTypeI().toMaybeFunctionType();
    }
    return ctor != null ? ctor.getInstanceType() : null;
  }

  private static void appendAnnotation(StringBuilder sb, String name, String type) {
    sb.append("@").append(name).append(" {").append(type).append("}");
  }

  /** Creates a JSDoc-suitable String representation the type of a parameter. */
  private String getParameterJSDocType(List<TypeI> types, int index, int minArgs, int maxArgs) {
    TypeI type = types.get(index);
    if (index >= minArgs) {
      if (maxArgs < Integer.MAX_VALUE || index < types.size() - 1) {
        return restrictByUndefined(type).toNonNullAnnotationString() + "=";
      }
      return "..." + restrictByUndefined(type).toNonNullAnnotationString();
    }
    return type.toNonNullAnnotationString();
  }

  /** Removes undefined from a union type. */
  private TypeI restrictByUndefined(TypeI type) {
    // If not voidable, there's nothing to do. If not nullable then the easiest
    // thing is to simply remove both null and undefined. If nullable, then add
    // null back into the union after removing null and undefined.
    if (!type.isVoidable()) {
      return type;
    }
    TypeI restricted = type.restrictByNotNullOrUndefined();
    if (!type.isNullable()) {
      return restricted.isBottom() ? type : restricted;
    }
    TypeI nullType = registry.getNativeType(JSTypeNative.NULL_TYPE);
    return registry.createUnionType(ImmutableList.of(restricted, nullType));
  }
}
