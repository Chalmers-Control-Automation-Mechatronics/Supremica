//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   DocumentSubject
//###########################################################################
//# $Id: DocumentSubject.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.subject.base;

import java.io.File;

import net.sourceforge.waters.model.base.DocumentProxy;


/**
 * <P>An immutable implementation of the {@link DocumentProxy} interface.</P>
 *
 * <P>This abstract extends the behaviour of a {@link NamedSubject} by
 * adding a settable member reprenting the document's file name. Although
 * the element is considered immutable its target file name is changeable,
 * so as to make it possible to write the same document into different
 * files.</P>
 *
 * @author Robi Malik
 */

public abstract class DocumentSubject
  extends NamedSubject
  implements DocumentProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a document.
   * @param  name        The name of the new element.
   */
  protected DocumentSubject(final String name,
                            final File location)
  {
    super(name);
    mLocation = location;
  }

  /**
   * Creates a copy of a document.
   * @param  partner     The object to be copied from.
   */
  protected DocumentSubject(final DocumentProxy partner)
  {
    super(partner);
    mLocation = partner.getLocation();
  }


  //#########################################################################
  //# Cloning
  public DocumentSubject clone()
  {
    final DocumentSubject cloned = (DocumentSubject) super.clone();
    cloned.mLocation = null;
    return cloned;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.DocumentProxy
  public File getLocation()
  {
    return mLocation;
  }

  public void setLocation(File location)
  {
    mLocation = location;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.subject.base.Subject
  public DocumentSubject getDocument()
  {
    return this;
  }


  //#########################################################################
  //# Data Members
  private File mLocation;

}
