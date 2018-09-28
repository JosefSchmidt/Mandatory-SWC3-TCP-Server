import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiThreadChatServerSync {

    // The server socket
    private static ServerSocket serverSocket = null;
    // The client's socket
    private static Socket clientSocket = null;

    // This chat server can accept up to maxClientsCount clients' connections.
    private static final int maxClientsCount = 10;
    // The server creates a new thread when a client enters the chatroom.
    private static final ClientThread[] threads = new ClientThread[maxClientsCount];

    private static final List userList = new ArrayList();

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
                for (i = 0; i < maxClientsCount; i++) {
                    // If no one is using the thread[i] assign a new clientSocket and thread to the Thread (Construction) and brake the loop.
                    if (threads[i] == null) {
                        // The ClientThread constructer takes 2 parameter: the clientSocket and a reference to the thread[];
                        (threads[i] = new ClientThread(clientSocket, threads, userList)).start();
                        break;
                    }
                }
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
    // List that holds all the connected names
    private List userList;


    // A ClientThread constructor
    public ClientThread(Socket clientSocket, ClientThread[] threads, List userList) {
        this.clientSocket = clientSocket;
        this.threads = threads;
        this.userList = userList;
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
            os = new PrintStream(clientSocket.getOutputStream());
            /*
             * Get users name (If a names i taken. It is not possible to get that user name).
             */
            String name;
            while (true) {
                os.println("Enter your name.");
                name = sis.nextLine().trim();

                // Checks username
                if(correctUsername(name)) {
                    //See's if username has been used before
                    if (loopThreads(name, userList)) {
                        break;
                    }
                    else {
                        os.println("The name is taken");
                    }
                }
                // prints incorrect typed username
                if(!correctUsername(name)) {
                    os.println("Username needs to be max 12 characters long, only letters, digits, ‘-‘ and ‘_’ allowed");
                }
            }

            /* Welcome the new the client. */
            os.println("Welcome " + name + " to our chat room.\nTo leave enter \"QUIT\"" + " in a new line.");

            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] == this) {
                        clientName = name;
                        break;
                    }
                }
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this) {
                        threads[i].os.println("*** A new user " + name
                                + " entered the chat room !!! ***");
                    }
                }
            }



            while(true){
                String line = sis.nextLine();
                if(line.startsWith("QUIT")){
                    deleteFromList(name);
                    break;
                }

                else {
                    // The message is public, broadcast it to all other clients.
                    synchronized (this) {
                        for (int i = 0; i < maxClientsCount; i++) {
                            if (threads[i] != null && threads[i].clientName != null) {
                                threads[i].os.println("<" + name + "> " + line);
                            }
                        }
                    }
                }

            }
            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this
                            && threads[i].clientName != null) {
                        threads[i].os.println("*** The user " + name
                                + " is leaving the chat room !!! ***");
                    }
                }
            }
            os.println("*** Bye " + name + " ***");

            /*
             * Clean up. Set the current thread variable to null so that a new client
             * could be accepted by the server.
             */
            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] == this) {
                        threads[i] = null;
                    }
                }
            }
            /*
             * Close the output stream, close the input stream, close the socket.
             */
            sis.close();
            os.close();
            clientSocket.close();

        }catch (IOException e) {
        e.printStackTrace();
        }

    }

    private boolean loopThreads(String name, List userList){
        // Checks if the username has been used by someone else
        for(int i = 0; i < userList.size(); i++){
            if(userList.get(i).equals(name)){
                return false;
            }
        }
        // Adds to a List that keeps a hold of all active users on the server.
        userList.add(name);
        return true;

    }

    private boolean correctUsername(String name) {
        // Only allow certain ASCII characters
        String regex = "^[a-zA-Z0-9-]+$+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(name);

        // Username needs to be max 12 characters long, only letters, digits, ‘-‘ and ‘_’ allowed.
        if(name.length() > 13 || !matcher.matches()){
            return false;
        }
        return true;
    }

    private void deleteFromList(String name){
        userList.remove(name);
    }
}

