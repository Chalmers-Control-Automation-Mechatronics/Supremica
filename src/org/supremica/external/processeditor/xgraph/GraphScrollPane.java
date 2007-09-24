package org.supremica.external.processeditor.xgraph;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Provides a scrollable view. 
 */
public class GraphScrollPane extends JScrollPane implements ChangeListener {

    public static final int MARGIN_RIGHT = 25;
    public static final int MARGIN_LEFT = 25;
    public static final int MARGIN_DOWN = 25;
    public static final int MARGIN_UP = 25;
    
    public static int speed = 5;
    //public boolean isChangeable = false;
    
    public int diffRight = 0;
    public int diffLeft = 0;
    public int diffUp = 0;
    public int diffDown = 0;

    /**
     * Creates a new instance of the class that displays the contents
     * of the specified component.
     * 
     * @param view the component to display 
     */
    public GraphScrollPane(Component view) {
	super(view);       
	getViewport().addChangeListener(this);
	getViewport().setOpaque(true);
	((Graph)view).addScrollListener(this);		
    }
    /*
    public void setChangeableSize(boolean b) {
	isChangeable = b;
    } 
    **/
    /**
     * Sets the scroll focus of this graph scroll pane
     *
     * @param x the requested focus location on the <i>x</i> axis
     * @param y the requested focus location on the <i>y</i> axis
     */
    public void setScrollFocus(int x, int y) {			
	
	diffRight = x -(horizontalScrollBar.getValue()+
			     getSize().width+
			     -verticalScrollBar.getSize().width+
			     -MARGIN_RIGHT);
	diffLeft = x - (horizontalScrollBar.getValue()+MARGIN_LEFT);

	diffUp = y -(verticalScrollBar.getValue()+MARGIN_UP);
	diffDown = y -(verticalScrollBar.getValue()+
			    getSize().height+
			    -horizontalScrollBar.getSize().height+
			    -MARGIN_DOWN);			  	
	
	if(diffRight > 0) {	    
	    horizontalScrollBar.setValue(horizontalScrollBar.getValue()+
	    			 diffRight/speed);	    
	}else if(diffLeft < 0) {
	    horizontalScrollBar.setValue(horizontalScrollBar.getValue()+
	    			 diffLeft/speed);	    
	}
	
	if(diffDown > 0) {
	    verticalScrollBar.setValue(verticalScrollBar.getValue()+
	    		       diffDown/speed);
	}else if(diffUp < 0) {
	    verticalScrollBar.setValue(verticalScrollBar.getValue()+
	    		       diffUp/speed);  
	}				
    } 
    /**
     * Returns the scroll bar value.
     * <p>
     * Returns the scroll bar values as a <code>Point</code> instance. 
     * The horizontal scroll bar value will be 
     * <code>getCornerValue().x</code> 
     * and the vertical scroll bar value will be 
     * <code>getCornerValue().y</code>.
     * 
     * @return the scroll bar values
     */
    public Point getCornerValue() {
	return new Point(horizontalScrollBar.getValue(), verticalScrollBar.getValue());    
    }
    /**
     * Repaints the graph scroll pane content.
     */
    public void stateChanged(ChangeEvent e) {
	//DEBUG
	//System.out.println("GraphScrollPane.stateChanged");
	//END DEBUG	       
	repaint();
    }
    /**
     * Repaints the graph scroll pane content.
     */
    public void repaint() {
	//DEBUG
	//System.out.println("GraphScrollPane.repaint()");
	//END DEBUG
	if(this.getViewport() != null) {	    	   
	    getViewport().revalidate();
	    getViewport().validate();
	    getViewport().repaint();	    
	}
	super.repaint();
    }    
}
