package org.supremica.external.processAlgebraPetriNet.ppnedit.gui;

import javax.swing.*;

import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph.*;

public class GraphContainer 
						extends EventAction{
    
    public GraphContainer() {	
		super();
    }   
    
    //OVERRIDED METHODS OR COMPLIMENTED METHODS
    public PNetGraphFrame getSelectedFrame() {
        return (PNetGraphFrame)super.getSelectedFrame();	
    }
    
	public int getFrameCount() {
	    return getAllFrames().length;
    }
	
    public BaseGraph[] getAllGraphs() {
		JInternalFrame[] frames = getAllFrames();
		BaseGraph[] graphs = new BaseGraph[frames.length];
		for(int i = 0; i < frames.length; i++) {
	    	graphs[i] = ((PNetGraphFrame)frames[i]).getGraph();
		}
		return graphs;
    } 
}
