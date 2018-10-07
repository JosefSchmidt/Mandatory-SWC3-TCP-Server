import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ClientThread extends Thread {


    private String clientName   = null;
    // Scanner for input
    private Scanner sis  = null;
    // PrintStream (Allows client to sent messenges to other clients through the server
    private PrintStream os = null;
    // Ip and portNumber
    private Socket clientSocket;
    private ClientThread[] threads;
    private int maxClientsCount;

    private List<String> userList;
    private double clientTime;


    // A ClientThread constructor
    ClientThread(Socket clientSocket, ClientThread[] threads, List<String> userList, double clientTime) {
        this.clientSocket = clientSocket;
        this.threads = threads;
        this.userList = userList;
        maxClientsCount = threads.length;
        this.clientTime = clientTime;
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

            os.println("There has been established a connection to the server");

            String name;
            while (true) {

                name = sis.nextLine();

                if(name.startsWith("JOIN")) {

                    name = name.substring(4, name.length());

                    name = name.trim();

                    // Checks username
                    if (correctUsername(name)) {
                        //See's if username has been used before
                        if (loopThreads(name, userList)) {
                            break;
                        } else {
                            os.println("J_ER: The name is taken");
                        }
                    }
                    // prints incorrect typed username
                    if (!correctUsername(name)) {
                        os.println(StringColor.ANSI_RED+ "J_ER" + StringColor.ANSI_WHITE + ": Username needs to be max 12 characters long, only letters, digits, ‘-‘ and ‘_’ allowed");
                    }
                } else os.println(StringColor.ANSI_RED + "J_ER" + StringColor.ANSI_WHITE + ": Wrong protocol.\nPlease enter \"JOIN\" \nfollowed by a username to enter the chatroom\n");
            }

            /* Welcome the new the client. */
            os.println(StringColor.ANSI_GREEN +"\nJ_OK"+ StringColor.ANSI_WHITE + ": Welcome " + name + " to our chat room.");


            // Only one thread client can be created at a time - this allows everyone to always get a message with all the new clients.
            synchronized (this) {
                // Adds the name to the new thread client
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] == this) {
                        clientName = name;
                        break;
                    }
                }
                // Sends a message to all other than the new thread client
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this) {
                        threads[i].os.println("*** A new user " + name
                                + " entered the chat room !!! ***");
                    }
                }
                for (int i = 0; i < maxClientsCount; i++) {
                        if (threads[i] != null && threads[i].clientName != null) {
                            threads[i].os.println(viewList());
                        }
                    }
                }

            // make sure that the getInputStream receives anything (This way the sis.nextLine() won't crash.
            while(sis.hasNextLine()) {

                //Gets a message from the client
                String line = sis.nextLine();

                    if(line.startsWith("DATA") && line.length() > 4) {
                        if(line.length() < 250) {

                            line = line.substring(5, line.length());
                            synchronized (this) {
                                for (int i = 0; i < maxClientsCount; i++) {
                                    if (threads[i] != null && threads[i].clientName != null) {
                                        threads[i].os.println("\n" + name + ": " + StringColor.ANSI_PURPLE + line + StringColor.ANSI_WHITE);
                                    }
                                }
                            }
                        } else os.println("Message too long. Max 250 characters is allowed");

                    } else if (line.startsWith("QUIT")) {
                        deleteFromList(name);
                        break;

                    } else if (line.startsWith("IMAV")) {

                        synchronized (this) {
                            for (int i = 0; i < maxClientsCount; i++) {
                                if (threads[i] != null && threads[i] == this) {
                                    threads[i].setClientTime(TimeTCP.setTime());
                                    threads[i].os.println("HeartBeat has been called. You have been given 60 seconds");
                                }
                            }
                        }

                    } else {
                        os.println("The server doesn't know the protocol. Please type a known protocol\n");
                    }
                }


            // Sends a message out to all clients except the client that's leaving, that the client is leaving.
            synchronized (this) {
                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i] != this && threads[i].clientName != null) {
                        threads[i].os.println(StringColor.ANSI_YELLOW + "*** The user " + name
                                + " has left the chat room !!! ***" + StringColor.ANSI_WHITE);
                    }
                }

                for (int i = 0; i < maxClientsCount; i++) {
                    if (threads[i] != null && threads[i].clientName != null) {
                        threads[i].os.println(viewList());
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

    private boolean loopThreads(String name, List<String> userList){
        // Checks if the username has been used by someone else
        for (Object anUserList : userList) {
            if (anUserList.equals(name)) {
                return false;
            }
        }
        // Adds to a List that keeps a hold of all active users on the server.
        userList.add(name);
        return true;

    }

    private boolean correctUsername(String name) {
        // Only allow certain ASCII characters
        String regex = "^[a-zA-Z0-9-_æøå]+$+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(name);

        // Username needs to be max 12 characters long, only letters, digits, ‘-‘ and ‘_’ allowed.
        if(name.length() > 13 || !matcher.matches()){
            return false;
        }
        return true;
    }

    private String viewList(){

        String list = "";

        for(int i = 0; i < userList.size(); i++){
            if(threads[i] != null && threads[i].getClientName() != null) {
                list = list + threads[i].clientName + "\n";
            }
        }

        list = "\nThe Clients in the server: \n" + StringColor.ANSI_BLUE + list + StringColor.ANSI_WHITE;

        return list;
    }

    public void deleteFromList(String name){
        // Updates the List of clients by deleting.
        userList.remove(name);
    }

    public Socket getClientSocket() {
        return clientSocket;
    }

    public double getClientTime() {
        return clientTime;
    }

    private void setClientTime(double clientTime) {
        this.clientTime = clientTime;
    }

    public PrintStream getOs() {
        return os;
    }

    public String getClientName() {
        return clientName;
    }


}