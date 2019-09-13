import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.MessageDigest; 

import javax.crypto.Cipher;

class Cryptography{

    private static final String ALGORITHM = "RSA";

    public static byte[] encrypt(byte[] publicKey, byte[] inputData, boolean reverse)
            throws Exception {
        if(reverse == false)
        {
            PublicKey key = KeyFactory.getInstance(ALGORITHM)
                    .generatePublic(new X509EncodedKeySpec(publicKey));

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] encryptedBytes = cipher.doFinal(inputData);

            return encryptedBytes;
        }
        else
        {
            PrivateKey key = KeyFactory.getInstance(ALGORITHM)
                .generatePrivate(new PKCS8EncodedKeySpec(publicKey));

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] encryptedBytes = cipher.doFinal(inputData);

            return encryptedBytes;
        }
    }

    public static byte[] decrypt(byte[] privateKey, byte[] inputData,boolean reverse)
            throws Exception {
        if(reverse == false)
        {
            PrivateKey key = KeyFactory.getInstance(ALGORITHM)
                    .generatePrivate(new PKCS8EncodedKeySpec(privateKey));

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] decryptedBytes = cipher.doFinal(inputData);

            return decryptedBytes;
        }
        else{
            PublicKey key = KeyFactory.getInstance(ALGORITHM)
                    .generatePublic(new X509EncodedKeySpec(privateKey));

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] decryptedBytes = cipher.doFinal(inputData);

            return decryptedBytes;
        }
    }

    public static KeyPair generateKeyPair()
            throws NoSuchAlgorithmException, NoSuchProviderException {

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);

        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");

        // 512 is keysize
        keyGen.initialize(512, random);

        KeyPair generateKeyPair = keyGen.generateKeyPair();
        return generateKeyPair;
    }
}

public class Client3 {

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
    byte[] publicKey;
    byte[] privateKey;
    
    public Client3(String username, String ServerIP)
    {
        this.ServerIP = ServerIP;
        this.username = username;
        this.inFromUser = new BufferedReader(new InputStreamReader(System.in));
        try{
        KeyPair generateKeyPair = Cryptography.generateKeyPair();
        publicKey = generateKeyPair.getPublic().getEncoded();
        privateKey = generateKeyPair.getPrivate().getEncoded();
        }
        catch(Exception e)
        {
            System.out.println("CryptoKey Error");
        }
        

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
                
                String base64publicKey = java.util.Base64.getEncoder().encodeToString(this.publicKey);
                String username_packet = "REGISTER TORECV " + username +"\n" +"Content-length: "+Integer.toString(base64publicKey.length())+"\n\n"+base64publicKey;
                toReceiveServerStream.writeBytes(username_packet); 
                //toReceiveServerStream.write(publicKey,0,publicKey.length);
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

                String output = "FETCHKEY " + to_username+"\n\n";
                
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

                input = inFromSendServer.readLine();
                boolean correct = true;

                int length = 0;

                Pattern pattern = Pattern.compile("FETCHEDKEY (.*?)$");
                Matcher matcher = pattern.matcher(input);
                if (matcher.find())
                    to_username = matcher.group(1);
                else
                    correct = false;

                input = inFromSendServer.readLine();
                pattern = Pattern.compile("Content-length: (.*?)$");
                matcher = pattern.matcher(input);
                if (matcher.find())
                    length = Integer.parseInt(matcher.group(1));
                else
                    correct = false;
            // System.out.println("Message Length :"+Integer.toString(length));
                input = inFromSendServer.readLine();
                
                if(correct == false)
                {
                    output = "ERROR 103 Header incomplete\n\n";
                    System.out.println("Error with Server");
                    toSendServerStream.writeBytes(output);
                    SendSocket.close();
                    openSendSocket(this.ServerIP);
                    continue;
                }
                

                char[] msg_buf= new char[length];
                int contentLength = inFromSendServer.read(msg_buf,0,length);
                String receiverPublicKeyString = new String(msg_buf);
                toSendServerStream.writeBytes("FETCH ACK\n\n");
                
                byte[] receiverPublicKey = java.util.Base64.getDecoder().decode(receiverPublicKeyString);

                //byte[] encryptedData = 


                String encryptedData = java.util.Base64.getEncoder().encodeToString(Cryptography.encrypt(receiverPublicKey,array[1].getBytes(),false));
                 

                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] shaBytes = md.digest(Cryptography.encrypt(receiverPublicKey,array[1].getBytes(),false));
                System.out.println("ReachedUp");
                String signature = java.util.Base64.getEncoder().encodeToString(Cryptography.encrypt(this.privateKey,shaBytes,true));
                

                output = "SEND " + to_username +"\nContent-length: "+Integer.toString(encryptedData.length())
                                    +"\nSignature: " + signature 
                                    +"\n\n" + encryptedData;
                                    //+array[1];
                //System.out.println(output);
                //System.out.print(array[1]);

                while(true){
                    try{
                        toSendServerStream.writeBytes(output);
                        System.out.println(output);
                        //toSendServerStream.write(encryptedData,0,encryptedData.length);
                        break; 
                    }
                    catch(Exception e)
                    {
                        System.out.println("AAH");
                        openSendSocket(this.ServerIP);
                    }
                }
            //was throwing error since username was null and username.equals was called
                String response = inFromSendServer.readLine();
                System.out.println(response);
                
                String newline = inFromSendServer.readLine();
                System.out.println(newline);
                pattern = Pattern.compile("SENT (.*?)$");
                matcher = pattern.matcher(response);
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

            String signature="";
            input = inFromReceiveServer.readLine();
            pattern = Pattern.compile("Signature: (.*?)$");
            matcher = pattern.matcher(input);
            if (matcher.find())
                signature = (matcher.group(1));
            else
                correct = false;
            byte[] sigbyte = java.util.Base64.getDecoder().decode(signature);

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

            char[] msg_buf = new char[length];
            int contentLength = inFromReceiveServer.read(msg_buf,0,length);
            String messageString = new String(msg_buf);
            
            // for(int i =0;i<length;i++)
                // message = message +inFromReceiveServer.read();
            //String = encryptedData
            byte[] decryptedData = Cryptography.decrypt(this.privateKey, java.util.Base64.getDecoder().decode(messageString),false);
            String message = new String(decryptedData);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] shaBytes = md.digest(java.util.Base64.getDecoder().decode(messageString));

            String output = "FETCHKEY " + fromusername+"\n\n";
                
            while(true){
                try{
                    toReceiveServerStream.writeBytes(output);
                    break; 
                }
                catch(Exception e)
                {
                    openReceiveSocket(this.ServerIP);
                }
            }

            input = inFromReceiveServer.readLine();
            System.out.println("Her"+input);
            correct = true;

            length = 0;

            pattern = Pattern.compile("FETCHEDKEY (.*?)$");
            matcher = pattern.matcher(input);
            System.out.println("WritingInput"+input);
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
            System.out.println("Writing"+length);
            System.out.println("Writing"+input);
            if(correct == false)
            {
                output = "ERROR 103 Header incomplete\n\n";
                System.out.println("Error with Server");
                toReceiveServerStream.writeBytes(output);
                ReceiveSocket.close();
                openReceiveSocket(this.ServerIP);
                continue;
            }
            System.out.println("Writing");
            toReceiveServerStream.writeBytes("FETCH ACK\n\n");
            msg_buf= new char[length];
            contentLength = inFromReceiveServer.read(msg_buf,0,length);
            String senderPublicKeyString = new String(msg_buf);
            
            byte[] senderPublicKey = java.util.Base64.getDecoder().decode(senderPublicKeyString);
            byte[] KpubHash = Cryptography.decrypt(senderPublicKey,sigbyte,true);

            System.out.println(KpubHash);
            System.out.println(shaBytes);














            output = "RECEIVED "+fromusername+"\n\n";
            toReceiveServerStream.writeBytes(output);

            System.out.println("From "+fromusername+ ": "+message);
            }
            catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection to: hostname");
                //InitialiseReceive();
            }
            catch(Exception e)
            {
                System.out.println("r");
                    
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
    
        Client3 client = new Client3(args[0],args[1]);

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
