//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

import java.util.Iterator;


class TemplateFragmentForeach extends TemplateFragment {

  //#########################################################################
  //# Constructors
  TemplateFragmentForeach(final String name,
			  final TemplateFragment body,
			  final int numskipped,
			  final int lineno)
  {
    super(numskipped, lineno);
    mName = name;
    mBody = body;
  }


  //#########################################################################
  //# Simple Access
  String getName()
  {
    return mName;
  }

  TemplateFragment getBody()
  {
    return mBody;
  }


  //#########################################################################
  //# Code Generation
  void writeCppGlue(final CppGlueWriter writer, TemplateContext context)
  {
    final TemplateContext outer = getRelevantContext(context);
    final ProcessorForeach processor = outer.getProcessorForeach(mName);
    if (processor != null) {
      final Iterator<?> iter = processor.getIterator();
      final int numerrors = writer.getNumErrors();
      final ProcessorINDEX indexproc = new ProcessorINDEX();
      final ProcessorVariable commaproc = new ProcessorCOMMA(iter, ",");
      final ProcessorVariable commaspproc = new ProcessorCOMMA(iter, ", ");
      final ProcessorConditional hasnextproc = new ProcessorHASNEXT(iter);
      while (iter.hasNext() && numerrors == writer.getNumErrors()) {
	final WritableGlue glue = (WritableGlue) iter.next();
	final TemplateContext subcontext = new TemplateContext(context);
	subcontext.registerProcessorVariable("INDEX", indexproc);
	subcontext.registerProcessorVariable("COMMA", commaproc);
	subcontext.registerProcessorVariable("COMMASP", commaspproc);
	subcontext.registerProcessorConditional("HASNEXT", hasnextproc);
	glue.registerProcessors(subcontext);
	mBody.writeCppGlue(writer, subcontext);
      }
      final int count = indexproc.getIndex();
      final ProcessorVariable countproc = new DefaultProcessorVariable(count);
      context.registerProcessorVariable("COUNT", countproc);
    } else {
      writer.reportError
	("Undefined instruction $FOREACH-" + mName, getLineNo());
    }
  }


  //#########################################################################
  //# Local Class ProcessorINDEX
  private class ProcessorINDEX implements ProcessorVariable {

    //######################################################################
    //# Constructors
    private ProcessorINDEX()
    {
      mIndex = 0;
    }

    //######################################################################
    //# Simple Access
    private int getIndex()
    {
      return mIndex;
    }

    //######################################################################
    //# Interface net.sourceforge.waters.build.jniglue.ProcessorVariable
    public String getText()
    {
      return Integer.toString(mIndex++);
    }

    //######################################################################
    //# Data Members
    private int mIndex;

  }


  //#########################################################################
  //# Local Class ProcessorCOMMA
  private class ProcessorCOMMA implements ProcessorVariable {

    //######################################################################
    //# Constructors
    private ProcessorCOMMA(final Iterator<?> iter, final String text)
    {
      mIterator = iter;
      mText = text;
    }

    //######################################################################
    //# Interface net.sourceforge.waters.build.jniglue.ProcessorVariable
    public String getText()
    {
      if (mIterator.hasNext()) {
	return mText;
      } else {
	return "";
      }
    }

    //######################################################################
    //# Data Members
    private final Iterator<?> mIterator;
    private final String mText;

  }


  //#########################################################################
  //# Local Class ProcessorHASNEXT
  private class ProcessorHASNEXT implements ProcessorConditional {

    //######################################################################
    //# Constructors
    private ProcessorHASNEXT(final Iterator<?> iter)
    {
      mIterator = iter;
    }

    //######################################################################
    //# Interface net.sourceforge.waters.build.jniglue.ProcessorConditional
    public boolean isConditionSatisfied()
    {
      return mIterator.hasNext();
    }

    //######################################################################
    //# Data Members
    private final Iterator<?> mIterator;

  }


  //#########################################################################
  //# Data Members
  private final String mName;
  private final TemplateFragment mBody;

}
