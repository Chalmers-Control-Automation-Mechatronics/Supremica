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

package net.sourceforge.waters.analysis.distributed.safetyverifier;

import java.io.Serializable;

import java.util.HashMap;
import java.util.Map;

/**
 * A mapping between states and handlers. This class provides an
 * intermediate mapping between handler ids (as strings) and the state
 * handler object. This makes it more convenient to change the handler
 * without affecting the mapping between states and handlers. This
 * could hinder performance as it is an indirect lookup via a map, so
 * when a hander is set, a protected template hook is called, which
 * subclasses can override to update a higher performance lookup table.
 *
 * This class is not synchronized.
 *
 * @author Sam Douglas
 */
public abstract class StateDistribution implements StateHandler, Serializable
{
  /**
   * Dispatches the given state to the appropriate handler.
   * @param state the state to dispatch
   * @throws Exception if something bad happens
   */
  public void addState(StateTuple state) throws Exception
  {
    lookupStateHandler(state).addState(state);
  }

  /**
   * Sets the hander for a handler ID. This can be used to add 
   * new handlers (with new ids) however it does not guarantee they
   * will be used in the state distribution.
   * @param handlerid the ID of the handler to update.
   * @param handler the object to handle states
   */
  public void setHandler(String handlerid, StateHandler handler)
  {
    mHandlers.put(handlerid, handler);
    handlersUpdated();
  }

  /**
   * Gets the state handler for an ID.
   * @param handlerid the handler id
   * @return the state handler, or null if undefined.
   */
  public StateHandler getHandler(String handlerid)
  {
    return mHandlers.get(handlerid);
  }

  /**
   * Gets an array of state handlers that are associated
   * with this distribution.
   * @return An array of state handlers.
   */
  public StateHandler[] getHandlers()
  {
    return mHandlers.values().toArray(new StateHandler[0]);
  }

  /**
   * Finds a handler for the specified state.
   * @param state to get handler for.
   * @return a state handler.
   */
  public abstract StateHandler lookupStateHandler(StateTuple state);

  /**
   * A template method to allow subclasses to update any 
   * additional mappings when the handlers are updated. 
   */
  protected void handlersUpdated()
  {
  }
  
  private final Map<String,StateHandler> mHandlers = new HashMap<String,StateHandler>();


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}







