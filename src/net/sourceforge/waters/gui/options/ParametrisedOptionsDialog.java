//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.options;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.PatternSyntaxException;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;

import net.sourceforge.waters.gui.dialog.ErrorLabel;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.gui.util.RaisedDialogPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.gui.ide.IDE;
import org.supremica.properties.ConfigPages;


/**
 *
 * @author Benjamin Wheeler
 */

public abstract class ParametrisedOptionsDialog extends JDialog
{

  //#########################################################################
  //# Constructor
  public ParametrisedOptionsDialog(final IDE ide)
  {
    super(ide);
    final ErrorLabel errorLabel = new ErrorLabel();
    mIde = ide;
    mContext = new GUIOptionContext(ide, this, errorLabel);
    mQuery = new SearchQuery();

    final GridBagLayout layout = new GridBagLayout();
    setLayout(layout);
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;

    mTopTabbedPane =
      mContext.createAggregatorOptionPageEditor(ConfigPages.ROOT);
    add(mTopTabbedPane, constraints);

    // Error label
    final JPanel errorPanel = new RaisedDialogPanel();
    errorPanel.add(errorLabel);
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.weighty = 0.0;
    add(errorPanel, constraints);

    // Buttons
    final JPanel buttonsPanel = new JPanel();
    final JTextField textField = new JTextField();
    textField.setColumns(10);
    buttonsPanel.add(textField);
    final JButton searchButton = new JButton("Search");
    searchButton.setRequestFocusEnabled(false);
    buttonsPanel.add(searchButton);
    searchButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent e)
      {
        search(textField.getText());
      }
    });


    final ActionListener commitHandler = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        commitDialog();
      }
    };
    final JButton okButton = new JButton("OK");
    okButton.setRequestFocusEnabled(false);
    okButton.addActionListener(commitHandler);
    buttonsPanel.add(okButton);
    final Action cancelAction = DialogCancelAction.getInstance();
    final JButton cancelButton = new JButton(cancelAction);
    cancelButton.setRequestFocusEnabled(false);
    buttonsPanel.add(cancelButton);

    final JRootPane root = getRootPane();
    root.setDefaultButton(searchButton);
    DialogCancelAction.register(this);
    add(buttonsPanel, constraints);

    pack();
    setLocationRelativeTo(mIde);
    setVisible(true);
  }

  //#########################################################################
  //# Simple Access
  public GUIOptionContext getContext()
  {
    return mContext;
  }

  //#########################################################################
  //# Auxiliary Methods
  private void commitDialog()
  {
    final FocusTracker tracker = mIde.getFocusTracker();
    if (tracker.shouldYieldFocus(this)) {
      mTopTabbedPane.commitOptions();
      dispose();
    }
  }

  private void search(final String regex) {

    if (!regex.equals(mQuery.getRegex()) || mQuery.isNewSearch()) {
      try {
          mQuery.setPattern(regex);
          mTopTabbedPane.search(mQuery);
      } catch (final PatternSyntaxException e) {
        final Logger logger = LogManager.getLogger();
        logger.error("Invalid regex query.");
        return;
      }
    }

    final OptionPanel<?> lastMatched = mQuery.getLastMatched();
    final OptionPanel<?> result = mQuery.getResult();
    if (result == null) {
      final Logger logger = LogManager.getLogger();
      logger.error("No result found.");
      return;
    }
    if (lastMatched != null) {
      lastMatched.getLabel()
      .setBorder(BorderFactory.createLineBorder(getBackground()));
    }
    mTopTabbedPane.scrollToVisible(result);
    result.getLabel().setBorder(BorderFactory.createLineBorder(Color.BLUE));

  }


  //#########################################################################
  //# Data Members
  private final IDE mIde;
  private final GUIOptionContext mContext;

  private final AggregatorOptionPagePanel mTopTabbedPane;
  private final SearchQuery mQuery;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 7019891241799665462L;

}
