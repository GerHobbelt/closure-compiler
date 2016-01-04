# Communicating with the Closure Compiler Service API

## Overview

The Closure Compiler service API provides programmatic access to Closure Compiler JavaScript
compilation through a Web-based API. While the Closure Compiler UI gives you an easy way to use the
compiler service through a simple form on a web page, copying output from this web page is not the
most efficient way to work. With the Closure Compiler service API, you gain the versatility to
build your own tools and create your own work flow.

This tutorial walks you through the process of sending JavaScript to the Closure Compiler service
and getting the Closure Compiler output back. The example uses the most basic level of Closure
Compiler compilation, which simply strips comments and whitespace from your JavaScript.

This tutorial assumes that you have basic familiarity with JavaScript and HTTP. While it uses a
Python script to submit JavaScript to the Closure Compiler service, you don't need to know Python
to follow the example.

## How to Communciate with the API

 1. Determine the Request Parameters

    You interact with the Closure Compiler service by making HTTP POST requests to the Closure
    Compiler server. With every request you must send at least the following parameters:

    `js_code` or `code_url`
    
    The value of this parameter indicates the JavaScript that you want to compile. You must include
    at least one of these parameters, and you can include both. The js_code parameter must be a
    string that contains JavaScript, such as alert('hello'). The code_url parameter must contain
    the URL of a JavaScript .js file that's available via HTTP.

    `compilation_level`

    The value of this parameter indicates the degree of compression and optimization to apply to
    your JavaScript. There are three possible compilation levels: `WHITESPACE_ONLY`,
    `SIMPLE_OPTIMIZATIONS`, and `ADVANCED_OPTIMIZATIONS`. This example use `WHITESPACE_ONLY`
    compilation, which just strips comments and whitespace.

    The compilation_level parameter defaults to a value of `SIMPLE_OPTIMIZATIONS`.

    `language` and `language_out`
    
    The value of language specifies what version of ECMAScript the Closure Compiler will parse.
    If this is unspecified, the default is `ECMASCRIPT3`, but you may want to specify `ECMASCRIPT5`
    or even `ECMASCRIPT6`. This example uses `ECMASCRIPT5`

    The optional parameter language_out may be specified, if so, it will transpile source into the
    specified version of ECMAScript.

    For more information on supported levels, see the API Reference.

    `output_info`
    
    The value of this parameter indicates the kind of information that you want from the compiler.
    There are four possible kinds of output: `compiled_code`, `warnings`, `errors`, and
    `statistics`. This example uses the value `compiled_code`, which tells the Closure Compiler
    service to output the compressed version of the JavaScript it receives in the request.

    `output_format`

    The format for the Closure Compiler service's output. There are three possible output formats:
    `text`, `json`, or `xml`. This example uses the value `text`, which outputs raw text.

    The output_format parameter defaults to a value of `text`.

    For more information about these required parameters and additional optional parameters,
    see the API Reference.

    The example in this introductory tutorial just sends one line of raw JavaScript to the Closure
    Compiler service, so it uses `js_code` instead of `code_url`. It uses a compilation_level of
    `WHITESPACE_ONLY`, asks for raw text output with an `output_format` of `text`, and asks for an
    `output_info` type of `compiled_code`.

 2. Make a Post Request to the Closure Compiler Service

    To get output from the Closure Compiler service, send the parameters you chose in Step 1 in a
    POST request to the Closure Compiler service API URL. One way to do this is with a simple HTML
    form like the one in the [Hello World of the Closure Compiler Service API](api.md).

    To use a form like this during development, however, you would have to copy the output out of the browser and paste it into a .js file. If, instead, you write a small program to send the request to the Closure Compiler service, you can write the Closure Compiler output straight to a file. For example, the following python script sends the request to the Closure Compiler service and writes out the response:

    ```python
    #!/usr/bin/python2.4
    
    import httplib, urllib, sys
    
    # Define the parameters for the POST request and encode them in
    # a URL-safe format.
    
    params = urllib.urlencode([
        ('js_code', sys.argv[1]),
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
    
    Note: To reproduce this example, Windows users may need to install Python. See the Python
    Windows FAQ for instructions on installing and using Python under Windows.

    This script optimizes JavaScript passed to it as a command line argument. Paste the above code
    into a file called compile.py, change the permissions of the file to make it executable, and
    execute the following command:

    `$ python compile.py 'alert("hello");// This comment should be stripped'`

    This command prints out the compressed code from the Closure Compiler response:

    `alert("hello");`
    
    Because this example uses basic compilation, the compiler doesn't do anything other than strip
    off the comment.

    Here are a few things to note about this script:

     * The parameters are passed to the request method of the HTTPConnection as a URL-encoded
       string. After the call to urllib.urlencode, the params variable contains the following
       string:
       
       ```
       js_code=alert%28%22hello%22%29%3B%2F%2F+This+comment+should+be+stripped&language=ECMASCRIPT5&output_info=compiled_code&out=text&compilation_level=WHITESPACE_ONLY
       ```
    
       If you write your own script, the script should post URL-encoded content like this.

     * The request must always have a Content-type header of application/x-www-form-urlencoded

### Next Steps

To learn to use the service to achieve better compression in a more realistic development scenario,
continue on to [Compressing Files with the API](api-tutorial-2.md).
