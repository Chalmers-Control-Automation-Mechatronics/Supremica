package org.supremica.gui.ide;

import javax.swing.JFrame;


public interface IDEReportInterface
{
    void error(String msg);
    
    // outputs an error message
    void error(String msg, Throwable t);
    
    void info(String msg);
    
    void debug(String msg);
    
    JFrame getFrame();
}
