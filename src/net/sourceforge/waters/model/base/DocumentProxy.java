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

package net.sourceforge.waters.model.base;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;

/**
 * <P>The interface characterising top-level objects in Waters.</P>
 *
 * <P>A <I>document</I> is an object that has its own file representation.
 * Every loaded document should have an associated file name
 * identifying the location from where it has been read, and where it
 * can be written to.</P>
 *
 * @author Robi Malik
 */
public interface DocumentProxy
  extends NamedProxy
{
  //#########################################################################
  //# Cloning
  public DocumentProxy clone();

  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the comment associated with this document.
   * The comment is a human-readable description of the document's contents
   * that may extend over several lines.
   */
  public String getComment();

  /**
   * Gets the URL associated with this document.
   */
  public URI getLocation();

  /**
   * Gets the file name associated with this document.
   * @return The file name used for reading and writing this document.
   * @throws MalformedURLException to indicate that this document is not
   *                            associated with a writable file.
   */
  public File getFileLocation() throws MalformedURLException;

  /**
   * Sets the file name associated with this document to a new value.
   */
  public void setLocation(URI location);
}
