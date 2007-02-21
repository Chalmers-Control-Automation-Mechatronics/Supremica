package org.supremica.external.sag.automaton;

import java.util.*;

public enum Permutator {
	; // semicolon needed in enum

	// Prints permutations of the numbers in 1..value.length
	static void printPermutationsOfIntegers(int[] value, int level, int k) {
		++level;
		value[k] = level;

		if (level == value.length)
			printIntegerArray(value);
		else
			for (int i = 0; i < value.length; i++)
				if (value[i] == 0)
					printPermutationsOfIntegers(value, level, i);
		value[k] = 0;
	}

	static void printIntegerArray(int[] value) {
		for (int i = 0; i < value.length; ++i) {
			System.out.print(value[i]);
			System.out.print(" ");
		}
		System.out.println();
	}

	public static void main(String[] args) {

		// Print permutations of 1, 2, 3
		System.out.println("Permutations of 1, 2, 3");
		int N = 3;
		int[] value = new int[N];
		printPermutationsOfIntegers(value, -1, 0);
		System.out.println();

		// Print permutations of elements in set
		Set<String> elements = new TreeSet<String>();
		elements.add("apa");
		elements.add("bepa");
		elements.add("cepa");
		System.out.println("Permutations of " + elements.toString());
		Collection<List<String>> permutations = getPermutationsOf(elements);
		System.out.println(permutations);
		System.out.println();

		// Print permutations of length 2 of elements in set
		System.out.println("Permutations of length 2 of " + elements.toString());
		System.out.println(getPermutationsOf(elements, 2));
		System.out.println();

		// Print permutations of all lengths of elements in set
		// This corresponds to all possible combinations of elements in a queue of length = elements.size()
		System.out.println("Permutations of all lengths of " + elements.toString());
		List<Collection<List<String>>> permutationsOfAllLengths = new ArrayList<Collection<List<String>>>();
		for (int i = 0; i <= elements.size(); ++i) {
			permutationsOfAllLengths.add(getPermutationsOf(elements, i));
		}
		System.out.println(permutationsOfAllLengths);
		System.out.println();

	}

	// Returns all permutations of elements in collection
	public static <T> Collection<List<T>> getPermutationsOf(
			Collection<T> elements) {
		Collection<List<T>> permutations = new ArrayList<List<T>>();

		visit(new ArrayList<T>(elements), permutations, null, -2, 0);

		return permutations;
	}

	//recursive
	protected static <T> void visit(List<T> elements,
			Collection<List<T>> permutations, List<T> currentPermutation,
			int elementIndex, int permutationIndex) {
		++elementIndex;
		if (currentPermutation == null) {
			currentPermutation = new ArrayList<T>();
			for (int i = 0; i < elements.size(); ++i) {
				currentPermutation.add(null);
			}
		} else {
			currentPermutation
					.set(permutationIndex, elements.get(elementIndex));
		}
		if (elementIndex + 1 == elements.size()) {
			permutations.add(new ArrayList<T>(currentPermutation));
		} else {
			for (int i = 0; i < elements.size(); i++) {
				if (currentPermutation.get(i) == null) {
					visit(elements, permutations, currentPermutation,
							elementIndex, i);
				}
			}
		}
		currentPermutation.set(permutationIndex, null);
	}

	// Returns all permutations of a certain length for elements in collection
	public static <T> Collection<List<T>> getPermutationsOf(
			Collection<T> elements, int nrOfElementsInPerm) {
		Collection<List<T>> permutations = new ArrayList<List<T>>();

		List<T> currentPermutation = new ArrayList<T>();
		for (int i = 0; i < nrOfElementsInPerm; ++i) {
			currentPermutation.add(null);
		}

		genericPermLengthVisit(new ArrayList<T>(elements), permutations, currentPermutation,
				new boolean[elements.size()], 0, -2);

		return permutations;
	}

	//recursive
	protected static <T> void genericPermLengthVisit(List<T> elements,
			Collection<List<T>> permutations, List<T> currentPermutation,
			boolean[] elementIsInserted, int elementIndex, int permutationIndex) {
		++permutationIndex;
		if (permutationIndex >= 0) {
			currentPermutation
					.set(permutationIndex, elements.get(elementIndex));
			elementIsInserted[elementIndex] = true;
		}
		if (permutationIndex + 1 == currentPermutation.size()) {
			permutations.add(new ArrayList<T>(currentPermutation));
		} else {
			for (int i = 0; i < elements.size(); i++) {
				if (!elementIsInserted[i]) {
					genericPermLengthVisit(elements, permutations, currentPermutation,
							elementIsInserted, i, permutationIndex);
				}
			}
		}
		elementIsInserted[elementIndex] = false;
	}
}