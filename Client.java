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
        ServerIP = ServerIP;
        username = username;
        inFromUser = new BufferedReader(new InputStreamReader(System.in));
    }
    public void InitialiseSend()
    {
        boolean a = openSendSocket(ServerIP);
        a = registerToSend(username);
    }
    public void InitialiseReceive()
    {
        boolean a = openReceiveSocket(ServerIP);
        a = registerToReceive(username);
    }

    public boolean openSendSocket(String IP)
    {
        while(true){
            try 
            {
                SendSocket = new Socket(IP, 6000);
                toSendServerStream = new DataOutputStream(SendSocket.getOutputStream());
                inFromSendServer = new BufferedReader(new InputStreamReader(SendSocket.getInputStream())); 
                return true;
            } catch (UnknownHostException e) {
                System.err.println("Don't know about host: hostname");
            } catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection to: hostname");
            }
        }
    }
    public boolean registerToSend(String username)
    {
        if(SendSocket == null)
            openSendSocket(ServerIP);
        while(true){
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
                return true;
            } catch (UnknownHostException e) {
                System.err.println("Don't know about host: hostname");
            } catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection to: hostname");
            }
        }
    }

    public boolean registerToReceive(String username)
    {
        if(ReceiveSocket == null)
            openReceiveSocket(ServerIP);
        while(true){
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
                continue;
            String[] array = input.substring(1).split(" ",2);
            if(array.length <2){
                System.out.println("Wrong Format!!\n");
                continue;
            }

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

            try{
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
            
            input = inFromReceiveServer.readLine();

            if(correct == false)
            {
                String output = "ERROR 103 Header incomplete\n\n";
                toReceiveServerStream.writeBytes(output);
                ReceiveSocket.close();
                continue;
            }
            String message = "";
            
            for(int i =0;i<length;i++)
                message = message +inFromReceiveServer.read();

            String output = "RECEIVED "+fromusername+"\n\n";
            toReceiveServerStream.writeBytes(output);

            System.out.println("From "+fromusername+ ": "+message);
            }
            catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection to: hostname");
                InitialiseReceive();
            }
            catch(Exception e)
            {
                System.out.println("WrongLength");
                String output = "ERROR 103 Header incomplete\n\n";
                try{
                toReceiveServerStream.writeBytes(output);
                ReceiveSocket.close();
                }
                catch(Exception r)
                {
                    System.out.println("No connection");
                }
                
                InitialiseReceive();
                continue;
            }

            

        }

    }

    public static void main(String[] args) {
    
        Client client = new Client(args[0],"localhost");

        boolean a = client.openSendSocket(args[1]);
        if(a == false)
            return;
        a = client.registerToSend(args[0]);
        if(a == false)
            return;
        a = client.openReceiveSocket(args[1]);
        if(a == false)
            return;
        a = client.registerToReceive(args[0]);
        if(a == false)
            return;
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