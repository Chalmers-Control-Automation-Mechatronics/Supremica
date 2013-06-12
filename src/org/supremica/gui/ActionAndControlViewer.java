
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
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

import net.sourceforge.waters.gui.util.IconLoader;

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
        final List<Image> images = IconLoader.ICONLIST_APPLICATION;
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
