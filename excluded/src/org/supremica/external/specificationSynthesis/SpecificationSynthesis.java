package org.supremica.external.specificationSynthesis;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.*;

import org.supremica.external.avocades.specificationsynthesis.*;
import org.supremica.external.specificationSynthesis.gui.*;




public class SpecificationSynthesis extends JFrame {

    public SpecificationSynthesis() {}

    /**
     * Starts a simple GUI by creating an instance of the 
     * <code>SpecificationSynthesis</code> class.
     *
     * @since   0.1
     *
     * @param   args   string arguments from the prompt (not in use).
     * 
     */
	public static void main(String[] args) {
		try {
    		UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    	}catch(Exception ex) {}
    	
		Gui gui  = new Gui();
		gui.init();
    }
}