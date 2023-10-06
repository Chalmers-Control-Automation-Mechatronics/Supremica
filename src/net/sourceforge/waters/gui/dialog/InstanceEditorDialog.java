//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.gui.dialog;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.DocumentFilter;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.gui.util.RaisedDialogPanel;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ScopeKind;
import net.sourceforge.waters.subject.base.UndoInfo;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.InstanceSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.ParameterBindingSubject;
import net.sourceforge.waters.subject.module.PlainEventListSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;

import org.supremica.gui.ide.IDE;


/**
 * @author Carly Hona
 */

public class InstanceEditorDialog extends JDialog
{

  //#######################################################################
  //# Constructor
  public InstanceEditorDialog(final ModuleWindowInterface root)
  {
    this(root, null);
  }

  public InstanceEditorDialog(final ModuleWindowInterface root,
                              final InstanceSubject inst)
  {
    if (inst == null) {
      setTitle("Creating new instance");
    } else {
      setTitle("Editing instance '" + inst.getName() + "'");
    }
    mRoot = root;
    mInstance = inst;
    createComponents();
    layoutComponents();
    setLocationRelativeTo(mRoot.getRootWindow());
    mNameInput.requestFocusInWindow();
    setVisible(true);
    setMinimumSize(getSize());
  }

  private void createComponents()
  {
    setModal(true);
    InstanceSubject template;
    if (mInstance == null) {
      try {
        template = TEMPLATE;
        final SelectionOwner panel = mRoot.getComponentsPanel();
        final List<InsertInfo> inserts = panel.getInsertInfo(TRANSFERABLE);
        final InsertInfo insert = inserts.get(0);
        mInsertPosition = insert.getInsertPosition();
      } catch (final IOException | UnsupportedFlavorException exception) {
        throw new WatersRuntimeException(exception);
      }
    } else {
      template = mInstance;
    }

    final SimpleDocumentListener okEnablement = new SimpleDocumentListener() {
      @Override
      public void documentChanged(final DocumentEvent event)
      {
        updateOkButtonStatus();
      }
    };
    final ActionListener commitHandler = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        commitDialog();
      }
    };

    mMainPanel = new RaisedDialogPanel();
    final ExpressionParser parser = mRoot.getExpressionParser();
    mNameLabel = new JLabel("Name:");
    final IdentifierProxy oldname = template.getIdentifier();
    final ModuleContext context = mRoot.getModuleContext();
    final FormattedInputHandler<IdentifierProxy> nameParser =
      new ComponentNameInputHandler(oldname, context, parser, true);
    mNameInput = new SimpleExpressionInputCell(oldname, nameParser);
    mNameInput.addSimpleDocumentListener(okEnablement);
    mNameInput.addActionListener(commitHandler);
    mNameInput.setToolTipText(NAME_TOOLTIP);

    final String toolTip = getModuleToolTip();
    mModuleLabel = new JLabel("Module:");
    mModuleInput = new ModuleInputCell(template.getModuleName());
    mModuleInput.setToolTipText(toolTip);
    mModuleInput.addActionListener(commitHandler);
    mModuleInput.addSimpleDocumentListener(okEnablement);

    mFileChooserButton = new JButton(" ... ");
    mFileChooserButton.setToolTipText(toolTip);
    mFileChooserButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        chooseFile();
      }
    });

    // Error panel ...
    mErrorPanel = new RaisedDialogPanel();
    mErrorLabel = new ErrorLabel();
    mErrorPanel.add(mErrorLabel);
    mNameInput.setErrorDisplay(mErrorLabel);
    mModuleInput.setErrorDisplay(mErrorLabel);

    // Buttons panel ...
    mButtonsPanel = new JPanel();
    mOkButton = new JButton("OK");
    mOkButton.setRequestFocusEnabled(false);
    mOkButton.addActionListener(commitHandler);
    mButtonsPanel.add(mOkButton);
    updateOkButtonStatus();

    final Action pressOK = new AbstractAction() {
      private static final long serialVersionUID = 1L;
      @Override
      public void actionPerformed(final ActionEvent e)
      {
        mOkButton.doClick();
      }
    };
    final KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    final String name = (String) pressOK.getValue(Action.NAME);
    mModuleInput.getInputMap().put(stroke, name);
    mModuleInput.getActionMap().put(name, pressOK);

    final JButton cancelButton = new JButton("Cancel");
    cancelButton.setRequestFocusEnabled(false);
    cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        dispose();
      }
    });
    mButtonsPanel.add(cancelButton);

    final JRootPane root = getRootPane();
    root.setDefaultButton(mOkButton);
    DialogCancelAction.register(this);
  }

  private void layoutComponents()
  {
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.insets = new Insets(2, 4, 2, 4);

    final GridBagLayout mainlayout = new GridBagLayout();
    mMainPanel.setLayout(mainlayout);

    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.weightx = 0.0;
    constraints.anchor = GridBagConstraints.WEST;
    mainlayout.setConstraints(mNameLabel, constraints);
    mMainPanel.add(mNameLabel);

    mNameInput.setColumns(20);
    constraints.gridx++;
    constraints.gridwidth = 1;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    mainlayout.setConstraints(mNameInput, constraints);
    mMainPanel.add(mNameInput);
    mNameInput.setColumns(20);

    constraints.gridx = 0;
    constraints.gridy++;
    constraints.weightx = 0.0;
    mainlayout.setConstraints(mModuleLabel, constraints);
    mMainPanel.add(mModuleLabel);

    constraints.gridx++;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    mainlayout.setConstraints(mModuleInput, constraints);
    mMainPanel.add(mModuleInput);

    constraints.gridx++;
    constraints.weightx = 0.0;
    constraints.fill = GridBagConstraints.NONE;
    mainlayout.setConstraints(mFileChooserButton, constraints);
    mMainPanel.add(mFileChooserButton);

    // Finally, build the full dialog ...
    final Container contents = getContentPane();
    final GridBagLayout layout = new GridBagLayout();
    contents.setLayout(layout);
    constraints.gridx = 0;
    constraints.gridy = GridBagConstraints.RELATIVE;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.insets = new Insets(0, 0, 0, 0);
    layout.setConstraints(mMainPanel, constraints);
    contents.add(mMainPanel);
    layout.setConstraints(mErrorPanel, constraints);
    contents.add(mErrorPanel);
    layout.setConstraints(mButtonsPanel, constraints);
    contents.add(mButtonsPanel);
    pack();
    final Dimension size = getSize();
    setMinimumSize(size);
  }

  private void chooseFile()
  {
    final JFileChooser fileChooser = mRoot.getRootWindow().getFileChooser();
    fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
    fileChooser.setMultiSelectionEnabled(false);
    final String text = mModuleInput.getText();
    final File userInput = new File(text);
    fileChooser.setSelectedFile(userInput);
    fileChooser.resetChoosableFileFilters();
    fileChooser.removeChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
    fileChooser.setFileFilter(new ModuleFileFilter());

    // Show the dialog ...
    final int choice = fileChooser.showOpenDialog(InstanceEditorDialog.this);
    // Get the filename ...
    if (choice == JFileChooser.APPROVE_OPTION) {
      mNameInput.clearErrorMessage();
      final File file = fileChooser.getSelectedFile();
      final URI fileUri = file.toURI();
      final ModuleSubject module = mRoot.getModuleSubject();
      final URI moduleUri = module.getLocation();
      final URI relativePath = relativise(fileUri, moduleUri);
      final String name = relativePath.getPath();
      mModuleInput.setText(removeExtension(name));
    }
    mModuleInput.requestFocusInWindow();
  }

  private void updateOkButtonStatus()
  {
    final boolean enabled =
      mNameInput.getText().length() > 0 && mModuleInput.getText().length() > 0;
    mOkButton.setEnabled(enabled);
  }

  private void commitDialog()
  {
    if (isInputLocked()) {
      // nothing
    } else if (mNameInput.getValue() == null) {
      mNameInput.requestFocusWithErrorMessage(NAME_TOOLTIP);
    } else if (mModuleInput.getValue() == null) {
      mModuleInput.requestFocusWithErrorMessage(getModuleToolTip());
    } else {
      final ModuleProxyCloner cloner =
        ModuleSubjectFactory.getCloningInstance();
      final IdentifierSubject inputIdent =
        (IdentifierSubject) mNameInput.getValue();
      final IdentifierSubject newIdent =
        (IdentifierSubject) cloner.getClone(inputIdent);
      final String moduleName = mModuleInput.getText();
      final SelectionOwner panel = mRoot.getComponentsPanel();
      InstanceSubject template = (InstanceSubject) cloner.getClone(mInstance);
      if (mInstance == null) {
        template = new InstanceSubject(newIdent, moduleName);
        final ModuleSubject module = mRoot.getModuleSubject();
        final DocumentManager manager = mRoot.getRootWindow().getDocumentManager();
        URI uri;
        try {
          uri = manager.resolve(module, moduleName, ModuleProxy.class);
        } catch (final WatersUnmarshalException exception1) {
          throw new WatersRuntimeException(exception1);
        }
        final File filename = new File(uri);
        ModuleProxy proxy = null;
        try {
          proxy = (ModuleProxy) manager.load(filename);
        } catch (final WatersUnmarshalException exception) {
          throw new WatersRuntimeException(exception);
        } catch (final IOException exception) {
          throw new WatersRuntimeException(exception);
        }
        final List<ParameterBindingSubject> listOfParameterBindings =
          template.getBindingListModifiable();
        final List<ConstantAliasProxy> listOfConstantAliases =
          proxy.getConstantAliasList();
        for (final ConstantAliasProxy alias : listOfConstantAliases) {
          if (alias.getScope() != ScopeKind.LOCAL) {
            final ParameterBindingSubject para =
              new ParameterBindingSubject(alias.getName(),
                   (ExpressionProxy) cloner.getClone(alias.getExpression()));
            listOfParameterBindings.add(para);
          }
        }
        final List<EventDeclProxy> listOfEventDecl = proxy.getEventDeclList();
        for (final EventDeclProxy alias : listOfEventDecl) {
          if (alias.getScope() != ScopeKind.LOCAL) {
            final ParameterBindingSubject para =
              new ParameterBindingSubject(alias.getName(),
                                          new PlainEventListSubject());
            listOfParameterBindings.add(para);
          }
        }
        final InsertInfo insert = new InsertInfo(template, mInsertPosition);
        final List<InsertInfo> list = Collections.singletonList(insert);
        final Command command = new InsertCommand(list, panel, mRoot);
        mInstance = template;
        mRoot.getUndoInterface().executeCommand(command);
      } else {
        template.setModuleName(moduleName);
        template.setIdentifier(newIdent);
        final UndoInfo info = mInstance.createUndoInfo(template, null);
        if (info != null) {
          final Command command = new EditCommand(mInstance, info, panel, null);
          mRoot.getUndoInterface().executeCommand(command);
        }
      }
      dispose();
    }
  }

  private boolean isInputLocked()
  {
    final IDE ide = mRoot.getRootWindow();
    final FocusTracker tracker = ide.getFocusTracker();
    return !tracker.shouldYieldFocus(this);
  }

  private URI relativise(final URI file, final URI module)
  {
    String product = file.toString();
    if (module != null) {
      final String filePath = product;
      final String modulePath = module.toString();
      final String[] fileArray = filePath.split("/");
      final String[] moduleArray = modulePath.split("/");
      if (file.compareTo(module) == 0) {
        product = fileArray[fileArray.length - 1];
      } else {
        int i = 0;
        while (i < moduleArray.length && i < fileArray.length) {
          if (moduleArray[i].compareTo(fileArray[i]) != 0) {
            i++;
            int dotSets = moduleArray.length - i;
            product = filePath.split("/", i)[i - 1];
            while (dotSets > 0) {
              product = "../" + product;
              dotSets--;
            }
          }
          i++;
        }
      }
    }
    try {
      return new URI(product);
    } catch (final URISyntaxException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  private String getModuleToolTip()
  {
    final DocumentManager manager = mRoot.getRootWindow().getDocumentManager();
    final ProxyMarshaller<ModuleProxy> marshaller =
      manager.findProxyMarshaller(ModuleProxy.class);
    final String ext = marshaller.getDefaultExtension();
    final StringBuilder builder = new StringBuilder();
    builder.append("Please enter or select a ");
    builder.append(ext);
    builder.append(" file.");
    return builder.toString();
  }

  private String removeExtension(final String fileName)
  {
    final DocumentManager manager = mRoot.getRootWindow().getDocumentManager();
    final ProxyMarshaller<ModuleProxy> marshaller =
      manager.findProxyMarshaller(ModuleProxy.class);
    final String ext = marshaller.getDefaultExtension();
    if (fileName.endsWith(ext)) {
      final int len = fileName.length() - ext.length();
      return fileName.substring(0, len);
    } else {
      return fileName;
    }
  }


  //#########################################################################
  //# Inner Class ModuleInputCell
  private class ModuleInputCell extends ValidatingTextCell<String>
  {
    //#######################################################################
    //# Constructor
    private ModuleInputCell(final String value)
    {
      super(new ModuleInputHandler());
      setValue(value);
    }

    //#######################################################################
    //# Overrides for javax.swing.JFormattedTextField
    @Override
    public String getValue()
    {
      return (String) super.getValue();
    }


    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = -356551293645968366L;
  }


  //#########################################################################
  //# Inner Class ModuleInputHandler
  private class ModuleInputHandler
    extends DocumentFilter
    implements FormattedInputHandler<String>
  {
    //#######################################################################
    //# Interface
    //# net.sourceforge.waters.gui.dialog.FormattedInputHandler<String>
    @Override
    public String format(final Object value)
    {
      return (String) value;
    }

    @Override
    public String parse(String text) throws ParseException
    {
      if (text.length() == 0) {
        return null;
      }
      text = removeExtension(text);
      try {
        final DocumentManager manager =
          mRoot.getRootWindow().getDocumentManager();
        final ModuleSubject module = mRoot.getModuleSubject();
        final URI moduleURI = module.getLocation();
        manager.load(moduleURI, text, ModuleProxy.class);
      } catch (final FileNotFoundException exception) {
        throw new ParseException("File not found.", 0);
      } catch (final WatersUnmarshalException | IOException exception) {
        throw new ParseException("Cannot open module.", 0);
      }
      return text;
    }

    @Override
    public DocumentFilter getDocumentFilter()
    {
      return this;
    }
  }


  //#########################################################################
  //# Inner Class ModuleFileFilter
  private class ModuleFileFilter extends FileFilter
  {
    //#######################################################################
    //# Constructor
    private ModuleFileFilter()
    {
      final DocumentManager manager =
        mRoot.getRootWindow().getDocumentManager();
      final ProxyMarshaller<ModuleProxy> marshaller =
        manager.findProxyMarshaller(ModuleProxy.class);
      mExtension = marshaller.getDefaultExtension();
      final FileFilter filter = marshaller.getDefaultFileFilter();
      mDescription = filter.getDescription();
    }

    //#######################################################################
    //# Overrides for javax.swing.filechooser.FileFilter
    @Override
    public boolean accept(final File file)
    {
      if (file.isDirectory()) {
        return true;
      } else if (!file.getName().endsWith(mExtension)) {
        return false;
      } else {
        final URI uri = file.getAbsoluteFile().toURI();
        final ModuleSubject module = mRoot.getModuleSubject();
        final URI modURI = module.getLocation();
        return !uri.equals(modURI);
      }
    }

    @Override
    public String getDescription()
    {
      return mDescription;
    }

    //#######################################################################
    //# Data Members
    private final String mExtension;
    private final String mDescription;
  }


  //#######################################################################
  //# Data Members
  private final ModuleWindowInterface mRoot;
  private JLabel mNameLabel;
  private SimpleExpressionInputCell mNameInput;
  private JLabel mModuleLabel;
  private ModuleInputCell mModuleInput;
  private JButton mFileChooserButton;
  private JButton mOkButton;
  private JPanel mMainPanel;

  private JPanel mErrorPanel;
  private ErrorLabel mErrorLabel;
  private JPanel mButtonsPanel;

  private InstanceSubject mInstance;
  private Object mInsertPosition;

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
  private static final InstanceSubject TEMPLATE =
    new InstanceSubject(new SimpleIdentifierSubject(""), "");
  private static final Transferable TRANSFERABLE = WatersDataFlavor
    .createTransferable(TEMPLATE);
  private static final String NAME_TOOLTIP =
    "Please enter a name for the instance.";

}
