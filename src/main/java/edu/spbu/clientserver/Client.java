package edu.spbu.clientserver;

import java.io.*;
import java.net.Socket;

public class Client implements Runnable{

    private static DataOutputStream sout;
    private static DataInputStream sin;
    private static String request;

    Client(String server, int port) throws IOException{
        Socket socket = new Socket(server, 8080);
        request = "GET /page.html HTTP/1.1\r\nHost: " + server +"\r\n\r\n";
        sout = new DataOutputStream(socket.getOutputStream());
        sin = new DataInputStream(socket.getInputStream());

    }

    public static void send(String server){
        try{
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(sout));
            out.write(request);
            System.out.println("Sent!");
            out.newLine();
            out.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
    }
    public static void receive(){
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(sin));
            String message = br.readLine();
            while(message != null){
                System.out.println(message);
                message = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            send("127.0.0.01");
            Thread.sleep(1000);
            receive();
        }catch(InterruptedException e)
        {
            e.printStackTrace();
        }

    }
}
