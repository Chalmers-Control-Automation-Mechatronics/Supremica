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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
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
    mModuleInput = new JFormattedTextField();
    mModuleInput.addActionListener(commithandler);
    mModuleInput.setToolTipText("Enter or select a .wmod file");
    mVerifier = new ModuleVerifier();
    final JFormattedTextField.AbstractFormatter formatter =
      new ModuleFormatter();
    final DefaultFormatterFactory factory =
      new DefaultFormatterFactory(formatter);
    mModuleInput.setFormatterFactory(factory);
    mModuleInput.setInputVerifier(mVerifier);
    mModuleInput.setText(template.getModuleName());

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
    mOkButton = new JButton("OK");
    mOkButton.setRequestFocusEnabled(false);
    mOkButton.addActionListener(commithandler);
    final Action pressOK = new AbstractAction() {
      private static final long serialVersionUID = 1L;
      public void actionPerformed(final ActionEvent e)
      {
        commitDialog();
      }
    };
    mOkButton.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "pressOK");
    mOkButton.getActionMap().put("pressOK", pressOK);
    mButtonsPanel.add(mOkButton);

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
      mModuleInput.setText(name.substring(0, name.length() - 5));
    }
    mModuleInput.requestFocusInWindow();
  }


  private void commitDialog()
  {
    if (isInputLocked()) {
      // nothing
    } else {
      final IdentifierProxy newName = (IdentifierProxy) mNameInput.getValue();
      final String moduleName = mModuleInput.getText();

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
    return mNameInput.isFocusOwner() && !mNameInput.shouldYieldFocus() ||
      mModuleInput.isFocusOwner() && !mVerifier.shouldYieldFocus(mModuleInput);
  }

  private URI relativise(final URI file, final URI module)
  {
    String product = file.toString();
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
    try {
      return new URI(product);
    } catch (final URISyntaxException exception) {
      throw new WatersRuntimeException(exception);
    }
  }


  //#########################################################################
  //# Inner Class ModuleFormatterFormatter
  private class ModuleFormatter extends DefaultFormatter
  {

    //#######################################################################
    //# Constructors
    private ModuleFormatter()
    {
      setCommitsOnValidEdit(false);
      mMessage = "";
    }


    //#######################################################################
    //# Overrides for class javax.swing.text.DefaultFormatter
    public Object stringToValue(final String text)
      throws java.text.ParseException
    {
      if (text.length() != 0) {
          final ModuleSubject module = mRoot.getModuleSubject();
          final URI moduleUri = module.getLocation();
        try {
          final DocumentManager docman = mRoot.getRootWindow().getDocumentManager();
          final Object value = docman.load(moduleUri, text, ModuleProxy.class);
          return value;
        } catch (final WatersUnmarshalException exception) {
          mMessage = "File or directory does not exist in .wmod format!";
          //TODO if <enter> was pressed, we have to make sure the message is displayed
          String mod = moduleUri.getPath();
          mod = mod.substring(0, mod.lastIndexOf("/") + 1);
          mod += text + ".wmod";
          final File file = new File(mod);
          if(file.exists()){
            mMessage = "File is not a correctly formatted .wmod file!";
          }
          throw new java.text.ParseException(mMessage, 0);
        } catch (final IOException exception) {
          mNameInput.setErrorMessage(exception.getMessage());
        }
      } else {
        mMessage = "Empty input!";
        throw new java.text.ParseException(mMessage, 0);
      }
      return mModuleInput.getValue();
    }

    public String valueToString(final Object value)
    {
      if (value == null) {
        return "";
      } else {
        final ModuleSubject mod = (ModuleSubject)value;
        return mod.getName();
      }
    }


    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;
  }


  //#########################################################################
  //# Inner Class ModuleVerifierVerifier
  private class ModuleVerifier
    extends InputVerifier
  {

    //#######################################################################
    //# Overrides for class javax.swing.InputVerifier
    public boolean verify(final JComponent input)
    {
      //TODO documentation says this has to be called from shouldYieldFocus but is it even needed?
      try {
        final JFormattedTextField textfield = (JFormattedTextField) input;
        final JFormattedTextField.AbstractFormatter formatter =
          textfield.getFormatter();
        final String text = textfield.getText();
        formatter.stringToValue(text);
        return true;
      } catch (final Exception exception) {
        return false;
      }
    }

    public boolean shouldYieldFocus(final JComponent input)
    {
      mNameInput.clearErrorMessage();
      final JFormattedTextField textfield = (JFormattedTextField) input;
      try {
        textfield.commitEdit();
        return true;
      } catch (final java.text.ParseException exception) {
        mNameInput.setErrorMessage(mMessage);
        return false;
      }
    }

  }

  //#########################################################################
  //# Inner Class ModuleFileFilter
  private class ModuleFileFilter extends FileFilter{

    public boolean accept(final File file)
    {
      final DocumentManager docman = mRoot.getRootWindow().getDocumentManager();
      try {
        if(file.isDirectory()){
          return true;
        }
        final ModuleSubject module = mRoot.getModuleSubject();
        final Object value = docman.load(file);
        if(value.equals(module)){
          return false;
        }
        return true;
      } catch (final WatersUnmarshalException exception) {
        return false;
      } catch (final IOException exception) {
        return false;
      }
    }

    public String getDescription()
    {
      return "Waters Module Files [*.wmod]";
    }

  }

  //#######################################################################
  //# Data Members
  private final ModuleWindowInterface mRoot;
  private JLabel mNameLabel;
  private SimpleExpressionCell mNameInput;
  private JLabel mModuleLabel;
  private JFormattedTextField mModuleInput;
  private JButton mFileChooserButton;
  private JButton mOkButton;
  private JPanel mMainPanel;
  private ModuleVerifier mVerifier;

  private JPanel mErrorPanel;
  private ErrorLabel mErrorLabel;
  private JPanel mButtonsPanel;
  private String mMessage;

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
