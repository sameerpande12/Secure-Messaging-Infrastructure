import java.io.*;
import java.util.*;
import java.net.*;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.AbstractMap;
import java.util.concurrent.ConcurrentHashMap;

class ClientHandlerEncryptedServer implements Runnable{
    private Socket clientSocket;

    private String regToSend = "REGISTER TOSEND ([a-zA-Z0-9]+)";
    private String regToRecv = "REGISTER TORECV ([a-zA-Z0-9]+)";
    private String sendHeader = "SEND ([a-zA-Z0-9]+)";
    private String content_length_header = "Content-length: ([0-9]+)";
    private String fetch_header = "FETCHKEY (.+)";
    // private String sentHeader = "SENT ";
    private boolean isReceiver;
    private ConcurrentHashMap<String,Socket> receiving_ports_map;
    private ConcurrentHashMap<String,Socket> sending_ports_map;
    private ConcurrentHashMap<String,String> public_key_map;
    private String clientUserName;
    private ConcurrentHashMap<Socket,AbstractMap.SimpleEntry<BufferedReader,DataOutputStream>> socket_streams;
    public ClientHandlerEncryptedServer(Socket inputSocket,boolean isReceiver,ConcurrentHashMap<String,Socket>receiving_ports_map,ConcurrentHashMap<String,Socket>sending_ports_map,ConcurrentHashMap<Socket,AbstractMap.SimpleEntry<BufferedReader,DataOutputStream>>socket_streams,ConcurrentHashMap<String,String>public_key_map){
        this.clientSocket = inputSocket;
        this.isReceiver = isReceiver;
        this.receiving_ports_map = receiving_ports_map;
        this.sending_ports_map = sending_ports_map;
        this.socket_streams = socket_streams;
        this.public_key_map = public_key_map;
    }

    @Override
    public void run(){
        try{
            BufferedReader  input_from_client = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            DataOutputStream  output_to_client = new DataOutputStream(clientSocket.getOutputStream());

            AbstractMap.SimpleEntry<BufferedReader,DataOutputStream> stream_pairs = new AbstractMap.SimpleEntry<>(input_from_client,output_to_client);
            // if(clientSocket==null || input_from_client==null || output_to_client==null){
            //     System.out.println("Gadbad hai bro");
            // }
            // else{
            //     System.out.println("Chill hai");
            // }
            
            
            socket_streams.put(clientSocket, stream_pairs);
            String requestHeader = input_from_client.readLine();
            System.out.println(requestHeader);
            if(!(requestHeader.matches(regToSend) || requestHeader.matches(regToRecv) || requestHeader.matches(fetch_header))){
                if((requestHeader.matches("REGISTER TOSEND (.*?)") && !requestHeader.matches(regToSend)) || (requestHeader.matches("REGISTER TORECV (.*?)") && !requestHeader.matches(regToRecv)) || (requestHeader.matches("FETCHKEY (.*?)") && !requestHeader.matches(fetch_header))){
                    output_to_client.writeBytes("ERROR 100 Malformed username\n\n");
                    System.out.println("ERROR 100 Malformed username\n\n");
                }
                else{ 
                    output_to_client.writeBytes("ERROR 101 No user registerd\n\n");
                    System.out.println("ERROR 101 No user registerd\n\n");
                }
                try{clientSocket.close();}
                catch(Exception err){;}
                try{socket_streams.remove(clientSocket);}
                catch(Exception err){;}
                return;
            }

            String nextline = input_from_client.readLine();
            System.out.println(nextline);
            
            if(this.isReceiver){
                String sender_username;
                // System.out.println("HI");
                if(requestHeader.matches(regToSend) && nextline.matches("")){
                    // System.out.println("HII");
                    Pattern pattern = Pattern.compile(regToSend);
                    Matcher matcher = pattern.matcher(requestHeader);
                    if(matcher.find()){
                        String username = matcher.group(1);
                        sender_username = username;
                        clientUserName = username;
                        
                        if(sending_ports_map.containsKey(sender_username)){
                            try{clientSocket.close();}
                            catch(Exception err){;}
                            try{socket_streams.remove(clientSocket);}
                            catch(Exception err){;}
                            return;

                        }
                        else{
                            sending_ports_map.put(username,clientSocket);
                            output_to_client.writeBytes("REGISTERED TOSEND "+username+"\n\n");
                            System.out.println("REGISTERED TOSEND "+username+"\n\n");
                        }
                        
                    }
                    else{
                        // System.out.println("HIII");
                        if(requestHeader.matches("REGISTER TOSEND (.*?)") && !requestHeader.matches(regToSend)){
                           output_to_client.writeBytes("ERROR 100 Malformed username\n\n");
                           System.out.println("ERROR 100 Malformed username\n\n");
                        }
                        else{
                            output_to_client.writeBytes("ERROR 101 No user registered\n\n");        
                            System.out.println("ERROR 101 No user registered:99\n\n");
                        }
                            try{clientSocket.close();}
                            catch(Exception err){;}
                            try{socket_streams.remove(clientSocket);}
                            catch(Exception err){;}
                        
                        
                        return;
                    }
                }
                else{
                    output_to_client.writeBytes("ERROR 101 No user registered\n\n");
                    System.out.println("ERROR 101 No user registered:98\n\n");
                        try{clientSocket.close();}
                        catch(Exception err){;}
                        try{socket_streams.remove(clientSocket);}
                        catch(Exception err){;}
                    
                    
                    return;
                    
                }
                //done registration or socket closed till here;
                while(true){
                    try
                    {
                        String firstLine = input_from_client.readLine();
                        System.out.println(firstLine);
                        if(!receiving_ports_map.containsKey(sender_username)){
                            output_to_client.writeBytes("ERROR 101 No user registered\n\n");
                            System.out.println("ERROR 101 No user registered:97\n\n");
                            continue;
                        }

                        String secondLine = input_from_client.readLine();
                        System.out.println(secondLine);
                        if(firstLine.matches("FETCHKEY (.*?)")){
                            if(firstLine.matches(fetch_header) && secondLine.matches("")){
                                Pattern fpattern = Pattern.compile(fetch_header);
                                Matcher fmatcher = fpattern.matcher(firstLine);

                                if(fmatcher.find()){
                                    
                            
                                    String requested_username = fmatcher.group(1);
                                    
                                    String public_key;
                                    if(public_key_map.containsKey(requested_username)){
                                        public_key = public_key_map.get(requested_username);
                                        // System.out.println("public_key:"+public_key);
                                        String fetched_message = "FETCHEDKEY "+requested_username+"\nContent-length: "+Integer.toString(public_key.length()) +"\n\n"+public_key;
                                        // System.out.println(fetched_message);
                                        output_to_client.writeBytes(fetched_message);
                                    }
                                    else{
                                        output_to_client.writeBytes("ERROR 101 No user registered\n\n");        
                                        System.out.println("ERROR 101 No user registered:96\n\n");
                                        // continue;
                                    }
                                    
                                }
                                else{
                                    output_to_client.writeBytes("ERROR 102 Unable to send\n\n");
                                    System.out.println("ERROR 102 Unable to send\n\n");
                                    // continue;
                                }
                            }

                            else if(firstLine.matches("FETCHKEY (.*?)")){
                                output_to_client.writeBytes("ERROR 101 No user registered\n\n");        
                                System.out.println("ERROR 101 No user registered:95\n\n");
                                // continue;
                                // to_continue=true;
                            }
                            else if(!secondLine.matches("") && firstLine.matches(fetch_header)){
                                output_to_client.writeBytes("ERROR 103 Header incomplete\n\n");
                                System.out.println("ERROR 103 Header incomplete\n\n");
                                // continue;
                                // to_continue = true;
                            }
                            else{

                                output_to_client.writeBytes("ERROR 103 Header incomplete\n\n");
                                System.out.println("ERROR 103 Header incomplete\n\n");
                                // continue;
                                // to_continue = true;
                            }

                            String fetch_ack = input_from_client.readLine();
                            System.out.println(fetch_ack);
                            String tempString = input_from_client.readLine();
                            System.out.println(tempString);
                            if(fetch_ack.matches("FETCH ACK")){
                                continue;
                            }
                            else{
                                    try{clientSocket.close();}
                                    catch(Exception err){;}
                                    // try{public_key_map.remove(sender_username);}No re-registraction for receiving port hence not to remove it
                                    // catch(Exception err){;}
                                    
                                    try{socket_streams.remove(clientSocket);}
                                    catch(Exception err){;}
                                    try{sending_ports_map.remove(sender_username);}
                                    catch(Exception err){;}
                                    // try{receiving_ports_map.remove(sender_username);}No re-registration for receiving port done
                                    // catch(Exception err){;}
                                    return;
                            }


                            // if(to_continue)continue;
                        }
                        else if(firstLine.matches("SEND (.*?)")){
                            if(firstLine.matches(sendHeader)&& secondLine.matches(content_length_header)){
                                Pattern pattern = Pattern.compile(sendHeader);
                                Matcher matcher = pattern.matcher(firstLine);
                                String receipient_username;
                                if(matcher.find()){
                                    receipient_username = matcher.group(1);
                                    System.out.println("Receipient username: "+receipient_username);
                                }
                                else{
                                    output_to_client.writeBytes("ERROR 102 Unable to send\n\n");
                                    System.out.println("ERROR 102 Unable to send\n\n");
                                    // clientSocket.close();
                                    // System.out.println("Incomplete header");
                                    // return;
                                    continue;
                                }
                                
                                pattern = Pattern.compile(content_length_header);
                                matcher = pattern.matcher(secondLine);
                                System.out.println("waiting to reading newline");
                                String newline = input_from_client.readLine();
                                System.out.println("done reading newline");
                                System.out.println("newline :"+newline);
                                int messageLength;
                                if(matcher.find() && newline.matches("")){
                                    messageLength = Integer.parseInt(matcher.group(1));
                                }
                                else{
                                    output_to_client.writeBytes("ERROR 103 Header incomplete\n\n");
                                    System.out.println("ERROR 103 Header incomplete\n\n");
                                    try{clientSocket.close();}
                                    catch(Exception err){;}
                                    try{public_key_map.remove(sender_username);}
                                    catch(Exception err){;}
                                    try{socket_streams.remove(clientSocket);}
                                    catch(Exception err){;}
                                    try{sending_ports_map.remove(sender_username);}
                                    catch(Exception err){;}
                                    try{receiving_ports_map.remove(sender_username);}
                                    catch(Exception err){;}
                                    return;
                                    
                                }
                                
                                //begin reading message
                                char [] message = new char[messageLength];
                                System.out.println("Message length="+messageLength);
                                
                                // int num_chars_read = input_from_client.read(message, 0, messageLength);
                                input_from_client.read(message, 0, messageLength);
                                //num_chars_read must be same as message length; -1 when reading completely not specified
                                //incorportate this later. now assume everything goes well
                                // System.out.println(new String(message));
                                if(!receiving_ports_map.containsKey(receipient_username)){
                                    output_to_client.writeBytes("ERROR 102 Unable to send\n\n");
                                    System.out.println("ERROR 102 Unable to send\n\n");
                                    continue;
                                }

                                Socket receipient_socket = receiving_ports_map.get(receipient_username);
                                synchronized(receipient_socket){
                                    
                                    String forward_string = String.format("FORWARD %s\nContent-length: %d\n\n%s",sender_username,messageLength,new String(message));
                                    System.out.println("\n"+forward_string);

                                    BufferedReader input_from_receipient = (socket_streams.get(receipient_socket)).getKey();
                                    DataOutputStream output_to_receipient = (socket_streams.get(receipient_socket)).getValue();
                                    output_to_receipient.writeBytes(forward_string);
                                    //sent data to reciepient


                                    firstLine = input_from_receipient.readLine();
                                    System.out.println(firstLine);
                                    secondLine = input_from_receipient.readLine();
                                    System.out.println(secondLine);
                                    
                                    //HOW TO DISTINGUISH FOR WHICH SENDER IS THE HEADER  INCOMPLETE MESSAGE ? 
                                    if(firstLine.matches("RECEIVED ([a-zA-Z0-9]+)") && secondLine.matches("")){
                                        // pattern = new Pattern.compile("RECEIVED ([a-zA-Z0-9]+)");
                                        // matcher = pattern.matcher(firstLine);
                                        
                                        output_to_client.writeBytes("SENT "+receipient_username+"\n\n");
                                        System.out.println("SENT "+receipient_username+"\n\n");
                                    }
                                    else if(firstLine.matches("ERROR 103 Header incomplete") && secondLine.matches("")){
                                        
                                        output_to_client.writeBytes("ERROR 102 Unable to send\n\n");
                                        System.out.println("ERROR 102 Unable to send\n\n");
                                    }
                                    else{
                                        
                                        output_to_client.writeBytes("ERROR 102 Unable to send\n\n");
                                        System.out.println("ERROR 102 Unable to send\n\n");
                                    }
                                
                                } 


                            }
                            else if(!secondLine.matches(content_length_header)){//case when content length header is missing
                                output_to_client.writeBytes("ERROR 103 Header incomplete\n\n");
                                System.out.println("ERROR 103 Header incomplete\n\n");
                                try{clientSocket.close();}
                                catch(Exception err){;}
                                try{public_key_map.remove(sender_username);}
                                catch(Exception err){;}
                                try{socket_streams.remove(clientSocket);}
                                catch(Exception err){;}
                                try{sending_ports_map.remove(sender_username);}
                                catch(Exception err){;}
                                try{receiving_ports_map.remove(sender_username);}
                                catch(Exception err){;}
                                return;
                                    
                            }
                            else{//case when requestheader is out of format. not decided yet for this block. temporary for now
                                output_to_client.writeBytes("ERROR 103 Header incomplete\n\n");
                                System.out.println("ERROR 103 Header incomplete\n\n");
                                try{clientSocket.close();}
                                catch(Exception err){;}
                                try{public_key_map.remove(sender_username);}
                                catch(Exception err){;}
                                try{socket_streams.remove(clientSocket);}
                                catch(Exception err){;}
                                try{sending_ports_map.remove(sender_username);}
                                catch(Exception err){;}
                                try{receiving_ports_map.remove(sender_username);}
                                catch(Exception err){;}
                                return;
                                
                            }
                        }
                        else if(firstLine.matches("UNREGISTER (.*?)") && secondLine.matches("")){
                            Pattern pattern = Pattern.compile("UNREGISTER ([a-zA-Z0-9]+)");
                            Matcher matcher = pattern.matcher(firstLine);
                            if(matcher.find()){
                                String usernameToUnregister = matcher.group(1);
                                if(usernameToUnregister.matches(sender_username)){
                                    output_to_client.writeBytes("UNREGISTERED "+usernameToUnregister+"\n\n");    
                                    System.out.println("UNREGISTERED "+usernameToUnregister+"\n\n");    
                                    try{clientSocket.close();}
                                    catch(Exception err){;}
                                    try{public_key_map.remove(sender_username);}
                                    catch(Exception err){;}
                                    try{socket_streams.remove(clientSocket);}
                                    catch(Exception err){;}
                                    try{sending_ports_map.remove(sender_username);}
                                    catch(Exception err){;}
                                    try{receiving_ports_map.remove(sender_username);}
                                    catch(Exception err){;}
                                    return;   
                                }
                                else{
                                    output_to_client.writeBytes("ERROR 200 You cannot unregister other user\n\n");
                                    System.out.println("ERROR 200 You cannont unregister other user\n\n");
                                    continue;
                                }
                            }
                            else{
                                output_to_client.writeBytes("ERROR 100 Malformed username\n\n");
                                System.out.println("ERROR 100 Malformed username\n\n");
                                continue;

                            }
                        }
                        else{

                            output_to_client.writeBytes("ERROR 103 Header incomplete\n\n");
                            System.out.println("ERROR 103 Header incomplete\n\n");
                            continue;

                        }
                    }
                    catch(Exception error){
        

                        System.out.println("Ctrl C deregistration");
                        try{clientSocket.close();}
                        catch(Exception err){;}
                        
                        try{socket_streams.remove(clientSocket);}
                        catch(Exception err){;}
                        try{public_key_map.remove(clientUserName);}
                        catch(Exception err){;}
                        
                        try{sending_ports_map.remove(clientUserName);}
                        catch(Exception err){;}
                        try{receiving_ports_map.remove(clientUserName);}
                        catch(Exception err){;}
                        return;
                    }

                    
                }

            }
            else{
                // System.out.println("Began server_sending thread. (client recv sockets)");
                // System.out.println(requestHeader);
                // System.out.println(nextline);
                if(requestHeader.matches(regToRecv) && nextline.matches(content_length_header)){
                    Pattern pattern = Pattern.compile(regToRecv);
                    Matcher matcher = pattern.matcher(requestHeader);
                    String receiver_username;

                    if(matcher.find()){
                        receiver_username = matcher.group(1);
                        clientUserName = receiver_username;                    
                        pattern = Pattern.compile(content_length_header);
                        matcher = pattern.matcher(nextline);
                        String newline = input_from_client.readLine();
                        System.out.println(newline);
                        int messageLength;
                        if(matcher.find() && newline.matches("")){
                            messageLength = Integer.parseInt(matcher.group(1));
                        }
                        else{
                            output_to_client.writeBytes("ERROR 103 Header incomplete\n\n");
                            System.out.println("ERROR 103 Header incomplete\n\n");
                            try{clientSocket.close();}
                            catch(Exception err){;}
                            try{public_key_map.remove(receiver_username);}
                            catch(Exception err){;}
                            try{socket_streams.remove(clientSocket);}
                            catch(Exception err){;}
                            try{sending_ports_map.remove(receiver_username);}
                            catch(Exception err){;}
                            try{receiving_ports_map.remove(receiver_username);}
                            catch(Exception err){;}
                            return;   
                        }
                        char [] message = new char[messageLength];
                        input_from_client.read(message,0,messageLength);
                        System.out.println(new String(message));
                        if(receiving_ports_map.containsKey(receiver_username)){
                                output_to_client.writeBytes("ERROR 101 No user registered\n\n");        
                                System.out.println("ERROR 101 No user registered:1\n\n");
                                try{clientSocket.close();}
                                catch(Exception err){;}
                                try{socket_streams.remove(clientSocket);}
                                catch(Exception err){;}
                                return;
                            
                        }
                        receiving_ports_map.put(receiver_username,clientSocket);
                        public_key_map.put(receiver_username,new String(message));
                        output_to_client.writeBytes("REGISTERED TORECV "+receiver_username+"\n\n");  
                        System.out.println("REGISTERED TORECV "+receiver_username+"\n\n");  
                    }
                    else{
                        output_to_client.writeBytes("ERROR 100 Malformed username\n\n");
                        System.out.println("ERROR 100 Malformed username\n\n");
                        try{clientSocket.close();}
                        catch(Exception err){;}
                        try{socket_streams.remove(clientSocket);}
                        catch(Exception err){;}
                    }
                }
                else if(requestHeader.matches("REGISTER TORECV (.*?)") && !requestHeader.matches(regToRecv)){
                    output_to_client.writeBytes("ERROR 100 Malformed username\n\n");
                    System.out.println("ERROR 100 Malformed username\n\n");
                    try{clientSocket.close();}
                    catch(Exception err){;}
                    try{socket_streams.remove(clientSocket);}
                    catch(Exception err){;}
                }
                else{
                    output_to_client.writeBytes("ERROR 101 No user registered\n\n");        
                    System.out.println("ERROR 101 No user registered:2\n\n");
                    try{clientSocket.close();}
                    catch(Exception err){;}
                    try{socket_streams.remove(clientSocket);}
                    catch(Exception err){;}

                }
            
            }
        }
        catch(IOException e){
            
            System.out.println("Ctrl C deregistration");
            try{clientSocket.close();}
            catch(Exception err){;}
            
            try{socket_streams.remove(clientSocket);}
            catch(Exception err){;}
            try{public_key_map.remove(clientUserName);}
            catch(Exception err){;}
            
            try{sending_ports_map.remove(clientUserName);}
            catch(Exception err){;}
            try{receiving_ports_map.remove(clientUserName);}
            catch(Exception err){;}
        }
    }
}

public class EncryptedServer{

    private ServerSocket serv_receiver_socket = null;
    private ServerSocket serv_sender_socket = null;

    

    class ServerCreatorEncryptedServer implements Runnable{
        ServerSocket serv_socket;
        boolean isReceiver;
        ConcurrentHashMap<String,Socket> receiving_ports_map;
        ConcurrentHashMap<String,Socket>sending_ports_map;
        ConcurrentHashMap<String,String> public_key_map;
        ConcurrentHashMap<Socket,AbstractMap.SimpleEntry<BufferedReader,DataOutputStream>> socket_streams;
        public ServerCreatorEncryptedServer(ServerSocket serv_socket,boolean isReceiver,ConcurrentHashMap<String,Socket>receiving_ports_map,ConcurrentHashMap<String,Socket>sending_ports_map,ConcurrentHashMap<Socket,AbstractMap.SimpleEntry<BufferedReader,DataOutputStream>>socket_streams,ConcurrentHashMap<String,String>public_key_map){
            this.serv_socket= serv_socket;
            this.isReceiver = isReceiver;
            this.receiving_ports_map = receiving_ports_map;
            this.sending_ports_map = sending_ports_map;
            this.socket_streams = socket_streams;
            this.public_key_map = public_key_map;
        }
        @Override
        public void run(){
            while(true){
                try{
                Socket inputSocket = serv_socket.accept();
                Thread thread = new Thread(new ClientHandlerEncryptedServer(inputSocket,this.isReceiver,receiving_ports_map,sending_ports_map,socket_streams,public_key_map));
                thread.start();
                }
                catch (IOException e){
                    System.out.println(e);
                }
            }
        }
    }

    public EncryptedServer(int receiver_port,int sender_port,String ip)throws IOException,InterruptedException{
        serv_receiver_socket = new ServerSocket(receiver_port,0,InetAddress.getByName(ip));//server listens on this port
        serv_sender_socket = new ServerSocket(sender_port,0,InetAddress.getByName(ip));// server sends from this port
        ConcurrentHashMap <String,Socket> receiving_ports_map = new ConcurrentHashMap<String,Socket>();//maps usernames to their receiving sockets
        ConcurrentHashMap <String,Socket> sending_ports_map = new ConcurrentHashMap<String,Socket>();
        ConcurrentHashMap<String,String> public_key_map = new ConcurrentHashMap<String,String>();
        ConcurrentHashMap<Socket,AbstractMap.SimpleEntry<BufferedReader,DataOutputStream>> socket_streams = new ConcurrentHashMap<Socket,AbstractMap.SimpleEntry<BufferedReader,DataOutputStream>>();
        
        Thread t1 = new Thread(new ServerCreatorEncryptedServer(serv_receiver_socket,true,receiving_ports_map,sending_ports_map,socket_streams,public_key_map));
        Thread t2 = new Thread(new ServerCreatorEncryptedServer(serv_sender_socket,false,receiving_ports_map,sending_ports_map,socket_streams,public_key_map));

        t1.start();
        t2.start();

        t1.join();
        t2.join();
        System.out.println("Joined");
        // while(true){
            
        //         Socket inputSocket = serv_send_socket.accept();
        //         Thread thread = new ClientHandlerEncryptedServer(inputSocket);
        //         thread.start();
            
        // }
        
    }
    public static void main(String args[])throws IOException,InterruptedException{
        try{    
            String ip = "localhost";
            if(args.length>0){
                ip = args[0];
            }
            new EncryptedServer(6000,6100,ip);
        }
        catch(Exception err){
            System.out.println("Caught Error. Server Closing");
            System.out.println(err);
        }
    }
}