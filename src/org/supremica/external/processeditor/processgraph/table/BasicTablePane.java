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
		JLabel renderer = (JLabel)table.getTableHeader().getDefaultRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
        table.setFillsViewportHeight(true);

        scrollPane = new JScrollPane(table);
        
        add(jbTableHeader);
        add(scrollPane);
	}
	
	public void setHeader(String headerText){
		jbTableHeader.setText(headerText);
		setName(headerText);
	}
	
	//override
	public void addKeyListener(KeyListener l){
		table.addKeyListener(l);
		super.addKeyListener(l);
	}
	
	//override
	public void addMouseListener(MouseListener l){
		table.addMouseListener(l);
		super.addMouseListener(l);
	}
	
	public boolean isRowHeaderVisible(){
		return showRowHeader;
	}
	
	public void showRowHeader(boolean show){
		
		showRowHeader = show;
		
		if(!show){
			scrollPane.setRowHeaderView(null);
			return;
		}
		
		int rows = table.getModel().getRowCount();
		String headers[];
		JList rowHeader;
		
		headers = new String[rows];
		for(int i = 0; i < rows; i++ ){
			headers[i] = table.getModel().getRowName(i);
		}
		
		rowHeader = new JList(headers);
	    rowHeader.setFixedCellWidth(60);
	    rowHeader.setFixedCellHeight(table.getRowHeight());
	    rowHeader.setCellRenderer(new RowHeaderRenderer(table));
		
	    scrollPane.setRowHeaderView(rowHeader);
	}
	
	public void addCol(String colName){
		table.getModel().addCol(colName);
		table.initColumnSizes();
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		showRowHeader(showRowHeader);
	}
	
	public void addRow(String rowName){
		table.addRow(rowName);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		showRowHeader(showRowHeader);
	}
	
	public void addRow(int rowIndex, String rowName){
		table.addRow(null, rowIndex, rowName);
		table.setPreferredScrollableViewportSize(table.getPreferredSize());
		showRowHeader(showRowHeader);
	}
	
	public void fillColumn(Object value,int column, int startIndex, int endIndex){
		for(int row = startIndex; row < endIndex; row++){
			table.setValueAt(value, row, column);
		}
	}
	
	public void removeRow(int index){
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
	
	public Object getValueAt(int row, int column){
		return table.getValueAt(row, column);
	}
	
	public String getColumnName(int column){
		return table.getColumnName(column);
	}
	
	public void setRowName(int row, String name){
		table.setRowName(row, name);
	}
	
	public String getRowName(int row){
		return table.getModel().getRowName(row);
	}
	
	public void setRowSelectionIntervall(int index0, int index1){
		table.setRowSelectionInterval(index0, index1);
	}
	
	public int[] getSelectedRows(){
		return table.getSelectedRows();
	}
	
	
	public void addtableListener(TableListener l){
		table.addTableListener(l);
	}
	
	public void removeTableListener(){
		table.removeTableListener();
	}	
}

class RowHeaderRenderer
    extends JLabel 
	implements ListCellRenderer
{
    private static final long serialVersionUID = 1L;

    //constructor
	  public RowHeaderRenderer(JTable table) {
	    JTableHeader header = table.getTableHeader();
	    setOpaque(true);
	    setBorder(UIManager.getBorder("TableHeader.cellBorder"));
	    setHorizontalAlignment(CENTER);
	    setForeground(header.getForeground());
	    setBackground(header.getBackground());
	    setFont(header.getFont().deriveFont(Font.PLAIN));
	  }
	  
	  public Component getListCellRendererComponent( JList list, 
	         Object value, int index, boolean isSelected, boolean cellHasFocus) {
	    setText((value == null) ? "" : value.toString());
	    return this;
	  }
}

