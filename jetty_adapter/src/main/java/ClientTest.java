import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ClientTest {
    private static int idx = 0;
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        URI uri = URI.create("http://localhost:8080/users/123?one=test");
        URI uri2 = URI.create("http://localhost:8080/users/12356?one=again");
        URI[] uris  = {uri, uri2};

        Runnable task = () -> {
            try {
                var request = HttpRequest.newBuilder(uris[idx++]).GET().build();
                var response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println(Thread.currentThread().getName() + " → " + response.body());
            } catch (Exception e) {
                e.printStackTrace();
            }
        };

        Thread t1 = new Thread(task, "Thread-1");
        Thread t2 = new Thread(task, "Thread-2");

        t1.start();
        t2.start();

        t1.join();
        t2.join();
    }
}
