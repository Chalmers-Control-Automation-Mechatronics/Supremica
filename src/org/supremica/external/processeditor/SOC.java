package org.supremica.external.processeditor;

import java.awt.*;
import javax.swing.*;

/**
 * A Simple class that contain the <code>main</code> method for the 
 * SOC program. <p>
 * Call to the <code>main</code> method from the standard prompt
 * starts the SOC program by creating an instance of the 
 * <code>SOCFrame</code> class.
 *
 * @author    Mikael Kjellgren <kjelle@etek.chalmers.se>
 *
 * @version   0.1
 */
public class SOC {
    /**
    * Starts the SOC program by creating an instance of the 
    * <code>SOCFrame</code> class.
    *
    * @since   0.1
    *
    * @param   args   string arguments from the prompt (not in use).
    *                 
    *
    */
    public static void main(String[] args) {
    	try {
    		UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    	}catch(Exception ex) {}
    	SOCFrame soc = new SOCFrame();	
    }
}
