package net.sourceforge.waters.gui.actions;

import org.supremica.gui.ide.IDE;

public class AnalyzeConflictCheckAction
extends WatersAnalyzeAction
{

  protected AnalyzeConflictCheckAction(final IDE ide)
  {
    super(ide);
  }

  protected String[] getAnalyzeMethod()
  {
    return new String[]{"Conflict", "conflict error", WatersAnalyzeAction.ANALYZE_CONFLICT};
  }

  protected void updateEnabledStatus()
  {
    // Do nothing
  }

  private static final long serialVersionUID = -8684703946705836025L;
}
