package net.sourceforge.waters.gui.simulator;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JInternalFrame;


public class InternalFrameMouseAdapter extends MouseAdapter
{
  //#################################################################################
  //# Constructor

  public InternalFrameMouseAdapter(final JInternalFrame frame)
  {

  }
  //#################################################################################
  //# Class MouseAdapter
  public void mouseClicked(final MouseEvent e){

   //parent.moveToFront();
  }

  public void mouseReleased(final MouseEvent e){
    System.out.println("DEBUG: Mouse has been released");
  }

  //#################################################################################
  //# Data Variables

}
