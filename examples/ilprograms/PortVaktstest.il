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
        LD  tonIN
        ST  odt.tonIN
        LD  tonPT
        ST  odt.tonPT
        CAL odt
        LD  odt.tonET
        ST  tonET
        LD  odt.tonQ
        ST  tonQ
END_FUNCTION_BLOCK

PROGRAM PortVaktstest
	VAR
		(*Insignaler*)
		KulaPortvakt 	AT %IX0  : BOOL;
		MatlyftNere  	AT %IX1  : BOOL;
		KulaMatlyft  	AT %IX2  : BOOL;
		MatlyftUppe	AT %IX3  : BOOL;
		KulaMatstation	AT %IX4  : BOOL;
		StorKula	AT %IX5  : BOOL;
		LitenKula	AT %IX6  : BOOL;
		HissNere	AT %IX7  : BOOL;
		KulaHiss	AT %IX10 : BOOL;
		HissVan1	AT %IX11 : BOOL;
		KulaVan1	AT %IX12 : BOOL;		
		PlockaVan1	AT %IX13 : BOOL;
		HissVan2	AT %IX14 : BOOL;
		KulaVan2	AT %IX15 : BOOL;
		PlockaVan2	AT %IX16 : BOOL;
		ArmHemma	AT %IX17 : BOOL;
		ArmVan1		AT %IX20 : BOOL;
		ArmVan2		AT %IX21 : BOOL;
		KulaFast	AT %IX22 : BOOL;
		Autostart   AT %IX23 : BOOL;
		ManuellStart AT %IX24 : BOOL;
		NodStopp	AT %IX25 : BOOL;
		LarmKvittering AT %IX26 : BOOL;
		(*Utsignaler*)
		InPortvakt   	AT %QX0  : BOOL;
		UrPortvakt   	AT %QX1  : BOOL;
		UppMatlyft      AT %QX2  : BOOL;
		UrMatning	AT %QX3  : BOOL;
		Mat		AT %QX4  : BOOL;
		UppHissVan1	AT %QX5  : BOOL;
		UppHissVan2	AT %QX6  : BOOL;
		UtVan1		AT %QX7  : BOOL;
		LyftVan1	AT %QX10 : BOOL;
		UtVan2		AT %QX11 : BOOL;
		LyftVan2	AT %QX12 : BOOL;
		UppArmVan1	AT %QX13 : BOOL;
		UppArmVan2	AT %QX14 : BOOL;
		VridArmHoger AT %QX15 : BOOL;
		Sug			AT %QX16 : BOOL;
	END_VAR
	VAR
		IsStorKula  : BOOL;
		Matn1, Matn2,Matn3,Matn4,Matn5 : BOOL := FALSE;
		ArmSteg1, ArmSteg2, AutomatiskStart : BOOL := FALSE;
		StartAllowed : BOOL := TRUE;

(*		MatningKlar, UrMatningKlar : BOOL := FALSE;
		PaborjaMatning : BOOL := FALSE;
		MatningGjord : BOOL;
*)
		delay2, delay4, delay5, delay6,delay66: TON;
		delay8, delay88,delay10,delay20, delay21 : TON;
		tmpb   : BOOL;
		tmpd   : DINT;		
		Init  : BOOL := TRUE;

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
(*init*)
	LD Init
	JMPCN end_init
		LD DINT#300 
		ST delay8.tonPT
		ST delay88.tonPT
		ST delay21.tonPT
		LD DINT#300
		ST delay2.tonPT (*Stäng InPortvakt*)
		LD DINT#1000
		ST delay4.tonPT
		ST delay5.tonPT
		ST delay6.tonPT
		ST delay66.tonPT (*PaborjaMatn-fördröjning*)
		ST delay20.tonPT
		ST delay21.tonPT
		LD DINT#2000
		ST delay10.tonPT (*Hiss*)
		LD FALSE
		ST Init
end_init:

(* autostart, special för simulator, funkar ej för riktig kulbana*)
(*	if !(fsfsdfsd || (KulaPortvakt || UrPortvakt))
		Släpp in kula
	fi
*)
		LDN AutomatiskStart
		OR(KulaPortvakt
		   OR UrPortvakt
		   ORN StartAllowed
		)
		JMPC END1A
			
			LD TRUE
			ST InPortvakt
END1A :

(* if (Autostart)
		sätt AutomatiskStart
*)
		LDN Autostart
		JMPCN ENDauto
			LD TRUE
			ST AutomatiskStart
ENDauto:

(* /autostart *)

(* manuell start*)
(*	if !(ManuellStart || (KulaPortvakt || UrPortvakt))
		Släpp in kula
	fi
*)
		LD ManuellStart
		OR(KulaPortvakt
		   OR UrPortvakt
		   ORN StartAllowed
		)
		JMPC END1
			
			LD TRUE
			ST InPortvakt
END1 :





(*
	if (KulaPortvakt && InPortvakt)
		Stäng InPortvakt
	fi
*)

		LD KulaPortvakt
		AND InPortvakt
		JMPCN END2
			LD  TRUE
			ST  delay2.tonIN
			CAL delay2
			LD  delay2.tonQ
			STN InPortvakt
			JMPCN e2
				LD FALSE
				ST delay2.tonIN
				CAL delay2
			e2:

END2 :

(*
	if (KulaPortvakt && !InPortvakt && MätlyftNere && !KulaMätlyft)
		Släpp ut kula ur portvakt
	fi
*)
		LD KulaPortvakt
		ANDN InPortvakt
		AND MatlyftNere
		ANDN KulaMatlyft
		JMPCN END3
			LD TRUE
			ST UrPortvakt
			LD FALSE
			ST StartAllowed
END3 :

(*
	if (!KulaPortvakt && UrPortvakt)
		Stäng UrPortvakt
	fi
*)

		LD UrPortvakt
		ANDN KulaPortvakt
		JMPCN END4
			LD TRUE
			ST  delay4.tonIN
			CAL delay4
			LD  delay4.tonQ		
			STN UrPortvakt
			STN delay4.tonIN
			CAL delay4
END4 :
(*
	bonus
	if (KulaMätlyft && MätlyftNere && !UrMatning &&!Mat && !UppMatlyft)
		Skicka upp mätlyften
	fi
*)

	LD KulaMatlyft
	AND MatlyftNere
	ANDN UrMatning (*försäkring*)
	ANDN Mat
	ANDN UppMatlyft
	JMPCN END5
		LD TRUE
		ST StartAllowed
		ST delay5.tonIN
		CAL delay5
		LD delay5.tonQ
		ST UppMatlyft
		ST Matn1
		STN delay5.tonIN
		CAL delay5
END5:

(*	if (MätlyftUppe && KulaMatstation && HissNere && Matn1 && !UrMatning)
		Sätt Mät
	fi
*)
	LD  MatlyftUppe
	AND KulaMatstation
	AND HissNere
	AND Matn1
	ANDN UrMatning
	JMPCN END6
		(*Vänta tills bollen stabiliserats innan Mat sätts*)
		LD TRUE   
		ST delay6.tonIN
		CAL delay6
		LD delay6.tonQ
		ST Mat
		(*Vänta tills Mat gått ner innan Matning påbörjas*)
		ST delay66.tonIN
		CAL delay66
		LD delay66.tonQ
		ST Matn2
		STN Matn1
END6:

(*	if (MätlyftUppe && KulaMatstation && HissNere && Mat && Matn2
		&& !KulaHiss)
		Läs av mätare
		Stäng av Mät
		Sätt mät klar
	fi
*)
	LD  MatlyftUppe
	AND KulaMatstation
	AND HissNere
	AND Mat
	AND Matn2
	ANDN KulaHiss
	JMPCN END7
		LD StorKula
		OR LitenKula
		JMPCN noMat
		
		LD StorKula
		ST IsStorKula
	noMat:	LD FALSE
		ST Mat (*släpp upp mätaren*)
		ST delay6.tonIN (*återställ delayen*)
		ST delay66.tonIN (*återställ delayen*)			
		CAL delay6
		CAL delay66
		ST Matn2
		LD TRUE
		ST Matn3
END7:

(* 	if (Matn3 && HissNere && !KulaHiss)
		Vänta lite
		Sätt UrMätning
		Vänta lite till
		Matn3 = F
		Matn4 = T
*)
	LD Matn3
	AND HissNere
	ANDN KulaHiss
	JMPCN END8
		LD TRUE
		ST delay8.tonIN
		CAL delay8
		LD delay8.tonQ
		ST UrMatning
		ST delay88.tonIN
		CAL delay88
		LD delay88.tonQ
		ST Matn4
		STN Matn3
END8:
(*	if (MatlyftUppe && !KulaMatstation &&
		Matn4)
		dra tillbaka UrMätlyft
		skicka ner matlyften
*)

	LD MatlyftUppe
	ANDN KulaMatstation
	AND Matn4
	JMPCN END9
		LD FALSE
		ST UrMatning
		ST UppMatlyft
		ST delay8.tonIN
		ST delay88.tonIN
		CAL delay8
		CAL delay88
		ST Matn4
END9:

(*	if (HissNere && KulaHiss && !UtVan1 && !UtVan2)
		Kör upp hiss

*)
	LD HissNere
	AND KulaHiss
	ANDN UtVan1
	ANDN UtVan2
	JMPCN END10
		LD TRUE
		ST delay10.tonIN
		CAL delay10
		LD delay10.tonQ
		JMPCN END10 (*Vänta innan hissen körs upp*)
		ST UppHissVan1
		LD IsStorKula
		JMPCN litenK	
			LD TRUE
			ST UppHissVan2

	litenK:
		LD FALSE
		ST delay10.tonIN
		CAL delay10  
END10:

(*	if (IsStorKula && HissVan2 && !KulaVan2)
		Skicka ner hiss
*)
	LD IsStorKula
	AND HissVan2
	ANDN KulaVan2
	JMPCN END11
		LD FALSE
		ST UppHissVan1
		ST UppHissVan2
		ST UtVan2
		ST delay10.tonIN (*nollställ upphissningsfördröjningen*)
		CAL delay10 
END11:
(*	if (!IsStorKula && HissVan1 && !KulaVan1)
		Skicka ner hiss
*)
	LD HissVan1
	ANDN IsStorKula
	ANDN KulaVan1
	JMPCN END12
		LD FALSE
		ST UppHissVan1
		ST UppHissVan2
		ST UtVan1
		ST delay10.tonIN (*nollställ upphissningsfördröjningen*)
		CAL delay10 
END12:

(* =============================
	Härifrån och tom END21 är ej testat på riktiga kulbanan.
*)


(* if (IsStorKula && HissVan2 && KulaVan2)
	UtVan1
*)
	LD  IsStorKula
	AND HissVan2
	AND KulaVan2
	JMPCN END13
		LD TRUE
		ST UtVan2		
END13:	
(* if (!IsStorKula && HissVan1 && KulaVan1)
	UtVan1
*)
	LD  KulaVan1
	AND HissVan1
	ANDN IsStorKula
	JMPCN END14
		LD TRUE
		ST UtVan1		
END14:

(* if (ArmHemma && !Sug && !KulaFast)
		Skicka upp arm om kula finns där uppe
		if (PlockaVan1)
			Arm1
			UppArm1
			VridArmHöger
		fi
		if (PlockaVan2)
			Arm2
			UppArm1
			UppArm2
			VridArmHöger
		fi
	fi
*)

	LD   ArmHemma
	ANDN Sug
	ANDN KulaFast
	JMPCN END15
		LD PlockaVan1
		JMPCN ingenvan1
			LD TRUE
			ST ArmSteg1
			ST UppArmVan1
			ST VridArmHoger
		ingenvan1:

		LD PlockaVan2
		JMPCN ingenvan2
			LD TRUE
			ST ArmSteg2
			ST UppArmVan1
			ST UppArmVan2
			ST VridArmHoger
		ingenvan2:
		CAL delay21(tonIN := FALSE)
END15:


(*	if (ArmVan1 && PlockaVan1 && !KulaFast)
		Sug
		LyftVan1
*)
		LD ArmVan1
		AND PlockaVan1
		ANDN KulaFast
		JMPCN END16
			LD TRUE
			ST Sug
			ST LyftVan1
END16:

(*	if (ArmVan2 && PlockaVan2 && !KulaFast)
		Sug
		LyftVan2
*)
		LD ArmVan2
		AND PlockaVan2
		ANDN KulaFast
		JMPCN END17
			LD TRUE
			ST Sug
			ST LyftVan2
END17:

(*	if	(ArmVan1 && KulaFast)
		LD FALSE
		ST LyftVan1
		ST delay20.tonIN
*)
		LD ArmVan1
		AND KulaFast
		JMPCN END18
			LD FALSE
			ST LyftVan1
			ST delay20.tonIN
			CAL delay20
END18:
(*	if	(ArmVan2 && KulaFast)
		LD FALSE
		ST LyftVan2
		ST delay
*)
		LD ArmVan2
		AND KulaFast
		JMPCN END19
			LD FALSE
			ST LyftVan2
			ST delay20.tonIN
			CAL delay20
END19:

(*	if ((ArmVan1 || ArmVan2) && KulaFast && !LyftVan1 && !LyftVan2)
		delay
		ta ner kulan
*)
		LD ArmVan1
		OR ArmVan2
		AND KulaFast
		ANDN LyftVan1
		ANDN LyftVan2
		JMPCN END20
			CAL delay20(tonIN := TRUE)
			JMPCN e20
				LD FALSE
				ST VridArmHoger
				ST UppArmVan1
				ST UppArmVan2
				ST delay21.tonIN
				CAL delay21
		e20:
END20:

(*	if (KulaFast && ArmHemma)
		Släpp kula
*)
		LD KulaFast
		AND ArmHemma
		JMPCN END21
			CAL delay21(tonIN := TRUE)
			LD  delay21.tonQ
			STN Sug
END21:
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

