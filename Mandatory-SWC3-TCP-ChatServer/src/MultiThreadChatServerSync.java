import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class MultiThreadChatServerSync {

    // The server socket
    private static ServerSocket serverSocket = null;
    // The client's socket
    private static Socket clientSocket = null;

    // This chat server can accept up to maxClientsCount clients' connections.
    private static final int maxClientsCount = 10;
    // The server creates a new thread when a client enters the chatroom.
    private static final ClientThread[] threads = new ClientThread[maxClientsCount];

    public static void main(String[] args){

        // Default hardcoded portNumber
        int portNumber = 5000;

        if(args.length < 1){
            System.out.println("Usage: java MultiThreadChatServerSync <portNumber>\n"
                    + "Now using port number=" + portNumber);
        } else {
            // If there is an argument. Assign the argument to portNumber
            portNumber = Integer.valueOf(args[0]).intValue();
        }


        /*
         * Open a server socket on the portNumber (default 5000). Note that we can
         * not choose a port less than 1023 if we are not privileged users (root).
         */
        try {
            serverSocket = new ServerSocket(portNumber);
        } catch (IOException e) {
            System.out.println(e);
        }

        /*
         * Create a client socket for each connection and pass it to a new client
         * thread.
         */
        while (true) {
            try {
                clientSocket = serverSocket.accept();

                // Find a place for new user
                int i = 10;
//                for (i = 0; i < maxClientsCount; i++) {
//                    // If no one is using the thread[i] assign a new clientSocket and thread to the Thread (Construction) and brake the loop.
//                    if (threads[i] == null) {
//                        // The ClientThread constructer takes 2 parameter: the clientSocket and a reference to the thread[];
//                        (threads[i] = new ClientThread(clientSocket, threads)).start();
//                        break;
//                    }
//                }
                // if i is the same as the maxClientsCount, a message will be sent back to the client attempting to access the server.
                if (i == maxClientsCount) {
                    // Invokes the PrintStream class
                    PrintStream os = new PrintStream(clientSocket.getOutputStream());
                    os.println("Server too busy. Try later.");
                    os.close();
                    clientSocket.close();
                }
            } catch (IOException e) {
                System.out.println(e);
            }
        }
    }
}



class ClientThread extends Thread {

    //the clients name
    private String clientName   = null;
    // Input stream (Allows clients to type messenges
    private Scanner sis  = null;
    // PrintStream (Allows client to sent messenges to other clients through the server
    private PrintStream os      = null;
    // The clients socket details
    private Socket clientSocket = null;
    // A reference to the ClienThread[] (which is located in the MultiThreadChatServerSynd class)
    private final ClientThread[] threads;
    // The max number of clients that can connect to the server
    private int maxClientsCount;


    // A ClientThread constructer
    public ClientThread(Socket clientSocket, ClientThread[] threads) {
        this.clientSocket = clientSocket;
        this.threads = threads;
        maxClientsCount = threads.length;
    }

    public void run() {
        // assigns the number of clients connected to the server
        int maxClientsCount = this.maxClientsCount;
        // assigns the reference to a variable ClientThread[]
        ClientThread[] threads = this.threads;


        try{
            /*
             * Create input and output streams for this client.
             */

            sis = new Scanner(clientSocket.getInputStream());


        }catch (IOException e) {
        e.printStackTrace();
        }

    }




}

