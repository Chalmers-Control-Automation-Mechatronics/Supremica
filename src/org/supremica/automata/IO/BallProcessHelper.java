/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Haradsgatan 26A
 * 431 42 Molndal
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.automata.IO;

import java.io.*;

public class BallProcessHelper
	extends SattLineHelper
{

	private static BallProcessHelper theHelper;

	protected BallProcessHelper()
	{
	}

	public static IEC61131Helper getInstance()
	{
		if (theHelper == null)
		{
			theHelper = new BallProcessHelper();
		}
		return theHelper;
	}

	/**
	 * Helper functions for producing SattLine Code for the Ball Process
	 *
	 * @returns Nothing
	 * @param pw does this and that
	 * @see org.supremica.gui.Supremica
	 */

	public void printBeginProgram(PrintWriter pw, String fileName)
	{
		printFileHeader(pw, fileName);
		printBasePictureInvocation(pw);
		printTypeDefinitions(pw);
		printBeginLocalVariables(pw);
		printLocalVariables(pw);
		printEndLocalVariables(pw);
		printSubModules(pw);
		printBeginModuleDefinition(pw);
		printOutputSignalHandlers(pw);
	}

	public void printEndProgram(PrintWriter pw)
	{
		printEndModuleDefinition(pw);
	}

	public void printBasePictureInvocation(PrintWriter pw)
	{
		pw.println("BasePicture Invocation");
		pw.println("   ( 0.0 , 0.0 , 0.0 , 1.0 , 1.0 ");
		pw.println("    IgnoreMaxModule ) : MODULEDEFINITION DateCode_ 451751498 ( GroupConn = ");
		pw.println("ScanGroup ) ");
	}

	public void printTypeDefinitions(PrintWriter pw)
	{
		pw.println("TYPEDEFINITIONS");
		pw.println("   IO_intype = RECORD DateCode_ 161179876");
		pw.println("      KulaPortvakt, M�tlyftNere, KulaM�tlyft, M�tlyftUppe, KulaM�tstation, ");
		pw.println("      StorKula, LitenKula, HissNere, KulaHiss, HissV�n1, KulaV�n1, PlockaV�n1, ");
		pw.println("      HissV�n2, KulaV�n2, PlockaV�n2, ArmHemma, ArmV�n1, ArmV�n2, KulaFast, ");
		pw.println("      AutoStart, ManuellStart, N�dStopp, LarmKvittering: BooleanSignal ;");
		pw.println("   ENDDEF");
		pw.println("    (*IO_intype*);");
		pw.println("   ");
		pw.println("   IO_outtype = RECORD DateCode_ 221276465");
		pw.println("      UrPortvakt, InPortvakt, UppM�tlyft, UrM�tning, M�t, L�ngaHissen, ");
		pw.println("      KortaHissen, UtV�n1, LyftV�n1, UtV�n2, LyftV�n2, KortArm, L�ngArm, ");
		pw.println("      VridArmH�ger, Sug, T�ndLampa: BooleanSignal ;");
		pw.println("   ENDDEF");
		pw.println("    (*IO_outtype*);");
		pw.println("   ");
		pw.println("   Insignal = RECORD DateCode_ 184588196");
		pw.println("      KulaPortvakt, M�tlyftNere, KulaM�tlyft, M�tlyftUppe, KulaM�tstation, ");
		pw.println("      StorKula, LitenKula, HissNere, KulaHiss, HissV�n1, KulaV�n1, PlockaV�n1, ");
		pw.println("      HissV�n2, KulaV�n2, PlockaV�n2, ArmHemma, ArmV�n1, ArmV�n2, KulaFast: ");
		pw.println("      boolean ;");
		pw.println("      AutoStart, ManuellStart: boolean  := True;");
		pw.println("      N�dStopp, LarmKvittering: boolean ;");
		pw.println("   ENDDEF");
		pw.println("    (*Insignal*);");
		pw.println("   ");
		pw.println("   Utsignal = RECORD DateCode_ 161103228");
		pw.println("      InPortvakt, UrPortvakt, UppM�tlyft, UrM�tning, M�t, UppHissV�n1, ");
		pw.println("      UppHissV�n2, UtV�n1, LyftV�n1, UtV�n2, LyftV�n2, UppArmV�n1, UppArmV�n2, ");
		pw.println("      VridArmH�ger, Sug, T�ndLampa: boolean ;");
		pw.println("   ENDDEF");
		pw.println("    (*Utsignal*);");
	}

	public void printLocalVariables(PrintWriter pw)
	{
		pw.println("   IP: Insignal ;");
		pw.println("   OP, SET_OP, RESET_OP: Utsignal ;");
		pw.println("   SysName: string  := \"remsys1111\";");
		pw.println("   ProgStationData: ProgStationData ;");
		pw.println("   ScanGroup: GroupData ;");
		pw.println("   IPCpanel1 \"Board to rack input connection, Panel 1\", OPCpanel1 ");
		pw.println("   \"Board to rack output connection, Panel 1\", IPCpanel2 ");
		pw.println("   \"Board to rack input connection, Panel 2\", OPCpanel2 ");
		pw.println("   \"Board to rack output connection, Panel 2\", IPCpanel3 ");
		pw.println("   \"Board to rack input connection, Panel 3\", OPCpanel3 ");
		pw.println("   \"Board to rack output connection, Panel 3\", IPCpanel4 ");
		pw.println("   \"Board to rack input connection, Panel 4\", OPCpanel4 ");
		pw.println("   \"Board to rack output connection, Panel 4\": BoardConnection ;");
	}

	public void printEndLocalVariables(PrintWriter pw)
	{
		// Empty for the Ball Process
	}

	public void printSubModules(PrintWriter pw)
	{
		pw.println("SUBMODULES");
		pw.println("   ProgStationControl Invocation");
		pw.println("      ( -0.42 , 0.92 , 0.0 , 0.04 , 0.04 ");
		pw.println("       ) : ProgStationControl (");
		pw.println("   ReqCycleTimeInit => 10, ");
		pw.println("   ReqCycleTimeFastInit => 10);");
		pw.println("   ");
		pw.println("   ScanGroupControl Invocation");
		pw.println("      ( -0.34 , 0.92 , 0.0 , 0.04 , 0.04 ");
		pw.println("       ) : ScanGroupControl (");
		pw.println("   Name => \"ScanGroupControl\", ");
		pw.println("   Group => ScanGroup, ");
		pw.println("   System => SysName, ");
		pw.println("   ReqCycleTimeInit => 10);");
		pw.println("   ");
		pw.println("   LabIO Invocation");
		pw.println("      ( -0.72 , 0.28 , -2.27243E-07 , 0.1 , 0.44 ");
		pw.println("       ) : MODULEDEFINITION DateCode_ 260496933");
		pw.println("   MODULEPARAMETERS");
		pw.println("      SysName \"Name of system to run in\": string ;");
		pw.println("      IP \"Input record\": Insignal ;");
		pw.println("      OP \"Output record\": Utsignal ;");
		pw.println("      IPConnector \"Input board connection\", OPConnector ");
		pw.println("      \"Output board connection\": BoardConnection ;");
		pw.println("   LOCALVARIABLES");
		pw.println("      ErrorSumFromRack \"ErrorRack OR ErrorSum\", X1");
		pw.println("      \"Elevator in outer unsafe zone?\", X2 \"Elevator in inner unsafe zone?\", ");
		pw.println("      EnableControl: boolean ;");
		pw.println("      T�ndLampa, Sug, VridArmH�ger, L�ngArm, KortArm, LyftV�n2, UtV�n2, ");
		pw.println("      LyftV�n1, UtV�n1 \"Output\", L�ngaHissen \"Output\", KortaHissen \"Output\", ");
		pw.println("      M�t \"Output\", UrM�tning \"Output\", UppM�tlyft \"Output\", UrPortvakt ");
		pw.println("      \"Output\", InPortvakt \"Output\", LarmKvittering \"Input\", N�dStopp \"Input\", ");
		pw.println("      ManuellStart \"Input\", AutoStart \"Input\", KulaFast \"Input\", ArmV�n2 ");
		pw.println("      \"Input\", ArmV�n1 \"Input\", ArmHemma \"Input\", PlockaV�n2 \"Input\", KulaV�n2 ");
		pw.println("      \"Input\", HissV�n2 \"Input\", PlockaV�n1 \"Input\", KulaV�n1 \"Input\", HissV�n1 ");
		pw.println("      \"Input\", KulaHiss \"Input\", HissNere \"Input\", LitenKula \"Input\", StorKula ");
		pw.println("      \"Input\", KulaM�tstation \"Input\", M�tlyftUppe \"Input\", KulaM�tlyft \"Input\"");
		pw.println("      , M�tlyftNere \"Input\", KulaPortvakt \"Input\": BooleanSignal ;");
		pw.println("      ScanGroup \"Scan group\": GroupData ;");
		pw.println("   SUBMODULES");
		pw.println("      Inputs");
		pw.println("      (* It's possible to replace this board with one of the following boards:");
		pw.println("         ID32, ID16, OD32, OD16, IODP");
		pw.println("         IA8, OA4, OAH4, IPA4");
		pw.println("         Empty *)");
		pw.println("       Invocation");
		pw.println("         ( 2.56186 , -0.46448 , 0.0 , 0.91825 , 0.145 ");
		pw.println("          ) : ID32 (");
		pw.println("      Name => \"IDPG\", ");
		pw.println("      IX0 => KulaPortvakt, ");
		pw.println("      IX0TagName => \"KulaPortvakt\", ");
		pw.println("      IX1 => M�tlyftNere, ");
		pw.println("      IX1TagName => \"M�tlyftNere\", ");
		pw.println("      IX2 => KulaM�tlyft, ");
		pw.println("      IX2TagName => \"KulaM�tlyft\", ");
		pw.println("      IX3 => M�tlyftUppe, ");
		pw.println("      IX3TagName => \"M�tlyftUppe\", ");
		pw.println("      IX4 => KulaM�tstation, ");
		pw.println("      IX4TagName => \"KulaM�tstation\", ");
		pw.println("      IX5 => StorKula, ");
		pw.println("      IX5TagName => \"StorKula\", ");
		pw.println("      IX6 => LitenKula, ");
		pw.println("      IX6TagName => \"LitenKula\", ");
		pw.println("      IX7 => HissNere, ");
		pw.println("      IX7TagName => \"HissNere\", ");
		pw.println("      IX10 => KulaHiss, ");
		pw.println("      IX10TagName => \"KulaHiss\", ");
		pw.println("      IX11 => HissV�n1, ");
		pw.println("      IX11TagName => \"HissV�n1\", ");
		pw.println("      IX12 => KulaV�n1, ");
		pw.println("      IX12TagName => \"KulaV�n1\", ");
		pw.println("      IX13 => PlockaV�n1, ");
		pw.println("      IX13TagName => \"PlockaV�n1\", ");
		pw.println("      IX14 => HissV�n2, ");
		pw.println("      IX14TagName => \"HissV�n2\", ");
		pw.println("      IX15 => KulaV�n2, ");
		pw.println("      IX15TagName => \"KulaV�n2\", ");
		pw.println("      IX16 => PlockaV�n2, ");
		pw.println("      IX16TagName => \"PlockaV�n2\", ");
		pw.println("      IX17 => ArmHemma, ");
		pw.println("      IX17TagName => \"ArmHemma\", ");
		pw.println("      IX20 => ArmV�n1, ");
		pw.println("      IX20TagName => \"ArmV�n1\", ");
		pw.println("      IX21 => ArmV�n2, ");
		pw.println("      IX21TagName => \"ArmV�n2\", ");
		pw.println("      IX22 => KulaFast, ");
		pw.println("      IX22TagName => \"KulaFast\", ");
		pw.println("      IX23 => AutoStart, ");
		pw.println("      IX23TagName => \"AutoStart\", ");
		pw.println("      IX24 => ManuellStart, ");
		pw.println("      IX24TagName => \"ManuellStart\", ");
		pw.println("      IX25 => N�dStopp, ");
		pw.println("      IX25TagName => \"N�dStopp\", ");
		pw.println("      IX26 => LarmKvittering, ");
		pw.println("      IX26TagName => \"LarmKvittering\", ");
		pw.println("      SysName => SysName, ");
		pw.println("      EnableControl => True, ");
		pw.println("      EnableParChanges => True, ");
		pw.println("      ErrorSum => True, ");
		pw.println("      Connector => IPconnector);");
		pw.println("      ");
		pw.println("      Outputs");
		pw.println("      (* It's possible to replace this board with one of the following boards:");
		pw.println("         ID32, ID16, OD32, OD16, IODP");
		pw.println("         IA8, OA4, OAH4, IPA4");
		pw.println("         Empty *)");
		pw.println("       Invocation");
		pw.println("         ( 3.47446 , -0.46448 , 0.0 , 0.91825 , 0.145 ");
		pw.println("          ) : OD32 (");
		pw.println("      Name => \"ODPG_8\", ");
		pw.println("      QX0 => InPortvakt, ");
		pw.println("      QX0TagName => \"InPortvakt\", ");
		pw.println("      QX1 => UrPortvakt, ");
		pw.println("      QX1TagName => \"UrPortvakt\", ");
		pw.println("      QX2 => UppM�tlyft, ");
		pw.println("      QX2TagName => \"UppM�tlyft\", ");
		pw.println("      QX3 => UrM�tning, ");
		pw.println("      QX3TagName => \"UrM�tning\", ");
		pw.println("      QX4 => M�t, ");
		pw.println("      QX4TagName => \"M�t\", ");
		pw.println("      QX5 => KortaHissen, ");
		pw.println("      QX5TagName => \"KortaHissen\", ");
		pw.println("      QX6 => L�ngaHissen, ");
		pw.println("      QX6TagName => \"L�ngaHissen\", ");
		pw.println("      QX7 => UtV�n1, ");
		pw.println("      QX7TagName => \"UtV�n1\", ");
		pw.println("      QX10 => LyftV�n1, ");
		pw.println("      QX10TagName => \"LyftV�n1\", ");
		pw.println("      QX11 => UtV�n2, ");
		pw.println("      QX11TagName => \"UtV�n2\", ");
		pw.println("      QX12 => LyftV�n2, ");
		pw.println("      QX12TagName => \"LyftV�n2\", ");
		pw.println("      QX13 => KortArm, ");
		pw.println("      QX13TagName => \"KortArm\", ");
		pw.println("      QX14 => L�ngArm, ");
		pw.println("      QX14TagName => \"L�ngArm\", ");
		pw.println("      QX15 => VridArmH�ger, ");
		pw.println("      QX15TagName => \"VridArmH�ger\", ");
		pw.println("      QX16 => Sug, ");
		pw.println("      QX16TagName => \"Sug\", ");
		pw.println("      QX17 => T�ndLampa, ");
		pw.println("      QX17TagName => \"T�ndLampa\", ");
		pw.println("      SysName => SysName, ");
		pw.println("      EnableControl => True, ");
		pw.println("      EnableParChanges => True, ");
		pw.println("      ErrorSum => True, ");
		pw.println("      Connector => OPconnector);");
		pw.println("      ");
		pw.println("   ");
		pw.println("   ModuleDef");
		pw.println("   ClippingBounds = ( 2.215 , -0.84 ) ( 4.705 , 1.18 )");
		pw.println("   Grid = 0.005");
		pw.println("   ");
		pw.println("   ModuleCode");
		pw.println("   EQUATIONBLOCK Interface COORD 2.565, -0.675 OBJSIZE 1.83, 0.205 :");
		pw.println("      (* ======================= Insignaler/svar ============================= ");
		pw.println("      *);");
		pw.println("      IP.KulaPortvakt = KulaPortvakt.Value;");
		pw.println("      IP.M�tlyftNere = M�tlyftNere.Value;");
		pw.println("      IP.KulaM�tlyft = KulaM�tlyft.Value;");
		pw.println("      IP.M�tlyftUppe = M�tlyftUppe.Value;");
		pw.println("      IP.KulaM�tstation = KulaM�tstation.Value;");
		pw.println("      IP.StorKula = StorKula.Value;");
		pw.println("      IP.LitenKula = LitenKula.Value;");
		pw.println("      IP.HissNere = HissNere.Value;");
		pw.println("      IP.KulaHiss = KulaHiss.Value;");
		pw.println("      IP.HissV�n1 = HissV�n1.Value;");
		pw.println("      IP.KulaV�n1 = KulaV�n1.Value;");
		pw.println("      IP.PlockaV�n1 = PlockaV�n1.Value;");
		pw.println("      IP.HissV�n2 = HissV�n2.Value;");
		pw.println("      IP.KulaV�n2 = KulaV�n2.Value;");
		pw.println("      IP.PlockaV�n2 = PlockaV�n2.Value;");
		pw.println("      IP.ArmHemma = ArmHemma.Value;");
		pw.println("      IP.ArmV�n1 = ArmV�n1.Value;");
		pw.println("      IP.ArmV�n2 = ArmV�n2.Value;");
		pw.println("      IP.KulaFast = KulaFast.Value;");
		pw.println("      IP.AutoStart = AutoStart.Value;");
		pw.println("      IP.ManuellStart = ManuellStart.Value;");
		pw.println("      IP.N�dStopp = N�dStopp.Value;");
		pw.println("      IP.LarmKvittering = LarmKvittering.Value;");
		pw.println("      (* ================ Utsignaler/kommandon ================================ ");
		pw.println("      *);");
		pw.println("      InPortvakt.Value = OP.InPortvakt;");
		pw.println("      UrPortvakt.Value = OP.UrPortvakt;");
		pw.println("      Sug.Value = OP.Sug;");
		pw.println("      T�ndLampa.Value = OP.T�ndLampa;");
		pw.println("      (* ---------------- M�tningen --------------------------------------------- ");
		pw.println("      *);");
		pw.println("      (* H�r ville jag programmera \"fel\", s� att om UrM�tning �r ute eller M�t �r ");
		pw.println("      *);");
		pw.println("      (* nere kommer M�tlyften att \"oscillera\" nere vid M�tlyftNere *);");
		pw.println("      (* UppM�tlyft.Value = OP.UppM�tlyft AND (M�tlyftNere.Value OR  *);");
		pw.println("      (*                          NOT UrM�tning.Value); *);");
		pw.println("      (* Men kulan ramlar av d�, s� .... *);");
		pw.println("      (* M�tlyften f�r g� upp endast om UrM�tning �r inne och M�t inte �r nere ");
		pw.println("      *);");
		pw.println("      (* Fast M�t f�r vara nere och UrM�tning f�r vara ute n�r M�tlyften �r uppe ");
		pw.println("      *);");
		pw.println("      UppM�tlyft.Value = OP.UppM�tlyft AND ( NOT (M�t.Value OR UrM�tning.Value) ");
		pw.println("         OR M�tlyftUppe.Value);");
		pw.println("      (* M�t f�r g� ner n�r som helst ?? *);");
		pw.println("      M�t.Value = OP.M�t;");
		pw.println("      (* UrM�tning f�r inte g� ut om M�t �r nere *);");
		pw.println("      UrM�tning.Value = OP.UrM�tning AND  NOT M�t.Value;");
		pw.println("      (* ----------------- Hissen ---------------------------------------------- ");
		pw.println("      *);");
		pw.println("      (* Vi har tv� \"s�kerhetsomr�den\". X1 som t�cker in b�de v�n1 och v�n2, och ");
		pw.println("      *);");
		pw.println("      (* X2 som bara s�krar v�n2. Det �r bara n�r Hissen befinner sig i n�t av ");
		pw.println("      *);");
		pw.println("      (* s�kerhetsomr�dena som vi beh�ver kolla UtV�n1/UtV�n2 *);");
		pw.println("      X1 = (X1 OR HissV�n1.Value AND L�ngaHissen.Value AND KortaHissen.Value) ");
		pw.println("         AND  NOT (HissV�n1.Value AND  NOT L�ngaHissen.Value AND  NOT ");
		pw.println("         KortaHissen.Value);");
		pw.println("      X2 = (X2 OR HissV�n2.Value) AND  NOT HissNere.Value;");
		pw.println("      (* S�tt L�ngaHissen om vi antingen vill till v�n1 eller v�n2 *);");
		pw.println("      (* F�r g� till v�n1 endast om UtV�n1 �r inne, men om vi �r p� v�n1 *);");
		pw.println("      (* och inte ska vidare s� f�r UtV�n1 g� ut *);");
		pw.println("      (* F�r g� till v�n2 endast om UtV�n1 och UtV�n2 �r inne, men om vi �r p� ");
		pw.println("      *);");
		pw.println("      (* v�n2 s� f�r UtV�n2 g� ut *);");
		pw.println("      L�ngaHissen.Value = OP.UppHissV�n1 AND ( NOT UtV�n1.Value OR HissV�n1.");
		pw.println("         Value AND  NOT KortaHissen.Value) AND  NOT OP.UppHissV�n2 OR OP.");
		pw.println("         UppHissV�n2 AND ( NOT (UtV�n1.Value OR UtV�n2.Value) OR HissV�n2.Value ");
		pw.println("         AND OP.UtV�n2) AND  NOT OP.UppHissV�n1;");
		pw.println("      KortaHissen.Value = OP.UppHissV�n2 AND  NOT (HissV�n1.Value AND OP.UtV�n1");
		pw.println("         );");
		pw.println("      UtV�n1.Value = OP.UtV�n1 AND  NOT X1;");
		pw.println("      (* Detta fungerar inte: NOT X1 OR HissV�n1.Value AND  NOT Kortahissen.Value ");
		pw.println("      *);");
		pw.println("      UtV�n2.Value = OP.UtV�n2;");
		pw.println("      (* ----------------- Armen ---------------------------------- *);");
		pw.println("      LyftV�n1.Value = OP.LyftV�n1;");
		pw.println("      LyftV�n2.Value = OP.LyftV�n2;");
		pw.println("      (* S�tt KortArm om vi antingen vill till v�n1 eller v�n2 *);");
		pw.println("      (* Armen ska dessutom h�llas p� plats s� l�nge v�ningen �r lyft *);");
		pw.println("      (* F�r allts� inte f�rs�ka g� ner (och tillbaka) med lyft v�ning *);");
		pw.println("      KortArm.Value = OP.UppArmV�n1 OR OP.UppArmV�n2 OR LyftV�n1.Value AND ");
		pw.println("         ArmV�n1.Value OR ArmV�n1.Value AND OP.VridArmH�ger OR ArmV�n2.Value ");
		pw.println("         AND OP.VridArmH�ger;");
		pw.println("      L�ngArm.Value = OP.UppArmV�n2 OR LyftV�n2.Value AND ArmV�n2.Value OR ");
		pw.println("         ArmV�n2.Value AND OP.VridArmH�ger;");
		pw.println("      (* Vrid armen endast om v�ningen vi ska till ej �r lyft *);");
		pw.println("      (* H�ll ocks� armen vriden om vi �r d�r och v�ningen �r lyft *);");
		pw.println("      VridArmH�ger.Value = OP.VridArmH�ger AND (KortArm.Value AND  NOT LyftV�n1");
		pw.println("         .Value OR OP.UppArmV�n2 AND  NOT LyftV�n2.Value) OR ArmV�n1.Value AND ");
		pw.println("         LyftV�n1.Value OR ArmV�n2.Value AND LyftV�n2.Value;");
		pw.println("   ");
		pw.println("   ENDDEF (*LabIO*) (");
		pw.println("   SysName => SysName, ");
		pw.println("   IP => IP, ");
		pw.println("   OP => OP, ");
		pw.println("   IPConnector => IPCpanel1, ");
		pw.println("   OPConnector => OPCpanel1);");
		pw.println("   ");
		pw.println("   Rack Invocation");
		pw.println("      ( -0.46 , 0.78 , 0.000148473 , 0.08 , 0.07141 ");
		pw.println("       ) : Rack (");
		pw.println("   SysName => SysName, ");
		pw.println("   ExternalRack => False, ");
		pw.println("   Slot0 => IPCpanel1, ");
		pw.println("   Slot40 => OPCpanel1, ");
		pw.println("   Slot100 => IPCpanel2, ");
		pw.println("   Slot140 => OPCpanel2, ");
		pw.println("   Slot200 => IPCpanel3, ");
		pw.println("   Slot240 => OPCpanel3, ");
		pw.println("   Slot300 => IPCpanel4, ");
		pw.println("   Slot340 => OPCpanel4);");

	}

	public void printOutputSignalHandlers(PrintWriter pw)
	{
		int stepCounter = 0;
		int transitionCounter = 0;
		pw.println("SEQUENCE InPortvaktHandler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.InPortvakt" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.InPortvakt" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.InPortvakt" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.InPortvakt" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE UrPortvaktHandler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.UrPortvakt" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.UrPortvakt" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.UrPortvakt" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.UrPortvakt" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE UppM�tningHandler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.UppM�tlyft" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.UppM�tlyft" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.UppM�tlyft" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.UppM�tlyft" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE UrM�tningHandler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.UrM�tning" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.UrM�tning" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.UrM�tning" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.UrM�tning" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE M�tHandler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.M�t" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.M�t" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.M�t" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.M�t" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE UppHissV�n1Handler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.UppHissV�n1" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.UppHissV�n1" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.UppHissV�n1" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.UppHissV�n1" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE UppHissV�n2Handler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.UppHissV�n2" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.UppHissV�n2" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.UppHissV�n2" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.UppHissV�n2" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE UtV�n1Handler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.UtV�n1" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.UtV�n1" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.UtV�n1" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.UtV�n1" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE UtV�n2Handler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.UtV�n2" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.UtV�n2" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.UtV�n2" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.UtV�n2" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE LyftV�n1Handler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.LyftV�n1" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.LyftV�n1" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.LyftV�n1" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.LyftV�n1" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE LyftV�n2Handler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.LyftV�n2" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.LyftV�n2" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.LyftV�n2" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.LyftV�n2" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE UppArmV�n1Handler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.UppArmV�n1" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.UppArmV�n1" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.UppArmV�n1" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.UppArmV�n1" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE UppArmV�n2Handler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.UppArmV�n2" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.UppArmV�n2" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.UppArmV�n2" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.UppArmV�n2" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE VridArmH�gerHandler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.VridArmH�ger" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.VridArmH�ger" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.VridArmH�ger" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.VridArmH�ger" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE SugHandler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.Sug" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.Sug" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.Sug" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.Sug" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
		pw.println("SEQUENCE T�ndLampaHandler" + getCoord());
		pw.println("SEQINITSTEP OPH" + stepCounter++);
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "SET_OP.T�ndLampa" + getTransitionConditionSuffix());
		pw.println("SEQSTEP OPH" + stepCounter++);
		pw.println(getActionP1Prefix() + "OP.T�ndLampa" + getAssignmentOperator() + "True;" + getActionP1Suffix());
		pw.println(getActionP0Prefix() + "OP.T�ndLampa" + getAssignmentOperator() + "False;" + getActionP0Suffix());
		pw.println("SEQTRANSITION OPTr"  + transitionCounter++ + getTransitionConditionPrefix() + "RESET_OP.T�ndLampa" + getTransitionConditionSuffix());
		pw.println("ENDSEQUENCE\n\n");
	}

	public void printGFile(PrintWriter pw, String filename)
	{
		pw.println("\" Syntax version 2.19, date: 2004-01-27-20:33:34.140 N \" ");
	}

	public void printLFile(PrintWriter pw, String filename)
	{
		pw.println("pioracklib");
		pw.println("nucleuslib");
	}

	public void printPFile(PrintWriter pw, String filename)
	{
		pw.println("DistributionData");
		pw.println(" ( Version \"Distributiondata version 1.0\" )");
		pw.println("SourceCodeSystems");
		pw.println(" (  )");
		pw.println("ExecutingSystems");
		pw.println(" ( Controller");
		pw.println("    ( Name \"remsys1111\"");
		pw.println("      Download LowMemReconfig ) )");
	}
}
