//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui.dialog
//# CLASS:   InstanceEditorDialog
//###########################################################################
//# $Id$
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

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.ModuleWindowInterface;
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
import net.sourceforge.waters.subject.base.UndoInfo;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.InstanceSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.ParameterBindingSubject;
import net.sourceforge.waters.subject.module.PlainEventListSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.xsd.module.ScopeKind;


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
    setTitle("Instance Editor");
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
    final IdentifierProxy oldname = template.getIdentifier();
    final ModuleContext context = mRoot.getModuleContext();
    final FormattedInputParser nameparser =
      new ComponentNameInputParser(oldname, context, parser);
    mNameInput = new SimpleExpressionCell(oldname, nameparser);
    mNameInput.addActionListener(commithandler);
    mNameInput.setToolTipText("Enter the name");

    mModuleLabel = new JLabel("Module:");
    mModuleInput = new JFormattedTextField();
    mModuleInput.addActionListener(commithandler);
    mModuleInput.setToolTipText("Enter or select a .wmod file");
    mVerifier = new ModuleVerifier();
    final DefaultFormatter formatter =
      new DefaultFormatter();
    formatter.setOverwriteMode(false);
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
    mButtonsPanel.add(mOkButton);

    final Action pressOK = new AbstractAction() {
      private static final long serialVersionUID = 1L;
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
      final ModuleProxyCloner cloner =
        ModuleSubjectFactory.getCloningInstance();
      final IdentifierSubject inputIdent =
        (IdentifierSubject) mNameInput.getValue();
      final IdentifierSubject newIdent =
        (IdentifierSubject) cloner.getClone(inputIdent);
      final String moduleName = mModuleInput.getText();
      if (moduleName.equals("")){
        mModuleInput.requestFocusInWindow();
        mNameInput.setErrorMessage("Enter or select a .wmod file!");
        return;
      }
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
    return mNameInput.isFocusOwner() && !mNameInput.shouldYieldFocus() ||
      mModuleInput.isFocusOwner() && !mVerifier.shouldYieldFocus(mModuleInput);
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


  //#########################################################################
  //# Inner Class ModuleVerifierVerifier
  private class ModuleVerifier
    extends InputVerifier
  {

    //#######################################################################
    //# Overrides for class javax.swing.InputVerifier
    public boolean verify(final JComponent input)
    {
      final ModuleSubject module = mRoot.getModuleSubject();
      final URI moduleUri = module.getLocation();
      final JFormattedTextField textfield = (JFormattedTextField) input;
      final String text = textfield.getText();
      File file = null;
      try {
        if (moduleUri != null) {
          String mod = moduleUri.getPath();
          mod = mod.substring(0, mod.lastIndexOf("/") + 1);
          mod += text + ".wmod";
          file = new File(mod);

          final JFormattedTextField.AbstractFormatter formatter =
            textfield.getFormatter();
          formatter.stringToValue(text);
          if (moduleUri.getPath().compareTo(file.getAbsolutePath()) == 0) {
            mMessage = "Cannot make recursive instantiations of the module!";
            return false;
          }
        } else {
          file = new File(text + ".wmod");
        }

        if (text.length() != 0) {
          final DocumentManager docman =
            mRoot.getRootWindow().getDocumentManager();
          docman.load(moduleUri, text, ModuleProxy.class);
        } else {
          mMessage = "Enter or select a .wmod file!";
          return false;
        }
        return true;
      } catch (final WatersUnmarshalException exception) {
        mMessage = "File or directory does not exist in .wmod format!";
        if (file.exists()) {
          mMessage = "File is not a correctly formatted .wmod file!";
        }
        return false;
      } catch (final Exception exception) {
        mMessage = exception.getMessage();
        return false;
      }
    }

    public boolean shouldYieldFocus(final JComponent input)
    {
      mNameInput.clearErrorMessage();
      if (verify(input)) {
        return true;
      }
      mNameInput.setErrorMessage(mMessage);
      return false;
    }

  }

  //#########################################################################
  //# Inner Class ModuleFileFilter
  private class ModuleFileFilter extends FileFilter{

    public boolean accept(final File file)
    {
      final ModuleSubject module = mRoot.getModuleSubject();
      final URI modURI = module.getLocation();
      //filter out the current module
      if (modURI != null && file.getAbsolutePath().compareTo(modURI.getPath()) == 0) {
        return false;
      }
      //filter out files that aren't .wmod
      if(file.isFile() && !file.getName().endsWith(".wmod")){
        return false;
      }
      return true;
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
