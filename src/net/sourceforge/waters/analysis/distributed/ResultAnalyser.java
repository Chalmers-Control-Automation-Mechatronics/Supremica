package net.sourceforge.waters.analysis.distributed;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Map;

import net.sourceforge.waters.analysis.distributed.application.JobResult;
import net.sourceforge.waters.analysis.distributed.safetyverifier.JobStats;
import net.sourceforge.waters.analysis.distributed.safetyverifier.SafetyVerificationJobResult;


/**
 * A simple application for extracting information from
 * serialised job results.
 *
 * @author Sam Douglas
 */
public class ResultAnalyser
{
  public static void main(final String[] args) throws Exception
  {
    //Read in the JobResult. I'm being a bit lazy here with
    //error handling. Let the JVM handle it.
    final File dumpFile = new File(args[0]);
    final FileInputStream fis = new FileInputStream(dumpFile);
    final ObjectInputStream ois = new ObjectInputStream(fis);
    final JobResult results = (JobResult) ois.readObject();
    ois.close();
    if (args.length < 2) {
      outputResultSummary(results);
      return;
    }
    final String op = args[1];
    if ("stats".equals(op)) {
      doStats(results, args, 2);
    }
    if ("exception".equals(op)) {
      final Exception e = results.getException();
      if (e != null) {
        e.printStackTrace();
      }
    }
  }

  private static void doStats(final JobResult results, final String[] args, final int first_param) throws Exception
  {
    final SafetyVerificationJobResult svresult = new SafetyVerificationJobResult(results);
    final JobStats mainstats = svresult.getJobStats();

    if (first_param >= args.length)
      {
	outputStatsSummary(mainstats);
	return;
      }

    final String op = args[first_param];

    if ("workers".equals(op))
      {
	doWorkers(mainstats, args, first_param + 1);
      }
  }

  private static void doWorkers(final JobStats stats, final String[] args, final int first_param) throws Exception
  {
    final JobStats[] workers = (JobStats[])stats.get("worker-stats");

    if (first_param >= args.length)
      {
	outputAllWorkerStats(workers);
	return;
      }
  }

  private static void outputAllWorkerStats(final JobStats[] workers)
  {
    boolean first = true;
    for (final JobStats s : workers)
      {
	if (s == null)
	  continue;

	//Separate entries with a newline. Don't
	//put a newline in for the first one though
	if (!first)
	  System.out.println();
	else
	  first = false;

	outputStatsSummary(s);
      }
  }

  private static void outputStatsSummary(final JobStats stats)
  {
    final Map<String,Object> statsmap = stats.getStatsMap();

    for (final Map.Entry<String,Object> ent : statsmap.entrySet())
      {
	System.out.format("%s: %s\n", ent.getKey(), ent.getValue());
      }
  }

  private static void outputResultSummary(final JobResult results) throws Exception
  {
    //Just output the fields of the result dump by default
    final Map<String,Object> resultmap = results.getAttributeMap();

    for (final Map.Entry<String,Object> ent : resultmap.entrySet())
      {
	System.out.format("%s: %s\n", ent.getKey(), ent.getValue());
      }
  }
}