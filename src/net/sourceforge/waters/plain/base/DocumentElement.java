//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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
  @Override
  public DocumentElement clone()
  {
    final DocumentElement cloned = (DocumentElement) super.clone();
    cloned.mComment = mComment;
    cloned.mLocation = null;
    return cloned;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.DocumentProxy
  @Override
  public String getComment()
  {
    return mComment;
  }

  @Override
  public URI getLocation()
  {
    return mLocation;
  }

  @Override
  public File getFileLocation() throws MalformedURLException
  {
    return getFileLocation(mLocation);
  }

  @Override
  public void setLocation(final URI location)
  {
    mLocation = location;
  }


  //#########################################################################
  //# Static Methods
  public static File getFileLocation(final URI uri) throws MalformedURLException
  {
    if (uri == null) {
      return null;
    }
    final URL url = uri.toURL();
    final String proto = url.getProtocol();
    if (proto.equals("file")) {
      final String path = uri.getPath();
      return new File(path);
    } else {
      throw new MalformedURLException
        ("Location '" + url + "' does not represent a writable file!");
    }
  }


  //#########################################################################
  //# Data Members
  private String mComment;
  private URI mLocation;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
