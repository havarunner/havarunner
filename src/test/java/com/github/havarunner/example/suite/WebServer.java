package com.github.havarunner.example.suite;

public class WebServer {
    boolean running;

    public WebServer() {
        running = true;
    }

    public void shutDown() {
        running = false;
    }

    public int httpStatus(String url) {
        return 200;
    }

    public String htmlDocument(String url) {
        return "<html><body><title>hello HawaRunner</title></body></html>";
    }

    public String htmlDocument(String url, String user) {
        return "<html><body><title>hello "+user+"</title></body></html>";
    }
}
