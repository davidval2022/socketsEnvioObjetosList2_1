/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

/**
 *
 * @author david
 */
//import main.java.ioc.dam.m9.uf3.eac2.b2.*;
import Conexion.Conexion;
import Utilidades.Utilidades;
import cliente.ThreadConexion;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Empleados;
import model.Persona;
import model.Personal;
import model.Users;

/**
 *
 * 
 */
public class ThreadClient_backup extends Thread {
    private Socket client;
    private Scanner in;
    private PrintWriter out;
    private HashMap logins;
    private String codigo;
    
    
    

    public ThreadClient_backup(Socket client, HashMap logins) {
        try {
            this.client = client;
            this.in = new Scanner(client.getInputStream());
            this.out = new PrintWriter(client.getOutputStream(), true);
            this.logins = logins;
        } catch (IOException ex) {
            Logger.getLogger(ThreadClient_backup.class.getName()).log(Level.SEVERE, null, ex);
        }         
    }
    

    
    @Override
    public void run() {
        String msg;
        Personal personal = new Personal();
        List<Empleados> listaEmpleados = new ArrayList<Empleados>();
        List<Users> listaUsers = new ArrayList<Users>();
        
        String[] codigosLogin = new String[2];
        codigo = "-1";
        String dni = "-1";
        Conexion conexion = new Conexion();
        Users user = null;
        
        boolean salir = false;
        try {
            
            //IMPLEMENTA
            BufferedWriter escriptor = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            BufferedReader lector = new BufferedReader(new InputStreamReader(client.getInputStream()));
            
            //NUEVO OBJETO
            // Se prepara un flujo de salida para objetos
            //ObjectOutputStream outObjeto = new ObjectOutputStream( client2.getOutputStream());
            ObjectOutputStream outObjeto;
            
            //enviamos al cliente la pregunta y el mensaje de bienvenida
            msg = "Bienvenido, inicia sesion! Finaliza con Exit";
            escriptor.write(msg);//enviamos el mensaje
            escriptor.newLine();
            escriptor.flush();
            //leemos la respuesta  que nos devuleve el cliente con el login y pass con este formato "login:pass"
            String palabra = lector.readLine(); //recibimos
            if(palabra.equalsIgnoreCase("exit")){
                System.out.println("Ciente desconectado");
                salir = true;
                escriptor.close();
                lector.close();
                client.close();

            }else{
                System.out.println("Datos de login recibidos: "+palabra);  
                String[] datos = new String[2];//preparamos un array de string para login:password
                String login = "-1";
                String pass = "-1";
                datos = palabra.split(":");
                if(datos[0] != null && datos[1] != null){
                    login = datos[0];
                    pass = datos[1];                    
                }

                user = conexion.comprobarCredencialesBD(login, pass);
                
                if(user != null){
                    if(logins.containsKey(user.getDni())){
                        msg = "-2";
                        System.out.println("Ciente desconectado, ya esta conectado ese usuario");
                        escriptor.write(msg);//enviamos
                        escriptor.newLine();
                        escriptor.flush();                    
                        lector.close();
                        client.close();
                        System.out.println(logins);
                        
                    }else{
                        codigo = Utilidades.crearCodigoLogin(user.getNumtipe());
                    }
                    
                }

                if(codigo.equalsIgnoreCase("-1")){//si es -1 es un error
                    msg = "-1";
                    System.out.println("Ciente desconectado, error en el login");
                    escriptor.write(msg);//enviamos
                    escriptor.newLine();
                    escriptor.flush();                    
                    lector.close();
                    client.close();
                }else{        
                    //si no es -1 es que nos ha enviado un codigo
                    //enviamos al cliente el codigo y luego ya vamos con que nos envie que quiere buscar (Empleados)
                    //comprobamos que el usuario no este ya conectado, para eso miramos el dni
                    
                    msg = codigo;
  
                    logins.put(user.getDni(), codigo);
                    escriptor.write(msg);//enviamos
                    escriptor.newLine();
                    escriptor.flush();
                    System.out.println("Users conectados: "+logins);
                    while(!salir){
                        //leemos la respuesta con la palabra exit o con el  grupo de codigos para identificar que estamos buscando
                        palabra = lector.readLine(); //recibimos
                        if(palabra.equalsIgnoreCase("exit") || palabra.equals(null)){
                            System.out.println("Ciente desconectado");
                            salir = true;
                            escriptor.close();
                            //outObjeto.close();
                            lector.close();
                            client.close();
                            logins.remove(user.getDni());//su el usuario sale, se borra el HashMap el dni
                            System.out.println("Users conectados: "+logins);

                        }else{
                            //la idea es que el cliente nos envie un codigo, primero el codigo de conexion, luego el nombre de la tabla ,luego la columna si es necesaria y si no
                            //ponemos un 0, luego la palabra a buscar y si no un 0, y luego el orden y si no un 0
                            //luego columna ,luego orden, si no queremos palabra columna ni orden ponemos 0 y 0 y 0 y si no ponemos la palabra que corresponda
                            //ej 1: U12112|||empleados|||nom|||juan|||0  (busca en empleados a juan)
                            //ej 2: U12112|||empleados|||dni|||12345678A|||0  (buscamos en empledos por dni)
                            //ej 3: U12112|||empleados|||0|||0|||0  (buscamos at todos los empleados
                            //ej 4: U12112|||users|||0|||0|||0  (buscamos at todos los usuarios


                            System.out.println("Contenido a enviar al cliente: "+palabra);  
                            //ahora comprobamos que el código sea correcto
                            String[] frase = new String[5];
                            frase = palabra.split(":");
                            String codigoUserRecibido = frase[0]; //el codigo recibido tiene que ser el mismo que le hemos asignado
                            String nombreTabla = frase[1]; //Será el numero de tabla. (ej: 1->empleados 2->users 3-jornada 4-usertipe 5->empresa)
                            String columna = frase[2]; //sera la palabra que busquemos(ej: juan,1234567D), si ponemos 0 sera todos los de la tabla
                            String palabraAbuscar = frase[3];// si es el caso será la columna (,dni,nom,etc), si no hay ponemos 0
                            String orden = frase[4];// si es el caso el orden, si no hay ponemos 0
                            if(!codigo.equals(codigoUserRecibido)){
                                System.out.println("Ciente desconectado, el codigo no era correcto");
                                salir = true;
                                escriptor.close();
                                //outObjeto.close();
                                lector.close();
                                client.close();
                            
                            }else{
                                //System.out.println("codigoUserRecibido: "+codigoUserRecibido);
                                //System.out.println("nombreTabla: "+nombreTabla); 
                                //System.out.println("columna: "+columna); 
                                //System.out.println("palabraAbuscar: "+palabraAbuscar); 
                                //System.out.println("orden: "+orden); 
                                
                                try {
                                    //en estas 3 de abajos buscamos en empleados
                                    if(nombreTabla.equalsIgnoreCase("empleados") && columna.equalsIgnoreCase("0")){
                                        listaEmpleados = conexion.getEmpleados();
                                        //devolvemos la respueta
                                        outObjeto = new ObjectOutputStream( client.getOutputStream());
                                        outObjeto.writeObject(listaEmpleados);
                                        
                                    }else if(nombreTabla.equalsIgnoreCase("empleados") && columna.equalsIgnoreCase("nom")){
                                        listaEmpleados =conexion.getEmpleadosNombre(palabraAbuscar);
                                        //devolvemos la respueta
                                        outObjeto = new ObjectOutputStream( client.getOutputStream());
                                        outObjeto.writeObject(listaEmpleados);
                                        
                                    }else if(nombreTabla.equalsIgnoreCase("empleados") && columna.equalsIgnoreCase("dni")){
                                        listaEmpleados =conexion.getEmpleadosDNI(palabraAbuscar);
                                        //devolvemos la respueta
                                        outObjeto = new ObjectOutputStream( client.getOutputStream());
                                        outObjeto.writeObject(listaEmpleados);
                                        //a partir de aqui vamos con users
                                    }else if(nombreTabla.equalsIgnoreCase("users") && columna.equalsIgnoreCase("0")){//todos los users
                                        listaUsers =conexion.getUsuarios();
                                        //devolvemos la respueta
                                        outObjeto = new ObjectOutputStream( client.getOutputStream());
                                        outObjeto.writeObject(listaUsers);
                                    }else if(nombreTabla.equalsIgnoreCase("users") && columna.equalsIgnoreCase("login")){//usuario por login
                                        listaUsers =conexion.getUsuarioLogin(login);
                                        //devolvemos la respueta
                                        outObjeto = new ObjectOutputStream( client.getOutputStream());
                                        outObjeto.writeObject(listaUsers);
                                    }//y habra que ir añadiendo
                                } catch (SQLException ex) {
                                    Logger.getLogger(ThreadClient_backup.class.getName()).log(Level.SEVERE, null, ex);
                                 }

                                
                            }
                                                      

                        }
                    }
                }
                
                
               
            }                    
            
        }catch(NullPointerException e) {
            System.out.println("NullPointerException thrown!");
            logins.remove(user.getDni());//su el usuario sale, se borra el HashMap el dni
            System.out.println("Users conectados: "+logins);
            
	}catch(ArrayIndexOutOfBoundsException e) {
            System.out.println("ArrayIndexOutOfBoundsException thrown!");
            logins.remove(user.getDni());//su el usuario sale, se borra el HashMap el dni
            System.out.println("Users conectados: "+logins);
            
	}catch (IOException ex) {          
            Logger.getLogger(ThreadClient_backup.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
                try {
                    if (client != null) {
                        client.close();
                   }
               } catch (IOException ioe) {
                   ioe.printStackTrace();
               }            
        }
    }  
    
    

    
}