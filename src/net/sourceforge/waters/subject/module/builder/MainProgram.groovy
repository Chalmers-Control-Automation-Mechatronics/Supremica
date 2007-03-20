package net.sourceforge.waters.subject.module.builder;

class MainProgram {
	boolean deferred = true
	static final pattern = /(?i)mainProgram/
	static final defaultAttr = null
	static final parentAttr = 'mainProgram'
	List statements = []
/*	def addToModule(ModuleBuilder mb, Scope parent, List programState) {
		statements.each { programState = it.addToModule(mb, parent, programState) }
		programState
		//sequences.each { it.addToModule(mb, Converter.SCAN_CYCLE_EVENT_NAME)	}
	}*/
	List execute(Scope parent) {
		statements.inject([]){executedStatements, statement -> executedStatements + statement.execute(parent)}
	}
}