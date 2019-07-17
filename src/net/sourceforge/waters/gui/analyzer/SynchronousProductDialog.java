//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.gui.analyzer;

import net.sourceforge.waters.gui.dialog.WatersAnalyzeDialog;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.des.AutomatonResult;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.SynchronousProductBuilder;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.gui.ide.IDE;


/**
 * @author George Hewlett, Carly Hona, Brandon Bassett
 */
public class SynchronousProductDialog extends AbstractAnalysisDialog
{

  //#######################################################################
  //# Constructor
  public SynchronousProductDialog(final WatersAnalyzerPanel panel)
  {
    super(panel, new AnalyzerProductDESContext(panel));
    setTitle("Synchronous product");
    //setMinimumSize(getSize());
  }

  //#########################################################################
  //# Overrides for net.sourceforge.waters.gui.dialog.AbstractAnalysisDialog

  @Override
  protected ModelAnalyzer createAnalyzer(final ModelAnalyzerFactory analyzerFactory,
                                         final ProductDESProxyFactory desFactory)
  {
    try {
      return analyzerFactory.createSynchronousProductBuilder(desFactory);
    } catch (final AnalysisConfigurationException exception) {   }

    return null;
  }

  @Override
  protected SynchronousProductBuilder getAnalyzer()
  {
    return (SynchronousProductBuilder) super.getAnalyzer();
  }

  @Override
  protected WatersAnalyzeDialog createAnalyzeDialog(final IDE ide,
                                                    final ProductDESProxy des)
  {
    //final SynchronousProductBuilder builder = getAnalyzer();
    getAnalyzer().setModel(des);
    try {
      getAnalyzer().run();
    } catch (final AnalysisException exception) {
      final Logger logger = LogManager.getLogger();
      final String msg = exception.getMessage();
      logger.error(msg);
    }
    final AutomatonResult result = getAnalyzer().getAnalysisResult();
    final AutomatonProxy aut = result.getComputedProxy();
    final AutomataTableModel model = getWatersAnalyzerPanel().getAutomataTableModel();
    model.insertRow(aut);

    return null;
  }


  //#########################################################################
  //# Initialisation and Layout of Components
  /**
   * Initialise buttons and components.
   */
  /*
  private void createComponents()
  {
    final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    final ExpressionParser parser = new ExpressionParser(factory, optable);
    final ActionListener commithandler = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        commitDialog();
      }
    };
    // Main panel ...
    mMainPanel = new RaisedDialogPanel();
    mNameLabel = new JLabel("Name:");
    IdentifierProxy oldName = null;
    try {
      oldName = parser.parseIdentifier(getNewName());
    } catch (final ParseException exception) {
      oldName = factory.createSimpleIdentifierProxy(DEFAULT_NAME);
    }
    final FormattedInputParser nameParser = new
      AutomatonNameInputParser(oldName, mAnalyzerPanel, parser, false);
    mNameInput =
      new SimpleExpressionCell(oldName, nameParser);
    mNameInput.addActionListener(commithandler);
    mNameInput.setToolTipText("Enter automaton name, e.g., x or v[i]");
    mKindLabel = new JLabel("Kind:");
    mKindGroup = new ButtonGroup();
    mPlantButton = new IconRadioButton("Plant", IconAndFontLoader.ICON_PLANT,
                                       mKindGroup, 'p');
    mPropertyButton =
      new IconRadioButton("Property", IconAndFontLoader.ICON_PROPERTY,
                          mKindGroup, 'o');
    mSpecButton =
      new IconRadioButton("Specification", IconAndFontLoader.ICON_SPEC,
                          mKindGroup, 's');
    mSupervisorButton =
      new IconRadioButton("Supervisor", IconAndFontLoader.ICON_SUPERVISOR,
                          mKindGroup, 'u');

    final ComponentKind kind = getKind();
    switch (kind) {
    case PLANT:
      mPlantButton.setSelected(true);
      break;
    case PROPERTY:
      mPropertyButton.setSelected(true);
      break;
    case SPEC:
      mSpecButton.setSelected(true);
      break;
    case SUPERVISOR:
      mSupervisorButton.setSelected(true);
      break;
    }

    // Error panel ...
    mErrorPanel = new RaisedDialogPanel();
    mErrorLabel = new ErrorLabel();
    mErrorPanel.add(mErrorLabel);
    mNameInput.setErrorDisplay(mErrorLabel);

    // Buttons panel ...
    mButtonsPanel = new JPanel();
    final JButton okButton = new JButton("OK");
    okButton.setRequestFocusEnabled(false);
    okButton.addActionListener(commithandler);
    mButtonsPanel.add(okButton);
    final Action cancelAction = DialogCancelAction.getInstance();
    final JButton cancelButton = new JButton(cancelAction);
    cancelButton.setRequestFocusEnabled(false);
    mButtonsPanel.add(cancelButton);

    final JRootPane root = getRootPane();
    root.setDefaultButton(okButton);
    DialogCancelAction.register(this);
  }*/



  //#########################################################################
  //# Action Listeners
  /**
   * Commits the contents of this dialog to the model. This method is attached
   * to the action listener of the 'OK' button of the event editor dialog.
   */
/*
  public void commitDialog()
  {
    final ProductDESProxyFactory factory =
      ProductDESElementFactory.getInstance();
    if (isInputLocked()) {
      // There is invalid input and an error message has been displayed.
      // Do not try to commit.
    } else {
      // Read the data from the dialog ...
      final String name = mNameInput.getText();
      final ComponentKind kind;
      if (mPlantButton.isSelected()) {
        kind = ComponentKind.PLANT;
      } else if (mPropertyButton.isSelected()) {
        kind = ComponentKind.PROPERTY;
      } else if (mSpecButton.isSelected()) {
        kind = ComponentKind.SPEC;
      } else if (mSupervisorButton.isSelected()) {
        kind = ComponentKind.SUPERVISOR;
      } else {
        throw new IllegalStateException("Component kind not selected!");
      }

      final ProductDESProxy des =
        AutomatonTools.createProductDESProxy("synchronousForAnalyzer",
                                             mAutomata, factory);
      final TRSynchronousProductBuilder builder = new TRSynchronousProductBuilder(des);
      //builder.setOutputName(name);
      //builder.setOutputKind(kind);
      try {
        builder.run();
      } catch (final AnalysisException exception) {
        final Logger logger = LogManager.getLogger();
        final String msg = exception.getMessage();
        logger.error(msg);
      }
      final AutomatonResult result = builder.getAnalysisResult();
      final AutomatonProxy aut = result.getComputedProxy();
      final AutomataTableModel model = mAnalyzerPanel.getAutomataTableModel();
      model.insertRow(aut);

      // Close the dialog
      dispose();
    }
  }*/


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Checks whether it is unsafe to commit the currently edited text field. If
   * this method returns <CODE>true</CODE>, it is unsafe to commit the current
   * dialog contents, and shifting the focus is to be avoided.
   *
   * @return <CODE>true</CODE> if the component currently owning the focus has
   *         been found to contain invalid information, <CODE>false</CODE>
   *         otherwise.
   */
  /*
  private boolean isInputLocked()
  {
    return mNameInput.isFocusOwner() && !mNameInput.shouldYieldFocus();
  }

  private String getNewName()
  {
    if (mAutomata.size() <= 4) {
      final StringBuilder builder = new StringBuilder();
      boolean first = true;
      for (final AutomatonProxy aut : mAutomata) {
        if (first) {
          first = false;
        } else {
          builder.append('_');
        }
        builder.append(aut.getName());
      }
      return builder.toString();
    } else {
      return DEFAULT_NAME;
    }
  }

  private ComponentKind getKind()
  {
    int plantCount = 0;
    int propCount = 0;
    int specCount = 0;
    for (final AutomatonProxy aut : mAutomata) {
      switch (aut.getKind()) {
      case PLANT:
        plantCount++;
        break;
      case PROPERTY:
        propCount++;
        break;
      case SPEC:
        specCount++;
        break;
      case SUPERVISOR:
        break;
      }
    }
    if (plantCount > 0) {
      return ComponentKind.PLANT;
    } else if (propCount > 0) {
      return ComponentKind.PROPERTY;
    } else if (specCount > 0) {
      return ComponentKind.SPEC;
    } else {
      return ComponentKind.SUPERVISOR;
    }
  }*/


  //#########################################################################
  //# Data Members
  // Dialog state
  //private final WatersAnalyzerPanel mAnalyzerPanel;
  //private final List<AutomatonProxy> mAutomata;

  // Swing components
  /*
  private JPanel mMainPanel;
  private JLabel mNameLabel;
  private SimpleExpressionCell mNameInput;
  private JLabel mKindLabel;
  private ButtonGroup mKindGroup;
  private IconRadioButton mPlantButton;
  private IconRadioButton mPropertyButton;
  private IconRadioButton mSpecButton;
  private IconRadioButton mSupervisorButton;
  private JPanel mErrorPanel;
  private ErrorLabel mErrorLabel;
  private JPanel mButtonsPanel;
*/

  //#########################################################################
  //# Class Constants
  @SuppressWarnings("javadoc")
  private static final long serialVersionUID = 6159733639861131531L;
 // private static final Insets INSETS = new Insets(2, 4, 2, 4);
  //private static final String DEFAULT_NAME = "sync";
}
