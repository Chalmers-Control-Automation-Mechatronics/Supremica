(*
Skall testa EQ 
*)
PROGRAM EQtest
   VAR
	BB1, BB2, BB3 : BOOL;
	DD1, DD2, DD3 : DINT;
	RR1, RR2, RR3 : REAL;
	WW1, WW2, WW3 : WSTRING;
   END_VAR
   VAR
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



	(*BOOL- jämför stack med variabel*)
(*T-T*)
	LD TRUE
	ST BB1
	LD TRUE
	EQ BB1
	ST BB2
	CAL WPrint("?? true == ")
	CAL BPrintln(BB2)
	
(*T-F*)
	LD FALSE
	ST BB1
	LD TRUE
	EQ BB1
	ST BB2
	CAL WPrint("?? false == ")
	CAL BPrintln(BB2)
	
(*F-T*)
	LD TRUE
	ST BB1
	LD FALSE
	EQ BB1
	ST BB2
	CAL WPrint("?? false == ")
	CAL BPrintln(BB2)
	
(*F-F*)
	LD FALSE
	ST BB1
	LD FALSE
	EQ BB1
	ST BB2
	CAL WPrint("?? true == ")
	CAL BPrintln(BB2)
	

	(*BOOL- jämför stack med konstant*)
(*T-T*)
	LD TRUE
	EQ TRUE
	ST BB2
	CAL WPrint("?? true == ")
	CAL BPrintln(BB2)
	
(*T-F*)
	LD FALSE
	EQ TRUE
	ST BB2
	CAL WPrint("?? false == ")
	CAL BPrintln(BB2)
	
(*F-T*)
	LD FALSE
	EQ TRUE
	ST BB2
	CAL WPrint("?? false == ")
	CAL BPrintln(BB2)
	
(*F-F*)
	LD FALSE
	ST BB1
	LD FALSE
	EQ BB1
	ST BB2
	CAL WPrint("?? true == ")
	CAL BPrintln(BB2)
	


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
