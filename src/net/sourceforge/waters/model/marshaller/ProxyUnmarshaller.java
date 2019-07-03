//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

package net.sourceforge.waters.model.marshaller;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import javax.swing.filechooser.FileFilter;

import net.sourceforge.waters.model.base.DocumentProxy;


/**
 * <P>
 * The basic unmarshaller interface.
 * </P>
 *
 * <P>
 * The ProxyUnmarshaller provides the basic means for the
 * {@link DocumentManager} to unmarshal, i.e., load documents from various file
 * formats.
 * </P>
 *
 * @author Robi Malik
 */

public interface ProxyUnmarshaller<D extends DocumentProxy>
{

  // #########################################################################
  // # Access Methods
  /**
   * Loads a document from a file.
   *
   * @param uri
   *          A URI specifying the location of the document to be retrieved.
   * @return The loaded document.
   * @throws WatersUnmarshalException
   *           to indicate that parsing the input file has failed for some
   *           reason.
   * @throws IOException
   *           to indicate that the input file could not be opened or read.
   */
  public D unmarshal(URI uri) throws WatersUnmarshalException, IOException;

  // #########################################################################
  // # Type Information
  /**
   * Gets the class of documents handled by this unmarshaller.
   */
  public Class<D> getDocumentClass();

  /**
   * Gets a default extension for the files read by this unmarshaller.
   */
  public String getDefaultExtension();

  /**
   * Gets a list of file name extensions for files that may be handled by this
   * marshaller.
   */
  public Collection<String> getSupportedExtensions();

  /**
   * Gets a list of file filters that may be handled by this marshaller.
   */
  public Collection<FileFilter> getSupportedFileFilters();

}
