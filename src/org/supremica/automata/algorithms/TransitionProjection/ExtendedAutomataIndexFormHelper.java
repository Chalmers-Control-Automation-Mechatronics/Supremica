
package org.supremica.automata.algorithms.TransitionProjection;

import gnu.trove.TIntHashSet;
import java.util.StringTokenizer;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.util.Args;

/**
 * ExtendedAutomataIndexFormHelper is the class to help using ExtendedAutomataIndexForm. This class is an adopted version of
 * AutomataIndexFormHelper to support Extended Finite Automata.
 *
 * @author Mohammad Reza Shoaei (shoaei@chalmers.se)
 * @version %I%, %G%
 * @since 1.0
 */
public class ExtendedAutomataIndexFormHelper {

    @SuppressWarnings("unused")
    private final Logger logger = LoggerFactory.createLogger(ExtendedAutomataIndexFormHelper.class);

    public static final int STATE_EXTRA_DATA = 2;
    public static final int STATE_STATUS_FROM_END = 1;
    public static final int STATE_PREVSTATE_FROM_END = 2;
    public static final int STATE_NO_PREVSTATE = -1;

    /**
     * Allocate a new state + extra data fields
     */
    public static int[] createState(final int nbrOfAutomata)
    {
        final int[] newState = new int[nbrOfAutomata + STATE_EXTRA_DATA];

        newState[nbrOfAutomata + STATE_EXTRA_DATA - STATE_PREVSTATE_FROM_END] = STATE_NO_PREVSTATE;

        return newState;
    }

    /**
     * Create a copy of an existing state
     */
    public static int[] createCopyOfState(final int[] state)
    {
        final int[] newState = new int[state.length];

        System.arraycopy(state, 0, newState, 0, state.length);

        return newState;
    }

    /**
     * Set the previous state index of an existing state
     */
    public static void setPrevStateIndex(final int[] state, final int stateIndex)
    {
        state[state.length - STATE_PREVSTATE_FROM_END] = stateIndex;
    }

    /**
     * Get the previous state index
     */
    public static int getPrevStateIndex(final int[] state)
    {
        return state[state.length - STATE_PREVSTATE_FROM_END];
    }

    /**
     * bit
     * 0: initial
     * 1: accepting
     * 2: forbidden
     * 3: first
     * 4: last
     * 5: fastClearStatus
     * 6: deadlocked
     **/
    public static int createStatus(final boolean isInitial, final boolean isAccepted, final boolean isForbidden)
    {
        int status = 0;

        if (isInitial)
        {
            status |= 1;
        }

        if (isAccepted)
        {
            status |= (1 << 1);
        }

        if (isForbidden)
        {
            status |= (1 << 2);
        }

//        if (state.isFirst())
//        {
//            status |= (1 << 3);
//        }
//
//        if (state.isLast())
//        {
//            status |= (1 << 4);
//        }

        return status;
    }

    public static boolean isInitial(final int status)
    {
        return (status & 1) == 1;
    }

    public static boolean isAccepting(final int status)
    {
        return ((status >> 1) & 1) == 1;
    }

    public static boolean isForbidden(final int status)
    {
        return ((status >> 2) & 1) == 1;
    }

//    public static boolean isFirst(int status)
//    {
//        return ((status >> 3) & 1) == 1;
//    }
//
//    public static boolean isLast(int status)
//    {
//        return ((status >> 4) & 1) == 1;
//    }
//
//    public static boolean isDeadlocked(int status)
//    {
//        return ((status >> 6) & 1) == 1;
//    }
//
//    public static boolean hasInitial(int[] state)
//    {
//        return isInitial(state[state.length - STATE_STATUS_FROM_END]);
//    }
//
//    public static boolean hasAccepting(int[] state)
//    {
//        return isAccepting(state[state.length - STATE_STATUS_FROM_END]);
//    }
//
//    public static boolean hasForbidden(int[] state)
//    {
//        return isForbidden(state[state.length - STATE_STATUS_FROM_END]);
//    }

    public static boolean hasValue(final int[] list, final int value){
        for(final int v : list)
            if(v == value)
                return true;
        return false;
    }
//    public static boolean isFirst(int[] state)
//    {
//        return isFirst(state[state.length - STATE_STATUS_FROM_END]);
//    }
//
//    public static boolean isLast(int[] state)
//    {
//        return isLast(state[state.length - STATE_STATUS_FROM_END]);
//    }
//
//    public static boolean isDeadlocked(int[] state)
//    {
//        return isDeadlocked(state[state.length - STATE_STATUS_FROM_END]);
//    }

    /**
     * Build a state from a string.
     * The string must be of the form
     * [state1 state2 state3 state4 state5 status]
     */
    public static int[] buildStateFromString(final String stringState)
    {
        final String trimmedString = stringState.trim();
        final int indexOfLeft = trimmedString.indexOf('[');

        if (indexOfLeft == -1)
        {
            return null;
        }

        final int indexOfRight = trimmedString.indexOf(']');

        if (indexOfRight == -1)
        {
            return null;
        }

        if (!(indexOfLeft < indexOfRight))
        {
            return null;
        }

        final StringTokenizer st = new StringTokenizer(trimmedString.substring(indexOfLeft + 1, indexOfRight));
        final int nbrOfTokens = st.countTokens();

        if (nbrOfTokens < 1)
        {
            return null;
        }

        final int[] newState = new int[nbrOfTokens];
        int i = 0;

        while (st.hasMoreTokens())
        {
            final String currToken = st.nextToken();

            try
            {
                final int tmpInt = Integer.parseInt(currToken);

                newState[i++] = tmpInt;
            }
            catch (final NumberFormatException ex)
            {

                // logger.debug(ex.getStackTrace());
                return null;
            }
        }

        if (i == nbrOfTokens)
        {
            return newState;
        }
        else
        {
            return null;
        }
    }

    public static String dumpState(final int[] state)
    {
        if (state == null)
        {
            return "[null]";
        }

        final StringBuilder sb = new StringBuilder("[");

        for (int i = 0; i < state.length; i++)
        {
            sb.append(state[i]);

            if (i != (state.length - 1))
            {
                sb.append(" ");
            }
        }

        sb.append("]");

        return sb.toString();
    }

    public static TIntHashSet toIntHashSet(final int[] array){
        Args.checkForNull(array);
        final TIntHashSet list = new TIntHashSet();
        for(final int element : array)
            if(element != Integer.MAX_VALUE)
                list.add(element);
        return list;
    }

    public static TIntHashSet getTrueIndexes(final boolean[] array){
        Args.checkForNull(array);
        final TIntHashSet trues = new TIntHashSet();
        for(int i=0; i<array.length; i++)
            if(array[i])
                trues.add(i);
        return trues;
    }

    public static TIntHashSet getFalseIndexes(final boolean[] array){
        Args.checkForNull(array);
        final TIntHashSet falses = new TIntHashSet();
        for(int i=0; i<array.length; i++)
            if(!array[i])
                falses.add(i);
        return falses;
    }

    public static int[] clearMaxInteger(final int[] array){
        Args.checkForNull(array);
        int[] temp = null;
        if(array.length == 1){
            if(array[0] != Integer.MAX_VALUE){
                temp = new int[]{array[0]};
            }
        } else {
        temp = new int[array.length - 1];
        int j = 0;
        for(int i = 0; i < array.length; i++)
            if(array[i] != Integer.MAX_VALUE)
                temp[j++] = array[i];
        }
        return temp;
    }

   public static TIntHashSet setUnion(final TIntHashSet x, final TIntHashSet y){
        Args.checkForNull(x);
        Args.checkForNull(y);
        final TIntHashSet result = new TIntHashSet();
        result.addAll(x.toArray());
        result.addAll(y.toArray());
        return result;
    }

    public static TIntHashSet setIntersection(final TIntHashSet x, final TIntHashSet y){
        if(x==null || y==null || x.isEmpty() || y.isEmpty())
            return new TIntHashSet();

        final TIntHashSet result = new TIntHashSet(x.toArray());
        result.retainAll(y.toArray());
        return result;
    }

    public static TIntHashSet setDifference(final TIntHashSet x, final TIntHashSet y){
        if(x == null || x.isEmpty())
            return new TIntHashSet();
        else if(y == null || y.isEmpty())
            return new TIntHashSet(x.toArray());

        final TIntHashSet result = new TIntHashSet();
        for (final int n : x.toArray())
            if(!y.contains(n))
                result.add(n);
        return result;
    }

    public static int[] addToBeginningOfArray(final int value, final int[] array){
        if(array == null){
            final int[] temp = new int[]{value};
            return temp;
        }
        final int[] temp = new int[array.length + 1];
        temp[0] = value;
        System.arraycopy(array, 0, temp, 1, array.length);
        return temp;
    }

    public static int[] addToEndOfArray(final int value, final int[] array){
        if(array == null){
            final int[] temp = new int[]{value};
            return temp;
        }
        final int[] temp = new int[array.length + 1];
        System.arraycopy(array, 0, temp, 0, array.length);
        temp[array.length] = value;
        return temp;
    }

    public static int[] generateCopy1DIntArray(final int[] oldArray)
    {
        final int[] newArray = new int[oldArray.length];

        System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);

        return newArray;
    }

    public static boolean[] generateCopy1DBooleanArray(final boolean[] oldArray)
    {
        final boolean[] newArray = new boolean[oldArray.length];

        System.arraycopy(oldArray, 0, newArray, 0, oldArray.length);

        return newArray;
    }

    public static boolean[][] generateCopy2DBooleanArray(final boolean[][] oldArray)
    {
        final boolean[][] newArray = new boolean[oldArray.length][];

        for (int i = 0; i < oldArray.length; i++)
        {
            newArray[i] = new boolean[oldArray[i].length];

            System.arraycopy(oldArray[i], 0, newArray[i], 0, oldArray[i].length);
        }

        return newArray;
    }

    public static int[][][] generateCopy3DIntArray(final int[][][] oldArray)
    {
        final int[][][] newArray = new int[oldArray.length][][];

        for (int i = 0; i < oldArray.length; i++)
        {
            newArray[i] = new int[oldArray[i].length][];

            for (int j = 0; j < oldArray[i].length; j++)
            {
                newArray[i][j] = new int[oldArray[i][j].length];

                System.arraycopy(oldArray[i][j], 0, newArray[i][j], 0, oldArray[i][j].length);
            }
        }

        return newArray;
    }

    public static int[][][][] generateCopy4DIntArray(final int[][][][] oldArray)
    {
        final int[][][][] newArray = new int[oldArray.length][][][];

        for (int i = 0; i < oldArray.length; i++)
        {
            newArray[i] = new int[oldArray[i].length][][];

            for (int j = 0; j < oldArray[i].length; j++)
            {
                newArray[i][j] = new int[oldArray[i][j].length][];

                for (int k = 0; k < oldArray[i][j].length; k++)
                {
                    if (oldArray[i][j][k] != null)
                    {

                        // This can be null, see prevStateTable
                        newArray[i][j][k] = new int[oldArray[i][j][k].length];

                        System.arraycopy(oldArray[i][j][k], 0, newArray[i][j][k], 0, oldArray[i][j][k].length);
                    }
                    else
                    {
                        newArray[i][j][k] = null;
                    }
                }
            }
        }

        return newArray;
    }

    public static int[][] generateCopy2DIntArray(final int[][] oldArray)
    {
        final int[][] newArray = new int[oldArray.length][];

        for (int i = 0; i < oldArray.length; i++)
        {
            newArray[i] = new int[oldArray[i].length];

            System.arraycopy(oldArray[i], 0, newArray[i], 0, oldArray[i].length);
        }

        return newArray;
    }

}
