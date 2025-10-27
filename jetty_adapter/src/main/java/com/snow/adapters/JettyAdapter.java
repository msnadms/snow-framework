package com.snow.adapters;

import com.snow.http.models.HttpHandler;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

public class JettyAdapter extends Handler.Abstract {

    private final HttpHandler snowHandler;

    public JettyAdapter(HttpHandler handler) {
        this.snowHandler = handler;
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback)  {
        try {
            var snowRequest = new JettyRequestAdapter(request);
            var snowResponse = new JettyResponseAdapter(request, response);
            snowHandler.handle(snowRequest, snowResponse);
            callback.succeeded();
            return true;
        } catch (Exception e) {
            callback.failed(e);
            return false;
        }
    }
}
