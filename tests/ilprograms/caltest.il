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

      	doubler      : DOUBLER;
		adder        : ADDER;
      	Result       : DINT;
   END_VAR


    CAL doubler(A:=DINT#7)
    LD doubler.A
	ST Result
	CAL println_DINT(output:=Result)	

	CAL adder(in_1:=DINT#100, in_2:=DINT#200, out=>Result)
	CAL println_DINT(output:=Result)	
	

END_PROGRAM
