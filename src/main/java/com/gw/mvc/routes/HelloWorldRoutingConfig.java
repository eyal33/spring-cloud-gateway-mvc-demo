package com.gw.mvc.routes;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.stripPrefix;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.method;
import static org.springframework.cloud.gateway.server.mvc.predicate.GatewayRequestPredicates.path;

@Slf4j
@Configuration
public class HelloWorldRoutingConfig {

    @Value("${app.gateway.remote-servers.target:http://localhost:8888}")
    private String targetUrl;

    //Need to override it(=0) when we are running integration tests.
    //Using MockMvc, we are calling directly to the api without the context path "/api/v1".
    //So, no need to strip any part of the url.
    @Value("${app.gateway.strip-prefix:2}")
    public int stripPrefixParts;

    @Bean
    public RouterFunction<ServerResponse> getHelloWorld() {
        return route("basic_route")
                .before(stripPrefix(stripPrefixParts))
                ////////////////////////////////////////////////////////////////////////
                // matching the path and the http method, then send the request to the target server.
                ////////////////////////////////////////////////////////////////////////
                .route(path("/basic/hello").and(method(HttpMethod.GET)), http(targetUrl))
                .onError(Exception.class, this::handleException)
                .build();
    }

    private ServerResponse handleException(Throwable throwable, ServerRequest request) {
        log.error("#handleException - failed to run request {}", request.uri(), throwable);

        return ServerResponse
                .status(HttpStatus.INTERNAL_SERVER_ERROR).build();

    }

}
