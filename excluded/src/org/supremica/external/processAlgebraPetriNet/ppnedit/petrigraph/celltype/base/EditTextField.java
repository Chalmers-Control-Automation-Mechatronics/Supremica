package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base;

import java.lang.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class EditTextField extends JTextField
                                   implements KeyListener{
	
	public EditTextField(){
		super();
		setActionCommand("ENTER");
		addKeyListener(this);
	}
	public EditTextField(String text){
		super(text);
		
		setColumns(getText().length()+1);
		setSize(getPreferredSize());
		
		setActionCommand("ENTER");
		addKeyListener(this);
	}
	
	/*----------- KeyListener ------------------*/    
    public void keyPressed(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {
		
		setColumns(getText().length()+1);
		
		setActionCommand("NEWTEXT");
		fireActionPerformed();
		setActionCommand("ENTER");
		
		setSize(getPreferredSize());
		setScrollOffset(0);
    }
	/*----------- End KeyListener ------------------*/
}


  
