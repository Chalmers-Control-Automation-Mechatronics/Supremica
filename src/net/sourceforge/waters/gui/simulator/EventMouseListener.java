package net.sourceforge.waters.gui.simulator;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

import net.sourceforge.waters.model.des.EventProxy;

public class EventMouseListener extends MouseAdapter
{
  //#################################################################################
  //# Constructors
  public EventMouseListener(final Simulation sim, final JTable table)
  {
    this.mSim = sim;
    this.parent = table;
  }

  //#################################################################################
  //# Class MouseAdapter
  public void mouseClicked(final MouseEvent e)
  {
      if (e.getClickCount() == 2)
      {
        for (int row = 0; row < parent.getRowCount(); row++)
        {
          for (int column = 0; column < parent.getColumnCount(); column++)
          {
            if (parent.getCellRect(row, column, true).contains(e.getPoint()))
            {
              final EventProxy event = mSim.getAllEvents().get(row);
              if (mSim.getValidTransitions().contains(event))
              {
                try {
                  mSim.step(event);
                } catch (final UncontrollableException exception) {
                  System.out.println(exception.getMessage());
                }
              }
              //else
                //System.out.println(mSim.getBlockingTextual(event));
            }
          }
        }
      }
  }

  //#################################################################################
  //# Data Members
  private final Simulation mSim;
  private final JTable parent;
}
