/*                                     
 * A test for the Promela importer in Waters.
 * This checks whether the compiler distinguish mtype constants and
 * variables in receive statements.
 */

mtype = { TRUE, FALSE }

chan ch = [0] of { mtype };

proctype A()
{
        ch!TRUE;
        ch!FALSE;
}

proctype B()
{
        byte x;
        ch?TRUE;
        ch?x;
}

init
{
        run A();
        run A();
        run B()
}
