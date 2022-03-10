package presentacionsockets;


import java.io.*;

/*
    - CHRISTIAN CASTRO FERREIRO (@author christian.castroferr)
    - DAM-2
    - 2º TRIMESTRE
*/

/*
    Presentación Sockets (PSP) - Grupo N.º 01
*/

/*
    Esta clase define los diferentes tipos de mensajes que se intercambiarán 
    entre los Clientes y el Servidor. 
*/

public class MensajeChat implements Serializable {

        protected static final long serialVersionUID = 1122211100L;
        
        /*
            Los diferentes "tipos", de mensajes en el chat
            ONLINE:  Para recibir la lista de los usuarios conectados...
            MENSAJE: Para enviar un MSJ.
            SALIR:   Para salir del chat.
        */
	
        
        // Códigos de CHAT
	static final int ONLINE = 0, MENSAJE = 1, SALIR = 2;
        
	private final int tipo;
	private final String mensaje;
	
	// Constructor
	MensajeChat(int tipo, String mensaje) 
        {
            this.tipo = tipo;
            this.mensaje = mensaje;
	}
	
	// Getters
	int getType() 
        {
            return tipo;
	}
        
	String getMessage() 
        {
            return mensaje;
	}
}
