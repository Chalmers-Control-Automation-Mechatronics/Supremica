//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.gui.springembedder;

import java.util.EventObject;


/**
 * <P>A notification sent by a the spring embedder to inform its observers
 * about its progress.</P>
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


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
