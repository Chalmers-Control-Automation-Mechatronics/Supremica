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

public class Factory extends JFrame implements ActionListener{

   	private FactoryCanvas canvas;
   	private int s_id = 0, t_id = 0, st_id = 0, maxNrOfShoes = 100, bType=11;
   	private int nrOfaTables = 6, nrOfbTables = 6, nrOfcTables = 1, nrOfStations = 24;
   	private boolean stationVisit[] = new boolean[nrOfStations];
   	private Shoe[] s = new Shoe[maxNrOfShoes];
   	private ShoeThread[] sthread = new ShoeThread[maxNrOfShoes];
   	private Table[] t = new Table[nrOfaTables + nrOfbTables + nrOfcTables];
   	private Station[] st = new Station[nrOfStations];

   	private int[] a_tableXPos = {50, 200, 350, 500, 650, 820};
   	private int[] a_tableYPos = {250, 250, 250, 250, 250, 180};
   	private int[] b_tableXPos = {125, 275, 425, 575, 750, 875};
   	private int[] b_tableYPos = {400, 100, 400, 100, 350, 30};
   	private int[] c_tableXPos = {740};
   	private int[] c_tableYPos = {520};
   	private int stationXCoord[] = {100,90,160,250,210,400,370,260,240,400,460,550,
   									700,650,550,540,870,880,700,720,860,970,970,840};
   	private int stationYCoord[] = {380,440,530,480,370,170,80,70,150,400,530,410,130,
   									60,80,140,510,610,600,510,340,150,10,90};

   	JButton startButton = new JButton ("Add shoe");
   	JButton quitButton = new JButton ("Quit");
   	int nr=0;

//Creates the factory containing tables, stations and shoes
//and adds them to the JFrame calledcanvas

  	public Factory()
   	{
    	setSize(1024, 740);
      	setTitle("Shoefactory");
      	addWindowListener(new WindowAdapter()
      	{
			public void windowClosing(WindowEvent e)
			{
    	    	System.exit(0);
	    	}
	   	});
      	Container contentPane = getContentPane();
      	canvas = new FactoryCanvas(nrOfaTables+nrOfbTables+nrOfcTables, maxNrOfShoes);
      	JPanel buttonPanel = new JPanel();
      	contentPane.add(new JLabel("Only used for testing"), BorderLayout.NORTH);
      	contentPane.add(canvas, BorderLayout.CENTER);
      	contentPane.add(buttonPanel, BorderLayout.SOUTH);

      	buttonPanel.add(startButton);
      	buttonPanel.add(quitButton);
      	startButton.addActionListener(this);
		quitButton.addActionListener(this);

      	for(int i=0; i<nrOfaTables; i++)
      	{
	  		t[t_id] = canvas.addTable(a_tableXPos[i], a_tableYPos[i], 0);
			t_id++;
	  	}
	  	for(int i=0; i<nrOfbTables; i++)
	  	{
	  		t[t_id] = canvas.addTable(b_tableXPos[i], b_tableYPos[i], bType);
			t_id++;
			if(bType==11)
				bType=12;
			else if(bType==12)
				bType=11;
	  	}
	  	for(int i=0; i<nrOfcTables; i++)
	  	{
	  		t[t_id] = canvas.addTable(c_tableXPos[i], c_tableYPos[i], 2);
			t_id++;
	  	}

	  	for(int l=1; l<=nrOfStations; l++){

      		st[st_id] = canvas.addStation(stationXCoord[st_id], stationYCoord[st_id]);
      		st_id++;
	  	}

	  	Agv tracks = new Agv(t);
      	canvas.addAGV(tracks);

   		canvas.start();
   	}

   	public void actionPerformed(ActionEvent e)
   	{
   	   	if(e.getSource() == startButton)
   	   	{
   	 // StationVisit[i] defines which stations to visit - only used for testing.

   	   	/*	stationVisit[0]=true;
   	   		stationVisit[1]=true;
   	   		stationVisit[2]=true;
   	   		stationVisit[3]=true;
   	   		stationVisit[4]=true;
   	   		stationVisit[5]=true;
   	   		stationVisit[6]=true;
   	   		stationVisit[7]=true;
   	   		stationVisit[8]=true;
   	   		stationVisit[9]=true;
   	   		stationVisit[10]=true;
   	   		stationVisit[11]=true;
   	   		stationVisit[12]=true;
   	   		stationVisit[13]=true;
   	   		stationVisit[14]=true;
   	   		stationVisit[15]=true;
   	   		stationVisit[16]=true;
   	   		stationVisit[17]=true;
   	   		stationVisit[18]=true;
   	   		stationVisit[19]=true;
   	   		stationVisit[20]=true;*/
   	   		stationVisit[21]=true;
   	   		stationVisit[22]=true;
   	   		addShoe(stationVisit);
   	   		sthread[nr].start();
   	   		nr++;
   	   	}
   	   	if(e.getSource() == quitButton)
   	   	{
			setVisible(false);
		}
   	}

//Adds a shoe and starts its own thread that will handle the movement

   	public void addShoe(boolean stations[])
   	{
      	s[s_id] = canvas.addShoe(s_id);
      	s[s_id].inSystem = true;
      	sthread[s_id] = new ShoeThread(s[s_id], t, st, stations);
      	sthread[s_id].setPriority(Thread.NORM_PRIORITY);
      	s_id++;
  	}
}
