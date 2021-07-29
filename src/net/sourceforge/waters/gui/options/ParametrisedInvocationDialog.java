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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import net.sourceforge.waters.gui.analyzer.WatersAnalyzerPanel;
import net.sourceforge.waters.gui.dialog.AnalysisProgressDialog;
import net.sourceforge.waters.gui.dialog.ErrorLabel;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.gui.util.RaisedDialogPanel;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.options.Option;
import net.sourceforge.waters.model.options.SelectorLeafOptionPage;

import org.supremica.gui.ide.IDE;


/**
 * Superclass for dialogs that invoke an analysis or simplification
 * operation in the Waters Analyser. Generates a GUI to select algorithms
 * and/or options based on a {@link SelectorLeafOptionPage}.
 *
 * @author Brandon Bassett
 */

public abstract class ParametrisedInvocationDialog<S> extends JDialog
{
  //#########################################################################
  //# Constructor
  public ParametrisedInvocationDialog(final WatersAnalyzerPanel panel,
                                      final SelectorLeafOptionPage<S> page)
  {
    super(panel.getModuleContainer().getIDE());
    mErrorLabel = new ErrorLabel();
    mContext = new GUIOptionContext(panel, this, mErrorLabel);

    final GridBagLayout layout = new GridBagLayout();
    setLayout(layout);
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.anchor = GridBagConstraints.CENTER;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;

    mPanel = mContext.createSelectorLeafOptionPageEditor(page);
    add(mPanel, constraints);

    // Error label
    final JPanel errorPanel = new RaisedDialogPanel();
    errorPanel.add(mErrorLabel);
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.weighty = 0.0;
    add(errorPanel, constraints);

    // Buttons
    final JPanel buttonsPanel = new JPanel();
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
    root.setDefaultButton(okButton);
    DialogCancelAction.register(this);
    add(buttonsPanel, constraints);

    pack();
    setLocationRelativeTo(mContext.getIDE());
    setVisible(true);
  }


  //#########################################################################
  //# Simple Access
  public GUIOptionContext getContext()
  {
    return mContext;
  }

  protected ProductDESProxyFactory getProductDESProxyFactory()
  {
    return mContext.getProductDESProxyFactory();
  }

  protected ModelAnalyzer getAnalyzer()
  {
    if (mCurrentModelAnalyzer == null) {
      updateModelAnalyzer();
    }
    return mCurrentModelAnalyzer;
  }


  //#########################################################################
  //# Hooks
  /**
   * Returns the option page used to generate this dialog.
   */
  protected abstract SelectorLeafOptionPage<S> getOptionPage();

  /**
   * Creates a new model analyser.
   * @param  selector  The current value for the top selector option
   *                   in the GUI, which determines what kind of analyser
   *                   is needed.
   */
  protected abstract ModelAnalyzer createModelAnalyzer(S selector)
    throws AnalysisConfigurationException, ClassNotFoundException;

  /**
   * Configures a model analyser based on the committed options in the
   * option page.
   * @param  analyzer  The analyser to be configured.
   */
  protected void configureModelAnalyzer(final ModelAnalyzer analyzer)
  {
    final SelectorLeafOptionPage<S> page = getOptionPage();
    for (final Option<?> option : mCurrentModelAnalyzer.getOptions(page)) {
      mCurrentModelAnalyzer.setOption(option);
    }
  }

  /**
   * Generates the pop-up dialog that shows progress while running the
   * analysis operation and/or its result.
   */
  protected abstract AnalysisProgressDialog createAnalyzeDialog
    (IDE ide, ProductDESProxy des, S selector);


  //#########################################################################
  //# Auxiliary Methods
  private void commitDialog()
  {
    final IDE ide = mContext.getIDE();
    final FocusTracker tracker = ide.getFocusTracker();
    if (tracker.shouldYieldFocus(this)) {
      mPanel.commitOptions();
      final S selector = updateModelAnalyzer();
      if (selector != null) {
        configureModelAnalyzer(mCurrentModelAnalyzer);
        final ProductDESProxy des = mContext.getProductDES();
        final JDialog dialog = createAnalyzeDialog(ide, des, selector);
        dispose();
        if (dialog != null) {
          dialog.setVisible(true);
        }
      }
    }
  }

  private S updateModelAnalyzer()
  {
    try {
      final S selector = mPanel.getSelectedValue();
      mCurrentModelAnalyzer = createModelAnalyzer(selector);
      return selector;
    } catch (final ClassNotFoundException exception) {
      throw new WatersRuntimeException(exception);
    } catch (final AnalysisConfigurationException exception) {
      mErrorLabel.displayError(exception.getMessage());
      return null;
    }
  }


  //#########################################################################
  //# Data Members
  private final GUIOptionContext mContext;
  private final SelectorLeafOptionPagePanel<S> mPanel;
  private final ErrorLabel mErrorLabel;
  private ModelAnalyzer mCurrentModelAnalyzer;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -3610355726871200803L;

}
