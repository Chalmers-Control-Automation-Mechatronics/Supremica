package net.sourceforge.waters.gui.renderer;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.GraphPanel.DragOverStatus;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.subject.base.ProxySubject;

public class PrintRenderingContext extends ModuleRenderingContext
{

    public PrintRenderingContext(final ModuleContext context)
  {
    super(context);
  }

    //#######################################################################
    //# Interface net.sourceforge.waters.gui.renderer.RenderingContext
    @Override
    public RenderingInformation getRenderingInformation(final Proxy proxy)
    {
      final ProxySubject item = (ProxySubject) proxy;
      final int priority = getPriority(item);
      return new RenderingInformation
        (false, false, false, false,
         EditorColor.getColor(item, DragOverStatus.NOTDRAG, false,
                              false, false),
         EditorColor.getShadowColor(item, DragOverStatus.NOTDRAG, false,
                                    false, false),
         priority);
    }

}
