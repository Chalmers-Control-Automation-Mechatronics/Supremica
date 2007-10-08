package org.supremica.external.processeditor.xml.dop2efa.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FilePathPane extends JPanel implements ActionListener{
	
	private JTextField tfFilePath;
    private JButton jbSetFilePath;
    
    private JFileChooser fc;
    
    public FilePathPane(){
    	jbSetFilePath = new JButton("Set");
        jbSetFilePath.addActionListener(this);
        
        tfFilePath = new JTextField(30);
        tfFilePath.setToolTipText("File path");
        
        add(tfFilePath);
        add(jbSetFilePath);
    }
    
    public void setFileChooser(JFileChooser fc){
    	if(this.fc == null){
    		this.fc = fc;
    	}
    }
    
    public String getFilePath(){
    	return tfFilePath.getText();
    }
    
    public void actionPerformed(ActionEvent evt) {
    	int returnVal;
    	File file;
        Object o;
        
        o = evt.getSource();
        if(o == jbSetFilePath){
        	
        	if(fc == null){
        		fc = new JFileChooser();
        	}
        	
        	returnVal = fc.showOpenDialog(this);

            if(returnVal == JFileChooser.APPROVE_OPTION){
                file = fc.getSelectedFile();
                tfFilePath.setText(file.getAbsolutePath());
            }
        }else{
        	System.err.println("Unknown source " + o);
        }
    }
}
