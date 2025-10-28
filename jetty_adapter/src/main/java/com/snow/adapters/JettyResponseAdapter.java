package com.snow.adapters;

import com.snow.http.models.HttpResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.io.OutputStream;
import java.nio.ByteBuffer;

public record JettyResponseAdapter(Request request, Response response) implements HttpResponse {

    @Override
    public void status(int code) {
        this.response.setStatus(code);
    }

    @Override
    public int getStatus() {
        return this.response.getStatus();
    }

    @Override
    public void header(String name, String value) {
        this.response.getHeaders().add(name, value);
    }

    @Override
    public OutputStream body() {
        return Response.asBufferedOutputStream(this.request, this.response);
    }

    @Override
    public void nativeWrite(byte[] data) {
        response.write(true, ByteBuffer.wrap(data), Callback.NOOP);
    }
}
