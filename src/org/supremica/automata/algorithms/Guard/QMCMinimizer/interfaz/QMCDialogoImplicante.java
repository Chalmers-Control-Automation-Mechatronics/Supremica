/**
 * 
 */
package org.supremica.automata.algorithms.Guard.QMCMinimizer.interfaz;

import java.awt.Dimension;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.supremica.automata.algorithms.Guard.QMCMinimizer.logica.QMCImplicanteBean;

/**
 * Clase que define el dialogo de nombrado de implicantes
 * @author Pedro Sanz
 * Fecha creación 12-jun-2006
 *
 */
public class QMCDialogoImplicante extends JDialog
{
    private static final long serialVersionUID = 1L;

    private JPanel jContentPane = null;
	private JButton botonAceptar = null;	
	private JPanel panelImplicantePrimo = null;
	private JLabel etiquetaValorDecimal = null;
	private JLabel etiquetaValorBinario = null;
	private JLabel etiquetaNombre = null;
	private JTextField cajaNombreImplicante = null;
    
    private QMCImplicanteBean implicante = null;
    private char nombreImplicante = '\n';
	private JTextField cajaValorDecimal = null;
	private JTextField cajaValorBinario = null;
    
    /**
     * Constructor por defecto
     */
    public QMCDialogoImplicante(QMCImplicanteBean implicante) {
        super();
        this.implicante = implicante;
        initialize();
    }

    /**
     * Método que inicializa los componentes del dialogo
     * 
     */
    private void initialize() {
        this.setSize(351, 200);
        this.setContentPane(getJContentPane());
        this.setTitle("Minimización Interactiva");
        this.setResizable(false);
        this.setModal(true);
        this.setLocationRelativeTo(null);
    }

    /**
     * This method initializes jContentPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jContentPane = new JPanel();
            jContentPane.setLayout(null);
            jContentPane.add(getBotonAceptar(), null);
            jContentPane.add(getPanelImplicantePrimo(), null);
            getCajaNombreImplicante().requestFocus();
            
        }
        return jContentPane;
    }

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBotonAceptar() {
	    if (botonAceptar == null) {
	        botonAceptar = new JButton();
	        botonAceptar.setBounds(new java.awt.Rectangle(127,123,86,27));
	        botonAceptar.setMnemonic(KeyEvent.VK_A);
	        botonAceptar.setText("Aceptar");
	        botonAceptar.setPreferredSize(new Dimension(86, 26));
	        botonAceptar.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent e) 
                {
                    // VALIDACIONES
                    if (getCajaNombreImplicante().getText().equals(""))
                    {
                        JOptionPane.showMessageDialog(null,"Debe nombrar al implicante con un carácter","Error",JOptionPane.ERROR_MESSAGE);
                        getCajaNombreImplicante().requestFocus();
                    }
                    // RECOGE DATOS VALIDADOS
                    else
                    {
                        nombreImplicante = cajaNombreImplicante.getText().toUpperCase().charAt(0);
                        dispose();
                    }
                    
                }    
	        });
	    }
	    return botonAceptar;
	}

	/**
	 * This method initializes panelImplicantePrimo	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPanelImplicantePrimo() {
	    if (panelImplicantePrimo == null) {
	        etiquetaNombre = new JLabel();
	        etiquetaNombre.setBounds(new java.awt.Rectangle(59,71,49,19));
	        etiquetaNombre.setText("Nombre:");
	        etiquetaValorBinario = new JLabel();
	        etiquetaValorBinario.setBounds(new java.awt.Rectangle(26,49,87,20));
	        etiquetaValorBinario.setText("Valor binario:");
	        etiquetaValorDecimal = new JLabel();
	        etiquetaValorDecimal.setText("Valor decimal:");
	        etiquetaValorDecimal.setBounds(new java.awt.Rectangle(27,25,86,20));
	        panelImplicantePrimo = new JPanel();
	        panelImplicantePrimo.setLayout(null);
	        panelImplicantePrimo.setBounds(new java.awt.Rectangle(16,16,305,99));
	        panelImplicantePrimo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Implicante primo", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
	        panelImplicantePrimo.add(etiquetaValorDecimal, null);
	        panelImplicantePrimo.add(etiquetaValorBinario, null);
	        panelImplicantePrimo.add(etiquetaNombre, null);
	        panelImplicantePrimo.add(getCajaNombreImplicante(), null);
	        panelImplicantePrimo.add(getCajaValorDecimal(), null);
	        panelImplicantePrimo.add(getCajaValorBinario(), null);
	    }
	    return panelImplicantePrimo;
	}

	/**
	 * This method initializes cajaNombreImplicante	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getCajaNombreImplicante() {
	    if (cajaNombreImplicante == null) {
	        cajaNombreImplicante = new JTextField();
	        cajaNombreImplicante.setBounds(new java.awt.Rectangle(112,73,31,18));            
            cajaNombreImplicante.addKeyListener(new java.awt.event.KeyAdapter() {
                // Validacion por tecla pulsada(escrita)
                public void keyTyped(java.awt.event.KeyEvent e) {
                    char tecla = e.getKeyChar();
                    if(tecla=='\n')
                    {
                        botonAceptar.doClick();
                    }
                    else if (!Character.isLetter(tecla) && tecla!=',' && tecla!='\b')
                    {
                        JOptionPane.showMessageDialog(null,"Solo se aceptan carácteres alfabéticos","Error",JOptionPane.ERROR_MESSAGE);
                        e.consume();
                    }
                }
              });
	    }
	    return cajaNombreImplicante;
	}
    
    /**
     * Devuelve el nombre introducido para el implicante
     * @return
     */
    public char getNombreImplicante()
    {
        return nombreImplicante;
    }

	/**
	 * This method initializes cajaValorDecimal	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getCajaValorDecimal() {
	    if (cajaValorDecimal == null) {
	        cajaValorDecimal = new JTextField();
	        cajaValorDecimal.setBounds(new java.awt.Rectangle(113,25,168,20));
            cajaValorDecimal.setText(implicante.getTerminos());
	        cajaValorDecimal.setEditable(false);
	    }
	    return cajaValorDecimal;
	}

	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getCajaValorBinario() {
	    if (cajaValorBinario == null) {
	        cajaValorBinario = new JTextField();
	        cajaValorBinario.setBounds(new java.awt.Rectangle(113,48,168,20));
            cajaValorBinario.setText(implicante.getValorBin());
	        cajaValorBinario.setEditable(false);
	    }
	    return cajaValorBinario;
	}

}  //  @jve:decl-index=0:visual-constraint="73,31"
