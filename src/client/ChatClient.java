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

    public void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void printMenu() {
        clearScreen();
        System.out.println("""
                Bienvenido al Sistema de Chat Cliente-Servidor
                ----------------------------------------------
                Este sistema te permite crear chats (grupales o personales), enviar mensajes de texto y notas de voz, hacer llamadas y ver el historial de conversaciones.

                Comandos disponibles:
                /create [nombre_chat_grupal]                - Crear un nuevo chat grupal
                /join [nombre_chat_grupal]                  - Unirse a un chat grupal existente
                /msg [nombre_chat] [mensaje]                - Enviar un mensaje a un usuario o grupo
                /audio [nombre_chat]                        - Enviar una nota de voz
                /listen [nombre_audio]                      - Escuchar una nota de voz
                /history [nombre_chat]                      - Ver el historial de un chat

                Escribe un comando para empezar.""");
    }

    public void startClient() {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader consoleInput = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            clearScreen();
            System.out.print("Ingresa tu nombre de usuario: ");
            String username = consoleInput.readLine();

            out.println("/username " + username);  // envio nombre de usuario al servidor

            printMenu();

            // el hilo para leer los mensajes del servidor
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // envio de mensajes a usuarios o grupos
            String userInput;
            while ((userInput = consoleInput.readLine()) != null) {
                if (userInput.startsWith("/msg ")) {
                    out.println(userInput); // enviar mensaje al grupo o usuario

                } else if (userInput.startsWith("/audio ")) {
                    // Grabar una nota de voz y enviarla
                    String audioFileName = "nota_de_voz.wav";
                    String targetChat = userInput.split(" ", 2)[1];
                    recordVoice(audioFileName); // Grabar el audio
                    out.println(userInput + " " + audioFileName); // Enviar el comando al servidor
                    sendAudio(socket, targetChat, audioFileName); // Enviar el archivo al servidor

                }else if(userInput.startsWith("/listen ")){
                    out.println(userInput);
                    receiveAndPlayAudio(socket);
                }else {
                    out.println(userInput); // Enviar otros comandos
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Grabar una nota de voz
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

            // Crear un hilo para detener la grabación después de 10 segundos
            new Thread(() -> {
                try {
                    Thread.sleep(10000); // Grabar durante 10 segundos
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

    // Enviar el archivo de audio al servidor
    private void sendAudio(Socket socket, String targetChat ,String fileName) {
        try {
            File audioFile = new File(fileName);
            byte[] audioData = Files.readAllBytes(audioFile.toPath());

            // Enviar el archivo al servidor
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        
            dos.writeInt(audioData.length);
            dos.write(audioData);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Recibir y reproducir el archivo de audio desde el servidor
    private void receiveAndPlayAudio(Socket socket) {
        try {
            DataInputStream dis = new DataInputStream(socket.getInputStream());

            // Leer la longitud del archivo de audio
            int audioDataLength = dis.readInt();

            if (audioDataLength <= 0) {
                System.out.println("Error: El archivo de audio es inválido o está vacío.");
                return;
            }

            byte[] audioData = new byte[audioDataLength];
            dis.readFully(audioData);  // Asegurarse de que se leen todos los bytes

            // Reproducir el audio
            AudioFormat format = new AudioFormat(16000, 16, 1, true, true);
            SourceDataLine speakers = AudioSystem.getSourceDataLine(format);
            speakers.open(format);
            speakers.start();
            speakers.write(audioData, 0, audioData.length);
            speakers.drain();
            speakers.close();

            System.out.println("Audio reproducido correctamente.");

        } catch (IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }

}
