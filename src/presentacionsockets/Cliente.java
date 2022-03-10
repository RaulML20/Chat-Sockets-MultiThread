package presentacionsockets;

import java.net.*;
import java.io.*;
import java.util.*;

/*
    - CHRISTIAN CASTRO FERREIRO (@author christian.castroferr)
    - DAM-2
    - 2º TRIMESTRE
*/

/*
    Presentación Sockets (PSP) - Grupo N.º 01
*/

public class Cliente  {

	// Para E/S
    
        // Para lectura desde el Socket
	private ObjectInputStream objInputStream;	
        // Para escritura en el Socket
	private ObjectOutputStream objOutputStream;	
        
        
	private Socket socketCliente;

	// Si se va a usar de manera gráfica (GUI) o a través de consola (CMD)
	private ClienteGrafico clienteGrafico;
	
	private String servidor, usuario;
	private int puerto;

	Cliente(String servidor, int puerto, String usuario) {
		// Que llama al constructor común con la GUI establecida en null
		this(servidor, puerto, usuario, null);
	}


	Cliente(String servidor, int puerto, String usuario, 
                                                ClienteGrafico clienteGrafico) 
        {
		this.servidor = servidor;
		this.puerto = puerto;
		this.usuario = usuario;
		// Guardaremos el estado si estamos en modo GUI o no
		this.clienteGrafico = clienteGrafico;
	}
	
	/*
	 * Iniciamos la comunicación (HandShake)
	 */
        
	public boolean start() {
		// Intentamos conectarnos al servidor.
		try 
                {
                    socketCliente = new Socket(servidor, puerto);
		} 
	
		catch(IOException iox) 
                {
			display("Error de conexión con el servidor: " + iox);
			return false;
		}
		
		String mensaje = "Conexión aceptada " 
                        + socketCliente.getInetAddress() + ":" 
                        + socketCliente.getPort();
		display(mensaje);
	
		// Creamos ambos flujos de datos (Streams)
                
		try
		{
                    objInputStream  = 
                                new ObjectInputStream
                                               (socketCliente.getInputStream());
                    objOutputStream = 
                              new ObjectOutputStream
                                              (socketCliente.getOutputStream());
		}
		catch (IOException eIO) 
                {
                    display("Excepción al crear nuevos flujos de"
                            + " entrada/salida: " + eIO);
                    return false;
		}

		// Creación del "Thread", para escuchar desde el SRV.
		new escuchaSRV().start();
                
                /*
                    Enviamos nuestro nombre de usuario al SRV, 
                    este es el único mensaje que enviaremos como una cadena. 
                    Todos los demás mensajes serán objetos tipo "MensajeChat"
                */

		try
		{
                    objOutputStream.writeObject(usuario);
		}
		catch (IOException eIO) {
			display("Excepción al crear el Login : " + eIO);
			desconexion();
			return false;
		}
		// Informaremos con valor true (éxito) al SRV.
		return true;
	}

	// Método display
	private void display(String mensaje) {
		if(clienteGrafico == null)
			System.out.println(mensaje);      
		else
			clienteGrafico.append(mensaje + "\n");		
	}
	
	/*
	 * Enviar un MSJ al SRV.
	 */
	void enviarMensaje(MensajeChat mensaje) {
		try 
		{
			objOutputStream.writeObject(mensaje);
		}
		catch(IOException iox) {
			display("Excepción escritura en SRV: " + iox);
		}
	}

	// Desconexión
        
	private void desconexion() {
            try 
            { 
                if(objInputStream != null) 
                    objInputStream.close();
            }
            catch(IOException iox) {} 
            try 
            {
                if(objOutputStream != null) 
                    objOutputStream.close();
            }
            catch(IOException iox) {} 
            try
            {
                if(socketCliente != null) 
                    socketCliente.close();
            }
            catch(IOException iox) {} 

            // informar a la interfaz gráfica (GUI)
            if(clienteGrafico != null)
                    clienteGrafico.connectionFailed();
			
	}

	public static void main(String[] args) 
        {
            String instrucciones = 
            "USO: > java Cliente [usuario] [numeroPuerto] [direcciónServidor]";
                    
		// Valores de fábrica (default)
		int numeroPuerto = 5050;
		String direccionServidor = "localhost";
		String usuario = "Usuario";
	
		switch(args.length) 
                {		
			case 3:
				direccionServidor = args[2];
			case 2:
				try 
                                {
                                    numeroPuerto = Integer.parseInt(args[1]);
				}
				catch(NumberFormatException nEx) 
                                {
					System.out.println
                                                    ("Puerto inválido.");
					System.out.println(instrucciones);
					return;
				}


			case 1: 
				usuario = args[0];

			case 0:
				break;
			
			default:
				System.out.println(instrucciones);
			return;
		}
		// Creamos el objeto "Cliente"
		Cliente cliente = 
                        new Cliente(direccionServidor, numeroPuerto, usuario);
                
		// Testeamos la conexión 
		if(!cliente.start())
			return;
		
		// Esperamos por el mensaje del usuario...
		Scanner scan = new Scanner(System.in);
		while(true) {
			System.out.print("--> ");
			// Leemos mensaje del usuario
			String mensaje = scan.nextLine();
                        
			// Cerramos sesión en caso de que el mensaje sea "SALIR"
			if(mensaje.equalsIgnoreCase("SALIR")) {
				cliente.enviarMensaje(new MensajeChat 
                                            (MensajeChat.SALIR, ""));
				break;
			}
                        
			// Indicamos número de usuarios online.
			else if(mensaje.equalsIgnoreCase("ONLINE")) {
				cliente.enviarMensaje
                                    (new MensajeChat(MensajeChat.ONLINE, ""));				
			}
			else {				
				cliente.enviarMensaje
                                    (new MensajeChat(MensajeChat.MENSAJE, 
                                                                      mensaje));
			}
		}
		// Cliente desconectado
		cliente.desconexion();	
	}

	/*
	*  Clase que espera el mensaje del servidor y lo agrega al JTextArea
	*/
	class escuchaSRV extends Thread {
            @Override
            public void run() {
                    while(true) {
                            try {
                                String mensaje = 
                                           (String) objInputStream.readObject();

                                if(clienteGrafico == null) 
                                {
                                        System.out.println(mensaje);
                                        System.out.print("--> ");
                                }
                                else 
                                {
                                        clienteGrafico.append(mensaje);
                                }
                            }
                            catch(IOException iox) 
                            {
                                display("Sesión cerrada para el usuario: " 
                                        + usuario);
                                if(clienteGrafico != null) 
                                        clienteGrafico.connectionFailed();
                                break;
                            }

                            catch(ClassNotFoundException cnte) {}
                    }
            }
	}
}
