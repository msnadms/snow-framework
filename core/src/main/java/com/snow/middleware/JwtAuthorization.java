package com.snow.middleware;

import com.snow.exceptions.BadRouteException;
import com.snow.http.models.HttpRequest;
import com.snow.http.models.HttpResponse;
import com.snow.middleware.functions.MiddlewareChain;
import com.snow.middleware.functions.MiddlewareFn;
import com.snow.util.HttpResponseUtil;
import com.snow.web.RoutingHelper;

import java.util.concurrent.CompletableFuture;

public class JwtAuthorization implements MiddlewareFn {

    public JwtAuthorization() {}

    @Override
    public CompletableFuture<Void> exec(HttpRequest request, HttpResponse response, MiddlewareChain next) {

        if (!Boolean.TRUE.equals(request.getAttribute("Authenticated"))) {
            return HttpResponseUtil.sendUnauthorized(request, response);
        }

        try {
            var claimsMap = ((ClaimsWrapper) request.getAttribute("claims")).getClaims();
            var ctx = RoutingHelper.getControllerContext(request.method(), request.route());
            String[] rolesRequired = ctx.definition().requiredRoles();
            StringBuilder unsatisfiedRoles = new StringBuilder();

            for (String role : rolesRequired) {
                String[] kv = role.split("=");
                if (!claimsMap.getOrDefault(kv[0].trim(), "").equalsIgnoreCase(kv[1].trim())) {
                    unsatisfiedRoles.append(kv[0]).append(" ");
                }
            }

            if (unsatisfiedRoles.isEmpty()) {
                request.setAttribute("Authorized", true);
                return next.execAsync();
            }

            return HttpResponseUtil.sendForbidden(request, response, unsatisfiedRoles.toString());
        } catch (BadRouteException e) {
            return HttpResponseUtil.sendNotFound(request, response, e);
        } catch (ClassCastException e) {
            return HttpResponseUtil.sendForbidden(request, response, "ANY");
        }
    }

}
