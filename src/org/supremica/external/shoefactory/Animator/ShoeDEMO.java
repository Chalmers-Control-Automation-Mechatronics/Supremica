package org.supremica.external.shoefactory.Animator;

import java.awt.*;
import grafchart.sfc.*;

public class ShoeDEMO
{
	GCDocument shoe;
	private int yPos=50, shoeNr;
	private boolean stationVisit;
	private static int[] stationRot = {15,8};
	public boolean done;

	public ShoeDEMO(GCDocument doc, boolean sV, int nr)
	{
		shoe = doc;
		shoe.setWorkspaceName("Shoe"+nr);
		shoe.setFrameRectangle(new Rectangle(0,0,800,400));
		shoeNr=nr;
		stationVisit = sV;

		IntegerVariable iv1 = shoe.createIntegerVariable(300,50,"nrOfRot","0");
		IntegerVariable iv2 = shoe.createIntegerVariable(400,50,"currentTable","0");
		BooleanVariable bv2 = shoe.createBooleanVariable(500,50,"done","0");
		BooleanVariable sbv1 = shoe.createBooleanVariable(300,150,"station0","1");
		BooleanVariable sbv2 = shoe.createBooleanVariable(400,150,"station1","1");
		StringVariable sv = shoe.createStringVariable(500,150,"pointerString","");
		
		String [] events ={"put_T0L","put_T0R","put_T1","put_T2","put_T1_S0","put_T2_S1","get_T0L","get_T0R","get_T1","get_T2","get_T1_S0","get_T2_S1"}; 
	
		GCStepInitial shoeInitialStep = shoe.createInitialStep(100,yPos,"Start",";");
		yPos+=100;
		GCTransition shoeTR = shoe.createTransition(100,yPos,"1");
		yPos+=50;
			
		//create two different specifications
		if(stationVisit)
		{
			GCStep shoeS = shoe.createStep(100,yPos,"shoeS","S pointerString = \"ShoeControl.moveFromTable\"+currentTable+\".goto\";\nS pointerString^=1;");
			yPos+=100;
			GCTransition shoeTR0a = shoe.createTransition(100,yPos,"currentTable==1");
			yPos+=50;
			GCStep shoeS0a = shoe.createStep(100,yPos,"shoeS0a",";");
			yPos+=100;
			GCTransition shoeTR1a = shoe.createTransition(100,yPos,"ShoeControl.onTable1.Station0.x");
			yPos+=50;
			GCStep shoeS1a = shoe.createStep(100,yPos,"ShoeS1a",";");
			yPos+=100;
			GCTransition shoeTR2a = shoe.createTransition(100,yPos,"ShoefactoryDEMO.stations.station0.leaveStation");
			yPos+=50;
			GCStep shoeS2 = shoe.createStep(100,yPos,"S2","S pointerString = \"ShoeControl.moveFromTable\"+currentTable+\".goto\";\nS pointerString^=2;");
			yPos+=100;
			GCTransition shoeTR3a = shoe.createTransition(100,yPos,"currentTable==2");
			yPos+=50;
			GCStep shoeS3a = shoe.createStep(100,yPos,"shoeS2a",";");
			yPos+=100;
			GCTransition shoeTR4a = shoe.createTransition(100,yPos,"ShoeControl.onTable2.Station1.x");
			yPos+=50;
			GCStep shoeS4a = shoe.createStep(100,yPos,"ShoeS4a",";");
			yPos+=100;
			GCTransition shoeTR5a = shoe.createTransition(100,yPos,"ShoefactoryDEMO.stations.station1.leaveStation");
			yPos+=50;
			GCStep shoeS5 = shoe.createStep(100,yPos,"S5","S pointerString = \"ShoeControl.moveFromTable\"+currentTable+\".goto\";\nS pointerString^=0;");
			yPos+=100;

			shoe.connect(shoeInitialStep,shoeTR);
			shoe.connect(shoeTR,shoeS);
			shoe.connect(shoeS,shoeTR0a);
			shoe.connect(shoeTR0a,shoeS0a);
			shoe.connect(shoeS0a,shoeTR1a);
			shoe.connect(shoeTR1a,shoeS1a);
			shoe.connect(shoeS1a,shoeTR2a);
			shoe.connect(shoeTR2a,shoeS2);
			shoe.connect(shoeS2,shoeTR3a);
			shoe.connect(shoeTR3a,shoeS3a);
			shoe.connect(shoeS3a,shoeTR4a);
			shoe.connect(shoeTR4a,shoeS4a);
			shoe.connect(shoeS4a,shoeTR5a);
			shoe.connect(shoeTR5a,shoeS5);
		}
		else
		{
			GCStep shoeS = shoe.createStep(100,yPos,"shoeS","S pointerString = \"ShoeControl.moveFromTable\"+currentTable+\".goto\";\nS pointerString^=2;");
			yPos+=100;
			GCTransition shoeTR0a = shoe.createTransition(100,yPos,"currentTable==2");
			yPos+=50;
			GCStep shoeS0a = shoe.createStep(100,yPos,"shoeS0a",";");
			yPos+=100;
			GCTransition shoeTR1a = shoe.createTransition(100,yPos,"ShoeControl.onTable2.Station1.x");
			yPos+=50;
			GCStep shoeS1a = shoe.createStep(100,yPos,"ShoeS1a",";");
			yPos+=100;
			GCTransition shoeTR2a = shoe.createTransition(100,yPos,"ShoefactoryDEMO.stations.station1.leaveStation");
			yPos+=50;
			GCStep shoeS2 = shoe.createStep(100,yPos,"S2","S pointerString = \"ShoeControl.moveFromTable\"+currentTable+\".goto\";\nS pointerString^=1;");
			yPos+=100;
			GCTransition shoeTR3a = shoe.createTransition(100,yPos,"currentTable==1");
			yPos+=50;
			GCStep shoeS3a = shoe.createStep(100,yPos,"shoeS2a",";");
			yPos+=100;
			GCTransition shoeTR4a = shoe.createTransition(100,yPos,"ShoeControl.onTable1.Station0.x");
			yPos+=50;
			GCStep shoeS4a = shoe.createStep(100,yPos,"ShoeS4a",";");
			yPos+=100;
			GCTransition shoeTR5a = shoe.createTransition(100,yPos,"ShoefactoryDEMO.stations.station0.leaveStation");
			yPos+=50;
			GCStep shoeS5 = shoe.createStep(100,yPos,"S5","S pointerString = \"ShoeControl.moveFromTable\"+currentTable+\".goto\";\nS pointerString^=0;");
			yPos+=100;

			shoe.connect(shoeInitialStep,shoeTR);
			shoe.connect(shoeTR,shoeS);
			shoe.connect(shoeS,shoeTR0a);
			shoe.connect(shoeTR0a,shoeS0a);
			shoe.connect(shoeS0a,shoeTR1a);
			shoe.connect(shoeTR1a,shoeS1a);
			shoe.connect(shoeS1a,shoeTR2a);
			shoe.connect(shoeTR2a,shoeS2);
			shoe.connect(shoeS2,shoeTR3a);
			shoe.connect(shoeTR3a,shoeS3a);
			shoe.connect(shoeS3a,shoeTR4a);
			shoe.connect(shoeTR4a,shoeS4a);
			shoe.connect(shoeS4a,shoeTR5a);
			shoe.connect(shoeTR5a,shoeS5);
		}

		yPos=0;
		WorkspaceObject woSC = shoe.createWorkspaceObject(700,50,"ShoeControl");
		GCDocument shoeCtrl = woSC.getSubWorkspace();
		
		WorkspaceObject ev = shoe.createWorkspaceObject(600,50,"Events");
		GCDocument Even = ev.getSubWorkspace();
		
		BooleanVariable [] bolval =new BooleanVariable [events.length];
		
		for(int i=0;i<events.length;i++)
			bolval[i]=Even.createBooleanVariable(100+100*(i%10),50+100*(i/10),events[i],"0");

		// Create Grafcet
		GCStepInitial initialStep = shoeCtrl.createInitialStep(100,yPos,"Start","S JgrafSupervisor.nrOfShoes = JgrafSupervisor.nrOfShoes+1;");
		yPos+=100;
		GCTransition tr0 = shoeCtrl.createTransition(100,yPos,"!ShoefactoryDEMO.shoeInWarehouse");
		yPos+=50;

		GCStep s0 = createTableSFC(shoeCtrl,tr0,100,yPos);

		yPos+=250;
		GCTransition tr1 = shoeCtrl.createTransition(100,yPos,"1");
		yPos+=50;
		GCStep s1 = shoeCtrl.createStep(100,yPos,"S0",";");
		yPos+=100;

		GCTransition tr2a = shoeCtrl.createTransition(100,yPos+150,"currentTable==0 & (station0 | station1)");
		GCTransition tr2done = shoeCtrl.createTransition(0,yPos+150,"Events.get_T0L");

		GCStep s2a = shoeCtrl.createStep(100,yPos+200,"move_table0","S moveFromTable0.start=1;");
		GCStep s2done = shoeCtrl.createStep(0,yPos+200,"removeshoe","S ShoefactoryDEMO.tables.table0.fullSlot.set(nrOfRot,0);\nS done = applyStaticMethod(\"org.supremica.external.shoefactory.Executor.FactoryExecutorDEMO\",\"deleteShoe\",\"boolean\",\"int\","+shoeNr+");");
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

		GCTransition tr2b = shoeCtrl.createTransition(400,yPos,"currentTable==1");
		GCStep sON6 = shoeCtrl.createStep(400,yPos+50,"on_table1","S onTable1.start=1;");
		GCTransition trON6 = shoeCtrl.createTransition(400,yPos+150,"onTable1.leavetable");
		GCStep s2b = shoeCtrl.createStep(400,yPos+200,"move_table1","S moveFromTable1.start=1;");
		GCTransition tr3b = shoeCtrl.createTransition(400,yPos+300,"moveFromTable1.moveReady");

		shoeCtrl.connect(s1,tr2b);
		shoeCtrl.connect(tr2b,sON6);
		shoeCtrl.connect(sON6,trON6);
		shoeCtrl.connect(trON6,s2b);
		shoeCtrl.connect(s2b,tr3b);
		shoeCtrl.connect(tr3b,s1);

		WorkspaceObject wo1a = shoeCtrl.createWorkspaceObject(825,50,"moveFromTable1");
		GCDocument moveToTable1a = wo1a.getSubWorkspace();
		createSubdoc(moveToTable1a, 1);

		WorkspaceObject wo1b = shoeCtrl.createWorkspaceObject(825,150,"onTable1");
		GCDocument onTable1b = wo1b.getSubWorkspace();
		createonTable(onTable1b, 1);

		GCTransition tr2c = shoeCtrl.createTransition(700,yPos,"currentTable==2");
		GCStep sON7 = shoeCtrl.createStep(700,yPos+50,"on_table2","S onTable2.start=1;");
		GCTransition trON7 = shoeCtrl.createTransition(700,yPos+150,"onTable2.leavetable");
		GCStep s2c = shoeCtrl.createStep(700,yPos+200,"move_table2","S moveFromTable2.start=1;");
		GCTransition tr3c = shoeCtrl.createTransition(700,yPos+300,"moveFromTable2.moveReady");

		shoeCtrl.connect(s1,tr2c);
		shoeCtrl.connect(tr2c,sON7);
		shoeCtrl.connect(sON7,trON7);
		shoeCtrl.connect(trON7,s2c);
		shoeCtrl.connect(s2c,tr3c);
		shoeCtrl.connect(tr3c,s1);

		WorkspaceObject wo2a = shoeCtrl.createWorkspaceObject(950,50,"moveFromTable2");
		GCDocument moveToTable2a = wo2a.getSubWorkspace();
		createSubdoc(moveToTable2a, 2);

		WorkspaceObject wo2b = shoeCtrl.createWorkspaceObject(950,150,"onTable2");
		GCDocument onTable2b = wo2b.getSubWorkspace();
		createonTable(onTable2b, 2);
	}

	public GCStep createTableSFC(GCDocument doc, GCTransition in, int x, int y)
	{
		GCStep s0 = doc.createStep(x,y,"Wait_table0","S ShoefactoryDEMO.shoeInWarehouse=1;\nP nrOfRot=ShoefactoryDEMO.tables.table0.rot;");
		y+=100;
		GCTransition tr0 = doc.createTransition(x,y,"Events.put_T0L");
		y+=50;
		GCStep s1 = doc.createStep(x,y,"Put_table0","S ShoefactoryDEMO.shoeInWarehouse=0;\nS ShoefactoryDEMO.tables.table0.fullSlot.set(nrOfRot,1);");
		y+=100;

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

		if(currentTable==1)
		{
			GCStepInitial initialStep = doc.createInitialStep(100,y,"Start",";");
			y+=100;
			GCTransition tr0 = doc.createTransition(100,y,"start");
			y+=50;
			GCStep s0 = doc.createStep(100,y,"S0","S start=0;");
			y+=100;
			GCTransition tr2b = doc.createTransition(100,y,"Events.get_T1_S0 & station0");
			y+=50;
			GCStep s1b = doc.createStep(100,y,"Station0","S ShoefactoryDEMO.stations.station0.enterStation=1;\nS ShoefactoryDEMO.tables.table1.fullSlot.set(nrOfRot,0);");
			y+=100;
			GCTransition tr3b = doc.createTransition(100,y,"Events.put_T1_S0");
			y+=50;
			GCStep s2b = doc.createStep(300,y,"S2","S ShoefactoryDEMO.stations.station0.leaveStation=1;\nS station0=0;\nS ShoefactoryDEMO.tables.table1.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.ShoeDEMO\",\"getIndex\",\"int\",\"int\",ShoefactoryDEMO.tables.table1.rot,\"int\","+stationRot[0] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.ShoeDEMO\",\"getIndex\",\"int\",\"int\",ShoefactoryDEMO.tables.table1.rot,\"int\","+stationRot[0] +");\nS leavetable=1;");
			y+=100;
			GCTransition tr4b = doc.createTransition(300,y,"1");
			y+=50;
			
			doc.connect(initialStep,tr0);
			doc.connect(tr0,s0);
			doc.connect(s0,tr2b);
			doc.connect(tr2b,s1b);
			doc.connect(s1b,tr3b);
			doc.connect(tr3b,s2b);
			doc.connect(s2b,tr4b);
			doc.connect(tr4b,initialStep);
		}

		if(currentTable==2)
		{
			GCStepInitial initialStep = doc.createInitialStep(100,y,"Start",";");
			y+=100;
			GCTransition tr0 = doc.createTransition(100,y,"start");
			y+=50;
			GCStep s0 = doc.createStep(100,y,"S0","S start=0;");
			y+=100;
			GCTransition tr2c = doc.createTransition(100,y,"Events.get_T2_S1 & station1");
			y+=50;
			GCStep s1c = doc.createStep(100,y,"Station1","S ShoefactoryDEMO.stations.station1.enterStation=1;\nS ShoefactoryDEMO.tables.table2.fullSlot.set(nrOfRot,0);");
			y+=100;
			GCTransition tr3c = doc.createTransition(100,y,"Events.put_T2_S1");
			y+=50;
			GCStep s2c = doc.createStep(100,y,"S2","S ShoefactoryDEMO.stations.station1.leaveStation=1;\nS station1=0;\nS ShoefactoryDEMO.tables.table2.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.ShoeDEMO\",\"getIndex\",\"int\",\"int\",ShoefactoryDEMO.tables.table2.rot,\"int\","+stationRot[1] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.ShoeDEMO\",\"getIndex\",\"int\",\"int\",ShoefactoryDEMO.tables.table2.rot,\"int\","+stationRot[1] +");\nS leavetable=1;");
			y+=100;
			GCTransition tr4c = doc.createTransition(100,y,"1");
			y+=50;
			
			doc.connect(initialStep,tr0);
			doc.connect(tr0,s0);
			doc.connect(s0,tr2c);
			doc.connect(tr2c,s1c);
			doc.connect(s1c,tr3c);
			doc.connect(tr3c,s2c);
			doc.connect(s2c,tr4c);
			doc.connect(tr4c,initialStep);
		}
	}

	public void createSubdoc(GCDocument doc, int currentTable)
	{
		int y=0;

		//create subdoc
		BooleanVariable bv1 = doc.createBooleanVariable(500,50,"start","0");
		BooleanVariable bv2 = doc.createBooleanVariable(600,50,"moveReady","0");
		IntegerVariable iv1 = doc.createIntegerVariable(700,50,"goto","1");

		GCStepInitial initialStep = doc.createInitialStep(100,y,"S_toTable"+currentTable+"_end","S moveReady=0;");
		y+=100;
		GCTransition tr0 = doc.createTransition(100,y,"start");
		y+=50;
		GCStep s0 = doc.createStep(100,y,"S0","S start=0;");
		y+=100;
		doc.connect(initialStep,tr0);
		doc.connect(tr0,s0);

		if(currentTable==0)
		{
			GCTransition tr02b = doc.createTransition(400,y,"Events.get_T0R & goto==2");
			GCTransition tr02c = doc.createTransition(100,y,"Events.get_T0R & goto==1");
			y+=50;
			GCStep s02b = doc.createStep(400,y,"move_table2","S ShoefactoryDEMO.tables.table0.fullSlot.set(nrOfRot,0);\nS ShoefactoryDEMO.agvs.agv0.from0to2=1;");
			GCStep s02c = doc.createStep(100,y,"move_table1","S ShoefactoryDEMO.tables.table0.fullSlot.set(nrOfRot,0);\nS ShoefactoryDEMO.agvs.agv0.from0to1=1;");
			y+=100;
			GCTransition tr2b = doc.createTransition(400,y,"ShoefactoryDEMO.agvs.agv0.atgoal");
			GCTransition tr2c = doc.createTransition(100,y,"ShoefactoryDEMO.agvs.agv0.atgoal");
			y+=50;
			GCStep s2b = doc.createStep(400,y,"S_toTable2_end","P nrOfRot=ShoefactoryDEMO.tables.table2.rot;");
			GCStep s2c = doc.createStep(100,y,"S_toTable1_end","P nrOfRot=ShoefactoryDEMO.tables.table1.rot;");
			y+=250;
			GCTransition tr3b = doc.createTransition(400,y,"Events.put_T2");
			GCTransition tr3c = doc.createTransition(100,y,"Events.put_T1");
			y+=50;
			GCStep s3b = doc.createStep(400,y,"S_atTable2","S currentTable = 2;\nS ShoefactoryDEMO.tables.table2.fullSlot.set(nrOfRot,1);\nS ShoefactoryDEMO.agvs.agv0.busy=0;\nS moveReady=1;");
			GCStep s3c = doc.createStep(100,y,"S_atTable1","S currentTable = 1;\nS ShoefactoryDEMO.tables.table1.fullSlot.set(nrOfRot,1);\nS ShoefactoryDEMO.agvs.agv0.busy=0;\nS moveReady=1;");
			y+=100;
			GCTransition trEndb = doc.createTransition(400,y,"1");
			GCTransition trEndc = doc.createTransition(100,y,"1");
			y+=50;

			doc.connect(s0,tr02b);
			doc.connect(s0,tr02c);

			doc.connect(tr02b,s02b);
			doc.connect(tr02c,s02c);

			doc.connect(s02b,tr2b);
			doc.connect(s02c,tr2c);

			doc.connect(tr2b,s2b);
			doc.connect(tr2c,s2c);

			doc.connect(s2b,tr3b);
			doc.connect(s2c,tr3c);

			doc.connect(tr3b,s3b);
			doc.connect(tr3c,s3c);

			doc.connect(s3b,trEndb);
			doc.connect(s3c,trEndc);

			doc.connect(trEndb,initialStep);
			doc.connect(trEndc,initialStep);
		}

		if(currentTable==1)
		{
			GCTransition tr02b = doc.createTransition(100,y,"Events.get_T1 & goto==0");
			GCTransition tr02c = doc.createTransition(700,y,"Events.get_T1 & goto==2");
			y+=50;
			GCStep s02b = doc.createStep(100,y,"move_table0","S ShoefactoryDEMO.tables.table1.fullSlot.set(nrOfRot,0);\nS ShoefactoryDEMO.agvs.agv0.from1to0=1;");
			GCStep s02c = doc.createStep(700,y,"move_table2","S ShoefactoryDEMO.tables.table1.fullSlot.set(nrOfRot,0);\nS ShoefactoryDEMO.agvs.agv0.from1to2=1;");
			y+=100;
			GCTransition tr2b = doc.createTransition(100,y,"ShoefactoryDEMO.agvs.agv0.atgoal");
			GCTransition tr2c = doc.createTransition(700,y,"ShoefactoryDEMO.agvs.agv0.atgoal");
			y+=50;
			GCStep s2b = doc.createStep(100,y,"S_toTable0_end","P nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.ShoeDEMO\",\"getRota\",\"int\",\"int\",ShoefactoryDEMO.tables.table0.rot);");
			GCStep s2c = doc.createStep(700,y,"S_toTable2_end","P nrOfRot=ShoefactoryDEMO.tables.table2.rot;");
			y+=250;
			GCTransition tr3b = doc.createTransition(100,y,"Events.put_T0R");
			GCTransition tr3c = doc.createTransition(700,y,"Events.put_T2");
			y+=50;
			GCStep s3b = doc.createStep(100,y,"S_atTable0","S currentTable = 0;\nS ShoefactoryDEMO.tables.table0.fullSlot.set(nrOfRot,1);\nS ShoefactoryDEMO.agvs.agv0.busy=0;\nS moveReady=1;");
			GCStep s3c = doc.createStep(700,y,"S_atTable2","S currentTable = 2;\nS ShoefactoryDEMO.tables.table2.fullSlot.set(nrOfRot,1);\nS ShoefactoryDEMO.agvs.agv0.busy=0;\nS moveReady=1;");
			y+=100;
			GCTransition trEndb = doc.createTransition(100,y,"1");
			GCTransition trEndc = doc.createTransition(700,y,"1");
			y+=50;

			doc.connect(s0,tr02b);
			doc.connect(s0,tr02c);

			doc.connect(tr02b,s02b);
			doc.connect(tr02c,s02c);

			doc.connect(s02b,tr2b);
			doc.connect(s02c,tr2c);

			doc.connect(tr2b,s2b);
			doc.connect(tr2c,s2c);

			doc.connect(s2b,tr3b);
			doc.connect(s2c,tr3c);

			doc.connect(tr3b,s3b);
			doc.connect(tr3c,s3c);

			doc.connect(s3b,trEndb);
			doc.connect(s3c,trEndc);

			doc.connect(trEndb,initialStep);
			doc.connect(trEndc,initialStep);
		}

		if(currentTable==2)
		{
			GCTransition tr02b = doc.createTransition(100,y,"Events.get_T2 & goto==0");
			GCTransition tr02c = doc.createTransition(400,y,"Events.get_T2 & goto==1");
			y+=50;
			GCStep s02b = doc.createStep(100,y,"move_table0","S ShoefactoryDEMO.tables.table2.fullSlot.set(nrOfRot,0);\nS ShoefactoryDEMO.agvs.agv0.from2to0=1;");
			GCStep s02c = doc.createStep(400,y,"move_table1","S ShoefactoryDEMO.tables.table2.fullSlot.set(nrOfRot,0);\nS ShoefactoryDEMO.agvs.agv0.from2to1=1;");
			y+=100;
			GCTransition tr2b = doc.createTransition(100,y,"ShoefactoryDEMO.agvs.agv0.atgoal");
			GCTransition tr2c = doc.createTransition(400,y,"ShoefactoryDEMO.agvs.agv0.atgoal");
			y+=50;
			GCStep s2b = doc.createStep(100,y,"S_toTable0_end","P nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.ShoeDEMO\",\"getRota\",\"int\",\"int\",ShoefactoryDEMO.tables.table0.rot);");
			GCStep s2c = doc.createStep(400,y,"S_toTable1_end","P nrOfRot=ShoefactoryDEMO.tables.table1.rot;");
			y+=250;
			GCTransition tr3b = doc.createTransition(100,y,"Events.put_T0R");
			GCTransition tr3c = doc.createTransition(400,y,"Events.put_T1");
			y+=50;
			GCStep s3b = doc.createStep(100,y,"S_atTable0","S currentTable = 0;\nS ShoefactoryDEMO.tables.table0.fullSlot.set(nrOfRot,1);\nS ShoefactoryDEMO.agvs.agv0.busy=0;\nS moveReady=1;");
			GCStep s3c = doc.createStep(400,y,"S_atTable1","S currentTable = 1;\nS ShoefactoryDEMO.tables.table1.fullSlot.set(nrOfRot,1);\nS ShoefactoryDEMO.agvs.agv0.busy=0;\nS moveReady=1;");
			y+=100;
			GCTransition trEndb = doc.createTransition(100,y,"1");
			GCTransition trEndc = doc.createTransition(400,y,"1");
			y+=50;

			doc.connect(s0,tr02b);
			doc.connect(s0,tr02c);

			doc.connect(tr02b,s02b);
			doc.connect(tr02c,s02c);

			doc.connect(s02b,tr2b);
			doc.connect(s02c,tr2c);

			doc.connect(tr2b,s2b);
			doc.connect(tr2c,s2c);

			doc.connect(s2b,tr3b);
			doc.connect(s2c,tr3c);

			doc.connect(tr3b,s3b);
			doc.connect(tr3c,s3c);

			doc.connect(s3b,trEndb);
			doc.connect(s3c,trEndc);

			doc.connect(trEndb,initialStep);
			doc.connect(trEndc,initialStep);
		}
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

	public GCDocument getShoe ()
	{
		return shoe ;
	}
	
	public static int getStationRot(int index)
	{
		return stationRot[index];
	}
}