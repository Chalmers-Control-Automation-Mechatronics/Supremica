package net.sourceforge.waters.subject.module.builder;

class Process extends Named {
	static final pattern = /(?i)process/
	static final defaultAttr = null
	static final parentAttr = 'process' 
	Process() {
		name = new IdentifierExpression('Process')
	}
	List statements  = []
	def addToModule(ModuleBuilder mb) {
		statements.each { it.addToModule(mb) }
		//sequences.each { it.addToModule(mb, Converter.PROCESS_SCAN_CYCLE_EVENT_NAME) }
	}
}