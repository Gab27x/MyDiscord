package org.example.mydiscord.model;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.net.InetAddress;

public class ServerTCPMyDiscord {
    public static void main(String[] args) {

        ExecutorService pool = Executors.newFixedThreadPool(3);


        try {
            ServerSocket serverSocket = new ServerSocket(5000);


            while (true) {
                System.out.println("Esperando conexión ...");
                Socket socket = serverSocket.accept();

                 pool.execute(new ClientHandler(socket));
            }

        }catch (IOException e){
            e.printStackTrace();
        }finally {
            pool.shutdown();
        }
    }
}





