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
  public static void main(String[] args) throws Exception
  {
    //Read in the JobResult. I'm being a bit lazy here with
    //error handling. Let the JVM handle it.
    File dumpFile = new File(args[0]);
    FileInputStream fis = new FileInputStream(dumpFile);
    ObjectInputStream ois = new ObjectInputStream(fis);
    JobResult results = (JobResult)ois.readObject();

    if (args.length < 2)
      {
	outputResultSummary(results);
	return;
      }

    String op = args[1];

    if ("stats".equals(op))
      {
	doStats(results, args, 2);
      }
    
    if ("exception".equals(op))
      {
	Exception e = results.getException();
	if (e != null)
	  e.printStackTrace();
      }
  }

  private static void doStats(JobResult results, String[] args, int first_param) throws Exception
  {
    SafetyVerificationJobResult svresult = new SafetyVerificationJobResult(results);
    JobStats mainstats = svresult.getJobStats();

    if (first_param >= args.length)
      {
	outputStatsSummary(mainstats);
	return;
      }

    String op = args[first_param];

    if ("workers".equals(op))
      {
	doWorkers(mainstats, args, first_param + 1);
      }
  }

  private static void doWorkers(JobStats stats, String[] args, int first_param) throws Exception
  {
    JobStats[] workers = (JobStats[])stats.get("worker-stats");

    if (first_param >= args.length)
      {
	outputAllWorkerStats(workers);
	return;
      }
  }

  private static void outputAllWorkerStats(JobStats[] workers)
  {
    boolean first = true;
    for (JobStats s : workers)
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

  private static void outputStatsSummary(JobStats stats)
  {
    Map<String,Object> statsmap = stats.getStatsMap();

    for (Map.Entry<String,Object> ent : statsmap.entrySet())
      {
	System.out.format("%s: %s\n", ent.getKey(), ent.getValue());
      }
  }

  private static void outputResultSummary(JobResult results) throws Exception
  {
    //Just output the fields of the result dump by default
    Map<String,Object> resultmap = results.getAttributeMap();

    for (Map.Entry<String,Object> ent : resultmap.entrySet())
      {
	System.out.format("%s: %s\n", ent.getKey(), ent.getValue());
      }
  }
}