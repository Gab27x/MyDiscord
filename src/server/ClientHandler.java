package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.nio.file.Files;

public class ClientHandler implements Runnable {

    private Socket clientSocket;
    private ChatServer server;
    private PrintWriter out;
    private String username;

    private DataInputStream dataIn; // InputStream para recibir datos binarios como audio
    private DataOutputStream dataOut; // OutputStream para enviar datos binarios como audio

    private CallManager callManager;

    public ClientHandler(Socket socket, ChatServer server) {
        this.clientSocket = socket;
        this.server = server;
        this.callManager = new CallManager();
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            this.dataIn = new DataInputStream(clientSocket.getInputStream()); // Para recibir los datos binarios

            String message;
            while ((message = in.readLine()) != null) {

                if (message.startsWith("/username "))
                    usernameCommand(message);

                else if (message.startsWith("/msg "))
                    msgCommand(message);

                else if (message.startsWith("/audio ")) {
                    receiveAudio(message);
                }

                else if (message.startsWith("/listen "))
                    listenCommand(message);

                else if (message.startsWith("/create "))
                    createCommand(message);

                else if (message.startsWith("/join "))
                    joinCommand(message);

                else if (message.startsWith("/call"))
                    callCommand(message);

                else if (message.startsWith("/history "))
                    historyCommand(message);

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
        sendMessage("[SERVER]: Usuario registrado exitosamente con username " + this.username);
    }

    public void msgCommand(String message) {
        String[] splitMessage = message.split(" ", 3);
        String targetChat = splitMessage[1];
        String privateMessage = splitMessage[2];

        boolean belongsToGroup = server.getGroupManager().getGroupMembers(targetChat).contains(this);

        if (belongsToGroup) { // Si target es un grupo al que pertenece
            sendMessage("[SERVER]: Mensaje enviado al grupo '" + targetChat + "'.");
            server.getGroupManager().broadcastToGroup(targetChat, this.username + ": " + privateMessage, this);
        } else { // Puede ser un usuario o un grupo al que no pertenece
            server.sendPrivateMessage(this.username, targetChat, privateMessage);
        }
    }

    public void receiveAudio(String message) { // TODO: Se sigue guardando el audio asi no pertenezca al grupo
        String[] splitMessage = message.split(" ", 3);
        String targetChat = splitMessage[1];
        String audioID = "Audio" + Calendar.getInstance().getTimeInMillis() + "_" + targetChat;
        boolean belongsToGroup = server.getGroupManager().getGroupMembers(targetChat).contains(this);

        String path = "data/historial/" + targetChat + "/audio/";

        if (belongsToGroup) { // Si target es un grupo al que pertenece
            sendMessage("[SERVER]: Nota de voz enviada al grupo '" + targetChat + "'.");
            server.getGroupManager().broadcastToGroup(targetChat, this.username + ": " + audioID, this);
        } else { // Puede ser un usuario o un grupo al que no pertenece
            server.sendPrivateMessage(this.username, targetChat, audioID);
        }

        // Verificar si contiene el nombre del archivo de audio
        if (splitMessage.length < 3) {
            sendMessage("[SERVER]: Debes proporcionar el nombre del chat.");
            return;
        }

        try {
            // Recibir el tamaño del archivo de audio
            int fileSize = dataIn.readInt();
            byte[] audioData = new byte[fileSize];

            // Leer los datos binarios del archivo de audio
            dataIn.readFully(audioData);

            // Guardar el archivo recibido en el servidor
            File audioFile = new File(path + audioID + ".wav");
            try (FileOutputStream fos = new FileOutputStream(audioFile)) {
                fos.write(audioData);
            }

            sendMessage("[SERVER]: Nota de voz recibida y almacenada como " + audioID + ".wav");

        } catch (IOException e) {
            sendMessage("[SERVER]: Error al recibir nota de voz.");
            e.printStackTrace();
        }
    }

    public void listenCommand(String message) {
        String[] splitMessage = message.split(" ", 2);
        String audioID = splitMessage[1];
        String path = "data/historial/" + audioID.split("_")[1] + "/audio/" + audioID + ".wav";

        // Verificar si tiene acceso al chat
        if (!server.getGroupManager().getGroupMembers(audioID.split("_")[1]).contains(this)) {
            sendMessage("[SERVER]: No tienes acceso a este chat.");
            return;
        }

        File audioFile = new File(path);

        if (!audioFile.exists()) {
            sendMessage("[SERVER]: El archivo de audio no existe.");
            return;
        }

        try {
            byte[] audioData = Files.readAllBytes(audioFile.toPath());
            sendAudioData(audioData); // Enviar los datos de audio al cliente
            sendMessage("[SERVER]: Nota de voz enviada correctamente.");
        } catch (IOException e) {
            sendMessage("[SERVER]: Error al enviar la nota de voz.");
            e.printStackTrace();
        }
    }

    public void createCommand(String message) {
        String groupName = message.split(" ", 2)[1];
        if (server.getGroupManager().createGroup(groupName)) {
            sendMessage("[SERVER]: Grupo '" + groupName + "' creado exitosamente.");
            // Unirse automáticamente al grupo recién creado
            joinCommand("/join " + groupName);

        } else {
            sendMessage("[SERVER]: El grupo '" + groupName + "' ya existe.");
        }
    }

    public void joinCommand(String message) {
        String groupName = message.split(" ", 2)[1];
        if (server.getGroupManager().addUserToGroup(groupName, this)) {
            sendMessage("[SERVER]: Te has unido al grupo '" + groupName + "'.");
        } else {
            sendMessage("[SERVER]: El grupo '" + groupName + "' no existe.");
        }
    }

    public void callCommand(String message) {
        String groupName = message.split(" ", 2)[1];
        notifyIncomingCall(this.username, groupName);
    }

    public void historyCommand(String message) {
        String groupName = message.split(" ", 2)[1];

        // Verificar si tiene acceso al chat
        if (!server.getGroupManager().getGroupMembers(groupName).contains(this)) {
            sendMessage("[SERVER]: No tienes acceso a este chat.");
            return;
        }

        String history = server.getGroupManager().getChatHistory(groupName);
        sendMessage("-------- Historial del chat - '" + groupName + "'------------\n" + history);
    }

    // Utilidades -------------------------------------------------

    public void sendMessage(String message) {
        out.println("\n" + message);
    }

    public String getUsername() {
        return username;
    }

    // Método para notificar a los participantes sobre una llamada entrante
    public void notifyIncomingCall(String initiator, String groupName) {
        Set<ClientHandler> participants = server.getGroupManager().getGroupMembers(groupName);

        for (ClientHandler participant : participants) {
            if (!participant.getUsername().equals(initiator)) {
                participant.sendMessage("Llamada entrante de " + initiator + " en el grupo '" + groupName + "'.");
            }
        }
    }

    public void sendAudioData(byte[] audioData) {
        try {
            if (audioData.length == 0) {
                sendMessage("[SERVER]: El archivo de audio está vacío.");
                return;
            }

            // Enviar la longitud del archivo de audio
            dataOut.writeInt(audioData.length);
            // Enviar los datos de audio
            dataOut.write(audioData);
            dataOut.flush(); // Asegurarse de que los datos se envían de inmediato

        } catch (IOException e) {
            sendMessage("[SERVER]: Error al enviar la nota de voz.");
            e.printStackTrace();
        }
    }
}
