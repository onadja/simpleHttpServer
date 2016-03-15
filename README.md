# simpleHttpServer
## Description
This is a simple multithreaded http server.

Supports:
* headers: If-Match, ETag, Accept-Charset, Content-type.
* charsets: ASCII-US, UTF-8.
* content types: text/html, application/javascript, image/jpeg.
* HTTP codes: 200 - OK, 400 - bad request, 404 - file not found, 405 - method not allowed.
* one method GET.
* caching files.

Properties file you can find in src/resources directory. Its name is config.properties. It supports properties: server.host - host, server.port - port, server.root - root directory to look files in, server.isCached - true/false - if files are cached or not.

## Usage. 
```mvn clean install``` compiles program. 

```mvn exec:java``` runs server.

Server commands: ```refreshCache``` - refreshes cache, ```exit``` - exits server.
