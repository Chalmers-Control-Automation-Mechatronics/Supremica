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

package net.sourceforge.waters.analysis.options;

import java.awt.Component;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;


public abstract class AbstractTextFieldParameter extends Parameter
{

  public AbstractTextFieldParameter(final int id, final String name, final String description)
  {
    super(id, name, description);
  }

  @Override
  public Component createComponent(final ProductDESContext model)
  {
    final JTextField textField = new JTextField();
    final PlainDocument doc = (PlainDocument) textField.getDocument();
    doc.setDocumentFilter(new InputFilter());
    textField.setColumns(10);
    return textField;
  }

  protected abstract boolean testAlphabet(final String text);

  private class InputFilter extends DocumentFilter {

    @Override
    public void replace(final FilterBypass fb,
                        final int offset,
                        final int length,
                        final String text,
                        final AttributeSet attrs)
      throws BadLocationException
    {
      // TODO Filter bad characters in text, insert all that are OK.
      // Even if some bad characters, insert those are OK.
      final Document doc = fb.getDocument();
      final StringBuilder sb = new StringBuilder();
      sb.append(doc.getText(0, doc.getLength()));
      sb.replace(offset, offset + length, text);
      if (testAlphabet(sb.toString())) {
        super.replace(fb, offset, length, text, attrs);
      }
    }
  }
}
