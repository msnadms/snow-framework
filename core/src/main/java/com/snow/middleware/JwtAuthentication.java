package com.snow.middleware;

import com.snow.exceptions.UnauthorizedRequestException;
import com.snow.http.models.HttpRequest;
import com.snow.http.models.HttpResponse;
import com.snow.middleware.functions.MiddlewareChain;
import com.snow.middleware.functions.MiddlewareFn;
import com.snow.util.HttpResponseUtil;
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
            return HttpResponseUtil.sendUnauthorized(request, response);
        }
        try {
            Mac verifier = Mac.getInstance(this.algorithm);
            verifier.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), verifier.getAlgorithm()));

            byte[] calculatedBytes = verifier.doFinal((chunks[0] + "." + chunks[1]).getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = Base64.getUrlDecoder().decode(chunks[2].getBytes(StandardCharsets.UTF_8));

            if (!Arrays.equals(signatureBytes, calculatedBytes)) {
                return HttpResponseUtil.sendUnauthorized(request, response);
            }

            processClaims(request, chunks[1]);

        } catch (NoSuchAlgorithmException | InvalidKeyException | IOException e) {
            return HttpResponseUtil.sendUnauthorized(request, response);
        }
        request.setAttribute("Authenticated", true);
        return next.execAsync();
    }

    private void processClaims(HttpRequest request, String chunk) throws IOException {
        byte[] claimsDecoded = Base64.getUrlDecoder().decode(chunk.getBytes(StandardCharsets.UTF_8));
        String claims = new String(claimsDecoded, StandardCharsets.UTF_8);
        Map<String, String> claimsMap = JsonUtil.deserializeToMap(claims);
        request.setAttribute("claims", new ClaimsWrapper(claimsMap));
    }

}
