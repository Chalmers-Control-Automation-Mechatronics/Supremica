package org.supremica.external.processAlgebraPetriNet.ppnedit.gui;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;

import org.supremica.external.processAlgebraPetriNet.ppnedit.converter.Converter;
import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.gui.*;

import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph.*;

import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import org.supremica.external.processeditor.xgraph.*;
import org.supremica.external.processeditor.xml.Loader;
import org.supremica.external.processeditor.SOCFileFilter;


class EventAction extends 
					ContainerGUI 
							implements GraphFrameListener,
                                       InternalFrameListener,
                                       ActionListener {
	
    public EventAction() {
        super();
    }
    
    public void setMultiModeView(boolean set) {
		//DEBUG
		//System.out.println("GraphContainer.setMultiModeView()");
		//END DEBUG
		getSelectedFrame().setMultiModeView(set);
    }
    
    public Component add(PNetGraphFrame newFrame) {
	    if(newFrame.getTitle().equals("Sheet")) {
	        newFrame.setTitle("Sheet "+(++numOfNewSheetToDay));
		}else {
	        setTitleUnique(newFrame);
	    }
		
	    JRadioButtonMenuItem jrbmi = new JRadioButtonMenuItem(newFrame.getTitle());
	    jrbmi.addActionListener(this);
	    jmWindows.add(jrbmi);
	    bgWindow.add(jrbmi);
	    jrbmi.setSelected(true);       
	    
		super.add(newFrame);
	    
		newFrame.setMaximum(true);
	    newFrame.moveToFront();
	    
		newFrame.addInternalFrameListener(this);
	    newFrame.addGraphFrameListener(this);
	
	    return newFrame;
    }
    
    public void setTitleUnique(JInternalFrame frame) {
	    int numOfEquals = setTitleUnique(frame.getTitle(), 0);
	    if(numOfEquals != 0) {
	        frame.setTitle(frame.getTitle()+" ("+numOfEquals+")");
	    
	    }
    }
    
    private int setTitleUnique(String title, int numOfIteration) {
		String newTitle;
		if(numOfIteration != 0) {
	    	newTitle = title+" ("+numOfIteration+"(";
		}else {
	    	newTitle = title;
		}
		JInternalFrame[] frames = getAllFrames();	
		for(int i = 0; i < frames.length; i++) {
	    	if(newTitle.equals(frames[i].getTitle())) {
				return setTitleUnique(title, ++numOfIteration);
	    	}
		}
		return numOfIteration;
    }
	
	private BaseCell getSelectedCell(){
		GraphCell[] tmp = getSelectedGraph().getSelection().getSelected();
		if(tmp == null || tmp.length != 1){
			return null;
		}
		
		if(tmp[0] instanceof BaseCell){
			return (BaseCell)tmp[0];
		}
		
		return null;
	}
	
	private BaseGraph getSelectedGraph(){
		if( getSelectedFrame() != null){
			return (BaseGraph)getSelectedFrame().getGraph();
		}
		
		return null;
	}
    
	public void selectionChanged(Selection s) {
	
		//debugg
		//System.out.println("selection changed");
		//debugg
		
		if(s.getSelectedCount() == 0) {
	    	jmiCut.setEnabled(false);
	    	jmiCopy.setEnabled(false);
	    	
			if(getSelectedGraph() != null){
				jmiPaste.setEnabled(getSelectedGraph().
				                    getStoredCells() != null);
			}
			
	    	jmiDelete.setEnabled(false);
	     	jmRelationType.setEnabled(false);
			//jmiSave.setEnabled(false);
		}else if(s.getSelectedCount() == 1) {
	    	if(getSelectedFrame() != null) {
				if(getSelectedCell() instanceof RopCell) {
		    		jmiSaveROP.setEnabled(true);
				}
	    	}
	    	jmiCut.setEnabled(true);
	    	jmiCopy.setEnabled(true);
	    	jmiPaste.setEnabled(true);
	    	jmiDelete.setEnabled(true);
		}else if(s.getSelectedCount() > 1){
			jmiSaveROP.setEnabled(false);
			jmiCut.setEnabled(true);
	    	jmiCopy.setEnabled(true);
	    	jmiPaste.setEnabled(true);
	    	jmiDelete.setEnabled(true);
		}else {
			jmiSaveROP.setEnabled(false);
	    	jmiCut.setEnabled(false);
	    	jmiCopy.setEnabled(false);
	    	jmiPaste.setEnabled(false);
	    	jmiDelete.setEnabled(false);
	    	jmRelationType.setEnabled(false);
		}
    }
    
    public void open() {
		String map = System.getProperty("user.dir");
		JFileChooser fc = new JFileChooser(map);
		SOCFileFilter myFilter = new SOCFileFilter(".xml");
		fc.addChoosableFileFilter(myFilter);
		fc.setFileFilter(myFilter);
		fc.setAcceptAllFileFilterUsed(false);
		int result = fc.showOpenDialog(this);
		
		if(result == JFileChooser.APPROVE_OPTION) {
            
	    	//debugg
            //System.out.println(fc.getSelectedFile());
	    	//debugg
            
            Loader loader = new Loader();	   
	    	Object newObject = loader.open(fc.getSelectedFile());
	    	if(newObject instanceof Worksheet){
	    		PNetGraphFrame newFrame = new PNetGraphFrame(fc.getSelectedFile().getName(), newObject); 
	    		add(newFrame);
	    		newFrame.setMaximum(true);
	    	}else if(newObject instanceof ROP){
	    		
	    		if(getSelectedGraph() != null){
	    			BaseCell cell = Converter.createBaseCell((ROP)newObject);
	    			getSelectedGraph().insert(cell);
	    			getSelectedGraph().repaint();
	    		}else{
	    			PNetGraphFrame newFrame = new PNetGraphFrame(fc.getSelectedFile().getName(), newObject); 
	    			add(newFrame);
	    			newFrame.setMaximum(true);
	    		}
	    	}else{
	    		System.err.println("Unknown object " +
	    							newObject.toString() + 
	    							" in EventAction.open()");
	    	}
            
            System.setProperty("user.dir",
                               fc.getSelectedFile().getAbsolutePath());
	    
		}else if(result == JFileChooser.CANCEL_OPTION) {
	    		;
		}else if(result == JFileChooser.ERROR_OPTION) {
	    	;
		}       
    }
    
    public void close() {
		try {
	    	getSelectedFrame().setClosed(true);
		}catch(Exception ex) {
	    	System.out.println("ERROR! in GraphContainer."+
			       "closeSelectedFrame()");
		}
    }
    
    public void save() {
		if(getSelectedFrame() != null) {   
	    	if(getSelectedGraph() instanceof PetriGraph){
	    		
				String map = System.getProperty("user.dir");
				JFileChooser fc = new JFileChooser(map);
				SOCFileFilter myFilter = new SOCFileFilter(".xml");		
				fc.addChoosableFileFilter(myFilter);		
				fc.setAcceptAllFileFilterUsed(true);
				fc.setFileFilter(myFilter);
				int result = fc.showSaveDialog(this);
			
				if(result == JFileChooser.APPROVE_OPTION) {
		    		
					File file = fc.getSelectedFile();
		    		if((!file.getName().endsWith(".xml"))&&
		       			fc.isFileSelectionEnabled()) {		       
						file = new File(file.getName()+".xml");	  
		    		}
						    
		    		Loader loader = new Loader();		    
		    		loader.save(((PetriGraph)getSelectedGraph()).toWorksheet(), file);    
		    		
					System.out.println(file.getName());
					
				}else if(result == JFileChooser.CANCEL_OPTION) {
		    		//DEBUG
		    		System.out.println("    -->CANCEL");
		    		//END DEBUG
				}else if(result == JFileChooser.ERROR_OPTION) {
		    		//DEBUG
		    		System.out.println("    -->ERROR! while open file");
		    		//END DEBUG
				}			    	
			}
		}
    }
    
    public void saveROP() {
		if(getSelectedFrame() != null) {   
	    	if(getSelectedCell() instanceof RopCell){	
				
				RopCell ropCell = (RopCell)getSelectedCell();
				
				String map = System.getProperty("user.dir");
				JFileChooser fc = new JFileChooser(map);
				SOCFileFilter myFilter = new SOCFileFilter(".xml");		
				fc.addChoosableFileFilter(myFilter);		
				fc.setAcceptAllFileFilterUsed(true);
				fc.setFileFilter(myFilter);
				int result = fc.showSaveDialog(this);
			
				if(result == JFileChooser.APPROVE_OPTION) {
		    		
					File file = fc.getSelectedFile();
		    		if((!file.getName().endsWith(".xml"))&&
		       			fc.isFileSelectionEnabled()) {		       
						file = new File(file.getName()+".xml");	  
		    		}
						    
		    		Loader loader = new Loader();		    
		    		loader.save(ropCell.getROP(), file);
					
				}else if(result == JFileChooser.CANCEL_OPTION) {
		    		//DEBUG
		    		//System.out.println("    -->CANCEL");
		    		//END DEBUG
				}else if(result == JFileChooser.ERROR_OPTION) {
		    		//DEBUG
		    		//System.out.println("    -->ERROR! while open file");
		    		//END DEBUG
				}			    	
	    	}
		}
    }
    
    public void newResource() {
		if(getSelectedFrame() != null) {
	    	getSelectedFrame().newResource();
		}	
    }
    
	public void newOperation() {
		if(getSelectedFrame() != null) {
	    	getSelectedFrame().newOperation();
		}    
    }

    public void insertResource() {	
		String map = System.getProperty("user.dir");
		JFileChooser fc = new JFileChooser(map);
		SOCFileFilter myFilter = new SOCFileFilter(".xml");
		fc.addChoosableFileFilter(myFilter);
		fc.setFileFilter(myFilter);
		fc.setAcceptAllFileFilterUsed(false);
		int result = fc.showOpenDialog(this);
		
		if(result == JFileChooser.APPROVE_OPTION) {
	    	System.out.println(fc.getSelectedFile());
	    	Loader loader = new Loader();	   
	    	Object newObject = loader.open(fc.getSelectedFile());
	    	
			if(getSelectedFrame() != null) {
				getSelectedFrame().insertResource(newObject);
	    	}else {
				add(new PNetGraphFrame(fc.getSelectedFile().getName(), newObject)); 	    
	    	}
		}else if(result == JFileChooser.CANCEL_OPTION) {
	    	;
		}else if(result == JFileChooser.ERROR_OPTION) {
	    
		}   	
    }
	
    public void cut() {
		copy();
		delete();
    }
    
	public void copy() {
		if(getSelectedFrame() != null) {
			getSelectedGraph().storeSelectedCells();
		}
    }
    
    public void paste() {
		if(getSelectedGraph() != null) {
			getSelectedGraph().pasteStoredCells();
		}	
    }
    
    public void delete() {	
		if(getSelectedGraph() != null) {
			getSelectedGraph().deleteSelectedCells();
		}
    }
    
    public void changeRelationType(String type) {
	if(getSelectedFrame() != null) {
            /*
	    if(getSelectedFrame().getGraph().getSelection().getSelectedAt(0)
	       instanceof NestedCell) {
		if(((NestedCell)getSelectedFrame().getGraph().getSelection().getSelectedAt(0)).complexFunction instanceof RelationType) {
		    ((RelationType)((NestedCell)getSelectedFrame().getGraph().getSelection().getSelectedAt(0)).complexFunction).setType(type);
		    ((NestedCell)getSelectedFrame().getGraph().getSelection().getSelectedAt(0)).rebuild();
		    getSelectedFrame().getGraph().getSelection().removeAll();
		}
	    }
            */
	}	
    }
    public void autoPositioning() {
	
    }
    
    //OVERRIDED METHODS OR COMPLIMENTED METHODS
    public PNetGraphFrame getSelectedFrame() {	
	    return (PNetGraphFrame)super.getSelectedFrame();	
    }
    public int getFrameCount() {
		return getAllFrames().length;
    }
	
    public Graph[] getAllGraphs() {
		JInternalFrame[] frames = getAllFrames();
		Graph[] graphs = new Graph[frames.length];
		for(int i = 0; i < frames.length; i++) {
	    	graphs[i] = ((PNetGraphFrame)frames[i]).getGraph();
		}
		return graphs;
    } 
  
    public void organizeAll() {
		JInternalFrame[] frames = getAllFrames();
		int stepX = 20; int stepY = 29;
		int tmpLocX = stepX*(frames.length-1); 
		int tmpLocY = stepY*(frames.length-1);		
		for(int i = 0; i < frames.length; i++) {
	    	System.out.println(frames[i].getTitle());
	    	((PNetGraphFrame)frames[i]).setMaximum(false);
	    	frames[i].moveToBack();
	    	frames[i].setBounds(new Rectangle(tmpLocX, tmpLocY,
					      					  getBounds().width,
					      					  getBounds().height));
	    	tmpLocX -= stepX;
	    	tmpLocY -= stepY;	  
		}
		((PNetGraphFrame)frames[0]).setSelected(true);
    }      
    
    //ACTION_LISTENER METHODS
    public void actionPerformed(ActionEvent e) {
		if("New".equals(e.getActionCommand())) {
	    	add(new PNetGraphFrame("Sheet"));
		}else if("Open...".equals(e.getActionCommand())) {
	    	open();
		}else if("Close".equals(e.getActionCommand())) {
	    	close();
		}else if("Save...".equals(e.getActionCommand())) {	    
	    	save();
		}else if("Save ROP".equals(e.getActionCommand())) {	    
	    	saveROP();
	    }else if("Exit".equals(e.getActionCommand())) {
	    	System.exit(0);
		}else if("New Resource".equals(e.getActionCommand())) {
	    	newResource();
		}else if("New Operation".equals(e.getActionCommand())) { 
	    	newOperation();
		}else if("Insert Resource...".equals(e.getActionCommand())) {
	    	insertResource();
		}else if("Cut".equals(e.getActionCommand())) {
	    	cut();
		}else if("Copy".equals(e.getActionCommand())) {
	    	copy();
		}else if("Paste".equals(e.getActionCommand())) {
	    	paste();
		}else if("Delete".equals(e.getActionCommand())) {
	    	delete();
		}else if("Sequence".equals(e.getActionCommand())) {
	    	changeRelationType("Sequence");
		}else if("Alternative".equals(e.getActionCommand())) {
	    	changeRelationType("Alternative");
		}else if("Parallel".equals(e.getActionCommand())) {
	    	changeRelationType("Parallel");
		}else if("Auto Positioning".equals(e.getActionCommand())) {
	    	autoPositioning();
		}else if("Multi Mode".equals(e.getActionCommand())) {
	    	setMultiModeView(((JCheckBoxMenuItem)e.getSource()).getState());  
		}else if("Organize All".equals(e.getActionCommand())) {
	    	organizeAll();
		}else{
	    	JInternalFrame[] frames = getAllFrames();
	    	for(int i = 0; i < frames.length; i++) {	    
				if(frames[i].getTitle().equals(e.getActionCommand())) {	    
		    		//frames[i].moveToFront();
		    		//((PNetGraphFrame)frames[i]).setIcon(false);
		    		((PNetGraphFrame)frames[i]).setMaximum(true);
		    		((PNetGraphFrame)frames[i]).setSelected(true);  
		    		break;
				}
	    	}
		}
    }
	
    //INTERNAL_FRAME_LISTENER METHODS
    public void internalFrameActivated(InternalFrameEvent e) {
	//DEBUG
	//System.out.println("GraphContainer.internalFrameActivated()");
	//END DEBUG	
	PNetGraphFrame frame = (PNetGraphFrame)e.getInternalFrame();
	for(int i = 2; i < jmWindows.getItemCount(); i++) {	    
	    if(jmWindows.getItem(i).getText().equals(e.getInternalFrame().getTitle())) {
                
		jmWindows.getItem(i).setSelected(true);
		jmiMultiMode.setState(frame.isMultiModeView());
		if(frame.getGraph().getSelection().getSelectedCount() == 0) {
		    jmiCut.setEnabled(false);
		    jmiCopy.setEnabled(false);
		    jmiPaste.setEnabled(true);
		    jmiDelete.setEnabled(true);
		    jmRelationType.setEnabled(false);
		/*
                }else if(frame.getGraph().getSelection().getSelectedCount() == 1) {
		    if(getSelectedFrame() != null) {
			if(getSelectedFrame().getGraph().getSelection().getSelectedAt(0)
			   instanceof ResourceCell) {
			    jmiSave.setEnabled(true);
			}else if(getSelectedFrame().getGraph().getSelection().getSelectedAt(0) 
				 instanceof NestedCell) {
			    if(((NestedCell)getSelectedFrame().getGraph().getSelection().getSelectedAt(0)).complexFunction instanceof RelationType) {
				jmRelationType.setEnabled(true);
				if(((RelationType)((NestedCell)getSelectedFrame().getGraph().getSelection().getSelectedAt(0)).complexFunction).getType().equals("Sequence")) {
				    jmRelationType.getItem(0).setSelected(true);
				}else if(((RelationType)((NestedCell)getSelectedFrame().getGraph().getSelection().getSelectedAt(0)).complexFunction).getType().equals("Alternative")) {
				    jmRelationType.getItem(1).setSelected(true);
				}else if(((RelationType)((NestedCell)getSelectedFrame().getGraph().getSelection().getSelectedAt(0)).complexFunction).getType().equals("Parallel")) {
				    jmRelationType.getItem(2).setSelected(true);
				}
			    }
			}
		    }
                    */
                    
		    jmiCut.setEnabled(true);
		    jmiCopy.setEnabled(true);
		    jmiPaste.setEnabled(true);
		    jmiDelete.setEnabled(true);
		}else {
		    jmiSave.setEnabled(false);
		    jmiCut.setEnabled(false);
		    jmiCopy.setEnabled(false);
		    jmiPaste.setEnabled(false);
		    jmiDelete.setEnabled(false);
		    jmRelationType.setEnabled(false);
		}
	    }
	}	
    }
    
    public void insert(BaseCell cell) {
		getSelectedFrame().getGraph().insert(cell);
		getSelectedFrame().getGraph().repaint();
    }
	
    public void internalFrameClosed(InternalFrameEvent e) {
		if(getFrameCount() == 0) {
	    	jmiMultiMode.setEnabled(false);
		}
    }
	
    public void internalFrameClosing(InternalFrameEvent e) {
		for(int i = 2; i < jmWindows.getItemCount(); i++) {  
	    	if(jmWindows.getItem(i).getText().
	       		equals(e.getInternalFrame().getTitle())) {
				jmWindows.remove(i);
	    	}
		}		
    }
    public void internalFrameDeactivated(InternalFrameEvent e) {
		//DEBUG
		//System.out.println("GraphContainer.internalFrameDeactivated()");
		//END DEBUG
    }
    public void internalFrameDeiconified(InternalFrameEvent e) {
		//DEBUG
		//System.out.println("GraphContainer.internalFrameDeiconified()");
		//END DEBUG
    }
    public void internalFrameIconified(InternalFrameEvent e) {
		//DEBUG
		//System.out.println("GraphContainer.internalFrameIconified()");
		//END DEBUG
    }
    public void internalFrameOpened(InternalFrameEvent e) {
		//DEBUG
		//System.out.println("GraphContainer.internalFrameOpened()");
		//END DEBUG	
    }
}
