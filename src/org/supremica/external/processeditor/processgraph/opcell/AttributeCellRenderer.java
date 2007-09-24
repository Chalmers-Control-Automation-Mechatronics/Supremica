package org.supremica.external.processeditor.processgraph.opcell;

import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * Attribute renderer.
 */
class AttributeCellRenderer extends AttributePanel implements ListCellRenderer
{    
    protected RowPainter myContainer = null;    
    public AttributeCellRenderer(RowPainter rp)
    {	
	myContainer = rp;
	setOpaque(true);
	setLayout(null);	
    }       
    
    /**
     * Creates a new instance of this class.
     */
    public Component getListCellRendererComponent(
						  JList list,
						  Object value,
						  int index,
						  boolean isSelected,
						  boolean cellHasFocus)
    {        
	//DEBUG
	//System.out.println("AttributeCellRenderer.getListCellRendererComponent()");
	//END DEBUG					
	this.setValue(((AttributePanel)value).nameLabel.getText());		       
	this.setUpperIndicator(((AttributePanel)value).getUpperIndicator());
	this.setLowerIndicator(((AttributePanel)value).getLowerIndicator());   	
	this.setAttributeSize();	       	       	
        if(isSelected) {
	    this.setBackground(Color.blue);
            this.nameLabel.setBackground(list.getSelectionBackground());
            this.nameLabel.setForeground(list.getSelectionForeground());   
        }else {
	    this.setBackground(((AttributePanel)value).getAttributeColor());
            this.nameLabel.setBackground(list.getBackground());
            this.nameLabel.setForeground(list.getForeground());	   
        }	       
        return this;
    }
}
