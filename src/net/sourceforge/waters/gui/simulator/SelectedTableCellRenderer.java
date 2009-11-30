package net.sourceforge.waters.gui.simulator;

import java.awt.Color;
import java.awt.Component;
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
    setEnabled(table == null || table.isEnabled()); // see question above

    if (mDesktop.automatonIsOpen(mSim.getAutomata().get(row)))
      //this.setFont(new Font("Times New Roman", Font.BOLD, 12));
      this.setBackground(Color.GREEN);
    else
      //this.setFont(new Font("Times New Roman", Font.PLAIN, 12));
      this.setBackground(null);
    super.getTableCellRendererComponent(table, value, selected, focused, row, column);

    return this;
  }
}

