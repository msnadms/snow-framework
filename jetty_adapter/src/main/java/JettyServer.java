import com.snow.adapters.JettyAdapter;
import com.snow.middleware.JwtAuthorization;
import com.snow.middleware.RequestInfoLogging;
import com.snow.web.Snow;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;

public class JettyServer {

    private static String SECRET = "a-string-secret-at-least-256-bits-long";

    public static void main(String[] args) throws Exception {
        Snow snow = new Snow("com.snow");

        Server server = new Server();
        HttpConfiguration httpConfig = new HttpConfiguration();
        ServerConnector connector = new ServerConnector(server, new HttpConnectionFactory(httpConfig));
        connector.setPort(8080);
        server.addConnector(connector);
        snow.use(new RequestInfoLogging());
        snow.use(new JwtAuthorization(SECRET));
        server.setHandler(new JettyAdapter(snow));
        server.start();
        server.join();
    }
}
