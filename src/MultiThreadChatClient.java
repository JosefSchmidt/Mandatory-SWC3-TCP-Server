import java.io.PrintStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class MultiThreadChatClient implements Runnable {

    private static Socket clientSocket      = null;  // The client socket
    private static PrintStream os           = null;  // The output stream
    private static Scanner sis              = null;  // The input stream
    private static BufferedReader inputLine = null;  // From console
    private static boolean closed           = false; // Connection state
    private static Scanner scanner = new Scanner(System.in); // The console input

    private static int portNumber = 0;
    private static String host;

    public static void main(String[] args) {
//        portNumber = 5000;     // The default port.
//        String host = "localhost"; // The default serverhost.
//        host = "172.16.31.186"; // The default serverhost.

//        if (args.length < 2) {
//            System.out.println("Usage: java MultiThreadChatClient <host> <portNumber>\n"
//                    + "Now using host=" + host + ", portNumber=" + portNumber);
//        } else {
//            host = args[0];
//            portNumber = Integer.valueOf(args[1]).intValue();
//        }




            /*
             * Open a socket on a given host and port. Open input and output streams.
             */

            while (true) {
                try {

                    System.out.println("Please enter the IP in format x.x.x.x");
                    host = scanner.nextLine();

                    System.out.println("Port number to server");
                    portNumber = scanner.nextInt();
                    scanner.nextLine();

                    clientSocket = new Socket(host, portNumber);
                    inputLine = new BufferedReader(new InputStreamReader(System.in));
                    os = new PrintStream(clientSocket.getOutputStream());
                    sis = new Scanner(clientSocket.getInputStream());


                } catch (UnknownHostException e) {
                    System.err.println("Don't know the host " + host);
                } catch (IOException e) {
                    System.err.println("Couldn't get I/O for the connection to the host " + host);
                }
                break;
            }



                /*
                 * If everything has been initialized then we want to write some data to the
                 * socket we have opened a connection to on the port portNumber.
                 */


                if (clientSocket != null && os != null && sis != null) {
                    try {
                        /* Create a thread to read from the server. */
                        new Thread(new MultiThreadChatClient()).start();

                        while (!closed) {
                            os.println(inputLine.readLine().trim());
                        }

                        os.close();
                        sis.close();
                        clientSocket.close();

                    } catch (IOException e) {
                        System.err.println("IOException:  " + e);
                    }
                }
            }

    /*
     * Create a thread to read from the server.
     *
     */
    public void run() {
        /*
         * Keep on reading from the socket till we receive "Bye" from the
         * server. Once we received that then we want to break.
         */
        String responseLine;
        while ((responseLine = sis.nextLine()) != null) {
            System.out.println(responseLine);
        }
        closed = true;
    }
}