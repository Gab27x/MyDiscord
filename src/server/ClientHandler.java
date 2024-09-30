package org.example.mydiscord.model;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try{

            System.out.println("Definir el formato de audio");
            AudioFormat format = new AudioFormat(44100, 16,
                    1, true, true);

            System.out.println("Información del parlante");
            DataLine.Info infoSpeaker = new DataLine.Info(SourceDataLine.class, format);

            System.out.println("Conexión con el parlante");
            SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(infoSpeaker);

            System.out.println("Abren el parlante del equipo");
            speaker.open(format);
            speaker.start();

            System.out.println("Conexion con el socket para extraer la información que envia el cliente");
            InputStream io = socket.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(io);

            System.out.println("Decodificando la información y enviando al parlante");
            byte[] buffer = new byte[1024];

            while (true){

                int byteRead = bis.read(buffer, 0, buffer.length);

                //Thread.sleep(10);

                speaker.write(buffer, 0, byteRead);
                if(byteRead == -1){
                    break;
                }
            }
            speaker.drain();
            speaker.flush();
            speaker.close();


        }catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //catch (InterruptedException e) {
        //    throw new RuntimeException(e);
        //}
    }
}