package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph;

/*
*	Listener interface
*
*	David Millares 2007-02-18 
*/

import org.supremica.external.processeditor.xgraph.CellEvent;

public interface InternalGraphListener {   
    
    public void rebuild();

    public void sizeChanged(InternalGraph g);
    
    public void cellClicked(InternalGraph g, CellEvent c);
    
    public void cellRemoved();
}
