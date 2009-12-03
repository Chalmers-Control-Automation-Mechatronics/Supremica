package net.sourceforge.waters.gui.simulator;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class SelectedTableCellRenderer extends DefaultTableCellRenderer
{
  /**
   *
   */
  private static final long serialVersionUID = 1086293271366963464L;
  private final Simulation mSim;
  private final AutomatonDesktopPane mDesktop;

  public SelectedTableCellRenderer(final Simulation sim, final AutomatonDesktopPane desktop)
  {
    super();
    mSim = sim;
    mDesktop = desktop;
  }

  public Component getTableCellRendererComponent
    (final JTable table, final Object value, final boolean selected, final boolean focused, final int row, final int column)
  {
    final Component tableCellRenderer = super.getTableCellRendererComponent(table, value, selected, focused, row, column);

    if (mDesktop.automatonIsOpen(((AbstractTunnelTable)table.getModel()).getAutomaton(row, mSim)))
    {
      final Font oldFont = tableCellRenderer.getFont();
      tableCellRenderer.setFont(oldFont.deriveFont(Font.BOLD));
    }
    return tableCellRenderer;
  }
}

