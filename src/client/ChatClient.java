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

            System.out.print("Ingresa tu nombre de usuario: ");
            String username = consoleInput.readLine();
            out.println("/username " + username);  // eenvio nombre de usuario al servidor

            System.out.println("Conectado al servidor de chat como " + username);

            // el hilo para leer los mensajes del servidor
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

            // envio de mensajes a usuarios o grupos
            String userInput;
            while ((userInput = consoleInput.readLine()) != null) {
                if (userInput.startsWith("/msg ")) {
                    // Enviar mensaje a un usuario específico
                    out.println(userInput);  // formato /msg username mensaje
                } else {
                    out.println(userInput);  // enviar mensaje al grupo
                }
            }

            // // Enviar mensajes con notas de voz (está bugueado)
            // String userInput;
            // while ((userInput = consoleInput.readLine()) != null) {
            //     if (userInput.equals("/voice")) {
            //         // Grabar una nota de voz y enviarla
            //         String audioFileName = "nota_de_voz.wav";
            //         recordVoice(audioFileName);
            //         sendAudio(socket, audioFileName);
            //     } else {
            //         out.println(userInput);
            //     }
            // }

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
                    Thread.sleep(10000);  // los 10 segundos
                    microphone.stop();
                    microphone.close();
                    System.out.println("Grabación completada.");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
    
            // guardar el archivo de audio
            AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, audioFile);
    
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    

    // enviar un archivo de audio al servidor
    private void sendAudio(Socket socket, String fileName) {
        try {
            File audioFile = new File(fileName);
            byte[] audioData = Files.readAllBytes(audioFile.toPath());

            // enviar el archivo al servidor
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
