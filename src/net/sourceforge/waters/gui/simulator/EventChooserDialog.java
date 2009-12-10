package net.sourceforge.waters.gui.simulator;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import net.sourceforge.waters.gui.IconLoader;
import net.sourceforge.waters.model.des.EventProxy;


public class EventChooserDialog extends JDialog
{

  // #######################################################################
  // # Constructor

  public EventChooserDialog(final JFrame owner, final EventProxy[] events)
  {
    super(owner, "Multiple Options available", true);
    cancelled = true;
    final JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout());
    mList = new JList(events);
    mList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mList.setSelectedIndex(0);
    mList.setPreferredSize(DEFAULT_LIST_DIMENSION);
    final JScrollPane scrollPane = new JScrollPane(mList);
    panel.add(scrollPane, BorderLayout.CENTER);
    final JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(new BorderLayout());
    final JButton selectButton = new JButton("Select Event");
    final JButton cancelButton = new JButton("Cancel");
    buttonPanel.add(selectButton, BorderLayout.WEST);
    buttonPanel.add(cancelButton, BorderLayout.EAST);
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
  }

  // ####################################################################
  // # Simple Access

  public EventProxy getSelectedEvent()
  {
    return (EventProxy)mList.getSelectedValue();
  }
  public boolean wasCancelled()
  {
    return cancelled;
  }

  // ####################################################################
  // # Data Members

  private final JList mList;
  private boolean cancelled;

  // ####################################################################
  // # Class Constants
  private static final long serialVersionUID = -4465845587624430860L;
  private static final Dimension DEFAULT_LIST_DIMENSION = new Dimension(250, 250);
  private static final Point DEFAULT_STARTING_LOCATION = new Point(100, 100);

}
