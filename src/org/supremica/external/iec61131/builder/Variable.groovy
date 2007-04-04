package org.supremica.external.iec61131.builder

import net.sourceforge.waters.subject.module.builder.ModuleBuilder

class Input extends ExternalVariable {
	static final pattern = /(?i)input/
	static final defaultAttr = 'name'
	static final parentAttr = 'inputs'
}
class Output extends ExternalVariable {
	static final pattern = /(?i)output/
	static final defaultAttr = 'name'
	static final parentAttr = 'outputs'
}
abstract class ExternalVariable extends Variable {
}
class InternalVariable extends Variable {
	static final pattern = /(?i)variable/
	static final defaultAttr = 'name'
	static final parentAttr = 'variables'
	def addProcessEvents(ModuleBuilder mb, Scope parent) {}
}

abstract class Variable {
	boolean value //defaults to false
	Boolean markedValue // defaults to null
	IdentifierExpression name
	List getControllableVariables(Scope parent) {
		[name.fullyQualified(parent)]
	}
}
