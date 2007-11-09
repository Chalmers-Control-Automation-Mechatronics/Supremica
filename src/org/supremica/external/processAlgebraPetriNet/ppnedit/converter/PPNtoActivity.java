package org.supremica.external.processAlgebraPetriNet.ppnedit.converter;

import java.io.*;
import java.util.*;

import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn.*;



//class to convert a String expression to Activity
public class PPNtoActivity {

	protected static ObjectFactory factory = new ObjectFactory();

	public static Activity createActivity(String exp) {

		if (exp == null || exp.length() == 0) {
			return null;
		}

		Activity a = factory.createActivity();

		a.setOperation(getActOperation(exp));
		a.setPrecondition(getPrecondition(exp));

		return a;
	}

	private static String getActOperation(String exp) {
		return exp;
	}

	private static Precondition getPrecondition(String exp) {
		Precondition precon = factory.createPrecondition();

		// loop
		OperationReferenceType opreftyp = factory
				.createOperationReferenceType();

		opreftyp.setMachine(getMachine(exp));
		opreftyp.setOperation(getPreOperation(exp));

		precon.getPredecessor().add(opreftyp);
		// end loop

		return precon;
	}

	private static String getMachine(String exp) {
		return "";
	}

	private static String getPreOperation(String exp) {
		return "";
	}
}
