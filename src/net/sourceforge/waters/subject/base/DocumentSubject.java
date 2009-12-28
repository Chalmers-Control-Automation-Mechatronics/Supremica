//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.base
//# CLASS:   DocumentSubject
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.subject.base;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import net.sourceforge.waters.model.base.DocumentProxy;


/**
 * <P>The subject implementation of the {@link DocumentProxy} interface.</P>
 *
 * <P>This abstract extends the behaviour of a {@link NamedSubject} by
 * adding a settable member reprenting the document's file name.
 * In this implementation, changes to the file location do not produce
 * any change events.</P>
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
   * Creates a document without specified file location, and with no
   * comment.
   * @param  name        The name of the new document.
   */
  protected DocumentSubject(final String name)
  {
    this(name, null, null);
  }

  /**
   * Creates a document.
   * @param  name        The name of the new document.
   * @param  comment     The comment string for the new document,
   *                     or <CODE>null</CODE>.
   * @param  location    The file location for the new document,
   *                     or <CODE>null</CODE>.
   */
  protected DocumentSubject(final String name,
                            final String comment,
			    final URI location)
  {
    super(name);
    mComment = comment;
    mLocation = location;
  }

  /**
   * Creates a copy of a document.
   * @param  partner     The document to be copied from.
   */
  protected DocumentSubject(final DocumentProxy partner)
  {
    super(partner);
    mComment = partner.getComment();
    mLocation = partner.getLocation();
  }


  //#########################################################################
  //# Cloning
  public DocumentSubject clone()
  {
    final DocumentSubject cloned = (DocumentSubject) super.clone();
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

  /**
   * Assigns a new comment to this document.
   * @param  comment     The new comment string, or <CODE>null</CODE>.
   */
  public void setComment(final String comment)
  {
    mComment = comment;
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
  //# Interface net.sourceforge.waters.subject.base.Subject
  public DocumentSubject getDocument()
  {
    return this;
  }


  //#########################################################################
  //# Data Members
  private String mComment;
  private URI mLocation;

}
