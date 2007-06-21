//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   AbstractSaveAsAction
//###########################################################################
//# $Id: AbstractSaveAction.java,v 1.1 2007-06-21 20:56:53 robi Exp $
//###########################################################################

package org.supremica.gui.ide.actions;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import net.sourceforge.waters.gui.WmodFileFilter;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

import org.supremica.automata.Automata;
import org.supremica.automata.IO.AutomataToXML;
import org.supremica.automata.IO.SupremicaUnmarshaller;
import org.supremica.gui.SupremicaXMLFileFilter;
import org.supremica.gui.ide.AutomataContainer;
import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;


public abstract class AbstractSaveAction
  extends net.sourceforge.waters.gui.actions.IDEAction
{

  //#########################################################################
  //# Constructor
  AbstractSaveAction(final IDE ide)
  {
    super(ide);
  }
    
    
  //#########################################################################
  //# Enabling and Disabling
  public boolean updateEnabledStatus()
  {
    final IDE ide = getIDE();
    final DocumentContainer container = ide.getActiveDocumentContainer();
    final boolean enabled = container != null;
    setEnabled(enabled);
    if (enabled) {
      final String type =
        container instanceof ModuleContainer ? "module" : "project";
      final String text = getShortDescription(type);
      putValue(Action.SHORT_DESCRIPTION, text);
    }
    return enabled;
  }

  abstract String getShortDescription(final String type);


  //#########################################################################
  //# Unified Methods for Save and Save-As
  void invokeSaveAction()
  {
    final IDE ide = getIDE();
    final DocumentContainer container = ide.getActiveDocumentContainer();
    final DocumentProxy doc = container.getDocument();
    File file = null;
    try {
      file = doc.getFileLocation();
    } catch (final MalformedURLException exception) {
      // No proper file---keep it null and invoke "save as" ...
    }
    if (file == null) {
      invokeSaveAsAction();
    } else if (doc instanceof ModuleProxy) {
      saveDocument(WmodFileFilter.getInstance(), file);
    } else if (doc instanceof Automata) {
      saveDocument(SupremicaXMLFileFilter.getInstance(), file);
    } else {
      throw new ClassCastException("Unexpected document type: " +
                                   doc.getClass().getName() + "!");
    }
  }

  void invokeSaveAsAction()
  {
    final JFileChooser chooser = getFileChooser();
    final IDE ide = getIDE();
    final int returnVal = chooser.showSaveDialog(ide.getFrame());
    if (returnVal != JFileChooser.APPROVE_OPTION) {
      return;
    }
    final FileFilter filter = chooser.getFileFilter();
    final File file = chooser.getSelectedFile();
    saveDocument(filter, file);
  }


  //#########################################################################
  //# Accessing the File Chooser
  JFileChooser getFileChooser()
  {
    final IDE ide = getIDE();
    final Actions actions = ide.getActions();
    final SaveAsAction saveas =
      (SaveAsAction) actions.getAction(SaveAsAction.class);
    return saveas.getFileChooser();
  }


  //#########################################################################
  //# Auxiliary Methods
  private void saveDocument(final FileFilter filter, File file)
  {
    final IDE ide = getIDE();
    final DocumentContainer container = ide.getActiveDocumentContainer();
    // Branch depending on chosen file filter
    if (filter instanceof SupremicaXMLFileFilter) {
      // Supremica XML
      if (!filter.accept(file)) {
        file = new File(file.getPath() + "." +
                        SupremicaXMLFileFilter.SUPXML);
      }
      // If editor active, update analyzer
      if (ide.editorActive()) {
        container.getAnalyzerPanel().updateAutomata();
      }
      saveSupFile(file, container.getAnalyzerPanel().getAllAutomata());
    } else if (filter instanceof WmodFileFilter) {
      // Waters WMOD
      if (!filter.accept(file)) {
        file = new File(file.getPath() + "." + WmodFileFilter.WMOD);
      }
      // If analyzer active, check if there are unsupported features
      // in the project ...
      if (ide.analyzerActive() &&
          !SupremicaUnmarshaller.validate
            (container.getAnalyzerPanel().getVisualProject())) {
        final int choice = JOptionPane.showConfirmDialog
          (ide.getFrame(), "This project contains attributes not supported by the WMOD-format.\nDo you want to save (and lose the unsupported features)?", "Warning", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
          return;
        }
      }
      final ModuleProxy module;
      if (container instanceof ModuleContainer) {
        module = container.getEditorPanel().getModuleSubject();
      } else if (container instanceof AutomataContainer) {
        final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
        final ProductDESImporter importer = new ProductDESImporter(factory);
        final ProductDESProxy des =
          container.getAnalyzerPanel().getAllAutomata();
        module = importer.importModule(des);
      } else {
        throw new ClassCastException
          ("Unknown document container type: " +
           container.getClass().getName() + "!");
      }
      saveWmodFile(file, module);
    } else {
      throw new ClassCastException
        ("Unknown file filter type: " + filter.getClass().getName() + "!");
    }
  }

  private void saveSupFile(final File file, final Automata automata)
  {
    final IDE ide = getIDE();
    try {
      final AutomataToXML exporter = new AutomataToXML(automata);
      exporter.serialize(file.getAbsolutePath());
      ide.info("Supremica project saved to " + file);
    } catch (final IOException exception) {
      JOptionPane.showMessageDialog(ide.getFrame(),
                                    "Error saving Supremica file:" +
                                    exception.getMessage());
    }
  }
    
  private void saveWmodFile(final File file, final ModuleProxy module)
  {
    final IDE ide = getIDE();
    try {
      DocumentManager documentManager = ide.getDocumentManager();
      documentManager.saveAs(module, file);
      ide.info("Waters module saved to " + file);
    } catch (final WatersMarshalException exception) {
      JOptionPane.showMessageDialog(ide.getFrame(),
                                    "Error saving module file:" +
                                    exception.getMessage());
    } catch (final IOException exception) {
      JOptionPane.showMessageDialog(ide.getFrame(),
                                    "Error saving module file:" +
                                    exception.getMessage());
    }
  }

}
