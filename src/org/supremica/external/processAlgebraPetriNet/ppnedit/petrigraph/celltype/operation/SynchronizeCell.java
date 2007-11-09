package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation;

/*
*	SynchronizeCell is a cell that syncronize
*	first and last event in its internall cells
*
*	David Millares 2007-02-16 
*/

/*
 * To Do:	EVERYTHING !!!
 * 			This class is not finished
 *		
 *
 */

import javax.swing.*;
import javax.swing.border.*;

import org.supremica.external.processAlgebraPetriNet.ppnedit.converter.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph.InternalGraph;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn.PPN;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn.PetriPro;
import org.supremica.external.processeditor.xgraph.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

//Linecell
//  |___ OpePetriCell
//              |__ GraphCell
//                      |__ JPanel
public class SynchronizeCell
					extends OpePetriCell {
    
    //constructor
    public SynchronizeCell(BaseCell[] cells) {	
        
		this.cells = cells;
		
		synchronize();
        updateExp();
        pack();
        verticalLine();
        setCompressed(true);
    }
    
    private void synchronize(){
        
		boolean noOperands = true;
        String tmp = "";
	
		//check indata
        if(cells == null || cells.length == 0){
            return;
        }
		cleanGraph();
       
	   	//search for sequence
        for(int i = 0; i < cells.length; i++){
	    
            tmp = PPN.toInternalExp(cells[i].getExp());
	    
            if(tmp.contains(PPN.SEQUENCE)){
                noOperands = false;
                break;
            }
        }
        
		//base case no sequence
        if(noOperands){
            BaseCell[] ts = new Transition[1];
            ts[0] = new Transition();
            
            String exp = "";
            for(int i = 0; i < (cells.length-1); i++){
                exp = exp + cells[i].getExp() + PPN.SYNCHRONIZE;
            }
            exp = exp + cells[cells.length-1].getExp();
            
			exp = PPN.toHumanExp(exp);
			
            ts[0].setExp(exp);
            setCompressedExp(exp);
            
            insert(ts[0]);
            addTargetCell(ts[0]);
            addSourceCell(ts[0]);
            
            return;
        } 
       
        //We have operations
        //we should sync first and last operation
        
        //make start event
        BaseCell start_event = new Transition();
        start_event.setExp("Start");
        addTarget(start_event);

        //sync first event
        BaseCell first_sync = new Transition();
        first_sync.setExp(getFirstEventSync(cells));
        
        //sync last event
        BaseCell last_sync = new Transition();
        last_sync.setExp(getLastEventSync(cells));

        //make last event
        BaseCell last_event = new Transition();
        last_event.setExp("End");
        addSource(last_event);

        //insert
        insert(start_event);
        for(int i = 0; i < cells.length; i++){
            BaseCell place = new Place();
      
            place.addSourceCell(start_event);
            place.addTargetCell(first_sync);

            insert(place);
        }
        insert(first_sync);

		//*****************************************
		//Make the rest!!!!!!!!!!!!!!!!!!!
		//*****************************************
		String rest = "";
		BaseCell cell = null;
		for(int i = 0; i < cells.length; i++){
	  
	  		rest = PPN.toInternalExp(cells[i].getExp());
	  		rest = PPN.removeFirstAndLastEvent(rest);
	
	  		if(rest.length() == 0){
	    		//no rest
	    		cell = new Place();
	    
	    		cell.addSourceCell(first_sync);
	    		cell.addTargetCell(last_sync);
	    
	    		insert(cell);
	    
	  		}else{
	  
	    		PetriPro pro = new PetriPro();
	    		pro.setExp(rest);
	    		cell = Converter.createBaseCell(pro.getROP());
	    
	    		BaseCell pstart = new Place();
	    		BaseCell pend = new Place();
	    	    
	    		pstart.addSourceCell(first_sync);
	    		pstart.addTargetCell(cell);

	    		pend.addSourceCell(cell);
	    		pend.addTargetCell(last_sync);
	    
	    		insert(pstart);
	    		insert(cell);
	    		insert(pend);
	  		}
        }
	
		//insert
		insert(last_sync);
		for(int i = 0; i < cells.length; i++){
        	BaseCell place = new Place();
	  
	  		place.addSourceCell(last_sync);
	  		place.addTargetCell(last_event);
	  
	  		insert(place);
        }
        insert(last_event);
    }
	protected void rebuildOpePetricell(){
		synchronize();
	}
    
    private String getFirstEventSync(BaseCell[] cells){
    
    	String firstEventSync = "";
      	String[] events = getFirstEvents(cells);
      
      	for(int i = 0; i < (events.length-1); i++){
        
			if(!PPN.containsNoOperations(events[i])){
        		events[i] = PPN.START + events[i] + PPN.END; 
        	}
	
        	firstEventSync = firstEventSync + events[i] + PPN.SYNCHRONIZE;
      	}//end for
      
      	if(!PPN.containsNoOperations(events[events.length-1])){
        	events[events.length-1] = PPN.START + 
	  	      		    			events[events.length-1] +
		      		    			PPN.END; 
      	}
      
      	firstEventSync = firstEventSync + events[events.length-1];
      	firstEventSync = PPN.toHumanExp(firstEventSync);
		
      	return firstEventSync;
	
    }
    
    private String[] getFirstEvents(BaseCell[] cells){
    
    	String[] events;
      
      	//chech indata
      	if(cells == null || cells.length == 0){
        	return null;
      	}
      
      	events = new String[cells.length];
      
      	for(int i = 0; i < cells.length; i++){
        	events[i] = PPN.toInternalExp(cells[i].getExp());
			events[i] = PPN.getFirstEvent(events[i]);
      	}
      
      	return events;
    }
  
  	//must do this function
    private String getLastEventSync(BaseCell[] cells){
		String lastEventSync = "";
      
      	String[] firstEvents = getFirstEvents(cells);
      	String[] lastEvents = getLastEvents(cells);
      
      	for(int i = 0; i < (lastEvents.length-1); i++){
        
			if(lastEvents[i].equals(firstEvents[i])){
				;//do nothing
			}else{
	  			if(!PPN.containsNoOperations(lastEvents[i])){
            		lastEvents[i] = PPN.START + 
	  	        	    				lastEvents[i] +
		            					PPN.END; 
          		}
				lastEventSync = lastEventSync + lastEvents[i] + PPN.SYNCHRONIZE;
			}
      	}//end for
      
      	if(!PPN.containsNoOperations(lastEvents[lastEvents.length-1])){
        	lastEvents[lastEvents.length-1] = PPN.START + 
	  	      		    	    lastEvents[lastEvents.length-1] +
		      		            PPN.END; 
      	}
      
      	lastEventSync = lastEventSync + lastEvents[lastEvents.length-1];
      	lastEventSync = PPN.toHumanExp(lastEventSync);
		
      	return lastEventSync;
    }
	
    private String[] getLastEvents(BaseCell[] cells){
    
    	String[] events;
      
      	//chech indata
      	if(cells == null || cells.length == 0){
        	return null;
      	}
      
      	events = new String[cells.length];
      
      	for(int i = 0; i < cells.length; i++){
        	events[i] = PPN.toInternalExp(cells[i].getExp());
			events[i] = PPN.getLastEvent(events[i]);
      	}
      
      	return events;
    }
    
    public Point[] getPoints(int anchor){
        Point myPos = super.getPos();
        
        if(compressed){
            Point[] tmp = new Point[1];
            tmp[0] = getCompressedPos(anchor);
            
            if(anchor == GraphCell.UPPER_CENTER){
                tmp[0].setLocation(tmp[0].x,0);
            } else if(anchor == GraphCell.LOWER_CENTER){
                tmp[0].setLocation(tmp[0].x,this.getHeight());
            }
            tmp[0].translate(myPos.x, myPos.y);
            return tmp;
        }
        
		InternalGraph graph = getGraph();
		
        //I use that in a SynchronizationCell only one target and one source exist
        Point[] tmp = null;
        if(anchor == GraphCell.UPPER_CENTER){
            BaseCell[] target = graph.getTargetCells();
            tmp = target[0].getPoints(anchor);
        }else if(anchor == GraphCell.LOWER_CENTER){
            BaseCell[] source = graph.getSourceCells();
            tmp = source[0].getPoints(anchor);
        }
        
        if(tmp != null){
            for(int i = 0; i < tmp.length; i++){
                tmp[i].translate(myPos.x,myPos.y);
            }
        }
        return tmp;
    }
    
    private void updateExp(){
		exp = "";
        if(cells != null){
			exp = PPN.SYNCHRONIZE + "{";
            for(int i=0; i < (cells.length-1); i++){
                  exp = exp + cells[i].getExp() + ", ";
            }
			exp = exp + cells[cells.length-1].getExp() + "}";
        }
        setCompressedExp(exp);
    }
    
    public String getExp(){
        updateExp();
        return super.getExp();
    }
    public void setCompressed(boolean set) {	
        updateExp();
        super.setCompressed(set);
    }
	
	//----- Override EditableCell -----------
	/**
	*	
    *	makes copy() on this cell to work.
	*
    */
	protected BaseCell clone(){
		BaseCell[] newCells = new EditableCell[cells.length];
		for(int i = 0; i < newCells.length; i++){
			newCells[i] = (BaseCell)((EditableCell)cells[i]).copy();
		} 
		return new SynchronizeCell(newCells);
	}
    
    //---------- Listeners ovveride ------------------
    public void cellClicked(InternalGraph g, CellEvent cEvent){
        if(l != null){
            l.cellClicked(g,cEvent);
        }
    }
	
	//--------------overide EditableCellListener ----
	/**
	*
    *	Override paste in OpPetriCell
    *	add one cell and Synchronize
    *	
    */
	public void paste(BaseCell cell){
		
		//check indata
		if(cell == null){
			return;
		}
		
		addCell(cell);
		synchronize();
		updateExp();
	}
	//--------------End override EditableCellListener ----
}
