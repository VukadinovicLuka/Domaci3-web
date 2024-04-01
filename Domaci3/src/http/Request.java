package http;

import http.HttpMethod;

public class Request {

    private final HttpMethod httpMethod;

    private final String path;

    private final String body;

    public Request(HttpMethod httpMethod, String path, String body) {
        this.httpMethod = httpMethod;
        this.path = path;
        this.body = body;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public String getPath() {
        return path;
    }

    public String getBody() {
        return body;
    }
}
