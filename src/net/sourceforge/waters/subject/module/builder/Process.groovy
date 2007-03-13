package net.sourceforge.waters.subject.module.builder;

class Process extends Scope {
	static final pattern = /(?i)process/
	static final defaultAttr = null
	static final parentAttr = 'process' 
	Process() {
		name = new IdentifierExpression('Process')
	}
	List sequences = []
	List functionBlockInstances  = []
	def addToModule(ModuleBuilder mb) {
		functionBlockInstances.each { it.addToModule(mb) }
		sequences.each { it.addToModule(mb, Converter.PROCESS_SCAN_CYCLE_EVENT_NAME) }
	}
}