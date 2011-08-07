/*                                     
 * A test for the Promela importer in Waters.
 * This checks whether a channel of enumerative type can be combined with
 * multiple senders and recipients.
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
        ch?x;
	ch?x;
}

init
{
        run A();
        run A();
        run B();
        run B()
}
