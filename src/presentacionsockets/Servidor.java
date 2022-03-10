package presentacionsockets;

/*
    - CHRISTIAN CASTRO FERREIRO (@author christian.castroferr)
    - DAM-2
    - 2º TRIMESTRE
*/

/*
    Presentación Sockets (PSP) - Grupo Nº. 01
*/

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 *   Clase Servidor
 */

public class Servidor {
    
	// Uni ID de conexion unico para cada conexión
	private static int idUnico;
        
	// ArrayList para mantener la lista de los clientes
	private ArrayList<hiloCliente> hCliente;

	
	// Para mostrar la fecha
	private SimpleDateFormat fechaFormatoSimple;
	// El Nº. de puerto para escuchar la conexión
	private int puerto;
	// Booleano que estará en falso para detener el SRV.
	private boolean flag;
	

        private ServidorGrafico servidorGrafico;
        

	public Servidor(int puerto)
        {
            this(puerto, null);
	}
	
	public Servidor(int puerto, ServidorGrafico servidorGrafico) 
        {
            // GUI
            this.servidorGrafico = servidorGrafico;
            // Puerto
            this.puerto = puerto;
            // Para mostrar la fecha en formato correcto (HH:mm:ss)
            fechaFormatoSimple = new SimpleDateFormat("HH:mm:ss");
            // ArrayList para la lista de usuarios
            hCliente = new ArrayList<>();
	}
	
	public void start() {
            flag = true;
            // Creación del socket
            
            try 
            {
                // Socket usado por el SRV.
                ServerSocket serverSocket = new ServerSocket(puerto);

                // Loop a la espera de conexiones entrantes.
                while(flag) 
                {
                    // Formatea el mensaje
                    mostrar("SRV escuchando clientes en el puerto " + puerto 
                                                                        + ".");

                    
                    // Conexión aceptada
                    Socket socket = serverSocket.accept();  
                    
                    // DETENER Flag
                    if(!flag)
                            break;
                    
                    // Creación de "Thread"
                    hiloCliente thread = new hiloCliente(socket); 
                    // Añadirlo al ArrayList
                    hCliente.add(thread);									
                    thread.start();
                }
                // Proceso de detención...
                try 
                {
                    serverSocket.close();
                    for(int i = 0; i < hCliente.size(); ++i) 
                    {
                            hiloCliente hiloCliente = hCliente.get(i);
                            try 
                            {
                                hiloCliente.inputStream.close();
                                hiloCliente.outputStream.close();
                                hiloCliente.servidorSocket.close();
                            }
                            catch(IOException ioex) {}
                    }
                }
                catch(IOException ioex) 
                {
                    mostrar("Cerrando SRV y clientes... " + ioex);
                }
            }
            // Error
            catch (IOException e) 
            {
                String mensaje = fechaFormatoSimple.format(new Date()) 
                        + " Excepción en nuevo Socket del SRV: " 
                        + e + "\n";
                    mostrar(mensaje);
            }
	}		
    /*
     * Paramos el servidor
     */
	protected void stop() 
        {
		flag = false;

		try 
                {
                    new Socket("localhost", puerto);
		}
		catch(IOException ioex) {}
	}
	/*
	 * Muestra un evento (no un mensaje) 
        */
	private void mostrar(String msg) {
		String fecha = fechaFormatoSimple.format(new Date()) 
                        + " " 
                        + msg;
		if(servidorGrafico == null)
			System.out.println(fecha);
		else
                    servidorGrafico.adjuntarEventos(fecha + "\n");
	}
	/*
	 *  Enviar un mensaje broadcast (multidifusión) a todos los clientes.
	 */
	private synchronized void mensajeBroadcast(String mensaje) {
		// Añade formato fecha (HH:mm:ss)
                // Añade también salto de linea (CRLF) al mensaje
		String tiempoFecha = fechaFormatoSimple.format(new Date());
		String mensaje2 = tiempoFecha + " " + mensaje + "\n";
                
		// Muestra el mensaje en el SRV.
		if(servidorGrafico == null)
                    System.out.print(mensaje2);
		else
                    servidorGrafico.adjuntarChat(mensaje2);    
		
		// En caso de que un cliente se desconecte...
                
		for(int i = hCliente.size(); --i >= 0;) 
                {
                    hiloCliente threadCliente = hCliente.get(i);
                    // Intenta escribir al cliente 
                    // (en caso de que no se pueda eliminar de la lista)
                    if(!threadCliente.escribirMensaje(mensaje2)) 
                    {
                        hCliente.remove(i);
                        mostrar("Cliente desconectado " + threadCliente.usuario
                                + " eliminado de la lista.");
                    }
		}
	}

	// En caso de que un cliente cierre sesión
	synchronized void eliminar(int idUnico) 
        {
            // Escaneamos el ArrayList, hasta encontrar el ID.
            for(int i = 0; i < hCliente.size(); ++i)
            {
                hiloCliente threadCliente = hCliente.get(i);

                if(threadCliente.idUnico == idUnico) 
                {
                    hCliente.remove(i);
                    return;
                }
            }
	}
	
	public static void main(String[] args) 
        {
            
            String instrucciones = "USO > java Servidor [numeroPuerto]";
            int numeroPuerto = 5050;
            switch(args.length) 
                
            {
                case 1:
                        try 
                        {
                            numeroPuerto = Integer.parseInt(args[0]);
                        }
                        catch(NumberFormatException ex) 
                        {
                            System.out.println("Nº. de puerto inválido.");
                            System.out.println(instrucciones);
                            return;
                        }

                case 0:
                    break;
                default:
                    System.out.println(instrucciones);
                    return;

            }
            // Creamos un objeto servidor.
            Servidor servidor = new Servidor(numeroPuerto);
            servidor.start();
	}

	// Una instancia del hilo, se ejecutará para cada cliente
	class hiloCliente extends Thread 
        {
            // socket
            Socket servidorSocket;
            ObjectInputStream inputStream;
            ObjectOutputStream outputStream;
            
            // ID Unico (para desconexion)
            int idUnico;
            
            String usuario;
            
            MensajeChat mensajeChat;
            
            // Fecha de conexion
            String fechaConexion;

            // Constructor
            hiloCliente(Socket servidorSocket) 
            {
                // ID unico
                idUnico = ++ idUnico;
                this.servidorSocket = servidorSocket;
                // Creando ambos flujos de Datos
                System.out.println("Thread intentando crear flujos de E/S");
                try
                {

                    outputStream = 
                       new ObjectOutputStream(servidorSocket.getOutputStream());
                    inputStream  = 
                         new ObjectInputStream(servidorSocket.getInputStream());
                    
                    // Leemos el usuario
                    usuario = (String) inputStream.readObject();
                    mostrar(usuario + " se acaba de conectar.");
                }
                catch (IOException ex) 
                {
                    mostrar("Excepción creando nuevos flujos de E/S: " + ex);
                    return;
                }

                catch (ClassNotFoundException e) {}
                
                fechaConexion = new Date().toString() + "\n";
            }


                @Override
		public void run() {

                    boolean flag = true;
                    while(flag) 
                    {

                        try 
                        {
                            mensajeChat = (MensajeChat) 
                                                       inputStream.readObject();
                        }
                        catch (IOException e) 
                        {
                                mostrar(usuario + 
                                        " Excepcion leyendo leyendo flujos: " 
                                        + e);
                                break;				
                        }
                        catch(ClassNotFoundException e2) {
                                break;
                        }

                        String mensaje = mensajeChat.getMessage();

  
                        switch(mensajeChat.getType()) 
                        {

                        case MensajeChat.MENSAJE:
                                mensajeBroadcast(usuario 
                                        + ": " 
                                        + mensaje);
                                break;
                        case MensajeChat.SALIR:
                                mostrar(usuario 
                             + " desconectado con el botón 'SALIR'");
                                
                                flag = false;
                                break;
                        case MensajeChat.ONLINE:
                                escribirMensaje
                                            ("Lista de usuarios conectados a: " 
                                            + fechaFormatoSimple.format
                                                           (new Date()) + "\n");
                                // Escaneo de todos los usuarios conectados
                                for(int i = 0; i < hCliente.size(); ++i) 
                                {
                                    hiloCliente hilo = hCliente.get(i);
                                    escribirMensaje((i+1) + ") " 
                                            + hilo.usuario 
                                            + " desde " 
                                            + hilo.fechaConexion);
                                }
                                break;
                        }
                }
                    // Eliminarme a mi mismo, como usuario conectado
                    eliminar(idUnico);
                    cerrar();
		}
		
		// Cerraremos todo
		private void cerrar() {
                    // Intentamos cerrar la conexión
                    try 
                    {
                        if(outputStream != null) 
                            outputStream.close();
                    }
                    catch(IOException iox) {}
                    try 
                    {
                        if(inputStream != null) inputStream.close();
                    }
                    catch(IOException iox) {}
                    try 
                    {
                        if(servidorSocket != null) servidorSocket.close();
                    }
                    catch (IOException iox) {}
		}

		/*
		 * Escritura de Strings..
		 */
		private boolean escribirMensaje(String mensaje) 
                {
                    // Si el cliente sigue conectado, escribir mensaje
                    if(!servidorSocket.isConnected()) {
                            cerrar();
                            return false;
                    }
                    // Escribir mensaje (Stream)
                    try 
                    {
                            outputStream.writeObject(mensaje);
                    }
                    catch(IOException ex) {
                            mostrar("Error enviando mensaje a: " + usuario);
                            mostrar(ex.toString());
                    }
                    return true;
		}
	}
}


