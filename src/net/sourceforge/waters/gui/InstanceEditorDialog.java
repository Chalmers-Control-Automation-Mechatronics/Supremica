//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   InstanceEditorDialog
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.gui.util.RaisedDialogPanel;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.InstanceSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.ParameterBindingSubject;
import net.sourceforge.waters.subject.module.PlainEventListSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.xsd.module.ScopeKind;


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
    setTitle("Instance Component Editor");
    mRoot = root;
    mInstance = inst;
    createComponents();
    layoutComponents();
    setLocationRelativeTo(mRoot.getRootWindow());
    mNameInput.requestFocusInWindow();
    setVisible(true);
  }

  private void createComponents()
  {
    setModal(true);
    setLocationRelativeTo(null);

    InstanceSubject template;
    if (mInstance == null) {
      try {
        template = TEMPLATE;
        final SelectionOwner panel = mRoot.getComponentsPanel();
        final List<InsertInfo> inserts = panel.getInsertInfo(TRANSFERABLE);
        final InsertInfo insert = inserts.get(0);
        mInsertPosition = insert.getInsertPosition();
      } catch (final IOException exception) {
        throw new WatersRuntimeException(exception);
      } catch (final UnsupportedFlavorException exception) {
        throw new WatersRuntimeException(exception);
      }
    } else {
      template = mInstance;
    }

    final ActionListener commithandler = new ActionListener() {
      public void actionPerformed(final ActionEvent event)
      {
        commitDialog();
      }
    };

    mMainPanel = new RaisedDialogPanel();
    final ExpressionParser parser = mRoot.getExpressionParser();
    mNameLabel = new JLabel("Name: ");
    final String oldname = template.getName();
    final SimpleIdentifierSubject ident =
      new SimpleIdentifierSubject(oldname);
    final SimpleIdentifierInputParser nameparser =
      new SimpleIdentifierInputParser(ident, parser);
    mNameInput = new SimpleExpressionCell(ident, nameparser);
    mNameInput.addActionListener(commithandler);
    mNameInput.setToolTipText("Enter the name");

    mModuleLabel = new JLabel("Module:");
    mModuleInput = new JFormattedTextField(16);
    mModuleInput.setText(template.getModuleName());
    mModuleInput.addActionListener(commithandler);
    mModuleInput.setToolTipText("Enter or select module");

    mFileChooserButton = new JButton(" ... ");
    mFileChooserButton.addActionListener(new ActionListener() {
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

    // Buttons panel ...
    mButtonsPanel = new JPanel();
    final JButton okButton = new JButton("OK");
    okButton.setRequestFocusEnabled(false);
    okButton.addActionListener(commithandler);
    mButtonsPanel.add(okButton);

    final JButton cancelButton = new JButton("Cancel");
    cancelButton.setRequestFocusEnabled(false);
    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent event)
      {
        dispose();
      }
    });
    mButtonsPanel.add(cancelButton);

    final JRootPane root = getRootPane();
    root.setDefaultButton(okButton);
    DialogCancelAction.register(this);
    //updateExpressionEnabled();
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

  public void chooseFile()
  {
    final JFileChooser fileChooser = mRoot.getRootWindow().getFileChooser();
    fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
    fileChooser.setMultiSelectionEnabled(false);
    final String text = mModuleInput.getText();
    final File userInput = new File(text);
    fileChooser.setSelectedFile(userInput);
    fileChooser.resetChoosableFileFilters();

    // Show the dialog ...
    final int choice = fileChooser.showOpenDialog(InstanceEditorDialog.this);
    // Get the filename ...
    if (choice == JFileChooser.APPROVE_OPTION) {
      final File file = fileChooser.getSelectedFile();
      final URI fileUri = file.toURI();
      final ModuleSubject module = mRoot.getModuleSubject();
      final URI moduleUri = module.getLocation();
      final URI relativePath = relativise(fileUri, moduleUri);
      final String name = relativePath.getPath();
      mModuleInput.setText(name.substring(0, name.length() - 5));
      mModuleInput.setCaretPosition(0);
    }
  }

  private boolean moduleExists(final String moduleName){
    final ModuleSubject module = mRoot.getModuleSubject();
    final URI moduleUri = module.getLocation();
    final String moduleUriName = moduleUri.toString();
    final String[] moduleArray = moduleUriName.split("/");
    final String[] array = moduleName.split("/");
    String fileName = "";
    int numDir = moduleArray.length -1;
    int i = 0;
    while(i < array.length){
      if(array[i].equals("..")){
        i++;
      }else{
        break;
      }
    }
    numDir -= i;
    int x = 1;
    while(x < numDir){
      fileName += "/" + moduleArray[x];
      x++;
    }
    while(i < array.length){
      fileName += "/" + array[i];
      i++;
    }
    fileName += ".wmod";
    final File file = new File(fileName);
    return file.exists();
  }


  public void commitDialog()
  {
    if (isInputLocked()) {
      // nothing
    } else {
      final IdentifierProxy newName = (IdentifierProxy) mNameInput.getValue();
      final String moduleName = mModuleInput.getText();

      if(!moduleExists(moduleName)){
        mNameInput.getErrorDisplay().displayError("File does not exist");
        return;
      }

      final SelectionOwner panel = mRoot.getComponentsPanel();
      final ModuleProxyCloner cloner =
        ModuleSubjectFactory.getCloningInstance();
      InstanceSubject template = (InstanceSubject) cloner.getClone(mInstance);
      if (mInstance == null) {
        template = new InstanceSubject(newName, moduleName);
        final ModuleSubject module = mRoot.getModuleSubject();
        final DocumentManager docman = mRoot.getRootWindow().getDocumentManager();
        URI uri;
        try {
          uri = docman.resolve(module, moduleName, ModuleProxy.class);
        } catch (final WatersUnmarshalException exception1) {
          throw new WatersRuntimeException(exception1);
        }
        final File filename = new File(uri);
        final DocumentManager manager =
          mRoot.getRootWindow().getDocumentManager();
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
        final String oldname = mInstance.getName();
        final boolean nameChange = !newName.toString().equals(oldname);
        final boolean moduleChange =
          !moduleName.equals(template.getModuleName());
        if (nameChange || moduleChange) {
          template.setModuleName(moduleName);
          template.setIdentifier((IdentifierSubject) newName);
          final Command command = new EditCommand(mInstance, template, panel);
          mRoot.getUndoInterface().executeCommand(command);
        }
      }
      dispose();
    }
  }

  private boolean isInputLocked()
  {
    // TODO
    return mNameInput.isFocusOwner() && !mNameInput.shouldYieldFocus();
  }

  private URI relativise(final URI file, final URI module)
  {
    String product = file.toString();
    final String filePath = product;
    final String modulePath = module.toString();
    final String[] fileArray = filePath.split("/");
    final String[] moduleArray = modulePath.split("/");
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
        try {
          return new URI(product);
        } catch (final URISyntaxException exception) {
          throw new WatersRuntimeException(exception);
        }
      }
      i++;
    }
    return file;
  }

  //#######################################################################
  //# Data Members
  private final ModuleWindowInterface mRoot;
  private JLabel mNameLabel;
  private SimpleExpressionCell mNameInput;
  private JLabel mModuleLabel;
  private JFormattedTextField mModuleInput;
  private JButton mFileChooserButton;
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

}
