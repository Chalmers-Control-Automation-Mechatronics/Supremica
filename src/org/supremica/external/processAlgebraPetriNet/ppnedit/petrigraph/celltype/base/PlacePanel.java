package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base;

import javax.swing.*;
import java.awt.*;

class PlacePanel extends JPanel {
	//diameter
    private int D = -1;
    
	private boolean token = false;
	
    public PlacePanel() {
       D = 24;
       setBounds(0,0, D, D);
    }
    public PlacePanel(int diameter) {
        D = diameter;
        setBounds(0,0, D, D);
    }
	
	public void drawToken(boolean drawToken){
		token = drawToken;
	}
	public boolean getToken(){
		return token;
	}
	
    public void paintComponent(Graphics g){
    	
    	g.setColor(Color.white);
   		g.fillOval(0, 0, D, D);
   		
    	g.setColor(Color.black);
   		g.drawOval(0, 0, D-1, D-1);
		
		if(token){
			g.setColor(Color.black);
        	g.fillOval(D/4, D/4, D/2, D/2);
		}
    }
}
