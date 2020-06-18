//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.model.base;


/**
 * <P>An empty implementation of the {@link ProxyVisitor} interface.</P>
 *
 * <P>This is an adapter class to make it more convenient to implement
 * visitors that do not explicitly implement all the visit methods.
 * All the visit methods in this adapter class call the visit method for the
 * immediate superclass and afterwards visit all children of their argument.
 * In all cases, <CODE>null</CODE> is returned.</P>
 *
 * <P>Differently from the {@link DefaultProxyVisitor}, the base method
 * {@link #visitProxy(Proxy) visitProxy()} quietly returns without doing
 * anything; it does not throw an exception.</P>

 * @author Robi Malik
 */

abstract public class DescendingProxyVisitor extends DefaultProxyVisitor {

  //#########################################################################
  //# Visitor Methods
  @Override
  public Object visitProxy(final Proxy proxy)
    throws VisitorException
  {
    return null;
  }

}
