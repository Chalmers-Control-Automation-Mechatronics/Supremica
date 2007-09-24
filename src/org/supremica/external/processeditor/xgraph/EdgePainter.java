package org.supremica.external.processeditor.xgraph;

import java.awt.*;

/**
 * Draws the edges from specified positions and styles.
 */
public class EdgePainter {                

    public static final int DEFAULT_THICKNESS = 1;
    public static final int DEFAULT_LINESPACE = 5;
    public static final int DEFAULT_OVERSHOOT = 10;
    public static int thickness = DEFAULT_THICKNESS;
    public static int lineSpace = DEFAULT_LINESPACE;        

    public static final int HORIZONTAL_FIRST = 1;
    public static final int VERTICAL_FIRST = 2;
    public static final int HORIZONTAL_FIRST_BREAK_MID = 3;
    public static final int VERTICAL_FIRST_BREAK_MID = 4;
    public static final int HORIZONTAL_FIRST_DUBBLE = 5;
    public static final int VERTICAL_FIRST_DUBBLE = 6;
    

    /**
     * Draws a horizontal single line from
     * the start location (<code>x1</code>, <code>y</code>) to 
     * the end location (<code>x2</code>, <code>y</code>).
     *
     * @param g the graphical context
     * @param x1 the start location along the <i>x</i> axis
     * @param x2 the end location along the <i>x</i> axis
     * @param y the location along the <i>y</i> axis
     */
    public static void drawHorizontalLine(Graphics g, 
					   int x1, int x2, int y) {       
	for(int i = 0; i <= thickness; i++) {
	    g.drawLine(x1, y+i, x2, y+i);
	    g.drawLine(x1, y-i, x2, y-i);
	}       
    }
    /**
     * Draws a horizontal double line from
     * the start location (<code>x1</code>, <code>y</code>) to 
     * the end location (<code>x2</code>, <code>y</code>).
     *
     * @param g the graphical context
     * @param x1 the start location along the <i>x</i> axis
     * @param x2 the end location along the <i>x</i> axis
     * @param y the location along the <i>y</i> axis
     */
    public static void drawHorizontalDubbleLine(Graphics g, 
					  int x1, int x2, int y) {	
	for(int i = 0; i <= thickness; i++) {
		g.drawLine(x1, y+i, x2, y+i);
		g.drawLine(x1, y-i, x2, y-i);
	}	       		
	for(int i = 0; i < thickness; i++) {
	    g.drawLine(x1, y+i+lineSpace, x2, y+i+lineSpace);    
	}		    
    }
    /**
     * Draws a verical single line from
     * the start location (<code>x1</code>, <code>y</code>) to 
     * the end location (<code>x2</code>, <code>y</code>).
     *
     * @param g the graphical context
     * @param x the location along the <i>x</i> axis
     * @param y1 the start location along the <i>x</i> axis
     * @param y2 the end location along the <i>y</i> axis
     */     
    public static void drawVerticalLine(Graphics g, int x, int y1, int y2) {	
	for(int i = 0; i <= thickness; i++) {
		g.drawLine(x+i, y1, x+i, y2);
		g.drawLine(x-i, y1, x-i, y2);
	}
    }
    /**
     * Draws a straight line from
     * the start location (<code>x1</code>, <code>y1</code>) to 
     * the end location (<code>x2</code>, <code>y2</code>).
     *
     * @param g the graphical context
     * @param x1 the start location along the <i>x</i> axis
     * @param y1 the start location along the <i>y</i> axis
     * @param x2 the end location along the <i>x</i> axis
     * @param y2 the end location along the <i>y</i> axis
     */     
    public static void drawStraightEdge(Graphics g, int x1, int y1,
					int x2, int y2, 
					int linetype) {
	if(linetype == HORIZONTAL_FIRST) {
	    drawHorizontalLine(g, x1, x2, y1);
	    drawVerticalLine(g, x2, y1, y2);
	}else if(linetype == VERTICAL_FIRST) {
	    drawVerticalLine(g, x1, y1, y2);
	    drawHorizontalLine(g, x1, x2, y2);
	}else if(linetype == HORIZONTAL_FIRST_DUBBLE) {
	    int overshoot = DEFAULT_OVERSHOOT;
	    if(x1 > x2) {
		overshoot = -DEFAULT_OVERSHOOT;
	    }
	    drawHorizontalDubbleLine(g, x1, x2+overshoot, y1);   
	    drawVerticalLine(g, x2, y1, y2);
	}else if(linetype == VERTICAL_FIRST_DUBBLE) {
	    int overshoot = DEFAULT_OVERSHOOT;
	    if(x1 > x2) {
		overshoot = -DEFAULT_OVERSHOOT;
	    }
	    drawVerticalLine(g, x1, y1, y2+DEFAULT_LINESPACE);
	    drawHorizontalDubbleLine(g, x1-overshoot, x2, y2);
	}else if(linetype == VERTICAL_FIRST_BREAK_MID) {
	    drawVerticalLine(g, x1, y1, y1+(y2-y1)/2);
	    drawHorizontalLine(g, x1, x2, y1+(y2-y1)/2);
	    drawVerticalLine(g, x2, y1+(y2-y1)/2, y2);
	}else if(linetype == HORIZONTAL_FIRST_BREAK_MID) {
	    drawHorizontalLine(g, x1, x1+(x2-x1)/2, y1);
	    drawVerticalLine(g, x1+(x2-x1)/2, y1, y2);
	    drawHorizontalLine(g, x1+(x2-x1)/2, x2, y2);
	}else {
	    g.drawLine(x1, y1, x2, y2);
	}
    }
}

