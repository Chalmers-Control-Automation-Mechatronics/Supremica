package net.sourceforge.waters.gui.actions;

import net.sourceforge.waters.gui.actions.WatersAnalyzeAction;
import org.supremica.gui.ide.IDE;


public class AnalyzeControlLoopAction extends WatersAnalyzeAction
{
  protected AnalyzeControlLoopAction(final IDE ide)
  {
    super(ide);
  }

  protected String[] getAnalyzeMethod()
  {
    return new String[]{"Control Loop", "control loop error", WatersAnalyzeAction.ANALYZE_CONTROL_LOOP};
  }

  protected void updateEnabledStatus()
  {
    // Do nothing
  }

  private static final long serialVersionUID = 2167516363996006935L;


}
