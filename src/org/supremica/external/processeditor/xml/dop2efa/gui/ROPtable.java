package org.supremica.external.processeditor.xml.dop2efa.gui;
 
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

/** 
 * TableToolTipsDemo is just like TableDemo except that it
 * sets up tool tips for both cells and column headers.
 */
public class ROPtable extends JTable {
    
    ROPtableModel model;
    
    protected String[] columnToolTips = {"Machine name",
    									 "Type",
                                         "comments about the machine",
                                         "If checked, included in convert"};

    public ROPtable() {
        super(new ROPtableModel());
        setPreferredScrollableViewportSize(new Dimension(50, 100));
        setFillsViewportHeight(true);
    }
    
    public ROPtableModel getModel(){
    	return (ROPtableModel) super.getModel();
    }
    
    public void addROPfile(File ropFile){
    	String filePath = ropFile.getAbsolutePath();
    	ROP rop = getROPfromFile(ropFile);
    	
    	if(rop == null){
    		return;
    	}
    	
    	String machineName = rop.getMachine();
    	String comment = rop.getComment();
    	String ropType = rop.getType().toString();
    	
    	Object[] o = new Object[]{machineName,
				  				  ropType,
				  				  comment,
				  				  new Boolean(true),
				  				  filePath};
    	
		if(!getModel().rowExist(o)){
			getModel().insertRow(o);
	    	repaint();
		}
    	
    }
    
    public List<String> getFilePathList(){
    	List<String> filePathList = new LinkedList<String>();
    	
    	for(int i = 0; i < getRowCount(); i++){
    		String filePath = (String)getModel().getValueAt(i,getModel().FILE_PATH_COL);
    		filePathList.add(filePath);
    	}
    	
    	return filePathList;
    }
    
    public List<String> getMarkedFilePathList(){
    	List<String> filePathList = new LinkedList<String>();
    	
    	for(int i = 0; i < getRowCount(); i++){
    		if((Boolean)getValueAt(i,getModel().BOOLEAN_COL)){
    			String filePath = (String)getModel().getValueAt(i,getModel().FILE_PATH_COL);
    			filePathList.add(filePath);
    		}
    	}
    	
    	return filePathList;
    }
    
    public void removeSelectedRows(){
    	int rows[] = getSelectedRows();
    	for(int i = 0; i < rows.length; i++){
    		getModel().deleteRow(rows[i]);
    	}
    }
    
    //Implement table cell tool tips.
    public String getToolTipText(MouseEvent e) {
    	String tip = null;
    	
    	String fileName = null;
		String machineName = null;
		String comment = null;
		
    	java.awt.Point p = e.getPoint();
    	
    	int rowIndex = rowAtPoint(p);
    	int colIndex = columnAtPoint(p);
    	int realColumnIndex = convertColumnIndexToModel(colIndex);
    	
    	TableModel model = getModel();
    	
    	switch(realColumnIndex){
    		case 0://Machine name column
    			fileName = (String)model.getValueAt(rowIndex,getModel().FILE_PATH_COL);
    			machineName = (String)model.getValueAt(rowIndex,colIndex);
    			
    			if(fileName != null && machineName != null){
    				if(fileName.length() > 0 && machineName.length() > 0){
    					tip = machineName +" in "+fileName;
    				}
    			}
    			break;
    		
    		case 1: //Comment column
    			comment = (String)getValueAt(rowIndex, colIndex);
    			if(comment != null && comment.length() > 0){
    				tip = comment;
    			}
        		break;
    		case 2: //Type column
    			//tip = "";
        		break;
    		case 3: // Checked column
    			
    			machineName = (String)model.getValueAt(rowIndex,0);
    			
                Object checked = model.getValueAt(rowIndex,colIndex);
    			
                if(checked instanceof Boolean){
                	if((Boolean)checked){
                		tip = machineName + " will be converted";
                	}else{
                		tip = machineName + " will not be converted";
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
    
    private ROP getROPfromFile(File file){
		
		if(file != null && file.exists()){
			Loader loader = new Loader();
			
			Object o = null;
			try{
				o = loader.open(file);
			}catch(Exception e){
				;
			}
			
			if(o instanceof ROP){
				return (ROP)o;
			}
		}
		System.err.println("File " + file + " contains no ROP.");
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
        JScrollPane scrollPane = new JScrollPane(new ROPtable());

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
