package org.supremica.external.iec61131.builder;

public enum Speed {
	SLOW   { int getValue() { return 0; }},
	MEDIUM { int getValue() { return 1; }},
	FAST   { int getValue() { return 2; }}
}
