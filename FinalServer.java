public class FinalServer{


    public static void main(String args[]){
        if(args.length<1){
            System.out.println("Please enter arguments as {mode-1/2/3} {server ip}");
        }
        String ip = "localhost";
        if(args.length>=2)ip = args[1];
        try{
            if(args[0].matches("1")){
                new Server(6000,6100,ip);
            }
            else if(args[0].matches("2")){
                new EncryptedServer(6000, 6100, ip);
            }
            else if(args[0].matches("3")){
                new EncryptedSignatureServer(6000,6100,ip);
            }
        }
        catch(Exception e){
            System.out.println("caught exception. closing the server");
            System.out.println(e);
        }
    }
}