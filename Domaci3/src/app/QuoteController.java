package app;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import http.Request;
import http.response.HtmlResponse;
import http.response.RedirectResponse;
import http.response.Response;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class QuoteController extends Controller {

    private static final Map<String, String> quotes = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();
    private static final Random random = new Random();

    private static String quoteOfTheDay = null;
    public QuoteController(Request request) {
        super(request);
    }

    @Override
    public Response doGet() {
        String styles = "<style>" +
                "body { font-family: Arial, sans-serif; background: #f4f4f4; color: #333; }" +
                "h2 { color: #556B2F; }" +
                "form, table { max-width: 600px; margin: 20px auto; padding: 20px; background: #fff; border-radius: 8px; }" +
                "input[type='text'], textarea, table { width: 100%; margin-bottom: 10px; padding: 10px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }" +
                "textarea { resize: vertical; }" +
                "input[type='submit'] { width: 100%; padding: 10px; border: none; border-radius: 4px; background: #5F9EA0; color: white; cursor: pointer; }" +
                "input[type='submit']:hover { background: #428bca; }" +
                ".quote-day { margin-bottom: 20px; background: #333; color: #fff; padding: 10px; border-radius: 4px; }" +
                "table { border-collapse: collapse; width: 100%; background: #fff; }" +
                "th, td { text-align: left; padding: 8px; }" +
                "tr:nth-child(even) { background-color: #f2f2f2; }" +
                "th { background-color: #5F9EA0; color: white; }" +
                "</style>";

        if (quoteOfTheDay == null) {
            String allQuotesJson = getAllQuotesFromService();
            updateQuotes(allQuotesJson);
            quoteOfTheDay = selectRandomQuote();
        }

        String htmlBody = "<form method=\"POST\" action=\"/save-quote\">" +
                "<h2>Quote of the day:</h2>" +
                "<div class=\"quote-day\">" + quoteOfTheDay + "</div>" +
                "<label for=\"author\">Author:</label>" +
                "<input type=\"text\" id=\"author\" name=\"author\" required>" +
                "<label for=\"quote\">Quote:</label>" +
                "<textarea id=\"quote\" name=\"quote\" required></textarea>" +
                "<input type=\"submit\" value=\"Save Quote\">" +
                "</form>";

        StringBuilder savedQuotesTable = new StringBuilder("<table>");
        savedQuotesTable.append("<tr><th>Author</th><th>Quote</th></tr>");

        quotes.forEach((author, quote) -> savedQuotesTable.append("<tr><td>")
                .append(author)
                .append("</td><td>")
                .append(quote)
                .append("</td></tr>"));

        savedQuotesTable.append("</table>");

        String content = "<html><head><title>Odgovor servera</title>" + styles + "</head>\n" +
                "<body>" + htmlBody + savedQuotesTable.toString() + "</body></html>";

        return new HtmlResponse(content);
    }


    @Override
    public Response doPost() {
        String[] keyValuePairs = this.request.getBody().split("&");
        Map<String, String> formData = new HashMap<>();
        for (String pair : keyValuePairs) {
            String[] entry = pair.split("=");
            formData.put(entry[0], entry.length > 1 ? URLDecoder.decode(entry[1], StandardCharsets.UTF_8) : ""); // Decoding the URL encoded string
        }

        quotes.put(formData.get("author"), formData.get("quote"));

        return new RedirectResponse("/quotes");
    }

    private void updateQuotes(String jsonResponse) {
        String[] quotesArray = gson.fromJson(jsonResponse, String[].class);
        for (String quoteJson : quotesArray) {
            JsonObject quoteObject = gson.fromJson(quoteJson, JsonObject.class);
            String author = quoteObject.get("author").getAsString();
            String quote = quoteObject.get("quote").getAsString();
            quotes.put(author, quote);
        }
    }

    private String selectRandomQuote() {
        int index = random.nextInt(quotes.size());
        Map.Entry<String, String> entry = quotes.entrySet().stream().skip(index).findFirst().get();
        return entry.getValue();
    }

    private String getAllQuotesFromService() {
        StringBuilder response = new StringBuilder();
        try (Socket socket = new Socket("localhost", 8081);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println("GET / HTTP/1.1");
            out.println("Host: localhost:8081");
            out.println("Connection: Close");
            out.println();

            String line;
            boolean headerEnded = false;
            while ((line = in.readLine()) != null) {
                if (headerEnded) {
                    response.append(line);
                }
                if (line.isEmpty()) {
                    headerEnded = true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return response.toString();
    }


}
