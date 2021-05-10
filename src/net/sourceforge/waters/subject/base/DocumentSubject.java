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

package net.sourceforge.waters.subject.base;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Set;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.ProxyTools;


/**
 * <P>The subject implementation of the {@link DocumentProxy} interface.</P>
 *
 * <P>This abstract extends the behaviour of a {@link NamedSubject} by
 * adding a settable member representing the document's file name and a comment
 * string. In this implementation, changes to the file location do not produce
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
  //# Cloning and Assigning
  @Override
  public DocumentSubject clone()
  {
    final DocumentSubject cloned = (DocumentSubject) super.clone();
    cloned.mComment = mComment;
    cloned.mLocation = null;
    return cloned;
  }

  @Override
  public ModelChangeEvent assignMember(final int index,
                                       final Object oldValue,
                                       final Object newValue)
  {
    switch (index) {
    case 2:
      mComment = (String) newValue;
      return ModelChangeEvent.createStateChanged(this);
    case 3:
      mLocation = (URI) newValue;
      return ModelChangeEvent.createStateChanged(this);
    default:
      return null;
    }
  }

  @Override
  protected void collectUndoInfo(final ProxySubject newState,
                                 final RecursiveUndoInfo info,
                                 final Set<? extends Subject> boundary)
  {
    super.collectUndoInfo(newState, info, boundary);
    final DocumentSubject doc = (DocumentSubject) newState;
    if (!ProxyTools.equals(mComment, doc.getComment())) {
      final UndoInfo step =
        new ReplacementUndoInfo(2, mComment, doc.getComment());
      info.add(step);
    }
    if (!ProxyTools.equals(mLocation, doc.getLocation())) {
      final UndoInfo step =
        new ReplacementUndoInfo(3, mLocation, doc.getLocation());
      info.add(step);
    }
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
    if (!ProxyTools.equals(mComment, comment)) {
      mComment = comment;
      final ModelChangeEvent event = ModelChangeEvent.createStateChanged(this);
      event.fire();
    }
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
