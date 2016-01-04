# Understanding the Restrictions Imposed by the Closure Compiler

The Closure Compiler expects its JavaScript input to conform to a few restrictions. The higher the
level of optimization you ask the Compiler to perform, the more restrictions the Compiler places on
the input JavaScript.

This document describes the main restrictions for each level of optimization. See also [this
wiki page](https://github.com/google/closure-compiler/wiki/Compiler-Assumptions) for additional
assumptions made by the compiler.

## Restrictions for all optimization levels

The Compiler places the following two restrictions on all JavaScript that it processes, for all
optimization levels:

 * **The Compiler only recognizes Ecmascript 262.**

    Ecmascript revision 3 is the basis of Javascript 1.5. When people use the term "JavaScript"
    they usually mean this version of JavaScript. However the compiler also supports Ecmascript
    revision 5 and support for Ecmascript revision 6 is under development. The compiler only
    supports official language features.

    Browser-specific features that conform to the appropriate Ecmascript language specification
    will work fine with the compiler. For example, ActiveX objects are created with legal JavaScript
    syntax, so code that creates ActiveX objects works with the compiler.

    The compiler maintainers actively work to support new language versions and their features.
    Projects can specify which Ecmascript language version they intend by using the `--language_in`
    flag.

 * **The Compiler does not preserve comments.**

    All Compiler optimization levels remove comments, so code that relies on specially formatted
    comments does not work with the Compiler.

    For example, because the Compiler does not preserve comments, you cannot use JScript's
    "conditional comments" directly. You can, however, work around this restriction by wrapping
    conditional comments in `eval()` expressions. The Compiler can process the following code
    without generating an error:

    ```js
    x = eval("/*@cc_on 2+@*/ 0");
    ```
   
    *Note: You can include open source licenses and other important text at the top of the Compiler
    output using the `@preserve` annotation.*
   
## Restrictions for SIMPLE_OPTIMIZATIONS

The Simple optimization level renames function parameters, local variables, and locally defined
functions to reduce code size. However, some JavaScript constructs can break this renaming process.

Avoid the following constructs and practices when using `SIMPLE_OPTIMIZATIONS`:

 * **`with`:**

    When you use with the Compiler can't distinguish between a local variable and an object property
    of the same name, and so it renames all instances of the name.

    Furthermore, the `with` statement makes your code harder for humans to read. The with statement
    changes the normal rules for name resolution, and can make it difficult even for the programmer
    who wrote the code to identify what a name refers to.

 * **`eval()`:**

    The Compiler doesn't parse the string argument of eval(), and so it won't rename any symbols
    within this argument.
   
 * **String representations of function or parameter names:**

    The Compiler renames functions and function parameters but does not change any strings in your
    code that refer to functions or parameters by name. You should thus avoid representing function
    or parameter names as strings in your code. For example, the Prototype library function
    `argumentNames()` uses `Function.toString()` to retrieve the names of a function's parameters.
    But while `argumentNames()` might tempt you to use the names of arguments in your code, Simple
    mode compilation breaks this kind of reference.
   
## Restrictions for ADVANCED_OPTIMIZATIONS

The `ADVANCED_OPTIMIZATIONS` compilation level performs the same transformations as
`SIMPLE_OPTIMIZATIONS`, and also adds global renaming of properties, variables, and functions,
dead code elimination, and property flattening. These new passes place additional restrictions on
the input JavaScript.

### Implications of global variable, function, and property renaming:

The global renaming of `ADVANCED_OPTIMIZATIONS` makes the following practices dangerous:

 * **Undeclared external references:**

    In order to rename global variables, functions, and properties correctly, the Compiler must know
    about all references to those globals. You must tell the Compiler about symbols that are defined
    outside of the code being compiled. Advanced Compilation and Externs describes how to declare
    external symbols.
   
 * **Using unexported internal names in external code:**

    Compiled code must export any symbols that uncompiled code refers to. Advanced Compilation and
    Externs describes how to export symbols.
   
 * **Using string names to refer to object properties:**

    The Compiler renames properties in Advanced mode, but it never renames strings.
    
    ```js
    var x = { renamed_property: 1 };
    var y = x.renamed_property; // This is OK.
    
    // 'renamed_property' below doesn't exist on x after renaming, so the
    //  following evaluates to false.
    if ( 'renamed_property' in x ) {}; // BAD
    
    // The following also fails:
    x['renamed_property']; // BAD
    ```
    
    If you need to refer to a property with a quoted string, always use a quoted string:

    ```js
    var x = { 'unrenamed_property': 1 };
    x['unrenamed_property'];  // This is OK.
    if ( 'unrenamed_property' in x ) {};   // This is OK
    ```
   
 * **Referring to variables as properties of the global object:**

    The Compiler renames properties and variables independently. For example, the Compiler treats
    the following two references to foo differently, even though they are equivalent:

    ```js
    var foo = {};
    window.foo; // BAD
    ```

    This code might compile to:

    ```js
    var a = {};
    window.b;
    ```
   
    If you need to refer to a variable as a property of the global object, always refer to it that
    way:

    ```
    window.foo = {}
    window.foo;
    ```
   
### Implications of dead code elimination

The `ADVANCED_OPTIMIZATIONS` compilation level removes code that is never executed. This
elimination of dead code makes the following practices dangerous:

 * **Calling functions from outside of compiled code:**

    When you compile functions without compiling the code that calls those functions, the Compiler
    assumes that the functions are never called and removes them. To avoid unwanted code removal,
    either:

     + compile all the JavaScript for your application together, or
     + export compiled functions.

    Advanced Compilation and Externs describes both of these approaches in greater detail.

 * **Retrieving functions through iteration over constructor or prototype properties:**

    To determine whether a function is dead code, the Compiler has to find all the calls to that
    function. By iterating over the properties of a constructor or its prototype you can find and
    call methods, but the Compiler can't identify the specific functions called in this manner.

    For example, the following code causes unintended code removal:
   
    ```js
    function Coordinate() {}
  
    Coordinate.prototype.initX = function() {
      this.x = 0;
    };
  
    Coordinate.prototype.initY = function() {
      this.y = 0;
    };
  
    var coord = new Coordinate();
  
    for (method in Coordinate.prototype) {
      Coordinate.prototype[method].call(coord); // BAD
    }
    ```
    
    The Compiler does not understand that `initX()` and `initY()` are called in for loop, and so it
    removes both of these methods.

    Note that if you pass a function as a parameter, the Compiler can find calls to that parameter.
    For example, the Compiler does not remove the `getHello()` function when it compiles the
    following code in Advanced mode.

    ```js
    function alertF(f) {
      alert(f());
    }
   
    function getHello() {
      return 'hello';
    }
   
    // The Compiler figures out that this call to alertF also calls getHello().
    alertF(getHello); // This is OK.
    ```
    
### Implications of object property flattening

In Advanced mode the Compiler collapses object properties to prepare for name shortening. For
example, the Compiler transforms this:

```js
var foo = {};
foo.bar = function (a) { alert(a) };
foo.bar("hello");
```

into this:

```js
var foo$bar = function (a) { alert(a) };
foo$bar("hello");
```

This property flattening allows the later renaming pass to rename more efficiently. The Compiler
can replace foo$bar with a single character, for example.

But property flattening also makes the following practice dangerous:

 * **Using `this` outside of constructors and prototype methods:**

    Property flattening can change meaning of the keyword this within a function. For example:

    ```js
    var foo = {};
    foo.bar = function (a) { this.bad = a; }; // BAD
    foo.bar("hello");
    ```

    becomes:

    ```js
    var foo$bar = function (a) { this.bad = a; };
    foo$bar("hello");
    ```
   
    Before the transformation, the `this` within `foo.bar` refers to `foo`. After the
    transformation, `this` refers to the global `this`. In cases like this one the Compiler
    produces this warning:

    ```
    WARNING - dangerous use of this in static method foo.bar
    ```
   
    To prevent property flattening from breaking your references to `this`, only use `this` within
    constructors and prototype methods. The meaning of `this` is unambiguous when you call a
    constructor with the new keyword, or within a function that is a property of a prototype.
