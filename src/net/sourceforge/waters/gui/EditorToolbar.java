//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorToolbar
//###########################################################################
//# $Id: EditorToolbar.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;


/** The primary editor toolbar.
 * The toolbar sits to the left of the editor window and allows for tool selections.
 * The selection status is then queried by other objects.
 *
 * @author Simon Ware
 */


public class EditorToolbar extends JPanel {

  //#########################################################################
  //# Data Members
  private final ButtonGroup mGroup = new ButtonGroup();
  private ToolButtonListener mLastSelected;

  private static final Color SELECTIONCOLOR = new Color(255, 200, 240);


  //#########################################################################
  //# Constructors
  public EditorToolbar()
  {
    setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
    mLastSelected = createButton("select", "Select", true);
    createButton("node", "Create Nodes", false);
    createButton("nodegroup", "Create Group Nodes", false);
    createButton("initial", "Set Initial Nodes", false);
    createButton("edge", "Create Edges", false);
    createButton("color", "Drag Events", false);
  }


  //#########################################################################
  //# Simple Access
  /**
   * Find out which tool is currently selected
   * @return The ActionCommand for the currently selected tool
   */
  public String getPlace()
  {
    return mGroup.getSelection().getActionCommand();
  }


  //#########################################################################
  //# Rendering Buttons
  private ToolButtonListener createButton(final String command,
					  final String tooltip,
					  final boolean selected)
  {
    final JPanel panel = new JPanel();
    final JRadioButton button = new JRadioButton();
    final String iconname = "/icons/waters/" + command + ".gif";
    final ImageIcon icon =
      new ImageIcon(EditorToolbar.class.getResource(iconname));
    button.setActionCommand(command);
    button.setIcon(icon);
    button.setToolTipText(tooltip);
    button.setSelected(selected);
    mGroup.add(button);
    panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
    panel.add(button);
    showButton(panel, button);
    add(panel);
    final ToolButtonListener listener = new ToolButtonListener(panel, button);
    button.addActionListener(listener);
    return listener;
  }

  private void showButton(final JPanel panel, final JRadioButton button)
  {
    if (button.isSelected()) {
      final Border border = BorderFactory.createLoweredBevelBorder();
      panel.setBorder(border);
      button.setBackground(SELECTIONCOLOR);
    } else {
      final Border border = BorderFactory.createRaisedBevelBorder();
      panel.setBorder(border);
      button.setBackground(null);
    }
    panel.repaint();
  }


  //#########################################################################
  //# Local Class ToolButtonListener
  private class ToolButtonListener implements ActionListener
  {
    //#######################################################################
    //# Data Members
    private final JPanel mPanel;
    private final JRadioButton mButton;

    //#######################################################################
    //# Constructors
    private ToolButtonListener(final JPanel panel, final JRadioButton button)
    {
      mPanel = panel;
      mButton = button;
    }

    //#######################################################################
    //# Interface java.awt.event.ActionListener
    public void actionPerformed(final ActionEvent event)
    {
      mLastSelected.showButton();
      showButton();
      mLastSelected = this;
    }

    //#######################################################################
    //# Rendering Buttons
    private void showButton()
    {
      EditorToolbar.this.showButton(mPanel, mButton);
    }

  }

}
