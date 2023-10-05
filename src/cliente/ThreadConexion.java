/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente;

/**
 *
 * @author tomas
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Empleados;
import model.LoginConsulta;
import model.Users;
//import model.Persona;

public class ThreadConexion extends Thread {
    
    String codigo = "";

    public ThreadConexion() {
    }
    
    public ThreadConexion(String codigo) {
        this.codigo = codigo;    
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    @Override
    public void run() {
        //super.run(); //To change body of generated methods, choose Tools | Templates.
        try{
            Socket socket = new Socket("localhost", 8888);
            Scanner lector = new Scanner(System.in);     
            LoginConsulta lconsulta = new LoginConsulta();
            // Se prepara un flujo de salida y entrada para objetos
            ObjectInputStream inObjeto = new ObjectInputStream(socket.getInputStream());
            ObjectOutputStream outObjeto = new ObjectOutputStream( socket.getOutputStream());
            
            String palabra= "";
            
            System.out.println("El cliente ha conectado, ahora te pedire el usuario y la contraseña");
            if(codigo.equalsIgnoreCase("0")){//si no tenemos codigo enviamos un objeto con el usuario y contraseña para hacer login y que nos envien un codigo
                System.out.println("Introduce usuario:");
                palabra = lector.next();
                lector.nextLine();
                lconsulta.setUsuario(palabra);
                System.out.println("Introduce contraseña:");
                palabra = lector.next();
                lector.nextLine();  
                lconsulta.setContraseña(palabra);
                //ahora acabamos el objeto lconsulta y lo enviamos al server para que nos logee y nos envie un codigo
                lconsulta.setTipoDeLogin("1");//como vamos a hacer que nos logee el tipo de login es 1
                outObjeto.writeObject(lconsulta);
                //ahora esperamos el codigo que nos debe de enviar si las credenciales son correctas.
                //Para hacer esto  lo que hará será coger nuestro objeto y añadirle el codigo.
                lconsulta =(LoginConsulta) inObjeto.readObject();
                //ahora vamos a leer  y guardar el codigo
                codigo = lconsulta.getCodigo();               
                //ahora cerramos la conexion. La siguiente vez que conectemos ya enviaremos el codigo, no hará falta enviar usuario y contraseña.  
                System.out.println("El codigo adjudicados es: "+codigo);
                
            }else{
                System.out.println("El codigo adjudicados es: "+codigo + "Así que tenemos que enviarlo en el objeto");
                
            }
            
            
            
        }catch(IOException e){
            System.out.println("e");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ThreadConexion.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    

    
    
    
    
    
    
    public static int contarCaracteres(String cadena, char caracter) {//para contar los :
        int posicion, contador = 0;
        //se busca la primera vez que aparece
        posicion = cadena.indexOf(caracter);
        while (posicion != -1) { //mientras se encuentre el caracter
            contador++;           //se cuenta
            //se sigue buscando a partir de la posición siguiente a la encontrada                                 
            posicion = cadena.indexOf(caracter, posicion + 1);
        }
        return contador;
    }



}