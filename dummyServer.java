import java.io.*; 
import java.net.*; 
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class dummyServer { 


   
    public static void main(String argv[]) throws Exception 
    { 
        String usernameRequest;
        String usernameRegistered;
        String receiver_usernameRequest;
        String receiver_usernameRegistered;
        String clientSentence; 
        String capitalizedSentence; 

      ServerSocket welcomeSocket = new ServerSocket(6000); 
  
      while(true) { 
  
        Socket connectionSocket = welcomeSocket.accept(); 

        BufferedReader inFromClient = 
            new BufferedReader(new
            InputStreamReader(connectionSocket.getInputStream())); 
                    
        DataOutputStream  outToClient = 
                new DataOutputStream(connectionSocket.getOutputStream()); 
        
            usernameRequest = inFromClient.readLine(); 
            String newline = inFromClient.readLine(); 
            if(usernameRequest == null)
                break;
            Pattern pattern = Pattern.compile("REGISTER TOSEND (.*?)$");
            Matcher matcher = pattern.matcher(usernameRequest);
            if (matcher.find())
            {
                System.out.println(matcher.group(1));
                usernameRegistered = "REGISTERED TOSEND " +matcher.group(1) + "\n\n"; 
                outToClient.writeBytes(usernameRegistered); 
            }  
            else
            {
                usernameRegistered = "ERROR 100 Malformed username"; 
                outToClient.writeBytes(usernameRegistered); 
                continue;
            }

            ServerSocket welcomeSocket2 = new ServerSocket(6100);
            Socket connectionSocket2 = welcomeSocket2.accept(); 
            BufferedReader inFromClient2 = 
            new BufferedReader(new
            InputStreamReader(connectionSocket2.getInputStream())); 
            DataOutputStream  outToClient2 = 
                new DataOutputStream(connectionSocket2.getOutputStream()); 

            System.out.println("Waiting");
            receiver_usernameRequest =inFromClient2.readLine();
            System.out.println(receiver_usernameRequest);
            if(usernameRequest == null)
                break;
            pattern = Pattern.compile("REGISTER TORECV (.*?)$");
            matcher = pattern.matcher(receiver_usernameRequest);
            System.out.println("K");
            if (matcher.find())
            {
                System.out.println(matcher.group(1));
                receiver_usernameRegistered = "REGISTERED TORECV " +matcher.group(1) + "\n\n"; 
                outToClient2.writeBytes(receiver_usernameRegistered); 
            }  
            else
            {
                receiver_usernameRegistered = "ERROR 100 Malformed username\n\n"; 
                outToClient2.writeBytes(receiver_usernameRegistered); 
            }

        
           
        } 
    } 
}