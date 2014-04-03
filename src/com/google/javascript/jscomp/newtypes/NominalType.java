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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author blickly@google.com (Ben Lickly)
 * @author dimvar@google.com (Dimitris Vardoulakis)
 */
public class NominalType {
  // In the case of a generic type (rawType.typeParameters non-empty) either:
  // a) typeMap is empty, this is an uninstantiated generic type (Foo.<T>), or
  // b) typeMap's keys exactly correspond to the type parameters of rawType;
  //    this represents a completely instantiated generic type (Foo.<number>).
  private final ImmutableMap<String, JSType> typeMap;
  private final RawNominalType rawType;

  private NominalType(
      ImmutableMap<String, JSType> typeMap, RawNominalType rawType) {
    Preconditions.checkState(typeMap.isEmpty() ||
        typeMap.keySet().containsAll(rawType.typeParameters) &&
        rawType.typeParameters.containsAll(typeMap.keySet()));
    this.typeMap = typeMap;
    this.rawType = rawType;
  }

  public static NominalType fromRaw(RawNominalType rawType) {
    return new NominalType(ImmutableMap.<String, JSType>of(), rawType);
  }

  // This should only be called during GlobalTypeInfo
  public RawNominalType getRawNominalType() {
    Preconditions.checkState(typeMap.isEmpty());
    return rawType;
  }

  NominalType instantiateGenerics(List<JSType> types) {
    Preconditions.checkState(types.size() == rawType.typeParameters.size());
    Map<String, JSType> typeMap = Maps.newHashMap();
    for (int i = 0; i < rawType.typeParameters.size(); i++) {
      typeMap.put(rawType.typeParameters.get(i), types.get(i));
    }
    return instantiateGenerics(typeMap);
  }

  NominalType instantiateGenerics(Map<String, JSType> newTypeMap) {
    ImmutableMap.Builder<String, JSType> builder = ImmutableMap.builder();
    if (!typeMap.isEmpty()) {
      for (String oldKey : typeMap.keySet()) {
        builder.put(oldKey, typeMap.get(oldKey).substituteGenerics(newTypeMap));
      }
    } else {
      for (String newKey : newTypeMap.keySet()) {
        if (rawType.typeParameters.contains(newKey)) {
          builder.put(newKey, newTypeMap.get(newKey));
        }
      }
    }
    return new NominalType(builder.build(), this.rawType);
  }

  // Methods that delegate to RawNominalType
  public String getName() {
    return rawType.name;
  }

  public int getId() {
    return rawType.getId();
  }

  public boolean isClass() {
    return rawType.isClass();
  }

  public boolean isInterface() {
    return !rawType.isClass();
  }

  /** True iff it has all properties and the RawNominalType is immutable */
  public boolean isFinalized() {
    return rawType.isFinalized;
  }

  public ImmutableSet<String> getAllPropsOfInterface() {
    return rawType.getAllPropsOfInterface();
  }

  public ImmutableSet<String> getAllPropsOfClass() {
    return rawType.getAllPropsOfClass();
  }

  public NominalType getInstantiatedSuperclass() {
    Preconditions.checkState(rawType.isFinalized);
    if (rawType.superClass == null) {
      return null;
    }
    return rawType.superClass.instantiateGenerics(typeMap);
  }

  public ImmutableSet<NominalType> getInstantiatedInterfaces() {
    Preconditions.checkState(rawType.isFinalized);
    ImmutableSet.Builder<NominalType> result = ImmutableSet.builder();
    for (NominalType interf : rawType.interfaces) {
      result.add(interf.instantiateGenerics(typeMap));
    }
    return result.build();
  }


  Property getProp(String pname) {
    Property p = rawType.getProp(pname);
    return p == null ? null : p.substituteGenerics(typeMap);
  }

  public JSType getPropDeclaredType(String pname) {
    JSType type = rawType.getPropDeclaredType(pname);
    return type == null ? null : type.substituteGenerics(typeMap);
  }

  JSType createConstructorObject(FunctionType ctorFn) {
    Preconditions.checkState(typeMap.isEmpty());
    return rawType.createConstructorObject(ctorFn);
  }

  boolean isSubclassOf(NominalType other) {
    if (rawType.equals(other.rawType)) {
      for (String typeVar :rawType.getTypeParameters()) {
        Preconditions.checkState(typeMap.containsKey(typeVar),
            "Type variable %s not in the domain: %s",
            typeVar, typeMap.keySet());
        Preconditions.checkState(other.typeMap.containsKey(typeVar));
        if (!typeMap.get(typeVar).isSubtypeOf(other.typeMap.get(typeVar))) {
          return false;
        }
      }
      return true;
    } else if (rawType.superClass == null) {
      return false;
    } else {
      return rawType.superClass.instantiateGenerics(typeMap)
          .isSubclassOf(other);
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

  boolean unifyWith(NominalType other, List<String> typeParameters,
      Multimap<String, JSType> typeMultimap) {
    if (this.rawType != other.rawType) {
      return false;
    }
    if (this.rawType.typeParameters.isEmpty()) {
      // Non-generic nominal types don't contribute to the unification.
      return true;
    }
    // Both nominal types must already be instantiated when unifyWith is called.
    Preconditions.checkState(!typeMap.isEmpty());
    Preconditions.checkState(!other.typeMap.isEmpty());
    boolean hasUnified = true;
    for (String typeParam: rawType.typeParameters) {
      hasUnified = hasUnified && typeMap.get(typeParam).unifyWith(
          other.typeMap.get(typeParam), typeParameters, typeMultimap);
    }
    return hasUnified;
  }

  @Override
  public String toString() {
    return rawType.name + rawType.genericSuffix(typeMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(typeMap, rawType);
  }

  @Override
  public boolean equals(Object other) {
    if (other == null) {
      return false;
    }
    Preconditions.checkState(other instanceof NominalType);
    NominalType o = (NominalType) other;
    return Objects.equals(typeMap, o.typeMap) && rawType.equals(o.rawType);
  }

  /**
   * Represents a class or interface as defined in the code.
   * If the raw nominal type has an @template, then many nominal types can be
   * created from it by instantiation.
   */
  public static class RawNominalType {
    private final String name;
    // Each instance of the class has these properties by default
    private Map<String, Property> classProps = Maps.newHashMap();
    // The object pointed to by the prototype property of the constructor of
    // this class has these properties
    private Map<String, Property> protoProps = Maps.newHashMap();
    // The constructor of this class has these "static" properties
    private Map<String, Property> ctorProps = Maps.newHashMap();
    boolean isFinalized = false;
    private NominalType superClass = null;
    private ImmutableSet<NominalType> interfaces = null;
    private final boolean isInterface;
    private ImmutableSet<String> allProps = null;
    // Empty iff this type is not generic
    private final ImmutableList<String> typeParameters;

    private RawNominalType(String name, ImmutableList<String> typeParameters,
        boolean isInterface) {
      if (typeParameters == null) {
        typeParameters = ImmutableList.of();
      }
      this.name = name;
      this.typeParameters = typeParameters;
      this.isInterface = isInterface;
    }

    public static RawNominalType makeClass(
        String name, ImmutableList<String> typeParameters) {
      return new RawNominalType(name, typeParameters, false);
    }

    public static RawNominalType makeInterface(
        String name, ImmutableList<String> typeParameters) {
      return new RawNominalType(name, typeParameters, true);
    }

    public int getId() {
      return hashCode();
    }

    public String getName() {
      return name;
    }

    public boolean isClass() {
      return !isInterface;
    }

    boolean isGeneric() {
      return !typeParameters.isEmpty();
    }

    ImmutableList<String> getTypeParameters() {
      return typeParameters;
    }

    private boolean hasAncestorClass(RawNominalType ancestor) {
      Preconditions.checkState(ancestor.isClass());
      if (this == ancestor) {
        return true;
      } else if (this.superClass == null) {
        return false;
      } else {
        return this.superClass.rawType.hasAncestorClass(ancestor);
      }
    }

    /** @return Whether the superclass can be added without creating a cycle. */
    public boolean addSuperClass(NominalType superClass) {
      Preconditions.checkState(!isFinalized);
      Preconditions.checkState(this.superClass == null);
      if (superClass.rawType.hasAncestorClass(this)) {
        return false;
      }
      this.superClass = superClass;
      return true;
    }

    private boolean hasAncestorInterface(RawNominalType ancestor) {
      Preconditions.checkState(ancestor.isInterface);
      if (this == ancestor) {
        return true;
      } else if (this.interfaces == null) {
        return false;
      } else {
        for (NominalType superInter : interfaces) {
          if (superInter.rawType.hasAncestorInterface(ancestor)) {
            return true;
          }
        }
        return false;
      }
    }

    /** @return Whether the interface can be added without creating a cycle. */
    public boolean addInterfaces(ImmutableSet<NominalType> interfaces) {
      Preconditions.checkState(!isFinalized);
      Preconditions.checkState(this.interfaces == null);
      Preconditions.checkNotNull(interfaces);
      if (this.isInterface) {
        for (NominalType interf : interfaces) {
          if (interf.rawType.hasAncestorInterface(this)) {
            this.interfaces = ImmutableSet.of();
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

    public ImmutableSet<NominalType> getInterfaces() {
      return this.interfaces;
    }

    private Property getOwnProp(String pname) {
      Property p = classProps.get(pname);
      if (p != null) {
        return p;
      }
      return protoProps.get(pname);
    }

    private Property getPropFromClass(String pname) {
      Preconditions.checkState(!isInterface);
      Property p = getOwnProp(pname);
      if (p != null) {
        return p;
      }
      if (superClass != null) {
        p = superClass.getProp(pname);
        if (p != null) {
          return p;
        }
      }
      return null;
    }

    private Property getPropFromInterface(String pname) {
      Preconditions.checkState(isInterface);
      Property p = protoProps.get(pname);
      if (p != null) {
        return p;
      }
      if (interfaces != null) {
        for (NominalType interf: interfaces) {
          p = interf.getProp(pname);
          if (p != null) {
            return p;
          }
        }
      }
      return null;
    }

    private Property getProp(String pname) {
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

    public Set<String> getAllOwnProps() {
      Set<String> ownProps = Sets.newHashSet();
      ownProps.addAll(classProps.keySet());
      ownProps.addAll(protoProps.keySet());
      return ownProps;
    }

    private ImmutableSet<String> getAllPropsOfInterface() {
      Preconditions.checkState(isInterface);
      Preconditions.checkState(isFinalized);
      if (allProps == null) {
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        if (interfaces != null) {
          for (NominalType interf: interfaces) {
            builder.addAll(interf.rawType.getAllPropsOfInterface());
          }
        }
        allProps = builder.addAll(protoProps.keySet()).build();
      }
      return allProps;
    }

    private ImmutableSet<String> getAllPropsOfClass() {
      Preconditions.checkState(!isInterface);
      Preconditions.checkState(isFinalized);
      if (allProps == null) {
        ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        if (superClass != null) {
          Preconditions.checkState(superClass.typeMap.isEmpty());
          builder.addAll(superClass.rawType.getAllPropsOfClass());
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

    // Returns the object referred to by the prototype property of the
    // constructor of this class.
    private JSType createProtoObject() {
      return JSType.fromObjectType(ObjectType.makeObjectType(
          superClass, protoProps, null, false));
    }

    //////////// Constructor Properties

    public boolean hasCtorProp(String pname) {
      Property prop = ctorProps.get(pname);
      if (prop == null) {
        return false;
      }
      Preconditions.checkState(!prop.isOptional());
      return true;
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

    // Returns the (function) object referred to by the constructor of this
    // class.
    private JSType createConstructorObject(FunctionType ctorFn) {
      return JSType.fromObjectType(
          ObjectType.makeObjectType(null, ctorProps, ctorFn, ctorFn.isLoose()));
    }

    private String genericSuffix(Map<String, JSType> typeMap) {
      Preconditions.checkState(typeMap.isEmpty() ||
          typeMap.keySet().containsAll(typeParameters));
      if (typeParameters.isEmpty()) {
        return "";
      }
      List<String> names = Lists.newArrayList();
      for (String typeParam : typeParameters) {
        JSType concrete = typeMap.get(typeParam);
        names.add(concrete == null ? typeParam : concrete.toString());
      }
      return ".<" + Joiner.on(",").join(names) + ">";
    }

    // If we try to mutate the class after the AST-preparation phase, error.
    public RawNominalType finalizeNominalType() {
      // System.out.println("Class " + name +
      //     " created with class properties: " + classProps +
      //     " and prototype properties: " + protoProps);
      this.classProps = ImmutableMap.copyOf(classProps);
      this.protoProps = ImmutableMap.copyOf(protoProps);
      if (this.interfaces == null) {
        this.interfaces = ImmutableSet.of();
      }
      addCtorProperty("prototype", createProtoObject());
      this.ctorProps = ImmutableMap.copyOf(ctorProps);
      this.isFinalized = true;
      return this;
    }

    @Override
    public String toString() {
      return name + genericSuffix(ImmutableMap.<String, JSType>of());
    }

    // equals and hashCode default to reference equality, which is what we want
  }
}
