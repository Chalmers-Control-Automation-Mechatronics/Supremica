package org.supremica.external.processeditor.processgraph.ilcell;

import java.awt.FlowLayout;

import javax.swing.*;
import java.awt.Color;
import java.awt.event.*;

public class TextInputPane 
						extends 
							JPanel
{
	TextInputPane(String title, String[] names){
		super();
		init(title, names);
	}
	
	private void init(String title, String[] names){
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		if(title != null && title.length() != 0){
			setBorder(BorderFactory.createTitledBorder(title));
		}
		
		for(int i = 0; i < names.length; i++){
			add(new TextPane(names[i]));
		}
	}
	
	public String getText(String name){
		int numberOfTextPanes = getComponentCount();
		
		if(name == null){
			return "";
		}
		
		for(int i = 0; i < numberOfTextPanes; i++){
			Object o = getComponent(i);
			if(o instanceof TextPane){
				if(name.equals(((TextPane)o).getName())){
					return ((TextPane)o).getText();
				}
			}
		}
		return "";
	}
	
	public void setText(String name, String text){
		int numberOfTextPanes = getComponentCount();
		
		if(name == null){
			return;
		}
		
		for(int i = 0; i < numberOfTextPanes; i++){
			Object o = getComponent(i);
			if(o instanceof TextPane){
				if(name.equals(((TextPane)o).getName())){
					((TextPane)o).setText(text);
				}
			}
		}
		return;
	}
	
}

class TextPane 
			extends 
				JPanel
					implements 
					MouseListener,
					ActionListener
{
	
	private JTextField txtField = null;
	private JLabel nameLabel = null;
	private JLabel dataLabel = null;
	
	TextPane(String name){
		super();
		setLayout(new FlowLayout(FlowLayout.LEFT));
		//setBorder(BorderFactory.createLineBorder(Color.black));
		
		nameLabel = new JLabel(name);
		dataLabel = new JLabel();
		
		nameLabel.addMouseListener(this);
		
		txtField = new JTextField();
		txtField.setColumns(20);
		txtField.addActionListener(this);
		
		add(nameLabel);
		add(dataLabel);
		
	}
	
	public String getText(){
		return dataLabel.getText();
	}
	
	public String getName(){
		return nameLabel.getText();
	}
	
	public void setText(String text){
		dataLabel.setText(text);
	}
	
	/* --- MouseListener --- */
	public void mouseClicked(MouseEvent e){
		txtField.setText(dataLabel.getText());
		
		remove(dataLabel);
		add(txtField);
			
		txtField.requestFocus();
			
		validate();
		repaint();
	}
	
	public void mouseEntered(MouseEvent e){}; 
	public void mouseExited(MouseEvent e){};
    public void mousePressed(MouseEvent e){}; 
    public void mouseReleased(MouseEvent e){};
    
    /* --- FocusListener --- */
    public void actionPerformed(ActionEvent e){
    	if(e.getSource().equals(txtField)){
    		
    		dataLabel.setText(txtField.getText());
    		
    		remove(txtField);
    		add(dataLabel);
    		validate();
    		repaint();
    	}
    }

}
