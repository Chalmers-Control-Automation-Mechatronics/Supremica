//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.instance
//# CLASS:   SingleEventOutput
//###########################################################################
//# $Id: SingleEventOutput.java,v 1.1 2008-06-18 09:35:34 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.instance;

import net.sourceforge.waters.model.compiler.context.SourceInfo;


/**
 * A record with the information needed to place an event identifier into
 * the instance compiler's output module. An event output consists of a
 * {@link CompiledSingleEvent} object which contains the event identifier
 * and a {@link SourceInfo} object which contains the location and context
 * of the original identifier in the source. This event output information
 * depends on the context and therefore does not exist statically. Rather,
 * it is created dynamically using an {@link EventOutputIterable} each time
 * an identifier in a label block is encountered.
 *
 * @author Robi Malik
 */

class SingleEventOutput
{

  //#########################################################################
  //# Constructor
  SingleEventOutput(final CompiledSingleEvent event,
                    final SourceInfo info)
  {
    mEvent = event;
    mSourceInfo = info;
  }
    

  //#########################################################################
  //# Simple Access
  CompiledSingleEvent getEvent()
  {
    return mEvent;
  }

  SourceInfo getSourceInfo()
  {
    return mSourceInfo;
  }


  //#########################################################################
  //# Data Members
  private final CompiledSingleEvent mEvent;
  private final SourceInfo mSourceInfo;

}