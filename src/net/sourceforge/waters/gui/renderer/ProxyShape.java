package net.sourceforge.waters.gui.renderer;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;

import net.sourceforge.waters.model.base.Proxy;
import java.util.List;

public interface ProxyShape
  extends RendererShape
{
	public Proxy getProxy();
  
  public List<Handle> getHandles();
  
  public Handle getClickedHandle(int x, int y);
}
