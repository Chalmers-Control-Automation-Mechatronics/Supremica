package org.supremica.external.processAlgebraPetriNet;

import javax.swing.*;


import org.supremica.external.processAlgebraPetriNet.Gui.*;

public class StepToPPN extends JFrame {
    PPNGui window;

    public StepToPPN () {
        window = new PPNGui();

		window.CreatePPNGui();
        window.setTitle("ProcessAlgebraPetriNet");
		window.setSize(1200, 950);
        window.setVisible(true);

	}


	public static void main(String[] args) {
		StepToPPN steptoppn  = new StepToPPN ();
    }
}