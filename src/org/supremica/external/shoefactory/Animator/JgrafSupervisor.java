package org.supremica.external.shoefactory.Animator;

import java.awt.*;
import grafchart.sfc.*;

public class JgrafSupervisor
{
	private int yPos=0, stepID=0, shoeNr;
	private boolean[] stationVisit;
	GCDocument jgSupervisor;
	private static String [] alphabet = {"put_T0","get_T0","put_T1","get_T1","put_T2","get_T2","put_T3","get_T3","put_T4","get_T4","put_T5","get_T5","put_T6","get_T6","put_T7","get_T7","put_T8","get_T8","put_T9","get_T9","put_T10","get_T10","put_T11","get_T11","put_T12","get_T12"};

	public JgrafSupervisor(GCDocument doc, int nr)
	{
		jgSupervisor = doc;
		jgSupervisor.setWorkspaceName("JgrafSupervisor");
		jgSupervisor.setFrameRectangle(new Rectangle(0,0,800,800));

		BooleanVariable[] bv = new BooleanVariable[alphabet.length];
		BooleanVariable ena = jgSupervisor.createBooleanVariable(300,50,"enabled","0");
		BooleanVariable suc = jgSupervisor.createBooleanVariable(400,50,"success","0");
		StringVariable sup = jgSupervisor.createStringVariable(500,50,"supervisor","theSupervisor");
		StringVariable curT = jgSupervisor.createStringVariable(600,50,"currTable","");
		StringVariable curE = jgSupervisor.createStringVariable(700,50,"currEvent","");
		StringVariable putT = jgSupervisor.createStringVariable(800,50,"putgetTable","");
		IntegerVariable nrO = jgSupervisor.createIntegerVariable(300,150,"nrOfShoes","1");
		IntegerVariable sho = jgSupervisor.createIntegerVariable(400,150,"index","1");
		StringVariable getR = jgSupervisor.createStringVariable(500,150,"getRot","");
		BooleanVariable stepA = jgSupervisor.createBooleanVariable(600,150,"stepActive","0");
		StringVariable stepS = jgSupervisor.createStringVariable(700,150,"stepString","");

		for(int i=0; i<alphabet.length; i++)
		{
			bv[i] = jgSupervisor.createBooleanVariable(100+100*i,250,alphabet[i],"0");
		}

		WorkspaceObject wo = jgSupervisor.createWorkspaceObject(100,50,"Supervisor");
		GCDocument supervisor = wo.getSubWorkspace();

		GCTransition[] tr = new GCTransition[alphabet.length+1];

		// Create Grafcet
		GCStepInitial initialStep = supervisor.createInitialStep(100,yPos,"Start","S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"initializeSupervisor\",\"boolean\",\"string\",supervisor) ;");
		yPos+=100;
		GCTransition trA = supervisor.createTransition(100,yPos,"nrOfShoes>0");
		yPos+=50;
		GCStep sA = supervisor.createStep(100,yPos,"sA",";");
		yPos+=100;
		tr[0] = supervisor.createTransition(100,yPos,"1");
		yPos+=50;
		for(int i=1; i<alphabet.length+1; i++)
		{
			tr[i] = createEnabledPath(supervisor,tr[i-1],i-1);
			yPos+=600;
		}
		supervisor.connect(initialStep,trA);
		supervisor.connect(trA,sA);
		supervisor.connect(sA,tr[0]);
		supervisor.connect(tr[alphabet.length],sA);
	}

	public GCTransition createEnabledPath(GCDocument supervisor, GCTransition in, int index)
	{
		GCStep s0 = supervisor.createStep(100,yPos,"Step"+stepID,";");
		if(index%2==0)
		{
			s0.setActionText("S currEvent=\"Shoe_\"+index+\""+alphabet[index]+"\";\nS getRot=\"Shoe\"+index+\".nrOfRot\";\nS stepActive=\"Shoe\"+index+\".onStep\";\nS putgetTable=\"Shoefactory.tables.table"+(index/2)+".fullSlot.get(\"+getRot^+\")\";\nS enabled = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",currEvent);\nS stepString = \"Shoe\"+index+\".onStep\";\nS stepActive = stepString^;\nS enabled = enabled & !putgetTable^ & stepActive;");
		}
		else
		{
			s0.setActionText("S currEvent=\"Shoe_\"+index+\""+alphabet[index]+"\";\nS getRot=\"Shoe\"+index+\".nrOfRot\";\nS stepActive=\"Shoe\"+index+\".onStep\";\nS putgetTable=applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"getTableString\",\"string\",\"int\","+(index/2)+");\nS enabled=applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",currEvent);\nS stepString = \"Shoe\"+index+\".onStep\";\nS stepActive = stepString^;\nS enabled = enabled & !putgetTable^ & stepActive;");
		}

		GCTransition trNotEn = supervisor.createTransition(100,yPos+100,"!enabled");
		stepID++;
		GCTransition trEn1 = supervisor.createTransition(300,yPos+100,"enabled");
		GCStep s1 = supervisor.createStep(300,yPos+150,"Step"+stepID,"S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",currEvent);\nN "+alphabet[index]+";");
		GCTransition trEn2 = supervisor.createTransition(300,yPos+250,"Step"+stepID+".T>1");
		stepID++;
		GCStep s2 = supervisor.createStep(100,yPos+300,"Step"+stepID,"S index = index+1;");
		GCTransition tr = supervisor.createTransition(100,yPos+400,"index>nrOfShoes");
		GCTransition trBack = supervisor.createTransition(300,yPos+400,"index<=nrOfShoes");
		stepID++;
		GCStep s3 = supervisor.createStep(100,yPos+450,"Step"+stepID,"S index = 1;");
		GCTransition tr2 = supervisor.createTransition(100,yPos+550,"1");
		stepID++;
		supervisor.connect(in,s0);
		supervisor.connect(s0,trNotEn);
		supervisor.connect(s0,trEn1);
		supervisor.connect(trEn1,s1);
		supervisor.connect(s1,trEn2);
		supervisor.connect(trNotEn,s2);
		supervisor.connect(trEn2,s2);
		supervisor.connect(s2,trBack);
		supervisor.connect(s2,tr);
		supervisor.connect(trBack,s0);
		supervisor.connect(tr,s3);
		supervisor.connect(s3,tr2);
		return tr2;
	}

	public static String getTableString(int i)
	{
		if(i==0 || i==1 || i==2 || i==3 || i==4 || i==5)
		{
			return "applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",getRot^-Shoefactory.tables.table"+i+".rot)==6;";
		}
		else if(i==6 || i==7 || i==8 || i==9 || i==12)
		{
			return "applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",getRot^-Shoefactory.tables.table"+i+".rot)==0;";
		}
		else
		{
			return ";";
		}
	}
}