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

    
    public Client(String username, String ServerIP)
    {
        this.ServerIP = ServerIP;
        this.username = username;
        this.inFromUser = new BufferedReader(new InputStreamReader(System.in));
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
    /*
    public boolean registerToSend(String username)
    {
        while(true){
        if(SendSocket == null)
            openSendSocket(ServerIP);
            try
            {
                //System.out.print("Please Enter Your Username: ");
                //String username = inFromUser.readLine();
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
                //System.out.println("Please Try Again");
            }
            catch (IOException e)
            {
                //System.err.println("Please Try Again");
            }
        }
    }*/

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
    /*
    public boolean registerToReceive(String username)
    {
        while(true){
            
        if(ReceiveSocket == null)
            openReceiveSocket(ServerIP);
            try
            {
                
                //System.out.print("Please Enter Your Username: ");
                //String username = inFromUser.readLine();
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
                //System.out.println("Please Try Again");
            }
            
            catch (IOException e)
            {
                System.err.println("Server Not Online");
            }
        }
    }
    
    public void send()
    {
        while(true){

            try{

            String input = inFromUser.readLine();
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
            username = array[0];
            String output = "SEND " + array[0] +"\nContent-length: "+array[1].length()
                                +"\n\n"+array[1];
            //System.out.println(output);
            //System.out.print(array[1]);
            
            toSendServerStream.writeBytes(output); 
            }
            catch(Exception e)
            {
                System.out.println("Caught");
            }

            try{//was throwing error since username was null and username.equals was called
                String response = inFromSendServer.readLine();
                String newline = inFromSendServer.readLine();
                Pattern pattern = Pattern.compile("SENT (.*?)$");
                Matcher matcher = pattern.matcher(response);
                if (matcher.find())
                {
                    if(username.equals(matcher.group(1))){
                        System.out.println("Message Sent!");
                    }
                }
                pattern = Pattern.compile("ERROR 102 Unable to send$");
                matcher = pattern.matcher(response);
                if (matcher.find())
                    System.out.println("Unable to Send!");
                pattern = Pattern.compile("ERROR 103  Header incomplete");
                matcher = pattern.matcher(response);
                if (matcher.find()){
                    System.out.println("Header Incomplete!");
                    InitialiseSend();
                }
                
            }
            catch(Exception e)
                {
                    System.out.println("Error");
                }

        }
    }*/

    public void send()
    {
        while(true){

            try{
                
                String input = inFromUser.readLine();
                
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
                username = array[0];
                if(username.equals("")){
                    System.out.println("Message should be in format @[username] [message]");
                    continue;
                }
                String output = "SEND " + array[0] +"\nContent-length: "+array[1].length()
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
                    if(username.equals(matcher.group(1))){
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
            

            Pattern pattern = Pattern.compile("FORWARD (.*?)$");
            Matcher matcher = pattern.matcher(input);
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
                    //System.out.println("No connection");
                }
                
                //InitialiseReceive();
                
            }

            

        }

    }

    public static void main(String[] args) {
    
        Client client = new Client(args[0],args[1]);

        //boolean a = client.openSendSocket(args[1]);
        //if(a == false)
        //    return;
        //a = client.registerToSend(args[0]);
        //if(a == false)
        //   return;
        boolean a = client.openReceiveSocket(args[1]);
        if(a == false)
            return;
        //a = client.registerToReceive(args[0]);
        //if(a == false)
        //    return;
        System.out.println("\nYou can now start sending messages!\n");
        //client.send();
        //client.receive();
        Thread t1 = new Thread() {

            @Override
            public void run() {
                client.send();
            }
        };
        Thread t2 = new Thread() {

            public void run() {
                client.receive();
            }
        };

        
            t1.start();
            t2.start();
        
        
        
        
    }
}
