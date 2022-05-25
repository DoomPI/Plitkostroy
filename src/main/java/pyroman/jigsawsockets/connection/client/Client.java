package pyroman.jigsawsockets.connection.client;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class Client implements AutoCloseable {

    private BufferedReader callbackReader;
    private PrintWriter requestWriter;
    private Socket socket;

    private Map<String, Consumer<String>> clientRequestHandlers;
    private Map<String, Consumer<String>> serverCallbackHandlers;

    private ExecutorService inboxThreadExecutor;

    public Client(Socket socket, String nickname,
                  boolean isHost,
                  Map<String, Consumer<String>> serverCallbackHandlers,
                  List<String> clientRequestKeys) {
        try {
            callbackReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            requestWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            requestWriter.println(isHost ? "HOST " + nickname : "CLIENT " + nickname);

            this.serverCallbackHandlers = serverCallbackHandlers;
            this.socket = socket;
            inboxThreadExecutor = Executors.newSingleThreadExecutor();
            clientRequestHandlers = new HashMap<>();

            for (String key : clientRequestKeys) {
                addNewClientRequestHandler(key);
            }

        } catch (IOException exception) {
            close();
        }
    }

    private void addNewClientRequestHandler(String requestKey) {
        clientRequestHandlers.put(requestKey, request -> {
            if (!socket.isClosed()) {
                requestWriter.println(request);
            } else {
                close();
                if (serverCallbackHandlers.containsKey("SERVER_CLOSED")) {
                    serverCallbackHandlers.get("SERVER_CLOSED").accept("SERVER_CLOSED");
                }
            }
        });
    }

    public void addNewServerCallbackHandler(String requestKey, Consumer<String> serverRequestHandler) {
        if (!serverCallbackHandlers.containsKey(requestKey)) {
            serverCallbackHandlers.put(requestKey, serverRequestHandler);
        }
    }

    private void acceptIncomingServerRequests() {
        inboxThreadExecutor.submit(() -> {
            String requestToRead;
            try {
                while ((requestToRead = callbackReader.readLine()) != null) {
                    String requestKey = requestToRead.trim().split(" ")[0];

                    if (serverCallbackHandlers.containsKey(requestKey)) {
                        String requestBody = requestToRead.trim().substring(requestKey.length() + 1);
                        serverCallbackHandlers.get(requestKey).accept(requestBody);
                    }
                }
            } catch (IOException exception) {
                close();
            }
        });
    }

    public void lobby() {
        acceptIncomingServerRequests();
    }

    public void sendRequestToServer(String request) {
        String requestKey = request.trim().split(" ")[0];

        if (clientRequestHandlers.containsKey(requestKey)) {
            clientRequestHandlers.get(requestKey).accept(request);
        }
    }

    public void close() {
        try {
            if (inboxThreadExecutor != null) {
                inboxThreadExecutor.shutdownNow();
            }
            if (socket != null) {
                socket.close();
            }
            if (callbackReader != null) {
                callbackReader.close();
            }
            if (requestWriter != null) {
                requestWriter.close();
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }

    }

    @Override
    public String toString() {
        return "lobbyClient";
    }
}
