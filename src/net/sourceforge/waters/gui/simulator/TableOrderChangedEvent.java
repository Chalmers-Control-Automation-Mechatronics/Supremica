package net.sourceforge.waters.gui.simulator;

import java.util.EventObject;

public class TableOrderChangedEvent extends EventObject
{

  // #########################################################################
  // # Constructor

  public TableOrderChangedEvent(final AbstractTunnelTable table)
  {
    super(table);
    mTable = table;
  }

  // #########################################################################
  // # Simple Access

  public AbstractTunnelTable getTable()
  {
    return mTable;
  }

  // #########################################################################
  // # Data Members

  private final AbstractTunnelTable mTable;

  // #########################################################################
  // # Class Constants

  private static final long serialVersionUID = 7165158300769771603L;
}
