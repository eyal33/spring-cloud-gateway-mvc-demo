package com.gw.mvc.routes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.UUID;
import java.util.function.Function;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.addRequestHeader;
import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.stripPrefix;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.method;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

@Slf4j
@Configuration
public class CustomFunctionsRoutingConfig {

    @Value("${app.gateway.remote-servers.target:http://localhost:8888}")
    private String targetUrl;

    //Need to override it(=0) when we are running integration tests.
    //Using MockMvc, we are calling directly to the api without the context path "/api/v1".
    //So, no need to strip any part of the url.
    @Value("${app.gateway.strip-prefix:2}")
    public int stripPrefixParts;

    @Bean
    public RouterFunction<ServerResponse> getHelloWorldWithHeaders() {
        return route("custom_functions_route")
                .before(stripPrefix(stripPrefixParts))
                //Use built-in before function. Create header: X-source-name=Gateway
                .before(addRequestHeader("X-source-name", "Gateway"))
                //Use custom Before function which create header X-correlation-id with random UUID for each request
                .before(addDynamicRequestHeader("X-correlation-id"))
                ////////////////////////////////////////////////////////////////////////
                // matching the path and the http method, then send the request to the target server.
                ////////////////////////////////////////////////////////////////////////
                .route(path("/custom/filter/java/hello").and(method(HttpMethod.GET)), http(targetUrl))
                //Use custom After function to log the response code from the target server
                .after(this::logResponseStatus)
                .onError(Exception.class, this::handleException)
                .build();
    }

    private ServerResponse handleException(Throwable throwable, ServerRequest request) {
        log.error("#handleException - failed to run request {}", request.uri(), throwable);

        return ServerResponse
                .status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }

    private Function<ServerRequest, ServerRequest> addDynamicRequestHeader(String headerName) {
        return request -> ServerRequest.from(request).header(headerName, UUID.randomUUID().toString()).build();
    }

    private ServerResponse logResponseStatus(ServerRequest serverRequest, ServerResponse serverResponse) {
        HttpStatusCode httpStatusCode = serverResponse.statusCode();
        String logMessage = String.format("#logResponseStatus: [From server] response code for '%s' request %s is %s.", serverRequest.method(), serverRequest.uri(), httpStatusCode);

        if (httpStatusCode.is5xxServerError()) {
            log.error(logMessage);
        } else {
            log.info(logMessage);
        }

        return serverResponse;
    }

}
