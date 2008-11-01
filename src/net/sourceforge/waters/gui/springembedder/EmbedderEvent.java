//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui.springembedder
//# CLASS:   EmbedderEvent
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.springembedder;

import java.util.EventObject;


/**
 * <P>A notification sent by a the spring embedder to inform its observers
 * abouts its progress.</P>
 *
 * @see EmbedderObserver
 *
 * @author Simon Ware
 */
public class EmbedderEvent extends EventObject
{
  
  //#########################################################################
  //# Static Creator Methods
  /**
   * Creates an <CODE>EMBEDDER_START</CODE> notification.
   */
  static EmbedderEvent createEmbedderStartEvent(final SpringEmbedder embedder)
  {
    return new EmbedderEvent(embedder, EmbedderEventType.EMBEDDER_START);
  }

  /**
   * Creates an <CODE>EMBEDDER_STOP</CODE> notification.
   */
  static EmbedderEvent createEmbedderStopEvent(final SpringEmbedder embedder)
  {
    return new EmbedderEvent(embedder, EmbedderEventType.EMBEDDER_STOP);
  }

  /**
   * Creates an <CODE>EMBEDDER_START</CODE> notification.
   */
  static EmbedderEvent createEmbedderProgressEvent
    (final SpringEmbedder embedder)
  {
    return new EmbedderEvent(embedder, EmbedderEventType.EMBEDDER_PROGRESS);
  }


  //#########################################################################
  //# Constructors
  private EmbedderEvent(final SpringEmbedder embedder,
			final EmbedderEventType type)
  {
    super(embedder);
    mType = type;
  }
  

  //#########################################################################
  //# Simple Access
  public EmbedderEventType getType()
  {
    return mType;
  }

  public SpringEmbedder getSource()
  {
    return (SpringEmbedder) super.getSource();
  }


  //#########################################################################
  //# Inner Class EmbedderEventType
  public static enum EmbedderEventType
  {
    EMBEDDER_START,
    EMBEDDER_PROGRESS,
    EMBEDDER_STOP;
  }


  //#########################################################################
  //# Data Members
  private final EmbedderEventType mType;

}
