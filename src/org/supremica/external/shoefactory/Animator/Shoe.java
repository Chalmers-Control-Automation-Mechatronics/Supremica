package org.supremica.external.shoefactory.Animator;

import java.awt.*;
import javax.swing.*;
import grafchart.sfc.*;

public class Shoe
{
	GCDocument shoe;
	private int yPos=0, stepID=0, shoeNr;
	private boolean[] stationVisit;
	private int[] stationRot = {3,7,11,18,20,4,8,13,18,5,12,19,7,11,14,20,3,9,14,20,19,3,9,18};
	public boolean done;

	public Shoe(GCDocument doc, boolean[] sV, int nr)
	{
		shoe = doc;
		shoe.setWorkspaceName("Shoe"+nr);
		shoe.setFrameRectangle(new Rectangle(0,0,800,800));
		shoeNr=nr;
		stationVisit = sV;

		IntegerVariable iv1 = shoe.createIntegerVariable(300,50,"nrOfRot","0");
		IntegerVariable iv2 = shoe.createIntegerVariable(400,50,"currentTable","0");
		BooleanVariable bv2 = shoe.createBooleanVariable(500,50,"done","0");
		BooleanVariable sAct = shoe.createBooleanVariable(200,150,"onStep","0");
		BooleanVariable[] bv = new BooleanVariable[24];

		for(int i=0; i<12;i++)
		{
			String s="0";
			if(stationVisit[i])
				s="1";
			bv[i] = shoe.createBooleanVariable(300+100*i,250,"station"+i,s);
			s="0";
			if(stationVisit[i+12])
				s="1";
			bv[i+12] = shoe.createBooleanVariable(300+100*i,350,"station"+(i+12),s);
		}

		GCStepInitial shoeInitialStep = shoe.createInitialStep(100,yPos,"Start",";");
		yPos+=100;
		GCTransition shoeTR0 = shoe.createTransition(100,yPos,"1");
		yPos+=50;
		GCStep shoeS0 = shoe.createStep(100,yPos,"shoeS0","S ShoeControl.moveFromTable0.goto=6;");
		yPos+=100;
		GCTransition shoeTR1a = shoe.createTransition(100,yPos,"Shoefactory.stations.station0.enterStation");
		GCTransition shoeTR1b = shoe.createTransition(300,yPos,"Shoefactory.stations.station1.enterStation");
		GCTransition shoeTR1c = shoe.createTransition(500,yPos,"Shoefactory.stations.station2.enterStation");
		GCTransition shoeTR1d = shoe.createTransition(700,yPos,"Shoefactory.stations.station3.enterStation");
		GCTransition shoeTR1e = shoe.createTransition(900,yPos,"Shoefactory.stations.station4.enterStation");
		yPos+=50;
		GCStep shoeS1a = shoe.createStep(100,yPos,"shoeS1a",";");
		GCStep shoeS1b = shoe.createStep(300,yPos,"shoeS1b",";");
		GCStep shoeS1c = shoe.createStep(500,yPos,"shoeS1c",";");
		GCStep shoeS1d = shoe.createStep(700,yPos,"shoeS1d",";");
		GCStep shoeS1e = shoe.createStep(900,yPos,"shoeS1e",";");
		yPos+=100;
		GCTransition shoeTR2a = shoe.createTransition(100,yPos,"Shoefactory.stations.station0.leaveStation");
		GCTransition shoeTR2b = shoe.createTransition(300,yPos,"Shoefactory.stations.station1.leaveStation");
		GCTransition shoeTR2c = shoe.createTransition(500,yPos,"Shoefactory.stations.station2.leaveStation");
		GCTransition shoeTR2d = shoe.createTransition(700,yPos,"Shoefactory.stations.station3.leaveStation");
		GCTransition shoeTR2e = shoe.createTransition(900,yPos,"Shoefactory.stations.station4.leaveStation");
		yPos+=50;
		GCStep shoeS1= shoe.createStep(100,yPos,"shoeS1","S ShoeControl.moveFromTable6.goto=7;");
		yPos+=100;
		GCTransition shoeTR3a = shoe.createTransition(100,yPos,"Shoefactory.stations.station5.enterStation");
		GCTransition shoeTR3b = shoe.createTransition(300,yPos,"Shoefactory.stations.station6.enterStation");
		GCTransition shoeTR3c = shoe.createTransition(500,yPos,"Shoefactory.stations.station7.enterStation");
		GCTransition shoeTR3d = shoe.createTransition(700,yPos,"Shoefactory.stations.station8.enterStation");
		yPos+=50;
		GCStep shoeS2a = shoe.createStep(100,yPos,"shoeS2a",";");
		GCStep shoeS2b = shoe.createStep(300,yPos,"shoeS2b",";");
		GCStep shoeS2c = shoe.createStep(500,yPos,"shoeS2c",";");
		GCStep shoeS2d = shoe.createStep(700,yPos,"shoeS2d",";");
		GCStep shoeS2e = shoe.createStep(900,yPos,"shoeS2e",";");
		yPos+=100;
		GCTransition shoeTR4a = shoe.createTransition(100,yPos,"Shoefactory.stations.station5.leaveStation");
		GCTransition shoeTR4b = shoe.createTransition(300,yPos,"Shoefactory.stations.station6.leaveStation");
		GCTransition shoeTR4c = shoe.createTransition(500,yPos,"Shoefactory.stations.station7.leaveStation");
		GCTransition shoeTR4d = shoe.createTransition(700,yPos,"Shoefactory.stations.station8.leaveStation");
		yPos+=50;
		
		GCStep shoeS2= shoe.createStep(100,yPos,"shoeS2","S ShoeControl.moveFromTable7.goto=8;");
		yPos+=100;
		
		GCTransition shoeTR5a = shoe.createTransition(100,yPos,"Shoefactory.stations.station9.enterStation");
		GCTransition shoeTR5b = shoe.createTransition(300,yPos,"Shoefactory.stations.station10.enterStation");
		GCTransition shoeTR5c = shoe.createTransition(500,yPos,"Shoefactory.stations.station11.enterStation");
		
		shoe.connect(shoeInitialStep,shoeTR0);
		shoe.connect(shoeTR0,shoeS0);
		shoe.connect(shoeS0,shoeTR1a);
		shoe.connect(shoeS0,shoeTR1b);
		shoe.connect(shoeS0,shoeTR1c);
		shoe.connect(shoeS0,shoeTR1d);
		shoe.connect(shoeS0,shoeTR1e);
		shoe.connect(shoeTR1a,shoeS1a);
		shoe.connect(shoeTR1b,shoeS1b);
		shoe.connect(shoeTR1c,shoeS1c);
		shoe.connect(shoeTR1d,shoeS1d);
		shoe.connect(shoeTR1e,shoeS1e);
		shoe.connect(shoeS1a,shoeTR2a);
		shoe.connect(shoeS1b,shoeTR2b);
		shoe.connect(shoeS1c,shoeTR2c);
		shoe.connect(shoeS1d,shoeTR2d);
		shoe.connect(shoeS1d,shoeTR2e);
		shoe.connect(shoeTR2a,shoeS1);
		shoe.connect(shoeTR2b,shoeS1);
		shoe.connect(shoeTR2c,shoeS1);
		shoe.connect(shoeTR2d,shoeS1);
		shoe.connect(shoeTR2e,shoeS1);
		shoe.connect(shoeS1,shoeTR3a);
		shoe.connect(shoeS1,shoeTR3b);
		shoe.connect(shoeS1,shoeTR3c);
		shoe.connect(shoeS1,shoeTR3d);
		shoe.connect(shoeTR3a,shoeS2a);
		shoe.connect(shoeTR3b,shoeS2b);
		shoe.connect(shoeTR3c,shoeS2c);
		shoe.connect(shoeTR3d,shoeS2d);
		shoe.connect(shoeS2a,shoeTR4a);
		shoe.connect(shoeS2b,shoeTR4b);
		shoe.connect(shoeS2c,shoeTR4c);
		shoe.connect(shoeS2d,shoeTR4d);
		
		shoe.connect(shoeTR4a,shoeS2);
		shoe.connect(shoeTR4b,shoeS2);
		shoe.connect(shoeTR4c,shoeS2);
		shoe.connect(shoeTR4d,shoeS2);
		shoe.connect(shoeS2,shoeTR5a);
		shoe.connect(shoeS2,shoeTR5b);
		shoe.connect(shoeS2,shoeTR5c);
		
		yPos=0;
		WorkspaceObject woSC = shoe.createWorkspaceObject(600,50,"ShoeControl");
		GCDocument shoeCtrl = woSC.getSubWorkspace();

		// Create Grafcet
		GCStepInitial initialStep = shoeCtrl.createInitialStep(100,yPos,"Start","S JgrafSupervisor.nrOfShoes = JgrafSupervisor.nrOfShoes+1;");

		yPos+=100;
		GCTransition tr0 = shoeCtrl.createTransition(100,yPos,"!Shoefactory.shoeInWarehouse");

		yPos+=50;

		GCStep s0 = createTableSFC(shoeCtrl,tr0,0,100,yPos,1);

		yPos+=250;
		GCTransition tr1 = shoeCtrl.createTransition(100,yPos,"1");
		yPos+=50;
		GCStep s1 = shoeCtrl.createStep(100,yPos,"S1",";");
		yPos+=100;

		GCTransition tr2a = shoeCtrl.createTransition(100,yPos+150,"currentTable==0 & (station0 | station1 |station2 |station3 |station4 |station5 |station6 |station7 |station8 |station9 |station10 |station11"+
		 														"|station12 |station13 |station14 |station15 |station16 |station17 |station18 |station19 |station20 |station21 |station22 |station23)");
		GCTransition tr2done = shoeCtrl.createTransition(0,yPos+150,"currentTable==0  & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table0.rot)==0 & (!station0 & !station1 & !station2 &!station3 &!station4 &!station5 &!station6 &!station7 &!station8 &!station9 &!station10 &!station11"+
		 														"&!station12 &!station13 &!station14 &!station15 &!station16 &!station17 &!station18 &!station19 &!station20 &!station21 &!station22 &!station23)");

		GCStep s2a = shoeCtrl.createStep(100,yPos+200,"move_table0","S moveFromTable0.start=1;");
		GCStep s2done = shoeCtrl.createStep(0,yPos+200,"removeshoe","S Shoefactory.tables.table0.fullSlot.set(nrOfRot,0);\nS done = applyStaticMethod(\"org.supremica.external.shoefactory.Executor.FactoryExecutor\",\"deleteShoe\",\"boolean\",\"int\","+shoeNr+");");
		GCTransition tr3a = shoeCtrl.createTransition(100,yPos+300,"moveFromTable0.moveReady");

		shoeCtrl.connect(initialStep,tr0);
		shoeCtrl.connect(s0,tr1);
		shoeCtrl.connect(tr1,s1);
		shoeCtrl.connect(s1,tr2a);
		shoeCtrl.connect(s1,tr2done);

		shoeCtrl.connect(tr2a,s2a);
		shoeCtrl.connect(tr2done,s2done);
		shoeCtrl.connect(s2a,tr3a);
		shoeCtrl.connect(tr3a,s1);

		WorkspaceObject wo0 = shoeCtrl.createWorkspaceObject(700,50,"moveFromTable0");
		GCDocument moveFromTable0 = wo0.getSubWorkspace();
		createSubdoc(moveFromTable0, 0);

		if(stationVisit[0] || stationVisit[1] || stationVisit[2] || stationVisit[3] || stationVisit[4])
		{
			GCTransition tr2b = shoeCtrl.createTransition(400,yPos,"currentTable==6");
			GCStep sON6 = shoeCtrl.createStep(400,yPos+50,"on_table6","S onTable6.start=1;");
			GCTransition trON6 = shoeCtrl.createTransition(400,yPos+150,"onTable6.leavetable");
			GCStep s2b = shoeCtrl.createStep(400,yPos+200,"move_table6","S moveFromTable6.start=1;");
			GCTransition tr3b = shoeCtrl.createTransition(400,yPos+300,"moveFromTable6.moveReady");

			shoeCtrl.connect(s1,tr2b);
			shoeCtrl.connect(tr2b,sON6);
			shoeCtrl.connect(sON6,trON6);
			shoeCtrl.connect(trON6,s2b);
			shoeCtrl.connect(s2b,tr3b);
			shoeCtrl.connect(tr3b,s1);

			WorkspaceObject wo1 = shoeCtrl.createWorkspaceObject(825,50,"moveFromTable6");
			GCDocument moveToTable6 = wo1.getSubWorkspace();
			createSubdoc(moveToTable6, 6);

			WorkspaceObject wo2 = shoeCtrl.createWorkspaceObject(825,150,"onTable6");
			GCDocument onTable6 = wo2.getSubWorkspace();
			createonTable(onTable6, 6);
		}

		if(stationVisit[5] || stationVisit[6] || stationVisit[7] || stationVisit[8])
		{
			GCTransition tr2c = shoeCtrl.createTransition(700,yPos,"currentTable==7");
			GCStep sON7 = shoeCtrl.createStep(700,yPos+50,"on_table7","S onTable7.start=1;");
			GCTransition trON7 = shoeCtrl.createTransition(700,yPos+150,"onTable7.leavetable");
			GCStep s2c = shoeCtrl.createStep(700,yPos+200,"move_table7","S moveFromTable7.start=1;");
			GCTransition tr3c = shoeCtrl.createTransition(700,yPos+300,"moveFromTable7.moveReady");

			shoeCtrl.connect(s1,tr2c);
			shoeCtrl.connect(tr2c,sON7);
			shoeCtrl.connect(sON7,trON7);
			shoeCtrl.connect(trON7,s2c);
			shoeCtrl.connect(s2c,tr3c);
			shoeCtrl.connect(tr3c,s1);

			WorkspaceObject wo = shoeCtrl.createWorkspaceObject(950,50,"moveFromTable7");
			GCDocument moveToTable7 = wo.getSubWorkspace();
			createSubdoc(moveToTable7, 7);

			WorkspaceObject wo2 = shoeCtrl.createWorkspaceObject(950,150,"onTable7");
			GCDocument onTable7 = wo2.getSubWorkspace();
			createonTable(onTable7, 7);
		}

		if(stationVisit[9] || stationVisit[10] || stationVisit[11])
		{
			GCTransition tr2d = shoeCtrl.createTransition(1000,yPos,"currentTable==8");
			GCStep sON8 = shoeCtrl.createStep(1000,yPos+50,"on_table8","S onTable8.start=1;");
			GCTransition trON8 = shoeCtrl.createTransition(1000,yPos+150,"onTable8.leavetable");
			GCStep s2d = shoeCtrl.createStep(1000,yPos+200,"move_table8","S moveFromTable8.start=1;");
			GCTransition tr3d = shoeCtrl.createTransition(1000,yPos+300,"moveFromTable8.moveReady");

			shoeCtrl.connect(s1,tr2d);
			shoeCtrl.connect(tr2d,sON8);
			shoeCtrl.connect(sON8,trON8);
			shoeCtrl.connect(trON8,s2d);
			shoeCtrl.connect(s2d,tr3d);
			shoeCtrl.connect(tr3d,s1);

			WorkspaceObject wo = shoeCtrl.createWorkspaceObject(1075,50,"moveFromTable8");
			GCDocument moveToTable8 = wo.getSubWorkspace();
			createSubdoc(moveToTable8, 8);

			WorkspaceObject wo2 = shoeCtrl.createWorkspaceObject(1075,150,"onTable8");
			GCDocument onTable8 = wo2.getSubWorkspace();
			createonTable(onTable8, 8);
		}

		if(stationVisit[12] || stationVisit[13] || stationVisit[14] || stationVisit[15])
		{
			GCTransition tr2e = shoeCtrl.createTransition(1300,yPos,"currentTable==9");
			GCStep sON9 = shoeCtrl.createStep(1300,yPos+50,"on_table9","S onTable9.start=1;");
			GCTransition trON9 = shoeCtrl.createTransition(1300,yPos+150,"onTable9.leavetable");
			GCStep s2e = shoeCtrl.createStep(1300,yPos+200,"move_table9","S moveFromTable9.start=1;");
			GCTransition tr3e = shoeCtrl.createTransition(1300,yPos+300,"moveFromTable9.moveReady");

			shoeCtrl.connect(s1,tr2e);
			shoeCtrl.connect(tr2e,sON9);
			shoeCtrl.connect(sON9,trON9);
			shoeCtrl.connect(trON9,s2e);
			shoeCtrl.connect(s2e,tr3e);
			shoeCtrl.connect(tr3e,s1);

			WorkspaceObject wo = shoeCtrl.createWorkspaceObject(1200,50,"moveFromTable9");
			GCDocument moveToTable9 = wo.getSubWorkspace();
			createSubdoc(moveToTable9, 9);

			WorkspaceObject wo2 = shoeCtrl.createWorkspaceObject(1200,150,"onTable9");
			GCDocument onTable9 = wo2.getSubWorkspace();
			createonTable(onTable9, 9);
		}
		if(stationVisit[20])
		{
			GCTransition tr2f = shoeCtrl.createTransition(1600,yPos,"currentTable==10");
			GCStep sON10 = shoeCtrl.createStep(1600,yPos+50,"on_table10","S onTable10.start=1;");
			GCTransition trON10 = shoeCtrl.createTransition(1600,yPos+150,"onTable10.leavetable");
			GCStep s2f = shoeCtrl.createStep(1600,yPos+200,"move_table10","S moveFromTable10.start=1;");
			GCTransition tr3f = shoeCtrl.createTransition(1600,yPos+300,"moveFromTable10.moveReady");

			shoeCtrl.connect(s1,tr2f);
			shoeCtrl.connect(tr2f,sON10);
			shoeCtrl.connect(sON10,trON10);
			shoeCtrl.connect(trON10,s2f);
			shoeCtrl.connect(s2f,tr3f);
			shoeCtrl.connect(tr3f,s1);

			WorkspaceObject wo = shoeCtrl.createWorkspaceObject(1325,50,"moveFromTable10");
			GCDocument moveToTable10 = wo.getSubWorkspace();
			createSubdoc(moveToTable10, 10);

			WorkspaceObject wo2 = shoeCtrl.createWorkspaceObject(1325,150,"onTable10");
			GCDocument onTable10 = wo2.getSubWorkspace();
			createonTable(onTable10, 10);
		}
		if(stationVisit[21] || stationVisit[22] || stationVisit[23])
		{
			GCTransition tr2g = shoeCtrl.createTransition(1900,yPos,"currentTable==11");
			GCStep sON11 = shoeCtrl.createStep(1900,yPos+50,"on_table11","S onTable11.start=1;");
			GCTransition trON11 = shoeCtrl.createTransition(1900,yPos+150,"onTable11.leavetable");
			GCStep s2g = shoeCtrl.createStep(1900,yPos+200,"move_table11","S moveFromTable11.start=1;");
			GCTransition tr3g = shoeCtrl.createTransition(1900,yPos+300,"moveFromTable11.moveReady");

			shoeCtrl.connect(s1,tr2g);
			shoeCtrl.connect(tr2g,sON11);
			shoeCtrl.connect(sON11,trON11);
			shoeCtrl.connect(trON11,s2g);
			shoeCtrl.connect(s2g,tr3g);
			shoeCtrl.connect(tr3g,s1);

			WorkspaceObject wo = shoeCtrl.createWorkspaceObject(1450,50,"moveFromTable11");
			GCDocument moveToTable11 = wo.getSubWorkspace();
			createSubdoc(moveToTable11, 11);

			WorkspaceObject wo2 = shoeCtrl.createWorkspaceObject(1450,150,"onTable11");
			GCDocument onTable11 = wo2.getSubWorkspace();
			createonTable(onTable11, 11);
		}
		if(stationVisit[16] || stationVisit[17] || stationVisit[18] || stationVisit[19])
		{
			GCTransition tr2h = shoeCtrl.createTransition(2200,yPos,"currentTable==12");
			GCStep sON12 = shoeCtrl.createStep(2200,yPos+50,"on_table12","S onTable12.start=1;");
			GCTransition trON12 = shoeCtrl.createTransition(2200,yPos+150,"onTable12.leavetable");
			GCStep s2h = shoeCtrl.createStep(2200,yPos+200,"move_table12","S moveFromTable12.start=1;");
			GCTransition tr3h = shoeCtrl.createTransition(2200,yPos+300,"moveFromTable12.moveReady");

			shoeCtrl.connect(s1,tr2h);
			shoeCtrl.connect(tr2h,sON12);
			shoeCtrl.connect(sON12,trON12);
			shoeCtrl.connect(trON12,s2h);
			shoeCtrl.connect(s2h,tr3h);
			shoeCtrl.connect(tr3h,s1);

			WorkspaceObject wo = shoeCtrl.createWorkspaceObject(1575,50,"moveFromTable12");
			GCDocument moveToTable12 = wo.getSubWorkspace();
			createSubdoc(moveToTable12,12);

			WorkspaceObject wo2 = shoeCtrl.createWorkspaceObject(1575,150,"onTable12");
			GCDocument onTable12 = wo2.getSubWorkspace();
			createonTable(onTable12,12);
		}
	}

	public GCStep createTableSFC(GCDocument doc, GCTransition in, int tableNr, int x, int y, int dir)
	{
		//dir=1 when from left and dir=0 from right
		String s0text="S onStep=1;\nP nrOfRot=Shoefactory.tables.table"+tableNr+".rot;\nX onStep=0;";
		String s1text="S onStep=1;\nS Shoefactory.tables.table"+tableNr+".fullSlot.set(nrOfRot,1);\nX onStep=0;";

		if(tableNr==0)
		{
			s0text="S Shoefactory.shoeInWarehouse=1;\n"+s0text;
			s1text="S Shoefactory.shoeInWarehouse=0;\n"+s1text;
		}
		if(tableNr==1 || tableNr==2 || tableNr==3 || tableNr==4 || tableNr==5)
		{
			if(dir==0)
				s0text="P nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getRota\",\"int\",\"int\",Shoefactory.tables.table"+tableNr+".rot);";
			if(dir==0 && tableNr==5)
				s0text="P nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getRotb\",\"int\",\"int\",Shoefactory.tables.table"+tableNr+".rot);";

			s1text="S Shoefactory.Plant.agv"+(tableNr-dir)+"Slot.setVisible(0);\nS Shoefactory.agvs.agv"+(tableNr-dir)+".busy =0;\n"+s1text;
		}
		if(tableNr==6 || tableNr==7 || tableNr==8 || tableNr==9 || tableNr==11 || tableNr==12)
			s1text="S Shoefactory.Plant.agv"+(tableNr-6)+"Slot.setVisible(0);\nS Shoefactory.agvs.agv"+(tableNr-6)+".busy =0;\n"+s1text;

		if(tableNr==10)
		{
			if(dir==0)
			{
				s0text="P nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getRotc\",\"int\",\"int\",Shoefactory.tables.table"+tableNr+".rot);";
				s1text="S Shoefactory.Plant.agv6Slot.setVisible(0);\nS Shoefactory.agvs.agv6.busy =0;\n"+s1text;
			}
			else
				s1text="S Shoefactory.Plant.agv4Slot.setVisible(0);\nS Shoefactory.agvs.agv4.busy =0;\n"+s1text;
		}
		GCStep s0 = doc.createStep(x,y,"Wait"+stepID+"_table"+tableNr,s0text);
		y+=100;
		GCTransition tr0 = doc.createTransition(x,y,"JgrafSupervisor.put_T"+tableNr);
		y+=50;
		stepID++;	//only used to give a unique name to the steps and avoid warnings when compiling
		GCStep s1 = doc.createStep(x,y,"Put"+stepID+"_table"+tableNr,s1text);
		y+=100;
		stepID++;

		doc.connect(in,s0);
		doc.connect(s0,tr0);
		doc.connect(tr0,s1);

		return s1;
	}

	public void createonTable(GCDocument doc, int currentTable)
	{
		int y=0;

		BooleanVariable start = doc.createBooleanVariable(400,150,"start","0");
		BooleanVariable leave = doc.createBooleanVariable(500,150,"leavetable","0");

		IntegerVariable rotations = doc.createIntegerVariable(600,150,"rotations","0");
		IntegerVariable syncTabrot = doc.createIntegerVariable(700,150,"syncTabrot","0");

		if(currentTable==6)
		{
			IntegerVariable iv0 = doc.createIntegerVariable(400,50,"workTime0","2");
			IntegerVariable iv1 = doc.createIntegerVariable(500,50,"workTime1","2");
			IntegerVariable iv2 = doc.createIntegerVariable(600,50,"workTime2","2");
			IntegerVariable iv3 = doc.createIntegerVariable(700,50,"workTime3","2");
			IntegerVariable iv4 = doc.createIntegerVariable(800,50,"workTime4","2");

			GCStepInitial initialStep = doc.createInitialStep(100,y,"Start","P syncTabrot=Shoefactory.tables.table6.rot;");
			y+=100;
			GCTransition tr0 = doc.createTransition(100,y,"start");
			y+=50;
			GCStep s0 = doc.createStep(100,y,"S0","S start=0;\nS rotations=rotations+1;\nS syncTabrot=Shoefactory.tables.table6.rot;");
			GCTransition tr1 = doc.createTransition(300,y+40,"(rotations!="+stationRot[0] +"| rotations!="+stationRot[1] +" | rotations!="+stationRot[2] +"| rotations!="+stationRot[3] +" | rotations!="+stationRot[4] +") & syncTabrot!=Shoefactory.tables.table6.rot");
			y+=100;
			doc.connect(initialStep,tr0);
			doc.connect(tr0,s0);
			doc.connect(s0,tr1);
			doc.connect(tr1,s0);

			GCTransition tr2a = doc.createTransition(100,y,"!station0&!station1&!station2&!station3&!station4");
			GCTransition tr2b = doc.createTransition(300,y,"Shoe"+shoeNr+".station0 & rotations=="+stationRot[0] +" & !Shoefactory.stations.station0.enterStation");
			GCTransition tr2c = doc.createTransition(500,y,"Shoe"+shoeNr+".station1 & rotations=="+stationRot[1] +" & !Shoefactory.stations.station1.enterStation");
			GCTransition tr2d = doc.createTransition(700,y,"Shoe"+shoeNr+".station2 & rotations =="+stationRot[2] +" & !Shoefactory.stations.station2.enterStation");
			GCTransition tr2e = doc.createTransition(900,y,"Shoe"+shoeNr+".station3 & rotations=="+stationRot[3] +" & !Shoefactory.stations.station3.enterStation");
			GCTransition tr2f = doc.createTransition(1100,y,"Shoe"+shoeNr+".station4 &rotations =="+stationRot[4] +" & !Shoefactory.stations.station4.enterStation");
			GCTransition tr2g = doc.createTransition(1300,y,"rotations>23");
			y+=50;
			GCStep s1a = doc.createStep(100,y,"S1a","S rotations =0;\nS leavetable=1;");
			GCStep s1b = doc.createStep(300,y,"S1b","S Shoefactory.stations.station0.enterStation=1;\nS Shoefactory.tables.table6.fullSlot.set(nrOfRot,0);");
			GCStep s1c = doc.createStep(500,y,"S1c","S Shoefactory.stations.station1.enterStation=1;\nS Shoefactory.tables.table6.fullSlot.set(nrOfRot,0);");
			GCStep s1d = doc.createStep(700,y,"S1d","S Shoefactory.stations.station2.enterStation=1;\nS Shoefactory.tables.table6.fullSlot.set(nrOfRot,0);");
			GCStep s1e = doc.createStep(900,y,"S1e","S Shoefactory.stations.station3.enterStation=1;\nS Shoefactory.tables.table6.fullSlot.set(nrOfRot,0);");
			GCStep s1f = doc.createStep(1100,y,"S1f","S Shoefactory.stations.station4.enterStation=1;\nS Shoefactory.tables.table6.fullSlot.set(nrOfRot,0);");
			GCStep s1g = doc.createStep(1300,y,"S1g","S rotations =0;");
			y+=100;
			GCTransition tr3a = doc.createTransition(100,y,"1");
			GCTransition tr3b = doc.createTransition(300,y,"S1b.S>workTime0 & !Shoefactory.tables.table6.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table6.rot,\"int\","+stationRot[0] +"))");
			GCTransition tr3c = doc.createTransition(500,y,"S1c.S>workTime1 & !Shoefactory.tables.table6.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table6.rot,\"int\","+stationRot[1] +"))");
			GCTransition tr3d = doc.createTransition(700,y,"S1d.S>workTime2 & !Shoefactory.tables.table6.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table6.rot,\"int\","+stationRot[2] +"))");
			GCTransition tr3e = doc.createTransition(900,y,"S1e.S>workTime3 & !Shoefactory.tables.table6.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table6.rot,\"int\","+stationRot[3] +"))");
			GCTransition tr3f = doc.createTransition(1100,y,"S1f.S>workTime4 & !Shoefactory.tables.table6.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table6.rot,\"int\","+stationRot[4] +"))");
			GCTransition tr3g = doc.createTransition(1300,y,"1");
			y+=50;
			GCStep s2b = doc.createStep(300,y,"S2b","S Shoefactory.stations.station0.leaveStation=1;\nS station0=0;\nS Shoefactory.tables.table6.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table6.rot,\"int\","+stationRot[0] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table6.rot,\"int\","+stationRot[0] +");");
			GCStep s2c = doc.createStep(500,y,"S2c","S Shoefactory.stations.station1.leaveStation=1;\nS station1=0;\nS Shoefactory.tables.table6.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table6.rot,\"int\","+stationRot[1] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table6.rot,\"int\","+stationRot[1] +");");
			GCStep s2d = doc.createStep(700,y,"S2d","S Shoefactory.stations.station2.leaveStation=1;\nS station2=0;\nS Shoefactory.tables.table6.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table6.rot,\"int\","+stationRot[2] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table6.rot,\"int\","+stationRot[2] +");");
			GCStep s2e = doc.createStep(900,y,"S2e","S Shoefactory.stations.station3.leaveStation=1;\nS station3=0;\nS Shoefactory.tables.table6.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table6.rot,\"int\","+stationRot[3] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table6.rot,\"int\","+stationRot[3] +");");
			GCStep s2f = doc.createStep(1100,y,"S2f","S Shoefactory.stations.station4.leaveStation=1;\nS station4=0;\nS Shoefactory.tables.table6.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table6.rot,\"int\","+stationRot[4] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table6.rot,\"int\","+stationRot[4] +");");
			y+=100;
			GCTransition tr4b = doc.createTransition(300,y,"1");
			GCTransition tr4c = doc.createTransition(500,y,"1");
			GCTransition tr4d = doc.createTransition(700,y,"1");
			GCTransition tr4e = doc.createTransition(900,y,"1");
			GCTransition tr4f = doc.createTransition(1100,y,"1");
			y+=50;

			doc.connect(s0,tr2a);
			doc.connect(s0,tr2b);
			doc.connect(s0,tr2c);
			doc.connect(s0,tr2d);
			doc.connect(s0,tr2e);
			doc.connect(s0,tr2f);
			doc.connect(s0,tr2g);
			doc.connect(tr2a,s1a);
			doc.connect(tr2b,s1b);
			doc.connect(tr2c,s1c);
			doc.connect(tr2d,s1d);
			doc.connect(tr2e,s1e);
			doc.connect(tr2f,s1f);
			doc.connect(tr2g,s1g);
			doc.connect(s1a,tr3a);
			doc.connect(s1b,tr3b);
			doc.connect(s1c,tr3c);
			doc.connect(s1d,tr3d);
			doc.connect(s1e,tr3e);
			doc.connect(s1f,tr3f);
			doc.connect(s1g,tr3g);
			doc.connect(tr3a,initialStep);
			doc.connect(tr3b,s2b);
			doc.connect(tr3c,s2c);
			doc.connect(tr3d,s2d);
			doc.connect(tr3e,s2e);
			doc.connect(tr3f,s2f);
			doc.connect(tr3g,s0);
			doc.connect(s2b,tr4b);
			doc.connect(s2c,tr4c);
			doc.connect(s2d,tr4d);
			doc.connect(s2e,tr4e);
			doc.connect(s2f,tr4f);
			doc.connect(tr4b,s0);
			doc.connect(tr4c,s0);
			doc.connect(tr4d,s0);
			doc.connect(tr4e,s0);
			doc.connect(tr4f,s0);
		}

		if(currentTable==7)
		{
			IntegerVariable iv0 = doc.createIntegerVariable(400,50,"workTime5","2");
			IntegerVariable iv1 = doc.createIntegerVariable(500,50,"workTime6","2");
			IntegerVariable iv2 = doc.createIntegerVariable(600,50,"workTime7","2");
			IntegerVariable iv3 = doc.createIntegerVariable(700,50,"workTime8","2");

			GCStepInitial initialStep = doc.createInitialStep(100,y,"Start","P syncTabrot=Shoefactory.tables.table7.rot;");
			y+=100;
			GCTransition tr0 = doc.createTransition(100,y,"start");
			y+=50;
			GCStep s0 = doc.createStep(100,y,"S0","S start=0;\nS rotations=rotations+1;\nS syncTabrot=Shoefactory.tables.table7.rot;");
			GCTransition tr1 = doc.createTransition(300,y+40,"(rotations!="+stationRot[5] +" | rotations!="+stationRot[6] +" | rotations!="+stationRot[7] +" | rotations!="+stationRot[8] +") & syncTabrot!=Shoefactory.tables.table7.rot");
			y+=100;
			doc.connect(initialStep,tr0);
			doc.connect(tr0,s0);
			doc.connect(s0,tr1);
			doc.connect(tr1,s0);

			GCTransition tr2a = doc.createTransition(100,y,"!station5&!station6&!station7&!station8");
			GCTransition tr2b = doc.createTransition(300,y,"Shoe"+shoeNr+".station5 &  rotations=="+stationRot[5] +" & !Shoefactory.stations.station5.enterStation");
			GCTransition tr2c = doc.createTransition(500,y,"Shoe"+shoeNr+".station6 & rotations=="+stationRot[6] +"& !Shoefactory.stations.station6.enterStation");
			GCTransition tr2d = doc.createTransition(700,y,"Shoe"+shoeNr+".station7 & rotations =="+stationRot[7] +" & !Shoefactory.stations.station7.enterStation");
			GCTransition tr2e = doc.createTransition(900,y,"Shoe"+shoeNr+".station8 & rotations=="+stationRot[8] +" & !Shoefactory.stations.station8.enterStation");
			GCTransition tr2f = doc.createTransition(1100,y,"rotations>23");
			y+=50;
			GCStep s1a = doc.createStep(100,y,"S1a","S rotations =0;\nS leavetable=1;");
			GCStep s1b = doc.createStep(300,y,"S1b","S Shoefactory.stations.station5.enterStation=1;\nS Shoefactory.tables.table7.fullSlot.set(nrOfRot,0);");
			GCStep s1c = doc.createStep(500,y,"S1c","S Shoefactory.stations.station6.enterStation=1;\nS Shoefactory.tables.table7.fullSlot.set(nrOfRot,0);");
			GCStep s1d = doc.createStep(700,y,"S1d","S Shoefactory.stations.station7.enterStation=1;\nS Shoefactory.tables.table7.fullSlot.set(nrOfRot,0);");
			GCStep s1e = doc.createStep(900,y,"S1e","S Shoefactory.stations.station8.enterStation=1;\nS Shoefactory.tables.table7.fullSlot.set(nrOfRot,0);");
			GCStep s1f = doc.createStep(1100,y,"S1f","S rotations =0;");
			y+=100;
			GCTransition tr3a = doc.createTransition(100,y,"1");
			GCTransition tr3b = doc.createTransition(300,y,"S1b.S>workTime5 & !Shoefactory.tables.table7.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table6.rot,\"int\","+stationRot[5] +"))");
			GCTransition tr3c = doc.createTransition(500,y,"S1c.S>workTime6 & !Shoefactory.tables.table7.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table6.rot,\"int\","+stationRot[6] +"))");
			GCTransition tr3d = doc.createTransition(700,y,"S1d.S>workTime7 & !Shoefactory.tables.table7.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table6.rot,\"int\","+stationRot[7] +"))");
			GCTransition tr3e = doc.createTransition(900,y,"S1e.S>workTime8 & !Shoefactory.tables.table7.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table6.rot,\"int\","+stationRot[8] +"))");
			GCTransition tr3f = doc.createTransition(1100,y,"1");
			y+=50;
			GCStep s2b = doc.createStep(300,y,"S2b","S Shoefactory.stations.station5.leaveStation=1;\nS station5=0;\nS Shoefactory.tables.table7.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table7.rot,\"int\","+stationRot[5] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table7.rot,\"int\","+stationRot[5] +");");
			GCStep s2c = doc.createStep(500,y,"S2c","S Shoefactory.stations.station6.leaveStation=1;\nS station6=0;\nS Shoefactory.tables.table7.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table7.rot,\"int\","+stationRot[6] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table7.rot,\"int\","+stationRot[6] +");");
			GCStep s2d = doc.createStep(700,y,"S2d","S Shoefactory.stations.station7.leaveStation=1;\nS station7=0;\nS Shoefactory.tables.table7.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table7.rot,\"int\","+stationRot[7] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table7.rot,\"int\","+stationRot[7] +");");
			GCStep s2e = doc.createStep(900,y,"S2e","S Shoefactory.stations.station8.leaveStation=1;\nS station8=0;\nS Shoefactory.tables.table7.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table7.rot,\"int\","+stationRot[8] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table7.rot,\"int\","+stationRot[8] +");");

			y+=100;
			GCTransition tr4b = doc.createTransition(300,y,"1");
			GCTransition tr4c = doc.createTransition(500,y,"1");
			GCTransition tr4d = doc.createTransition(700,y,"1");
			GCTransition tr4e = doc.createTransition(900,y,"1");

			y+=50;

			doc.connect(s0,tr2a);
			doc.connect(s0,tr2b);
			doc.connect(s0,tr2c);
			doc.connect(s0,tr2d);
			doc.connect(s0,tr2e);
			doc.connect(s0,tr2f);
			doc.connect(tr2a,s1a);
			doc.connect(tr2b,s1b);
			doc.connect(tr2c,s1c);
			doc.connect(tr2d,s1d);
			doc.connect(tr2e,s1e);
			doc.connect(tr2f,s1f);
			doc.connect(s1a,tr3a);
			doc.connect(s1b,tr3b);
			doc.connect(s1c,tr3c);
			doc.connect(s1d,tr3d);
			doc.connect(s1e,tr3e);
			doc.connect(s1f,tr3f);
			doc.connect(tr3a,initialStep);
			doc.connect(tr3b,s2b);
			doc.connect(tr3c,s2c);
			doc.connect(tr3d,s2d);
			doc.connect(tr3e,s2e);
			doc.connect(tr3f,s0);
			doc.connect(s2b,tr4b);
			doc.connect(s2c,tr4c);
			doc.connect(s2d,tr4d);
			doc.connect(s2e,tr4e);

			doc.connect(tr4b,s0);
			doc.connect(tr4c,s0);
			doc.connect(tr4d,s0);
			doc.connect(tr4e,s0);
		}

		if(currentTable==8)
		{
			IntegerVariable iv0 = doc.createIntegerVariable(400,50,"workTime9","2");
			IntegerVariable iv1 = doc.createIntegerVariable(500,50,"workTime10","2");
			IntegerVariable iv2 = doc.createIntegerVariable(600,50,"workTime11","2");

			GCStepInitial initialStep = doc.createInitialStep(100,y,"Start","P syncTabrot=Shoefactory.tables.table8.rot;");
			y+=100;
			GCTransition tr0 = doc.createTransition(100,y,"start");
			y+=50;
			GCStep s0 = doc.createStep(100,y,"S0","S start=0;\nS rotations=rotations+1;\nS syncTabrot=Shoefactory.tables.table8.rot;");
			GCTransition tr1 = doc.createTransition(300,y+40,"(rotations!="+stationRot[9] +" | rotations!="+stationRot[10] +" | rotations!="+stationRot[11] +") & syncTabrot!=Shoefactory.tables.table8.rot");
			y+=100;
			doc.connect(initialStep,tr0);
			doc.connect(tr0,s0);
			doc.connect(s0,tr1);
			doc.connect(tr1,s0);

			GCTransition tr2a = doc.createTransition(100,y,"!station9&!station10&!station11");
			GCTransition tr2b = doc.createTransition(300,y,"Shoe"+shoeNr+".station9 &  rotations=="+stationRot[9] +" & !Shoefactory.stations.station9.enterStation");
			GCTransition tr2c = doc.createTransition(500,y,"Shoe"+shoeNr+".station10 & rotations=="+stationRot[10] +"& !Shoefactory.stations.station10.enterStation");
			GCTransition tr2d = doc.createTransition(700,y,"Shoe"+shoeNr+".station11 & rotations =="+stationRot[11] +" & !Shoefactory.stations.station11.enterStation");
			GCTransition tr2e = doc.createTransition(900,y,"rotations>23");
			y+=50;
			GCStep s1a = doc.createStep(100,y,"S1a","S rotations =0;\nS leavetable=1;");
			GCStep s1b = doc.createStep(300,y,"S1b","S Shoefactory.stations.station9.enterStation=1;\nS Shoefactory.tables.table8.fullSlot.set(nrOfRot,0);");
			GCStep s1c = doc.createStep(500,y,"S1c","S Shoefactory.stations.station10.enterStation=1;\nS Shoefactory.tables.table8.fullSlot.set(nrOfRot,0);");
			GCStep s1d = doc.createStep(700,y,"S1d","S Shoefactory.stations.station11.enterStation=1;\nS Shoefactory.tables.table8.fullSlot.set(nrOfRot,0);");
			GCStep s1e = doc.createStep(900,y,"S1e","S rotations =0;");
			y+=100;
			GCTransition tr3a = doc.createTransition(100,y,"1");
			GCTransition tr3b = doc.createTransition(300,y,"S1b.S>workTime9 & !Shoefactory.tables.table8.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table8.rot,\"int\","+stationRot[9] +"))");
			GCTransition tr3c = doc.createTransition(500,y,"S1c.S>workTime10 & !Shoefactory.tables.table8.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table8.rot,\"int\","+stationRot[10] +"))");
			GCTransition tr3d = doc.createTransition(700,y,"S1d.S>workTime11 & !Shoefactory.tables.table8.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table8.rot,\"int\","+stationRot[11] +"))");
			GCTransition tr3e = doc.createTransition(900,y,"1");
			y+=50;
			GCStep s2b = doc.createStep(300,y,"S2b","S Shoefactory.stations.station9.leaveStation=1;\nS station9=0;\nS Shoefactory.tables.table8.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table8.rot,\"int\","+stationRot[9] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table8.rot,\"int\","+stationRot[9] +");");
			GCStep s2c = doc.createStep(500,y,"S2c","S Shoefactory.stations.station10.leaveStation=1;\nS station10=0;\nS Shoefactory.tables.table8.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table8.rot,\"int\","+stationRot[10] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table8.rot,\"int\","+stationRot[10] +");");
			GCStep s2d = doc.createStep(700,y,"S2d","S Shoefactory.stations.station11.leaveStation=1;\nS station11=0;\nS Shoefactory.tables.table8.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table8.rot,\"int\","+stationRot[11] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table8.rot,\"int\","+stationRot[11] +");");

			y+=100;
			GCTransition tr4b = doc.createTransition(300,y,"1");
			GCTransition tr4c = doc.createTransition(500,y,"1");
			GCTransition tr4d = doc.createTransition(700,y,"1");

			y+=50;

			doc.connect(s0,tr2a);
			doc.connect(s0,tr2b);
			doc.connect(s0,tr2c);
			doc.connect(s0,tr2d);
			doc.connect(s0,tr2e);

			doc.connect(tr2a,s1a);
			doc.connect(tr2b,s1b);
			doc.connect(tr2c,s1c);
			doc.connect(tr2d,s1d);
			doc.connect(tr2e,s1e);

			doc.connect(s1a,tr3a);
			doc.connect(s1b,tr3b);
			doc.connect(s1c,tr3c);
			doc.connect(s1d,tr3d);
			doc.connect(s1e,tr3e);

			doc.connect(tr3a,initialStep);
			doc.connect(tr3b,s2b);
			doc.connect(tr3c,s2c);
			doc.connect(tr3d,s2d);
			doc.connect(tr3e,s0);

			doc.connect(s2b,tr4b);
			doc.connect(s2c,tr4c);
			doc.connect(s2d,tr4d);

			doc.connect(tr4b,s0);
			doc.connect(tr4c,s0);
			doc.connect(tr4d,s0);
		}

		if(currentTable==9)
		{
			IntegerVariable iv0 = doc.createIntegerVariable(400,50,"workTime12","2");
			IntegerVariable iv1 = doc.createIntegerVariable(500,50,"workTime13","2");
			IntegerVariable iv2 = doc.createIntegerVariable(600,50,"workTime14","2");
			IntegerVariable iv3 = doc.createIntegerVariable(700,50,"workTime15","2");

			GCStepInitial initialStep = doc.createInitialStep(100,y,"Start","P syncTabrot=Shoefactory.tables.table9.rot;");
			y+=100;
			GCTransition tr0 = doc.createTransition(100,y,"start");
			y+=50;
			GCStep s0 = doc.createStep(100,y,"S0","S start=0;\nS rotations=rotations+1;\nS syncTabrot=Shoefactory.tables.table9.rot;");
			GCTransition tr1 = doc.createTransition(300,y+40,"(rotations!="+stationRot[12] +" | rotations!="+stationRot[13] +" | rotations!="+stationRot[14] +" | rotations!="+stationRot[15] +") & syncTabrot!=Shoefactory.tables.table9.rot");
			y+=100;
			doc.connect(initialStep,tr0);
			doc.connect(tr0,s0);
			doc.connect(s0,tr1);
			doc.connect(tr1,s0);

			GCTransition tr2a = doc.createTransition(100,y,"!station12&!station13&!station14&!station15");
			GCTransition tr2b = doc.createTransition(300,y,"Shoe"+shoeNr+".station12 &  rotations=="+stationRot[12] +" & !Shoefactory.stations.station12.enterStation");
			GCTransition tr2c = doc.createTransition(500,y,"Shoe"+shoeNr+".station13 & rotations=="+stationRot[13] +"& !Shoefactory.stations.station13.enterStation");
			GCTransition tr2d = doc.createTransition(700,y,"Shoe"+shoeNr+".station14 & rotations =="+stationRot[14] +" & !Shoefactory.stations.station14.enterStation");
			GCTransition tr2e = doc.createTransition(900,y,"Shoe"+shoeNr+".station15 & rotations=="+stationRot[15] +" & !Shoefactory.stations.station15.enterStation");
			GCTransition tr2f = doc.createTransition(1100,y,"rotations >23");

			y+=50;
			GCStep s1a = doc.createStep(100,y,"S1a","S rotations =0;\nS leavetable=1;");
			GCStep s1b = doc.createStep(300,y,"S1b","S Shoefactory.stations.station12.enterStation=1;\nS Shoefactory.tables.table9.fullSlot.set(nrOfRot,0);");
			GCStep s1c = doc.createStep(500,y,"S1c","S Shoefactory.stations.station13.enterStation=1;\nS Shoefactory.tables.table9.fullSlot.set(nrOfRot,0);");
			GCStep s1d = doc.createStep(700,y,"S1d","S Shoefactory.stations.station14.enterStation=1;\nS Shoefactory.tables.table9.fullSlot.set(nrOfRot,0);");
			GCStep s1e = doc.createStep(900,y,"S1e","S Shoefactory.stations.station15.enterStation=1;\nS Shoefactory.tables.table9.fullSlot.set(nrOfRot,0);");
			GCStep s1f = doc.createStep(1100,y,"S1f","S rotations =0;");
			y+=100;
			GCTransition tr3a = doc.createTransition(100,y,"1");
			GCTransition tr3b = doc.createTransition(300,y,"S1b.S>workTime12 & !Shoefactory.tables.table9.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table6.rot,\"int\","+stationRot[12] +"))");
			GCTransition tr3c = doc.createTransition(500,y,"S1c.S>workTime13 & !Shoefactory.tables.table9.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table6.rot,\"int\","+stationRot[13] +"))");
			GCTransition tr3d = doc.createTransition(700,y,"S1d.S>workTime14 & !Shoefactory.tables.table9.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table6.rot,\"int\","+stationRot[14] +"))");
			GCTransition tr3e = doc.createTransition(900,y,"S1e.S>workTime15 & !Shoefactory.tables.table9.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table6.rot,\"int\","+stationRot[15] +"))");
			GCTransition tr3f = doc.createTransition(1100,y,"1");
			y+=50;
			GCStep s2b = doc.createStep(300,y,"S2b","S Shoefactory.stations.station12.leaveStation=1;\nS station12=0;\nS Shoefactory.tables.table9.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table9.rot,\"int\","+stationRot[12] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table9.rot,\"int\","+stationRot[12] +");");
			GCStep s2c = doc.createStep(500,y,"S2c","S Shoefactory.stations.station13.leaveStation=1;\nS station13=0;\nS Shoefactory.tables.table9.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table9.rot,\"int\","+stationRot[13] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table9.rot,\"int\","+stationRot[13] +");");
			GCStep s2d = doc.createStep(700,y,"S2d","S Shoefactory.stations.station14.leaveStation=1;\nS station14=0;\nS Shoefactory.tables.table9.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table9.rot,\"int\","+stationRot[14] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table9.rot,\"int\","+stationRot[14] +");");
			GCStep s2e = doc.createStep(900,y,"S2e","S Shoefactory.stations.station15.leaveStation=1;\nS station15=0;\nS Shoefactory.tables.table9.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table9.rot,\"int\","+stationRot[15] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table9.rot,\"int\","+stationRot[15] +");");

			y+=100;
			GCTransition tr4b = doc.createTransition(300,y,"1");
			GCTransition tr4c = doc.createTransition(500,y,"1");
			GCTransition tr4d = doc.createTransition(700,y,"1");
			GCTransition tr4e = doc.createTransition(900,y,"1");

			y+=50;

			doc.connect(s0,tr2a);
			doc.connect(s0,tr2b);
			doc.connect(s0,tr2c);
			doc.connect(s0,tr2d);
			doc.connect(s0,tr2e);
			doc.connect(s0,tr2f);

			doc.connect(tr2a,s1a);
			doc.connect(tr2b,s1b);
			doc.connect(tr2c,s1c);
			doc.connect(tr2d,s1d);
			doc.connect(tr2e,s1e);
			doc.connect(tr2f,s1f);

			doc.connect(s1a,tr3a);
			doc.connect(s1b,tr3b);
			doc.connect(s1c,tr3c);
			doc.connect(s1d,tr3d);
			doc.connect(s1e,tr3e);
			doc.connect(s1f,tr3f);

			doc.connect(tr3a,initialStep);
			doc.connect(tr3b,s2b);
			doc.connect(tr3c,s2c);
			doc.connect(tr3d,s2d);
			doc.connect(tr3e,s2e);
			doc.connect(tr3f,s0);

			doc.connect(s2b,tr4b);
			doc.connect(s2c,tr4c);
			doc.connect(s2d,tr4d);
			doc.connect(s2e,tr4e);

			doc.connect(tr4b,s0);
			doc.connect(tr4c,s0);
			doc.connect(tr4d,s0);
			doc.connect(tr4e,s0);
		}

		if(currentTable==10)
		{
			IntegerVariable iv0 = doc.createIntegerVariable(400,50,"workTime20","2");
			IntegerVariable iv1 = doc.createIntegerVariable(400,50,"from12","0");

			GCStepInitial initialStep = doc.createInitialStep(100,y,"Start","P syncTabrot=Shoefactory.tables.table10.rot;");
			y+=100;
			GCTransition tr0 = doc.createTransition(100,y,"start");
			y+=50;
		//	GCStep s0a = doc.createStep(100,y,"S0a","S rotations =12");
		//	y+=100;
			GCStep s0 = doc.createStep(100,y,"S0","S start=0;\nS rotations=rotations+1;\nS syncTabrot=Shoefactory.tables.table10.rot;");
			GCTransition tr1 = doc.createTransition(300,y+40,"(rotations!=("+stationRot[20]+"-from12)" +") & syncTabrot!=Shoefactory.tables.table10.rot");
			y+=100;
			doc.connect(initialStep,tr0);
			doc.connect(tr0,s0);
			doc.connect(s0,tr1);
			doc.connect(tr1,s0);

			GCTransition tr2a = doc.createTransition(100,y,"!station20");
			GCTransition tr2b = doc.createTransition(300,y,"Shoe"+shoeNr+".station20 &  rotations==("+stationRot[20] +"-from12) & !Shoefactory.stations.station20.enterStation");
			GCTransition tr2c = doc.createTransition(500,y,"rotations>23");
			y+=50;
			GCStep s1a = doc.createStep(100,y,"S1a","S rotations =0;\nS leavetable=1;");
			GCStep s1b = doc.createStep(300,y,"S1b","S Shoefactory.stations.station20.enterStation=1;\nS Shoefactory.tables.table10.fullSlot.set(nrOfRot,0);");
			GCStep s1c = doc.createStep(500,y,"S1c","S rotations =0;");
			y+=100;
			GCTransition tr3a = doc.createTransition(100,y,"1");
			GCTransition tr3b = doc.createTransition(300,y,"S1b.S>workTime20 & !Shoefactory.tables.table10.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table10.rot,\"int\","+stationRot[20] +"))");
			GCTransition tr3c = doc.createTransition(500,y,"1");
			y+=50;
			GCStep s2b = doc.createStep(300,y,"S2b","S Shoefactory.stations.station20.leaveStation=1;\n S from12 =0;\nS station20=0;\nS Shoefactory.tables.table10.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table10.rot,\"int\","+stationRot[20] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table10.rot,\"int\","+stationRot[20] +");");
			y+=100;
			GCTransition tr4b = doc.createTransition(300,y,"1");
			y+=50;

			doc.connect(s0,tr2a);
			doc.connect(s0,tr2b);
			doc.connect(s0,tr2c);

			doc.connect(tr2a,s1a);
			doc.connect(tr2b,s1b);
			doc.connect(tr2c,s1c);

			doc.connect(s1a,tr3a);
			doc.connect(s1b,tr3b);
			doc.connect(s1c,tr3c);

			doc.connect(tr3a,initialStep);
			doc.connect(tr3b,s2b);
			doc.connect(tr3c,s0);

			doc.connect(s2b,tr4b);

			doc.connect(tr4b,s0);
		}

		if(currentTable==11)
		{
				IntegerVariable iv0 = doc.createIntegerVariable(400,50,"workTime21","2");
				IntegerVariable iv1 = doc.createIntegerVariable(500,50,"workTime22","2");
				IntegerVariable iv2 = doc.createIntegerVariable(600,50,"workTime23","2");

				GCStepInitial initialStep = doc.createInitialStep(100,y,"Start","P syncTabrot=Shoefactory.tables.table11.rot;");
				y+=100;
				GCTransition tr0 = doc.createTransition(100,y,"start");
				y+=50;
				GCStep s0 = doc.createStep(100,y,"S0","S start=0;\nS rotations=rotations+1;\nS syncTabrot=Shoefactory.tables.table11.rot;");
				GCTransition tr1 = doc.createTransition(300,y+40,"(rotations!="+stationRot[21] +" | rotations!="+stationRot[22] +" | rotations!="+stationRot[23] +") & syncTabrot!=Shoefactory.tables.table11.rot");
				y+=100;
				doc.connect(initialStep,tr0);
				doc.connect(tr0,s0);
				doc.connect(s0,tr1);
				doc.connect(tr1,s0);

				GCTransition tr2a = doc.createTransition(100,y,"!station21&!station22&!station23");
				GCTransition tr2b = doc.createTransition(300,y,"Shoe"+shoeNr+".station21 &  rotations=="+stationRot[21] +" & !Shoefactory.stations.station21.enterStation");
				GCTransition tr2c = doc.createTransition(500,y,"Shoe"+shoeNr+".station22 & rotations=="+stationRot[22] +"& !Shoefactory.stations.station22.enterStation");
				GCTransition tr2d = doc.createTransition(700,y,"Shoe"+shoeNr+".station23 & rotations =="+stationRot[23] +" & !Shoefactory.stations.station23.enterStation");
				GCTransition tr2e = doc.createTransition(900,y,"rotations>23");
				y+=50;
				GCStep s1a = doc.createStep(100,y,"S1a","S rotations =0;\nS leavetable=1;");
				GCStep s1b = doc.createStep(300,y,"S1b","S Shoefactory.stations.station21.enterStation=1;\nS Shoefactory.tables.table11.fullSlot.set(nrOfRot,0);");
				GCStep s1c = doc.createStep(500,y,"S1c","S Shoefactory.stations.station22.enterStation=1;\nS Shoefactory.tables.table11.fullSlot.set(nrOfRot,0);");
				GCStep s1d = doc.createStep(700,y,"S1d","S Shoefactory.stations.station23.enterStation=1;\nS Shoefactory.tables.table11.fullSlot.set(nrOfRot,0);");
				GCStep s1e = doc.createStep(900,y,"S1e","S rotations =0;");
				y+=100;
				GCTransition tr3a = doc.createTransition(100,y,"1");
				GCTransition tr3b = doc.createTransition(300,y,"S1b.S>workTime21 & !Shoefactory.tables.table11.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table11.rot,\"int\","+stationRot[21] +"))");
				GCTransition tr3c = doc.createTransition(500,y,"S1c.S>workTime22 & !Shoefactory.tables.table11.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table11.rot,\"int\","+stationRot[22] +"))");
				GCTransition tr3d = doc.createTransition(700,y,"S1d.S>workTime23 & !Shoefactory.tables.table11.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table11.rot,\"int\","+stationRot[23] +"))");
				GCTransition tr3e = doc.createTransition(900,y,"1");
				y+=50;
				GCStep s2b = doc.createStep(300,y,"S2b","S Shoefactory.stations.station21.leaveStation=1;\nS station21=0;\nS Shoefactory.tables.table11.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table11.rot,\"int\","+stationRot[21] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table11.rot,\"int\","+stationRot[21] +");");
				GCStep s2c = doc.createStep(500,y,"S2c","S Shoefactory.stations.station22.leaveStation=1;\nS station22=0;\nS Shoefactory.tables.table11.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table11.rot,\"int\","+stationRot[22] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table11.rot,\"int\","+stationRot[22] +");");
				GCStep s2d = doc.createStep(700,y,"S2d","S Shoefactory.stations.station23.leaveStation=1;\nS station23=0;\nS Shoefactory.tables.table11.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table11.rot,\"int\","+stationRot[23] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table11.rot,\"int\","+stationRot[23] +");");
				y+=100;
				GCTransition tr4b = doc.createTransition(300,y,"1");
				GCTransition tr4c = doc.createTransition(500,y,"1");
				GCTransition tr4d = doc.createTransition(700,y,"1");
				y+=50;

				doc.connect(s0,tr2a);
				doc.connect(s0,tr2b);
				doc.connect(s0,tr2c);
				doc.connect(s0,tr2d);
				doc.connect(s0,tr2e);

				doc.connect(tr2a,s1a);
				doc.connect(tr2b,s1b);
				doc.connect(tr2c,s1c);
				doc.connect(tr2d,s1d);
				doc.connect(tr2e,s1e);

				doc.connect(s1a,tr3a);
				doc.connect(s1b,tr3b);
				doc.connect(s1c,tr3c);
				doc.connect(s1d,tr3d);
				doc.connect(s1e,tr3e);

				doc.connect(tr3a,initialStep);
				doc.connect(tr3b,s2b);
				doc.connect(tr3c,s2c);
				doc.connect(tr3d,s2d);
				doc.connect(tr3e,s0);

				doc.connect(s2b,tr4b);
				doc.connect(s2c,tr4c);
				doc.connect(s2d,tr4d);

				doc.connect(tr4b,s0);
				doc.connect(tr4c,s0);
				doc.connect(tr4d,s0);
		}

		if(currentTable==12)
		{
			IntegerVariable iv0 = doc.createIntegerVariable(400,50,"workTime16","2");
			IntegerVariable iv1 = doc.createIntegerVariable(500,50,"workTime17","2");
			IntegerVariable iv2 = doc.createIntegerVariable(600,50,"workTime18","2");
			IntegerVariable iv3 = doc.createIntegerVariable(700,50,"workTime19","2");

			GCStepInitial initialStep = doc.createInitialStep(100,y,"Start","P syncTabrot=Shoefactory.tables.table12.rot;");
			y+=100;
			GCTransition tr0 = doc.createTransition(100,y,"start");
			y+=50;
			GCStep s0 = doc.createStep(100,y,"S0","S start=0;\nS rotations=rotations+1;\nS syncTabrot=Shoefactory.tables.table12.rot;");
			GCTransition tr1 = doc.createTransition(300,y+40,"(rotations!="+stationRot[16] +" | rotations!="+stationRot[17] +" | rotations!="+stationRot[18] +" | rotations!="+stationRot[19] +") & syncTabrot!=Shoefactory.tables.table12.rot");
			y+=100;
			doc.connect(initialStep,tr0);
			doc.connect(tr0,s0);
			doc.connect(s0,tr1);
			doc.connect(tr1,s0);

			GCTransition tr2a = doc.createTransition(100,y,"!station16&!station17&!station18&!station19");
			GCTransition tr2b = doc.createTransition(300,y,"Shoe"+shoeNr+".station16 &  rotations=="+stationRot[16] +" & !Shoefactory.stations.station16.enterStation");
			GCTransition tr2c = doc.createTransition(500,y,"Shoe"+shoeNr+".station17 & rotations=="+stationRot[17] +"& !Shoefactory.stations.station17.enterStation");
			GCTransition tr2d = doc.createTransition(700,y,"Shoe"+shoeNr+".station18 & rotations =="+stationRot[18] +" & !Shoefactory.stations.station18.enterStation");
			GCTransition tr2e = doc.createTransition(900,y,"Shoe"+shoeNr+".station19 & rotations=="+stationRot[19] +" & !Shoefactory.stations.station19.enterStation");
			GCTransition tr2f = doc.createTransition(1100,y,"rotations>23");
			y+=50;
			GCStep s1a = doc.createStep(100,y,"S1a","S rotations =0;\nS leavetable=1;");
			GCStep s1b = doc.createStep(300,y,"S1b","S Shoefactory.stations.station16.enterStation=1;\nS Shoefactory.tables.table12.fullSlot.set(nrOfRot,0);");
			GCStep s1c = doc.createStep(500,y,"S1c","S Shoefactory.stations.station17.enterStation=1;\nS Shoefactory.tables.table12.fullSlot.set(nrOfRot,0);");
			GCStep s1d = doc.createStep(700,y,"S1d","S Shoefactory.stations.station18.enterStation=1;\nS Shoefactory.tables.table12.fullSlot.set(nrOfRot,0);");
			GCStep s1e = doc.createStep(900,y,"S1e","S Shoefactory.stations.station19.enterStation=1;\nS Shoefactory.tables.table12.fullSlot.set(nrOfRot,0);");
			GCStep s1f = doc.createStep(1100,y,"S1f","S rotations =0;");
			y+=100;
			GCTransition tr3a = doc.createTransition(100,y,"1");
			GCTransition tr3b = doc.createTransition(300,y,"S1b.S>workTime16 & !Shoefactory.tables.table12.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table12.rot,\"int\","+stationRot[16] +"))");
			GCTransition tr3c = doc.createTransition(500,y,"S1c.S>workTime17 & !Shoefactory.tables.table12.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table12.rot,\"int\","+stationRot[17] +"))");
			GCTransition tr3d = doc.createTransition(700,y,"S1d.S>workTime18 & !Shoefactory.tables.table12.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table12.rot,\"int\","+stationRot[18] +"))");
			GCTransition tr3e = doc.createTransition(900,y,"S1e.S>workTime19 & !Shoefactory.tables.table12.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table12.rot,\"int\","+stationRot[19] +"))");
			GCTransition tr3f = doc.createTransition(1100,y,"1");
			y+=50;
			GCStep s2b = doc.createStep(300,y,"S2b","S Shoefactory.stations.station16.leaveStation=1;\nS station16=0;\nS Shoefactory.tables.table12.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table12.rot,\"int\","+stationRot[16] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table12.rot,\"int\","+stationRot[16] +");");
			GCStep s2c = doc.createStep(500,y,"S2c","S Shoefactory.stations.station17.leaveStation=1;\nS station17=0;\nS Shoefactory.tables.table12.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table12.rot,\"int\","+stationRot[17] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table12.rot,\"int\","+stationRot[17] +");");
			GCStep s2d = doc.createStep(700,y,"S2d","S Shoefactory.stations.station18.leaveStation=1;\nS station18=0;\nS Shoefactory.tables.table12.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table12.rot,\"int\","+stationRot[18] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table12.rot,\"int\","+stationRot[18] +");");
			GCStep s2e = doc.createStep(900,y,"S2e","S Shoefactory.stations.station19.leaveStation=1;\nS station19=0;\nS Shoefactory.tables.table12.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table12.rot,\"int\","+stationRot[19] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table12.rot,\"int\","+stationRot[19] +");");
			y+=100;
			GCTransition tr4b = doc.createTransition(300,y,"1");
			GCTransition tr4c = doc.createTransition(500,y,"1");
			GCTransition tr4d = doc.createTransition(700,y,"1");
			GCTransition tr4e = doc.createTransition(900,y,"1");
			y+=50;

			doc.connect(s0,tr2a);
			doc.connect(s0,tr2b);
			doc.connect(s0,tr2c);
			doc.connect(s0,tr2d);
			doc.connect(s0,tr2e);
			doc.connect(s0,tr2f);

			doc.connect(tr2a,s1a);
			doc.connect(tr2b,s1b);
			doc.connect(tr2c,s1c);
			doc.connect(tr2d,s1d);
			doc.connect(tr2e,s1e);
			doc.connect(tr2f,s1f);

			doc.connect(s1a,tr3a);
			doc.connect(s1b,tr3b);
			doc.connect(s1c,tr3c);
			doc.connect(s1d,tr3d);
			doc.connect(s1e,tr3e);
			doc.connect(s1f,tr3f);

			doc.connect(tr3a,initialStep);
			doc.connect(tr3b,s2b);
			doc.connect(tr3c,s2c);
			doc.connect(tr3d,s2d);
			doc.connect(tr3e,s2e);
			doc.connect(tr3f,s0);

			doc.connect(s2b,tr4b);
			doc.connect(s2c,tr4c);
			doc.connect(s2d,tr4d);
			doc.connect(s2e,tr4e);

			doc.connect(tr4b,s0);
			doc.connect(tr4c,s0);
			doc.connect(tr4d,s0);
			doc.connect(tr4e,s0);
		}
	}

	public void createSubdoc(GCDocument doc, int currentTable)
	{
		int y=0;

		//create subdoc
		BooleanVariable bv1 = doc.createBooleanVariable(500,50,"start","0");
		BooleanVariable bv2 = doc.createBooleanVariable(600,50,"moveReady","0");
		IntegerVariable iv1 = doc.createIntegerVariable(700,50,"goto","0");

		GCStepInitial initialStep = doc.createInitialStep(100,y,"Start","S moveReady=0;");
		y+=100;
		GCTransition tr0 = doc.createTransition(100,y,"start");
		y+=50;
		GCStep s0 = doc.createStep(100,y,"S0","S start=0;\nS goto=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"askUser\",\"int\",\"int\","+shoeNr+",\"int\",currentTable,\"boolean\",station0,\"boolean\",station1,\"boolean\",station2,\"boolean\",station3,\"boolean\",station4,"
												+"\"boolean\",station5,\"boolean\",station6,\"boolean\",station7,\"boolean\",station8,\"boolean\",station9,\"boolean\",station10,\"boolean\",station11,\"boolean\",station12,\"boolean\",station13,"
												+"\"boolean\",station14,\"boolean\",station15,\"boolean\",station16,\"boolean\",station17,\"boolean\",station18,\"boolean\",station19,\"boolean\",station20,\"boolean\",station21,\"boolean\",station22,\"boolean\",station23);");
		y+=100;
		doc.connect(initialStep,tr0);
		doc.connect(tr0,s0);

		if(currentTable==0)
		{
			GCTransition tr02b = doc.createTransition(400,y,"goto==6 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table0.rot)==6 &! Shoefactory.agvs.agv0.busy");
			GCTransition tr02c = doc.createTransition(700,y,"(goto==7 | goto==8 |goto==9 |goto==10 |goto==11 |goto==12)  & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table0.rot)==6&! Shoefactory.agvs.agv0.busy");
			y+=50;
			GCStep s02b = doc.createStep(400,y,"move_table6","S Shoefactory.tables.table0.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv0.from0to6=1;");
			GCStep s02c = doc.createStep(700,y,"move_table7","S Shoefactory.tables.table0.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv0.from0to1=1;");
			y+=100;
			GCTransition tr2b = doc.createTransition(400,y,"Shoefactory.agvs.agv0.atgoal" );
			GCTransition tr2c = doc.createTransition(700,y,"Shoefactory.agvs.agv0.atgoal");
			y+=50;
			GCStep s2b = doc.createStep(400,y,"S_toTable6_end","P nrOfRot=Shoefactory.tables.table6.rot;");
			GCStep s2c = createTableSFC(doc,tr2c,1,700,y,1);
			y+=250;
			GCTransition tr3b = doc.createTransition(400,y,"S_toTable6_end.t>Shoefactory.time & !Shoefactory.tables.table6.fullSlot.get(nrOfRot) ");
			GCTransition tr3c = doc.createTransition(700,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table1.rot)==6&! Shoefactory.agvs.agv1.busy & goto==7 & JgrafSupervisor.get_T1 ");
			GCTransition tr3d = doc.createTransition(1000,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table1.rot)==6&! Shoefactory.agvs.agv1.busy &(goto==8|goto==9|goto==10|goto==11|goto==11|goto==12) & JgrafSupervisor.get_T1");
			y+=50;
			GCStep s3b = doc.createStep(400,y,"S_atTable6","S currentTable = 6;\nS Shoefactory.tables.table6.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv0.busy=0;\nS moveReady=1;");
			GCStep s3c = doc.createStep(700,y,"S_toTable7_b","S Shoefactory.tables.table1.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv1.from1to7=1;");
			GCStep s3d = doc.createStep(1000,y,"S_toTable8_b","S Shoefactory.tables.table1.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv1.from1to2=1  ;");
			y+=100;
			GCTransition tr4c = doc.createTransition(700,y,"Shoefactory.agvs.agv1.atgoal");
			GCTransition tr4d = doc.createTransition(1000,y,"Shoefactory.agvs.agv1.atgoal");
			y+=50;
			GCStep s4c = doc.createStep(700,y,"S_toTable7_end","P nrOfRot=Shoefactory.tables.table7.rot;");
			GCStep s4d = createTableSFC(doc,tr4d,2,1000,y,1);
			y+=250;
			GCTransition tr5c = doc.createTransition(700,y,"S_toTable7_end.t>Shoefactory.time & !Shoefactory.tables.table7.fullSlot.get(nrOfRot) ");
			GCTransition tr5d = doc.createTransition(1000,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table2.rot)==6&! Shoefactory.agvs.agv2.busy &goto==8");
			GCTransition tr5e = doc.createTransition(1300,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table2.rot)==6&! Shoefactory.agvs.agv2.busy &(goto==9| goto==10 |goto==11 |goto==12) ");
			y+=50;
			GCStep s5c = doc.createStep(700,y,"S_atTable7","S currentTable = 7;\nS Shoefactory.tables.table7.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv1.busy=0;\nS moveReady=1;");
			GCStep s5d = doc.createStep(1000,y,"S_toTable8_c","S Shoefactory.tables.table2.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv2.from2to8=1;");
			GCStep s5e = doc.createStep(1300,y,"S_toTable9_c","S Shoefactory.tables.table2.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv2.from2to3=1;");
			y+=100;
			GCTransition tr6d = doc.createTransition(1000,y,"Shoefactory.agvs.agv2.atgoal");
			GCTransition tr6e = doc.createTransition(1300,y,"Shoefactory.agvs.agv2.atgoal");
			y+=50;
			GCStep s6d = doc.createStep(1000,y,"S_toTable8_end","P nrOfRot=Shoefactory.tables.table8.rot;");
			GCStep s6e = createTableSFC(doc,tr6e,3,1300,y,1);
			y+=250;
			GCTransition tr7d = doc.createTransition(1000,y,"S_toTable8_end.t>Shoefactory.time & !Shoefactory.tables.table8.fullSlot.get(nrOfRot)");
			GCTransition tr7e = doc.createTransition(1300,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table3.rot)==6&! Shoefactory.agvs.agv3.busy & goto==9");
			GCTransition tr7f = doc.createTransition(1600,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table3.rot)==6&! Shoefactory.agvs.agv3.busy& (goto==10 |goto==11|goto==12)");
			y+=50;
			GCStep s7d = doc.createStep(1000,y,"S_atTable8","S currentTable = 8;\nS Shoefactory.tables.table8.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv2.busy=0;\nS moveReady=1;");
			GCStep s7e = doc.createStep(1300,y,"S_toTable9_d","S Shoefactory.tables.table3.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv3.from3to9=1;");
			GCStep s7f = doc.createStep(1600,y,"S_toTable10_d","S Shoefactory.tables.table3.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv3.from3to4=1;");
			y+=100;
			GCTransition tr8e = doc.createTransition(1300,y,"Shoefactory.agvs.agv3.atgoal");
			GCTransition tr8f = doc.createTransition(1600,y,"Shoefactory.agvs.agv3.atgoal");
			y+=50;
			GCStep s8e = doc.createStep(1300,y,"S_toTable9_end","P nrOfRot=Shoefactory.tables.table9.rot;");
			GCStep s8f = createTableSFC(doc,tr8f,4,1600,y,1);
			y+=250;
			GCTransition tr9e = doc.createTransition(1300,y,"S_toTable9_end.t>Shoefactory.time & !Shoefactory.tables.table9.fullSlot.get(nrOfRot)");
			GCTransition tr9f = doc.createTransition(1600,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table4.rot)==6&! Shoefactory.agvs.agv4.busy & goto==10");
			GCTransition tr9g = doc.createTransition(1900,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table4.rot)==6&! Shoefactory.agvs.agv4.busy &goto==11 ");
			GCTransition tr9h = doc.createTransition(2200,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table4.rot)==6&! Shoefactory.agvs.agv4.busy &goto==12");
			y+=50;
			GCStep s9e = doc.createStep(1300,y,"S_atTable9","S currentTable = 9;\nS Shoefactory.tables.table9.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv3.busy=0;\nS moveReady=1;");
			GCStep s9f = doc.createStep(1600,y,"S_toTable10_e","S Shoefactory.tables.table4.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv4.from4to10=1;");
			GCStep s9g = doc.createStep(1900,y,"S_toTable11_e","S Shoefactory.tables.table4.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv4.from4to5=1;");
			GCStep s9h = doc.createStep(2200,y,"S_toTable12_e","S Shoefactory.tables.table4.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv4.from4to10=1;");
			y+=100;
			GCTransition tr10f = doc.createTransition(1600,y,"Shoefactory.agvs.agv4.atgoal");
			GCTransition tr10g = doc.createTransition(1900,y,"Shoefactory.agvs.agv4.atgoal");
			GCTransition tr10h = doc.createTransition(2200,y,"Shoefactory.agvs.agv4.atgoal");
			y+=50;
			GCStep s10f = doc.createStep(1600,y,"S_toTable10_end","P nrOfRot=Shoefactory.tables.table10.rot;");
			GCStep s10g = createTableSFC(doc,tr10g,5,1900,y,1);
			GCStep s10h = createTableSFC(doc,tr10h,10,2200,y,1);
			y+=250;
			GCTransition tr11f = doc.createTransition(1600,y,"S_toTable10_end.t>Shoefactory.time & !Shoefactory.tables.table10.fullSlot.get(nrOfRot)");
			GCTransition tr11g = doc.createTransition(1900,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",applyStaticMethod(\"org.supremica.external.shoefactory.Executor.FactoryExecutor\",\"fiveRot\",\"int\",\"int\",nrOfRot)-Shoefactory.tables.table5.rot)==9&! Shoefactory.agvs.agv5.busy");
			GCTransition tr11h = doc.createTransition(2200,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table10.rot)==12&! Shoefactory.agvs.agv6.busy");
			y+=50;
			GCStep s11f = doc.createStep(1600,y,"S_atTable10","S currentTable = 10;\nS Shoefactory.tables.table10.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv4.busy=0;\nS moveReady=1;");
			GCStep s11g = doc.createStep(1900,y,"S_toTable11_f","S Shoefactory.tables.table5.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv5.from5to11=1;");
			GCStep s11h = doc.createStep(2200,y,"S_toTable12_f","S Shoefactory.tables.table10.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv6.from10to12=1;");
			y+=100;
			GCTransition tr12g = doc.createTransition(1900,y,"Shoefactory.agvs.agv5.atgoal");
			GCTransition tr12h = doc.createTransition(2200,y,"Shoefactory.agvs.agv6.atgoal");
			y+=50;
			GCStep s12g = doc.createStep(1900,y,"S_toTable11_end","P nrOfRot=Shoefactory.tables.table11.rot;");
			GCStep s12h = doc.createStep(2200,y,"S_toTable12_end","P nrOfRot=Shoefactory.tables.table12.rot;");
			y+=100;
			GCTransition tr13g = doc.createTransition(1900,y,"S_toTable11_end.t>Shoefactory.time & !Shoefactory.tables.table11.fullSlot.get(nrOfRot)");
			GCTransition tr13h = doc.createTransition(2200,y,"S_toTable12_end.t>Shoefactory.time & !Shoefactory.tables.table12.fullSlot.get(nrOfRot)");
			y+=50;
			GCStep s13g = doc.createStep(1900,y,"S_atTable11","S currentTable = 11;\nS Shoefactory.tables.table11.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv5.busy=0;\nS moveReady=1;");
			GCStep s13h = doc.createStep(2200,y,"S_atTable12","S currentTable = 12;\nS Shoefactory.tables.table12.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv6.busy=0;\nS moveReady=1;");
			y+=250;
			GCTransition trEndb = doc.createTransition(400,y,"1");
			GCTransition trEndc = doc.createTransition(700,y,"1");
			GCTransition trEndd = doc.createTransition(1000,y,"1");
			GCTransition trEnde = doc.createTransition(1300,y,"1");
			GCTransition trEndf = doc.createTransition(1600,y,"1");
			GCTransition trEndg = doc.createTransition(1900,y,"1");
			GCTransition trEndh = doc.createTransition(2200,y,"1");
			y+=50;

			doc.connect(s0,tr02b);
			doc.connect(s0,tr02c);

			doc.connect(tr02b,s02b);
			doc.connect(tr02c,s02c);

			doc.connect(s02b,tr2b);
			doc.connect(s02c,tr2c);

			doc.connect(tr2b,s2b);

			doc.connect(s2b,tr3b);
			doc.connect(s2c,tr3c);
			doc.connect(s2c,tr3d);

			doc.connect(tr3b,s3b);
			doc.connect(tr3c,s3c);
			doc.connect(tr3d,s3d);

			doc.connect(s3c,tr4c);
			doc.connect(s3d,tr4d);

			doc.connect(tr4c,s4c);

			doc.connect(s4c,tr5c);
			doc.connect(s4d,tr5d);
			doc.connect(s4d,tr5e);

			doc.connect(tr5c,s5c);
			doc.connect(tr5d,s5d);
			doc.connect(tr5e,s5e);

			doc.connect(s5d,tr6d);
			doc.connect(s5e,tr6e);

			doc.connect(tr6d,s6d);

			doc.connect(s6d,tr7d);
			doc.connect(s6e,tr7e);
			doc.connect(s6e,tr7f);

			doc.connect(tr7d,s7d);
			doc.connect(tr7e,s7e);
			doc.connect(tr7f,s7f);

			doc.connect(s7e,tr8e);
			doc.connect(s7f,tr8f);

			doc.connect(tr8e,s8e);

			doc.connect(s8e,tr9e);
			doc.connect(s8f,tr9f);
			doc.connect(s8f,tr9g);
			doc.connect(s8f,tr9h);

			doc.connect(tr9e,s9e);
			doc.connect(tr9f,s9f);
			doc.connect(tr9g,s9g);
			doc.connect(tr9h,s9h);

			doc.connect(s9f,tr10f);
			doc.connect(s9g,tr10g);
			doc.connect(s9h,tr10h);

			doc.connect(tr10f,s10f);

			doc.connect(s10f,tr11f);
			doc.connect(s10g,tr11g);
			doc.connect(s10h,tr11h);

			doc.connect(tr11f,s11f);
			doc.connect(tr11g,s11g);
			doc.connect(tr11h,s11h);

			doc.connect(s11g,tr12g);
			doc.connect(s11h,tr12h);

			doc.connect(tr12g,s12g);
			doc.connect(tr12h,s12h);

			doc.connect(s12g,tr13g);
			doc.connect(s12h,tr13h);

			doc.connect(tr13g,s13g);
			doc.connect(tr13h,s13h);

			doc.connect(s3b,trEndb);
			doc.connect(s5c,trEndc);
			doc.connect(s7d,trEndd);
			doc.connect(s9e,trEnde);
			doc.connect(s11f,trEndf);
			doc.connect(s13g,trEndg);
			doc.connect(s13h,trEndh);

			doc.connect(trEndb,initialStep);
			doc.connect(trEndc,initialStep);
			doc.connect(trEndd,initialStep);
			doc.connect(trEnde,initialStep);
			doc.connect(trEndf,initialStep);
			doc.connect(trEndg,initialStep);
			doc.connect(trEndh,initialStep);


		}

		if(currentTable==6)
		{
			GCTransition tr02a = doc.createTransition(100,y,"nrOfRot==Shoefactory.tables.table6.rot&! Shoefactory.agvs.agv0.busy &goto==0");
			GCTransition tr02c = doc.createTransition(700,y,"nrOfRot==Shoefactory.tables.table6.rot&! Shoefactory.agvs.agv0.busy &(goto==7|goto==8|goto==9|goto==10|goto==11|goto==12)");
			y+=50;
			GCStep s02a = doc.createStep(100,y,"move_table0","S Shoefactory.tables.table6.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv0.from6to0=1;");
			GCStep s02c = doc.createStep(700,y,"move_table7","S Shoefactory.tables.table6.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv0.from6to1=1;");
			y+=100;
			GCTransition tr2a = doc.createTransition(100,y,"Shoefactory.agvs.agv0.atgoal" );
			GCTransition tr2c = doc.createTransition(700,y,"Shoefactory.agvs.agv0.atgoal");
			y+=50;
			GCStep s2a = doc.createStep(100,y,"S_toTable0_end","P nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getRota\",\"int\",\"int\",Shoefactory.tables.table0.rot);");
			GCStep s2c = createTableSFC(doc,tr2c,1,700,y,1);
			y+=250;
			GCTransition tr3a = doc.createTransition(100,y,"S_toTable0_end.t>Shoefactory.time & !Shoefactory.tables.table0.fullSlot.get(nrOfRot) ");
			GCTransition tr3c = doc.createTransition(700,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table1.rot)==6&! Shoefactory.agvs.agv1.busy & goto==7 & JgrafSupervisor.get_T1");
			GCTransition tr3d = doc.createTransition(1000,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table1.rot)==6&! Shoefactory.agvs.agv1.busy & (goto==8|goto==9|goto==10|goto==11|goto==12) & JgrafSupervisor.get_T1");
			y+=50;
			GCStep s3a = doc.createStep(100,y,"S_atTable0","S currentTable = 0;\nS Shoefactory.tables.table0.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv0.busy=0;\nS moveReady=1;");
			GCStep s3c = doc.createStep(700,y,"S_toTable7_b","S Shoefactory.tables.table1.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv1.from1to7=1;");
			GCStep s3d = doc.createStep(1000,y,"S_toTable8_b","S Shoefactory.tables.table1.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv1.from1to2=1;");
			y+=100;
			GCTransition tr4c = doc.createTransition(700,y,"Shoefactory.agvs.agv1.atgoal");
			GCTransition tr4d = doc.createTransition(1000,y,"Shoefactory.agvs.agv1.atgoal");
			y+=50;
			GCStep s4c = doc.createStep(700,y,"S_toTable7_end","P nrOfRot=Shoefactory.tables.table7.rot;");
			GCStep s4d = createTableSFC(doc,tr4d,2,1000,y,1);
			y+=250;
			GCTransition tr5c = doc.createTransition(700,y,"S_toTable7_end.t>Shoefactory.time & !Shoefactory.tables.table7.fullSlot.get(nrOfRot) ");
			GCTransition tr5d = doc.createTransition(1000,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table2.rot)==6&! Shoefactory.agvs.agv2.busy & goto==8");
			GCTransition tr5e = doc.createTransition(1300,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table2.rot)==6&! Shoefactory.agvs.agv2.busy & (goto==9|goto==10|goto==11|goto==12)");
			y+=50;
			GCStep s5c = doc.createStep(700,y,"S_atTable7","S currentTable = 7;\nS Shoefactory.tables.table7.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv1.busy=0;\nS moveReady=1;");
			GCStep s5d = doc.createStep(1000,y,"S_toTable8_c","S Shoefactory.tables.table2.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv2.from2to8=1;");
			GCStep s5e = doc.createStep(1300,y,"S_toTable9_c","S Shoefactory.tables.table2.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv2.from2to3=1;");
			y+=100;
			GCTransition tr6d = doc.createTransition(1000,y,"Shoefactory.agvs.agv2.atgoal");
			GCTransition tr6e = doc.createTransition(1300,y,"Shoefactory.agvs.agv2.atgoal");
			y+=50;
			GCStep s6d = doc.createStep(1000,y,"S_toTable8_end","P nrOfRot=Shoefactory.tables.table8.rot;");
			GCStep s6e = createTableSFC(doc,tr6e,3,1300,y,1);
			y+=250;
			GCTransition tr7d = doc.createTransition(1000,y,"S_toTable8_end.t>Shoefactory.time & !Shoefactory.tables.table8.fullSlot.get(nrOfRot)");
			GCTransition tr7e = doc.createTransition(1300,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table3.rot)==6&! Shoefactory.agvs.agv3.busy & goto==9");
			GCTransition tr7f = doc.createTransition(1600,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table3.rot)==6&! Shoefactory.agvs.agv3.busy &(goto==10|goto==11|goto==12)");
			y+=50;
			GCStep s7d = doc.createStep(1000,y,"S_atTable8","S currentTable = 8;\nS Shoefactory.tables.table8.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv2.busy=0;\nS moveReady=1;");
			GCStep s7e = doc.createStep(1300,y,"S_toTable9_d","S Shoefactory.tables.table3.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv3.from3to9=1;");
			GCStep s7f = doc.createStep(1600,y,"S_toTable10_d","S Shoefactory.tables.table3.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv3.from3to4=1;");
			y+=100;
			GCTransition tr8e = doc.createTransition(1300,y,"Shoefactory.agvs.agv3.atgoal");
			GCTransition tr8f = doc.createTransition(1600,y,"Shoefactory.agvs.agv3.atgoal");
			y+=50;
			GCStep s8e = doc.createStep(1300,y,"S_toTable9_end","P nrOfRot=Shoefactory.tables.table9.rot;");
			GCStep s8f = createTableSFC(doc,tr8f,4,1600,y,1);
			y+=250;
			GCTransition tr9e = doc.createTransition(1300,y,"S_toTable9_end.t>Shoefactory.time & !Shoefactory.tables.table9.fullSlot.get(nrOfRot)");
			GCTransition tr9f = doc.createTransition(1600,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table4.rot)==6&! Shoefactory.agvs.agv4.busy & goto==10");
			GCTransition tr9g = doc.createTransition(1900,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table4.rot)==6&! Shoefactory.agvs.agv4.busy & goto==11");
			GCTransition tr9h = doc.createTransition(2200,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table4.rot)==6&! Shoefactory.agvs.agv4.busy & goto==12");
			y+=50;
			GCStep s9e = doc.createStep(1300,y,"S_atTable9","S currentTable = 9;\nS Shoefactory.tables.table9.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv3.busy=0;\nS moveReady=1;");
			GCStep s9f = doc.createStep(1600,y,"S_toTable10_e","S Shoefactory.tables.table4.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv4.from4to10=1;");
			GCStep s9g = doc.createStep(1900,y,"S_toTable11_e","S Shoefactory.tables.table4.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv4.from4to5=1;");
			GCStep s9h = doc.createStep(2200,y,"S_toTable12_e","S Shoefactory.tables.table4.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv4.from4to10=1;");
			y+=100;
			GCTransition tr10f = doc.createTransition(1600,y,"Shoefactory.agvs.agv4.atgoal");
			GCTransition tr10g = doc.createTransition(1900,y,"Shoefactory.agvs.agv4.atgoal");
			GCTransition tr10h = doc.createTransition(2200,y,"Shoefactory.agvs.agv4.atgoal");
			y+=50;
			GCStep s10f = doc.createStep(1600,y,"S_toTable10_end","P nrOfRot=Shoefactory.tables.table10.rot;");
			GCStep s10g = createTableSFC(doc,tr10g,5,1900,y,1);
			GCStep s10h = createTableSFC(doc,tr10h,10,2200,y,1);
			y+=250;
			GCTransition tr11f = doc.createTransition(1600,y,"S_toTable10_end.t>Shoefactory.time & !Shoefactory.tables.table10.fullSlot.get(nrOfRot)");
			GCTransition tr11g = doc.createTransition(1900,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",applyStaticMethod(\"org.supremica.external.shoefactory.Executor.FactoryExecutor\",\"fiveRot\",\"int\",\"int\",nrOfRot)-Shoefactory.tables.table5.rot)==9 &! Shoefactory.agvs.agv5.busy");
			GCTransition tr11h = doc.createTransition(2200,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table10.rot)==12&! Shoefactory.agvs.agv6.busy");
			y+=50;
			GCStep s11f = doc.createStep(1300,y,"S_atTable10","S currentTable = 10;\nS Shoefactory.tables.table10.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv4.busy=0;\nS moveReady=1;");
			GCStep s11g = doc.createStep(1900,y,"S_toTable11_f","S Shoefactory.tables.table5.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv5.from5to11=1;");
			GCStep s11h = doc.createStep(2200,y,"S_toTable12_f","S Shoefactory.tables.table10.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv6.from10to12=1;");
			y+=100;
			GCTransition tr12g = doc.createTransition(1900,y,"Shoefactory.agvs.agv5.atgoal");
			GCTransition tr12h = doc.createTransition(2200,y,"Shoefactory.agvs.agv6.atgoal");
			y+=50;
			GCStep s12g = doc.createStep(1900,y,"S_toTable11_end","P nrOfRot=Shoefactory.tables.table11.rot;");
			GCStep s12h = doc.createStep(2200,y,"S_toTable12_end","P nrOfRot=Shoefactory.tables.table12.rot;");
			y+=100;
			GCTransition tr13g = doc.createTransition(1900,y,"S_toTable11_end.t>Shoefactory.time & !Shoefactory.tables.table11.fullSlot.get(nrOfRot)");
			GCTransition tr13h = doc.createTransition(2200,y,"S_toTable12_end.t>Shoefactory.time & !Shoefactory.tables.table12.fullSlot.get(nrOfRot)");
			y+=50;
			GCStep s13g = doc.createStep(1900,y,"S_atTable11","S currentTable = 11;\nS Shoefactory.tables.table11.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv5.busy=0;\nS moveReady=1;");
			GCStep s13h = doc.createStep(2200,y,"S_atTable12","S currentTable = 12;\nS Shoefactory.tables.table12.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv6.busy=0;\nS moveReady=1;");
			y+=250;
			GCTransition trEnda = doc.createTransition(100,y,"1");
			GCTransition trEndc = doc.createTransition(700,y,"1");
			GCTransition trEndd = doc.createTransition(1000,y,"1");
			GCTransition trEnde = doc.createTransition(1300,y,"1");
			GCTransition trEndf = doc.createTransition(1600,y,"1");
			GCTransition trEndg = doc.createTransition(1900,y,"1");
			GCTransition trEndh = doc.createTransition(2200,y,"1");
			y+=50;

			doc.connect(s0,tr02a);
			doc.connect(s0,tr02c);

			doc.connect(tr02a,s02a);
			doc.connect(tr02c,s02c);

			doc.connect(s02a,tr2a);
			doc.connect(s02c,tr2c);

			doc.connect(tr2a,s2a);

			doc.connect(s2a,tr3a);
			doc.connect(s2c,tr3c);
			doc.connect(s2c,tr3d);

			doc.connect(tr3a,s3a);
			doc.connect(tr3c,s3c);
			doc.connect(tr3d,s3d);

			doc.connect(s3c,tr4c);
			doc.connect(s3d,tr4d);

			doc.connect(tr4c,s4c);

			doc.connect(s4c,tr5c);
			doc.connect(s4d,tr5d);
			doc.connect(s4d,tr5e);

			doc.connect(tr5c,s5c);
			doc.connect(tr5d,s5d);
			doc.connect(tr5e,s5e);

			doc.connect(s5d,tr6d);
			doc.connect(s5e,tr6e);

			doc.connect(tr6d,s6d);

			doc.connect(s6d,tr7d);
			doc.connect(s6e,tr7e);
			doc.connect(s6e,tr7f);

			doc.connect(tr7d,s7d);
			doc.connect(tr7e,s7e);
			doc.connect(tr7f,s7f);

			doc.connect(s7e,tr8e);
			doc.connect(s7f,tr8f);

			doc.connect(tr8e,s8e);

			doc.connect(s8e,tr9e);
			doc.connect(s8f,tr9f);
			doc.connect(s8f,tr9g);
			doc.connect(s8f,tr9h);

			doc.connect(tr9e,s9e);
			doc.connect(tr9f,s9f);
			doc.connect(tr9g,s9g);
			doc.connect(tr9h,s9h);

			doc.connect(s9f,tr10f);
			doc.connect(s9g,tr10g);
			doc.connect(s9h,tr10h);

			doc.connect(tr10f,s10f);

			doc.connect(s10f,tr11f);
			doc.connect(s10g,tr11g);
			doc.connect(s10h,tr11h);

			doc.connect(tr11f,s11f);
			doc.connect(tr11g,s11g);
			doc.connect(tr11h,s11h);

			doc.connect(s11g,tr12g);
			doc.connect(s11h,tr12h);

			doc.connect(tr12g,s12g);
			doc.connect(tr12h,s12h);

			doc.connect(s12g,tr13g);
			doc.connect(s12h,tr13h);

			doc.connect(tr13g,s13g);
			doc.connect(tr13h,s13h);

			doc.connect(s3a,trEnda);
			doc.connect(s5c,trEndc);
			doc.connect(s7d,trEndd);
			doc.connect(s9e,trEnde);
			doc.connect(s11f,trEndf);
			doc.connect(s13g,trEndg);
			doc.connect(s13h,trEndh);

			doc.connect(trEnda,initialStep);
			doc.connect(trEndc,initialStep);
			doc.connect(trEndd,initialStep);
			doc.connect(trEnde,initialStep);
			doc.connect(trEndf,initialStep);
			doc.connect(trEndg,initialStep);
			doc.connect(trEndh,initialStep);
		}

		if(currentTable==7)
		{
			GCTransition tr02a = doc.createTransition(100,y,"nrOfRot==Shoefactory.tables.table7.rot&! Shoefactory.agvs.agv1.busy & (goto==0 | goto==6)");
			GCTransition tr02d = doc.createTransition(1000,y,"nrOfRot==Shoefactory.tables.table7.rot&! Shoefactory.agvs.agv1.busy & (goto==8|goto==9|goto==10|goto==11|goto==12) ");
			y+=50;
			GCStep s02a = doc.createStep(100,y,"move_table0","S Shoefactory.tables.table7.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv1.from7to1=1;");
			GCStep s02d = doc.createStep(1000,y,"move_table8","S Shoefactory.tables.table7.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv1.from7to2=1;");
			y+=100;
			GCTransition tr2a = doc.createTransition(100,y,"Shoefactory.agvs.agv1.atgoal" );
			GCTransition tr2d = doc.createTransition(1000,y,"Shoefactory.agvs.agv1.atgoal");
			y+=50;
			GCStep s2a = createTableSFC(doc,tr2a,1,100,y,0);
			GCStep s2d = createTableSFC(doc,tr2d,2,1000,y,1);
			y+=250;
			GCTransition tr3a = doc.createTransition(100,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table1.rot)==0&! Shoefactory.agvs.agv0.busy & goto==0");
			GCTransition tr3b = doc.createTransition(400,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table1.rot)==0&! Shoefactory.agvs.agv0.busy & goto==6");
			GCTransition tr3d = doc.createTransition(1000,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table2.rot)==6&! Shoefactory.agvs.agv2.busy &goto==8 ");
			GCTransition tr3e = doc.createTransition(1300,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table2.rot)==6&! Shoefactory.agvs.agv2.busy & (goto==9|goto==10|goto==11|goto==12)");
			y+=50;
			GCStep s3a = doc.createStep(100,y,"S_toTable0_b","S Shoefactory.tables.table1.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv0.from1to0=1;");
			GCStep s3b = doc.createStep(400,y,"S_toTable6_b","S Shoefactory.tables.table1.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv0.from1to6=1;");
			GCStep s3d = doc.createStep(1000,y,"S_toTable8_b","S Shoefactory.tables.table2.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv2.from2to8=1;");
			GCStep s3e = doc.createStep(1300,y,"S_toTable9_b","S Shoefactory.tables.table2.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv2.from2to3=1;");
			y+=100;
			GCTransition tr4a = doc.createTransition(100,y,"Shoefactory.agvs.agv0.atgoal");
			GCTransition tr4b = doc.createTransition(400,y,"Shoefactory.agvs.agv0.atgoal");
			GCTransition tr4d = doc.createTransition(1000,y,"Shoefactory.agvs.agv2.atgoal");
			GCTransition tr4e =	doc.createTransition(1300,y,"Shoefactory.agvs.agv2.atgoal");
			y+=50;
			GCStep s4a = doc.createStep(100,y,"S_toTable0_end","P nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getRota\",\"int\",\"int\",Shoefactory.tables.table0.rot);");
			GCStep s4b = doc.createStep(400,y,"S_toTable6_end","P nrOfRot=Shoefactory.tables.table6.rot;");
			GCStep s4d = doc.createStep(1000,y,"S_toTable8_end","P nrOfRot=Shoefactory.tables.table8.rot;");
			GCStep s4e = createTableSFC(doc,tr4e,3,1300,y,1);
			y+=250;
			GCTransition tr5a = doc.createTransition(100,y,"S_toTable0_end.t>Shoefactory.time & !Shoefactory.tables.table0.fullSlot.get(nrOfRot) ");
			GCTransition tr5b = doc.createTransition(400,y,"S_toTable6_end.t>Shoefactory.time & !Shoefactory.tables.table6.fullSlot.get(nrOfRot) ");
			GCTransition tr5d = doc.createTransition(1000,y,"S_toTable8_end.t>Shoefactory.time & !Shoefactory.tables.table8.fullSlot.get(nrOfRot) ");
			GCTransition tr5e = doc.createTransition(1300,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table3.rot)==6&! Shoefactory.agvs.agv3.busy & goto==9");
			GCTransition tr5f = doc.createTransition(1600,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table3.rot)==6&! Shoefactory.agvs.agv3.busy & (goto==10 |goto==11 |goto==12)");
			y+=50;
			GCStep s5a = doc.createStep(100,y,"S_atTable0","S currentTable = 0;\nS Shoefactory.tables.table0.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv0.busy=0;\nS moveReady=1;");
			GCStep s5b = doc.createStep(400,y,"S_atTable6","S currentTable = 6;\nS Shoefactory.tables.table6.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv0.busy=0;\nS moveReady=1;");
			GCStep s5d = doc.createStep(1000,y,"S_atTable8","S currentTable = 8;\nS Shoefactory.tables.table8.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv2.busy=0;\nS moveReady=1;");
			GCStep s5e = doc.createStep(1300,y,"S_toTable9_c","S Shoefactory.tables.table3.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv3.from3to9=1;");
			GCStep s5f = doc.createStep(1600,y,"S_toTable10_c","S Shoefactory.tables.table3.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv3.from3to4=1;");
			y+=100;
			GCTransition tr6e = doc.createTransition(1300,y,"Shoefactory.agvs.agv3.atgoal");
			GCTransition tr6f = doc.createTransition(1600,y,"Shoefactory.agvs.agv3.atgoal");
			y+=50;
			GCStep s6e = doc.createStep(1300,y,"S_toTable9_end","P nrOfRot=Shoefactory.tables.table9.rot;");
			GCStep s6f = createTableSFC(doc,tr6f,4,1600,y,1);
			y+=250;
			GCTransition tr7e = doc.createTransition(1300,y,"S_toTable9_end.t>Shoefactory.time & !Shoefactory.tables.table9.fullSlot.get(nrOfRot)");
			GCTransition tr7f = doc.createTransition(1600,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table4.rot)==6&! Shoefactory.agvs.agv4.busy & goto==10");
			GCTransition tr7g = doc.createTransition(1900,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table4.rot)==6&! Shoefactory.agvs.agv4.busy & goto==11");
			GCTransition tr7h = doc.createTransition(2200,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table4.rot)==6&! Shoefactory.agvs.agv4.busy & goto==12");
			y+=50;
			GCStep s7e = doc.createStep(1300,y,"S_atTable9","S currentTable = 9;\nS Shoefactory.tables.table9.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv3.busy=0;\nS moveReady=1;");
			GCStep s7f = doc.createStep(1600,y,"S_toTable10_e","S Shoefactory.tables.table4.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv4.from4to10=1;");
			GCStep s7g = doc.createStep(1900,y,"S_toTable11_e","S Shoefactory.tables.table4.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv4.from4to5=1;");
			GCStep s7h = doc.createStep(2200,y,"S_toTable12_e","S Shoefactory.tables.table4.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv4.from4to10=1;");
			y+=100;
			GCTransition tr8f = doc.createTransition(1600,y,"Shoefactory.agvs.agv4.atgoal");
			GCTransition tr8g = doc.createTransition(1900,y,"Shoefactory.agvs.agv4.atgoal");
			GCTransition tr8h = doc.createTransition(2200,y,"Shoefactory.agvs.agv4.atgoal");
			y+=50;
			GCStep s8f = doc.createStep(1600,y,"S_toTable10_end","P nrOfRot=Shoefactory.tables.table10.rot;");
			GCStep s8g = createTableSFC(doc,tr8g,5,1900,y,1);
			GCStep s8h = createTableSFC(doc,tr8h,10,2200,y,1);
			y+=250;
			GCTransition tr9f = doc.createTransition(1600,y,"S_toTable10_end.t>Shoefactory.time & !Shoefactory.tables.table10.fullSlot.get(nrOfRot)");
			GCTransition tr9g = doc.createTransition(1900,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",applyStaticMethod(\"org.supremica.external.shoefactory.Executor.FactoryExecutor\",\"fiveRot\",\"int\",\"int\",nrOfRot)-Shoefactory.tables.table5.rot)==9 &! Shoefactory.agvs.agv5.busy");
			GCTransition tr9h = doc.createTransition(2200,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table10.rot)==12&! Shoefactory.agvs.agv6.busy");
			y+=50;
			GCStep s9f = doc.createStep(1600,y,"S_atTable10","S currentTable = 10;\nS Shoefactory.tables.table10.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv4.busy=0;\nS moveReady=1;");
			GCStep s9g = doc.createStep(1900,y,"S_toTable11_f","S Shoefactory.tables.table5.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv5.from5to11=1;");
			GCStep s9h = doc.createStep(2200,y,"S_toTable12_f","S Shoefactory.tables.table10.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv6.from10to12=1;");
			y+=100;
			GCTransition tr10g = doc.createTransition(1900,y,"Shoefactory.agvs.agv5.atgoal");
			GCTransition tr10h = doc.createTransition(2200,y,"Shoefactory.agvs.agv6.atgoal");
			y+=50;
			GCStep s10g = doc.createStep(1900,y,"S_toTable11_end","P nrOfRot=Shoefactory.tables.table11.rot;");
			GCStep s10h = doc.createStep(2200,y,"S_toTable12_end","P nrOfRot=Shoefactory.tables.table12.rot;");
			y+=100;
			GCTransition tr11g = doc.createTransition(1900,y,"S_toTable11_end.t>Shoefactory.time & !Shoefactory.tables.table11.fullSlot.get(nrOfRot)");
			GCTransition tr11h = doc.createTransition(2200,y,"S_toTable12_end.t>Shoefactory.time & !Shoefactory.tables.table12.fullSlot.get(nrOfRot)");
			y+=50;
			GCStep s11g = doc.createStep(1900,y,"S_atTable11","S currentTable = 11;\nS Shoefactory.tables.table11.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv5.busy=0;\nS moveReady=1;");
			GCStep s11h = doc.createStep(2200,y,"S_atTable12","S currentTable = 12;\nS Shoefactory.tables.table12.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv6.busy=0;\nS moveReady=1;");
			y+=250;
			GCTransition trEnda = doc.createTransition(100,y,"1");
			GCTransition trEndb = doc.createTransition(400,y,"1");
			GCTransition trEndd = doc.createTransition(1000,y,"1");
			GCTransition trEnde = doc.createTransition(1300,y,"1");
			GCTransition trEndf = doc.createTransition(1600,y,"1");
			GCTransition trEndg = doc.createTransition(1900,y,"1");
			GCTransition trEndh = doc.createTransition(2200,y,"1");
			y+=50;

			doc.connect(s0,tr02a);
			doc.connect(s0,tr02d);

			doc.connect(tr02a,s02a);
			doc.connect(tr02d,s02d);

			doc.connect(s02a,tr2a);
			doc.connect(s02d,tr2d);

			doc.connect(s2a,tr3a);
			doc.connect(s2a,tr3b);
			doc.connect(s2d,tr3d);
			doc.connect(s2d,tr3e);

			doc.connect(tr3a,s3a);
			doc.connect(tr3b,s3b);
			doc.connect(tr3d,s3d);
			doc.connect(tr3e,s3e);

			doc.connect(s3a,tr4a);
			doc.connect(s3b,tr4b);
			doc.connect(s3d,tr4d);
			doc.connect(s3e,tr4e);

			doc.connect(tr4a,s4a);
			doc.connect(tr4b,s4b);
			doc.connect(tr4d,s4d);

			doc.connect(s4a,tr5a);
			doc.connect(s4b,tr5b);
			doc.connect(s4d,tr5d);
			doc.connect(s4e,tr5e);
			doc.connect(s4e,tr5f);

			doc.connect(tr5a,s5a);
			doc.connect(tr5b,s5b);
			doc.connect(tr5d,s5d);
			doc.connect(tr5e,s5e);
			doc.connect(tr5f,s5f);

			doc.connect(s5e,tr6e);
			doc.connect(s5f,tr6f);

			doc.connect(tr6e,s6e);

			doc.connect(s6e,tr7e);
			doc.connect(s6f,tr7f);
			doc.connect(s6f,tr7g);
			doc.connect(s6f,tr7h);

			doc.connect(tr7e,s7e);
			doc.connect(tr7f,s7f);
			doc.connect(tr7g,s7g);
			doc.connect(tr7h,s7h);

			doc.connect(s7f,tr8f);
			doc.connect(s7g,tr8g);
			doc.connect(s7h,tr8h);

			doc.connect(tr8f,s8f);

			doc.connect(s8f,tr9f);
			doc.connect(s8g,tr9g);
			doc.connect(s8h,tr9h);

			doc.connect(tr9f,s9f);
			doc.connect(tr9g,s9g);
			doc.connect(tr9h,s9h);

			doc.connect(s9g,tr10g);
			doc.connect(s9h,tr10h);

			doc.connect(tr10g,s10g);
			doc.connect(tr10h,s10h);

			doc.connect(s10g,tr11g);
			doc.connect(s10h,tr11h);

			doc.connect(tr11g,s11g);
			doc.connect(tr11h,s11h);

			doc.connect(s5a,trEnda);
			doc.connect(s5b,trEndb);
			doc.connect(s5d,trEndd);
			doc.connect(s7e,trEnde);
			doc.connect(s9f,trEndf);
			doc.connect(s11g,trEndg);
			doc.connect(s11h,trEndh);

			doc.connect(trEnda,initialStep);
			doc.connect(trEndb,initialStep);
			doc.connect(trEndd,initialStep);
			doc.connect(trEnde,initialStep);
			doc.connect(trEndf,initialStep);
			doc.connect(trEndg,initialStep);
			doc.connect(trEndh,initialStep);
		}

		if(currentTable==8)
		{
			GCTransition tr02a = doc.createTransition(100,y,"nrOfRot==Shoefactory.tables.table8.rot&! Shoefactory.agvs.agv2.busy & (goto==0|goto==6|goto==7)");
			GCTransition tr02e = doc.createTransition(1300,y,"nrOfRot==Shoefactory.tables.table8.rot&! Shoefactory.agvs.agv2.busy & (goto==9|goto==10|goto==11)");
			y+=50;
			GCStep s02a = doc.createStep(100,y,"move_table0","S Shoefactory.tables.table8.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv2.from8to2=1;");
			GCStep s02e = doc.createStep(1300,y,"move_table9","S Shoefactory.tables.table8.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv2.from8to3=1;");
			y+=100;
			GCTransition tr2a = doc.createTransition(100,y,"Shoefactory.agvs.agv2.atgoal" );
			GCTransition tr2e = doc.createTransition(1300,y,"Shoefactory.agvs.agv2.atgoal");
			y+=50;
			GCStep s2a = createTableSFC(doc,tr2a,2,100,y,0);
			GCStep s2e = createTableSFC(doc,tr2e,3,1300,y,1);
			y+=250;
			GCTransition tr3a = doc.createTransition(100,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table2.rot)==0&! Shoefactory.agvs.agv1.busy & (goto==0|goto==6)");
			GCTransition tr3c = doc.createTransition(700,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table2.rot)==0&! Shoefactory.agvs.agv1.busy & goto==7");
			GCTransition tr3e = doc.createTransition(1300,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table3.rot)==6&! Shoefactory.agvs.agv3.busy & goto==9");
			GCTransition tr3f = doc.createTransition(1600,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table3.rot)==6&! Shoefactory.agvs.agv3.busy & (goto==10|goto==11|goto==12)");
			y+=50;
			GCStep s3a = doc.createStep(100,y,"S_toTable0_b","S Shoefactory.tables.table2.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv1.from2to1=1;");
			GCStep s3c = doc.createStep(700,y,"S_toTable7_b","S Shoefactory.tables.table2.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv1.from2to7=1;");
			GCStep s3e = doc.createStep(1300,y,"S_toTable9_b","S Shoefactory.tables.table3.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv3.from3to9=1;");
			GCStep s3f = doc.createStep(1600,y,"S_toTable10_b","S Shoefactory.tables.table3.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv3.from3to4=1;");
			y+=100;
			GCTransition tr4a = doc.createTransition(100,y,"Shoefactory.agvs.agv1.atgoal");
			GCTransition tr4c = doc.createTransition(700,y,"Shoefactory.agvs.agv1.atgoal");
			GCTransition tr4e = doc.createTransition(1300,y,"Shoefactory.agvs.agv3.atgoal");
			GCTransition tr4f = doc.createTransition(1600,y,"Shoefactory.agvs.agv3.atgoal");
			y+=50;
			GCStep s4a = createTableSFC(doc,tr4a,1,100,y,0);
			GCStep s4c = doc.createStep(700,y,"S_toTable7_end","P nrOfRot=Shoefactory.tables.table7.rot;");
			GCStep s4e = doc.createStep(1300,y,"S_toTable9_end","P nrOfRot=Shoefactory.tables.table9.rot;");
			GCStep s4f = createTableSFC(doc,tr4f,4,1600,y,1);
			y+=250;
			GCTransition tr5a = doc.createTransition(100,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table1.rot)==0&! Shoefactory.agvs.agv0.busy");
			GCTransition tr5b = doc.createTransition(400,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table1.rot)==0&! Shoefactory.agvs.agv0.busy");
			GCTransition tr5c = doc.createTransition(700,y,"S_toTable7_end.t>Shoefactory.time & !Shoefactory.tables.table7.fullSlot.get(nrOfRot) ");
			GCTransition tr5e = doc.createTransition(1300,y,"S_toTable9_end.t>Shoefactory.time & !Shoefactory.tables.table9.fullSlot.get(nrOfRot) ");
			GCTransition tr5f = doc.createTransition(1600,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table4.rot)==6&! Shoefactory.agvs.agv4.busy & goto==10");
			GCTransition tr5g = doc.createTransition(1900,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table4.rot)==6&! Shoefactory.agvs.agv4.busy & goto==11");
			GCTransition tr5h = doc.createTransition(2200,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table4.rot)==6&! Shoefactory.agvs.agv4.busy & goto==12");
			y+=50;
			GCStep s5a = doc.createStep(100,y,"S_toTable0_c","S Shoefactory.tables.table1.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv0.from1to0=1;");
			GCStep s5b = doc.createStep(400,y,"S_toTable6_c","S Shoefactory.tables.table1.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv0.from1to6=1;");
			GCStep s5c = doc.createStep(700,y,"S_atTable7","S currentTable = 7;\nS Shoefactory.tables.table7.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv1.busy=0;\nS moveReady=1;");
			GCStep s5e = doc.createStep(1300,y,"S_atTable9","S currentTable = 9;\nS Shoefactory.tables.table9.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv3.busy=0;\nS moveReady=1;");
			GCStep s5f = doc.createStep(1600,y,"S_toTable10_c","S Shoefactory.tables.table4.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv4.from4to10=1;");
			GCStep s5g = doc.createStep(1900,y,"S_toTable11_c","S Shoefactory.tables.table4.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv4.from4to5=1;");
			GCStep s5h = doc.createStep(2200,y,"S_toTable12_c","S Shoefactory.tables.table4.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv4.from4to10=1;");
			y+=100;
			GCTransition tr6a = doc.createTransition(100,y,"Shoefactory.agvs.agv0.atgoal");
			GCTransition tr6b = doc.createTransition(400,y,"Shoefactory.agvs.agv0.atgoal");
			GCTransition tr6f = doc.createTransition(1600,y,"Shoefactory.agvs.agv4.atgoal");
			GCTransition tr6g = doc.createTransition(1900,y,"Shoefactory.agvs.agv4.atgoal");
			GCTransition tr6h = doc.createTransition(2200,y,"Shoefactory.agvs.agv4.atgoal");
			y+=50;
			GCStep s6a = doc.createStep(100,y,"S_toTable0_end","P nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getRota\",\"int\",\"int\",Shoefactory.tables.table0.rot);");
			GCStep s6b = doc.createStep(400,y,"S_toTable6_end","P nrOfRot=Shoefactory.tables.table6.rot;");
			GCStep s6f = doc.createStep(1600,y,"S_toTable10_end","P nrOfRot=Shoefactory.tables.table10.rot;");
			GCStep s6g = createTableSFC(doc,tr6g,5,1900,y,1);
			GCStep s6h = createTableSFC(doc,tr6h,10,2200,y,1);
			y+=250;
			GCTransition tr7a = doc.createTransition(100,y,"S_toTable0_end.t>Shoefactory.time & !Shoefactory.tables.table0.fullSlot.get(nrOfRot)");
			GCTransition tr7b = doc.createTransition(400,y,"S_toTable6_end.t>Shoefactory.time & !Shoefactory.tables.table6.fullSlot.get(nrOfRot)");
			GCTransition tr7f = doc.createTransition(1600,y,"S_toTable10_end.t>Shoefactory.time & !Shoefactory.tables.table10.fullSlot.get(nrOfRot)");
			GCTransition tr7g = doc.createTransition(1900,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",applyStaticMethod(\"org.supremica.external.shoefactory.Executor.FactoryExecutor\",\"fiveRot\",\"int\",\"int\",nrOfRot)-Shoefactory.tables.table5.rot)==9 &! Shoefactory.agvs.agv5.busy");
			GCTransition tr7h = doc.createTransition(2200,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table10.rot)==12&! Shoefactory.agvs.agv6.busy");
			y+=50;
			GCStep s7a = doc.createStep(100,y,"S_atTable0","S currentTable = 0;\nS Shoefactory.tables.table0.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv0.busy=0;\nS moveReady=1;");
			GCStep s7b = doc.createStep(400,y,"S_atTable6","S currentTable = 6;\nS Shoefactory.tables.table6.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv0.busy=0;\nS moveReady=1;");
			GCStep s7f = doc.createStep(1600,y,"S_atTable10","S currentTable = 10;\nS Shoefactory.tables.table10.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv4.busy=0;\nS moveReady=1;");
			GCStep s7g = doc.createStep(1900,y,"S_toTable11_e","S Shoefactory.tables.table5.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv5.from5to11=1;");
			GCStep s7h = doc.createStep(2200,y,"S_toTable12_e","S Shoefactory.tables.table10.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv6.from10to12=1;");
			y+=100;
			GCTransition tr8g = doc.createTransition(1900,y,"Shoefactory.agvs.agv5.atgoal");
			GCTransition tr8h = doc.createTransition(2200,y,"Shoefactory.agvs.agv6.atgoal");
			y+=50;
			GCStep s8g = doc.createStep(1900,y,"S_toTable11_end","P nrOfRot=Shoefactory.tables.table11.rot;");
			GCStep s8h = doc.createStep(2200,y,"S_toTable12_end","P nrOfRot=Shoefactory.tables.table12.rot;");
			y+=100;
			GCTransition tr9g = doc.createTransition(1900,y,"S_toTable11_end.t>Shoefactory.time & !Shoefactory.tables.table11.fullSlot.get(nrOfRot)");
			GCTransition tr9h = doc.createTransition(2200,y,"S_toTable12_end.t>Shoefactory.time & !Shoefactory.tables.table12.fullSlot.get(nrOfRot)");
			y+=50;
			GCStep s9g = doc.createStep(1900,y,"S_atTable11","S currentTable = 11;\nS Shoefactory.tables.table11.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv5.busy=0;\nS moveReady=1;");
			GCStep s9h = doc.createStep(2200,y,"S_atTable12","S currentTable = 12;\nS Shoefactory.tables.table12.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv6.busy=0;\nS moveReady=1;");
			y+=250;
			GCTransition trEnda = doc.createTransition(100,y,"1");
			GCTransition trEndb = doc.createTransition(400,y,"1");
			GCTransition trEndc = doc.createTransition(700,y,"1");
			GCTransition trEnde = doc.createTransition(1300,y,"1");
			GCTransition trEndf = doc.createTransition(1600,y,"1");
			GCTransition trEndg = doc.createTransition(1900,y,"1");
			GCTransition trEndh = doc.createTransition(2200,y,"1");
			y+=50;

			doc.connect(s0,tr02a);
			doc.connect(s0,tr02e);

			doc.connect(tr02a,s02a);
			doc.connect(tr02e,s02e);

			doc.connect(s02a,tr2a);
			doc.connect(s02e,tr2e);

			doc.connect(s2a,tr3a);
			doc.connect(s2e,tr3c);
			doc.connect(s2e,tr3e);
			doc.connect(s2e,tr3f);

			doc.connect(tr3a,s3a);
			doc.connect(tr3c,s3c);
			doc.connect(tr3e,s3e);
			doc.connect(tr3f,s3f);

			doc.connect(s3a,tr4a);
			doc.connect(s3c,tr4c);
			doc.connect(s3e,tr4e);
			doc.connect(s3f,tr4f);

			doc.connect(tr4c,s4c);
			doc.connect(tr4e,s4e);

			doc.connect(s4a,tr5a);
			doc.connect(s4a,tr5b);
			doc.connect(s4c,tr5c);
			doc.connect(s4e,tr5e);
			doc.connect(s4f,tr5f);
			doc.connect(s4f,tr5g);
			doc.connect(s4f,tr5h);

			doc.connect(tr5a,s5a);
			doc.connect(tr5b,s5b);
			doc.connect(tr5c,s5c);
			doc.connect(tr5e,s5e);
			doc.connect(tr5f,s5f);
			doc.connect(tr5g,s5g);
			doc.connect(tr5h,s5h);

			doc.connect(s5a,tr6a);
			doc.connect(s5b,tr6b);
			doc.connect(s5f,tr6f);
			doc.connect(s5g,tr6g);
			doc.connect(s5h,tr6h);

			doc.connect(tr6a,s6a);
			doc.connect(tr6b,s6b);
			doc.connect(tr6f,s6f);

			doc.connect(s6a,tr7a);
			doc.connect(s6b,tr7b);
			doc.connect(s6f,tr7f);
			doc.connect(s6g,tr7g);
			doc.connect(s6h,tr7h);

			doc.connect(tr7a,s7a);
			doc.connect(tr7b,s7b);
			doc.connect(tr7f,s7f);
			doc.connect(tr7g,s7g);
			doc.connect(tr7h,s7h);

			doc.connect(s7g,tr8g);
			doc.connect(s7h,tr8h);

			doc.connect(tr8g,s8g);
			doc.connect(tr8h,s8h);

			doc.connect(s8g,tr9g);
			doc.connect(s8h,tr9h);

			doc.connect(tr9g,s9g);
			doc.connect(tr9h,s9h);

			doc.connect(s7a,trEnda);
			doc.connect(s7b,trEndb);
			doc.connect(s5c,trEndc);
			doc.connect(s5e,trEnde);
			doc.connect(s7f,trEndf);
			doc.connect(s9g,trEndg);
			doc.connect(s9h,trEndh);

			doc.connect(trEnda,initialStep);
			doc.connect(trEndb,initialStep);
			doc.connect(trEndc,initialStep);
			doc.connect(trEnde,initialStep);
			doc.connect(trEndf,initialStep);
			doc.connect(trEndg,initialStep);
			doc.connect(trEndh,initialStep);
		}

		else if(currentTable==9)
		{
			GCTransition tr02a = doc.createTransition(100,y,"(goto==0 | goto==6 | goto==7 | goto==8) & nrOfRot==Shoefactory.tables.table9.rot & !Shoefactory.agvs.agv3.busy");
			GCTransition tr02f = doc.createTransition(1300,y,"(goto==10 | goto==11 | goto==12) & nrOfRot==Shoefactory.tables.table9.rot & !Shoefactory.agvs.agv3.busy");
			y+=50;
			GCStep s02a = doc.createStep(100,y,"S_toTableL_a","S Shoefactory.tables.table9.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv3.from9to3=1;");
			GCStep s02f = doc.createStep(1300,y,"S_toTableR_a","S Shoefactory.tables.table9.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv3.from9to4=1;");
			y+=100;
			GCTransition tr2a = doc.createTransition(100,y,"Shoefactory.agvs.agv3.atgoal" );
			GCTransition tr2f = doc.createTransition(1300,y,"Shoefactory.agvs.agv3.atgoal");
			y+=50;
			GCStep s2a = createTableSFC(doc,tr2a,3,100,y,0);
			GCStep s2f = createTableSFC(doc,tr2f,4,1300,y,1);
			y+=250;
			GCTransition tr3a = doc.createTransition(400,y,"(goto==0 | goto==6 | goto==7) & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table3.rot)==0 & !Shoefactory.agvs.agv2.busy");
			GCTransition tr3d = doc.createTransition(100,y,"goto==8 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table3.rot)==0 & !Shoefactory.agvs.agv2.busy");
			GCTransition tr3f = doc.createTransition(1300,y,"goto==10 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table4.rot)==6 & !Shoefactory.agvs.agv4.busy");
			GCTransition tr3g = doc.createTransition(1600,y,"goto==11 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table4.rot)==6 & !Shoefactory.agvs.agv4.busy");
			GCTransition tr3h = doc.createTransition(1900,y,"goto==12 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table4.rot)==6 & !Shoefactory.agvs.agv4.busy");
			y+=50;
			GCStep s3a = doc.createStep(400,y,"S_toTableL_b","S Shoefactory.tables.table3.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv2.from3to2=1;");
			GCStep s3d = doc.createStep(100,y,"S_toTable8_b","S Shoefactory.tables.table3.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv2.from3to8=1;");
			GCStep s3f = doc.createStep(1300,y,"S_toTable10_b","S Shoefactory.tables.table4.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv4.from4to10=1;");
			GCStep s3g = doc.createStep(1600,y,"S_toTable11_b","S Shoefactory.tables.table4.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv4.from4to5=1;");
			GCStep s3h = doc.createStep(1900,y,"S_toTable12_b","S Shoefactory.tables.table4.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv4.from4to10=1;");
			y+=100;
			GCTransition tr4a = doc.createTransition(400,y,"Shoefactory.agvs.agv2.atgoal");
			GCTransition tr4d = doc.createTransition(100,y,"Shoefactory.agvs.agv2.atgoal");
			GCTransition tr4f = doc.createTransition(1300,y,"Shoefactory.agvs.agv4.atgoal");
			GCTransition tr4g = doc.createTransition(1600,y,"Shoefactory.agvs.agv4.atgoal");
			GCTransition tr4h = doc.createTransition(1900,y,"Shoefactory.agvs.agv4.atgoal");
			y+=50;
			GCStep s4a = createTableSFC(doc,tr4a,2,400,y,0);
			GCStep s4d = doc.createStep(100,y,"S_toTable8_end","P nrOfRot=Shoefactory.tables.table8.rot;");
			GCStep s4f = doc.createStep(1300,y,"S_toTable10_end","P nrOfRot=Shoefactory.tables.table10.rot;");
			GCStep s4g = createTableSFC(doc,tr4g,5,1600,y,1);
			GCStep s4h = createTableSFC(doc,tr4h,10,1900,y,1);
			y+=250;
			GCTransition tr5a = doc.createTransition(700,y,"(goto==0 | goto==6) & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table2.rot)==0 & !Shoefactory.agvs.agv1.busy &JgrafSupervisor.get_T2 ");
			GCTransition tr5c = doc.createTransition(400,y,"goto==7 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table2.rot)==0 & !Shoefactory.agvs.agv1.busy");
			GCTransition tr5d = doc.createTransition(100,y,"S_toTable8_end.t>Shoefactory.time & !Shoefactory.tables.table8.fullSlot.get(nrOfRot) ");
			GCTransition tr5f = doc.createTransition(1300,y,"S_toTable10_end.t>Shoefactory.time & !Shoefactory.tables.table10.fullSlot.get(nrOfRot) ");
			GCTransition tr5g = doc.createTransition(1600,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",applyStaticMethod(\"org.supremica.external.shoefactory.Executor.FactoryExecutor\",\"fiveRot\",\"int\",\"int\",nrOfRot)-Shoefactory.tables.table5.rot)==9 & !Shoefactory.agvs.agv5.busy");
			GCTransition tr5h = doc.createTransition(1900,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table10.rot)==12 & !Shoefactory.agvs.agv6.busy");
			y+=50;
			GCStep s5a = doc.createStep(700,y,"S_toTableL_c","S Shoefactory.tables.table2.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv1.from2to1=1;");
			GCStep s5c = doc.createStep(400,y,"S_toTable7_c","S Shoefactory.tables.table2.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv1.from2to7=1;");
			GCStep s5d = doc.createStep(100,y,"S_atTable8","S currentTable = 8;\nS Shoefactory.tables.table8.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv2.busy=0;\nS moveReady=1;");
			GCStep s5f = doc.createStep(1300,y,"S_atTable10","S currentTable = 10;\nS Shoefactory.tables.table10.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv4.busy=0;\nS moveReady=1;");
			GCStep s5g = doc.createStep(1600,y,"S_toTable11_c","S Shoefactory.tables.table5.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv5.from5to11=1;");
			GCStep s5h = doc.createStep(1900,y,"S_toTable12_c","S Shoefactory.tables.table10.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv6.from10to12=1;");
			y+=250;
			GCTransition tr6a = doc.createTransition(700,y,"Shoefactory.agvs.agv1.atgoal");
			GCTransition tr6c = doc.createTransition(400,y,"Shoefactory.agvs.agv1.atgoal");
			GCTransition tr6g = doc.createTransition(1600,y,"Shoefactory.agvs.agv5.atgoal");
			GCTransition tr6h = doc.createTransition(1900,y,"Shoefactory.agvs.agv6.atgoal");
			y+=50;
			GCStep s6a = createTableSFC(doc,tr6a,1,700,y,0);
			GCStep s6c = doc.createStep(400,y,"S_toTable7_end","P nrOfRot=Shoefactory.tables.table7.rot;");
			GCStep s6g = doc.createStep(1600,y,"S_toTable11_end","P nrOfRot=Shoefactory.tables.table11.rot;");
			GCStep s6h = doc.createStep(1900,y,"S_toTable12_end","P nrOfRot=Shoefactory.tables.table12.rot;");
			y+=250;
			GCTransition tr7a = doc.createTransition(1000,y,"goto==0 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table1.rot)==0 ");
			GCTransition tr7b = doc.createTransition(700,y,"goto==6 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table1.rot)==0 ");
			GCTransition tr7c = doc.createTransition(400,y,"S_toTable7_end.t>Shoefactory.time & !Shoefactory.tables.table7.fullSlot.get(nrOfRot)");
			GCTransition tr7g = doc.createTransition(1600,y,"S_toTable11_end.t>Shoefactory.time & !Shoefactory.tables.table11.fullSlot.get(nrOfRot)");
			GCTransition tr7h = doc.createTransition(1900,y,"S_toTable12_end.t>Shoefactory.time & !Shoefactory.tables.table12.fullSlot.get(nrOfRot)");
			y+=50;
			GCStep s7a = doc.createStep(1000,y,"S_toTable0_d","S Shoefactory.tables.table1.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv0.from1to0=1;");
			GCStep s7b = doc.createStep(700,y,"S_toTable6_d","S Shoefactory.tables.table1.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv0.from1to6=1;");
			GCStep s7c = doc.createStep(400,y,"S_atTable7","S currentTable =7;\nS Shoefactory.tables.table7.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv1.busy=0;\nS moveReady=1;");
			GCStep s7g = doc.createStep(1600,y,"S_atTable11","S currentTable =11;\nS Shoefactory.tables.table11.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv5.busy=0;\nS moveReady=1;");
			GCStep s7h = doc.createStep(1900,y,"S_atTable12","S currentTable =12;\nS Shoefactory.tables.table12.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv6.busy=0;\nS moveReady=1;");
			y+=100;
			GCTransition tr8a = doc.createTransition(1000,y,"Shoefactory.agvs.agv0.atgoal");
			GCTransition tr8b = doc.createTransition(700,y,"Shoefactory.agvs.agv0.atgoal");
			y+=50;
			GCStep s8a = doc.createStep(1000,y,"S_toTable0_end","P nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getRota\",\"int\",\"int\",Shoefactory.tables.table0.rot);");
			GCStep s8b = doc.createStep(700,y,"S_toTable6_end","P nrOfRot=Shoefactory.tables.table6.rot;");
			y+=100;
			GCTransition tr9a = doc.createTransition(1000,y,"S_toTable0_end.t>Shoefactory.time & !Shoefactory.tables.table0.fullSlot.get(nrOfRot)");
			GCTransition tr9b = doc.createTransition(700,y,"S_toTable6_end.t>Shoefactory.time & !Shoefactory.tables.table6.fullSlot.get(nrOfRot)");
			y+=50;
			GCStep s9a = doc.createStep(1000,y,"S_atTable0","S currentTable = 0;\nS Shoefactory.tables.table0.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv0.busy=0;\nS moveReady=1;");
			GCStep s9b = doc.createStep(700,y,"S_atTable6","S currentTable = 6;\nS Shoefactory.tables.table6.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv0.busy=0;\nS moveReady=1;");
			y+=100;
			GCTransition trEnda = doc.createTransition(1000,y,"1");
			GCTransition trEndb = doc.createTransition(700,y,"1");
			GCTransition trEndc = doc.createTransition(400,y,"1");
			GCTransition trEndd = doc.createTransition(100,y,"1");
			GCTransition trEndf = doc.createTransition(1300,y,"1");
			GCTransition trEndg = doc.createTransition(1600,y,"1");
			GCTransition trEndh = doc.createTransition(1900,y,"1");
			y+=50;

			doc.connect(s0,tr02a);
			doc.connect(s0,tr02f);

			doc.connect(tr02a,s02a);
			doc.connect(tr02f,s02f);

			doc.connect(s02a,tr2a);
			doc.connect(s02f,tr2f);

			doc.connect(s2a,tr3a);
			doc.connect(s2a,tr3d);
			doc.connect(s2f,tr3f);
			doc.connect(s2f,tr3g);
			doc.connect(s2f,tr3h);

			doc.connect(tr3a,s3a);
			doc.connect(tr3d,s3d);
			doc.connect(tr3f,s3f);
			doc.connect(tr3g,s3g);
			doc.connect(tr3h,s3h);

			doc.connect(s3a,tr4a);
			doc.connect(s3d,tr4d);
			doc.connect(s3f,tr4f);
			doc.connect(s3g,tr4g);
			doc.connect(s3h,tr4h);

			doc.connect(tr4d,s4d);
			doc.connect(tr4f,s4f);

			doc.connect(s4a,tr5a);
			doc.connect(s4a,tr5c);
			doc.connect(s4d,tr5d);
			doc.connect(s4f,tr5f);
			doc.connect(s4g,tr5g);
			doc.connect(s4h,tr5h);

			doc.connect(tr5a,s5a);
			doc.connect(tr5c,s5c);
			doc.connect(tr5d,s5d);
			doc.connect(tr5f,s5f);
			doc.connect(tr5g,s5g);
			doc.connect(tr5h,s5h);

			doc.connect(s5a,tr6a);
			doc.connect(s5c,tr6c);
			doc.connect(s5g,tr6g);
			doc.connect(s5h,tr6h);

			doc.connect(tr6c,s6c);
			doc.connect(tr6g,s6g);
			doc.connect(tr6h,s6h);

			doc.connect(s6a,tr7a);
			doc.connect(s6a,tr7b);
			doc.connect(s6c,tr7c);
			doc.connect(s6g,tr7g);
			doc.connect(s6h,tr7h);

			doc.connect(tr7a,s7a);
			doc.connect(tr7b,s7b);
			doc.connect(tr7c,s7c);
			doc.connect(tr7g,s7g);
			doc.connect(tr7h,s7h);

			doc.connect(s7a,tr8a);
			doc.connect(s7b,tr8b);

			doc.connect(tr8a,s8a);
			doc.connect(tr8b,s8b);

			doc.connect(s8a,tr9a);
			doc.connect(s8b,tr9b);

			doc.connect(tr9a,s9a);
			doc.connect(tr9b,s9b);

			doc.connect(s9a,trEnda);
			doc.connect(s9b,trEndb);
			doc.connect(s7c,trEndc);
			doc.connect(s5d,trEndd);
			doc.connect(s5f,trEndf);
			doc.connect(s7g,trEndg);
			doc.connect(s7h,trEndh);

			doc.connect(trEnda,initialStep);
			doc.connect(trEndb,initialStep);
			doc.connect(trEndc,initialStep);
			doc.connect(trEndd,initialStep);
			doc.connect(trEndf,initialStep);
			doc.connect(trEndg,initialStep);
			doc.connect(trEndh,initialStep);
		}

		else if(currentTable==10)
		{
			GCTransition tr02a = doc.createTransition(700,y,"(goto==0 | goto==6 | goto==7 | goto==8 | goto==9) & nrOfRot==Shoefactory.tables.table10.rot & !Shoefactory.agvs.agv4.busy");
			GCTransition tr02g = doc.createTransition(400,y,"goto==11 & nrOfRot==Shoefactory.tables.table10.rot&! Shoefactory.agvs.agv4.busy");
			GCTransition tr02h = doc.createTransition(100,y,"goto==12 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table10.rot)==12");
			y+=50;
			GCStep s02a = doc.createStep(700,y,"S_toTableL_a","S Shoefactory.tables.table10.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv4.from10to4=1;");
			GCStep s02g = doc.createStep(400,y,"S_toTable11_a","S Shoefactory.tables.table10.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv4.from10to5=1;");
			GCStep s02h = doc.createStep(100,y,"S_toTable12_a","S Shoefactory.tables.table10.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv6.from10to12=1;");
			y+=100;
			GCTransition tr2a = doc.createTransition(700,y,"Shoefactory.agvs.agv4.atgoal" );
			GCTransition tr2g = doc.createTransition(400,y,"Shoefactory.agvs.agv4.atgoal");
			GCTransition tr2h = doc.createTransition(100,y,"Shoefactory.agvs.agv6.atgoal");
			y+=50;
			GCStep s2a = createTableSFC(doc,tr2a,4,700,y,0);
			GCStep s2g = createTableSFC(doc,tr2g,5,400,y,1);
			GCStep s2h = doc.createStep(100,y,"S_toTable12_end","P nrOfRot=Shoefactory.tables.table12.rot;");
			y+=250;
			GCTransition tr3a = doc.createTransition(1000,y,"(goto==0 | goto==6 | goto==7 | goto==8) & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table4.rot)==0 & !Shoefactory.agvs.agv3.busy");
			GCTransition tr3e = doc.createTransition(700,y,"goto==9 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table4.rot)==0&! Shoefactory.agvs.agv3.busy");
			GCTransition tr3g = doc.createTransition(400,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",applyStaticMethod(\"org.supremica.external.shoefactory.Executor.FactoryExecutor\",\"fiveRot\",\"int\",\"int\",nrOfRot)-Shoefactory.tables.table5.rot)==9 &! Shoefactory.agvs.agv5.busy");
			GCTransition tr3h = doc.createTransition(100,y,"S_toTable12_end.t>Shoefactory.time & !Shoefactory.tables.table12.fullSlot.get(nrOfRot) ");
			y+=50;
			GCStep s3a = doc.createStep(1000,y,"S_toTableL_b","S Shoefactory.tables.table4.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv3.from4to3=1;");
			GCStep s3e = doc.createStep(700,y,"S_toTable9_b","S Shoefactory.tables.table4.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv3.from4to9=1;");
			GCStep s3g = doc.createStep(400,y,"S_toTable11_b","S Shoefactory.tables.table5.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv5.from5to11=1;");
			GCStep s3h = doc.createStep(100,y,"S_atTable12","S currentTable =12;\nS Shoefactory.tables.table12.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv6.busy=0;\nS moveReady=1;");
			y+=100;
			GCTransition tr4a = doc.createTransition(1000,y,"Shoefactory.agvs.agv3.atgoal");
			GCTransition tr4e =	doc.createTransition(700,y,"Shoefactory.agvs.agv3.atgoal");
			GCTransition tr4g = doc.createTransition(400,y,"Shoefactory.agvs.agv5.atgoal");
			y+=50;
			GCStep s4a = createTableSFC(doc,tr4a,3,1000,y,0);
			GCStep s4e = doc.createStep(700,y,"S_toTable9_end","P nrOfRot=Shoefactory.tables.table9.rot;");
			GCStep s4g = doc.createStep(400,y,"S_toTable11_end","P nrOfRot=Shoefactory.tables.table11.rot;");
			y+=250;
			GCTransition tr5a = doc.createTransition(1300,y,"(goto==0 | goto==6 | goto==7) & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table3.rot)==0 & !Shoefactory.agvs.agv2.busy");
			GCTransition tr5d = doc.createTransition(1000,y,"goto==8 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table3.rot)==0&! Shoefactory.agvs.agv2.busy");
			GCTransition tr5e = doc.createTransition(700,y,"S_toTable9_end.t>Shoefactory.time &  !Shoefactory.tables.table9.fullSlot.get(nrOfRot) ");
			GCTransition tr5g = doc.createTransition(400,y,"S_toTable11_end.t>Shoefactory.time & !Shoefactory.tables.table11.fullSlot.get(nrOfRot) ");
			y+=50;
			GCStep s5a = doc.createStep(1300,y,"S_toTableL_c","S Shoefactory.tables.table3.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv2.from3to2=1;");
			GCStep s5d = doc.createStep(1000,y,"S_toTable8_c","S Shoefactory.tables.table3.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv2.from3to8=1;");
			GCStep s5e = doc.createStep(700,y,"S_atTable9","S currentTable =9;\nS Shoefactory.tables.table9.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv3.busy=0;\nS moveReady=1;");
			GCStep s5g = doc.createStep(400,y,"S_atTable11","S currentTable =11;\nS Shoefactory.tables.table11.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv5.busy=0;\nS moveReady=1;");
			y+=100;
			GCTransition tr6a = doc.createTransition(1300,y,"Shoefactory.agvs.agv2.atgoal");
			GCTransition tr6d = doc.createTransition(1000,y,"Shoefactory.agvs.agv2.atgoal");
			y+=50;
			GCStep s6a = createTableSFC(doc,tr6a,2,1300,y,0);
			GCStep s6d = doc.createStep(1000,y,"S_toTable8_end","P nrOfRot=Shoefactory.tables.table8.rot;");
			y+=250;
			GCTransition tr7a = doc.createTransition(1600,y,"(goto==0 | goto==6) & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table2.rot)==0 & !Shoefactory.agvs.agv1.busy");
			GCTransition tr7c = doc.createTransition(1300,y,"goto==7 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table2.rot)==0&! Shoefactory.agvs.agv1.busy");
			GCTransition tr7d = doc.createTransition(1000,y,"S_toTable8_end.t>Shoefactory.time & !Shoefactory.tables.table8.fullSlot.get(nrOfRot)");
			y+=50;
			GCStep s7a = doc.createStep(1600,y,"S_toTableL_d","S Shoefactory.tables.table2.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv1.from2to1=1;");
			GCStep s7c = doc.createStep(1300,y,"S_toTable7_d","S Shoefactory.tables.table2.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv1.from2to7=1;");
			GCStep s7d = doc.createStep(1000,y,"S_atTable8","S currentTable =8;\nS Shoefactory.tables.table8.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv2.busy=0;\nS moveReady=1;");
			y+=100;
			GCTransition tr8a = doc.createTransition(1600,y,"Shoefactory.agvs.agv1.atgoal");
			GCTransition tr8c = doc.createTransition(1300,y,"Shoefactory.agvs.agv1.atgoal");
			y+=50;
			GCStep s8a = createTableSFC(doc,tr8a,1,1600,y,0);
			GCStep s8c = doc.createStep(1300,y,"S_toTable7_end","P nrOfRot=Shoefactory.tables.table7.rot;");
			y+=250;
			GCTransition tr9a =	doc.createTransition(1900,y,"goto==0 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table1.rot)==0 & !Shoefactory.agvs.agv0.busy");
			GCTransition tr9b =	doc.createTransition(1600,y,"goto==6 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table1.rot)==0 & !Shoefactory.agvs.agv0.busy");
			GCTransition tr9c = doc.createTransition(1300,y,"S_toTable7_end.t>Shoefactory.time & !Shoefactory.tables.table7.fullSlot.get(nrOfRot)");
			y+=50;
			GCStep s9a = doc.createStep(1900,y,"S_toTable0_e","S Shoefactory.tables.table1.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv0.from1to0=1;");
			GCStep s9b = doc.createStep(1600,y,"S_toTable6_e","S Shoefactory.tables.table1.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv0.from1to6=1;");
			GCStep s9c = doc.createStep(1300,y,"S_atTable7","S currentTable =7;\nS Shoefactory.tables.table7.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv1.busy=0;\nS moveReady=1;");
			y+=100;
			GCTransition tr10a = doc.createTransition(1900,y,"Shoefactory.agvs.agv0.atgoal");
			GCTransition tr10b = doc.createTransition(1600,y,"Shoefactory.agvs.agv0.atgoal");
			y+=50;
			GCStep s10a = doc.createStep(1900,y,"S_toTable0_end","P nrOfRot=Shoefactory.tables.table0.rot;");
			GCStep s10b = doc.createStep(1600,y,"S_toTable6_end","P nrOfRot=Shoefactory.tables.table6.rot;");
			y+=100;
			GCTransition tr11a = doc.createTransition(1900,y,"S_toTable0_end.t>Shoefactory.time & !Shoefactory.tables.table0.fullSlot.get(nrOfRot)");
			GCTransition tr11b = doc.createTransition(1600,y,"S_toTable6_end.t>Shoefactory.time & !Shoefactory.tables.table6.fullSlot.get(nrOfRot)");
			y+=50;
			GCStep s11a = doc.createStep(1900,y,"S_atTable0","S currentTable =0;\nS Shoefactory.tables.table0.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv0.busy=0;\nS moveReady=1;");
			GCStep s11b = doc.createStep(1600,y,"S_atTable6","S currentTable =6;\nS Shoefactory.tables.table6.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv0.busy=0;\nS moveReady=1;");
			y+=100;
			GCTransition trEnda = doc.createTransition(1900,y,"1");
			GCTransition trEndb = doc.createTransition(1600,y,"1");
			GCTransition trEndc = doc.createTransition(1300,y,"1");
			GCTransition trEndd = doc.createTransition(1000,y,"1");
			GCTransition trEnde = doc.createTransition(700,y,"1");
			GCTransition trEndg = doc.createTransition(400,y,"1");
			GCTransition trEndh = doc.createTransition(100,y,"1");
			y+=50;

			doc.connect(s0,tr02a);
			doc.connect(s0,tr02g);
			doc.connect(s0,tr02h);

			doc.connect(tr02a,s02a);
			doc.connect(tr02g,s02g);
			doc.connect(tr02h,s02h);

			doc.connect(s02a,tr2a);
			doc.connect(s02g,tr2g);
			doc.connect(s02h,tr2h);

			doc.connect(tr2h,s2h);

			doc.connect(s2a,tr3a);
			doc.connect(s2a,tr3e);
			doc.connect(s2g,tr3g);
			doc.connect(s2h,tr3h);

			doc.connect(tr3a,s3a);
			doc.connect(tr3e,s3e);
			doc.connect(tr3g,s3g);
			doc.connect(tr3h,s3h);

			doc.connect(s3a,tr4a);
			doc.connect(s3e,tr4e);
			doc.connect(s3g,tr4g);

			doc.connect(tr4e,s4e);
			doc.connect(tr4g,s4g);

			doc.connect(s4a,tr5a);
			doc.connect(s4a,tr5d);
			doc.connect(s4e,tr5e);
			doc.connect(s4g,tr5g);

			doc.connect(tr5a,s5a);
			doc.connect(tr5d,s5d);
			doc.connect(tr5e,s5e);
			doc.connect(tr5g,s5g);

			doc.connect(s5a,tr6a);
			doc.connect(s5d,tr6d);

			doc.connect(tr6d,s6d);

			doc.connect(s6a,tr7a);
			doc.connect(s6a,tr7c);
			doc.connect(s6d,tr7d);

			doc.connect(tr7a,s7a);
			doc.connect(tr7c,s7c);
			doc.connect(tr7d,s7d);

			doc.connect(s7a,tr8a);
			doc.connect(s7c,tr8c);

			doc.connect(tr8c,s8c);

			doc.connect(s8a,tr9a);
			doc.connect(s8a,tr9b);
			doc.connect(s8c,tr9c);

			doc.connect(tr9a,s9a);
			doc.connect(tr9b,s9b);
			doc.connect(tr9c,s9c);

			doc.connect(s9a,tr10a);
			doc.connect(s9b,tr10b);

			doc.connect(tr10a,s10a);
			doc.connect(tr10b,s10b);

			doc.connect(s10a,tr11a);
			doc.connect(s10b,tr11b);

			doc.connect(tr11a,s11a);
			doc.connect(tr11b,s11b);

			doc.connect(s11a,trEnda);
			doc.connect(s11b,trEndb);
			doc.connect(s9c,trEndc);
			doc.connect(s7d,trEndd);
			doc.connect(s5e,trEnde);
			doc.connect(s5g,trEndg);
			doc.connect(s3h,trEndh);

			doc.connect(trEnda,initialStep);
			doc.connect(trEndb,initialStep);
			doc.connect(trEndc,initialStep);
			doc.connect(trEndd,initialStep);
			doc.connect(trEnde,initialStep);
			doc.connect(trEndg,initialStep);
			doc.connect(trEndh,initialStep);
		}

		else if(currentTable==11)
		{
			GCTransition tr02a = doc.createTransition(100,y,"(goto==0 | goto==6 | goto==7 | goto==8 | goto==9 | goto==10 | goto==11 | goto==12) & nrOfRot==Shoefactory.tables.table11.rot & !Shoefactory.agvs.agv5.busy");
			y+=50;
			GCStep s02a = doc.createStep(100,y,"S_toTableL_a","S Shoefactory.tables.table11.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv5.from11to5=1;");
			y+=100;
			GCTransition tr2a = doc.createTransition(100,y,"Shoefactory.agvs.agv5.atgoal" );
			y+=50;
			GCStep s2a = createTableSFC(doc,tr2a,5,100,y,0);
			y+=250;
			GCTransition tr3a = doc.createTransition(700,y,"(goto==0 | goto==6 | goto==7 | goto==8 | goto==9) & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",applyStaticMethod(\"org.supremica.external.shoefactory.Executor.FactoryExecutor\",\"fiveRotdown\",\"int\",\"int\",nrOfRot)-Shoefactory.tables.table5.rot)==0 & !Shoefactory.agvs.agv4.busy");
			GCTransition tr3f = doc.createTransition(100,y,"goto==10 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",applyStaticMethod(\"org.supremica.external.shoefactory.Executor.FactoryExecutor\",\"fiveRotdown\",\"int\",\"int\",nrOfRot)-Shoefactory.tables.table5.rot)==0 & !Shoefactory.agvs.agv4.busy");
			GCTransition tr3h = doc.createTransition(400,y,"goto==12 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",applyStaticMethod(\"org.supremica.external.shoefactory.Executor.FactoryExecutor\",\"fiveRotdown\",\"int\",\"int\",nrOfRot)-Shoefactory.tables.table5.rot)==0 & !Shoefactory.agvs.agv4.busy");
			y+=50;
			GCStep s3a = doc.createStep(700,y,"S_toTableL_b","S Shoefactory.tables.table5.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv4.from5to4=1;");
			GCStep s3f = doc.createStep(100,y,"S_toTable10_b","S Shoefactory.tables.table5.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv4.from5to10=1;");
			GCStep s3h = doc.createStep(400,y,"S_toTable12_b","S Shoefactory.tables.table5.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv4.from5to10=1;");
			y+=100;
			GCTransition tr4a = doc.createTransition(700,y,"Shoefactory.agvs.agv4.atgoal");
			GCTransition tr4f = doc.createTransition(100,y,"Shoefactory.agvs.agv4.atgoal");
			GCTransition tr4h = doc.createTransition(400,y,"Shoefactory.agvs.agv4.atgoal");
			y+=50;
			GCStep s4a = createTableSFC(doc,tr4a,4,700,y,0);
			GCStep s4f = doc.createStep(100,y,"S_toTable10_end","P nrOfRot=Shoefactory.tables.table10.rot;");
			GCStep s4h = createTableSFC(doc,tr4h,10,400,y,1);
			y+=250;
			GCTransition tr5a = doc.createTransition(1000,y,"(goto==0 | goto==6 | goto==7 | goto==8) & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table4.rot)==0 & !Shoefactory.agvs.agv3.busy");
			GCTransition tr5e = doc.createTransition(700,y,"goto==9 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table4.rot)==0 & !Shoefactory.agvs.agv3.busy");
			GCTransition tr5f = doc.createTransition(100,y,"S_toTable10_end.t>Shoefactory.time & !Shoefactory.tables.table10.fullSlot.get(nrOfRot)");
			GCTransition tr5h = doc.createTransition(400,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table10.rot)==12 & !Shoefactory.agvs.agv6.busy");
			y+=50;
			GCStep s5a = doc.createStep(1000,y,"S_toTableL_c","S Shoefactory.tables.table4.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv3.from4to3=1;");
			GCStep s5e = doc.createStep(700,y,"S_toTable9_c","S Shoefactory.tables.table4.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv3.from4to9=1;");
			GCStep s5f = doc.createStep(100,y,"S_atTable10","S currentTable = 10;\nS Shoefactory.tables.table10.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv4.busy=0;\nS moveReady=1;");
			GCStep s5h = doc.createStep(400,y,"S_toTable12_c","S Shoefactory.tables.table10.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv6.from10to12=1;");
			y+=100;
			GCTransition tr6a = doc.createTransition(1000,y,"Shoefactory.agvs.agv3.atgoal");
			GCTransition tr6e = doc.createTransition(700,y,"Shoefactory.agvs.agv3.atgoal");
			GCTransition tr6h = doc.createTransition(400,y,"Shoefactory.agvs.agv6.atgoal");
			y+=50;
			GCStep s6a = createTableSFC(doc,tr6a,3,1000,y,0);
			GCStep s6e = doc.createStep(700,y,"S_toTable9_end","P nrOfRot=Shoefactory.tables.table9.rot;");
			GCStep s6h = doc.createStep(400,y,"S_toTable12_end","P nrOfRot=Shoefactory.tables.table12.rot;");
			y+=250;
			GCTransition tr7a = doc.createTransition(1300,y,"(goto==0 | goto==6 | goto==7) & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table3.rot)==0 & !Shoefactory.agvs.agv2.busy");
			GCTransition tr7d = doc.createTransition(1000,y,"goto==8 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table3.rot)==0 & !Shoefactory.agvs.agv2.busy");
			GCTransition tr7e = doc.createTransition(700,y,"S_toTable9_end.t>Shoefactory.time & !Shoefactory.tables.table9.fullSlot.get(nrOfRot)");
			GCTransition tr7h = doc.createTransition(400,y,"S_toTable12_end.t>Shoefactory.time & !Shoefactory.tables.table12.fullSlot.get(nrOfRot)");
			y+=50;
			GCStep s7a = doc.createStep(1300,y,"S_toTableL_d","S Shoefactory.tables.table3.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv2.from3to2=1;");
			GCStep s7d = doc.createStep(1000,y,"S_toTable8_d","S Shoefactory.tables.table3.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv2.from3to8=1;");
			GCStep s7e = doc.createStep(700,y,"S_atTable9","S currentTable = 9;\nS Shoefactory.tables.table9.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv3.busy=0;\nS moveReady=1;");
			GCStep s7h = doc.createStep(400,y,"S_atTable12","S currentTable = 12;\nS Shoefactory.tables.table12.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv6.busy=0;\nS moveReady=1;");
			y+=100;
			GCTransition tr8a = doc.createTransition(1300,y,"Shoefactory.agvs.agv2.atgoal");
			GCTransition tr8d = doc.createTransition(1000,y,"Shoefactory.agvs.agv2.atgoal");
			y+=50;
			GCStep s8a = createTableSFC(doc,tr8a,2,1300,y,0);
			GCStep s8d = doc.createStep(1000,y,"S_toTable8_end","P nrOfRot=Shoefactory.tables.table8.rot;");
			y+=250;
			GCTransition tr9a = doc.createTransition(1600,y,"(goto==0 | goto==6) & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table2.rot)==0 & !Shoefactory.agvs.agv1.busy");
			GCTransition tr9c = doc.createTransition(1300,y,"goto==7 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table2.rot)==0 & !Shoefactory.agvs.agv1.busy");
			GCTransition tr9d = doc.createTransition(1000,y,"S_toTable8_end.t>Shoefactory.time & !Shoefactory.tables.table8.fullSlot.get(nrOfRot)");
			y+=50;
			GCStep s9a = doc.createStep(1600,y,"S_toTableL_e","S Shoefactory.tables.table2.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv1.from2to1=1;");
			GCStep s9c = doc.createStep(1300,y,"S_toTable7_e","S Shoefactory.tables.table2.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv1.from2to7=1;");
			GCStep s9d = doc.createStep(1000,y,"S_atTable8","S currentTable = 8;\nS Shoefactory.tables.table8.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv2.busy=0;\nS moveReady=1;");
			y+=100;
			GCTransition tr10a = doc.createTransition(1600,y,"Shoefactory.agvs.agv1.atgoal");
			GCTransition tr10c = doc.createTransition(1300,y,"Shoefactory.agvs.agv1.atgoal");
			y+=50;
			GCStep s10a = createTableSFC(doc,tr10a,1,1600,y,0);
			GCStep s10c = doc.createStep(1300,y,"S_toTable7_end","P nrOfRot=Shoefactory.tables.table7.rot;");
			y+=250;
			GCTransition tr11a = doc.createTransition(1900,y,"goto==0 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table1.rot)==0 & !Shoefactory.agvs.agv0.busy");
			GCTransition tr11b = doc.createTransition(1600,y,"goto==6 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table1.rot)==0 & !Shoefactory.agvs.agv0.busy");
			GCTransition tr11c = doc.createTransition(1300,y,"S_toTable7_end.t>Shoefactory.time & !Shoefactory.tables.table7.fullSlot.get(nrOfRot)");
			y+=50;
			GCStep s11a = doc.createStep(1900,y,"S_toTable0_f","S Shoefactory.tables.table1.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv0.from1to0=1;");
			GCStep s11b = doc.createStep(1600,y,"S_toTable6_f","S Shoefactory.tables.table1.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv0.from1to6=1;");
			GCStep s11c = doc.createStep(1300,y,"S_atTable7","S currentTable = 7;\nS Shoefactory.tables.table7.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv1.busy=0;\nS moveReady=1;");
			y+=100;
			GCTransition tr12a = doc.createTransition(1900,y,"Shoefactory.agvs.agv0.atgoal");
			GCTransition tr12b = doc.createTransition(1600,y,"Shoefactory.agvs.agv0.atgoal");
			y+=50;
			GCStep s12a = doc.createStep(1900,y,"S_toTable0_end","P nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getRota\",\"int\",\"int\",Shoefactory.tables.table0.rot);");
			GCStep s12b = doc.createStep(1600,y,"S_toTable6_end","P nrOfRot=Shoefactory.tables.table6.rot;");
			y+=100;
			GCTransition tr13a = doc.createTransition(1900,y,"S_toTable0_end.t>Shoefactory.time & !Shoefactory.tables.table0.fullSlot.get(nrOfRot)");
			GCTransition tr13b = doc.createTransition(1600,y,"S_toTable6_end.t>Shoefactory.time & !Shoefactory.tables.table6.fullSlot.get(nrOfRot)");
			y+=50;
			GCStep s13a = doc.createStep(1900,y,"S_atTable0","S currentTable = 0;\nS Shoefactory.tables.table0.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv0.busy=0;\nS moveReady=1;");
			GCStep s13b = doc.createStep(1600,y,"S_atTable6","S currentTable = 6;\nS Shoefactory.tables.table6.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv0.busy=0;\nS moveReady=1;");
			y+=250;
			GCTransition trEnda = doc.createTransition(1900,y,"1");
			GCTransition trEndb = doc.createTransition(1600,y,"1");
			GCTransition trEndc = doc.createTransition(1300,y,"1");
			GCTransition trEndd = doc.createTransition(1000,y,"1");
			GCTransition trEnde = doc.createTransition(700,y,"1");
			GCTransition trEndf = doc.createTransition(100,y,"1");
			GCTransition trEndh = doc.createTransition(400,y,"1");
			y+=50;

			doc.connect(s0,tr02a);

			doc.connect(tr02a,s02a);

			doc.connect(s02a,tr2a);

			doc.connect(s2a,tr3a);
			doc.connect(s2a,tr3f);
			doc.connect(s2a,tr3h);

			doc.connect(tr3a,s3a);
			doc.connect(tr3f,s3f);
			doc.connect(tr3h,s3h);

			doc.connect(s3a,tr4a);
			doc.connect(s3f,tr4f);
			doc.connect(s3h,tr4h);

			doc.connect(tr4f,s4f);

			doc.connect(s4a,tr5a);
			doc.connect(s4a,tr5e);
			doc.connect(s4f,tr5f);
			doc.connect(s4h,tr5h);

			doc.connect(tr5a,s5a);
			doc.connect(tr5e,s5e);
			doc.connect(tr5f,s5f);
			doc.connect(tr5h,s5h);

			doc.connect(s5a,tr6a);
			doc.connect(s5e,tr6e);
			doc.connect(s5h,tr6h);

			doc.connect(tr6e,s6e);
			doc.connect(tr6h,s6h);

			doc.connect(s6a,tr7a);
			doc.connect(s6a,tr7d);
			doc.connect(s6e,tr7e);
			doc.connect(s6h,tr7h);

			doc.connect(tr7a,s7a);
			doc.connect(tr7d,s7d);
			doc.connect(tr7e,s7e);
			doc.connect(tr7h,s7h);

			doc.connect(s7a,tr8a);
			doc.connect(s7d,tr8d);

			doc.connect(tr8d,s8d);

			doc.connect(s8a,tr9a);
			doc.connect(s8a,tr9c);
			doc.connect(s8d,tr9d);

			doc.connect(tr9a,s9a);
			doc.connect(tr9c,s9c);
			doc.connect(tr9d,s9d);

			doc.connect(s9a,tr10a);
			doc.connect(s9c,tr10c);

			doc.connect(tr10c,s10c);

			doc.connect(s10a,tr11a);
			doc.connect(s10a,tr11b);
			doc.connect(s10c,tr11c);

			doc.connect(tr11a,s11a);
			doc.connect(tr11b,s11b);
			doc.connect(tr11c,s11c);

			doc.connect(s11a,tr12a);
			doc.connect(s11b,tr12b);

			doc.connect(tr12a,s12a);
			doc.connect(tr12b,s12b);

			doc.connect(s12a,tr13a);
			doc.connect(s12b,tr13b);

			doc.connect(tr13a,s13a);
			doc.connect(tr13b,s13b);

			doc.connect(s13a,trEnda);
			doc.connect(s13b,trEndb);
			doc.connect(s11c,trEndc);
			doc.connect(s9d,trEndd);
			doc.connect(s7e,trEnde);
			doc.connect(s5f,trEndf);
			doc.connect(s7h,trEndh);

			doc.connect(trEnda,initialStep);
			doc.connect(trEndb,initialStep);
			doc.connect(trEndc,initialStep);
			doc.connect(trEndd,initialStep);
			doc.connect(trEnde,initialStep);
			doc.connect(trEndf,initialStep);
			doc.connect(trEndh,initialStep);
		}

		else if(currentTable==12)
		{
			GCTransition tr02a = doc.createTransition(400,y,"(goto==0 | goto==6 | goto==7 | goto==8 | goto==9 | goto==11) & nrOfRot==Shoefactory.tables.table12.rot & !Shoefactory.agvs.agv6.busy");
			GCTransition tr02f = doc.createTransition(100,y,"goto==10 & nrOfRot==Shoefactory.tables.table12.rot & !Shoefactory.agvs.agv6.busy");
			y+=50;
			GCStep s02a = doc.createStep(400,y,"S_toTableL_a","S Shoefactory.tables.table12.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv6.from12to10=1;");
			GCStep s02f = doc.createStep(100,y,"S_toTable10_a","S Shoefactory.tables.table12.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv6.from12to10=1;");
			y+=100;
			GCTransition tr2a = doc.createTransition(400,y,"Shoefactory.agvs.agv6.atgoal" );
			GCTransition tr2f = doc.createTransition(100,y,"Shoefactory.agvs.agv6.atgoal" );
			y+=50;
			GCStep s2a = createTableSFC(doc,tr2a,10,400,y,0);
			GCStep s2f = doc.createStep(100,y,"S_toTable10_end","P nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getRotc\",\"int\",\"int\",Shoefactory.tables.table10.rot);");
			y+=250;
			GCTransition tr3a = doc.createTransition(700,y,"(goto==0 | goto==6 | goto==7 | goto==8 | goto==9) & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table10.rot)==0 & !Shoefactory.agvs.agv4.busy");
			GCTransition tr3f = doc.createTransition(100,y,"S_toTable10_end.t>Shoefactory.time & !Shoefactory.tables.table10.fullSlot.get(nrOfRot) ");
			GCTransition tr3g = doc.createTransition(400,y,"goto==11 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table10.rot)==0 & !Shoefactory.agvs.agv4.busy");
			y+=50;
			GCStep s3a = doc.createStep(700,y,"S_toTableL_b","S Shoefactory.tables.table10.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv4.from10to4=1;");
			GCStep s3f = doc.createStep(100,y,"S_atTable10","S currentTable = 10;\nS Shoe"+shoeNr+".onTable10.from12=12; \nS Shoefactory.tables.table10.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv6.busy=0;\nS moveReady=1;");
			GCStep s3g = doc.createStep(400,y,"S_toTable11_b","S Shoefactory.tables.table10.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv4.from10to5=1;");
			y+=100;
			GCTransition tr4a = doc.createTransition(700,y,"Shoefactory.agvs.agv4.atgoal");
			GCTransition tr4g = doc.createTransition(400,y,"Shoefactory.agvs.agv4.atgoal");
			y+=50;
			GCStep s4a = createTableSFC(doc,tr4a,4,700,y,0);
			GCStep s4g = createTableSFC(doc,tr4g,5,400,y,1);
			y+=250;
			GCTransition tr5a = doc.createTransition(1000,y,"(goto==0 | goto==6 | goto==7 | goto==8) & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table4.rot)==0 & !Shoefactory.agvs.agv3.busy");
			GCTransition tr5e = doc.createTransition(700,y,"goto==9 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table4.rot)==0 & !Shoefactory.agvs.agv3.busy");
			GCTransition tr5g = doc.createTransition(400,y,"applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",applyStaticMethod(\"org.supremica.external.shoefactory.Executor.FactoryExecutor\",\"fiveRot\",\"int\",\"int\",nrOfRot)-Shoefactory.tables.table5.rot)==9 & !Shoefactory.agvs.agv5.busy");
			y+=50;
			GCStep s5a = doc.createStep(1000,y,"S_toTableL_c","S Shoefactory.tables.table4.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv3.from4to3=1;");
			GCStep s5e = doc.createStep(700,y,"S_toTable9_c","S Shoefactory.tables.table4.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv3.from4to9=1;");
			GCStep s5g = doc.createStep(400,y,"S_toTable11_c","S Shoefactory.tables.table5.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv5.from5to11=1;");
			y+=100;
			GCTransition tr6a = doc.createTransition(1000,y,"Shoefactory.agvs.agv3.atgoal");
			GCTransition tr6e = doc.createTransition(700,y,"Shoefactory.agvs.agv3.atgoal");
			GCTransition tr6g = doc.createTransition(400,y,"Shoefactory.agvs.agv5.atgoal");
			y+=50;
			GCStep s6a = createTableSFC(doc,tr6a,3,1000,y,0);
			GCStep s6e = doc.createStep(700,y,"S_toTable9_end","P nrOfRot=Shoefactory.tables.table9.rot;");
			GCStep s6g = doc.createStep(400,y,"S_toTable11_end","P nrOfRot=Shoefactory.tables.table11.rot;");
			y+=250;
			GCTransition tr7a = doc.createTransition(1300,y,"(goto==0 | goto==6 | goto==7) & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table3.rot)==0 & !Shoefactory.agvs.agv2.busy");
			GCTransition tr7d = doc.createTransition(1000,y,"goto==8 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table3.rot)==0 & !Shoefactory.agvs.agv2.busy");
			GCTransition tr7e = doc.createTransition(700,y,"S_toTable9_end.t>Shoefactory.time & !Shoefactory.tables.table9.fullSlot.get(nrOfRot)");
			GCTransition tr7g = doc.createTransition(400,y,"S_toTable11_end.t>Shoefactory.time & !Shoefactory.tables.table11.fullSlot.get(nrOfRot)");
			y+=50;
			GCStep s7a = doc.createStep(1300,y,"S_toTableL_d","S Shoefactory.tables.table3.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv2.from3to2=1;");
			GCStep s7d = doc.createStep(1000,y,"S_toTable8_d","S Shoefactory.tables.table3.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv2.from3to8=1;");
			GCStep s7e = doc.createStep(700,y,"S_atTable9","S currentTable = 9;\nS Shoefactory.tables.table9.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv3.busy=0;\nS moveReady=1;");
			GCStep s7g = doc.createStep(400,y,"S_atTable11","S currentTable = 11;\nS Shoefactory.tables.table11.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv5.busy=0;\nS moveReady=1;");
			y+=100;
			GCTransition tr8a = doc.createTransition(1300,y,"Shoefactory.agvs.agv2.atgoal");
			GCTransition tr8d = doc.createTransition(1000,y,"Shoefactory.agvs.agv2.atgoal");
			y+=50;
			GCStep s8a = createTableSFC(doc,tr8a,2,1300,y,0);
			GCStep s8d = doc.createStep(1000,y,"S_toTable8_end","P nrOfRot=Shoefactory.tables.table8.rot;");
			y+=250;
			GCTransition tr9a = doc.createTransition(1600,y,"(goto==0 | goto==6) & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table2.rot)==0 & !Shoefactory.agvs.agv1.busy");
			GCTransition tr9c = doc.createTransition(1300,y,"goto==7 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table2.rot)==0 & !Shoefactory.agvs.agv1.busy");
			GCTransition tr9d = doc.createTransition(1000,y,"S_toTable8_end.t>Shoefactory.time & !Shoefactory.tables.table8.fullSlot.get(nrOfRot)");
			y+=50;
			GCStep s9a = doc.createStep(1600,y,"S_toTableL_e","S Shoefactory.tables.table2.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv1.from2to1=1;");
			GCStep s9c = doc.createStep(1300,y,"S_toTable7_e","S Shoefactory.tables.table2.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv1.from2to7=1;");
			GCStep s9d = doc.createStep(1000,y,"S_atTable8","S currentTable = 8;\nS Shoefactory.tables.table8.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv2.busy=0;\nS moveReady=1;");
			y+=100;
			GCTransition tr10a = doc.createTransition(1600,y,"Shoefactory.agvs.agv1.atgoal");
			GCTransition tr10c = doc.createTransition(1300,y,"Shoefactory.agvs.agv1.atgoal");
			y+=50;
			GCStep s10a = createTableSFC(doc,tr10a,1,1600,y,0);
			GCStep s10c = doc.createStep(1300,y,"S_toTable7_end","P nrOfRot=Shoefactory.tables.table7.rot;");
			y+=250;
			GCTransition tr11a = doc.createTransition(1900,y,"goto==0 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table1.rot)==0 & !Shoefactory.agvs.agv0.busy");
			GCTransition tr11b = doc.createTransition(1600,y,"goto==6 & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",nrOfRot-Shoefactory.tables.table1.rot)==0 & !Shoefactory.agvs.agv0.busy");
			GCTransition tr11c = doc.createTransition(1300,y,"S_toTable7_end.t>Shoefactory.time & !Shoefactory.tables.table7.fullSlot.get(nrOfRot)");
			y+=50;
			GCStep s11a = doc.createStep(1900,y,"S_toTable0_f","S Shoefactory.tables.table1.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv0.from1to0=1;");
			GCStep s11b = doc.createStep(1600,y,"S_toTable6_f","S Shoefactory.tables.table1.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv0.from1to6=1;");
			GCStep s11c = doc.createStep(1300,y,"S_atTable7","S currentTable = 7;\nS Shoefactory.tables.table7.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv1.busy=0;\nS moveReady=1;");
			y+=100;
			GCTransition tr12a = doc.createTransition(1900,y,"Shoefactory.agvs.agv0.atgoal");
			GCTransition tr12b = doc.createTransition(1600,y,"Shoefactory.agvs.agv0.atgoal");
			y+=50;
			GCStep s12a = doc.createStep(1900,y,"S_toTable0_end","P nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getRota\",\"int\",\"int\",Shoefactory.tables.table0.rot);");
			GCStep s12b = doc.createStep(1600,y,"S_toTable6_end","P nrOfRot=Shoefactory.tables.table6.rot;");
			y+=100;
			GCTransition tr13a = doc.createTransition(1900,y,"S_toTable0_end.t>Shoefactory.time & !Shoefactory.tables.table0.fullSlot.get(nrOfRot)");
			GCTransition tr13b = doc.createTransition(1600,y,"S_toTable6_end.t>Shoefactory.time & !Shoefactory.tables.table6.fullSlot.get(nrOfRot)");
			y+=50;
			GCStep s13a = doc.createStep(1900,y,"S_atTable0","S currentTable = 0;\nS Shoefactory.tables.table0.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv0.busy=0;\nS moveReady=1;");
			GCStep s13b = doc.createStep(1600,y,"S_atTable6","S currentTable = 6;\nS Shoefactory.tables.table6.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv0.busy=0;\nS moveReady=1;");
			y+=250;
			GCTransition trEnda = doc.createTransition(1900,y,"1");
			GCTransition trEndb = doc.createTransition(1600,y,"1");
			GCTransition trEndc = doc.createTransition(1300,y,"1");
			GCTransition trEndd = doc.createTransition(1000,y,"1");
			GCTransition trEnde = doc.createTransition(700,y,"1");
			GCTransition trEndf = doc.createTransition(100,y,"1");
			GCTransition trEndg = doc.createTransition(400,y,"1");
			y+=50;

			doc.connect(s0,tr02a);
			doc.connect(s0,tr02f);

			doc.connect(tr02a,s02a);
			doc.connect(tr02f,s02f);

			doc.connect(s02a,tr2a);
			doc.connect(s02f,tr2f);

			doc.connect(tr2f,s2f);

			doc.connect(s2a,tr3a);
			doc.connect(s2f,tr3f);
			doc.connect(s2a,tr3g);

			doc.connect(tr3a,s3a);
			doc.connect(tr3f,s3f);
			doc.connect(tr3g,s3g);

			doc.connect(s3a,tr4a);
			doc.connect(s3g,tr4g);

			doc.connect(s4a,tr5a);
			doc.connect(s4a,tr5e);
			doc.connect(s4g,tr5g);

			doc.connect(tr5a,s5a);
			doc.connect(tr5e,s5e);
			doc.connect(tr5g,s5g);

			doc.connect(s5a,tr6a);
			doc.connect(s5e,tr6e);
			doc.connect(s5g,tr6g);

			doc.connect(tr6e,s6e);
			doc.connect(tr6g,s6g);

			doc.connect(s6a,tr7a);
			doc.connect(s6a,tr7d);
			doc.connect(s6e,tr7e);
			doc.connect(s6g,tr7g);

			doc.connect(tr7a,s7a);
			doc.connect(tr7d,s7d);
			doc.connect(tr7e,s7e);
			doc.connect(tr7g,s7g);

			doc.connect(s7a,tr8a);
			doc.connect(s7d,tr8d);

			doc.connect(tr8d,s8d);

			doc.connect(s8a,tr9a);
			doc.connect(s8a,tr9c);
			doc.connect(s8d,tr9d);

			doc.connect(tr9a,s9a);
			doc.connect(tr9c,s9c);
			doc.connect(tr9d,s9d);

			doc.connect(s9a,tr10a);
			doc.connect(s9c,tr10c);

			doc.connect(tr10c,s10c);

			doc.connect(s10a,tr11a);
			doc.connect(s10a,tr11b);
			doc.connect(s10c,tr11c);

			doc.connect(tr11a,s11a);
			doc.connect(tr11b,s11b);
			doc.connect(tr11c,s11c);

			doc.connect(s11a,tr12a);
			doc.connect(s11b,tr12b);

			doc.connect(tr12a,s12a);
			doc.connect(tr12b,s12b);

			doc.connect(s12a,tr13a);
			doc.connect(s12b,tr13b);

			doc.connect(tr13a,s13a);
			doc.connect(tr13b,s13b);

			doc.connect(s13a,trEnda);
			doc.connect(s13b,trEndb);
			doc.connect(s11c,trEndc);
			doc.connect(s9d,trEndd);
			doc.connect(s7e,trEnde);
			doc.connect(s3f,trEndf);
			doc.connect(s7g,trEndg);

			doc.connect(trEnda,initialStep);
			doc.connect(trEndb,initialStep);
			doc.connect(trEndc,initialStep);
			doc.connect(trEndd,initialStep);
			doc.connect(trEnde,initialStep);
			doc.connect(trEndf,initialStep);
			doc.connect(trEndg,initialStep);
		}
	}

	public static int askUser(int nr,int tNr,boolean s0,boolean s1,boolean s2,boolean s3,boolean s4,boolean s5,boolean s6,boolean s7,boolean s8,boolean s9,boolean s10,boolean s11,
								boolean s12,boolean s13,boolean s14,boolean s15,boolean s16,boolean s17,boolean s18,boolean s19,boolean s20,boolean s21,boolean s22,boolean s23)
	{
		boolean manualUse=false;


		JFrame frame = new JFrame();
		Object[] options={"0"};

		if(manualUse)
		{
			if(s0 || s1 || s2 || s3 || s4)
				options=newOptions(options,"6");
			if(s5 || s6 || s7 || s8)
				options=newOptions(options,"7");
			if(s9 || s10 || s11)
				options=newOptions(options,"8");
			if(s12 || s13 || s14 || s15)
				options=newOptions(options,"9");
			if(s20)
				options=newOptions(options,"10");
			if(s21 || s22 || s23)
				options=newOptions(options,"11");
			if(s16 || s17 || s18 || s19)
				options=newOptions(options,"12");

			int selection = JOptionPane.showOptionDialog(frame, "Select which table to visit","Shoe"+nr+"- currently at table "+tNr,JOptionPane.DEFAULT_OPTION,JOptionPane.QUESTION_MESSAGE,null,options,options[0]);

			if(options[selection].toString().compareTo("6")==0)
				return 6;
			if(options[selection].toString().compareTo("7")==0)
				return 7;
			if(options[selection].toString().compareTo("8")==0)
				return 8;
			if(options[selection].toString().compareTo("9")==0)
				return 9;
			if(options[selection].toString().compareTo("10")==0)
				return 10;
			if(options[selection].toString().compareTo("11")==0)
				return 11;
			if(options[selection].toString().compareTo("12")==0)
				return 12;
			else
				return 0;
		}
		else
		{
			if(s0 || s1 || s2 || s3 || s4)
				return 6;
			if(s5 || s6 || s7 || s8)
				return 7;
			if(s9 || s10 || s11)
				return 8;
			if(s12 || s13 || s14 || s15)
				return 9;
			if(s16 || s17 || s18 || s19)
				return 12;
			if(s20)
				return 10;
			if(s21 || s22 || s23)
				return 11;
			else
				return 0;
		}
	}

	public static Object[] newOptions(Object[] old, String s)
	{
		Object[] obj = new Object[old.length+1];
		System.arraycopy(old,0,obj,0,old.length);
		obj[old.length]=s;
		return obj;
	}

		public static int getIndex(int nr, int stat)
	{
		if(nr<stat)
			return 24-stat+nr;
		else
			return nr-stat;
	}

	public static int getRota(int r)
	{
		if(r<6)
			return r+6;
		else
			return r-6;
	}

	public static int getRotb(int r)
	{
		if(r<9)
			return r+3;
		else
			return r-9;
	}

	public static int getRotc(int r)
	{
		if(r<12)
			return r+12;
		else
			return r-12;
	}

	public GCDocument getShoe ()
	{
		return shoe ;
	}


}