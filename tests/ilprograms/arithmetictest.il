(* Must test EQ before this.
 *)
PROGRAM ArithmeticTest
   VAR
	BB1, BB2, BB3 : BOOL;
	DD1, DD2, DD3 : DINT;
	RR1, RR2, RR3 : REAL;
	WW1, WW2, WW3 : WSTRING;
   END_VAR
   VAR
	Assert   : Asserter;
	(*Printing*)
	WPrintln : PrintlnWSTRING;
	DPrintln : PrintlnDINT;
	BPrintln : PrintlnBOOL;
	RPrintln : PrintlnREAL;
	WPrint   : PrintWSTRING;
	DPrint   : PrintDINT;
	BPrint   : PrintBOOL;
	RPrint   : PrintREAL;
   END_VAR

CAL WPrintln("Testing ADD")
	CAL Assert(TRUE,"här skall INGET fel ges")
	CAL Assert(FALSE,"här skall ett fel ges")
CAL WPrintln("Testing SUB")

CAL WPrintln("Testing MUL")

CAL WPrintln("Testing DIV")

CAL WPrintln("Testing MOD")

END_PROGRAM

FUNCTION_BLOCK Asserter
	VAR_IN_OUT
		result : BOOL;
		msg    : WSTRING;
	END_VAR
	VAR
		WPrintln : PrintlnWSTRING;
		WPrint   : PrintWSTRING;
	END_VAR
	LD result
	JMPC noerror
		CAL WPrint(output := "fdsf")
		(*CAL WPrint("Error: assertion not true! : ")*)
		(*CAL WPrintln(msg)*)
noerror:
END_FUNCTION_BLOCK







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
