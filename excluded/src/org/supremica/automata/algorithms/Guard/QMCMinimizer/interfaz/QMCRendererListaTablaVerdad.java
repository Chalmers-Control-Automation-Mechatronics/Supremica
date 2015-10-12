
package org.supremica.automata.algorithms.Guard.QMCMinimizer.interfaz;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;

import org.supremica.automata.algorithms.Guard.QMCMinimizer.logica.QMCBinarioBean;

    /*
     * Renderer de los objetos cabecera de las filas
     */

/**
 * Clase que define la vista de una celda de la cabecera de filas de la tabla de verdad
 * usando las propiedades de las cabeceras de columnas.
 * @author Pedro Sanz
 **/
    public class QMCRendererListaTablaVerdad
        extends JCheckBox
        implements ListCellRenderer<QMCBinarioBean>
    {
        private static final long serialVersionUID = 1L;

        JTableHeader header;
        /**
         * M�todo constructor del renderer que establece las caracter�sticas de la tabla
         */
        public QMCRendererListaTablaVerdad(final JTable table) {
            header = table.getTableHeader();
            setOpaque(true);
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            setHorizontalAlignment(CENTER);
            setForeground(header.getForeground());
            setBackground(header.getBackground());
            setFont(header.getFont());
        }

        /**
        * Implementaci�n del m�todo getListCellRendererComponent de la interfaz ListCellRenderer
        * @return this el componente que dibuja la celda
        */
        public Component getListCellRendererComponent( final JList<? extends QMCBinarioBean> list,
            final QMCBinarioBean value, final int index, final boolean isSelected, final boolean cellHasFocus) {

            setEnabled(list.isEnabled());
            //setSelected(((QMCBinarioBean)value).isTermino());
            if(((QMCBinarioBean)value).isTermino() || ((QMCBinarioBean)value).isIndiferencia())
            {
            	setSelected(true);
            }
            else
            {
            	setSelected(false);
            }
            setFont(list.getFont());
            setText(String.valueOf(((QMCBinarioBean)value).getValorDec()));


            return this;

        }
}


