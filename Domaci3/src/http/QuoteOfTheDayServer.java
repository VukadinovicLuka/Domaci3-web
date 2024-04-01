package http;// QuoteOfTheDayServer
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import com.google.gson.Gson;

public class QuoteOfTheDayServer {

    private static final Gson gson = new Gson();

    private static final String[] quotes = {
            "{\"author\":\"Oscar Wilde\", \"quote\":\"Be yourself; everyone else is already taken.\"}",
            "{\"author\":\"Frank Zappa\", \"quote\":\"So many books, so little time.\"}",
            "{\"author\":\"Mahatma Gandhi\", \"quote\":\"Be the change that you wish to see in the world.\"}"
    };

    public static void main(String[] args) throws IOException {
        int port = 8081;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("Quote of the Day Server is running on port " + port);

        while (true) {
            try (Socket clientSocket = serverSocket.accept();
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
                out.println("HTTP/1.1 200 OK");
                out.println("Content-Type: application/json");
                out.println("Connection: Close");
                out.println();
                out.println(gson.toJson(quotes));
            } catch (IOException e) {
                System.err.println("Error handling client request: " + e.getMessage());
            }
        }
    }
}
