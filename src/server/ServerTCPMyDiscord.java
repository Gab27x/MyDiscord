package server;

import model.Group;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;


public class ServerTCPMyDiscord {

    private static final int PORT = 5000;
    private static ArrayList<String> clientsName = new ArrayList<>();
    private static ArrayList<Group> groups = new ArrayList<>();



    public static void main(String[] args) {

        // Inicializar el servidor y su estado
        ExecutorService pool = Executors.newFixedThreadPool(3);
        init();

        try {
            // Crear el servidor
            ServerSocket serverSocket = new ServerSocket(PORT);


            while (true) {
                System.out.println("Esperando conexión ...");
                Socket socket = serverSocket.accept();

        //         pool.execute(new ClientHandler(socket));
            }

        }catch (IOException e){
            e.printStackTrace();


        }finally {
            pool.shutdown();
        }

    }

    private static void init(){
        // Cargar el estado del servidor (persistencia)
        // Se usa: clientsName.add("nombre"); para cargar los nombres de los clientes




    }


}
