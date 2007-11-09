package org.supremica.external.processAlgebraPetriNet.ppnedit;

import javax.swing.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.gui.PNetVision;

public class PPNEdit {
    
    static JFrame frame;
    
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        
        //Set UIManager
        try {
        	UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }catch(Exception ex) {}    
        
        //Create and set up the window.
        frame = new PNetVision();
    }
    
    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
