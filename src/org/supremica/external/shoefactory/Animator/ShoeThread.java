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

class ShoeThread extends Thread
{
   	private Shoe shoe;
   	private int s_id, x=0, y=300, dx, dy;
   	private boolean[] stationVisit = new boolean[24];
   	private Table[] tables;
   	private Station[] stations;
   	private int slot=0, theta=5, xCorrection, yCorrection;
   	static int[] stationAngle = {-45,-90,-170,-240,-330,-70,-140,-210,-270,-60,-170,-300,
								-100,-160,-220,-270,50,110,250,310,-310,-30,-150,-280};
   	static int[] angle = {0,0,0,0,0,0,0,0,0,0,0,0,0};

// Class Shothread defines how the shoes and tables move in the factory.
// Will most likely need massive changes when including supremica and JgrafC. control.

   	public ShoeThread(Shoe s, Table[] t, Station[] st, boolean[] p)
   	{
   		shoe = s;
   		tables = t;
   		stations = st;
       	stationVisit = p;
       	xCorrection=0;
   	}

    public void run()
    {
      	try
      	{
      		//checkTable(0,0);
      		transport(tables[0].x-xCorrection, tables[0].y + tables[0].size/2 - 5);
      		slot = tables[0].getPlaceNr(angle[0], 0);
      		System.out.println(angle[0]+", "+slot);
      		tables[0].putShoe(shoe, slot);
      		turnTable(0,angle[0]-180, false);
     		shoe.ats.translate(tables[0].size - shoe.size,0);
      		xCorrection += tables[0].size - shoe.size;
   			shoe = tables[0].getShoe(slot);
   			transport(tables[6].x + tables[6].size/2 - 5 - xCorrection, tables[0].y + tables[0].size/2 - 5);
   			sleep(5);

   			if(stationVisit[0] || stationVisit[1] || stationVisit[2] || stationVisit[3] || stationVisit[4])
   			{
   				//checkTable(6,1);
    	  		transport(tables[6].x + tables[6].size/2 - 5 - xCorrection, tables[6].y);
      			tables[6].putShoe(shoe, tables[6].getPlaceNr(angle[6], 1));
      			slot = tables[6].getPlaceNr(angle[6], 1);

      			turnTable(6,angle[6]+stationAngle[0], false);
      			if(stationVisit[0])
      			{
      				shoe = tables[6].getShoe(slot);
      				stations[0].putShoe(shoe);
      				System.out.println("OK 0");sleep(2000);
      				shoe = stations[0].getShoe();
      				slot = tables[6].getPlaceNr(stationAngle[0], angle[6], 1);
			   		tables[6].putShoe(shoe, slot);

      			}
      			turnTable(6,angle[6]+stationAngle[1]-stationAngle[0], false);
      			if(stationVisit[1])
      			{
      				shoe = tables[6].getShoe(slot);
      				stations[1].putShoe(shoe);
      				System.out.println("OK 1");sleep(2000);
      				shoe = stations[1].getShoe();
      				slot = tables[6].getPlaceNr(stationAngle[1], angle[6], 1);
			   		tables[6].putShoe(shoe, slot);

      			}
      			turnTable(6,angle[6]+stationAngle[2]-stationAngle[1], false);
      			if(stationVisit[2])
      			{
      				shoe = tables[6].getShoe(slot);
      				stations[2].putShoe(shoe);
      				System.out.println("OK 2");sleep(2000);
      				shoe = stations[2].getShoe();
      				slot = tables[6].getPlaceNr(stationAngle[2], angle[6], 1);
			   		tables[6].putShoe(shoe, slot);
      			}
      			turnTable(6,angle[6]+stationAngle[3]-stationAngle[2], false);
      			if(stationVisit[3])
      			{
      				shoe = tables[6].getShoe(slot);
      				stations[3].putShoe(shoe);
      				System.out.println("OK 3");sleep(2000);
      				shoe = stations[3].getShoe();
      				slot = tables[6].getPlaceNr(stationAngle[3], angle[6], 1);
			   		tables[6].putShoe(shoe, slot);
      			}
      			turnTable(6,angle[6]+stationAngle[4]-stationAngle[3], false);
      			if(stationVisit[4])
      			{
      				shoe = tables[6].getShoe(slot);
      				stations[4].putShoe(shoe);
      				System.out.println("OK 4");sleep(2000);
      				shoe = stations[4].getShoe();
      				slot = tables[6].getPlaceNr(stationAngle[4], angle[6], 1);
			   		tables[6].putShoe(shoe, slot);
      			}
      			turnTable(6,angle[6]-360-stationAngle[4], false);

   				shoe = tables[6].getShoe(slot);
   				transport(tables[6].x + tables[6].size/2 - 5 - xCorrection, tables[0].y + tables[0].size/2 - 5);
   				sleep(5);
   			}

   			//checkTable(1,0);
   			transport(tables[1].x-xCorrection, tables[1].y + tables[1].size/2 - 5);
   			tables[1].putShoe(shoe, tables[1].getPlaceNr(angle[1], 0));
   			slot = tables[1].getPlaceNr(angle[1], 0);
   			turnTable(1,angle[1]-180, false);
      		shoe.ats.translate(tables[1].size - shoe.size,0);
      		xCorrection += tables[1].size - shoe.size;
   			shoe = tables[1].getShoe(slot);
   			transport(tables[7].x + tables[7].size/2 - 5 - xCorrection, tables[1].y + tables[1].size/2 - 5);
   			sleep(5);

   			if(stationVisit[5] || stationVisit[6] || stationVisit[7] || stationVisit[8])
   			{
   				//checkTable(7,1);
    	  		transport(tables[7].x + tables[7].size/2 - 5 - xCorrection, tables[7].y + tables[7].size - shoe.size);
      			slot = tables[7].getPlaceNr(angle[7], 1);
      			tables[7].putShoe(shoe, slot);

      			turnTable(7,angle[7]+stationAngle[5], false);
      			if(stationVisit[5])
      			{
      				shoe = tables[7].getShoe(slot);
      				stations[5].putShoe(shoe);
      				System.out.println("OK 5");sleep(2000);
      				shoe = stations[5].getShoe();
      				slot = tables[7].getPlaceNr(stationAngle[5], angle[7], 1);
			   		tables[7].putShoe(shoe, slot);
      			}
      			turnTable(7,angle[7]+stationAngle[6]-stationAngle[5], false);
      			if(stationVisit[6])
      			{
      				shoe = tables[7].getShoe(slot);
      				stations[6].putShoe(shoe);
      				System.out.println("OK 6");sleep(2000);
      				shoe = stations[6].getShoe();
      				slot = tables[7].getPlaceNr(stationAngle[6], angle[7], 1);
			   		tables[7].putShoe(shoe, slot);
      			}
      			turnTable(7,angle[7]+stationAngle[7]-stationAngle[6], false);
      			if(stationVisit[7])
      			{
      				shoe = tables[7].getShoe(slot);
      				stations[7].putShoe(shoe);
      				System.out.println("OK 7");sleep(2000);
      				shoe = stations[7].getShoe();
      				slot = tables[7].getPlaceNr(stationAngle[7], angle[7], 1);
			   		tables[7].putShoe(shoe, slot);
      			}
      			turnTable(7,angle[7]+stationAngle[8]-stationAngle[7], false);
      			if(stationVisit[8])
      			{
      				shoe = tables[7].getShoe(slot);
      				stations[8].putShoe(shoe);
      				System.out.println("OK 8");sleep(2000);
      				shoe = stations[8].getShoe();
      				slot = tables[7].getPlaceNr(stationAngle[8], angle[7], 1);
			   		tables[7].putShoe(shoe, slot);
      			}
      			turnTable(7,-360, false);

   				shoe = tables[7].getShoe(slot);
   				transport(tables[7].x + tables[7].size/2 - 5 - xCorrection, tables[1].y + tables[1].size/2 - 5);
   				sleep(5);
   			}

   			//checkTable(2,0);
   			transport(tables[2].x-xCorrection, tables[2].y + tables[2].size/2 - 5);
      		tables[2].putShoe(shoe, tables[2].getPlaceNr(angle[2], 0));
      		turnTable(2,angle[2]-180, false);
      		shoe.ats.translate(tables[2].size - shoe.size,0);
      		xCorrection += tables[2].size - shoe.size;
   			shoe = tables[2].getShoe(slot);
   			transport(tables[8].x + tables[8].size/2 - 5 - xCorrection, tables[2].y + tables[2].size/2 - 5);
   			sleep(5);

   			if(stationVisit[9] || stationVisit[10] || stationVisit[11])
   			{
   				//checkTable(8,1);
    	  		transport(tables[8].x + tables[8].size/2 - 5 - xCorrection, tables[8].y);
    	  		slot = tables[8].getPlaceNr(angle[8], 1);
      			tables[8].putShoe(shoe, slot);

      			turnTable(8,angle[8]+stationAngle[9], false);
      			if(stationVisit[9])
      			{
      				shoe = tables[8].getShoe(slot);
      				stations[9].putShoe(shoe);
      				System.out.println("OK 9");sleep(2000);
      				shoe = stations[9].getShoe();
      				slot = tables[8].getPlaceNr(stationAngle[9], angle[8], 1);
			   		tables[8].putShoe(shoe, slot);
      			}
      			turnTable(8,angle[8]+stationAngle[10]-stationAngle[9], false);
      			if(stationVisit[10])
      			{
      				shoe = tables[8].getShoe(slot);
      				stations[10].putShoe(shoe);
      				System.out.println("OK 10");sleep(2000);
      				shoe = stations[10].getShoe();
      				slot = tables[8].getPlaceNr(stationAngle[10], angle[8], 1);
			   		tables[8].putShoe(shoe, slot);
      			}
      			turnTable(8,angle[8]+stationAngle[11]-stationAngle[10], false);
      			if(stationVisit[11])
      			{
      				shoe = tables[8].getShoe(slot);
      				stations[11].putShoe(shoe);
      				System.out.println("OK 11");sleep(2000);
      				shoe = stations[11].getShoe();
      				slot = tables[8].getPlaceNr(stationAngle[11], angle[8], 1);
			   		tables[8].putShoe(shoe, slot);
      			}
      			turnTable(8,-360, false);

   				shoe = tables[8].getShoe(slot);
   				transport(tables[8].x + tables[8].size/2 - 5 - xCorrection, tables[2].y + tables[2].size/2 - 5);
   				sleep(5);
   			}

   			//checkTable(3,0);
   			transport(tables[3].x-xCorrection, tables[3].y + tables[3].size/2 - 5);
   			slot = tables[3].getPlaceNr(angle[3], 0);
      		tables[3].putShoe(shoe, slot);
      		turnTable(3,angle[3]-180, false);
      		shoe.ats.translate(tables[3].size - shoe.size,0);
      		xCorrection += tables[3].size - shoe.size;
   			shoe = tables[3].getShoe(slot);
   			transport(tables[9].x + tables[9].size/2 - 5 - xCorrection, tables[3].y + tables[3].size/2 - 5);
   			sleep(5);

   			if(stationVisit[12] || stationVisit[13] || stationVisit[14] || stationVisit[15])
   			{
   				//checkTable(9,1);
    	  		transport(tables[9].x + tables[9].size/2 - 5 - xCorrection, tables[9].y + tables[9].size - shoe.size);
      			slot = tables[9].getPlaceNr(angle[9], 1);
      			tables[9].putShoe(shoe, slot);

      			turnTable(9,angle[9]+stationAngle[12], false);
   				if(stationVisit[12])
      			{
      				shoe = tables[9].getShoe(slot);
      				stations[12].putShoe(shoe);
      				System.out.println("OK 12");sleep(2000);
      				shoe = stations[12].getShoe();
      				slot = tables[9].getPlaceNr(stationAngle[12], angle[9], 1);
			   		tables[9].putShoe(shoe, slot);
      			}
      			turnTable(9,angle[9]+stationAngle[13]-stationAngle[12], false);
      			if(stationVisit[13])
      			{
      				shoe = tables[9].getShoe(slot);
      				stations[13].putShoe(shoe);
      				System.out.println("OK 13");sleep(2000);
      				shoe = stations[13].getShoe();
      				slot = tables[9].getPlaceNr(stationAngle[13], angle[9], 1);
			   		tables[9].putShoe(shoe, slot);
      			}
      			turnTable(9,angle[9]+stationAngle[14]-stationAngle[13], false);
      			if(stationVisit[14])
      			{
      				shoe = tables[9].getShoe(slot);
      				stations[14].putShoe(shoe);
      				System.out.println("OK 14");sleep(2000);
      				shoe = stations[14].getShoe();
      				slot = tables[9].getPlaceNr(stationAngle[14], angle[9], 1);
			   		tables[9].putShoe(shoe, slot);
      			}
      			turnTable(9,angle[9]+stationAngle[15]-stationAngle[14], false);
      			if(stationVisit[15])
      			{
      				shoe = tables[9].getShoe(slot);
      				stations[15].putShoe(shoe);
      				System.out.println("OK 15");sleep(2000);
      				shoe = stations[15].getShoe();
      				slot = tables[9].getPlaceNr(stationAngle[15], angle[9], 1);
			   		tables[9].putShoe(shoe, slot);
      			}
      			turnTable(9,-360, false);

   				shoe = tables[9].getShoe(slot);
   				transport(tables[9].x + tables[9].size/2 - 5 - xCorrection, tables[3].y + tables[3].size/2 - 5);
   				sleep(5);
   			}

   			//checkTable(4,0);
   			transport(tables[4].x-xCorrection, tables[4].y + tables[4].size/2 - 5);
   			slot = tables[4].getPlaceNr(angle[4], 0);
      		tables[4].putShoe(shoe, slot);
      		turnTable(4,angle[4]-180, false);
      		shoe.ats.translate(tables[4].size - shoe.size,0);
      		xCorrection += tables[4].size - shoe.size;
   			shoe = tables[4].getShoe(slot);
   			transport(tables[10].x + tables[10].size/2 - 5 - xCorrection, tables[4].y + tables[4].size/2 - 5);
   			sleep(5);

   			if(stationVisit[16] || stationVisit[17] || stationVisit[18] || stationVisit[19] || stationVisit[20])
   			{
   				//checkTable(10,1);
    	  		transport(tables[10].x + tables[10].size/2 - 5 - xCorrection, tables[10].y);
      			slot = tables[10].getPlaceNr(angle[10], 1);
      			tables[10].putShoe(shoe, slot);

      			turnTable(10,angle[10]-180, false);

      			if(stationVisit[16] || stationVisit[17] || stationVisit[18] || stationVisit[19])
      			{
      				shoe.ats.translate(0, tables[10].size - shoe.size);
      				yCorrection = tables[10].size - shoe.size;
      				shoe = tables[10].getShoe(slot);
      				//checkTable(12,2);
      				transport(tables[12].x + tables[12].size/2 - 5 - xCorrection, tables[12].y - yCorrection);
					slot = tables[12].getPlaceNr(angle[12], 2);
      				tables[12].putShoe(shoe, slot);

      				turnTable(12,angle[12]+stationAngle[16], true);
      				if(stationVisit[16])
      				{
      					shoe = tables[12].getShoe(slot);
      					stations[16].putShoe(shoe);
      					System.out.println("OK 16");sleep(2000);
      					shoe = stations[16].getShoe();
      					slot = tables[12].getPlaceNr(stationAngle[16], angle[12], 2);
			   			tables[12].putShoe(shoe, slot);
      				}
      				turnTable(12,angle[12]+stationAngle[17]-stationAngle[16], true);
      				if(stationVisit[17])
      				{
      					shoe = tables[12].getShoe(slot);
      					stations[17].putShoe(shoe);
      					System.out.println("OK 17");sleep(2000);
      					shoe = stations[17].getShoe();
      					slot = tables[12].getPlaceNr(stationAngle[17], angle[12], 2);
			   			tables[12].putShoe(shoe, slot);
      				}
      				turnTable(12,angle[12]+stationAngle[18]-stationAngle[17], true);
      				if(stationVisit[18])
      				{
      					shoe = tables[12].getShoe(slot);
      					stations[18].putShoe(shoe);
      					System.out.println("OK 18");sleep(2000);
      					shoe = stations[18].getShoe();
      					slot = tables[12].getPlaceNr(stationAngle[18], angle[12], 2);
			   			tables[12].putShoe(shoe, slot);
      				}
      				turnTable(12,angle[12]+stationAngle[19]-stationAngle[18], true);
      				if(stationVisit[19])
      				{
      					shoe = tables[12].getShoe(slot);
      					stations[19].putShoe(shoe);
      					System.out.println("OK 19");sleep(2000);
      					shoe = stations[19].getShoe();
      					slot = tables[12].getPlaceNr(stationAngle[19], angle[12], 2);
			   			tables[12].putShoe(shoe, slot);
      				}
      				turnTable(12,360, true);

      				shoe = tables[12].getShoe(slot);
      				transport(tables[10].x + tables[10].size/2 - 5 - xCorrection, tables[10].y + tables[10].size - shoe.size - yCorrection);
      			}

      			slot = tables[10].getPlaceNr(-180, angle[10], 1);
      			tables[10].putShoe(shoe, slot);
      			turnTable(10, angle[10]+stationAngle[20]-180, false);
      			if(stationVisit[20])
      			{
      				shoe = tables[10].getShoe(slot);
      				stations[20].putShoe(shoe);
      				System.out.println("OK 20");sleep(2000);
      				shoe = stations[20].getShoe();
      				slot = tables[10].getPlaceNr(stationAngle[20], angle[10], 1);
			   		tables[10].putShoe(shoe, slot);
      			}
      			turnTable(10,-360, false);
      			shoe.ats.translate(0, -yCorrection);
   				shoe = tables[10].getShoe(slot);
   				transport(tables[10].x + tables[10].size/2 - 5 - xCorrection, tables[4].y + tables[4].size/2 - 5);
   				sleep(5);
   			}
			transport(tables[10].x + tables[10].size/2 - 5 - xCorrection, tables[5].y + tables[5].size/2 - 5);

   			//checkTable(5,0);
   			transport(tables[5].x-xCorrection, tables[5].y + tables[5].size/2 - 5);
   			slot = tables[5].getPlaceNr(angle[5], 0);
      		tables[5].putShoe(shoe, slot);
      		turnTable(5,angle[5]-180, false);
      		shoe.ats.translate(tables[5].size - shoe.size,0);
      		xCorrection += tables[5].size - shoe.size;
   			shoe = tables[5].getShoe(slot);
   			transport(tables[11].x + tables[11].size/2 - 5 - xCorrection, tables[5].y + tables[5].size/2 - 5);
   			sleep(5);

   			//checkTable(11,1);
   			transport(tables[11].x + tables[11].size/2 - 5 - xCorrection, tables[11].y + tables[11].size - shoe.size);
      		slot = tables[11].getPlaceNr(angle[11], 1);
      		tables[11].putShoe(shoe, slot);

      		turnTable(11,angle[11]+stationAngle[21], false);
      		if(stationVisit[21])
      			{
      				shoe = tables[11].getShoe(slot);
      				stations[21].putShoe(shoe);
      				System.out.println("OK 21");sleep(2000);
      				shoe = stations[21].getShoe();
      				slot = tables[11].getPlaceNr(stationAngle[21], angle[11], 1);
			   		tables[11].putShoe(shoe, slot);
      			}
      		turnTable(11,angle[11]+stationAngle[22]-stationAngle[21], false);
      		if(stationVisit[22])
      			{
      				shoe = tables[11].getShoe(slot);
      				stations[22].putShoe(shoe);
      				System.out.println("OK 22");sleep(2000);
      				shoe = stations[22].getShoe();
      				slot = tables[11].getPlaceNr(stationAngle[22], angle[11], 1);
			   		tables[11].putShoe(shoe, slot);
      			}
      		turnTable(11,angle[11]+stationAngle[23]-stationAngle[22], false);

      		shoe = tables[11].getShoe(slot);
      		stations[23].putShoe(shoe);
			System.out.println("OK 23");sleep(2000);

      		shoe = stations[23].getShoe();
      		shoe.inSystem = false;
   			sleep(5);
   		}
     	catch (InterruptedException exception)
     	{
     	}
  	}

//checkTable checks whether there is a free slot to put the shoe into.
//If not, the table rotates.

  	public void checkTable(int i, int type)
  	{
  		while(tables[i].fullslot[tables[i].getPlaceNr(angle[i],type)])
      	{
			waitTime(50);
      		tables[i].rotateTable(theta, tables[i].x+tables[i].size/2, tables[i].y+tables[i].size/2);
      		angle[i]+=theta;
      		if(angle[i]==-360)
      			angle[i]=0;
      	}
        transport(tables[i].x-xCorrection, tables[i].y + tables[i].size/2 - 5);
  	}

// turnTable turns the corresponding table to a predefined position

  	public void turnTable(int i, int turnshoe, boolean clockwise)
  	{
  		if(turnshoe >= 360)
  			turnshoe -= 360;
  		else if(turnshoe <= -360)
  			turnshoe += 360;

  		while(angle[i] != turnshoe)
      	{
      		//Makes a brief pause every time a slot is "available"
			if(i<=5 && angle[i]%30==0)
				waitTime(300);
			if(i>=6 && angle[i]%15==0)
				waitTime(300);

      		if(!clockwise){
      			tables[i].rotateTable(-theta, tables[i].x + tables[i].size/2, tables[i].y + tables[i].size/2);
      			angle[i] -= theta;
      			if(angle[i]==-360)
      				angle[i]=0;
      		}
      		else{
      			tables[i].rotateTable(theta, tables[i].x + tables[i].size/2, tables[i].y + tables[i].size/2);
      			angle[i] += theta;
      			if(angle[i]==360)
      				angle[i]=0;
      		}
      		waitTime(10);
      	}
      	tables[i].turning = false;
  	}

//The method that handles the shoe when it isn't on a table or in a station.

   	public void transport(int xf, int yf)
   	{
   		while(x!=xf || y!=yf)
   		{
   			if(x==xf)
   				dx=0;
   			if(y==yf)
   				dy=0;
   			if(x>xf)
   				dx=-1;
   			if(x<xf)
   				dx=1;
   			if(y>yf)
   				dy=-1;
   			if(y<yf)
   				dy=1;
      		x+=dx; y+=dy;
      		shoe.moveShoe(dx,dy);
      		waitTime(20);
      	}
	}

//Only used as a pausing method.

	public void waitTime(int w)
	{
		Thread T =new Thread();
		T.start();
		try
		{
	   	 	T.sleep(w);
		}
		catch(InterruptedException e)
		{
		}
		T.interrupt();
	}
}
