import java.io.*;
import java.net.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.util.Pair;
import java.util.concurrent.ConcurrentHashMap;

class ClientHandler extends Thread{
    private Socket clientSocket;

    private String regToSend = "REGISTER TOSEND ([a-zA-Z0-9]+)";
    private String regToRecv = "REGISTER TORECV ([a-zA-Z0-9]+)";
    private String sendHeader = "SEND ([a-zA-Z0-9]+)";
    private String content_length_header = "Content-length: ([0-9]+)";
    private String sentHeader = "SENT ";
    private boolean isReceiver;
    private ConcurrentHashMap<String,Socket> receiving_ports_map;
    private ConcurrentHashMap<Socket,Pair<BufferedReader,DataOuputStream>> receiving_streams;
    public ClientHandler(Socket inputSocket,boolean isReceiver,ConcurrentHashMap<String,Socket>receiving_ports_map,ConcurrentHashMap<Socket,Pair<BufferedReader,DataOuputStream>>receving_streams){
        this.clientSocket = inputSocket;
        this.isReceiver = isReceiver;
        this.receiving_ports_map = receiving_ports_map;
        this.receiving_streams = receiving_streams;
    }

    @Override
    public void run(){
        try{
            BufferedReader  input_from_client = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            DataOutputStream  output_to_client = new DataOutputStream(clientSocket.getOutputStream());
            receiving_streams.put(clientSocket,new Pair(input_from_client,output_to_client));
            String requestHeader = input_from_client.readLine();
            String nextline = input_from_client.readLine();
            
            if(this.isReceiver){
                String sender_username;
                if(requestHeader.matches(regToSend) && nextline.matches("")){
                    Pattern pattern = Pattern.compile(regToSend);
                    Matcher matcher = pattern.matcher(requestHeader);
                    if(matcher.find()){
                        String username = matcher.group(1);
                        sender_username = username;
                        output_to_client.writeBytes("REGISTERED TOSEND "+username+"\n\n");
                        
                    }
                    else{
                        output_to_client.writeBytes("ERROR 100 Malformed username\n\n");
                        clientSocket.close();
                    }
                }
                else{
                    output_to_client.writeBytes("ERROR 101 No user registered\n\n");
                    clientSocket.close();
                }

                while(true){
                    String firstLine = input_from_client.readLine();
                    String secondLine = input_from_client.readLine();
                    
                    if(firstLine.matches(sendHeader)&& secondLine.matches(content_length_header)){
                        Patter pattern = Pattern.compile(sendHeader);
                        Matcher matcher = pattern.matcher(firstLine);
                        String receipient_username;
                        if(matcher.find()){
                             receipient_username = matcher.group(1);
                        }
                        else{
                            output_to_client.writeBytes("ERROR 103 Header incomplete\n\n");
                            clientSocket.close();
                            continue;
                        }
                        
                        pattern = Patter.compile(content_length_header);
                        matcher = pattern.matcher(secondLine);
                        int messageLength;
                        if(matcher.find() && input_from_client.readLine()==""){
                            messageLength = Integer.parseInt(matcher.group(1));
                        }
                        else{
                            output_to_client.writeBytes("ERROR 103 Header incomplete\n\n");
                            clientSocket.closse();
                            continue;
                        }
                        //begin reading message
                        char [] message = new char[messageLength];
                        
                        int num_chars_read = input_from_client.read(message, 0, messageLength);
                        //num_chars_read must be same as message length; -1 when reading completely not specified
                        //incorportate this later. now assume everything goes well
                        Socket receipientSocket = receiving_ports_map.get(sender_username);
                        String forward_string = String.format("FORWARD %s\nContent-length: %d\n\n%s",receipient_username,messageLength,new String(message));
                        

                        BufferedReader input_from_receipient = (receiving_streams.get(receipient_username)).getKey();
                        DataOutputStream output_to_receipient = (receiving_streams.get(receipient_username)).getValue();
                        output_to_receipient.writeBytes(forward_string);

                        // firstLine = input_from_client.readLine();
                        // secondLine = input_from_client.readLine();
                        // boolean delivered = true;
                        // if(firstLine.matches("RECEIVED ([a-zA-Z0-9]+)") && secondLine.matches("")){
                        //     pattern = new Pattern.compile("RECEIVED ([a-zA-Z0-9]+)");
                        //     matcher = pattern.matcher(firstLine);
                        // }
                        // else if(firstLine.matches("ERROR 103 Header incomplete") && secondLine.matches("")){
                        //     delivered = false;
                        // }
                        // else{
                        //     delivered = false;
                        // }
                        


                    }
                    

                    
                }

            }
            else{
                if(requestHeader.matches(regToRecv) && nextline.matches("")){
                    Pattern pattern = Pattern.compile(regToRecv);
                    Matcher matcher = pattern.matcher(requestHeader);
                    if(matcher.find()){
                        String username = matcher.group(1);
                        receiving_ports_map.put(username,clientSocket);
                        output_to_client.writeBytes("REGISTERED TORECV "+username+"\n\n");
                        // System.out.println("OK");
                    }
                    else{
                        output_to_client.writeBytes("ERROR 100 Malformed username\n\n");
                        clientSocket.close();
                    }
                }
                else{
                    output_to_client.writeBytes("ERROR 101 No user registered\n\n");
                    clientSocket.close();

                }
            }
        }
        catch(IOException e){
            System.out.println("IOError");
            try{
                clientSocket.close();
            }
            catch(Exception exceptin){
                ;
            }
        }
    }
}

public class Server{

    private ServerSocket serv_receiver_socket = null;
    private ServerSocket serv_sender_socket = null;

    

    class ServerCreator implements Runnable{
        ServerSocket serv_socket;
        boolean isReceiver;
        ConcurrentHashMap<String,Socket> receiving_ports_map;
        ConcurrentHashMap<Socket,Pair<BufferedReader,DataOuputStream>> receiving_streams;
        public ServerCreator(ServerSocket serv_socket,boolean isReceiver,ConcurrentHashMap<String,Socket>receiving_ports_map,ConcurrentHashMap<Socket,Pair<BufferedReader,DataOuputStream>>receiving_streams){
            this.serv_socket= serv_socket;
            this.isReceiver = isReceiver;
            this.receiving_ports_map = receiving_ports_map;
            this.receiving_streams = receiving_streams;
        }
        @Override
        public void run(){
            while(true){
                try{
                Socket inputSocket = serv_socket.accept();
                Thread thread = new Thread(new ClientHandler(inputSocket,this.isReceiver,receiving_ports_map,receiving_streams));
                thread.start();
                }
                catch (IOException e){
                    System.out.println(e);
                }
            }
        }
    }

    public Server(int receiver_port,int sender_port)throws IOException,InterruptedException{
        serv_receiver_socket = new ServerSocket(receiver_port);//server listens on this port
        serv_sender_socket = new ServerSocket(sender_port);// server sends from this port
        ConcurrentHashMap <String,Socket> receiving_ports_map = new ConcurrentHashMap<String,Socket>();//maps usernames to their receiving sockets
        ConcurrentHashMap<Socket,Pair<BufferedReader,DataOuputStream>> receiving_streams;
        Thread t1 = new Thread(new ServerCreator(serv_receiver_socket,true,receiving_ports_map,receiving_streams));
        Thread t2 = new Thread(new ServerCreator(serv_sender_socket,false,receiving_ports_map,receiving_streams));

        t1.start();
        t2.start();

        t1.join();
        t2.join();
        System.out.println("Joined");
        // while(true){
            
        //         Socket inputSocket = serv_send_socket.accept();
        //         Thread thread = new ClientHandler(inputSocket);
        //         thread.start();
            
        // }
        
    }
    public static void main(String args[])throws IOException,InterruptedException{
        
        new Server(6000,6100);
        
    }
}