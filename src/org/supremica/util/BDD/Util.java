package org.supremica.util.BDD;

public class Util
{

  /** shuffle/permute a list of integers */
  public static void permutate(final int[] list, final int size)
  {
    for (int i = 0; i < size; i++) {
      final int next = (int) (Math.random() * size);
      final int tmp = list[i];
      list[i] = list[next];
      list[next] = tmp;
    }
  }

  /** shuffle/permute a list of integers */
  public static int[] permutate(final int size)
  {
    final int[] x = new int[size];
    for (int i = 0; i < size; i++) {
      x[i] = i;
    }
    permutate(x, size);
    return x;
  }

  /** reverse some list */
  public static void reverse(final Object[] variables, final int size)
  {
    for (int j = 0; j < size / 2; j++) {
      final int i = size - j - 1;
      final Object tmp = variables[i];
      variables[i] = variables[j];
      variables[j] = tmp;
    }
  }

  /** reverse some int-array */
  public static void reverse(final int[] variables, final int size)
  {
    for (int j = 0; j < size / 2; j++) {
      final int i = size - j - 1;
      final int tmp = variables[i];
      variables[i] = variables[j];
      variables[j] = tmp;
    }
  }

  /** reverse some doube-array */
  public static void reverse(final double[] variables, final int size)
  {
    for (int j = 0; j < size / 2; j++) {
      final int i = size - j - 1;
      final double tmp = variables[i];
      variables[i] = variables[j];
      variables[j] = tmp;
    }
  }

}
