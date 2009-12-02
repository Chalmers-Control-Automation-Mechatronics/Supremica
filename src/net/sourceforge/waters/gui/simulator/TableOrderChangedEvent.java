package net.sourceforge.waters.gui.simulator;

import java.util.EventObject;

public class TableOrderChangedEvent extends EventObject
{

  /**
   *
   */
  private static final long serialVersionUID = 7165158300769771603L;

  public TableOrderChangedEvent(final AbstractTunnelTable table)
  {
    super(table);
    mTable = table;
  }

  public AbstractTunnelTable getTable()
  {
    return mTable;
  }

  private final AbstractTunnelTable mTable;
}
