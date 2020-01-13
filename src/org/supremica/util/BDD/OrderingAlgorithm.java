package org.supremica.util.BDD;

public enum OrderingAlgorithm
{
  AO_HEURISTIC_RANDOM("Random (!)"),
  AO_HEURISTIC_PCG("PCG search"),
  AO_HEURISTIC_TSP("modified TSP"),
  AO_HEURISTIC_DFS("Topological sort (DFS)"),
  AO_HEURISTIC_BFS("Topological sort (BFS)"),
  AO_HEURISTIC_STCT("STCT: simulated annealing"),
  AO_HEURISTIC_TSP_STCT("TSP + STCT:SA "),
  AO_HEURISTIC_TSP_SIFT("TSP + sifting"),
  AO_HEURISTIC_FORCE("Aloul's FORCE"),
  AO_HEURISTIC_FORCE_WIN4("Aloul's FORCE + win4");

  private OrderingAlgorithm(final String name) {
    mName = name;
  }

  @Override
  public String toString()
  {
    return mName;
  }

  private final String mName;

}
