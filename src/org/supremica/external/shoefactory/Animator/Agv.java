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
package org.supremica.external.shoeFactory.Animator;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;

//Class Agv is only a fancy name for the lines drawn in the factory
//That is, the lines represent the moving agv between the tables.

class Agv extends JPanel
{
	private Table[] t;

   	public Agv(Table[] tables)
   	{
   		t = tables;
   	}

   	public void draw(Graphics2D g2)
   	{
   	   	g2.drawLine(t[0].x+t[0].size, t[0].y+t[0].size/2, t[1].x, t[1].y+t[1].size/2);
   	   	g2.drawLine(t[1].x+t[1].size, t[1].y+t[1].size/2, t[2].x, t[2].y+t[2].size/2);
   	   	g2.drawLine(t[2].x+t[2].size, t[2].y+t[2].size/2, t[3].x, t[3].y+t[3].size/2);
   	   	g2.drawLine(t[3].x+t[3].size, t[3].y+t[3].size/2, t[4].x, t[4].y+t[4].size/2);
   	   	g2.drawLine(t[4].x+t[4].size, t[4].y+t[4].size/2, t[10].x+t[10].size/2, t[4].y+t[4].size/2);
   	   	g2.drawLine(t[10].x+t[10].size/2, t[4].y+t[4].size/2, t[10].x+t[10].size/2, t[5].y+t[5].size/2);
   	   	g2.drawLine(t[10].x+t[10].size/2, t[5].y+t[5].size/2, t[5].x, t[5].y+t[5].size/2);

   	   	g2.drawLine(t[6].x+t[6].size/2, t[0].y+t[0].size/2, t[6].x+t[6].size/2, t[6].y);
   	   	g2.drawLine(t[8].x+t[8].size/2, t[2].y+t[2].size/2, t[8].x+t[8].size/2, t[8].y);

   	   	g2.drawLine(t[7].x+t[7].size/2, t[1].y+t[1].size/2, t[7].x+t[7].size/2, t[7].y+t[7].size);
   	   	g2.drawLine(t[9].x+t[9].size/2, t[3].y+t[3].size/2, t[9].x+t[9].size/2, t[9].y+t[9].size);

   	   	g2.drawLine(t[10].x+t[10].size/2, t[4].y+t[4].size/2, t[10].x+t[10].size/2, t[10].y);
   	   	g2.drawLine(t[5].x+t[5].size, t[5].y+t[5].size/2, t[11].x+t[11].size/2, t[5].y+t[5].size/2);
   	   	g2.drawLine(t[11].x+t[11].size/2, t[5].y+t[5].size/2, t[11].x+t[11].size/2, t[11].y+t[11].size);
   	   	g2.drawLine(t[10].x+t[10].size/2, t[10].y+t[10].size, t[12].x+t[12].size/2, t[12].y);
   	}
}