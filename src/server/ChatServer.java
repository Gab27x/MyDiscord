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

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ChatServer server;
    private PrintWriter out;
    private String username;  

    private String currentGroup; 


    public ClientHandler(Socket socket, ChatServer server) {
        this.clientSocket = socket;
        this.server = server;
        this.currentGroup = null;
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);

            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("/username ")) {
                    this.username = message.split(" ", 2)[1];
                    sendMessage("Tu nombre de usuario es: " + this.username);
                } else if (message.startsWith("/msg ")) {
                    // Enviar mensaje a un usuario específico
                    String[] splitMessage = message.split(" ", 3);
                    String targetUsername = splitMessage[1];
                    String privateMessage = splitMessage[2];
                    server.sendPrivateMessage(this.username, targetUsername, privateMessage);
                } else if (message.startsWith("/create ")) {
                    String groupName = message.split(" ", 2)[1];
                    if (server.getGroupManager().createGroup(groupName)) {
                        sendMessage("Grupo '" + groupName + "' creado exitosamente.");
                    } else {
                        sendMessage("El grupo '" + groupName + "' ya existe.");
                    }
                } else if (message.startsWith("/join ")) {
                    String groupName = message.split(" ", 2)[1];
                    if (server.getGroupManager().addUserToGroup(groupName, this)) {
                        currentGroup = groupName;
                        sendMessage("Te has unido al grupo '" + groupName + "'.");
                    } else {
                        sendMessage("El grupo '" + groupName + "' no existe.");
                    }
                } 
                
                // else if (message.startsWith("/audio ")) {
                //     // Recibir el archivo de audio
                //     DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                //     String audioFileName = message.split(" ", 2)[1];
                //     int fileLength = dis.readInt();
                //     byte[] audioData = new byte[fileLength];
                //     dis.readFully(audioData);  // Asegúrate de leer solo los binarios correctamente
                
                //     // Guardar el archivo de audio
                //     server.getGroupManager().saveAudioFile(currentGroup, audioFileName, audioData);
                
                //     // Notificar al grupo que se ha enviado una nota de voz
                //     server.getGroupManager().broadcastToGroup(currentGroup, "Nota de voz enviada: " + audioFileName, this);
                // }


                 else if (currentGroup != null) {
                    String formattedMessage = "[" + currentGroup + "] " + this.username + ": " + message;
                    server.getGroupManager().broadcastToGroup(currentGroup, formattedMessage, this);
                } else {
                    sendMessage("Debes unirte a un grupo primero.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            server.removeClient(this);
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public String getUsername() {
        return username;
    }
}
