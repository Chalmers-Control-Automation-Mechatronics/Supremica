package org.jgrafchart;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import com.nwoods.jgo.*;
import javax.swing.border.*;
import javax.swing.event.*;
import org.jgrafchart.Transitions.*;
import org.jgrafchart.Actions.*;
import java.util.*;

public class GrafchartStorage
{
	private ArrayList store = new ArrayList();

	public GrafchartStorage() {}

	public void add(GCDocument doc)
	{
		store.add(doc);
	}

	public void remove(GCDocument doc)
	{
		if (store.contains(doc))
		{
			store.remove(store.indexOf(doc));
		}
	}

	public ArrayList getStorage()
	{
		return store;
	}

	public Iterator iterator()
	{
		return store.iterator();
	}

	public void printOutNames()
	{
		GCDocument doc;

		for (Iterator i = store.iterator(); i.hasNext(); )
		{
			doc = (GCDocument) i.next();

			System.out.println(doc.getName());
		}
	}
}
