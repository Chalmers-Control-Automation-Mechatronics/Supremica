//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.build.jniglue
//# CLASS:   TemplateFragmentSequence
//###########################################################################
//# $Id: TemplateFragmentSequence.java,v 1.1 2005-02-18 01:30:10 robi Exp $
//###########################################################################

package net.sourceforge.waters.build.jniglue;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


class TemplateFragmentSequence extends TemplateFragment {

  //#########################################################################
  //# Constructors
  TemplateFragmentSequence()
  {
    super(-1);
    mBody = new LinkedList();
  }


  //#########################################################################
  //# Simple Access
  void addFragment(final TemplateFragment fragment)
  {
    mBody.add(fragment);
  }


  //#########################################################################
  //# Code Generation
  void writeCppGlue(final CppGlueWriter writer, final TemplateContext context)
  {
    final Iterator iter = mBody.iterator();
    while (iter.hasNext()) {
      final TemplateFragment fragment = (TemplateFragment) iter.next();
      fragment.writeCppGlue(writer, context);
    }
  }


  //#########################################################################
  //# Data Members
  private final List mBody;

}