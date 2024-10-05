package server;

import java.net.*;
import java.io.*;
import java.util.*;

public class CallManager {

    private Map<String, List<ClientHandler>> activeCalls; // Almacena los usuarios activos en una llamada

    public CallManager() {
        activeCalls = new HashMap<>();
    }

    // Iniciar una llamada
    public void startCall(String groupName, ClientHandler initiator) throws IOException {
        if (!activeCalls.containsKey(groupName)) {
            activeCalls.put(groupName, new ArrayList<>());
        }
        activeCalls.get(groupName).add(initiator);

        // Notificar a todos los participantes del grupo
        for (ClientHandler participant : activeCalls.get(groupName)) {
            if (!participant.equals(initiator)) {
                participant.notifyIncomingCall(initiator.getUsername(), groupName);
            }
        }
    }

    // Enviar el audio entre los participantes
    public void handleCallData(String groupName, byte[] audioData, ClientHandler sender) {
        if (activeCalls.containsKey(groupName)) {
            for (ClientHandler participant : activeCalls.get(groupName)) {
                if (!participant.equals(sender)) {
                    participant.sendAudioData(audioData);
                }
            }
        }
    }

    // Finalizar una llamada
    public void endCall(String groupName, ClientHandler caller) {
        if (activeCalls.containsKey(groupName)) {
            activeCalls.get(groupName).remove(caller);
            if (activeCalls.get(groupName).isEmpty()) {
                activeCalls.remove(groupName);
            }
        }
    }
}
