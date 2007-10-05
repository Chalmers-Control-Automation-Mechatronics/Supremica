package org.supremica.external.processAlgebraPetriNet.algorithms.dop2efa.gui;

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
    
    public String getFilePath(){
    	return tfFilePath.getText();
    }
    
    public void actionPerformed(ActionEvent evt) {
    	
        Object o = evt.getSource();
        
        if(o == jbSetFilePath){
        	fc = new JFileChooser();
        	int returnVal = fc.showOpenDialog(this);

            if(returnVal == JFileChooser.APPROVE_OPTION){
                File file = fc.getSelectedFile();
                tfFilePath.setText(file.getAbsolutePath());
            }
        }else{
        	System.err.println("unknown source " + o);
        }
    }
}
