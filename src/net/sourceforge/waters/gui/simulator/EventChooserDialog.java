package net.sourceforge.waters.gui.simulator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.BevelBorder;

import net.sourceforge.waters.gui.IconLoader;
import net.sourceforge.waters.model.des.EventProxy;


public class EventChooserDialog extends JDialog
{


  // #######################################################################
  // # Constructor

  public EventChooserDialog(final JFrame owner, final JLabel[] labels, final EventProxy[] correspondingEvent)
  {
    super(owner, "Multiple Options available", true);
    cancelled = true;
    final JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    mList = new JList(labels);
    mList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mList.setSelectedIndex(0);
    mList.setCellRenderer(new ListCellRenderer(){

      public Component getListCellRendererComponent(final JList list, final Object value,
          final int index, final boolean isSelected, final boolean cellHasFocus)
      {
        final JLabel output = (JLabel) value;
        if (isSelected)
          output.setBackground(Color.blue);
        if (cellHasFocus)
          output.setFont(output.getFont().deriveFont(Font.BOLD));
        else
          output.setFont(output.getFont().deriveFont(Font.PLAIN));
        return output;
      }
    });
    mList.setPreferredSize(new Dimension(DEFAULT_LIST_WIDTH, labels.length * DEFAULT_ROW_HEIGHT));
      // This code correctly assigns the width, but not the height
    mList.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
    eventList = correspondingEvent;
    final JScrollPane scrollPane = new JScrollPane(mList);
    panel.add(scrollPane, BorderLayout.CENTER);
    final JPanel buttonPanel = new JPanel();
    final JButton selectButton = new JButton("Select Event");
    final JButton cancelButton = new JButton("Cancel");
    final double maximumWidth = Math.max(selectButton.getPreferredSize().getWidth(), cancelButton.getPreferredSize().getWidth());
    selectButton.setPreferredSize(new Dimension((int)maximumWidth, (int)selectButton.getPreferredSize().getHeight()));
    cancelButton.setPreferredSize(new Dimension((int)maximumWidth, (int)cancelButton.getPreferredSize().getHeight()));
    final GridBagLayout layout = new GridBagLayout();
    layout.columnWidths = new int[]{(int)((DEFAULT_LIST_WIDTH - maximumWidth * 2) / 2),
      (int)maximumWidth,
      (int)maximumWidth,
      (int)((DEFAULT_LIST_WIDTH - maximumWidth * 2) / 2)};
    buttonPanel.setLayout(layout);
    buttonPanel.add(new JLabel()); // To keep the empty tile empty
    buttonPanel.add(selectButton);
    buttonPanel.add(cancelButton);
    panel.add(buttonPanel, BorderLayout.SOUTH);
    final JLabel topLabel = new JLabel("Select the Event you wish to fire");
    topLabel.setIcon(IconLoader.ICON_EVENT);
    panel.add(topLabel, BorderLayout.NORTH);
    this.add(panel);
    this.pack();
    this.setLocation(DEFAULT_STARTING_LOCATION);
    selectButton.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(final MouseEvent evt)
      {
        if (EventChooserDialog.this.getSelectedEvent() != null)
        {
          cancelled = false;
          EventChooserDialog.this.dispose();
        }
      }
    });
    cancelButton.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(final MouseEvent evt)
      {
        EventChooserDialog.this.dispose();
      }
    });
    mList.addMouseListener(new MouseAdapter()
    {
      public void mouseClicked(final MouseEvent evt)
      {
        if (evt.getClickCount() == 2 && EventChooserDialog.this.getSelectedEvent() != null)
        {
          cancelled = false;
          EventChooserDialog.this.dispose();
        }
      }
    });
    mList.addKeyListener(new KeyListener()
    {
      public void keyPressed(final KeyEvent e)
      {
        if (e.getKeyCode() == 10) // <ENTER> Key
        {
          cancelled = false;
          EventChooserDialog.this.dispose();
        }
      }

      public void keyReleased(final KeyEvent e)
      {
        // Do Nothing
      }

      public void keyTyped(final KeyEvent e)
      {
        // Do Nothing
      }

    });
  }

  // ####################################################################
  // # Simple Access

  public EventProxy getSelectedEvent()
  {
    return (EventProxy)eventList[mList.getSelectedIndex()];
  }
  public boolean wasCancelled()
  {
    return cancelled;
  }

  // ####################################################################
  // # Data Members

  private final JList mList;
  private boolean cancelled;
  private final EventProxy[] eventList;

  // ####################################################################
  // # Class Constants
  private static final long serialVersionUID = -4465845587624430860L;
  private static final int DEFAULT_LIST_WIDTH = 250;
  private static final int DEFAULT_ROW_HEIGHT = 20;
  private static final Point DEFAULT_STARTING_LOCATION = new Point(100, 100);

}
