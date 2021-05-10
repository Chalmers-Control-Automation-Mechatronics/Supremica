//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2021 Knut Akesson, Martin Fabian, Robi Malik
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

package org.supremica.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import net.sourceforge.waters.gui.util.IconAndFontLoader;

import org.supremica.automata.Project;

public class ActionAndControlViewer
	extends JFrame
{
	private static final long serialVersionUID = 1L;
	private final JPanel contentPane;
	private final JMenuBar menuBar = new JMenuBar();
	private final ActionAndControlViewerPanel thePanel;
	private final Project theProject;

	public ActionAndControlViewer(final Project theProject)
		throws Exception
	{
		this.theProject = theProject;
		thePanel = new ActionAndControlViewerPanel(theProject);
		contentPane = (JPanel) getContentPane();

		// contentPane.setLayout(new BorderLayout());
		// contentPane.add(toolBar, BorderLayout.NORTH);
		setTitle("Execution parameters: " + theProject.getName());
		setSize(200, 500);
        addWindowListener(new WindowAdapter() {
          @Override
          public void windowClosing(final WindowEvent e)
          {
            setVisible(false);
            //dispose();
          }
        });

		// Center the window
		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		final Dimension frameSize = getSize();

		if (frameSize.height > screenSize.height)
		{
			frameSize.height = screenSize.height;
		}

		if (frameSize.width > screenSize.width)
		{
			frameSize.width = screenSize.width;
		}

		setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
        final List<Image> images = IconAndFontLoader.ICONLIST_APPLICATION;
        setIconImages(images);
		initMenubar();
		contentPane.add(thePanel, BorderLayout.CENTER);
	}

	private void initMenubar()
	{
		setJMenuBar(menuBar);

		// File
		final JMenu menuFile = new JMenu();

		menuFile.setText("File");
		menuFile.setMnemonic(KeyEvent.VK_F);

		// File.Close
		final JMenuItem menuFileClose = new JMenuItem();

		menuFileClose.setText("Close");
		menuFile.add(menuFileClose);
		menuBar.add(menuFile);
		menuFileClose.addActionListener(new ActionListener()
		{
			@Override
      public void actionPerformed(final ActionEvent e)
			{
				setVisible(false);

				//dispose();
			}
		});

		// Edit
		final JMenu menuEdit = new JMenu();

		menuEdit.setText("Edit");
		menuEdit.setMnemonic(KeyEvent.VK_E);

		// Edit.ClearActions
		final JMenuItem menuEditClearActions = new JMenuItem();

		menuEditClearActions.setText("Clear execution parameters");
		menuEdit.add(menuEditClearActions);
		menuBar.add(menuEdit);
		menuEdit.addActionListener(new ActionListener()
		{
			@Override
      public void actionPerformed(final ActionEvent e)
			{
				if (theProject != null)
				{
					theProject.clearExecutionParameters();
				}
			}
		});
	}

	public void initialize() {}
}
