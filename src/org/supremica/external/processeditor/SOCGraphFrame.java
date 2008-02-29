package org.supremica.external.processeditor;

import javax.swing.*;
import java.awt.*;
import java.io.*;

import org.supremica.external.processeditor.processgraph.*;
import org.supremica.external.processeditor.processgraph.ilcell.*;
import org.supremica.external.processeditor.processgraph.eopcell.ExecutionOfOperationCell;
import org.supremica.external.processeditor.processgraph.resrccell.*;
import org.supremica.external.processeditor.xgraph.*;
import org.supremica.external.processeditor.xml.*;
import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.external.processeditor.xml.Converter;
import org.supremica.external.processeditor.xgraph.Graph;
import org.supremica.external.processeditor.xgraph.GraphCell;
import org.supremica.external.processeditor.xgraph.GraphScrollPane;
import org.supremica.external.processeditor.processgraph.resrccell.ResourceCell;
import org.supremica.external.processeditor.processgraph.ilcell.InterLockCell;
import org.supremica.external.processeditor.processgraph.eopcell.ExecutionOfOperationCell;
import org.supremica.manufacturingTables.xsd.processeditor.ObjectFactory;
import org.supremica.manufacturingTables.xsd.processeditor.ROP;
import org.supremica.manufacturingTables.xsd.il.IL;
import org.supremica.manufacturingTables.xsd.eop.EOP;

import org.supremica.manufacturingTables.xsd.processeditor.RelationType;
import org.supremica.external.processeditor.xgraph.Selection;
import org.supremica.external.processeditor.xgraph.SelectionListener;

/**
 * Represents the SOC worksheets.
 * <p>
 * Represents the SOC worksheets and handles all possible modifications to it.
 * To the worksheet the user can open/insert existing resources from files, 
 * paste copied objects or create new ones. With objects we general menas
 * resources, relations or operations. All user modifications to the objects
 * has to go thru the worksheets, i.e. the internal frames 
 * which is an instance of this class. 
 */
public class SOCGraphFrame extends JInternalFrame implements SelectionListener {
    private Point defaultLocation = new Point(30, 30);
    private GraphScrollPane graphScroll;
    private Graph graph = new Graph();    
    private boolean multiMode = false;    

    private SOCGraphFrameListener myListener;

    private final int INSERT_MARGIN_X = 50;
    private final int INSERT_MARGIN_Y = 50;
    private int numOfNewCellsToDay = 0;
    private int numOfNewResourcesToDay = 0;

    private ObjectFactory objectFactory = new ObjectFactory();
	
    /**
     * Creates a new instance of the class.
     * <p>
     * Creates a new worksheet with no frame title.<br>
     * The new worksheet will be the active worksheet by default.
     */
    public SOCGraphFrame() {
	this("");
    }
    /**
     * Creates a new instance of the class.
     * <p>
     * Creates a new worksheet with optional title.<br>
     * The new worksheet will be the active worksheet by default.
     *
     * @param title the frame title
     */
    public SOCGraphFrame(String title) {
	super(title, true, true, true, true);
	setSize(800, 600);				
	setLocation(50,50);
	setFrameIcon(new ImageIcon(Toolkit.getDefaultToolkit().
		     getImage(this.getClass().
			      getResource("/icons/processeditor/icon.gif"))));

	graph.getSelection().addSelectionListener(this);
	graphScroll = new GraphScrollPane(graph);	
	getContentPane().add(graphScroll, BorderLayout.CENTER);	
	setVisible(true);	
    }                   
    /*
    public SOCGraphFrame(String title, String function) {
	this(title);       
	GraphCell cell = new NestedCell(function);
	graph.insert(cell,0);
	cell.setPos(new Point(INSERT_MARGIN_X, INSERT_MARGIN_Y));	
	graph.updateLargePreferredSize();
    }
    **/
    /**
     * Creates a new instance of the class.
     * <p>
     * Creates a new worksheet with optional title,
     * where the enclosed object is inserted to the worksheet.<br>
     * The new worksheet will be the active worksheet by default.
     *
     * @param title the frame title
     * @param o object that should be opended together with the worksheet
     */
    public SOCGraphFrame(String title, Object o) {
	this(title);
	insertResource(o, null);
    }
    /**
     * Creates a new instance of the class.
     * <p>
     * Creates a new worksheet with optional title,
     * where the enclosed object is inserted to the worksheet.
     * A file or URL path can be chosen to be associated with the object.
     * 
     * @param title the frame title
     * @param o object that should be opended togeterh with the worksheet
     * @param file URL associated with the object
     */
    public SOCGraphFrame(String title, Object o, File file) {
	this(title);
	//DEBUG
	//System.out.println("SOCGraphFrame()");
	//END DEBUG
	insertResource(o, file);	       
    }    
    public void addSOCGraphFrameListener(SOCGraphFrameListener l) {
	myListener = l;
    }
    /**
     * Returns the graph of the worksheet.
     *
     * @return the graph of the worksheet
     */
    public Graph getGraph() {
	return graph;
    }
    /**
     * Sets the multi mode on or off.
     * <p>
     * If the graphical representation of the relations, i.e. the light blue
     * rectangles enclosing one or more operation and/or other relation 
     * components, is unwanted they can be removed by turning 
     * the mulit mode off.
     *
     * @param set true if multi mode should be on, false otherwise
     */
    public void setMultiModeView(boolean set) {	
	for(int i = 0; i < graph.cells.length; i++) {
	    if(graph.cells[i] instanceof NestedCell) {
		((NestedCell)graph.cells[i]).setMultiModeView(set);
	    }
	}		
	graph.getSelection().removeAll();
	graph.repaint();
	multiMode = set;
    }
    /**
     * Returns if the multi mode is on or off.
     *
     * @return if true the multi mode is on, false otherwise
     */
    public boolean isMultiModeView() {
	return multiMode;	
    }
    /**
     * Is invoked each time the selection is changed.
     *
     * @param s the <code>Selection</code> object
     */
    public void selectionChanged(Selection s) {
	//DEBUG
	//System.out.println("SOCGraphFrame.selectionChanged()");
	//END DEBUG
	if(myListener != null) {
	    myListener.selectionChanged(s);
	}
    }    
    /**
     * Adds a new resource to the worksheet.
     */
    public void newResource() {
	//DEBUG
	System.out.println("SOCGraphFrame.newResource()");
	//END DEBUG
	//ROPType newROP = new ROPImpl();
	try {
	    ROP newROP = objectFactory.createROP();       
	    newROP.setType(ROPType.COP);
	    newROP.setId(Integer.toString(++numOfNewResourcesToDay));	
	    newROP.setMachine("Machine");	
	    GraphCell newResource = new ResourceCell(newROP);
	    graph.insert(newResource, 0);
	    Point p = graphScroll.getCornerValue();
	    p.translate(INSERT_MARGIN_X, INSERT_MARGIN_Y);
	    newResource.setPos(p);
	}catch(Exception ex) {
	    //DEBUG
	    System.out.println("ERROR! in SOCGraphFrame.newResource()");
	    //END DEBUG
	}	           
	
    }
    /**
     * Adds a new operation to selected resource.
     * <p>     
     * If no object is selected the operation will be added to the worksheet. 
     */
    public void newOperation() {
	GraphCell newOperation = new NestedCell("Op "+(++numOfNewCellsToDay)); //, true);
	if((getGraph().getSelection().getSelectedCount() == 1) &&
	   (getGraph().getSelection().getSelectedAt(0) instanceof NestedCellListener)) {
	    ((NestedCellListener)getGraph().getSelection().getSelectedAt(0)).paste(newOperation);
	    getGraph().getSelection().removeAll();
	}else {	
	    graph.insert(newOperation, 0);
	    Point p = graphScroll.getCornerValue();
	    p.translate(INSERT_MARGIN_X, INSERT_MARGIN_Y);
	    newOperation.setPos(p);
	    graph.updateLargePreferredSize();
	}
    }
    
    public void newInterLock() {
    	GraphCell newInterLock = new InterLockCell();
    	graph.insert(newInterLock, 0);
    	Point p = graphScroll.getCornerValue();
    	p.translate(INSERT_MARGIN_X, INSERT_MARGIN_Y);
    	newInterLock.setPos(p);
    	graph.updateLargePreferredSize();
    }
    
    public void newExecutionOfOperation() {
    	GraphCell newExecutionOfOperation = new ExecutionOfOperationCell();
    	graph.insert(newExecutionOfOperation, 0);
    	Point p = graphScroll.getCornerValue();
    	p.translate(INSERT_MARGIN_X, INSERT_MARGIN_Y);
    	newExecutionOfOperation.setPos(p);
    	graph.updateLargePreferredSize();
    }
    
    /**
     * Adds a new relation to selected resource.
     * <p>
     * If no object is selected the relation will be placed on the worksheet.
     *
     * @param relationType can either be "Sequence", "Alternative",
     * "parallel" or "Arbitrary".
     */
    public void newRelation(RelationType relationType) {
    	
    		ObjectFactory factory = new ObjectFactory();
    		Relation newComplexFunction = factory.createRelation();
    		newComplexFunction.setType(relationType);
    		Activity newOperation1 = factory.createActivity();
    		Activity newOperation2 = factory.createActivity();
    		
    		newOperation1.setOperation("Op "+(++numOfNewCellsToDay));
    		newOperation2.setOperation("Op "+(++numOfNewCellsToDay));
    		
    		newComplexFunction.getActivityRelationGroup().add(newOperation1);
    		newComplexFunction.getActivityRelationGroup().add(newOperation2);
    		
    		GraphCell newRelation = new NestedCell(newComplexFunction);
    		if((getGraph().getSelection().getSelectedCount() == 1)  &&
    		   (getGraph().getSelection().getSelectedAt(0) instanceof NestedCellListener)) 
    		{
    			((NestedCellListener)getGraph().getSelection().getSelectedAt(0)).paste(newRelation);
    			getGraph().getSelection().removeAll();
    		}else {
    			graph.insert(newRelation, 0);
    			Point p = graphScroll.getCornerValue();
    			p.translate(INSERT_MARGIN_X, INSERT_MARGIN_Y);
    			newRelation.setPos(p);
    			graph.updateLargePreferredSize();
    		}
    	
    }
    
    /**
     * Adds a new relation to the selected resource.
     * <p>
     * If no object is selected the relation will be placed on the worksheet.
     * The relation will be defined by the user input to 
     * the dialog window shown.
     *
     * @param algebraic the algebraic expression representing the relation
     */
    public void newAlgebraic(String algebraic) {
	//DEBUG
	///System.out.println("SOCGraphFrame.newAlgebraic()");
	//END DEBUG
	if(algebraic != null) {	   
	    Object complexFunction = Converter.convertStringToActivityRelation(algebraic);
	    GraphCell newOperation = new NestedCell(complexFunction);
	    if((getGraph().getSelection().getSelectedCount() == 1) &&
	       (getGraph().getSelection().getSelectedAt(0) instanceof NestedCellListener)) {
		((NestedCellListener)getGraph().getSelection().getSelectedAt(0)).paste(newOperation);
		getGraph().getSelection().removeAll();
	    }else {	
		graph.insert(newOperation, 0);
		Point p = graphScroll.getCornerValue();
		p.translate(INSERT_MARGIN_X, INSERT_MARGIN_Y);
		newOperation.setPos(p);
		graph.updateLargePreferredSize();
	    }
	}
    }    
    /**
     * Inserts a new resource to the worksheet.
     *
     * @param o the object that is to be inserted
     * @param file URL associated with the object      
     */
    public void insertResource(Object o, File file) {
    	try{
    		ResourceCell cell = null;
    		
    		if(o instanceof ROP) {
    			cell = new ResourceCell((ROP)o);
    		}else if(o instanceof EOP){
    			cell = new ExecutionOfOperationCell((EOP)o);
    		}else if(o instanceof IL){
    			cell = new InterLockCell((IL)o);
    		}
    			
    		cell.setFile(file);
    		graph.insert(cell,0);
    		cell.setPos(new Point(INSERT_MARGIN_X, INSERT_MARGIN_Y));      
    		graph.updateLargePreferredSize();		
    	}catch(Exception ex) {
    		if(ex instanceof NullPointerException) {
    			System.err.println("EMPTY resource");
    		}else {
    			System.err.println("ERROR! while building resource "+
				       "in SOCGraphFrame() from " + o.toString());
    		}
    	}
    }    
    /**
     * Pastes the copied object into the selected object.
     *
     * @param o the object to paste
     */
    public void paste(Object o) {	
    	//DEBUG
    	//System.out.println("SOCGraphFrame.paste()");
    	//END DEBUG
    	if(o instanceof NestedCell) {
    		GraphCell clone = ((NestedCell)o).copy();
    		graph.insert(clone,0);	    	    
    		Point p = graphScroll.getCornerValue();
    		p.translate(INSERT_MARGIN_X, INSERT_MARGIN_Y);
    		clone.setPos(p);
    		graph.updateLargePreferredSize();
    		//graph.validate();
    	}
    }
    /**
     * Maximizes and restores the worksheet.
     *
     * @param b a boolean, where true maximizes the worksheet and 
     * false restores it
     */
    public void setMaximum(boolean b) {
	try {
	    super.setMaximum(b);	    
	}catch(Exception ex) {
	    System.out.println("ERROR! in SOCGraphFrame."+
			       "setMaximum(boolean b)");    
	}
    }
    /**
     * Moves the worksheet infront of all other worksheet. 
     */
    public void moveToFront() {
	try {
	    super.moveToFront();
	}catch(Exception ex) {
	    System.out.println("ERROR! in SOCGraphFrame."+
			       "moveToFront");
	}
    }
    /**
     * Iconifies or de-iconifies the worksheet.
     *
     * @param b a boolean, where true means to iconify the worksheet and 
     * false means to de-iconify it
     */
    public void setIcon(boolean b) {
	try {
	    super.setIcon(b);
	}catch(Exception ex) {
	    System.out.println("ERROR! in SOCGraphFrame."+
			       "setIcon");
	}
    }
    /**
     * Selects or deselects the worksheet if it's showing.
     *
     * @param b a boolean, where true means the worksheet should become
     * selected (currently active) and false means it should become deselected
     */
    public void setSelected(boolean b) {
	try {
	    super.setSelected(b);
	}catch(Exception ex) {
	    System.out.println("ERROR! in SOCGraphFrame."+
			       "setSelected");
	    System.out.println(ex);
	}
    }     
    /*
    public void defaultRestore() {
	setLocation(defaultLocation);
    } 
    **/      
}
