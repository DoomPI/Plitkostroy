package pyroman.jigsawsockets.connection.server;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import pyroman.jigsawsockets.game.Game;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameServer {

    private final ServerSocket serverSocket;
    private final ExecutorService clientsThreadPool;
    private final ObservableList<ClientHandler> allClients;
    private final ExecutorService serverRunner;

    private final Game game;

    public GameServer(ServerSocket serverSocket, int numberOfPlayers) {
        this.serverSocket = serverSocket;
        clientsThreadPool = Executors.newFixedThreadPool(numberOfPlayers);
        serverRunner = Executors.newSingleThreadExecutor();
        allClients = FXCollections.observableArrayList();

        game = new Game();

        allClients.addListener((ListChangeListener<ClientHandler>) change -> {
            for (ClientHandler clientHandler : allClients) {
                clientHandler.requestUpdate();
            }

            if (allClients.size() == numberOfPlayers) {
                for (ClientHandler clientHandler : allClients) {
                    clientHandler.requestGameStart();
                }
            }

            if (allClients.isEmpty()) {
                close();
            }

            while (change.next()) {
                if (change.wasRemoved()) {
                    close();
                    break;
                }
            }
        });

    }

    public void start() {
        acceptNewClients();
    }

    private void acceptNewClients() {
        serverRunner.submit(() -> {
            try {
                while (!serverSocket.isClosed()) {
                    Socket socket = serverSocket.accept();

                    ClientHandler clientHandler = new ClientHandler(socket, this);
                    clientsThreadPool.submit(clientHandler);
                }
            } catch (IOException exception) {
                close();
            }
        });
    }

    public void close() {
        try {
            clientsThreadPool.shutdownNow();
            serverRunner.shutdownNow();

            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static class ClientHandler implements Runnable {

        private Socket socket;
        private BufferedReader messageReader;
        private PrintWriter messageWriter;
        private String nickname;
        private GameServer server;
        private volatile boolean finished;

        public ClientHandler(Socket socket, GameServer server) {
            try {
                this.server = server;
                this.messageReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String[] clientInfo = messageReader.readLine().split(" ");
                this.nickname = clientInfo[1];
                this.socket = socket;
                this.messageWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                finished = false;

                addClientToLobby();
            } catch (IOException exception) {
                removeClientFromServer();
                close();
            }
        }

        private void requestUpdate() {
            StringBuilder stringBuilder = new StringBuilder("UPDATE ");
            for (ClientHandler clientHandler : server.allClients) {
                stringBuilder.append(clientHandler.nickname).append(" ");
            }
            messageWriter.println(stringBuilder);
        }

        private void requestGameStart() {
            messageWriter.println("START " + server.game.getPlayingTime().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            messageWriter.println("OPPONENT " + getPartnerNickname());
        }

        private String getPartnerNickname() {
            String partnerNickname = null;
            for (ClientHandler clientHandler : server.allClients) {
                if (!clientHandler.nickname.equals(nickname)) {
                    partnerNickname = clientHandler.nickname;
                }
            }
            return partnerNickname;
        }

        private void requestFigureByIndex(int index) {
            messageWriter.println("FIGURE " + server.game.getFigureDataByIndex(index));
        }

        private void registerPlayerData(String gameResult) {
            finished = true;
            broadcastMessage("FINISHED " + gameResult);

            if (allFinished()) {
                for (ClientHandler clientHandler : server.allClients) {
                    clientHandler.messageWriter.println("STOP GAME");
                }
            }
        }

        private boolean allFinished() {
            for (ClientHandler clientHandler : server.allClients) {
                if (!clientHandler.finished) {
                    return false;
                }
            }

            return true;
        }

        private void addClientToLobby() {
            server.allClients.add(this);
        }

        @Override
        public void run() {
            handleMessages();
        }

        private void handleMessages() {
            String messageToSend;
            try {
                while (((messageToSend = messageReader.readLine()) != null)) {
                    if (messageToSend.startsWith("FIGURE ")) {
                        requestFigureByIndex(Integer.parseInt(messageToSend.split(" ")[1]));
                    } else if (messageToSend.startsWith("RESULT ")) {
                        registerPlayerData(messageToSend.substring(7));
                    } else if (messageToSend.startsWith("TIME ")) {
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                        server.game.setPlayingTime(LocalTime.parse(messageToSend.split(" ")[1], dateTimeFormatter));
                    }
                }
            } catch (IOException exception) {
                broadcastMessage("LOST " + nickname);
            } finally {
                removeClientFromServer();
                close();
            }
        }

        private void broadcastMessage(String messageToSend) {
            for (ClientHandler clientHandler : server.allClients) {
                if (!clientHandler.nickname.equals(nickname)) {
                    clientHandler.messageWriter.println(messageToSend);
                }
            }
        }

        private void removeClientFromServer() {
            if (nickname != null) {
                server.allClients.remove(this);
            }
        }

        public void close() {
            try {
                if (socket != null) {
                    socket.close();
                }

                if (messageReader != null) {
                    messageReader.close();
                }
                if (messageWriter != null) {
                    messageWriter.close();
                }

            } catch (IOException exception) {
                throw new UncheckedIOException(exception);
            }
        }
    }
}
