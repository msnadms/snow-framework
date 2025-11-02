package com.snow.middleware;

import java.util.Map;

public class ClaimsWrapper {

    private final Map<String, String> claims;

    public ClaimsWrapper(Map<String, String> claims) {
        this.claims = claims;
    }

    public Map<String, String> getClaims() {
        return claims;
    }
}
