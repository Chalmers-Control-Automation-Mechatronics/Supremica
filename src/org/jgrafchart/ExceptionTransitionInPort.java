package org.jgrafchart;

import com.nwoods.jgo.*;
import java.awt.*;

  
  public class ExceptionTransitionInPort extends JGoPort
  {
    public ExceptionTransitionInPort()
    {
      super();
      setSelectable(false);
      setDraggable(false);
      setStyle(StyleRectangle);
      setValidDestination(true);
      setValidSource(false);
    }
  }
