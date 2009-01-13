/**
 * 
 */
package org.supremica.automata.algorithms.Guard.QMCMinimizer.interfaz;

import javax.swing.table.DefaultTableModel;

/**
 * Clase que define el modelo de la tabla de verdad de la aplicación
 * @author Pedro Sanz
 * Fecha 15/11/2005
 *
 */
public class QMCModeloTablaVerdad extends DefaultTableModel {
    
   
    
    public QMCModeloTablaVerdad ()
    {
        
    }
    
    public QMCModeloTablaVerdad (String [] cabecera, Object[][] datos)
    {
        super(datos,cabecera);
            
    }
    public Class getColumnClass (int indColumn)
    {        
        return Integer.class;
    }
    
    public boolean isCellEditable(int fila, int columna)
    {
        return false;
    }
    
    
    

}
