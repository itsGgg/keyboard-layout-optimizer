package model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.exceptions.InvalidConstantsException;
import model.exceptions.InvalidExtensionException;
import model.exceptions.InvalidNameException;
import model.exceptions.UnconvertibleException;

/**
 * Classe évaluant les n-grammes
 * Elle a besoin d'un keyboardlayout(AZLayout.json) pour evaluer les touches
 */
public class Evaluator {
    private final HashMap<String, Key> keyboardLayout;
    private final MovementEvaluator combinationEvaluator;
    private final List<Double> constants;

    public Evaluator(EvaluatorConfiguration configuration, String layoutFilename, String constantsFilename)
            throws InvalidExtensionException, IOException, InvalidConstantsException, InvalidNameException {
        this.keyboardLayout = configuration.jsonToHashMapConverter(layoutFilename);
        this.constants = configuration.jsonToListConverter(constantsFilename);
        if (this.constants.size() != 24)
            throw new InvalidConstantsException();
        combinationEvaluator = new MovementEvaluator(this.constants);
    }

    /**
     * Renvoie un score total pour les n-grammes donnés
     * 
     * @param ngrams les occurrences des n-grammes
     * @return le score total
     * @throws UnconvertibleException si le fichier ne peut pas être converti
     */
    public double evaluateGrams(HashMap<String, Integer> ngrams) throws UnconvertibleException {
        double result = 0, constant, total_result = 0;
        double unigramScore = 0, bigramScore = 0, trigramScore = 0;
        int leftHandCount = 0, rightHandCount = 0;

        for (Map.Entry<String, Integer> entry : ngrams.entrySet()) {
            String ngram = entry.getKey();
            int frequency = entry.getValue();

            // Vérifier l'alternance main gauche - droite
            List<Key> keyCombination = convertStringToKeys(ngram);
            for (Key key : keyCombination) {
                if (key.getHand() == Hand.LEFT) {
                    leftHandCount += frequency;
                } else {
                    rightHandCount += frequency;
                }
            }
            constant = combinationEvaluator.analyzeMovement(keyCombination);

            result += frequency * constant;
            switch (keyCombination.size()) {
                case 1:
                    unigramScore += result;
                    break;
                case 2:
                    bigramScore += result;
                    break;
                case 3:
                    trigramScore += result;
                    break;
            }
            result = 0;
        }
        double totalKeysPressed = leftHandCount + rightHandCount;
        double rightHandRatio = (double) rightHandCount / totalKeysPressed; // ratio de l'utilisation de la main droite
        double distanceToBalance = (double) Math.abs(rightHandRatio - 0.5); // Calcul de la distance à l'équilibre

        // Donner un poids plus grand/faible selon la distance à l'équilibre
        double handBalanceWeight = constants.get(1);
        if (distanceToBalance == 0) {
            handBalanceWeight = constants.get(21);
        } else if (distanceToBalance <= 0.2) {
            handBalanceWeight = constants.get(22);
        } else if (distanceToBalance > 0.2 && distanceToBalance <= 0.5) {
            handBalanceWeight = constants.get(23);
        }
        // Rajouter au résultat la distance à l'équilibre * le poids qu'on donne à cette
        // distance
        // Rajouter au score total le poids des n-grammes
        total_result = (unigramScore * constants.get(18)) + (bigramScore * constants.get(19))
                + (trigramScore * constants.get(20));
        // Rajouter la distace à l'équilibre
        total_result += handBalanceWeight * distanceToBalance;
        // Calcul du poids total des n-grammes pour la somme pondérée
        double total_weight = constants.get(18) + constants.get(19) + constants.get(20);
        return total_result / total_weight;
    }

    /**
     * Méthode permettant de récuperer la clé associé à un n-gramme depuis le
     * HashMap keyboardLayout
     * 
     * @param s la combinaison de touches représentant le gramme
     * @return les touches
     * @throws UnconvertibleException si le fichier ne peut pas être converti
     */
    private List<Key> convertStringToKeys(String s) throws UnconvertibleException {
        List<Key> keysCombination = new ArrayList<>();
        for (int i = 0; i < s.length(); i++) {
            String key = String.valueOf(s.charAt(i));
            switch (key) {
                case "E":
                    if (s.startsWith("Espace", i)) {
                        key = " ";
                        i += "Espace".length() - 1;
                    } else if (s.startsWith("Entrée", i)) {
                        key = "\n";
                        i += "Entrée".length() - 1;
                    }
                    break;
                case "T":
                    if (s.startsWith("Tabulation", i)) {
                        key = "\t";
                        i += "Tabulation".length() - 1;
                    }
                    break;
                case "S":
                    if (s.startsWith("Shift", i)) {
                        key = "Shift";
                        i += "Shift".length() - 1;
                    }
                    break;
                case "A":
                    if (s.startsWith("AltGr", i)) {
                        key = "AltGr";
                        i += "AltGr".length() - 1;
                    }
                case "\"":
                    if (s.startsWith("\"\"", i)) {
                        key = "\"";
                        i += "\"\"".length() - 1;
                    }
                    break;
            }
            Key keyObj = this.keyboardLayout.get(key);
            if (keyObj != null)
                keysCombination.add(keyObj);
            else
                throw new UnconvertibleException();
        }
        return List.copyOf(keysCombination);
    }
}
