package edu.spbu.clientserver;


import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class Server {
    public static DataInputStream sin;
    public static DataOutputStream sout;

    public static void main(String[] args){
        try{
            ServerSocket server = new ServerSocket(8080);
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
                if(str[0].equals("GET")){
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
            String message="HTTP/1.1 200 OK\r\n" +
                    "Server: Kakoi-to server\r\n" +
                    "Content-Type: text/html\r\n" +
                    "Connection: close\r\n\r\n" + content;;
            sout.write(message.getBytes());
            sout.close();
        } else
        {
            sout.write("<html><head><title>сломалосб</title></head><body><h1>404</h1></body></html>".getBytes());
            sout.flush();
        }
        sout.close();
    }
}
