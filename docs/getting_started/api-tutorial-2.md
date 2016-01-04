# Compressing Files with the Closure Compiler Service API

## Overview

[Communicating with the API](api-tutorial-1.md) described the basics of how to communicate with the
Closure Compiler service, but it only illustrated using the service to strip comments from a single
line of JavaScript. This tutorial illustrates how to use the Closure Compiler service in a more
realistic development scenario: processing a whole JavaScript file to achieve a significant size
reduction.

This tutorial assumes that you have basic familiarity with JavaScript and HTTP. While it uses a
Python script to submit JavaScript to the Closure Compiler service, you don't need to know Python
to follow the example.

## Compressing a File

The example in [Communicating with the API](api-tutorial-1.md) passed a JavaScript string as a
command line parameter to our compilation script. This approach won't work very well for a
realistically sized JavaScript program, however, because the JavaScript string quickly becomes
unwieldy when the code is longer than a few lines. For larger programs you can use the code_url
request parameter to specify the name of a JavaScript file to process. You can use code_url in
addition to js_code, or as a replacement for js_code.

For example, consider the following JavaScript program:

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
  var headerElement = document.createElement('div');
  var headerText = document.createTextNode(noteTitle);
  headerElement.appendChild(headerText);
  
  var contentElement = document.createElement('div');
  var contentText = document.createTextNode(noteContent);
  contentElement.appendChild(contentText);

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

You can pass this program to the Closure Compiler service more conveniently as a file than as one
big string. Follow these steps to process a file with the service:

 1. Save the JavaScript in a file.
 2. Make the file accessible on the web (by uploading it to your web server, for example).
 3. Make a POST request to the Closure Compiler service as demonstrated in
    [Communicating with the API](api-tutorial-1.md), but for the js_code parameter substitute a
    `code_url` parameter. The value of `code_url` must be the URL of the JavaScript file created
    in Step 1.

For example, you can find the JavaScript for this example in the file
[tutorial2.js](http://closure-compiler.appspot.com/closure/compiler/samples/tutorial2.js).
To process this file with the Closure Compiler service API, change the python program from
[Communicating with the API](api-tutorial-1.md) to use `code_url`, like so:

```python
#!/usr/bin/python2.4

import httplib, urllib, sys

# Define the parameters for the POST request and encode them in
# a URL-safe format.

params = urllib.urlencode([
    ('code_url', sys.argv[1]), # <--- This parameter has a new name!
    ('compilation_level', 'WHITESPACE_ONLY'),
    ('language', 'ECMASCRIPT5'),
    ('output_format', 'text'),
    ('output_info', 'compiled_code'),
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

*Note: To reproduce this example, Windows users may need to install Python. See the Python Windows
FAQ for instructions on installing and using Python under Windows.*

Send the code to the Closure Compiler service with the following command:

    $ python compile.py http://closure-compiler.appspot.com/closure/compiler/samples/tutorial2.js

The Closure Compiler service retrieves the file from
`https://closure-compiler.appspot.com/closure/compiler/samples/tutorial2.js` and returns compressed
JavaScript in the response.

To compile multiple output files together into one output file, include multiple `code_url`
parameters, as in this example:

```python
params = urllib.urlencode([
    # Multiple code_url parameters:
    ('code_url', 'http://yourserver.com/yourJsPart1.js'),
    ('code_url', 'http://yourserver.com/yourJsPart2.js'),
    ('compilation_level', 'WHITESPACE_ONLY'),
    ('language', 'ECMASCRIPT5'),
    ('output_format', 'text'),
    ('output_info', 'compiled_code'),
  ])
```

## Improving the Compression

The examples so far have used a compilation_level of `WHITESPACE_ONLY`, which just strips comments
and white space. With the SIMPLE_OPTIMIZATIONS compression level you can achieve much higher
compression rates. To use SIMPLE_OPTIMIZATIONS compression, change the compilation_level parameter
to `SIMPLE_OPTIMIZATIONS`:

```python
params = urllib.urlencode([
    ('code_url', sys.argv[1]),
    ('compilation_level', 'SIMPLE_OPTIMIZATIONS'),  # <--- This parameter has a new value!
    ('language', 'ECMASCRIPT5'),
    ('output_format', 'text'),
    ('output_info', 'compiled_code'),
  ])
```

and run the script as before:

    $ python compile.py http://closure-compiler.appspot.com/closure/compiler/samples/tutorial2.js

The output should look like this:

```js
var GLOBAL_document=document,$$PROP_appendChild="appendChild";function makeNoteDom(a,b,c){var d=GLOBAL_document.createElement("div");a=GLOBAL_document.createTextNode(a);d[$$PROP_appendChild](a);a=GLOBAL_document.createElement("div");b=GLOBAL_document.createTextNode(b);a[$$PROP_appendChild](b);b=GLOBAL_document.createElement("div");b[$$PROP_appendChild](d);b[$$PROP_appendChild](a);c[$$PROP_appendChild](b)}function makeNotes(a,b){for(var c=0;c<a.length;c++)makeNoteDom(a[c].title,a[c].content,b)}
function main(){var a=[{title:"Note 1",content:"Content of Note 1"},{title:"Note 2",content:"Content of Note 2"}],b=GLOBAL_document.getElementById("notes");makeNotes(a,b)}main();
```

This code is harder to read than the source program, but it's smaller.

### How Much Smaller Is the Code?

If we change the `output_info` in our request parameters from `compiled_code` to statistics we can
see exactly how much space we have saved:

```
Original Size: 1372
Compressed Size: 677
Compilation Time: 0
```

The new JavaScript is less than half the size of the original.

### How Did the Closure Compiler Service Make the Program Smaller?

In this case, the Closure Compiler achieves the reduction in size in part by renaming local
variables. For example, the original file includes this line of code:

```js
var headerElement = document.createElement('div');
```

Closure Compiler changes this statement to:

```js
var d=document.createElement("div");
```

The Closure Compiler changes the symbol `headerElement` to `e` everywhere within the function
`makeNoteDom`, and thus preserves functionality. But the 13 characters of `headerElement` have been
shortened to one character in each of the three places where they appear.
This gives a total savings of 36 characters.

Compilation with `SIMPLE_OPTIMIZATIONS` always preserves the functionality of syntactically valid
JavaScript, provided that the code does not access local variables using string names (with, for
example, `eval()` statements).

## Next Steps

Now that you're familiar with `SIMPLE_OPTIMIZATIONS` and the basic mechanics of using the service,
the next step is to learn about the `ADVANCED_OPTIMIZATIONS` compilation level. This level
requires some extra steps to ensure that your JavaScript works the same way before and after
compilation, but it makes the JavaScript even smaller. See Advanced Compilation and Externs to
learn about `ADVANCED_OPTIMIZATIONS`.
