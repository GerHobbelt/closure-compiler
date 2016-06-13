# Getting Started with the Closure Compiler Application

## The Hello World of the Closure Compiler Application

The Closure Compiler application is a Java command-line utility that compresses, optimizes, and
looks for mistakes in your JavaScript. To try out the Closure Compiler application with a simple
JavaScript program, follow the steps below.

To work through this exercise you need the Java Runtime Environment version 7.

 1. **Download the Closure Compiler package**

    Create a working directory called `closure-compiler`.

    Download the Closure Compiler
    [compiler.jar](https://dl.google.com/closure-compiler/compiler-latest.zip) file and save it in
    `closure-compiler`.
    
 2. **Create a JavaScript file**

    Create a file named `hello.js` containing the following JavaScript:

    ```js
    // A simple function.
    function hello(longName) {
      alert('Hello, ' + longName);
    }
    hello('New User');
    ```

    Save this file in the closure-compiler directory.

 3. **Compile the JavaScript file**

    Run the following command from the closure-compiler directory:

    ```
    java -jar compiler.jar --js hello.js --js_output_file hello-compiled.js
    ```
    
    This command creates a new file called `hello-compiled.js`, which contains the following
    JavaScript:

    ```js
    function hello(a){alert("Hello, "+a)}hello("New User");
    ```
    
    Note that the compiler has stripped comments, whitespace and an unnecessary semi-colon. The
    compiler has also replaced the parameter name `longName` with the shorter name `a`. The result
    is a much smaller JavaScript file.

    To confirm that the compiled JavaScript code still works correctly, include hello-compiled.js in an HTML file like this one:
    
    ```html
    <html>
    <head><title>Hello World</title></head>
    <body>
      <script src="hello-compiled.js"></script>
    </body>
    </html>
    ```
    
    Load the HTML file in a browser, and you should see a friendly greeting!

## Next Steps

This example illustrates only the most simple optimizations performed by the Closure Compiler. To
learn more about the compiler's capabilities, read Advanced Compilation and Externs.

To learn more about other flags and options for the Closure Compiler, execute the jar with the
`--help` flag:

```
java -jar compiler.jar --help
```
