/*
* Supremica Software License Agreement
*
* The Supremica software is not in the public domain
* However, it is freely available without fee for education,
* research, and non-profit purposes.  By obtaining copies of
* this and other files that comprise the Supremica software,
* you, the Licensee, agree to abide by the following
* conditions and understandings with respect to the
* copyrighted software:
*
* The software is copyrighted in the name of Supremica,
* and ownership of the software remains with Supremica.
*
* Permission to use, copy, and modify this software and its
* documentation for education, research, and non-profit
* purposes is hereby granted to Licensee, provided that the
* copyright notice, the original author's names and unit
* identification, and this permission notice appear on all
* such copies, and that no charge be made for such copies.
* Any entity desiring permission to incorporate this software
* into commercial products or to use it for commercial
* purposes should contact:
*
* Knut Akesson (KA), knut@supremica.org
* Supremica,
* Haradsgatan 26A
* 431 42 Molndal
* SWEDEN
*
* to discuss license terms. No cost evaluation licenses are
* available.
*
* Licensee may not use the name, logo, or any other symbol
* of Supremica nor the names of any of its employees nor
* any adaptation thereof in advertising or publicity
* pertaining to the software without specific prior written
* approval of the Supremica.
*
* SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
* SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
* IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
*
* Supremica or KA shall not be liable for any damages
* suffered by Licensee from the use of this software.
*
* Supremica is owned and represented by KA.
*/
package org.supremica.external.shoefactory.Animator;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

class FactoryCanvas extends JPanel implements Runnable
{
   private ArrayList aTables = new ArrayList();
   private ArrayList bTables = new ArrayList();
   private ArrayList cTables = new ArrayList();
   private ArrayList shoes = new ArrayList();
   private ArrayList stations = new ArrayList();
   private Agv tracks;
   private int stationXCoord[];
   private int stationYCoord[];
   private int dx=1, dy=1;
   private Thread thread;

//The graphical interface that draws the various objects

   public FactoryCanvas(int nrOfTables, int maxNrOfShoes)
   {
   		//setBackground(Color.white);
   }

   public Table addTable(int tableXPos, int tableYPos, int type)
   {
   		Table t = new Table(tableXPos, tableYPos, type);
   		if(type == 0)
   		{
	    	aTables.add(t);
	    	return t;
	    }
	    if(type == 11 || type==12)
	    {
	    	bTables.add(t);
	    	return t;
	    }
	    if(type == 2)
	    {
	    	cTables.add(t);
	    	return t;
	    }
	    else return null;
   }

   public Shoe addShoe(int s_id)
   {
   		Shoe s = new Shoe();
   		System.out.println("Nr of shoes: "+s_id);
      	shoes.add(s);
      	return s;
   }

   public Station addStation(int xPos, int yPos)
   {
      	Station st = new Station(xPos, yPos);
      	stations.add(st);
      	return st;
   }

   public void addAGV(Agv a)
   {
   		tracks = a;
   }

   	public void draw(Graphics2D g2)
   	{
   		tracks.draw(g2);
   		for (int i=0; i<aTables.size(); i++)
      	{
      		g2.setPaint(Color.red);
         	Table t = (Table)aTables.get(i);
         	g2.setTransform(t.att);
			t.draw(g2);
       	}
       	for (int j=0; j<bTables.size(); j++)
      	{
      		g2.setPaint(Color.yellow);
         	Table t = (Table)bTables.get(j);
         	g2.setTransform(t.att);
         	t.draw(g2);
       	}
       	for (int k=0; k<cTables.size(); k++)
      	{
      		g2.setPaint(Color.orange);
         	Table t = (Table)cTables.get(k);
         	g2.setTransform(t.att);
         	t.draw(g2);
       	}
       	for (int l=0; l<stations.size(); l++)
       	{
       		g2.setPaint(Color.blue);
       		Station st = (Station)stations.get(l);
       		g2.setTransform(st.atst);
       		st.draw(g2);
       	}
       	for (int m=0; m<shoes.size(); m++)
      	{
      		Shoe s = (Shoe)shoes.get(m);
      		//AffineTransform oldTransform = g2.getTransform();
   			g2.setTransform(s.ats);
   			g2.setPaint(Color.black);
   			s.draw(g2);
   			//g2.setTransform(oldTransform);
   		}
   	}

   	public void paintComponent(Graphics g)
   	{
      	super.paintComponent(g);
      	Graphics2D g2 = (Graphics2D)g;
      	draw(g2);
      	g2.setPaint(Color.black);
      	g2.dispose();
   	}

   	public void start()
   	{
        thread = new Thread(this);
        thread.setPriority(Thread.NORM_PRIORITY);
        thread.start();
    }

    public void run()
    {
        Thread me = Thread.currentThread();
        while (thread == me) {
            repaint();
            try {
                thread.sleep(10);
            } catch (InterruptedException e) { break; }
        }
        thread = null;
    }
}
