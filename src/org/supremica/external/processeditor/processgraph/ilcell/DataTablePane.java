package org.supremica.external.processeditor.processgraph.ilcell;

import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Font.*;
import java.io.*;

public class DataTablePane
						extends 
							JPanel 
						implements 
							ActionListener
{
	
	private JButton jbAdd;
	private JButton jbRemove;
	private JButton jbFromFile;
	
	private int xSize = 50;
	private int ySize = 50;
	
	private BasicTable table;
	private JFileChooser fc;
	
	public DataTablePane(){
	
		setLayout(new BorderLayout());	    	    
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.Y_AXIS));
    
		jbAdd = new JButton("Add");	
		jbRemove = new JButton("Remove");
		jbFromFile = new JButton("File");
		
		jbAdd.addActionListener(this);
		jbRemove.addActionListener(this); 
		jbFromFile.addActionListener(this);
		
		buttonPanel.add(jbAdd);
		buttonPanel.add(Box.createRigidArea(new Dimension(1,10)));
		buttonPanel.add(jbRemove);	    	    	   
		buttonPanel.add(Box.createRigidArea(new Dimension(1,10)));
		buttonPanel.add(jbFromFile);
     
		table = new BasicTable();
		
		table.addCol("Name");
		table.addCol("Type");
		table.addCol("Values");
		table.initColumnSizes();
		
		table.setShowHorizontalLines(true);  
		table.setShowVerticalLines(true); 
    
		table.setSize(((xSize*4)/5),ySize);
		table.setRowHeight(20);
    	    
		JScrollPane scrollPane = new JScrollPane(table);	    	    	    
    
		add(scrollPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.EAST);	    
		setSize(xSize, ySize);	   
	}
	
	public BasicTable getTable(){
		return table;
	}
	
	public void setFileChooser(JFileChooser fc){
		if(this.fc == null){
    		this.fc = fc;
    	}
	}
	
	/**
	 * (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
    	
        Object o = evt.getSource();
        
        if(o == jbAdd){
        	addRow();
        }else if(o == jbRemove){
        	remove();
        }else{
        	System.err.println("unknown source " + o);
        }
    }
	
	private void addRow(){
		table.addRow("");
	}
	
	/**
	 * 	
	 */
	private void fromFile(){
		
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
            	;
            }else if(file.isDirectory()){
            	File[] files = file.listFiles();
            	
            	for(int i = 0; i < files.length; i++){
            		if(files[i].isFile()){
            			;
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
	 *	Removes selected ROP files from table
	 */
    private void remove(){
    	table.removeSelectedRows();
    	repaint();
    }
}