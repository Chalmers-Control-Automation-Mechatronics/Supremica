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
   END_VAR
(*BOOL*)
(*====*)
   CAL WPrintln("Tests of BOOL constants and variables")
   (*BOOL*)
   LD TRUE
   ST BB1
   CAL WPrint("?? true == ")
   CAL BPrintln(BB1)

   (*BOOL*)
   LD FALSE
   ST BB1
   CAL WPrint("?? false == ")
   CAL BPrintln(BB1)
   

   (*BOOL*)
   LD BOOL#1
   ST BB1
   CAL WPrint("?? true == ")
   CAL BPrint(BB1)
   

   (*BOOL*)
   LD BOOL#0
   ST BB1
   CAL WPrint("?? false == ")
   CAL BPrint(BB1)
   

   (*BOOL*)
   LD TRUE
   ST BB1
   LD BB1
   ST BB2
   CAL WPrint("?? true == ")
   CAL BPrintln(BB2)
   

   (*BOOL*)
   LD FALSE
   ST BB1
   LD BB1
   ST BB2
   CAL WPrint("?? false == ")
   CAL BPrintln(BB2)
   

CAL WPrintln("Testing LDN/STN")
   (*LDN/STN finns bara på BOOL*)
   LDN TRUE
   ST  BB1
   CAL WPrint("?? false == ")
   CAL BPrintln(BB1)
   

   LDN FALSE
   ST  BB1
   CAL WPrint("?? true == ") 
   CAL BPrintln(BB1)
   

   LD  TRUE
   STN BB1
   CAL WPrint("?? false == ")
   CAL BPrintln(BB1)
   

   LD  TRUE
   STN  BB1
   CAL WPrint("?? false == ")
   CAL BPrintln(BB1)
   

   LD TRUE
   ST  BB2
   LDN BB2
   ST  BB1
   CAL WPrint("?? false == ")
   CAL BPrintln(BB1)
   

   LD FALSE
   ST  BB2
   LDN BB2
   ST  BB1
   CAL WPrint("?? true == ")
   CAL BPrintln(BB1)

   LD  TRUE
   STN BB1
   CAL WPrint("?? false == ")
   CAL BPrintln(BB1)
   

   LD  FALSE
   STN  BB1
   CAL WPrint("?? true == ") 
   CAL BPrintln(BB1)
   

(*Fall som alltid skall ge fel*)
(*
	ST DINT#3
	STN TRUE
*)


(*DINT*)
(*====*)
CAL WPrintln("Tests of DINT constants and variables")
   (*DINT*)
   LD DINT#0
   ST DD1
   CAL WPrint("?? 0 == ")
   CAL DPrintln(DD1)
   

   (*DINT*)
   LD DINT#-1
   ST DD1
   CAL WPrint("?? -1 == ")
   CAL DPrintln(DD1)
   

   (*DINT*)
   LD DINT#1
   ST DD1
   CAL WPrint("?? 1 == ")
   CAL DPrintln(DD1)
   

   (*DINT*)
   LD DINT#23_344
   ST DD1
   CAL WPrint("?? 23344 == ")
   CAL DPrintln(DD1)
   

   (*DINT: test of binary constants*)
   LD DINT#2#1111_1111
   ST DD1
   CAL WPrint("?? 255 == ")
   CAL DPrintln(DD1)

   LD DINT#2#0000_1000
   ST DD1
   CAL WPrint("?? 8 == ")
   CAL DPrintln(DD1)

   LD DINT#2#0
   ST DD1
   CAL WPrint("?? 0 == ")
   CAL DPrintln(DD1)

   LD DINT#2#11
   ST DD1
   CAL WPrint("?? 3 == ")
   CAL DPrintln(DD1)

   (*DINT: test of octal constants*)
   LD DINT#8#377
   ST DD1
   CAL WPrint("?? 255 == ")
   CAL DPrintln(DD1)
   

   LD DINT#8#020
   ST DD1
   CAL WPrint("?? 16 == ")
   CAL DPrintln(DD1)
   

   LD DINT#8#0
   ST DD1
   CAL WPrint("?? 0 == ") 
   CAL DPrintln(DD1)
   

   (*DINT: test of hexadecimal constants*)
   LD DINT#16#FF
   ST DD1
   CAL WPrint("?? 255 == ")
   CAL DPrintln(DD1)
   

   LD DINT#16#A0
   ST DD1
   CAL WPrint("?? 160 == ")
   CAL DPrintln(DD1)
   

   LD DINT#16#0
   ST DD1
   CAL WPrint("?? 0 == ")
   CAL DPrintln(DD1)
   

   (*DINT: test of variables*)
   LD DINT#34
   ST DD1
   LD DD1
   ST DD2
   CAL WPrint("?? 34 == ") 
   CAL DPrintln(DD2)
   

(*REAL*)
(*====*)
CAL WPrintln("Tests of REAL constants and variables")

   LD REAL#0.0
   ST RR1
   CAL WPrint("?? 0 == ")
   CAL RPrintln(RR1)

   LD REAL#12.12
   ST RR1
   CAL WPrint("?? 12.12 == ")
   CAL RPrintln(RR1)
   

   LD REAL#-12.12
   ST RR1
   CAL WPrint("?? -12.12 == ")
   CAL RPrintln(RR1)
   

   LD REAL#0.1
   ST RR1
   CAL WPrint("?? 0.1 == ") 
   CAL RPrintln(RR1)
   

   LD REAL#10.123
   ST RR1
   CAL WPrint("?? 10.123 == ")
   CAL RPrintln(RR1)
   

   LD REAL#+12_123.21
   ST RR1
   CAL WPrint("?? 12123.21 == ")
   CAL RPrintln(RR1)
   

   LD REAL#-0.001298
   ST RR1
   CAL WPrint("?? -0.001298 == ")
   CAL RPrintln(RR1)
   

   LD REAL#-1.65E-10
   ST RR1
   CAL WPrint("?? -1.65E-10 == ") 
   CAL RPrintln(RR1)
   

   LD REAL#0.9E20
   ST RR1
   CAL WPrint("?? 0.9E20 == ") 
   CAL RPrintln(RR1)
   

   LD REAL#0.3276e+14
   ST RR1
   CAL WPrint("?? 0.3276e+14== ") 
   CAL RPrintln(RR1)
   

   LD REAL#0.3276e+14
   ST RR1
   LD RR1
   ST RR2
   CAL WPrint("?? 0.3276e+14== ") 
   CAL RPrintln(RR2)
   

(*WSTRING*)
(*=======*)
CAL WPrintln("Tests of WSTRING constants and variables")

   LD "Testing double_byte_character_string"
   ST WW1
   CAL WPrint("?? Testing double_byte_character_string == ")
   CAL WPrintln(WW1)
   

   LD "åäöÅÄÖ!@#$%^&*()_+|~1" (*234567890-=\`¤{}[]:;$"'<>,.?/"*)
   ST WW1
   CAL WPrintln("?? åäöÅÄÖ!@#$%^&*()_+|~1 == ")
   CAL WPrint("   ")
   CAL WPrintln(WW1)
   

(*XXX error, see further comments*)
   LD "234567890-=`{}[]:;$"'<>$\,.?/$"
			(* XXX ¤ does not work*)
			(* XXX $\ should print as \
			   XXX $" should print as "
			*)
   ST WW1
   CAL WPrintln("?? 234567890-=`{}[]:;$"'<>$\,.?/$ == ")
   CAL WPrint("   ")
   CAL WPrintln(WW1)
   

   LD "abcdefghijklmnopqrstuvxyzåäöABCDEFGHIJKLMNOPQRSTUVXYZÅÄÖ"
   ST WW1
   LD WW1
   ST WW2
   CAL WPrintln("?? abcdefghijklmnopqrstuvxyzåäöABCDEFGHIJKLMNOPQRSTUVXYZÅÄÖ == ")
   CAL WPrint("   ")
   CAL WPrintln(WW2)
   
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














