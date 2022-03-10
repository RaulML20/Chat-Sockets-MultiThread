package presentacionsockets;

/*
    - CHRISTIAN CASTRO FERREIRO (@author christian.castroferr)
    - DAM-2
    - 2º TRIMESTRE
*/

/*
    Presentación Sockets (PSP) - Grupo N.º 01
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/*
 * Servidor Gráfico
 */

public final class ServidorGrafico extends JFrame 
        implements ActionListener, WindowListener {        

    
    /*
        El objetivo de serialVersionUID es darle al programador control
        sobre qué versiones de una clase se consideran incompatibles 
        con respecto a la serialización.
    */
    
	private static final long serialVersionUID = 1L;
        
	private final JButton jbtnInicioYDetener;
        private final JTextArea jtaChat, jtaEventos;
	private final JTextField jtfPuerto;
        
	private Servidor servidor;
	

	ServidorGrafico(int puerto) 
        {
		super("Servidor de Chat Gráfico");
		servidor = null;

		JPanel panelNorte = new JPanel();
		panelNorte.add(new JLabel("Nº. Puerto: "));
		jtfPuerto = new JTextField(" " + puerto);
		panelNorte.add(jtfPuerto);
                
		// Para iniciar el SRV.
		jbtnInicioYDetener = new JButton("INICIAR");
		jbtnInicioYDetener.addActionListener(this);
		panelNorte.add(jbtnInicioYDetener);
		add(panelNorte, BorderLayout.NORTH);
		
		// Eventos y Sala de chat
		JPanel center = new JPanel(new GridLayout(2,1));
		jtaChat = new JTextArea(80,80);
		jtaChat.setEditable(false);
		adjuntarChat("Sala de chat.\n");
		center.add(new JScrollPane(jtaChat));
                
		jtaEventos = new JTextArea(80,80);
		jtaEventos.setEditable(false);
		adjuntarEventos("Log de eventos.\n");
		center.add(new JScrollPane(jtaEventos));	
		add(center);
		
		// Para que sea visible el Frame y el WindowListener
		addWindowListener(this);
		setSize(400, 600);
		setVisible(true);
	}		

	// Añadimos los mensajes en el JTextArea
        
	void adjuntarChat(String str) 
        {
		jtaChat.append(str);
		jtaChat.setCaretPosition(jtaChat.getText().length() - 1);
	}
        
	void adjuntarEventos(String str) 
        {
		jtaEventos.append(str);
		jtaEventos.setCaretPosition(jtaChat.getText().length() - 1);
	}
	
	// Iniciamos o detenemos el servidor mediante Button..
        @Override
	public void actionPerformed(ActionEvent ae) {
            // En caso de ejecución, podremos detener el SRV.
            if(servidor != null) {
                    servidor.stop();
                    servidor = null;
                    jtfPuerto.setEditable(true);
                    jbtnInicioYDetener.setText("INICIAR");
                    return;
            }
            // INICIO SRV.	
            int puerto;
            try 
            {
                puerto = Integer.parseInt(jtfPuerto.getText().trim());
            }
            catch(NumberFormatException ex) 
            {
                adjuntarEventos("Puerto inválido.");
                return;
            }
            // Crea un nuevo SRV
            servidor = new Servidor(puerto, this);
            // Ejecuta como "Thread"
            new ServerRunning().start();
            jbtnInicioYDetener.setText("DETENER");
            jtfPuerto.setEditable(false);
	}
	
	// Punto de entrada de inicio del SRV.
	public static void main(String[] arg) 
        {

		new ServidorGrafico(5050);
	}

	/*
	 *      Si el usuario hace click en el botón cerrar "X"
	 *      Necesitamos liberar la conexión con el servidor,
         *      para liberar el puerto
        */
        
        @Override
	public void windowClosing(WindowEvent we) {
            // En caso de que el SRV exista
            if(servidor != null) 
            {
                    try 
                    {
                            servidor.stop();		
                    }
                    catch(Exception ex) {}
                    servidor = null;
            }
            // Hacemos un "dispose" al JFrame
            dispose();
            System.exit(0);
	}
	// Métodos WindowsListener
        @Override
	public void windowClosed(WindowEvent e) {}
        @Override
	public void windowOpened(WindowEvent e) {}
        @Override
	public void windowIconified(WindowEvent e) {}
        @Override
	public void windowDeiconified(WindowEvent e) {}
        @Override
	public void windowActivated(WindowEvent e) {}
        @Override
	public void windowDeactivated(WindowEvent e) {}

	// Iniciamos el "Thread" del SRV.
        
	class ServerRunning extends Thread 
        {
            @Override
            public void run() 
            {
                servidor.start();     
                // En caso de que el servidor falle...
                jbtnInicioYDetener.setText("INICIAR");
                jtfPuerto.setEditable(true);
                adjuntarEventos("Servidor colapsado\n");
                servidor = null;
            }
	}

}
