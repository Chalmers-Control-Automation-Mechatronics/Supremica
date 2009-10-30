package org.supremica.external.processeditor.processgraph.resrccell;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.JList;

import org.supremica.external.processeditor.processgraph.NestedCell;
import org.supremica.external.processeditor.processgraph.NestedGraph;
import org.supremica.external.processeditor.processgraph.OperationEdge;
import org.supremica.external.processeditor.processgraph.opcell.OperationCell;
import org.supremica.external.processeditor.xgraph.CellEvent;
import org.supremica.external.processeditor.xgraph.GraphCell;
import org.supremica.external.processeditor.xml.Converter;
import org.supremica.manufacturingTables.xsd.processeditor.ObjectFactory;
import org.supremica.manufacturingTables.xsd.processeditor.ROP;
import org.supremica.manufacturingTables.xsd.processeditor.ROPType;
import org.supremica.manufacturingTables.xsd.processeditor.Relation;
import org.supremica.manufacturingTables.xsd.processeditor.RelationType;


/**
 * Graphical representation of the objects of the <code>org.xml.ROP</code> 
 * class.
 */
public class ResourceCell
	extends	NestedCell 
{    
	private static final long serialVersionUID = 1L;

	private boolean emptyCell = true;    

    public GraphCell initCell = null;
    public GraphCell functionCell = null;
    public GraphCell endCell = null;

    private String[] uniqueAttributes = new String[0];
    private Color[] uniqueAttributesColor = new Color[0];    
    private int[] uniqueAttributesVisible = new int[0];       

    private File myFile = null;

    private JList selectedList = null;

    private ObjectFactory objectFactory = new ObjectFactory();

    /**
     * Creates a new empty instance of the class.
     * <p>     
     */
    public ResourceCell() {
    	super();
    	setLayout(null);	
    	cell.addGraphListener(this);
    	cells.addGraphListener(this);
    }  
    /**
     * Creates a new instance of the class that will be associated with the 
     * specified ROP object.
     */
    public ResourceCell(ROP rop) {	
	this();	
	try {	   
	    //complexFunction = rop;	    	   	    
	    setFunction(rop);	    
	    build();	    	    	       	   
	    buildBody();
	    setCompressed(!compressed);
	    downPack();	    
	}catch(Exception ex) {
	    //DEBUG
	    System.out.println("ERROR! while building resourceCell");
	    //END DEBUG
	}
	uniqueAttributes();
	setAttributeTypeColor();		
    }        
    /**
     * Rebuilds the contents of this cell's graph container.
     */
    public void rebuild() {
    	//DEBUG
    	//System.out.println("ResourceCell.rebuild()");
    	//END DEBUG
    	try{
    		Point tmpPos = getPos();
    		removeAll();
    		cell = new NestedGraph();
    		cells = new NestedGraph();
    		build();	   	    
    		buildBody();		     
    		compressed = !compressed;
    		setCompressed(!compressed);
    		cell.addGraphListener(this);
    		cells.addGraphListener(this);
    		downPack();
    		setPos(tmpPos);	    	    
    	}catch(Exception ex) {
    		//DEBUG
    		//System.out.println("ERROR! while rebuilding resourceCell");
    		//END DEBUG
    	}
    	uniqueAttributes();
    	setAttributeTypeColor();		  
    }
    /**
     * Builds the contents of this nested cell, based on this resource cell 
     * ROP object.
     */
    public void build() {
    	//DEBUG
    	//System.out.println(this+".NestedCell.build()");
    	//END DEBUG			
    	//??????????????????????????????????
    	//This call was removed 2007-05-24
    	//buildCompressedCell();		
    	//??????????????????????????????????	       
    	String type = "";
    	if(getROP().getType().equals("COP")) {
    		type = "COP";
    	}else if(getROP().getType().equals("ROP")) {
    		type = "DOP";
    	}       
    	initCell = new Initiator(getROP().getMachine(),
				 	type, 
				 	getROP().getId());	
    	((Initiator)initCell).addNestedCellListener(this);	
    	cells.insert(initCell);	
    	initCell.setPos(new Point(NestedGraph.marginX, NestedGraph.marginY));
	            
    	endCell = new Initiator("END");
    	((Initiator)endCell).addNestedCellListener(this);
    	cells.insert(endCell);
    	endCell.setPos(new Point(NestedGraph.marginX,
    				initCell.getPos().y+
    				initCell.getSize().height+
    				NestedGraph.marginY+
    				cellStepY));	    			
    	cells.insert(new OperationEdge(initCell, endCell));	
    }	
    /**
     * Builds the contents of this resource cell, based on this resoruce cell
     * ROP object.
     */
    protected void buildBody() {	
    	//DEBUG
    	//System.out.println("ResourceCell.buildBody()");
    	//END DEBUG
    	if(getROP().getRelation() != null) {
    		try {
    			cells.removeAllEdges();	    
    			functionCell = new NestedCell(getROP().getRelation());	    
    			((NestedCell)functionCell).addNestedCellListener(this);
    			cells.insert(functionCell);	    
    			functionCell.setPos(new Point(NestedGraph.marginX,
    					initCell.getPos().y+
    					initCell.getSize().height+
    					NestedGraph.marginY+
    					cellStepY));	    
    			endCell.setPos(new Point(NestedGraph.marginX,
    					functionCell.getPos().y+
    					functionCell.getSize().height+
    					NestedGraph.marginY+
    					cellStepY));	    	    
    			cells.insert(new OperationEdge(initCell, functionCell));
    			cells.insert(new OperationEdge(functionCell, endCell));
    			adjusteHorizontalPos();	    
    			emptyCell = false;
    		}catch(Exception ex) {
    			System.err.println("ERROR! while buildBody in ResourceCell");
    			ex.printStackTrace();
    		}	    	
    	}else {
    		emptyCell = true;	    
    	}
    }
    
    /**
     * Creates a new body for this resource cell, 
     * based on the object <code>o</code>.
     *
     * @param o the object that specifies the body
     */
    public void newBody(Object o) {	
    	//DEBUG
    	//System.out.println("ResourceCell.newBody");
    	//END DEBUG
    	if(getROP() != null) {
    		if(o instanceof NestedCell) {
    			if(getRelation() != null) {
    				getActivityRelationGroup().
    				add(0, ((NestedCell)o).getFunction());		   
    			}else {		     
    				if(((NestedCell)o).getFunction() instanceof Relation) {    
    					getROP().setRelation(((NestedCell)o).getRelation());
    				}else if(((NestedCell)o).getActivity() != null) {
    					try {
    						Relation newRelation = objectFactory.createRelation();
    						newRelation.setType(RelationType.SEQUENCE);
    						newRelation.getActivityRelationGroup().add(((NestedCell)o).getFunction());
    						getROP().setRelation(newRelation); 
    					}catch(Exception ex) {}
    				}
    			}
    			rebuild();
    		}else if(o instanceof ROP) {
    			setFunction(o);
    			rebuild();
    		}else if(o instanceof Relation) {
    			getROP().setRelation((Relation)o);
    			rebuild();
    		}	    
    	}
	
    }    
    /**
     * Returns whether this resource cell is empty or not.
     *
     * @return <code>true</code> if this resource cell is empty,
     * <code>false</code> otherwise
     */
    public boolean isEmpty() {
    	return emptyCell;
    }
    /**
     * Invoked when a cell is pressed.
     * <p>
     * Forward this event to its graph container.
     */
    public void cellPressed(CellEvent cEvent) {
	if(selectedList != null) {
	    selectedList.clearSelection();
	    selectedList = null;
	}
	super.cellPressed(cEvent);
    }
    /**
     * Set the list
     *
     * @param list the concerned list
     */
    public void setList(JList list) {
	if(selectedList != null) {
	    if(!selectedList.equals(list)) {
		selectedList.clearSelection();
	    }
	}
	selectedList = list;
    }      
    /**
     * Returns the color of the specified attribute type.
     *
     * @param attributeType the attribute type
     * @return the color of the attribute type
     */
    public Color getAttributeColor(String attributeType) {
	for(int i = 0; i < uniqueAttributes.length; i++) {
	    if(uniqueAttributes[i].equals(attributeType)) {
		return uniqueAttributesColor[i];
	    }
	}      	
	return Color.white;
    }
    /**
     * Updates the internal array <code>uniqueAttributes</code>.
     */
    private void uniqueAttributes() {
	String[] oldUniqueAttributes = uniqueAttributes;
	uniqueAttributes = Converter.getUniqueAttributes(getFunction());
	Color[] oldUniqueAttributesColor = uniqueAttributesColor;
	uniqueAttributesColor = new Color[uniqueAttributes.length];
	uniqueAttributesVisible = new int[uniqueAttributes.length];
	for(int i = 0; i < uniqueAttributes.length; i++) {	    
	    uniqueAttributesColor[i] = Color.white;
	    for(int j = 0; j < oldUniqueAttributes.length; j++) {
		if(uniqueAttributes[i].equals(oldUniqueAttributes[j])) {
		    uniqueAttributesColor[i] = oldUniqueAttributesColor[j];
		    break;
		}
	    }	 	   	    
	    uniqueAttributesVisible[i] = Converter.isAttributeTypeVisible(getFunction(), uniqueAttributes[i]);
	}		
    }
    /**
     * Sets the color of the attribute types.
     */
    public void setAttributeTypeColor() {	
	if(functionCell != null) {
	    CellEvent[] opCells = functionCell.getCells();
	    for(int i = 0; i < opCells.length; i++) {
		if(opCells[i].getSource() instanceof OperationCell) {
		    ((OperationCell)opCells[i].getSource()).setAttributeTypeColor(uniqueAttributes, uniqueAttributesColor);
		}
	    }
	}
    }   
    /**
     * Returns the unique attribute types of this resource cell.
     *
     * @return the array with unique attribute types
     */
    public String[] getUniqueAttributes() {	
	return uniqueAttributes;
    }   
    /**
     * Returns the color of the unique attribute types of this resource cell.
     *
     * @return the array with color of the unique attribute types
     */
    public Color[] getUniqueAttributesColor() {
	return uniqueAttributesColor;
    }
    /**
     * Sets the color of the  attributes of the specified type.
     *
     * @param type the attribute type
     * @param c the color
     */
    public void setUniqueAttributesColor(String type, Color c) {
	for(int i = 0; i < uniqueAttributes.length; i++) {
	    if(uniqueAttributes[i].equals(type)) {
		uniqueAttributesColor[i] = c;
		setAttributeTypeColor();
	    }
	}
    }
    /**
     * Returns the information whether the unique attribute types are visible.
     * 
     *
     * @return the array with the information whether the unique attributes 
     * are visible. The information is structured in as follow:
     * <ul>
     * <li>org.xml.Converter.IS_VISIBLE_ERROR</li>
     * <li>org.xml.Converter.IS_VISIBLE_TRUE</li>
     * <li>org.xml.Converter.IS_VISIBLE_FALSE</li>
     * <li>org.xml.Converter.IS_VISIBLE_TRUE_CHANGED</li>
     * <li>org.xml.Converter.IS_VISIBLE_FALSE_CHANGED</li>
     * </ul>
     */
    public int[] getUniqueAttributesVisible() {
	return uniqueAttributesVisible;
    }
    /**
     * Sets the attributes of the specified type visible or not.
     *
     * @param type the attribute type
     * @param visible if <code>true</code> the attributes will
     * be set to visible, otherwise <code>false</code>
     */
    public void setAttributeTypeVisible(String type, boolean visible) {
	if(functionCell != null) {
	    CellEvent[] allOperationCells = functionCell.getCells();
	    for(int i = 0; i < allOperationCells.length; i++) {
		if(allOperationCells[i].getSource() instanceof OperationCell) {
		    ((OperationCell)allOperationCells[i].getSource()).setAttributeTypeVisible(type, visible);
		}
	    }
	    rebuild();
	}
    }
    /**
     * Sets unique operation name of the specified object.
     * <p>
     * Sets unique operation name for the specified object if it is 
     * of an <code>instance</code> of the <code>org.xml.rop.Activity</code>
     * class.
     * 
     * @param o the object to give unique operation name
     * @return the object with unique operation name
     */
    public Object setUniqueNames(Object o) {
	//DEBUG
	//System.out.println("ResourceCell.setUniqueNames()");
	//END DEBUG
	return Converter.setUniqueNames(getFunction(), o);
    }
    /**
     * Creates and returns a copy of this resource cell.
     *
     * @return the copy
     */
    protected  NestedCell clone() {
	if(getROP() != null) {
	    return new ResourceCell((ROP)Converter.clone(getFunction()));
	}else {
	    return null;
	}	 
    }
    /**
     * Returns a copy of this resource cell.
     * 
     * @return the copy
     */
    public NestedCell copy() {
	return clone();
    }    
    /**
     * Creates a outer relation around the body of this resource cell.
     */
    public void createOuterRelation() {
	//DEBUG
	//System.out.println("ResourceCell.createOuterRelation()");
	//END DEBUG
	if(getROP() != null) {
	    try {
		Relation newElement = objectFactory.createRelation();
		newElement.setType(RelationType.SEQUENCE);
		newElement.
		    getActivityRelationGroup().
		    add(getROP().getRelation());
		newBody(newElement);
	    }catch(Exception ex) {}
	}
    }
    /**
     * If there is any, removes the outer relation around this resorce
     * cell body.
     */
    public void removeOuterRelation() {
	//DEBUG
	//System.out.println("ResourceCell.removeOuterRelation()");
	//END DEBUG
	if(getActivityRelationGroup() != null &&
	   getActivityRelationGroup().size() == 1 &&
	   getActivityRelationGroup().get(0) instanceof Relation) {
	    newBody(getActivityRelationGroup().get(0));
	}    
    }
    /**
     * Adds the <code>newElement</code> object into this resource cell's
     * complex function.
     * <p>
     * The <code>newElement</code> is inserted next to the 
     * <code>oldElement</code>.
     * 
     * @param oldElement the object where to insert the added object next to
     * @param newElement the object to be added
     */
    public void elementAdd(Object oldElement, Object newElement) {
	if(getROP() != null) {
	    try {
		Relation newRelationElement = objectFactory.createRelation();
		newRelationElement.setType(RelationType.SEQUENCE);
		newRelationElement.getActivityRelationGroup().add(oldElement);
		newRelationElement.getActivityRelationGroup().add(newElement);
		newBody(newRelationElement);
	    }catch(Exception ex) {}
	}
    }
    /**
     * Deleteds the <code>element</code> object from
     * this resource cell's complex function.
     *
     * @param element the object to be deleted
     */
    public void elementDelete(Object element) {
	//DEBUG
	//System.out.println("ResourceCell.elementDelete()");
	//END DEBUG
	Point tmpPos = getPos();
	tmpPos.translate(getSize().width/2, getSize().height/2);
	ROP newROP = null;
	try {
	    newROP = objectFactory.createROP();
	}catch(Exception ex) {}
	    if(getROP() != null) {
		try {
		    newROP.setType(getROP().getType());    
		}catch(Exception ex) {
		    newROP.setType(ROPType.COP);
		}
		try {
		    newROP.setId(getROP().getId());
		}catch(Exception ex) {
		    newROP.setId("0");
		}
		try {
		    newROP.setComment(getROP().getComment());
		}catch(Exception ex) {}
		try {
		    newROP.setMachine(getROP().getMachine());
		}catch(Exception ex) {
		    newROP.setMachine("Machine");
		}
	    }else {
		newROP.setType(ROPType.COP);
		newROP.setId("0");
		newROP.setMachine("Machine");
	    }	
	newBody(newROP);
	tmpPos.translate(-getSize().width/2, -getSize().height/2);
	setPos(tmpPos);
    }
    /**
     * Replaces the <code>oldElement</code> object with 
     * the <code>newElement</code> object in this resource cell's
     * complex function.
     *
     * @param oldElement the object that is to be replaced
     * @param newElement the object that is to replace the old object.
     */
    public void elementReplace(Object oldElement, Object newElement) {
	newBody(newElement);
    }
    /**
     * Pastes the object to this resource cell's body.
     */
    public void paste(Object o) {	
	newBody(o);
    }    
    /**
     * Invoked when a mouse button has been released on this cell.
     */
    public void mouseReleased(MouseEvent e) {	
	upPack();
	cellListener.cellReleased(new CellEvent(this, e));
	if(e.getClickCount() > 1) {
	    resourceInfo();
	}
    }
    /**
     * Returns <code>false</code>.
     */
    public boolean isCompressed() {
    	return false;
    }
    /**
     * Displays the resource info window, which allow the user to edit
     * the resource information.
     */ 
    public void resourceInfo() {
	ResourceCellInfoWindow resourceInfo = 
	    new ResourceCellInfoWindow(getROP());
	int result =  resourceInfo.showDialog();
	removeSelection();
	if(result == ResourceCellInfoWindow.APPROVE_OPTION) {
	    rebuild();
	}
    }
    /**
     * Sets the file associated with this resource.
     *
     * @param file the file
     */
    public void setFile(File file) {
    	myFile = file;
    }
    /**
     * Returns the file associated with this resource.
     *
     * @return the file
     */
    public File getFile() {
	return myFile;
    }
}
