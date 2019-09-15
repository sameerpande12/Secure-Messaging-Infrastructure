import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {

    public Socket SendSocket = null; 
    public Socket ReceiveSocket = null; 
    public String ServerIP = "";
    public String username = null;
    public DataOutputStream toSendServerStream = null;
    public BufferedReader inFromSendServer = null;
    public DataOutputStream toReceiveServerStream = null;
    public BufferedReader inFromReceiveServer = null;
    BufferedReader inFromUser = null;
    public Thread t1, t2;
    boolean register = true;
    
    public Client(String username, String ServerIP)
    {
        this.ServerIP = ServerIP;
        this.username = username;
        this.inFromUser = new BufferedReader(new InputStreamReader(System.in));
        t1 = new Thread() {

            @Override
            public void run() {
                send();
            }
        };
        t2 = new Thread() {

            public void run() {
                receive();
            }
        };
    }
    public void InitialiseSend()
    {
        boolean a = openSendSocket(ServerIP);
        //a = registerToSend(this.username);
    }
    public void InitialiseReceive()
    {
        boolean a = openReceiveSocket(ServerIP);
        //a = registerToReceive(this.username);
    }

    public void interrupt()
    {     
        System.out.println("Interrupting");   
        t1.interrupt();
        t2.interrupt();
    }
    public boolean openSendSocket(String IP)
    {
        while(true){
            try 
            {
                SendSocket = new Socket(IP, 6000);
                toSendServerStream = new DataOutputStream(SendSocket.getOutputStream());
                inFromSendServer = new BufferedReader(new InputStreamReader(SendSocket.getInputStream())); 

                String username_packet = "REGISTER TOSEND " + username +"\n\n";
                toSendServerStream.writeBytes(username_packet); 
                String response = inFromSendServer.readLine();
                String newline = inFromSendServer.readLine();
                Pattern pattern = Pattern.compile("REGISTERED TOSEND (.*?)$");
                Matcher matcher = pattern.matcher(response);
                if (matcher.find())
                {
                    if(username.equals(matcher.group(1))){
                        System.out.println("You have been registered to send!");
                        return true;
                    }
                }
                pattern = Pattern.compile("ERROR 100 Malformed username");
                matcher = pattern.matcher(response);
                if (matcher.find())
                {
                    System.out.println("Malformed Username");
                    return false;
                }    
                return false;
            } catch (UnknownHostException e) {
                System.err.println("Don't know about host: "+ this.ServerIP);
            } catch (IOException e) {
                System.err.println("Server is not Online. Trying to Establish Connection.");
            }
        }
    }
    

    public boolean openReceiveSocket(String ServerIP)
    {
        while(true)
        {
            try 
            {
                ReceiveSocket = new Socket(ServerIP, 6100);
                toReceiveServerStream = new DataOutputStream(ReceiveSocket.getOutputStream());
                inFromReceiveServer = new BufferedReader(new InputStreamReader(ReceiveSocket.getInputStream())); 

                String username_packet = "REGISTER TORECV " + username +"\n\n";
                toReceiveServerStream.writeBytes(username_packet); 
                String response = inFromReceiveServer.readLine();
                String newline = inFromReceiveServer.readLine();

                Pattern pattern = Pattern.compile("REGISTERED TORECV (.*?)$");
                Matcher matcher = pattern.matcher(response);
                if (matcher.find())
                {
                    if(username.equals(matcher.group(1))){
                        System.out.println("You have been registered to receive!");
                        return true;
                    }
                }
                pattern = Pattern.compile("ERROR 100 Malformed username");
                matcher = pattern.matcher(response);
                if (matcher.find())
                {
                    System.out.println("Malformed Username");
                    return false;
                }

                return false;
            } catch (UnknownHostException e) {
                System.err.println("Don't know about host: "+ ServerIP);
            } catch (IOException e) {
                System.err.println("Server is not Online. Trying to Establish Connection.");
            }
        }
    }
    
    public boolean unregister()
    {
        while(true)
        {
            String output = "UNREGISTER " + username+ "\n\n";
            //System.out.println(output);
            //System.out.print(array[1]);
            
            try{
                toSendServerStream.writeBytes(output);
                
            
                String response = inFromSendServer.readLine();
                String newline = inFromSendServer.readLine();
            

                Pattern pattern = Pattern.compile("UNREGISTERED (.*?)$");
                Matcher matcher = pattern.matcher(response);
                if (matcher.find())
                {
                    if(this.username.equals(matcher.group(1))){
                        System.out.println("Closing Send Server");
                        break;
                    }
                }
            }
            catch(Exception e)
            {
                openSendSocket(this.ServerIP);
                continue;
            }
        }
        return true;
    }

    public void send()
    {
        while(true){

            try{
                
                String input = inFromUser.readLine();
                if(input.equals("UNREGISTER"))
                {
                    boolean success = unregister();
                    if(success)
                        break;
                    else
                    {
                        System.out.println("Unable to unregister");
                        continue;
                    }
                }
                if(input.charAt(0) != '@')
                {
                    System.out.println("Message should be in format @[username] [message]");
                    continue;
                }
                String[] array = input.substring(1).split(" ",2);
                if(array.length <2){
                    System.out.println("Message should be in format @[username] [message]");
                    continue;
                }
                String to_username = array[0];
                if(to_username.equals("")){
                    System.out.println("Message should be in format @[username] [message]");
                    continue;
                }
                String output = "SEND " + to_username +"\nContent-length: "+array[1].length()
                                    +"\n\n"+array[1];
                //System.out.println(output);
                //System.out.print(array[1]);
                
                while(true){
                    try{
                        toSendServerStream.writeBytes(output);
                        break; 
                    }
                    catch(Exception e)
                    {
                        openSendSocket(this.ServerIP);
                    }
                }
            //was throwing error since username was null and username.equals was called
                String response = inFromSendServer.readLine();
                String newline = inFromSendServer.readLine();
                Pattern pattern = Pattern.compile("SENT (.*?)$");
                Matcher matcher = pattern.matcher(response);
                if (matcher.find())
                {
                    if(to_username.equals(matcher.group(1))){
                        System.out.println("Message Sent!");
                        continue;
                    }
                }
                pattern = Pattern.compile("ERROR 102 Unable to send$");
                matcher = pattern.matcher(response);
                if (matcher.find()){
                    System.out.println("Unable to Send!");
                    continue;
                }
                pattern = Pattern.compile("ERROR 103  Header incomplete");
                matcher = pattern.matcher(response);
                if (matcher.find())
                {
                    System.out.println("Header Incomplete!");
                    boolean a = openSendSocket(this.ServerIP);
                }
                
            }
            catch(Exception e)
            {
                //System.out.println("Error");
                openSendSocket(this.ServerIP);
                //throw new NullPointerException();  
            }

        }
    }

    public void receive()
    {
        while(true)
        {
            String fromusername="";
            int length=0;
            boolean correct = true;
            try
            {
            String input = inFromReceiveServer.readLine();
            
            Pattern pattern = Pattern.compile("UNREGISTERED (.*?)$");
            Matcher matcher = pattern.matcher(input);
            if (matcher.find())
            {
                System.out.println("Closed Receiver Socket");
                ReceiveSocket.close();
                break;
            }

            pattern = Pattern.compile("FORWARD (.*?)$");
            matcher = pattern.matcher(input);
            if (matcher.find())
                fromusername = matcher.group(1);
            else
                correct = false;

            input = inFromReceiveServer.readLine();
            pattern = Pattern.compile("Content-length: (.*?)$");
            matcher = pattern.matcher(input);
            if (matcher.find())
                length = Integer.parseInt(matcher.group(1));
            else
                correct = false;
            // System.out.println("Message Length :"+Integer.toString(length));
            input = inFromReceiveServer.readLine();

            if(correct == false)
            {
                String output = "ERROR 103 Header incomplete\n\n";
                toReceiveServerStream.writeBytes(output);
                ReceiveSocket.close();
                openReceiveSocket(this.ServerIP);
                continue;
            }

            char [] msg_buf = new char[length];
            int contentLength = inFromReceiveServer.read(msg_buf,0,length);
            String message = new String(msg_buf);
            
            // for(int i =0;i<length;i++)
                // message = message +inFromReceiveServer.read();

            String output = "RECEIVED "+fromusername+"\n\n";
            toReceiveServerStream.writeBytes(output);

            System.out.println("From "+fromusername+ ": "+message);
            }
            catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection to: hostname");
                //InitialiseReceive();
            }
            catch(Exception e)
            {
                
                    
                //System.out.println("No " +ReceiveSocket);
                if(inFromReceiveServer == null){
                    openReceiveSocket(this.ServerIP);
                    continue;
                }
                //System.out.println("WrongLength");
                String output = "ERROR 103 Header incomplete\n\n";
                try
                {
                    toReceiveServerStream.writeBytes(output);
                    ReceiveSocket.close();
                    openReceiveSocket(this.ServerIP);
                    continue;
                }
                catch(Exception r)
                {
                    openReceiveSocket(this.ServerIP);
                    continue;
                    //throw new NullPointerException();
                    
                }
                
                //InitialiseReceive();
                
            }

            

        }

    }

    public static void main(String[] args) {
    
        Client client = new Client(args[0],args[1]);

        boolean a = client.openSendSocket(args[1]);
        if(a == false)
            return;
        //a = client.registerToSend(args[0]);
        //if(a == false)
        //   return;
        a = client.openReceiveSocket(args[1]);
        if(a == false)
            return;
        //a = client.registerToReceive(args[0]);
        //if(a == false)
        //    return;
        System.out.println("\nYou can now start sending messages!\n");
        //client.send();
        //client.receive();
        

        
        client.t1.start();
        client.t2.start();
        //System.out.println("Interrupted");
        
        
        
    }
}
