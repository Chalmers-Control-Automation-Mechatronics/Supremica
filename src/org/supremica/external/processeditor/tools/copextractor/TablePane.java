package org.supremica.external.processeditor.tools.copextractor;

import java.util.List;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class TablePane
					extends
					    JPanel 
							implements
							    ActionListener
{
    private static final long serialVersionUID = 1L;

	private JButton jbAddFile = null;
	private JButton jbRemove = null;
	
	private int xSize = 100;
	private int ySize = 100;
	
	private SpecificationTable table = null;
	private JFileChooser fc = null;
	
	public TablePane(){
	    
		setLayout( new BorderLayout() );    
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout( new BoxLayout( buttonPanel,BoxLayout.Y_AXIS ) );
    
		jbAddFile = new JButton( "Add" );	
		jbRemove = new JButton( "Remove" );
		
		jbAddFile.addActionListener( this );
		jbRemove.addActionListener( this ); 
    
		buttonPanel.add( jbAddFile );
		buttonPanel.add( Box.createRigidArea( new Dimension( 1, 10 )) );
		buttonPanel.add( jbRemove );	    	    	   
    
		table = new SpecificationTable();
		
		
		table.setShowHorizontalLines( true );  
		table.setShowVerticalLines( true ); 
    
		table.setSize( ((xSize*4)/5), ySize );
		table.setRowHeight(20);
    	      	    	    
		add( new JScrollPane( table ), BorderLayout.CENTER );
		add( buttonPanel, BorderLayout.EAST );	    
		setSize( xSize, ySize );	   
	}
	
	public void addMouseListener(MouseListener l){
		table.addMouseListener( l );
	}
	
	public void setFileChooser(JFileChooser fc){
		if(this.fc == null){
    		this.fc = fc;
    	}
	}
	
	/**
	 * 
	 * @return List<String> whit all file paths in table 
	 */
	public List<String> getFilePathList(){
    	return table.getFilePathList();
    }
	
	/**
	 * 
	 * @return List<String> whit all file paths to marked files
	 */
	public List<String> getMarkedFilePathList(){
    	return table.getMarkedFilePathList();
    }
	
	public void refresh(){
		table.refresh();
	}
	
	public int rowAtPoint(Point p){
		return table.rowAtPoint( p );
	}
	
	public String getFilePathAtRow(int row){
		return table.getFilePathAtRow(row);
	}
	
	/**
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
    	
        Object o = evt.getSource();
        
        if(o == jbAddFile){
        	addFiles();
        }else if(o == jbRemove){
        	remove();
        }else{
        	//debug
        	System.err.println("unknown source " + o);
        	//debug
        }
    }
    
	/**
	 * 	Open file chooser to add ROP files. 
	 */
	private void addFiles(){
		
		if(fc == null){
			//Create a file chooser
			fc = new JFileChooser();
		}
		
        //store selection mode
        int tmp = fc.getFileSelectionMode();
        
        //set selection mode
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        
    	int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            
        	File file = fc.getSelectedFile();
            
        	if(file.isFile()){
            	table.addFile(file);
            }else if(file.isDirectory()){
            	File[] files = file.listFiles();
            	
            	for(int i = 0; i < files.length; i++){
            		if(files[i].isFile()){
            			table.addFile(files[i]);
            		}
            	}
            }
        } else {
            ;
        }
        
        //restore selection mode
        fc.setFileSelectionMode(tmp);
    }
	
	/**
	 * 	Add one file to table
	 * 	if the file contains no ROP
	 * 	nothing happens.
	 */
	public void addFile(File file){
		table.addFile(file);
	}
	
	/**
	 *	Removes selected ROP files from table
	 */
    private void remove(){
    	table.removeSelectedRows();
    	repaint();
    }
	
	
	
	
	
	
}