/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cliente;


import server.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Empleados;
import model.LoginConsulta;
import model.Users;


public class MainClient {

    /**
     * @param args the command line arguments
     */
    
    static LoginConsulta lconsulta = new LoginConsulta();
    static String codigoGlobal = "0";
    
    public static void main(String[] args) {
        Scanner lector = new Scanner(System.in); 
        boolean salir = false;
        String respuesta = "";
        
        
        while (!salir){
            menu();
            respuesta = lector.next();
            lector.nextLine();
            if(respuesta.equalsIgnoreCase("1")){
                loginEnElServer();
                
            }else if(respuesta.equalsIgnoreCase("2")){
                System.out.println("Has escogido la opcion 2\n");
                consultas();
                
            }else if(respuesta.equalsIgnoreCase("3")){
                System.out.println("codigo: "+codigoGlobal);
               
                
            }else if(respuesta.equalsIgnoreCase("4")){
                try {
                    salir();
                } catch (ClassNotFoundException ex) {
                    System.out.println("No encontrada la clase LoginConsulta");
                    Logger.getLogger(MainClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                System.out.println("Esa opcion no existe\n");
            }                                
        }      
    }
    
 
    public static void menu(){
        System.out.println("MENU");
        System.out.println("1. Login con usuario y contraseña");
        System.out.println("2. Si ya tienes codigo, puedes acceder a las consultas al server desde aquí");     
        System.out.println("3. Ver codigo usuario guardado(solo durante la sesión, se borra al cerrar)");
        System.out.println("4. Salir");
    }
    
    public static void loginEnElServer(){

        try{
            Socket socket = new Socket("localhost", 8888);
            Scanner lector = new Scanner(System.in);     
            
            // Se prepara un flujo de salida y entrada para objetos

            ObjectOutputStream outObjeto = new ObjectOutputStream( socket.getOutputStream());
            ObjectInputStream inObjeto = new ObjectInputStream(socket.getInputStream());
            String palabra= "";
            //primero recibimos el objeto lconsulta del server con un mensaje de incicio
            //inObjeto = new ObjectInputStream(socket.getInputStream());
            
            lconsulta = (LoginConsulta) inObjeto.readObject();//recibimos mensaje 1 del server////////////////////////////////////////////////////////
            
            String codigo = lconsulta.getCodigo();
            
          
            //System.out.println("El objeto lconsulta se ha recibido");


            System.out.println(lconsulta.getInfoDelServer());
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
                outObjeto.writeObject(lconsulta);//envio mensaje 1 al server////////////////////////////////////////////////////////
                //ahora esperamos el codigo que nos debe de enviar si las credenciales son correctas.
                //Para hacer esto  lo que hará será coger nuestro objeto y añadirle el codigo.
                lconsulta =(LoginConsulta) inObjeto.readObject();//recibo mensaje 2 del server////////////////////////////////////////////////////////
                //ahora vamos a leer  y guardar el codigo, si no nos envía ningun error, ya que si ya estamos registrados nos enviará un error
                if(lconsulta.getError().equalsIgnoreCase("0")){
                    codigo = lconsulta.getCodigo(); 
                    codigoGlobal = codigo;
                    //ahora cerramos la conexion. La siguiente vez que conectemos ya enviaremos el codigo, no hará falta enviar usuario y contraseña.  
                    System.out.println("El codigo adjudicados es: "+codigo);
                    System.out.println("Mensaje del server: \n"+lconsulta.getInfoDelServer());
                }else{
                    System.out.println("A ocurrido un error, el codigo del error es: "+lconsulta.getError());
                    System.out.println(lconsulta.getInfoDelServer());
                }
                              


            }else{
                System.out.println("El codigo adjudicados es: "+codigo + "Así que tenemos que enviarlo en el objeto");

            }
            socket.close();

        }catch(IOException e){
            System.out.println("e");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MainClient.class.getName()).log(Level.SEVERE, null, ex);
        }  

    }
    
    
    public static void consultas(){
        try{
            Socket socket = new Socket("localhost", 8888);
            Scanner lector = new Scanner(System.in);     
            
            // Se prepara un flujo de salida y entrada para objetos

            ObjectOutputStream outObjeto = new ObjectOutputStream( socket.getOutputStream());
            ObjectInputStream inObjeto = new ObjectInputStream(socket.getInputStream());
            String palabra= "";
            //primero recibimos el objeto lconsulta del server con un mensaje de incicio
            //inObjeto = new ObjectInputStream(socket.getInputStream());
            
            lconsulta = (LoginConsulta) inObjeto.readObject();//recibo mensaje 1 del server////////////////////////////////////////////////////////
           
            System.out.println("El objeto lconsulta se ha recibido, ahora deberas introducir los datos, el codigo que tenemos es : "+codigoGlobal);
            
            if(codigoGlobal.equalsIgnoreCase("0")){
                System.out.println("Introduce el codigo que tengas:");
                codigoGlobal = lector.next();
                lector.nextLine();
                
            }

            System.out.println(lconsulta.getInfoDelServer());
            lconsulta.setCodigo(codigoGlobal);
            System.out.println("Introduce nombre de tabla:");
            palabra = lector.next();
            lector.nextLine();  
            lconsulta.setTabla(palabra);
            System.out.println("Introduce nombre de columna: (0 para ninguna)");
            palabra = lector.next();               
            lector.nextLine(); 
            lconsulta.setColumna(palabra);
            if(!palabra.equalsIgnoreCase("0")){
                System.out.println("Introduce el filtro: (la palabra a filtrar)");
                palabra = lector.next();
                lector.nextLine();
                lconsulta.setPalabra(palabra);
            }
            System.out.println("Introduce el orden: (ascendente/descendente o 0 si te da igual)");
            palabra = lector.next();
            lector.nextLine();
            lconsulta.setOrden(palabra);

            //ahora acabamos el objeto lconsulta y lo enviamos al server para que nos envie los datos de la consulta
            lconsulta.setTipoDeLogin("2");//como ya tenemos un codigo enviamos el numero 2
            outObjeto.writeObject(lconsulta);//envio mensaje 1 al server////////////////////////////////////////////////////////
            //ahora esperamos que nos envie el otro tipo de objeto el de las consultas
            if(lconsulta.getTabla().equals("empleados")){

                List<Empleados> listaPersonas = new ArrayList<>();
                ObjectInputStream perEnt = new ObjectInputStream(socket.getInputStream());
                listaPersonas = (ArrayList) perEnt.readObject();//recibo mensaje 2 del server////////////////////////////////////////////////////////
                //recibo objeto
                for(int i =0; i< listaPersonas.size();i++){
                    System.out.println("Nombre: " + listaPersonas.get(i).getNom() + " Apellidos: " + listaPersonas.get(i).getApellidos() + " DNI: "+listaPersonas.get(i).getDni());                   

                } 

            }else if(lconsulta.getTabla().equals("users")){
                List<Users> listaPersonas = new ArrayList<>();
                ObjectInputStream perEnt = new ObjectInputStream(socket.getInputStream());
                listaPersonas = (ArrayList) perEnt.readObject();//recibo mensaje 2 del server////////////////////////////////////////////////////////
                //recibo objeto
                for(int i =0; i< listaPersonas.size();i++){
                    System.out.println("Nombre: " + listaPersonas.get(i).getLogin() + " Tipo de user: " + listaPersonas.get(i).getNumtipe() + " DNI: "+listaPersonas.get(i).getDni());                   

                }                                 
            }else{
                System.out.println("El nombre de la tabla no es correcto");
            }
            
            lconsulta = (LoginConsulta) inObjeto.readObject();//recibo mensaje 2 del server////////////////////////////////////////////////////////
            socket.close();
            



        }catch(IOException e){
            System.out.println("Ha ocurrido un error seguramente con el código, posiblmente no estará registrado ");                            

            
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(MainClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public static void salir() throws ClassNotFoundException{
        Scanner lector = new Scanner(System.in);
        String palabra = "";
        System.out.println("Escribe 1 para solamente salir de aplicacion, (Guarda el codigo primero en un papel para poder volver a conectar)");
        System.out.println("Escribe 2 para  cerrar la sesion del server (La proxima vez necesitaras volver a introducir usuario y contraseña)");
        System.out.println("Escribe 3 para salir de aplicacion y cerrar la sesion del server (La proxima vez necesitaras volver a introducir usuario y contraseña)");
        System.out.println("Escribe otra cosa para volver al menu");
        boolean salir = false;
        while (!salir){
            if(!palabra.equalsIgnoreCase("1")||!palabra.equalsIgnoreCase("2")||!palabra.equalsIgnoreCase("3") ||!palabra.equalsIgnoreCase("4")){
                salir = true;
            }
            palabra = lector.next();
            lector.nextLine();
        }
        if(palabra.equalsIgnoreCase("1")){//solo salimos
            System.exit(0);
        }else if(palabra.equals("2")){//cerramos solo la sesion en el server
            try {
                Socket socket = new Socket("localhost", 8888);               
                // Se prepara un flujo de salida y entrada para objetos
                ObjectOutputStream outObjeto = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inObjeto = new ObjectInputStream(socket.getInputStream());                
                
                lconsulta = (LoginConsulta) inObjeto.readObject();//recibo mensaje 1 del server////////////////////////////////////////////////////////
                //System.out.println("Muestro algunos datos: \n"
                //        + "codigo: "+lconsulta.getCodigo()+ "/ info: "+lconsulta.getInfoDelServer()+ "/ Tipo de login"+lconsulta.getTipoDeLogin());
                
                
                //System.out.println("Tu codigo actual es: (en salir 2) "+codigoGlobal);
                if(codigoGlobal.equals("0")){
                    System.out.println("Escribe aquí el codigo.. si no es correcto no podrás cerrar sesión: ");
                    codigoGlobal = lector.next();
                    lector.nextLine();                    
                }
                
                //enviamos el tipo de sesion 3 al server que quiere decir que cerramos sesion
                lconsulta.setTipoDeLogin("3");
                lconsulta.setCodigo(codigoGlobal);
                outObjeto.writeObject(lconsulta);//envio mensaje 1 al server////////////////////////////////////////////////////////
                
                //el server nos enviara algo de despedida
                lconsulta = (LoginConsulta) inObjeto.readObject();//recibo mensaje 2 del server////////////////////////////////////////////////////////
                System.out.println((lconsulta.getInfoDelServer()));
                

                socket.close();
                codigoGlobal = "0";
            } catch (IOException ex) {
                Logger.getLogger(MainClient.class.getName()).log(Level.SEVERE, null, ex);
            }            
            
        }else if(palabra.equals("3")){//cerramos la aplicacion y la sesion en el server
            try {
                Socket socket = new Socket("localhost", 8888);               
                // Se prepara un flujo de salida y entrada para objetos
                ObjectOutputStream outObjeto = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream inObjeto = new ObjectInputStream(socket.getInputStream());                
                
                lconsulta = (LoginConsulta) inObjeto.readObject();//recibo mensaje 1 del server////////////////////////////////////////////////////////
                //System.out.println("Muestro algunos datos: \n"
                //        + "codigo: "+lconsulta.getCodigo()+ "/ info: "+lconsulta.getInfoDelServer()+ "/ Tipo de login"+lconsulta.getTipoDeLogin());
                
                
                //System.out.println("Tu codigo actual es: (en salir 2) "+codigoGlobal);
                if(codigoGlobal.equals("0")){
                    System.out.println("Escribe aquí el codigo.. si no es correcto no podrás cerrar sesión: ");
                    codigoGlobal = lector.next();
                    lector.nextLine();                    
                }
                
                //enviamos el tipo de sesion 3 al server que quiere decir que cerramos sesion
                lconsulta.setTipoDeLogin("3");
                lconsulta.setCodigo(codigoGlobal);
                outObjeto.writeObject(lconsulta);//envio mensaje 1 al server////////////////////////////////////////////////////////
                
                //el server nos enviara algo de despedida
                lconsulta = (LoginConsulta) inObjeto.readObject();//recibo mensaje 2 del server////////////////////////////////////////////////////////
                System.out.println((lconsulta.getInfoDelServer()));
                

                socket.close();
                codigoGlobal = "0";
                System.exit(0);
            } catch (IOException ex) {
                Logger.getLogger(MainClient.class.getName()).log(Level.SEVERE, null, ex);
            }             
            
        }
            
        
        
        
        
        
        
    }

}
