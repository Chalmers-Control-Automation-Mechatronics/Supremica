/**
 * 
 */
package org.supremica.automata.algorithms.Guard.QMCMinimizer.logica;

import org.supremica.automata.algorithms.Guard.QMCMinimizer.interfaz.QMCInicio;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JList;
import javax.swing.JOptionPane;

import org.supremica.automata.algorithms.Guard.QMCMinimizer.util.QMCUtilLogica;
import org.supremica.automata.algorithms.Guard.QMCMinimizer.util.QMCUtilFormateo;

/**
 * Clase tipo renderer que visualiza la cabecera de las tabla de implicantes
 * @author Pedro Sanz
 * Fecha creación 11-jun-2006
 */
public class QMCControladorListaImplicantesEsenciales extends MouseAdapter {

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    int index;
    QMCImplicanteBean item;
    JList listaCabecerasFilasImplicantes;
    QMCInicio aplicacion;
    QMCAlgoritmo algoritmo;
    
    public QMCControladorListaImplicantesEsenciales(QMCInicio aplicacion, QMCAlgoritmo algoritmo)
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
        listaCabecerasFilasImplicantes = (JList)e.getSource();
        index = listaCabecerasFilasImplicantes.locationToIndex(e.getPoint());                                    
        item = (QMCImplicanteBean)listaCabecerasFilasImplicantes.getModel().getElementAt(index);
        item.setEsencial(!item.isEsencial());
      
        
        // Accion de marcado
        if(item.isEsencial())
        {
            // comprueba si es esencial
            if(QMCUtilLogica.compruebaImplicanteEsencial(algoritmo.getListaImplicantesPrimos(), index))
            {
               // Marca los terminos que cubre el implicantes seleccionado
               item.marcaTerminosCubiertos(algoritmo.getListaTerminosInteractivos(), true);                             
               // Refresca la cabecera
               aplicacion.getTablaImplicantesEsenciales().getTableHeader().repaint();
            }
            else
            {                         
                // muestra dialogo error
                JOptionPane.showMessageDialog(null, "El implicante primo seleccionado no es esencial", "Implicante esencial incorrecto", JOptionPane.ERROR_MESSAGE);
                // desmarca la seleccion
                item.setEsencial(false);                          
            }                          
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
        Rectangle rect = listaCabecerasFilasImplicantes.getCellBounds(index, index);
        listaCabecerasFilasImplicantes.repaint(rect);
    }    

}
