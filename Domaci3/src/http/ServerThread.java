package http;

import app.RequestHandler;
import http.response.Response;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;

public class ServerThread implements Runnable {

    private Socket client;
    private BufferedReader in;
    private PrintWriter out;

    public ServerThread(Socket sock) {
        this.client = sock;

        try {
            // Inicijalizacija ulaznog toka
            in = new BufferedReader(
                    new InputStreamReader(
                            client.getInputStream()));

            // Inicijalizacija izlaznog sistema
            out = new PrintWriter(
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    client.getOutputStream())), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        try {
            // Uzimamo samo prvu liniju zahteva, iz koje dobijamo HTTP method i putanju
            String requestLine = in.readLine();
            StringBuilder payload = new StringBuilder();

            StringTokenizer stringTokenizer = new StringTokenizer(requestLine);
            String method = stringTokenizer.nextToken();
            String path = stringTokenizer.nextToken();

            // Preskačemo sve ostale zaglavlja zahteva i tražimo prazan red koji označava kraj zaglavlja
            do {
                requestLine = in.readLine();
            } while (!requestLine.trim().equals(""));

            // Ako je metoda POST, čitamo telo zahteva
            if (method.equals(HttpMethod.POST.toString())) {
                while(in.ready()) {
                    payload.append((char) in.read());
                }
            }

            Request request = new Request(HttpMethod.valueOf(method), path, payload.toString());

            RequestHandler requestHandler = new RequestHandler();
            Response response = requestHandler.handle(request);

            out.println(response.getResponseString());

            in.close();
            out.close();
            client.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
