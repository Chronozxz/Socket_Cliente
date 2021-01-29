import java.net.*;
import java.io.*;

public class Temporizador extends Thread{//temporizador del servidor

    long tiempo;
    boolean timeout;//si ya acabó el tiempo
    boolean llegoMensaje;
    DataOutputStream out;
    Socket cliente;

    public Temporizador(Socket cliente, long tiempo )
    {//solicita socket y el tiempo
    	this.cliente = cliente;
		try{
			out = new DataOutputStream(cliente.getOutputStream());
		}catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
        this.tiempo = tiempo;
        timeout=false;
        llegoMensaje = false;
    }

    public void run(){
        try {
    	    sleep(tiempo);//se espera el tiempo establecido
            timeout=true;//se acabo el tiempo
            if( ! llegoMensaje)
            {//verifica si llego mensaje en el cliente
            	out.write(0);
            	out.flush();
            }
        }catch (Exception e){
            System.out.println("Error: " + e.getMessage());
        }
    }
}
