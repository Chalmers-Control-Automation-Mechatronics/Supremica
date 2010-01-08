package net.sourceforge.waters.gui.actions;

import org.supremica.gui.ide.IDE;

public class AnalyzeControllabilityAction extends WatersAnalyzeAction
{

  protected AnalyzeControllabilityAction(final IDE ide)
  {
    super(ide);
  }

  protected String[] getAnalyzeMethod()
  {
    return new String[]{"Controllability", "controllability error", WatersAnalyzeAction.ANALYZE_CONTROLLABLE};
  }

  protected void updateEnabledStatus()
  {
    // Do nothing
  }

  private static final long serialVersionUID = -1008097797553564719L;
}
