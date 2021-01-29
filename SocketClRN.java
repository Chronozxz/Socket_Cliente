import java.io.*;
import java.net.*;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.zip.Checksum;
import java.util.zip.CRC32;
import java.util.Scanner;

public class SocketClRN {
     
    public static void main(String[] arg) throws IOException, ClassNotFoundException, CloneNotSupportedException {

        String Host = "localhost";
        int Puerto = 6161;//puerto remoto
        String ficheroCopia = "";//Nombre del fichero a solicitar 
        float ErrorMax = Float.parseFloat( arg[1] );//error
        float CronoMax = Float.parseFloat( arg[3] );//tiempo
        System.out.println("PROGRAMA CLIENTE INICIADO....");
        Socket cliente = new Socket(Host, Puerto);//Conecta con el cliente
        
        Scanner scan = new Scanner(System.in); 
        BufferedReader in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
        DataOutputStream out = new DataOutputStream(cliente.getOutputStream());
		
		System.out.println("Nombre del archivo a solicitar");
		ficheroCopia = scan.nextLine(); //Solicita el nombre del archivo
		
        try{//envia el nombre del archivo
            int tam = ficheroCopia.length();
            out.write(tam);
            out.flush();
            
            for(int index = 0; index < tam; index++)
            {//Va inviando el array
                out.write((int) ficheroCopia.charAt(index));
                out.flush();
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }

        if(in.read() == 0)
        {//Verifica si existe el archivo (feedback del server)
            System.out.println("Error al solocitar el archivo, (No existe o hubo un problema al solicitarlo).");
            return;
        }

        try{
            //Flujo de entrada para objetos
            ObjectInputStream perEnt = new ObjectInputStream(cliente.getInputStream());
            // Se abre el fichero donde se hará la copia
            FileOutputStream fileOutput = new FileOutputStream (ficheroCopia);//Crea un achivo
            BufferedOutputStream bufferedOutput = new BufferedOutputStream(fileOutput);//para escribir en el nuevo archivo
            //iniciación  de variables y objetos
            Checksum checksum = new CRC32();//Crear el checksum 32
            Mensaje msj[] = new Mensaje[8];//archivos ordenados
            Mensaje msjAux[] = new Mensaje[8];//recibe los archivos desordenados
            Mensaje msjGuardados[] = new Mensaje[8];//base de los archivos recibidos correctamente
            
            in = new BufferedReader(new InputStreamReader(cliente.getInputStream()));
            out = new DataOutputStream(cliente.getOutputStream());
            int recibiendo = 1;
            byte seq = 0;
            int paqueteCompleto;
            int terminado = 0;
            int aceptaPaquete = -1;
            int conf = -1;
            int msjconfirmados[] = new int[8];
            String data;
            Temporizador temz = new Temporizador(cliente, 0);//crea el temporizador
            System.out.println("-----------Recibiendo-----------");//Recibiendo paquetes
            while(recibiendo == 1)
            {
	        	temz = new Temporizador(cliente, obtenTemporizador(CronoMax)+30);//inicia el temporizador con un tiempo aletorio
	        	aceptaPaquete = -1;
	        	out.write(1);
		        out.flush();
	        	in.read();	
	        	temz.start();//empieza el temporizador
	        	
	        	conf = in.read();//lee si acabí primero el temporizador del servidor
	        	
		        if(conf == 1 && temz.timeout == false)
		        {//comprueba si aún está activo el temporizador del cliente, y si si acepta el paquete
		        	temz.llegoMensaje = true;
		        	aceptaPaquete = 1;
		        	out.write(1);
		        	out.flush();
		        }else{
		        	aceptaPaquete = 0;
		        }
	        	if(in.read() == 1)
	        		in.read();
	        	out.write(aceptaPaquete);
		        out.flush();
	        	//System.out.println("acepta = "+ aceptaPaquete);
	        	if(aceptaPaquete == 1){
			    	System.out.println("-----Nuevo paquete-----");
			        for(int inx = 0; inx < 8; inx ++)
			        {//va leyendo mensaje por mensaje
			            msjAux[inx] = new Mensaje();
			            msjAux[inx] = (Mensaje) perEnt.readObject();//lee el objeto
			            data = String.valueOf( perEnt.readObject() );//lee la data en string
						msjAux[inx].data = 	data.getBytes​("ISO-8859-1");//cambia la data string en bytes
			        }
			        
			        for(int inxOriginal = 0; inxOriginal < 8; inxOriginal++ )//Reordenado de paquetes respecto a su numero de seq
			            for(int inxAux = 0; inxAux < 8; inxAux++)
			                if((msjAux[inxAux].numSeq+ 128)%8 ==  inxOriginal)//(-128 + 128) %8 calcula el index original
			                    msj[inxOriginal] = (Mensaje) msjAux[inxAux].clone();//clona el objeto
			                    
			        paqueteCompleto = 0;
			        for(int index = 0; index < 8; index++)
			        {//para cada paquete
			            switch(msj[index].tipo) 
			            {
			                case 0:
			                	if(msjconfirmados[index] == 0)
			                	{//si no existe el paquete en los mensajes guardador correctos
			                		if(msj[index].length > 0)
			                		{//si hay data
							            checksum.update(msj[index].data, 0, msj[index].length);//calcula el checksum
							            if(((msj[index].CRC32 == 0 ) && (checksum.getValue() != 0) ) || ErrorMax >= obtenConfirmacion())
							            {//genera el error y pone paquete completo 1, solicita reenviar 
							                paqueteCompleto = 1;
							            }else{//si no hay error lo guarda en paquetes correctos
							            	msjGuardados[index]  = (Mensaje) msj[index].clone();
							            	msjconfirmados[index] = 1;
							            }
					                }else{//si no hay datos lo guarda en paquetes correctos (últimos paquetes)
						            	msjGuardados[index]  = (Mensaje) msj[index].clone();
						            	msjconfirmados[index] = 1;
						            }
			                    }
			                break;
			                case 1://renviar paquetes
			                    paqueteCompleto = 1;
			                break;
			                case 2://Recibio todos los paquetes correctamente
			                    terminado = 1;
			                break;
			            }
			        }
			        if(paqueteCompleto == 0)
			        {//escribimos en el fichero, si todos los paquetes guardados estan bien
				        System.out.println("paquete correcto");
			            for(int inx = 0; inx < 8; inx++){
			                if(msjGuardados[inx].length > 0)
			                    bufferedOutput.write(msjGuardados[inx].data,0,msjGuardados[inx].length);//los escribe en otro archivo
		                    msjconfirmados[inx] = 0;
		                    msjGuardados[inx] = new Mensaje();//lo elimina de los paquetes guardados correctos
			            }
			        }else{//almenos hay un paquete con error y solicita reenviar
			        	System.out.println("paquete con errores");
			        }
			        
	            }else{//si no alcanzó a llegar los paquetes
	            	System.out.println("-----No llegó el paquete-----");
	                paqueteCompleto = 1;
	            }
            
	            if(terminado == 1 && paqueteCompleto == 0)
	            {//si ya se recibieron todos los paquetes y están correctos termina todo
	                out.write(2);
	                out.flush();  
	                recibiendo = 0;
	            }else{//solicita nuevo paquete o reenviar paquete
	                out.write(paqueteCompleto);
	                out.flush();        
	            }
            }
            
            System.out.println("Terminado");
            // CERRAR STREAMS Y SOCKETS, Ficheros
            bufferedOutput.close();
            perEnt.close();
            cliente.close();//cierra  la conexión
        }catch (EOFException e)
        { 
            System.out.println("Error: "+ e.getMessage()); 
        }catch (NullPointerException e) {
            System.out.println("Error: "+ e.getMessage());
        }
        in.close();
        out.close();
    }// Fin de main
    
    private static float obtenConfirmacion()
    {//ontiene un error de 0 a 100
        return ((float)Math.random()*10000/(float)100);
    }

    private static long obtenTemporizador(float temMax)
    {//obtiene un tiempo como maximo temMax
        return  (long)((float)(Math.random()* (int)(temMax*100.0f))/100.0f); 
    }
}
