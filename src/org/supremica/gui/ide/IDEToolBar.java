//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   IDEToolBar
//###########################################################################
//# $Id: IDEToolBar.java,v 1.12 2006-11-03 15:01:57 torda Exp $
//###########################################################################

package org.supremica.gui.ide;

import java.awt.Insets;
import javax.swing.*;
import java.util.*;
import org.supremica.gui.ide.actions.IDEAction;
import org.supremica.log.*;
import net.sourceforge.waters.gui.ControlledToolbar;

import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.ToolbarChangedEvent;
import net.sourceforge.waters.gui.ControlledSurface;

public class IDEToolBar
    extends JToolBar
    implements ControlledToolbar
{
    private static Logger logger = LoggerFactory.createLogger(IDEToolBar.class);
    private static final Insets theInsets = new Insets(0, 0, 0, 0);
    // note do this nicer
    private String command = "";
    
    private List<Observer> mObservers = new ArrayList<Observer>();
    
    private List collection = new LinkedList();
    
    private IDE ide;
    
    public IDEToolBar(IDE ide)
    {
        this.ide = ide;
        setRollover(true);
        setFloatable(false);
    }
    
    public IDEToolBar(IDEToolBar toolBar)
    {
        this(toolBar.ide);
//		logger.debug("Toolbar copy constructor");
        for (Iterator actIt = toolBar.collection.iterator(); actIt.hasNext(); )
        {
            Action currAction = (Action)actIt.next();
            if (currAction == null)
            {
                addSeparator();
//				logger.debug("Added separator");
            }
            else
            {
                add(currAction);
                
// Huh - note that the button is stored in an action. Possible two buttons may true to save
// themselves in the action -> problems
// The above solution works because not a third instance tries to add members to the toolbar.
// Fix as soon as possible...
/*
                                if (currAction instanceof IDEAction)
                                {
                                        add(((IDEAction)currAction).getButton());
                                        logger.debug("Added IDEAction");
                                }
                                else
                                {
                                        add(currAction);
                                        logger.debug("Added Action");
                                }
 */
            }
        }
        
        // Also copy observers
        mObservers.addAll(toolBar.mObservers);
    }
    
    public JToggleButton add(Action theAction, ButtonGroup theButtonGroup)
    {
        JToggleButton theButton = new JToggleButton(theAction);
        theButton.setText("");
        add(theButton);
        theButtonGroup.add(theButton);
        collection.add(theAction);
        if (theAction instanceof IDEAction)
        {
            ((IDEAction)theAction).setButton(theButton);
        }
        theButton.setMargin(theInsets);
        
        return theButton;
    }
    
    public JButton add(Action theAction)
    {
        JButton theButton = super.add(theAction);
        collection.add(theAction);
        if (theAction instanceof IDEAction)
        {
            ((IDEAction)theAction).setButton(theButton);
        }
        theButton.setMargin(theInsets);
        
        return theButton;
    }
    
    public void addSeparator()
    {
        collection.add(null);
        super.addSeparator();
    }
    
    public int nbrOfActions()
    {
        return collection.size();
    }
    
    public ControlledSurface.Tool getCommand()
    {
        return Enum.valueOf(ControlledSurface.Tool.class, command);
    }
    
    public void setCommand(String c)
    {
        command = c;
        fireEditorChangedEvent(new ToolbarChangedEvent());
    }
    
    public void attach(Observer o)
    {
        //System.err.println("IDEToolBar attach");
        mObservers.add(o);
    }
    
    public void detach(Observer o)
    {
        //System.err.println("IDEToolBar detach");
        mObservers.remove(o);
    }
    
    public void fireEditorChangedEvent(EditorChangedEvent e)
    {
        //System.err.println("IDEToolBar fireEditorChangedEvent size: " + mObservers.size());
        for (Observer o : mObservers)
        {
            o.update(e);
        }
    }
}
