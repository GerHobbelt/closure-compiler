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

import com.google.javascript.jscomp.CompilerOptions.Reach;
import com.google.javascript.jscomp.CompilerOptions.ExtractPrototypeMemberDeclarationsMode;

/**
 * A CompilationLevel represents the level of optimization that should be
 * applied when compiling JavaScript code.
 *
 * @author bolinfest@google.com (Michael Bolin)
 */
public enum CompilationLevel {

  /**
   * WHITESPACE_ONLY removes comments and extra whitespace in the input JS.
   */
  WHITESPACE_ONLY,

  /**
   * SIMPLE_OPTIMIZATIONS performs transformations to the input JS that do not
   * require any changes to JS that depend on the input JS. For example,
   * function arguments are renamed (which should not matter to code that
   * depends on the input JS), but functions themselves are not renamed (which
   * would otherwise require external code to change to use the renamed function
   * names).
   */
  SIMPLE_OPTIMIZATIONS,

  /**
   * ADVANCED_OPTIMIZATIONS aggressively reduces code size by renaming function
   * names and variables, removing code which is never called, etc.
   */
  ADVANCED_OPTIMIZATIONS,

  /**
   * SHUMWAY_OPTIMIZATIONS applies optimizations that are known to work for Shumway.
   */
  SHUMWAY_OPTIMIZATIONS,

  /**
   * J2ME_OPTIMIZATIONS applies optimizations that are known to work for J2ME.js
   */
  J2ME_OPTIMIZATIONS,

  /**
   * J2ME_AOT_OPTIMIZATIONS applies optimizations that are known to work for J2ME.js AOT generated code.
   */
  J2ME_AOT_OPTIMIZATIONS,

  /**
   * FROM_CONFIG_FILE lets you specify you own options
   */
  FROM_CONFIG_FILE
  ;

  public static CompilationLevel fromString(String value) {
    if (value == null) {
      return null;
    }
    switch (value) {
      case "WHITESPACE_ONLY":
      case "WHITESPACE":
        return CompilationLevel.WHITESPACE_ONLY;
      case "SIMPLE_OPTIMIZATIONS":
      case "SIMPLE":
        return CompilationLevel.SIMPLE_OPTIMIZATIONS;
      case "ADVANCED_OPTIMIZATIONS":
      case "ADVANCED":
        return CompilationLevel.ADVANCED_OPTIMIZATIONS;
    }
    return null;
  }

  private CompilationLevel() {}

  public void setOptionsForCompilationLevel(CompilerOptions options) {
    switch (this) {
      case WHITESPACE_ONLY:
        applyBasicCompilationOptions(options);
        break;
      case SIMPLE_OPTIMIZATIONS:
        applySafeCompilationOptions(options);
        break;
      case ADVANCED_OPTIMIZATIONS:
        applyFullCompilationOptions(options);
        break;
      case SHUMWAY_OPTIMIZATIONS:
        applyShumwayCompilationOptions(options);
        break;
      case J2ME_OPTIMIZATIONS:
        applyJ2MECompilationOptions(options, false);
        break;
      case J2ME_AOT_OPTIMIZATIONS:
        applyJ2MECompilationOptions(options, true);
        break;
      case FROM_CONFIG_FILE:
        applyCompilationOptionsFromFile(options);
        break;
      default:
        throw new RuntimeException("Unknown compilation level.");
    }
  }

  public void setDebugOptionsForCompilationLevel(CompilerOptions options) {
    options.setAnonymousFunctionNaming(AnonymousFunctionNamingPolicy.UNMAPPED);
    options.generatePseudoNames = true;
    options.removeClosureAsserts = false;
    // Don't shadow variables as it is too confusing.
    options.shadowVariables = false;
  }

  /**
   * Gets options that only strip whitespace and comments.
   * @param options The CompilerOptions object to set the options on.
   */
  private static void applyBasicCompilationOptions(CompilerOptions options) {
    options.skipAllCompilerPasses();
  }

  /**
   * Add options that are safe. Safe means options that won't break the
   * JavaScript code even if no symbols are exported and no coding convention
   * is used.
   * @param options The CompilerOptions object to set the options on.
   */
  private static void applySafeCompilationOptions(CompilerOptions options) {
    // ReplaceIdGenerators is on by default, but should run in simple mode.
    options.replaceIdGenerators = false;

    // Does not call applyBasicCompilationOptions(options) because the call to
    // skipAllCompilerPasses() cannot be easily undone.
    options.dependencyOptions.setDependencySorting(true);
    options.setClosurePass(true);
    options.setRenamingPolicy(VariableRenamingPolicy.LOCAL, PropertyRenamingPolicy.OFF);
    options.shadowVariables = true;
    options.setInlineVariables(Reach.LOCAL_ONLY);
    options.setFlowSensitiveInlineVariables(true);
    options.setInlineFunctions(Reach.LOCAL_ONLY);
    options.setAssumeClosuresOnlyCaptureReferences(false);
    options.setCheckGlobalThisLevel(CheckLevel.OFF);
    options.setFoldConstants(true);
    options.setCoalesceVariableNames(true);
    options.setDeadAssignmentElimination(true);
    options.setCollapseVariableDeclarations(true);
    options.convertToDottedProperties = true;
    options.labelRenaming = true;
    options.setRemoveDeadCode(true);
    options.setOptimizeArgumentsArray(true);
    options.setRemoveUnusedVariables(Reach.LOCAL_ONLY);
    options.collapseObjectLiterals = true;
    options.protectHiddenSideEffects = true;
  }

  /**
   * Add the options that will work only if the user exported all the symbols
   * correctly.
   * @param options The CompilerOptions object to set the options on.
   */
  private static void applyFullCompilationOptions(CompilerOptions options) {
    // Do not call applySafeCompilationOptions(options) because the call can
    // create possible conflicts between multiple diagnostic groups.

    // All the safe optimizations.
    options.dependencyOptions.setDependencySorting(true);
    options.setClosurePass(true);
    options.setFoldConstants(true);
    options.setCoalesceVariableNames(true);
    options.setDeadAssignmentElimination(true);
    options.setExtractPrototypeMemberDeclarations(true);
    options.setCollapseVariableDeclarations(true);
    options.convertToDottedProperties = true;
    options.labelRenaming = true;
    options.setRemoveDeadCode(true);
    options.setOptimizeArgumentsArray(true);
    options.collapseObjectLiterals = true;
    options.protectHiddenSideEffects = true;

    // All the advanced optimizations.
    options.removeClosureAsserts = true;
    options.removeAbstractMethods = true;
    options.removeSuperMethods = true;
    options.reserveRawExports = true;
    options.setRenamingPolicy(
        VariableRenamingPolicy.ALL, PropertyRenamingPolicy.ALL_UNQUOTED);
    options.shadowVariables = true;
    options.setRemoveUnusedPrototypeProperties(true);
    options.setRemoveUnusedPrototypePropertiesInExterns(false);
    options.setRemoveUnusedClassProperties(true);
    options.setCollapseAnonymousFunctions(true);
    options.setCollapseProperties(true);
    options.setCheckGlobalThisLevel(CheckLevel.WARNING);
    options.setRewriteFunctionExpressions(false);
    options.setSmartNameRemoval(true);
    options.setInlineConstantVars(true);
    options.setInlineFunctions(Reach.ALL);
    options.setAssumeClosuresOnlyCaptureReferences(false);
    options.setInlineVariables(Reach.ALL);
    options.setFlowSensitiveInlineVariables(true);
    options.setComputeFunctionSideEffects(true);

    // Remove unused vars also removes unused functions.
    options.setRemoveUnusedVariables(Reach.ALL);

    // Move code around based on the defined modules.
    options.setCrossModuleCodeMotion(true);
    options.setCrossModuleMethodMotion(true);

    // Call optimizations
    options.setDevirtualizePrototypeMethods(true);
    options.optimizeParameters = true;
    options.optimizeReturns = true;
    options.optimizeCalls = true;
  }

  private static void applyShumwayCompilationOptions(CompilerOptions options) {
    // Do not call applySafeCompilationOptions(options) because the call can
    // create possible conflicts between multiple diagnostic groups.

    // All the safe optimizations.
    options.dependencyOptions.setDependencySorting(true);
    options.closurePass = true;
    options.foldConstants = true;
    options.coalesceVariableNames = true;
    options.deadAssignmentElimination = true;
    options.setExtractPrototypeMemberDeclarations(true);
    options.collapseVariableDeclarations = true;
    options.convertToDottedProperties = true;
    options.labelRenaming = true;
    options.removeDeadCode = true;
    options.optimizeArgumentsArray = true;
    options.collapseObjectLiterals = true;
    options.protectHiddenSideEffects = true;

    // All the advanced optimizations.
    options.removeClosureAsserts = true;
    options.reserveRawExports = true;
    options.setRenamingPolicy(
            VariableRenamingPolicy.LOCAL, PropertyRenamingPolicy.OFF);
    options.shadowVariables = true;
    options.collapseAnonymousFunctions = true;
    options.collapseProperties = true;
    options.checkGlobalThisLevel = CheckLevel.WARNING;
    options.rewriteFunctionExpressions = false;
    options.inlineConstantVars = true;
    options.setInlineFunctions(Reach.ALL);
    options.setAssumeClosuresOnlyCaptureReferences(false);
    options.inlineGetters = true;
    options.setInlineVariables(Reach.ALL);
    options.flowSensitiveInlineVariables = true;
    options.computeFunctionSideEffects = true;

    // Remove unused vars also removes unused functions.
    options.setRemoveUnusedVariables(Reach.LOCAL_ONLY);

    // Move code around based on the defined modules.
    options.crossModuleCodeMotion = true;
    options.crossModuleMethodMotion = true;

    // Call optimizations
    options.devirtualizePrototypeMethods = true;
    options.optimizeParameters = true;
    options.optimizeReturns = true;

    // The following optimizations break Shumway builds and need further investigation:
    //   options.removeUnusedPrototypeProperties = true;
    //   options.removeUnusedPrototypePropertiesInExterns = true;
    //   options.removeUnusedClassProperties = true;
    //   options.smartNameRemoval = true;
    //   options.optimizeCalls = true;
  }

  private static void applyJ2MECompilationOptions(CompilerOptions options, boolean aotMode) {
    // Do not call applySafeCompilationOptions(options) because the call can
    // create possible conflicts between multiple diagnostic groups.

    // All the safe optimizations.
    options.dependencyOptions.setDependencySorting(true);
    options.closurePass = true;
    options.foldConstants = true;
    options.coalesceVariableNames = true;
    options.deadAssignmentElimination = true;
    options.setExtractPrototypeMemberDeclarations(true);
    options.collapseVariableDeclarations = true;
    options.convertToDottedProperties = true;
    options.labelRenaming = true;
    options.removeDeadCode = true;
    options.optimizeArgumentsArray = true;
    options.collapseObjectLiterals = true;
    options.protectHiddenSideEffects = true;

    // All the advanced optimizations.
    options.removeClosureAsserts = true;
    options.reserveRawExports = true;
    // We disable VariableRenamingPolicy for J2ME because we need symbols when profiling release builds.
    options.setRenamingPolicy(
            VariableRenamingPolicy.OFF, PropertyRenamingPolicy.OFF);
    options.shadowVariables = true;
    options.collapseAnonymousFunctions = true;
    options.collapseProperties = true;
    options.checkGlobalThisLevel = CheckLevel.WARNING;
    options.rewriteFunctionExpressions = false;
    options.inlineConstantVars = true;
    options.setInlineFunctions(Reach.ALL);
    options.setAssumeClosuresOnlyCaptureReferences(false);
    options.inlineGetters = true;
    options.setInlineVariables(Reach.ALL);
    options.flowSensitiveInlineVariables = true;
    options.computeFunctionSideEffects = true;

    // Remove unused vars also removes unused functions.
    options.setRemoveUnusedVariables(Reach.LOCAL_ONLY);

    // Move code around based on the defined modules.
    options.crossModuleCodeMotion = true;
    options.crossModuleMethodMotion = true;

    // Call optimizations
    options.devirtualizePrototypeMethods = true;
    options.optimizeParameters = true;
    options.optimizeReturns = true;

    // The following optimizations break Shumway builds and need further investigation:
    //   options.removeUnusedPrototypeProperties = true;
    //   options.removeUnusedPrototypePropertiesInExterns = true;
    //   options.removeUnusedClassProperties = true;
    //   options.smartNameRemoval = true;
    //   options.optimizeCalls = true;
  }

  /**
   * Parse a boolean compiler option.
   * @param options The CompilerOptions object to set the options on.
   * @param prop    The name of the boolean configuration property.
   * @return        A boolean value (true or false) representing the configuration property's desired state.
   */
  private static Boolean getBoolProperty(CompilerOptions options, String prop) {
    return Boolean.parseBoolean(options.inputCompilerOptions.getProperty(prop));
  }

  private static ExtractPrototypeMemberDeclarationsMode getPrototypeMemberDeclarationsMode(CompilerOptions options, String prop) {
    String val = options.inputCompilerOptions.getProperty(prop).toUpperCase();
    if (val == "OFF") {
      return ExtractPrototypeMemberDeclarationsMode.OFF;
    }
    if (val == "USE_GLOBAL_TEMP") {
      return ExtractPrototypeMemberDeclarationsMode.USE_GLOBAL_TEMP;
    }
    if (val == "USE_IIFE") {
      return ExtractPrototypeMemberDeclarationsMode.USE_IIFE;
    }
    throw new RuntimeException("Unknown member declaration mode.");
  }
  
  /**
   * Add the options that will work only if the user exported all the symbols
   * correctly.
   * @param options The CompilerOptions object to set the options on.
   */
  private static void applyCompilationOptionsFromFile(CompilerOptions options) {
    // All the safe optimizations.
    options.dependencyOptions.setDependencySorting(true);
    options.closurePass = getBoolProperty(options, "closurePass");
    options.foldConstants = getBoolProperty(options, "foldConstants");
    options.coalesceVariableNames = getBoolProperty(options, "coalesceVariableNames");
    options.deadAssignmentElimination = getBoolProperty(options, "deadAssignmentElimination");
    options.extractPrototypeMemberDeclarations = getPrototypeMemberDeclarationsMode(options, "extractPrototypeMemberDeclarations");
    options.collapseVariableDeclarations = getBoolProperty(options, "collapseVariableDeclarations");
    options.convertToDottedProperties = getBoolProperty(options, "convertToDottedProperties");
    options.labelRenaming = getBoolProperty(options, "labelRenaming");
    options.removeDeadCode = getBoolProperty(options, "removeDeadCode");
    options.optimizeArgumentsArray = getBoolProperty(options, "optimizeArgumentsArray");
    options.collapseObjectLiterals = getBoolProperty(options, "collapseObjectLiterals");
    options.protectHiddenSideEffects = getBoolProperty(options, "protectHiddenSideEffects");

    // All the advanced optimizations.
    options.removeClosureAsserts = getBoolProperty(options, "removeClosureAsserts");
    options.reserveRawExports = getBoolProperty(options, "reserveRawExports");
    options.setRenamingPolicy(
        VariableRenamingPolicy.ALL, PropertyRenamingPolicy.ALL_UNQUOTED);
    options.shadowVariables = getBoolProperty(options, "shadowVariables");
    options.removeUnusedPrototypeProperties = getBoolProperty(options, "removeUnusedPrototypeProperties");
    options.removeUnusedPrototypePropertiesInExterns = getBoolProperty(options, "removeUnusedPrototypePropertiesInExterns");
    options.removeUnusedClassProperties = getBoolProperty(options, "removeUnusedClassProperties");
    options.collapseAnonymousFunctions = getBoolProperty(options, "collapseAnonymousFunctions");
    options.collapseProperties = getBoolProperty(options, "collapseProperties");
    options.checkGlobalThisLevel = CheckLevel.WARNING;
    options.rewriteFunctionExpressions = getBoolProperty(options, "rewriteFunctionExpressions");
    options.smartNameRemoval = getBoolProperty(options, "smartNameRemoval");
    options.inlineConstantVars = getBoolProperty(options, "inlineConstantVars");
    options.setInlineFunctions(Reach.ALL);
    options.setAssumeClosuresOnlyCaptureReferences(false);
    options.inlineGetters = getBoolProperty(options, "inlineGetters");
    options.setInlineVariables(Reach.ALL);
    options.flowSensitiveInlineVariables = getBoolProperty(options, "flowSensitiveInlineVariables");
    options.computeFunctionSideEffects = getBoolProperty(options, "computeFunctionSideEffects");

    // Remove unused vars also removes unused functions.
    options.setRemoveUnusedVariables(Reach.ALL);

    // Move code around based on the defined modules.
    options.crossModuleCodeMotion = getBoolProperty(options, "crossModuleCodeMotion");
    options.crossModuleMethodMotion = getBoolProperty(options, "crossModuleMethodMotion");

    // Call optimizations
    options.devirtualizePrototypeMethods = getBoolProperty(options, "devirtualizePrototypeMethods");
    options.optimizeParameters = getBoolProperty(options, "optimizeParameters");
    options.optimizeReturns = getBoolProperty(options, "optimizeReturns");
    options.optimizeCalls = getBoolProperty(options, "optimizeCalls");
  }

  /**
   * Enable additional optimizations that use type information. Only has
   * an effect for ADVANCED_OPTIMIZATIONS; this is a no-op for other modes.
   * @param options The CompilerOptions object to set the options on.
   */
  public void setTypeBasedOptimizationOptions(CompilerOptions options) {
    switch (this) {
      case ADVANCED_OPTIMIZATIONS:
        options.inferTypes = true;
        options.setDisambiguateProperties(true);
        options.setAmbiguateProperties(true);
        options.setInlineProperties(true);
        options.setUseTypesForLocalOptimization(true);
        break;
      case SIMPLE_OPTIMIZATIONS:
      case WHITESPACE_ONLY:
        break;
    }
  }

  /**
   * Enable additional optimizations that operate on global declarations. Advanced mode does
   * this by default, but this isn't valid in simple mode in the general case and should only
   * be enabled when code is self contained (such as when it is enclosed by a function wrapper.
   *
   * @param options The CompilerOptions object to set the options on.
   */
  public void setWrappedOutputOptimizations(CompilerOptions options) {
    // Global variables and properties names can't conflict.
    options.reserveRawExports = false;
    switch (this) {
      case SIMPLE_OPTIMIZATIONS:
        // Enable global variable optimizations (but not property optimizations)
        options.setVariableRenaming(VariableRenamingPolicy.ALL);
        options.setCollapseAnonymousFunctions(true);
        options.setInlineConstantVars(true);
        options.setInlineFunctions(Reach.ALL);
        options.setInlineVariables(Reach.ALL);
        options.setRemoveUnusedVariables(Reach.ALL);
        break;
      case ADVANCED_OPTIMIZATIONS:
      case WHITESPACE_ONLY:
        break;
    }
  }
}
