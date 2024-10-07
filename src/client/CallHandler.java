package client;

import javax.sound.sampled.*;
import java.net.*;
import java.io.*;

public class CallHandler {

    private DatagramSocket udpSocket;
    private InetAddress serverAddress;
    private int serverPort;

    private TargetDataLine microphone;
    private boolean inCall;

    public CallHandler(String serverIP, int port) throws IOException {
        this.serverAddress = InetAddress.getByName(serverIP);
        this.serverPort = port;
        this.udpSocket = new DatagramSocket();
        this.inCall = false;
    }

    // Iniciar una llamada
    public void startCall() throws LineUnavailableException {
        AudioFormat format = new AudioFormat(16000, 8, 1, true, true);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        microphone = (TargetDataLine) AudioSystem.getLine(info);
        microphone.open(format);
        microphone.start();

        inCall = true;

        // Hilo que envía los datos de audio al servidor
        new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                while (inCall) {
                    int bytesRead = microphone.read(buffer, 0, buffer.length);
                    if (bytesRead > 0) {
                        sendAudio(buffer, bytesRead);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Enviar datos de audio al servidor
    private void sendAudio(byte[] audioData, int length) throws IOException {
        DatagramPacket packet = new DatagramPacket(audioData, length, serverAddress, serverPort);
        udpSocket.send(packet);
    }

    // Recibir datos de audio
    public void receiveAudio() {
        new Thread(() -> {
            try {
                byte[] buffer = new byte[1024];
                while (inCall) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    udpSocket.receive(packet);
                    playAudio(packet.getData());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Reproducir datos de audio recibidos
    private void playAudio(byte[] audioData) {
        // Implementar la reproducción de los datos de audio recibidos.
        // Puede usarse una línea de salida como SourceDataLine para enviar los datos al altavoz.
    }

    // Finalizar llamada
    public void endCall() {
        inCall = false;
        microphone.close();
        udpSocket.close();
    }
}
