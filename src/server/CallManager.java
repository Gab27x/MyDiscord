package server;

import java.util.*;

public class CallManager {

    private Map<String, List<ClientHandler>> activeCalls; // Almacena los usuarios activos en una llamada

    public CallManager() {
        activeCalls = new HashMap<>();
    }

    // Iniciar una llamada
    public void startCall(String groupName, ClientHandler initiator) {
        // Si no hay una llamada activa, la iniciamos
        if (!activeCalls.containsKey(groupName)) {
            activeCalls.put(groupName, new ArrayList<>());
            initiator.sendMessage("[SERVER]: Llamada iniciada en el grupo '" + groupName + "'.");
        }
        
        // Agregamos al iniciador a la llamada
        activeCalls.get(groupName).add(initiator);
        initiator.sendMessage("[SERVER]: Te has unido a la llamada en el grupo '" + groupName + "'.");
    }

    // Enviar el audio entre los participantes
    public void handleCallData(String groupName, byte[] audioData, ClientHandler sender) {
        if (activeCalls.containsKey(groupName)) {
            for (ClientHandler participant : activeCalls.get(groupName)) {
                if (!participant.equals(sender)) {
                    participant.sendAudioData(audioData);  // Enviar el audio a otros participantes
                }
            }
        }
    }

    // Finalizar una llamada
    public void endCall(String groupName, ClientHandler caller) {
        if (activeCalls.containsKey(groupName)) {
            activeCalls.get(groupName).remove(caller);  // Eliminar al usuario de la llamada
            caller.sendMessage("[SERVER]: Has salido de la llamada en el grupo '" + groupName + "'.");

            // Si ya no quedan participantes, eliminamos la llamada
            if (activeCalls.get(groupName).isEmpty()) {
                activeCalls.remove(groupName);
                caller.sendMessage("[SERVER]: La llamada en el grupo '" + groupName + "' ha terminado.");
            }
        }
    }

    // Verifica si un usuario está en una llamada en un grupo
    public boolean isInCall(String groupName, ClientHandler user) {
        return activeCalls.containsKey(groupName) && activeCalls.get(groupName).contains(user);
    }

}
