package net.sourceforge.waters.gui.renderer;


import net.sourceforge.waters.model.base.Proxy;
import java.util.List;

public interface ProxyShape
  extends RendererShape
{
    public Proxy getProxy();
  
    public List<Handle> getHandles();

    public Handle getClickedHandle(int x, int y);
}
