package org.supremica.external.processeditor.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JOptionPane;

import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Project;
import org.supremica.automata.State;
import org.supremica.automata.algorithms.AutomataSynthesizer;
import org.supremica.automata.algorithms.SynchronizationOptions;
import org.supremica.automata.algorithms.SynthesisAlgorithm;
import org.supremica.automata.algorithms.SynthesisType;
import org.supremica.automata.algorithms.SynthesizerOptions;
import org.supremica.gui.AutomatonViewer;
import org.supremica.manufacturingTables.xsd.processeditor.Activity;
import org.supremica.manufacturingTables.xsd.processeditor.Attribute;
import org.supremica.manufacturingTables.xsd.processeditor.ObjectFactory;
import org.supremica.manufacturingTables.xsd.processeditor.OperationReferenceType;
import org.supremica.manufacturingTables.xsd.processeditor.ROP;
import org.supremica.manufacturingTables.xsd.processeditor.ROPType;
import org.supremica.manufacturingTables.xsd.processeditor.Relation;
import org.supremica.manufacturingTables.xsd.processeditor.RelationType;


public class ConvertAutomatas {

    String[] zoneNames = new String[0];
    LabeledEvent[][] zoneStEvents = new LabeledEvent[0][];
    LabeledEvent[][] zoneFinEvents = new LabeledEvent[0][];

    String[] fParNames = new String[0];
    LabeledEvent[][] fParStEvents = new LabeledEvent[0][];
    LabeledEvent[][] fParFinEvents = new LabeledEvent[0][];

    final int NUM_OF_SOLUTIONS = 250;
    final int COMPLETE = 1;
    final int BASIC = 2;
    final int SIMPLIFIED = 3;

    ObjectFactory ropFactory = new ObjectFactory();

    File myFile = null;
    int count = 0;

    String[][] tmpString = new String[0][];

    public ConvertAutomatas() {}

    public void complete(final File[] files ) {
	final Loader loader = new Loader();
	final Automata automata = new Automata();
	zoneNames = new String[0];
	zoneStEvents = new LabeledEvent[0][];
	zoneFinEvents = new LabeledEvent[0][];
	for(int i = 0; i < files.length; i++) {
	    ropToComplete(loader.open(files[i]), automata);
	}
	//CREATE AUTOMATA FOR MUTUAL ZONES
	for(int i = 0; i < zoneStEvents.length; i++) {

	    final Automaton zone = new Automaton("Mut"+zoneNames[i]);
	    zone.setType(AutomatonType.SPECIFICATION);
	    automata.addAutomaton(zone);
	    final State unbooked = new State("q0");
	    zone.addState(unbooked);
	    unbooked.setInitial(true);
	    unbooked.setAccepting(true);
	    final State booked = new State("q1");
	    zone.addState(booked);
	    for(int j = 0; j < zoneStEvents[i].length; j++) {
		zone.getAlphabet().addEvent(zoneStEvents[i][j]);
		zone.getAlphabet().addEvent(zoneFinEvents[i][j]);
		zone.addArc(new Arc(unbooked, booked, zoneStEvents[i][j]));
		zone.addArc(new Arc(booked, unbooked, zoneFinEvents[i][j]));
	    }
	}
	for(int i = 0; i < fParStEvents.length; i++) {
	    //FORCED PARALLELITY
	    final Automaton par = new Automaton("par"+fParNames[i]);
	    par.setType(AutomatonType.SPECIFICATION);
	    automata.addAutomaton(par);
	    int index = 0;
	    State tmpState = new State("q"+index);
	    tmpState.setInitial(true);
	    par.addState(tmpState);
	    for(int j = 0; j < fParStEvents[i].length; j++) {
		par.getAlphabet().addEvent(fParStEvents[i][j]);
		index++;
		final State newState = new State("q"+index);
		par.addState(newState);
		par.addArc(new Arc(tmpState, newState, fParStEvents[i][j]));
		tmpState = newState;
	    }
	    for(int j = 0; j < fParFinEvents[i].length; j++) {
		par.getAlphabet().addEvent(fParFinEvents[i][j]);
		index++;
		final State newState = new State("q"+index);
		par.addState(newState);
		par.addArc(new Arc(tmpState, newState, fParFinEvents[i][j]));
		tmpState = newState;
	    }
	    tmpState.setAccepting(true);
	}

	final org.supremica.automata.IO.AutomataToXML serializer = new org.supremica.automata.IO.AutomataToXML(automata);

	try {
	    serializer.serialize(new PrintWriter(new FileWriter(files[files.length-1].getParent()+"//automatasCompleteModel.xml")));
	}catch(final Exception ex) {
	    //DEBUG
	    System.out.println("ERROR: ConvertToAutomatas.convert()");
	    //END DEBUG
	}
    }

    public void ropToComplete(final Object rop, final Automata automata) {
	if(rop instanceof ROP) {
	    //CREATE AUTOMATA FOR THE RESOURCE
	    final Automaton resrc = new Automaton("rescr"+((ROP)rop).getMachine());
	    resrc.setType(AutomatonType.SPECIFICATION);
	    automata.addAutomaton(resrc);

	    final State resrc_q0 = new State("q0");
	    resrc_q0.setInitial(true);
	    resrc_q0.setAccepting(true);
	    final State resrc_q1 = new State("q1");
	    resrc.addState(resrc_q0);
	    resrc.addState(resrc_q1);

	    //CREATE AUTOMATA FOR THE SPECIFICATION
	    final Automaton spec = new Automaton("Seq"+((ROP)rop).getMachine());
	    spec.setType(AutomatonType.SPECIFICATION);
	    automata.addAutomaton(spec);
	    State specTmpState = null;

	    final Iterator<?> itActivity = ((ROP)rop).getRelation().getActivityRelationGroup().iterator();
	    final int numOfOp = ((ROP)rop).getRelation().getActivityRelationGroup().size();
	    int index = 0;
	    while(itActivity.hasNext()) {
		final Object next = itActivity.next();
		if(next instanceof Activity) {
		    //CREATE AUTOMATA FOR THE RESOURCE
		    final LabeledEvent resrcFin = new LabeledEvent("fin"+((Activity)next).getOperation());
		    final LabeledEvent resrcSt = new LabeledEvent("st"+((Activity)next).getOperation());
		    resrc.getAlphabet().addEvent(resrcFin);
		    resrc.getAlphabet().addEvent(resrcSt);
		    resrc.addArc(new Arc(resrc_q0, resrc_q1, resrcSt));
		    resrc.addArc(new Arc(resrc_q1, resrc_q0, resrcFin));

		    //CREATE AUTOMATA FOR THE SPECIFICATION
		    spec.getAlphabet().addEvent(resrcFin);
		    if(index == 0) {
			final State initState = new State("q0");
			spec.addState(initState);
			initState.setInitial(true);
 			specTmpState = initState;
		    }
		    final State newState = new State("q"+Integer.toString(index+1));
		    spec.addState(newState);
		    if(index == numOfOp-1) {
			newState.setAccepting(true);
		    }
		    spec.addArc(new Arc(specTmpState, newState, resrcFin));
		    specTmpState = newState;


		    //CREATE AUTOMATA FOR THE OPERATIONS
		    final Automaton op = new Automaton("Op"+((Activity)next).getOperation());
		    op.setType(AutomatonType.PLANT);

		    final LabeledEvent opFin = new LabeledEvent("fin"+((Activity)next).getOperation());
		    final LabeledEvent opSt = new LabeledEvent("st"+((Activity)next).getOperation());
		    op.getAlphabet().addEvent(opFin);
		    op.getAlphabet().addEvent(opSt);

		    final State init = new State(((Activity)next).getOperation()+"_init");
		    init.setInitial(true);
		    final State exec = new State(((Activity)next).getOperation()+"_exec");
		    //ADD TIMES AS A COST
		    //ADD MUTAL ZONES AND FORECED PARALLALITY
		    int cost = 0;
		    if(((Activity)next).getProperties() != null) {
			final Iterator<?> itAttribute = ((Activity)next).getProperties().getAttribute().iterator();
			while(itAttribute.hasNext()) {
			    final Object nextAttribute = itAttribute.next();
			    if(nextAttribute instanceof Attribute) {
				if("time".equals(((Attribute)nextAttribute).getType().toLowerCase())) {
				    try {
					cost = Integer.parseInt(((Attribute)nextAttribute).getAttributeValue());
				    }catch (final Exception ex) {
					cost = 0;
				    }
				}else if("mutual".equals(((Attribute)nextAttribute).getType().toLowerCase())) {
				    if(!"".equals(((Attribute)nextAttribute).getAttributeValue())) {
					addZone(((Attribute)nextAttribute).getAttributeValue(), opSt, opFin);
				    }
				}else if("forced parallel".equals(((Attribute)nextAttribute).getType().toLowerCase())) {
				    if(!"".equals(((Attribute)nextAttribute).getAttributeValue())) {
					addFPar(((Attribute)nextAttribute).getAttributeValue(), opSt, opFin);
				    }
				}
			    }
			}
		    }
		    exec.setCost(cost);
		    final State comp = new State(((Activity)next).getOperation()+"_comp");
		    comp.setAccepting(true);

		    op.addState(init);
		    op.addState(exec);
		    op.addState(comp);

		    op.addArc(new Arc(init, exec, opSt));
		    op.addArc(new Arc(exec, comp, opFin));

		    automata.addAutomaton(op);

		    //CREATE AUTOMATA FOR THE PREDECESSORS
		    try {
			final Iterator<?> itPred = ((Activity)next).getPrecondition().getPredecessor().iterator();
			while(itPred.hasNext()) {
			    final OperationReferenceType nextPred = (OperationReferenceType)itPred.next();
			    final Automaton pred = new Automaton("Pred"+((Activity)next).getOperation()+"-"+nextPred.getOperation());
			    final State pred_q0 = new State("q0");
			    pred_q0.setInitial(true);
			    final State pred_q1 = new State("q1");
			    final State pred_q2 = new State("q2");
			    pred_q2.setAccepting(true);
			    pred.addState(pred_q0);
			    pred.addState(pred_q1);
			    pred.addState(pred_q2);

			    final LabeledEvent predFin = new LabeledEvent("fin"+nextPred.getOperation());
			    final LabeledEvent predSt = new LabeledEvent("st"+((Activity)next).getOperation());
			    pred.getAlphabet().addEvent(predFin);
			    pred.getAlphabet().addEvent(predSt);

			    pred.addArc(new Arc(pred_q0, pred_q1, predFin));
			    pred.addArc(new Arc(pred_q1, pred_q2, predSt));

			    automata.addAutomaton(pred);

			}

		    }catch(final Exception ex) {}

		    index++;
		}
	    }
	}
    }
    public void basic(final File[] files) {
    	final Loader loader = new Loader();
	final Automata automata = new Automata();
	zoneNames = new String[0];
	zoneStEvents = new LabeledEvent[0][];
	zoneFinEvents = new LabeledEvent[0][];
	for(int i = 0; i < files.length; i++) {
	    ropToBasic(loader.open(files[i]), automata);
	}
	//CREATE AUTOMATA FOR MUTUAL ZONES
	for(int i = 0; i < zoneStEvents.length; i++) {

	    final Automaton zone = new Automaton("Mut"+zoneNames[i]);
	    zone.setType(AutomatonType.SPECIFICATION);
	    automata.addAutomaton(zone);
	    final State unbooked = new State("q0");
	    zone.addState(unbooked);
	    unbooked.setInitial(true);
	    unbooked.setAccepting(true);
	    final State booked = new State("q1");
	    zone.addState(booked);
	    for(int j = 0; j < zoneStEvents[i].length; j++) {
		zone.getAlphabet().addEvent(zoneStEvents[i][j]);
		zone.getAlphabet().addEvent(zoneFinEvents[i][j]);
		zone.addArc(new Arc(unbooked, booked, zoneStEvents[i][j]));
		zone.addArc(new Arc(booked, unbooked, zoneFinEvents[i][j]));
	    }
	}
	for(int i = 0; i < fParStEvents.length; i++) {
	    //FORCED PARALLELITY
	    final Automaton par = new Automaton("par"+fParNames[i]);
	    par.setType(AutomatonType.SPECIFICATION);
	    automata.addAutomaton(par);
	    int index = 0;
	    State tmpState = new State("q"+index);
	    tmpState.setInitial(true);
	    par.addState(tmpState);
	    for(int j = 0; j < fParStEvents[i].length; j++) {
		par.getAlphabet().addEvent(fParStEvents[i][j]);
		index++;
		final State newState = new State("q"+index);
		par.addState(newState);
		par.addArc(new Arc(tmpState, newState, fParStEvents[i][j]));
		tmpState = newState;
	    }
	    for(int j = 0; j < fParFinEvents[i].length; j++) {
		par.getAlphabet().addEvent(fParFinEvents[i][j]);
		index++;
		final State newState = new State("q"+index);
		par.addState(newState);
		par.addArc(new Arc(tmpState, newState, fParFinEvents[i][j]));
		tmpState = newState;
	    }
	    tmpState.setAccepting(true);
	    }

	final org.supremica.automata.IO.AutomataToXML serializer = new org.supremica.automata.IO.AutomataToXML(automata);
	try {
	    serializer.serialize(new PrintWriter(new FileWriter(files[files.length-1].getParent()+"//automatasBasicModel.xml")));
	}catch(final Exception ex) {
	    //DEBUG
	    System.out.println("ERROR: ConvertToAutomatas.convert()");
	    //END DEBUG
	}
    }
    public void ropToBasic(final Object rop, final Automata automata) {
	if(rop instanceof ROP) {
	    //CREATE AUTOMATA FOR THE RESOURCE SPECIFICATION
	    final Automaton spec = new Automaton("Seq"+((ROP)rop).getMachine());
	    spec.setType(AutomatonType.SPECIFICATION);
	    automata.addAutomaton(spec);

	    State specTmpState = null;
	    final Iterator<?> itActivity = ((ROP)rop).getRelation().getActivityRelationGroup().iterator();
	    final int numOfOp = ((ROP)rop).getRelation().getActivityRelationGroup().size();
	    int index = 0;
	    while(itActivity.hasNext()) {
		final Object next = itActivity.next();
		if(next instanceof Activity) {
		    final LabeledEvent opFin = new LabeledEvent("fin"+((Activity)next).getOperation());
		    final LabeledEvent opSt = new LabeledEvent("st"+((Activity)next).getOperation());
		    spec.getAlphabet().addEvent(opFin);
		    spec.getAlphabet().addEvent(opSt);
		    if(index == 0) {
			final State initState = new State("q0");
			spec.addState(initState);
			initState.setInitial(true);
			specTmpState = initState;
		    }
		    final State execState = new State("q"+Integer.toString(++index));
		    spec.addState(execState);
		    final State compState = new State("q"+Integer.toString(++index));
		    spec.addState(compState);
		    if(index == numOfOp*2) {
			compState.setAccepting(true);
		    }
		    spec.addArc(new Arc(specTmpState, execState, opSt));
		    spec.addArc(new Arc(execState, compState, opFin));
		    specTmpState = compState;

		    //ADD TIMES AS A COST
		    //ADD MUTAL ZONES AND FORECED PARALLALITY
		    int cost = 0;
		    if(((Activity)next).getProperties() != null) {
			final Iterator<?> itAttribute = ((Activity)next).getProperties().getAttribute().iterator();
			while(itAttribute.hasNext()) {
			    final Object nextAttribute = itAttribute.next();
			    if(nextAttribute instanceof Attribute) {
				if("time".equals(((Attribute)nextAttribute).getType().toLowerCase())) {
				    try {
					cost = Integer.parseInt(((Attribute)nextAttribute).getAttributeValue());
				    }catch (final Exception ex) {
					cost = 0;
				    }
				}else if("mutual".equals(((Attribute)nextAttribute).getType().toLowerCase())) {
				    if(!"".equals(((Attribute)nextAttribute).getAttributeValue())) {
					addZone(((Attribute)nextAttribute).getAttributeValue(), opSt, opFin);
				    }
				}else if("forced_parallel".equals(((Attribute)nextAttribute).getType().toLowerCase())) {
				    if(!"".equals(((Attribute)nextAttribute).getAttributeValue())) {
					addFPar(((Attribute)nextAttribute).getAttributeValue(), opSt, opFin);
				    }
				}
			    }
			}
		    }
		    execState.setCost(cost);

		    //CREATE AUTOMATA FOR THE PREDECESSOR
		    try {
			if(((Activity)next).getPrecondition() != null) {
			    final Iterator<?> itPred = ((Activity)next).getPrecondition().getPredecessor().iterator();
			    while(itPred.hasNext()) {
				final OperationReferenceType nextPred = (OperationReferenceType)itPred.next();
				final Automaton pred = new Automaton("Pred"+((Activity)next).getOperation()+"-"+nextPred.getOperation());
				final State pred_q0 = new State("q0");
				pred_q0.setInitial(true);
				final State pred_q1 = new State("q1");
				final State pred_q2 = new State("q2");
				pred_q2.setAccepting(true);
				pred.addState(pred_q0);
				pred.addState(pred_q1);
				pred.addState(pred_q2);

				final LabeledEvent predFin = new LabeledEvent("fin"+nextPred.getOperation());
				final LabeledEvent predSt = new LabeledEvent("st"+((Activity)next).getOperation());
				pred.getAlphabet().addEvent(predFin);
				pred.getAlphabet().addEvent(predSt);

				pred.addArc(new Arc(pred_q0, pred_q1, predFin));
				pred.addArc(new Arc(pred_q1, pred_q2, predSt));

				automata.addAutomaton(pred);
			    }
			}
		    }catch(final Exception ex) {
			//DEBUG
			System.out.println("ERROR while create basic automatas");
			//END DEBUG
		    }
		}
	    }

	}
    }
    public void simplified(final File[] files) {
	final Loader loader = new Loader();
	final Automata automata = new Automata();
	for(int i = 0; i < files.length; i++) {
	    ropToSimplified(loader.open(files[i]), automata);
	}

	final org.supremica.automata.IO.AutomataToXML serializer = new org.supremica.automata.IO.AutomataToXML(automata);
	try {
	    serializer.serialize(new PrintWriter(new FileWriter(files[files.length-1].getParent()+"//automatasSimplifiedModel.xml")));
	}catch(final Exception ex) {
	    //DEBUG
	    System.out.println("ERROR: ConvertToAutomatas.convert()");
	    //END DEBUG
	}
    }
    public void ropToSimplified(final Object rop, final Automata automata) {
	if(rop instanceof ROP) {

	    //CREATE AUTOMATA FOR THE SPECIFICATION
	    final Automaton spec = new Automaton("Seq"+((ROP)rop).getMachine());
	    spec.setType(AutomatonType.SPECIFICATION);
	    automata.addAutomaton(spec);
	    State specTmpState = null;

	    final Iterator<?> itActivity = ((ROP)rop).getRelation().getActivityRelationGroup().iterator();
	    final int numOfOp = ((ROP)rop).getRelation().getActivityRelationGroup().size();
	    int index = 0;
	    while(itActivity.hasNext()) {
		final Object next = itActivity.next();
		if(next instanceof Activity) {
		    //CREATE AUTOMATA FOR THE SPECIFICATION
		    final LabeledEvent op = new LabeledEvent(((Activity)next).getOperation());
		    spec.getAlphabet().addEvent(op);
		    if(index == 0) {
			final State initState = new State("q0");
			spec.addState(initState);
			initState.setInitial(true);
 			specTmpState = initState;
		    }
		    final State newState = new State("q"+Integer.toString(index+1));
		    spec.addState(newState);
		    if(index == numOfOp-1) {
			newState.setAccepting(true);
		    }
		    spec.addArc(new Arc(specTmpState, newState, op));
		    specTmpState = newState;

		    //CREATE AUTOMATA FOR THE PREDECESSORS
		    try {
			final Iterator<?> itPred = ((Activity)next).getPrecondition().getPredecessor().iterator();
			while(itPred.hasNext()) {
			    final OperationReferenceType nextPred = (OperationReferenceType)itPred.next();
			    final Automaton pred = new Automaton("Pred"+((Activity)next).getOperation()+"-"+nextPred.getOperation());
			    final State pred_q0 = new State("q0");
			    pred_q0.setInitial(true);
			    final State pred_q1 = new State("q1");
			    final State pred_q2 = new State("q2");
			    pred_q2.setAccepting(true);
			    pred.addState(pred_q0);
			    pred.addState(pred_q1);
			    pred.addState(pred_q2);

			    final LabeledEvent predEvent = new LabeledEvent(nextPred.getOperation());
			    pred.getAlphabet().addEvent(predEvent);
			    pred.getAlphabet().addEvent(op);

			    pred.addArc(new Arc(pred_q0, pred_q1, predEvent));
			    pred.addArc(new Arc(pred_q1, pred_q2, op));

			    automata.addAutomaton(pred);

			}
		    }catch(final Exception ex) {}

		    index++;
		}
	    }

	}
    }
    public void addZone(final String zone, final LabeledEvent stOp, final LabeledEvent finOp) {
	//DEBUG
	//System.out.println("ConvertToAutomatas.addZone()");
	//END DEBUG
	int zoneIndex = -1;
	for(int i = 0; i < zoneNames.length; i++) {
	    if(zoneNames[i].equals(zone)) {
		zoneIndex = i;
		break;
	    }
	}
	if(zoneIndex == -1) {
	    final String[] tmpZoneNames = new String[zoneNames.length+1];
	    final LabeledEvent[][] tmpZoneStEvents = new LabeledEvent[zoneNames.length+1][];
	    final LabeledEvent[][] tmpZoneFinEvents = new LabeledEvent[zoneNames.length+1][];
	    for(int i = 0; i < zoneNames.length; i++) {
		tmpZoneNames[i] = zoneNames[i];
		tmpZoneStEvents[i] = zoneStEvents[i];
		tmpZoneFinEvents[i] = zoneFinEvents[i];
	    }
	    tmpZoneNames[zoneNames.length] = zone;
	    tmpZoneStEvents[zoneNames.length] = new LabeledEvent[1];
	    tmpZoneStEvents[zoneNames.length][0] = stOp;
	    tmpZoneFinEvents[zoneNames.length] = new LabeledEvent[1];
	    tmpZoneFinEvents[zoneNames.length][0] = finOp;

	    zoneNames = tmpZoneNames;
	    zoneStEvents = tmpZoneStEvents;
	    zoneFinEvents = tmpZoneFinEvents;
	}else {
	    final LabeledEvent[] tmpZoneStEvents = new LabeledEvent[zoneStEvents[zoneIndex].length+1];
	    final LabeledEvent[] tmpZoneFinEvents = new LabeledEvent[zoneStEvents[zoneIndex].length+1];
	    for(int i = 0; i < zoneStEvents[zoneIndex].length; i++) {
		tmpZoneStEvents[i] = zoneStEvents[zoneIndex][i];
		tmpZoneFinEvents[i] = zoneFinEvents[zoneIndex][i];
	    }
	    tmpZoneStEvents[zoneStEvents[zoneIndex].length] = stOp;
	    tmpZoneFinEvents[zoneFinEvents[zoneIndex].length] = finOp;

	    zoneStEvents[zoneIndex] = tmpZoneStEvents;
	    zoneFinEvents[zoneIndex] = tmpZoneFinEvents;
	}
    }
    public void addFPar(final String group, final LabeledEvent stOp, final LabeledEvent finOp) {
	//DEBUG
	//System.out.println("ConvertToAutomatas.addZone()");
	//END DEBUG
	int groupIndex = -1;
	for(int i = 0; i < fParNames.length; i++) {
	    if(fParNames[i].equals(group)) {
		groupIndex = i;
		break;
	    }
	}
	if(groupIndex == -1) {
	    final String[] tmpFParNames = new String[fParNames.length+1];
	    final LabeledEvent[][] tmpFParStEvents = new LabeledEvent[fParNames.length+1][];
	    final LabeledEvent[][] tmpFParFinEvents = new LabeledEvent[fParNames.length+1][];
	    for(int i = 0; i < fParNames.length; i++) {
		tmpFParNames[i] = fParNames[i];
		tmpFParStEvents[i] = fParStEvents[i];
		tmpFParFinEvents[i] = fParFinEvents[i];
	    }
	    tmpFParNames[fParNames.length] = group;
	    tmpFParStEvents[fParNames.length] = new LabeledEvent[1];
	    tmpFParStEvents[fParNames.length][0] = stOp;
	    tmpFParFinEvents[fParNames.length] = new LabeledEvent[1];
	    tmpFParFinEvents[fParNames.length][0] = finOp;

	    fParNames = tmpFParNames;
	    fParStEvents = tmpFParStEvents;
	    fParFinEvents = tmpFParFinEvents;
	}else {
	    final LabeledEvent[] tmpFParStEvents = new LabeledEvent[fParStEvents[groupIndex].length+1];
	    final LabeledEvent[] tmpFParFinEvents = new LabeledEvent[fParStEvents[groupIndex].length+1];
	    for(int i = 0; i < fParStEvents[groupIndex].length; i++) {
		tmpFParStEvents[i] = fParStEvents[groupIndex][i];
		tmpFParFinEvents[i] = fParFinEvents[groupIndex][i];
	    }
	    tmpFParStEvents[fParStEvents[groupIndex].length] = stOp;
	    tmpFParFinEvents[fParFinEvents[groupIndex].length] = finOp;

	    fParStEvents[groupIndex] = tmpFParStEvents;
	    fParFinEvents[groupIndex] = tmpFParFinEvents;
	}
    }

    public void supervisor(final File file, final int type) {
	//DEBUG
	System.out.println("ConvertAutomatas.supervisor()");
	//END DEBUG
	try {
	    final org.supremica.automata.IO.ProjectBuildFromXML builder = new org.supremica.automata.IO.ProjectBuildFromXML();
	    final Project theProject = builder.build(file);

	    final SynthesizerOptions syntOptions = new SynthesizerOptions();
	    syntOptions.setSynthesisType(SynthesisType.NONBLOCKINGCONTROLLABLE);
	    syntOptions.setSynthesisAlgorithm(SynthesisAlgorithm.MONOLITHIC);
	    syntOptions.setPurge(true);
	    syntOptions.setMaximallyPermissive(true);

	    final SynchronizationOptions syncOptions = new SynchronizationOptions();
	    if(type == SIMPLIFIED) {
		syncOptions.setUseShortStateNames(true);
	    }else {
		syncOptions.setUseShortStateNames(false);
	    }
	    final AutomataSynthesizer synthesizer = new AutomataSynthesizer(theProject, syncOptions, syntOptions);
	    final Automata supAutomata = synthesizer.execute();

	    final org.supremica.automata.IO.AutomataToXML serializer = new org.supremica.automata.IO.AutomataToXML(supAutomata);
	    serializer.serialize(new PrintWriter(new FileWriter(file.getParent()+"//supervisor.xml")));

	}catch(final Exception ex) {
	    //DEBUG
	    System.out.println("ERROR ConvertAutomatas.supervisor()");
	    //END DEBUG
	}
    }

    public void solutionExtraction(final File file, final int type) {
	//DEBUG
	System.out.println("ConvertAutomatas.solutionExtraction()");
	//END DEBUG
	try {
	    final org.supremica.automata.IO.ProjectBuildFromXML builder = new org.supremica.automata.IO.ProjectBuildFromXML();
	    final Project theProject = builder.build(file);

	    final Iterator<?> supIt = theProject.supervisorIterator();
	    myFile = new File(file.getParent()+"\\"+"Solutions");
	    myFile.mkdirs();

	    final File attFile = new File(file.getParent()+"\\"+"attribute.txt");
	    if(attFile.exists()) {
		BufferedReader attData = new BufferedReader(new FileReader(attFile));
		String oneLine = attData.readLine();
		oneLine = attData.readLine();
		int numOfRows = 0;
		while(oneLine != null) {
		    numOfRows++;
		    oneLine = attData.readLine();
		}
		attData.close();
		if(numOfRows > 0) {
		    tmpString = new String[numOfRows][];
		    attData = new BufferedReader(new FileReader(attFile));
		    oneLine = attData.readLine();
		    oneLine = attData.readLine();
		    int i = 0;
		    while(oneLine != null) {
			final StringTokenizer token = new StringTokenizer(oneLine);
			tmpString[i] = new String[token.countTokens()];
			for(int j = 0; j < tmpString[i].length; j++) {
			    tmpString[i][j] = token.nextToken();
			}
			oneLine = attData.readLine();
			i++;
		    }
		}
	    }
	    //DEBUG
	    /*
	    for(int i = 0; i < tmpString.length; i++) {
		for(int j = 0; j < tmpString[i].length; j++) {
		    System.out.print(tmpString[i][j]+" ");
		}
		System.out.println("");
	    }
	    **/
	    //END DEBUG
	    while(supIt.hasNext()) {
		final Automaton sup =  (Automaton)supIt.next();
		exploreState(sup.getInitialState(), new String[sup.getAlphabet().size()], 0, type);
	    }
	}catch(final Exception ex) {
	    //DEBUG
	    System.out.println("ERROR ConverterAutomatas.solutionExtraction()");
	    //END DEBUG
	}
	//DEBUG
	//System.out.println("NoOfSolution"+count);
	//END DEBUG
    }
    public void exploreState(final State state, final String[] seq, final int index, final int type) {
	//DEBUG
	//System.out.println("ConvertAutomatas.exploreState()");
	//END DEBUG

	final Iterator<?> arcs = state.outgoingArcsIterator();

	if(type == COMPLETE) {
	    if(arcs.hasNext()) {
		while(arcs.hasNext() && count <= NUM_OF_SOLUTIONS) {
		    final Arc arc = (Arc)arcs.next();
		    seq[index] = arc.getEvent().getName();
		    exploreState(arc.getTarget(), seq, index+1, type);
		}
	    }else {
		count++;
		if(count <= NUM_OF_SOLUTIONS) {
		/*
		if(count == 98) {
		    for(int i = 0; i < seq.length; i++) {
			System.out.print(seq[i]+"-");
		    }
		    System.out.println("");
		**/
		    createSequence(seq, "Solution_No"+count);
		}else {
		    JOptionPane.showMessageDialog(null, "Solution Extraction completed. Number of Solutions > "+NUM_OF_SOLUTIONS, "Solution Extraction Completed",  JOptionPane.INFORMATION_MESSAGE);
		}
	    }
	}else if(type == BASIC) {
	    if(arcs.hasNext()) {
		boolean finExist = true;
		while(arcs.hasNext() && count <= NUM_OF_SOLUTIONS) {
		    final Arc arc = (Arc)arcs.next();
		    seq[index] = arc.getEvent().getName();
		    if(arc.getEvent().getName().startsWith("st")) {
			exploreState(arc.getTarget(), seq, index+1, type);
		    }
		    if(arc.getEvent().getName().startsWith("fin") && finExist) {
			if(arc.getTarget().getName().startsWith("fin")) {
			    finExist = false;
			}
		    }
		}
	    }else {
		count++;
		if(count <= NUM_OF_SOLUTIONS) {
		    createSequence(seq, "Solution_No"+count);
		}else {
		    JOptionPane.showMessageDialog(null, "Solution Extraction canceled. Number of Solutions > "+NUM_OF_SOLUTIONS, "Solution Exctraction Canceled", JOptionPane.INFORMATION_MESSAGE);
		}
	    }
	}else if(type == SIMPLIFIED) {
	    if(arcs.hasNext()) {
		boolean stExist = true;
		boolean finExist = true;
		while(arcs.hasNext() && count <= NUM_OF_SOLUTIONS) {
		    final Arc arc = (Arc)arcs.next();
		    seq[index] = arc.getEvent().getName();
		    if(arc.getEvent().getName().startsWith("st") && stExist) {
			exploreState(arc.getTarget(), seq, index+1, type);
			stExist = false;
		    }
		    if(arc.getEvent().getName().startsWith("fin") && finExist) {
			exploreState(arc.getTarget(), seq, index+1, type);
			if(arc.getTarget().getName().startsWith("fin")) {
			    finExist = false;
			}
		    }
		}
	    }else {
		count++;
		if(count <= NUM_OF_SOLUTIONS) {
		    createSequence(seq, "Solution_No"+count);
		}else {
		    JOptionPane.showMessageDialog(null, "Solution Extraction canceled. Number of Solutions > "+NUM_OF_SOLUTIONS, "Solution Exctraction Canceled", JOptionPane.INFORMATION_MESSAGE);
		}
	    }
	}
    }

    public void createSequence(final String[] seq, final String seqName) {
	//DEBUG
	//System.out.println("ConvertAutomatas.createSequence()");
	//END DEBUG
	try {
	    final ROP resrc = ropFactory.createROP();
	    resrc.setMachine(seqName);
	    resrc.setType(ROPType.COP);
	    resrc.setId(Integer.toString(count));
	    final Object tmp = createSequenceRelation(seq);
	    if(tmp instanceof Activity) {
		final Relation tmpRelation = ropFactory.createRelation();
		tmpRelation.setType(RelationType.SEQUENCE);
		tmpRelation.getActivityRelationGroup().add(tmp);
		resrc.setRelation(tmpRelation);
	    }else if(tmp instanceof Relation) {
		resrc.setRelation((Relation)tmp);
	    }
	    final Float time = Converter.sumAttribute(resrc, "time");
	    final Loader loader = new Loader();
	    loader.save(resrc, new File(myFile.getPath()+"\\"+seqName+"_time_"+Float.toString(time)+".xml"));
	    //DEBUG
	    /*
	    for(int i = 0; i < seq.length; i++) {
		System.out.println(seq[i]);
	    }
	    **/
	    //END DEBUG
	}catch(final Exception ex) {
	    //DEBUG
	    System.out.println("ERROR ConvertAutomatas.createSequence()");
	    //END DEBUG
	}
    }
    public Object createSequenceRelation(final String[] seq) {

	try {
	    if(seq.length > 2) {
		final Relation rel = ropFactory.createRelation();
		rel.setType(RelationType.SEQUENCE);
		int stIndex = 0;
		int diffCount = 0;
		for(int i = 0; i < seq.length; i++) {
		    if(seq[i].startsWith("st")) {
			diffCount++;
		    }else if(seq[i].startsWith("fin")) {
			diffCount--;
		    }
		    if(diffCount == 0) {
			final String[] subSeq = new String[i-stIndex+1];
			for(int j = 0; j < i-stIndex+1; j++) {
			    subSeq[j] = seq[stIndex+j];
			}
			    rel.getActivityRelationGroup().add(createParallelRelation(subSeq));
			stIndex = i + 1;
		    }
		}
		return rel;
	    }else {
		final Activity act = ropFactory.createActivity();
		final String op = seq[0].substring(2, seq[0].length());
		act.setOperation(op);
		for(int i = 0; i < tmpString.length; i++) {
		    if(tmpString[i].length > 1 && tmpString[i][0].equals(op)) {
			act.setProperties(ropFactory.createProperties());
			Attribute newAtt = null;
			for(int j = 1; j < tmpString[i].length; j++) {
			    newAtt = ropFactory.createAttribute();
			    final int index = tmpString[i][j].indexOf('@');
			    boolean attribute = true;
			    if(index > -1 && index < tmpString[i][j].length()) {
				if(tmpString[i][j].substring(index+1,tmpString[i][j].length()).toLowerCase().equals("description")) {
				    act.setOperation(tmpString[i][j].substring(0,index).replace('_', ' '));
				    attribute = false;
				}else {
				    newAtt.setAttributeValue(tmpString[i][j].substring(0,index));
				    newAtt.setType(tmpString[i][j].substring(index+1,tmpString[i][j].length()));
				}
			    }else {
				newAtt.setAttributeValue(tmpString[i][j].substring(0,tmpString[i][j].length()));
				newAtt.setType("Unknowned");
			    }
			    if(attribute) {
				newAtt.setInvisible(true);
				act.getProperties().getAttribute().add(newAtt);
			    }
			}
		    }
		}
		return act;
	    }
	}catch(final Exception ex) {
	    //DEBUG
	    //System.out.println("ConvertAutomatas.createSequenceRelation()");
	    //END DEBUG
	}
	return null;
    }
    public Object createParallelRelation(String[] seq) {
	//DEBUG
	//System.out.println("ConvertAutomatas.createParallelRelation()");
	//END DEBUG
	try {
	    if(seq.length > 2) {
		final Relation rel = ropFactory.createRelation();
		rel.setType(RelationType.PARALLEL);
		boolean loop = true;
		while(loop) {
		    int count = 0;
		    for(int i = 1; i < seq.length; i++) {
			if(seq[i].startsWith("fin")) {
			    count = i;
			    break;
			}
		    }
		    int stIndex = -1;
		    int finIndex = -1;
		    for(int i = 0; i < count; i++) {
			for(int j = 0; j < count; j++) {
			    if(seq[i].substring(2, seq[i].length()).equals(seq[seq.length-1-j].substring(3, seq[seq.length-1-j].length()))) {
				stIndex = i;
				finIndex = seq.length-1-j;
				break;
			    }
			}
		    }
		    if(stIndex != -1 && finIndex != -1) {
			final Activity act = ropFactory.createActivity();
			final String op = seq[stIndex].substring(2, seq[stIndex].length());
			act.setOperation(op);
			for(int i = 0; i < tmpString.length; i++) {
			    if(tmpString[i].length > 1 && tmpString[i][0].equals(op)) {
				act.setProperties(ropFactory.createProperties());
				Attribute newAtt = null;
				for(int j = 1; j < tmpString[i].length; j++) {
				    newAtt = ropFactory.createAttribute();
				    final int index = tmpString[i][j].indexOf('@');
				    boolean attribute = true;
				    if(index > -1 && index < tmpString[i][j].length()) {
					if(tmpString[i][j].substring(index+1,tmpString[i][j].length()).toLowerCase().equals("description")) {
					     act.setOperation(tmpString[i][j].substring(0,index).replace('_',' '));
					     attribute = false;
					}else {
					    newAtt.setAttributeValue(tmpString[i][j].substring(0,index));
					    newAtt.setType(tmpString[i][j].substring(index+1,tmpString[i][j].length()));
					}
				    }else {
					newAtt.setAttributeValue(tmpString[i][j].substring(0,tmpString[i][j].length()));
					newAtt.setType("Unknowned");
				    }
				    if(attribute) {
					newAtt.setInvisible(true);
					act.getProperties().getAttribute().add(newAtt);
				    }
				}
			    }
			}
			rel.getActivityRelationGroup().add(act);
			seq = removeIndex(seq, stIndex, finIndex);
		    }else {
			if(seq.length > 1) {
			    //DEBUG
			    /*
			    System.out.println("DEBUGING");
			    for(int i = 0; i < seq.length; i++) {
				System.out.print(seq[i]);
			    }
			    System.out.println("");
			    **/
			    //END DEBUG
			    rel.getActivityRelationGroup().add(createSequenceRelation(seq));
			}
			loop = false;
		    }
		}
		return rel;
	    }else {
		final Activity act = ropFactory.createActivity();
		final String op = seq[0].substring(2, seq[0].length());
		act.setOperation(op);
		for(int i = 0; i < tmpString.length; i++) {
		    if(tmpString[i].length > 1 && tmpString[i][0].equals(op)) {
			act.setProperties(ropFactory.createProperties());
			Attribute newAtt = null;
			for(int j = 1; j < tmpString[i].length; j++) {
			    newAtt = ropFactory.createAttribute();
			    final int index = tmpString[i][j].indexOf('@');
			    boolean attribute = true;
			    if(index > -1 && index < tmpString[i][j].length()) {
				if(tmpString[i][j].substring(index+1,tmpString[i][j].length()).toLowerCase().equals("description")) {
				     act.setOperation(tmpString[i][j].substring(0,index).replace('_', ' '));
				     attribute = false;
				}else {
				    newAtt.setAttributeValue(tmpString[i][j].substring(0,index));
				    newAtt.setType(tmpString[i][j].substring(index+1,tmpString[i][j].length()));
				}
			    }else {
				newAtt.setAttributeValue(tmpString[i][j].substring(0,tmpString[i][j].length()));
				newAtt.setType("Unknowned");
			    }
			    if(attribute) {
				newAtt.setInvisible(true);
				act.getProperties().getAttribute().add(newAtt);
			    }
			}
		    }
		}
		return act;
	    }
	}catch(final Exception ex) {
	    //DEBUG
	    System.out.println("ERROR ConvertAutomatas.createParallelRelation()");
	    //END DEBUG
	}
	return null;
    }

    public String[] removeIndex(final String[] seq, final int stIndex, final int finIndex) {
	if(seq.length > 1 && stIndex < seq.length && stIndex > -1 && finIndex < seq.length && finIndex > -1) {
	    final String[] tmpSeq = new String[seq.length-2];
	    int tmpIndex = 0;
	    for(int i = 0; i < seq.length; i++) {
		if(!( i == stIndex || i == finIndex)) {
		    tmpSeq[tmpIndex++] = seq[i];
		}
	    }
	    return tmpSeq;
	}
	return new String[0];
    }

    public void viewAutomata(final File file) {
	//DEBUG
	System.out.println("viewAutomata");
	//END DEBUG
	try {
	    final org.supremica.automata.IO.ProjectBuildFromXML builder = new org.supremica.automata.IO.ProjectBuildFromXML();
	    final Project theProject = builder.build(file);
	    for(final Automaton currAutomaton : theProject) {
		final MyAutomatonViewer viewer = new MyAutomatonViewer(currAutomaton);
		viewer.setVisible(true);
		viewer.update();
	    }
	}catch(final Exception ex) {

	}
    }

    class MyAutomatonViewer extends AutomatonViewer {
        private static final long serialVersionUID = 1L;

        public MyAutomatonViewer(final Automaton automaton) throws Exception {
	        super(automaton);
	    }
    }
}
