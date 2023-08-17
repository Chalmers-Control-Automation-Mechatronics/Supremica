//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.gui;

import java.awt.Frame;

import javax.swing.JDialog;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
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
 * to be displayed and edited. The <I>primary graph</I> is a monitored subject
 * ({@link GraphSubject}) that can be edited in a controlled way. The backup
 * graph panel creates a <I>secondary graph</I> as a copy of the primary graph
 * when a major modification is initiated, such as running the spring embedder
 * ({@link SpringEmbedder}) or a drag operation. During such operations, the
 * secondary graph can be updated frequently; but only when the operation
 * completes, the changes are committed to the primary graph using a single
 * command.
 * </P>
 *
 * <P>
 * In addition, the backup graph panel provides support to detect graphs with
 * insufficient geometry information, and to invoke the spring embedder and
 * animate its progress for such graphs.
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

  public BackupGraphPanel(final GraphSubject graph,
                          final ModuleSubject module,
                          final ModuleContext context)
  {
    super(graph, module, context);
  }

  //#########################################################################
  //# Simple Access
  @Override
  public ModuleSubject getModule()
  {
    return (ModuleSubject) super.getModule();
  }

  @Override
  public GraphSubject getGraph()
  {
    return (GraphSubject) super.getGraph();
  }

  @Override
  public GraphProxy getDrawnGraph()
  {
    if (mSecondaryGraph != null) {
      return mSecondaryGraph;
    } else {
      return super.getGraph();
    }
  }

  @Override
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
  @Override
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
  @Override
  public void close()
  {
    final GraphSubject graph = getGraph();
    graph.removeModelObserver(mGraphModelObserver);
    final ModuleSubject module = getModule();
    if (module != null) {
      module.getEventDeclListModifiable().removeModelObserver(mGraphModelObserver);
    }
    super.close();
  }

  protected void graphChanged(final ModelChangeEvent event)
  {
    clearTransform();
    repaint();
  }

  protected void registerGraphObserver()
  {
    final GraphSubject graph = getGraph();
    graph.addModelObserver(mGraphModelObserver);
    final ModuleSubject module = getModule();
    if (module != null) {
      module.getEventDeclListModifiable().addModelObserver(mGraphModelObserver);
    }
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
      createSecondaryGraph();
      final SimpleComponentSubject comp =
        (SimpleComponentSubject) getGraph().getParent();
      final String name = comp == null ? "graph" : comp.getName();
      final long timeout = Config.GUI_EDITOR_SPRING_EMBEDDER_TIMEOUT.getValue();
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

  protected ProxySubject getCopy(final Proxy proxy)
  {
    final ProxySubject subject = (ProxySubject) proxy;
    return getCopy(subject);
  }

  protected ProxySubject getCopy(final ProxySubject item)
  {
    assert(item != null);
    if (mSecondaryGraph == null) {
      return item;
    } else {
      return mSecondaryGraph.getCopy(item);
    }
  }


  //#########################################################################
  //# Inner Class GraphModelObserver
  private class GraphModelObserver implements ModelObserver
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.subject.base.ModelObserver
    @Override
    public void modelChanged(final ModelChangeEvent event)
    {
      graphChanged(event);
    }

    @Override
    public int getModelObserverPriority()
    {
      return ModelObserver.RENDERING_PRIORITY;
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
