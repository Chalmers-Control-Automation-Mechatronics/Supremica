/*                                     
 * A test for the Promela importer in Waters.
 * This tests whether the compiler can handle receive statements that
 * appear before matching send statements in the code.
 */

chan ch = [0] of { byte };

proctype A()
{
        ch!1;
}

proctype C()
{
        byte val;
        ch?val;
}

proctype B()
{
        ch!1;
}

init
{
        run A();
        run B();
        run C();
        run C()
}
