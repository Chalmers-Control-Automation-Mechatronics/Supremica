package org.supremica.external.processAlgebraPetriNet.ppnedit.gui;

import javax.swing.*;
import java.awt.*;

public class PetriProFrame extends JFrame  {
    
    public PetriProFrame() {	
        getContentPane().add(new TextPanel());
        pack();
        
        //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
        setVisible(true);
    }
}
