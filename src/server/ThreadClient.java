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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Empleados;
import model.LoginConsulta;
import model.Persona;
import model.Personal;
import model.Users;

/**
 *
 * 
 */
public class ThreadClient extends Thread {
    private Socket client;
    private Scanner in;
    private PrintWriter out;
    private HashMap logins;
    private String codigo;
    
    
    

    public ThreadClient(Socket client, HashMap logins) {
        try {
            this.client = client;
            this.in = new Scanner(client.getInputStream());
            this.out = new PrintWriter(client.getOutputStream(), true);
            this.logins = logins;
        } catch (IOException ex) {
            Logger.getLogger(ThreadClient.class.getName()).log(Level.SEVERE, null, ex);
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
        LoginConsulta  lconsulta = new LoginConsulta();
        

        try {
                
            //NUEVO OBJETO
            // Se prepara un flujo de salida y entrada para objetos
            ObjectInputStream inObjeto = new ObjectInputStream(client.getInputStream());
            ObjectOutputStream outObjeto = new ObjectOutputStream( client.getOutputStream());
            
            //primero le enviamos al cliente un objeto para que lo rellene, con el tipo de login, y con los datos que toque, en principio todo a "0" que son strings menos
            //un mensaje que si le avisamos lo que tiene que hacer           
            lconsulta.LoginConsultaInicio();
            //enviamos
            outObjeto.writeObject(lconsulta);//envio mensaje 1 al cliente////////////////////////////////////////////////////////
            
            
            //Ahora obtenemos el objeto lconsulta modificado que nos envia el cliente    
            lconsulta = (LoginConsulta) inObjeto.readObject();//recibo mensaje 1 del cliente////////////////////////////////////////////////////////
            //y ahora lo analizamos
            //dependiendo si el atributo tipoDeLogin es 1 o 2 sabremos si es login o consulta
            if(lconsulta.getTipoDeLogin().equalsIgnoreCase("1")){//si es 1 nos envia usuario y contraseña
                user = conexion.comprobarCredencialesBD(lconsulta.getUsuario(), lconsulta.getContraseña());
                if(user != null){
                    if(logins.containsValue(user.getDni())){
                        System.out.println("Ciente desconectado, ya esta conectado ese usuario");
                        lconsulta.setError("1");
                        lconsulta.setInfoDelServer("Este usuario ya esta logeado. Utiliza el código adjudicado");
                        outObjeto.writeObject(lconsulta);//envio mensaje 2 al cliente////////////////////////////////////////////////////////
                        client.close();
                        System.out.println(logins);                       
                    }else{
                        codigo = Utilidades.crearCodigoLogin(user.getNumtipe());
                        //ahora le enviamos el codigo y cerramos la conexion, en la siguiente conexion ya nos enviará el codigo de usuario y también un codigo de consulta
                        // o sea , 2 codigos, aunque evidentimente primero nos enviara un 2 que es para saber de que manera va a conectar
                        logins.put(codigo,user.getDni());
                        lconsulta.setCodigo(codigo);
                        lconsulta.setError("0");
                        lconsulta.setInfoDelServer("Bienvenido.. te enviamos tu codigo de acceso: "+codigo);
                        outObjeto.writeObject(lconsulta);//envio mensaje 2 al cliente////////////////////////////////////////////////////////
                        
                        System.out.println("Users logeados: "+logins);
                        

                    }                    
                }else{
                    System.out.println("Ciente desconectado, error en el login");
                    lconsulta.setError("2");
                    lconsulta.setInfoDelServer("Error en las credenciales, comprueba que las escribes correctamente");
                    outObjeto.writeObject(lconsulta);//envio mensaje 2 al cliente////////////////////////////////////////////////////////
                    
                    System.out.println(logins);                      
                }
                
                
            }else if(lconsulta.getTipoDeLogin().equalsIgnoreCase("2")){
                if(logins.containsKey(lconsulta.getCodigo())){
                    //no necesitamos al user, pero se puede buscar igual , sería para logs
                    // dni = (String) logins.get(codigo);
                    //si contiene el codigo correcto de usuario, entonces buscamos la consulta
                   // user = conexion.getUsersDNI(dni);
                   ///////////
                   //Seguimos analizando la consulta
                   String codigoUserRecibido = lconsulta.getCodigo(); //el codigo recibido tiene que ser el mismo que le hemos asignado
                   String nombreTabla = lconsulta.getTabla(); //Será el numero de tabla. (ej: 1->empleados 2->users 3-jornada 4-usertipe 5->empresa)
                   String columna = lconsulta.getColumna(); //sera la datosRecibidos que busquemos(ej: juan,1234567D), si ponemos 0 sera todos los de la tabla
                   String palabraAbuscar = lconsulta.getPalabra();// si es el caso será la columna (,dni,nom,etc), si no hay ponemos 0
                   String orden = lconsulta.getOrden();// si es el caso el orden, si no hay ponemos 0  
                   outObjeto = new ObjectOutputStream( client.getOutputStream());
                   //ahora enviamos los datos
                    try {
                        //en estas 3 de abajos buscamos en empleados
                        if(nombreTabla.equalsIgnoreCase("empleados") && columna.equalsIgnoreCase("0")){
                            listaEmpleados = conexion.getEmpleados();
                            //devolvemos la respueta
                            outObjeto.writeObject(listaEmpleados);//envio mensaje 2 al cliente////////////////////////////////////////////////////////

                        }else if(nombreTabla.equalsIgnoreCase("empleados") && columna.equalsIgnoreCase("nom")){
                            listaEmpleados =conexion.getEmpleadosNombre(palabraAbuscar);
                            //devolvemos la respueta
                            outObjeto.writeObject(listaEmpleados);//envio mensaje 2 al cliente////////////////////////////////////////////////////////

                        }else if(nombreTabla.equalsIgnoreCase("empleados") && columna.equalsIgnoreCase("dni")){
                            listaEmpleados =conexion.getEmpleadosDNI(palabraAbuscar);
                            //devolvemos la respueta
                            outObjeto.writeObject(listaEmpleados);//envio mensaje 2 al cliente////////////////////////////////////////////////////////
                            //a partir de aqui vamos con users
                        }else if(nombreTabla.equalsIgnoreCase("users") && columna.equalsIgnoreCase("0")){//todos los users
                            listaUsers =conexion.getUsuarios();
                            //devolvemos la respueta
                            outObjeto.writeObject(listaUsers);//envio mensaje 2 al cliente////////////////////////////////////////////////////////
                        }else if(nombreTabla.equalsIgnoreCase("users") && columna.equalsIgnoreCase("login")){//usuario por login
                            listaUsers =conexion.getUsuarioLogin(palabraAbuscar);
                            //devolvemos la respueta
                            outObjeto.writeObject(listaUsers);//envio mensaje 2 al cliente////////////////////////////////////////////////////////
                        }
                        //y habra que ir añadiendo
                        
                    } catch (SQLException ex) {
                        Logger.getLogger(ThreadClient.class.getName()).log(Level.SEVERE, null, ex);
                    }
                   
                   
                    
                    
                    
                    
                    
                }else{
                   System.out.println("Este codigo no esta registrado: "+lconsulta.getCodigo()); 
                   
                }
              
                
            }else if(lconsulta.getTipoDeLogin().equalsIgnoreCase("3")){
  
                //System.out.println("Valor del tipo de login: "+lconsulta.getTipoDeLogin()+ " Valor del codigo de usuario: "+lconsulta.getCodigo());
                
                if(lconsulta.getCodigo().equalsIgnoreCase("0") || lconsulta.getCodigo().equals(null)){
                    System.out.println("El cliente ha mandado un codigo erroneo para cerrar sesion.. se ignora");
                }else{
                    System.out.println("El cliente "+lconsulta.getCodigo()+" ha cerrado sesion");
                    lconsulta.setInfoDelServer("Cerrando sesion en el server.. hasta otra");
                    outObjeto.writeObject(lconsulta);
                    logins.remove(lconsulta.getCodigo());//su el usuario sale, se borra el HashMap el dni
                    System.out.println("Users conectados: "+logins);

                }
                /*
                    lconsulta.setInfoDelServer("Cerrando sesion en el server.. hasta otra");
                    outObjeto.writeObject(lconsulta);
                    logins.remove(lconsulta.getCodigo());//su el usuario sale, se borra el HashMap el dni
                    System.out.println("Users conectados: "+logins);
                */
              
            }else{
                System.out.println("Este codigo de tipoDeLogin no existe, de momento solo hay 1,2 y 3"); 
                System.out.println("Codigo recibido: "+lconsulta.getCodigo());
                
                
            }
            client.close();                  
            
        }catch(NullPointerException e) {
            System.out.println("NullPointerException thrown!");
           // logins.remove(user.getDni());//su el usuario sale, se borra el HashMap el dni
            //System.out.println("Users conectados: "+logins);
            
	}catch(ArrayIndexOutOfBoundsException e) {
            System.out.println("ArrayIndexOutOfBoundsException thrown!");
            //logins.remove(user.getDni());//su el usuario sale, se borra el HashMap el dni
            //System.out.println("Users conectados: "+logins);
            
	}catch (IOException ex) {          
            Logger.getLogger(ThreadClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ThreadClient.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
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