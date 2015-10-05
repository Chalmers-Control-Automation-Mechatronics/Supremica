package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base;

import java.lang.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class EditInfoPanel extends InfoPanel
                                   implements ActionListener{
    
	public static final String ENTER = "ENTER";
	public static final String NEWTEXT = "NEWTEXT";
	
	private JTextField textField = null;
    
	public EditInfoPanel(String text){
		super();
		setLayout(new GridLayout(1,1));
		
		textField = new EditTextField(text);
		textField.addActionListener(this);
		setSize(textField.getPreferredSize());
		
		setText(text);
		add(textField);
	}
	
    public void paintComponent(Graphics g) {}
	
	public void actionPerformed( ActionEvent event ){
		if(event.getActionCommand().equals(ENTER)){
			setSize(textField.getPreferredSize());
			setText(textField.getText());
			repaint();
		}else if(event.getActionCommand().equals(NEWTEXT)){
			setSize(textField.getPreferredSize());
			repaint();
		}
	}
}


  
