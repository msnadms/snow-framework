package com.snow.middleware;

import com.snow.exceptions.UnauthorizedRequestException;
import com.snow.http.models.HttpRequest;
import com.snow.http.models.HttpResponse;
import com.snow.middleware.functions.MiddlewareChain;
import com.snow.middleware.functions.MiddlewareFn;
import com.snow.util.HttpUtil;
import com.snow.util.JsonUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@SuppressWarnings("ClassCanBeRecord")
public class JwtAuthentication implements MiddlewareFn {

    private static final Logger logger = Logger.getLogger(JwtAuthentication.class.getName());
    private final String secret;
    private final String algorithm;

    public JwtAuthentication(String secret) {
        this.secret = secret;
        this.algorithm = "HmacSHA256";
    }

    public JwtAuthentication(String secret, String algorithm) {
        this.secret = secret;
        this.algorithm = algorithm;
    }

    @Override
    public CompletableFuture<Void> exec(HttpRequest request, HttpResponse response, MiddlewareChain next) {

        var authHeaders = request.headers().get("Authorization");
        String authHeader = authHeaders.get(0);
        String token = authHeader.replaceFirst("Bearer ", "");

        String[] chunks = token.split("\\.");
        if (chunks.length != 3) {
            logger.severe("Invalid JWT Token");
            return sendUnauthorized(request, response);
        }
        try {
            Mac verifier = Mac.getInstance(this.algorithm);
            verifier.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), verifier.getAlgorithm()));

            byte[] calculatedBytes = verifier.doFinal((chunks[0] + "." + chunks[1]).getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = Base64.getUrlDecoder().decode(chunks[2].getBytes(StandardCharsets.UTF_8));

            if (!Arrays.equals(signatureBytes, calculatedBytes)) {
                return sendUnauthorized(request, response);
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return sendUnauthorized(request, response);
        }
        return next.execAsync();
    }

    private CompletableFuture<Void> sendUnauthorized(HttpRequest request, HttpResponse response) {
        logger.severe(String.format("Unauthorized Request: %s %s", request.method(), request.route()));
        response.status(401);
        response.nativeWrite("Unauthorized".getBytes());
        return HttpUtil.returnExceptionally(
                new UnauthorizedRequestException(request.method(), request.route())
        );
    }

}
