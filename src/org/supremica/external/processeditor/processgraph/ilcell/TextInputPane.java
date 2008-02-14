package org.supremica.external.processeditor.processgraph.ilcell;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TextInputPane 
						extends 
							JPanel
{
	private EditableText[] txtPanes = null;
	
	public TextInputPane(String title, String[] names){
		super();
		init(title, names);
	}
	
	private void init(String title, String[] names){
		
		JPanel idPane = new JPanel();
		idPane.setLayout(new GridLayout( names.length, 0 ));
		
		JPanel dataPane = new JPanel();
		dataPane.setLayout(new GridLayout(names.length, 0 ));
		
		if(title != null && title.length() != 0){
			setBorder(BorderFactory.createTitledBorder(title));
		}
		
		txtPanes = new EditableText[names.length];
		for(int i = 0; i < names.length; i++){
			txtPanes[i] = new EditableText(names[i]);
			
			idPane.add(txtPanes[i].getNamePane());
			dataPane.add(txtPanes[i].getDataPane());
		}
		
		setLayout(new BorderLayout());
		add(idPane, BorderLayout.LINE_START);
		add(dataPane, BorderLayout.CENTER);
	}
	
	public String getText(String name){
		if(name == null){
			return "";
		}
		
		for(int i = 0; i < txtPanes.length; i++){
			if(name.equals(txtPanes[i].getName())){
				return txtPanes[i].getText();
			}
		}
		return "";
	}
	
	public void setText(String name, String text){
		if(name == null){
			return;
		}
		
		for(int i = 0; i < txtPanes.length; i++){
			if(name.equals(txtPanes[i].getName())){
				txtPanes[i].setText(text);
				return;
			}
		}
	}	
}

class EditableText 
			implements 
				ActionListener,
				MouseListener,
				FocusListener
{
	private JPanel namePane = null;
	private JPanel dataPane = null;
	
	private JButton jbName = null;
	
	private JTextField txtField = null;
	private JLabel dataLabel = null;
	
	public EditableText(String name){
		
		jbName = new JButton(name);
		jbName.addActionListener(this);
		jbName.addMouseListener(this);
		
		//hide button
		jbName.setContentAreaFilled(false);
		jbName.setBorderPainted(false);
		jbName.setFocusable(false);
		
		txtField = new JTextField();
		txtField.addActionListener(this);
		txtField.addFocusListener(this);
		
		dataLabel = new JLabel();
		dataLabel.addMouseListener(this);
		dataLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		namePane = new JPanel();
		namePane.setLayout(new BorderLayout());
		
		namePane.add(jbName, BorderLayout.LINE_END);
	
		
		dataPane = new JPanel();
		dataPane.setLayout(new BorderLayout());
		dataPane.add(dataLabel, BorderLayout.LINE_START);
	}
	
	public JPanel getNamePane(){
		return namePane;
	}
	
	public JPanel getDataPane(){
		return dataPane;
	}
	
	public String getText(){
		return dataLabel.getText();
	}
	
	public String getName(){
		return jbName.getText();
	}
	
	public void setText(String text){
		dataLabel.setText(text);
	}
	
	public void updateMe(){
		
		Component com = dataPane;
		
		com.validate();
		com.repaint();
		
		while(com.getParent() != null){
			com = com.getParent();
			com.validate();
			com.repaint();
		}
	}
	
	public void startEditing(){
		
		txtField.setText(dataLabel.getText());
		
		if(txtField.getText().length() > 7){
			txtField.setColumns(txtField.getText().length());
		}else{
			txtField.setColumns(7);
		}
		
		dataPane.remove(dataLabel);
		dataPane.add(txtField);
		
		txtField.setSize(txtField.getPreferredSize());
		
		txtField.validate();
		txtField.repaint();
		
		txtField.grabFocus();
		txtField.selectAll();
		
		dataPane.validate();
		dataPane.repaint();
		
		updateMe();
		
	}
	
	public void stopEditing(){
		dataLabel.setText(txtField.getText());
		
		dataPane.remove(txtField);
		dataPane.add(dataLabel);
		
		dataLabel.setSize(txtField.getPreferredSize());
		
		dataLabel.validate();
		dataLabel.repaint();
		
		updateMe();
	}
    
    /* --- ActionListener --- */
    public void actionPerformed(ActionEvent e){
    	if(e.getSource().equals(txtField)){
    		stopEditing();
    	}else if(e.getSource().equals(jbName)){
    		startEditing();
    	}
    }
    
    /* --- MouseListener --- */
    public void mouseClicked(MouseEvent e){
    	if(e.getSource().equals(dataLabel)){
    		startEditing();
    	}
    }
    public void mouseEntered(MouseEvent e){
    	if(e.getSource().equals(jbName)){
    		jbName.setContentAreaFilled(true);
    		jbName.setBorderPainted(true);
    	}
    }
    public void mouseExited(MouseEvent e){
    	if(e.getSource().equals(jbName)){
    		jbName.setContentAreaFilled(false);
    		jbName.setBorderPainted(false);
    	}
    } 
    public void mousePressed(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}

    /* --- FocusListener--- */
    public void focusGained(FocusEvent e) {} 
    public void focusLost(FocusEvent e){
    	if(e.getSource().equals(txtField)){
    		stopEditing();
    	}
    } 

}
