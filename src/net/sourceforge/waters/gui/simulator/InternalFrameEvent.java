package net.sourceforge.waters.gui.simulator;

import java.util.EventObject;

public class InternalFrameEvent extends EventObject
{
  //########################################################################
  //# Constructors
  public InternalFrameEvent(final String automatonName, final AutomatonInternalFrame frame, final boolean opening)
  {
    super(automatonName);
    mName = automatonName;
    mFrame = frame;
    mOpening = opening;
  }

  //########################################################################
  //# Accessor Methods

  public String getName()
  {
    return mName;
  }
  public boolean isOpeningEvent()
  {
    return mOpening;
  }
  public AutomatonInternalFrame getFrame()
  {
    return mFrame;
  }

  //########################################################################
  //# Data members

  private final boolean mOpening;
  private final String mName;
  private final AutomatonInternalFrame mFrame;

  //########################################################################
  //# Class Constants

  private static final long serialVersionUID = -1492942591191059689L;
}
