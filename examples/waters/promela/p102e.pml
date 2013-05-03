/*            
 * A test for the Promela importer in Waters.
 * This tests whether the compiler can distinguish two channels      
 * of different length.
 */           

chan msg = [1] of { byte };
chan ack = [0] of { byte };

proctype producer()
{
  msg!5;
  ack?1;
}

proctype consumer()
{
  byte data;
  msg?data;
  ack!1;
}

init
{
  run producer();
  run consumer()
}
