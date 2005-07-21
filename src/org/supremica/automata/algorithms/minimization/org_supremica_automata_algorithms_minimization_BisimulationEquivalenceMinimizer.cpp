
/*********************************************************************************************
 *
 * What: strong_eq.c
 *
 **********************************************************************************************
 *
 * Version:   What:                  Date:                      Who:
 *
 * 1.0        Original               880324                     Sven Westin
 *
 * 1.1        Corrected bug that ocurred when all processes initially are bisimilar.
 *            Comments rewritten. Names of identifiers changed.
 *                                   890420                     Sven Westin
 *
 * 1.2        Modified code to run by JNI-calls (see cvs system for further modifications).
 *                                   050706                     Hugo Flordal
 *
 * NOTE: Code for tracing and debugging is removed from this version.
 *
 **********************************************************************************************
 *
 * This program implements an algorithm that decides strong
 * bisimulation equivalence for processes in a labelled transition
 * system.
 *
 * The algorithm used is (in detail) described in the paper "Fast
 * Decision of Bisimulation Equivalence using Partition Refinement" by
 * Sven Westin. (Univ. of Gothenburg and Chalmers Univ. of Technology,
 * S-412-96 Gothenburg, Sweden)
 *
 * The algorithm is a generalization of an algorithm solving the
 * relational coarsest partition problem. The latter algorithm is
 * described in the article "Three partition refinement algorithms" by
 * Paige and Tarjan (SIAM Journal of Computing, Vol 16, no 6
 * Dec. 1987,p. 977-982)
 *
 ************************************************************************************************
 *
 * COMPILING INSTRUCTIONS
 *
 * Note that you probably have to change the folders below!
 *
 * Compiling on Windows:
 *
 * cl -I"c:\Program Files\Java\jdk1.5.0_04"\include -I"c:\Program Files\Java\jdk1.5.0_04"\include\win32 -I"C:\Program Files\Microsoft Visual Studio .NET 2003\Vc7"\include -LD org_supremica_automata_algorithms_minimization_BisimulationEquivalenceMinimizer.cpp -FeBisimulationEquivalence.dll /link /LIBPATH:"C:\Program Files\Microsoft Visual Studio .NET 2003\Vc7"\lib
 *
 * Compiling on Linux:
 *
 * g++ -shared -fPIC -I$JAVA_HOME/include -I$JAVA_HOME/include/linux org_supremica_automata_algorithms_minimization_BisimulationEquivalenceMinimizer.cpp -o ~/Supremica/platform/linux.x86/lib/libBisimulationEquivalence.so
 *
 * Compiling on Mac OS X:
 *
 * g++ -c -I /System/Library/Frameworks/JavaVM.framework/Headers/ org_supremica_automata_algorithms_minimization_BisimulationEquivalenceMinimizer.cpp
 * g++ -dynamiclib -install_name BisimulationEquivalenceMinimizer.dylib -o BisimulationEquivalenceMinimizer.dylib org_supremica_automata_algorithms_minimization_BisimulationEquivalenceMinimizer.o
 *
 ************************************************************************************************/

#ifdef _WINDOWS
#include <windows.h>
#endif

#include <jni.h>
#include "org_supremica_automata_algorithms_minimization_BisimulationEquivalenceMinimizer.h"

#include <stdlib.h>
#include <stdio.h>
#include <ctype.h>

#define FALSE 0
#define TRUE 1

///////////////////
// Stuff by Hugo //
///////////////////

int NBROFSTATES, NBROFEVENTS, NBROFTRANSITIONS;

/*********************************************************************************************
 *
 * Data Structures
 *
 *********************************************************************************************/


/**
 * Element node. One for each element (state) in the set
 */
struct element
{
  int index; /* Index of state */
  struct element *next; /* Next element in Q block */
  struct element *prev; /* Previous element in Q block */
  struct edge **in_edges; /* Array of edges incident to this element.
									- Each member (in_edges[i]) in the
									array points to edges in relation
									'i' incident to this element.
									(The largest index 'i'
									actually used is determined at
									run-time by the input routine) */
  struct qblock *my_qblock; /* block in Q
							   - to which this element belongs */
  struct count *cp; /* Points to count node (Number of
					   edges into 'B' from this element) */
  int mark_3; /* Mark field for step 3 */
  int mark_5; /* Mark field for step 5 */
  struct element *E_1_next; /* Next element in E l(B). {Step 3
							   - - and step 5) E 1 (B) - E l(S-B) */
  struct element *bprime_next; /* Temporary set-B' {Step-3) */
};

/**
 * Edge node. One for each edge between the elements in the set
 */
struct edge
{
  struct element *from_element;
  struct edge *in_edges; /* List of edges (except this one)
							incident to 'to_element' */
  struct count *cp; /* Points to count node
					   (Number of edges into'S' from
					   'from_element') */
};

/**
 * Q block node. Head node for a block in the Q partition. This record
 * points to a doubly linked list of elements in the Q block.
 * (q_next,q_prev)
 */
struct qblock
{
  int n_elements; /* Number of elements in Q block */
  struct qblock *q_next; /* Next Q block */
  struct qblock *q_prev; /* Previous Q block */
  struct element *first; /* First element in block */
  struct xblock **my_x_block; /* Array of pointers to X
										block's to which this
										block belongs.  Each
										member (my x block[i])
										points to a X block in X
										partition 'i' (may be
										NULL) */
  struct qblock *q_tmp; /* Associated blocks (Used in step 4) */
  struct qblock *splitl; /* List of splitted Q blocks (step 4) */
  struct qblock **x_next; /* Next Q block in this X block
									(in partition X i) */
  struct qblock **x_prev; /* Previous Q block in this X block
									(in partition X_i) */
};

/**
 * X block node. Head node for a block in the X partition. The X
 * partition is not used in practice. This record is used for linking
 * together compound X blocks (set of Q blocks) in the set C. The
 * record points to a doubly linked list (x_next,x_prev) of Q
 * blocks.
 */
struct xblock
{
  int relation; /* Number of relation on which to
				   partition, i.e., number
				   of the X partition to which this
				   X block belongs (X i) */
  struct qblock *q_first; /* First Q block in this X block */
  struct xblock *next; /* Next X block */
  struct xblock *prev; /* Previous X block */
};

/**
 * Count node. Used for the three-way splitting in step 5
 */
struct count
{
  int counter; /* Counter(s) used in step 3,5,7 */
};

/*********************************************************************************************
 *
 * Globals
 *
 *********************************************************************************************/

struct qblock *Q = NULL; /* Pointer to Q partition */
struct xblock *C = NULL; /* Pointer to the set C */
struct qblock *B = NULL; /* Pointer to refinement block B
							B (subset of block S) */
struct element *E_1_B = NULL; /* Pointer to edges incident
								 to B * (calculated in step 3) */
struct element *Bprime = NULL;

/* copy of elements in block B.
   Points directly on first element
   in B, i.e. no copy of Q block
   node. Step 3,5, and 7 */

int rel = -1; /* Relation on which we partition in this refinement
				 step */

int Iteration = 0 ; /* Number of current refinement step */


/*********************************************************************************************
 *
 * Help Routines
 *
 *********************************************************************************************/

int error(char *s, int al, int a2,int a3, int a4, int a5)
{
  fprintf(stderr,"ERROR: ");
  fprintf(stderr,s,al,a2,a3,a4,a5);
  fprintf(stderr,"\n");
  exit(2);
}

int error(char *s)
{
  fprintf(stderr,"ERROR: ");
  fprintf(stderr,s);
  fprintf(stderr,"\n");
  exit(2);
}

/**
 * Forward declaration
 */
int step_4();

/*********************************************************************************************
 *
 * Procedures for dynamic storage allocation of record space.
 *
 *
 * The space allocation (i.e the interaction with UNIX memory)
 * allocation can be done in a more efficient way.
 * Also system interaction should be isolated
 * to make porting easier.
 *
 *
 **********************************************************************************************/

struct element *make_element_node()
{
  int i;
  struct element *el;
  if (!(el = (struct element *) malloc(sizeof(struct element))))
	error ("make_element_node: no available memory");
  el->next = el->prev = NULL;
  el->in_edges = (struct edge**) malloc(NBROFEVENTS*sizeof(struct edge*));
  for (i=0; i<NBROFEVENTS; i++)
	el->in_edges[i] = NULL;
  el->my_qblock = NULL;
  el->cp = NULL;
  el->mark_3 = el->mark_5 = 0;
  el->E_1_next = NULL;
  el->bprime_next = NULL;
  return(el);
} /* make_element_node */

struct edge *make_edge_node()
{
  struct edge *ed;
  if (!(ed = (struct edge *) malloc(sizeof(struct edge))))
	error("make_edge_node: no available memory");
  ed->from_element = NULL;
  ed->in_edges = NULL;
  ed->cp = NULL;
  return(ed);
} /* make_edge_node */

struct qblock *make_qblock_node()
{
  int i;
  struct qblock *qb;
  if (! (qb = (struct qblock *) malloc(sizeof(struct qblock))))
	error ("make_qblock_node: no available memory");
  qb->n_elements = 0;
  qb->q_next = qb->q_prev = qb->q_tmp = NULL;
  qb->splitl = NULL;
  qb->first = NULL;
  qb->my_x_block = (struct xblock**) malloc(NBROFEVENTS*sizeof(struct xblock*));
  qb->x_next = (struct qblock**) malloc(NBROFEVENTS*sizeof(struct qblock*));
  qb->x_prev= (struct qblock**) malloc(NBROFEVENTS*sizeof(struct qblock*));
  for (i=0; i<NBROFEVENTS; i++) {
	qb->x_next[i] = qb->x_prev[i] = NULL;
	qb->my_x_block[i] = NULL;
  }
  return(qb);
} /* make_qblock_node */

struct xblock *make_xblock_node()
{
  struct xblock *xb;
  if (!(xb = (struct xblock *) malloc(sizeof(struct xblock))))
	error("make_xblock_node: no available memory");
  // xb->relation = NULL;
  xb->relation = -1; // HUGO
  xb->q_first = NULL;
  xb->next = xb->prev = NULL;
  return(xb);
} /* make_xblock_node */

struct count *make_count_node()
{
  struct count *cp;
  if (! (cp = (struct count *) malloc(sizeof(struct count))))
	error("make_count_node: no available memory");
  cp->counter = 1;
  return(cp);
} /* make_count_node */

/***************************************************************************************
 *
 * Procedures implementing the
 * Strong Bisimulation Equivalence Algorithm
 *
 ********************************************************************************************/

/*********************************************************************************************
 *
 * Transformation Algorithm
 *
 ********************************************************************************************/

/*
  This procedure refines the initial partition P into a new partition P' such that
  for each block in P' either:

  Forall i in I . |E_i({x})| >= 1 for all elements x in the block
  or
  For all i in I. |E'i({x})| = 0 for all elements x in the block

  This is implemented as follows:

  For each relation, i, (that is for all X blocks in the initial C-list):

  1. For every Q block node q in the list pointed to by Q:
  For every edge node e in the list pointed to by
  in_edges [i] in the element node y
  1. Find the start element, x, of the edge.
  2. Add x to the list E 1 B {if it has not
  already been added):

  2. Run step_4 of the main algorithm.

*/

int transform()
{
  struct xblock *curr_xb = C;
  struct qblock *curr_qb;
  struct edge *ed;
  struct element *curr_el, *el_1;

  /* Scan through all X blocks (i.e. all relations)  */

  while (curr_xb != NULL)
  {
	E_1_B = NULL;

	/* Save number of current relation */
	rel = curr_xb->relation;

	/* Scan all elements in Q */
	curr_qb = Q;
	while (curr_qb !=NULL)
	{
	  curr_el = curr_qb-> first;
	  while (curr_el != NULL)
	  {
		if ((ed = curr_el->in_edges[rel]) != NULL)
		{
		  if (E_1_B == NULL)
		  {
			/* First element needs special care */

			/* Add element to E_1(B) */
			E_1_B = el_1 = ed->from_element;
			/* Mark element as part of E 1 (B) */
			el_1->mark_3 = 1;
			/* tag set to 0 in step 7.
			 */
		  }
		  else
		  {
			/* Avoid duplicates */
			if (!ed->from_element->mark_3) {
			  el_1->E_1_next = ed->from_element; /* Add element to E_1(B) */
			  el_1 = ed->from_element;
			  /* Mark element as part of E_1(B) */
			  el_1->mark_3 = 1;
			}

		  }
		  /* More edges incident to this
			 element? */
		  while ((ed = ed->in_edges) != NULL) {

			/* Avoid duplicates */
			if (!ed->from_element->mark_3) {
			  el_1->E_1_next = ed->from_element;
			  el_1 = ed->from_element;
			  /* Mark element as part of E_1(B) */
			  el_1->mark_3 = 1;
			}

		  }

		  el_1->E_1_next = NULL;

		}

		curr_el = curr_el->next;
	  }
	  curr_qb = curr_qb->q_next;
	}

	/* Now partition (Q only) with
	   respect to the elements in
	   E 1 B
	*/
	step_4();


	/* Reset tag-fields in all
	   elements of Q
	*/
	curr_qb = Q;
	while (curr_qb !=NULL) {
	  curr_el = curr_qb->first;
	  while (curr_el != NULL) {
		curr_el->mark_3 = 0;
		curr_el = curr_el->next;
	  }
	  curr_qb = curr_qb->q_next;
	}


	curr_xb = curr_xb->next;
  }
  return 0;
} /* transform */


/***********************************************************************************************
 *
 * Generalized Relational Coarsest Partition Algorithm
 *
 ***********************************************************************************************/

/*
  Step 1: Find a block S in X and a
  refinement block B in 0 such that B is a subset of S
  Step 2: Replace S in X by S-B and B
  This procedure selects a block S from the set C.
  Take the first X block node in
  the doubly linked list C as the block S.
  The number of the relation on which to partition (stored in the
  X block node) is saved in the global variable 'reI'. From the
  X block the smallest (least number of elements) of the two first Q blocks will be taken as the refinement block B. If the remaining block S-B is still compound, leave it in C. Otherwise remove S-B from C. Delete it's associated X block node.
*/

int step_1_2()
{
  struct xblock *xb; struct qblock *q1,*q2;
  if ((xb = C) == NULL)   /* Get first block in set C */
	error("step_1:No X block");

  rel = xb->relation;   /* Save number of relation on
						   which to partition
						   (Needed later..)	*/
  if ((q1 = xb->q_first) == NULL) 	/* Get pointers to two first
									   Q blocks in current X block */
	error("step_1:No Q block in X block");
  if ((q2 = q1->x_next[rel]) == NULL)
 	error("step_l:X block not compound");
  /* Choose the smallest of the two first Q blocks to be our refinement block B */
  B = (q1->n_elements < q2->n_elements) ? q1 : q2 ;
  /* Remove B from the X block */
  if (B==q1) {
	xb->q_first = q2;
	q2->x_prev[rel] = NULL;
  }
  else {
 	q1->x_next[rel] = q2->x_next[rel];
	if (q2->x_next[rel] != NULL)
	  q2->x_next[rel]->x_prev[rel] = q1;
  };

  B->x_next [rel] = NULL;
  B->x_prev[rel] = NULL;
  B->my_x_block[rel] = NULL;

  if (xb->q_first->x_next[rel] == NULL) {
	                                     /* S-B has become simple, remove
											S-B from C. Delete the
											X block */
	if ((C = xb->next) != NULL)          /* C now points to next X block */
	  C->prev = NULL;
	xb->q_first->my_x_block[rel] = NULL; /* The block now
											has no associated
											X block, i.e. not in set C */
	xb->q_first->x_next[rel] = xb->q_first->x_prev[rel] = NULL;

	/* Here the X block pointed to by xb can be DELETED */
  }

  return 0;
} /* step_1_2 */


  /*
   *
   *    Step 3: Find the set of elements in U having
   * i-labelled edges incident to B
   *
   * 1. Copy elements in B to a temporary set pointed to by
   * 'Bprime'. This is done by duplicating the pointers linking the
   * elements of the Q block B together into a new set of pointers
   * (stored in the element records. , Bprime' points to the first
   * element in the set B.
   *
   * 2. E l(B) is calculated. This is done by scanning all the elements
   * in B and, for each element y in B, adding all elements x having
   * i-labelled edges incident to y into the linked list pointed to by
   * (the global variable) E 1 B.  Duplicates are suppressed by-setting
   * a mark ('mark_3') in x. This mark is reset in step 7.
   *
   * During the same scan, a count node is hooked onto each element x
   * incident to B. The count will be (after the scan) the number of
   * edges from x that points into B.
   *
   */
int step_3()
{
  struct element *elb,*el_1;
  struct edge *ed;
  if (B->first==NULL)
	error("step 3: Block B has no elements");

  Bprime = elb = B->first;
  E_1_B = NULL;
  do {
	/* 'do':Scan the list of elements in
	   B once only */

	/* Copy elements of B into temporary set Bprime (by copying pointers) */

	elb->bprime_next = elb->next;

	/* Computation of E_1(B) */

	if ((ed = elb->in_edges[rel]) != NULL) {
	  if (E_1_B == NULL) {	/* First element needs special care */

		E_1_B = el_1 = ed->from_element; /* Add element to E_l(B) */
		/* Mark element as part of E_1(B) */
		el_1->mark_3 = 1;
		/* tag set to 0 in step 7. */
		el_1->cp = make_count_node();
	  }
	  else {
		/* Avoid duplicates */
		if (!ed->from_element->mark_3) {
		  el_1->E_1_next = ed->from_element; /* Add element to E l(B) */
		  el_1 = ed->from_element;
		  /* Mark element as part of E_1(B} */
		  el_1->mark_3 = 1;
		  el_1->cp = make_count_node();
		}
		else
		  (ed->from_element->cp->counter)++;
	  }
	  /* More edges incident to this
		 element? */


	  while ((ed = ed->in_edges) != NULL) {

		/* Avoid duplicates */
		if (!ed->from_element->mark_3) {
		  el_1->E_1_next = ed->from_element;
		  el_1 = ed->from_element;
		  /* Mark element as part of E_l(B) */
		  el_1->mark_3 = 1;
		  el_1->cp = make_count_node();
		}
		else
		  (ed->from_element->cp->counter)++;
	  }

	  el_1->E_1_next = NULL;

	}

	/* Process next element in B */
  } while ((elb = elb->next) != NULL);

  return 0;
} /* step_3 */


  /*
   * Step 4: Replace Q by sp1it_i(B,Q)
   *
   * 1. For each block D of Q containing some elements of E_l(B)
   * split D into D := D intersection E l(B) and
   * D':= D - (D intersection E l(B)) .-Do this by scanning the
   * elements pointed to by E 1 B. To process an element x in
   * E 1 B, determine the block-D of Q containing it, and create
   * an associated block D' (pointed to by D) if it does not already exist.
   * Move x from D to D'. Let the field 'my_x_block' in x
   * point to D'.
   * During the scan, construct a list of the blocks that are split.
   * Let 'splitl' point to this list.
   *
   *
   *
   * 2. For each Q block node D in the list pointed to by 'sp1itl'
   *
   * 1. Let D' be a new block in Q.
   *
   * 2. Update the partitions X'i:
   *
   * 1. If D is empty, remove D from Q.
   *
   * 2. For all X'i partitions <= Last_rel do
   *
   * If DUD' was part of a compound block T in X'i then
   * If D empty then
   * remove D from T
   * Put D' in T
   * Else (if DUD' was NOT part of a compound block) then
   * If D is not empty
   * Construct a new X'i block called T
   * Link T into C
   * Put D and D' as new Q blocks in T
   *
   *
   */
int step_4()
{
  struct element *curr_el = E_1_B;
  struct qblock *splitl = NULL;
  struct qblock *qbc,*sp_1_c, *qbD,*qbDp,*q1,*q2;
  struct element *t1,*t2;
  struct xblock *xbc;
  int i;

  /* Scan all elements in E_l(B).
	 For each element: move it from
	 it's block D to an associated
	 block D' */

  while (curr_el != NULL) {

	qbc = curr_el->my_qblock;
	if (qbc->q_tmp == NULL) {

	  /* No associated block D',
		 create it */
	  qbc->q_tmp = make_qblock_node();

	  /* link D into list of splitted
		 blocks */
	  /* First block special care */
	  if (splitl == NULL) {
		splitl = sp_1_c = curr_el->my_qblock;
		sp_1_c->splitl = NULL;
	  }
	  else {
		sp_1_c->splitl = curr_el->my_qblock;
		sp_1_c = curr_el->my_qblock;
		sp_1_c->splitl = NULL;
	  }

	}

	/* Remove current element from
	   D */
	if ((t1=curr_el->next) != NULL)
	  t1->prev = curr_el->prev;
	if ((t2 = curr_el->prev) != NULL) {
	  t2->next = t1;
	}
	else
	  qbc->first = t1;

	/* Put current element first in D' */
	if ((curr_el->next = qbc->q_tmp->first) != NULL)
	  curr_el->next->prev = curr_el;
	curr_el->prev = NULL;
	qbc->q_tmp->first = curr_el;


	curr_el->my_qblock = qbc->q_tmp;

	++(qbc->q_tmp->n_elements);
	--(qbc->n_elements);

	curr_el= curr_el->E_1_next;

  }

  while (splitl != NULL) {

	qbD = splitl;
	qbDp = splitl->q_tmp;

	qbD->q_tmp = qbDp->q_tmp = NULL;
	/* Put D' first in the set Q */
	if ((qbDp->q_next = Q) != NULL)
	  Q->q_prev = qbDp;
	qbDp->q_prev = NULL;
	Q = qbDp;

	if (qbD->first == NULL) {
	  /* D is empty. Remove D from Q */
	  if ((q1 = qbD->q_next) != NULL)
		q1->q_prev = qbD->q_prev;
	  if ((q2=qbD->q_prev) != NULL)
		q2->q_next = q1;
	  else
		Q = q1;
	}

	/* Scan all relations (i.e. update each
	   X'i separately) */


	for (i=0 ; i<NBROFEVENTS; i++) {

	  qbDp->my_x_block[i] = qbD->my_x_block[i];

	  if (qbD->my_x_block[i] != NULL) {
		/* DUD' is part of a compound
		   block T in X'i
		*/

		if (qbD->first == NULL) {
		  /* D is empty. Remove D from T */

		  if ((q1=qbD->x_next[i]) != NULL)
			q1->x_prev[i] = qbD->x_prev[i];
		  if ((q2=qbD->x_prev[i]) != NULL)
			q2->x_next[i] = q1;
		  else
			qbD->my_x_block[i]->q_first = q1;

		}

		/* Link D' into T */

		if ((qbDp->x_next[i] = qbDp->my_x_block[i]->q_first) != NULL)
		  /* The-'if' here is not really needed,
			 q_first must point to a Q block
			 as we know D was part of a compund
			 block */
		  qbDp->my_x_block[i]->q_first->x_prev[i] = qbDp;
		qbDp->x_prev[i] = NULL;
		qbDp->my_x_block[i]->q_first = qbDp;

	  }
	  else {
		/* DUD' is NOT part of a compound
		   block in X'i */

		/* if D is empty nothing needs to
		   be done about D' (i.e. D' will
		   still not be a compound block
		   of X'i and should not be put
		   into C
		*/

		if (qbD->first != NULL) {

		  /* D not empty so DUD' is now
			 a compound block of X'i.
			 Construct a new X block T that
			 is a linked into C. Put D and
			 D' as the Q blocks of T

		  */

		  xbc = make_xblock_node();
		  xbc->relation = i;
		  qbD->my_x_block[i] = qbDp->my_x_block[i] = xbc;
		  xbc->q_first = qbD;
		  qbD->x_prev[i] = NULL;
		  qbD->x_next[i] = qbDp;
		  qbDp->x_next[i] = NULL;
		  qbDp->x_prev[i] = qbD;

		  if ((xbc->next = C) != NULL)
			C->prev = xbc;
		  xbc->prev = NULL;
		  C = xbc;

		}
	  }
	}

	splitl = splitl->splitl;
  }


  /* HERE DISPOSE of qblock pointed to by qbD is possible */



  return 0;
} /* step_4 */

/*
 *
 * Step 5: Find the set of elements in U having edges
 * incident to B and not edges incident to S-B.
 *
 * Step 5 is performed similar to step 4.
 * The same global pointer ('E_1_B') is used.
 *
 *
 * Scan all the elements in , Bprime' (constructed in
 * step 3). This list points to the same elements as B (but B can't be used as B might have been splitted in step 4).
 *
 *
 *
 * For each element y in Bprime, add all elements, x
 * having i-labelled edges incident to
 * y and for which count(x,S) = count(x,B).
 * The edge node representing the edge (x,y) in E_i points to count(x,S). The element node x points to count (x, B) .
 * Duplicates are suppressed by setting a mark ('mark_5') in x. This mark is reset in step 7.
 *
 *
 */

int step_5()
{
  struct element *elb,*el_1;
  struct edge *ed;
  E_1_B = NULL;

  if (Bprime == NULL)
	error("step 5: Bprime empty");
  elb = Bprime;
  do {
	if ((ed = elb->in_edges[rel]) != NULL) {
	  if (ed->from_element->cp->counter == ed->cp->counter) {
		if (E_1_B == NULL) {
		  /* First element needs special
			 care */
		  /* Add element to E_1(B) */
		  E_1_B = el_1 = ed->from_element;
		  el_1->E_1_next = NULL;

		  /* Mark element as part of E_l(B) */
		  /* mark_5 set to 0 in step 7 */
		  el_1->mark_5 = 1;
		}
		else {

		  /* Avoid duplicates */

		  if (!ed->from_element->mark_5) {
			el_1->E_1_next = ed->from_element;
			el_1 = ed->from_element;
			el_1->E_1_next = NULL;
			el_1->mark_5 = 1;
		  }
		}
	  }

	  /* More edges incident to this
		 element? */
	  while ((ed = ed->in_edges) != NULL) {

		if (ed->from_element->cp->counter == ed->cp->counter) {
		  if (E_1_B == NULL) {
			/* First element needs special
			   care */
			/* Add element to E l(B) */
			E_1_B = el_1 = ed->from_element;


			el_1->E_1_next = NULL;
			/* Mark element as part of E 1(B) */
			/* mark_5 set to 0 in step 7 */
			el_1->mark_5 = 1;
		  }
		  else
			/* Avoid duplicates */
			if (!ed->from_element->mark_5) {
			  el_1->E_1_next = ed->from_element;
			  el_1 = ed->from_element;
			  el_1->E_1_next = NULL;
			  el_1->mark_5 = 1;
			}
		}
	  }
	}

	/* Process next element in B */
  } while ((elb = elb->bprime_next) != NULL);


  return 0;
} /* step_5 */

/*
 * Step 6: Replace Q by split_i(S-B,Q}
 *
 * (Same as step 4)
 *
 */


/* Step 7: Update counts
 *
 * This step updates the count-nodes and set's the tag-fields
 * (mark_3,mark_5) to 0. The update of the count-
 * nodes will be done in the following way (different from the
 * way described in the article, as I am not sure the way described in
 * the article will work):
 *
 * Scan the edges x E_i y such that y is in B'.
 * To process an edge x E i y,
 * decrement count (x,S) (to which x E i y points).
 * If the count becomes zero, delete the count-record.
 * Then make the edge point to count(x,B) (to which x points).
 * (i.e. not only the edges where count becomes zero as written
 * in the article)
 */

int step_7()
{

  struct element *elb,*el_1;
  struct edge *ed;


  elb = Bprime;


  do {

	if ((ed = elb->in_edges[rel]) != NULL) {
	  --(ed->cp->counter) ;
	  /* if counter = 0 after this step, the record can be DELETED */
	  ed->cp = ed->from_element->cp;
	  /* Necessary cleanup before next
		   refinement step */
	  ed->from_element->mark_3=ed->from_element->mark_5=0;

	  while ((ed = ed->in_edges) != NULL) {
		--(ed->cp->counter);
		/* if counter = 0 after this step, the record can be DELETED */
		ed->cp = ed->from_element->cp;
		ed->from_element->mark_3=ed->from_element->mark_5=0;
	  }
	}

	/* Process next element in B */

  } while ((elb = elb->bprime_next) != NULL);

  return 0;
} /* step_7 */


/*
 * grcp
 *
 * This is the actual loop repeating the refinement steps 1 to 7
 * until the list C of compound X blocks becomes empty
 * (i.e. for every i in I. X_i = 0).
 *
 */
int grcp()
{
  if (Q == NULL)
	error("partition: No Q block !");
  if (Q -> q_next == NULL)
	return 0;
	// If there initially is only one Q block
	// then, for every i in I.
	// Q = X_i ,i.e. , all processes
	// are bisimulation equivalent

  while (C != NULL) {

	Iteration++;

	step_1_2();
	step_3();
	step_4();
	step_5() ;
	step_4(); /* step 6 is same as step 4 */
	step_7();

  }
  return 0;
} /* grcp */

///////////////////////////////////////////
// Functions called by Java through JNI. //
///////////////////////////////////////////

/**
 * Initializes the partitioning stuff using the supplied information.
 */
JNIEXPORT void JNICALL Java_org_supremica_automata_algorithms_minimization_BisimulationEquivalenceMinimizer_initialize(JNIEnv* env, jclass obj, jint nbrOfStates, jint nbrOfEvents, jint nbrOfTransitions, jintArray initialPartitioning, jintArray transitions)
{
  NBROFSTATES = (int) nbrOfStates;
  NBROFEVENTS = (int) nbrOfEvents;
  NBROFTRANSITIONS = (int) nbrOfTransitions;
  int NBROFINITIALPARTITIONS = 1 + env->GetArrayLength(initialPartitioning) - nbrOfStates;

  //printf("States: %i, events: %i, transitions: %i, partitions: %i\n", NBROFSTATES, NBROFEVENTS, NBROFTRANSITIONS, NBROFINITIALPARTITIONS);

  // Assign initial partitioning
  // Get a c-array with the values
  int *INITIALPARTITIONING = (int *) env->GetIntArrayElements(initialPartitioning, NULL);

  /*
  int i;
  for(i=0; i<NBROFSTATES; i++)
	printf("State: %i ", INITIALPARTITIONING[i]);
  printf("\n");
  */

  // Assign transition array
  // Get a c-array with the values
  int *TRANSITIONS = (int *) env->GetIntArrayElements(transitions, NULL);

  /*
  for(i=0; i<NBROFTRANSITIONS*3; )
  {
	printf("Trans: <%i,%i,%i> ", TRANSITIONS[i], TRANSITIONS[i+1], TRANSITIONS[i+2]);
	i += 3;
  }
  printf("\n");
  */

  /*********************************************************************************************
   *
   * The rest of the code derives from input()
   *
   * Input of start partition and
   * creation of the data-structure on which to partition.
   *
   *********************************************************************************************/

  int n=0;
  int i=0, j=0;
  struct element *el,*elp;
  struct qblock *qb,*qbp;
  struct xblock *xbc,*xbp;
  struct edge *ed;
  int stateindex;
  int pos;

  // Allocate memory for xblocks, one per event
  xblock** xblocks;
  if (!(xblocks = (xblock**) malloc((NBROFEVENTS)*sizeof(xblock*))))
    error("make_xblocks: no available memory");
  // Allocate memory for elements, one per state
  element** elements;
  if (!(elements = (element**) malloc((NBROFSTATES)*sizeof(element*))))
    error("make_elements: no available memory");
  // Allocate memory for counts, one per state-event combo
  count** counts;
  if (!(counts = (count**) malloc((NBROFSTATES*NBROFEVENTS)*sizeof(count*))))
    error("make_counts: no available memory");
  for (i=0; i<NBROFSTATES*NBROFEVENTS; i++)
  	counts[i] = NULL;

  /*
	Make a chain of X blocks (size NBROFEVENTS+1).
	Make C point to first X block. Give each
	X block number 1,2..NBROFEVENTS
	Each X block will contain all the Q blocks
	at initialization.

	We make rel_tab point to the X blocks to
	make the building of the Q blocks easier
  */
  C = xbp = make_xblock_node();
  C->relation = 0;
  xblocks[0] = xbp;
  for (i=1; i<NBROFEVENTS; i++) {
	xblocks[i] = xbc = make_xblock_node();
	xbc->relation = i;
	xbp->next = xbc;
	xbc->prev = xbp;
	xbp = xbc;
  }

  /* First get the initial partitions! */

  pos = 0;
  while ((pos != NBROFSTATES+NBROFINITIALPARTITIONS-1) && ((stateindex = INITIALPARTITIONING[pos++]) != -1))
  {
	// Start of a new Q block
	qb = make_qblock_node();

	if (xblocks[0]->q_first == NULL)
	{
	  // First Q block
	  Q = qbp = qb;
	  for (i=0 ; i<NBROFEVENTS; i++) {
		xblocks[i]->q_first = qb;
		qb->my_x_block[i] = xblocks[i];
	  }
	}
	else
	{
	  // Put new qblock last in list of Q blocks
	  // and last in list of Q blocks for this
	  // X block
	  qbp->q_next = qb;
	  qb->q_prev = qbp;
	  for (i = 0 ; i < NBROFEVENTS; i++) {
		qbp->x_next[i] = qb;
		qb->x_prev[i] = qbp;
		qb->my_x_block[i] = xblocks[i];
	  }
	  qbp = qb;
	}

	// First element in the qblock
	el = elp = make_element_node();
	qb->first = el;
	qb->n_elements = 1;
	el->index = stateindex;
	el->my_qblock = qb;

	// Put element in symbol table for later use
	// when scanning edges
	elements[stateindex] = el;

	// Get rest of elements
	while ((pos != NBROFSTATES+NBROFINITIALPARTITIONS-1) && ((stateindex = INITIALPARTITIONING[pos++]) != -1))
	{
	  el = make_element_node();
	  elp->next = el;
	  el->prev = elp;
	  elp = el;
	  qb->n_elements++;
	  el->index = stateindex;
	  el->my_qblock = qb;

	  elements[stateindex] = el;
	  if (stateindex==NBROFSTATES)
		error("Initialize: Wrong number of states");
	}
  }

  // Now get the edges!
  pos = 0;
  while (pos < NBROFTRANSITIONS*3)
  {
	int from_state = TRANSITIONS[pos++];
	int event = TRANSITIONS[pos++];
	int to_state = TRANSITIONS[pos++];

	// Make new edge node
	ed = make_edge_node();

	// From-state stuff
	// Make edge point to 'from_element'
	ed->from_element = elements[from_state];
	// Create a count node if no one exists Make edge point to
	// count-node Increment count node
	if (counts[from_state*NBROFEVENTS+event] == NULL)
	{
	  ed->cp = counts[from_state*NBROFEVENTS+event] = make_count_node();
	}
	else
	{
	  counts[from_state*NBROFEVENTS+event]->counter++;
	  ed->cp = counts[from_state*NBROFEVENTS+event];
	}

	// To-state stuff
	// Put edge first in list of incident
	// edges to 'to_element'
	ed->in_edges = elements[to_state]->in_edges[event];
	elements[to_state]->in_edges[event] = ed;
  }

	/*
  // DEBUG
	for (i=0; i<NBROFSTATES; i++)
	{
		printf("State: %i (%i)", i, NBROFSTATES);
		int j;
	  	for (j=0; j<NBROFEVENTS; j++)
	  	{
			if(counts[i*NBROFEVENTS+j] != NULL)
		  		printf(" count: %i (ev: %i)", counts[i*NBROFEVENTS+j]->counter, j);
		}
		printf("\n");
	  	for (j=0; j<NBROFEVENTS; j++)
	  	{
	  		edge *ed = elements[i]->in_edges[j];
			while (ed != NULL)
			{
				printf(" From %i (ev: %i)\n", ed->from_element->index, j);
				ed = ed->in_edges;
			}
		}
	}
	*/
}

/**
 * Does the partitioning.
 */
JNIEXPORT void JNICALL Java_org_supremica_automata_algorithms_minimization_BisimulationEquivalenceMinimizer_partition(JNIEnv* env, jclass obj)
{
  transform();
  grcp();
}

/**
 * Generates an int-array with indices to states in equivalence classes, separated by -1 elements.
 * Returns a java int-array.
 */
JNIEXPORT jintArray JNICALL Java_org_supremica_automata_algorithms_minimization_BisimulationEquivalenceMinimizer_getPartitioning(JNIEnv* env, jclass obj)
{
  jintArray jResult;

  // Count final number of partitions
  int nbrOfPartitions = 0;
  struct qblock *qb = Q;
  int length;
  int index;
  int *result;

  // Count the number of partitions
  while (qb != NULL)
  {
	nbrOfPartitions++;
	qb = qb->q_next;
  }

  // Fill result array
  length = NBROFSTATES+nbrOfPartitions-1;
  result = (int *) malloc(length*sizeof(int));
  struct element *el;
  index = 0;
  qb = Q; // The first partition
  while (qb != NULL) {
	el = qb->first;
	while (el != NULL)
	{
	  result[index++] = el->index;
	  el = el->next;
	}
	if (index != length)
	  result[index++] = -1;
	qb = qb->q_next;
  }

  jResult = env->NewIntArray(length);
  env->SetIntArrayRegion(jResult, 0, length, (jint *)result);

  // Free memory!
  // QBLOCKS
  struct qblock *q;
  struct qblock *qnext;
  for (q=Q; q!=NULL; q=qnext)
  {
	// ELEMENTS
	struct element *el;
	struct element *elnext;
	for (el=q->first; el!=NULL; el=elnext)
	{
	  // EDGES
	  int i;
	  struct edge *ed;
	  struct edge *ednext;
	  for (i=0; i<NBROFEVENTS; i++)
	  {
		//struct edge *ed = el->in_edges[i];
		for (ed=el->in_edges[i]; ed!=NULL; ed=ednext)
		{
		  ednext = ed->in_edges;
		  /*
		  // COUNT
		  if (ed->cp != NULL)
			free(ed->cp);
		  */
		  free(ed);
		}
	  }
	  elnext = el->next;
	  // COUNT
	  if (el->cp != NULL)
		free(el->cp);
	  free(el->in_edges);
	  free(el);
	}
	qnext = q->q_next;
	free(q->my_x_block);
	free(q->x_next);
	free(q->x_prev);
	free(q);
  }
  // XBLOCKS
  struct xblock *x;
  struct xblock *xnext;
  for (x=C; x!=NULL; x=xnext)
  {
	xnext = x->next;
	free(x);
  }

  // Reset constants
  E_1_B = NULL;
  Bprime = NULL;
  Iteration = 0;

  return jResult;
}
