// Client side C/C++ program to demonstrate Socket programming 
#include <unistd.h> 
#include <stdio.h> 
#include <stdlib.h>
#include <sys/socket.h> 
#include <arpa/inet.h> 
#include <string.h> 
#include <stdint.h>
#include <time.h>
#define PORT 8080 
#define DATA_LEN 1024

typedef struct msg
{
	uint8_t numSeq;
	int CRC8;
	char data[DATA_LEN];
	uint16_t length;
	uint8_t tipo;
}msga;   

int getSock()
{
	int sock = 0;
	if ((sock = socket(AF_INET, SOCK_STREAM, 0)) < 0) 
    { 
        printf("\n Socket creation error \n"); 
        exit(EXIT_FAILURE); 
    } 
}
   
struct sockaddr_in configAddress()
{
	struct sockaddr_in serv_addr; 
	
	serv_addr.sin_family = AF_INET; 
    serv_addr.sin_port = htons(PORT); 
    
    return serv_addr;
}

struct sockaddr_in configIPAddr(struct sockaddr_in serv_addr)
{// Convert IPv4 and IPv6 addresses from text to binary form 
	if(inet_pton(AF_INET, "127.0.0.77", &serv_addr.sin_addr)<=0)  
	{ 
		printf("\nInvalid address/ Address not supported \n"); 
		exit(EXIT_FAILURE); 
	} 
	
	return serv_addr;
}

struct sockaddr_in setConnect(int sock, struct sockaddr_in serv_addr)
{
	if (connect(sock, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) < 0) 
    { 
        printf("\nConnection Failed \n"); 
        exit(EXIT_FAILURE); 
    } 
    
    return serv_addr;
}	

float getConfirmacion()
{
	return ( (rand() % 10000) / 100.0f );//maximo 99.99%
}

float getCronometro()
{
	return ( (rand() % 6000) / 100.0f );//maximo 1 min = 60.00seg
}

float getValor(char *str, char *c, char *cr)
{
	float valor;
	if (strcmp(str, "-e") == 0)
	{
		valor = atof( c );
	}else if(strcmp(str, "-p") == 0)
	{
		valor = atof(cr);
	}else{
		valor = 0.0;
	}
	return valor;
}

int main(int argc, char const *argv[]) 
{ 
	struct sockaddr_in serv_addr; 
    int sock = 0, valread, leidos; 
    unsigned long long numPaquetes=0; 
    char buffer[DATA_LEN]={0}, dir[100], Ctrue[1], buf[DATA_LEN]={0};
    char *conf = strdup(argv[1]), *porConf = strdup(argv[2]);
    char *cron = strdup(argv[3]), *porCron = strdup(argv[4]);
    float conf_max = 0.0, nueva_conf;
    float cron_max = 0.0, nuevo_cron;
    FILE *archCopy;
    
  	conf_max = getValor(conf, porConf, porCron);
	cron_max = getValor(cron, porConf, porCron);
    
    printf("conectando al servidor\n");
    
    //Configurando el soocket y el address
    sock = getSock();
	serv_addr = configAddress();
	serv_addr = configIPAddr(serv_addr);
	serv_addr = setConnect(sock, serv_addr);

    //Abrir el archivo
    printf("Que archivo desea descargar?\n");
    scanf("%s",dir);

	//enviar nombre del archivo
    send(sock, dir, strlen(dir), 0 );
    
    //Leer si existe el archivo
    read( sock, Ctrue, 1);
     
    if(Ctrue[0] == '0'){//Si no existe se sale del archivo
    	printf("No existe el archivo\n");
    	return 0;
    }else{
    	archCopy = fopen (dir, "wb");//abre el archivo

		//generar la semilla de random para confirmaciones
		srand(time(NULL));
	
		struct msg paquete;
		//mientras numero de datos leidos != 0
    	while((read(sock, &paquete, sizeof(paquete))) != 0 )

    	{
    		//nueva_conf = getConfirmacion();
    		//nuevo_cron = getCronometro();
    		//printf("Error %f --",nueva_conf);
			//printf("Temp %f \n",nuevo_cron);
			
			if(nueva_conf <= conf_max){
				//confirmar mensaje
			}
			if(nuevo_cron <= cron_max){
				//temporizador
			}

			fwrite(&paquete.data, 1, paquete.length, archCopy);
    	}
    	
	    printf("Archivo recibido \n");
    	fclose(archCopy);//cierra archivo
    }

    return 0; 
} 
