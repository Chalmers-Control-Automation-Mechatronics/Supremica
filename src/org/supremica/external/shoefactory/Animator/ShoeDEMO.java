package org.supremica.external.shoefactory.Animator;

import java.awt.*;
import grafchart.sfc.*;

public class ShoeDEMO
{
	GCDocument shoe;
	private int yPos=0, stepID=0, shoeNr;
	private boolean[] stationVisit;
	private int[] stationRot = {15,8};
	public boolean done;

	public ShoeDEMO(GCDocument doc, boolean[] sV, int nr)
	{
		shoe = doc;
		shoe.setWorkspaceName("Shoe"+nr);
		shoe.setFrameRectangle(new Rectangle(0,0,800,800));
		shoeNr=nr;
		stationVisit = sV;

		IntegerVariable iv1 = shoe.createIntegerVariable(300,50,"nrOfRot","0");
		IntegerVariable iv2 = shoe.createIntegerVariable(400,50,"currentTable","0");
		BooleanVariable bv2 = shoe.createBooleanVariable(500,50,"done","0");
		BooleanVariable sbv1 = shoe.createBooleanVariable(300,150,"station0","1");
		BooleanVariable sbv2 = shoe.createBooleanVariable(400,150,"station1","1");
		StringVariable sv = shoe.createStringVariable(600,150,"pointerString","");

		GCStepInitial shoeInitialStep = shoe.createInitialStep(100,yPos,"Start","S pointerString = \"ShoeControl.moveFromTable\"+currentTable+\".goto\";\nS pointerString^=1;");
		yPos+=250;

	//create two different specifications
		if(stationVisit[2])
		{
			GCTransition shoeTR0a = shoe.createTransition(100,yPos,"currentTable==1");
			yPos+=50;
			GCStep shoeS0a = shoe.createStep(100,yPos,"shoeS0a",";");
			yPos+=100;
			GCTransition shoeTR1a = shoe.createTransition(100,yPos,"Shoefactory.stations.station0.enterStation");
			yPos+=50;
			GCStep shoeS1a = shoe.createStep(100,yPos,"ShoeS1a",";");
			yPos+=100;
			GCTransition shoeTR2a = shoe.createTransition(100,yPos,"Shoefactory.stations.station0.leaveStation");
			yPos+=50;
			GCStep shoeS2 = shoe.createStep(100,yPos,"S2","S pointerString = \"ShoeControl.moveFromTable\"+currentTable+\".goto\";\nS pointerString^=2;");
			yPos+=100;
			GCTransition shoeTR3a = shoe.createTransition(100,yPos,"currentTable==2");
			yPos+=50;
			GCStep shoeS3a = shoe.createStep(100,yPos,"shoeS2a",";");
			yPos+=100;
			GCTransition shoeTR4a = shoe.createTransition(100,yPos,"Shoefactory.stations.station1.enterStation");
			yPos+=50;
			GCStep shoeS4a = shoe.createStep(100,yPos,"ShoeS4a",";");
			yPos+=100;
			GCTransition shoeTR5a = shoe.createTransition(100,yPos,"Shoefactory.stations.station1.leaveStation");
			yPos+=50;
			GCStep shoeS5 = shoe.createStep(100,yPos,"S5","S pointerString = \"ShoeControl.moveFromTable\"+currentTable+\".goto\";\nS pointerString^=0;");
			yPos+=100;
			GCTransition shoeTR6 = shoe.createTransition(100,yPos,"currentTable==0");
			yPos+=50;
			GCStep shoeS6 = shoe.createStep(100,yPos,"shoeS6",";");
			yPos+=100;

			shoe.connect(shoeInitialStep,shoeTR0a);
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
			shoe.connect(shoeS5,shoeTR6);
			shoe.connect(shoeTR6,shoeS6);
		}
		else
		{
			GCTransition shoeTR0a = shoe.createTransition(100,yPos,"currentTable==2");
			yPos+=50;
			GCStep shoeS0a = shoe.createStep(100,yPos,"shoeS0a",";");
			yPos+=100;
			GCTransition shoeTR1a = shoe.createTransition(100,yPos,"Shoefactory.stations.station1.enterStation");
			yPos+=50;
			GCStep shoeS1a = shoe.createStep(100,yPos,"ShoeS1a",";");
			yPos+=100;
			GCTransition shoeTR2a = shoe.createTransition(100,yPos,"Shoefactory.stations.station1.leaveStation");
			yPos+=50;
			GCStep shoeS2 = shoe.createStep(100,yPos,"S2","S pointerString = \"ShoeControl.moveFromTable\"+currentTable+\".goto\";\nS pointerString^=1;");
			yPos+=100;
			GCTransition shoeTR3a = shoe.createTransition(100,yPos,"currentTable==1");
			yPos+=50;
			GCStep shoeS3a = shoe.createStep(100,yPos,"shoeS2a",";");
			yPos+=100;
			GCTransition shoeTR4a = shoe.createTransition(100,yPos,"Shoefactory.stations.station0.enterStation");
			yPos+=50;
			GCStep shoeS4a = shoe.createStep(100,yPos,"ShoeS4a",";");
			yPos+=100;
			GCTransition shoeTR5a = shoe.createTransition(100,yPos,"Shoefactory.stations.station0.leaveStation");
			yPos+=50;
			GCStep shoeS5 = shoe.createStep(100,yPos,"S5","S pointerString = \"ShoeControl.moveFromTable\"+currentTable+\".goto\";\nS pointerString^=0;");
			yPos+=100;
			GCTransition shoeTR6 = shoe.createTransition(100,yPos,"currentTable==0");
			yPos+=50;
			GCStep shoeS6 = shoe.createStep(100,yPos,"shoeS6",";");
			yPos+=100;

			shoe.connect(shoeInitialStep,shoeTR0a);
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
			shoe.connect(shoeS5,shoeTR6);
			shoe.connect(shoeTR6,shoeS6);
		}

		yPos=0;
		WorkspaceObject woSC = shoe.createWorkspaceObject(700,50,"ShoeControl");
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
		GCStep s1 = shoeCtrl.createStep(100,yPos,"S1","S activeSteps.onStep8=1;\nX activeSteps.onStep8=0;");
		yPos+=100;

		GCTransition tr2a = shoeCtrl.createTransition(100,yPos+150,"currentTable==0 & (station0 | station1)");
		GCTransition tr2done = shoeCtrl.createTransition(0,yPos+150,"JgrafSupervisor.get_T0L");

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

		WorkspaceObject woAS = shoe.createWorkspaceObject(800,50,"activeSteps");
		GCDocument as = woAS.getSubWorkspace();

		BooleanVariable as0 = as.createBooleanVariable(100,50,"onStep0","0");
		BooleanVariable as1 = as.createBooleanVariable(200,50,"onStep1","0");
		BooleanVariable as2 = as.createBooleanVariable(300,50,"onStep2","0");
		BooleanVariable as3 = as.createBooleanVariable(400,50,"onStep3","0");
		BooleanVariable as4 = as.createBooleanVariable(500,50,"onStep4","0");
		BooleanVariable as5 = as.createBooleanVariable(600,50,"onStep5","0");
		BooleanVariable as6 = as.createBooleanVariable(100,150,"onStep6","0");
		BooleanVariable as7 = as.createBooleanVariable(200,150,"onStep7","0");
		BooleanVariable as8 = as.createBooleanVariable(300,150,"onStep8","0");
		BooleanVariable as9 = as.createBooleanVariable(400,150,"onStep9","0");
		BooleanVariable as10 = as.createBooleanVariable(500,150,"onStep10","0");
		BooleanVariable as11 = as.createBooleanVariable(600,50,"onStep11","0");

		if(stationVisit[0])
		{
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

			WorkspaceObject wo1 = shoeCtrl.createWorkspaceObject(825,50,"moveFromTable1");
			GCDocument moveToTable1 = wo1.getSubWorkspace();
			createSubdoc(moveToTable1, 1);

			WorkspaceObject wo2 = shoeCtrl.createWorkspaceObject(825,150,"onTable1");
			GCDocument onTable1 = wo2.getSubWorkspace();
			createonTable(onTable1, 1);
		}

		if(stationVisit[1] || stationVisit[2])
		{
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

			WorkspaceObject wo = shoeCtrl.createWorkspaceObject(950,50,"moveFromTable2");
			GCDocument moveToTable2 = wo.getSubWorkspace();
			createSubdoc(moveToTable2, 2);

			WorkspaceObject wo2 = shoeCtrl.createWorkspaceObject(950,150,"onTable2");
			GCDocument onTable2 = wo2.getSubWorkspace();
			createonTable(onTable2, 2);
		}
	}

	public GCStep createTableSFC(GCDocument doc, GCTransition in, int tableNr, int x, int y, int dir)
	{
		//dir=1 when from left and dir=0 from right
		String s0text="S activeSteps.onStep11=1;\nP nrOfRot=Shoefactory.tables.table"+tableNr+".rot;\nX activeSteps.onStep11=0;";
		String s1text="S Shoefactory.tables.table"+tableNr+".fullSlot.set(nrOfRot,1);";

		if(tableNr==0)
		{
			s0text="S Shoefactory.shoeInWarehouse=1;\n"+s0text;
			s1text="S Shoefactory.shoeInWarehouse=0;\n"+s1text;
		}

		if(tableNr==1 || tableNr==2)
		{
			s1text="S Shoefactory.Plant.agv0Slot.setVisible(0);\nS Shoefactory.agvs.agv0.busy=0;\n"+s1text;
		}

		GCStep s0 = doc.createStep(x,y,"Wait"+stepID+"_table"+tableNr,s0text);
		y+=100;
		GCTransition tr0 = doc.createTransition(x,y,"JgrafSupervisor.put_T"+tableNr+"L");
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

		if(currentTable==1)
		{
			IntegerVariable iv0 = doc.createIntegerVariable(400,50,"workTime0","2");

			GCStepInitial initialStep = doc.createInitialStep(100,y,"Start","P syncTabrot=Shoefactory.tables.table1.rot;");
			y+=100;
			GCTransition tr0 = doc.createTransition(100,y,"start");
			y+=50;
			GCStep s0 = doc.createStep(100,y,"S0","S start=0;\nS activeSteps.onStep0=1;\nX activeSteps.onStep0=0;\nS rotations=rotations+1;\nS syncTabrot=Shoefactory.tables.table1.rot;");
			GCTransition tr1 = doc.createTransition(300,y+40,"!JgrafSupervisor.get_T1_S0 & syncTabrot!=Shoefactory.tables.table1.rot");
			y+=100;
			doc.connect(initialStep,tr0);
			doc.connect(tr0,s0);
			doc.connect(s0,tr1);
			doc.connect(tr1,s0);

			GCTransition tr2a = doc.createTransition(100,y,"!station0");
			GCTransition tr2b = doc.createTransition(300,y,"JgrafSupervisor.get_T1_S0");
			GCTransition tr2f = doc.createTransition(1100,y,"rotations>23");
			y+=50;
			GCStep s1a = doc.createStep(100,y,"S1a","S rotations =0;\nS leavetable=1;");
			GCStep s1b = doc.createStep(300,y,"S1b","S activeSteps.onStep1=1;\nX activeSteps.onStep1=0;\nS Shoefactory.stations.station0.enterStation=1;\nS Shoefactory.tables.table1.fullSlot.set(nrOfRot,0);");
			GCStep s1f = doc.createStep(1100,y,"S1f","S rotations =0;");
			y+=100;
			GCTransition tr3a = doc.createTransition(100,y,"1");
			GCTransition tr3b = doc.createTransition(300,y,"JgrafSupervisor.put_T1_S0");
			GCTransition tr3f = doc.createTransition(1100,y,"1");
			y+=50;
			GCStep s2b = doc.createStep(300,y,"S2b","S Shoefactory.stations.station0.leaveStation=1;\nS station0=0;\nS Shoefactory.tables.table1.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.ShoeDEMO\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table1.rot,\"int\","+stationRot[0] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.ShoeDEMO\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table1.rot,\"int\","+stationRot[0] +");");
			y+=100;
			GCTransition tr4b = doc.createTransition(300,y,"1");
			y+=50;

			doc.connect(s0,tr2a);
			doc.connect(s0,tr2b);
			doc.connect(s0,tr2f);
			doc.connect(tr2a,s1a);
			doc.connect(tr2b,s1b);
			doc.connect(tr2f,s1f);
			doc.connect(s1a,tr3a);
			doc.connect(s1b,tr3b);
			doc.connect(s1f,tr3f);
			doc.connect(tr3a,initialStep);
			doc.connect(tr3b,s2b);
			doc.connect(tr3f,s0);
			doc.connect(s2b,tr4b);

			doc.connect(tr4b,s0);
		}

		if(currentTable==2)
		{
			IntegerVariable iv1 = doc.createIntegerVariable(500,50,"workTime1","2");
			IntegerVariable iv2 = doc.createIntegerVariable(600,50,"workTime2","2");

			GCStepInitial initialStep = doc.createInitialStep(100,y,"Start","P syncTabrot=Shoefactory.tables.table2.rot;");
			y+=100;
			GCTransition tr0 = doc.createTransition(100,y,"start");
			y+=50;
			GCStep s0 = doc.createStep(100,y,"S0","S start=0;\nS activeSteps.onStep2=1;\nX activeSteps.onStep2=0;\nS rotations=rotations+1;\nS syncTabrot=Shoefactory.tables.table2.rot;");
			GCTransition tr1 = doc.createTransition(300,y+40,"!JgrafSupervisor.get_T2_S1 & syncTabrot!=Shoefactory.tables.table2.rot");
			y+=100;
			doc.connect(initialStep,tr0);
			doc.connect(tr0,s0);
			doc.connect(s0,tr1);
			doc.connect(tr1,s0);

			GCTransition tr2a = doc.createTransition(100,y,"!station1");
			GCTransition tr2c = doc.createTransition(500,y,"JgrafSupervisor.get_T2_S1");
			GCTransition tr2g = doc.createTransition(1300,y,"rotations>23");
			y+=50;
			GCStep s1a = doc.createStep(100,y,"S1a","S rotations =0;\nS leavetable=1;");
			GCStep s1c = doc.createStep(500,y,"S1c","S activeSteps.onStep3=1;\nX activeSteps.onStep3=0;\nS Shoefactory.stations.station1.enterStation=1;\nS Shoefactory.tables.table2.fullSlot.set(nrOfRot,0);");
			GCStep s1g = doc.createStep(1300,y,"S1g","S rotations =0;");
			y+=100;
			GCTransition tr3a = doc.createTransition(100,y,"1");
			GCTransition tr3c = doc.createTransition(500,y,"JgrafSupervisor.put_T2_S1");
			GCTransition tr3g = doc.createTransition(1300,y,"1");
			y+=50;
			GCStep s2c = doc.createStep(500,y,"S2c","S Shoefactory.stations.station1.leaveStation=1;\nS station1=0;\nS Shoefactory.tables.table2.fullSlot.set(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.ShoeDEMO\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table2.rot,\"int\","+stationRot[1] +"),1);\nS nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.ShoeDEMO\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table2.rot,\"int\","+stationRot[1] +");");
			y+=100;
			GCTransition tr4c = doc.createTransition(500,y,"1");
			y+=50;

			doc.connect(s0,tr2a);
			doc.connect(s0,tr2c);
			doc.connect(s0,tr2g);
			doc.connect(tr2a,s1a);
			doc.connect(tr2c,s1c);
			doc.connect(tr2g,s1g);
			doc.connect(s1a,tr3a);
			doc.connect(s1c,tr3c);
			doc.connect(s1g,tr3g);
			doc.connect(tr3a,initialStep);
			doc.connect(tr3c,s2c);
			doc.connect(tr3g,s0);
			doc.connect(s2c,tr4c);
			doc.connect(tr4c,s0);
		}
	}

	public void createSubdoc(GCDocument doc, int currentTable)
	{
		int y=0;

		//create subdoc
		BooleanVariable bv1 = doc.createBooleanVariable(500,50,"start","0");
		BooleanVariable bv2 = doc.createBooleanVariable(600,50,"moveReady","0");
		IntegerVariable iv1 = doc.createIntegerVariable(700,50,"goto","1");

		GCStepInitial initialStep = doc.createInitialStep(100,y,"Start","S moveReady=0;");
		y+=100;
		GCTransition tr0 = doc.createTransition(100,y,"start");
		y+=50;
		GCStep s0 = doc.createStep(100,y,"S0","S start=0;\nS activeSteps.onStep10=1;\nX activeSteps.onStep10=0;");
		y+=100;
		doc.connect(initialStep,tr0);
		doc.connect(tr0,s0);

		if(currentTable==0)
		{
			GCTransition tr02b = doc.createTransition(400,y,"JgrafSupervisor.get_T0R & goto==2");
			GCTransition tr02c = doc.createTransition(100,y,"JgrafSupervisor.get_T0R & goto==1");
			y+=50;
			GCStep s02b = doc.createStep(400,y,"move_table2","S Shoefactory.tables.table0.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv0.from0to6=1;");
			GCStep s02c = doc.createStep(100,y,"move_table1","S Shoefactory.tables.table0.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv0.from0to1=1;");
			y+=100;
			GCTransition tr2b = doc.createTransition(400,y,"Shoefactory.agvs.agv0.atgoal" );
			GCTransition tr2c = doc.createTransition(100,y,"Shoefactory.agvs.agv0.atgoal");
			y+=50;
			GCStep s2b = doc.createStep(400,y,"S_toTable2_end","S activeSteps.onStep4=1;\nX activeSteps.onStep4=0;\nP nrOfRot=Shoefactory.tables.table2.rot;");
			GCStep s2c = doc.createStep(100,y,"S_toTable1_end","S activeSteps.onStep5=1;\nX activeSteps.onStep5=0;\nP nrOfRot=Shoefactory.tables.table1.rot;");
			y+=250;
			GCTransition tr3b = doc.createTransition(400,y,"JgrafSupervisor.put_T2");
			GCTransition tr3c = doc.createTransition(100,y,"JgrafSupervisor.put_T1");
			y+=50;
			GCStep s3b = doc.createStep(400,y,"S_atTable2","S currentTable = 2;\nS Shoefactory.tables.table2.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv0.busy=0;\nS moveReady=1;");
			GCStep s3c = doc.createStep(100,y,"S_atTable1","S currentTable = 1;\nS Shoefactory.tables.table1.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv0.busy=0;\nS moveReady=1;");
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
			GCTransition tr02b = doc.createTransition(100,y,"JgrafSupervisor.get_T1 & goto==0");
			GCTransition tr02c = doc.createTransition(700,y,"JgrafSupervisor.get_T1 & goto==2");
			y+=50;
			GCStep s02b = doc.createStep(100,y,"move_table0","S Shoefactory.tables.table1.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv0.from1to0=1;");
			GCStep s02c = doc.createStep(700,y,"move_table2","S Shoefactory.tables.table1.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv0.from1to6=1;");
			y+=100;
			GCTransition tr2b = doc.createTransition(100,y,"Shoefactory.agvs.agv0.atgoal" );
			GCTransition tr2c = doc.createTransition(700,y,"Shoefactory.agvs.agv0.atgoal");
			y+=50;
			GCStep s2b = doc.createStep(100,y,"S_toTable0_end","S activeSteps.onStep6=1;\nX activeSteps.onStep6=0;\nP nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.ShoeDEMO\",\"getRota\",\"int\",\"int\",Shoefactory.tables.table0.rot);");
			GCStep s2c = doc.createStep(700,y,"S_toTable2_end","S activeSteps.onStep4=1;\nX activeSteps.onStep4=0;\nP nrOfRot=Shoefactory.tables.table2.rot;");
			y+=250;
			GCTransition tr3b = doc.createTransition(100,y,"JgrafSupervisor.put_T0R");
			GCTransition tr3c = doc.createTransition(700,y,"JgrafSupervisor.put_T2");
			y+=50;
			GCStep s3b = doc.createStep(100,y,"S_atTable0","S currentTable = 0;\nS Shoefactory.tables.table0.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv0.busy=0;\nS moveReady=1;");
			GCStep s3c = doc.createStep(700,y,"S_atTable2","S currentTable = 2;\nS Shoefactory.tables.table2.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv0.busy=0;\nS moveReady=1;");
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
			GCTransition tr02b = doc.createTransition(100,y,"JgrafSupervisor.get_T2 & goto==0");
			GCTransition tr02c = doc.createTransition(400,y,"JgrafSupervisor.get_T2 & goto==1");
			y+=50;
			GCStep s02b = doc.createStep(100,y,"move_table0","S Shoefactory.tables.table2.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv0.from6to0=1;");
			GCStep s02c = doc.createStep(400,y,"move_table1","S Shoefactory.tables.table2.fullSlot.set(nrOfRot,0);\nS Shoefactory.agvs.agv0.from6to1=1;");
			y+=100;
			GCTransition tr2b = doc.createTransition(100,y,"Shoefactory.agvs.agv0.atgoal" );
			GCTransition tr2c = doc.createTransition(400,y,"Shoefactory.agvs.agv0.atgoal");
			y+=50;
			GCStep s2b = doc.createStep(100,y,"S_toTable0_end","S activeSteps.onStep6=1;\nX activeSteps.onStep6=0;\nP nrOfRot=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.ShoeDEMO\",\"getRota\",\"int\",\"int\",Shoefactory.tables.table0.rot);");
			GCStep s2c = doc.createStep(400,y,"S_toTable1_end","S activeSteps.onStep5=1;\nX activeSteps.onStep5=0;\nP nrOfRot=Shoefactory.tables.table1.rot;");
			y+=250;
			GCTransition tr3b = doc.createTransition(100,y,"JgrafSupervisor.put_T0R");
			GCTransition tr3c = doc.createTransition(400,y,"JgrafSupervisor.put_T1");
			y+=50;
			GCStep s3b = doc.createStep(100,y,"S_atTable0","S currentTable = 0;\nS Shoefactory.tables.table0.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv0.busy=0;\nS moveReady=1;");
			GCStep s3c = doc.createStep(400,y,"S_atTable1","S currentTable = 2;\nS Shoefactory.tables.table2.fullSlot.set(nrOfRot,1);\nS Shoefactory.agvs.agv0.busy=0;\nS moveReady=1;");
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

	public GCDocument getShoe ()
	{
		return shoe ;
	}
}