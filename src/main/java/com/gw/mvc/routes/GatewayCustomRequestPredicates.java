package com.gw.mvc.routes;

import lombok.experimental.UtilityClass;
import org.springframework.cloud.gateway.server.mvc.common.Shortcut;
import org.springframework.util.Assert;
import org.springframework.web.servlet.function.RequestPredicate;
import org.springframework.web.servlet.function.RequestPredicates;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

@UtilityClass
public class GatewayCustomRequestPredicates {

    @Shortcut(
            type = Shortcut.Type.LIST
    )
    public static RequestPredicate multiPath(String... patterns) {

        Assert.notEmpty(patterns, "'patterns' must not be empty");
        RequestPredicate requestPredicate = RequestPredicates.path(patterns[0]);

        for(int i = 1; i < patterns.length; ++i) {
            requestPredicate = requestPredicate.or(RequestPredicates.path(patterns[i]));
        }

        return requestPredicate;
    }


    public static class PredicateSupplier implements org.springframework.cloud.gateway.server.mvc.predicate.PredicateSupplier {
        PredicateSupplier() {
        }

        public Collection<Method> get() {
            return Arrays.asList(GatewayCustomRequestPredicates.class.getMethods());
        }
    }

}
