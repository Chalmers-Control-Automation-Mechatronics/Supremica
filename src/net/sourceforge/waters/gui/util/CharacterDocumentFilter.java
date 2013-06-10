//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters GUI
//# PACKAGE: net.sourceforge.waters.gui.util
//# CLASS:   CharacterDocumentFilter
//###########################################################################
//# $Id$
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
      final StringBuffer buffer = new StringBuffer(len);
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