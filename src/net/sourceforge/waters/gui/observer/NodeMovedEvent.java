package net.sourceforge.waters.gui.observer;

import net.sourceforge.waters.model.base.GeometryProxy;

import net.sourceforge.waters.model.module.NodeProxy;

public class NodeMovedEvent
	implements EditorChangedEvent
{
	private final GeometryProxy mOldGeometry;
	private final GeometryProxy mNewGeometry;
	private final NodeProxy mNode;
	
	public NodeMovedEvent(GeometryProxy oldGeometry, GeometryProxy newGeometry,
							NodeProxy node)
	{
		mOldGeometry = oldGeometry;
		mNewGeometry = newGeometry;
		mNode = node;
	}
	
	public int getType()
	{
		return NODEMOVED;
	}
	
	public GeometryProxy getOld()
	{
		return mOldGeometry;
	}
	
	public GeometryProxy getNew()
	{
		return mNewGeometry;
	}
	
	public NodeProxy getNode()
	{
		return mNode;
	}
}
