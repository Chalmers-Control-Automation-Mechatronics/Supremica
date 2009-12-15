package net.sourceforge.waters.gui.simulator;

import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagLayout;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellRenderer;

import net.sourceforge.waters.gui.EditorColor;

public class SelectedTableCellRenderer implements TableCellRenderer
{


  // #########################################################################
  // # Constructor
  public SelectedTableCellRenderer(final Simulation sim, final AutomatonDesktopPane desktop)
  {
    super();
    mSim = sim;
    mDesktop = desktop;
    mPanel = new JPanel();
    mLabel = new JLabel();
  }

  // #########################################################################
  // # Class DefaultTableCellRender
  public Component getTableCellRendererComponent
    (final JTable table, final Object value, final boolean selected, final boolean focused, final int row, final int column)
  {
    mPanel = new JPanel();
    mPanel.setLayout(new GridBagLayout());
    if (selected)
    {
      mPanel.setBackground(EditorColor.BACKGROUND_FOCUSSED);
    }
    else
    {
      mPanel.setBackground(EditorColor.BACKGROUNDCOLOR);
    }
    if (column == 1 || column == 4)
    {
      mLabel = new JLabel(String.valueOf(value), SwingConstants.LEFT);
      mPanel.setAlignmentY(JPanel.LEFT_ALIGNMENT);
      mPanel.add(mLabel);
      if (mDesktop.automatonIsOpen(((AbstractTunnelTable)table.getModel()).getAutomaton(row, mSim)))
      {
        final Font oldFont = mPanel.getFont();
        mLabel.setFont(oldFont.deriveFont(Font.BOLD));
      }
      else
      {
        final Font oldFont = mPanel.getFont();
        mLabel.setFont(oldFont.deriveFont(Font.PLAIN));
      }
    }
    else
    {
      final JLabel mLabel = new JLabel();
      mLabel.setIcon((Icon)value);
      mPanel.add(mLabel);
    }
    return mPanel;
  }

  // #########################################################################
  // # Data Members
  private static final long serialVersionUID = 1086293271366963464L;
  private final Simulation mSim;
  private final AutomatonDesktopPane mDesktop;
  private JPanel mPanel;
  private JLabel mLabel;
}

