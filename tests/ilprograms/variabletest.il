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
(* Resultat:
	Titta vid XXX kommentarer för att hitta fel
   Innehåll:
     Test1
	Program LDSTtest: för att test LD och ST för 4 datatyper

     Test2
	FB  LDSTfbtestfb
	PRG LDSTfbtest : för att test att 4 datatyper + funktionsblock
			 kan sparas i FB io-variabler
*)

(*Test1*)
PROGRAM LDSTtest
	VAR
		WSpln : PrintlnWSTRING;
		Dpln  : PrintlnDINT;
		Rpln  : PrintlnREAL;
		Bpln  : PrintlnBOOL;
		WSp   : PrintWSTRING;
		Dp    : PrintDINT;
		Rp    : PrintREAL;
		Bp    : PrintBOOL;
	END_VAR
   VAR
	BB1, BB2 : BOOL;
	DD1, DD2 : DINT;
	RR1, RR2 : REAL;
	WW1, WW2 : WSTRING;
   END_VAR
(*BOOL*)
(*====*)
   CAL PRINTLN("Tests of BOOL constants and variables")
   (*BOOL*)
   LD TRUE
   ST BB1
   CAL PRINT("?? true == ", BB1)
   CAL PRINTLN("")

   (*BOOL*)
   LD FALSE
   ST BB1
   CAL PRINT("?? false == ", BB1)
   CAL PRINTLN("")

   (*BOOL*)
   LD BOOL#1
   ST BB1
   CAL PRINT("?? true == ", BB1)
   CAL PRINTLN("")

   (*BOOL*)
   LD BOOL#0
   ST BB1
   CAL PRINT("?? false == ", BB1)
   CAL PRINTLN("")

   (*BOOL*)
   LD TRUE
   ST BB1
   LD BB1
   ST BB2
   CAL PRINT("?? true == ", BB2)
   CAL PRINTLN("")

   (*BOOL*)
   LD FALSE
   ST BB1
   LD BB1
   ST BB2
   CAL PRINT("?? false == ", BB2)
   CAL PRINTLN("")

CAL PRINTLN("Testing LDN/STN")
   (*LDN/STN finns bara på BOOL*)
   LDN TRUE
   ST  BB1
   CAL PRINT("?? false == ", BB1)
   CAL PRINTLN("")

   LDN FALSE
   ST  BB1
   CAL PRINT("?? true == ", BB1)
   CAL PRINTLN("")

   LD  TRUE
   STN BB1
   CAL PRINT("?? false == ", BB1)
   CAL PRINTLN("")

   LD  TRUE
   STN  BB1
   CAL PRINT("?? false == ", BB1)
   CAL PRINTLN("")

   LD TRUE
   ST  BB2
   LDN BB2
   ST  BB1
   CAL PRINT("?? false == ", BB1)
   CAL PRINTLN("")

   LD FALSE
   ST  BB2
   LDN BB2
   ST  BB1
   CAL PRINT("?? true == ", BB1)
   CAL PRINTLN("")

   LD  TRUE
   STN BB1
   CAL PRINT("?? false == ", BB1)
   CAL PRINTLN("")

   LD  FALSE
   STN  BB1
   CAL PRINT("?? true == ", BB1)
   CAL PRINTLN("")

(*Fall som alltid skall ge fel*)
(*
	ST DINT#3
	STN TRUE
*)


(*DINT*)
(*====*)
CAL PRINTLN("Tests of DINT constants and variables")
   (*DINT*)
   LD DINT#0
   ST DD1
   CAL PRINT("?? 0 == ", DD1)
   CAL PRINTLN("")

   (*DINT*)
   LD DINT#-1
   ST DD1
   CAL PRINT("?? -1 == ", DD1)
   CAL PRINTLN("")

   (*DINT*)
   LD DINT#1
   ST DD1
   CAL PRINT("?? 1 == ", DD1)
   CAL PRINTLN("")

   (*DINT*)
   LD DINT#23_344
   ST DD1
   CAL PRINT("?? 23344 == ", DD1)
   CAL PRINTLN("")

   (*DINT: test of binary constants*)
   LD DINT#2#1111_1111
   ST DD1
   CAL PRINT("?? 255 == ", DD1)
   CAL PRINTLN("")

   LD DINT#2#0000_1000
   ST DD1
   CAL PRINT("?? 8 == ", DD1)
   CAL PRINTLN("")

   LD DINT#2#0
   ST DD1
   CAL PRINT("?? 0 == ", DD1)
   CAL PRINTLN("")

   LD DINT#2#11
   ST DD1
   CAL PRINT("?? 3 == ", DD1)
   CAL PRINTLN("")

   (*DINT: test of octal constants*)
   LD DINT#8#377
   ST DD1
   CAL PRINT("?? 255 == ", DD1)
   CAL PRINTLN("")

   LD DINT#8#020
   ST DD1
   CAL PRINT("?? 16 == ", DD1)
   CAL PRINTLN("")

   LD DINT#8#0
   ST DD1
   CAL PRINT("?? 0 == ", DD1)
   CAL PRINTLN("")

   (*DINT: test of hexadecimal constants*)
   LD DINT#16#FF
   ST DD1
   CAL PRINT("?? 255 == ", DD1)
   CAL PRINTLN("")

   LD DINT#16#A0
   ST DD1
   CAL PRINT("?? 160 == ", DD1)
   CAL PRINTLN("")

   LD DINT#16#0
   ST DD1
   CAL PRINT("?? 0 == ", DD1)
   CAL PRINTLN("")

   (*DINT: test of variables*)
   LD DINT#34
   ST DD1
   LD DD1
   ST DD2
   CAL PRINT("?? 34 == ", DD2)
   CAL PRINTLN("")

(*REAL*)
(*====*)
CAL PRINTLN("Tests of REAL constants and variables")

   LD REAL#0.0
   ST RR1
   CAL PRINT("?? 0 == ", RR1)
   CAL PRINTLN("")

   LD REAL#12.12
   ST RR1
   CAL PRINT("?? 12.12 == ", RR1)
   CAL PRINTLN("")

   LD REAL#-12.12
   ST RR1
   CAL PRINT("?? -12.12 == ", RR1)
   CAL PRINTLN("")

   LD REAL#0.1
   ST RR1
   CAL PRINT("?? 0.1 == ", RR1)
   CAL PRINTLN("")

   LD REAL#10.123
   ST RR1
   CAL PRINT("?? 10.123 == ", RR1)
   CAL PRINTLN("")

   LD REAL#+12_123.21
   ST RR1
   CAL PRINT("?? 12123.21 == ", RR1)
   CAL PRINTLN("")

   LD REAL#-0.001298
   ST RR1
   CAL PRINT("?? -0.001298 == ", RR1)
   CAL PRINTLN("")

   LD REAL#-1.65E-10
   ST RR1
   CAL PRINT("?? -1.65E-10 == ", RR1)
   CAL PRINTLN("")

   LD REAL#0.9E20
   ST RR1
   CAL PRINT("?? 0.9E20 == ", RR1)
   CAL PRINTLN("")

   LD REAL#0.3276e+14
   ST RR1
   CAL PRINT("?? 0.3276e+14== ", RR1)
   CAL PRINTLN("")

   LD REAL#0.3276e+14
   ST RR1
   LD RR1
   ST RR2
   CAL PRINT("?? 0.3276e+14== ", RR2)
   CAL PRINTLN("")

(*WSTRING*)
(*=======*)
CAL PRINTLN("Tests of WSTRING constants and variables")

   LD "Testing double_byte_character_string"
   ST WW1
   CAL PRINT("?? Testing double_byte_character_string == ", WW1)
   CAL PRINTLN("")

   LD "åäöÅÄÖ!@#$%^&*()_+|~1" (*234567890-=\`¤{}[]:;$"'<>,.?/"*)
   ST WW1
   CAL PRINTLN("?? åäöÅÄÖ!@#$%^&*()_+|~1 == ")
   CAL PRINT("   ", WW1)
   CAL PRINTLN("")

(*XXX error, see further comments*)
   LD "234567890-=`{}[]:;$"'<>$\,.?/$"
			(* XXX ¤ does not work*)
			(* XXX $\ should print as \
			   XXX $" should print as "
			*)
   ST WW1
   CAL PRINTLN("?? 234567890-=`{}[]:;$"'<>$\,.?/$ == ")
   CAL PRINT("   ", WW1)
   CAL PRINTLN("")

   LD "abcdefghijklmnopqrstuvxyzåäöABCDEFGHIJKLMNOPQRSTUVXYZÅÄÖ"
   ST WW1
   LD WW1
   ST WW2
   CAL PRINTLN("?? abcdefghijklmnopqrstuvxyzåäöABCDEFGHIJKLMNOPQRSTUVXYZÅÄÖ == ")
   CAL PRINT("   ", WW2)
   CAL PRINTLN("")
END_PROGRAM

(*Test 2*)

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
		WSpln : PrintlnWSTRING;
		Dpln  : PrintlnDINT;
		Rpln  : PrintlnREAL;
		Bpln  : PrintlnBOOL;
	END_VAR
   VAR
	BB1, BB2 : BOOL;
	DD1, DD2 : DINT;
	RR1, RR2 : REAL;
	WW1, WW2 : WSTRING;
	FB1 : LDSTfbtestfb;
	FB2 : LDSTfbfoo;
   END_VAR

CAL PRINTLN("Testing LD & ST of BOOL,DINT,REAL,WSTRING to/from FBs")
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

   CAL PRINT("?? ",BB1," == ",BB2)
   CAL PRINTLN("")
   CAL PRINT("?? ",DD1," == ",DD2)
   CAL PRINTLN("")
   CAL PRINT("?? ",RR1," == ",RR2)
   CAL PRINTLN("")
   CAL PRINT("?? ",WW1," == ",WW2)
   CAL PRINTLN("")
(* Thomas titta på detta*)


(*
CAL PRINTLN("Testing LD & ST of FBs to/from FBs")
*)
   LD FB2
   ST FB1.FF
   LD FB1.FF
   ST FB2

END_PROGRAM

