/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilidades;

/**
 *
 * @author david
 */
public  class Utilidades {
    
    public  static String crearCodigoLogin(int tipouser){
        String codigo = "-1";
        
        int numeroAleatorio = (int) (Math.floor(Math.random()*(1-99999+1)+99999));
        if(tipouser == 0){
            codigo = "A"+String.valueOf(numeroAleatorio);
        }else if(tipouser == 1){
            codigo = "U"+String.valueOf(numeroAleatorio);
        }           
        return codigo;
        
    }
    
    
}
