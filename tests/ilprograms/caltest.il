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



FUNCTION_BLOCK PrintlnWSTRING_DINT
	VAR_IN_OUT
		string: WSTRING;
		dint  : DINT;
	END_VAR
	VAR
		jblockWS: JPrintWSTRING;
		jblockD : JPrintlnDINT;
	END_VAR

	LD string
	ST jblockWS.output
	CAL jblockWS

	LD dint
	ST jblockD.output
	CAL jblockD
END_FUNCTION_BLOCK


FUNCTION_BLOCK DOUBLER
	VAR_IN_OUT
		A : DINT;
	END_VAR

	LD A
	ADD A

	ST A

END_FUNCTION_BLOCK


FUNCTION_BLOCK ADDER
	VAR_IN_OUT
		in_1 : DINT;
	    in_2 : DINT;
		out  : DINT;
	END_VAR

	LD in_1
	ADD in_2

	ST out

END_FUNCTION_BLOCK


FUNCTION_BLOCK ANDER
	VAR_IN_OUT
		in_1 : BOOL;
	    in_2 : BOOL;
		out  : BOOL;
	END_VAR

	LD in_1
	AND in_2
	ST out
	
END_FUNCTION_BLOCK	



PROGRAM CALTEST
	VAR
		print_WS   : PrintWSTRING;
		print_DINT : PrintDINT;
		print_REAL : PrintREAL;
		print_BOOL : PrintBOOL;

		println_WS   : PrintlnWSTRING;
		println_DINT : PrintlnDINT;
		println_REAL : PrintlnREAL;
		println_BOOL : PrintlnBOOL;

		printlnWS_D  : PrintlnWSTRING_DINT;

      	doubler      : DOUBLER;
		adder        : ADDER;
		ander        : ANDER;

		N1           : DINT;
		N2           : DINT;
      	Result_DINT  : DINT;
		Result_BOOL  : BOOL;
	END_VAR

	LD DINT#10
	ST N1


	CAL printlnWS_D("Doubler input: ", N1) 
    CAL doubler(N1)
	CAL printlnWS_D("Doubler output: ", doubler.A) 

	CAL printlnWS_D("Doubler input: ", N1) 
    CAL doubler(A:=N1)
	CAL printlnWS_D("Doubler output: ", doubler.A) 

	CAL printlnWS_D("Doubler input: ", DINT#33) 
    CAL doubler(DINT#33)
	CAL printlnWS_D("Doubler output: ", doubler.A) 

	CAL printlnWS_D("Doubler input: ", DINT#33) 
    CAL doubler(A:=DINT#33)
	CAL printlnWS_D("Doubler output: ", doubler.A) 



	CAL adder(in_1:=DINT#100, in_2:=DINT#200, out=>Result_DINT)
	CAL println_DINT(Result_DINT)	
	

	CAL ander(in_1:=TRUE, in_2:=TRUE, out=>Result_BOOL)
	CAL println_BOOL(Result_BOOL)


END_PROGRAM
