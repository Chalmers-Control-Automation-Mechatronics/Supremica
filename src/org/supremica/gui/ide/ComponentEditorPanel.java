//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide
//# CLASS:   ComponentEditorPanel
//###########################################################################
//# $Id: ComponentEditorPanel.java,v 1.55 2007-10-29 12:14:16 avenir Exp $
//###########################################################################


package org.supremica.gui.ide;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.*;
import java.net.MalformedURLException;
import java.io.*;
import java.util.List;
import java.util.Locale;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.swing.*;
import javax.print.*;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.ControlledToolbar;
import net.sourceforge.waters.gui.EditorEvents;
import net.sourceforge.waters.gui.EditorWindowInterface;
import net.sourceforge.waters.gui.EventEditorDialog;
import net.sourceforge.waters.gui.EventTableModel;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.renderer.Handle;
import net.sourceforge.waters.gui.renderer.ProxyShapeProducer;
import net.sourceforge.waters.gui.renderer.SimpleNodeProxyShape;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.BoxGeometryProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.subject.base.NamedSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;

import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.gui.GraphicsToClipboard;
import org.supremica.properties.Config;

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

    private static Logger logger = LoggerFactory.createLogger(ComponentEditorPanel.class);

    private ModuleContainer mModuleContainer;
    private ControlledSurface surface;
    private EditorEvents events;
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

    //########################################################################
    //# Interface net.sourceforge.waters.gui.EditorWindowInterface
    public ModuleWindowInterface getModuleWindowInterface()
    {
        return mModuleContainer.getEditorPanel();
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

    /*
    public void exportEncapsulatedPostscript()
    {
        EPS2D eps2d = new EPS2D("filename.eps");

        surface.print(eps2d);
    }
     */

    public void exportEncapsulatedPostscript()
    {
        // The output stream, used to write to an eps-file
        BufferedWriter w = null;

        try
        {
            // An auxiliary object, finding the shapes of the logical objects of the graph, such as nodes, edges, etc.
            ProxyShapeProducer producer = surface.getShapeProducer();

            // Some transform needed to convert java's pixel representation into postscript coordinate system
            AffineTransform transform = new AffineTransform(1, 0, 0, -1, 0, (new java.awt.print.Paper()).getHeight());
            AffineTransform offsetTransform = new AffineTransform(1, 0, 0, -1, 0, 0);
            AffineTransform labelTransform = new AffineTransform(1, 0, 0, -1, 1, (new java.awt.print.Paper()).getHeight() - 12);

            // The delimiters of the eps-file BoundingBox, stored in the following order: minX, minY, maxX, maxY
            double[] boundingBoxLimits = new double[]{(new java.awt.print.Paper()).getWidth(), (new java.awt.print.Paper()).getHeight(), 0, 0};

            // This is somewhat ugly
            final double MARKING_GREY_SCALE = 0.5; // The grayscale level of the marked states

            // Open a file chooser in the location of the modelfile,
            // and create the eps-file to be filled with the graphical
            // information.
            JFileChooser chooser = new JFileChooser();
            try
            {
                final File location = mModule.getFileLocation();
                if (location != null)
                {
                    chooser.setCurrentDirectory(location);
                }
            }
            catch (final MalformedURLException exception)
            {
                // JAR URL---no file---no preselection of directory.
            }
            chooser.setSelectedFile(new File(element.getName() + ".eps"));
            int returnVal = chooser.showSaveDialog(surface);
            File epsFile = chooser.getSelectedFile();
            // Not OK?
            if (returnVal != JFileChooser.APPROVE_OPTION)
            {
                return;
            }

            // Create the writer, responsible for writing the information to the eps-file
            w = new BufferedWriter(new FileWriter(epsFile));

            // Create the head of the eps-file
            w.write("%!PS-Adobe EPSF-3.0");
            w.newLine();
            w.write("%%Creator: Supremica-IDE");
            w.newLine();
            w.write("%%Title: " + epsFile.getName());
            w.newLine();
            java.util.Calendar gregCalendar = new java.util.GregorianCalendar();
            String month = "" + (gregCalendar.get(java.util.Calendar.MONTH) + 1);
            if (month.length() == 1)
            {
                month = "0" + month;
            }
            String day = "" + gregCalendar.get(java.util.Calendar.DAY_OF_MONTH);
            if (day.length() == 1)
            {
                day = "0" + day;
            }
            w.write("%%CreationDate: " + gregCalendar.get(java.util.Calendar.YEAR) + "-" + month + "-" + day);
            w.newLine();
            w.write("%%Page: 1 1\n");
            w.newLine();

            w.write("/actionLabel {\n");
            w.write("\tgsave\n");
            w.write("\t0.6 0.15 0.15 setrgbcolor\n");
            w.write("\tControllableFont label\n");
            w.write("\tgrestore\n");
            w.write("} def\n");
            w.newLine();

            // Useful functions are defined and added to the eps-file
            w.write("/arrow {\n");
            w.write("\tarrowHead\n");
            w.write("\tedge\n");
            w.write("} def\n");
            w.newLine();

            w.write("/arrowHead {\n");
            w.write("\tnewpath\n");
            w.write("\tmoveto\n");
            w.write("\tlineto\n");
            w.write("\tlineto\n");
            w.write("\tclosepath\n");
            w.write("\tfill\n");
            w.write("\tstroke\n");
            w.write("} def\n");
            w.newLine();

            w.write("/controllableLabel {\n");
            w.write("\tControllableFont label\n");
            w.write("} def\n");
            w.newLine();

            w.write("/edge {\n");
            w.write("\tnewpath\n");
            w.write("\t6 -2 roll moveto\n");
            w.write("\tcurrentpoint 6 2 roll curveto\n");
            w.write("\tstroke\n");
            w.write("} def\n");
            w.newLine();

            w.write("/guardLabel {\n");
            w.write("\tgsave\n");
            w.write("\t0.0 0.5 0.5 setrgbcolor\n");
            w.write("\tControllableFont label\n");
            w.write("\tgrestore\n");
            w.write("} def\n");
            w.newLine();

            w.write("/label {\n");
            w.write("\tsetfont\n");
            w.write("\tnewpath\n");
            w.write("\tmoveto\n");
            w.write("\tshow\n");
            w.write("\tstroke\n");
            w.write("} def\n");
            w.newLine();

            w.write("/loop {\n");
            w.write("\tnewpath\n");
            w.write("\t28 -2 roll moveto\n");
            w.write("\t26 -2 roll lineto\n");
            w.write("\t24 -2 roll lineto\n");
            w.write("\t22 -6 roll curveto\n");
            w.write("\t16 -6 roll curveto\n");
            w.write("\t10 -6 roll curveto\n");
            w.write("\t4 -2 roll lineto\n");
            w.write("\tlineto\n");
            w.write("\tstroke\n");
            w.write("} def\n");
            w.newLine();

            // Useful functions are defined and added to the eps-file
            w.write("/loopArrow {\n");
            w.write("\tarrowHead\n");
            w.write("\tloop\n");
            w.write("} def\n");
            w.newLine();

            if (Config.GUI_EDITOR_LAYOUT_MODE.get().equals(Config.LAYOUT_MODE_LEGALVALUES.ChalmersIDES))
            {
                // DOUBLE CIRCLES FOR MARKING
                w.write("/markedState {\n");
                w.write("0.75 setlinewidth\n");
                w.write("\t2 copy\n");
                w.write("\tnewpath\n");
                w.write("\t" + SimpleNodeProxyShape.RADIUS + " 0 360 arc\n");
                w.write("\tstroke\n");
                w.write("\tnewpath\n");
                w.write("\t" + (SimpleNodeProxyShape.RADIUS-2) + " 0 360 arc\n");
                w.write("\tstroke\n");
                w.write("0.25 setlinewidth\n");
                 w.write("} def\n");
                w.newLine();
            }
            else
            {
                // DEFAULT
                w.write("/markedState {\n");
                w.write("\tnewpath\n");
                w.write("\t" + SimpleNodeProxyShape.RADIUS + " 0 360 arc\n");
                w.write("\tgsave\n");
                w.write("\t" + MARKING_GREY_SCALE + " setgray\n");
                w.write("\tfill\n");
                w.write("\tgrestore\n");
                w.write("\tstroke\n");
                w.write("} def\n");
                w.newLine();
            }

            w.write("/state {\n");
            if (Config.GUI_EDITOR_LAYOUT_MODE.get().equals(Config.LAYOUT_MODE_LEGALVALUES.ChalmersIDES))
            {
                w.write("0.75 setlinewidth\n");
            }
            w.write("\tnewpath\n");
            w.write("\t" + SimpleNodeProxyShape.RADIUS + " 0 360 arc\n");
            w.write("\tstroke\n");
            if (Config.GUI_EDITOR_LAYOUT_MODE.get().equals(Config.LAYOUT_MODE_LEGALVALUES.ChalmersIDES))
            {
                w.write("0.25 setlinewidth\n");
            }
            w.write("} def\n");
            w.newLine();

            w.write("/stateLabel {\n");
            w.write("\tgsave\n");
            w.write("\t0.0 0.5 0.0 setrgbcolor\n");
            w.write("\tStateFont label\n");
            w.write("\tgrestore\n");
            w.write("} def\n");
            w.newLine();

            w.write("/straightArrow {\n");
            w.write("\tarrowHead\n");
            w.write("\tstraightEdge\n");
            w.write("} def\n");
            w.newLine();

            w.write("/straightEdge {\n");
            w.write("\tnewpath\n");
            w.write("\tmoveto\n");
            w.write("\tlineto\n");
            w.write("\tstroke\n");
            w.write("} def\n");
            w.newLine();


            w.write("/uncontrollableLabel {\n");
            w.write("\tUncontrollableFont label\n");
            w.write("} def\n");
            w.newLine();

            w.write("/ControllableFont\n");
            w.write("\t/Times-Roman findfont\n");
            w.write("\t12 scalefont\n");
            w.write("def\n");
            w.newLine();

            w.write("/StateFont\n");
            w.write("\t/Helvetica findfont\n");
            w.write("\t12 scalefont\n");
            w.write("def\n");
            w.newLine();

            w.write("/UncontrollableFont\n");
            w.write("\t/Times-Italic findfont\n");
            w.write("\t12 scalefont\n");
            w.write("def\n");
            w.newLine();

            // Default line width
            w.write("0.25 setlinewidth\n");
            
            // For every node...
            // *** BUG *** This must be done through renderer.
            for (NodeProxy node : surface.getDrawnGraph().getNodes())
            {
                // ... representing a state
                if (node instanceof SimpleNodeProxy)
                {
                    SimpleNodeProxy simpleNode = (SimpleNodeProxy) node;
                    boolean markedState = false;
                    boolean forbiddenState = false;

                    // Find the center point of the state in ps-coordinates
                    // and round it off to the nearest integer
                    Point2D centerPoint = transform.transform(simpleNode.getPointGeometry().getPoint(), null);
                    centerPoint.setLocation(Math.round(centerPoint.getX()), Math.round(centerPoint.getY()));

                    // Check if this node is marked, i.e. whether it is labeled with a marked event
                    for (Proxy event : simpleNode.getPropositions().getEventList())
                    {
                        if (event instanceof IdentifierProxy)
                        {
                            if (((IdentifierProxy)event).getName().equals(EventDeclProxy.DEFAULT_MARKING_NAME))
                            {
                                markedState = true;
                            }
                            if (((IdentifierProxy)event).getName().equals(EventDeclProxy.DEFAULT_FORBIDDEN_NAME))
                            {
                                forbiddenState = true;
                            }
                        }
                    }

                    // Choose appropriate postscript-command for this state
                    String command = "state";
                    if (markedState)
                    {
                        command = "markedState";
                    }

                    // Add the state to the eps-file
                    w.write(centerPoint.getX() + " " + centerPoint.getY() + " " + command + "\n");
                    if (forbiddenState)
                    {
                        w.write("gsave\n");
                        w.write("newpath\n");
                        w.write((centerPoint.getX()-8) + " " + (centerPoint.getY()-8) + " moveto\n");
                        w.write((centerPoint.getX()+8) + " " + (centerPoint.getY()+8) + " lineto\n");
                        w.write((centerPoint.getX()+8) + " " + (centerPoint.getY()-8) + " moveto\n");
                        w.write((centerPoint.getX()-8) + " " + (centerPoint.getY()+8) + " lineto\n");
                        w.write("\t1.0 0.0 0.0 setrgbcolor\n");
                        w.write("stroke\n");
                        w.write("grestore\n");
                    }

                    // Create bounds for the state, at a small distance (PADDING)
                    // outside the state circle and update the bounding box
                    final int PADDING = 0;
                    Rectangle2D stateBounds = new Rectangle2D.Double(centerPoint.getX() - SimpleNodeProxyShape.RADIUS - PADDING, centerPoint.getY() - SimpleNodeProxyShape.RADIUS - PADDING, 2*(SimpleNodeProxyShape.RADIUS + PADDING), 2*(SimpleNodeProxyShape.RADIUS + PADDING));
                    boundingBoxLimits = updateBoundingBoxLimits(boundingBoxLimits, stateBounds, null);

                    // If current state is initial, add the initial (straight) arrow to the eps-file
                    if (simpleNode.isInitial())
                    {
                        for (Handle handle : producer.getShape(simpleNode).getHandles())
                        {
                            if (handle.getType() == Handle.HandleType.INITIAL)
                            {
                                PathIterator paths = handle.getShape().getPathIterator(transform);

                                String psStr = "";
                                while (!paths.isDone())
                                {
                                    double[] coords = new double[6];
                                    int res = paths.currentSegment(coords);

                                    if (res != PathIterator.SEG_CLOSE)
                                    {
                                        psStr += Math.round(coords[0]) + " " + Math.round(coords[1]) + " \n";
                                    }

                                    paths.next();
                                }
                                w.write(psStr.substring(0, psStr.length()-1) + "straightArrow\n");

                                Rectangle2D bounds = handle.getShape().getBounds2D();
                                double[] boundsCoords = new double[]{bounds.getMinX(), bounds.getMinY(), bounds.getMaxX(), bounds.getMaxY()};
                                transform.transform(boundsCoords, 0, boundsCoords, 0, 2);

                                // Update the bounding box
                                boundingBoxLimits = updateBoundingBoxLimits(boundingBoxLimits, handle.getShape().getBounds2D(), transform);

                                break;
                            }
                        }
                    }

                    // Add the label of the current state to the eps-file, using the
                    // left lower corner of the label shape as the ps-coordinates.
                    Shape stateLabelShape = producer.getShape(simpleNode.getLabelGeometry()).getShape();
                    Point2D leftLowerCorner = labelTransform.transform(new Point2D.Double(stateLabelShape.getBounds2D().getMinX(), stateLabelShape.getBounds2D().getMinY()), null);
                    leftLowerCorner.setLocation(Math.round(leftLowerCorner.getX()), Math.round(leftLowerCorner.getY()));

                    w.write("(" + simpleNode.getName() + ") " + leftLowerCorner.getX() + " " + leftLowerCorner.getY() + " stateLabel\n");

                    // Update the bounding box
                    boundingBoxLimits = updateBoundingBoxLimits(boundingBoxLimits, stateLabelShape.getBounds2D(), transform);
                }
                else if (node instanceof GroupNodeProxy)
                { // Treatment of node groups
                    w.write("% Node group " + node.getName() + "\n");
                    w.write("newpath\n");
                    w.write("gsave\n");
                    w.write("0.7 setgray\n");
                    w.write("1.5 setlinewidth\n");

                    GroupNodeProxy groupNode = (GroupNodeProxy) node;
                    BoxGeometryProxy groupGeometryProxy = groupNode.getGeometry();
                    PathIterator paths = groupGeometryProxy.getRectangle().getPathIterator(transform);
                    while (!paths.isDone())
                    {
                        double[] coords = new double[6];
                        int res = paths.currentSegment(coords);

                        if (res == PathIterator.SEG_MOVETO)
                        {
                            w.write(Math.round(coords[0]) + " " + Math.round(coords[1]) + " moveto\n");
                        }
                        else if (res == PathIterator.SEG_LINETO)
                        {
                            w.write(Math.round(coords[0]) + " " + Math.round(coords[1]) + " lineto\n");
                        }
                        else if (res == PathIterator.SEG_QUADTO)
                        {
                            logger.error("Oops, could this happen? Not implemented...");
                        }
                        else if (res == PathIterator.SEG_CUBICTO)
                        {
                            String str = "";
                            for (int i = 0; i < coords.length; i++)
                            {
                                str += Math.round(coords[i]) + " ";
                            }
                            str += "curveto\n";
                        }
                        else
                        {
                            w.write("stroke\n");
                            w.write("grestore\n");

                            // Update the bounding box
                            boundingBoxLimits = updateBoundingBoxLimits(boundingBoxLimits, groupGeometryProxy.getRectangle().getBounds2D(), transform);
                        }
                        paths.next();
                    }
                }
                else
                {
                    logger.error("node " + node.getName() + " UNTREATED... (unheard of)");
                }

                // Add an empty line after each state-info
                w.newLine();
            }

            // For every edge...
            for (EdgeProxy edge : surface.getDrawnGraph().getEdges())
            {
                Shape edgeshape = producer.getShape(edge).getShape();
                PathIterator paths = edgeshape.getPathIterator(transform);

                // Set correct ps-command for the edge
                String command = "arrow";
                if (edge.getGeometry() == null) // then this is a straight edge
                {
                    command = "straightArrow";
                }

                // Write the geometry information about the edge to the eps-file
                String psStr = "";
                while (!paths.isDone())
                {
                    double[] coords = new double[6];
                    int res = paths.currentSegment(coords);

                    if (res != PathIterator.SEG_CLOSE)
                    {
                        // The ps-commands are either "moveto", using 2 arguments,
                        // "curveto" using 4 arguments (in java SEG_QUADTO) or
                        // "curveto" using 6 arguments (B�zier interpolation) (in java SEG_CUBICTO)
                        int nrOfCoords = 2;
                        if (res == PathIterator.SEG_QUADTO)
                        {
                            nrOfCoords = 4;
                        }
                        else if (res == PathIterator.SEG_CUBICTO)
                        {
                            nrOfCoords = 6;

                            // SEG_CUBICTO, used to draw selfloops, requires special treatment
                            command = "loopArrow";
                        }
                        else
                        {
                            //This happens quite often!! But what should we do?!
                        }

                        for (int i=0; i<nrOfCoords; i++)
                        {
                            //w.write(Math.round(coords[i]) + " ");
                            psStr += Math.round(coords[i]) + " ";
                        }

                        psStr += "\n";
                    }
                    else
                    {
                        w.write(psStr.substring(0, psStr.length()-1) + command);
                        w.newLine();
                        break;
                    }

                    paths.next();
                }

                // Update the bounding box
                boundingBoxLimits = updateBoundingBoxLimits(boundingBoxLimits, edgeshape.getBounds2D(), transform);

                // For each event attached to this edge...
                for (Proxy proxyid : edge.getLabelBlock().getEventList())
                {
//                                        if (proxyid instanceof IndexedIdentifierProxy)
//                                        {
//                                            IndexedIdentifierProxy indexedProxyId = (IndexedIdentifierProxy) proxyid;
//                                            System.out.println("found indexed identifier, name = " + indexedProxyId.getName());
//                                            for (SimpleExpressionProxy index : indexedProxyId.getIndexes())
//                                            {
//                                                System.out.println("index.text = " + index.getPlainText());
////                                                net.sourceforge.waters.model.des.ProductDESProxyFactory factory = net.sourceforge.waters.plain.des.ProductDESElementFactory.getInstance();
////                                                net.sourceforge.waters.model.marshaller.DocumentManager mDocumentManager = new net.sourceforge.waters.model.marshaller.DocumentManager();
////                                                net.sourceforge.waters.model.compiler.ModuleCompiler compiler = new net.sourceforge.waters.model.compiler.ModuleCompiler(mDocumentManager, factory, surface.getModule());
////                                                net.sourceforge.waters.model.expr.Value val = compiler.visitIndexedIdentifierProxy(indexedProxyId);
////                                                System.out.println("value = " + val);
//                                            }
//                                        }
                    // Find the left lower corner coordinates of the shape representing this event
                    // and round them off to nearest integers
                    Shape eventShape = producer.getShape(proxyid).getShape();
                    Point2D leftLowerCorner = labelTransform.transform(new Point2D.Double(eventShape.getBounds2D().getMinX(), eventShape.getBounds2D().getMinY()), null);
                    leftLowerCorner.setLocation(Math.round(leftLowerCorner.getX()), Math.round(leftLowerCorner.getY()));

                    // Event name
                    String eventName = ((IdentifierProxy) proxyid).getName();

                    // Determine controllability of event
                    boolean controllable = true;
                    List<EventDeclProxy> eventdecllist = surface.getModule().getEventDeclList();
                    for (EventDeclProxy edp : eventdecllist)
                    {
                        if (edp.getName().equals(eventName))
                        {
                            if (edp.getKind() == net.sourceforge.waters.xsd.base.EventKind.UNCONTROLLABLE)
                            {
                                controllable = false;
                            }
                        }
                    }

                    // The strings that are used to construct the ps-command
                    String coordStr = leftLowerCorner.getX() + " " + leftLowerCorner.getY();
                    //String labelStr = "(" + (controllable? "" : "!") + eventName + ")";
                    String labelStr = "(" + eventName + ")";
                    command = (controllable? "" : "un") + "controllableLabel";
                    
                    w.write(labelStr + " " + coordStr + " " + command);
                    w.newLine();

                    // Update the bounding box
                    boundingBoxLimits = updateBoundingBoxLimits(boundingBoxLimits, eventShape.getBounds2D(), labelTransform);
                }

                // Add guards and actions to the eps-file
                GuardActionBlockProxy guardActionBlock = edge.getGuardActionBlock();
                if (guardActionBlock != null)
                {
                    for (BinaryExpressionProxy action : guardActionBlock.getActions())
                    {
                        Shape actionShape = producer.getShape(action).getShape();
                        Point2D actionAnchor = labelTransform.transform(new Point2D.Double(actionShape.getBounds2D().getMinX(), actionShape.getBounds2D().getMinY()), null);
                        actionAnchor.setLocation(Math.round(actionAnchor.getX()), Math.round(actionAnchor.getY()));

                        w.write("(" + action + ") " + actionAnchor.getX() + " " + actionAnchor.getY() + " actionLabel\n");

                        // Update the bounding box
                        boundingBoxLimits = updateBoundingBoxLimits(boundingBoxLimits, actionShape.getBounds2D(), labelTransform);
                    }
                    for (SimpleExpressionProxy guard : guardActionBlock.getGuards())
                    {
                        Shape guardShape = producer.getShape(guard).getShape();
                        Point2D guardAnchor = labelTransform.transform(new Point2D.Double(guardShape.getBounds2D().getMinX(), guardShape.getBounds2D().getMinY()), null);
                        guardAnchor.setLocation(Math.round(guardAnchor.getX()), Math.round(guardAnchor.getY()));

                        w.write("(" + guard + ") " + guardAnchor.getX() + " " + guardAnchor.getY() + " guardLabel\n");

                        // Update the bounding box
                        boundingBoxLimits = updateBoundingBoxLimits(boundingBoxLimits, guardShape.getBounds2D(), labelTransform);
                    }
                }

                // Add an empty line after each edge-info
                w.newLine();
            }

            // Add closing command to the eps-file
            w.write("%%EOF");

            // Close the output stream
            w.flush();
            w.close();

            // The recently created eps-file is reprinted, this time with the
            // information about the supremal bounding box added to its header.
            // A temporary file is used for the reprinting
            File newEpsFile = File.createTempFile("temp", ".eps", epsFile.getParentFile());
            BufferedReader r = new BufferedReader(new FileReader(epsFile));
            w = new BufferedWriter(new FileWriter(newEpsFile));

            // Every line of command is copied and a "BoundingBox"-line is added
            String str = r.readLine();
            while (str != null)
            {
                if (str.contains("%%Page"))
                {
                    w.write("%%BoundingBox: " + Math.round(boundingBoxLimits[0]) + " " + Math.round(boundingBoxLimits[1]) + " " + Math.round(boundingBoxLimits[2]) + " " + Math.round(boundingBoxLimits[3]));
                    w.newLine();
                }

                w.write(str);
                w.newLine();

                str = r.readLine();
            }

            // The in- and output streams are closed
            w.flush();
            w.close();
            r.close();

            // The old file is deleted, while the new one takes its name
            epsFile.delete();
            boolean renameSucceeded = newEpsFile.renameTo(epsFile);
            if (!renameSucceeded)
            {
                throw new Exception("Unable to rename the newly created file to " + epsFile.getName());
            }

            mModuleContainer.getIDE().info(epsFile.getName() + " created.");
        }
        catch (Exception ex)
        {
            // *** BUG *** should not catch all exceptions ...
            if (w != null)
            {
                try
                {
                    w.flush();
                    w.close();
                }
                catch (Exception e)
                {
                    mModuleContainer.getIDE().error("Error at flushing the output stream");
                }
            }

            ex.printStackTrace();
        }
    }
//     {
// 		String psMimeType = "application/postscript";

//         StreamPrintServiceFactory[] factories =
//             PrinterJob.lookupStreamPrintServices(psMimeType);
//         if (factories.length > 0)
//         {
//             try
//             {
//                 // Get file to export to
//                 JFileChooser chooser = new JFileChooser();
//                 chooser.setSelectedFile(new File(element.getName() + ".eps"));
//                 int returnVal = chooser.showSaveDialog(surface);
//                 File epsFile = chooser.getSelectedFile();
//                 // Not OK?
//                 if (returnVal != JFileChooser.APPROVE_OPTION)
//                 {
//                     return;
//                 }

// 				// Create output
// 				File dir = epsFile.getParentFile();
// 				File psFile = File.createTempFile("temp", ".ps", dir);

//                 // Get printerservice and set up PrintJob
//                 FileOutputStream outstream = new FileOutputStream(psFile);
//                 StreamPrintService psPrinter = factories[0].getPrintService(outstream);
//                 // psPrinter is our Postscript print service
//                 PrinterJob printJob = PrinterJob.getPrinterJob();
//                 printJob.setPrintService(psPrinter);
//                 printJob.setPrintable(surface);
//                 // Printing attributes
//                 PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
//                 PrintRequestAttribute jobName = new JobName("Supremica Printing", Locale.ENGLISH);
//                 attributes.add(jobName);

//                 // Print!
//                 printJob.print(attributes);


// 				// Convert ps to eps using "ps2epsi"
// 				try
// 				{
// 					String[] cmds = new String[]{"ps2epsi.bat", psFile.getName(), epsFile.getName()};

// 					Process ps2epsiProcess = Runtime.getRuntime().exec(cmds, null, dir);
// 					ps2epsiProcess.waitFor();

// 					if (ps2epsiProcess.exitValue() != 0)
// 					{
// 						throw new Exception("Conversion from ps to eps exited unsuccessfully.");
// 					}
// 				}
// 				catch (Exception ex)
// 				{
// 					mModuleContainer.getIDE().error("The conversion from ps to eps failed. Make sure that \"ps2epsi.bat\" is globally accessible.");
// 					throw ex;
// 				}

// 				// Loop through the eps-file and correct it if necessary
// 				File newEpsFile = File.createTempFile(epsFile.getName(), ".tmp", dir);

// 				BufferedReader r = new BufferedReader(new FileReader(epsFile));
// 				BufferedWriter w = new BufferedWriter(new FileWriter(newEpsFile));

// 				boolean alert = false;
// 				String str = r.readLine();
// 				while (str != null)
// 				{
// 					if (str.contains("save countdictstack"))
// 					{
// 						alert = false;
// 					}

// 					if (alert == true)
// 					{
// 						w.write("save countdictstack mark newpath /showpage {} def /setpagedevice {pop} def\n");

// 						alert = false;
// 					}

// 					w.write(str);
// 					w.newLine();

// 					if (str.contains("%%EndPreview"))
// 					{
// 						alert = true;
// 					}

// 					str = r.readLine();
// 				}

// 				w.flush();
// 				w.close();
// 				r.close();
// 				outstream.close();

// 				// Clean up
// 				psFile.delete();
// 				epsFile.delete();

// 				boolean renameSucceeded = newEpsFile.renameTo(epsFile);

// 				if (!renameSucceeded)
// 				{
// 					throw new Exception("Unable to rename the newly created file to " + epsFile.getName());
// 				}
// 			}
// 			catch (FileNotFoundException ex)
//             {
//                 mModuleContainer.getIDE().error("File not found. " + ex);
//             }
//             catch (PrinterException ex)
//             {
//                 mModuleContainer.getIDE().error("Error printing. " + ex);
//             }
// 			catch (Exception ex)
// 			{
// 				mModuleContainer.getIDE().error("Error converting from ps to eps. " + ex);
// 			}
//         }
//         else
//         {
//             mModuleContainer.getIDE().info("No Postscript printer service installed.");
//         }
// 	}

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
            printJob.setPrintable(surface);

            // Printing attributes
            PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
            PrintRequestAttribute name = new JobName("Supremica Printing", Locale.ENGLISH);
            attributes.add(name);

            // Show printing dialog
            if (printJob.printDialog(attributes))
            {
                logger.debug("Printing...");

                // Print!
                printJob.print();
                //printJob.print(attributes);

                logger.debug("Printing done!");
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
                if (decl != null)
                {
					final String name = decl.getName();
					final SimpleIdentifierSubject ident =
						new SimpleIdentifierSubject(name);
					final EditorEvents eventpane = gedit.getEventPane();
					final EventTableModel model =
						(EventTableModel) eventpane.getModel();
					model.addIdentifier(ident);
				}
            }
        });
    }

    /**
     * This is an auxiliary method that is used when creating an Encapsulated
     * Postscript output. It is responsible for updating the limits of the
     * eps-style BoundingBox, finding the minimal and maximal (x,y)-pairs.
     *
     * @param bbLimits - {xmin, ymin, xmax, ymax} - the current values of the overall bounding box.
     * @param bounds - a bounding box for a piece of the graph, e.g. for a node or an edge.
     * @return Updated bbLimits.
     */
    private double[] updateBoundingBoxLimits(double[] bbLimits, Rectangle2D bounds, AffineTransform transform)
    {
        double[] boundsCoords = new double[]{bounds.getMinX(), bounds.getMaxY(), bounds.getMaxX(), bounds.getMinY()};

        if (transform != null)
        {
            transform.transform(boundsCoords, 0, boundsCoords, 0, 2);
        }
        else //If no transform was given, then the boundsCoords should have normal ordering, i.e. minX, minY, maxX, maxY
        {
            double temp = boundsCoords[1];
            boundsCoords[1] = boundsCoords[3];
            boundsCoords[3] = temp;
        }

        // minX
        if (boundsCoords[0] < bbLimits[0])
        {
            bbLimits[0] = boundsCoords[0];
        }
        // minY
        if (boundsCoords[1] < bbLimits[1])
        {
            bbLimits[1] = boundsCoords[1];
        }
        // maxX
        if (boundsCoords[2] > bbLimits[2])
        {
            bbLimits[2] = boundsCoords[2];
        }
        // maxY
        if (boundsCoords[3] > bbLimits[3])
        {
            bbLimits[3] = boundsCoords[3];
        }

        return bbLimits;
    }
}
