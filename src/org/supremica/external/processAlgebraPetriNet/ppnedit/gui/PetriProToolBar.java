package org.supremica.external.processAlgebraPetriNet.ppnedit.gui;

import javax.swing.*;
import javax.swing.border.*;

import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn.PetriPro;

import java.awt.*;
import java.awt.Font.*;
import java.awt.event.*;
import java.io.*;




public class PetriProToolBar extends JToolBar implements ActionListener{    
    //string of buttons
    private final String[] buttons ={"PetriProEdit"};
    
    //GETTING ICONS AT DIRECTORY searchIconsIn
    private String searchIconsIn;

    private JButton[] jbPetriNet;
        
    private ToolBarListener l;
    
    private PetriProFrame ppframe;

    public PetriProToolBar(){  
	super();
        
	String allClasspath = System.getProperty("java.class.path");
	String[] classpath = allClasspath.split(";");
	for(int i = 0; i < classpath.length; i++) {
	    if((new File(classpath[i]+"\\org\\copvision\\Icons\\")).exists()) {
		searchIconsIn = classpath[i]+"\\org\\copvision\\Icons\\";
	    }
	}       
	
        
        addButtons(buttons);	
    }
    private void addButtons(String[] buttons){
        
        //numbers of Buttons
        int ant = buttons.length; 
        
        //create buttons
        jbPetriNet = new JButton[ant];
        
        //add all buttons
        for(int i = 0; i < buttons.length; i++){
            this.add(jbPetriNet[i] = new JButton(buttons[i]));  //add button
            
            //action
            jbPetriNet[i].setActionCommand(buttons[i]);
            jbPetriNet[i].addActionListener(this);              
        }
    }
    
    public void addToolBarListener(ToolBarListener tbl) {
    	l = tbl;
    }
    
    public void actionPerformed(ActionEvent e){
    	String itemName = e.getActionCommand();
        
    	//Check if JButton	
    	if (e.getSource()instanceof JButton){
            
            String actionButton = e.getActionCommand();
            
            if(actionButton == "PetriProEdit"){
                ppframe = new PetriProFrame();
            } else {
                System.out.println("Not implemented" + actionButton);
            }
        }
    }
}		       
