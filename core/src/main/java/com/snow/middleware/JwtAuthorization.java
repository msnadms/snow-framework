package com.snow.middleware;

import com.snow.exceptions.BadRouteException;
import com.snow.exceptions.ForbiddenRequestException;
import com.snow.http.models.HttpRequest;
import com.snow.http.models.HttpResponse;
import com.snow.middleware.functions.MiddlewareChain;
import com.snow.middleware.functions.MiddlewareFn;
import com.snow.util.HttpUtil;
import com.snow.util.JsonUtil;
import com.snow.web.RoutingHelper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class JwtAuthorization implements MiddlewareFn {

    private static final Logger logger = Logger.getLogger(JwtAuthorization.class.getName());

    public JwtAuthorization() {}

    @Override
    public CompletableFuture<Void> exec(HttpRequest request, HttpResponse response, MiddlewareChain next) {

        var authHeader = request.headers().get("Authorization");

        if (authHeader == null) {
            return sendForbidden(request, response, "ANY");
        }
        String token = authHeader.get(0).replaceFirst("Bearer ", "");
        String[] chunks = token.split("\\.");
        if (chunks.length != 3) {
            return sendForbidden(request, response, "ANY");
        }

        byte[] claimsDecoded = Base64.getUrlDecoder().decode(chunks[1].getBytes(StandardCharsets.UTF_8));
        String claims = new String(claimsDecoded, StandardCharsets.UTF_8);

        try {
            var claimsMap = JsonUtil.deserializeToMap(claims);
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
                return next.execAsync();
            }

            return sendForbidden(request, response, unsatisfiedRoles.toString());
        } catch (IOException | BadRouteException e) {
            logger.severe(String.format("Route %s %s not found", request.method(), request.route()));
            response.status(404);
            response.nativeWrite("Not Found".getBytes());
            return HttpUtil.returnExceptionally(e);
        }
    }

    private CompletableFuture<Void> sendForbidden(HttpRequest request, HttpResponse response, String role) {
        logger.severe(
                String.format(
                        "Forbidden Request: %s %s - does not have role: %s",
                        request.method(),
                        request.route(),
                        role)
        );
        response.status(403);
        response.nativeWrite("Forbidden".getBytes());
        return HttpUtil.returnExceptionally(
                new ForbiddenRequestException(request.method(), request.route(), role)
        );
    }

}
