package org.supremica.softplc.CompILer.CodeGen.Constants;

import java.util.*;

/**Class IlSimpleOperators provides common constants for
 * representing IL's instructions
 * @author Anders Röding
 */
public class IlSimpleOperator
{
	private static List<IlSimpleOperator> theInstructions = new ArrayList<IlSimpleOperator>();
	private String instruction;

	private IlSimpleOperator(String s)
	{
		instruction = s;

		theInstructions.add(this);
	}

	/**iterator gives an iterator over the instruction constants*/
	public static Iterator<IlSimpleOperator> iterator()
	{
		return theInstructions.iterator();
	}

	/**toString gives a string representation of an instruction*/
	public String toString()
	{
		return instruction;
	}

	public static IlSimpleOperator

	// il_simple_operation -> il_simple_operator
	LD = new IlSimpleOperator("LD"), LDN = new IlSimpleOperator("LDN"),
	ST = new IlSimpleOperator("ST"), STN = new IlSimpleOperator("STN"),
	NOT = new IlSimpleOperator("NOT"), S = new IlSimpleOperator("S"),
	R = new IlSimpleOperator("R"), S1 = new IlSimpleOperator("S1"),
	R1 = new IlSimpleOperator("R1"), CLK = new IlSimpleOperator("CLK"),
	CU = new IlSimpleOperator("CU"), CD = new IlSimpleOperator("CD"),
	PV = new IlSimpleOperator("PV"), IN = new IlSimpleOperator("IN"),
	PT = new IlSimpleOperator("PT"), AND = new IlSimpleOperator("AND"),    // operators "AND" and "&"
	OR = new IlSimpleOperator("OR"), XOR = new IlSimpleOperator("XOR"),
	ANDN = new IlSimpleOperator("ANDN"),    // operators "ANDN" and "&N"
	ORN = new IlSimpleOperator("ORN"), XORN = new IlSimpleOperator("XORN"),
	ADD = new IlSimpleOperator("ADD"), SUB = new IlSimpleOperator("SUB"),
	MUL = new IlSimpleOperator("MUL"), DIV = new IlSimpleOperator("DIV"),
	MOD = new IlSimpleOperator("MOD"), GT = new IlSimpleOperator("GT"),
	GE = new IlSimpleOperator("GE"), EQ = new IlSimpleOperator("EQ"),
	LT = new IlSimpleOperator("LT"), LE = new IlSimpleOperator("LE"),
	NE = new IlSimpleOperator("NE");

	public static IlSimpleOperator getOperator(String s)
		throws IllegalOperatorException
	{
		if (s.equals("LD"))
		{
			return LD;
		}
		else if (s.equals("LDN"))
		{
			return LDN;
		}
		else if (s.equals("ST"))
		{
			return ST;
		}
		else if (s.equals("STN"))
		{
			return STN;
		}
		else if (s.equals("NOT"))
		{
			return NOT;
		}
		else if (s.equals("S"))
		{
			return S;
		}
		else if (s.equals("R"))
		{
			return R;
		}
		else if (s.equals("S1"))
		{
			return S1;
		}
		else if (s.equals("R1"))
		{
			return R1;
		}
		else if (s.equals("CLK"))
		{
			return CLK;
		}
		else if (s.equals("CU"))
		{
			return CU;
		}
		else if (s.equals("CD"))
		{
			return CD;
		}
		else if (s.equals("PV"))
		{
			return PV;
		}
		else if (s.equals("IN"))
		{
			return IN;
		}
		else if (s.equals("PT"))
		{
			return PT;
		}
		else if (s.equals("AND"))
		{
			return AND;
		}
		else if (s.equals("&"))
		{
			return AND;    // ?????????????
		}
		else if (s.equals("OR"))
		{
			return OR;
		}
		else if (s.equals("XOR"))
		{
			return XOR;
		}
		else if (s.equals("ANDN"))
		{
			return ANDN;
		}
		else if (s.equals("&N"))
		{
			return ANDN;    // ???????????????????
		}
		else if (s.equals("ORN"))
		{
			return ORN;
		}
		else if (s.equals("XORN"))
		{
			return XORN;
		}
		else if (s.equals("ADD"))
		{
			return ADD;
		}
		else if (s.equals("SUB"))
		{
			return SUB;
		}
		else if (s.equals("MUL"))
		{
			return MUL;
		}
		else if (s.equals("DIV"))
		{
			return DIV;
		}
		else if (s.equals("MOD"))
		{
			return MOD;
		}
		else if (s.equals("GT"))
		{
			return GT;
		}
		else if (s.equals("GE"))
		{
			return GE;
		}
		else if (s.equals("EQ"))
		{
			return EQ;
		}
		else if (s.equals("LT"))
		{
			return LT;
		}
		else if (s.equals("LE"))
		{
			return LE;
		}
		else if (s.equals("NE"))
		{
			return NE;
		}

		throw new IllegalOperatorException();
	}
}
