/*
 * Copyright 2013 The Closure Compiler Authors.
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

package com.google.javascript.jscomp.newtypes;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author blickly@google.com (Ben Lickly)
 * @author dimvar@google.com (Dimitris Vardoulakis)
 */
public class NominalType {
  final String name;
  // Each instance of the class has these properties by default
  private Map<String, Property> classProps = Maps.newHashMap();
  // The object pointed to by the prototype property of the constructor of this
  // class has these properties
  private Map<String, Property> protoProps = Maps.newHashMap();
  // The constructor of this class has these "static" properties
  private Map<String, Property> ctorProps = Maps.newHashMap();
  boolean isFinalized = false;
  private NominalType superClass = null;
  private ImmutableCollection<NominalType> interfaces = null;
  private final boolean isInterface;
  private ImmutableSet<String> allProps = null;
  private final ImmutableList<String> templateVars;
  // Each class-definition site has a unique id.
  // All classes instantiated from the same polymorphic class have the same id.
  private final int id;

  private NominalType(
      String name, ImmutableList<String> templateVars,
      int id, boolean isInterface) {
    this.name = name;
    this.templateVars =
        (templateVars == null || templateVars.isEmpty()) ? null : templateVars;
    this.id = id;
    this.isInterface = isInterface;
  }

  public static NominalType makeClass(
      String name, ImmutableList<String> templateVars, int id) {
    return new NominalType(name, templateVars, id, false);
  }

  public static NominalType makeInterface(
      String name, ImmutableList<String> templateVars, int id) {
    return new NominalType(name, templateVars, id, true);
  }

  public int getId() {
    return id;
  }

  public boolean isClass() {
    return !isInterface;
  }

  /** True iff we have added all properties and made nominal type immutable */
  public boolean isFinalized() {
    return isFinalized;
  }

  ImmutableList<String> getTemplateVars() {
    return templateVars;
  }

  private boolean hasAncestorClass(NominalType ancestor) {
    Preconditions.checkState(ancestor.isClass());
    if (this.id == ancestor.id) {
      return true;
    } else if (this.superClass == null) {
      return false;
    } else {
      return this.superClass.hasAncestorClass(ancestor);
    }
  }

  /** @return Whether the superclass can be added without creating a cycle. */
  public boolean addSuperClass(NominalType superClass) {
    Preconditions.checkState(!isFinalized);
    Preconditions.checkState(this.superClass == null);
    if (superClass.hasAncestorClass(this)) {
      return false;
    }
    this.superClass = superClass;
    return true;
  }

  private boolean hasAncestorInterface(NominalType ancestor) {
    Preconditions.checkState(ancestor.isInterface);
    if (this.id == ancestor.id) {
      return true;
    } else if (this.interfaces == null) {
      return false;
    } else {
      for (NominalType superInter : interfaces) {
        if (superInter.hasAncestorInterface(ancestor)) {
          return true;
        }
      }
      return false;
    }
  }

  /** @return Whether the interface can be added without creating a cycle. */
  public boolean addInterfaces(ImmutableCollection<NominalType> interfaces) {
    Preconditions.checkState(!isFinalized);
    Preconditions.checkState(this.interfaces == null);
    if (this.isInterface) {
      for (NominalType interf : interfaces) {
        if (interf.hasAncestorInterface(this)) {
          return false;
        }
      }
    }
    this.interfaces = interfaces;
    return true;
  }

  public NominalType getSuperClass() {
    return superClass;
  }

  public ImmutableCollection<NominalType> getInterfaces() {
    return this.interfaces;
  }

  private Property getOwnProp(String pname) {
    Property p = classProps.get(pname);
    if (p != null) {
      return p;
    }
    return protoProps.get(pname);
  }

  Property getPropFromClass(String pname) {
    Preconditions.checkState(!isInterface);
    Property p = getOwnProp(pname);
    if (p != null) {
      return p;
    }
    if (superClass != null) {
      p = superClass.getPropFromClass(pname);
      if (p != null) {
        return p;
      }
    }
    return null;
  }

  Property getPropFromInterface(String pname) {
    Preconditions.checkState(isInterface);
    Property p = protoProps.get(pname);
    if (p != null) {
      return p;
    }
    if (interfaces != null) {
      for (NominalType interf: interfaces) {
        p = interf.getPropFromInterface(pname);
        if (p != null) {
          return p;
        }
      }
    }
    return null;
  }

  Property getProp(String pname) {
    if (isInterface) {
      return getPropFromInterface(pname);
    }
    return getPropFromClass(pname);
  }

  public boolean mayHaveOwnProp(String pname) {
    return getOwnProp(pname) != null;
  }

  public boolean mayHaveProp(String pname) {
    return getProp(pname) != null;
  }

  public JSType getPropDeclaredType(String pname) {
    Property p = getProp(pname);
    if (p == null) {
      return null;
    } else if (p.getDeclaredType() == null && superClass != null) {
      return superClass.getPropDeclaredType(pname);
    }
    return p.getDeclaredType();

  }

  boolean isSubclassOf(NominalType other) {
    if (other.equals(this)) {
      return true;
    } else if (superClass == null) {
      return false;
    } else {
      return superClass.isSubclassOf(other);
    }
  }

  // A special-case of join
  static NominalType pickSuperclass(NominalType c1, NominalType c2) {
    if (c1 == null || c2 == null) {
      return null;
    }
    if (c1.isSubclassOf(c2)) {
      return c2;
    }
    Preconditions.checkState(c2.isSubclassOf(c1));
    return c1;
  }

  // A special-case of meet
  static NominalType pickSubclass(NominalType c1, NominalType c2) {
    if (c1 == null) {
      return c2;
    } else if (c2 == null) {
      return c1;
    }
    if (c1.isSubclassOf(c2)) {
      return c1;
    }
    Preconditions.checkState(c2.isSubclassOf(c1));
    return c2;
  }

  public ImmutableSet<String> getAllPropsOfInterface() {
    Preconditions.checkState(isInterface);
    if (allProps == null) {
      ImmutableSet.Builder<String> builder = ImmutableSet.builder();
      if (interfaces != null) {
        for (NominalType interf: interfaces) {
          builder.addAll(interf.getAllPropsOfInterface());
        }
      }
      allProps = builder.addAll(protoProps.keySet()).build();
    }
    return allProps;
  }

  public ImmutableSet<String> getAllPropsOfClass() {
    Preconditions.checkState(!isInterface);
    if (allProps == null) {
      ImmutableSet.Builder<String> builder = ImmutableSet.builder();
      if (superClass != null) {
        builder.addAll(superClass.getAllPropsOfClass());
      }
      allProps = builder
          .addAll(classProps.keySet()).addAll(protoProps.keySet()).build();
    }
    return allProps;
  }

  //////////// Class Properties

  /** Add a new non-optional declared property to instances of this class */
  public void addClassProperty(String pname, JSType type) {
    classProps.put(pname, new Property(type, type, false));
    // Upgrade any proto props to declared, if present
    if (protoProps.containsKey(pname)) {
      addProtoProperty(pname, type);
    }
  }

  /** Add a new undeclared property to instances of this class */
  public void addUndeclaredClassProperty(String pname) {
    // Only do so if there isn't a declared prop already.
    if (mayHaveProp(pname)) {
      return;
    }
    classProps.put(pname, new Property(JSType.UNKNOWN, null, false));
  }

  //////////// Prototype Properties

  /** Add a new non-optional declared prototype property to this class */
  public void addProtoProperty(String pname, JSType type) {
    if (classProps.containsKey(pname) &&
        classProps.get(pname).getDeclaredType() == null) {
      classProps.remove(pname);
    }
    protoProps.put(pname, new Property(type, type, false));
  }

  /** Add a new undeclared prototype property to this class */
  public void addUndeclaredProtoProperty(String pname) {
    if (!protoProps.containsKey(pname) ||
        protoProps.get(pname).getDeclaredType() == null) {
      protoProps.put(pname, new Property(JSType.UNKNOWN, null, false));
    }
  }

  // Returns the object referred to by the prototype property of the constructor
  // of this class.
  private JSType createProtoObject() {
    return JSType.fromObjectType(ObjectType.makeObjectType(
        superClass, protoProps, null, false));
  }

  //////////// Constructor Properties

  public boolean mayHaveCtorProp(String pname) {
    return ctorProps.containsKey(pname);
  }

  /** Add a new non-optional declared property to this class's constructor */
  public void addCtorProperty(String pname, JSType type) {
    ctorProps.put(pname, new Property(type, type, false));
  }

  /** Add a new undeclared property to this class's constructor */
  public void addUndeclaredCtorProperty(String pname) {
    if (ctorProps.containsKey(pname)) {
      return;
    }
    ctorProps.put(pname, new Property(JSType.UNKNOWN, null, false));
  }

  public JSType getCtorPropDeclaredType(String pname) {
    Property p = ctorProps.get(pname);
    Preconditions.checkState(p != null);
    return p.getDeclaredType();
  }

  // Returns the (function) object referred to by the constructor of this class.
  JSType createConstructorObject(FunctionType ctorFn) {
    return JSType.fromObjectType(
        ObjectType.makeObjectType(null, ctorProps, ctorFn, ctorFn.isLoose()));
  }

  NominalType instantiateGenerics(List<JSType> types) {
    Preconditions.checkState(types.size() == templateVars.size());
    Map<String, JSType> typeMap = Maps.newHashMap();
    for (int i = 0; i < templateVars.size(); i++) {
      typeMap.put(templateVars.get(i), types.get(i));
    }
    return instantiateGenerics(typeMap);
  }

  NominalType instantiateGenerics(Map<String, JSType> typeMap) {
    Preconditions.checkState(templateVars != null);
    for (String typeParam: typeMap.keySet()) {
      Preconditions.checkState(templateVars.contains(typeParam));
    }
    for (String typeParam: templateVars) {
      Preconditions.checkState(typeMap.containsKey(typeParam));
    }
    NominalType result = new NominalType(
        this.name + genericSuffix(typeMap), null, id, this.isInterface);
    // To implement
    Preconditions.checkState(superClass == null);
    Preconditions.checkState(interfaces.isEmpty(),
        "Interfaces not supported: " + interfaces);
    for (String propName : this.classProps.keySet()) {
      result.classProps.put(propName,
          this.classProps.get(propName).substituteGenerics(typeMap));
    }
    for (String propName : this.protoProps.keySet()) {
      result.protoProps.put(propName,
          this.protoProps.get(propName).substituteGenerics(typeMap));
    }
    return result.finalizeNominalType();
  }

  private String genericSuffix(Map<String, JSType> typeMap) {
    List<String> names = Lists.newArrayList();
    for (String templateVar : templateVars) {
      names.add(typeMap.get(templateVar).toString());
    }
    Preconditions.checkState(!names.isEmpty());
    return ".<" + Joiner.on(",").join(names) + ">";
  }

  // If we try to mutate the class after the AST-preparation phase, error.
  public NominalType finalizeNominalType() {
    // System.out.println("Class " + name +
    //     " created with class properties: " + classProps +
    //     " and prototype properties: " + protoProps);
    this.classProps = ImmutableMap.copyOf(classProps);
    this.protoProps = ImmutableMap.copyOf(protoProps);
    if (this.interfaces == null) {
      this.interfaces = ImmutableList.of();
    }
    addCtorProperty("prototype", createProtoObject());
    this.ctorProps = ImmutableMap.copyOf(ctorProps);
    this.isFinalized = true;
    this.allProps = null;
    return this;
  }

  @Override
  public String toString() {
    return name + (templateVars == null ? "" :
        ".<" + Joiner.on(",").join(templateVars) + ">");
  }

  // Consider the following class. The instantiations Foo.<number> and
  // Foo.<string> are identical except the name field, so we must use the name
  // in hashCode and equals. Unlikely to happen in practice because classes have
  // properties.
  //
  // /**
  //  * @template T
  //  * @constructor
  //  * @param {T} x
  //  */
  // function Foo(x) {}

  @Override
  public int hashCode() {
    // Since we want NominalTypes to have a consistent hashCode even in the
    // face of mutation, we limit it to be based only on the immutable
    // fields.
    return Objects.hash(id, name);
  }

  @Override
  public boolean equals(Object o) {
    Preconditions.checkState(o instanceof NominalType);
    NominalType other = (NominalType) o;
    return id == other.id && Objects.equals(name, other.name);
  }
}
