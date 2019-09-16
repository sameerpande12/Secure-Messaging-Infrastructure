public class FinalClient{
    public static void main(String args[]){
        if(args.length!=3){
            System.out.println("Please enter arguments as {username} {server ip} {mode=1/2/3}");
        }
        String username = args[0];
        String ip = args[1];
        String mode = args[2];

        if(mode.matches("1")){
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
        }
        else if(mode.matches("2")){

                EncryptedClient client = new EncryptedClient(args[0],args[1]);

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
        }
        else if(mode.matches("3")){

            EncryptedSignatureClient client = new EncryptedSignatureClient(args[0],args[1]);

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
        else{
            System.out.println("Please enter correct mode value");
        }
    }
}