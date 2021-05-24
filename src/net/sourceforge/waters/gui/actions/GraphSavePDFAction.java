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

package net.sourceforge.waters.gui.actions;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import net.sourceforge.waters.gui.GraphEditorPanel;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.marshaller.StandardExtensionFileFilter;
import net.sourceforge.waters.model.module.ModuleProxy;

import org.supremica.gui.ide.ComponentEditorPanel;
import org.supremica.gui.ide.IDE;
import org.supremica.properties.Config;


public class GraphSavePDFAction
  extends WatersGraphAction
{

  //#########################################################################
  //# Constructor
  GraphSavePDFAction(final IDE ide)
  {
    super(ide);
    putValue(Action.NAME, "Save as PDF ...");
    putValue(Action.SHORT_DESCRIPTION,
             "Save the currently viewed automaton in a PDF file");
    putValue(Action.SMALL_ICON, IconAndFontLoader.ICON_TOOL_PRINT_PDF);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    final GraphEditorPanel surface = getActiveGraphEditorPanel();
    if (surface != null) {
      final IDE ide = getIDE();
      final ModuleProxy module = surface.getModule();
      // The name of the graph ...
      final ComponentEditorPanel panel = getActiveComponentEditorPanel();
      final String name = panel.getComponent().getName();
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
      final int width = surface.getWidth();
      final int height = surface.getHeight();
      final Document document = new Document(new com.lowagie.text.Rectangle(width, height));

      try
      {
          final PdfWriter writer= PdfWriter.getInstance(document,  new FileOutputStream(file));

          document.addAuthor("Supremica");
          document.open();

          final PdfContentByte cb = writer.getDirectContent();
          final PdfTemplate tp = cb.createTemplate(width, height);
          final Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper());
          surface.print(g2);
          //Rectangle2D rectangle2D = new Rectangle2D.Double(0, 0, width, height);
          //chart.draw(g2, rectangle2D);
          g2.dispose();
          cb.addTemplate(tp, 0, 0);

      }
      catch (final DocumentException de)
      {
          System.err.println(de.getMessage());
      }
      catch (final IOException ioe)
      {
          System.err.println(ioe.getMessage());
      }

      document.close();
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
        new StandardExtensionFileFilter("PDF (*.pdf)", PDF);
      mFileChooser.addChoosableFileFilter(filter);
      mFileChooser.setFileFilter(filter);
      final File startdir = Config.FILE_OPEN_PATH.getValue();
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
    mFileChooser.setSelectedFile(new File(name + PDF));
    return mFileChooser;
  }


  //#########################################################################
  //# Data Members
  private JFileChooser mFileChooser = null;
  private boolean mChooserPathFollowsModule = true;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

  private static final String PDF = ".pdf";

}
