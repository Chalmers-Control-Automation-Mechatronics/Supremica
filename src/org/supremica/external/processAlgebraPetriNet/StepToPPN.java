package org.supremica.external.processAlgebraPetriNet;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.*;

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