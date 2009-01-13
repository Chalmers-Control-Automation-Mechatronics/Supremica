package org.supremica.automata.algorithms.Guard.QMCMinimizer.logica;

import org.supremica.automata.algorithms.Guard.QMCMinimizer.interfaz.QMCInicio;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.supremica.automata.algorithms.Guard.QMCMinimizer.util.QMCUtilLogica;

/**
 *  Clase que define el controlador de eventos de las tablas de adyacencias
 * @author Pedro Sanz
 *
 */
public class QMCControladorTablaAdyacencias extends MouseAdapter {

    JTable tablaAdyacencias;
    QMCInicio aplicacion;
    QMCAlgoritmo algoritmo;
    QMCBinarioBean adyacencia;
    int columna, fila, filaAnt, posicion, contadorClicks;
    boolean check, checkAnt;
    String error;


    public QMCControladorTablaAdyacencias(QMCInicio aplicacion, QMCAlgoritmo algoritmo)
    {
        this.aplicacion = aplicacion;
        this.algoritmo = algoritmo;

    }

    /**
     * Implementación del método mouseClicked que realiza las comprobaciones pertinentes cada par de clicks
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(java.awt.event.MouseEvent e)
    {

        tablaAdyacencias = (JTable)e.getSource();
        fila = tablaAdyacencias.getSelectedRow();
        columna = tablaAdyacencias.getSelectedColumn();

        // Comprueba que la columna marcada pertenece al checkbox
        if(columna == tablaAdyacencias.getColumnCount()-1 && e.getComponent().isEnabled() && e.getButton() == MouseEvent.BUTTON1)
        {
            check = ((Boolean)tablaAdyacencias.getValueAt(fila,columna)).booleanValue();

            // Nueva pareja de adyacencias
            if(contadorClicks!=1)
            {
                filaAnt = fila;
                checkAnt = check;
                contadorClicks = 0;
                // Para evitar que se desmarque si estaba marcado
                tablaAdyacencias.setValueAt(new Boolean(true),fila,columna);
            }
            // Cierra adyacencia
            else
            {
                // Selecciona las filas de las adyacencias elegidas
                tablaAdyacencias.setRowSelectionInterval(filaAnt,fila);
                if(fila-filaAnt > 1)
                {
                    tablaAdyacencias.removeRowSelectionInterval(filaAnt+1,fila-1);
                }

                // Metodo de comprobacion de la posible adyacencia
                error = QMCUtilLogica.compruebaAdyacencia(filaAnt, fila, algoritmo.getListaAdyacenciasAnterior());
                if(error == "Correcta")
                {
                    posicion = QMCUtilLogica.buscaAdyacencia(algoritmo.getListaAdyacenciasActual(),filaAnt+","+fila);
                    // Adyacencia redundante desordenada
                    if(posicion ==-1)
                    {
                    	posicion = QMCUtilLogica.buscaAdyacenciaCorrespondiente(algoritmo.getListaAdyacenciasAnterior(),algoritmo.getListaAdyacenciasActual(),filaAnt,fila);
                    }
                    adyacencia = (QMCBinarioBean)algoritmo.getListaAdyacenciasActual().get(posicion);
                    // Muestra los datos de la nueva adyacencia en la tabla actual
                    aplicacion.getTablaActual().setValueAt(adyacencia.getValorDec()+" ("+adyacencia.getCoordenadasVacuas()+")",posicion,1);
                    aplicacion.getTablaActual().setValueAt(adyacencia.getValorBin(),posicion,2);
                    aplicacion.getTablaActual().setRowSelectionInterval(posicion,posicion);

                    // Marca termino usado de la tabla anterior
                    tablaAdyacencias.setValueAt(new Boolean(true),fila,columna);

                }
                else
                {
                    // Muestra mensaje de adyacencia incorrecta
                    JOptionPane.showMessageDialog(null, "Adyacencia incorrecta : "+error, "Minimización interactiva", JOptionPane.ERROR_MESSAGE);

                    // Corrige click
                    if(check == true)
                    {
                        tablaAdyacencias.setValueAt(new Boolean(false),fila,columna);
                    }
                    else
                    {
                        // Evita que se desmarque si estaba marcado
                        tablaAdyacencias.setValueAt(new Boolean(true),fila,columna);
                    }
                    // Corrige click anterior
                    if(checkAnt == true)
                    {
                        tablaAdyacencias.setValueAt(new Boolean(false),filaAnt,columna);
                    }
                    else
                    {
                        tablaAdyacencias.setValueAt(new Boolean(true),filaAnt,columna);
                    }
                }
            }
            contadorClicks++;
        }
        // Eventos boton derecho
        else if(e.getButton() == MouseEvent.BUTTON3)
        {
            aplicacion.setTablaCopia(tablaAdyacencias);
            // Marca ayuda contextual
/*            if(tablaAdyacencias.getColumnCount()==2)
            {
            	aplicacion.getHelpBroker().setCurrentID("TerminosAgrupados");
            }
            else
            {
            	aplicacion.getHelpBroker().setCurrentID("Adyacencias");
            }*/
            aplicacion.getMenuEmergenteTabla().show(tablaAdyacencias, e.getX(),e.getY());
        }
    }

}
