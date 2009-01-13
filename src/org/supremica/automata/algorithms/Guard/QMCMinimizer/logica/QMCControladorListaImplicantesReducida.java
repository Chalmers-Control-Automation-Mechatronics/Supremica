/**
 * Fecha creación 11-jun-2006
 */
package org.supremica.automata.algorithms.Guard.QMCMinimizer.logica;

import org.supremica.automata.algorithms.Guard.QMCMinimizer.interfaz.QMCInicio;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;

import org.supremica.automata.algorithms.Guard.QMCMinimizer.util.QMCUtilFormateo;

/**
 * Clase que define el controlador de eventos de la lista de implicantes reducida
 * @author Pedro Sanz 
 *
 */
public class QMCControladorListaImplicantesReducida extends MouseAdapter {

    int index;
    QMCImplicanteBean item;
    JList listaImplicantesReducida;
    QMCInicio aplicacion;
    QMCAlgoritmo algoritmo;
    
    public QMCControladorListaImplicantesReducida(QMCInicio aplicacion, QMCAlgoritmo algoritmo)
    {
        this.aplicacion = aplicacion;
        this.algoritmo = algoritmo;
    }
    
    /**
     * Implementación del método mouseClicked que realiza las comprobaciones pertinentes  
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */    
    public void mouseClicked(MouseEvent e)
    {
        listaImplicantesReducida = (JList)e.getSource();
        index = listaImplicantesReducida.locationToIndex(e.getPoint());                                    
        item = (QMCImplicanteBean)listaImplicantesReducida.getModel().getElementAt(index);
        item.setEsencial(!item.isEsencial());
        
        // Accion de marcado
        if(item.isEsencial())
        {
            // Marca los terminos que cubre el implicantes seleccionado
            item.marcaTerminosCubiertos(algoritmo.getListaTerminosInteractivos(), true);                             
            // Refresca la cabecera
            aplicacion.getTablaImplicantesEsenciales().getTableHeader().repaint();
        }
        // Accion de desmarcado
        else
        {
            // Desmarca los terminos marcados por el implicante
            item.marcaTerminosCubiertos(algoritmo.getListaTerminosInteractivos(),false);
            // Renueva las marcas del resto de terminos
            QMCUtilFormateo.renuevaMarcas(algoritmo.getListaImplicantesInteractivos(), algoritmo.getListaTerminosInteractivos());
            aplicacion.getTablaImplicantesEsenciales().getTableHeader().repaint();
        }                          
        Rectangle rect = listaImplicantesReducida.getCellBounds(index, index);
        listaImplicantesReducida.repaint(rect);
        
    }

}
