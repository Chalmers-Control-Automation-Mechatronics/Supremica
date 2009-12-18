package net.sourceforge.waters.gui.simulator;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.table.JTableHeader;

public class SorterButton extends JButton
{

  // ###############################################################
  // # Constructor
  public SorterButton(final String name, final EventJTree tree, final int index, final JTableHeader header)
  {
    super(name);
    this.setBackground(header.getBackground());
    this.setContentAreaFilled(false);
    addMouseListener(new MouseAdapter(){
      public void mouseClicked(final MouseEvent evt)
      {
        tree.sortBy(index);
        System.out.println("DEBUG: Sorted by :" + index);
      }
    });
  }

  // ################################################################
  // # Class Constants
  private static final long serialVersionUID = -4734984975052581474L;
}
