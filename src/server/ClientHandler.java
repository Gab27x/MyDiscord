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

    private DataOutputStream dataOut;  // OutputStream para enviar datos binarios como audio

    private CallManager callManager;


    public ClientHandler(Socket socket, ChatServer server) {
        this.clientSocket = socket;
        this.server = server;
        this.callManager=new CallManager(); //TODO no creo que esto este bien
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);

            String message;
            while ((message = in.readLine()) != null) {

                if (message.startsWith("/username ")) usernameCommand(message);

                else if (message.startsWith("/msg ")) msgCommand(message);
                
                else if (message.startsWith("/create ")) createCommand(message);
                
                else if (message.startsWith("/join ")) joinCommand(message);
                
                else if(message.startsWith("/call")) callCommand(message);
                
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


                //  else if (currentGroup != null) {
                //     String formattedMessage = "[" + currentGroup + "] " + this.username + ": " + message;
                //     server.getGroupManager().broadcastToGroup(currentGroup, formattedMessage, this);
                // } 

                else {
                    sendMessage("Comando no válido.");
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

    // Comandos del cliente ----------------------------------------

    public void usernameCommand(String message) {
        this.username = message.split(" ", 2)[1];
        sendMessage("[SERVER]: "+"Usuario registrado exitosamente con username " + this.username);
    }

    public void msgCommand(String message) {
        String[] splitMessage = message.split(" ", 3);
        String targetChat = splitMessage[1];
        String privateMessage = splitMessage[2];

        boolean belongsToGroup = server.getGroupManager().getGroupMembers(targetChat).contains(this);

        if (belongsToGroup) { // Si target es un grupo al que pertenece
            sendMessage("[SERVER]: "+"Mensaje enviado al grupo '" + targetChat + "'.");
            server.getGroupManager().broadcastToGroup(targetChat, this.username + ": " + privateMessage, this);
        } else{ // Puede ser un usuario o un grupo al que no pertenece
            server.sendPrivateMessage(this.username, targetChat, privateMessage);
        }
    }

    public void createCommand(String message) {
        String groupName = message.split(" ", 2)[1];
        if (server.getGroupManager().createGroup(groupName)) {
            sendMessage("[SERVER]: "+"Grupo '" + groupName + "' creado exitosamente.");
        } else {
            sendMessage("[SERVER]: "+"El grupo '" + groupName + "' ya existe.");
        }
    }

    public void joinCommand(String message) {
        String groupName = message.split(" ", 2)[1];
        if (server.getGroupManager().addUserToGroup(groupName, this)) {
            sendMessage("[SERVER]: "+"Te has unido al grupo '" + groupName + "'.");
        } else {
            sendMessage("[SERVER]: "+"El grupo '" + groupName + "' no existe.");
        }
    }

    public void callCommand(String message) {
        String groupName = message.split(" ", 2)[1];
        notifyIncomingCall(this.username, groupName);
    }

    // Utilidades -------------------------------------------------
    public void sendMessage(String message) {
        out.println("\n" + message);
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
