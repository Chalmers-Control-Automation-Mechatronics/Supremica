package org.supremica.util.BDD;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class PCGFrame
	extends Dialog
	implements ActionListener
{
	private static final long serialVersionUID = 1L;

	private PCGNode[] nodes;
	private int[] perm;
	private int size;
	private Button bUp, bDown, bDone, bDump;
	private java.awt.List order;

	public PCGFrame(int[] perm, Vector all)
	{
		super(new Frame(), "[PCGFrame]", true);

		PCGNode[] nods = new PCGNode[perm.length];

		for (int i = 0; i < perm.length; i++)
		{
			nods[i] = (PCGNode) all.elementAt(i);
		}

		init(perm, nods);
	}

	public PCGFrame(int[] perm, PCGNode[] nodes)
	{
		super(new Frame(), "[PCGFrame]", true);

		init(perm, nodes);
	}

	private void init(int[] perm, PCGNode[] nodes)
	{
		this.perm = perm;
		this.nodes = nodes;
		this.size = perm.length;

		Panel pNorth = new Panel(new FlowLayout(FlowLayout.LEFT));

		add(pNorth, BorderLayout.NORTH);
		pNorth.add(bDone = new Button("Done"));
		pNorth.add(bDump = new Button("Dump to stdout"));

		Panel pEast = new Panel(new GridLayout(6, 1));

		add(pEast, BorderLayout.EAST);
		pEast.add(new Label());
		pEast.add(new Label());
		pEast.add(bUp = new Button("Up"));
		pEast.add(bDown = new Button("Down"));
		pEast.add(new Label());
		pEast.add(new Label());
		add(order = new java.awt.List(25, false));
		build_list();
		bUp.addActionListener(this);
		bDown.addActionListener(this);
		bDone.addActionListener(this);
		bDump.addActionListener(this);
		add(new Label("reorder PCG, then press done"), BorderLayout.SOUTH);
		pack();
	}

	public void getUserPermutation()
	{
		setVisible(true);
	}

	public void actionPerformed(ActionEvent e)
	{
		Object src = e.getSource();

		if (src == bDown)
		{
			onDown();
		}
		else if (src == bUp)
		{
			onUp();
		}
		else if (src == bDone)
		{
			onDone();
		}
		else if (src == bDump)
		{
			onDump();
		}
	}

	private void onDown()
	{
		int sel = order.getSelectedIndex();

		if ((sel != -1) && (sel != size - 1))
		{
			int tmp = perm[sel];

			perm[sel] = perm[sel + 1];
			perm[sel + 1] = tmp;

			build_list();
			order.select(sel + 1);
		}
	}

	private void onUp()
	{
		int sel = order.getSelectedIndex();

		if ((sel != -1) && (sel != 0))
		{
			int tmp = perm[sel];

			perm[sel] = perm[sel - 1];
			perm[sel - 1] = tmp;

			build_list();
			order.select(sel - 1);
		}
	}

	private void onDone()
	{
		dispose();
	}

	private void onDump()
	{
		Options.out.println("Automata order in the BDDs:");

		int len = order.getItemCount();

		for (int i = 0; i < len; i++)
		{
			if (i != 0)
			{
				Options.out.print(" < ");
			}

			Options.out.print(order.getItem(i));
		}

		Options.out.println();
	}

	// --------------------------------------
	private void build_list()
	{
		order.removeAll();

		for (int i = 0; i < size; i++)
		{
			int p = perm[i];

			order.add(nodes[p].getName());
		}
	}
}
