package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base;

import javax.swing.*;
import java.awt.*;

class CellPlace extends JPanel {
    
    //diameter
    private int D;
    
    public CellPlace() {
       D = 25;
       setBounds(0,0, D, D);
    }
    public CellPlace(int diameter) {
        D = diameter;
        setBounds(0,0, D, D);
    }
    
	public void paintComponent(Graphics g) {
		g.setColor(Color.black);
   		g.drawOval(0, 0, D-1, D-1);
		g.dispose();
    }
}
