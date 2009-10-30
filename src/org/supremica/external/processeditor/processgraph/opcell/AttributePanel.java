package org.supremica.external.processeditor.processgraph.opcell;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;


/**
 * Represents the attribute panel.
 */
public class AttributePanel extends JPanel
{

	private static final long serialVersionUID = 1L;

	public String value = "";   
    public String type = "";
    public Color attributeColor = Color.white;    
    public JLabel nameLabel = new JLabel();
    public Boolean up = null, down = null;    
    public static int sizeX = 40;
    public static int sizeY = 30;    

    public JPanel indOne = new JPanel(),indTwo= new JPanel();       

    /**
     * Creates a new instance of this class.    
     *
     * @param v the attribute value
     * @param t the attribute type
     * @param u sets the upper indicator. 
     * If <code>true</code> the indicator is set,
     * otherwise <code>false</code>. If <code>null</code> no indicator is used.
     * @param l sets the lower indicator. 
     * If <code>true</code> the indicator is set,
     * otherwise <code>fasle</code>. If <code>null</code> no indicator is used.
     */
    public AttributePanel(String v, String t,  Boolean u, Boolean l) {    
	//DEBUG
	//System.out.println("AttributePanel()");
	//END DEUG	
	type = t;
	value = v;
	setValue(v);		
	setUpperIndicator(u);
	setLowerIndicator(l);
				
	this.setLayout(null);
	this.setBorder(new BevelBorder(BevelBorder.RAISED));
	this.add(nameLabel);	       
	this.add(indOne);
	this.add(indTwo);              	
    } 
    /**
     * Creates a new instance of this class.
     */     
    public AttributePanel() {    
	    this("", "", true, false);//,30,30);
    }                    
    /**
     * Sets the attribute value.
     *
     * @param v the attribute value.
     */
    public void setValue(String v) {    
	//DEBUG
	//System.out.println("AttributePanel.setName()");
	//END DEBUG
	nameLabel.setText(v);       	       
	repaint();
    }
    /**
     * Returns the attribute color.
     *
     * @return the attribute color
     */
    public Color getAttributeColor() {
	return attributeColor;
    }
    /**
     * Sets the attribute color.
     *
     * @param c the attribute color
     */
    public void setAttributeColor(Color c) {
	//DEBUG 
	//System.out.println("AttributePanel.setAttributeColor()");	
	//END DEBUG
	attributeColor = c;       
    }
    /**
     * Sets the upper indicator.
     *
     * @param u if <code>true</code> the indicator is set,
     * otherwise <code>false</code>. If <code>null</code> no indicator is used.
     */
    public void setUpperIndicator(Boolean u) {    
	up = u;
	indOne.setBorder(new BevelBorder(BevelBorder.RAISED));
	if(u == null) {
	    indOne.setBorder(null);
	    indOne.setBackground(new Color(0,0,0,0));
	}else if(u) {	    		
	    indOne.setBackground(Color.green);	   
	}else {	    
	    indOne.setBackground(Color.lightGray);
	}	    	
	repaint();
    }
    /**
     * Returns the value of the upper indicator.
     *
     * @return <code>true</code> the indicator is set, 
     * <code>false</code> otherwise. If <code>null</code> no indicator is used.
     */
    public Boolean getUpperIndicator() {
	return up;
    }
    /**
     * Sets the lower indicator.
     *
     * @param l if <code>true</code> the indicator is set,
     * otherwise <code>false</code>. If <code>null</code> no indicator is used.
     */
    public void setLowerIndicator(Boolean l) {    
	down = l;
	indTwo.setBorder(new BevelBorder(BevelBorder.RAISED));
	if(l == null) {
	    indTwo.setBorder(null);
	    indTwo.setBackground(new Color(0,0,0,0));
	}else if(l) {	    
	    indTwo.setBackground(Color.green);			    
	}else {	    
	    indTwo.setBackground(Color.lightGray);
	}	
	repaint();
    }
    /**
     * Returns the value of the lower indicator.
     * 
     * @return <code>true</code> the indicator is set,
     * <code>false</code> otherwise. If <code>null</code> no indicator is used.
     */
    public Boolean getLowerIndicator() {
	return down;
    }   
    /**
     * Sets the font size of this attribute panel.
     */
    public void setAttributeSize() {	 
	//DEBUG
	//System.out.println("AttributePanel.setAttributeSize()");
	//END DEBUG
	this.setSize(sizeX, sizeY);		       
	nameLabel.setFont(new Font("Serif", Font.PLAIN, 12));				   
	while(nameLabel.getFontMetrics(nameLabel.getFont()).stringWidth(nameLabel.getText()) > sizeX) {
	    nameLabel.setFont(new Font("Serif", Font.PLAIN, nameLabel.getFont().getSize()-1));
	}
	nameLabel.setBounds(0,sizeY/5, sizeX, sizeY*3/5);
	indOne.setBounds(0,0, sizeX, sizeY/5);
	indTwo.setBounds(0,sizeY*4/5, sizeX, sizeY/5);
	repaint();
    }
    /**
     * Sets the indicators invisible.
     *
     * @param set if <code>true</code> the indicators will be invisible,
     * otherwise <code>false</code>
     */
    public void setIndicatorsInvisible(boolean set) {
	if(set) {
	    indOne.setBorder(null);	
	    indTwo.setBorder(null);
	    indOne.setBackground(new Color(0,0,0,0));
	    indTwo.setBackground(new Color(0,0,0,0));
	}
    }
}


  
