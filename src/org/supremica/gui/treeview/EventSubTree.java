//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Supremica/Waters IDE
//# PACKAGE: org.supremica.gui.treeview
//# CLASS:   EventSubTree
//###########################################################################
//# $Id$
//###########################################################################


package org.supremica.gui.treeview;

import javax.swing.Icon;

import net.sourceforge.waters.gui.util.IconLoader;

import org.supremica.automata.LabeledEvent;


/**
 * An EventSubTree is a tree node with the event name as root
 * and the event properties as children.
 */

public class EventSubTree
    extends SupremicaTreeNode
{
    private static final long serialVersionUID = 1L;

    private int directLeafs = 0;

    public EventSubTree(final LabeledEvent event)
    {
        super(event);    // Note that this also caches the event for quick access

        final SupremicaTreeNode currControllableNode = new SupremicaTreeNode("controllable: " + event.isControllable());
        add(currControllableNode); directLeafs++;

        final SupremicaTreeNode currObservableNode = new SupremicaTreeNode("observable: " + event.isObservable());
        add(currObservableNode); directLeafs++;

        // Hide junk...
        /*
        SupremicaTreeNode currPrioritizedNode = new SupremicaTreeNode("prioritized: " + event.isPrioritized());
        add(currPrioritizedNode); directLeafs++;

        SupremicaTreeNode currOperatorIncreaseNode = new SupremicaTreeNode("operatorIncrease: " + event.isOperatorIncrease());
        add(currOperatorIncreaseNode); directLeafs++;

        SupremicaTreeNode currOperatorResetNode = new SupremicaTreeNode("operatorReset: " + event.isOperatorReset());
        add(currOperatorResetNode); directLeafs++;
         */
    }

    /**
     * Change this to reflect the correct number of children/properties/leaves
     * Could this be calculated from sizeof(LabeledEvent)? It should not.
     * This depends only on the above construction
     *
     * This method is used to quickly determine if an event occurs in all
     * automata or not, see showIntersection in EventsViewerPanel.
     */
    public int numDirectLeafs()
    {
        return directLeafs;
    }

    @Override
    public Icon getOpenIcon()
    {
      final LabeledEvent event = (LabeledEvent) userObject;
      if (event.isControllable()) {
        if (event.isObservable()) {
          return IconLoader.ICON_CONTROLLABLE_OBSERVABLE;
        } else {
          return IconLoader.ICON_CONTROLLABLE_UNOBSERVABLE;
        }
      } else {
        if (event.isObservable()) {
          return IconLoader.ICON_UNCONTROLLABLE_OBSERVABLE;
        } else {
          return IconLoader.ICON_UNCONTROLLABLE_UNOBSERVABLE;
        }
      }
    }

    @Override
    public Icon getClosedIcon()
    {
        //return null;
        return getOpenIcon();
    }

    @Override
    public String toString()
    {
        return ((LabeledEvent) userObject).getLabel();
    }

}
