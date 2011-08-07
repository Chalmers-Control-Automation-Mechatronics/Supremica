/*                                     
 * A test for the Promela importer in Waters.
 * This checks whether an mtype is recognised and converted to an enumeration
 * in combination with an asynchronous channel.
 */

mtype = { D0, D1, FV }

chan ch = [1] of { mtype };

proctype A()
{
  do
  :: ch!D0
  :: ch!D1
  :: ch!FV ; break
  od
}

proctype B()
{
  mtype data;
  ch?data;
}

init
{
  run A();
  run B()
}
