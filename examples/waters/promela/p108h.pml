/*                                     
 * A test for the Promela importer in Waters.
 * This checks whether a channel of enumerative type can be combined with
 * multiple senders and recipients.
 */

mtype { TRUE, FALSE }

chan ch = [0] of { mtype, byte };

proctype A()
{
        ch!TRUE(0);
        ch!FALSE(0);
}

proctype B()
{
        mtype x;
        ch?x(0);
	ch?x(0);
}

init
{
        run A();
        run A();
        run B();
        run B()
}
