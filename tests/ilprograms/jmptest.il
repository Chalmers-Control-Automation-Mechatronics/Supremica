(*This program was run successfully by Anders on thursday 25 April 09:00*)
PROGRAM Jmptest
   VAR
      A : BOOL; 
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

CAL WPrintln("Basic tests....")

(*Test: JMP*)
(*===========*)
    LD TRUE
    JMP e1
        CAL WPrintln("Error")
e1: 
    CAL WPrintln("JMP OK")
    (*Check that TRUE remains in resultregister*)
    ST A
	CAL WPrint("OK if true:")
	CAL BPrintln(A)
    
(*Test: JMPC*)
    (*True should be on the stack*)
    JMPC e2
        CAL WPrintln("Error: JMPC")
e2:	
    ST A
	CAL WPrint("OK if true:")
	CAL BPrintln(A)

(*Test: JMPCN*)
    (*True should be on the stack*)
    JMPCN e3
	CAL WPrintln("JMPCN OK") 	
e3:    
(*-------------------------*)
(*Reversed tests*)
(*Expected output:
	JMPC  OK
	JMPCN OK
*)
CAL WPrintln("Reversed tests....")
    LD FALSE
    JMPC e4
	CAL WPrintln("JMPC OK")
e4:
    (*false in RR*)
    JMPCN e5
        CAL WPrintln("ERROR: JMPCN")
	JMP e6
e5: CAL WPrintln("JMPC OK")
e6:  
 
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
