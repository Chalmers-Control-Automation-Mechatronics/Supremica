//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2023 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui.texteditor;

import java.io.*;
import java.awt.*;
import javax.swing.*;

public class TextPanel
	extends JScrollPane
{
    private static final long serialVersionUID = 1L;

	private JTextArea textarea = null;

	public TextPanel()
	{
		this("");
	}

	public TextPanel(String str)
	{
		this.textarea = new JTextArea(str)
    {
        private void adjustFontSize(int adjust)
        {
          java.awt.Font font = getFont();
          int size = font.getSize();
          setFont(new Font(font.getName(), font.getStyle(), font.getSize() + adjust));
        }
        
        @Override
        protected void processKeyEvent(java.awt.event.KeyEvent event)
        {
          if(event.getID() == java.awt.event.KeyEvent.KEY_TYPED)
          {
/* On Windows (MF's machine at least) Ctrl+ and Ctrl- do not work as expected.
    Nor does Shift+ or Shift- work. Alt+ and Alt- behave as expected.
    These lines are there for testing...
              if(event.isControlDown())
                System.err.println("Ctrl is down");
              if(event.isShiftDown())
                System.err.println("Sfift is down");
              if(event.isAltDown())
                System.err.println("Alt is down");
*/// For now, plain + and - adjust the font size in the text panel
              if(event.getKeyChar() == '+')
              {
                adjustFontSize(1);
              }
              else if(event.getKeyChar() == '-')
              {
                adjustFontSize(-1);
              }
          }
          super.processKeyEvent(event);
        }
    };

		textarea.setFont(new Font("monospaced", Font.PLAIN, 12));
		textarea.setLineWrap(true);
		textarea.setWrapStyleWord(true);
		textarea.setTabSize(4);
		textarea.setEditable(false);
		setBorder(BorderFactory.createEtchedBorder());
		getViewport().add(textarea);
	}

	public void append(String str)
	{
		textarea.append(str);
	}

	public JTextArea getTextArea()
	{
		return textarea;
	}

	public static void main(String[] args)
	{
		TextFrame textframe = new TextFrame("Testing...");
		PrintWriter pw = textframe.getPrintWriter();

		pw.println("Hello World!");
	}
}
