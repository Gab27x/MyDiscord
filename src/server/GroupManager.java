package server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.io.IOException;

public class GroupManager {

    // asocia el nombre del grupo con los usuarios dentro del grupo
    private Map<String, Set<ClientHandler>> groups;

    private FileManager fileManager;


    public GroupManager() {
        groups = new HashMap<>();
        fileManager = FileManager.getInstance();  
    }

    // Crear un nuevo grupo
    public synchronized boolean createGroup(String groupName) {
        if (!groups.containsKey(groupName)) {
            groups.put(groupName, new HashSet<>());
            fileManager.createGroupDirectories(groupName);  // carpets para el grupo
            return true;
        }
        return false;
    }

    // Añadir un usuario a un grupo
    public synchronized boolean addUserToGroup(String groupName, ClientHandler client) {
        Set<ClientHandler> group = groups.get(groupName);
        if (group != null) {
            group.add(client);
            try {
                // Enviar el historial del chat al cliente cuando se une
                String history = fileManager.readChatHistory(groupName);
                client.sendMessage("-------- Historial del chat - '" + groupName + "'------------\n" + history);
            } catch (IOException e) {
                client.sendMessage("Error al cargar el historial del grupo.");
            }
            return true;
        }
        return false;
    }

    // Enviar un mensaje a todos los miembros del grupo y guardarlo en el historial
    public synchronized void broadcastToGroup(String groupName, String message, ClientHandler sender) {
        Set<ClientHandler> group = groups.get(groupName);
        if (group != null) {
            try {
                fileManager.writeChatMessage(groupName, message);  // Guardar el mensaje en el historial
            } catch (IOException e) {
                sender.sendMessage("Error al guardar el mensaje en el historial.");
            }

            for (ClientHandler client : group) {
                if (client != sender) {
                    client.sendMessage(message);
                }
            }
        }
    }

    // Guardar un archivo de audio en el historial del grupo
    public synchronized void saveAudioFile(String groupName, String audioFileName, byte[] audioData) {
        try {
            fileManager.saveAudioFile(groupName, audioFileName, audioData);

            // Registrar el archivo de audio en el historial de mensajes del grupo
            String audioReference = "Nota de voz enviada: " + audioFileName;
            fileManager.writeChatMessage(groupName, audioReference);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //el synchronized indica que la funcion solo puede ser ejecutada por un hilo a la vez (el hilo que esta manejando esa llamada)
    //permite obtener la lista de los nombres de los miembros del grupo, para luego notificarlos de una llamada
    public synchronized Set<ClientHandler> getGroupMembers(String groupName) {
        return groups.getOrDefault(groupName, new HashSet<>());  // Devuelve el conjunto de miembros del grupo o un conjunto vacío si no existe
    }

    // Obtener historial de chat de un grupo
    public synchronized String getChatHistory(String groupName) {
        try {
            return fileManager.readChatHistory(groupName);
        } catch (IOException e) {
            return "Error al cargar el historial del grupo.";
        }
    }

}
