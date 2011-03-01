package org.supremica.automata.BDD.EFA;

 public class ReinforcementLearningPlusTabuSearch {

        private static final int ADD_REWARD = 2;
        private static final int ADD_PUNISH = -1;
        private int max_activity;
        private int num_access;
        private int num_advance;
        private int[] activity = null, queue2 = null;
        private boolean punish_inactive;

        public ReinforcementLearningPlusTabuSearch(int size) {
            max_activity = size;
            punish_inactive = true;
            activity = new int[size];
            queue2 = new int[size];
        }

        public void reset() {
            num_access = 0;
            num_advance = 0;
            if (activity != null) {
                for (int i = 0; i < activity.length; i++) {
                    activity[i] = 0;
                }
            }
        }

        public int choose(int[] queue, int size) {
            if (size <= 0) {
                return -1;    // ERROR, no choices!
            }
            if (size == 1) {
                return queue[0];
            }
            return find_best_active(queue, size);
        }

        public void advance(int element, boolean changed) {

            num_access++;

            if (changed) {
                num_advance++;
            }

            if (punish_inactive) {
                activity[element] += (changed) ? ADD_REWARD : ADD_PUNISH;
                // dont let it grow more than we can handle...
                if (activity[element] > max_activity) {
                    activity[element] = max_activity;
                } else if (activity[element] < -max_activity) {
                    activity[element] = -max_activity;
                }
            }
        }

        private int find_best_active(int[] queue, int size) {
            int count = 0;
            int best = Integer.MIN_VALUE;
            for (int i = 0; i < size; i++) {
                int current = queue[i];
                if (activity[current] > best) {
                    best = activity[current];
                    count = 0;
                }
                if (activity[current] == best) {
                    queue2[count++] = current;
                }
            }
            return queue2[(int) (Math.random() * count)];
        }
    }