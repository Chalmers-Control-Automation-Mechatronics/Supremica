//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   AbstractSaveAction
//###########################################################################
//# $Id: AbstractSaveAction.java,v 1.2 2007-06-23 10:16:00 robi Exp $
//###########################################################################

package org.supremica.gui.ide.actions;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.marshaller.ProxyMarshaller;
import net.sourceforge.waters.model.marshaller.StandardExtensionFileFilter;
import net.sourceforge.waters.model.marshaller.WatersMarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.unchecked.Casting;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

import org.supremica.automata.Project;
import org.supremica.automata.IO.SupremicaUnmarshaller;
import org.supremica.gui.ide.AutomataContainer;
import org.supremica.gui.ide.DocumentContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;


/**
 * A common base class for saving actions.
 * This action class unifies the 'save' and 'save-as' actions for IDE
 * documents, ensuring that they share the same file chooser and also code.
 *
 * The action handles both Waters modules and Supremica projects,
 * and the conversion between these two formats when the user chooses a
 * different file filter.
 *
 * @author Robi Malik, Hugo Flordal
 */

public abstract class AbstractSaveAction
  extends net.sourceforge.waters.gui.actions.IDEAction
{

  //#########################################################################
  //# Constructor
  AbstractSaveAction(final IDE ide)
  {
    super(ide);
    setEnabled(false);
  }
    
    
  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  public void update(final EditorChangedEvent event)
  {
    switch (event.getKind()) {
    case MAINPANEL_SWITCH:
      updateEnabledStatus();
      break;
    default:
      break;
    }
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
    try {
      final File file = doc.getFileLocation();
      if (file == null) {
        invokeSaveAsAction();
      } else {
        saveDocument(file);
      }
    } catch (final MalformedURLException exception) {
      invokeSaveAsAction();
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
  //# Auxiliary Methods
  private JFileChooser getFileChooser()
  {
    final IDE ide = getIDE();
    final JFileChooser chooser = ide.getFileChooser();
    final DocumentManager manager = ide.getDocumentManager();
    final FileFilter modfilter =
      manager.findProxyMarshaller(ModuleProxy.class).getDefaultFileFilter();
    final FileFilter supfilter =
      manager.findProxyMarshaller(Project.class).getDefaultFileFilter();
    final FileFilter current = chooser.getFileFilter();
    chooser.setDialogType(JFileChooser.SAVE_DIALOG);
    chooser.setMultiSelectionEnabled(false);
    chooser.resetChoosableFileFilters();
    chooser.addChoosableFileFilter(modfilter);
    chooser.addChoosableFileFilter(supfilter);
    if (current == modfilter || current == supfilter) {
      chooser.setFileFilter(current);
    } else {
      chooser.setFileFilter(modfilter);
    }
    return chooser;
  }

  private void saveDocument(final FileFilter filter, final File file)
  {
    final IDE ide = getIDE();
    final DocumentManager manager = ide.getDocumentManager();
    final DocumentContainer container = ide.getActiveDocumentContainer();
    final DocumentProxy doc = container.getDocument();
    final Class<? extends DocumentProxy> clazz = doc.getClass();
    final ProxyMarshaller<? extends DocumentProxy> marshaller =
      manager.findProxyMarshaller(clazz);
    final FileFilter docfilter = marshaller.getDefaultFileFilter();
    if (docfilter == filter) {
      final String ext = marshaller.getDefaultExtension();
      final File extfile =
        StandardExtensionFileFilter.ensureDefaultExtension(file, ext);
      saveDocument(extfile);
    } else if (doc instanceof Project) {
      // Converting Supremica >> Waters ...
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
      if (container instanceof ModuleContainer) {
        final ModuleProxy module =
          container.getEditorPanel().getModuleSubject();
        marshalDocument(file, module);
      } else if (container instanceof AutomataContainer) {
        final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
        final ProductDESImporter importer = new ProductDESImporter(factory);
        final Project project = (Project) doc;
        final ModuleProxy module = importer.importModule(project);
        marshalDocument(file, module);
      } else {
        throw new ClassCastException
          ("Unknown document container type: " +
           container.getClass().getName() + "!");
      }
    } else if (doc instanceof ModuleProxy) {
      // Converting Waters >> Supremica ...
      if (ide.editorActive()) {
        container.getAnalyzerPanel().updateAutomata();
      }
      final Project project = container.getAnalyzerPanel().getVisualProject();
      marshalDocument(file, project);
    } else {
      throw new ClassCastException
        ("Unknown document type: " + clazz.getName() + "!");
    }
  }

  private void marshalDocument(final File file, final DocumentProxy doc)
  {
    final IDE ide = getIDE();
    final DocumentManager manager = ide.getDocumentManager();
    final Class<DocumentProxy> clazz = Casting.toClass(doc.getClass());
    final ProxyMarshaller<DocumentProxy> marshaller =
      manager.findProxyMarshaller(clazz);
    final String ext = marshaller.getDefaultExtension();
    final File extfile =
      StandardExtensionFileFilter.ensureDefaultExtension(file, ext);
    final String type = getTypeString(doc);
    try {
      marshaller.marshal(doc, extfile);
      ide.info(type + " saved to " + file);
    } catch (final WatersMarshalException exception) {
      JOptionPane.showMessageDialog(ide.getFrame(),
                                    "Error saving " + type + " file:" +
                                    exception.getMessage());
    } catch (final IOException exception) {
      JOptionPane.showMessageDialog(ide.getFrame(),
                                    "Error saving " + type + " file:" +
                                    exception.getMessage());
    }
  }

  private void saveDocument(final File file)
  {
    final IDE ide = getIDE();
    final DocumentContainer container = ide.getActiveDocumentContainer();
    final DocumentProxy doc = container.getDocument();
    final DocumentManager manager = ide.getDocumentManager();
    final String type = getTypeString(doc);
    try {
      manager.saveAs(doc, file);
      ide.info(type + " saved to " + file);
    } catch (final WatersMarshalException exception) {
      JOptionPane.showMessageDialog(ide.getFrame(),
                                    "Error saving " + type + " file:" +
                                    exception.getMessage());
    } catch (final IOException exception) {
      JOptionPane.showMessageDialog(ide.getFrame(),
                                    "Error saving " + type + " file:" +
                                    exception.getMessage());
    }
  }

  private String getTypeString(final DocumentProxy doc)
  {
    if (doc instanceof Project) {
      return "Supremica project";
    } else if (doc instanceof ModuleProxy) {
      return "Waters module";
    } else {
      throw new ClassCastException
        ("Unknown document type: " + doc.getClass().getName() + "!");
    }
  }

}
