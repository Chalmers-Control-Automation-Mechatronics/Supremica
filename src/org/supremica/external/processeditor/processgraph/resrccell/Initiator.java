package org.supremica.external.processeditor.processgraph.resrccell;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;

import org.supremica.external.processeditor.processgraph.NestedCellListener;
import org.supremica.external.processeditor.xgraph.GraphCell;


/**
 * Represents the initiator (and ending) cell of a complete sequence.
 */
public class Initiator extends GraphCell {

	private static final long serialVersionUID = 1L;

	public int sizeX = 80 ;
    public int sizeY = 50;
    
    private int borderThickness = 2;
    private int mainBorderThickness = 2;    
    private JPanel centerPanel, westPanel, eastPanel;        
    private JLabel resourceName;

    private NestedCellListener nestedCellListener = null;
    /**
     * Creates a new instance of the class with the specified name labeled.
     *
     * @param name the name that will be labeled on this initiator cell
     */ 
    public Initiator(String name) {	
	super(name);	
	setLayout(null);

	centerPanel = new JPanel();
	centerPanel.setLayout(null);
	westPanel = new JPanel();
	westPanel.setLayout(null);
	eastPanel = new JPanel();
	eastPanel.setLayout(null);
	
	centerPanel.setBounds(sizeX/8,0,(sizeX*6)/8,sizeY);	
	centerPanel.setLayout(new BorderLayout());
	centerPanel.add(resourceName = 
			new JLabel(name, JLabel.CENTER), 
			BorderLayout.CENTER);
	resourceName.setBounds(0, 
			       0,
			       centerPanel.getSize().width,
			       sizeY);
	resourceName.setFont(new Font("Serif", Font.BOLD, 12));
				
	westPanel.setBorder(new LineBorder(Color.gray,borderThickness));
	westPanel.setBounds(-borderThickness,
			    -borderThickness,
			    (sizeX/8)+borderThickness,
			    sizeY+2*borderThickness);
	eastPanel.setBorder(new LineBorder(Color.gray,borderThickness));
	eastPanel.setBounds(sizeX*7/8,
			    -borderThickness,
			    sizeX/8+2*borderThickness,
			    sizeY+2*borderThickness);	
	JPanel container = new JPanel();
	container.setLayout(null);
	container.add(centerPanel);
	container.add(westPanel);
	container.add(eastPanel);	       
	container.setBounds(mainBorderThickness, 
			    mainBorderThickness, 
			    sizeX,
			    sizeY);	
	add(container);
	setBackground(Color.gray);
	setBorder(new BevelBorder(BevelBorder.RAISED));
	setSize(mainBorderThickness*2+sizeX, 
		mainBorderThickness*2+sizeY);     
    }
    /**
     * Creates a new instance of the class. 
     * <p>
     * Specifies the machine, type and id that will be labeled.
     *
     * @param machine specifies the machine name
     * @param type specifies the type
     * @param id specifies the id
     */
    public Initiator(String machine, String type, String id) {	

	super(machine);	
	setLayout(null);

	centerPanel = new JPanel();
	centerPanel.setLayout(null);
	westPanel = new JPanel();
	westPanel.setLayout(null);
	eastPanel = new JPanel();
	eastPanel.setLayout(null);
	
	centerPanel.setBounds(sizeX/8,
			      0,
			      (sizeX*6)/8,
			      sizeY);
	centerPanel.setLayout(new BorderLayout());
	JLabel machineLabel = new JLabel(machine, 
					 JLabel.CENTER);
	JLabel typeLabel = new JLabel("TYPE: "+type, 
				      JLabel.CENTER);
	JLabel idLabel = new JLabel("ID: "+id, 
				    JLabel.CENTER);
	centerPanel.add(machineLabel, 
			BorderLayout.NORTH);
	centerPanel.add(typeLabel, 
			BorderLayout.CENTER);
	centerPanel.add(idLabel, 
			BorderLayout.SOUTH);
	machineLabel.setFont(new Font("Serif", 
				      Font.BOLD, 
				      12));		       
	westPanel.setBorder(new LineBorder(Color.gray,borderThickness));
	westPanel.setBounds(-borderThickness,
			    -borderThickness,
			    (sizeX/8)+borderThickness,
			    sizeY+2*borderThickness);
	eastPanel.setBorder(new LineBorder(Color.gray,borderThickness));
	eastPanel.setBounds(sizeX*7/8,
			    -borderThickness,
			    sizeX/8+2*borderThickness,
			    sizeY+2*borderThickness);	
	JPanel container = new JPanel();
	container.setLayout(null);
	container.add(centerPanel);
	container.add(westPanel);
	container.add(eastPanel);	       
	container.setBounds(mainBorderThickness, 
			    mainBorderThickness, 
			    sizeX,
			    sizeY);	
	add(container);
	setBackground(Color.gray);
	setBorder(new BevelBorder(BevelBorder.RAISED));
	setSize(mainBorderThickness*2+sizeX, 
		mainBorderThickness*2+sizeY);     
    }
    /**
     * Adds a new nested cell listener to this object.
     *
     * @param l the listener
     */
    public void addNestedCellListener(NestedCellListener l) {
	nestedCellListener = l;
    }
    /**
     * Calls the nested cell listener's <code>upPack()</code> method. 
     */
    public void upPack() {
	if(nestedCellListener != null) {
	    cellListener.upPack();
	}
    }
     /**
     * Translates this cell position
     * <p>
     * Translates this cell position, at position (<i>x</i>, <i>y</i>), by
     * <code>dx</code> along the <i>x</i> axis and <code>dy</code> along the
     * <i>y</i> axis so that the new position will be 
     * (<code>x+dx</code), <code>y+dy</code>).
     *
     * @param dx the distance to move this cell along the <i>x</i> axis
     * @param dy the distnace to move this cell along the <i>y</i> axis
     */
    public void translatePos(int dx, int dy) {
	super.translatePos(dx, dy);
	upPack();
    }            
}
   
 
