/*                                     
 * A test for the Promela importer in Waters.
 * This tests whether step events are indexed for multiple instances of
 * a process.
 */

chan ch = [0] of { byte };

proctype A()
{
        do
	:: ch!1
	:: break
        od
}

proctype B()
{
        do
	:: ch?1
	:: break
        od
}
     
init
{
        run A();
        run A();
        run B()
}
