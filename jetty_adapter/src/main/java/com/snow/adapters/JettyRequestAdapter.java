package com.snow.adapters;

import com.snow.http.models.HttpRequest;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.server.Request;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public record JettyRequestAdapter(Request request) implements HttpRequest {

    @Override
    public String method() {
        return this.request.getMethod();
    }

    @Override
    public String route() {
        return this.request.getHttpURI().getPathQuery();
    }

    @Override
    public Map<String, List<String>> headers() {
        return HttpFields.asMap(this.request.getHeaders());
    }

    @Override
    public InputStream body() {
        return Request.asInputStream(request);
    }
}
