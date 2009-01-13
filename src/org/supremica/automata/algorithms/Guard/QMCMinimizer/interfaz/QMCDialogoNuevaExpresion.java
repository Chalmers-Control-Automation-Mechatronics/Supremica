/**
 *
 */

package org.supremica.automata.algorithms.Guard.QMCMinimizer.interfaz;



import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.supremica.automata.algorithms.Guard.QMCMinimizer.logica.QMCFuncion;
import org.supremica.automata.algorithms.Guard.QMCMinimizer.util.QMCUtilLogica;

/**
 * Clase diologo para la creacion de una nueva tabla de verdad mediante la funcion de conmutacion
 * @author Pedro Sanz
 * Creado 01/12/2005
 *
 */
public class QMCDialogoNuevaExpresion extends JDialog {

	private JPanel jContentPane = null;
	private JLabel etiquetaVariables1 = null;
	private JTextField cajaVariables = null;
	private JLabel etiquetaVariables2 = null;
	private JTextField cajaTerminos = null;
	private JPanel panelTipoFuncion = null;
	private ButtonGroup formaFuncion = null;
	private JRadioButton seleccionSop = null;
	private JRadioButton seleccionPos = null;
	private JButton botonAceptar = null;
	private JButton botonCancelar = null;

	private QMCFuncion funcion = null;


    private String formaSOP = " )= "+"\u03A3"+" m(";
    private String formaPOS = " )= "+"\u03C0"+" M(";
    private String indiferencias = "";
    private String terminos = "";
    private String variables = "";



	private JCheckBox checkIndiferencias = null;
	private JLabel etiquetaIndiferencias1 = null;
	private JTextField cajaIndiferencias = null;
	private JLabel etiquetaFinal = null;
	private JLabel etiquetaIndiferencias = null;

	/**
	 * Constructor por defecto
	 */
	public QMCDialogoNuevaExpresion() {
		super();
		initialize();
	}

	/**
     * Motodo que inicializa los componentes del dialogo
     *
     */
	private void initialize() {
		this.setSize(467, 258);
		this.setResizable(false);
		this.setModal(true);
		this.setTitle("Nueva Expresion");
		this.setContentPane(getJContentPane());
		this.setLocationRelativeTo(null);
	}

	/**
	 * Motodo que inicializa el componente jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			etiquetaVariables2 = new JLabel();
			etiquetaVariables2.setPreferredSize(new java.awt.Dimension(14,20));
			etiquetaVariables2.setLocation(new java.awt.Point(126,44));
			etiquetaVariables2.setSize(new java.awt.Dimension(45,20));
			etiquetaVariables2.setText(formaSOP);
			etiquetaVariables1 = new JLabel();
			etiquetaVariables1.setText("Funcion F( ");
			etiquetaVariables1.setSize(new java.awt.Dimension(61,20));
			etiquetaVariables1.setPreferredSize(new java.awt.Dimension(60,20));
			etiquetaVariables1.setLocation(new java.awt.Point(13,44));
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(etiquetaVariables1, null);
			jContentPane.add(getCajaVariables(), null);
			jContentPane.add(etiquetaVariables2, null);
			jContentPane.add(getCajaTerminos(), null);
			jContentPane.add(getPanelTipoFuncion(), null);
			jContentPane.add(getBotonAceptar(), null);
			jContentPane.add(getBotonCancelar(), null);
			jContentPane.add(getCheckIndiferencias(), null);
			jContentPane.add(getEtiquetaIndiferencias1(), null);
			jContentPane.add(getCajaIndiferencias(), null);
			jContentPane.add(getEtiquetaFinal(), null);
			jContentPane.add(getEtiquetaIndiferencias(), null);
		}
		return jContentPane;
	}

	/**
	 * Motodo que inicializa el componente cajaVariables
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getCajaVariables() {
		if (cajaVariables == null) {
			cajaVariables = new JTextField();
			cajaVariables.setLocation(new java.awt.Point(74,44));
			cajaVariables.setToolTipText("Introduzca las variables de entrada de la funcion");
			cajaVariables.setSize(new java.awt.Dimension(53,20));
            cajaVariables.setText("x,y,z,u,v");
            cajaVariables.addKeyListener(new java.awt.event.KeyAdapter() {

                // Validacion por tecla pulsada(escrita)
			    public void keyTyped(java.awt.event.KeyEvent e) {
                    char tecla = e.getKeyChar();
                    if(tecla=='\n')
                    {
                        botonAceptar.doClick();
                    }
                    else if (!Character.isLetter(tecla) && tecla!=',' && tecla!='\b')
                    {
                        JOptionPane.showMessageDialog(null,"Solo se aceptan letras y comas","Error",JOptionPane.ERROR_MESSAGE);
                        e.consume();
                    }

			    }
			});
		}
		return cajaVariables;
	}

	/**
	 * Motodo que inicializa el componente cajaFuncion
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getCajaTerminos() {
		if (cajaTerminos == null) {
			cajaTerminos = new JTextField();
			cajaTerminos.setLocation(new java.awt.Point(170,45));
			cajaTerminos.setPreferredSize(new java.awt.Dimension(231,20));
			cajaTerminos.setToolTipText("Introduzca los torminos de la funcion");
			cajaTerminos.setSize(new java.awt.Dimension(280,20));
            cajaTerminos.addKeyListener(new java.awt.event.KeyAdapter() {
                // Validacion por tecla pulsada(escrita)
                public void keyTyped(java.awt.event.KeyEvent e) {
                    char tecla = e.getKeyChar();
                    if(tecla=='\n')
                    {
                        botonAceptar.doClick();
                    }
                    else if (!Character.isDigit(tecla) && tecla!=',' && tecla!='\b')
                    {
                        JOptionPane.showMessageDialog(null,"Solo se aceptan dogitos y comas","Error",JOptionPane.ERROR_MESSAGE);
                        e.consume();
                    }


                }
			});
		}
		return cajaTerminos;
	}

	/**
	 * Motodo que inicializa el componente panelTipoFuncion
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getPanelTipoFuncion() {
		if (panelTipoFuncion == null) {
			panelTipoFuncion = new JPanel();
			panelTipoFuncion.setBounds(new java.awt.Rectangle(76,103,288,58));
			panelTipoFuncion.setToolTipText("Seleccione el tipo de forma canonica de la funcion");
			panelTipoFuncion.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Forma Canonica", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
			formaFuncion = new ButtonGroup();
			formaFuncion.add(getSeleccionSop());
			formaFuncion.add(getSeleccionPos());
			panelTipoFuncion.add(getSeleccionSop(), null);
			panelTipoFuncion.add(getSeleccionPos(), null);


			}
		return panelTipoFuncion;
	}

	/**
	 * Motodo que inicializa el componente botonSop
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getSeleccionSop() {
		if (seleccionSop == null) {
			seleccionSop = new JRadioButton();
			seleccionSop.setText("Forma SOP");
			seleccionSop.setSelected(true);
			seleccionSop.setMnemonic(java.awt.event.KeyEvent.VK_S);
			seleccionSop.addActionListener(new java.awt.event.ActionListener() {
			    public void actionPerformed(java.awt.event.ActionEvent e) {
                    etiquetaVariables2.setText(formaSOP);
                    getEtiquetaIndiferencias1().setText(") + d(");

			    }
			});
		}
		return seleccionSop;
	}

	/**
	 * Motodo que inicializa el componente seleccionPos
	 *
	 * @return javax.swing.JRadioButton
	 */
	private JRadioButton getSeleccionPos() {
		if (seleccionPos == null) {
			seleccionPos = new JRadioButton();
			seleccionPos.setText("Forma POS");
			seleccionPos.setMnemonic(java.awt.event.KeyEvent.VK_P);
			seleccionPos.addActionListener(new java.awt.event.ActionListener() {
			    public void actionPerformed(java.awt.event.ActionEvent e) {
                    etiquetaVariables2.setText(formaPOS);
                    getEtiquetaIndiferencias1().setText(") o d(");
                }
			});
		}
		return seleccionPos;
	}

	/**
	 * Motodo que inicializa el componente botonAceptar
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getBotonAceptar() {
		if (botonAceptar == null) {
			botonAceptar = new JButton();
			botonAceptar.setMnemonic(java.awt.event.KeyEvent.VK_A);
			botonAceptar.setSize(new java.awt.Dimension(86,26));
			botonAceptar.setLocation(new java.awt.Point(122,179));
			botonAceptar.setPreferredSize(new java.awt.Dimension(86,26));
			botonAceptar.setText("Aceptar");
			botonAceptar.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
                    // VALIDACIONES
                    if (cajaVariables.getText().equals(""))
                    {
                        JOptionPane.showMessageDialog(null,"Introduzca las variables de entrada","Error",JOptionPane.ERROR_MESSAGE);
                        cajaVariables.requestFocus();
                    }
                    else if(cajaTerminos.getText().equals(""))
                    {
                        JOptionPane.showMessageDialog(null,"Introduzca la expresion de la funcion","Error",JOptionPane.ERROR_MESSAGE);
                        cajaTerminos.requestFocus();
                    }
                    else if(checkIndiferencias.isSelected() && cajaIndiferencias.getText().equals(""))
                    {
                        JOptionPane.showMessageDialog(null,"Ha seleccionado funcion con indiferencias, introduzca indiferencias","Error",JOptionPane.ERROR_MESSAGE);
                        cajaTerminos.requestFocus();
                    }
                    else if(QMCUtilLogica.variablesRepetidas(cajaVariables.getText()))
                    {
                        JOptionPane.showMessageDialog(null,"Existen variables repetidas","Error",JOptionPane.ERROR_MESSAGE);
                        cajaVariables.requestFocus();
                    }
                    else if(!QMCUtilLogica.variablesSuficientes(cajaVariables.getText(),cajaTerminos.getText()))
                    {
                        JOptionPane.showMessageDialog(null,"No hay suficientes variables para cubrir los terminos introducidos","Error",JOptionPane.ERROR_MESSAGE);
                        cajaVariables.requestFocus();
                    }
                    // RECOGE DATOS VALIDADOS
                    else
                    {
                        funcion = new QMCFuncion();
                        variables = cajaVariables.getText();
                        terminos = cajaTerminos.getText();

                        if (getCheckIndiferencias().isSelected())
                        {
                            indiferencias = cajaIndiferencias.getText();
                            funcion.setIndiferencias(indiferencias);
                        }
                        if (getSeleccionSop().isSelected())
                        {
                            funcion.setForma('s');
                        }
                        else
                        {
                            funcion.setForma('p');
                        }

                        funcion.setVariables(variables);
                        funcion.setTerminos(terminos);
                        dispose();
                    }

                }


			});
		}
		return botonAceptar;
	}

	/**
	 * Motodo que inicializa el componente jButton
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getBotonCancelar() {
		if (botonCancelar == null) {
			botonCancelar = new JButton();
			botonCancelar.setPreferredSize(new java.awt.Dimension(79,26));
			botonCancelar.setMnemonic(java.awt.event.KeyEvent.VK_C);
			botonCancelar.setLocation(new java.awt.Point(244,179));
			botonCancelar.setSize(new java.awt.Dimension(86,26));
			botonCancelar.setText("Cancelar");
			botonCancelar.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dispose();

					}
			});
		}
		return botonCancelar;

	}

	/**
	 * Motodo que inicializa el componente checkIndiferencias
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getCheckIndiferencias() {
	    if (checkIndiferencias == null) {
	        checkIndiferencias = new JCheckBox();
	        checkIndiferencias.setBounds(new java.awt.Rectangle(185,73,21,21));
	        checkIndiferencias.addActionListener(new java.awt.event.ActionListener() {
	            public void actionPerformed(java.awt.event.ActionEvent e) {
	            if (checkIndiferencias.isSelected())
                {
                    getCajaTerminos().setSize(200,20);
                    getEtiquetaIndiferencias1().setVisible(true);
                    getCajaIndiferencias().setVisible(true);
                    getCajaIndiferencias().requestFocus();
                    if(getSeleccionSop().isSelected())
                    {
                        getEtiquetaIndiferencias1().setText(") + d(");
                    }
                    else
                    {
                        getEtiquetaIndiferencias1().setText(") o d(");
                    }



                }
                else
                {
                    getCajaTerminos().setSize(280,20);
                    getEtiquetaIndiferencias1().setVisible(false);
                    getCajaIndiferencias().setVisible(false);

                }

                }
	        });
	    }
	    return checkIndiferencias;
	}

	/**
	 * Motodo que inicializa el componente etiquetaIndiferencias
	 *
	 * @return javax.swing.JLabel
	 */
	private JLabel getEtiquetaIndiferencias1() {
	    if (etiquetaIndiferencias1 == null) {
	        etiquetaIndiferencias1 = new JLabel();
            etiquetaIndiferencias1.setVisible(false);
	        etiquetaIndiferencias1.setText(") + d(");
	        etiquetaIndiferencias1.setBounds(new java.awt.Rectangle(370,46,31,16));
	    }
	    return etiquetaIndiferencias1;
	}

	/**
	 * Motodo que inicializa el componente cajaIndiferencias
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getCajaIndiferencias() {
	    if (cajaIndiferencias == null) {
	        cajaIndiferencias = new JTextField();
            cajaIndiferencias.setVisible(false);
	        cajaIndiferencias.setBounds(new java.awt.Rectangle(400,46,52,20));
	        cajaIndiferencias.setToolTipText("Introduzca los torminos indiferentes");
	        cajaIndiferencias.addKeyListener(new java.awt.event.KeyAdapter() {
                // Validacion por tecla pulsada(escrita)
                public void keyTyped(java.awt.event.KeyEvent e) {
                    char tecla = e.getKeyChar();
                    if(tecla=='\n')
                    {
                        botonAceptar.doClick();
                    }
                    else if (!Character.isDigit(tecla) && tecla!=',' && tecla!='\b')
                    {
                        JOptionPane.showMessageDialog(null,"Solo se aceptan dogitos y comas","Error",JOptionPane.ERROR_MESSAGE);
                        e.consume();
                    }

                }
	        });
	    }
	    return cajaIndiferencias;
	}

	/**
	 * Motodo que inicializa el componente etiquetaFinal
	 *
	 * @return javax.swing.JLabel
	 */
	private JLabel getEtiquetaFinal() {
	    if (etiquetaFinal == null) {
	        etiquetaFinal = new JLabel();
	        etiquetaFinal.setText(" )");
	        etiquetaFinal.setBounds(new java.awt.Rectangle(449,44,9,20));
	    }
	    return etiquetaFinal;
	}

	/**
	 * Motodo que inicializa el componente jLabel
	 *
	 * @return javax.swing.JLabel
	 */
	private JLabel getEtiquetaIndiferencias() {
	    if (etiquetaIndiferencias == null) {
	        etiquetaIndiferencias = new JLabel();
	        etiquetaIndiferencias.setText(" Indiferencias");
	        etiquetaIndiferencias.setBounds(new java.awt.Rectangle(205,74,85,20));
	    }
	    return etiquetaIndiferencias;
	}

    /**
     * @return Devuelve la funcion introducida
     */
    public QMCFuncion getFuncion()
    {
        return funcion;
    }

}  //  @jve:decl-index=0:visual-constraint="10,10"
