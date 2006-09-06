package org.supremica.util;

import javax.swing.*;
import java.awt.*;
import org.supremica.gui.InterfaceManager;

/**
 * This component extends JPopupMenu and adds a method to display the menu inside the screen,
 * even if the mouse pointer is near the edge of the screen.
 * <br>
 * Class created by Cris Sinnott
 * <br>
 * Source at : http://www.egroups.com/list/advanced-java/md1875700976.html
 */
public class VPopupMenu
    extends JPopupMenu
{
    public VPopupMenu()
    {
        InterfaceManager interfaceManager = InterfaceManager.getInstance();
    }
    
    /**Displays the popUpMenu at a specified position*/
    public void show(Component invoker, int x, int y)
    {
        Point p = getPopupMenuOrigin(invoker, x, y);
        
        super.show(invoker, p.x, p.y);
    }
    
    /**Figures out the sizes needed to calculate the menu position*/
    protected Point getPopupMenuOrigin(Component invoker, int x, int y)
    {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension pmSize = this.getSize();
        
        // For the first time the menu is popped up
        // the size has not yet been initialised
        if (pmSize.width == 0)
        {
            pmSize = this.getPreferredSize();
        }
        
        Point absp = new Point(x, y);
        
        SwingUtilities.convertPointToScreen(absp, invoker);
        
        int aleft = absp.x + pmSize.width;
        int abottom = absp.y + pmSize.height;
        
        if (aleft > screenSize.width)
        {
            x -= aleft - screenSize.width;
        }
        
        if (abottom > screenSize.height)
        {
            y -= abottom - screenSize.height;
        }
        
        return new Point(x, y);
    }
}
