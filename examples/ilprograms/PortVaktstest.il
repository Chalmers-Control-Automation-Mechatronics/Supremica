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
		Start 		 AT %IX24 : BOOL;
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
	END_VAR
	VAR
		IsStorKula  : BOOL;
		Matn1, Matn2,Matn3,Matn4,Matn5 : BOOL := FALSE;


(*		MatningKlar, UrMatningKlar : BOOL := FALSE;
		PaborjaMatning : BOOL := FALSE;
		MatningGjord : BOOL;
*)
		delay2, delay4, delay5, delay6,delay66: TON;
		delay8, delay88,delay10 : TON;
		tmpb   : BOOL;
		tmpd   : DINT;		
		Init  : BOOL := TRUE;
	END_VAR
(*init*)
	LD Init
	JMPCN end_init
		CAL PRINTLN("Initialising......!!")
		LD DINT#300 
		ST delay8.tonPT
		ST delay88.tonPT
		LD DINT#300
		ST delay2.tonPT (*Stäng InPortvakt*)
		LD DINT#1000
		ST delay4.tonPT
		ST delay5.tonPT
		ST delay6.tonPT
		ST delay66.tonPT (*PaborjaMatn-fördröjning*)
		LD DINT#2000
		ST delay10.tonPT (*Hiss*)
		LD FALSE
		ST Init
end_init:

(*start*)
(*	if !(Start || (KulaPortvakt || UrPortvakt))
		Släpp in kula
	fi
*)
		LD Start
		OR(KulaPortvakt
		   OR UrPortvakt
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
			STN tmpb
			CAL PRINTLN(tmpb)
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
		CAL PRINT("Läser av=", StorKula)
		CAL PRINTLN("")
		LD StorKula
		ST IsStorKula
		(*lägg till lite bättre logik*)
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
		ST delay10.tonIN (*nollställ upphissningsfördröjningen*)
		CAL delay10 
END12:

CAL PRINTLN("IsStorKula= ",IsStorKula)
CAL PRINTLN("")

END_PROGRAM

