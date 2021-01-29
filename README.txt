Programa cliente que hace una simulación de un servidor. 
Con el programa parte 1 servidor se comunicarán los dos y así se pedirá un pdf de servidor que será enviado a cliente con una estructura.


Programa cliente versión java, envía paquetes con el protocolo retroceso N.
***Para compilar 

--Cliente
javac SocketClRN.java

--Cliente
java SocketClRN -e num1 -p num2
num1: número flotante, índice de error, se recomienda uno pequeño, menor a 5
num2: número entero debe ser mayor al del servidor, por que si es menor siempre habrá mensajes perdidos
***Notas: 
1.- Con un archivo de 6.9mb tarda como 10 min.
con:
java SocketClRN -e 5 -p 200
