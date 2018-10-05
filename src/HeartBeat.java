import java.io.IOException;

public class HeartBeat extends Thread {

    // A reference to the ClienThread[] (which is located in the MultiThreadChatServerSync class)
    private ClientThread[] threads;

    public HeartBeat(ClientThread[] threads){
        this.threads = threads;
    }

    public void run(){
        while (true){
            checkClients();
        }
    }

    private void checkClients(){
        double currentTime = TimeTCP.setTime();

        for (int i = 0; i <threads.length; i++){
            if(threads[i] != null){
                if(Math.abs(threads[i].getClientTime()-currentTime) > 1){
                    try {
                        threads[i].deleteFromList(threads[i].getClientName());
                        threads[i].getOs().println("TCP-connection has been terminated. Session has expired");
                        threads[i].getClientSocket().close();



                        threads[i] = null;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}


