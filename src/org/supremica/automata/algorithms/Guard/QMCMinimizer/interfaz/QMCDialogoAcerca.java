/**
 * 
 */
package org.supremica.automata.algorithms.Guard.QMCMinimizer.interfaz;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Clase diálogo que muestra datos acerca de la creación y el diseño de la aplicación
 * @author Pedro Sanz
 * Creado 01/11/2005
 *
 */
public class QMCDialogoAcerca extends JDialog
{
    private static final long serialVersionUID = 1L;

    private JPanel jContentPane = null;
	private JButton botonAceptar = null;
	private JLabel titulo = null;
	private JLabel Proyecto = null;
	private JLabel director = null;
	private JLabel realizado = null;
	private JLabel uemc = null;
	private JLabel etiquetaCorreo = null;
	
	/**
	 * Constructor por defecto
	 */
	public QMCDialogoAcerca() {
		super();
		initialize();
	}

	/**
     * Método que inicializa los componentes del dialogo
     * 
     */
	private void initialize() {
		this.setTitle("Acerca de QMCMinimizer");
		this.setResizable(false);
		this.setModal(true);		
		this.setContentPane(getJContentPane());
		this.setSize(326,274);
		this.setLocationRelativeTo(null);
	}

	/**
	 * Método que iniicializa el compononte jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 4;
			gridBagConstraints11.gridy = 5;
			
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 4;
			gridBagConstraints.gridy = 7;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 4;
			gridBagConstraints1.gridy = 0;
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 4;
			gridBagConstraints2.gridy = 1;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 4;
			gridBagConstraints3.gridy = 3;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 4;
			gridBagConstraints4.gridy = 4;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 4;
			gridBagConstraints5.gridy = 2;
			
			uemc = new JLabel();
			uemc.setText("Universidad Europea Miguel de Cervantes");
			uemc.setPreferredSize(new java.awt.Dimension(235,26));
			
			realizado = new JLabel();
			realizado.setText("Realizado por:  Pedro Sanz David");
			realizado.setPreferredSize(new java.awt.Dimension(185,26));
			
			etiquetaCorreo = new JLabel();
			etiquetaCorreo.setText("contacto:  pedro.sanz@ono.com");			
			etiquetaCorreo.setPreferredSize(new java.awt.Dimension(188,36));			
			
			director = new JLabel();
			director.setText("Director: Juan Manuel Pascual Gaspar");
			director.setPreferredSize(new java.awt.Dimension(217,26));
			
			Proyecto = new JLabel();
			Proyecto.setText("Proyecto fin de carrera");
			Proyecto.setPreferredSize(new java.awt.Dimension(131,26));
			
			titulo = new JLabel();
			titulo.setText("-   QMCMinimizer v1.0   -");
			titulo.setPreferredSize(new java.awt.Dimension(140,36));
			
			jContentPane = new JPanel();
			jContentPane.setLayout(new GridBagLayout());
			jContentPane.add(titulo, gridBagConstraints1);
			jContentPane.add(Proyecto, gridBagConstraints2);
			jContentPane.add(director, gridBagConstraints3);
			jContentPane.add(realizado, gridBagConstraints4);
			jContentPane.add(uemc, gridBagConstraints5);
			jContentPane.add(getBotonAceptar(), gridBagConstraints);
			jContentPane.add(etiquetaCorreo, gridBagConstraints11);
		}
		return jContentPane;
	}

	/**
	 * Método que iniicializa el compononte botonAceptar	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBotonAceptar() {
		if (botonAceptar == null) {
			botonAceptar = new JButton();
			botonAceptar.setText("Aceptar");
			botonAceptar.setPreferredSize(new java.awt.Dimension(86,26));
			botonAceptar.setMnemonic(java.awt.event.KeyEvent.VK_A);
			botonAceptar.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dispose();//Cierra el dialogo
				}
			});
		}
		return botonAceptar;
	}

}  //  @jve:decl-index=0:visual-constraint="13,26"
