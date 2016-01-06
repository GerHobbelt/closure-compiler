# Advanced Compilation and Externs

## Overview

Using the Closure Compiler with a compilation_level of `ADVANCED_OPTIMIZATIONS` offers better
compression rates than compilation with `SIMPLE_OPTIMIZATIONS` or `WHITESPACE_ONLY`. Compilation
with `ADVANCED_OPTIMIZATIONS` achieves extra compression by being more aggressive in the ways that
it transforms code and renames symbols. However, this more aggressive approach means that you must
take greater care when you use `ADVANCED_OPTIMIZATIONS` to ensure that the output code works the
same way as the input code.

This tutorial illustrates what the `ADVANCED_OPTIMIZATIONS` compilation level does and what you
can do to make sure your code works after compilation with `ADVANCED_OPTIMIZATIONS`. It also
introduces the concept of the extern: a symbol that is defined in code external to the code
processed by the compiler.

Before reading this tutorial you should be familiar with the process of compiling JavaScript with
one of the Closure Compiler tools (the compiler service UI, the compiler service API, or the
compiler application).

## Even Better Compression

With the default compilation level of `SIMPLE_OPTIMIZATIONS`, the Closure Compiler makes JavaScript
smaller by renaming local variables. There are symbols other than local variables that can be
shortened, however, and there are ways to shrink code other than renaming symbols. Compilation
with `ADVANCED_OPTIMIZATIONS` exploits the full range of code-shrinking possibilities.

Compare the outputs for `SIMPLE_OPTIMIZATIONS` and `ADVANCED_OPTIMIZATIONS` for the following code:

```js
function unusedFunction(note) {
  alert(note['text']);
}

function displayNoteTitle(note) {
  alert(note['title']);
}

var flowerNote = {};
flowerNote['title'] = "Flowers";
displayNoteTitle(flowerNote);
```

Compilation with `SIMPLE_OPTIMIZATIONS` shortens the code to this:

```js
function unusedFunction(a){alert(a.text)}function displayNoteTitle(a){alert(a.title)}var flowerNote={};flowerNote.title="Flowers";displayNoteTitle(flowerNote);
```

Compilation with `ADVANCED_OPTIMIZATIONS` shortens the code even further to this:

```js
var a={};a.title="Flowers";alert(a.title);
```

Both of these scripts produce an alert reading "Flowers", but the second script is much smaller.

The `ADVANCED_OPTIMIZATIONS` level goes beyond simple shortening of variable names in several ways,
including:

 * *more aggressive renaming:*
    
    Compilation with `SIMPLE_OPTIMIZATIONS` only renames the note parameters of the
    `displayNoteTitle()` and `unusedFunction()` functions, because these are the only variables in
    the script that are local to a function. `ADVANCED_OPTIMIZATIONS` also renames the global
    variable `flowerNote`.

 * *dead code removal:*

    Compilation with `ADVANCED_OPTIMIZATIONS` removes the function `unusedFunction()` entirely,
    because it is never called in the code.

 * *function inlining:*
 
    Compilation with `ADVANCED_OPTIMIZATIONS` replaces the call to `displayNoteTitle()` with the
    single `alert()` that composes the function's body. This replacement of a function call with
    the function's body is known as "inlining". If the function were longer or more complicated,
    inlining it might change the behavior of the code, but the Closure Compiler determines that in
    this case inlining is safe and saves space. Compilation with `ADVANCED_OPTIMIZATIONS` also
    inlines constants and some variables when it determines that it can do so safely.

This list is just a sample of the size-reducing transformations that `ADVANCED_OPTIMIZATIONS`
compilation can perform.

## How to Enable `ADVANCED_OPTIMIZATIONS`

The Closure Compiler service UI, service API, and application all have different methods for
setting the compilation_level to `ADVANCED_OPTIMIZATIONS`.

### How to Enable `ADVANCED_OPTIMIZATIONS` in the Closure Compiler service UI

To enable `ADVANCED_OPTIMIZATIONS` for the Closure Compiler service UI, click the "Advanced"
radio button.

### How to Enable `ADVANCED_OPTIMIZATIONS` in the Closure Compiler service API

To enable `ADVANCED_OPTIMIZATIONS` for the Closure Compiler service API, include a request
parameter named compilation_level with a value of `ADVANCED_OPTIMIZATIONS`, as in the following
python program:

```python
#!/usr/bin/python2.4

import httplib, urllib, sys

params = urllib.urlencode([
    ('code_url', sys.argv[1]),
    ('compilation_level', '`ADVANCED_OPTIMIZATIONS`'),
    ('language', 'ECMASCRIPT5'),
    ('output_format', 'text'),
    ('output_info', 'compiled_code'),
  ])

headers = { "Content-type": "application/x-www-form-urlencoded" }
conn = httplib.HTTPConnection('closure-compiler.appspot.com')
conn.request('POST', '/compile', params, headers)
response = conn.getresponse()
data = response.read()
print data
conn.close()
```

### How to Enable `ADVANCED_OPTIMIZATIONS` in the Closure Compiler application

To enable `ADVANCED_OPTIMIZATIONS` for the Closure Compiler application, include the command line
flag `--compilation_level ADVANCED_OPTIMIZATIONS`, as in the following command:

```
java -jar compiler.jar --compilation_level `ADVANCED_OPTIMIZATIONS` --js hello.js
```

## What to Watch Out for When Using `ADVANCED_OPTIMIZATIONS`

Below are listed some common unintended effects of `ADVANCED_OPTIMIZATIONS`, and steps you can
take to avoid them.

### Removal of Code You Want to Keep

If you compile just the function below with `ADVANCED_OPTIMIZATIONS`, Closure Compiler produces
empty output:

```js
function displayNoteTitle(note) {
  alert(note['myTitle']);
}
```

Because the function is never called in the JavaScript that you pass to the compiler, Closure
Compiler assumes that this code is not needed!

In many cases this behavior is exactly what you want. For example, if you compile your code
together with a large library, Closure Compiler can determine which functions from that library
you actually use and discard the ones that you don't use.

If, however, you find that Closure Compiler is removing functions you want to keep, there are two
ways to prevent this:

 * Move your function calls into the code processed by Closure Compiler.
 * Export the symbols you want to keep.

The next sections discuss each option in more detail.

#### Solution: Move Your Function Calls into the Code Processed by the Closure Compiler

You may encounter unwanted code removal if you only compile part of your code with Closure Compiler.
For example, you might have a library file that contains only function definitions, and an HTML
file that includes the library and that contains the code that calls those functions. In this case,
if you compile the library file with `ADVANCED_OPTIMIZATIONS`, Closure Compiler removes all of your
library functions.

The simplest solution to this problem is to compile your functions together with the portion of
your program that calls those functions. For example, Closure Compiler will not remove
`displayNoteTitle()` when it compiles the following program:

```js
function displayNoteTitle(note) {
  alert(note['myTitle']);
}
displayNoteTitle({'myTitle': 'Flowers'});
```

The displayNoteTitle() function isn't removed in this case because Closure Compiler sees that it is
called.

In other words, you can prevent unwanted code removal by including your program's entry point in
the code that you pass to Closure Compiler. The entry point of a program is the place in the code
where the program begins executing. For example, in the flower note program from the previous
section, the last three lines are executed as soon as the JavaScript is loaded in the browser.
This is the entry point for this program. To determine what code you need to keep, Closure Compiler
starts at this entry point and traces the control flow of the program forward from there.

#### Solution: Export the Symbols You Want to Keep

What if you want to compress only a set of function definitions to create a small, reusable library?
How do you prevent Closure Compiler from removing all the functions?

The best solution in this case is to export your functions, as the following example illustrates:

```js
function displayNoteTitle(note) {
  alert(note['myTitle']);
}
// Store the function in a global property referenced by a string:
window['displayNoteTitle'] = displayNoteTitle;
```

Compilation with `ADVANCED_OPTIMIZATIONS` compresses this to:

```js
function a(b){alert(b.myTitle)}window.displayNoteTitle=a;
```

Note that the compressed code still contains a function with a single alert statement. This
function is the old `displayNoteTitle()` function, renamed to `a()`. The function can still be
called by its old name, however, because its old name has been added as a property of the global
object. For example, if code external to this compiled code makes the following call, the call
still correctly produces an alert:

```js
displayNoteTitle({'myTitle': 'Flowers'});
```

This call works because the `window.displayNoteTitle` property now has a function as its value,
just as the native `window.alert` property has the `alert()` function as its value.

Furthermore, Closure Compiler does not remove the definition of the function (now renamed to `a()`),
because Closure Compiler sees that the function is used in code that gets executed. It's used in
the assignment statement `window['displayNoteTitle'] = displayNoteTitle;`

You can export constructors and prototype properties in the same way. For example:

```js
MyClass = function(name) {
  this.myName = name;
};
MyClass.prototype.myMethod = function() {
  alert(this.myName);
};
window['MyClass'] = MyClass; // <-- Constructor
MyClass.prototype['myMethod'] = MyClass.prototype.myMethod;
```

If putting together these export statements seems too tedious, you can use a function to do the
exporting for you, using the Closure Library functions `goog.exportSymbol()` and
`goog.exportProperty()`.

### Inconsistent Property Names

Closure Compiler compilation never changes string literals in your code, no matter what compilation
level you use. This means that compilation with `ADVANCED_OPTIMIZATIONS` treats properties
differently depending on whether your code accesses them with a string. If you mix string references
to a property with dot-syntax references, Closure Compiler renames some of the references to that
property but not others. As a result, your code will probably not run correctly.

For example, take the following code:

```js
function displayNoteTitle(note) {
  alert(note['myTitle']);
}
var flowerNote = {};
flowerNote.myTitle = 'Flowers';

alert(flowerNote.myTitle);
displayNoteTitle(flowerNote);
```

The last two statements in this source code do exactly the same thing. However, when you compress
the code with `ADVANCED_OPTIMIZATIONS`, you get this:

```js
var a={};a.a="Flowers";alert(a.a);alert(a.myTitle);
```

The last statement in the compressed code produces an error. The direct reference to the `myTitle`
property has been renamed to `a`, but the quoted reference to `myTitle` within the
`displayNoteTitle` function has not been renamed. As a result, the last statement refers to a
`myTitle` property that is no longer there.

#### Solution: Be Consistent in Your Property Names

This solution is pretty simple. Whenever possible, use dot-syntax property names rather than quoted
strings. Use quoted string property names only when you don't want Closure Compiler to rename a
property at all. For example, to export a property you must use a quoted string. However, for
properties used only within your compiled code, use dot syntax.

### Compiling Two Portions of Code Separately

If you split your application into different modules of code, you might want to compile the modules
separately. However, if two chunks of code interact at all, you may have trouble compiling them
separately. Even if you succeed, the output of the two Closure Compiler runs will not be compatible.

For example, assume that an application is divided into two parts: a part that retrieves data, and
a part that displays data.

Here's the code for retrieving the data:

```js
function getData() {
  // In an actual project, this data would be retrieved from the server.
  return {title: 'Flower Care', text: 'Flowers need water.'};
}
```

Here's code for displaying the data:

```js
var displayElement = document.getElementById('display');
function displayData(parent, data) {
  var textElement = document.createTextNode(data.text);
  parent.appendChild(textElement);
}
displayData(displayElement, getData());
```

If you try to compile these two chunks of code separately, you will encounter several problems.
First, the Closure Compiler removes the `getData()` function, for the reasons described in Removal
of Code You Want to Keep. Second, the Closure Compiler produces a fatal error when processing the
code that displays the data.

```
input:6: ERROR - variable getData is undefined
displayData(displayElement, getData());
```

Because the compiler does not have access to the `getData()` function when it compiles the code
that displays the data, it treats `getData` as `undefined`.

#### Solution: Compile All Code for a Page Together

To ensure proper compilation, compile all the code for a page together in a single compilation run.
The Closure Compiler can accept multiple JavaScript files and JavaScript strings as input, so you
can pass library code and other code together in a single compilation request.

*Note: This approach won't work if you need to mix compiled and uncompiled code. See [Broken
References between Compiled and Uncompiled
Code][#broken_references_between_compiled_and_uncompiled_code] for tips on handling this situation.*

### Broken References between Compiled and Uncompiled Code

Symbol renaming in `ADVANCED_OPTIMIZATIONS` will break communication between code processed by the
Closure Compiler and any other code. Compilation renames the functions defined in your source code.
Any external code that calls your functions will break after you compile, because it still refers
to the old function name. Similarly, references in compiled code to externally defined symbols may
be altered by Closure Compiler.

Keep in mind that "uncompiled code" includes any code passed to the `eval()` function as a string.
Closure Compiler never alters string literals in code, so Closure Compiler does not change strings
passed to `eval()` statements.

Be aware that these are actually very different problems: maintaining compiled-to-external
communication, and maintaining external-to-compiled communication. These separate problems have
different solutions, and to get the most out of Closure Compiler it's important to use the right
solution for the right situation.

Solutions for these two problems are discussed further in the next sections.

#### Solution for Calling in from External Code to Compiled Code: Exports

If you have JavaScript code that you reuse as a library, you may want to use Closure Compiler to
shrink only the library while still allowing uncompiled code to call functions in the library.

The solution in this situation is identical to the solution for unwanted code removal described in
[Export the Symbols You Want to Keep](#solution_export_the_symbols_you_want_to_keep). Exporting
symbols not only keeps those symbols from being removed, but also makes them available to external
code.

#### Solution for Calling out from Compiled Code to External Code: Externs

If you use third-party JavaScript libraries or APIs like the OpenSocial API or the Google Maps API,
you need to be sure that Closure Compiler doesn't rename the symbols you use that are defined in
those external libraries. For example, if your code calls the OpenSocial JavaScript function
`opensocial.newDataRequest()`, you do not want Closure Compiler to transform this call into `a.b()`.
Your code must use the same names that the external file uses.

Closure Compiler provides a mechanism for declaring that a name is defined in external code and so
should not be renamed. This mechanism is called the *extern*. The compiler assumes that externs will
exist in the environment in which the compiled JavaScript will be interpreted.

## Declaring Externs

The following JavaScript file contains code that requires an externs declaration:

```js
/**
 * A simple script for adding a list of notes to a page. The list diplays
 * the text of each note under its title.
 */

/**
 * Creates the DOM structure for a note and adds it to the document.
 */
function makeNoteDom(noteTitle, noteContent, noteContainer) {
  // Create DOM structure to represent the note.
  var headerElement = textDiv(noteTitle);
  var contentElement = textDiv(noteContent);

  var newNote = document.createElement('div');
  newNote.appendChild(headerElement);
  newNote.appendChild(contentElement);

  // Add the note's DOM structure to the document.
  noteContainer.appendChild(newNote);
}

/**
 * Iterates over a list of note data objects and creates a DOM
 */
function makeNotes(data, noteContainer) {
  for (var i = 0; i < data.length; i++) {
    makeNoteDom(data[i].title, data[i].content, noteContainer);
  }
}

function main() {
  var noteData = [
      {title: 'Note 1', content: 'Content of Note 1'},
      {title: 'Note 2', content: 'Content of Note 2'}];
  var noteListElement = document.getElementById('notes');
  makeNotes(noteData, noteListElement);
}

main();
```

Let's assume that the `textDiv()` function is defined in a separate file called `textops.js` that
is maintained by a third party. The function looks like this:

```js
function textDiv(text) {
  var divElement = document.createElement('div');
  var textElement = document.createTextNode(text);
  divElement.appendChild(textElement);
  return divElement;
}
```

You don't want Closure Compiler to rename `textDiv`, because it is defined externally, so you
declare an extern.

In both cases, you declare the extern by writing JavaScript that defines the symbol you want
preserved, and then giving that JavaScript to the Closure Compiler. In this example, we want to
prevent renaming of the `textDiv()` function, so we write JavaScript that declares the function,
giving it an empty body:

```js
function textDiv(text){};
```

If we give this JavaScript to Closure Compiler as an extern definition, Closure Compiler does not
include it in the output JavaScript. The sole purpose of this JavaScript is to tell Closure
Compiler, "Our code uses a function named `textDiv()` that's defined somewhere you don't know about,
so don't rename any calls to `textDiv()`."

Sending our JavaScript to Closure Compiler with the extern declaration `function textDiv(text){};`
produces the following output. Note that we have given the Closure Compiler service a formatting
parameter of `pretty_print` to make it easier to see how Closure Compiler uses the externs
declaration.

```js
for(var a = document.getElementById("notes"),
    b = [{title:"Note 1", content:"Content of Note 1"}, {title:"Note 2", content:"Content of Note 2"}],
    c = 0;c < b.length;c++) {
  var d = textDiv(b[c].title), e = textDiv(b[c].content), f = document.createElement("div");
  f.appendChild(d);
  f.appendChild(e);
  a.appendChild(f)
};
```

Closure Compiler has changed the original program dramatically, but the `textDiv` calls are still
there with the original function name. This code will therefore interoperate with the third party
`textops.js` file that defines `textDiv()`.

Both the Closure Compiler application and the Closure Compiler service API allow extern
declarations. The Closure Compiler service UI does not provide an interface element for specifying
externs files.

### Do Not Use Externs Instead of Exports!

Externs are such a handy way to protect symbols from renaming that you might be tempted to use them
instead of exports. If you want to expose an API from a compiled file so that external code can
call functions in the file, you might think that you can just declare your API functions in an
externs file.

Don't do this! Always use the exporting technique described in [Export the Symbols You Want to
Keep](#solution_export_the_symbols_you_want_to_keep) to expose functions in your compiled files.

Exporting allows better compression than externs. Closure Compiler never renames symbols declared
in externs, no matter how many times they appear in your code. This means that a single extern
declaration can create many long names in your compiled file. Exporting, in contrast, only includes
the full name of the exported symbol once. This one instance of the full name sets up an alias, so
that the symbol can be shortened everywhere else it appears.

*Note: Only use externs to declare symbols in external code. Never use them to protect symbols
defined in the compiled code.*

### How to Declare Externs with the Closure Compiler Application

Pass the names of externs files to the Closure Compiler application using a separate
`--externs` command-line flag for each extern file, as in the following command:

```
java -jar compiler.jar --compilation_level `ADVANCED_OPTIMIZATIONS` \
  --js makeallnotes.js --externs extern1.js --externs extern2.js
```

### How to Declare Externs with the Closure Compiler Service API

There are two ways to send an extern declaration to the Closure Compiler service:

 * Pass JavaScript to the Closure Compiler service in the `js_externs` parameter.
 * Pass the URL of a JavaScript file to the Closure Compiler service in the `externs_url` parameter.
 
The only difference between using `js_externs` and using `externs_url` is how the JavaScript gets
communicated to the Closure Compiler service.

These two methods are described in more detail in the next sections.

#### Declaring Externs by Passing JavaScript to the Closure Compiler Service in the `js_externs` Parameter

If you don't have many externs to declare, you can include the JavaScript for the declarations as
the value of a `js_externs` parameter in your Closure Compiler service request, as in this example:

```python
#!/usr/bin/python2.4

import httplib, urllib, sys

# Define the parameters for the POST request and encode them in
# a URL-safe format.

params = urllib.urlencode([
    ('code_url', sys.argv[1]),
    ('compilation_level', '`ADVANCED_OPTIMIZATIONS`'),
    ('language', 'ECMASCRIPT5'),
    ('output_format', 'text'),
    ('output_info', 'compiled_code'),
    ('js_externs', 'function textDiv(text){}'), # <-- New parameter!
    ('formatting', 'pretty_print')
  ])

# Always use the following value for the Content-type header.
headers = { "Content-type": "application/x-www-form-urlencoded" }
conn = httplib.HTTPConnection('closure-compiler.appspot.com')
conn.request('POST', '/compile', params, headers)
response = conn.getresponse()
data = response.read()
print data
conn.close()
```

You can include multiple `js_externs` parameters in your request. You can also include multiple
extern declarations in a single parameter value by separating them with a semi-colon.

#### Declaring Externs by Passing the URL of a JavaScript File to the Closure Compiler Service in the `externs_url` Parameter

If you have many externs to declare, passing them all in js_externs parameters may be unwieldy.
In this case:

 1. Store all of your externs declarations in a separate JavaScript file.
 2. Upload this file to a webserver.
 3. Pass the URL of this file to the Closure Compiler service as the value of the `externs_url`
    parameter.
    
For example:

```python
params = urllib.urlencode([
    ('code_url', sys.argv[1]),
    ('compilation_level', '`ADVANCED_OPTIMIZATIONS`'),
    ('language', 'ECMASCRIPT5'),
    ('output_format', 'text'),
    ('output_info', 'compiled_code'),
    # Different parameter:
    ('externs_url', 'http://www.myserver.com/myexterns.js'),
    ('formatting', 'pretty_print')
  ])
```

You can specify multiple `externs_url` parameters. The Closure Compiler service concatenates all
extern declarations from `js_externs` JavaScript and `externs_url` files.
