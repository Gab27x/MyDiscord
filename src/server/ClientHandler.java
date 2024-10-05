package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.io.DataOutputStream;

import java.util.HashSet;
import java.util.Set;

public class ClientHandler implements Runnable{

    private Socket clientSocket;
    private ChatServer server;
    private PrintWriter out;
    private String username;  

    private String currentGroup; 

    private DataOutputStream dataOut;  // OutputStream para enviar datos binarios como audio

    private CallManager callManager;


    public ClientHandler(Socket socket, ChatServer server) {
        this.clientSocket = socket;
        this.server = server;
        this.currentGroup = null;
        this.callManager=new CallManager(); //TODO no creo que esto este bien
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
                } else if(message.startsWith("/call")){
                    String groupName = message.split(" ", 2)[1];
                    callManager.startCall(groupName, this); //TODO ALGUIEN REVISE ESTO
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

    public void sendAudioData(byte[] audioData) {
        try {
            // Primero enviamos la longitud del paquete de audio
            dataOut.writeInt(audioData.length);
            // Luego enviamos los bytes de audio
            dataOut.write(audioData);
            dataOut.flush();  // Asegurarnos de que los datos se envían de inmediato
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para notificar a los participantes sobre una llamada entrante
    public void notifyIncomingCall(String initiator, String groupName) {
        // Obtener los participantes del grupo del GroupManager
        Set<ClientHandler> participants = server.getGroupManager().getGroupMembers(groupName); //TODO Cambio esto a Set, no se si sea importante la diferencia entre Set y List

        // Notificar a cada participante (excepto al iniciador) sobre la llamada entrante
        for (ClientHandler participant : participants) {
            if (!participant.getUsername().equals(initiator)) {  // No notificar al iniciador de la llamada
                participant.sendMessage("Llamada entrante de " + initiator + " en el grupo '" + groupName + "'.");
            }
        }
    }
    
}
