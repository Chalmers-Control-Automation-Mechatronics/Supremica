package org.supremica.external.shoefactory.Animator;

import java.awt.*;
import grafchart.sfc.*;
import org.supremica.automata.*;
import org.supremica.gui.*;

public class JgrafSupervisorDEMO
{
	private int yPos=0, stepID=0, shoeNr;
	private static String[] putEventsA = {"put_T0L","put_T0R"};
	private static String[] putEventsB = {"put_T1","put_T2"};
	private static String[] putEventsC = {"put_T1_S0","put_T2_S1"};
	private static String[] getEventsA = {"get_T0L","get_T0R"};
	private static String[] getEventsB = {"get_T1","get_T2"};
	private static String[] getEventsC = {"get_T1_S0","get_T2_S1"};
	
	private String actionString = "S getRot=\"Shoe\"+index+\".nrOfRot\";\nS currTable = \"Shoe\"+index+\".currentTable\";\nS gotoString = \"Shoe\"+index+\".ShoeControl.moveFromTable\"+currTable^+\".goto\";\n";
	private String conditionString = "";
	
	public JgrafSupervisorDEMO(GCDocument doc, int nr)
	{
		GCDocument jgSupervisor = doc;
		jgSupervisor.setWorkspaceName("JgrafSupervisor");
		jgSupervisor.setFrameRectangle(new Rectangle(0,0,800,400));

		WorkspaceObject wo = jgSupervisor.createWorkspaceObject(100,50,"Supervisor");
		GCDocument supervisor = wo.getSubWorkspace();
		
		BooleanVariable[] bv1 = new BooleanVariable[putEventsA.length+putEventsB.length+putEventsC.length+getEventsA.length+getEventsB.length+getEventsC.length];
		
		BooleanVariable suc = jgSupervisor.createBooleanVariable(300,50,"success","0");
		StringVariable sup = jgSupervisor.createStringVariable(400,50,"supervisor","theSupervisor");
		StringVariable curS = jgSupervisor.createStringVariable(500,50,"currStation","");
		StringVariable gotS = jgSupervisor.createStringVariable(600,50,"gotoString","");
		IntegerVariable nrO = jgSupervisor.createIntegerVariable(700,50,"nrOfShoes","0");
		IntegerVariable sho = jgSupervisor.createIntegerVariable(800,50,"index","0");
		StringVariable curE = jgSupervisor.createStringVariable(900,50,"currEvent","");
		StringVariable getR = jgSupervisor.createStringVariable(300,150,"getRot","");
		StringVariable stepS = jgSupervisor.createStringVariable(400,150,"stepString","");
		StringVariable curT = jgSupervisor.createStringVariable(500,150,"currTable","");
		StringVariable agvS = jgSupervisor.createStringVariable(600,150,"agvString","");
		BooleanVariable turn = jgSupervisor.createBooleanVariable(700,150,"turn","0");
		
		// Create Grafcet
		GCStepInitial initialStep = supervisor.createInitialStep(100,yPos,"Start","S nrOfShoes = applyStaticMethod(\"org.supremica.external.shoefactory.Executor.FactoryExecutorDEMO\",\"getSValue\",\"int\");\nS success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"initializeSupervisor\",\"boolean\",\"string\",supervisor);");
		yPos+=100;
		GCTransition trA = supervisor.createTransition(100,yPos,"nrOfShoes>0 & success");
		yPos+=50;
		GCStep sA = supervisor.createStep(100,yPos,"sA","S index=applyStaticMethod(\"org.supremica.external.shoefactory.Executor.FactoryExecutorDEMO\",\"getShoeIndex\",\"int\",\"int\",0);\nS turn=1 ;\nX turn=0;");
		yPos+=100;
		GCTransition trB = supervisor.createTransition(100,yPos,"1");
		yPos+=50;
		GCStep sB = supervisor.createStep(100,yPos,"sB",";");
		yPos+=100;
		GCTransition trC = supervisor.createTransition(100,yPos+30,"");
		
		GCStep sC = supervisor.createStep(100,yPos+200,"sC","S index=applyStaticMethod(\"org.supremica.external.shoefactory.Executor.FactoryExecutorDEMO\",\"getShoeIndex\",\"int\",\"int\",index);\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Executor.FactoryExecutorDEMO\",\"saveValues\",\"boolean\",\"int\",nrOfShoes);");
		GCTransition trDa = supervisor.createTransition(100,yPos+300,"index>0");
		GCTransition trDb = supervisor.createTransition(300,yPos+300,"index<0 & nrOfShoes>0");
		
		for(int i=1; i<putEventsA.length+1; i++)
		{
			GCTransition tr = checkTablePutEvents(supervisor,sB,i-1);
			bv1[i-1] = jgSupervisor.createBooleanVariable(100*i,250,putEventsA[i-1],"0");
			supervisor.connect(tr,sC);
		}
		
		for(int i=1; i<putEventsB.length+1; i++)
		{
			GCTransition tr = checkTableWithStationPutEvents(supervisor,sB,i-1);
			bv1[putEventsA.length] = jgSupervisor.createBooleanVariable(100*i,350,putEventsB[i-1],"0");
			supervisor.connect(tr,sC);
		}
		
		for(int i=1; i<putEventsC.length+1; i++)
		{
			GCTransition tr = checkStationPutEvents(supervisor,sB,i-1);
			bv1[putEventsA.length+putEventsB.length] = jgSupervisor.createBooleanVariable(100*i,450,putEventsC[i-1],"0");
			supervisor.connect(tr,sC);
		}
		
		for(int i=1; i<getEventsA.length+1; i++)
		{
			GCTransition tr = checkTableGetEvents(supervisor,sB,i-1);
			bv1[putEventsA.length+putEventsB.length+putEventsC.length] = jgSupervisor.createBooleanVariable(100*i,550,getEventsA[i-1],"0");
			supervisor.connect(tr,sC);
		}
		
		for(int i=1; i<getEventsB.length+1; i++)
		{
			GCTransition tr = checkTableWithStationGetEvents(supervisor,sB,i-1);
			bv1[putEventsA.length+putEventsB.length+putEventsC.length+getEventsA.length] = jgSupervisor.createBooleanVariable(100*i,650,getEventsB[i-1],"0");
			supervisor.connect(tr,sC);
		}
		
		for(int i=1; i<getEventsC.length+1; i++)
		{
			GCTransition tr = checkStationGetEvents(supervisor,sB,i-1);
			bv1[putEventsA.length+putEventsB.length+putEventsC.length+getEventsA.length+getEventsB.length] = jgSupervisor.createBooleanVariable(100*i,750,getEventsC[i-1],"0");
			supervisor.connect(tr,sC);
		}
		trC.setConditionText("!("+conditionString+"0)");
		sB.setActionText(actionString);
		
		supervisor.connect(initialStep,trA);
		supervisor.connect(trA,sA);
		supervisor.connect(sA,trB);
		supervisor.connect(trB,sB);
		supervisor.connect(sB,trC);
		supervisor.connect(trC,sC);
		supervisor.connect(sC,trDa);
		supervisor.connect(sC,trDb);
		supervisor.connect(trDa,sB);
		supervisor.connect(trDb,sA);
	}
	
	public GCTransition checkTablePutEvents(GCDocument supervisor, GCStep in, int index)
	{
		int xPos=300+100*(index);
		conditionString = conditionString+putEventsA[index]+" | ";
		
		if(index==0)
			actionString = actionString+"S stepString = \"Shoe\"+index+\".ShoeControl.Wait_table0.x\";\nS "+putEventsA[index]+" = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\""+putEventsA[index]+"\") & !ShoefactoryDEMO.tables.table0.fullSlot.get(ShoefactoryDEMO.tables.table0.rot) & stepString^;\n";
		else 
			actionString = actionString+"S stepString = \"Shoe\"+index+\".ShoeControl.moveFromTable\"+currTable^+\".S_toTable0_end.x\";\nS "+putEventsA[index]+" = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\""+putEventsA[index]+"\") & !ShoefactoryDEMO.tables.table0.fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.ShoeDEMO\",\"getRota\",\"int\",\"int\",ShoefactoryDEMO.tables.table0.rot)) & stepString^;\n";	
					
		GCTransition trEn1 = supervisor.createTransition(xPos,yPos,putEventsA[index]);
		GCStep s1 = supervisor.createStep(xPos,yPos+50,"Step"+stepID,";");
		if(index==0)
			s1.setActionText("S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\""+putEventsA[index]+"\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisorDEMO\",\"moveInitial\",\"boolean\",\"string\",\"Table0\",\"string\",\"Shoe_\"+index+\""+putEventsA[index]+"\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisorDEMO\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\""+putEventsA[index]+"\");\nS currEvent = \"Shoe\"+index+\".Events."+putEventsA[index]+"\";\nS currEvent^=1;");
		else
			s1.setActionText("S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\""+putEventsA[index]+"\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisorDEMO\",\"moveInitial\",\"boolean\",\"string\",\"Table0\",\"string\",\"Shoe_\"+index+\""+putEventsA[index]+"\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisorDEMO\",\"moveInitial\",\"boolean\",\"string\",\"IO_1\",\"string\",\"Shoe_\"+index+\""+putEventsA[index]+"\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisorDEMO\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\""+putEventsA[index]+"\");\nS currEvent = \"Shoe\"+index+\".Events."+putEventsA[index]+"\";\nS currEvent^=1;");		
	
		GCTransition trEn2 = supervisor.createTransition(xPos,yPos+150,"Step"+stepID+".T>1");
		stepID++;
		supervisor.connect(in,trEn1);
		supervisor.connect(trEn1,s1);
		supervisor.connect(s1,trEn2);
		
		return trEn2;
	}
	
	public GCTransition checkTableWithStationPutEvents(GCDocument supervisor, GCStep in, int index)
	{
		int tNr=index+1, xPos=300+100*(index+putEventsA.length);
		conditionString = conditionString+putEventsB[index]+" | ";
		
	    actionString = actionString+"S stepString = \"Shoe\"+index+\".ShoeControl.moveFromTable\"+currTable^+\".S_toTable"+tNr+"_end.x\";\nS "+putEventsB[index]+" = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\""+putEventsB[index]+"\") & !ShoefactoryDEMO.tables.table"+tNr+".fullSlot.get(ShoefactoryDEMO.tables.table"+tNr+".rot) & stepString^;\n";

		GCTransition trEn1 = supervisor.createTransition(xPos,yPos,putEventsB[index]);
		GCStep s1 = supervisor.createStep(xPos,yPos+50,"Step"+stepID,"S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\""+putEventsB[index]+"\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisorDEMO\",\"moveInitial\",\"boolean\",\"string\",\"Table"+tNr+"\",\"string\",\"Shoe_\"+index+\""+putEventsB[index]+"\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisorDEMO\",\"moveInitial\",\"boolean\",\"string\",\"IO_1\",\"string\",\"Shoe_\"+index+\""+putEventsB[index]+"\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisorDEMO\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\""+putEventsB[index]+"\");\nS currEvent = \"Shoe\"+index+\".Events."+putEventsB[index]+"\";\nS currEvent^=1;");
		
		GCTransition trEn2 = supervisor.createTransition(xPos,yPos+150,"Step"+stepID+".T>1");
		stepID++;
		
		supervisor.connect(in,trEn1);
		supervisor.connect(trEn1,s1);
		supervisor.connect(s1,trEn2);
		
		return trEn2;
	}
	
	public GCTransition checkStationPutEvents(GCDocument supervisor, GCStep in, int index)
	{
		int tNr=index+1, xPos=300+100*(index+putEventsA.length+putEventsB.length);
		conditionString = conditionString+putEventsC[index]+" | ";
		actionString = actionString+"S stepString = \"Shoe\"+index+\".ShoeControl.onTable"+tNr+".Station"+index+".x\";\nS "+putEventsC[index]+" = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\""+putEventsC[index]+"\") & !ShoefactoryDEMO.tables.table"+tNr+".fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.ShoeDEMO\",\"getIndex\",\"int\",\"int\",ShoefactoryDEMO.tables.table"+tNr+".rot,\"int\","+ShoeDEMO.getStationRot(index)+")) & ShoefactoryDEMO.stations.station"+index+".S1.s>ShoefactoryDEMO.stations.station"+index+".workTime & stepString^;\n";

		GCTransition trEn1 = supervisor.createTransition(xPos,yPos,putEventsC[index]);
		GCStep s1 = supervisor.createStep(xPos,yPos+50,"Step"+stepID,"S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\""+putEventsC[index]+"\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisorDEMO\",\"moveInitial\",\"boolean\",\"string\",\"Station_"+tNr+"_"+index+"\",\"string\",\"Shoe_\"+index+\""+putEventsC[index]+"\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisorDEMO\",\"moveInitial\",\"boolean\",\"string\",\"Table"+tNr +"\",\"string\",\"Shoe_\"+index+\""+putEventsC[index]+"\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisorDEMO\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\""+putEventsC[index]+"\");\nS currEvent = \"Shoe\"+index+\".Events."+putEventsC[index]+"\";\nS currEvent^=1;");
		GCTransition trEn2 = supervisor.createTransition(xPos,yPos+150,"Step"+stepID+".T>1");
		stepID++;
		
		supervisor.connect(in,trEn1);
		supervisor.connect(trEn1,s1);
		supervisor.connect(s1,trEn2);
		
		return trEn2;
	}
	
	public GCTransition checkTableGetEvents(GCDocument supervisor, GCStep in, int index)
	{
		int xPos=300+100*(index+putEventsA.length+putEventsB.length+putEventsC.length);
		conditionString = conditionString+getEventsA[index]+" | ";
		

		if(index==0)
			actionString = actionString+"S currStation=\"Shoe\"+index+\".station1\";\nS stepString = \"Shoe\"+index+\".ShoeControl.S0.x\";\nS "+getEventsA[index]+" = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\""+getEventsA[index]+"\") & getRot^==ShoefactoryDEMO.tables.table0.rot & !currStation^ & stepString^;\n";
		else
			actionString = actionString+"S agvString = \"ShoefactoryDEMO.agvs.agv0.busy\";\nS stepString = \"Shoe\"+index+\".ShoeControl.moveFromTable0.S0.x\";\nS "+getEventsA[index]+" = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\""+getEventsA[index]+"\") & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",getRot^-ShoefactoryDEMO.tables.table0.rot)==6 & !agvString^ & stepString^;\n";

																																																																																																								
		GCTransition trEn1 = supervisor.createTransition(xPos,yPos,getEventsA[index]);
		GCStep s1 = supervisor.createStep(xPos,yPos+50,"Step"+stepID,";");

		if(index==0)
			s1.setActionText("S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\""+getEventsA[index]+"\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisorDEMO\",\"moveInitial\",\"boolean\",\"string\",\"Table0\",\"string\",\"Shoe_\"+index+\""+getEventsA[index]+"\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisorDEMO\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\""+getEventsA[index]+"\");\nS nrOfShoes = nrOfShoes-1;\nS currEvent = \"Shoe\"+index+\".Events."+getEventsA[index]+"\";\nS currEvent^=1;");

		else
			s1.setActionText("S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\""+getEventsA[index]+"\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisorDEMO\",\"moveInitial\",\"boolean\",\"string\",\"Table0\",\"string\",\"Shoe_\"+index+\""+getEventsA[index]+"\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisorDEMO\",\"moveInitial\",\"boolean\",\"string\",\"IO_1\",\"string\",\"Shoe_\"+index+\""+getEventsA[index]+"\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisorDEMO\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\""+getEventsA[index]+"\");\nS currEvent = \"Shoe\"+index+\".Events."+getEventsA[index]+"\";\nS currEvent^=1;");
	
		GCTransition trEn2 = supervisor.createTransition(xPos,yPos+150,"Step"+stepID+".T>1");
		stepID++;
		supervisor.connect(in,trEn1);
		supervisor.connect(trEn1,s1);
		supervisor.connect(s1,trEn2);
		
		return trEn2;
	}
	
	public GCTransition checkTableWithStationGetEvents(GCDocument supervisor, GCStep in, int index)
	{
		int tNr=index+1, xPos=300+100*(index+putEventsA.length+putEventsB.length+putEventsC.length+getEventsA.length);
		conditionString = conditionString+getEventsB[index]+" | ";
		actionString = actionString+"S agvString = \"ShoefactoryDEMO.agvs.agv0.busy\";\nS stepString = \"Shoe\"+index+\".ShoeControl.moveFromTable"+tNr+".S0.x\";\nS "+getEventsB[index]+" = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\""+getEventsB[index]+"\") & getRot^==ShoefactoryDEMO.tables.table"+tNr+".rot & !agvString^ & stepString^;\n";

		GCTransition trEn1 = supervisor.createTransition(xPos,yPos,getEventsB[index]);
		GCStep s1 = supervisor.createStep(xPos,yPos+50,"Step"+stepID,"S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\""+getEventsB[index]+"\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisorDEMO\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\""+getEventsB[index]+"\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisorDEMO\",\"moveInitial\",\"boolean\",\"string\",\"Table"+tNr+"\",\"string\",\"Shoe_\"+index+\""+getEventsB[index]+"\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisorDEMO\",\"moveInitial\",\"boolean\",\"string\",\"IO_1\",\"string\",\"Shoe_\"+index+\""+getEventsB[index]+"\");\nS currEvent = \"Shoe\"+index+\".Events."+getEventsB[index]+"\";\nS currEvent^=1;");
		
		GCTransition trEn2 = supervisor.createTransition(xPos,yPos+150,"Step"+stepID+".T>1");
		stepID++;
		supervisor.connect(in,trEn1);
		supervisor.connect(trEn1,s1);
		supervisor.connect(s1,trEn2);
		
		return trEn2;
	}
	
	public GCTransition checkStationGetEvents(GCDocument supervisor, GCStep in, int index)
	{
		int tNr=index+1, xPos=300+100*(index+putEventsA.length+putEventsB.length+putEventsC.length+getEventsA.length+getEventsB.length);;
		conditionString = conditionString+getEventsC[index]+" | ";
		actionString = actionString+"S currStation=\"Shoe\"+index+\".station"+index+"\";\nS stepString = \"Shoe\"+index+\".ShoeControl.onTable"+tNr+".S0.x\";\nS "+getEventsC[index]+" = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\""+getEventsC[index]+"\") & applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisorDEMO\",\"checkRotationsToStation\",\"boolean\",\"int\",ShoefactoryDEMO.tables.table"+tNr+".rot,\"int\",getRot^,\"int\","+ShoeDEMO.getStationRot(index)+") & currStation^ & stepString^;\n";

		GCTransition trEn1 = supervisor.createTransition(xPos,yPos,getEventsC[index]);
		GCStep s1 = supervisor.createStep(xPos,yPos+50,"Step"+stepID,"S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\""+getEventsC[index]+"\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisorDEMO\",\"moveInitial\",\"boolean\",\"string\",\"Station_"+tNr+"_"+index+"\",\"string\",\"Shoe_\"+index+\""+getEventsC[index]+"\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisorDEMO\",\"moveInitial\",\"boolean\",\"string\",\"Table"+tNr+"\",\"string\",\"Shoe_\"+index+\""+getEventsC[index]+"\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisorDEMO\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\""+getEventsC[index]+"\");\nS currEvent = \"Shoe\"+index+\".Events."+getEventsC[index]+"\";\nS currEvent^=1;");
		GCTransition trEn2 = supervisor.createTransition(xPos,yPos+150,"Step"+stepID+".T>1");
		stepID++;
		supervisor.connect(in,trEn1);
		supervisor.connect(trEn1,s1);
		supervisor.connect(s1,trEn2);
		
		return trEn2;
	}
	
	public static boolean moveInitial(String supervisor, String event)
	{
		Gui theGui = ActionMan.getGui();
		VisualProjectContainer container = theGui.getVisualProjectContainer();
		Project activeProject = container.getActiveProject();
		Automaton currAutomaton = activeProject.getAutomaton(supervisor);
		State currState = currAutomaton.getInitialState();

		if(currState.isEnabled(event))
		{
			State nextState = currState.nextState(event);
			currState.setInitial(false);
			currState.setAccepting(false);
			nextState.setInitial(true);
			nextState.setAccepting(true);
			currAutomaton.setInitialState(nextState);
			return true;	
		}
		else 
		{
			return false;
		}
	}
		
	public static boolean checkRotationsToStation(int tRot, int nrOfRot, int stRot)
	{
		if(Math.abs(tRot-nrOfRot)==stRot || tRot+24-nrOfRot==stRot)
			return true;
		else 
			return false;
	}
}