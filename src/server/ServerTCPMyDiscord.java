package server;

import model.Group;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;


public class ServerTCPMyDiscord {

    private static final int PORT = 5000;
    private static ArrayList<String> clientsNames = new ArrayList<>();
    private static ArrayList<Group> groups = new ArrayList<>();
    private static HashMap<String,ArrayList<String>> clientsGroups = new HashMap<>();




    public static void main(String[] args) {

        // Inicializar el servidor y su estado
        ExecutorService pool = Executors.newFixedThreadPool(3);
        init();

        boolean running = true;

        try {

            ServerSocket serverSocket = new ServerSocket(PORT);

            while (running) {

                System.out.println("Esperando conexión ...");
                Socket socket = serverSocket.accept();


            }

        }catch (IOException e){
            e.printStackTrace();


        }finally {
            pool.shutdown();
        }

    }

    private static void init(){
        // Cargar el estado del servidor (persistencia)

        // Cargar los clientes
        loadClients();

        // Cargar los grupos
        loadGroups();

        getAllClientsGroups();




    }




    // CLIENTS METHODS

    private static void addClient(String clientName){
        // Add the client to the list of clients
        clientsNames.add(clientName);
        saveClients();
    }

    private static void saveClients(){
        // Guardar el estado del servidor (persistencia)

    }

    private static void loadClients(){
        // Cargar el estado del servidor (persistencia)

    }

    public static ArrayList<String> getClientsNames() {
        return clientsNames;
    }

    // CLIENT GROUPS METHODS

    private static void getAllClientsGroups(){
        for (Group group : groups) {
            String groupName = group.getName(); // Assuming Group has getGroupName() method

            // Get the clients that belong to this group
            ArrayList<String> groupClients = group.getMembers(); // Assuming Group has getClients() method

            // Iterate over each client in this group
            for (String client : groupClients) {
                // If the client is not already in the map, add them with a new list
                clientsGroups.computeIfAbsent(client, k -> new ArrayList<>());

                // Add this group to the client's list of groups
                clientsGroups.get(client).add(groupName);
            }
        }

    }

    private static String getClientGroups(String clientName){
        // Get the groups that the client belongs to
        return clientsGroups.get(clientName).toString();
    }

    // GROUPS METHODS

    private static void createGroup(String groupName, ArrayList<String> groupMembers){
        // Create a new group
        groups.add(new Group(groupName, groupMembers));
        saveGroups();
    }
    private static void saveGroups(){
        // Guardar el estado del servidor (persistencia)

    }
    private static void loadGroups(){
        // Cargar el estado del servidor (persistencia)

    }


    public static ArrayList<Group> getGroups() {
        return groups;
    }


}




