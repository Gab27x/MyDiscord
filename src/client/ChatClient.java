package client;

import javax.sound.sampled.*;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

public class ChatClient {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        new ChatClient().startClient();
    }

    public void startClient() {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Conectado al servidor de chat");

            // Hilo para leer los mensajes del servidor
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("Mensaje del servidor: " + message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // Enviar mensajes o notas de voz
            String userInput;
            while ((userInput = consoleInput.readLine()) != null) {
                if (userInput.equals("/voice")) {
                    // Grabar una nota de voz y enviarla
                    String audioFileName = "nota_de_voz.wav";
                    recordVoice(audioFileName);
                    sendAudio(socket, audioFileName);
                } else {
                    out.println(userInput);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void recordVoice(String fileName) {
        try {
            AudioFormat format = new AudioFormat(16000, 16, 1, true, true);
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            TargetDataLine microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(format);
    
            System.out.println("Grabando... (10 segundos)");
    
            microphone.start();
            AudioInputStream audioStream = new AudioInputStream(microphone);
    
            File audioFile = new File(fileName);
    
            // Crear un nuevo hilo para detener la grabación después de 10 segundos
            new Thread(() -> {
                try {
                    Thread.sleep(10000);  // Grabar por 10 segundos
                    microphone.stop();
                    microphone.close();
                    System.out.println("Grabación completada.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
    
            // Guardar el archivo de audio
            AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, audioFile);
    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    // Método para enviar un archivo de audio al servidor
    private void sendAudio(Socket socket, String fileName) {
        try {
            File audioFile = new File(fileName);
            byte[] audioData = Files.readAllBytes(audioFile.toPath());

            // Enviar el archivo al servidor
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            dos.writeUTF("/audio " + audioFile.getName());  // Comando para el servidor
            dos.writeInt(audioData.length);
            dos.write(audioData);

            System.out.println("Nota de voz enviada");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
