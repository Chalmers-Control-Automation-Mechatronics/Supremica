package org.supremica.automata.algorithms.Guard.QMCMinimizer.interfaz;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;

import org.supremica.automata.algorithms.Guard.QMCMinimizer.logica.QMCImplicanteBean;



/**
 * Clase que define la vista de una celda de la cabecera de filas de la tabla de implicantes
 * usando las propiedades de las cabeceras de columnas.
 * @author Pedro Sanz
 **/
public class QMCRendererListaImplicantes
    extends JCheckBox
    implements ListCellRenderer<QMCImplicanteBean>
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructor que define el formato de las celdas
     **/
    public QMCRendererListaImplicantes(final JTable table) {
        final JTableHeader header = table.getTableHeader();
        setOpaque(true);
        setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        setHorizontalAlignment(CENTER);
        setForeground(header.getForeground());
        setBackground(header.getBackground());
        setFont(header.getFont());
    }

    /**
     * Implementación del método getListCellRendererComponent de la interfaz ListCellRenderer
     * @return this el componente que dibuja la celda
     */
    public Component getListCellRendererComponent( final JList<? extends QMCImplicanteBean> list,
          final QMCImplicanteBean value, final int index, final boolean isSelected, final boolean cellHasFocus) {

        //setText((value == null) ? "" : value.toString());
        setEnabled(list.isEnabled());
        setSelected(((QMCImplicanteBean)value).isEsencial());
        setFont(list.getFont());
        setText(String.valueOf(((QMCImplicanteBean)value).getNombre()));


        return this;

    }
}
