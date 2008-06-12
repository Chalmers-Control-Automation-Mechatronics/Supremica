/**
 * 
 */
package org.supremica.automata.algorithms
/**
 * @author torda
 *
 */

import org.supremica.gui.SupremicaLoggerFactory
import org.supremica.automata.algorithms.AutomataBDDSynthesizer
import org.supremica.testcases.ExtCatMouse
import org.supremica.util.ActionTimer
import org.supremica.properties.Config
import org.supremica.util.BDD.Options
import static org.supremica.util.BDD.Options.*
import java.text.SimpleDateFormat

public class BenchmarkBddSynthesizer {
	public static final Map bddOptionsMap = [algo_family:REACH_ALGO_NAMES,
	         //                                reorder_algo:REORDER_ALGO_NAMES,//depends reorder_dynamic
	         //                                reorder_dynamic:[0,1],
	         //                                reorder_after_build:[0,1],//depends reorder_dynamic
	         //                                reorder_with_groups:[0,1],//depends reorder_dynamic
	         //                                reorder_within_group:[0,1],//depends reorder_dynamic
	         //                                disj_optimizer_algo:DISJ_OPTIMIZER_NAMES,//depends on algo_family is disj i.e. one of ALGO_DISJUNCTIVE,ALGO_SMOOTHED_KEEP,ALGO_SMOOTHED_PART,ALGO_SMOOTHED_PART2,ALGO_SMOOTHED_PATH,ALGO_SMOOTHED_MONO,ALGO_SMOOTHED_DELAYED_MONO,ALGO_SMOOTHED_DELAYED_STAR_MONO,ALGO_DISJUNCTIVE_WORKSET,ALGO_SMOOTHED_MONO_WORKSET,ALGO_DISJUNCTIVE_STEPSTONE
	         //                                transition_optimizer_algo:TRANSITION_OPTIMIZER_NAMES,//depends on algo_family = ALGO_PETRINET
	         //                                ordering_algorithm:ORDERING_ALGORITHM_NAMES,
	         //                                ordering_force_cost:FORCE_TYPE_NAMES,//depends on ordering_algorithm in AO_HEURISTIC_FORCE,AO_HEURISTIC_FORCE_WIN4
	         //                                as_heuristics:AS_HEURISTIC_NAMES,//only for language inclusion verification
	         //                                frontier_strategy:FRONTIER_STRATEGY_NAMES,
	         //                                es_heuristics:ES_HEURISTIC_NAMES,
	         //                                dssi_heuristics:DSSI_HEURISTIC_NAMES,
	         //                                ndas_heuristics:NDAS_HEURISTIC_NAMES,
	         //                                encoding_algorithm:ENCODING_NAMES,
	         //                                fill_statevars:[0,1],
	         //                                burst_mode:[0,1],
	         //                                interleaved_variables:[0,1]
	]

	public static void main(def args) {
		SupremicaLoggerFactory.initialiseSupremicaLoggerFactory();
		Config.VERBOSE_MODE.set true
		println new Date()
		println "BddBenchmark_${new SimpleDateFormat('yyyyMMddHHmmss').format(new Date())}.csv"
		def resultWriter = new FileWriter("BddBenchmark_${new SimpleDateFormat('yyyyMMddHHmmss').format(new Date())}.csv")
		
//		def configuration = [algo_family:0.toString(), reorder_algo:0.toString()]
		def allOptionCombinations = bddOptionsMap.values().collect{0..(it.length-1)}.combinations()
		def i = 0
		resultWriter.append(bddOptionsMap.keySet().join(',')).append(',time,maxNodeCount,maxNodeCountDescription\n')
		def result = allOptionCombinations.collect { optionValues ->
			println "$i out of ${allOptionCombinations.size()} runs have been completed"
			def options = [:]
			++i
			bddOptionsMap.keySet().eachWithIndex { optionName, j ->
				options[optionName] = optionValues[j].toString()
			}
			def processBuilder = new ProcessBuilder('java', '-cp', '../build;../lib/unjared',
			               'org.supremica.automata.algorithms.BddSynthesisRunner',
			               'new org.supremica.testcases.ExtCatMouse(4,4).automata')
			processBuilder.environment().clear()
			processBuilder.environment().putAll(options)
			def process = processBuilder.start();
			def outputFromProcess = new StringBuffer()
			process.consumeProcessOutputStream(outputFromProcess)
			process.consumeProcessErrorStream(System.err)
			process.waitForOrKill(1*60*1000)
			println outputFromProcess
			def matcher = (outputFromProcess.toString() =~ /.*ElapsedTime(\d*)EndElapsedTimeMaxNodeCount(\d*)EndMaxNodeCountMaxNodeCountDesc(.*)EndMaxNodeCountDesc/)
			def elapsedTime = matcher.size() == 0 ? -1 : matcher[0][1]
			def maxNodeCount = matcher.size() == 0 ? -1 : matcher[0][2]
			def maxNodeCountDesc = matcher.size() == 0 ? '-' : matcher[0][3]
			println elapsedTime
			def resultOfThisRun = options.collect{ bddOptionsMap[it.key][it.value.toInteger()] } + elapsedTime + maxNodeCount + maxNodeCountDesc
			resultWriter.append(resultOfThisRun.join(',')).append('\n');
			resultWriter.flush()
			resultOfThisRun
		}
		resultWriter.close()
		println result
		println 'done'
		println new Date()
		//		def processBuilder = new ProcessBuilder("java",
//				"-classpath", "../build;../lib/unjared",
//				"org.supremica.automata.algorithms.BddSynthesisRunner", "new org.supremica.testcases.ExtCatMouse(3,3).automata");
//		Map<String, String> env = processBuilder.environment();
//		env.clear()
//		env.putAll(configuration);
//		pb.directory(new File("../build"));
//		Process process = processBuilder.start();
//		process.consumeProcessOutputStream(System.out)
		//println process.in.text
		//println process.err.text
	//	process.waitForOrKill(10*1000)
		/*allPossibleConfigurations.each { configuration ->
			Options.algo_family = indexof(REACH_ALGO_NAMES, configuration[0])
			Options.reorder_algo = indexof(REORDER_ALGO_NAMES, configuration[1])
			bddSynthesizer = new AutomataBDDSynthesizer(catMouse, true, true)
			configuration.each { print it + ", " }
			stopWatch = new ActionTimer()
			stopWatch.start()
			benchmarkThread = Thread.start {
				try {
					int safeStates = bddSynthesizer.computeSafeStates();
				} catch (Throwable t) {
					t.printStackTrace()
				}
			}
			benchmarkThread.join(timeout)
			stopWatch.stop()
			if (benchmarkThread.isAlive()) {
				println "Exceeded timeout of ${timeout/1000} seconds"
				benchmarkThread.interrupt()
				bddSynthesizer.cleanup()
			} else {
				println stopWatch
			}
//			bddSynthesizer.bDDAutomata.kill()//deref(safeStates)
//			bddSynthesizer.cleanup()
		}*/
		
	}

	def indexof(array, element) {
		for (int i : 0..array.length) if (array[i] == element) return i
		-1
	}

	def mapFromLists(keys, values) {
		if (keys.size() != values.size()) throw IllegalArgumentException("Key and value arrays must be of same size")
		def map = [:]
		keys.eachWithIndex {key, i -> map[key] = values[i] }
		map
	}

}