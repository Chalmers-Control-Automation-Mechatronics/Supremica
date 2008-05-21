package org.supremica.external.processeditor.tools.specificationsynthes;
 
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;

import java.util.List;
import java.util.LinkedList;
import java.io.File;

import org.supremica.external.processeditor.xml.Loader;

import org.supremica.manufacturingTables.xsd.processeditor.ROP;
import org.supremica.manufacturingTables.xsd.eop.EOP;
import org.supremica.manufacturingTables.xsd.il.IL;

public class SpecificationTable extends JTable {
    
    TableModel model = null;
    
    protected String[] columnToolTips = {"Machine name",
    									 "Type",
                                         "comments about the machine",
                                         "If checked, included in convert"};

    public SpecificationTable() {
        super(new BasicTableModel());
        setPreferredScrollableViewportSize(new Dimension(50, 100));
        setFillsViewportHeight(true);
    }
    
    public BasicTableModel getModel(){
    	return (BasicTableModel) super.getModel();
    }
    
    public void addFile( File file ){
    	
    	String name = "";
    	String comment = "";
    	String type = "";
    	
    	String filePath = file.getAbsolutePath();
    	Object o = getObjectFromFile( file );
    	Object[] os = null;
    	
    	//Sanity check
    	if( null == o ){
    		return;
    	}
    	
    	if( o instanceof ROP ){
    		
    		name = ((ROP)o).getMachine();
        	comment = ((ROP)o).getComment();
        	type = ((ROP)o).getType().value();
        	
    	}else if( o instanceof EOP ){
    		
    		name = ((EOP)o).getId();
        	comment = ((EOP)o).getComment();
        	type = "EOP";
        	
    	}else if( o instanceof IL ){
    		
    		name = ((IL)o).getId();
        	comment = ((IL)o).getComment();
        	type = "IL";
        	
    	}else{
    		//debug
    		//System.out.println("Unkonwn " + o.toString());
    		//debug
    		
    		return; //Add nothing
    	}
    	
    	os = new Object[]{name,
				  		  type,
				  		  comment,
				  		  new Boolean(true),
				  	      filePath};
    	
    	//add Object to table
		if( !getModel().rowExist( os ) ){
			getModel().insertRow( os );
	    	repaint();
		}
    }
    
    public void refresh(){
    	
    	String name = "";
    	String type = "";
    	String comment = "";
    	
    	File file = null;
    	Object o = null;
    	
    	int row = 0;
    	while(row < getModel().getRowCount()){
    		
    		//create file
    		file = new File((String)getModel().
    							getValueAt(row,getModel().FILE_PATH_COL));
    		
    		//create rop
    		o = getObjectFromFile( file );
    		
    		if(o == null){
    			getModel().deleteRow(row);
    		}else{
    			//test if property have changed
    			name = (String)getModel().
    							getValueAt(row, getModel().NAME_COL);
    			type = (String)getModel().
    							getValueAt(row, getModel().TYPE_COL);
    			comment = (String)getModel().
    							getValueAt(row, getModel().COMMENT_COL);
    			
    			
    			
    			//next row
    			row = row + 1;
    		}
    	}
    }
    
    public List<String> getFilePathList(){
    	List<String> filePathList = new LinkedList<String>();
    	String filePath = "";
    	
    	for(int i = 0; i < getRowCount(); i++){
    		filePath = (String)getModel().getValueAt(i,getModel().FILE_PATH_COL);
    		filePathList.add(filePath);
    	}
    	
    	return filePathList;
    }
    
    public List<String> getMarkedFilePathList(){
    	List<String> filePathList = new LinkedList<String>();
    	String filePath = "";
    	
    	for(int i = 0; i < getRowCount(); i++){
    		if((Boolean)getValueAt(i,getModel().BOOLEAN_COL)){
    			filePath = (String)getModel().getValueAt(i,getModel().FILE_PATH_COL);
    			filePathList.add(filePath);
    		}
    	}
    	
    	return filePathList;
    }
    
    public void removeSelectedRows(){
    	int rows[] = getSelectedRows();
    	for(int i = 0; i < rows.length; i++){
    		getModel().deleteRow(rows[i]-i);
    	}
    }
    
    //Implement table cell tool tips.
    public String getToolTipText(MouseEvent e) {
    	String tip = null;
    	
    	String fileName = null;
    	String type = null;
		String name = null;
		String comment = null;
		
    	java.awt.Point p = e.getPoint();
    	
    	int rowIndex = rowAtPoint(p);
    	int colIndex = columnAtPoint(p);
    	int realColumnIndex = convertColumnIndexToModel(colIndex);
    	
    	TableModel model = getModel();
    	
    	switch(realColumnIndex){
    		case 0://Machine name column
    			fileName = (String)model.getValueAt(rowIndex,getModel().FILE_PATH_COL);
    			name = (String)model.getValueAt(rowIndex,colIndex);
    			
    			if(fileName != null && name != null){
    				if(fileName.length() > 0 && name.length() > 0){
    					tip = name +" in "+fileName;
    				}
    			}
    			break;
    		
    		case 1: //Type column
    			type = (String)getValueAt(rowIndex, colIndex);
    			if(type != null && type.length() > 0){
    				tip = type;
    			}
        		break;
    		case 2: //comment 
    			comment = (String)getValueAt(rowIndex, colIndex);
    			if(comment != null && comment.length() > 0){
    				tip = comment;
    			}
        		break;
    		case 3: // Checked column
    			
    			name = (String)model.getValueAt(rowIndex,0);
    			
                Object checked = model.getValueAt(rowIndex,colIndex);
    			
                if(checked instanceof Boolean){
                	if((Boolean)checked){
                		tip = name + " will be converted";
                	}else{
                		tip = name + " will NOT be converted";
                	}
                }
        		break;
        	default:
        		tip = super.getToolTipText(e);
    	}
    	
        return tip;
    }
    
	//Implement table header tool tips. 
    protected JTableHeader createDefaultTableHeader() {
    	return new JTableHeader(columnModel) {
    		
    		public String getToolTipText(MouseEvent e) {
    					String tip = null;
    					java.awt.Point p = e.getPoint();
    				
    					int index = columnModel.getColumnIndexAtX(p.x);
    					int realIndex = columnModel.getColumn(index).getModelIndex();
    					return columnToolTips[realIndex];
    				}
        	};
    }
    
    /**
     *	Open file and return a JAXB object
     * 
     * @param file
     * @return object
     */
    private Object getObjectFromFile( File file ){
		
		if(file != null && file.exists()){
			Loader loader = new Loader();
			
			Object o = null;
			
			try{
				o = loader.open(file);
			}catch(Exception e){
				;
			}
			
			return o;
		}
		
		return null;
	}

    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("ROPtableDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        JComponent newContentPane = new JPanel(new GridLayout(1,0));
        
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(new SpecificationTable());

        //Add the scroll pane to this panel.
        newContentPane.add(scrollPane);

        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
