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

(*=============================================================*)
(*=============================================================*)

FUNCTION_BLOCK LDSTfbfoo
	LD TRUE
END_FUNCTION_BLOCK

FUNCTION_BLOCK LDSTfbtestfb
(*This FB is only a holder of vars, its run method doesn't
  do anything essential*)
   VAR_IN_OUT
	BB1, BB2 : BOOL;
	DD1, DD2 : DINT;
	RR1, RR2 : REAL;
	WW1, WW2 : WSTRING;
	FF : LDSTfbfoo;
   END_VAR
	LD BB1
	ST BB2
	LD DD1
	ST DD2
	LD RR1
	ST RR2
	LD WW1
	ST WW2
END_FUNCTION_BLOCK

PROGRAM LDSTfbtest
	VAR
		WPrintln : PrintlnWSTRING;
		DPrintln  : PrintlnDINT;
		RPrintln  : PrintlnREAL;
		BPrintln  : PrintlnBOOL;
		WPrint   : PrintWSTRING;
		DPrint    : PrintDINT;
		RPrint    : PrintREAL;
		BPrint    : PrintBOOL;
	END_VAR
   VAR
	BB1, BB2 : BOOL;
	DD1, DD2 : DINT;
	RR1, RR2 : REAL;
	WW1, WW2 : WSTRING;
	FB1 : LDSTfbtestfb;
	FB2 : LDSTfbfoo;
   END_VAR

CAL WPrintln("Testing LD & ST of BOOL,DINT,REAL,WSTRING to/from FBs")
   LD TRUE
   ST BB1
   ST FB1.BB1

   LD DINT#1234567890
   ST DD1
   ST FB1.DD1

   LD REAL#123.345
   ST RR1
   ST FB1.RR1

   LD "abcdef"
   ST WW1
   ST FB1.WW1

   CAL FB1

   LD FB1.BB2
   ST BB2
   LD FB1.DD2
   ST DD2
   LD FB1.RR2
   ST RR2
   LD FB1.WW2
   ST WW2

   	CAL WPrint("?? ")
	CAL BPrint(BB1)
	CAL WPrint(" == ")
	CAL BPrintln(BB2)
   
   	CAL WPrint("?? ")
	CAL DPrint(DD1)
	CAL WPrint(" == ")
	CAL DPrintln(DD2)
   
   	CAL WPrint("?? ")
	CAL RPrint(RR1)
	CAL WPrint(" == ")
	CAL RPrintln(RR2)
   
  	CAL WPrint("?? ")
	CAL WPrint(WW1)
	CAL WPrint(" == ")
	CAL WPrintln(WW2)
   
(* Thomas titta på detta*)



	LD FB2
   	ST FB1.FF
   	LD FB1.FF
   	ST FB2

END_PROGRAM














