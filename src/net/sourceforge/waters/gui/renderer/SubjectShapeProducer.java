package net.sourceforge.waters.gui.renderer;

import java.awt.Frame;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.swing.JDialog;

import net.sourceforge.waters.gui.springembedder.SpringAbortDialog;
import net.sourceforge.waters.gui.springembedder.SpringEmbedder;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PointGeometrySubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.subject.module.SplineGeometrySubject;
import net.sourceforge.waters.xsd.module.SplineKind;

import org.supremica.log.*;
import org.supremica.properties.Config;
import net.sourceforge.waters.gui.EditorGraph;


public class SubjectShapeProducer
extends ProxyShapeProducer
implements ModelObserver
{
	private static final Logger logger =
		LoggerFactory.createLogger(SubjectShapeProducer.class);
	
	public SubjectShapeProducer(final GraphSubject graph,
                              final ModuleProxy module,
                              final Frame root)
	throws GeometryAbsentException
	{
		super(module);
		boolean runEmbedder = false;
		Random rand = new Random();
    if (graph.getBlockedEvents() != null) {
      if (graph.getBlockedEvents().getGeometry() == null) {
        graph.getBlockedEvents().setGeometry(new LabelGeometrySubject(new Point(5, 5)));
      }
    }
		for (NodeSubject node : graph.getNodesModifiable())
		{
			if (node instanceof SimpleNodeSubject)
			{
				SimpleNodeSubject n = (SimpleNodeSubject) node;
				if (n.isInitial())
				{
					if (n.getInitialArrowGeometry() == null)
					{
						n.setInitialArrowGeometry
						(new PointGeometrySubject(new Point(-5, -5)));
					}
				}
				
				if (n.getPointGeometry() == null)
				{
					runEmbedder = true;
					final int base;
					final int spread;
					if (n.isInitial())
					{
						base = 10;
						spread = 50;
					}
					else
					{
						base = 100;
						spread = 500;
					}
					n.setPointGeometry(new PointGeometrySubject
							(new Point(base + rand.nextInt(spread),
									base + rand.nextInt(spread))));
				}
				if (n.getLabelGeometry() == null)
				{
					n.setLabelGeometry(new LabelGeometrySubject(new Point(5, 5)));
				}
			}
			else if (node instanceof GroupNodeSubject)
			{
				if (((GroupNodeSubject)node).getGeometry() == null)
				{
					throw new GeometryAbsentException("There is no geometry information"
							+ " for a group node in this graph");
				}
			}
		}
		for (EdgeSubject edge : graph.getEdgesModifiable())
		{
			if (edge.getGeometry() == null)
			{
				final Collection<Point2D> points =
					Collections.singleton(GeometryTools.getMidPoint
							(GeometryTools.getPosition(edge.getSource()),
									GeometryTools.getPosition(edge.getTarget())
							));
				edge.setGeometry(new SplineGeometrySubject(points,
						SplineKind.INTERPOLATING));
			}
			if (edge.getStartPoint() == null)
			{
				Point2D p1 = edge.getGeometry().getPoints().get(0);
				PointGeometrySubject p =
					new PointGeometrySubject
					(GeometryTools.defaultPosition(edge.getSource(), p1));
				edge.setStartPoint(p);
			}
			if (edge.getEndPoint() == null)
			{
				PointGeometrySubject p = new PointGeometrySubject(
						GeometryTools.defaultPosition(edge.getTarget(),
								edge.getGeometry().getPoints().get(0)));
				edge.setEndPoint(p);
			}
			if (edge.getLabelBlock().getGeometry() == null)
			{
				LabelGeometrySubject offset =
					new LabelGeometrySubject
					(new Point(LabelBlockProxyShape.DEFAULTOFFSETX,
							LabelBlockProxyShape.DEFAULTOFFSETY));
				edge.getLabelBlock().setGeometry(offset);
			}
			if(edge.getGuardActionBlock() == null) {
				//do nothing
				/*
				ModuleSubjectFactory m = ModuleSubjectFactory.getInstance();
				GuardActionBlockSubject block = m.createGuardActionBlockProxy();
				((EdgeSubject) edge).setGuardActionBlock(block);
				*/
			} else if (edge.getGuardActionBlock().getGeometry() == null) {
				//do nothing
			}
		}
		if (runEmbedder)
		{
			final SimpleComponentSubject comp =
				(SimpleComponentSubject) graph.getParent();
			final String name = comp == null ? "graph" : comp.getName();
			final long timeout = Config.GUI_EDITOR_SPRING_EMBEDDER_TIMEOUT.get();
			final SpringEmbedder embedder = new SpringEmbedder(graph);
			final Thread thread = new Thread(embedder);
			final JDialog dialog =
				new SpringAbortDialog(root, name, embedder, timeout);
			dialog.setLocationRelativeTo(root);
			dialog.setVisible(true);
			thread.start();
		}
		graph.addModelObserver(this);
	}
	
	public SubjectShapeProducer(Subject graph, ModuleProxy module)
	{
		super(module);
		graph.addModelObserver(this);
	}
	
	
	private void removeMapping(NodeProxy node)
	{
		getMap().remove(node);
    if (node instanceof SimpleNodeProxy) {
      removeMapping(((SimpleNodeProxy)node).getLabelGeometry());
    }
	}
	
	private void removeMapping(GroupNodeProxy node)
	{
		getMap().remove(node);
	}
	
	private void removeMapping(LabelGeometryProxy label)
	{
		getMap().remove(label);
	}
	
	private void removeMapping(EdgeProxy edge)
	{
		getMap().remove(edge);
		removeMapping(edge.getLabelBlock());
		removeMapping(edge.getGuardActionBlock());
	}
	
	private void removeMapping(LabelBlockProxy label)
	{
		getMap().remove(label);
		for (Proxy p : label.getEventList())
		{
			getMap().remove(p);
		}
	}
	
	private void removeMapping(GuardActionBlockProxy GA)
	{
		getMap().remove(GA);
		if(GA == null) return;
		for(BinaryExpressionProxy action : GA.getActions())
		{
			getMap().remove(GA.getActions());
		}
		List<SimpleExpressionProxy> guards = GA.getGuards();
		if(!guards.isEmpty())
		{
			SimpleExpressionProxy guard = guards.get(0);
			getMap().remove(guard);
		}
	}
	
	private void removeMapping(Subject subject)
	{
		if (subject instanceof SimpleNodeProxy)
		{
			removeMapping((SimpleNodeProxy)subject);
		}
		if (subject instanceof GroupNodeProxy)
		{
			removeMapping((GroupNodeProxy)subject);
		}
		if (subject instanceof EdgeProxy)
		{
			removeMapping((EdgeProxy)subject);
		}
		if (subject instanceof LabelBlockProxy)
		{
			removeMapping((LabelBlockProxy)subject);
		}
		if (subject instanceof GuardActionBlockProxy)
		{
			removeMapping((GuardActionBlockProxy)subject);
		}
    if (subject instanceof LabelGeometryProxy)
		{
			removeMapping((LabelGeometryProxy)subject);
		}
	}
	
	public void modelChanged(ModelChangeEvent event)
	{
		if (event.getKind() == ModelChangeEvent.ITEM_REMOVED ||
				event.getKind() == ModelChangeEvent.ITEM_ADDED)
		{
			if (event.getSource().getParent() instanceof EventListExpressionSubject)
			{
				Subject subject = event.getSource().getParent();
				if (subject.getParent() instanceof SimpleNodeProxy)
				{
					removeMapping((SimpleNodeProxy) subject.getParent());
				}
				else if (subject instanceof LabelBlockProxy)
				{
					removeMapping((LabelBlockProxy)subject);
				}
			}
			else
			{
				removeMapping((Subject)event.getValue());
			}
		}
		else if (event.getSource() instanceof SimpleNodeProxy)
		{
			if (event.getKind() == ModelChangeEvent.NAME_CHANGED)
			{
				removeMapping(((SimpleNodeProxy)event.getSource()).getLabelGeometry());
			}
			else
			{
				removeMapping((SimpleNodeProxy)event.getSource());
			}
		}
		else
		{
			removeMapping(event.getSource());
		}
	}
}
