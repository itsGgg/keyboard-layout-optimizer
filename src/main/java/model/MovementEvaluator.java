package model;

import java.util.Collections;
import java.util.List;

/**
 * Classe permettant de traiter chaque type de combinaison de touches et renvoie
 * une constante qui influencera le score final de l'évaluation de dispostion
 */
public record MovementEvaluator(List<Double> constants) {
    public MovementEvaluator(List<Double> constants) {
        this.constants = Collections.unmodifiableList(List.copyOf(constants));
    }

    public double analyzeMovement(List<Key> keyCombinations) {
        double constant = constants.get(0);
        if (isUnigram(keyCombinations)) {
            constant = calcUnigramConst(keyCombinations);
        } else if (isBigram(keyCombinations)) {
            constant = calcBigramConst(keyCombinations);
        } else if (isTrigram(keyCombinations)) {
            constant = calcTrigrammeConst(keyCombinations);
        } else {
            for (Key key : keyCombinations) {
                System.out.println(key.getCoordinates());
            }
            throw new IllegalArgumentException("Il ne s'agit ni d'un Unigramme, ni de Bigramme, ni de Trigramme");
        }

        return constant;
    }

    /**
     * Renvoie une constante calculé à partir de la distance entre la touche de
     * repos correspondante à un doigt et la touche qui répresente l'unigramme.
     * 
     * @param keyCombinations
     * @return
     */
    private double calcUnigramConst(List<Key> keyCombinations) {
        if (keyCombinations.size() != 1) {
            throw new IllegalArgumentException("Il ne s'agit pas d'un unigramme");
        }
        double constant = constants.get(0);
        double fingerWeight = this.getFingerWeight(keyCombinations.get(0).getFinger());
        // debug
        // System.out.println("FingerWeight : " + fingerWeight);
        // Calcul de la distance
        Key key = keyCombinations.get(0);
        Point coordRestKey = getRestPosition(key.getHand(), key.getFinger());
        // Debug
        // System.out.println("Coord touche repos" + coordRestKey.toString());
        // Calculer la distance entre le point de repos du doigt et la touche appuyé
        double distance = coordRestKey.distance(key.getCoordinates());
        // Debug
        // System.out.println("Distance par rapport a touche de repos : " + distance);
        if (distance == 0) {
            return constants.get(6); // Gérer le cas où il s'agit de la touche repos
        } else if (distance > 1) {
            constant += constants.get(7);
        }

        // Débug
        // System.out.println("distance : " + distance);
        // System.out.println("constant : " + constant);
        if (constant != 0) {
            constant *= distance * fingerWeight;
        } else {
            constant = distance * fingerWeight;
        }

        // Débug
        // System.out.println("constant : " + constant);
        return constant;
    }

    /**
     * Renvoie une constante de calcul pour le score des bigrammes selon le type des
     * bigrammes :
     * - bigramme alternant les deux mains,
     * - roulement pur de l'intérieur vers l'extérieur ou inversement,
     * - roulement avec LSB/Ciseaux/SFB,
     * - LSB,Ciseaux, SFB
     * 
     * @param keyCombinations
     * @return
     */
    private double calcBigramConst(List<Key> keyCombinations) {
        double constant = constants.get(0); // Valeur par défaut

        if (isBigramAlternatingHands(keyCombinations)) {
            constant += constants.get(8);
            // System.out.println("constante AlternatingHands -0.8 : " + constant);
        }
        if (isPureRoll(keyCombinations)) {
            if (isRollOutIn(keyCombinations)) {
                constant += constants.get(9);
                // System.out.println("constante isRollOutIn -0.4 : " + constant);
                return constant;
            } else if (isRollInOut(keyCombinations)) {
                constant += constants.get(10);
                // System.out.println("constante rollInOut -0.2 : " + constant);
                return constant;
            }
        }
        if (isRoll(keyCombinations)) {
            constant += constants.get(11);
            // System.out.println("Constante roulement +1 car pas roulement pur : " +
            // constant);
        }
        // Utilisation de la même main
        if (isLSB(keyCombinations)) {
            constant += constants.get(12);
            // System.out.println("constante LSB 3 rajouté: " + constant);
        }
        if (isSFB(keyCombinations)) {
            constant += constants.get(13);
            // System.out.println("constante SFB 2 rajouté " + constant);
        }
        if ((isScissor(keyCombinations))) {
            constant += constants.get(14);
            // System.out.println("constante SCISSOR 2.7 rajouté : " + constant);
        }

        if (constant <= 0) {
            constant = constants.get(0);
        }
        return constant;
    }

    /**
     * Renvoie true si on a un roulement c.à.d un mouvement réalisé par deux doigts
     * différents d'une même main
     * 
     * @param keyCombinations
     * @return
     */
    private boolean isRoll(List<Key> keyCombinations) {
        if (keyCombinations.size() != 2) {
            throw new IllegalArgumentException("Il ne s'agit pas d'un bigramme");
        }
        Key key1 = keyCombinations.get(0);
        Key key2 = keyCombinations.get(1);
        if (!key1.isCorrectKey() || !key2.isCorrectKey()) {
            throw new IllegalArgumentException("Une des touches de ce Bigramme n'est pas initialisé correctement");
        }
        // Vérifie que la même main est utilisé
        if (key1.getHand() != key2.getHand()) {
            return false;
        }
        // Vérifie que les touches sont appuyés par des doigts différents
        if (key1.getFinger() == key2.getFinger()) {
            return false;
        }

        return true;
    }

    /**
     * Vérifie que on a un roulement pur, c'est a dire un roulement sans
     * LSB/Scisseaux/SFB
     * 
     * @param keyCombinations
     * @return
     */
    private boolean isPureRoll(List<Key> keyCombinations) {
        if (isRoll(keyCombinations) && !isLSB(keyCombinations) && !isSFB(keyCombinations)
                && !isScissor(keyCombinations)) {
            return true;
        }
        return false;
    }

    /**
     * Méthode permettant de vérifier si un Roulement est
     * fait de l'extérieur vers l'intérieur
     * 
     * @param keyCombinations
     * @return
     */
    private boolean isRollOutIn(List<Key> keyCombinations) {
        if (keyCombinations.size() != 2) {
            throw new IllegalArgumentException("Il ne s'agit pas d'un bigramme");
        }
        Key key1 = keyCombinations.get(0);
        Key key2 = keyCombinations.get(1);
        if (!key1.isCorrectKey() || !key2.isCorrectKey()) {
            throw new IllegalArgumentException("Une des touches de ce Bigramme n'est pas initialisé correctement");
        }
        // Vérifie que la même main est utilisé
        if (key1.getHand() != key2.getHand()) {
            return false;
        }
        // Vérifie que on utilise des doigts différents
        if (key1.getFinger() == key2.getFinger()) {
            return false;
        }
        // Vérifie que le mouvement se réalise de l'éxterieur vers l'intérieur
        if (key1.getFinger().ordinal() > key2.getFinger().ordinal()) {
            return true;
        }

        return false;
    }

    /**
     * Vérifier si un roulement est fait de l'intérieur vers l'éxtérieur
     * 
     * @param keyCombinations
     * @return
     */
    private boolean isRollInOut(List<Key> keyCombinations) {
        if (keyCombinations.size() != 2) {
            throw new IllegalArgumentException("Il ne s'agit pas d'un bigramme");
        }
        Key key1 = keyCombinations.get(0);
        Key key2 = keyCombinations.get(1);
        if (!key1.isCorrectKey() || !key2.isCorrectKey()) {
            throw new IllegalArgumentException("Une des touches de ce Bigramme n'est pas initialisé correctement");
        }
        // Vérifie que la même main est utilisé
        if (key1.getHand() != key2.getHand()) {
            return false;
        }
        // Vérifie que on utilise des doigts différents
        if (key1.getFinger() == key2.getFinger()) {
            return false;
        }
        // Vérifie que le mouvement se réalise de l'intérieur vers l'extérieur
        if (key2.getFinger().ordinal() > key1.getFinger().ordinal()) {
            return true;
        }
        return false;
    }

    /**
     * Renvoie true si on a un same finger bigram (SFB) c'est à dire un bigram dont
     * les touches sont appuyés par le même doigt
     * 
     * @param keyCombinations
     * @return
     */
    private boolean isSFB(List<Key> keyCombinations) {
        if (keyCombinations.size() != 2) {
            throw new IllegalArgumentException("Il ne s'agit pas d'un SFB");
        }
        Key key1 = keyCombinations.get(0);
        Key key2 = keyCombinations.get(1);
        if (!key1.isCorrectKey() || !key2.isCorrectKey()) {
            throw new IllegalArgumentException("Une des touches de ce Bigramme n'est pas initialisé correctement");
        }
        // Vérifie que la même main est utilisé
        if (key1.getHand() != key2.getHand()) {
            return false;
        }
        // Vérifie que on utilise le même doigt
        if (key1.getFinger() != key2.getFinger()) {
            return false;
        }
        return true;
    }

    /**
     * Renvoie true si on a un mouvement en ciseaux pour ce bigramme
     * mouvement en ciseaux : mouvement à une main faisant succéder la rangée
     * inférieure et la rangée supérieur
     * 
     * @param keyCombinations
     * @return
     */
    private boolean isScissor(List<Key> keyCombinations) {
        if (keyCombinations.size() != 2) {
            throw new IllegalArgumentException("Il ne s'agit pas d'un bigramme");
        }
        Key key1 = keyCombinations.get(0);
        Key key2 = keyCombinations.get(1);
        if (!key1.isCorrectKey() || !key2.isCorrectKey()) {
            throw new IllegalArgumentException("Une des touches de ce Bigramme n'est pas initialisé correctement");
        }
        // Vérifie que la même main est utilisé
        if ((key1.getHand() != key2.getHand())) {
            return false;
        }
        // Vérifie que le mouvement est réalisé par des doigts différents
        if ((key1.getFinger() == key2.getFinger())) {
            return false;
        }
        // Vérifie que les touches se trouvent sur des rangés différentes
        return key1.getCoordinates().getY() != key2.getCoordinates().getY();
    }

    /**
     * Renvoie true si on a lateral stretch bigram (LSB) ou bigram à mouvement
     * latéral
     * 
     * @param keyCombinations
     * @return
     */
    private boolean isLSB(List<Key> keyCombinations) {
        if (keyCombinations.size() != 2) {
            throw new IllegalArgumentException("Il ne s'agit pas d'un bigramme");
        }
        Key key1 = keyCombinations.get(0);
        Key key2 = keyCombinations.get(1);
        if (!key1.isCorrectKey() || !key2.isCorrectKey()) {
            throw new IllegalArgumentException("Une des touches de ce Bigramme n'est pas initialisé correctement");
        }
        // Vérifie que la même main est utilisé
        if (key1.getHand() != key2.getHand()) {
            return false;
        }
        // Vérifie que les touches appuyés sont sur une colonne différente que celle de
        // la postion des doigts les appuyant
        Point restPointKey1 = getRestPosition(key1.getHand(), key1.getFinger());
        Point restPointKey2 = getRestPosition(key2.getHand(), key2.getFinger());

        return restPointKey1.getX() != key1.getCoordinates().getX()
                || restPointKey2.getX() != key2.getCoordinates().getX();
    }

    /**
     * Renvoie true si le bigramme est fait avec une alternance de mains
     * 
     * @param keyCombinations
     * @return
     */
    private boolean isBigramAlternatingHands(List<Key> keyCombinations) {
        if (keyCombinations.size() != 2) {
            throw new IllegalArgumentException("Il ne s'agit pas d'un bigramme");
        }
        Key key1 = keyCombinations.get(0);
        Key key2 = keyCombinations.get(1);
        if (!key1.isCorrectKey() || !key2.isCorrectKey()) {
            throw new IllegalArgumentException("Une des touches de ce Bigramme n'est pas initialisé correctement");
        }
        // Vérifier que deux mains différentes sont utilisés
        if (key1.getHand() != key2.getHand()) {
            return true;
        }
        return false;
    }

    /**
     * Calculer une constante selon le type de trigramme : mauvaise redirection,
     * redirection, skipgramme
     * 
     * @param keyCombinations
     * @return
     */
    private double calcTrigrammeConst(List<Key> keyCombinations) {
        double constant = constants.get(0);

        if (isBadRedirection(keyCombinations)) {
            constant += constants.get(15);
            // System.out.println("Constante badRedirection : " + constant);
        } else if (isRedirection(keyCombinations)) {
            constant += constants.get(16);
            // System.out.println("Constante redirection : " + constant);
        }
        if (isSKS(keyCombinations)) {
            constant += constants.get(17);
            // System.out.println("Constante SKS +2 rajouté: " + constant);
        }

        return constant;
    }

    /**
     * Renvoie true si on a une redirection
     * 
     * @param keyCombinations
     * @return
     */
    private boolean isRedirection(List<Key> keyCombinations) {
        if (keyCombinations.size() != 3) {
            throw new IllegalArgumentException("Il ne s'agit pas d'un trigramme");
        }
        Key key1 = keyCombinations.get(0);
        Key key2 = keyCombinations.get(1);
        Key key3 = keyCombinations.get(2);
        if (!key1.isCorrectKey() || !key2.isCorrectKey() || !key3.isCorrectKey()) {
            throw new IllegalArgumentException("Une des touches de ce Bigramme n'est pas initialisé correctement");
        }
        // Vérifie les touches sont appuyés par la même main
        if (key1.getHand() == key2.getHand() && key2.getHand() == key3.getHand()) {
            double x1 = key1.getCoordinates().getX();
            double x2 = key2.getCoordinates().getX();
            double x3 = key3.getCoordinates().getX();
            // Vérifier que il y a un changement de redirection intérieur - extérieur ou
            // inversement
            return (x1 < x2 && x2 > x3) || (x1 > x2 && x2 < x3);
        }
        return false;
    }

    /**
     * Renvoie true si on a une mauvaise redirection c'est à dire une
     * redirection qui n'utilise pas l'index
     * 
     * @param keyCombinations
     * @return
     */
    private boolean isBadRedirection(List<Key> keyCombinations) {
        if (isRedirection(keyCombinations)) {
            Key key1 = keyCombinations.get(0);
            Key key2 = keyCombinations.get(1);
            Key key3 = keyCombinations.get(2);
            // Vérifie que la redirection n'utilise pas l'Index
            if (!key1.getFinger().isIndex() && !key2.getFinger().isIndex() && !key3.getFinger().isIndex()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Renvoie true si on a un skipgramme (SKS)
     * 
     * @param keyCombinations
     * @return
     */
    private boolean isSKS(List<Key> keyCombinations) {
        if (keyCombinations.size() != 3) {
            throw new IllegalArgumentException("Il ne s'agit pas d'un trigramme");
        }
        Key key1 = keyCombinations.get(0);
        Key key2 = keyCombinations.get(1);
        Key key3 = keyCombinations.get(2);
        if (!key1.isCorrectKey() || !key2.isCorrectKey() || !key3.isCorrectKey()) {
            throw new IllegalArgumentException("Une des touches de ce Bigramme n'est pas initialisé correctement");
        }
        // Vérifie que la 1ere et la 3eme touche sont appuyés par la même main
        if (key1.getHand() != key3.getHand()) {
            return false;
        }
        // Vérifie que si la 1ere et la 3eme touches sont appuyés par la même main, la
        // 2éme touche est appuyé par une main différente
        if (key1.getHand() == key3.getHand()) {
            if (key1.getHand() == key2.getHand()) {
                return false;
            }
        }
        // Vérifie que le doigt appuyant la 1ere touche est le même que celui de la
        // troisième touche
        if (key1.getFinger() != key3.getFinger()) {
            return false;
        }
        // Vérifie que le doigt appuyant la 2eme touche est differente du doigt
        // appuyant la 1ere et la 3eme touche
        if (key1.getFinger() == key3.getFinger()) {
            if (key2.getFinger() != key1.getFinger()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Méthode renvoyant la touche de repos associé à un doigt d'une main
     * 
     * @param hand
     * @param finger
     */
    private Point getRestPosition(Hand hand, Finger finger) {
        switch (finger) {
            case THUMB:
                if (hand == Hand.LEFT)
                    return new Point(4, 4); // espace
                else
                    return new Point(5, 4); // altgr
            case INDEX:
                if (hand == Hand.LEFT)
                    return new Point(4, 2); // emplacement du F en azerty par ex
                else
                    return new Point(6, 2);
            case MIDDLE:
                if (hand == Hand.LEFT)
                    return new Point(3, 2);
                else
                    return new Point(7, 2);
            case RING:
                if (hand == Hand.LEFT)
                    return new Point(2, 2);
                else
                    return new Point(8, 2);
            case PINKY:
                if (hand == Hand.LEFT)
                    return new Point(1, 2);
                else
                    return new Point(9, 2);
        }
        return null;
    }

    /**
     * Renvoie une constante selon la difficulté d'appui d'une touche avec un doigt
     * 
     * @return la constante
     */
    public double getFingerWeight(Finger finger) {
        switch (finger) {
            case THUMB:
                return constants.get(1);
            case INDEX:
                return constants.get(2);
            case MIDDLE:
                return constants.get(3);
            case RING:
                return constants.get(4);
            case PINKY:
                return constants.get(5);
            default:
                throw new IllegalArgumentException("Not a finger : " + this);
        }
    }

    /**
     * Méthode permettant de vérifier si une combinaison de touches compose un
     * unigramme
     * 
     * @param keyCombinations
     * @return
     */
    private boolean isUnigram(List<Key> keyCombinations) {
        if (keyCombinations.size() == 1 && keyCombinations.get(0).getCoordinates() != null) {
            return true;
        }
        if (!keyCombinations.get(0).isCorrectKey()) {
            throw new IllegalArgumentException("La touche composant l'Unigramme n'a pas été initialisé correctement.");
        }
        return false;
    }

    /**
     * Méthode permettant de vérifier si une combinaison de touches compose un
     * bigramme
     * 
     * @param keyCombinations
     * @return
     */
    private boolean isBigram(List<Key> keyCombinations) {
        if (keyCombinations.size() != 2) {
            return false;
        }
        for (Key key : keyCombinations) {
            if (!key.isCorrectKey()) {
                throw new IllegalArgumentException(
                        "La touche composant le Bigramme n'a pas été initialisé correctement.");
            }
        }
        return true;
    }

    /**
     * Méthode permettant de vérifier si une combinaison de touches compose
     * un trigramme
     * 
     * @param keyCombinations
     * @return
     */
    private boolean isTrigram(List<Key> keyCombinations) {
        if (keyCombinations.size() != 3) {
            return false;
        }
        for (Key key : keyCombinations) {
            if (!key.isCorrectKey()) {
                throw new IllegalArgumentException(
                        "La touche composant le Trigramme n'a pas été initialisé correctement.");
            }
        }
        return true;
    }
}
