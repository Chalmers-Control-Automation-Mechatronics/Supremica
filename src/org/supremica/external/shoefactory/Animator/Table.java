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
import java.awt.image.*;
import java.util.*;
import javax.swing.*;

class Table extends JPanel
{
	Ellipse2D[] slot;
	Ellipse2D table;
	boolean[] fullslot;
	private ArrayList transform = new ArrayList();
	private Shoe[] shoe = new Shoe[24];
	AffineTransform att;
	int x, y, size;
	boolean turning = false;

   	public Table(int xPos, int yPos, int type)
   	{
   		x = xPos; y = yPos;

		//Creates a different amount of slots depending on the type of table

      	if(type==0)
      	{
      		size=100;
	    	table = new Ellipse2D.Double(x, y, size, size);
	    	slot = new Ellipse2D[12];
	    	fullslot = new boolean[12];
			for (int i = 0; i<slot.length; i++)
			{
      	   		slot[i] = new Ellipse2D.Double(x+45-45*Math.cos(i*Math.toRadians(360)/slot.length), y+45-45*Math.sin(i*Math.toRadians(360)/slot.length), 10, 10);
      		}
	    }
	    if(type==11)
	    {
	    	size=120;
	    	table = new Ellipse2D.Double(x, y, size, size);
	    	slot = new Ellipse2D[24];
	    	fullslot = new boolean[24];
			for (int i = 0; i<slot.length; i++)
			{
      	   		slot[i] = new Ellipse2D.Double(x+55+55*Math.cos(i*Math.toRadians(360)/slot.length-Math.toRadians(90)), y+55+55*Math.sin(i*Math.toRadians(360)/slot.length-Math.toRadians(90)), 10, 10);
      		}
	    }
	    if(type==12)
	    {
	    	size=120;
	    	table = new Ellipse2D.Double(x, y, size, size);
	    	slot = new Ellipse2D[24];
	    	fullslot = new boolean[24];
			for (int i = 0; i<slot.length; i++)
			{
      	   		slot[i] = new Ellipse2D.Double(x+55+55*Math.cos(i*Math.toRadians(360)/slot.length+Math.toRadians(90)), y+55+55*Math.sin(i*Math.toRadians(360)/slot.length+Math.toRadians(90)), 10, 10);
      		}
	    }
	    if(type==2)
	    {
	    	size=140;
	    	table = new Ellipse2D.Double(x, y, size, size);
	    	slot = new Ellipse2D[24];
	    	fullslot = new boolean[24];
			for (int i = 0; i<slot.length; i++)
			{
      	   		slot[i] = new Ellipse2D.Double(x+65+65*Math.cos(i*Math.toRadians(360)/slot.length-Math.toRadians(90)), y+65+65*Math.sin(i*Math.toRadians(360)/slot.length-Math.toRadians(90)), 10, 10);
      		}
	    }
	    att = new AffineTransform();
	}

	public void draw(Graphics2D g2)
	{
		g2.fill(table);
		for(int i=0; i<slot.length; i++)
		{
			if(fullslot[i])
			{
				g2.setPaint(Color.black);
				g2.fill(slot[i]);
			}
			else{
				g2.setPaint(Color.white);
				g2.fill(slot[i]);
			}
		}
	}

//Put a shoe on the table

    public void putShoe(Shoe s, int place)
    {
    	shoe[place] = s;
    	shoe[place].onTable = true;
    	fullslot[place] = true;
    	repaint();
    }

   	public void rotateTable(int a, int x, int y)
   	{
   	    att.rotate(Math.toRadians(a), x, y);
   	    repaint();
   	}

//Take the shoe from the table

   	public Shoe getShoe(int place)
   	{
   		shoe[place].onTable = false;
   		fullslot[place] = false;
   		repaint();
   		return shoe[place];
   	}

//getPlaceNr gives the id of the slot at the current angleposition

   	public int getPlaceNr(int stationAngle, int angle, int type)
   	{
   		if(angle==360 || angle==-360)
   			angle=0;
   		if(type==0)
   			return Math.abs((angle-stationAngle)/30);
   		else
   			return Math.abs((angle-stationAngle)/15);
   	}

   	public int getPlaceNr(int angle, int type)
   	{
   		if(angle==360 || angle==-360)
   			angle=0;
   		if(type==0)
   			return Math.abs(angle/30);
   		else
   			return Math.abs(angle/15);
   	}
}
