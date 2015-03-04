#ifndef SELECT_H
#define SELECT_H

/**
 * @author Nathanael Foy, INRIA
 * @date 26.02.15
 */

/**
 * @brief The Select class chooses the symbols of the keyboard to flash.
 *
 * The evidence accumulation in the BCI-based speller updates at every
 * step the current probability of every symbol. At each step the Select
 * object chooses the next set of symbols to flash.
 */
class Select {
public:
    /**
     * @brief select chooses the next set of symbol to flash.
     * @param n the maximum number of letter in the group.
     * @param p the probability that the group contains the target.
     * @return the set of indices to flash
     *
     * Since we don't want that too many letters flash at the same time,
     * because otherwise it would distract the user, we restrict the
     * maximum number of symbols in the group to n.
     *
     * Nevertheless the group may possibly contain much less
     * letters than n.
     *
     * Also, we don't want that the group contains the target too often,
     * because the P300 only occurs in response to relatively rare events.
     * Therefore this probability is controlled by the parameter p.
     *
     * The goal of the algorithm is to form a group of symbols that contains
     * at most n symbols and whose symbols have probabilities that sum up to
     * a value as close as possible to p.
     *
     * This problem is an instance of the bagpack problem, known to be
     * NP-complete. However many heuristics exist, for instance based on
     * dynamic programming or branch-and-bound.
     */
    virtual std::set<int> select(std::vector<double>, int n, double p)=0;
};

#endif // SELECT_H
