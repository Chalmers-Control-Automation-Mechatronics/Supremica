package org.supremica.external.jgrafchart.toSMV.SFCDataStruct;
import java.util.*;
import org.jdom.*;
import org.jdom.input.*;

public class SFC
{
	Document sfcDoc;
	List allSteps = new LinkedList();
	List allTransitions = new LinkedList();
	List allVariables = new LinkedList();
	List allDigitalInputs = new LinkedList();
	List allLinks;
	List allParallelSplits = new LinkedList();
	List allParallelJoins = new LinkedList();
	List allSFCLinks = new LinkedList();
	Element rootElement ;
	boolean digitalInput = false;
	//List nestedSFCs =


	public SFC(Document sfcDoc )
	{
		this.sfcDoc = sfcDoc;
		rootElement = sfcDoc.getRootElement();
		allLinks = rootElement.getChildren("GCLink");
		/*added later*/
		makeLinkObjects();
		addAllSteps();
		addAllTransitions();
		addAllVariables();
		addAllParallelLinks();
		processParallelSplits();
		processParallelJoins();
		makeStepToTransReference();
		makeTransToStepReference();
		makeSplitJoinPairs();
		convertParallelLinks();
	}

	public List getAllSteps()
	{
		if(allSteps != null)
			return allSteps;
		else
			return null;
	}

	public List getAllTransitions()
	{
		if(allTransitions != null)
			return allTransitions;
		else
			return null;

	}

	public List getAllVariables()
	{
		if(allVariables != null)
			return allVariables;
		else
			return null;
	}

	public List getAllDigitalInputs()
	{
		return allDigitalInputs;
	}

	public List getAllParallelJoins()
	{
		return allParallelJoins;
	}

	public List getAllParallelSplits()
	{
		return allParallelSplits;
	}

	public boolean isThereDigitalInput()
	{
		return digitalInput;
	}

	private void convertParallelLinks()
	{
		if(allParallelSplits != null)
		{
			List doneTransitions = new LinkedList();
			Iterator itPar = allParallelSplits.iterator();
			while(itPar.hasNext())
			{
				SFCParallelSplit aSplit = (SFCParallelSplit) itPar.next();
				SFCParallelJoin aJoin = aSplit.getMatchingJoin();

				List variableLists ;
				String matchingTransId = aJoin.getNextTransitionId();
				SFCTransition matchingTrans = findTransition(matchingTransId);

				String prevTransId = aSplit.getPrevTransitionId();
				System.out.println("The PrevTransId for Split :"+aSplit+" is :"+prevTransId);
				SFCTransition aTrans = findTransition(prevTransId);
				if(!doneTransitions.contains(aTrans))
				{
					variableLists = makeSubSFC(aTrans,matchingTrans.getId());
					doneTransitions.add(aTrans);
					modifyOriginalSFC(aTrans,matchingTrans,variableLists);
				}
			}

		}
	}
	/* Purpose is to replace the parallel split join with single step which starts subSFCs and waits for
	 * them to complete.
	 **/
	private void modifyOriginalSFC(SFCTransition aTrans, SFCTransition matchingTrans, List listOfLists)
	{

		String replacingStepId  = "S_subSFC_"+aTrans;
		SFCStep replacingStep = new SFCStep(replacingStepId,"");
		Iterator itLists = listOfLists.iterator();

		List startVarList = (List) itLists.next();
		List endVarList = (List) itLists.next();
		Iterator itStartVarNames = startVarList.iterator();
		while(itStartVarNames.hasNext())
		{
			String startVarName = (String) itStartVarNames.next();
			SFCAction anNAction = new SFCAction("N",startVarName,null);
			replacingStep.addAction(anNAction);
		}

		List emptyList = new LinkedList();
		aTrans.setOutgoingSteps(emptyList);

		aTrans.addOutgoingStep(replacingStep);
		replacingStep.addIncomingTransition(aTrans);
		replacingStep.addOutgoingTransition(matchingTrans);
		matchingTrans.addIncomingStep(replacingStep);

		String conditionToAppend = "";
		Iterator itEndVarNames = endVarList.iterator();
		while(itEndVarNames.hasNext())
		{
			String endVarName = (String) itEndVarNames.next();
			conditionToAppend = conditionToAppend+" & "+endVarName;
		}

		matchingTrans.appendActionText(conditionToAppend);
		allSteps.add(replacingStep);
	}

	private List makeSubSFC(SFCTransition aTrans,String matchingTransId)
	{
		List startVariableNames = new LinkedList();
		List endVariableNames = new LinkedList();
		List listOfLists = new LinkedList();

		List nextSteps = aTrans.getOutgoingSteps();
		Iterator itNextSteps = nextSteps.iterator();
		while(itNextSteps.hasNext())
		{
			SFCStep aStep = (SFCStep) itNextSteps.next();
			String stepId = aStep.getId();
			SFCStep initialStep = new SFCStep(stepId+"_init","",true);

			String varName = "start_subSFC_"+stepId;
			SFCVariable aVariable = new SFCVariable(varName,"boolean","0","0");
			startVariableNames.add(varName);
			allVariables.add(aVariable);

			String transId = "T_init_"+stepId;
			SFCTransition initialTransition = new SFCTransition(transId,varName);

			initialStep.addOutgoingTransition(initialTransition);

			allSteps.add(initialStep);
			allTransitions.add(initialTransition);


			replacePrevTransition(aStep,aTrans,initialTransition);
			initialTransition.addOutgoingStep(aStep);
			initialTransition.addIncomingStep(initialStep);

			SFCStep lastStep = getLastStepBeforeJoin(aStep,matchingTransId);

			String endVarName = "end_subSFC_"+stepId;
			SFCVariable endVariable = new SFCVariable(endVarName,"boolean","0","0");
			endVariableNames.add(endVarName);
			allVariables.add(endVariable);

			SFCAction anAction = new SFCAction("S",endVarName,"1");
			lastStep.addAction(anAction);


			SFCTransition matchingTrans = findTransition(matchingTransId);

			SFCTransition lastTransition = new SFCTransition("T_binit_"+lastStep,"1");
			replaceNextTransition(lastStep,matchingTrans,lastTransition);
			lastTransition.addOutgoingStep(initialStep);
			lastTransition.addIncomingStep(lastStep);
			initialStep.addIncomingTransition(lastTransition);

			removePrevStep(matchingTrans,lastStep);
			//removeNextStep(aTrans,aStep);

		}
		listOfLists.add(startVariableNames);
		listOfLists.add(endVariableNames);
		return listOfLists;
	}

	private void replacePrevTransition(SFCStep aStep,SFCTransition oldTrans,SFCTransition newTrans)
	{
		List incomingTrans = aStep.getIncomingTransitions();
		boolean done = incomingTrans.remove(oldTrans);
		if(!done)
		{
			System.out.println("Incoming transition :"+oldTrans+" not found for step :"+aStep);
		}
		incomingTrans.add(newTrans);
		aStep.setIncomingTransitions(incomingTrans);

	}

	private void replaceNextTransition(SFCStep aStep,SFCTransition oldTrans,SFCTransition newTrans)
	{
		List outgoingTrans = aStep.getOutgoingTransitions();
		boolean done = outgoingTrans.remove(oldTrans);
		if(!done)
		{
			System.out.println("Outgoing transition :"+oldTrans+" not found for step :"+aStep);
		}
		outgoingTrans.add(newTrans);
		aStep.setOutgoingTransitions(outgoingTrans);
	}

	private void removePrevStep(SFCTransition aTrans,SFCStep oldStep)
	{
		List incomingSteps = aTrans.getIncomingSteps();
		boolean done = incomingSteps.remove(oldStep);
		if(!done)
		{
			System.out.println("Incoming Step :"+oldStep+" not found for Transition :"+aTrans);
		}
		aTrans.setIncomingSteps(incomingSteps);
	}

	private void removeNextStep(SFCTransition aTrans,SFCStep oldStep)
	{
		List outgoingSteps = aTrans.getOutgoingSteps();
		boolean done = outgoingSteps.remove(oldStep);
		if(!done)
		{
			System.out.println("Outgoin Step :"+oldStep+" not found for Transition :"+aTrans);
		}
		aTrans.setOutgoingSteps(outgoingSteps);
	}

	private SFCStep getLastStepBeforeJoin(SFCStep aStep,String lastTransId)
	{
		List transitions = aStep.getOutgoingTransitions();
		Iterator it = transitions.iterator();
		while(it.hasNext())
		{
			SFCTransition aTrans = (SFCTransition) it.next();
			if(aTrans.getId().equals(lastTransId))
			{
				return aStep;
			}
			else
			{
				List steps = aTrans.getOutgoingSteps();
				Iterator itSteps = steps.iterator();
				while(itSteps.hasNext())
				{
					SFCStep nextStep = (SFCStep) itSteps.next();
					return getLastStepBeforeJoin(nextStep,lastTransId);
				}
			}
		}
		return null;
	}

	private void addAllParallelLinks()
	{
		List parSplitElems = rootElement.getChildren("ParallelSplit");
		List parJoinElems = rootElement.getChildren("ParallelJoin");

		Iterator parSplitElemsIt = parSplitElems.iterator();
		Iterator parJoinElemsIt = parJoinElems.iterator();

		while(parSplitElemsIt.hasNext())
		{
			Element parSplitElem = (Element) parSplitElemsIt.next();
			Element parJoinElem = (Element) parJoinElemsIt.next();

			String parSplitId = parSplitElem.getAttributeValue("id");
			String parSplitPrevTransitionId = findParSplitPrevTransitionId(parSplitId);
			List parSplitNextStepIds = findParSplitNextStepIds(parSplitId);//outgoing

			SFCParallelSplit parSplit = new SFCParallelSplit(parSplitId,parSplitPrevTransitionId,parSplitNextStepIds);

			String parJoinId = parJoinElem.getAttributeValue("id");
			List parJoinPrevStepIds = findParJoinPrevStepIds(parJoinId);//incoming
			String parJoinNextTransitionId = findParJoinNextTransitionId(parJoinId);

			SFCParallelJoin parJoin = new SFCParallelJoin(parJoinId, parJoinPrevStepIds,parJoinNextTransitionId);

			allParallelSplits.add(parSplit);
			allParallelJoins.add(parJoin);
		}

	}

	private List findOutgoingObjectIds(String id)
	{
		List outgoingObjects = new LinkedList();
		Iterator it = allLinks.iterator();

		while (it.hasNext())
		{
				Element aLink = (Element) it.next();

				if (id.equals(aLink.getAttributeValue("fromObject")))
				{
					String toId = aLink.getAttributeValue("toObject");
					outgoingObjects.add(toId);

				}
		}
		 return outgoingObjects ;

	}

	private List findIncomingObjectIds(String id)
	{
		List incomingObjects = new LinkedList();
		Iterator it = allLinks.iterator();

		while (it.hasNext())
		{
				Element aLink = (Element) it.next();
				if (id.equals(aLink.getAttributeValue("toObject")))
				{
					String fromId = aLink.getAttributeValue("fromObject");
					incomingObjects.add(fromId);

				}
		}
		 return incomingObjects ;

	}

	private List findParSplitNextStepIds(String id)
	{
		List outgoingObjects = new LinkedList();
		Iterator it = allLinks.iterator();

		while (it.hasNext())
		{
				Element aLink = (Element) it.next();
				String splitId = aLink.getAttributeValue("fromObject");
				String idL = id+"L";
				String idR = id+"R";
				if (idL.equals(splitId) || idR.equals(splitId))
				{
					String toId = aLink.getAttributeValue("toObject");
					if(findStep(toId) != null)
					{
						outgoingObjects.add(toId);
					}
				}
		}
		 return outgoingObjects ;
	}

	private void processParallelSplits()
	{
		Iterator parSplitsIt = allParallelSplits.iterator();
		while(parSplitsIt.hasNext())
		{
			SFCParallelSplit aSplit = (SFCParallelSplit) parSplitsIt.next();
			List nextStepIds = getSplitToStepReference(aSplit.getId());
			//aSplit.setNextStepIds(nextStepIds);
		}
	}

	private void processParallelJoins()
	{
		Iterator parJoinsIt = allParallelJoins.iterator();
		while(parJoinsIt.hasNext())
		{
			SFCParallelJoin aJoin = (SFCParallelJoin) parJoinsIt.next();
			String joinId = aJoin.getId();
			List prevStepIds = getStepToJoinReference(joinId);

		}
		List superJoins = getSuperJoins();
		System.out.println("Number of SuperJoins :"+superJoins.size());
		Iterator superJoinsIt = superJoins.iterator();
		while(superJoinsIt.hasNext())
		{
			SFCParallelJoin aSuperJoin = (SFCParallelJoin) superJoinsIt.next();

			String nextTransitionId = aSuperJoin.getNextTransitionId();

			System.out.println("The next Transition Id for SuperJoin is :"+nextTransitionId);

			List subJoins = aSuperJoin.getSubJoins();

			System.out.println("The subJoin for SuperJoin is :"+subJoins);

			Iterator subJoinsIt = subJoins.iterator();
			while (subJoinsIt.hasNext())
			{
				SFCParallelJoin aSubJoin = (SFCParallelJoin) subJoinsIt.next();
				aSubJoin.setNextTransitionId(nextTransitionId);
			}
		}
	}


	private List getSuperJoins()
	{
		List superJoins = new LinkedList();
		Iterator parJoinsIt = allParallelJoins.iterator();
		while(parJoinsIt.hasNext())
		{
			SFCParallelJoin aJoin = (SFCParallelJoin) parJoinsIt.next();
			String joinId = aJoin.getId();

			List incomingObjectIdsL = findIncomingObjectIds(joinId+"L");
			List incomingObjectIdsR = findIncomingObjectIds(joinId+"R");

			List incomingObjectIds = new LinkedList();

			incomingObjectIds.addAll(incomingObjectIdsL);
			incomingObjectIds.addAll(incomingObjectIdsR);

			Iterator it = incomingObjectIds.iterator();
			while(it.hasNext())
			{
				String fromId = (String) it.next();
				if(isParallelJoin(fromId) && findParJoinNextTransitionId(joinId) != null)
				{
					if(!superJoins.contains(aJoin))
					{
						superJoins.add(aJoin);
					}
					SFCParallelJoin subJoin = findParallelJoin(fromId);
					aJoin.addSubJoins(subJoin);
				}
			}
		}
		return superJoins;

	}

	/*
	private void addSubJoins(String aSuperJoinId)
	{

	}
	*/

	private List getStepToJoinReference(String joinId)
	{
		List allPrevStepIds = new LinkedList();

		SFCParallelJoin aJoin = findParallelJoin(joinId);

		List incomingObjectIdsL = findIncomingObjectIds(joinId+"L");
		List incomingObjectIdsR = findIncomingObjectIds(joinId+"R");

		List incomingObjectIds = new LinkedList();

		incomingObjectIds.addAll(incomingObjectIdsL);
		incomingObjectIds.addAll(incomingObjectIdsR);

		Iterator it = incomingObjectIds.iterator();
		while(it.hasNext())
		{
			String fromId = (String) it.next();
			if(findStep(fromId) != null)
			{
				allPrevStepIds.add(fromId);
				if(!aJoin.getPrevStepIds().contains(fromId))
				{
					List prevStepIds = aJoin.getPrevStepIds();
					prevStepIds.add(fromId);
					aJoin.setPrevStepIds(prevStepIds);
				}
			}
			else if(isParallelJoin(fromId))
			{
				List upperJoinPrevStepIds = getStepToJoinReference(fromId);
				Iterator itPrevSteps = upperJoinPrevStepIds.iterator();
				while(itPrevSteps.hasNext())
				{
					String stepId = (String) itPrevSteps.next();
					List prevStepIds = aJoin.getPrevStepIds();
					if(!prevStepIds.contains(stepId))
					{
						prevStepIds.add(stepId);
					}
					aJoin.setPrevStepIds(prevStepIds);
				}
			}
			else
				System.out.println("Unknown Previous Object:"+fromId+" in getStepToJoinReference():"+isParallelJoin(fromId));
		}
		return allPrevStepIds;
	}


	private List getSplitToStepReference(String splitId)
	{
		SFCParallelSplit aSplit = findParallelSplit(splitId);

		List allNextStepIds = new LinkedList();

		List outgoingObjectsL = findOutgoingObjectIds(splitId+"L");
		List outgoingObjectsR = findOutgoingObjectIds(splitId+"R");
		//System.out.println("Number of Outgoing objects for Split :"+splitId+"L are"+outgoingObjects1.size());
		//System.out.println("Number of Outgoing objects for Split :"+splitId+"R are"+outgoingObjects2.size());

		List outgoingObjects = new LinkedList();

		outgoingObjects.addAll(outgoingObjectsL);
		outgoingObjects.addAll(outgoingObjectsR);

		//System.out.println("Total Number of Outgoing objects for Split :"+splitId+" are"+outgoingObjects.size());

		Iterator outgoingObjectsIt = outgoingObjects.iterator();
		while(outgoingObjectsIt.hasNext())
		{
			String toId = (String) outgoingObjectsIt.next();
			if(findStep(toId) != null)
			{
				allNextStepIds.add(toId);
				if(!aSplit.getNextStepIds().contains(toId))
				{
					List nextStepIds = aSplit.getNextStepIds();
					nextStepIds.add(toId);
					aSplit.setNextStepIds(nextStepIds);
				}
			}
			else if(isParallelSplit(toId))
			{
				//System.out.println("SubSplit :"+toId);
				List subSplitNextStepIds = getSplitToStepReference(toId);
				//System.out.println("Total Number of Outgoing objects for subSplit :"+toId+" are"+subSplitNextStepIds.size());
				//System.out.println("While Number of Steps in SuperSplit :"+aSplit.getNextStepIds().size());
				Iterator itSubSplitSteps = subSplitNextStepIds.iterator();
				while(itSubSplitSteps.hasNext())
				{
					String stepId = (String) itSubSplitSteps.next();
					//System.out.println("Step in SubSplit :"+stepId);

					List nextStepIds = aSplit.getNextStepIds();


					if(!nextStepIds.contains(stepId))
					{
						nextStepIds.add(stepId);
					}
					aSplit.setNextStepIds(nextStepIds);

				}
			}
			else
			{
				System.out.println("Unknown nextObject:"+toId+" in getSplitToStepReference():"+isParallelSplit(toId));
			}
		}
		return allNextStepIds;
	}

	private List findParJoinPrevStepIds(String id)
	{
		List incomingObjects = new LinkedList();
		Iterator it = allLinks.iterator();

		while (it.hasNext())
		{
				Element aLink = (Element) it.next();
				String joinId = aLink.getAttributeValue("toObject");
				String joinIdL = joinId+"L";
				String joinIdR = joinId+"R";
				if (id.equals(joinIdL) || id.equals(joinIdR))
				{
					String fromId = aLink.getAttributeValue("fromObject");
					incomingObjects.add(fromId);

				}
		}
		 return incomingObjects ;

	}


	private void addAllSteps()
	{

		List initialStep = rootElement.getChildren("GCInitialStep");
		Iterator it1 = initialStep.iterator();
		boolean initialSFCStep = true;
		while(it1.hasNext())
		{
			Element e1 = (Element)it1.next();
			String id1 = e1.getAttributeValue("id");
			String actionText1 = e1.getAttributeValue("actionText");
			SFCStep anInitialStep = new SFCStep(id1,actionText1,initialSFCStep);
			allSteps.add(anInitialStep);
		}

		List allRemainingSteps = rootElement.getChildren("GCStep");
		Iterator it2 = allRemainingSteps.iterator();
		while(it2.hasNext())
		{
			Element e2 = (Element) it2.next();
			String id2 = e2.getAttributeValue("id");
			String actionText2 = e2.getAttributeValue("actionText");
			SFCStep aStep = new SFCStep(id2,actionText2);
			allSteps.add(aStep);
		}

	}

	private void addAllTransitions()
	{
		List allTransElements = rootElement.getChildren("GCTransition");
		Iterator it = allTransElements.iterator();
		while (it.hasNext())
		{
				Element e = (Element) it.next();
				String id = e.getAttributeValue("id");
				String actionText = e.getAttributeValue("actionText");
				SFCTransition aTransition = new SFCTransition(id,actionText);
				allTransitions.add(aTransition);
		}

	}

	private void addAllVariables()
	{
		/* For Boolean Variables*/
		List allBoolVarElements = rootElement.getChildren("BooleanVariable");
		Iterator itBools = allBoolVarElements.iterator();
		while (itBools.hasNext())
		{
				Element e = (Element) itBools.next();
				String name = e.getAttributeValue("name");
				String type = "boolean";
				String value = e.getAttributeValue("value");
				String initialValue = e.getAttributeValue("initialValue");

				SFCVariable aBoolVariable = new SFCVariable(name,type,value,initialValue);
				allVariables.add(aBoolVariable);
		}

		List allDInVarElements = rootElement.getChildren("DigitalIn");

		if(allDInVarElements.size() > 0)
		{
			digitalInput = true;
		}

		Iterator itDIns = allDInVarElements.iterator();
		while (itDIns.hasNext())
		{
				Element e = (Element) itDIns.next();
				String name = e.getAttributeValue("name");
				String type = "boolean";
				String value = e.getAttributeValue("value");
				SFCVariable aDInVariable = new SFCVariable(name,type,value);
				allDigitalInputs.add(aDInVariable);
		}

		List allDOutVarElements = rootElement.getChildren("DigitalOut0");
		Iterator itDOuts = allDOutVarElements.iterator();
		while (itDOuts.hasNext())
		{
				Element e = (Element) itDOuts.next();
				String name = e.getAttributeValue("name");
				String type = "boolean";
				String value = e.getAttributeValue("value");
				SFCVariable aDOutVariable = new SFCVariable(name,type,value);
				allVariables.add(aDOutVariable);
		}
	}

	private void makeStepToTransReference()
	{
		Iterator stepsIt = allSteps.iterator();

		while (stepsIt.hasNext())
		{
			SFCStep aStep = (SFCStep) stepsIt.next();
			List outgoingTrans = new LinkedList();

			List nextObjectIds = findOutgoingObjectIds(aStep.getId());
			//System.out.println("Number of outgoing objects are " + nextObjectIds.size());

			Iterator it = nextObjectIds.iterator();

			while (it.hasNext())
			{

				String objectId = (String) it.next();
				//System.out.println("Step to Object id :"+objectId);
				SFCTransition nextTrans = findTransition(objectId);

				if(nextTrans != null)
				{
					//System.out.println("The Transitions Id is " + nextTrans.getId());
					outgoingTrans.add(nextTrans);
					nextTrans.addIncomingStep(aStep);
				}
				else if (nextTrans == null && isParallelJoin(objectId))
				{
					SFCParallelJoin parJoin = findParallelJoin(objectId);
					//System.out.println("A Parallel Join : "+parJoin.getId());
					String transId = parJoin.getNextTransitionId();
					nextTrans = findTransition(transId);
					outgoingTrans.add(nextTrans);
					nextTrans.addIncomingStep(aStep);
					//System.out.println("The Next Transition Id after Parallel Join is "+ nextTrans.getId());
				}
				else
				{
					//System.out.println("No reference from Step to Transition..Probably Last Step");

				}

			}

			if(outgoingTrans.size() >= 1)
			{
				aStep.setOutgoingTransitions(outgoingTrans);
				//System.out.println("The size is "+ outgoingTrans.size());
			}
		}
	}

	private void makeTransToStepReference()
	{
		Iterator transIt = allTransitions.iterator();

		while(transIt.hasNext())
		{
			SFCTransition aTransition = (SFCTransition) transIt.next();
			List outgoingSteps = new LinkedList();

			List nextObjectIds = findOutgoingObjectIds(aTransition.getId());
			//System.out.println("Number of outgoing objects are " + nextObjectIds.size());

			Iterator it = nextObjectIds.iterator();

			while(it.hasNext())
			{
				String objectId = (String) it.next();
				SFCStep nextStep = findStep(objectId);
				//System.out.println("Transition to Object id :"+objectId);
				if(nextStep != null)
				{
					//System.out.println("The Step Id is " + nextStep.getId());
					outgoingSteps.add(nextStep);
					nextStep.addIncomingTransition(aTransition);
				}
				else if(nextStep == null && isParallelSplit(objectId))
				{
					SFCParallelSplit parSplit = findParallelSplit(objectId);
					//System.out.println("A Parallel Split : "+objectId);
					List stepIds = parSplit.getNextStepIds();
					//System.out.println("Step Ids size : "+stepIds.size());
					Iterator stepIdsIt = stepIds.iterator();
					while(stepIdsIt.hasNext())
					{
						String stepId = (String) stepIdsIt.next();
						SFCStep aStep = findStep(stepId);
						outgoingSteps.add(aStep);
						aStep.addIncomingTransition(aTransition);
					}
				}
				else
				{
					//System.out.println("There must be some step or Split after Transition");
				}

			}
			aTransition.setOutgoingSteps(outgoingSteps);
			//System.out.println("The size is "+ outgoingSteps.size());
		}

	}

	/*
	 *Tester toi be deleted in end
	 *
	 */
	public void tester()
	{

		//System.out.println("After making Pairs");

		Iterator itParSplits = allParallelSplits.iterator();
		while(itParSplits.hasNext())
		{
			SFCParallelSplit aSplit = (SFCParallelSplit) itParSplits.next();
			System.out.println("Split Id is :"+aSplit.getId());
			System.out.println(" Sub Splits are :"+aSplit.getSubSplits().size());
			List subSplits = aSplit.getSubSplits();
			Iterator subSplitsIt = subSplits.iterator();
			while(subSplitsIt.hasNext())
			{
				SFCParallelSplit subSplit = (SFCParallelSplit) subSplitsIt.next();
				System.out.println("  SubSplit:"+subSplit.getId());
			}
			System.out.println(" Matching Join Id is :"+aSplit.getMatchingJoin().getId());
			System.out.println("Number of outgoing Steps are :"+aSplit.getNextStepIds().size());

		}

	}

	/*
	 *Tester toi be deleted in end
	 *
	 */
	public void tester1()
	{

		//System.out.println("After making Pairs");

		Iterator itParJoins = allParallelJoins.iterator();
		while(itParJoins.hasNext())
		{
			SFCParallelJoin aJoin = (SFCParallelJoin) itParJoins.next();
			System.out.println("Join Id is :"+aJoin.getId());

			List prevSteps = aJoin.getPrevStepIds();
			Iterator prevStepsIt = prevSteps.iterator();
			while(prevStepsIt.hasNext())
			{
				String aStepId = (String) prevStepsIt.next();
				System.out.println("  PrevStep:"+aStepId);
			}
			System.out.println("Number of Previous Steps are :"+prevSteps.size());

		}

		Iterator itParSplits = allParallelSplits.iterator();
		while(itParSplits.hasNext())
		{
			SFCParallelSplit aSplit = (SFCParallelSplit) itParSplits.next();
			System.out.println("Split Id is :"+aSplit.getId());

			List nextSteps = aSplit.getNextStepIds();
			Iterator nextStepsIt = nextSteps.iterator();
			while(nextStepsIt.hasNext())
			{
				String aaStepId = (String) nextStepsIt.next();
				System.out.println("  PrevStep:"+aaStepId);
			}
			System.out.println("Number of Next Steps are :"+nextSteps.size());

		}

	}



	private void makeSplitJoinPairs()
	{
		if(allParallelSplits.size() > 0)
		{
			Iterator itParSplits = allParallelSplits.iterator();
			while(itParSplits.hasNext())
			{
				SFCParallelSplit aSplit = (SFCParallelSplit) itParSplits.next();
				findMatchingJoin(aSplit);
			}
			//System.out.println("completed pairing");
		}
	}


	private void findMatchingJoin(SFCParallelSplit aSplit)
	{

		//System.out.println("Going to construct L and R for :"+aSplit.getId());

		String leftSplitArm = aSplit.getId()+"L";
		String rightSplitArm = aSplit.getId()+"R";

		//String leftBranch = findTargetInLink(leftSplitArm);
		//String rightBranch = findTargetInLink(rightSplitArm);

		//System.out.println("Left branch :"+leftBranch +"Right Branch :"+rightBranch);

		String splitOrJoin1 = findUntilSplitOrJoin(leftSplitArm);
		String splitOrJoin2 = findUntilSplitOrJoin(rightSplitArm);

		if(splitOrJoin1 != null)
		{
			//System.out.println("Came in Left Branch splitOrJoin :"+splitOrJoin1);
			if(isParallelSplit(splitOrJoin1))
			{
				SFCParallelSplit split = findParallelSplit(splitOrJoin1);
				//System.out.println("Parallel Split found :"+split.getId());
				aSplit.addSubSplits(split);
				//System.out.println("Calling find matching join Recursively for split :"+split);
				findMatchingJoin(split);

			}
			else if(isParallelJoin(splitOrJoin1))
			{
				if(aSplit.getMatchingJoin() == null)
				{
					//System.out.println("***************Setting Macthing Join :"+splitOrJoin1+" for Split :"+aSplit.getId());
					SFCParallelJoin join = findParallelJoin(splitOrJoin1);
					aSplit.setMatchingJoin(join);
				}
			}
			//System.out.println("isParallelJoin(splitOrJoin1) returns :"+isParallelJoin(splitOrJoin1)+" For :"+splitOrJoin1);
		}

		if(splitOrJoin2 != null)
		{
			//System.out.println("Came in Right Branch splitOrJoin :"+splitOrJoin2);

			if(isParallelSplit(splitOrJoin2))
			{
				SFCParallelSplit split = findParallelSplit(splitOrJoin2);
				aSplit.addSubSplits(split);
				//System.out.println("Calling find matching join Recursively for split :"+split);
				findMatchingJoin(split);

			}
			else if(isParallelJoin(splitOrJoin2))
			{
				//System.out.println("***************Setting Macthing Join :"+splitOrJoin2+" for Split :"+aSplit.getId());
				SFCParallelJoin join = findParallelJoin(splitOrJoin2);
				aSplit.setMatchingJoin(join);
			}
			//System.out.println("isParallelJoin(splitOrJoin2) returns :"+isParallelJoin(splitOrJoin2)+" For :"+splitOrJoin2);
		}
		while(aSplit.getMatchingJoin() == null)
		{
			List subSplits = aSplit.getSubSplits();
			Iterator subSplitsIt = subSplits.iterator();
			while(subSplitsIt.hasNext())
			{
				SFCParallelSplit subSplit = (SFCParallelSplit) subSplitsIt.next();
				SFCParallelJoin subSplitJoin = subSplit.getMatchingJoin();
				if(subSplitJoin != null)
				{
					String splitOrJoin3 = findUntilSplitOrJoin(subSplitJoin.getId());
					if(splitOrJoin3 != null)
					{
						//System.out.println("Came in Left Branch splitOrJoin :"+splitOrJoin3);
						if(isParallelSplit(splitOrJoin3))
						{
							SFCParallelSplit split = findParallelSplit(splitOrJoin3);
							//System.out.println("Parallel Split found :"+split.getId());
							aSplit.addSubSplits(split);
							//System.out.println("Calling find matching join Recursively for split :"+split);
							findMatchingJoin(split);
						}
						else if(isParallelJoin(splitOrJoin3))
						{
							//System.out.println("***************Setting Macthing Join :"+splitOrJoin3+" for Split :"+aSplit.getId());
							SFCParallelJoin join = findParallelJoin(splitOrJoin3);
							aSplit.setMatchingJoin(join);
						}
					//System.out.println("isParallelJoin(splitOrJoin1) returns :"+isParallelJoin(splitOrJoin3)+" For :"+splitOrJoin3);
					}

				}
				//else
					//System.out.println("SubSplit :"+subSplit.getId()+" has not matching Join");
			}
		}
		//System.out.println("中中中中中九nding for Split :"+aSplit.getId());
	}

	private String findUntilSplitOrJoin(String fromObjId)
	{
		String toObjId = findTargetInLink(fromObjId);

		//System.out.println("Got Target :"+toObjId+" for :"+fromObjId);


		if(toObjId != null)
		{
			if(isParallelSplit(toObjId))
			{
				return toObjId;
			}
			else if(isParallelJoin(toObjId))
			{
				return toObjId;
			}
			else
				return findUntilSplitOrJoin(toObjId);
		}
		else
		{
			//System.out.println("Target Object not found for Source :"+fromObjId);
			return null;
		}
	}


	private String findTargetInLink(String fromObjId)
	{
		Iterator itLinks = allSFCLinks.iterator();
		while (itLinks.hasNext())
		{
				SFCLink aLink = (SFCLink) itLinks.next();
				if(aLink.getFromObjectId().equals(fromObjId))
				{
					return aLink.getToObjectId();
				}
		}
		return null;
	}

	private void makeLinkObjects()
	{
		Iterator itLinks = allLinks.iterator();
		while (itLinks.hasNext())
		{
				Element aLink = (Element) itLinks.next();
				String fromObjectId = aLink.getAttributeValue("fromObject");
				String toObjectId = aLink.getAttributeValue("toObject");
				SFCLink sfcLink = new SFCLink(fromObjectId,toObjectId);
				allSFCLinks.add(sfcLink);
		}
	}

	private SFCTransition findTransition(String transitionId)
	{
		Iterator it = allTransitions.iterator();
		while(it.hasNext())
		{
			SFCTransition sfcTrans = (SFCTransition) it.next();

			if (sfcTrans.getId().equals(transitionId))
			{
				return sfcTrans;
			}
		}
		return null;
	}


	private SFCStep findStep(String stepId)
	{
		Iterator it = allSteps.iterator();
		while(it.hasNext())
		{
			SFCStep sfcStep = (SFCStep) it.next();

			if (sfcStep.getId().equals(stepId))
			{
				return sfcStep;
			}
		}
		return null;
	}

	private SFCParallelJoin findParallelJoin(String parJoinId)
	{
		Iterator it = allParallelJoins.iterator();
		while(it.hasNext())
		{
			SFCParallelJoin parJoin = (SFCParallelJoin) it.next();
			String lastChar = parJoinId.substring(parJoinId.length()-1,parJoinId.length());

			if(lastChar.equals("R") || lastChar.equals("L"))
			{
				parJoinId = parJoinId.substring(0,parJoinId.length()-1);
			}

			if(parJoin.getId().equals(parJoinId))
			{
				return parJoin;
			}
		}
		return null;
	}

	private SFCParallelSplit findParallelSplit(String parSplitId)
	{
		Iterator it = allParallelSplits.iterator();
		while(it.hasNext())
		{
			SFCParallelSplit parSplit = (SFCParallelSplit) it.next();
			//String parSplitIdTrunc = parSplitId.substring(0,parSplitId.length()-1);

			if(parSplit.getId().equals(parSplitId))
			{
				return parSplit;
			}
		}
		return null;
	}

	private boolean isParallelJoin(String id)
	{
		Iterator parJoinIt = allParallelJoins.iterator();
		//System.out.println("allParallelJoin size :"+ allParallelJoins.size());
		while (parJoinIt.hasNext())
		{
			SFCParallelJoin parJoin = (SFCParallelJoin) parJoinIt.next();
			String parJoinId  = parJoin.getId();
			//System.out.println("Par Join Id is :"+parJoinId);
			String parJoinIdL = parJoinId+"L";
			String parJoinIdR = parJoinId+"R";
			if(id.equals(parJoinIdL) || id.equals(parJoinIdR) || id.equals(parJoinId))
			{
				return true;
			}
		}
		return false;
	}


	private boolean isParallelSplit(String id)
	{
		Iterator parSplitIt = allParallelSplits.iterator();
		//System.out.println("ParallelSplit id :"+ id);
		//System.out.println("allParallelSplit size :"+ allParallelSplits.size());
		while (parSplitIt.hasNext())
		{
			SFCParallelSplit parSplit = (SFCParallelSplit) parSplitIt.next();
			if(id.equals(parSplit.getId()))
			{
				return true;
			}
		}
		return false;
	}

	private String findParSplitPrevTransitionId(String id)
	{
		List prevTransitionId = findIncomingObjectIds(id);
		String objectId =  (String) (prevTransitionId.iterator()).next();
		if(findTransition(objectId) == null)
		{
			return findParSplitPrevTransitionId(objectId.substring(0,objectId.length()-1));
		}
		else
		{
			return objectId;
		}
	}

	private String findParJoinNextTransitionId(String id)
	{
		List nextTransitionId = findOutgoingObjectIds(id);
		String aTransId =  (String)  (nextTransitionId.iterator()).next();

		SFCTransition aTransition = findTransition(aTransId);
		if(aTransition != null)
			return aTransition.getId();
		else
			return null;
	}
}
