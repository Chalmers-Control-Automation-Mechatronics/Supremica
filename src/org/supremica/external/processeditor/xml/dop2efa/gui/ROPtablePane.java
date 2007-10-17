package org.supremica.external.processeditor.xml.dop2efa.gui;

import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.Font.*;
import java.io.*;

public class ROPtablePane extends JPanel implements ActionListener{
	
	private JButton jbAddFile;
	private JButton jbRemove;
	
	private int xSize = 100;
	private int ySize = 100;
	
	private ROPtable ropTable ;
	private JFileChooser fc;
	
	public ROPtablePane(){
	
		setLayout(new BorderLayout());	    	    
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.Y_AXIS));
    
		jbAddFile = new JButton("Add");	
		jbRemove = new JButton("Remove");
		
		jbAddFile.addActionListener(this);
		jbRemove.addActionListener(this); 
    
		buttonPanel.add(jbAddFile);
		buttonPanel.add(Box.createRigidArea(new Dimension(1,10)));
		buttonPanel.add(jbRemove);	    	    	   
    
     
		ropTable = new ROPtable();
    
		ropTable.setShowHorizontalLines(true);  
		ropTable.setShowVerticalLines(true); 
    
		ropTable.setSize(((xSize*4)/5),ySize);
		ropTable.setRowHeight(20);
    	    
		JScrollPane scrollPane = new JScrollPane(ropTable);	    	    	    
    
		add(scrollPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.EAST);	    
		setSize(xSize, ySize);	   
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
    	return ropTable.getFilePathList();
    }
	
	/**
	 * 
	 * @return List<String> whit all file paths to marked files
	 */
	public List<String> getMarkedFilePathList(){
    	return ropTable.getMarkedFilePathList();
    }
	
	public void refresh(){
		ropTable.refresh();
	}
	
	//take care of actions
	public void actionPerformed(ActionEvent evt) {
    	
        Object o = evt.getSource();
        
        if(o == jbAddFile){
        	addFiles();
        }else if(o == jbRemove){
        	remove();
        }else{
        	System.err.println("unknown source " + o);
        }
    }
	
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
            	ropTable.addROPfile(file);
            }else if(file.isDirectory()){
            	File[] files = file.listFiles();
            	
            	for(int i = 0; i < files.length; i++){
            		if(files[i].isFile()){
            			ropTable.addROPfile(files[i]);
            		}
            	}
            }
        } else {
            ;
        }
        
        //restore selection mode
        fc.setFileSelectionMode(tmp);
    }
   
    private void remove(){
    	ropTable.removeSelectedRows();
    	repaint();
    }
	
	
	
	
	
	
}