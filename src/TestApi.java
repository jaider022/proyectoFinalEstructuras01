import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class TestApi {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        // Let's get ALL properties to see what is actually there!
        HttpRequest req2 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8082/api/inmuebles"))
                .GET()
                .build();
        System.out.println("ALL properties: " + client.send(req2, HttpResponse.BodyHandlers.ofString()).body());

        // And let's query without minHabitaciones
        HttpRequest req3 = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8082/api/recomendaciones/manual?tipo=Cualquiera&ciudad=Cualquiera&precioMax=25000000&minHabitaciones=0&parqueadero=false"))
                .GET()
                .build();
        System.out.println("Search 2: " + client.send(req3, HttpResponse.BodyHandlers.ofString()).body());
    }
}
