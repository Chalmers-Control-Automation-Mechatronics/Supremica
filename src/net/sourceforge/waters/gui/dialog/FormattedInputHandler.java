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

package net.sourceforge.waters.gui.dialog;

import java.text.ParseException;

import javax.swing.text.DocumentFilter;


/**
 * <P>An interface that determines how objects of a type <CODE>T</CODE> can be
 * converted to and from text.</P>
 *
 * <P>The formatted input handler is used to configure a {@link
 * ValidatingTextCell} to facilitate the input of structured objects
 * as text.</P>
 *
 * @author Robi Malik
 */

public interface FormattedInputHandler<T>
{
  /**
   * Formats the given object as a string.
   * @param   value  The object to be formatted, which should be of type T.
   *                 The argument may be <CODE>null</CODE> for text fields
   *                 that support the <CODE>null</CODE> value, which typically
   *                 is formatted as an empty string.
   * @return  A string representation of the object.
   */
  public String format(Object value);

  /**
   * Tries to parse the given text into an object.
   * @param  text    The text to be parsed. An empty string may be provided
   *                 to request a <CODE>null</CODE> object for text fields
   *                 that support it.
   * @return An object corresponding to the textual input.
   * @throws ParseException to indicate that the text does not represent
   *                 a valid object of the handler's type.
   */
  public T parse(final String text) throws ParseException;

  /**
   * Gets a document filter to restrict the characters entered
   * into a text field.
   */
  public DocumentFilter getDocumentFilter();
}
