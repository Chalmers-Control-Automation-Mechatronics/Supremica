/************************ AlphabetSubTree.java *****************/

package org.supremica.gui.treeview;

import javax.swing.*;
import java.util.*;

import org.supremica.automata.Alphabet;
import org.supremica.automata.LabeledEvent;
import org.supremica.gui.Supremica;

public class AlphabetSubTree
	extends SupremicaTreeNode
{
	private static ImageIcon alphabetIcon = 
		new ImageIcon(Supremica.class.getResource("/icons/Alphabet16.gif"));

	public AlphabetSubTree(Alphabet alpha)
	{
		super("Alphabet");
	
		buildSubTree(alpha, this);
	}
	
	// Note, we cannot builds a subtree without an Alphabet-root, since
	// such a tree would be a forest with no root. Therefore, we need to
	// take the node to insert to and insert into it
	static public void buildSubTree(Alphabet alpha, SupremicaTreeNode root)
	{
		Iterator eventIt = alpha.iterator();
		while (eventIt.hasNext())
		{
			LabeledEvent currEvent = (LabeledEvent) eventIt.next();
			root.add(new EventSubTree(currEvent));
		}
	}

	public Icon getOpenIcon()
	{		
		return alphabetIcon;
	}

	public Icon getClosedIcon()
	{
		return getOpenIcon();
	}

	public Icon getLeafIcon()
	{
		return getOpenIcon();
	}
}
