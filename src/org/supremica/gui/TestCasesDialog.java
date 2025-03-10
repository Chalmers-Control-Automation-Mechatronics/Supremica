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

package org.supremica.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Automaton;
import org.supremica.automata.Project;
import org.supremica.automata.algorithms.AutomataSynthesizer;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.automata.algorithms.SynthesisAlgorithm;
import org.supremica.automata.algorithms.SynthesisType;
import org.supremica.automata.algorithms.SynthesizerOptions;
import org.supremica.gui.ide.DocumentContainerManager;
import org.supremica.gui.ide.IDE;
import org.supremica.testcases.AllocationBatch;
import org.supremica.testcases.Arbiter;
import org.supremica.testcases.BricksGame;
import org.supremica.testcases.CatMouse;
import org.supremica.testcases.Counters;
import org.supremica.testcases.DiningPhilosophers;
import org.supremica.testcases.ExtCatMouse;
import org.supremica.testcases.ExtDiningPhilosophers;
import org.supremica.testcases.OperationBasedSystems;
import org.supremica.testcases.PigeonHole;
import org.supremica.testcases.RandomAutomata;
import org.supremica.testcases.RoundRobin;
import org.supremica.testcases.SanchezTestCase;
import org.supremica.testcases.StickPickingGame;
import org.supremica.testcases.TransferLine;
import org.supremica.testcases.Users;
import org.supremica.testcases.warehouse.SelectEventsWindow;
import org.supremica.testcases.warehouse.Warehouse;
import org.supremica.util.SupremicaException;


class Util
{
     public Point[] zigzagTraversing(final Point[] result, final int i, int x, int y, int di, int dj, final int min_x, final int min_y, final int max_x, final int max_y, final boolean firstTime)
    {
        result[i] = new Point(x,y);

        if(firstTime && y+dj > max_y)
        {
            di=1;
            dj=0;
        }

        x += di;
        y += dj;

        if(x+1 > max_x && y+1 > max_y)
        {
            result[i+1] = new Point(x,y);
            return result;
        }

        if(x-1 < min_x)
        {
            if(di == 0)
            {
                di=1;
                dj=-1;
            }
            else
            {
                di=0;
                dj=1;

                if(y == max_y)
                {
                    di=1;
                    dj=0;
                }
            }
        }
        else if(y+1 > max_y)
        {
            if(dj == 0)
            {
                di=1;
                dj=-1;
            }
            else
            {
                di=1;
                dj=0;
            }
        }

        if(y-1 < min_y)
        {
            if(dj == 0)
            {
                di=-1;
                dj=1;
            }
            else
            {
                di=1;
                dj=0;

                if(x == max_x)
                {
                    di=0;
                    dj=1;
                }
            }

        }
        else if(x+1 > max_x)
        {
            if(di == 0)
            {
                di=-1;
                dj=1;
            }
            else
            {
                di=0;
                dj=1;
            }
        }

       zigzagTraversing(result,i+1,x,y,di,dj,min_x,min_y,max_x,max_y, false);

	return null;
    }

    public Point[] verticalTraversing(final Point[] result, final int i, int x, int y, final int min_x, final int min_y, final int max_x, final int max_y)
    {
        result[i] = new Point(x,y);

        y++;

        if(y > max_y)
        {
            x++;
            y = min_y;
            if(x > max_x)
            {
                return result;
            }
            else
                verticalTraversing(result, i+1, x, y, min_x, min_y, max_x, max_y);
        }
        else
            verticalTraversing(result, i+1, x, y, min_x, min_y, max_x, max_y);

        return null;
    }

    public void writeToFile(final BufferedWriter bw, final String text, final boolean tokenizable) throws Exception
    {
        if(tokenizable)
        {
            final StringTokenizer st = new StringTokenizer(text,"\n");
            String token;
            while(st.hasMoreTokens())
            {
                token = st.nextToken();
                bw.newLine();
                try { bw.write(token); }
                catch (final IOException e) {}
            }

        }
        else
        {
            try { bw.write(text); }
            catch (final IOException e) {}
        }
    }
}

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

interface TestCase
{
    Project doIt()
    throws Exception;

    void compute(IDE ide)
    throws Exception;
}

class UsersPanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;
    IntegerField int_num = null;
    IntegerField int_rsc = null;
    JCheckBox req = new JCheckBox("request (a)");
    JCheckBox acc = new JCheckBox("access  (b)", true);
    JCheckBox rel = new JCheckBox("release (c)");

    public UsersPanel()
    {
        super(new GridLayout(2, 1, 10, 10));

        final JPanel cont = new JPanel();

        cont.setBorder(BorderFactory.createTitledBorder("Controllability"));
        cont.add(req);
        cont.add(acc);
        cont.add(rel);

        final JPanel num_users = new JPanel();

        num_users.add(new JLabel("Number of resources: "));
        num_users.add(int_rsc = new IntegerField("1", 6));
        num_users.add(new JLabel("Number of users: "));
        num_users.add(int_num = new IntegerField("3", 6));
        add(BorderLayout.NORTH, cont);
        add(BorderLayout.SOUTH, num_users);
    }

    @Override
    public void compute(final IDE ide){}

    @Override
    public Project doIt()
    throws Exception
    {
        final Users users = new Users(int_num.get(), int_rsc.get(), req.isSelected(), acc.isSelected(), rel.isSelected());

        return users.getProject();
    }
}

class OperationPanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;

    public OperationPanel()
    {
        super();//(new GridLayout(2, 1, 10, 10));
    }

    @Override
    public void compute(final IDE ide){}

    @Override
    public Project doIt()
    throws Exception
    {
        final OperationBasedSystems obs = new OperationBasedSystems();

        return obs.getProject();
    }
}

class PhilosPanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;
    IntegerField int_num = new IntegerField("5", 6);
    JCheckBox l_take = new JCheckBox("take left fork", true);
    JCheckBox r_take = new JCheckBox("take right fork", true);
    JCheckBox l_put = new JCheckBox("put left fork", true);
    JCheckBox r_put = new JCheckBox("put right fork", true);
    JCheckBox animation = new JCheckBox("Include animation (5 philos)", false);
    JCheckBox memory = new JCheckBox("Forks have memory", false);
    JCheckBox multiple = new JCheckBox("Multiple instances", false);
        Util util = new Util();

    public PhilosPanel()
    {
        // super(new GridLayout(2, 1, 10, 10));
        super();

        final JPanel cont = new JPanel();
        //cont.setLayout(new BoxLayout());
        cont.setBorder(BorderFactory.createTitledBorder("Controllability"));
        cont.add(l_take);
        cont.add(r_take);
        cont.add(l_put);
        cont.add(r_put);

        final JPanel num_users = new JPanel();
        num_users.add(new JLabel("Number of philosophers and forks: "), BorderLayout.NORTH);
        num_users.add(int_num, BorderLayout.NORTH);

        final JPanel animationPanel = new JPanel();
        animationPanel.add(animation);
        animationPanel.add(memory);
        animationPanel.add(multiple);

        final Box theBox = Box.createVerticalBox();
        theBox.add(cont);
        theBox.add(num_users);
        theBox.add(animationPanel);
        add(theBox, BorderLayout.NORTH);
    }

    @Override
    public void compute(final IDE ide){}

    @Override
    public Project doIt()
    throws Exception
    {
        final DiningPhilosophers dp = new DiningPhilosophers(int_num.get(), l_take.isSelected(), r_take.isSelected(), l_put.isSelected(), r_put.isSelected(), animation.isSelected(), memory.isSelected());

        /*
        Iterator<LabeledEvent> uit;
        for(int i=0;i<dp.getProject().nbrOfAutomata();i++)
        {
            System.out.println("i: "+i);
            uit = dp.getProject().getAutomatonAt(i).getAlphabet().getUncontrollableAlphabet().iterator();
            while(uit.hasNext())
                System.out.println(""+uit.next());
        }
*/

        return dp.getProject();
    }
}

class ExtPhilosPanel
    extends JPanel
    implements TestCase, ActionListener
{
    private static final long serialVersionUID = 1L;
    IntegerField int_num = new IntegerField("5", 6);
    IntegerField int_interm_num = new IntegerField("2", 6);
    IntegerField int_step_phils = new IntegerField("0", 6);
    IntegerField int_step_intermStates = new IntegerField("0", 6);
    IntegerField int_numberOfInstances = new IntegerField("1", 6);
    JCheckBox i_l_take = new JCheckBox("The uncontrollable events are 'philosopher i takes the left fork' for i even");
    JCheckBox l_take = new JCheckBox("take left fork", true);
    JCheckBox r_take = new JCheckBox("take right fork", true);
    JCheckBox l_put = new JCheckBox("put left fork", true);
    JCheckBox r_put = new JCheckBox("put right fork", true);
    JCheckBox animation = new JCheckBox("Include animation (5 philos)", false);
    JCheckBox memory = new JCheckBox("Forks have memory", false);
    JCheckBox multiple = new JCheckBox("Multiple instances", false);
    JCheckBox synth_algorithm = new JCheckBox("Choose synthesis algorithm manually (deafult is BDD)", false);

    IntegerField int_N = new IntegerField("0", 6);
    IntegerField int_K = new IntegerField("0", 6);
    JCheckBox all_cases = new JCheckBox("Compute all cases for n in interval (5,N) and k in interval (3,K)",false);

    JPanel traversing_algorithms;
    JRadioButton zigzagButton = new JRadioButton("Zigzag traversing");
    JRadioButton verticalButton = new JRadioButton("Vertical traversing");


    Util util = new Util();

    public ExtPhilosPanel()
    {
        // super(new GridLayout(2, 1, 10, 10));
        super();

        final JPanel cont = new JPanel();
        cont.setBorder(BorderFactory.createTitledBorder("Controllability"));
        cont.setLayout(new GridLayout(2, 1));
        final JPanel ext_control = new JPanel();
        ext_control.add(i_l_take,BorderLayout.NORTH);

        i_l_take.setSelected(true);
        i_l_take.setEnabled(true);
        l_take.setEnabled(false);
        r_take.setEnabled(false);
        l_put.setEnabled(false);
        r_put.setEnabled(false);

        final JPanel normal_control = new JPanel();
        normal_control.add(l_take,BorderLayout.SOUTH);
        normal_control.add(r_take,BorderLayout.SOUTH);
        normal_control.add(l_put,BorderLayout.SOUTH);
        normal_control.add(r_put,BorderLayout.SOUTH);
        cont.add(ext_control);
        cont.add(normal_control);

        final JPanel num_users = new JPanel();
        num_users.add(new JLabel("Number of philosophers and forks: "), BorderLayout.NORTH);
        num_users.add(int_num, BorderLayout.NORTH);

        final JPanel num_intermStates = new JPanel();
        num_intermStates.add(new JLabel("Number of intermediate states: "), BorderLayout.NORTH);
        num_intermStates.add(int_interm_num, BorderLayout.NORTH);

        final JPanel animationPanel = new JPanel();
        animationPanel.add(animation);
        animationPanel.add(memory);
        animationPanel.add(multiple);
        animationPanel.add(synth_algorithm);

        multiple.addActionListener(this);
        i_l_take.addActionListener(this);

        final JPanel numberOfInstances = new JPanel();
        numberOfInstances.add(new JLabel("Number of instances: "), BorderLayout.NORTH);
        numberOfInstances.add(int_numberOfInstances, BorderLayout.SOUTH);
        int_numberOfInstances.setEnabled(false);

        final JPanel step_phils = new JPanel();
        step_phils.add(new JLabel("step_P (increasement of number of phils for each instance): "), BorderLayout.NORTH);
        step_phils.add(int_step_phils, BorderLayout.SOUTH);
        int_step_phils.setEnabled(false);

        final JPanel step_intermStates = new JPanel();
        step_intermStates.add(new JLabel("step_S (increasement of number of intermediate states for each instance): "), BorderLayout.NORTH);
        step_intermStates.add(int_step_intermStates, BorderLayout.SOUTH);
        int_step_intermStates.setEnabled(false);

        all_cases.addActionListener(this);

        final JPanel NK = new JPanel();
        NK.add(new JLabel("N: "), BorderLayout.SOUTH);
        NK.add(int_N, BorderLayout.SOUTH);
        NK.add(new JLabel("K: "), BorderLayout.SOUTH);
        NK.add(int_K, BorderLayout.SOUTH);
        int_N.setEnabled(false);
        int_K.setEnabled(false);

        zigzagButton.setSelected(false);
        verticalButton.setSelected(true);

        traversing_algorithms = new JPanel();

        final Border border = BorderFactory.createTitledBorder("Traversing order of the test cases for the N*K plane");
        traversing_algorithms.setBorder(border);
        final ButtonGroup group = new ButtonGroup();
        group.add(zigzagButton);
        group.add(verticalButton);

        traversing_algorithms.add(zigzagButton);
        traversing_algorithms.add(verticalButton);

        zigzagButton.setEnabled(false);
        verticalButton.setEnabled(false);

        final Box theBox = Box.createVerticalBox();
        theBox.add(cont);
        theBox.add(num_users);
        theBox.add(num_intermStates);
        theBox.add(animationPanel);
        theBox.add(step_phils);
        theBox.add(step_intermStates);
        theBox.add(numberOfInstances);
        theBox.add(all_cases);
        theBox.add(NK);
        theBox.add(traversing_algorithms);
        add(theBox, BorderLayout.NORTH);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        if (multiple.isSelected()) {
            int_step_phils.setEnabled(true);
            int_step_intermStates.setEnabled(true);
            int_numberOfInstances.setEnabled(true);
        } else {
            int_step_phils.setEnabled(false);
            int_step_intermStates.setEnabled(false);
            int_numberOfInstances.setEnabled(false);
        }

        if (i_l_take.isSelected()) {
            l_take.setEnabled(false);
            r_take.setEnabled(false);
            l_put.setEnabled(false);
            r_put.setEnabled(false);
        } else {
            l_take.setEnabled(true);
            r_take.setEnabled(true);
            l_put.setEnabled(true);
            r_put.setEnabled(true);
        }

        if(all_cases.isSelected())
        {
            int_N.setEnabled(true);
            int_K.setEnabled(true);
            zigzagButton.setEnabled(true);
            verticalButton.setEnabled(true);

        }
        else
        {
            int_N.setEnabled(false);
            int_K.setEnabled(false);
            zigzagButton.setEnabled(false);
            verticalButton.setEnabled(false);
        }

    }

    @Override
    public Project doIt()
    throws Exception
    {
        final ExtDiningPhilosophers dp = new ExtDiningPhilosophers(i_l_take.isSelected(), int_num.get(), int_interm_num.get(), l_take.isSelected(), r_take.isSelected(), l_put.isSelected(), r_put.isSelected(), animation.isSelected(), memory.isSelected());

        return dp.getProject();
    }

    @Override
    public void compute (final IDE ide)
    throws Exception
    {
        int number_of_phils = int_num.get();
        int number_of_interm_states = int_interm_num.get();
        final int number_of_instances = int_numberOfInstances.get();

        final TextArea result = new TextArea("");

        String result_text = "n \t k \t t \t m \t s \n \n";

        BufferedWriter[] out_back;

        final SynthesizerOptions synthesizerOptions = new SynthesizerOptions();
        if(synth_algorithm.isSelected())
        {
            final SynthesizerDialog synthesizerDialog = new SynthesizerDialog(ide.getFrame(), 2*number_of_phils, synthesizerOptions);
            synthesizerDialog.setVisible(true);
        } else {
            synthesizerOptions.setSynthesisType
              (SynthesisType.NONBLOCKING_CONTROLLABLE);
            synthesizerOptions.setSynthesisAlgorithm
              (SynthesisAlgorithm.MONOLITHIC_WATERS);
            synthesizerOptions.setPurge(true);
            synthesizerOptions.setMaximallyPermissive(true);
            synthesizerOptions.setMaximallyPermissiveIncremental(true);
        }

        AutomataSynthesizer synthesizer;

        ExtDiningPhilosophers dp;
        AutomataSynthesisWorker asw;

        int finalNbrOfInstances;
        Point[] p = null;
        if(all_cases.isSelected())
        {
            finalNbrOfInstances = (int_N.get()-int_num.get()+1) * (int_K.get()-int_interm_num.get()+1);
            p = new Point[finalNbrOfInstances];
            if(zigzagButton.isSelected())
                util.zigzagTraversing(p,0,int_num.get(),int_interm_num.get(),0,1,int_num.get(),int_interm_num.get(),int_N.get(),int_K.get(),true);

            if(verticalButton.isSelected())
                util.verticalTraversing(p,0,int_num.get(),int_interm_num.get(),int_num.get(),int_interm_num.get(),int_N.get(),int_K.get());
        }
        else
            finalNbrOfInstances = number_of_instances;

        out_back = new BufferedWriter[finalNbrOfInstances];

        for(int i=0;i<finalNbrOfInstances;i++)
        {
            result_text += "\n";

            try { out_back[i] = new BufferedWriter(new FileWriter("Results/DP/results_diningPhil"+i+".txt"));}
            catch (final IOException e) {}

            if(all_cases.isSelected())
            {
                number_of_phils = p[i].x;
                number_of_interm_states = p[i].y;
            }
            else
            {
                number_of_phils = i*int_step_phils.get() + int_num.get();
                number_of_interm_states = i*int_step_intermStates.get()+ int_interm_num.get();
            }

            System.err.println("number of philosophers: "+number_of_phils);
            System.err.println("number of intermediate states: "+number_of_interm_states);
            System.err.println("computing supervisor... ");

            result_text += ""+number_of_phils+"\t";

            result_text += " "+number_of_interm_states+"\t";

            dp = new ExtDiningPhilosophers(i_l_take.isSelected(), number_of_phils, number_of_interm_states, l_take.isSelected(), r_take.isSelected(), l_put.isSelected(), r_put.isSelected(), animation.isSelected(), memory.isSelected());

            if(synth_algorithm.isSelected())
            {
                asw = new AutomataSynthesisWorker(null, dp.getAutomata(), synthesizerOptions);
                asw.join();

                result_text += " "+asw.getTimeSeconds()+"\t";
                result_text += " \t ";

                final Automaton supervisor = asw.getSupervisor();
                result_text +=" "+supervisor.nbrOfStates();
            }
            else
            {
              synthesizer =
                new AutomataSynthesizer(dp.getAutomata(),
                                        SynchronizationOptions.getDefaultSynthesisOptions(),
                                        synthesizerOptions);
              final Automaton supervisor =
                synthesizer.execute().getFirstAutomaton();
              final BigDecimal time = synthesizer.getTimeSeconds();
              result_text += " " + time + "\t";
              result_text += " \t";
              final int nbrOfStates = supervisor.getStateSet().size();
              result_text += " " + nbrOfStates;
            }

            System.err.println("Finished. ");
            System.err.println("--------------------------------");

            result_text += "\t";

            util.writeToFile(out_back[i],result_text,true);

            try { out_back[i].close(); }
            catch (final IOException e) {}

            System.gc();
        }
        result_text += "\n \n------------------------------------------------------------------ \n";
        result_text += "\n n: Number of philosophers \n k: Number of intermediate states for each philosopher \n t: Computation time in seconds \n m: The memory used in Mbytes \n s: Number of states for the supervisor \n d: Number of nodes for BDD";
        result.setText(result_text);
        result.setVisible(true);
    }
}

class CatMousePanel
    extends JPanel
    implements TestCase, ActionListener
{
    private static final long serialVersionUID = 1L;
    IntegerField int_num = new IntegerField("1", 6);
    IntegerField int_step_cats = new IntegerField("0", 6);
    IntegerField int_numberOfInstances = new IntegerField("1", 6);
    JCheckBox multiple = new JCheckBox("Multiple instances", false);
    JPanel num_users;
    JPanel steps;
    Box theBox;
    JPanel numberOfInstances;
    Util util = new Util();

    public CatMousePanel()
    {
        // super(new GridLayout(2, 1, 10, 10));
        super();

        num_users = new JPanel();
        num_users.add(new JLabel("Number of cats (or mice): "), BorderLayout.NORTH);
        num_users.add(int_num, BorderLayout.NORTH);

        final JPanel multiplePanel = new JPanel();
        multiplePanel.add(multiple);

        multiple.addActionListener(this);

        steps = new JPanel();
        steps.add(new JLabel("step (increasement of number of cats (or mice) for each instance): "), BorderLayout.NORTH);
        steps.add(int_step_cats, BorderLayout.SOUTH);
        int_step_cats.setEnabled(false);

        numberOfInstances = new JPanel();
        numberOfInstances.add(new JLabel("Number of instances: "), BorderLayout.NORTH);
        numberOfInstances.add(int_numberOfInstances, BorderLayout.SOUTH);
        int_numberOfInstances.setEnabled(false);

        theBox = Box.createVerticalBox();
        theBox.add(num_users);
        theBox.add(multiplePanel);
        theBox.add(steps);
        theBox.add(numberOfInstances);
        add(theBox, BorderLayout.NORTH);
    }

    @Override
    public void compute(final IDE ide)throws Exception{}

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        if (multiple.isSelected()) {
            int_step_cats.setEnabled(true);
            int_numberOfInstances.setEnabled(true);
        }
        else
        {
            int_step_cats.setEnabled(false);
            int_numberOfInstances.setEnabled(false);
        }
    }

    @Override
    public Project doIt()
    throws Exception
    {
        final CatMouse cm = new CatMouse(int_num.get());

        return cm.getProject();
    }

}

class ExtCatMousePanel
    extends CatMousePanel
    implements TestCase,ActionListener
{
    private static final long serialVersionUID = 1L;
    IntegerField int_num_levels = new IntegerField("1", 6);
    IntegerField int_step_levels = new IntegerField("0", 6);
    IntegerField int_N = new IntegerField("0", 6);
    IntegerField int_K = new IntegerField("0", 6);
    JCheckBox synth_algorithm = new JCheckBox("Choose synthesis algorithm manually (deafult is BDD)", false);
    JCheckBox all_cases = new JCheckBox("Compute all cases for n in interval (1,N) and k in interval (1,K)",false);

    JPanel traversing_algorithms;
    JRadioButton zigzagButton = new JRadioButton("Zigzag traversing");
    JRadioButton verticalButton = new JRadioButton("Vertical traversing");


    public ExtCatMousePanel()
    {
        // super(new GridLayout(2, 1, 10, 10));
        super();
        num_users.add(new JLabel("     Number of levels: "), BorderLayout.NORTH);
        num_users.add(int_num_levels, BorderLayout.NORTH);

        final JPanel stepsL = new JPanel();
        stepsL.add(new JLabel("stepL (increasement of number levels for each instance): "), BorderLayout.SOUTH);
        stepsL.add(int_step_levels, BorderLayout.SOUTH);
        int_step_levels.setEnabled(false);

        all_cases.addActionListener(this);

        final JPanel NK = new JPanel();
        NK.add(new JLabel("N: "), BorderLayout.SOUTH);
        NK.add(int_N, BorderLayout.SOUTH);
        NK.add(new JLabel("K: "), BorderLayout.SOUTH);
        NK.add(int_K, BorderLayout.SOUTH);
        int_N.setEnabled(false);
        int_K.setEnabled(false);

        zigzagButton.setSelected(false);
        verticalButton.setSelected(true);

        traversing_algorithms = new JPanel();

        final Border border = BorderFactory.createTitledBorder("Traversing order of the test cases for the N*K plane");
        traversing_algorithms.setBorder(border);
        final ButtonGroup group = new ButtonGroup();
        group.add(zigzagButton);
        group.add(verticalButton);

        traversing_algorithms.add(zigzagButton);
        traversing_algorithms.add(verticalButton);

        zigzagButton.setEnabled(false);
        verticalButton.setEnabled(false);

        theBox.add(stepsL);
        theBox.add(numberOfInstances);
        theBox.add(synth_algorithm);
        theBox.add(all_cases);
        theBox.add(traversing_algorithms);
        theBox.add(NK);

    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        if (multiple.isSelected()) {
            int_step_cats.setEnabled(true);
            int_step_levels.setEnabled(true);
            int_numberOfInstances.setEnabled(true);
        }
        else
        {
            int_step_cats.setEnabled(false);
            int_step_levels.setEnabled(false);
            int_numberOfInstances.setEnabled(false);
        }

        if(all_cases.isSelected())
        {
            int_N.setEnabled(true);
            int_K.setEnabled(true);
            zigzagButton.setEnabled(true);
            verticalButton.setEnabled(true);
        }
        else
        {
            int_N.setEnabled(false);
            int_K.setEnabled(false);
            zigzagButton.setEnabled(false);
            verticalButton.setEnabled(false);
        }
    }

    //This function will be called when "Syntheisize" button is pressed
    @Override
    public void compute(final IDE ide) throws Exception
    {
        int number_of_cats = int_num.get();
        int number_of_levels = int_num_levels.get();
        final int number_of_instances = int_numberOfInstances.get();

        final TextArea result = new TextArea("");

        String result_text = "n \t k \t t \t m \t s \t d \n";

        BufferedWriter[] out_back;

        final SynthesizerOptions synthesizerOptions = new SynthesizerOptions();

        if(synth_algorithm.isSelected())
        {
            //Manually select the synthesis algorithm
            final SynthesizerDialog synthesizerDialog = new SynthesizerDialog(ide.getFrame(), 2*number_of_cats+5*number_of_levels, synthesizerOptions);
            synthesizerDialog.setVisible(true);
        }
        else
        {
            synthesizerOptions.setSynthesisType
              (SynthesisType.NONBLOCKING_CONTROLLABLE);
            synthesizerOptions.setSynthesisAlgorithm
              (SynthesisAlgorithm.MONOLITHIC_WATERS);
            synthesizerOptions.setPurge(true);
            synthesizerOptions.setMaximallyPermissive(true);
            synthesizerOptions.setMaximallyPermissiveIncremental(true);
        }

        AutomataSynthesizer synthesizer;

        ExtCatMouse ecm;
        AutomataSynthesisWorker asw;

        int finalNbrOfInstances;
        Point[] p = null;

        if(all_cases.isSelected())
        {
            //All instances for a N*K plane
            finalNbrOfInstances = int_N.get() * int_K.get();
            p = new Point[finalNbrOfInstances];
            if(zigzagButton.isSelected())
                util.zigzagTraversing(p,0,int_num.get(),int_num_levels.get(),0,1,int_num.get(),int_num_levels.get(),int_N.get(),int_K.get(),true);
//                            util.zigzagTraversing(p,0,int_num.get(),int_num_levels.get(),-1,1,1,1,int_N.get(),int_K.get(),true);

            if(verticalButton.isSelected())
                util.verticalTraversing(p,0,int_num.get(),int_num_levels.get(),int_num.get(),int_num_levels.get(),int_N.get(),int_K.get());
        }
        else
            finalNbrOfInstances = number_of_instances;

        out_back = new BufferedWriter[finalNbrOfInstances];

        for(int i=0;i<finalNbrOfInstances;i++)
        {
            result_text += "\n";

            try { out_back[i] = new BufferedWriter(new FileWriter("Results/CM/results_catmouse"+i+".txt"));}
            catch (final IOException e) {}

            if(all_cases.isSelected())
            {
                number_of_cats = p[i].y;
                number_of_levels = p[i].x;
            }
            else
            {
                number_of_cats = i*int_step_cats.get() + int_num.get();
                number_of_levels = i*int_step_levels.get()+ int_num_levels.get();
            }

            System.err.println("number of cats: "+number_of_cats);
            System.err.println("number of levels: "+number_of_levels);
            System.err.println("computing supervisor... ");

            result_text += ""+number_of_cats+"\t";

            result_text += " "+number_of_levels+"\t";

            //INSTANCE GENERATION
            ecm = new ExtCatMouse(number_of_cats, number_of_levels);
            //INSTANCE GENERATION

            if(synth_algorithm.isSelected())
            {
                asw = new AutomataSynthesisWorker(null, ecm.getAutomata(), synthesizerOptions);
                asw.join();

                result_text += " "+asw.getTimeSeconds()+"\t";
                result_text += " \t ";

                final Automaton supervisor = asw.getSupervisor();
                result_text +=" "+supervisor.nbrOfStates();
            }
            else
            {
                synthesizer =
                  new AutomataSynthesizer(ecm.getAutomata(),
                                          SynchronizationOptions.getDefaultSynthesisOptions(),
                                          synthesizerOptions);
                final Automaton supervisor =
                  synthesizer.execute().getFirstAutomaton();
                final BigDecimal time = synthesizer.getTimeSeconds();
                result_text += " "+time+"\t";
                result_text += " \t";
                final int nbrOfStates = supervisor.getStateSet().size();
                result_text +=" "+nbrOfStates;
            }

            System.err.println("Finished. ");
            System.err.println("--------------------------------");

            result_text += "\t";

            util.writeToFile(out_back[i],result_text,true);

            try { out_back[i].close(); }
            catch (final IOException e) {}

        }

        result_text += "\n \n------------------------------------------------------------------ \n";
        result_text += "\n n: Number of levels \n k: Number of cats and mice \n t: Computation time in seconds \n m: The memory used in Mbytes \n s: Number of states for the supervisor \n d: Number of nodes for BDD";
        result.setText(result_text);
        result.setVisible(true);
    }


    @Override
    public Project doIt()
    throws Exception
    {
        final ExtCatMouse cm = new ExtCatMouse(int_num.get(), int_num_levels.get());
        return cm.getProject();
    }

}

class BricksPanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;
    IntegerField num_rows = new IntegerField("4", 6);
    IntegerField num_cols = new IntegerField("4", 6);

    BricksPanel()
    {
        final JPanel panel = new JPanel(new GridLayout(2, 2));

        add(panel, BorderLayout.WEST);
        panel.add(new JLabel("Number of rows: "));
        panel.add(num_rows);
        panel.add(new JLabel("Number of cols: "));
        panel.add(num_cols);
    }

    @Override
    public void compute(final IDE ide){}

    @Override
    public Project doIt()
    throws Exception
    {
        final BricksGame bg = new BricksGame(num_rows.get(), num_cols.get());

        return bg.getProject();
    }
}

class WarehousePanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;
    Warehouse warehouse = new Warehouse();
    IntegerField nbr_events_k = new IntegerField("3", 6);
    IntegerField nbr_events_m = new IntegerField("1", 6);
    SelectEventsWindow selectOperatorEventsWindow = null;
    SelectEventsWindow selectUnobservableEventsWindow = null;

    WarehousePanel()
    {
        final JPanel panel = new JPanel(new GridLayout(3, 2));

        add(panel, BorderLayout.WEST);
        panel.add(new JLabel("Number of operator events (k): "));
        panel.add(nbr_events_k);
        panel.add(new JLabel("Number of supervisor events (m): "));
        panel.add(nbr_events_m);

        final JButton selectOperatorEventsButton = new JButton("Select operator events");

        selectOperatorEventsButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                if (selectOperatorEventsWindow == null)
                {
                    selectOperatorEventsWindow = new SelectEventsWindow(warehouse.getTruckAlphabet(), "Select operator events", "Select operator events", true);
                }

                selectOperatorEventsWindow.actionPerformed(e);

                // ActionMan.fileOpen(ActionMan.getGui());
            }
        });
        panel.add(selectOperatorEventsButton);

        //JButton selectControlEventsButton = new JButton("Select control events");
        //panel.add(selectControlEventsButton);
        final JButton selectUnobservableEventsButton = new JButton("Select unobservable events");

        selectUnobservableEventsButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                if (selectUnobservableEventsWindow == null)
                {
                    selectUnobservableEventsWindow = new SelectEventsWindow(warehouse.getTruckAlphabet(), "Select unobservable events", "Select unobservable events", false);
                }

                selectUnobservableEventsWindow.actionPerformed(e);

                // ActionMan.fileOpen(ActionMan.getGui());
            }
        });
        panel.add(selectUnobservableEventsButton);
    }

    @Override
    public void compute(final IDE ide){}

    @Override
    public Project doIt()
    throws Exception
    {
        warehouse.setK(nbr_events_k.get());
        warehouse.setM(nbr_events_m.get());

        //System.err.println("Warehouse doIt");
        return warehouse.getProject();
    }
}

class StickGamePanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;
    IntegerField num_players = new IntegerField("2", 6);
    IntegerField num_sticks = new IntegerField("7", 6);

    StickGamePanel()
    {
        final JPanel panel = new JPanel(new GridLayout(2, 2));

        add(panel, BorderLayout.WEST);
        panel.add(new JLabel("Number of players: "));
        panel.add(num_players);
        panel.add(new JLabel("Number of sticks: "));
        panel.add(num_sticks);
    }

    @Override
    public void compute(final IDE ide){}

    @Override
    public Project doIt()
    throws Exception
    {

        // System.err.println("SticksGamePanel::doIt()");
        final StickPickingGame spg = new StickPickingGame(num_players.get(), num_sticks.get());

        return spg.getProject();
    }
}

class AllocationBatchPanel
    extends JPanel
    implements TestCase, ActionListener
{
    private static final long serialVersionUID = 1L;
    JTextField filename;
    JButton browse;

    AllocationBatchPanel()
    {
        super(new BorderLayout(10, 10));

        final JPanel pCenter = new JPanel(new GridLayout(4, 2));

        add(pCenter, BorderLayout.WEST);
        pCenter.add(new JLabel("batch file:  "));
        pCenter.add(filename = new JTextField(20));
        pCenter.add(browse = new JButton("..."));
        browse.addActionListener(this);
        add(pCenter, BorderLayout.CENTER);
        add(new JLabel("Experimental serialized allocation batch"), BorderLayout.NORTH);
    }

    @Override
    public void compute(final IDE ide){}

    @Override
    public Project doIt()
    throws Exception
    {
        final String file = filename.getText();

        if (file.length() > 0)
        {
            final AllocationBatch ab = new AllocationBatch(file);

            return ab.getProject();
        }    // else...

        throw new SupremicaException("you must choose a filename");
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        final Object src = e.getSource();

        if (src == browse)
        {
            final JFileChooser chooser = new JFileChooser();

            chooser.setDialogTitle("Please choose a batch file");

            final int returnVal = chooser.showOpenDialog(this);

            if (returnVal == JFileChooser.APPROVE_OPTION)
            {
                filename.setText(chooser.getSelectedFile().getAbsolutePath());
            }
        }
    }
}

class CountersPanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;
    IntegerField int_num = null;
    IntegerField int_size = null;

    public CountersPanel()
    {
        final JPanel panel = new JPanel(new GridLayout(2, 2));

        add(panel, BorderLayout.CENTER);
        panel.add(new JLabel("Number of counters: "));
        panel.add(int_num = new IntegerField("3", 6));
        panel.add(new JLabel("Counter states: "));
        panel.add(int_size = new IntegerField("8", 6));
    }

    @Override
    public void compute(final IDE ide){}

    @Override
    public Project doIt()
    throws Exception
    {
        final Counters counters = new Counters(int_num.get(), int_size.get());

        return counters.getProject();
    }
}

class RandomPanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;
    IntegerField int_num = null;
    IntegerField int_size = null;
    IntegerField int_events = null;
    DoubleField dbl_dens = null;

    public RandomPanel()
    {
        final JPanel panel = new JPanel(new GridLayout(4, 2));

        add(panel, BorderLayout.WEST);
        panel.add(new JLabel("Number of automata: "));
        panel.add(int_num = new IntegerField("3", 6));
        panel.add(new JLabel("Number of states: "));
        panel.add(int_size = new IntegerField("5", 6));
        panel.add(new JLabel("Number of events: "));
        panel.add(int_events = new IntegerField("3", 3));
        panel.add(new JLabel("Deterministic transition-density: "));
        panel.add(dbl_dens = new DoubleField("0.75", 6));
    }

    @Override
    public void compute(final IDE ide){}

    @Override
    public Project doIt()
    throws Exception
    {
        final RandomAutomata ra = new RandomAutomata(int_num.get(), int_size.get(), int_events.get(), dbl_dens.get());

        return ra.getProject();
    }
}

class TransferLinePanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;
    IntegerField int_cap1 = null;
    IntegerField int_cap2 = null;
    IntegerField int_cells = null;
//<<<<<<< TestCasesDialog.java
//	IntegerField int_caps = null;	// Gromyko, Pistore, Traverno allow arbitrary size of all resources, inc machines
//
//=======
//
//>>>>>>> 1.47

    public TransferLinePanel()
    {
        final JPanel panel = new JPanel(new GridLayout(4, 2));

        add(panel, BorderLayout.CENTER);
        panel.add(new JLabel("Ref: 'Notes on Control of Discrete", SwingConstants.RIGHT));
        panel.add(new JLabel("-Event Systems', W.M. Wonham", SwingConstants.LEFT));
        panel.add(new JLabel("Number of cells: "));
        panel.add(int_cells = new IntegerField("3", 5));
        panel.add(new JLabel("Buffer 1 capacity: "));
        panel.add(int_cap1 = new IntegerField("3", 5));
        panel.add(new JLabel("Buffer 2 capacity: "));
        panel.add(int_cap2 = new IntegerField("1", 5));
    }

    @Override
    public void compute(final IDE ide){}

    @Override
    public Project doIt()
    throws Exception
    {
        final int cap1 = int_cap1.get();
        final int cap2 = int_cap2.get();

        if ((cap1 < 1) || (cap2 < 1))
        {
            throw new SupremicaException("Buffer capacity must be at least 1");
        }

        final TransferLine tl = new TransferLine(int_cells.get(), cap1, cap2, false);

        return tl.getProject();
    }
}

class PigeonHolePanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;
    IntegerField int_pigeons = null;
    IntegerField int_holes = null;

    public PigeonHolePanel()
    {
        final Box theBox = Box.createVerticalBox();

        add(theBox, BorderLayout.NORTH);

        final JPanel labelPanel = new JPanel();

        labelPanel.add(new JLabel("Ref: 'The Intractability of Resolution', Armin Haken."));

        final JPanel panel = new JPanel(new GridLayout(2, 2));

        panel.add(new JLabel("Number of pigeons: "));
        panel.add(int_pigeons = new IntegerField("5", 3));
        panel.add(new JLabel("Number of holes: "));
        panel.add(int_holes = new IntegerField("6", 3));
        theBox.add(labelPanel);
        theBox.add(panel);
    }

    @Override
    public void compute(final IDE ide){}

    @Override
    public Project doIt()
    throws Exception
    {
        final int p = int_pigeons.get();
        final int h = int_holes.get();

        if ((p < 1) || (h < 1))
        {
            throw new SupremicaException("Weird configuration...");
        }

        final PigeonHole ph = new PigeonHole(p, h);

        return ph.getProject();
    }
}

class SanchezPanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;
    IntegerField int_blocks = null;
    JComboBox<String> choice = null;
    static final String[] choice_items = { "#1: Async prod", "#2: Synch prod",
    "#3: SupC" };

    public SanchezPanel()
    {
        final JPanel panel = new JPanel(new GridLayout(3, 2));

        add(panel, BorderLayout.NORTH);
        panel.add(new JLabel("Ref: 'A Comparision of Synthesis", SwingConstants.RIGHT));
        panel.add(new JLabel(" Tools For...', A. Sanchez et. al.", SwingConstants.LEFT));
        panel.add(new JLabel("Number of blocks: "));
        panel.add(int_blocks = new IntegerField("5", 3));
        panel.add(new JLabel("Benchmark: "));
        panel.add(choice = new JComboBox<String>(choice_items));
    }

    @Override
    public void compute(final IDE ide){}

    @Override
    public Project doIt()
    throws Exception
    {
        final int p = int_blocks.get();
        final int type = choice.getSelectedIndex();
        final SanchezTestCase stc = new SanchezTestCase(p, type);

        return stc.getProject();
    }
}

class RoundRobinPanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;
    IntegerField num_proc = new IntegerField("4", 2);

    public RoundRobinPanel()
    {
        final Box theBox = Box.createVerticalBox();

        add(theBox, BorderLayout.NORTH);

        final JPanel labelPanel = new JPanel();

        labelPanel.add(new JLabel("Ref: 'Compositional Minimization of " + "Finite State Systems', S. Graf et. al."));

        final JPanel panel = new JPanel(new GridLayout(1, 2));

        panel.add(new JLabel("Number of processes: "));
        panel.add(num_proc);
        theBox.add(labelPanel);
        theBox.add(panel);
    }

    @Override
    public void compute(final IDE ide){}

    @Override
    public Project doIt()
    throws Exception
    {
        final RoundRobin rr = new RoundRobin(num_proc.get());

        return rr.getProject();
    }
}

class ArbiterPanel
    extends JPanel
    implements TestCase
{
    private static final long serialVersionUID = 1L;
    IntegerField num_users = new IntegerField("4", 2);
    JCheckBox synchronize = new JCheckBox("Synchronize arbiter cells (yields an appealing structure)", true);

    public ArbiterPanel()
    {
        final Box theBox = Box.createVerticalBox();
        add(theBox, BorderLayout.NORTH);

        final JPanel labelPanel = new JPanel();
        labelPanel.add(new JLabel("Ref: 'Compositional Model Checking', E.M. Clarke et. al."));

        final JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.add(new JLabel("Number of users: "));
        panel.add(num_users);

        final JPanel synchronizePanel = new JPanel();
        synchronizePanel.add(synchronize, BorderLayout.NORTH);
        theBox.add(labelPanel);
        theBox.add(panel);
        theBox.add(synchronizePanel);
    }

    @Override
    public void compute(final IDE ide){}

    @Override
    public Project doIt()
    throws Exception
    {
        // At least two users!!
        if (num_users.get() < 2)
        {
            throw new SupremicaException("The arbiter tree must have at least two users.");
        }

        //Arbiter arb = new Arbiter(users, synchronize.isSelected());
        final Arbiter arb = new Arbiter(num_users.get(), synchronize.isSelected());

        return arb.getProject();
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
        addTab("Operations", null, new OperationPanel(), "Operations");

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
                        compute(ide);
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
            super("Do it");

            setToolTipText("Go ahead and do it");
            addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)

                // throws Exception // cannot do this - what the f**k!
                {
                    try
                    {
                        doit();
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

    void compute(final IDE ide)
    throws Exception
    {
        final Component comp = extab.getSelectedComponent();

        // We know that this is actually also a TestCase (right?)
        final TestCase tc = (TestCase) comp;

        setVisible(false);

        tc.compute(ide);
    }


    void doit()
    throws Exception
    {
        final Component comp = extab.getSelectedComponent();

        // We know that this is actually also a TestCase (right?)
        final TestCase tc = (TestCase) comp;

        setVisible(false);

        project = tc.doIt();    // Should return a Project (named)

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
