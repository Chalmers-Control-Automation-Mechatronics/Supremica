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

package net.sourceforge.waters.build.jniglue;

import java.util.Collection;
import java.util.Iterator;


abstract class StringProcessorForeach implements ProcessorForeach {

  //#########################################################################
  //# Constructors
  StringProcessorForeach(final String varname)
  {
    mVariableName = varname;
  }


  //#########################################################################
  //# Provided by User
  abstract Collection<?> getStringCollection();


  //#########################################################################
  //# Interface net.sourceforge.waters.build.jniglue.ProcessorForeach
  public Iterator<?> getIterator()
  {
    final Collection<?> collection = getStringCollection();
    final Iterator<?> iter = collection.iterator();
    return new GlueIterator(iter);
  }


  //#########################################################################
  //# Local Class GlueIterator
  private class GlueIterator implements Iterator<Object> {

    //#######################################################################
    //# Constructors
    private GlueIterator(final Iterator<?> iter)
    {
      mIterator = iter;
    }

    //#######################################################################
    //# Interface java.util.Iterator
    public boolean hasNext()
    {
      return mIterator.hasNext();
    }

    public Object next()
    {
      final String text = (String) mIterator.next();
      return new GlueString(text);
    }

    public void remove()
    {
      mIterator.remove();
    }

    //#######################################################################
    //# Data Members
    private final Iterator<?> mIterator;
  }


  //#########################################################################
  //# Local Class GlueString
  private class GlueString implements WritableGlue {

    //#######################################################################
    //# Constructors
    private GlueString(final String text)
    {
      mProcessor = new DefaultProcessorVariable(text);
    }

    //#######################################################################
    //# interface net.sourceforge.waters.build.jniglue.WritableGlue
    public void registerProcessors(final TemplateContext context)
    {
      context.registerProcessorVariable(mVariableName, mProcessor);
    }

    //#######################################################################
    //# Data Members
    private final ProcessorVariable mProcessor;
  }


  //#########################################################################
  //# Data Members
  private final String mVariableName;

}
