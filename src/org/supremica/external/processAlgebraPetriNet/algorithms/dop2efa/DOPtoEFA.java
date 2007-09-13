/**
 * class to convert from DOP to EFA 
 */
package org.supremica.external.processAlgebraPetriNet.algorithms.dop2efa;


import java.io.File;
import java.util.List;
import java.util.LinkedList;

import org.supremica.manufacturingTables.xsd.rop_copvision.*;

/**
 * @author David Millares
 *
 */
public class DOPtoEFA extends DOPrelation{
	
	private static String outputFileName = "testModule.wmod";
	
	public DOPtoEFA(){}
	
	public static void createEFA(ROP rop){
		createEFA(rop,new File(outputFileName));
	}
	
	public static void createEFA(ROP rop, File f){
		
		String comment = rop.getComment(); 
		if(comment == null || comment.length() == 0){
			comment = "no comment";
		}
		
		String machine = rop.getMachine(); 
		if(machine == null || machine.length() == 0){
			machine = "no_machine";
		}
		
		Module m = new Module(machine,true);
		
		ObjectFactory factory = new ObjectFactory();
		
		Relation main_sequence = factory.createRelation();
		main_sequence.setType(RelationType.SEQUENCE);
		
		Activity start_machine = factory.createActivity();
		Activity stop_machine = factory.createActivity();
		
		//special will be parsed in class EGA 
		start_machine.setOperation(PGA.ONLY_START + machine);
		stop_machine.setOperation(PGA.ONLY_STOP + machine);
		
		//build main sequence
		main_sequence.getActivityRelationGroup().add(start_machine);
		main_sequence.getActivityRelationGroup().add(rop.getRelation());
		main_sequence.getActivityRelationGroup().add(stop_machine);
		
		main_sequence = collapseRelationTree(main_sequence);
		
		EFA main_efa = new EFA("main_" + machine,m);
		m.addAutomaton(main_efa);
		
		sequence(main_sequence,machine +  "_idle",machine + "_finish", main_efa);
		
		m.writeToFile(f);
	}
	
	
	
	public static void createEFA(File ropFile, File f){
		
		//check indata
		if(ropFile == null || f == null){
			return;
		}
		
		createEFA(getROPfromFile(ropFile),f);
	}
	
	
	
	public static ROP getROPfromFile(File f){
		
		if(f != null && f.exists()){
			Loader loader = new Loader();
			Object o = loader.open(f);
			
			if(o instanceof ROP){
				return (ROP)o;
			}
		}
		System.err.println("File " + f + " contains no ROP.");
		return null;
	}
	
	
	/**
	 * Test main method to test this class
	 * @param args
	 */
	public static void main(String[] args) {
		
		Module m = new Module("Test",true);
		
		EFA efa = new EFA("seq",m);
		EFA efa1 = new EFA("alt",m);
		
		m.addAutomaton(efa);
		m.addAutomaton(efa1);
		
		//build pgaList
		
		PGA pga = new PGA();
		PGA pga1 = new PGA();
		PGA pga2 = new PGA();
		
		pga.setProcess("Op0");
		pga1.setProcess("Op1");
		pga2.setProcess("Op2");
		
		//efa.addIntegerVariable("i", 0, 5, 0, null);
		//efa.addIntegerVariable("j", 1, 5, 2, null);
		
		//pga.setStartGuard("i==1");
		//pga.setStartAction("i+=1");
		
		
		//pga.andStartGuard("j==2;");
		
		List<PGA> pgaList = new LinkedList<PGA>();
		pgaList.add(pga);
		pgaList.add(pga1);
		pgaList.add(pga2);
		
		//build egaList
		List<String> egaList = new LinkedList<String>();
		
		egaList.add("a");
		egaList.add("b");
		egaList.add("c");
		egaList.add("d");
		
		
		//build relation
		ObjectFactory factory  = new ObjectFactory();
		
		Relation seq = factory.createRelation();
		seq.setType(RelationType.SEQUENCE);
		
		Activity a = factory.createActivity();
		a.setOperation("a");
		
		Activity b = factory.createActivity();
		b.setOperation("b");
		
		Activity c = factory.createActivity();
		c.setOperation("c");
		
		seq.getActivityRelationGroup().add(a);
		seq.getActivityRelationGroup().add(b);
		seq.getActivityRelationGroup().add(c);
		
		Relation alt = factory.createRelation();
		alt.setType(RelationType.ALTERNATIVE);
		
		alt.getActivityRelationGroup().add(a);
		alt.getActivityRelationGroup().add(b);
		alt.getActivityRelationGroup().add(c);
		
		Relation par = factory.createRelation();
		par.setType(RelationType.PARALLEL);
		
		par.getActivityRelationGroup().add(a);
		par.getActivityRelationGroup().add(b);
		par.getActivityRelationGroup().add(c);
		
		/*
		//Test native
		//efa.addState("s0");
		//efa.addState("s1");

		//nativeProcess(pga,"s0","s1",efa);
		
		
		
		nativeSequence(pgaList,"start","end",efa);
		nativeAlternative(pgaList,"start","end",efa1);
		
		
		//nativeAlternative(egaList,"start","end",efa1);
		
		
		m.writeToFile(new File(outputFileName));
		System.out.println("Done test native");
		*/
		
		
		/*
		//Test Relation
		
		sequence(seq,"start","end",efa);
		alternative(alt, "from","to", efa1);
		
		m.writeToFile(new File(outputFileName));
		System.out.println("Done test Relation");
		*/
		
		//Test ROP
		
		ROP rop = factory.createROP();
		rop.setMachine("machine");
		rop.setComment("Test Parallel");
		
		Relation r = new Relation();
		r.setType(RelationType.PARALLEL);
		
		r.getActivityRelationGroup().add(0, a);
		r.getActivityRelationGroup().add(1, par);
		r.getActivityRelationGroup().add(2, par);
		
		rop.setRelation(r);
		
		createEFA(rop);
		System.out.println("Done test ROP");
		
		
		/*
		System.out.println("Try to create EFA");
		DOPtoEFA.createEFA();
		System.out.println("Done create EFA");
		
		
		
		ObjectFactory factory  = new ObjectFactory();
		
		
		//test activities
		Activity a = factory.createActivity();
		a.setOperation("a");
		
		Activity b = factory.createActivity();
		b.setOperation("b");
		
		Activity c = factory.createActivity();
		c.setOperation("c");
		
		Activity d = factory.createActivity();
		d.setOperation("d");
		
		Activity e = factory.createActivity();
		e.setOperation("e");
		
		Activity f = factory.createActivity();
		f.setOperation("f");
		
		Activity g = factory.createActivity();
		g.setOperation("g");
		
		Activity h = factory.createActivity();
		h.setOperation("h");
		
		Activity i = factory.createActivity();
		i.setOperation("i");
		
		Activity j = factory.createActivity();
		j.setOperation("j");
		
		
		
		//test relations
		Relation seq1 = factory.createRelation();
		seq1.setType(RelationType.SEQUENCE);
		
		Relation seq2 = factory.createRelation();
		seq2.setType(RelationType.SEQUENCE);
		
		Relation seq3 = factory.createRelation();
		seq3.setType(RelationType.SEQUENCE);
		
		
		Relation alt1 = factory.createRelation();
		alt1.setType(RelationType.ALTERNATIVE);
		
		Relation alt2 = factory.createRelation();
		alt2.setType(RelationType.ALTERNATIVE);
		
		Relation alt3 = factory.createRelation();
		alt3.setType(RelationType.ALTERNATIVE);
		
		//relation alt1..3 seq1..3
		//activity a b c d e f g h i j
		
		
		ROP rop = (new ObjectFactory()).createROP();
		rop.setMachine("machine");
		rop.setComment("Test sequence alternative");
		
		//create test relation
		
		//seq1
		seq1.getActivityRelationGroup().add(a);
		alt1.getActivityRelationGroup().add(b);
		alt1.getActivityRelationGroup().add(c);
		
		seq1.getActivityRelationGroup().add(alt1);
		
		seq1.getActivityRelationGroup().add(d);
		//end seq1
		
		//seq2
		seq2.getActivityRelationGroup().add(e);
		
		alt2.getActivityRelationGroup().add(f);
		alt2.getActivityRelationGroup().add(g);
		
		seq2.getActivityRelationGroup().add(alt2);
		
		seq2.getActivityRelationGroup().add(h);
		//end seq2
		
		//alt3
		alt3.getActivityRelationGroup().add(seq1);
		alt3.getActivityRelationGroup().add(i);
		alt3.getActivityRelationGroup().add(seq2);
		//end alt3
		
		seq3.getActivityRelationGroup().add(seq1);
		seq3.getActivityRelationGroup().add(seq2);
		
		seq3 = flattenSequence(seq3);
		
		//seq3.getActivityRelationGroup().add(c);
		//seq3.getActivityRelationGroup().add(d);
		
		rop.setRelation(seq3);

		//System.out.println("Try to createEFA(rop)");
		//createEFA(rop);
		//System.out.println("Done createEFA(rop)");
	
		 */
		
	}
}
