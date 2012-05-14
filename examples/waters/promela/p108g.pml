/*                                     
 * A test for the Promela importer in Waters.
 * This checks whether a channel of enumerative type can be combined with
 * multiple senders and recipients.
 * 
 * This one fails with an error
 */

mtype { TRUE, FALSE }

chan ch = [0] of { byte, mtype };

proctype A()
{
        ch!0(TRUE);
        ch!0(TRUE);
}

proctype B()
{
        mtype x;
        ch?0(x);
	ch?0(x);
}

init
{
        run A();
        run A();
        run B();
        run B()
}
