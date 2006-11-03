package org.supremica.gui;


// This is good, enum, not integer constants!
public enum ExportFormat
{
	UNKNOWN, XML, DOT, DSX, SP, HTML, XML_DEBUG,
	DOT_DEBUG, DSX_DEBUG, SP_DEBUG,
	HTML_DEBUG, FSM, FSM_DEBUG,
	PCG, PCG_DEBUG,    // ARASH: process communication graphs
	SSPC    // ARASH: Sanchez SSPC tool
}