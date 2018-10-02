import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class MultiThreadChatServerSync {

    private static ServerSocket serverSocket = null;

    private static Socket clientSocket = null;

    private static final int maxClientsCount = 10;

    private static final ClientThread[] threads = new ClientThread[maxClientsCount];

    private static final List<String> userList = new ArrayList<>();

    private static HeartBeat heartBeat = new HeartBeat(threads);


    public static void main(String[] args){

        heartBeat.start();

        // Default hardcoded portNumber
        int portNumber = 5000;

        if(args.length < 1){
            System.out.println("Usage: java MultiThreadChatServerSync <portNumber>\n"
                    + "Now using port number=" + portNumber +"\nServer is now running");
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
         * Creates a client socket for each connection and passes a new clientThread to the thread[].
         */
        while (true) {
            try {

                clientSocket = serverSocket.accept();

                int i = 0;

                for (i = 0; i < maxClientsCount; i++) {
                    // If no one is using the thread[i] assign a new clientSocket and thread to the Thread (Construction) and brake the loop.
                    if (threads[i] == null) {
                        (threads[i] = new ClientThread(clientSocket, threads, userList, TimeTCP.setTime())).start();
                        break;
                    }
                }

                // If all thread[] is full
                if (i == maxClientsCount) {
                    // Invokes the PrintStream class
                    PrintStream os = new PrintStream(clientSocket.getOutputStream());
                    os.println("Server too busy. Try later.");
                    os.close();
                    clientSocket.close();
                }

            } catch (IOException e) {
                System.out.println("The disconnected");
            }
        }
    }
}



