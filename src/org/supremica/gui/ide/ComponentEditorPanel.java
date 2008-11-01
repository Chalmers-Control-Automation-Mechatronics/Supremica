//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   ComponentEditorPanel
//###########################################################################
//# $Id$
//###########################################################################


package org.supremica.gui.ide;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.*;
import java.io.*;
import java.util.Locale;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.swing.*;
import javax.print.*;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.ControlledToolbar;
import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.EventEditorDialog;
import net.sourceforge.waters.gui.EventTableModel;
import net.sourceforge.waters.gui.GraphEventPanel;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.subject.base.NamedSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;

import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.gui.GraphicsToClipboard;


/**
 * A Swing component for editing a Waters graph.
 * A component editor panel allows the user to edit an object of type
 * {@link SimpleComponentSubject} graphically. It consists of a splitpane
 * with two windows: the event list to the left and the graph editor
 * surface to the right.
 *
 * @author Knut &Aring;kesson
 */

public class ComponentEditorPanel
    extends JPanel
    implements EditorWindowInterface
{
    
    //########################################################################
    //# Constructor
    /**
     * Creates a new component editor panel.
     * @param  moduleContainer  the module container as a handle to the
     *                          IDE application.
     * @param  component        the simple component containing the graph
     *                          to be edited.
     * @param  size             the expected total size of the panel.
     */
    public ComponentEditorPanel(final ModuleContainer moduleContainer,
								final SimpleComponentSubject component,
								final Dimension size)
        throws GeometryAbsentException
    {
        mComponent = component;
        mModuleContainer = moduleContainer;
        mModule = moduleContainer.getModule();
		final IDE ide = moduleContainer.getIDE();
		final WatersPopupActionManager manager = ide.getPopupActionManager();
        mSurface = new ControlledSurface
            (component.getGraph(), mModule, this,
			 (ControlledToolbar) ide.getToolBar(), manager);
        mSurface.setPreferredSize(IDEDimensions.rightEditorPreferredSize);
        mSurface.setMinimumSize(IDEDimensions.rightEditorMinimumSize);
        mEventsPane = new GraphEventPanel(this, component, manager);
        
        final LayoutManager layout = new BorderLayout();
        setLayout(layout);
        
        final JScrollPane scrollsurface = new JScrollPane(mSurface);
        final JScrollPane scrollevents = new JScrollPane(mEventsPane);
        final JViewport viewevents = scrollevents.getViewport();
        final JSplitPane split = new JSplitPane
            (JSplitPane.HORIZONTAL_SPLIT, scrollevents, scrollsurface);
        final int halfwidth = size.width >> 1;
        final int prefeventswidth = mEventsPane.getPreferredSize().width;
        final int divide = Math.min(prefeventswidth, halfwidth);
        split.setDividerLocation(divide);
        add(split, BorderLayout.CENTER);
    }


    //########################################################################
    //# Interface net.sourceforge.waters.gui.EditorWindowInterface
    public SimpleComponentSubject getComponent()
    {
        return mComponent;
    }

    public ModuleWindowInterface getModuleWindowInterface()
    {
        return mModuleContainer.getEditorPanel();
    }
    
    public JFrame getFrame()
    {
        return mModuleContainer.getFrame();
    }
    
    public ControlledSurface getControlledSurface()
    {
        return mSurface;
    }
    
    public GraphEventPanel getEventPanel()
    {
        return mEventsPane;
    }
    
    public UndoInterface getUndoInterface()
    {
        return mModuleContainer;
    }
    
    public void copyAsWMFToClipboard()
    {
        if (toClipboard == null)
        {
            toClipboard = GraphicsToClipboard.getInstance();
        }
        
        //Rectangle2D bb = mSurface.getBoundingBox();
        //double minX = bb.getMinX();
        //double maxX = bb.getMaxX();
        //double minY = bb.getMinY();
        //double maxY = bb.getMaxY();
        //LOGGER.debug("minX: " + minX + " maxX: " + maxX + " minY: " + minY + " maxY: " + maxY);
        //create a WMF object
        //int width = (int)(maxX - minX) + 1;
        //int height = (int)(maxY - minY) + 1;
        // Copy a larger area, approx 10 percent, there seems to be
        // a problem with the size of wmf-data
        //width += (int)0.1*width;
        //height += (int)0.1*height;
        Graphics theGraphics = toClipboard.getGraphics(mSurface.getWidth(), mSurface.getHeight());
        
        mSurface.print(theGraphics);
        toClipboard.copyToClipboard();
    }
    
    
    public void exportPDF()
    {
        // Get file to export to
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(getName() + ".pdf"));
        int returnVal = chooser.showSaveDialog(mSurface);
        File file = chooser.getSelectedFile();
        // Not OK?
        if (returnVal != JFileChooser.APPROVE_OPTION)
        {
            return;
        }
        
        // Create output
        int width = mSurface.getWidth();
        int height = mSurface.getHeight();
        Document document = new Document(new com.lowagie.text.Rectangle(width, height));
        
        try
        {
            PdfWriter writer= PdfWriter.getInstance(document,  new FileOutputStream(file));
            
            document.addAuthor("Supremica");
            document.open();
            
            PdfContentByte cb = writer.getDirectContent();
            PdfTemplate tp = cb.createTemplate(width, height);
            Graphics2D g2 = tp.createGraphics(width, height, new DefaultFontMapper());
            mSurface.print(g2);
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
     * Prints postscript output to file (specified by user).
     */
    public void exportPostscript()
    {
        String psMimeType = "application/postscript";
        
        StreamPrintServiceFactory[] factories =
            PrinterJob.lookupStreamPrintServices(psMimeType);
        if (factories.length > 0)
        {
            try
            {
                // Get file to export to
                JFileChooser chooser = new JFileChooser();
                String name;
                name = getName();
                chooser.setSelectedFile(new File(name + ".ps"));
                int returnVal = chooser.showSaveDialog(mSurface);
                File file = chooser.getSelectedFile();
                // Not OK?
                if (returnVal != JFileChooser.APPROVE_OPTION)
                {
                    return;
                }
                
                // Get printerservice and set up PrintJob
                FileOutputStream outstream = new FileOutputStream(file);
                StreamPrintService psPrinter = factories[0].getPrintService(outstream);
                // psPrinter is our Postscript print service
                PrinterJob printJob = PrinterJob.getPrinterJob();
                printJob.setPrintService(psPrinter);
                printJob.setPrintable(mSurface);
                // Printing attributes
                PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
                PrintRequestAttribute jobName = new JobName("Supremica Printing", Locale.ENGLISH);
                attributes.add(jobName);
                
                // Show printing dialog
                //if (printJob.printDialog(attributes))
                // Print!
                printJob.print(attributes);
            }
            catch (FileNotFoundException ex)
            {
                mModuleContainer.getIDE().error("File not found. " + ex);
            }
            catch (PrinterException ex)
            {
                mModuleContainer.getIDE().error("Error printing. " + ex);
            }
        }
        else
        {
            mModuleContainer.getIDE().info("No Postscript printer service installed.");
        }
    }
        
    /**
     * Open a print dialog and let the user choose how to print.
     */
    public void printFigure()
    {
        try
        {
            PrinterJob printJob = PrinterJob.getPrinterJob();
            if (printJob.getPrintService() == null)
            {
                mModuleContainer.getIDE().error("No default printer set.");
                return;
            }
            printJob.setPrintable(mSurface);
            
            // Printing attributes
            PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
            PrintRequestAttribute name = new JobName("Supremica Printing", Locale.ENGLISH);
            attributes.add(name);
            
            // Show printing dialog
            if (printJob.printDialog(attributes))
            {
                LOGGER.debug("Printing...");
                
                // Print!
                printJob.print();
                //printJob.print(attributes);
                
                LOGGER.debug("Printing done!");
            }
        }
        catch (Exception ex)
        {
            System.err.println(ex);
            System.err.println(ex.getStackTrace());
        }
    }


    //########################################################################
    //# Data Members
    private final ModuleContainer mModuleContainer;
    private final ControlledSurface mSurface;
    private final GraphEventPanel mEventsPane;
    private final SimpleComponentSubject mComponent;
    private final ModuleSubject mModule;

    private GraphicsToClipboard toClipboard;


    //########################################################################
    //# Static Class Constants
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER =
		LoggerFactory.createLogger(ComponentEditorPanel.class);

}
