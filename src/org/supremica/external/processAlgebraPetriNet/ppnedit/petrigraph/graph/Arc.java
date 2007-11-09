package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph;


/*
*	class Arc handels how arcs appers in the graph.
*	Holds special write functions.
*
*	David Millares 2007-02-16 
*/

/*
 * To Do:
 *
 */

import java.awt.*;

import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.*;
import org.supremica.external.processeditor.xgraph.*;


public final class Arc {
	
	/**
	*
	*	function to connect sourcecell to targetcell
	*
	*/
	public static void drawArc(Graphics2D g, BaseCell source, BaseCell target){
		
		/* check indata */
		if(source == null || target == null){
			return;
		}
		
		Point p = null;
		Point[] pos = null;
		boolean place_is_source;
		
		if(source instanceof Place){
			p = source.getPos(GraphCell.CENTER);
			pos = target.getPoints(GraphCell.UPPER_CENTER);
			place_is_source = true;
		}else{	
			p = target.getPos(GraphCell.CENTER);
			pos = source.getPoints(GraphCell.LOWER_CENTER);
			place_is_source = false;
		}

		/* check points */		
		if(p == null || pos == null){
			return;
		}
		
		Point pupp = new Point(p.x,p.y-Place.getRadius());
		Point pdown = new Point(p.x,p.y+Place.getRadius());
		
		/* draw arcs */
		for(int i = 0; i < pos.length; i++){
			if(place_is_source){
				if(pdown.y < pos[i].y){
					drawUpperLine(g,pdown.x,pdown.y,pos[i].x,pos[i].y);
				}else if(p.y < pos[i].y){
					drawUpperCurve(g,p,pos[i],10,10);
				}else{
					drawUpperCurve(g,pupp,pos[i],10,10);
				}
			}else{
				if(pupp.y > pos[i].y){
					drawLowerLine(g,pupp.x,pupp.y,pos[i].x,pos[i].y);
				}else if(p.y > pos[i].y){
					drawLowerCurve(g,p,pos[i],10,10);
				}else{
					drawLowerCurve(g,pdown,pos[i],10,10);
				}
				
				/*draw arrow to place*/
				
			}
		}		
	}
	
	/**
	*
	*	function to connect two Points whit a concav curve 
	*
	*/
	public static void drawLowerCurve(Graphics2D g, Point from, Point to, int x, int y){
		if(from.x > to.x){
			drawLowerLine(g,from.x,from.y,to.x+x,to.y+y);
			drawLowerLine(g,to.x+x,to.y+y,to.x,to.y);
		}else{
			drawLowerLine(g,from.x,from.y,to.x-x,to.y+y);
			drawLowerLine(g,to.x-x,to.y+y,to.x,to.y);
		}
	}
	
	/**
	*
	*	function to connect two Points whit a convex curve 
	*
	*/
	public static void drawUpperCurve(Graphics2D g, Point from, Point to, int x, int y){
		if(from.x > to.x){
			drawUpperLine(g,from.x,from.y,to.x+x,to.y-y);
			drawUpperLine(g,to.x+x,to.y-y,to.x,to.y);
		}else{
			drawUpperLine(g,from.x,from.y,to.x-x,to.y-y);
			drawUpperLine(g,to.x-x,to.y-y,to.x,to.y);
		}
	}
	
	/**
	*
	*	function who take one Place as indata and draw arcs
	*	to its target- and source cells
	*
	*/
	public static void drawCellLines(Graphics2D g, Place cell){
		
		g.setColor(Color.black);
		
		/* connect target cells to place*/
		BaseCell[] target = cell.getTargetCells();
		if(target != null){
			for(int i = 0; i < target.length; i++){
				drawArc(g,cell,target[i]);
			}
		}
		
		/* connect source cells to place*/
		BaseCell[] source = cell.getSourceCells();
		if(source != null){
			for(int i = 0; i < source.length; i++){
				drawArc(g,source[i],cell);
			}
		}
    }

	/**
	*
	*	function to connect two points whit a strait line
	*
	*/
    public static void drawLine(Graphics2D g2d, int x1, int y1,
												int x2, int y2){
        g2d.drawLine(x1,y1,x2,y2);
    }
    
	/**
	*
	*	function to connect two points whit a downgoing line
	*
	*/
    public static void drawUpperLine(Graphics2D g2d, int x1, int y1,
	                                                 int x2, int y2){
    	int x, y;
        int width, height;
		
		if(x1 == x2 || y1 == y2){
            drawLine(g2d,x2,y1,x2,y2);
			return;
        }
		
		if(y2 < y1){
			/* switch */
			y = y2;
			y2 = y1;
			y1 = y;
			
			x = x2;
			x2 = x1;
			x1 = x;
		}
		
		/* create rec*/
		y = y1;
		
        if(x1 > x2){
            x = x2;
        }else{
            x = x1 - Math.abs(x2-x1);
        }
        
        width = 2*Math.abs(x1 - x2);
        height = 2*Math.abs(y2 - y1);
		
		//g2d.drawRect(x,y,width,height);
		
		/* draw curve*/
        if(x1 > x2){
            g2d.drawArc(x,y,width,height,180,-90);
        }else{
            g2d.drawArc(x,y,width,height,0,90);
        }
    }
    
	/**
	*
	*	function to connect two points whit a uppgoing lilne
	*
	*/
    public static void drawLowerLine(Graphics2D g2d, int x1, int y1,
													  int x2, int y2){
        int x, y;
        int width, height;
		
		if(x1 == x2 || y1 == y2){
            drawLine(g2d,x2,y1,x2,y2);
			return;
        }
		
		if(y2 < y1){
			/* switch */
			y = y2;
			y2 = y1;
			y1 = y;
			
			x = x2;
			x2 = x1;
			x1 = x;
		}
		
		/* create rec */
        width = 2*Math.abs(x1 - x2);
        height = 2*Math.abs(y1 - y2);
		
		y = y2 - height;
        
        if(x1 > x2){
            x = x1 - width;
        }else{
            x = x1;
        }
		
		//g2d.drawRect(x,y,width,height);
        
        if(x1 > x2){
            g2d.drawArc(x,y,width,height,0,-90);
        }else{
            g2d.drawArc(x,y,width,height,180,90);
        }
    }
	
	/**
	*
	*	function to draw a triangle
	*
	*/
	public static void drawTriangle(Graphics2D g2d, int x, int y, double degre) {
		Polygon triangle = new Polygon();
        
		int base = 10;
		int heigth = 5;
		
		
		
        // triangle tip
        triangle.addPoint(x,y);					
        
		if(degre >= 90 && degre <= 180 ||
		   degre <= -90 && degre >= -180){
		   
		   //convert grades to rad
			degre = (Math.PI/180)*degre;
		   
		    x = x + (int)(base*Math.cos(degre));
			y = y - (int)(base*Math.sin(degre));
		
        	triangle.addPoint(x,y);
		
        	x = x + (int)(heigth*Math.sin(degre));
			y = y - (int)(heigth*Math.cos(degre));
		
        	triangle.addPoint(x,y);
			
		}else{
		
			//convert grades to rad
			degre = (Math.PI/180)*degre;
			
			x = x + (int)(base*Math.cos(degre));
			y = y - (int)(base*Math.sin(degre));
		
        	triangle.addPoint(x,y);
		
        	x = x + (int)(heigth*Math.sin(degre));
			y = y - (int)(heigth*Math.cos(degre));
		
        	triangle.addPoint(x,y);
		}
        
        // close polygon
        //triangle.addPoint(x,y);
		
		//draw triangle					
        //g2d.drawPolygon(triangle);
        g2d.fillPolygon(triangle);						
   }
    
	/**
	*
	*	function to connect two points whit a strait line whit a arrow.
	*
	*/
    public static void drawArrow(Graphics2D g2d, int xCenter, int yCenter, 
                                                 int x, int y) {
    	drawArrow(g2d,xCenter,yCenter,x,y,(float)1);
    }
    
	/**
	*
	*	function to connect two points whit a strait line whit a arrow.
	*
	*/
    public static void drawArrow(Graphics2D g2d, int xCenter, int yCenter, 
                                                 int x, int y, float stroke) {
        double aDir=Math.atan2(xCenter-x,yCenter-y);
        g2d.drawLine(x,y,xCenter,yCenter);
        
        // make the arrow head solid even if dash pattern has been specified
        g2d.setStroke(new BasicStroke(1f));					
        Polygon tmpPoly=new Polygon();
        int i1=12+(int)(stroke*2);
        
        // make the arrow head the same size regardless of the length length
        int i2=6+(int)stroke;							
        
        // arrow tip
        tmpPoly.addPoint(x,y);							
        
        tmpPoly.addPoint(x+xCor(i1,aDir+.5),y+yCor(i1,aDir+.5));
        tmpPoly.addPoint(x+xCor(i2,aDir),y+yCor(i2,aDir));
        tmpPoly.addPoint(x+xCor(i1,aDir-.5),y+yCor(i1,aDir-.5));
        
        // arrow tip
        tmpPoly.addPoint(x,y);							
        //g2d.drawPolygon(tmpPoly);
        
        // remove this line to leave arrow head unpainted
        g2d.fillPolygon(tmpPoly);						
   }
   
   /**
	*
	*	help function to drawArrow
	*
	*/
   private static int yCor(int len, double dir) {
        return (int)(len * Math.cos(dir));
   }
   
   /**
	*
	*	help function to drawArrow
	*
	*/
   private static int xCor(int len, double dir) {
        return (int)(len * Math.sin(dir));
   }
}
