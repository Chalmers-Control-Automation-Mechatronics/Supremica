//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   GraphSaveEPSAction
//###########################################################################
//# $Id: GraphSaveEPSAction.java,v 1.2 2007-11-06 11:09:03 flordal Exp $
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import net.sourceforge.waters.gui.ControlledSurface;
import net.sourceforge.waters.gui.renderer.Handle;
import net.sourceforge.waters.gui.renderer.ProxyShapeProducer;
import net.sourceforge.waters.gui.renderer.SimpleNodeProxyShape;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.marshaller.StandardExtensionFileFilter;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.BoxGeometryProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

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
      final String name = getActiveEditorWindowInterface().getComponentName();
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
      try {
        saveEPS(file, graph, shaper);
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

  public static void saveEPS(final File outputfile,
                       final GraphProxy graph,
                       final ProxyShapeProducer shaper)
    throws IOException
  {
      /*
    OutputStream stream = new FileOutputStream(outputfile);
    try {
      stream = new BufferedOutputStream(stream);
      final EPSDocumentGraphics2D g2d = new EPSDocumentGraphics2D(false);
      g2d.setGraphicContext(new GraphicContext());
      final Rectangle2D bounds = shaper.getMinimumBoundingRectangle();
      g2d.translate(-bounds.getX(), -bounds.getY());
      final int width = (int) Math.ceil(bounds.getWidth());
      final int height = (int) Math.ceil(bounds.getHeight());
      g2d.setupDocument(stream, width, height);
      final Renderable renderable = new DefaultRenderable();
      final Renderer renderer = new Renderer();
      renderer.renderGraph(graph, null, renderable, shaper, g2d);
      g2d.finish();
    } finally {
      stream.close();
    }
       */
  
        ///////////////////
        // AVENIR'S CODE //
        ///////////////////
      
        // Create the writer, responsible for writing the information
        // to the EPS-file
        BufferedWriter w = null;
        
        try
        {
            // First we write everything to a temporary file,
            // which is later copied to the final output.
            final File tempfile = File.createTempFile("temp", ".eps");
            w = new BufferedWriter(new FileWriter(tempfile));
            
            ///////////////////
            // START WRITING //
            ///////////////////
            
            // Create the head of the eps-file
            w.write("%!PS-Adobe EPSF-3.0");
            w.newLine();
            w.write("%%Creator: Supremica-IDE");
            w.newLine();
            w.write("%%Title: " + outputfile.getName());
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
            
            // This is somewhat ugly
            final double MARKING_GREY_SCALE = 0.5; // The grayscale level of the marked states
            
            // Define how to draw marked states
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
            
            // An auxiliary object, finding the shapes of the logical objects of the graph, such as nodes, edges, etc.
            shaper.createAllShapes();
            
            // Some transform needed to convert java's pixel representation into postscript coordinate system
            AffineTransform transform = new AffineTransform(1, 0, 0, -1, 0, (new java.awt.print.Paper()).getHeight());
            AffineTransform offsetTransform = new AffineTransform(1, 0, 0, -1, 0, 0);
            AffineTransform labelTransform = new AffineTransform(1, 0, 0, -1, 1, (new java.awt.print.Paper()).getHeight() - 12);
            
            // The delimiters of the eps-file BoundingBox, stored in the following order: minX, minY, maxX, maxY
            double[] boundingBoxLimits = new double[]{(new java.awt.print.Paper()).getWidth(), (new java.awt.print.Paper()).getHeight(), 0, 0};
            
            // For every node...
            // *** BUG *** This must be done through renderer.
            for (NodeProxy node : graph.getNodes())
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
                        for (Handle handle : shaper.getShape(simpleNode).getHandles())
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
                    Shape stateLabelShape = shaper.getShape(simpleNode.getLabelGeometry()).getShape();
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
                            System.err.println("Oops, could this happen? Not implemented...");
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
                    System.err.println("node " + node.getName() + " UNTREATED... (unheard of)");
                }
                
                // Add an empty line after each state-info
                w.newLine();
            }
            
            // For every edge...
            for (EdgeProxy edge : graph.getEdges())
            {
                Shape edgeshape = shaper.getShape(edge).getShape();
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
                    Shape eventShape = shaper.getShape(proxyid).getShape();
                    Point2D leftLowerCorner = labelTransform.transform(new Point2D.Double(eventShape.getBounds2D().getMinX(), eventShape.getBounds2D().getMinY()), null);
                    leftLowerCorner.setLocation(Math.round(leftLowerCorner.getX()), Math.round(leftLowerCorner.getY()));
                    
                    // Event name
                    String eventName = ((IdentifierProxy) proxyid).getName();
                    
                    // Determine controllability of event
                    boolean controllable = true;
                    /*
                    List<EventDeclProxy> eventdecllist = module.getEventDeclList();
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
                     */
                    
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
                        Shape actionShape = shaper.getShape(action).getShape();
                        Point2D actionAnchor = labelTransform.transform(new Point2D.Double(actionShape.getBounds2D().getMinX(), actionShape.getBounds2D().getMinY()), null);
                        actionAnchor.setLocation(Math.round(actionAnchor.getX()), Math.round(actionAnchor.getY()));
                        
                        w.write("(" + action + ") " + actionAnchor.getX() + " " + actionAnchor.getY() + " actionLabel\n");
                        
                        // Update the bounding box
                        boundingBoxLimits = updateBoundingBoxLimits(boundingBoxLimits, actionShape.getBounds2D(), labelTransform);
                    }
                    for (SimpleExpressionProxy guard : guardActionBlock.getGuards())
                    {
                        Shape guardShape = shaper.getShape(guard).getShape();
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
            w.close();
            w = null;
            
            // The recently created eps-file is reprinted, this time with the
            // information about the supremal bounding box added to its header.
            // A temporary file is used for the reprinting
            BufferedReader r = new BufferedReader(new FileReader(tempfile));
            w = new BufferedWriter(new FileWriter(outputfile));
            
            // Every line of command is copied
            // and a "BoundingBox"-line is added
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
            r.close();
            // The old file is deleted.
            tempfile.delete();
        } catch (final IOException exception) {
            throw new RuntimeException(exception);
        } finally {
            // Close the output stream in a finally block,
            // so it happens even in case of exceptions.
            if (w != null) {
                try {
                    w.close();
                } catch (final IOException exception) {
                    throw new RuntimeException(exception);
                }
            }
        } 
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
    private static double[] updateBoundingBoxLimits(double[] bbLimits, Rectangle2D bounds, AffineTransform transform)
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

  //#########################################################################
  //# Data Members
  private JFileChooser mFileChooser = null;
  private boolean mChooserPathFollowsModule = true;

  private static final String EPS = ".eps";

}
