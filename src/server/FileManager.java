package server;

import java.io.*;
import java.nio.file.*;
import com.google.gson.Gson;


public class FileManager {


    private Gson gson = new Gson();


    /*private static FileManager instance = null;

    private final String dataDirectory = "data/";
    private final String gruposDirectory = dataDirectory + "grupos/";
    private final String historialDirectory = dataDirectory + "historial/";

    private FileManager() {
        createDirectories();
    }

    public static FileManager getInstance() {
        if (instance == null) {
            instance = new FileManager();
        }
        return instance;
    }

    private void createDirectories() {
        try {
            Files.createDirectories(Paths.get(gruposDirectory));
            Files.createDirectories(Paths.get(historialDirectory));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // estructura para cada grupo nuevo
    public void createGroupDirectories(String groupName) {
        String groupPath = historialDirectory + groupName + "/";
        try {
            Files.createDirectories(Paths.get(groupPath));
            Files.createFile(Paths.get(groupPath + "historial de chat.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Escribe un nuevo mensaje en el historial de chat
    public void writeChatMessage(String groupName, String message) throws IOException {
        String filePath = historialDirectory + groupName + "/historial de chat.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            writer.write(message + "\n");
        }
    }

    // Lee el historial de chat de un grupo
    public String readChatHistory(String groupName) throws IOException {
        String filePath = historialDirectory + groupName + "/historial de chat.txt";
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }

    public void saveAudioFile(String groupName, String audioFileName, byte[] audioData) throws IOException {
        String audioDirectory = historialDirectory + groupName + "/audio/";
        Files.createDirectories(Paths.get(audioDirectory));  // Crear la carpeta audio/
        String filePath = audioDirectory + audioFileName;
        Files.write(Paths.get(filePath), audioData);
    }



    // Leer un archivo de audio (para enviar a un cliente)
    public byte[] readAudioFile(String groupName, String audioFileName) throws IOException {
        String filePath = historialDirectory + groupName + "/audio/" + audioFileName;
        return Files.readAllBytes(Paths.get(filePath));
    }
*/
}
