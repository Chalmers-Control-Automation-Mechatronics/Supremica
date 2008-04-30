package org.supremica.external.processeditor.tools.specificationsynthes;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FilePathPane 
					extends JPanel 
								implements ActionListener{
	
	private final static String TOOL_TIP_TEXT = "File path";
	
	private JTextField tfFilePath = null;
    private JButton jbSetFilePath = null;
    
    private JFileChooser fc = null;
    
    private String ext = "";
    
    public FilePathPane(){
    	jbSetFilePath = new JButton("Set");
        jbSetFilePath.addActionListener(this);
        
        tfFilePath = new JTextField(30);
        tfFilePath.setToolTipText(TOOL_TIP_TEXT);
        
        add(tfFilePath);
        add(jbSetFilePath);
    }
    
    public void setFileChooser(JFileChooser fc){
    	if(this.fc == null){
    		this.fc = fc;
    	}
    }
    
    public void setFileExtension(String ext){
    	this.ext = ext;
    }
    
    public String getFilePath(){
    	String absoluteFilePath = tfFilePath.getText();
    	
    	//no extension
    	if(ext.equals("")){
    		return absoluteFilePath;
    	}
    	
    	//add extension
    	if(!absoluteFilePath.endsWith(ext)){
			absoluteFilePath = absoluteFilePath.concat(ext);
		}
    	return absoluteFilePath;
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
