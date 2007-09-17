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
		
		Module m = new Module(machine,false);
		
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
		
		Relation alt = factory.createRelation();
		alt.setType(RelationType.ALTERNATIVE);
		
		Relation par = factory.createRelation();
		par.setType(RelationType.PARALLEL);
		
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
		
		
		par.getActivityRelationGroup().add(g);
		par.getActivityRelationGroup().add(h);
		par.getActivityRelationGroup().add(i);
		
		seq.getActivityRelationGroup().add(a);
		seq.getActivityRelationGroup().add(b);
		seq.getActivityRelationGroup().add(par);
		seq.getActivityRelationGroup().add(c);
		
		alt.getActivityRelationGroup().add(d);
		alt.getActivityRelationGroup().add(e);
		alt.getActivityRelationGroup().add(f);
		
		
		
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
		rop.setComment("Test Arbitrary");
		
		Relation r = new Relation();
		r.setType(RelationType.ARBITRARY);
		
		r.getActivityRelationGroup().add(0, a);
		r.getActivityRelationGroup().add(1, b);
		//r.getActivityRelationGroup().add(2, c);
		
		rop.setRelation(r);
		
		createEFA(rop);
		System.out.println("Done test ROP");
	}
}
