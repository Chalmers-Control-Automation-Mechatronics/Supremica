package org.supremica.external.processAlgebraPetriNet.ppnedit.converter;

import java.io.*;
import java.util.*;

import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn.*;



/**
 * class to make ROP from a PPN expression String
 */
public class PPNtoROP extends PPNtoRelation {

	public static ROP createROP(String exp) {
		
		if(exp == null){
			return null;
		}
		
		if (!PPN.validExp(exp)) {
			return null;
		}
		
		if (!exp.contains(PPN.EQUAL)) {
			return null;
		}
		
		ROP rop = factory.createROP();

		rop.setType(ROPType.ROP);
		rop.setComment("");
		rop.setId("");
		rop.setMachine("No machine");

		Relation relation = createRelation(exp);
		rop.setRelation(relation);

		return rop;
	}
}
