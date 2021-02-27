CaseClass String Beautifier
===========================

Case-class String Beautifier does to strings generated by calling `.toString` upon case-class objects, what a json-beautifier does to a minified json.

> Usage: case-class-string-beautifier [options] <br/>
> <br/> 
> -f, --file <filepath>   file name to read its content or use '-' to read from stdin <br/>
> -s, --string <content>  read content directly from argument <br/>
> -i, --stdin             read content from stdin <br/>

Sample usage:
```bash
case-class-string-beautifier -s 'HttpResponse(409 Conflict,List(Connection: keep-alive,Server: Apache,Cache-Control: no-cache,Referrer-Policy: strict-origin-when-cross-origin,X-Permitted-Cross-Domain-Policies: none,X-Download-Options: noopen,X-Frame-Options: SAMEORIGIN,X-Runtime: 14.498687,X-Content-Type-Options: nosniff,Status: 409 Conflict,X-Frame-Options: SAMEORIGIN),HttpEntity.Chunked(application/json),HttpProtocol(HTTP/1.1))'
```
Result:
```
HttpResponse(
  409 Conflict,
  List(
    Connection: keep-alive,
    Server: Apache,
    Cache-Control: no-cache,
    Referrer-Policy: strict-origin-when-cross-origin,
    X-Permitted-Cross-Domain-Policies: none,
    X-Download-Options: noopen,
    X-Frame-Options: SAMEORIGIN,
    X-Runtime: 14.498687,
    X-Content-Type-Options: nosniff,
    Status: 409 Conflict,
    X-Frame-Options: SAMEORIGIN
  ),
  HttpEntity.Chunked(application/json),
  HttpProtocol(HTTP/1.1)
)
```

Design Considerations
---------------------

Since detecting the ending of these sorts of string is difficult, the beautifier is designed to be simple and lenient.

- If there is any redundant tail such as `YYY` in the string `A(B(),C)YYY`, the tool will still show you the result along with a warning about the redundant tail.
- If the tool encounters any illegal token, it prints the partial tree as parsed along with the error. We considered this in design to help users figure out and fix problems easier.
- Since string typed parameters are not quoted in `toString` output of the case-class, occurrence of `","` is interpreted as a parameter separator as there is no determinate way to distinguish them from the actual parameter separator token. Here's an example:
  ```
  A(I bought apple, orange, and banana, 10)
  ```
  ```
  A(
    I bought apple,
     orange,
     and banana,
     10
  )
  ```
  
Future Work
-----------
- Output as json

Maintainer
----------

Nima Taheri (nima.trueway@gmail.com)