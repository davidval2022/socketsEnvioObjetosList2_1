/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;


public class MainServer {

    /**
     * @param args the command line arguments
     */
    
    
    
    public static void main(String[] args) {
        try {
            ServerSocket server = new ServerSocket(8888);
            HashMap<String,String> logins = new HashMap<String,String>();
            int i = 0;
            while (true) {
                System.out.println("Esperant client...en el nuevo");
                Socket socket = server.accept();
                System.out.println("Client connectat" + i);
                i++;
                new ThreadClient(socket,logins).start();

            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
