/**
 * 
 */
package org.supremica.automata.algorithms

import org.supremica.util.BDD.Options
import org.supremica.util.BDD.NodeCountStatistics
import org.supremica.util.ActionTimer
import org.supremica.automata.algorithms.AutomataBDDSynthesizer

/**
 * @author torda
 *
 */
System.getenv().each{
	Options."$it.key" = it.value.toInteger()
}
Options.collectNodeCountStatistics = true
model = new GroovyShell().evaluate(args[0])
stopWatch = new ActionTimer()
bddSynthesizer = new AutomataBDDSynthesizer(model, true, true)
println 'aaa'
stopWatch.start()
int safeStates = bddSynthesizer.computeSafeStates();
stopWatch.stop()
println 'bbb'
print "ElapsedTime" + stopWatch.elapsedTime() + "EndElapsedTime"
print "MaxNodeCount" + NodeCountStatistics.instance.maxNodeCount().count + "EndMaxNodeCount"
print "MaxNodeCountDesc" + NodeCountStatistics.instance.maxNodeCount().description + "EndMaxNodeCountDesc"
System.out.flush()
