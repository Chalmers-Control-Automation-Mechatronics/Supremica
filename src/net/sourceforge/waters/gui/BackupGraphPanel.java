//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   BackupGraphPanel
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Frame;

import javax.swing.JDialog;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.renderer.ProxyShapeProducer;
import net.sourceforge.waters.gui.renderer.RenderingContext;
import net.sourceforge.waters.gui.renderer.SubjectShapeProducer;
import net.sourceforge.waters.gui.springembedder.EmbedderEvent;
import net.sourceforge.waters.gui.springembedder.EmbedderObserver;
import net.sourceforge.waters.gui.springembedder.SpringAbortDialog;
import net.sourceforge.waters.gui.springembedder.SpringEmbedder;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import org.supremica.properties.Config;


/**
 * <P>
 * A graph display panel with basic modification support.
 * </P>
 *
 * <P>
 * The backup graph panel can manage two copies of a graph ({@link GraphProxy})
 * to be displayed and edited. The <I>primary graph</I> is monitored subject
 * ({@link GraphSubject}) that can be edited in a controlled way. The
 * <I>secondary graph</I> is copy created from the primary graph when a major
 * modification is initiated, such as running the spring embedder
 * ({@link SpringEmbedder}) or a drag operation. During such operations, the
 * secondary graph can be updated frequently; but only when the operation
 * completes, the changes are committed to the primary graph using a single
 * command.
 * </P>
 *
 * <P>
 * In addition, the backup graph panel provides support to detect graphs
 * with insufficient geometry information, and invoke the spring embedder
 * and animate its progress for such graphs.
 * </P>
 *
 * @author Robi Malik
 */

public class BackupGraphPanel
  extends GraphPanel
  implements EmbedderObserver
{

  //#########################################################################
  //# Constructors
  public BackupGraphPanel(final GraphSubject graph, final ModuleSubject module)
  {
    super(graph, module);
  }


  //#########################################################################
  //# Simple Access
  public ModuleSubject getModule()
  {
    return (ModuleSubject) super.getModule();
  }

  public GraphSubject getGraph()
  {
    return (GraphSubject) super.getGraph();
  }

  public GraphProxy getDrawnGraph()
  {
    if (mSecondaryGraph != null) {
      return mSecondaryGraph;
    } else {
      return super.getGraph();
    }
  }

  public SubjectShapeProducer getShapeProducer()
  {
    if (mSecondaryGraph != null) {
      assert(mSecondaryShapeProducer != null);
      return mSecondaryShapeProducer;
    } else {
      return (SubjectShapeProducer) super.getShapeProducer();
    }
  }

  public boolean isEmbedderRunning()
  {
    return mEmbedder != null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.springembedder.EmbedderObserver
  public void embedderChanged(final EmbedderEvent event)
  {
    if (event.getType() == EmbedderEvent.EmbedderEventType.EMBEDDER_STOP) {
      closeEmbedder();
      commitSecondaryGraph("Automatic Layout");
      clearSecondaryGraph();
    }
  }


  //#########################################################################
  //# Repaint Support
  public void close()
  {
    final ProxyShapeProducer producer = getShapeProducer();
    producer.close();
    final GraphSubject graph = getGraph();
    graph.removeModelObserver(mGraphModelObserver);
  }

  protected void graphChanged(final ModelChangeEvent event)
  {
    repaint();
  }

  protected void registerGraphObserver()
  {
    final GraphSubject graph = getGraph();
    graph.addModelObserver(mGraphModelObserver);
  }


  //#########################################################################
  //# Spring Embedding
  protected boolean ensureGeometryExists()
    throws GeometryAbsentException
  {
    final GraphSubject graph = getGraph();
    final SpringEmbedder embedder = new SpringEmbedder(graph);
    final boolean needEmbedder = embedder.needsGeometry();
    if (needEmbedder) {
      mEmbedder = embedder;
      runEmbedder();
    }
    return needEmbedder;
  }

  protected SpringEmbedder createEmbedder()
  {
    final GraphSubject graph = getGraph();
    mEmbedder = new SpringEmbedder(graph);
    return mEmbedder;
  }


  protected void runEmbedder()
  {
    try {
      mEmbedder.setUpGeometry();
      // ***BUG***
      // For clean undo, geometry should only be added to the secondary graph;
      // Unfortunately, this does not work yet and will cause exceptions...
      // ~~~Robi
      createSecondaryGraph();
      final SimpleComponentSubject comp =
        (SimpleComponentSubject) getGraph().getParent();
      final String name = comp == null ? "graph" : comp.getName();
      final long timeout = Config.GUI_EDITOR_SPRING_EMBEDDER_TIMEOUT.get();
      mEmbedder = new SpringEmbedder(mSecondaryGraph.getNodesModifiable(),
                                     mSecondaryGraph.getEdgesModifiable());
      mEmbedder.addObserver(this);
      final Thread thread = new Thread(mEmbedder);
      final Frame frame = (Frame) getTopLevelAncestor();
      final JDialog dialog =
        new SpringAbortDialog(frame, name, mEmbedder, timeout);
      dialog.setLocationRelativeTo(this);
      dialog.setVisible(true);
      thread.start();
    } catch (final GeometryAbsentException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  protected void closeEmbedder()
  {
    mEmbedder.removeObserver(this);
    mEmbedder = null;
  }


  //#########################################################################
  //# Secondary Graph
  protected EditorGraph getSecondaryGraph()
  {
    return mSecondaryGraph;
  }

  protected boolean createSecondaryGraph()
  {
    if (mSecondaryGraph == null) {
      final ModuleSubject module = getModule();
      final RenderingContext context =
        getShapeProducer().getRenderingContext();
      mSecondaryGraph = new EditorGraph(getGraph());
      mSecondaryShapeProducer = new SubjectShapeProducer
        (mSecondaryGraph, mSecondaryGraph, module, context);
      mSecondaryGraph.addModelObserver(mGraphModelObserver);
      return true;
    } else {
      return false;
    }
  }

  protected void clearSecondaryGraph()
  {
    if (mSecondaryGraph != null) {
      mSecondaryGraph.removeModelObserver(mGraphModelObserver);
      mSecondaryGraph = null;
      mSecondaryShapeProducer = null;
      repaint();
    }
  }

  protected void commitSecondaryGraph(final String description)
  {
    if (mSecondaryGraph != null) {
      final Command cmd =
        mSecondaryGraph.createUpdateCommand(null, description, false);
      if (cmd != null) {
        cmd.execute();
      }
    }
  }

  protected ProxySubject getOriginal(final Proxy proxy)
  {
    final ProxySubject subject = (ProxySubject) proxy;
    return getOriginal(subject);
  }

  protected ProxySubject getOriginal(final ProxySubject item)
  {
    assert(item != null);
    if (mSecondaryGraph == null) {
      return item;
    } else {
      return mSecondaryGraph.getOriginal(item);
    }
  }


  //#########################################################################
  //# Inner Class GraphModelObserver
  private class GraphModelObserver implements ModelObserver
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.subject.base.ModelObserver
    public void modelChanged(final ModelChangeEvent event)
    {
      graphChanged(event);
    }

  }


  //#########################################################################
  //# Data Members
  /**
   * A temporary copy of the currently shown graph.  It contains the
   * uncommitted changes while the user is dragging some objects with the
   * mouse in an incomplete move operation. This variable is
   * <CODE>null</CODE> if no move operation is in progress. The secondary
   * graph is created at the start of each move operation, and disposed
   * after committing the changes.
   */
  private EditorGraph mSecondaryGraph = null;
  /**
   * The shape producer associated with the secondary graph.
   * It is non-null if and only if the secondary graph is non-null, i.e.,
   * while a move operation is in progress.
   */
  private SubjectShapeProducer mSecondaryShapeProducer = null;
  /**
   * The current spring embedder, if any is running.
   */
  private SpringEmbedder mEmbedder = null;
  /**
   * A model observer attached to the secondary graph,
   * to update the display when changes occur.
   */
  private final GraphModelObserver mGraphModelObserver =
    new GraphModelObserver();


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
