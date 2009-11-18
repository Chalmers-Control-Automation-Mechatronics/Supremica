package net.sourceforge.waters.gui.simulator;

import org.supremica.gui.WhiteScrollPane;
import org.supremica.gui.ide.IDEDimensions;

public class SimulatorTablePanel
extends WhiteScrollPane
{
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public void initilise()
   {
    setPreferredSize(IDEDimensions.leftAnalyzerPreferredSize);
    setMinimumSize(IDEDimensions.leftAnalyzerMinimumSize);
  }

}
