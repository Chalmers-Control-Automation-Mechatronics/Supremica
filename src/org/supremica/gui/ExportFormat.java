package org.supremica.gui;


// This is good, enum, not integer constants!
//** MF Not good! Should be objects of the various classes...
public enum ExportFormat
{
	UNKNOWN, XML, DOT, DSX, HTML, XML_DEBUG,
	DOT_DEBUG, DSX_DEBUG, SP_DEBUG,
	HTML_DEBUG, FSM, FSM_DEBUG,
	PCG, PCG_DEBUG,    // ARASH: process communication graphs
	SSPC,    // ARASH: Sanchez SSPC tool
    STS, STS_DEBUG //Sajed: State Tree Structure introduced by Chuan Ma and Wonham
}