// # -*- indent-tabs-mode: nil c-basic-offset: 2 -*-
// ###########################################################################
// # PROJECT:
// # PACKAGE: org.supremica.automata.algorithms.IISCT.SMTSolver
// # CLASS: IISynthesizer
// ###########################################################################
// # $Id$
// ###########################################################################

package org.supremica.automata.algorithms.IISCT;

import com.microsoft.z3.BoolExpr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.sourceforge.waters.analysis.efa.simple.SimpleEFAHelper;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFALabelEncoding;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAStateEncoding;
import net.sourceforge.waters.analysis.efa.simple.SimpleEFAVariable;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;

import org.supremica.automata.algorithms.IISCT.SMTSolver.SolverException;
import org.supremica.automata.algorithms.IISCT.SMTSolver.Z3Solver;

import gnu.trove.map.hash.TLongObjectHashMap;

/**
 *
 * @author Mohammad Reza Shoaei
 */
public class IISynthesizer {

	public IISynthesizer(final List<SimpleEFAVariable> variableContext,
			final ListBufferTransitionRelation transitionRelation,
			final SimpleEFALabelEncoding labelEncoding, final SimpleEFAStateEncoding stateEncoding,
			final HashMap<Integer, SimpleExpressionProxy> labelToSpec) throws AnalysisException {
		mVarCtx = variableContext;
		mTR = transitionRelation;
		mTR.reconfigure(ListBufferTransitionRelation.CONFIG_SUCCESSORS);
		mIter = mTR.createSuccessorsReadOnlyIterator();
		mLbEnc = labelEncoding;
		mStEnc = stateEncoding;
		mLbToSpec = labelToSpec;
		mTree = new Tree();
		zSupExpr = new TLongObjectHashMap<>(25, 0.8f, -1);
		// zLbToTranExpr = new TIntObjectHashMap<BoolExpr>(50, 0.6f, -1);
		zSolver = new Z3Solver(variableContext);
		ZFALSE = zSolver.mkFalse();
		ZTRUE = zSolver.mkTrue();
	}

	public boolean synthesis() throws AnalysisException {
		// (TODO) Analyzing cycles, in particular selfloops, using FixedPoint
		initTree();
		TreeNode leaf;
		while (mTree.hasLeaf()) {
			leaf = mTree.popLeaf();
			System.err.println(leaf.id);
			// if (leaf.id == 4){
			// System.err.print("Here");
			// }
			if (leaf.isForbidden()) {
				// Check for bad states and do supervision
				if (!pathBlocking(leaf)) {
					// There does not exists any supervisor for the given system
					return false;
				}
			} else {
				// Strengthening leaf w.r.t. to its parent
				strengthen(leaf);
				// Check if the leaf can be closed
				close(leaf);
				// If leaf cannot be closed then unwind it
				unwind(leaf);
			}
		}
		printSupervisionGuards();
		return true;
	}

	private void strengthenByBioParent(final TreeNode v) throws AnalysisException {

		final TreeNode pv = getFirstBiologicalParent(v);

		// (TODO) Special case: selfloops
		final BoolExpr zvR = zSolver.mkAnd(v.data.R);
		final BoolExpr zpvR = zSolver.mkAnd(pv.data.R);
		final BoolExpr zPath = getZPath(v, pv);
		final BoolExpr zRP = zSolver.mkAnd(zPath, zpvR);

		final List<BoolExpr> C = pv.data.R.stream().sequential()
				.map(r -> zSolver.substitute(r, pv.data.zid, v.data.zid))
				.filter(r -> !zSolver.entails(zvR, r) && zSolver.entails(zRP, r))
				.collect(Collectors.toList());

		if (!C.isEmpty()) {
			if (!isSelfloop(v) && !zSolver.isTrueSyntactic(zPath)) {
				final List<TreeNode> path = v.getPathToRoot().stream()
						.filter(node -> node.id >= pv.id).collect(Collectors.toList());
				final ListExpr[] F = (ListExpr[]) IC3BlockPath(path, zSolver.mkAnd(C))[1];
				for (int i = F.length - 1; i > 0; i--) {
					final TreeNode node = path.get(i);
					final List<BoolExpr> iR = node.data.R;
					final BoolExpr ziR = zSolver.mkAnd(iR);
					final List<BoolExpr> f = F[i];
					final int iRSize = iR.size();
					for (final BoolExpr c : f) {
						if (!zSolver.entails(ziR, c)) {
							iR.add(c);
						}
					}
					if (iRSize < iR.size()) {
						uncover(node);
					}
				}
			}
			v.data.R.addAll(C);
			uncover(v);
		}
	}

	/*
	private void strengthenByParent(final TreeNode v) {
		final TreeNode pv = v.parent;
		if (pv.data.R.isEmpty()) {
			return;
		}
		final BoolExpr T = getZTran(v);
		if (zSolver.isTrueSyntactic(T)) {
			v.data.R.addAll(pv.data.R);
			return;
		}
		final BoolExpr TF = zSolver.mkAnd(zSolver.mkAnd(pv.data.R), T);
		pv.data.R.stream().map(r -> zSolver.substitute(r, pv.data.zid, v.data.zid))
				.filter(r -> zSolver.entails(TF, r)).forEach(r -> v.data.R.add(r));
	}
	*/

	private void strengthen(final TreeNode v) throws AnalysisException {
		final TreeNode pv = getFirstBiologicalParent(v);
		if (pv == null || pv.data.R.isEmpty()) {
			return;
		}
		strengthenByBioParent(v);

		// switch(mode){
		// case STRENGTHEN_BY_PARENT:
		// strengthenByParent(v);
		// break;
		// case STRENGTHEN_BY_BIOPARENT:
		// strengthenByBioParent(v);
		// break;
		// }
	}

	private void close(final TreeNode v) throws AnalysisException {
		if (v.data.isCovered()) {
			return;
		}
		final int result = covers(v);
		if (result > INT_UNKNOWN) {
			v.data.coveredBy = result;
			if (result == v.id) {
				v.data.isCoveringNode = true;
			} else {
				getFirstBiologicalParent(v).data.isCoveringNode = true;
			}
			v.children.forEach(child -> mTree.removeLeaf(child));
		}
	}

	private int covers(final TreeNode v) throws AnalysisException {
		if (v == null) {
			throw mkErrorMessage("Given node is null.");
		}
		// final BoolExpr vD = getDomain(v, v.parent);
		final List<BoolExpr> vR = new ArrayList<>(v.data.R);
		vR.add(getZTran(v));
		if (zSolver.entails(vR, ZFALSE)) {
			return v.id;
		}

		final TreeNode pv = getFirstBiologicalParent(v);
		if (pv == null) {
			return INT_UNKNOWN;
		}
		if (pv.data.R.isEmpty()) {
			return pv.id;
		}
		// final BoolExpr domains = getDomain(v, pv);
		if (!v.data.R.isEmpty()) {
			// IF
			// R(v) |= R(pv)
			// OR
			// (FORALL X) T(v_0)<0> & R(v_0)<0> ... T(v_n)<n> & R(v_n)<n> ==>
			// R(v_0)<n>,
			// where v_0 = pv and v_n = v
			// OR
			// IF both v and pv have blocked the bad state AND
			// (FORALL cube={f_1 OR f_2 OR ...} in R(v) s.t.
			// VARS(f_i)!=VARS(f_j) for i!=j)
			// if there exists f_i in cube s.t. f_i |= R(pv)
			// THEN RETURN TRUE
			// ELSE RETURN FALSE

			final BoolExpr zpvR = substitute(zSolver.mkAnd(pv.data.R), pv, v);
			final BoolExpr zvR = zSolver.mkAnd(v.data.R);
			if (zSolver.entails(zvR, zpvR)) {
				return pv.id;
			}
			// final List<BoolExpr> cnf = zSolver.getCNF2(zpvR);
			// final boolean present = cnf.stream().sequential()
			// .filter(e -> zSolver.entails(zvR, e)).findAny().isPresent();
			// if (present) {
			// return pv.id;
			// }

			// else if (prove(v, pv)) {
			// return pv.id;
			// // If both nodes were unreachable then we do underapproximation
			// } else if (underapproximate
			// // && (v.data.status == STATUS_BLOCKED_NODE || v.data.status
			// // == STATUS_BLOCKED_INIT)
			// // && (pv.data.status == STATUS_BLOCKED_NODE ||
			// // pv.data.status == STATUS_BLOCKED_INIT)
			// && (pv.data.status == STATUS_BLOCKED_NODE)
			// && under_approximate(v.data.R, zpvR)) {
			// return pv.id;
			// }
		}
		//
		// else {
		// return prove(v, pv) ? pv.id : INT_UNKNOWN;
		// }
		return INT_UNKNOWN;
	}

	private void unwind(final TreeNode v) throws AnalysisException {
		if (v.isForbidden() || v.data.isCovered()) {
			return;
		}
		if (!v.isLeaf()) {
			throw mkErrorMessage("Node is not a leaf.");
		}
		if (v.data.L == INT_UNKNOWN) {
			throw mkErrorMessage("Node has no location.");
		}
		if (v.children.size() > 0) {
			throw mkErrorMessage("Node has already children.");
		}
		// Making children!
		mIter.resetState(v.data.L);
		while (mIter.advance()) {
			final int target = mIter.getCurrentTargetState();
			final int labelId = mIter.getCurrentEvent();
			final TreeNode node = mkTreeNode(v, target, labelId);
			node.setForbidden(mStEnc.isForbidden(target));
		}
		if (!v.children.isEmpty()) {
			if (mLbToSpec != null && !mLbToSpec.isEmpty()) {
				// Making forbidden nodes and updating 'v' children's info
				final List<Object[]> fs = new ArrayList<>();
				for (final TreeNode child : v.children) {
					final SimpleExpressionProxy spec = mLbToSpec.get(child.data.Lb);
					final String s = (spec == null) ? STR_TRUE : spec.toString();
					if (!isTrueStr(s)) {
						fs.add(new Object[] { child.data.L, child.data.Lb, mkNot(s) });
						// Setting the child node's transition formula to: T & Spec
						child.data.sT = mkAnd(child.data.sT, s);
					}
				}
				if (!fs.isEmpty()) {
					for (final Object[] f : fs) {
						final TreeNode forbidden = mkTreeNode(v, (int) f[0], (int) f[1]);
						forbidden.setForbidden(true);
						// Setting the forbidden node's transition formula to: T & (~Spec)
						forbidden.data.sT = mkAnd(forbidden.data.sT, (String) f[2]);
					}
				}
			}
		} else {
			// If v has no children (deadlock) then it will be covered by itself
			v.data.coveredBy = v.id;
			v.data.isCoveringNode = true;
		}
	}

	private boolean pathBlocking(final TreeNode v) throws AnalysisException {
		final List<TreeNode> path = v.getPathToRoot();
		// Has a counterexample to induction (CTI)?
		final Object[] result = IC3BlockPath(path, null);
		final int status = (int) result[0];
		final ListExpr[] F = (ListExpr[]) result[1];
		// (TODO) Combine CTI and BLOCKED_INIT
		switch (status) {
			case STATUS_CTI:
				if (!supervise(path, F)) {
					// If there exists a counter example to induction and we
					// cannot
					// control it,
					// then no supervisor exist
					return false;
				}
				// Check if we can close the last node before forbidden node
				// after
				// supervision
				close(path.get(path.size() - 2));
				break;
			// case STATUS_BLOCKED_INIT:
			// // If we hit the initial state then we do supervision
			// updateIndInv(path, F);
			// break;
			case STATUS_BLOCKED_NODE:
				// (TODO) overapproximate supervisor!
				// If bad is not reachable then we update ind. inv. R
				updateIndInv(path, F);
				break;
			case STATUS_NOTREACHABLE:
				// If bad is not reachable, then we continue
				break;
			default:
				throw new AnalysisException("Unsupported status: " + status);
		}
		return true;
	}

	/**
	 *
	 * @param path
	 *            The path from the root to the significant node.
	 * @param assumption
	 *            An assumption formula (A) representing the value of variables
	 *            at node v_n. Namely, the incoming transition formula related
	 *            to v_n, say T_n, will become a new formula T_n & A'. Note
	 *            that, it is assumed the index of variables in T_n and A are in
	 *            the right order.<br>
	 *            {@code Null}: The assumption will be ignored.
	 * @return Return {STATUS, F}: STATUS is an integer: 0 -> CTI, 1 -> Reaching
	 *         initial state but not a CTI, and 2 -> Not reachable from initial
	 *         state; and F is the trace for nodes v_0 ... v_(n-1).
	 * @throws AnalysisException
	 */
	private Object[] IC3BlockPath(final List<TreeNode> path, final BoolExpr assumption)
			throws AnalysisException {
		final int index = path.size() - 1;
		// Current node
		TreeNode cNode;
		// Previous node
		TreeNode pNode;
		// Bad cube formula
		BoolExpr c;
		BoolExpr TF;
		// Pair of cube formula (pair[0]) and index j (pair[1])
		Object[] pair;
		// Stack of pairs
		final Stack<Object[]> stack = new Stack<>();

		// Initializing the trace set F and transition formula set T
		final BoolExpr[] T = new BoolExpr[index];
		final ListExpr[] F = new ListExpr[index];
		for (int i = index; i > 0; i--) {
			cNode = path.get(i);
			pNode = path.get(i - 1);
			T[i - 1] = getZTran(cNode);
			F[i - 1] = new ListExpr();
			F[i - 1].add(zSolver.mkAnd(pNode.data.R));
		}

		// Applying the assumption formula T_n := T_n & A'
		if (assumption != null && !zSolver.isTrueSyntactic(assumption)) {
			T[index - 1] = zSolver.mkAnd(T[index - 1], assumption);
		}

		cNode = path.get(index);
		pNode = path.get(index - 1);
		// (TODO) Domain assumption
		List<BoolExpr> images;
		TF = zSolver.mkAnd(T[index - 1], zSolver.mkAnd(F[index - 1]));
		images = getPreImage(cNode, pNode, TF);
		if (zSolver.isFalseSyntactic(images.get(0))) {
			return new Object[] { STATUS_NOTREACHABLE, F };
		}
		for (final BoolExpr cube : images) {
			stack.push(new Object[] { cube, index - 1 });
		}
		int j;
		int status = STATUS_BLOCKED_NODE;
		// ****************************************************
		while (!stack.isEmpty()) {
			pair = stack.peek();
			c = (BoolExpr) pair[0];
			j = (int) pair[1];
			// Is this the root node (path[0])?
			if (j == 0) {
				// return counter example
				status = STATUS_CTI;
				stack.pop();
				for (final ListExpr f : F){
					f.clear();
				}
				F[0].add(ZFALSE);
				continue;
				//break;
			}
			// TF = T[j-1] & F[j-1]
			TF = zSolver.mkAnd(T[j - 1], zSolver.mkAnd(F[j - 1]));
			// If TF is False then the forbidden node is not reachable
//			if (zSolver.entails(TF, ZFALSE)) {
//				for (int i = j; i < F.length; i++) {
//					F[i].add(ZFALSE);
//				}
//				return new Object[] { STATUS_NOTREACHABLE, F };
//			}
			// If TF is syntacticly True then push the cube and continue
			if (zSolver.isTrueSyntactic(TF)) {
				stack.push(new Object[] { c, j - 1 });
				continue;
			}

			final BoolExpr nc = zSolver.mkNot(c);
			if (zSolver.entails(TF, nc)) {
				stack.pop();
				// if (!zSolver.entails(zSolver.mkAnd(F[j]), nc)) {
				// F[j].add(nc);
				// }
				// (TODO) Generalization
				final List<BoolExpr> dnf = zSolver.getDNF2(nc);
				final BoolExpr zFj = zSolver.mkAnd(F[j]);
				// IF EXISTS(g in DNF(~c)) s.t. T[j-1] & F[j-1] |= g AND
				// ~(F[j]|=g) THEN F[j] <- g
				// IF ~EXISTS(g in DNF(~c)) s.t. T[j-1] & F[j-1] |= g THEN
				// (FORALL g in DNF(~c)) F[j] <- g
				boolean flag = false;
				for (final BoolExpr g : dnf) {
					if (zSolver.entails(TF, g)) {
						flag = true;
						if (!zSolver.entails(zFj, g)) {
							F[j].add(g);
						}
					}
				}
				if (!flag) {
					F[j].add(nc);
				}
			} else {
				cNode = path.get(j);
				pNode = path.get(j - 1);
				final BoolExpr pvR = zSolver.mkAnd(pNode.data.R);
				images = getPreImage(cNode, pNode, zSolver.mkAnd(TF, c));
				// (TODO) underapproximate by picking only the first cube
				for (final BoolExpr cube : images) {
					final BoolExpr e = zSolver.getCNF2(cube).stream().sequential()
							.filter(exp -> !zSolver.entails(pvR, exp))
							.reduce(ZTRUE, zSolver::mkAnd);
					stack.push(new Object[] { e, j - 1 });
				}
			}
		}
		// ****************************************************
		if (status == STATUS_CTI) {
			for (final List<BoolExpr> f:F){
				f.replaceAll(zSolver::mkNot);
			}
		} else {
			for (int i = 0; i < F.length; i++) {
				F[i].remove(0);
			}
		}
		return new Object[] { status, F };
	}

	public void dispose() {
		zSolver.dispose();
		zSolver = null;
	}

	private List<BoolExpr> getPreImage(final TreeNode cNode, final TreeNode pNode,
			final BoolExpr expr) throws SolverException {
		if (cNode.data.zid == pNode.data.zid) {
			return zSolver.getDNF2(expr);
		} else {
			return zSolver.getPreImage(expr, cNode.data.zid);
		}
	}

	private void initTree() throws AnalysisException {
		final TreeNode root = mTree.root;
		root.data.zid = mTree.root.id;
		mkTreeNode(root, mStEnc.getInitialStateId(), INITIAL_LABEL_ID);
	}

	/*
	 * private boolean entails(final List<BoolExpr> R, final BoolExpr zpvR,
	 * final BoolExpr... assumption) { final BoolExpr zvR = zSolver.mkAnd(R);
	 * return zSolver.entails(zvR, zpvR, assumption); }
	 *
	 * private boolean prove(final TreeNode v, final TreeNode pv, final
	 * BoolExpr... assumption) { final List<BoolExpr> zpath = new ArrayList<>();
	 * TreeNode node = v; while (node != pv.parent) {
	 * zpath.add(getZTranFormula(node)); zpath.addAll(node.data.R); node =
	 * node.parent; } return zSolver.prove(zSolver.mkAnd(zpath),
	 * zSolver.mkAnd(pv.data.R), pv.data.zid, v.data.zid, assumption); }
	 */
	private BoolExpr getZPath(final TreeNode v, final TreeNode pv) {
		return v.getPathToRoot().stream().sequential().filter(node -> node.id > pv.id)
				.map(node -> getZTran(node)).reduce(ZTRUE, zSolver::mkAnd);
	}

	/*
	 * private Boolean under_approximate(final List<BoolExpr> vR, final BoolExpr
	 * zpvR, final BoolExpr... assumption) { loop: for (final BoolExpr cube :
	 * vR) { final BoolExpr solved_cube = zSolver.simplify(cube); final
	 * List<BoolExpr> split = zSolver.split(solved_cube); for (final BoolExpr
	 * exp : split) { if (zSolver.entails(exp, zpvR, assumption)) { continue
	 * loop; } } return false; } return true; }
	 *
	 * private BoolExpr getDomain(final TreeNode v, final TreeNode pv) { if
	 * (v.data.zid == pv.data.zid) { return getDomain(v); } else { return
	 * zSolver.mkAnd(getDomain(v), getDomain(pv)); }
	 *
	 * }
	 *
	 * private BoolExpr getDomain(final TreeNode v) { if (v.data.D == null) { if
	 * (!v.isRoot() && v.data.zid == v.parent.data.zid && v.parent.data.D !=
	 * null) { v.data.D = v.parent.data.D; } else { v.data.D =
	 * zSolver.getZDomain(v.data.zid); } } return v.data.D; }
	 *
	 * private BoolExpr getDomain(final int index) { return
	 * zSolver.getZDomain(index); }
	 *
	 * private BoolExpr getDomain(final int index1, final int index2) { return
	 * zSolver.mkAnd(zSolver.getZDomain(index1), zSolver.getZDomain(index2)); }
	 */
	private void uncover(final TreeNode v) {
		// If node is covering nodes
		if (v.data.isCoveringNode && !isDeadlock(v)) {
			mTree.nodestream().parallel().filter((node) -> node.data.coveredBy == v.id)
					.map(node -> {
						node.data.coveredBy = INT_UNKNOWN;
						return node;
					}).filter(node -> node.isLeaf()).forEach(node -> mTree.pushLeaf(node));
			v.data.isCoveringNode = false;
		}
	}

	private List<BoolExpr> evaluate(final List<TreeNode> path) throws AnalysisException {
		zSolver.reset();
		final List<Integer> indices = new ArrayList<>();
		for (int i = 1; i < path.size(); i++) {
			final TreeNode v = path.get(i);
			indices.add(v.data.zid);
			zSolver.addZ3ExpToContext(getZTran(v));
		}
		zSolver.check();
		final List<BoolExpr> models = zSolver.getZModel(indices);
		if (models == null) {
			return Arrays.asList(ZFALSE);
		}
		// Adding 'True' at the beginning of the list
		models.add(ZTRUE);
		Collections.rotate(models, 1);
		return models;
	}

	private boolean supervise(final List<TreeNode> path, final ListExpr F[])
			throws AnalysisException {
		if (path.stream().anyMatch(node -> node.data.isControllable)) {
			final List<BoolExpr> eval = evaluate(path.subList(0, path.size() - 1));
			eval.add(ZFALSE);
			TreeNode sNode = null;
			boolean found = false;
			final List<BoolExpr> C = new ArrayList<>();
			for (int i = path.size() - 1; i > 0; i--) {
				final TreeNode v = path.get(i - 1);
				final BoolExpr e = eval.get(i - 1);
				// Updating this node ind. inv.
				BoolExpr guard = zSolver.simplify(zSolver.mkNot(zSolver.mkAnd(F[i - 1])));
				if (zSolver.isUNSAT(guard,e)){
					guard = ZFALSE;
				}
				if (!found){
					updateIndInv(v, Arrays.asList(guard));
				}
				// If controllable node is found then we stop.
				if (v.data.isControllable) {
					v.data.status = STATUS_SUPERVISED;
					found = true;
					if (sNode == null){
						sNode = v;
						C.add(getZGeneralizedExpr(guard, v.data.zid));
					} else if (v.data.L == sNode.data.L){
						C.add(getZGeneralizedExpr(guard, v.data.zid));
					}
				}
			}
			final long u = sNode.data.getUniqueHash();
			BoolExpr c = zSupExpr.get(u);
			final BoolExpr guard = zSolver.simplify(zSolver.mkOr(C));
			c = (c == null) ? guard : (BoolExpr) zSolver.mkAnd(c, guard).simplify();
			zSupExpr.put(u, c);
			return true;

		} else {
			updateIndInv(path, F);
		}
		// If no controllable node is found then we cannot control the path!
		return false;
	}

	private BoolExpr getZGeneralizedExpr(final BoolExpr exp, final int index) {
		return zSolver.substitute(exp, index, GENERAL_ZID);
	}

	private BoolExpr getZSpecificExpr(final BoolExpr exp, final int index) {
		return zSolver.substitute(exp, GENERAL_ZID, index);
	}

	private void updateIndInv(final List<TreeNode> path, final ListExpr F[])
			throws AnalysisException {
		for (int i = path.size() - 2; i > 0; i--) {
			updateIndInv(path.get(i), F[i]);
		}
	}

	private void updateIndInv(final TreeNode v, final List<BoolExpr> Fv) throws AnalysisException {
		if (Fv.size() < 1) {
			return;
		}
		final boolean changed = v.data.R.addAll(Fv);
		if (changed) {
			uncover(v);
		}
	}

	// ************************* Helper Methods ********************************

	private HashMap<Integer, String> getStateSpecInfo(final int stateId) {
		final HashMap<Integer, String> map = new HashMap<>();
		final String sLabel = mStEnc.getAttribute(stateId, SimpleEFAHelper.DEFAULT_SPEC_KEY);
		if (sLabel == null || sLabel.isEmpty()) {
			return map;
		}
		final String[] clauses = sLabel.split(SimpleEFAHelper.DEFAULT_VALUE_SEPARATOR);
		for (final String clause : clauses) {
			final String[] items = clause.split(SimpleEFAHelper.DEFAULT_SPEC_TO);
			map.put(Integer.parseInt(items[0]), items[2]);
		}
		return map;
	}

	private TreeNode mkTreeNode(final TreeNode parent, final int stateId, final int labelId) {
		final TreeNode child = mTree.addChild(parent);
		child.data.L = stateId;
		child.data.Lb = labelId;
		if (labelId == INITIAL_LABEL_ID) {
			child.data.sT = mkAnd(getInitialExpression());
			child.data.isControllable = false;
		} else {
			child.data.sT = mkAnd(mLbEnc.getConstraintByLabelId(labelId));
			child.data.isControllable = mLbEnc.isControllable(labelId);
		}
		if (hasNextVariable(child.data.sT)) {
			// If condition has next variable then make a fresh next z3
			// variable.
			// E.g., let child.parent.data.zid=0 and child.data.zid=1.
			// Then (Formula) x<2 & x'=x+y --> (Z3) x0<2 & x1=x0+y0
			child.data.zid = child.id;
			child.data.sT = mkCompleteTranCond(child.data.sT);
		} else {
			// If condition has no next variable (pure guard)
			// then current and next z3 variables will be the same
			child.data.zid = child.parent.data.zid;
		}
		return child;
	}

	private boolean hasNextVariable(final String cond) {
		// Given a set of variables X,
		// it is assumed that action formulas are of the form X'= F(X)
		return cond.trim()
				.contains(mOp.getNextOperator().getName() + mOp.getEqualsOperator().getName());
	}

	private String mkCompleteTranCond(String cond) {
		cond = cond.trim();
		final String eq = mOp.getEqualsOperator().getName();
		for (final SimpleEFAVariable var : mVarCtx) {
			final String varp = var.getPrimedVariableName().toString();
			if (!cond.contains(varp + eq)) {
				cond += mOp.getAndOperator().getName() + varp + eq + var.getName();
			}
		}
		return cond;
	}

	private BoolExpr getZTran(final TreeNode v) {
		if (v.data.zT == null) {
			v.data.zT = mkBoolExpr(mkIndex(v.parent.data.zid, v.data.zid), v.data.sT);
		}
		return (BoolExpr) zSolver.mkAnd(v.data.zT, getZSup(v)).simplify();
	}

	private BoolExpr getZSup(final TreeNode v) {
		final BoolExpr C = zSupExpr.get(v.data.getUniqueHash());
		return C == null ? ZTRUE : getZSpecificExpr(C, v.data.zid);
	}

	private int[] mkIndex(final int val1, final int val2) {
		return new int[] { val1, val2 };
	}

	private String mkAnd(final String str1, final String str2) {
		if (isFalseStr(str1) || isFalseStr(str2)) {
			return STR_FALSE;
		}
		if (isTrueStr(str1)) {
			return str2;
		}
		if (isTrueStr(str2)) {
			return str1;
		}
		return "(" + str1 + ")" + mOp.getAndOperator().getName() + "(" + str2 + ")";
	}

	private String mkAnd(final ConstraintList cons) {
		if (cons.isTrue()) {
			return STR_TRUE;
		}
		final String andStr = cons.getConstraints().stream()
				.map((exp) -> "(" + exp.toString() + ")&").reduce("", String::concat);
		return andStr.substring(0, andStr.length() - 1);
	}

	/*
	 * private String mkOr(final String str1, final String str2) { if
	 * (isTrueStr(str1) || isTrueStr(str2)) { return STR_TRUE; } if
	 * (isFalseStr(str1)) { return str2; } if (isFalseStr(str2)) { return str1;
	 * } return "(" + str1 + ")|(" + str2 + ")"; }
	 */

	private String mkNot(final String str) {
		if (isTrueStr(str)) {
			return STR_FALSE;
		}
		if (isFalseStr(str)) {
			return STR_TRUE;
		}
		return str.isEmpty() ? "" : "!(" + str + ")";
	}

	private BoolExpr mkBoolExpr(final int[] indexSet, final String str) {
		if (isFalseStr(str)) {
			return ZFALSE;
		}
		if (isTrueStr(str)) {
			return ZTRUE;
		}
		BoolExpr exp = null;
		try {
			zSolver.clear();
			zSolver.addStrExpToContext(indexSet, str);
			exp = zSolver.getContextZFormula();
		} catch (final SolverException e) {
		}
		return exp;
	}

	private Boolean isFalseStr(final String str) {
		return str.equals(STR_FALSE);
	}

	private Boolean isTrueStr(final String str) {
		return str.isEmpty() || str.equals(STR_TRUE);
	}

	private ConstraintList getInitialExpression() {
		final List<SimpleExpressionProxy> exps = mVarCtx.stream()
				.map(SimpleEFAVariable::getInitialStatePredicate).collect(Collectors.toList());
		return new ConstraintList(exps);
	}

	private TreeNode getFirstBiologicalParent(final TreeNode v) {
		TreeNode bPV = null;
		switch (v.data.bPV) {
			case INT_NA:
				break;
			case INT_UNKNOWN:
				v.data.bPV = INT_NA;
				if (isSelfloop(v)){
					bPV = v.parent;
				} else {
					bPV = v.findFirstAncestor(node -> node.id < v.id && node.data.L == v.data.L);
				}
				if (bPV != null) {
					v.data.bPV = bPV.id;
				}
				break;
			default:
				bPV = mTree.getNode(v.data.bPV);
				break;
		}
		return bPV;
	}

	private void printSupervisionGuards() throws AnalysisException {
		System.err.println("********* IISCT *********");
		final List<TreeNode> sups = mTree.getNodes().stream()
				.filter(node -> node.data.hasSupGuards()).collect(Collectors.toList());
		for (final TreeNode v : sups) {
			final BoolExpr img = zSolver.getPreImage2(getZTran(v), v.data.zid);
			final BoolExpr sub = zSolver.substitute(img, v.parent.data.zid, -1);
			System.err.println(mStEnc.getSimpleState(v.parent.data.L).getName() + " > "
					+ mLbEnc.getEventDeclByLabelId(v.data.Lb).getName() + " : " + sub);
		}
		/*for (final TreeNode v : sups) {
			final List<TreeNode> path = new ArrayList<>(v.getPathToRoot());
			final List<BoolExpr> eval = evaluate(path.subList(0, path.size() - 1));
			//eval.add(ZFALSE);
			final List<TreeNode> nodes = path.stream()
					.filter(node -> node.data.L == v.data.L && node.id < v.id).sorted()
					.collect(Collectors.toList());
			BoolExpr sub = ZFALSE;
			BoolExpr C = null;
			for (int i = nodes.size() - 1; i >= 0; i--) {
				final TreeNode node = nodes.get(i);
				final BoolExpr e = eval.get(path.indexOf(node));
				C = zSolver.substitute(getZSup(v), v.data.zid, node.data.zid);
				final BoolExpr img = zSolver.getPreImage2(zSolver.mkAnd(getZTran(node), C),
						node.data.zid, e);
				if (!zSolver.isFalseSyntactic(img)) {
					if (node.data.zid == node.parent.data.zid) {
						sub = zSolver.substitute(C, node.parent.data.zid, -1);
					} else {
						sub = zSolver.substitute(img, node.parent.data.zid, -1);
					}
					break;
				}
			}
			System.err.println(mStEnc.getSimpleState(v.parent.data.L).getName() + " > "
					+ mLbEnc.getEventDeclByLabelId(v.data.Lb).getName() + " : " + sub);
		}*/
		System.err.println("***********************");
		System.err.println("System: " + mTR.getName());
		System.err.println("Nr Nodes: " + mTree.size());
		System.err.println(zSupExpr.valueCollection());
		System.err.println("***********************");
	}

	private AnalysisException mkErrorMessage(final String msg) {
		return new AnalysisException(Thread.currentThread().getStackTrace()[1].getClassName()
				+ " > " + Thread.currentThread().getStackTrace()[1].getMethodName() + " : " + msg);
	}

	private BoolExpr substitute(final BoolExpr exp, final TreeNode oldNode,
			final TreeNode newNode) {
		return zSolver.substitute(exp, oldNode.data.zid, newNode.data.zid);
	}

	private boolean isSelfloop(final TreeNode v) {
		return v.data.L == v.parent.data.L;
	}

	private boolean isDeadlock(final TreeNode v) {
		return v.data.coveredBy == v.id;
	}

	private class Data {
		// Location Id
		public int L = INT_UNKNOWN;
		// Incoming transition condition in complete form without specification
		public String sT = STR_TRUE;
		// Incoming transition condition with specification
		public BoolExpr zT;
		// Z3 variables id
		public int zid = INT_UNKNOWN;
		// Incoming transition label Id
		public int Lb = INT_UNKNOWN;
		// Is covering any node
		public boolean isCoveringNode = false;
		// Has incoming controllable transition
		public boolean isControllable = false;
		// Node that covers this node
		public int coveredBy = INT_UNKNOWN;
		// Biological parent of this node
		public int bPV = INT_UNKNOWN;
		// Set of inductive invariant predicates
		public List<BoolExpr> R = new ArrayList<>();
		// Domain
		public BoolExpr D;
		// Status of IC3PathBlocking
		public int status = INT_UNKNOWN;

		public boolean isCovered() {
			return coveredBy != INT_UNKNOWN;
		}

		public boolean isSupervisable() {
			return status == STATUS_SUPERVISED && isControllable;
		}

		public boolean hasSupGuards() {
			return status == STATUS_SUPERVISED;
		}

		// Cantor pairing function: Pairing two integers into a unique integer
		public long getUniqueHash() {
			final long A = 2 * (long) L;
			final long B = 2 * (long) Lb;
			return (A >= B ? A * A + A + B : A + B * B) / 2;
		}

		@Override
		public String toString() {
			return "{L=" + L + ", T=" + sT + ", R=" + R + "}";
		}

	}

	// ************************************************************************
	public class TreeNode implements Comparable<TreeNode> {
		public TreeNode(final int id) {
			this.id = id;
			this.data = new Data();
			this.children = new ArrayList<>();
			this.parent = null;
			this.pathToRoot = null;
			this.isForbidden = false;
		}

		public boolean isRoot() {
			return parent == null;
		}

		public boolean isLeaf() {
			return children.isEmpty();
		}

		public void setForbidden(final boolean isForbidden) {
			this.isForbidden = isForbidden;
		}

		public boolean isForbidden() {
			return this.isForbidden;
		}

		public TreeNode findFirstAncestor(final Predicate<TreeNode> p) {
			final List<TreeNode> list = this.getPathToRoot().stream().filter(p).sorted()
					.collect(Collectors.toList());
			return list.isEmpty() ? null : list.get(list.size() - 1);
		}

		// public Stream<TreeNode> getPathFromNode(final TreeNode fromNode) {
		// return this.getPathToRoot().stream()
		// .filter(node -> node.id >= fromNode.id).sorted();
		// }

		@Override
		public int compareTo(final TreeNode node) {
			if (this.id < node.id) {
				return -1;
			} else if (this.id == node.id) {
				return 0;
			} else {
				return 1;
			}
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj == null || getClass() != obj.getClass()) {
				return false;
			}
			return this.id == ((TreeNode) obj).id;
		}

		@Override
		public int hashCode() {
			int hash = 5;
			hash = 53 * hash + this.id;
			return hash;
		}

		@Override
		public String toString() {
			return "Node " + Integer.toString(this.id);
		}

		public List<TreeNode> getPathToRoot() {
			if (pathToRoot == null) {
				final ArrayList<TreeNode> path = new ArrayList<TreeNode>();
				TreeNode v = this;
				do {
					path.add(v);
					v = v.parent;
				} while (v != null);
				Collections.reverse(path);
				pathToRoot = Collections.unmodifiableList(path);
			}
			return pathToRoot;
		}

		public final int id;
		public TreeNode parent;
		public final List<TreeNode> children;
		public Data data;
		private boolean isForbidden;
		private List<TreeNode> pathToRoot;
	}

	// ************************************************************************
	private final class Tree {

		public Tree() {
			elementsIndex = new ArrayList<>();
			leafs = new Stack<>();
			root = new TreeNode(0);
			elementsIndex.add(root);
			leafs.push(root);
		}

		public synchronized TreeNode addChild(final TreeNode parent) {
			if (parent == null) {
				return null;
			}
			final TreeNode child = new TreeNode(elementsIndex.size());
			elementsIndex.add(child);
			child.parent = parent;
			parent.children.add(child);
			removeLeaf(parent);
			pushLeaf(child);
			return child;
		}

		public void pushLeaf(final TreeNode v) {
			leafs.push(v);
		}

		public void addLeaf(final TreeNode v) {
			leafs.add(v);
		}

		public void removeLeaf(final TreeNode v) {
			leafs.remove(v);
		}

		public TreeNode popLeaf() {
			return leafs.pop();
		}

		public synchronized Stream<TreeNode> nodestream() {
			return elementsIndex.stream();
		}

		public TreeNode getNode(final int nodeId) {
			return elementsIndex.get(nodeId);
		}

		public List<TreeNode> getNodes() {
			return new ArrayList<>(elementsIndex);
		}

		public synchronized boolean hasLeaf() {
			return !leafs.isEmpty();
		}

		public int size() {
			return elementsIndex.size();
		}

		@Override
		public String toString() {
			return "\nNodes: " + elementsIndex.toString() + "\nLeafs: " + leafs.toString();
		}

		public final TreeNode root;
		private final List<TreeNode> elementsIndex;
		private final Stack<TreeNode> leafs;
	}

	// ************************************************************************
	@SuppressWarnings("serial")
	private static class ListExpr extends ArrayList<BoolExpr> {
	}

	// ************************************************************************
	// FILEDS
	// ************************************************************************
	private static final CompilerOperatorTable mOp = CompilerOperatorTable.getInstance();
	private static final String STR_TRUE = "1";
	private static final String STR_FALSE = "0";
	private static final int INT_UNKNOWN = -1;
	private static final int INT_NA = -2;
	private static final int INITIAL_LABEL_ID = Short.MAX_VALUE;
	private static final int STATUS_CTI = 0;
	private static final int STATUS_BLOCKED_INIT = 1;
	private static final int STATUS_BLOCKED_NODE = 2;
	private static final int STATUS_NOTREACHABLE = 3;
	private static final int STATUS_SUPERVISED = 4;
	private static final int GENERAL_ZID = 0;
	private static final int STRENGTHEN_BY_PARENT = 0;
	private static final int STRENGTHEN_BY_BIOPARENT = 1;
	private final Collection<SimpleEFAVariable> mVarCtx;
	private final ListBufferTransitionRelation mTR;
	private final SimpleEFALabelEncoding mLbEnc;
	private final SimpleEFAStateEncoding mStEnc;
	private final TransitionIterator mIter;
	private final Tree mTree;
	private Z3Solver zSolver;
	private final boolean overapproximate = false;
	private final boolean underapproximate = false;
	private final HashMap<Integer, SimpleExpressionProxy> mLbToSpec;
	// private final TIntObjectHashMap<BoolExpr> zLbToTranExpr;
	private final TLongObjectHashMap<BoolExpr> zSupExpr;
	private final BoolExpr ZFALSE;
	private final BoolExpr ZTRUE;
}
