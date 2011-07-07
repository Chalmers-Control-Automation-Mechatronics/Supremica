/*                                     
 * A test for the Promela importer in Waters.
 * This tests whether the compiler can correctly generate message exchange
 * events when proctype communicates with itself.
 */

chan ch = [0] of { byte };

proctype A()
{
        do
	:: ch!1
	:: ch?1
	:: ch!2 -> break
	:: ch?2 -> break
        od
}

init
{
        run A();
        run A();
        run A()
}
