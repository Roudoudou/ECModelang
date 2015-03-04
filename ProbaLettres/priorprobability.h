#ifndef PRIORPROBABILITY_H
#define PRIORPROBABILITY_H

/**
 * @author Nathanael Foy, INRIA
 * @date 26.02.15
 */

/**
 * @brief The PriorProbability class computes prior probabilities.
 *
 * This class computes the prior probabilities for every symbol displayed
 * on the BCI-based symbol. These symbols include the letters, possibly
 * punctuation marks (only ".", "!" and "?" for simplicity), numeric
 * values (0-9), an "undo" buttons and word completions (2 to 10).
 *
 */
class PriorProbability {
public:
    /**
     * @brief Configuration set of parameteres.
     *
     * The Configuration object contains the set of parameters that are
     * necessary to configure the PriorProbability object.
     *
     * It contains parameters to configure the underlying Presage object
     * to compute the word predictions.
     *
     * It also contains parameters relative to the keyboard,
     * for instance whether the keyboard offers an "undo" button, punctuation
     * marks, numerics, and the number of word completions presented to the
     * user.
     *
     * It may also possibly contain some information about the classifier such
     * as the accuracy computed from the cross-validation test, in order to
     * help estimate the probability of the "undo" button.
     *
     * It is defined as a map of <key, value> but feel free to define it
     * in another fashion at your convenience.
     */
    typedef std::map<std::string, std::string> Configuration;

    /**
     * @brief PriorProbability constructor.
     * @param config set of parameters.
     */
    virtual PriorProbability(const Configuration& config)=0;

    /**
     * @brief compute compute the prior probabilities for every symbol.
     * @param context the text that has been typed by the user so far.
     * @return the prior probabilities.
     *
     * The prior probabilities are stored in a map such that every key
     * corresponds to a symbol and the value corresponds to the associated
     * prior probability.
     *
     * A symbol can be a letter, for instance "A" (only
     * upper cases are considered for simplicity), a numeric value (0-9),
     * a punctuation mark (".", "!", "?"), a symbol to represent the "undo"
     * button (say "*"), or a word completion.
     *
     * The probabilities of the letters are computed from the list of possible
     * word completions provided by Presage (http://presage.sourceforge.net/)
     * and their respective probabilities.
     *
     * The probabilities associated to the letters must take into account the
     * probabilities of the word completions presented to the user.
     * For instance, the less probable the word completions are, the more
     * probability should be given to the letters.
     *
     * The number of word completions presented to the users can
     * also vary depending on the configuration (between 2 and 10 word
     * completions).
     *
     * A method to estimate the probability of the "undo" button has to be
     * defined.
     *
     * It can possibly be based on the accuracy of the classifier. It may
     * also depend on the current word being spelled by the user, whether
     * this word can be completed to an existing word or not.
     *
     * Generally speaking, keep things simple at the beginning and leave
     * the possibility to improve things in later stages ;-)
     *
     */
    virtual std::map<std::string, double> compute(const std::string& ctx)=0;
};

#endif // PRIORPROBABILITY_H
