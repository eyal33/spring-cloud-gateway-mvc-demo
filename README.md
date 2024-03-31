# spring-cloud-gateway-mvc-demo

## Pre-requisites
Install Java 21

Install Docker Desktop

## Wiremock server (on port 8888)

Start Mock server docker on port 8888
```
docker run -it --rm --name wm -p 8888:8080 -v "<project location>\spring-cloud-gateway-mvc-demo\tools\wiremock:/home/wiremock" rodolpheche/wiremock  --verbose

For example,
docker run -it --rm --name wm -p 8888:8080 -v "C:\Users\<user>\dev\Git_work\github\spring-cloud-gateway-mvc-demo\tools\wiremock:/home/wiremock" rodolpheche/wiremock  --verbose
```

## Generated requests file
Look at the file generated-requests.http under tools directory in order to execute api requests from the IDE(Intellij)


## Write Your first route
For running Spring gateway MVC, we need the following jars. Take a look at **pom.xml**

```xml
<dependencies>
  ...
  <!-- Spring gateway MVC -->
  <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-gateway-mvc</artifactId>
  </dependency>

  <!-- Apache rest client - used for dispatching requests to targets -->
  <dependency>
      <groupId>org.apache.httpcomponents.client5</groupId>
      <artifactId>httpclient5</artifactId>
  </dependency>
  ...
</dependencies>
```

### application.yaml
```yaml
server:
  port: 8090
  servlet:
    context-path: /api/v1

spring:
  cloud:
    gateway:
      mvc:
        http-client:
          type: autodetect
          connect-timeout: 30s
          read-timeout: 30s
  threads:
    virtual:
      enabled: true

#application parameters
app:
  gateway:
    remote-servers:
      target: http://localhost:8888
```

### Example 1 - Java Bean RouterFunction - hello world

First route by defining RouteFunction Bean.

See file _HelloWorldRoutingConfig.java_

In order to check it, start Mock server at port 8888 and return a response for GET request of /basic/hello 
You should call GET http://localhost:8090/api/v1/basic/hello (See generated-requests.http).

### Example 2 - Java Bean RouterFunction - use custom & built-in functions

We want to do the following steps:
- Add header to the request with a fixed value(addRequestHeader).
- Add header to the request with a dynamic value(addDynamicRequestHeader).
- Log the response status received from the target server(logResponseStatus).

See file _CustomFunctionsRoutingConfig.java_.

By calling the GET request http://localhost:8090/api/v1/custom/filter/java/hello, you could see two additional headers at the request received in the Wiremock server.

### Example 3 - Route in application.yaml - use custom filter (id=custom_filter_hello_world)
We want new filter which can add new header with Random UUID value. <br>
Function _addDynamicRequestHeaderFromSupplier_ is defined in class _CustomBeforeFilterFunctions.java_.
See file _spring.factories_. We define there a FilterSupplier which is loaded to the available filters.

By calling the GET request http://localhost:8090/api/v1/custom/filter/configuration/hello, you should see the header 'X-my-header-from-configuration' at the request received in the Wiremock server.


### Example 4 - Route in application.yaml - use custom predicate (id=custom_predicate_hello_world)
We want new predicate which can get list of paths to match. MultiPath is the name of the predicate. <br>
Function _multiPath_ is defined in class _CustomGatewayRequestPredicates.java_.
See file _spring.factories_. We define there a PredicateSupplier which is loaded to the available predicates.

By calling the GET requests: http://localhost:8090/api/v1/custom/filter/configuration/helloPredicate1 or http://localhost:8090/api/v1/custom/filter/configuration/helloPredicate2, you should get a response from Wiremock server.



