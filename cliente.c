// Client side C/C++ program to demonstrate Socket programming 
#include <unistd.h> 
#include <stdio.h> 
#include <stdlib.h>
#include <sys/socket.h> 
#include <arpa/inet.h> 
#include <string.h> 
#define PORT 8080 
   
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
	if(inet_pton(AF_INET, "127.0.0.97", &serv_addr.sin_addr)<=0)  
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

int main(int argc, char const *argv[]) 
{ 
	struct sockaddr_in serv_addr; 
    int sock = 0, valread, leidos; 
    unsigned long long numPaquetes=0; 
    char buffer[1024]={0}; 
    char dir[100];
    char Ctrue[1];
    char buf[1024]={0};
    FILE *archCopy;
    
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

		//mientras numero de datos leidos != 0
    	while((valread = read(sock, &buf, 1024)) != 0)
    	{
			fwrite(&buf, 1, valread, archCopy);
    	}
    	
	    printf("Archivo recibido \n");
    	fclose(archCopy);//cierra archivo
    }

    return 0; 
} 
