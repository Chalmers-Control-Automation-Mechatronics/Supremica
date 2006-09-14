//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.plain.base
//# CLASS:   DocumentElement
//###########################################################################
//# $Id: DocumentElement.java,v 1.4 2006-09-14 11:51:27 robi Exp $
//###########################################################################

package net.sourceforge.waters.plain.base;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

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
   * Creates a document without specified file location.
   * @param  name        The name of the new document.
   */
  protected DocumentElement(final String name)
  {
    this(name, null);
  }

  /**
   * Creates a document.
   * @param  name        The name of the new document.
   * @param  location    The file location for the new document,
   *                     or <CODE>null</CODE>.
   */
  protected DocumentElement(final String name,
			    final URI location)
  {
    super(name);
    mLocation = location;
  }

  /**
   * Creates a copy of a document.
   * @param  partner     The object to be copied from.
   */
  protected DocumentElement(final DocumentProxy partner)
  {
    super(partner);
    mLocation = partner.getLocation();
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
  public URI getLocation()
  {
    return mLocation;
  }

  public File getFileLocation() throws MalformedURLException
  {
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
  //# Data Members
  private URI mLocation;

}
