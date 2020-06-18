//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2020 Knut Akesson, Martin Fabian, Robi Malik
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

package org.supremica.gui.examplegenerator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Project;
import org.supremica.gui.Gui;
import org.supremica.gui.Utility;
import org.supremica.gui.ide.DocumentContainerManager;
import org.supremica.gui.ide.IDE;


class TextArea extends JFrame {

	private static final long serialVersionUID = 1L;

	JTextArea _resultArea = new JTextArea(20, 70);
     public TextArea(final String text) {
        //... Set textarea's initial text, scrolling, and border.
        _resultArea.setText(text);
         _resultArea.setEditable(false);
        final JScrollPane scrollingArea = new JScrollPane(_resultArea);

        //... Get the content pane, set layout, add to center
        final JPanel content = new JPanel();
        content.setLayout(new BorderLayout());
        content.add(scrollingArea, BorderLayout.CENTER);


        //... Set window characteristics.
        this.setContentPane(content);
        this.setTitle("Results");
        this.pack();
    }

     public void setText(final String text)
     {
        _resultArea.setText(text);
     }
 }

// should perform integer validation - see Horstmann
class IntegerField
    extends JTextField
{
    private static final long serialVersionUID = 1L;

    public IntegerField(final String init, final int cols)
    {
        super(init, cols);
    }

    int get()
    {
        return Integer.parseInt(getText());
    }
}

class DoubleField
    extends JTextField
{
    private static final long serialVersionUID = 1L;

    public DoubleField(final String init, final int cols)
    {
        super(init, cols);
    }

    double get()
    {
        return Double.parseDouble(getText());
    }
}

class ExampleTab
    extends JTabbedPane
{
    private static final long serialVersionUID = 1L;

    ExampleTab()
    {
        addTab("Users", null, new UsersPanel(), "Mutual exclusion users");
        addTab("Philos", null, new PhilosPanel(), "Dininig philosophers");
        addTab("ExtPhilos", null, new ExtPhilosPanel(), "Extended Dininig philosophers");
        addTab("CatMouse", null, new CatMousePanel(), "Cat & Muuse");
        addTab("ExtCatMouse", null, new ExtCatMousePanel(), "Extended Cat & Mouse");
        addTab("Bricks", null, new BricksPanel(), "n-by-m bricks game");
        addTab("Sticks game", null, new StickGamePanel(), "Stick picking game");
        addTab("Transfer line", null, new TransferLinePanel(), "Transfer line");
        addTab("Counters", null, new CountersPanel(), "Independent Counters");
        addTab("Random automata", null, new RandomPanel(), "Random automata");
        addTab("Pigeon-Hole", null, new PigeonHolePanel(), "Pigeon-Hole");
        addTab("Sanchez-BM", null, new SanchezPanel(), "Sanchez-BM");
        addTab("Warehouse", null, new WarehousePanel(), "Warehouse");
        addTab("Round robin", null, new RoundRobinPanel(), "Round robin access");
        addTab("Arbiter", null, new ArbiterPanel(), "Arbiter tree");

        //addTab("Allocation Batch", null, new AllocationBatchPanel(), "Serialized Allocation Batch");
    }
}

public class TestCasesDialog
    extends JDialog
{
    private static final long serialVersionUID = 1L;
    private static Logger logger = LogManager.getLogger(TestCasesDialog.class);
    private final ExampleTab extab = new ExampleTab();
    private Project project = null;
    private final Object gui;

    public class ComputeButton
        extends JButton
    {
        private static final long serialVersionUID = 1L;

        ComputeButton()
        {
            super("Synthesize");

            setToolTipText("Run the example for multiple instances");
            addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)

                // throws Exception // cannot do this - what the f**k!
                {
                    try
                    {
                        IDE ide=null;
                        if (gui instanceof IDE)
                            ide = (IDE) gui;
                        else
                            System.out.println("ide is null: (TestCasesDialog in ComputeButton)");
                        synthesizeSupervisor(ide);
                    }
                    catch (final Exception ex)
                    {
                        logger.error("Exception while constructing test case: " + ex);
                        logger.debug(ex.getStackTrace());

                        // what are we supposed to do?
                    }
                }
            });
        }
    }

    class DoitButton
        extends JButton
    {
        private static final long serialVersionUID = 1L;

        DoitButton()
        {
            super("Generate Automata");

            setToolTipText("Generate automata and close this dialog");
            addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)

                // throws Exception // cannot do this - what the f**k!
                {
                    try
                    {
                        generateAutomata();
                    }
                    catch (final Exception ex)
                    {
                        logger.error("Exception while constructing test case: " + ex);
                        logger.debug(ex.getStackTrace());

                        // what are we supposed to do?
                    }
                }
            });
        }
    }

    class CancelButton
        extends JButton
    {
        private static final long serialVersionUID = 1L;

        CancelButton()
        {
            super("Cancel");

            setToolTipText("Enough of this");
            addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    action();
                }
            });
        }

        void action()
        {
            dispose();
        }
    }

    class HelpButton
        extends JButton
    {
        private static final long serialVersionUID = 1L;

        HelpButton()
        {
            super("Help");

            setToolTipText("Want some help?");
            addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    action();
                }
            });
        }

        void action()
        {}
    }

    void synthesizeSupervisor(final IDE ide)
    throws Exception
    {
        final Component comp = extab.getSelectedComponent();

        // We know that this is actually also a TestCase (right?)
        final TestCase tc = (TestCase) comp;

        setVisible(false);

        tc.synthesizeSupervisor(ide);
    }


    void generateAutomata()
    throws Exception
    {
        final Component comp = extab.getSelectedComponent();

        // We know that this is actually also a TestCase (right?)
        final TestCase tc = (TestCase) comp;

        setVisible(false);

        project = tc.generateAutomata();    // Should return a Project (named)

//        System.out.println("Name of project: "+project);

        if (gui instanceof Gui)
        {
            ((Gui) gui).addProject(project);
        }
        else if (gui instanceof IDE)
        {
            final IDE ide = (IDE) gui;
            final Project project = getProject();
            final DocumentContainerManager manager =
                ide.getDocumentContainerManager();
	    manager.newContainer(project);
        }
        dispose();
    }

    public Project getProject()
    {
        return project;
    }

    public TestCasesDialog(final Frame frame, final Object gui)
    {
        super(frame, "Example Generator", false);    // modal dialog with frame as parent

        this.gui = gui;

        final Container pane = getContentPane();

        pane.setLayout(new BorderLayout(10, 10));

        // Utility.setupFrame(this, 400, 200);
        // Dimension size = new Dimension(400, 200);
        // Point point = Utility.getPosForCenter(size);
        // setSize(size);
        // setLocation(point);
        final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton tmp;

//        ComputeButton cb = new ComputeButton();
        buttons.add(new ComputeButton());
        buttons.add(tmp = new DoitButton());
        buttons.add(new CancelButton());
        buttons.add(new HelpButton());
        pane.add(extab, BorderLayout.CENTER);
        pane.add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(tmp);    // :)
        pack();

        final Point point = Utility.getPosForCenter(getSize());

        setLocation(point);
    }
}
