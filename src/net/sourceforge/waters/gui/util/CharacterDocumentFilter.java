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

package net.sourceforge.waters.gui.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * A {@link DocumentFilter} implementation that can be linked to
 * Swing text fields in order to restrict the set of possible
 * characters.
 *
 * @author Robi Malik
 */

public abstract class CharacterDocumentFilter
  extends DocumentFilter
{

  //#########################################################################
  //# To be Overridden
  protected abstract boolean isAllowedCharacter(char ch);


  //#########################################################################
  //# Overrides for class javax.swing.DocumentFilter
  @Override
  public void insertString(final DocumentFilter.FilterBypass bypass,
                           final int offset,
                           final String text,
                           final AttributeSet attribs)
    throws BadLocationException
  {
    final String filtered = filter(text);
    if (filtered != null) {
      super.insertString(bypass, offset, filtered, attribs);
    }
  }

  @Override
  public void replace(final DocumentFilter.FilterBypass bypass,
                      final int offset,
                      final int length,
                      final String text,
                      final AttributeSet attribs)
    throws BadLocationException
  {
    final String filtered = filter(text);
    if (filtered != null) {
      super.replace(bypass, offset, length, filtered, attribs);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private String filter(final String text)
  {
    if (text == null) {
      return null;
    } else {
      final int len = text.length();
      final StringBuilder buffer = new StringBuilder(len);
      for (int i = 0; i < len; i++) {
        final char ch = text.charAt(i);
        if (isAllowedCharacter(ch)) {
          buffer.append(ch);
        }
      }
      if (buffer.length() == 0) {
        return null;
      } else {
        return buffer.toString();
      }
    }
  }

}







