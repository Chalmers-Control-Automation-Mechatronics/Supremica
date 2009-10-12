package net.sourceforge.waters.analysis.distributed.safetyverifier;

class Util
{
  /**
   * Compute the integer base2 logarithm.
   */
  public static int clog2(int x)
  {
    x--;
    int y = 0;
    while (x > 0) 
      {
	x >>= 1;
	y++;
      }
    return y;
  }
}