package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph;


import javax.swing.border.*;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Component;

public class SelectedCellBorder 
						extends AbstractBorder{
    /**
	 * 
	 */
	private static final long serialVersionUID = 859659338984709413L;

	protected Color boxColor;
	
	private final int BOXSIZE = 3;
	
	public SelectedCellBorder() {
        boxColor = Color.gray;
    }
	
    public SelectedCellBorder(Color color) {
        boxColor = color;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
          g.setColor(boxColor);
		  
		  //upper boxes
		  drawBox(g, 0,0);
		  drawBox(g, width/2-BOXSIZE/2,0);
		  drawBox(g, width-BOXSIZE,0);
		  
		  //lower boxes
		  drawBox(g, 0, height-BOXSIZE);
		  drawBox(g, width/2-BOXSIZE/2, height-BOXSIZE);
		  drawBox(g, width-BOXSIZE, height-BOXSIZE);
		  
		  //middle boxes
		  drawBox(g, 0, height/2-BOXSIZE/2);
		  drawBox(g, width-BOXSIZE, height/2-BOXSIZE/2);
		  
    }
	private void drawBox(Graphics g, int x, int y){
		g.fillRect(x, y, BOXSIZE, BOXSIZE);
	}

    public Insets getBorderInsets(Component c) {
        return new Insets(BOXSIZE,BOXSIZE,BOXSIZE,BOXSIZE);
    }

    public boolean isBorderOpaque() { 
        return false;
    }
}
