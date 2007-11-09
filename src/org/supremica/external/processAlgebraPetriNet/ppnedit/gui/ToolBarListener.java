package org.supremica.external.processAlgebraPetriNet.ppnedit.gui;

import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.BaseCell;

public interface ToolBarListener{
     public void newSheet();
     
     public void openFile();
     
     public void saveFile();				
     
     public void newResource();
     
     public void newOperation();
     
     public void newEdge();
     
     public void delete();
     
     public void insert(BaseCell cell);
}

