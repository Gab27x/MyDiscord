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
        groupManager = new GroupManager();  // Inicializar el gestor de grupos
    }
    public static void main(String[] args) {
        new ChatServer().startServer();
    }

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Servidor de chat iniciado en el puerto " + PORT);

            while (true) {
                // Acepta una nueva conexión
                Socket clientSocket = serverSocket.accept();
                System.out.println("Nuevo cliente conectado");

                // Crea un nuevo manejador para este cliente y lo agrega a la lista
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);

                // Inicia un nuevo hilo para manejar la conexión del cliente
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Envía un mensaje a todos los clientes conectados
    public synchronized void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    // Elimina un cliente de la lista
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

    private String currentGroup;  // El grupo al que el cliente está actualmente unido


    public ClientHandler(Socket socket, ChatServer server) {
        this.clientSocket = socket;
        this.server = server;
        this.currentGroup = null;  // Inicialmente, el cliente no está en ningún grupo
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);

            String message;
            while ((message = in.readLine()) != null) {
                if (message.startsWith("/create ")) {
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
                } else if (message.startsWith("/audio ")) {
                    // Recibir el archivo de audio
                    DataInputStream dis = new DataInputStream(clientSocket.getInputStream());
                    String audioFileName = message.split(" ", 2)[1];
                    int fileLength = dis.readInt();
                    byte[] audioData = new byte[fileLength];
                    dis.readFully(audioData);  // Asegúrate de leer solo los binarios correctamente
                
                    // Guardar el archivo de audio
                    server.getGroupManager().saveAudioFile(currentGroup, audioFileName, audioData);
                
                    // Notificar al grupo que se ha enviado una nota de voz
                    server.getGroupManager().broadcastToGroup(currentGroup, "Nota de voz enviada: " + audioFileName, this);
                }
                 else if (currentGroup != null) {
                    String formattedMessage = "[" + currentGroup + "] " + message;
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
}
