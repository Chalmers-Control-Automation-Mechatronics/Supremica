package org.supremica.external.processAlgebraPetriNet.ppnedit.gui;

import javax.swing.*;

import org.supremica.external.processAlgebraPetriNet.ppnedit.converter.*;
import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn.*;
import org.supremica.external.processeditor.xgraph.*;

import java.awt.*;






public class PNetGraphFrame 
						extends JInternalFrame
                                    	implements SelectionListener {
    
    private Point defaultLocation = new Point(30, 30);
    private GraphScrollPane graphScroll;
    private BaseGraph graph = new PetriGraph();
    private boolean multiMode = false;

    private GraphFrameListener myListener;

    private final int INSERT_MARGIN_X = 50;
    private final int INSERT_MARGIN_Y = 50;
    private int numOfNewCellsToDay = 0;
	
  	//constructors
    public PNetGraphFrame() {
		this("New");
    }
    
	public PNetGraphFrame(String title) {
		super(title, true, true, true, true);
		setSize(600, 600);				

		graph.getSelection().addSelectionListener(this);
		graphScroll = new GraphScrollPane(graph);	
		getContentPane().add(graphScroll, BorderLayout.CENTER);	
		setVisible(true);	
    }
	       
    public PNetGraphFrame(String title, String function) {
		this(title);
        PetriPro pro = new PetriPro(title);
        pro.setExp(function);
		BaseCell cell = new PetriProCell(pro);
		graph.insert(cell,0);
		cell.setPos(new Point(INSERT_MARGIN_X, INSERT_MARGIN_Y));	
    }
	
    public PNetGraphFrame(String title, Object o) {
		this(title);
		
		if(o instanceof Worksheet){
			graph = new PetriGraph((Worksheet)o);
			graph.getSelection().addSelectionListener(this);
			graphScroll = new GraphScrollPane(graph);	
			getContentPane().add(graphScroll, BorderLayout.CENTER);	
			setVisible(true);
		}else{
			insertResource(o);	       
		}
    }
    
    public void addGraphFrameListener(GraphFrameListener l) {
		myListener = l;
    }
    
    public BaseGraph getGraph() {
		return graph;
    }
    
    public void setMultiModeView(boolean set) {
        /*	
		for(int i = 0; i < graph.cells.length; i++) {
	    	if(graph.cells[i] instanceof NestedCell) {
			((NestedCell)graph.cells[i]).setMultiModeView(set);
	    }
	}		
	graph.getSelection().removeAll();
	graph.repaint();
	multiMode = set;
        */
    }
    
    public boolean isMultiModeView() {
		return multiMode;	
    }
    
    public void selectionChanged(Selection s) {
		if(myListener != null) {
	    	myListener.selectionChanged(s);
		}
    }

    //add a cell to the graph    
    public void newResource() {
        //create and insert the cell
		BaseCell newResource = new Transition();
		paste(newResource);
    }
    
    public void newOperation() {
		BaseCell newResource = new Transition();
		paste(newResource);
    }
    
    public void insertResource(Object o) {
       	if(o instanceof ROP) {
	    	try{
				BaseCell cell = Converter.createBaseCell((ROP)o);
				graph.insert(cell,0);
				cell.setPos(new Point(INSERT_MARGIN_X, INSERT_MARGIN_Y));      
	    	}catch(Exception ex) {
				if(ex instanceof NullPointerException) {
		    		System.out.println("EMPTY ROP!");
				}else {
		    		System.out.println("ERROR! while building rop"+
					       "in PNetGraphFrame()");
				}
	    	}
		}
    }
	    
    public void paste(Object o) {
		if(o instanceof EditableCell) {
	    	BaseCell clone = ((EditableCell)o).copy();
	    	graph.storeCells(new BaseCell[]{clone});	    
	    	graph.pasteStoredCells();
		}
    }
    
    public void setMaximum(boolean b) {
		try {
	    	super.setMaximum(b);	    
		}catch(Exception ex) {
	    	System.out.println("ERROR! in PNetGraphFrame."+
			       "setMaximum(boolean b)");    
		}
    }
    
    //place this PNetGraphFrame infront of the others
    public void moveToFront() {
		try {
	    	super.moveToFront();
		}catch(Exception ex) {
	    	System.out.println("ERROR! in PNetGraphFrame."+
				       "moveToFront");
		}
    }
    
    //minimize this frame
    public void setIcon(boolean b) {
		try {
	    	super.setIcon(b);
		}catch(Exception ex) {
	    	System.out.println("ERROR! in PNetGraphFrame."+
			       "setIcon");
		}
    }
    
    //makes this frame selected
    public void setSelected(boolean b) {
		try {
	    	super.setSelected(b);
		}catch(Exception ex) {
	    	System.out.println("ERROR! in PNetGraphFrame."+
			       "setSelected");
	    	System.out.println(ex);
		}
    }
    
    //place this frame at default position
    public void defaultRestore() {
		setLocation(defaultLocation);
    }
}
