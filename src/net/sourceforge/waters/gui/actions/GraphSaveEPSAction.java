//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   GraphSaveEPSAction
//###########################################################################
//# $Id: GraphSaveEPSAction.java,v 1.4 2007-12-04 03:22:54 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.io.File;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.net.MalformedURLException;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.EditorSurface;
import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.renderer.EPSGraphPrinter;
import net.sourceforge.waters.gui.renderer.ProxyShapeProducer;
import net.sourceforge.waters.model.marshaller.StandardExtensionFileFilter;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.ModuleProxy;

import org.supremica.gui.ide.IDE;
import org.supremica.properties.Config;


public class GraphSaveEPSAction
  extends WatersGraphAction
{

  //#########################################################################
  //# Constructor
  GraphSaveEPSAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Save as EPS ...");
    putValue(Action.SHORT_DESCRIPTION,
             "Save the currently viewed automaton in an " +
             "Encapsulated Postscript (EPS) file");
    putValue(Action.SMALL_ICON,
             new ImageIcon(IDE.class.getResource
                           ("/toolbarButtonGraphics/general/Print16.gif")));
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final ControlledSurface surface = getActiveControlledSurface();
    if (surface != null) {
      final IDE ide = getIDE();
      final ModuleProxy module = surface.getModule();
      // The name of the graph ...
      final EditorWindowInterface iface = getActiveEditorWindowInterface();
      final String name = iface.getComponent().getName();
      final JFileChooser chooser = getFileChooser(module, name);
      if (chooser.showSaveDialog(ide) != JFileChooser.APPROVE_OPTION) {
        return;
      }
      final File file = chooser.getSelectedFile();
      try {
        final File location = module.getFileLocation();
        if (location != null) {
          final File moddir = module.getFileLocation().getParentFile();
          final File epsdir = file.getParentFile();
          mChooserPathFollowsModule = epsdir.equals(moddir);
        }
      } catch (final MalformedURLException exception) {
        // JAR URL---no file---no preselection of directory.
      }
      final GraphProxy graph = surface.getDrawnGraph();
      final ProxyShapeProducer shaper = surface.getShapeProducer();
      final EPSGraphPrinter printer = new EPSGraphPrinter(graph, shaper, file);
      try {
        printer.print();
      } catch (final IOException exception) {
        ide.getDocumentContainerManager().showIOError(exception);
      }
    }
  }
   

  //#########################################################################
  //# Auxiliary Methods
  private JFileChooser getFileChooser(final ModuleProxy module,
                                      final String name)
  {
    if (mFileChooser == null) {
      mFileChooser = new JFileChooser();
      mFileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
      mFileChooser.setMultiSelectionEnabled(false);
      final FileFilter filter = 
        new StandardExtensionFileFilter(EPS,
                                        "Encapsulated Postscript (*.eps)");
      mFileChooser.addChoosableFileFilter(filter);
      mFileChooser.setFileFilter(filter);
      final File startdir = new File(Config.FILE_OPEN_PATH.getAsString());
      mFileChooser.setCurrentDirectory(startdir);
    }
    if (mChooserPathFollowsModule) {
      try {
        final File location = module.getFileLocation();
        if (location != null) {
          mFileChooser.setCurrentDirectory(location);
        }
      } catch (final MalformedURLException exception) {
        // JAR URL---no file---no preselection of directory.
      }
    }
    mFileChooser.setSelectedFile(new File(name + EPS));
    return mFileChooser;
  }


  //#########################################################################
  //# Data Members
  private JFileChooser mFileChooser = null;
  private boolean mChooserPathFollowsModule = true;

  private static final String EPS = ".eps";

}
