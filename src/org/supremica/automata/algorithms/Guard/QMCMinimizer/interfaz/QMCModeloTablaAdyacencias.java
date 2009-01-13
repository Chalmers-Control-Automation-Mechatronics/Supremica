package org.supremica.automata.algorithms.Guard.QMCMinimizer.interfaz;

import javax.swing.table.DefaultTableModel;

/**
 * Clase que define el modelo de datos de la tabla de adyacencias
 * @author Pedro Sanz
 *
 */
public class QMCModeloTablaAdyacencias extends DefaultTableModel
{
 

  
    public QMCModeloTablaAdyacencias(String [] cabecera, Object[][] datos)
    {
        super(datos,cabecera);
            
    }
    
    Class [] tipoColumna = {String.class, String.class, String.class, Boolean.class};
    
    boolean [] columnaEditable = {false,false,false,true};
    
    public Class getColumnClass (int indColumn)
    {
        return tipoColumna[indColumn];
    }
    
    public boolean isCellEditable(int indFila, int indColumna)
    {
        return columnaEditable[indColumna];
    }
    
    
    

}
