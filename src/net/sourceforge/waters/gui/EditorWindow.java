//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorWindow
//###########################################################################
//# $Id: EditorWindow.java,v 1.33 2006-10-07 20:20:12 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.awt.print.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.swing.*;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.UndoRedoEvent;
import net.sourceforge.waters.model.base.IndexedList;
import net.sourceforge.waters.subject.base.NamedSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;

import org.supremica.gui.GraphicsToClipboard;


// Printing
import java.awt.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import java.util.Locale;
import java.net.URI;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;


public class EditorWindow
  extends JFrame
  implements EditorWindowInterface
{

  //#########################################################################
  //# Constructor
  public EditorWindow(final String title,
                      final ModuleSubject module,
                      final SimpleComponentSubject subject,
                      final ModuleWindow root,
                      final UndoInterface undoInterface)
    throws GeometryAbsentException
  {
    mUndoInterface = undoInterface;
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setTitle(title);

    toolbar = new EditorToolbar();
    surface = new ControlledSurface(subject.getGraph(), module, this, toolbar);

    surface.setPreferredSize(new Dimension(500, 500));
    surface.setMinimumSize(new Dimension(0, 0));

    mEventPane = new EditorEvents(root, subject, this);
    menu = new EditorMenu(surface, this);

    mModuleWindow = root;

    final Container panel = getContentPane();
    final GridBagLayout gridbag = new GridBagLayout();
    final GridBagConstraints constraints = new GridBagConstraints();

    constraints.gridy = 0;
    constraints.weighty = 1.0;
    constraints.anchor = GridBagConstraints.NORTH;

    panel.setLayout(gridbag);
    gridbag.setConstraints(toolbar, constraints);
    panel.add(toolbar);

    scrollsurface = new JScrollPane(surface);
    final JScrollPane scrollevents = new JScrollPane(mEventPane);
    final JViewport viewevents = scrollevents.getViewport();
    /*surface.addComponentListener(new ComponentAdapter()
    {
      public void componentResized(ComponentEvent e)
      {
        scrollsurface.getHorizontalScrollBar().getModel().setMinimum(0);
        scrollsurface.getVerticalScrollBar().getModel().setMinimum(0);
      }
    });*/

    /* EFA variable pane
    final JScrollPane scrollvariables = new JScrollPane(mEventPane);
    final JViewport viewvariables = scrollvariables.getViewport();
    final JSplitPane subSplit = new JSplitPane
      (JSplitPane.VERTICAL_SPLIT, scrollvariables, scrollevents);
    viewvariables.setBackground(Color.WHITE);
    subSplit.setResizeWeight(1.0);
    */
                
    final JSplitPane split = new JSplitPane
      (JSplitPane.HORIZONTAL_SPLIT, scrollsurface, scrollevents);
                
    viewevents.setBackground(Color.WHITE);
    split.setResizeWeight(1.0);

    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.BOTH;

    gridbag.setConstraints(split, constraints);
    panel.add(split);
    setJMenuBar(menu);
    pack();

    // Try to set the divider location so the event panel is displayed
    // at its preferred size.
    final int splitwidth = split.getSize().width;
    final int surfacewidth = surface.getSize().width;
    final int eventswidth = mEventPane.getSize().width;
    final int separatorwidth = splitwidth - surfacewidth - eventswidth;
    final int halfwidth = (splitwidth - separatorwidth) >> 1;
    if (halfwidth > 0) {
      final int prefeventswidth = mEventPane.getPreferredSize().width;
      final int setwidth = Math.min(prefeventswidth, halfwidth);
      final int divider = splitwidth - setwidth - separatorwidth;
      split.setDividerLocation(divider);
    }

    mModule = module;
    mSubject = subject;
    surface.createOptions(this);

    setVisible(true);
  }

  public boolean isSaved()
  {
    return isSaved;
  }

  public void setSaved(boolean s)
  {
    isSaved = s;
  }

  public JFrame getFrame()
  {
    return (JFrame) this;
  }

  public ControlledSurface getControlledSurface()
  {
    return surface;
  }

  public EditorEvents getEventPane()
  {
    return mEventPane;
  }

  public void copyAsWMFToClipboard()
  {
    if (toClipboard == null)
      {
        toClipboard = GraphicsToClipboard.getInstance();
      }

    Graphics theGraphics = toClipboard.getGraphics(surface.getWidth(), surface.getHeight());

    surface.print(theGraphics);
    toClipboard.copyToClipboard();
  }

  public UndoInterface getUndoInterface()
  {
    return mUndoInterface;
  }

  public void setVisible(boolean visible)
  {
    super.setVisible(visible);
    if (visible) {
      mUndoInterface.attach(menu);
      menu.update(new UndoRedoEvent());
    } else {
      mUndoInterface.detach(menu);
    }
  }

  public void setDisplayed()
  {
    setVisible(true);
    requestFocus();
  }

  public void printFigure()
  {
    try
      {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        if (printJob.getPrintService() == null)
          {
            System.err.println("No default printer set.");
            return;
          }
        printJob.setPrintable((EditorSurface) getControlledSurface());
                        
        // Printing attributes
        PrintRequestAttribute name = new JobName("Waters Printing", Locale.ENGLISH);
        PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
        attributes.add(name);
                        
        // Show printing dialog
        if (printJob.printDialog(attributes))
          {
            // Print!
            printJob.print(attributes);
          }
      }
    catch (Exception ex)
      {
        System.err.println(ex.getStackTrace());
      }
  }

  public void exportPostscript()
  {
    // Get file to export to
    JFileChooser chooser = new JFileChooser();
    chooser.setSelectedFile(new File(mSubject.getName() + ".ps"));
    int returnVal = chooser.showSaveDialog(surface);
    File file = chooser.getSelectedFile();
    // Not OK?
    if (returnVal != JFileChooser.APPROVE_OPTION) 
      {
        return;
      }
                
    // Create output
    try
      {
        PrinterJob printJob = PrinterJob.getPrinterJob();
        printJob.setPrintable((EditorSurface) getControlledSurface());

        PrintRequestAttribute postscript = new Destination(file.toURI());
        PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
        attributes.add(postscript);
                        
        // Print!
        printJob.print(attributes);
      }
    catch (Exception ex)
      {
        System.err.println(ex.getStackTrace());
      }
  }

  public void exportPDF()
  {
    // Get file to export to
    JFileChooser chooser = new JFileChooser();
    chooser.setSelectedFile(new File(mSubject.getName() + ".pdf"));
    int returnVal = chooser.showSaveDialog(surface);
    File file = chooser.getSelectedFile();
    // Not OK?
    if (returnVal != JFileChooser.APPROVE_OPTION) 
      {
        return;
      }
                        
    // Create output
    int width = surface.getWidth();
    int height = surface.getHeight();
    Document document = new Document(new com.lowagie.text.Rectangle(width, height));

    try
      {
        PdfWriter writer= PdfWriter.getInstance(document,  new FileOutputStream(file));

        document.addAuthor("Supremica");
        document.open();

        PdfContentByte cb = writer.getDirectContent();
        PdfTemplate tp = cb.createTemplate(width, height);
        Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper());
        surface.print(g2);
        //Rectangle2D rectangle2D = new Rectangle2D.Double(0, 0, width, height);
        //chart.draw(g2, rectangle2D);
        g2.dispose();
        cb.addTemplate(tp, 0, 0);

      }
    catch (DocumentException de)
      {
        System.err.println(de.getMessage());
      }
    catch (IOException ioe)
      {
        System.err.println(ioe.getMessage());
      }

    document.close();
  }

  /**
   * Creates a new event declaration for addition to a graph.
   * This methods pops up the event editor dialog. When finished, it
   * retrieves the event created by the dialog and adds an entry with
   * the same name to the graphs window's event pane.
   */ 
  public void createEvent()
  {
    final EventEditorDialog diag = new EventEditorDialog(mModuleWindow);
    diag.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent event) {
          final NamedSubject decl = diag.getEditedItem();
          final String name = decl.getName();
          final SimpleIdentifierSubject ident =
            new SimpleIdentifierSubject(name);
          final EventTableModel model =
            (EventTableModel) mEventPane.getModel();
          model.addIdentifier(ident);
        }
      });
  }


  //#########################################################################
  //# Data Members
  private EditorToolbar toolbar;
  private ControlledSurface surface;
  private EditorMenu menu;
  private final JScrollPane scrollsurface;
  private final EditorEvents mEventPane;
  private final SimpleComponentSubject mSubject;
  private final ModuleSubject mModule;
  private final ModuleWindow mModuleWindow;
  private boolean isSaved = false;
  private GraphicsToClipboard toClipboard = null;
  private final UndoInterface mUndoInterface;

}
