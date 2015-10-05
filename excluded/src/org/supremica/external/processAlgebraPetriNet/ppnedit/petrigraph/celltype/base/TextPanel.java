package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base;

import java.lang.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class TextPanel extends JPanel{
    
    public Font font;
    
	private JTextField textField = new JTextField();
	
    private InfoPanelListener infoPanelListener = null;
    
    public TextPanel(){
		super();
        setLayout(new GridLayout(1,1));
        font = new Font("Serif", Font.PLAIN, 12);
		
		textField.setFont(font);
        
		setText("!!");
        
		add(textField);
		setBounds(0,0,100,100);
    }
	public TextPanel(String text){
		this();
		setText(text);
	}
    
    public void setText(String nm){
        textField.setText(nm);
    }
    public void addInfoPanelListener(InfoPanelListener l){
        infoPanelListener = l;
    }
    
    public void paintComponent(Graphics g) {}
}


  
