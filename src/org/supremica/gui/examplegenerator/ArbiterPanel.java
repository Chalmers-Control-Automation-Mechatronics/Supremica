package org.supremica.gui.examplegenerator;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.supremica.automata.Project;
import org.supremica.gui.ide.IDE;
import org.supremica.testcases.Arbiter;
import org.supremica.util.SupremicaException;

class ArbiterPanel
extends JPanel
implements TestCase
{
private static final long serialVersionUID = 1L;
IntegerField num_users = new IntegerField("4", 2);
JCheckBox synchronize = new JCheckBox("Synchronize arbiter cells (yields an appealing structure)", true);

public ArbiterPanel()
{
    Box theBox = Box.createVerticalBox();
    add(theBox, BorderLayout.NORTH);

    JPanel labelPanel = new JPanel();
    labelPanel.add(new JLabel("Ref: 'Compositional Model Checking', E.M. Clarke et. al."));

    JPanel panel = new JPanel(new GridLayout(1, 2));
    panel.add(new JLabel("Number of users: "));
    panel.add(num_users);

    JPanel synchronizePanel = new JPanel();
    synchronizePanel.add(synchronize, BorderLayout.NORTH);
    theBox.add(labelPanel);
    theBox.add(panel);
    theBox.add(synchronizePanel);
}

public void synthesizeSupervisor(IDE ide){}

public Project generateAutomata()
throws Exception
{
    // At least two users!!
    if (num_users.get() < 2)
    {
        throw new SupremicaException("The arbiter tree must have at least two users.");
    }

    //Arbiter arb = new Arbiter(users, synchronize.isSelected());
    Arbiter arb = new Arbiter(num_users.get(), synchronize.isSelected());

    return arb.getProject();
}
}
