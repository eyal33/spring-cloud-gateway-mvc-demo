package com.gw.mvc.custom;

import lombok.experimental.UtilityClass;
import org.springframework.cloud.gateway.server.mvc.common.Shortcut;
import org.springframework.web.servlet.function.HandlerFilterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;

@UtilityClass
public class CustomBeforeFilterFunctions {

    @Shortcut
    public static HandlerFilterFunction<ServerResponse, ServerResponse> addDynamicRequestHeaderFromSupplier(String headerName) {
        return HandlerFilterFunction.ofRequestProcessor(addDynamicRequestHeaderFunc(headerName));
    }
    public static Function<ServerRequest, ServerRequest> addDynamicRequestHeaderFunc(String headerName) {
        return request -> ServerRequest.from(request).header(headerName, UUID.randomUUID().toString()).build();
    }

    public static Function<ServerRequest, ServerRequest> addDynamicRequestHeader(String headerName) {
        return request -> ServerRequest.from(request).header(headerName, UUID.randomUUID().toString()).build();
    }

    //Define in spring.factories file
    static class FilterSupplier implements org.springframework.cloud.gateway.server.mvc.filter.FilterSupplier {
        FilterSupplier() {
        }

        public Collection<Method> get() {

            return Arrays.asList(CustomBeforeFilterFunctions.class.getMethods());
        }
    }

}

