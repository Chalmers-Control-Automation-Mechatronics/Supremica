(*JAVA_BLOCKS for time handling*)
(*=============================*)
JAVA_BLOCK OnDelayTimer
	VAR_IN_OUT
		tonIN : BOOL;
		tonPT : DINT;
		tonET : DINT;
		tonQ  : BOOL;
	END_VAR
END_JAVA_BLOCK
FUNCTION_BLOCK TON
	VAR_IN_OUT
		tonIN : BOOL;
		tonPT : DINT;
		tonET : DINT;
		tonQ  : BOOL;
	END_VAR
	VAR
		odt : OnDelayTimer;
	END_VAR
	LD tonIN
	ST odt.tonIN
	LD tonPT
	ST odt.tonPT
	CAL odt
	LD odt.tonET
	ST tonET
	LD odt.tonQ
	ST tonQ
(*
	CAL PRINT("tid =", tonET)
	CAL PRINTLN("")
*)
END_FUNCTION_BLOCK
PROGRAM TONtest
	VAR
		t : TON;
		b : BOOL;
		tid : DINT;
	END_VAR
	LD DINT#3000
	ST t.tonPT
	LD TRUE
	ST t.tonIN
	CAL t
	LD t.tonQ
	ST b
	LD t.tonET
	ST tid
	(*CAL PRINTLN("b= ",b,tid)*)
END_PROGRAM

(*JAVA_BLOCKS for printing (no newline)*)
(*=====================================*)

JAVA_BLOCK JPrintWSTRING
	VAR_IN_OUT
		output: WSTRING;
	END_VAR
END_JAVA_BLOCK
FUNCTION_BLOCK PrintWSTRING
	VAR_IN_OUT
		output: WSTRING;
	END_VAR
	VAR
		jblock: JPrintWSTRING;
	END_VAR
	LD output
	ST jblock.output
	CAL jblock
END_FUNCTION_BLOCK

JAVA_BLOCK JPrintDINT
	VAR_IN_OUT
		output: DINT;
	END_VAR
END_JAVA_BLOCK
FUNCTION_BLOCK PrintDINT
	VAR_IN_OUT
		output: DINT;
	END_VAR
	VAR
		jblock: JPrintDINT;
	END_VAR
	LD output
	ST jblock.output
	CAL jblock
END_FUNCTION_BLOCK
JAVA_BLOCK JPrintREAL
	VAR_IN_OUT
		output:REAL;
	END_VAR
END_JAVA_BLOCK
FUNCTION_BLOCK PrintREAL
	VAR_IN_OUT
		output:REAL;
	END_VAR
	VAR
		jblock:JPrintREAL;
	END_VAR
	LD output
	ST jblock.output
	CAL jblock
END_FUNCTION_BLOCK

JAVA_BLOCK JPrintBOOL
	VAR_IN_OUT
		output: BOOL;
	END_VAR
END_JAVA_BLOCK
FUNCTION_BLOCK PrintBOOL
	VAR_IN_OUT
		output: BOOL;
	END_VAR
	VAR
		jblock: JPrintBOOL;
	END_VAR
	LD output
	ST jblock.output
	CAL jblock
END_FUNCTION_BLOCK


(*testprogram for printing (no newline)*)
PROGRAM PRINTtest
	VAR
		WSp : PrintWSTRING;
		Dp  : PrintDINT;
		Rp  : PrintREAL;
		Bp  : PrintBOOL;
	END_VAR
	LD  "HELLO"
	ST  WSp.output
	CAL WSp

	LD  DINT#5677
	ST  Dp.output
	CAL Dp

	LD  REAL#45.5667
	ST  Rp.output
	CAL Rp

	LD  TRUE
	ST  Bp.output
	CAL Bp
END_PROGRAM


(*JAVA_BLOCKS for printing (newline)*)
(*=====================================*)

JAVA_BLOCK JPrintlnWSTRING
	VAR_IN_OUT
		output: WSTRING;
	END_VAR
END_JAVA_BLOCK
FUNCTION_BLOCK PrintlnWSTRING
	VAR_IN_OUT
		output: WSTRING;
	END_VAR
	VAR
		jblock: JPrintlnWSTRING;
	END_VAR
	LD output
	ST jblock.output
	CAL jblock
END_FUNCTION_BLOCK

JAVA_BLOCK JPrintlnDINT
	VAR_IN_OUT
		output: DINT;
	END_VAR
END_JAVA_BLOCK
FUNCTION_BLOCK PrintlnDINT
	VAR_IN_OUT
		output: DINT;
	END_VAR
	VAR
		jblock: JPrintlnDINT;
	END_VAR
	LD output
	ST jblock.output
	CAL jblock
END_FUNCTION_BLOCK

JAVA_BLOCK JPrintlnREAL
	VAR_IN_OUT
		output:REAL;
	END_VAR
END_JAVA_BLOCK
FUNCTION_BLOCK PrintlnREAL
	VAR_IN_OUT
		output:REAL;
	END_VAR
	VAR
		jblock:JPrintlnREAL;
	END_VAR
	LD output
	ST jblock.output
	CAL jblock
END_FUNCTION_BLOCK

JAVA_BLOCK JPrintlnBOOL
	VAR_IN_OUT
		output: BOOL;
	END_VAR
END_JAVA_BLOCK
FUNCTION_BLOCK PrintlnBOOL
	VAR_IN_OUT
		output: BOOL;
	END_VAR
	VAR
		jblock: JPrintlnBOOL;
	END_VAR
	LD output
	ST jblock.output
	CAL jblock
END_FUNCTION_BLOCK


(*testprogram for printing (no newline)*)
PROGRAM PRINTLNtest
	VAR
		WSpln : PrintlnWSTRING;
		Dpln  : PrintlnDINT;
		Rpln  : PrintlnREAL;
		Bpln  : PrintlnBOOL;
	END_VAR
	LD  "HELLO"
	ST  WSpln.output
	CAL WSpln

	CAL WSpln(output := "ddddddddddddddddddddddddddddd")

	LD  DINT#5677
	ST  Dpln.output
	CAL Dpln

	LD  REAL#45.5667
	ST  Rpln.output
	CAL Rpln

	LD  TRUE
	ST  Bpln.output
	CAL Bpln

	CAL WSpln(output:="hej!!!")
END_PROGRAM
