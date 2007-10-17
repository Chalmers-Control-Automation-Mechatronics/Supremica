package org.supremica.external.processeditor;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.*;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import org.supremica.external.processeditor.xml.dop2efa.gui.ConvertPanel;
import org.supremica.gui.Supremica;

public class SOCDOPtoEFAFrame extends JFrame implements ActionListener{
	
	ConvertPanel cPanel;
	
	public SOCDOPtoEFAFrame(){
		
		this.setExtendedState(Frame.MAXIMIZED_BOTH); 
		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		
		this.setTitle("DOP to EFA");
		this.setIconImage(Toolkit.getDefaultToolkit().
				getImage(Supremica.class.getClass().
						getResource("/icons/processeditor/icon.gif")));
		
        //Add contents to the window.
        cPanel = new ConvertPanel();
        cPanel.addActionListener(this);
        cPanel.setFrame(this);
        
        getContentPane().add(cPanel);
        
        
        this.pack();
        
        //Place frame in center of screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = this.getSize();
        
        Point c = new Point(screenSize.width/2, screenSize.height/2);
        c.translate(-frameSize.width/2, -frameSize.height/2);
        this.setLocation(c);
        
        this.setVisible(false);
	}
	
	public void setInputFileChooser(JFileChooser fc) {
    	cPanel.setInputFileChooser(fc);
    }
    
    public void setOutputFileChooser(JFileChooser fc) {
    	cPanel.setOutputFileChooser(fc);
    }
	
	/**
     *	Take care of action
     */
    public void actionPerformed(ActionEvent evt) {
    	
        String action = evt.getActionCommand();
        if(ConvertPanel.EXIT.equals(action)){
        	setVisible(false);
        }
    }
    
    public void setVisible(boolean b){
    	if(b){
    		cPanel.refreshTable();
    	}
    	super.setVisible(b);
    }
}
