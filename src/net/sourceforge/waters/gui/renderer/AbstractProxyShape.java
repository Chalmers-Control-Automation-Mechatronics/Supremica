package net.sourceforge.waters.gui.renderer;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;

import net.sourceforge.waters.model.base.Proxy;
import java.util.List;
import java.util.Collections;

public abstract class AbstractProxyShape
  extends AbstractRendererShape
	implements ProxyShape
{
	private final Proxy mProxy;
	
	protected AbstractProxyShape(Proxy proxy)
	{
		mProxy = proxy;
	}
  
	public void draw(Graphics2D g, RenderingInformation status)
	{
		super.draw(g, status);
		if (status.showHandles()) {
			for (RendererShape handle : getHandles()) {
				handle.draw(g, status);
			}
		}
	}
	
	public Proxy getProxy()
	{
		return mProxy;
	}
  
  public List<Handle> getHandles()
  {
    return Collections.emptyList();
  }
  
  public boolean isClicked(int x, int y)
  {
    for (Handle h : getHandles()) {
      if (h.isClicked(x, y)) {
        return true;
      }
    }
    return super.isClicked(x, y);
  }
  
  public Handle getClickedHandle(int x, int y)
  {
    for (Handle handle : getHandles()) {
      if (handle.isClicked(x, y)) {
        return handle;
      }
    }
    return null;
  }
}
