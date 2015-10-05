package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base;

import javax.swing.*;

import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph.Arc;

import java.awt.*;


class TransitionPanel extends JPanel {
    
    private final int ARROW_HEIGTH = 15;
    private final int HEIGTH = 1;
    private final int WIDTH = 20;
    private final int MARGIN = 1;
	
	private boolean drawArrow = false;
	
    public TransitionPanel() {
        setBounds(0,0, WIDTH, HEIGTH + ARROW_HEIGTH);
    }
	public void drawArrow(boolean b){
		drawArrow = b;
	}
    
    public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
        
		if(drawArrow){
			drawConnection(g2);
		}
        drawCell(g2);
    }
    private void drawConnection(Graphics2D g){
        //draw conection
        Arc.drawLine(g,getSize().width/2,0,getSize().width/2,getSize().height);
        Arc.drawArrow(g,getSize().width/2,0,getSize().width/2,
                      getSize().height-HEIGTH-1,(float)1);
    }
    private void drawCell(Graphics2D g){
		//draw transition
    	//g.setPaint(new GradientPaint(0, 0, Color.gray, getSize().width, 0, Color.black));
    	g.setPaint(Color.black);
    	g.fillRect(MARGIN,getSize().height-HEIGTH-MARGIN, getSize().width-MARGIN, getSize().height-MARGIN);
    }
}
