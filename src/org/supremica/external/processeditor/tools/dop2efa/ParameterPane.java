package org.supremica.external.processeditor.tools.dop2efa;

import javax.swing.*;
import java.util.Hashtable;

public class ParameterPane extends JPanel
{
    private static final long serialVersionUID = 1L;

	Hashtable<String, JCheckBox> options;
	
	public ParameterPane(){
		super();
		options = new Hashtable<String, JCheckBox>(); 
	}
	
	public void addOption(String txt,
						  String identifier,
						  boolean selected,
						  int mnemonic){
		
		//Create check boxes.
		JCheckBox chBox = new JCheckBox(txt);
		
        chBox.setMnemonic(mnemonic);
        chBox.setSelected(selected);
        
        options.put(identifier, chBox);
        
        add(chBox);
	}
	
	public boolean getValueOption(String identifier){
		return options.get(identifier).isSelected();
	}
}
