/**
 * Fecha creación 15-jun-2006
 */
package org.supremica.automata.algorithms.Guard.QMCMinimizer.logica;

import org.supremica.automata.algorithms.Guard.QMCMinimizer.interfaz.QMCInicio;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JTable;


/**
 * Clase que define el controlador de eventos de la tabla de terminos
 * @author Pedro Sanz
 *
 */
public class QMCControladorTablaTerminos extends MouseAdapter {

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    QMCInicio aplicacion;
    QMCAlgoritmo algoritmo;
    QMCFuncion funcion;
    QMCBinarioBean termino, terminoListaOrdenada;
    JTable tablaTerminos;
    @SuppressWarnings("unchecked")
    ArrayList listaTerminos, listaTerminosOrdenados;
    int contador, fila, columna;
    String tipoTermino;
    boolean check;


    public QMCControladorTablaTerminos (final QMCInicio aplicacion, final QMCFuncion funcion, final QMCAlgoritmo algoritmo)
    {
        this.aplicacion = aplicacion;
        this.algoritmo = algoritmo;
        this.funcion = funcion;
        contador = 0;
        if(funcion.getForma() == 's')
        {
            tipoTermino = "minterm";
        }
        else
        {
            tipoTermino = "maxterm";
        }
    }

    /**
     * Implementación del método mouseClicked que realiza las comprobaciones pertinentes
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    public void mouseClicked(final MouseEvent e)
    {
        tablaTerminos = (JTable)e.getSource();
        columna = tablaTerminos.getSelectedColumn();
        fila = tablaTerminos.getSelectedRow();

        // Comprueba que la columna marcada pertenece al checkbox
        if(columna == 3 && tablaTerminos.isEnabled() && e.getButton() == MouseEvent.BUTTON1)
        {
            check = ((Boolean)tablaTerminos.getValueAt(fila,3)).booleanValue();
            listaTerminos = funcion.getListaTerminos();
            listaTerminosOrdenados = algoritmo.getListaAdyacenciasActual();
            termino = (QMCBinarioBean)listaTerminos.get(fila);
            terminoListaOrdenada = (QMCBinarioBean)listaTerminosOrdenados.get(contador);

            // Comprueba si coincide el termino seleccionado con el termino ordenado
            if(termino.equals(terminoListaOrdenada))
            {
                aplicacion.getTablaTerminosAgrupados().setValueAt(new Integer(terminoListaOrdenada.getValorDec()),contador,1);
                aplicacion.getTablaTerminosAgrupados().setRowSelectionInterval(contador,contador);
                contador++;
            }
            // Desmarca
            else
            {
                // Muestra mensaje de orden de elección incorrecto
                JOptionPane.showMessageDialog(null, "El "+tipoTermino+" elegido no sigue el orden de indices", "Minimización interactiva", JOptionPane.ERROR_MESSAGE);
                // Corrige click
                if(check == true)
                {
                    tablaTerminos.setValueAt(new Boolean(false),fila,3);
                }
                else
                {
                    // Evita que se desmarque si estaba marcado
                    tablaTerminos.setValueAt(new Boolean(true),fila,3);
                }
            }
        }
        // Eventos boton derecho
        else if(e.getButton() == MouseEvent.BUTTON3)
        {
            System.out.println("boton derecho");
            aplicacion.setTablaCopia(tablaTerminos);
            // Marca ayuda contextual
//            aplicacion.getHelpBroker().setCurrentID("Terminos");
            aplicacion.getMenuEmergenteTabla().show(tablaTerminos, e.getX(),e.getY());
        }

    }

}
