//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   ComponentEditorPanel
//###########################################################################
//# $Id: ComponentEditorPanel.java,v 1.36 2007-01-26 15:09:52 avenir Exp $
//###########################################################################


package org.supremica.gui.ide;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.awt.print.*;
import java.util.Locale;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.swing.*;
import javax.print.*;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.ControlledToolbar;
import net.sourceforge.waters.gui.EditorEvents;
import net.sourceforge.waters.gui.EditorMenu;
import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.EventEditorDialog;
import net.sourceforge.waters.gui.EventTableModel;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.subject.base.NamedSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import org.supremica.gui.FileDialogs;

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
    private static final long serialVersionUID = 1L;
    
    private ModuleContainer mModuleContainer;
    private ControlledSurface surface;
    private EditorEvents events;
    private EditorMenu menu;
    private SimpleComponentSubject element = null;
    private ModuleSubject mModule = null;
    private boolean isSaved = false;
    private GraphicsToClipboard toClipboard = null;    
    
    /**
     * Creates a new component editor panel.
     * @param  moduleContainer  the module container as a handle to the
     *                          IDE application.
     * @param  element          the simple component containing the graph
     *                          to be edited.
     * @param  size             the expected total size of the panel.
     */
    public ComponentEditorPanel(final ModuleContainer moduleContainer,
        final SimpleComponentSubject element,
        final Dimension size)
        throws GeometryAbsentException        
    {
        this.element = element;
        mModuleContainer = moduleContainer;
        mModule = moduleContainer.getModule();
        surface = new ControlledSurface
            (element.getGraph(), mModule, this,
            (ControlledToolbar) mModuleContainer.getIDE().getToolBar());
        surface.setPreferredSize(IDEDimensions.rightEditorPreferredSize);
        surface.setMinimumSize(IDEDimensions.rightEditorMinimumSize);
        
        final ModuleWindowInterface root = mModuleContainer.getEditorPanel();
        events = new EditorEvents(root, element, this);
        menu = new EditorMenu(surface, this);
        
        final LayoutManager layout = new BorderLayout();
        setLayout(layout);
        
        final JScrollPane scrollsurface = new JScrollPane(surface);
        final JScrollPane scrollevents = new JScrollPane(events);
        final JViewport viewevents = scrollevents.getViewport();
        final JSplitPane split = new JSplitPane
            (JSplitPane.HORIZONTAL_SPLIT, scrollevents, scrollsurface);
        final int halfwidth = size.width >> 1;
        final int prefeventswidth = events.getPreferredSize().width;
        final int divide = Math.min(prefeventswidth, halfwidth);
        split.setDividerLocation(divide);
        add(split, BorderLayout.CENTER);

        surface.createOptions(this);
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
        return mModuleContainer.getFrame();
    }
    
    public ControlledSurface getControlledSurface()
    {
        return surface;
    }
    
    public EditorEvents getEventPane()
    {
        return events;
    }
    
/*
        public void repaint()
        {
                System.err.println("ComponentEditorPanel.repaint");
                //scrollsurface.invalidate();
                super.repaint();
        }
 */
    
    public void setDisplayed()
    {
        EditorPanel editorPanel = mModuleContainer.getEditorPanel();
        editorPanel.setRightComponent(this);
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
        
        //Rectangle2D bb = surface.getBoundingBox();
        //double minX = bb.getMinX();
        //double maxX = bb.getMaxX();
        //double minY = bb.getMinY();
        //double maxY = bb.getMaxY();
        //logger.debug("minX: " + minX + " maxX: " + maxX + " minY: " + minY + " maxY: " + maxY);
        //create a WMF object
        //int width = (int)(maxX - minX) + 1;
        //int height = (int)(maxY - minY) + 1;
        // Copy a larger area, approx 10 percent, there seems to be
        // a problem with the size of wmf-data
        //width += (int)0.1*width;
        //height += (int)0.1*height;
        Graphics theGraphics = toClipboard.getGraphics(surface.getWidth(), surface.getHeight());
        
        surface.print(theGraphics);
        toClipboard.copyToClipboard();
    }
    
    
    public void exportPDF()
    {
        // Get file to export to
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File(mModule.getName() + ".pdf"));
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
                name = element.getName();
                chooser.setSelectedFile(new File(name + ".ps"));
                int returnVal = chooser.showSaveDialog(surface);
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
                printJob.setPrintable(surface);
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

 public void exportEncapsulatedPostscript()
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
                chooser.setSelectedFile(new File(element.getName() + ".eps"));
                int returnVal = chooser.showSaveDialog(surface);
                File epsFile = chooser.getSelectedFile();
                // Not OK?
                if (returnVal != JFileChooser.APPROVE_OPTION)
                {
                    return;
                }

				// Create output
				File dir = epsFile.getParentFile();
				File psFile = File.createTempFile("temp", ".ps", dir);

                // Get printerservice and set up PrintJob
                FileOutputStream outstream = new FileOutputStream(psFile);
                StreamPrintService psPrinter = factories[0].getPrintService(outstream);
                // psPrinter is our Postscript print service
                PrinterJob printJob = PrinterJob.getPrinterJob();
                printJob.setPrintService(psPrinter);
                printJob.setPrintable(surface);
                // Printing attributes
                PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
                PrintRequestAttribute jobName = new JobName("Supremica Printing", Locale.ENGLISH);
                attributes.add(jobName);

                // Print!
                printJob.print(attributes);


				// Convert ps to eps using "ps2epsi"
				try 
				{
					String[] cmds = new String[]{"ps2epsi", psFile.getName(), epsFile.getName()};
					
					Process ps2epsiProcess = Runtime.getRuntime().exec(cmds, null, dir);
					ps2epsiProcess.waitFor();
					
					if (ps2epsiProcess.exitValue() != 0)
					{
						throw new Exception("Conversion from ps to eps exited unsuccessfully.");
					}
				}
				catch (Exception ex)
				{
					mModuleContainer.getIDE().error("The conversion from ps to eps failed. Make sure that \"ps2epsi.bat\" is globally accessible.");
					throw ex;
				}

				// Loop through the eps-file and correct it if necessary
				File newEpsFile = File.createTempFile(epsFile.getName(), ".tmp", dir);
				
				BufferedReader r = new BufferedReader(new FileReader(epsFile));
				BufferedWriter w = new BufferedWriter(new FileWriter(newEpsFile));

				boolean alert = false;
				String str = r.readLine();
				while (str != null)
				{
					if (str.contains("save countdictstack"))
					{
						alert = false;
					}
					
					if (alert == true)
					{
						w.write("save countdictstack mark newpath /showpage {} def /setpagedevice {pop} def");
						w.newLine();
						
						alert = false;
					}
					
					w.write(str);
					w.newLine();
					
					if (str.contains("%%EndPreview"))
					{
						alert = true;
					}
					
					str = r.readLine();
				}
				
				w.flush();
				w.close();
				r.close();
				outstream.close();

				// Clean up
				psFile.delete();
				epsFile.delete();

				boolean renameSucceeded = newEpsFile.renameTo(epsFile);

				if (!renameSucceeded)
				{
					throw new Exception("Unable to rename the newly created file to " + epsFile.getName());
				}
			}
			catch (FileNotFoundException ex)
            {
                mModuleContainer.getIDE().error("File not found. " + ex);
            }
            catch (PrinterException ex)
            {
                mModuleContainer.getIDE().error("Error printing. " + ex);
            }
			catch (Exception ex)
			{
				mModuleContainer.getIDE().error("Error converting from ps to eps. " + ex);
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
                System.err.println("No default printer set.");
                return;
            }
            printJob.setPrintable(surface);
         
            // Printing attributes
            PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
            PrintRequestAttribute name = new JobName("Supremica Printing", Locale.ENGLISH);
            attributes.add(name);
         
            // Show printing dialog
            if (printJob.printDialog(attributes))
            {
                System.out.println("Printing...");
         
                // Print!
                printJob.print();
                //printJob.print(attributes);
         
                System.out.println("Printing done!");
            }
        }
        catch (Exception ex)
        {
            System.err.println(ex);
            System.err.println(ex.getStackTrace());
        }
    }
    
    public void createEvent()
    {
        final ModuleWindowInterface root = mModuleContainer.getEditorPanel();
        final EditorWindowInterface gedit =
            mModuleContainer.getActiveEditorWindowInterface();
        final EventEditorDialog diag = new EventEditorDialog(root);
        diag.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent event)
            {
                final NamedSubject decl = diag.getEditedItem();
                final String name = decl.getName();
                final SimpleIdentifierSubject ident =
                    new SimpleIdentifierSubject(name);
                final EditorEvents eventpane = gedit.getEventPane();
                final EventTableModel model =
                    (EventTableModel) eventpane.getModel();
                model.addIdentifier(ident);
            }
        });
    }
    
}
