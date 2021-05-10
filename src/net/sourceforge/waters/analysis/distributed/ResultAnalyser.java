//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

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
