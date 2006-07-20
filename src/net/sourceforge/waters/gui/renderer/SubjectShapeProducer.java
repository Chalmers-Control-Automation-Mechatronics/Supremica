package net.sourceforge.waters.gui.renderer;

import net.sourceforge.waters.model.base.Proxy;

import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;

import net.sourceforge.waters.subject.base.ModelObserver;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.Subject;

import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.EventListExpressionSubject;

public class SubjectShapeProducer
	extends ProxyShapeProducer
	implements ModelObserver
{
	public SubjectShapeProducer(Subject graph, ModuleProxy module)
	{
		super(module);
		graph.addModelObserver(this);
	}
	
	private void removeMapping(NodeProxy node)
	{
		getMap().remove(node);
		removeMapping(node.getName());
	}
	
	private void removeMapping(GroupNodeProxy node)
	{
		getMap().remove(node);
	}
	
	private void removeMapping(String label)
	{
		getMap().remove(label);
	}
	
	private void removeMapping(EdgeProxy edge)
	{
		getMap().remove(edge);
		removeMapping(edge.getLabelBlock());
	}
	
	private void removeMapping(LabelBlockProxy label)
	{
		getMap().remove(label);
		for (Proxy p : label.getEventList())
		{
			getMap().remove(p);
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
				removeMapping((String)event.getValue());
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
