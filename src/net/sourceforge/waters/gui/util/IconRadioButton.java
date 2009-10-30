//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.util
//# CLASS:   IconRadioButton
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.util;


import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;


/**
 * A radio button replacement to produce radio buttons with additional icons.
 *
 * In standard Swing, the icon of a radio button shows whether the button
 * is selected, so no additional image can be shown. This class provides
 * buttons that show their selection status, an additional icon, and the
 * descriptive text.
 *
 * It is implemented as a {@link JPanel} containing a {@link JRadioButton}
 * without label and a borderless {@link JButton} with an image and a textual
 * label, which is linked to the radio button.
 * 
 * @author Robi Malik
 */

public class IconRadioButton
  extends JPanel
  implements ActionListener
{

  //#########################################################################
  //# Constructor
  public IconRadioButton(final String text,
                         final Icon icon,
                         final ButtonGroup group)
  {
    final GridBagLayout layout = new GridBagLayout();
    setLayout(layout);
    final GridBagConstraints constraints = new GridBagConstraints();
    mButton = new JRadioButton();
    mButton.setFocusable(false);
    mButton.setRequestFocusEnabled(false);
    group.add(mButton);
    constraints.gridx = GridBagConstraints.RELATIVE;
    constraints.gridy = 0;
    constraints.weightx = 0.0;
    constraints.weighty = 1.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    layout.setConstraints(mButton, constraints);
    add(mButton);
    final JButton label = new JButton(text, icon);
    final Insets margin = new Insets(0, 0, 0, 0);
    label.setMargin(margin);
    label.setBorderPainted(false);
    label.setContentAreaFilled(false);
    label.setRequestFocusEnabled(false);
    label.addActionListener(this);
    constraints.weightx = 1.0;
    layout.setConstraints(label, constraints);
    add(label);
  }


  //#########################################################################
  //# Simple Access
  public boolean isSelected()
  {
    return mButton.isSelected();
  }

  public void setSelected(final boolean selected)
  {
    mButton.setSelected(selected);
  }

  public void addActionListener(final ActionListener listener)
  {
    mButton.addActionListener(listener);
  }

  public void removeActionListener(final ActionListener listener)
  {
    mButton.removeActionListener(listener);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    mButton.doClick();
  }


  //#########################################################################
  //# Data Members
  private final JRadioButton mButton;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
