package org.supremica.external.processAlgebraPetriNet.algorithms.dop2efa.gui;

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
	private JButton jbAddDirectory;
	private JButton jbRemove;
	
	private int xSize = 100;
	private int ySize = 100;
	
	private ROPtable ropTable ;
	private JFileChooser fc;
	
	public ROPtablePane(){
	
		setLayout(new BorderLayout());	    	    
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.Y_AXIS));
    
		jbAddFile = new JButton("Add File");	
		jbAddDirectory = new JButton("Add Directory");
		jbRemove = new JButton("Remove");
		
		jbAddFile.addActionListener(this);
		jbAddDirectory.addActionListener(this);	
		jbRemove.addActionListener(this); 
    
		buttonPanel.add(jbAddFile);
		buttonPanel.add(Box.createRigidArea(new Dimension(1,10)));
		buttonPanel.add(jbAddDirectory);
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
	
	public List<String> getFilePathList(){
    	return ropTable.getFilePathList();
    }
	
	public List<String> getMarkedFilePathList(){
    	return ropTable.getMarkedFilePathList();
    }
	
	public void actionPerformed(ActionEvent evt) {
    	
        Object o = evt.getSource();
        
        if(o == jbAddFile){
        	addFile();
        }else if(o == jbAddDirectory){
        	addDirectory();
        }else if(o == jbRemove){
        	remove();
        }else{
        	System.err.println("unknown source " + o);
        }
    }
	
	private void addFile(){
    	//Create a file chooser
        fc = new JFileChooser();
        
    	int returnVal = fc.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            ropTable.addROPfile(file);
        } else {
            ;
        }
    }
    
    private void addDirectory(){
    	
    	fc = new JFileChooser();
        fc.setDialogTitle("Add directory");
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    	
    	int returnVal = fc.showOpenDialog(this);
    	
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            
            if(file.isDirectory()){
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
    }
    
    private void remove(){
    	ropTable.removeSelectedRows();
    	repaint();
    }
	
	
	
	
	
	
}