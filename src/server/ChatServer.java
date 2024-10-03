package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    private static final int PORT = 12345;
    private List<ClientHandler> clients = new ArrayList<>();
    private GroupManager groupManager;

    public ChatServer() {
        groupManager = new GroupManager();  
    }
    public static void main(String[] args) {
        new ChatServer().startServer();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor de chat iniciado en el puerto " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuevo cliente conectado");

                // hay un manejador para cada cliente y lo agrega a la lista
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);

                // Un hilo pra manejar cada cliente
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Mensajes del servidor a todos los clientes
    public synchronized void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    // Enviar mensaje privados
    public synchronized void sendPrivateMessage(String senderUsername, String targetUsername, String message) {
        for (ClientHandler client : clients) {
            if (client.getUsername() != null && client.getUsername().equals(targetUsername)) {
                client.sendMessage("Mensaje privado de " + senderUsername + ": " + message);
                return;
            }
        }
        // Validacipon si el usuario no está conectado
        for (ClientHandler client : clients) {
            if (client.getUsername().equals(senderUsername)) {
                client.sendMessage("Usuario '" + targetUsername + "' no está disponible.");
            }
        }
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public GroupManager getGroupManager() {
        return groupManager;
    }
}
