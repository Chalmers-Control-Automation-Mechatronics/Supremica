package net.sourceforge.waters.gui.simulator;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JPanel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;

public class EventJTreeHeader extends JTableHeader implements ComponentListener
{

  // #####################################################################
  // # Constructor

  public EventJTreeHeader(final JPanel panel)
  {
    parent = panel;
    oldWidth = parent.getSize().getWidth();
    parent.addComponentListener(this);
    this.getColumnModel().addColumn(new TableColumn());
    this.getColumnModel().addColumn(new TableColumn());
    this.getColumnModel().addColumn(new TableColumn());
    if (oldWidth == 0)
      oldWidth = 245;
    final double width = oldWidth;
    System.out.println("DEBUG: Old width: " + oldWidth);
    this.getColumnModel().getColumn(0).setWidth((int)(width * 0.2));
    this.getColumnModel().getColumn(0).setMaxWidth((int)(width * 0.2));
    this.getColumnModel().getColumn(0).setHeaderValue("Type");
    this.getColumnModel().getColumn(1).setWidth((int)(width * 0.6));
    this.getColumnModel().getColumn(1).setHeaderValue("Name");
    this.getColumnModel().getColumn(2).setWidth((int)(width * 0.2));
    this.getColumnModel().getColumn(2).setMaxWidth((int)(width * 0.2));
    this.getColumnModel().getColumn(2).setHeaderValue("Ebd");
    this.setReorderingAllowed(false);
    this.setVisible(true);
    this.setResizingAllowed(false);
  }

  // ####################################################################3
  // # Interface Component Listener


  public void componentHidden(final ComponentEvent e)
  {
    // Do nothing
  }

  public void componentMoved(final ComponentEvent e)
  {
    // Do nothing
  }

  public void componentResized(final ComponentEvent e)
  {
    final TableColumn firstColumn = this.getColumnModel().getColumn(0);
    final TableColumn secondColumn = this.getColumnModel().getColumn(1);
    final TableColumn thirdColumn = this.getColumnModel().getColumn(2);
    final double width = parent.getWidth();
    final double newWidth = width - firstColumn.getWidth() - thirdColumn.getWidth();
    secondColumn.setWidth((int)newWidth);
     oldWidth = parent.getWidth();
    for (int looper = 0 ; looper < 3; looper++)
      System.out.println("DEBUG: Column " + looper + " has width " + this.getColumnModel().getColumn(looper).getWidth());
    System.out.println("DEBUG: The entire width is: " + this.getWidth());
  }

  public void componentShown(final ComponentEvent e)
  {
    // Do nothing
  }

  // ###########################################################################
  // # Data Members

  private double oldWidth;
  private final JPanel parent;

  // ###########################################################################
  // # Class Constants
  /**
   *
   */
  private static final long serialVersionUID = 3210675056736810131L;


}
