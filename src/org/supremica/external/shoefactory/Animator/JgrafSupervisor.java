package org.supremica.external.shoefactory.Animator;

import java.awt.*;
import grafchart.sfc.*;
import org.supremica.automata.*;
import org.supremica.gui.*;

public class JgrafSupervisor
{
	private int yPos = 0, stepID = 0, shoeNr;
	private static String[] putEventsA = { "put_T0L", "put_T0R", "put_T1L",
										   "put_T1R", "put_T2L", "put_T2R",
										   "put_T3L", "put_T3R", "put_T4L",
										   "put_T4R", "put_T5L", "put_T5R" };
	private static String[] putEventsB = { "put_T6", "put_T7", "put_T8",
										   "put_T9", "put_T10L", "put_T10R",
										   "put_T11", "put_T12" };
	private static String[] putEventsC = { "put_T6_S0", "put_T6_S1",
										   "put_T6_S2", "put_T6_S3",
										   "put_T6_S4", "put_T7_S5",
										   "put_T7_S6", "put_T7_S7",
										   "put_T7_S8", "put_T8_S9",
										   "put_T8_S10", "put_T8_S11",
										   "put_T9_S12", "put_T9_S13",
										   "put_T9_S14", "put_T9_S15",
										   "put_T12_S16", "put_T12_S17",
										   "put_T12_S18", "put_T12_S19",
										   "put_T10_S20", "put_T11_S21",
										   "put_T11_S22", "put_T11_S23" };
	private static String[] getEventsA = { "get_T0L", "get_T0R", "get_T1L",
										   "get_T1R", "get_T2L", "get_T2R",
										   "get_T3L", "get_T3R", "get_T4L",
										   "get_T4R", "get_T5L", "get_T5R" };
	private static String[] getEventsB = { "get_T6", "get_T7", "get_T8",
										   "get_T9", "get_T10L", "get_T10R",
										   "get_T11", "get_T12" };
	private static String[] getEventsC = { "get_T6_S0", "get_T6_S1",
										   "get_T6_S2", "get_T6_S3",
										   "get_T6_S4", "get_T7_S5",
										   "get_T7_S6", "get_T7_S7",
										   "get_T7_S8", "get_T8_S9",
										   "get_T8_S10", "get_T8_S11",
										   "get_T9_S12", "get_T9_S13",
										   "get_T9_S14", "get_T9_S15",
										   "get_T12_S16", "get_T12_S17",
										   "get_T12_S18", "get_T12_S19",
										   "get_T10_S20", "get_T11_S21",
										   "get_T11_S22", "get_T11_S23" };
	private String actionString = "S getRot=\"Shoe\"+index+\".nrOfRot\";\nS currTable = \"Shoe\"+index+\".currentTable\";\nS gotoString = \"Shoe\"+index+\".ShoeControl.moveFromTable\"+currTable^+\".goto\";\nS errorEvent = random()<0.0 & applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Error\"+index);\n";
	private String conditionString = "";

	public JgrafSupervisor(GCDocument doc, int nr)
	{
		GCDocument jgSupervisor = doc;

		jgSupervisor.setWorkspaceName("JgrafSupervisor");
		jgSupervisor.setFrameRectangle(new Rectangle(0, 0, 800, 400));

		WorkspaceObject wo = jgSupervisor.createWorkspaceObject(100, 50, "Supervisor");
		GCDocument supervisor = wo.getSubWorkspace();
		BooleanVariable[] bv1 = new BooleanVariable[putEventsA.length + putEventsB.length + putEventsC.length + getEventsA.length + getEventsB.length + getEventsC.length];
		BooleanVariable suc = jgSupervisor.createBooleanVariable(300, 50, "success", "0");
		StringVariable sup = jgSupervisor.createStringVariable(400, 50, "supervisor", "theSupervisor");
		StringVariable curS = jgSupervisor.createStringVariable(500, 50, "currStation", "");
		StringVariable gotS = jgSupervisor.createStringVariable(600, 50, "gotoString", "");
		IntegerVariable nrO = jgSupervisor.createIntegerVariable(700, 50, "nrOfShoes", "0");
		IntegerVariable sho = jgSupervisor.createIntegerVariable(800, 50, "index", "0");
		StringVariable curE = jgSupervisor.createStringVariable(900, 50, "currEvent", "");
		StringVariable from12 = jgSupervisor.createStringVariable(1000, 50, "from12", "");
		StringVariable getR = jgSupervisor.createStringVariable(300, 150, "getRot", "");
		StringVariable stepS = jgSupervisor.createStringVariable(400, 150, "stepString", "");
		StringVariable curT = jgSupervisor.createStringVariable(500, 150, "currTable", "");
		StringVariable agvS = jgSupervisor.createStringVariable(600, 150, "agvString", "");
		BooleanVariable turn = jgSupervisor.createBooleanVariable(700, 150, "turn", "0");
		BooleanVariable errorEv = jgSupervisor.createBooleanVariable(800, 150, "errorEvent", "0");

		// Create Grafcet
		GCStepInitial initialStep = supervisor.createInitialStep(100, yPos, "Start", "S nrOfShoes = applyStaticMethod(\"org.supremica.external.shoefactory.Executor.FactoryExecutor\",\"getSValue\",\"int\");\nS success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"initializeSupervisor\",\"boolean\",\"string\",supervisor);");

		yPos += 100;

		GCTransition trA = supervisor.createTransition(100, yPos, "nrOfShoes>0 & success");

		yPos += 50;

		GCStep sA = supervisor.createStep(100, yPos, "sA", "S index=applyStaticMethod(\"org.supremica.external.shoefactory.Executor.FactoryExecutor\",\"getShoeIndex\",\"int\",\"int\",0);\nS turn=1 ;\nX turn=0;");

		yPos += 100;

		GCTransition trB = supervisor.createTransition(100, yPos, "1");

		yPos += 50;

		GCStep sB = supervisor.createStep(100, yPos, "sB", ";");

		yPos += 100;

		GCTransition trC = supervisor.createTransition(100, yPos + 30, "");
		GCStep sC = supervisor.createStep(100, yPos + 200, "sC", "S index=applyStaticMethod(\"org.supremica.external.shoefactory.Executor.FactoryExecutor\",\"getShoeIndex\",\"int\",\"int\",index);\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Executor.FactoryExecutor\",\"saveValues\",\"boolean\",\"int\",nrOfShoes);");
		GCTransition trDa = supervisor.createTransition(100, yPos + 300, "index>0");
		GCTransition trDb = supervisor.createTransition(300, yPos + 300, "index<0 & nrOfShoes>0");

		for (int i = 1; i < putEventsA.length + 1; i++)
		{
			GCTransition tr = checkTablePutEvents(supervisor, sB, i - 1);

			bv1[i - 1] = jgSupervisor.createBooleanVariable(100 * i, 250, putEventsA[i - 1], "0");

			supervisor.connect(tr, sC);
		}

		for (int i = 1; i < putEventsB.length + 1; i++)
		{
			GCTransition tr = checkTableWithStationPutEvents(supervisor, sB, i - 1);

			bv1[putEventsA.length] = jgSupervisor.createBooleanVariable(100 * i, 350, putEventsB[i - 1], "0");

			supervisor.connect(tr, sC);
		}

		for (int i = 1; i < putEventsC.length + 1; i++)
		{
			GCTransition tr = checkStationPutEvents(supervisor, sB, i - 1);

			bv1[putEventsA.length + putEventsB.length] = jgSupervisor.createBooleanVariable(100 * i, 450, putEventsC[i - 1], "0");

			supervisor.connect(tr, sC);
		}

		for (int i = 1; i < getEventsA.length + 1; i++)
		{
			GCTransition tr = checkTableGetEvents(supervisor, sB, i - 1);

			bv1[putEventsA.length + putEventsB.length + putEventsC.length] = jgSupervisor.createBooleanVariable(100 * i, 550, getEventsA[i - 1], "0");

			supervisor.connect(tr, sC);
		}

		for (int i = 1; i < getEventsB.length + 1; i++)
		{
			GCTransition tr = checkTableWithStationGetEvents(supervisor, sB, i - 1);

			bv1[putEventsA.length + putEventsB.length + putEventsC.length + getEventsA.length] = jgSupervisor.createBooleanVariable(100 * i, 650, getEventsB[i - 1], "0");

			supervisor.connect(tr, sC);
		}

		for (int i = 1; i < getEventsC.length + 1; i++)
		{
			GCTransition tr = checkStationGetEvents(supervisor, sB, i - 1);

			bv1[putEventsA.length + putEventsB.length + putEventsC.length + getEventsA.length + getEventsB.length] = jgSupervisor.createBooleanVariable(100 * i, 750, getEventsC[i - 1], "0");

			supervisor.connect(tr, sC);
		}

		GCTransition trEr1 = supervisor.createTransition(9100, yPos, "errorEvent");
		GCStep sEr = supervisor.createStep(9100, yPos + 50, "Error", "S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Error\"+index);\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Error\"+index);\nS currEvent = \"Shoe\"+index+\".Events.error\";\nS currEvent^=1;");
		GCTransition trEr2 = supervisor.createTransition(9100, yPos + 150, "Error.T>1");

		trC.setConditionText("!(" + conditionString + "errorEvent)");
		sB.setActionText(actionString);
		supervisor.connect(initialStep, trA);
		supervisor.connect(trA, sA);
		supervisor.connect(sA, trB);
		supervisor.connect(trB, sB);
		supervisor.connect(sB, trEr1);
		supervisor.connect(trEr1, sEr);
		supervisor.connect(sEr, trEr2);
		supervisor.connect(sB, trC);
		supervisor.connect(trEr2, sC);
		supervisor.connect(trC, sC);
		supervisor.connect(sC, trDa);
		supervisor.connect(sC, trDb);
		supervisor.connect(trDa, sB);
		supervisor.connect(trDb, sA);
	}

	public GCTransition checkTablePutEvents(GCDocument supervisor, GCStep in, int index)
	{
		int xPos = 300 + 100 * (index);

		conditionString = conditionString + putEventsA[index] + " | ";

		if (index == 0)
		{
			actionString = actionString + "S stepString = \"Shoe\"+index+\".ShoeControl.Wait_table0.x\";\nS " + putEventsA[index] + " = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + putEventsA[index] + "\") & !Shoefactory.tables.table0.fullSlot.get(Shoefactory.tables.table0.rot) & stepString^;\n";
		}
		else if (index == 1)
		{
			actionString = actionString + "S stepString = \"Shoe\"+index+\".ShoeControl.moveFromTable\"+currTable^+\".S_toTable0_end.x\";\nS " + putEventsA[index] + " = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + putEventsA[index] + "\") & !Shoefactory.tables.table0.fullSlot.get(getRot^) & stepString^;\n";
		}
		else
		{
			actionString = actionString + "S stepString = \"Shoe\"+index+\".ShoeControl.moveFromTable\"+currTable^+\".Wait_table" + index / 2 + ".x\";\nS " + putEventsA[index] + " = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + putEventsA[index] + "\") & !Shoefactory.tables.table" + index / 2 + ".fullSlot.get(getRot^) & stepString^;\n";
		}

		GCTransition trEn1 = supervisor.createTransition(xPos, yPos, putEventsA[index]);
		GCStep s1 = supervisor.createStep(xPos, yPos + 50, "Step" + stepID, ";");

		if (index == 0)
		{
			s1.setActionText("S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + putEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"Table0\",\"string\",\"Shoe_\"+index+\"" + putEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\"" + putEventsA[index] + "\");\nS currEvent = \"Shoe\"+index+\".Events." + putEventsA[index] + "\";\nS currEvent^=1;");
		}
		else if (index == 1)
		{
			s1.setActionText("S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + putEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"Table0\",\"string\",\"Shoe_\"+index+\"" + putEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"IO_1\",\"string\",\"Shoe_\"+index+\"" + putEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\"" + putEventsA[index] + "\");\nS currEvent = \"Shoe\"+index+\".Events." + putEventsA[index] + "\";\nS currEvent^=1;");
		}
		else
		{
			s1.setActionText("S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + putEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"Table" + index / 2 + "\",\"string\",\"Shoe_\"+index+\"" + putEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"IO_" + (index / 2 + index % 2) + "\",\"string\",\"Shoe_\"+index+\"" + putEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\"" + putEventsA[index] + "\");\nS currEvent = \"Shoe\"+index+\".Events." + putEventsA[index] + "\";\nS currEvent^=1;");
		}

		GCTransition trEn2 = supervisor.createTransition(xPos, yPos + 150, "Step" + stepID + ".T>1");

		stepID++;

		supervisor.connect(in, trEn1);
		supervisor.connect(trEn1, s1);
		supervisor.connect(s1, trEn2);

		return trEn2;
	}

	public GCTransition checkTableWithStationPutEvents(GCDocument supervisor, GCStep in, int index)
	{
		int tNr, xPos = 300 + 100 * (index + putEventsA.length);

		conditionString = conditionString + putEventsB[index] + " | ";

		if (index < 4)
		{
			tNr = index + 6;
		}
		else if ((index == 4) || (index == 5))
		{
			tNr = 10;
		}
		else
		{
			tNr = index + 5;
		}

		if ((index == 4) || (index == 5))
		{
			actionString = actionString + "S stepString = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"checkActiveStep\",\"string\",\"int\",currTable^,\"int\",index,\"string\",\"putEvent\",\"int\",gotoString^);\nS " + putEventsB[index] + " = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + putEventsB[index] + "\") & !Shoefactory.tables.table10.fullSlot.get(Shoefactory.tables.table10.rot) & stepString^;\n";
		}
		else
		{
			actionString = actionString + "S stepString = \"Shoe\"+index+\".ShoeControl.moveFromTable\"+currTable^+\".S_toTable" + tNr + "_end.x\";\nS " + putEventsB[index] + " = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + putEventsB[index] + "\") & !Shoefactory.tables.table" + tNr + ".fullSlot.get(Shoefactory.tables.table" + tNr + ".rot) & stepString^;\n";
		}

		GCTransition trEn1 = supervisor.createTransition(xPos, yPos, putEventsB[index]);
		GCStep s1 = supervisor.createStep(xPos, yPos + 50, "Step" + stepID, ";");

		if (index == 4)
		{
			s1.setActionText("S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + putEventsB[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"Table10\",\"string\",\"Shoe_\"+index+\"" + putEventsB[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"IO_5\",\"string\",\"Shoe_\"+index+\"" + putEventsB[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\"" + putEventsB[index] + "\");\nS currEvent = \"Shoe\"+index+\".Events." + putEventsB[index] + "\";\nS currEvent^=1;");
		}
		else if (index == 5)
		{
			s1.setActionText("S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + putEventsB[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"Table10\",\"string\",\"Shoe_\"+index+\"" + putEventsB[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"IO_7\",\"string\",\"Shoe_\"+index+\"" + putEventsB[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\"" + putEventsB[index] + "\");\nS currEvent = \"Shoe\"+index+\".Events." + putEventsB[index] + "\";\nS currEvent^=1;");
		}
		else if ((index == 6) || (index == 7))
		{
			s1.setActionText("S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + putEventsB[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"Table" + tNr + "\",\"string\",\"Shoe_\"+index+\"" + putEventsB[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"IO_7\",\"string\",\"Shoe_\"+index+\"" + putEventsB[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\"" + putEventsB[index] + "\");\nS currEvent = \"Shoe\"+index+\".Events." + putEventsB[index] + "\";\nS currEvent^=1;");
		}
		else
		{
			s1.setActionText("S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + putEventsB[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"Table" + tNr + "\",\"string\",\"Shoe_\"+index+\"" + putEventsB[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"IO_" + (index + 1) + "\",\"string\",\"Shoe_\"+index+\"" + putEventsB[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\"" + putEventsB[index] + "\");\nS currEvent = \"Shoe\"+index+\".Events." + putEventsB[index] + "\";\nS currEvent^=1;");
		}

		GCTransition trEn2 = supervisor.createTransition(xPos, yPos + 150, "Step" + stepID + ".T>1");

		stepID++;

		supervisor.connect(in, trEn1);
		supervisor.connect(trEn1, s1);
		supervisor.connect(s1, trEn2);

		return trEn2;
	}

	public GCTransition checkStationPutEvents(GCDocument supervisor, GCStep in, int index)
	{
		int tNr,
			xPos = 300 + 100 * (index + putEventsA.length + putEventsB.length);

		conditionString = conditionString + putEventsC[index] + " | ";

		if (index < 5)
		{
			tNr = 6;
		}
		else if ((index >= 5) && (index < 9))
		{
			tNr = 7;
		}
		else if ((index >= 9) && (index < 12))
		{
			tNr = 8;
		}
		else if ((index >= 12) && (index < 16))
		{
			tNr = 9;
		}
		else if ((index >= 16) && (index < 20))
		{
			tNr = 12;
		}
		else if (index == 20)
		{
			tNr = 10;
		}
		else
		{
			tNr = 11;
		}

		actionString = actionString + "S stepString = \"Shoe\"+index+\".ShoeControl.onTable" + tNr + ".Station" + index + ".x\";\nS " + putEventsC[index] + " = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + putEventsC[index] + "\") & !Shoefactory.tables.table" + tNr + ".fullSlot.get(applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"getIndex\",\"int\",\"int\",Shoefactory.tables.table" + tNr + ".rot,\"int\"," + Shoe.getStationRot(index) + ")) & Shoefactory.stations.station" + index + ".S1.s>Shoefactory.stations.station" + index + ".workTime & stepString^ & !errorEvent;\n";

		GCTransition trEn1 = supervisor.createTransition(xPos, yPos, putEventsC[index]);
		GCStep s1 = supervisor.createStep(xPos, yPos + 50, "Step" + stepID, "S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + putEventsC[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"Station_" + tNr + "_" + index + "\",\"string\",\"Shoe_\"+index+\"" + putEventsC[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"Table" + tNr + "\",\"string\",\"Shoe_\"+index+\"" + putEventsC[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\"" + putEventsC[index] + "\");\nS currEvent = \"Shoe\"+index+\".Events." + putEventsC[index] + "\";\nS currEvent^=1;");
		GCTransition trEn2 = supervisor.createTransition(xPos, yPos + 150, "Step" + stepID + ".T>1");

		stepID++;

		supervisor.connect(in, trEn1);
		supervisor.connect(trEn1, s1);
		supervisor.connect(s1, trEn2);

		return trEn2;
	}

	public GCTransition checkTableGetEvents(GCDocument supervisor, GCStep in, int index)
	{
		int xPos = 300 + 100 * (index + putEventsA.length + putEventsB.length + putEventsC.length);

		conditionString = conditionString + getEventsA[index] + " | ";

		if (index % 2 == 0)
		{
			if (index == 0)
			{
				actionString = actionString + "S currStation=\"Shoe\"+index+\".station23\";\nS stepString = \"Shoe\"+index+\".ShoeControl.S0.x\";\nS " + getEventsA[index] + " = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\") & getRot^==Shoefactory.tables.table0.rot & !currStation^ & stepString^;\n";
			}
			else if (index == 10)
			{
				actionString = actionString + "S agvString = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"checkAgv\",\"string\",\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\",\"int\",gotoString^);\nS stepString = \"Shoe\"+index+\".ShoeControl.moveFromTable11.Put_table5.x\";\nS " + getEventsA[index] + " = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\") & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"fiveRotdown\",\"int\",\"int\",getRot^)-Shoefactory.tables.table5.rot)==0 & !agvString^ & stepString^;\n";
			}
			else
			{
				actionString = actionString + "S agvString = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"checkAgv\",\"string\",\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\",\"int\",gotoString^);\nS stepString = \"Shoe\"+index+\".ShoeControl.moveFromTable\"+currTable^+\".Put_table" + index / 2 + ".x\";\nS " + getEventsA[index] + " = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\") & getRot^==Shoefactory.tables.table" + index / 2 + ".rot & !agvString^ & stepString^;\n";
			}
		}
		else
		{
			if (index == 1)
			{
				actionString = actionString + "S agvString = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"checkAgv\",\"string\",\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\",\"int\",gotoString^);\nS stepString = \"Shoe\"+index+\".ShoeControl.moveFromTable0.S0.x\";\nS " + getEventsA[index] + " = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\") & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",getRot^-Shoefactory.tables.table0.rot)==6 & !agvString^ & stepString^;\n";
			}
			else if (index == 11)
			{
				actionString = actionString + "S agvString = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"checkAgv\",\"string\",\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\",\"int\",gotoString^);\nS stepString = \"Shoe\"+index+\".ShoeControl.moveFromTable\"+currTable^+\".Put_table5.x\";\nS " + getEventsA[index] + " = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\") & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",applyStaticMethod(\"org.supremica.external.shoefactory.Animator.Shoe\",\"fiveRot\",\"int\",\"int\",getRot^)-Shoefactory.tables.table5.rot)==9 & !agvString^ & stepString^;\n";
			}
			else
			{
				actionString = actionString + "S agvString = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"checkAgv\",\"string\",\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\",\"int\",gotoString^);\nS stepString = \"Shoe\"+index+\".ShoeControl.moveFromTable\"+currTable^+\".Put_table" + index / 2 + ".x\";\nS " + getEventsA[index] + " = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\") & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",getRot^-Shoefactory.tables.table" + index / 2 + ".rot)==6 & !agvString^ & stepString^;\n";
			}
		}

		GCTransition trEn1 = supervisor.createTransition(xPos, yPos, getEventsA[index]);
		GCStep s1 = supervisor.createStep(xPos, yPos + 50, "Step" + stepID, ";");

		if (index % 2 == 0)
		{
			if (index == 0)
			{
				s1.setActionText("S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"Table0\",\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\");\nS nrOfShoes = nrOfShoes-1;\nS currEvent = \"Shoe\"+index+\".Events." + getEventsA[index] + "\";\nS currEvent^=1;");
			}
			else if (index == 10)
			{
				s1.setActionText("S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"Table5\",\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"IO_5\",\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\");\nS currEvent = \"Shoe\"+index+\".Events." + getEventsA[index] + "\";\nS currEvent^=1;");
			}
			else
			{
				s1.setActionText("S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"Table" + index / 2 + "\",\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"IO_" + index / 2 + "\",\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\");\nS currEvent = \"Shoe\"+index+\".Events." + getEventsA[index] + "\";\nS currEvent^=1;");
			}
		}
		else
		{
			if (index == 1)
			{
				s1.setActionText("S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"Table0\",\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"IO_1\",\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\");\nS currEvent = \"Shoe\"+index+\".Events." + getEventsA[index] + "\";\nS currEvent^=1;");
			}
			else if (index == 11)
			{
				s1.setActionText("S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"Table5\",\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"IO_6\",\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\");\nS currEvent = \"Shoe\"+index+\".Events." + getEventsA[index] + "\";\nS currEvent^=1;");
			}
			else
			{
				s1.setActionText("S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"Table" + index / 2 + "\",\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"IO_" + (index / 2 + 1) + "\",\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\"" + getEventsA[index] + "\");\nS currEvent = \"Shoe\"+index+\".Events." + getEventsA[index] + "\";\nS currEvent^=1;");
			}
		}

		GCTransition trEn2 = supervisor.createTransition(xPos, yPos + 150, "Step" + stepID + ".T>1");

		stepID++;

		supervisor.connect(in, trEn1);
		supervisor.connect(trEn1, s1);
		supervisor.connect(s1, trEn2);

		return trEn2;
	}

	public GCTransition checkTableWithStationGetEvents(GCDocument supervisor, GCStep in, int index)
	{
		int tNr,
			xPos = 300 + 100 * (index + putEventsA.length + putEventsB.length + putEventsC.length + getEventsA.length);

		conditionString = conditionString + getEventsB[index] + " | ";

		if (index < 4)
		{
			tNr = index + 6;
		}
		else if ((index == 4) || (index == 5))
		{
			tNr = 10;
		}
		else
		{
			tNr = index + 5;
		}

		if (index == 4)
		{
			actionString = actionString + "S agvString = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"checkAgv\",\"string\",\"string\",\"Shoe_\"+index+\"" + getEventsB[index] + "\",\"int\",gotoString^);\nS stepString = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"checkActiveStep\",\"string\",\"int\",currTable^,\"int\",index,\"string\",\"getEvent\",\"int\",gotoString^);\nS " + getEventsB[index] + " = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + getEventsB[index] + "\") & getRot^==Shoefactory.tables.table10.rot & !agvString^ & stepString^;\n";
		}
		else if (index == 5)
		{
			actionString = actionString + "S agvString = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"checkAgv\",\"string\",\"string\",\"Shoe_\"+index+\"" + getEventsB[index] + "\",\"int\",gotoString^);\nS stepString = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"checkActiveStep\",\"string\",\"int\",currTable^,\"int\",index,\"string\",\"getEvent\",\"int\",gotoString^);\nS " + getEventsB[index] + " = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + getEventsB[index] + "\") & applyStaticMethod(\"java.lang.Math\",\"abs\",\"int\",\"int\",getRot^-Shoefactory.tables.table10.rot)==12 & !agvString^ & stepString^;\n";
		}
		else
		{
			actionString = actionString + "S agvString = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"checkAgv\",\"string\",\"string\",\"Shoe_\"+index+\"" + getEventsB[index] + "\",\"int\",gotoString^);\nS stepString = \"Shoe\"+index+\".ShoeControl.moveFromTable" + tNr + ".S0.x\";\nS " + getEventsB[index] + " = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + getEventsB[index] + "\") & getRot^==Shoefactory.tables.table" + tNr + ".rot & !agvString^ & stepString^;\n";
		}

		GCTransition trEn1 = supervisor.createTransition(xPos, yPos, getEventsB[index]);
		GCStep s1 = supervisor.createStep(xPos, yPos + 50, "Step" + stepID, ";");

		if (index == 4)
		{
			s1.setActionText("S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + getEventsB[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\"" + getEventsB[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"Table10\",\"string\",\"Shoe_\"+index+\"" + getEventsB[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"IO_5\",\"string\",\"Shoe_\"+index+\"" + getEventsB[index] + "\");\nS currEvent = \"Shoe\"+index+\".Events." + getEventsB[index] + "\";\nS currEvent^=1;");
		}
		else if (index == 5)
		{
			s1.setActionText("S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + getEventsB[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\"" + getEventsB[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"Table10\",\"string\",\"Shoe_\"+index+\"" + getEventsB[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"IO_7\",\"string\",\"Shoe_\"+index+\"" + getEventsB[index] + "\");\nS currEvent = \"Shoe\"+index+\".Events." + getEventsB[index] + "\";\nS currEvent^=1;");
		}
		else if ((index == 6) || (index == 7))
		{
			s1.setActionText("S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + getEventsB[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\"" + getEventsB[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"Table" + tNr + "\",\"string\",\"Shoe_\"+index+\"" + getEventsB[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"IO_7\",\"string\",\"Shoe_\"+index+\"" + getEventsB[index] + "\");\nS currEvent = \"Shoe\"+index+\".Events." + getEventsB[index] + "\";\nS currEvent^=1;");
		}
		else
		{
			s1.setActionText("S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + getEventsB[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\"" + getEventsB[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"Table" + tNr + "\",\"string\",\"Shoe_\"+index+\"" + getEventsB[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"IO_" + (index + 1) + "\",\"string\",\"Shoe_\"+index+\"" + getEventsB[index] + "\");\nS currEvent = \"Shoe\"+index+\".Events." + getEventsB[index] + "\";\nS currEvent^=1;");
		}

		GCTransition trEn2 = supervisor.createTransition(xPos, yPos + 150, "Step" + stepID + ".T>1");

		stepID++;

		supervisor.connect(in, trEn1);
		supervisor.connect(trEn1, s1);
		supervisor.connect(s1, trEn2);

		return trEn2;
	}

	public GCTransition checkStationGetEvents(GCDocument supervisor, GCStep in, int index)
	{
		int tNr,
			xPos = 300 + 100 * (index + putEventsA.length + putEventsB.length + putEventsC.length + getEventsA.length + getEventsB.length);;

		conditionString = conditionString + getEventsC[index] + " | ";

		if (index < 5)
		{
			tNr = 6;
		}
		else if ((index >= 5) && (index < 9))
		{
			tNr = 7;
		}
		else if ((index >= 9) && (index < 12))
		{
			tNr = 8;
		}
		else if ((index >= 12) && (index < 16))
		{
			tNr = 9;
		}
		else if ((index >= 16) && (index < 20))
		{
			tNr = 12;
		}
		else if (index == 20)
		{
			tNr = 10;
		}
		else
		{
			tNr = 11;
		}

		if (tNr == 10)
		{
			actionString = actionString + "S currStation=\"Shoe\"+index+\".station" + index + "\";\nS stepString = \"Shoe\"+index+\".ShoeControl.onTable" + tNr + ".S0.x\";\nS from12 = \"Shoe\"+index+\"ShoeControl.onTable10.from12\";\nS " + getEventsC[index] + " = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + getEventsC[index] + "\") & applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"checkRotationsToStation\",\"boolean\",\"int\",Shoefactory.tables.table" + tNr + ".rot,\"int\",getRot^,\"int\"," + Shoe.getStationRot(index) + "-from12^) & currStation^ & stepString^;\n";
		}
		else
		{
			actionString = actionString + "S currStation=\"Shoe\"+index+\".station" + index + "\";\nS stepString = \"Shoe\"+index+\".ShoeControl.onTable" + tNr + ".S0.x\";\nS " + getEventsC[index] + " = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"isEventEnabled\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + getEventsC[index] + "\") & applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"checkRotationsToStation\",\"boolean\",\"int\",Shoefactory.tables.table" + tNr + ".rot,\"int\",getRot^,\"int\"," + Shoe.getStationRot(index) + ") & currStation^ & stepString^;\n";
		}

		GCTransition trEn1 = supervisor.createTransition(xPos, yPos, getEventsC[index]);
		GCStep s1 = supervisor.createStep(xPos, yPos + 50, "Step" + stepID, "S success = applyStaticMethod(\"org.supremica.external.jgrafchart.Supervisor\",\"executeEvent\",\"boolean\",\"string\",supervisor,\"string\",\"Shoe_\"+index+\"" + getEventsC[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"Station_" + tNr + "_" + index + "\",\"string\",\"Shoe_\"+index+\"" + getEventsC[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"Table" + tNr + "\",\"string\",\"Shoe_\"+index+\"" + getEventsC[index] + "\");\nS success = applyStaticMethod(\"org.supremica.external.shoefactory.Animator.JgrafSupervisor\",\"moveInitial\",\"boolean\",\"string\",\"shoeSpec\"+index,\"string\",\"Shoe_\"+index+\"" + getEventsC[index] + "\");\nS currEvent = \"Shoe\"+index+\".Events." + getEventsC[index] + "\";\nS currEvent^=1;");
		GCTransition trEn2 = supervisor.createTransition(xPos, yPos + 150, "Step" + stepID + ".T>1");

		stepID++;

		supervisor.connect(in, trEn1);
		supervisor.connect(trEn1, s1);
		supervisor.connect(s1, trEn2);

		return trEn2;
	}

	public static boolean moveInitial(String supervisor, String event)
	{
		Gui theGui = ActionMan.getGui();
		VisualProjectContainer container = theGui.getVisualProjectContainer();
		Project activeProject = container.getActiveProject();
		Automaton currAutomaton = activeProject.getAutomaton(supervisor);
		State currState = currAutomaton.getInitialState();

		if (currState.isEnabled(event))
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

	//Gives the correct String to the supervisor when it checks an agv
	public static String checkAgv(String event, int gotoTable)
	{
		int nr, agvNr;

		if (event.endsWith("L"))
		{
			nr = Integer.valueOf(event.substring(event.indexOf("get") + 5, event.length() - 1)).intValue();
			agvNr = nr - 1;
		}
		else if (event.endsWith("R"))
		{
			nr = Integer.valueOf(event.substring(event.indexOf("get") + 5, event.length() - 1)).intValue();
			agvNr = nr;
		}
		else
		{
			nr = Integer.valueOf(event.substring(event.indexOf("get") + 5, event.length())).intValue();
			agvNr = nr - 6;
		}

		if (nr < 5)
		{
			if (gotoTable < nr)
			{
				if (gotoTable == nr + 5)
				{
					return "Shoefactory.agvs.agv" + agvNr + ".busyC";
				}
				else
				{
					return "Shoefactory.agvs.agv" + agvNr + ".busyA";
				}
			}
			else
			{
				if (gotoTable == nr + 6)
				{
					return "Shoefactory.agvs.agv" + agvNr + ".busyB";
				}
				else
				{
					return "Shoefactory.agvs.agv" + agvNr + ".busyA";
				}
			}
		}
		else if (nr == 5)
		{
			if ((gotoTable == 10) || (gotoTable == 12))
			{
				return "Shoefactory.agvs.agv4.busyC";
			}
			else if (gotoTable == 11)
			{
				return "Shoefactory.agvs.agv5.busy";
			}
			else
			{
				return "Shoefactory.agvs.agv4.busyA";
			}
		}
		else if ((nr >= 6) && (nr < 10))
		{
			if (gotoTable < nr)
			{
				return "Shoefactory.agvs.agv" + agvNr + ".busyB";
			}
			else
			{
				return "Shoefactory.agvs.agv" + agvNr + ".busyC";
			}
		}
		else if (nr == 10)
		{
			if (gotoTable == 11)
			{
				return "Shoefactory.agvs.agv4.busyC";
			}
			else if (gotoTable == 12)
			{
				return "Shoefactory.agvs.agv6.busy";
			}
			else
			{
				return "Shoefactory.agvs.agv4.busyB";
			}
		}
		else
		{
			return "Shoefactory.agvs.agv" + agvNr + ".busy";
		}
	}

	public static String checkActiveStep(int currTable, int shoeNr, String event, int bla)
	{
		if (currTable == 10)
		{
			return "Shoe" + shoeNr + ".ShoeControl.moveFromTable10.S0.x";
		}
		else if (event.equals("putEvent"))
		{
			if (bla == 10)
			{
				return "Shoe" + shoeNr + ".ShoeControl.moveFromTable" + currTable + ".S_toTable10_end.x";
			}
			else
			{
				return "Shoe" + shoeNr + ".ShoeControl.moveFromTable" + currTable + ".Wait_table10.x";
			}
		}
		else
		{
			return "Shoe" + shoeNr + ".ShoeControl.moveFromTable" + currTable + ".Put_table10.x";
		}
	}

	public static boolean checkRotationsToStation(int tRot, int nrOfRot, int stRot)
	{
		if ((Math.abs(tRot - nrOfRot) == stRot) || (tRot + 24 - nrOfRot == stRot))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
}
