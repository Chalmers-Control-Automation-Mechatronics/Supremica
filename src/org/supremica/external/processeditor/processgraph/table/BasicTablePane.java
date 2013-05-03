package org.supremica.external.processeditor.processgraph.table;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JList;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingConstants;

//import javax.swing.JList;
import javax.swing.ListCellRenderer;

import javax.swing.UIManager;
import javax.swing.table.JTableHeader;

//import javax.swing.JScrollPane;



public class BasicTablePane
	extends JPanel

{
    private static final long serialVersionUID = 1L;

    protected JButton jbTableHeader = null;
	protected BasicTable table = null;
	protected JScrollPane scrollPane = null;

	private boolean showRowHeader = false;

	public BasicTablePane(){
		super();

		Font tmpFont = null;

		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

		jbTableHeader = new JButton("Table");

		//bold table header font
		tmpFont = jbTableHeader.getFont().deriveFont(Font.BOLD);
		jbTableHeader.setFont(tmpFont);

		//hide button
		jbTableHeader.setContentAreaFilled(false);
		jbTableHeader.setBorderPainted(false);
		jbTableHeader.setFocusable(false);

		table = new BasicTable();
		table.setDefaultRenderer(Object.class, new BasicCellRenderer());

		//bold column header text
		tmpFont = table.getTableHeader().getFont().deriveFont(Font.BOLD);
		table.getTableHeader().setFont(tmpFont);

		//center column header text
		final JLabel renderer = (JLabel)table.getTableHeader().getDefaultRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);

		table.setPreferredScrollableViewportSize(table.getPreferredSize());
        table.setFillsViewportHeight(true);

        scrollPane = new JScrollPane(table);

        add(jbTableHeader);
        add(scrollPane);
	}

	public void setHeader(final String headerText){
		jbTableHeader.setText(headerText);
		setName(headerText);
	}

	//override
	public void addKeyListener(final KeyListener l){
		table.addKeyListener(l);
		super.addKeyListener(l);
	}

	//override
	public void addMouseListener(final MouseListener l){
		table.addMouseListener(l);
		super.addMouseListener(l);
	}

	public boolean isRowHeaderVisible(){
		return showRowHeader;
	}

	public void showRowHeader(final boolean show){

		showRowHeader = show;

		if(!show){
			scrollPane.setRowHeaderView(null);
			return;
		}

		final int rows = table.getModel().getRowCount();
		String headers[];
		JList<String> rowHeader;

		headers = new String[rows];
		for(int i = 0; i < rows; i++ ){
			headers[i] = table.getModel().getRowName(i);
		}

		rowHeader = new JList<String>(headers);
	    rowHeader.setFixedCellWidth(60);
	    rowHeader.setFixedCellHeight(table.getRowHeight());
	    rowHeader.setCellRenderer(new RowHeaderRenderer(table));

	    scrollPane.setRowHeaderView(rowHeader);
	}

	public void addCol(final String colName){
		table.getModel().addCol(colName);
		table.initColumnSizes();
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		showRowHeader(showRowHeader);
	}

	public void addRow(final String rowName){
		table.addRow(rowName);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		showRowHeader(showRowHeader);
	}

	public void addRow(final int rowIndex, final String rowName){
		table.addRow(null, rowIndex, rowName);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		showRowHeader(showRowHeader);
	}

	public void fillColumn(final Object value,final int column, final int startIndex, final int endIndex){
		for(int row = startIndex; row < endIndex; row++){
			table.setValueAt(value, row, column);
		}
	}

	public void removeRow(final int index){
		table.removeRow(index);
		showRowHeader(showRowHeader);
	}

	public BasicTable getTable(){
		return table;
	}

	public int getRowCount(){
		return table.getRowCount();
	}

	public int getColumnCount(){
		return table.getColumnCount();
	}

	public Object getValueAt(final int row, final int column){
		return table.getValueAt(row, column);
	}

	public String getColumnName(final int column){
		return table.getColumnName(column);
	}

	public void setRowName(final int row, final String name){
		table.setRowName(row, name);
	}

	public String getRowName(final int row){
		return table.getModel().getRowName(row);
	}

	public void setRowSelectionIntervall(final int index0, final int index1){
		table.setRowSelectionInterval(index0, index1);
	}

	public int[] getSelectedRows(){
		return table.getSelectedRows();
	}


	public void addtableListener(final TableListener l){
		table.addTableListener(l);
	}

	public void removeTableListener(){
		table.removeTableListener();
	}
}

class RowHeaderRenderer
    extends JLabel
	implements ListCellRenderer<String>
{
    private static final long serialVersionUID = 1L;

    //constructor
	  public RowHeaderRenderer(final JTable table) {
	    final JTableHeader header = table.getTableHeader();
	    setOpaque(true);
	    setBorder(UIManager.getBorder("TableHeader.cellBorder"));
	    setHorizontalAlignment(CENTER);
	    setForeground(header.getForeground());
	    setBackground(header.getBackground());
	    setFont(header.getFont().deriveFont(Font.PLAIN));
	  }

	  public Component getListCellRendererComponent( final JList<? extends String> list,
	         final String value, final int index, final boolean isSelected, final boolean cellHasFocus) {
	    setText((value == null) ? "" : value.toString());
	    return this;
	  }
}

