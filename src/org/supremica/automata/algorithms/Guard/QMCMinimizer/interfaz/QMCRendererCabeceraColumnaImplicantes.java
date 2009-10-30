package org.supremica.automata.algorithms.Guard.QMCMinimizer.interfaz;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;


import org.supremica.automata.algorithms.Guard.QMCMinimizer.logica.QMCBinarioBean;

/**
 * Clase que define el componente renderer para la cabecera de columnas de las tablas de implicantes
 * @author Pedro Sanz
 */



public class QMCRendererCabeceraColumnaImplicantes extends JCheckBox
    implements TableCellRenderer {

  private static final long serialVersionUID = 1L;


  public QMCRendererCabeceraColumnaImplicantes() 
  {
      
  }
 
 
  /**
   * Implementación del método getTableCellRendererComponent de la interfaz TableCellRenderer
   * @return this el componente que dibuja la celda
   */
  public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
  {
    if (table != null)
    {
      JTableHeader header = table.getTableHeader();
      if (header != null)
      {    	   
        setForeground(header.getForeground());
        setBackground(header.getBackground());
        setFont(header.getFont()); 
      }
    }    
    setSelected(((QMCBinarioBean)value).isCubierta());
    //setText((value == null) ? "" : value.toString());
    setText(((QMCBinarioBean)value).getValorDec());
    setBorder(UIManager.getBorder("TableHeader.cellBorder"));
    
    return this;
  }

 
}

