package server;

import java.io.*;
import java.nio.file.*;
import java.util.List;

public class FileManager {
    
    // Lee el historial de chat de un archivo y devuelve su contenido
    public String readChatHistory(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    // Escribe un nuevo mensaje en el historial de chat
    public void writeChatMessage(String filePath, String message) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(message + "\n");
        }
    }

    // Guarda un archivo de audio en la carpeta correspondiente
    public void saveAudioFile(String filePath, byte[] audioData) throws IOException {
        Files.write(Paths.get(filePath), audioData);
    }

    // Lee un archivo JSON (por ejemplo, grupos.json) y lo convierte en una cadena
    public String readJsonFile(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    // Escribe datos JSON en un archivo
    public void writeJsonFile(String filePath, String jsonData) throws IOException {
        Files.write(Paths.get(filePath), jsonData.getBytes());
    }
    
    // Sincronización para evitar conflictos en operaciones de escritura
    public synchronized void synchronizedWriteChatMessage(String filePath, String message) throws IOException {
        writeChatMessage(filePath, message);
    }

}
