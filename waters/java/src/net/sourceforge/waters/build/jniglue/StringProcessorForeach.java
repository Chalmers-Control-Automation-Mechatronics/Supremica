//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   StringProcessorForeach
//###########################################################################
//# $Id: StringProcessorForeach.java,v 1.1 2005-02-18 01:30:10 robi Exp $
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
  abstract Collection getStringCollection();


  //#########################################################################
  //# Interface net.sourceforge.waters.build.jniglue.ProcessorForeach
  public Iterator getIterator()
  {
    final Collection collection = getStringCollection();
    final Iterator iter = collection.iterator();
    return new GlueIterator(iter);
  }


  //#########################################################################
  //# Local Class GlueIterator
  private class GlueIterator implements Iterator {

    //#######################################################################
    //# Constructors
    private GlueIterator(final Iterator iter)
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
    private final Iterator mIterator;
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