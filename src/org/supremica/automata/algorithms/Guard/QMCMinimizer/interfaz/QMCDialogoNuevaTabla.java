/**
 * Paquete que contiene las clases que construyen la interfaz de usuario
 */
package org.supremica.automata.algorithms.Guard.QMCMinimizer.interfaz;



import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.supremica.automata.algorithms.Guard.QMCMinimizer.logica.QMCFuncion;

/**
 * Clase diálogo para la creación de una nueva tabla de verdad
 * @author Pedro Sanz
 * Creado 05/11/2005
 */
public class QMCDialogoNuevaTabla extends JDialog
{
    private static final long serialVersionUID = 1L;

	private JPanel jContentPane = null;
	private JButton botonAceptar = null;
	private JButton botonCancelar = null;
	private JTextField cajaNumVariables = null;
	private JLabel etiquetaNumVariables = null;

    private QMCFuncion funcion = null;
    
    
	/**
	 * Constructor del diálogo
	 */
	public QMCDialogoNuevaTabla() {
		super();
		initialize();
	}

	/**
     * Método que inicializa los componentes del dialogo
     * 
     */
	private void initialize() {
		
		this.setTitle("Nueva tabla de verdad");
		this.setBounds(new java.awt.Rectangle(307,152));
		this.setResizable(false);
		this.setModal(true);
		this.setContentPane(getJContentPane());
		this.setLocationRelativeTo(null);
	}

	/**
	 * Método que inicializa el componente jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			etiquetaNumVariables = new JLabel();
			etiquetaNumVariables.setBounds(new java.awt.Rectangle(63,32,150,17));
			etiquetaNumVariables.setText("Nº de variables de entrada");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(getBotonAceptar(), null);
			jContentPane.add(getBotonCancelar(), null);
			jContentPane.add(getCajaNumVariables(), null);
			jContentPane.add(etiquetaNumVariables, null);
		}
		return jContentPane;
	}

	/**
	 * Método que inicializa el componente botonAceptar	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBotonAceptar() {
		if (botonAceptar == null) {
			botonAceptar = new JButton();
			botonAceptar.setPreferredSize(new java.awt.Dimension(86,32));
			botonAceptar.setSelected(true);
			botonAceptar.setMnemonic(java.awt.event.KeyEvent.VK_A);
			botonAceptar.setLocation(new java.awt.Point(50,74));
			botonAceptar.setSize(new java.awt.Dimension(86,26));
			botonAceptar.setText("Aceptar");
			botonAceptar.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
                    
                    if (cajaNumVariables.getText().equals(""))
                    {
                        JOptionPane.showMessageDialog(null,"Introduzca un número de variables de entrada","Error",JOptionPane.ERROR_MESSAGE);
                        cajaNumVariables.requestFocus();
                    }
                    else if (cajaNumVariables.getText().equals("0"))
                    {
                        JOptionPane.showMessageDialog(null,"El número de variables de entrada no puede ser cero","Error",JOptionPane.ERROR_MESSAGE);
                        cajaNumVariables.requestFocus();
                    }
                    else
                    {
                        funcion = new QMCFuncion(); 
                        funcion.setVariables(Integer.parseInt(cajaNumVariables.getText()));
                        //funcion.setBinarios("","");
                        funcion.setForma('s');
                        
                        dispose();
                    }
					
				}
			});
		}
		return botonAceptar;
	}

	/**
	 * Método que inicializa el componente jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBotonCancelar() {
		if (botonCancelar == null) {
			botonCancelar = new JButton();
			botonCancelar.setText("Cancelar");
			botonCancelar.setSize(new java.awt.Dimension(86,26));
			botonCancelar.setPreferredSize(new java.awt.Dimension(86,32));
			botonCancelar.setMnemonic(java.awt.event.KeyEvent.VK_C);
			botonCancelar.setLocation(new java.awt.Point(165,74));
			botonCancelar.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dispose();//Cierra el dialogo	
				}
			});
			
		}
		return botonCancelar;
	}

	/**
	 * Método que inicializa el componente cajaNumVariables	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getCajaNumVariables() {
		if (cajaNumVariables == null) {
					
			cajaNumVariables = new JTextField();
			cajaNumVariables.setLocation(new java.awt.Point(218,30));
			cajaNumVariables.setSize(new java.awt.Dimension(19,20));
			cajaNumVariables.addKeyListener(new java.awt.event.KeyAdapter() { 
			
				//Validacion por tecla pulsada(escrita)
				public void keyTyped(java.awt.event.KeyEvent e) {    
					char tecla = e.getKeyChar();
					if(tecla=='\n')
					{
                        botonAceptar.doClick();
					}                  
                    else if(tecla=='\b')
                    {
                        
                    }
					else if (!Character.isDigit(tecla) && tecla!='\b')
					{
						JOptionPane.showMessageDialog(null,"Solo se aceptan digitos","Error",JOptionPane.ERROR_MESSAGE);
                        e.consume();
					}
								
				}
			});
			
		}		 
		return cajaNumVariables;
	
	}
    
     /**
     * @return Devuelve la funcion introducida.
     */
	public QMCFuncion getFuncion()
    {
        return funcion;
    }

	
}  //  @jve:decl-index=0:visual-constraint="10,10"
