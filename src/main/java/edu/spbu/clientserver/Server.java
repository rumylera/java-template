package edu.spbu.clientserver;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class Server {
    private static DataInputStream sin;
    private static DataOutputStream sout;

    public static void main(String[] args){
        try{
            ServerSocket server = new ServerSocket(666);
            System.out.println("Server is running(away from you)...");
            while(true){
                Socket client = server.accept();
                System.out.println("There's someone behind the door...");
                sin = new DataInputStream(client.getInputStream());
                sout = new DataOutputStream(client.getOutputStream());
                String address = receive();
                send(address);
            }
        } catch(IOException e){
            e.printStackTrace();
        }
    }
    public static String receive(){
        String address;
        BufferedReader br = new BufferedReader(new InputStreamReader(sin));
        try {
            String line = br.readLine();
            if(line != null){
                String[] str = line.split(" ");
                System.out.println(Arrays.toString(str));
                if(str[0].equals("Get")){
                    address = str[1].substring(1);
                    System.out.println("Hey, " + address + ", where r u??");
                    return address;
                }
                else {
                    System.out.println("Ooops...");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static void send(String address) throws IOException{
        File file = new File(address);
        if(file.exists()){
            BufferedReader br = new BufferedReader(new FileReader(file));
            String str, content;
            StringBuilder responser = new StringBuilder();
            str = br.readLine();
            while(str!=null){
                responser.append(str);
                str = br.readLine();
            }
            content = responser.toString();
            String message="Server answer: " + content;
            sout.write(message.getBytes());
            sout.close();
        } else{
        sout.writeUTF("Net tut takih pokemonov");
        }
    }
}
