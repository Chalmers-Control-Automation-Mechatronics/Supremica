//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   ImportDialog
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.gui.ide;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;

import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.gui.util.RaisedDialogPanel;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.marshaller.CopyingProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;


/**
 * An import dialog. The main import dialog is triggered by the IDE's main menu
 * and enables the user to import and convert external files containing a
 * hierarchy of modules. The dialog window consists of two text fields (that can
 * be set by text entry or using file choosers) for the name of the source file
 * to be imported and a target directory into which converted module files are
 * saved.
 *
 * @see CopyingProxyUnmarshaller
 * @author Robi Malik
 */

public class ImportDialog<D extends DocumentProxy> extends JDialog
{

  //#########################################################################
  //# Constructors
  public ImportDialog(final IDE ide,
      final List<CopyingProxyUnmarshaller<D>> importers, final File source,
      final File target)
  {
    super(ide);
    mIDE = ide;
    mImporters = importers;
    mFileFilters = new LinkedList<FileFilter>();
    for (final CopyingProxyUnmarshaller<D> importer : importers) {
      final Collection<FileFilter> filters = importer.getSupportedFileFilters();
      mFileFilters.addAll(filters);
    }
    mDocument = null;

    final JPanel main = new RaisedDialogPanel();
    final GridBagLayout gridbag = new GridBagLayout();
    main.setLayout(gridbag);
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.weightx = 0.0;
    constraints.weighty = 1.0;
    constraints.anchor = GridBagConstraints.WEST;
    constraints.fill = GridBagConstraints.NONE;
    constraints.insets = INSETS;

    final JLabel sourceLabel = new JLabel("Import from:");
    gridbag.setConstraints(sourceLabel, constraints);
    main.add(sourceLabel);
    constraints.gridy = 1;
    final JLabel targetLabel = new JLabel("Output directory:");
    gridbag.setConstraints(targetLabel, constraints);
    main.add(targetLabel);
    constraints.gridy = 0;
    constraints.gridx++;
    constraints.weightx = 3.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    mSourceInput = new JTextField(24);
    if (source != null) {
      final String text = source.toString();
      final int len = text.length();
      mSourceInput.setText(text);
      mSourceInput.setCaretPosition(0);
      mSourceInput.setSelectionStart(0);
      mSourceInput.setSelectionEnd(len);
    }
    gridbag.setConstraints(mSourceInput, constraints);
    main.add(mSourceInput);
    final SourceChangeHandler changehandler = new SourceChangeHandler();
    mSourceInput.getDocument().addDocumentListener(changehandler);
    constraints.gridy = 1;
    mTargetInput = new JTextField(24);
    if (target != null) {
      mTargetInput.setText(target.toString());
      mTargetInput.setCaretPosition(0);
    }
    gridbag.setConstraints(mTargetInput, constraints);
    main.add(mTargetInput);
    constraints.gridy = 0;
    constraints.gridx++;
    constraints.weightx = 0.0;
    constraints.fill = GridBagConstraints.NONE;
    final JButton sourceBrowse = new JButton("...");
    sourceBrowse.setFocusable(false);
    sourceBrowse.addActionListener(new SourceBrowseHandler());
    gridbag.setConstraints(sourceBrowse, constraints);
    main.add(sourceBrowse);
    constraints.gridy = 1;
    final JButton targetBrowse = new JButton("...");
    targetBrowse.setFocusable(false);
    targetBrowse.addActionListener(new TargetBrowseHandler());
    gridbag.setConstraints(targetBrowse, constraints);
    main.add(targetBrowse);

    final JPanel buttons = new JPanel();
    mOKButton = new JButton("Import");
    mOKButton.setRequestFocusEnabled(false);
    mOKButton.addActionListener(new ImportHandler());
    buttons.add(mOKButton);
    changehandler.enableOKButton();
    final JRootPane root = getRootPane();
    root.setDefaultButton(mOKButton);
    final Action cancelAction = DialogCancelAction.getInstance();
    final JButton cancelButton = new JButton(cancelAction);
    DialogCancelAction.register(this);
    cancelButton.setRequestFocusEnabled(false);
    buttons.add(cancelButton);

    final BorderLayout layout = new BorderLayout();
    final Container contents = getContentPane();
    contents.setLayout(layout);
    contents.add(main, BorderLayout.CENTER);
    contents.add(buttons, BorderLayout.SOUTH);

    pack();

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
  }

  //##########################################################################
  //# Simple Access
  public File getSource()
  {
    final String text = mSourceInput.getText();
    return text == null ? null : new File(text);
  }

  public File getTarget()
  {
    final String text = mTargetInput.getText();
    return text == null ? null : new File(text);
  }

  public D getDocument()
  {
    return mDocument;
  }


  //##########################################################################
  //# Inner Class SourceChangeHandler
  private class SourceChangeHandler implements DocumentListener
  {

    //########################################################################
    //# Interface javax.swing.event.DocumentListener
    public void changedUpdate(final DocumentEvent event)
    {
      enableOKButton();
    }

    public void insertUpdate(final DocumentEvent event)
    {
      enableOKButton();
    }

    public void removeUpdate(final DocumentEvent event)
    {
      enableOKButton();
    }

    //########################################################################
    //# Auxiliary Methods
    private void enableOKButton()
    {
      final String text = mSourceInput.getText();
      final boolean enable = !text.equals("");
      mOKButton.setEnabled(enable);
    }

  }


  //##########################################################################
  //# Inner Class SourceBrowseHandler
  private class SourceBrowseHandler implements ActionListener
  {
    //########################################################################
    //# Interface java.awt.event.ActionListener
    public void actionPerformed(final ActionEvent event)
    {
      // Get the state and dialog ...
      final JFileChooser chooser = mIDE.getFileChooser();
      chooser.setDialogType(JFileChooser.OPEN_DIALOG);
      chooser.setMultiSelectionEnabled(false);
      final String text = mSourceInput.getText();
      if (!text.equals("")) {
        final File file = new File(text);
        chooser.setSelectedFile(file);
      }
      chooser.resetChoosableFileFilters();
      final FileFilter current = chooser.getFileFilter();
      boolean reselect = false;
      for (final FileFilter filter : mFileFilters) {
        chooser.addChoosableFileFilter(filter);
        if (filter == current) {
          reselect = true;
        }
      }
      // Select the first filter ...
      final FileFilter first =
          reselect ? current : mFileFilters.iterator().next();
      chooser.setFileFilter(first);
      // Show the dialog ...
      final int choice = chooser.showOpenDialog(ImportDialog.this);
      // Get the filename ...
      if (choice == JFileChooser.APPROVE_OPTION) {
        final File file = chooser.getSelectedFile();
        mSourceInput.setText(file.toString());
        mSourceInput.setCaretPosition(0);
      }
    }
  }


  //##########################################################################
  //# Inner Class TargetBrowseHandler
  private class TargetBrowseHandler implements ActionListener
  {
    //########################################################################
    //# Interface java.awt.event.ActionListener
    public void actionPerformed(final ActionEvent event)
    {
      final JFileChooser chooser = new JFileChooser();
      final String text = mTargetInput.getText();
      if (!text.equals("")) {
        final File file = new File(text);
        chooser.setSelectedFile(file);
      }
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      final int choice = chooser.showSaveDialog(ImportDialog.this);
      if (choice == JFileChooser.APPROVE_OPTION) {
        final File file = chooser.getSelectedFile();
        mTargetInput.setText(file.toString());
        mTargetInput.setCaretPosition(0);
      }
    }
  }


  //##########################################################################
  //# Inner Class ImportHandler
  private class ImportHandler implements ActionListener
  {
    //########################################################################
    //# Interface java.awt.event.ActionListener
    public void actionPerformed(final ActionEvent event)
    {
      String msg = null;
      try {
        final String filename = mSourceInput.getText();
        final int dotpos = filename.lastIndexOf('.');
        if (dotpos >= 0) {
          final String ext = filename.substring(dotpos);
          for (final CopyingProxyUnmarshaller<D> importer : mImporters) {
            if (importer.getSupportedExtensions().contains(ext)) {
              final String target = mTargetInput.getText();
              final File outdir = new File(target);
              importer.setOutputDirectory(outdir);
              final File file = new File(filename);
              final URI uri = file.toURI();
              mDocument = importer.unmarshalCopying(uri);
              dispose();
              return;
            }
          }
          msg = "Can't find importer for '" + filename + "'!";
        } else {
          msg = "Can't determine type of file '" + filename + "'!";
        }
      } catch (final IOException exception) {
        msg = exception.getMessage();
      } catch (final WatersMarshalException exception) {
        msg = exception.getMessage();
      } catch (final WatersUnmarshalException exception) {
        msg = exception.getMessage();
      }
      JOptionPane.showMessageDialog(ImportDialog.this, msg,
          "Error while Importing!", JOptionPane.ERROR_MESSAGE);
    }
  }


  //#########################################################################
  //# Data Members
  private final IDE mIDE;
  private final List<CopyingProxyUnmarshaller<D>> mImporters;
  private final List<FileFilter> mFileFilters;
  private final JTextField mSourceInput;
  private final JTextField mTargetInput;
  private final JButton mOKButton;

  private D mDocument;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
  private static final Insets INSETS = new Insets(2, 4, 2, 4);

}
