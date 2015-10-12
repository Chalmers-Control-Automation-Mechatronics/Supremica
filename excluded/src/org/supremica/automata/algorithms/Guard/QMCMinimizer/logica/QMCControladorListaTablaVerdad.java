/**
 * Fecha creaci�n 14-jun-2006
 */
package org.supremica.automata.algorithms.Guard.QMCMinimizer.logica;

import org.supremica.automata.algorithms.Guard.QMCMinimizer.interfaz.QMCInicio;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTable;


/**
 * Clase que define el controlador de eventos de la lista de terminos de la tabla de verdad *
 * @author Pedro Sanz
 */
public class QMCControladorListaTablaVerdad extends MouseAdapter {


    int index, fila;
    String valorTerm, tipoTerm;
    ArrayList<?> listaBinarios;
    QMCBinarioBean binarioInteractivo, binarioLogico;
    JList<Object> listaCabecerasTablaVerdad;
    JTable tablaTerminos;
    QMCInicio aplicacion;
    QMCFuncion funcion;


    public QMCControladorListaTablaVerdad(final QMCInicio aplicacion, final QMCFuncion funcion)
    {
        this.aplicacion = aplicacion;
        this.funcion = funcion;
    }

    /**
     * Implementaci�n del m�todo mouseClicked que realiza las comprobaciones pertinentes
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @SuppressWarnings("unchecked")
    public void mouseClicked(final MouseEvent e)
    {
        listaCabecerasTablaVerdad = (JList<Object>)e.getSource();
        index = listaCabecerasTablaVerdad.locationToIndex(e.getPoint());
        binarioInteractivo = (QMCBinarioBean)listaCabecerasTablaVerdad.getModel().getElementAt(index);
        binarioInteractivo.setTermino(!binarioInteractivo.isTermino());
        listaBinarios = funcion.getListaBinarios();
        binarioLogico = (QMCBinarioBean)listaBinarios.get(listaCabecerasTablaVerdad.getSelectedIndex());
        tablaTerminos = aplicacion.getTablaTerminos();
        if(funcion.getForma() == 's')
        {
            tipoTerm = "minterm";
        }
        else
        {
            tipoTerm = "maxterm";
        }

        // Accion de marcado
        if(binarioInteractivo.isTermino())
        {
            // comprueba si es termino
            if(binarioLogico.isTermino() || binarioLogico.isIndiferencia())
            {
               fila = Integer.parseInt(binarioLogico.getPosicion());

               tablaTerminos.setValueAt(binarioLogico.getValorDec(),fila ,0);
               tablaTerminos.setValueAt(binarioLogico.getValorBin(),fila ,1);
               tablaTerminos.setValueAt(new Integer(binarioLogico.getIndice()),fila ,2);
               tablaTerminos.setRowSelectionInterval(fila,fila);
            }
            else
            {
                // muestra dialogo error
                JOptionPane.showMessageDialog(null, "El valor marcado no es "+tipoTerm, "Minimizaci�n interactiva", JOptionPane.ERROR_MESSAGE);
                // desmarca la seleccion
                binarioInteractivo.setTermino(false);
            }
        }
        // Accion de desmarcado
        else
        {
            binarioInteractivo.setTermino(true);
        }
        final Rectangle rect = listaCabecerasTablaVerdad.getCellBounds(index, index);
        listaCabecerasTablaVerdad.repaint(rect);
    }


}
