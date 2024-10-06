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
                /create [nombre_chat]                       - Crear un nuevo chat
                /join [nombre_chat]                         - Unirse a un chat existente
                /enviar_mensaje_usuario [usuario] [mensaje] - Enviar un mensaje a un usuario
                /enviar_mensaje_grupo [grupo] [mensaje]     - Enviar un mensaje a un grupo
                /enviar_audio_usuario [usuario]             - Grabar y enviar una nota de voz a un usuario
                /enviar_audio_grupo [grupo]                 - Grabar y enviar una nota de voz a un grupo
                /llamar_usuario [usuario]                   - Realizar una llamada a un usuario
                /llamar_grupo [grupo]                       - Realizar una llamada a un grupo
                /ver_historial [grupo]                      - Ver el historial de un grupo
                /salir                                      - Cerrar el programa

                Escribe un comando para empezar.
                """);
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
                        System.out.println("\n[SERVER]: " + message + "\n");
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
