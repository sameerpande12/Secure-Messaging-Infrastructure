import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class ClientHandler extends Thread{
    private Socket clientSocket;

    private String regToSend = "REGISTER TOSEND ([a-zA-Z0-9]+)";
    private String regToRecv = "REGISTER TORECV ([a-zA-Z0-9]+)";
    public ClientHandler(Socket inputSocket){
        this.clientSocket = inputSocket;
    }

    @Override
    public void run(){
        try{
            BufferedReader  input_from_client = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            DataOutputStream  output_to_client = new DataOutputStream(clientSocket.getOutputStream());

            String requestHeader = input_from_client.readLine();
            String newline = input_from_client.readLine();
            if(requestHeader.matches(regToSend) && newline.matches("")){
                Pattern pattern = Pattern.compile(regToSend);
                Matcher matcher = pattern.matcher(requestHeader);
                if(matcher.find()){
                    String username = matcher.group(1);
                    System.out.println("OK");
                    output_to_client.writeBytes("REGISTERED TOSEND "+username+"\n\n");
                    System.out.println("OK1");
                }
                else{
                    // output_to_client.writeBytes("ERROR 100 Malformed username\n\n");
                }
            }
            else if(requestHeader.matches(regToRecv) && newline.matches("")){
                Pattern pattern = Pattern.compile(regToRecv);
                Matcher matcher = pattern.matcher(requestHeader);
                if(matcher.find()){
                    // String username = matcher.group(1);
                    // output_to_client.writeBytes("REGISTERED TORECV "+username+"\n\n");
                    System.out.println("OK");
                }
                else{
                    // output_to_client.writeBytes("ERROR 100 Malformed username\n\n");
                }
            }
        }
        catch(IOException e){
            System.out.println("IOError");
        }
    }
}

public class Server{

    private ServerSocket serv_receiver_socket = null;
    private ServerSocket serv_sender_socket = null;

    class ServerCreator implements Runnable{
        ServerSocket serv_socket;
        public ServerCreator(ServerSocket serv_socket){
            this.serv_socket= serv_socket;
        }
        @Override
        public void run(){
            while(true){
                Socket inputSocket = serv_socket.accept();
                Thread thread = new Thread(new ClientHandler(inputSocket));
                thread.start();
            }
        }
    }

    public Server(int receiver_port,int sender_port)throws IOException{
        serv_receiver_socket = new ServerSocket(receiver_port);
        serv_sender_socket = new ServerSocket(sender_port);

        
        // while(true){
            
        //         Socket inputSocket = serv_send_socket.accept();
        //         Thread thread = new ClientHandler(inputSocket);
        //         thread.start();
            
        // }
        
    }
    public static void main(String args[])throws IOException{
        
        Server server = new Server(6000,6100);
        
    }
}