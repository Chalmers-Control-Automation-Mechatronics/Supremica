package org.supremica.automata.algorithms.Guard.QMCMinimizer.interfaz;

import javax.swing.table.DefaultTableModel;

/**
 * Clase que define el modelo de datos de la tabla de adyacencias
 * @author Pedro Sanz
 *
 */
public class QMCModeloTablaTerminosAgrupados extends DefaultTableModel
{
    private static final long serialVersionUID = 1L;

    public QMCModeloTablaTerminosAgrupados(final String [] cabecera, final Object[][] datos)
    {
        super(datos,cabecera);

    }

    Class<?> [] tipoColumna = {Integer.class,Integer.class,Boolean.class};

    boolean [] columnaEditable = {false,false,true};

    public Class<?> getColumnClass (final int indColumn)
    {
        return tipoColumna[indColumn];
    }

    public boolean isCellEditable(final int indFila, final int indColumna)
    {
        return columnaEditable[indColumna];
    }

}