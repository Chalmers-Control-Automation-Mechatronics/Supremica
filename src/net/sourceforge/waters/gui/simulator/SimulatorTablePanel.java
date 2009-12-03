package net.sourceforge.waters.gui.simulator;

import org.supremica.gui.WhiteScrollPane;
import org.supremica.gui.ide.IDEDimensions;

public class SimulatorTablePanel
extends WhiteScrollPane
{

  // #########################################################################
  // # Simple Access / Constructor (Which is it??)
  public void initilise()
   {
    setPreferredSize(IDEDimensions.leftAnalyzerPreferredSize);
    setMinimumSize(IDEDimensions.leftAnalyzerMinimumSize);
  }

  // #########################################################################
  // # Class Constants
  private static final long serialVersionUID = 1L;
}
