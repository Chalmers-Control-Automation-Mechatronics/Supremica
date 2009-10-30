//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.base
//# CLASS:   DocumentElement
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.plain.base;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>An immutable implementation of the {@link DocumentProxy} interface.</P>
 *
 * <P>This abstract extends the behaviour of a {@link NamedElement} by
 * adding a settable member representing the document's file name. Although
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
   * @param  name        The name of the new document.
   * @param  comment     The comment describing the new document.
   * @param  location    The file location for the new document,
   *                     or <CODE>null</CODE>.
   */
  protected DocumentElement(final String name,
			    final String comment,
			    final URI location)
  {
    super(name);
    mComment = comment;
    mLocation = location;
  }

  /**
   * Creates a document with default values.
   * This constructor creates a document with no specified file location,
   * and with no associated comment.
   * @param  name        The name of the new document.
   */
  protected DocumentElement(final String name)
  {
    this(name, null, null);
  }

  /**
   * Creates a copy of a document.
   * @param  partner     The object to be copied from.
   */
  protected DocumentElement(final DocumentProxy partner)
  {
    super(partner);
    mComment = partner.getComment();
    mLocation = partner.getLocation();
  }


  //#########################################################################
  //# Cloning
  public DocumentElement clone()
  {
    final DocumentElement cloned = (DocumentElement) super.clone();
    cloned.mComment = mComment;
    cloned.mLocation = null;
    return cloned;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.DocumentProxy
  public String getComment()
  {
    return mComment;
  }

  public URI getLocation()
  {
    return mLocation;
  }

  public File getFileLocation() throws MalformedURLException
  {
    if (mLocation == null) {
      return null;
    }
    final URL url = mLocation.toURL();
    final String proto = url.getProtocol();
    if (proto.equals("file")) {
      final String path = mLocation.getPath();
      return new File(path);
    } else {
      throw new MalformedURLException
        ("Location '" + url + "' does not represent a writable file!");
    }
  }

  public void setLocation(final URI location)
  {
    mLocation = location;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public boolean equalsByContents(final Proxy partner)
  {
    if (super.equalsByContents(partner)) {
      final DocumentProxy doc = (DocumentProxy) partner;
      return
	mComment == null ?
	doc.getComment() == null :
	mComment.equals(doc.getComment());
    } else {
      return false;
    }
  }

  public int hashCodeByContents()
  {
    int result = super.hashCodeByContents();
    if (mComment != null) { 
      result += 5 * mComment.hashCode();
    }
    return result;
  }


  //#########################################################################
  //# Data Members
  private String mComment;
  private URI mLocation;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
