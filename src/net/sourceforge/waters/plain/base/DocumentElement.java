//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.base
//# CLASS:   DocumentElement
//###########################################################################
//# $Id: DocumentElement.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.base;

import java.io.File;

import net.sourceforge.waters.model.base.DocumentProxy;


/**
 * <P>An immutable implementation of the {@link DocumentProxy} interface.</P>
 *
 * <P>This abstract extends the behaviour of a {@link NamedElement} by
 * adding a settable member reprenting the document's file name. Although
 * the element is considered immutable its target file name is changeable,
 * so as to make it possible to write the same document into different
 * files.</P>
 *
 * @author Robi Malik
 */

public abstract class DocumentElement
  extends NamedElement
  implements DocumentProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a document.
   * @param  name        The name of the new element.
   */
  protected DocumentElement(final String name,
			    final File location)
  {
    super(name);
    mLocation = location;
  }

  /**
   * Creates a copy of a named element.
   * @param  partner     The object to be copied from.
   */
  protected DocumentElement(final DocumentElement partner)
  {
    super(partner);
    mLocation = partner.mLocation;
  }


  //#########################################################################
  //# Cloning
  public DocumentElement clone()
  {
    final DocumentElement cloned = (DocumentElement) super.clone();
    cloned.mLocation = null;
    return cloned;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.DocumentElement
  public File getLocation()
  {
    return mLocation;
  }

  public void setLocation(File location)
  {
    mLocation = location;
  }


  //#########################################################################
  //# Data Members
  private File mLocation;

}
