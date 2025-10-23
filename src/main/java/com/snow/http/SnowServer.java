package com.snow.http;

import com.snow.exceptions.BadRequestException;
import com.snow.http.models.HttpContent;
import com.snow.http.models.HttpHeaders;
import com.snow.util.CommonUtil;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SnowServer {

    private static final Logger logger = Logger.getLogger(SnowServer.class.getName());
    private final int port;
    private final ExecutorService executor;
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    private static SnowServer instance;

    public static synchronized SnowServer create(int port, int numThreads) {
        if (instance != null) {
            logger.log(Level.WARNING, "Server already exists with port: " + instance.port);
            return instance;
        }
        instance = new SnowServer(port, numThreads);
        return instance;
    }

    private SnowServer(int port, int numThreads) {
        this.port = port;
        this.executor = Executors.newFixedThreadPool(numThreads);
    }

    public synchronized void start() throws IOException {
        running = true;
        serverSocket = new ServerSocket(port);
        logger.info("Server started on port: " + port);
        try {
            while (running) {
                Socket socket = serverSocket.accept();
                executor.submit(() -> handleClient(socket));
            }
        } catch (IOException ex) {
            if (running) {
                logger.log(Level.SEVERE, "Accept failed unexpectedly", ex);
            }
        } finally {
            if (running) {
                stop();
            }
        }
    }

    private void handleClient(Socket socket) {
        try (InputStream in = socket.getInputStream();
             OutputStream out = socket.getOutputStream()) {

            String[] requestLineParts = parseRequestMethod(in);
            String method = requestLineParts[0];
            String path = requestLineParts[1];

            HttpHeaders headers = parseRequestHeaders(in);
            HttpContent body = parseRequestBody(headers, in);
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage());
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid header format. Content-Length must be an integer.");
        }
    }

    private String[] parseRequestMethod(InputStream in) throws IOException {
        ByteArrayOutputStream lineBuffer = new ByteArrayOutputStream();
        int prev = -1, curr;
        while ((curr = in.read()) != -1) {
            lineBuffer.write(curr);
            if (prev == '\r' && curr == '\n') {
                break;
            }
            prev = curr;
        }
        String method = lineBuffer.toString(StandardCharsets.US_ASCII);
        return method.trim().split(" ");
    }

    private HttpHeaders parseRequestHeaders(InputStream in) throws IOException {
        ByteArrayOutputStream headerStream = new ByteArrayOutputStream();
        int prev = -1, curr;
        while ((curr = in.read()) != -1) {
            headerStream.write(curr);
            if (prev == '\r' && curr == '\n') {
                byte[] asByteArray = headerStream.toByteArray();
                if (CommonUtil.isHeaderTerminator(asByteArray)) {
                    break;
                }
            }
            prev = curr;
        }
        return new HttpHeaders(headerStream.toByteArray());
    }

    private HttpContent parseRequestBody(HttpHeaders headers, InputStream in) throws IOException {
        var contentLengthHeader = headers.getHeader("Content-Length");
        System.out.println(contentLengthHeader);
        if (contentLengthHeader != null && !contentLengthHeader.equals("0")) {
            int contentLength = Integer.parseInt(contentLengthHeader);
            byte[] asByteArray = in.readNBytes(contentLength);
            return new HttpContent(asByteArray);
        }
        return null;
    }

    public void stop() {
        if (!running) {
            logger.info("Server not running, stop failed");
            return;
        }
        logger.info("Stopping server...");
        running = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            CommonUtil.gracefulShutdown(executor);
            logger.info("Server stopped");
        }
    }

    public synchronized void dispose() {
        if (instance != this) {
            return;
        }
        stop();
        instance = null;
    }
}
