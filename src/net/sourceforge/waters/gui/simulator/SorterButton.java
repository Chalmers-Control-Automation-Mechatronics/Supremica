package net.sourceforge.waters.gui.simulator;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;

public class SorterButton extends JButton
{

  // ###############################################################
  // # Constructor
  public SorterButton(final String name, final EventJTree tree, final int index)
  {
    super(name);
    addMouseListener(new MouseAdapter(){
      public void mouseClicked(final MouseEvent evt)
      {
        tree.sortBy(index);
        tree.forceRecalculation();
      }
    });
  }

  // ################################################################
  // # Class Constants
  private static final long serialVersionUID = -4734984975052581474L;
}
