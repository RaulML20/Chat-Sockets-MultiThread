package presentacionsockets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/*
    - CHRISTIAN CASTRO FERREIRO (@author christian.castroferr)
    - DAM-2
    - 2º TRIMESTRE
*/

/*
    Presentación Sockets (PSP) - Grupo N.º 01
*/

public class ClienteGrafico extends JFrame implements ActionListener 
{

        /*
            El objetivo de serialVersionUID es darle al programador control
            sobre qué versiones de una clase se consideran incompatibles 
            con respecto a la serialización.
        */
    
    
	private static final long serialVersionUID = 1L;
	
	private final JLabel jlblEtiqueta;
	private final JTextField jtfServidor, jtfPuerto, jtfTexto;
        private final JButton jbtnInicioSesion, jbtnCerrarSesion, jbtnOnline;
	private final JTextArea jtaArea;
        
	// Revisión de conexión (verdadero o falso)
	private boolean conectado;
	// Objeto cliente
	private Cliente cliente;
        
	// El host y puerto por defecto
        private final String hostDefecto;
	private final int puertoDefecto;
	

	// Conexión de constructor donde recibe el parámetro host y puerto
	ClienteGrafico(String servidor, int puerto) {

		super("Cliente Chat");
                hostDefecto = servidor;
		puertoDefecto = puerto;
		
		
		// Creación del 1º Panel
		JPanel panelNorte = new JPanel(new GridLayout(5,1));
		// El nombre del servidor y el puerto
		JPanel servidorPuerto = new JPanel(new GridLayout(1, 5, 1, 5));
		// Establece el valor por defecto para el host y servidor
		jtfServidor = new JTextField(hostDefecto);
		jtfPuerto = new JTextField("" + puertoDefecto);
		jtfPuerto.setHorizontalAlignment(SwingConstants.RIGHT);

		servidorPuerto.add(new JLabel("Dirección SRV: "));
		servidorPuerto.add(jtfServidor);
		servidorPuerto.add(new JLabel("Nº. de PUERTO: "));
		servidorPuerto.add(jtfPuerto);
		servidorPuerto.add(new JLabel(""));
		// Añade el JPanel "Servidor y Puerto" em en 1º Panel (JPanel)
		panelNorte.add(servidorPuerto);

		// Etiqueta Label y Texto
		jlblEtiqueta = new JLabel("Introduzca su nombre (abajo):", 
                                        SwingConstants.CENTER);
		panelNorte.add(jlblEtiqueta);
		jtfTexto = new JTextField("user");
		jtfTexto.setBackground(Color.WHITE);
		panelNorte.add(jtfTexto);
		add(panelNorte, BorderLayout.NORTH);

		// Creación del chat y del 2º Panel (Panel Central)
		jtaArea = new JTextArea("*** Bienvenido a la sala de chat***\n", 
                                                                    80, 80);
		JPanel panelCentral = new JPanel(new GridLayout(1,1));
		panelCentral.add(new JScrollPane(jtaArea));
		jtaArea.setEditable(false);
		add(panelCentral, BorderLayout.CENTER);

		// Creación de los 3 botones
		jbtnInicioSesion = new JButton("Iniciar sesión");
		jbtnInicioSesion.addActionListener(this);
                
		jbtnCerrarSesion = new JButton("Cerrar sesión");
		jbtnCerrarSesion.addActionListener(this);
                
		jbtnCerrarSesion.setEnabled(false);
		jbtnOnline = new JButton("Usuarios online:");
                
		jbtnOnline.addActionListener(this);
		jbtnOnline.setEnabled(false);	

		JPanel panelSur = new JPanel();
		panelSur.add(jbtnInicioSesion);
		panelSur.add(jbtnCerrarSesion);
		panelSur.add(jbtnOnline);
		add(panelSur, BorderLayout.SOUTH);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(600, 600);
		setVisible(true);
                
                // Proporciona enfoque al texto seleccionado (similar en CSS)
		jtfTexto.requestFocus();

	}

	// Llamada de la clase "Cliente" para introducir texto en el TextArea
	void append(String string) 
        {
		jtaArea.append(string);
		jtaArea.setCaretPosition(jtaArea.getText().length() - 1);
	}
        
        /* 
            En caso de conexión fallida
            Reseteo de componentes
        */
       
	void connectionFailed() {
		jbtnInicioSesion.setEnabled(true);
		jbtnCerrarSesion.setEnabled(false);
		jbtnOnline.setEnabled(false);
                
		jlblEtiqueta.setText("Introduzca su nombre (abajo): ");
		jtfTexto.setText("user");
		// Reseteo de Nº. de puerto y servidor
		jtfPuerto.setText("" + puertoDefecto);
		jtfServidor.setText(hostDefecto);
		// Permite al usuario cambiarlo
		jtfServidor.setEditable(false);
		jtfPuerto.setEditable(false);
		/* 
                    No reacciona al salto de línea (CRLF), 
                    tras introducir el nombre
                */
		jtfTexto.removeActionListener(this);
		conectado = false;
	}
		
	/*
	*   En caso de pulsar el botón
	*/
        
        @Override
	public void actionPerformed(ActionEvent evt) {
            Object obj = evt.getSource();
            // En caso de ser el botón "Cerrar sesión"
            if(obj == jbtnCerrarSesion) {
                    cliente.enviarMensaje(new MensajeChat(MensajeChat.SALIR, 
                                                                       ""));
                    return;
            }
            // En caso de ser el botón "Online"
            if(obj == jbtnOnline) {
                    cliente.enviarMensaje(new MensajeChat(MensajeChat.ONLINE,
                                                                       ""));				
                    return;
            }

            // Revisa la conexión
            if(conectado) {
                    // Envía el mensaje
                    cliente.enviarMensaje(new MensajeChat
                                (MensajeChat.MENSAJE, jtfTexto.getText()));				
                    jtfTexto.setText("");
                    return;
            }


            if(obj == jbtnInicioSesion) 
            {
                    // Solicitud de conexión válida (OK)
                    String usuario = jtfTexto.getText().trim();
                    
                    // un usuario vacío (lo ignora)
                    
                    if(usuario.length() == 0)
                            return;
                    
                    // Direccion de servidor inválido (lo ignora)
                    String servidor = jtfServidor.getText().trim();
                    if(servidor.length() == 0)
                            return;
                    
                    // Nº. de puerto vacío o inválido (lo ignora)
                    String numeroPuerto = jtfPuerto.getText().trim();
                    if(numeroPuerto.length() == 0)
                            return;
                    
                    int puerto = 0;
                    try 
                    {
                        puerto = Integer.parseInt(numeroPuerto);
                    }
                    catch(NumberFormatException ex) 
                    {
                        return;   
                    }

                    // Intento crear una nueva instancia de cliente (GUI)
                    cliente = new Cliente(servidor, puerto, usuario, this);
                    
                    // Iniciamos el cliente
                    if(!cliente.start()) 
                            return;
                    
                    jtfTexto.setText("");
                    jlblEtiqueta.setText("Introduce tu mensaje debajo: ");
                    conectado = true;

                    // Desactivamos el botón de "inicio de sesión"
                    jbtnInicioSesion.setEnabled(false);
                    
                    // Activamos los 2 botones ("salir" y "lookup")
                    jbtnCerrarSesion.setEnabled(true);
                    jbtnOnline.setEnabled(true);
                    
                    // Desactivamos "Servidor" y "Puerto" (JTextField)
                    jtfServidor.setEditable(false);
                    jtfPuerto.setEditable(false);
                    
                    /* 
                        Action listener para cuando un usuario introduce un 
                        mensaje
                    */
                    jtfTexto.addActionListener(this);
            }

	}

	// Iniciamos cliente (pasándole los 2 parámetros)
	public static void main(String[] args) {
		new ClienteGrafico("localhost", 5050);
	}

}
