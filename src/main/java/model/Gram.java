package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import model.exceptions.InvalidCharactersException;
import model.exceptions.InvalidExtensionException;
import model.exceptions.InvalidFileException.Entry;
import model.exceptions.InvalidNameException;

/**
 * Enregistre tous les grammes des corpus traités dans nbOccurrences et crée un
 * fichier CSV à partir de ce dernier
 */
public final class Gram {
    private final Map<String, Integer> nbOccurrences;
    private final Map<Character, List<String>> allCharacters;
    private final String keyboard;
    private final FileGram[] filesGram;
    private static final String EXTENSION = ".csv";
    private static volatile int count = 0;

    public Gram(String fileName, int length) throws InvalidExtensionException, InvalidNameException {
        if (fileName.length() <= 5 || !fileName.substring(fileName.length() - 5, fileName.length()).equals(".json"))
            throw new InvalidExtensionException(fileName, Entry.KEYBOARD);
        File charactersList = new File(fileName);
        if (!charactersList.exists())
            throw new InvalidNameException(fileName, Entry.KEYBOARD);
        filesGram = new FileGram[length];
        nbOccurrences = new LinkedHashMap<>();
        keyboard = fileName;
        Map<Character, List<String>> tmp;
        try {
            tmp = new ObjectMapper().readValue(charactersList, new TypeReference<Map<Character, List<String>>>() {
            });
        } catch (IOException e) {
            tmp = null;
        }
        allCharacters = Collections.unmodifiableMap(tmp);
    }

    /**
     * Génère un fichier CSV contenant toutes les occurrences d'un corpus de texte
     * 
     * @throws IOException s'il y a un problème dans le BufferWriter
     */
    public void fillCSV() throws IOException {
        try (BufferedWriter CSVFile = new BufferedWriter(new FileWriter("result-" + new File(keyboard).getName() + EXTENSION))) {
            for (String key : nbOccurrences.keySet())
                CSVFile.write(new StringBuilder().append("\"").append(key).append("\"").append(", ")
                        .append(nbOccurrences.get(key)).append("\n").toString());
        }
    }

    /**
     * Crée un FileGram et remplit la table nbOccurrences en fonction de son index
     * 
     * @param index l'index sur le tableau
     * @param name  le nom du fichier à traiter
     * @throws InvalidNameException       si le nom ne correspond à aucun fichier
     * @throws InvalidCharactersException s'il y a des caractères incompatibles
     *                                    avec le clavier fourni
     * @throws IOException                s'il y a un problème dans le
     *                                    BufferedReader
     */
    public void createFileGram(int index, String name)
            throws InvalidNameException, InvalidCharactersException, IOException {
        filesGram[index] = new FileGram(name);
        filesGram[index].fileNbOccurrences();
    }

    public void addOccurrences() {
        for (int i = 0; i < 3; i++)
            for (FileGram fileGram : filesGram)
                fileGram.gramsTab[i].forEach((word, occurrence) -> nbOccurrences.merge(word, occurrence, Integer::sum));
    }

    public static void reboot() {
        count = 0;
    }

    public static synchronized void increment() {
        count++;
    }

    public static int getCount() {
        return count;
    }

    public Map<String, Integer> getNbOccurrences() {
        return nbOccurrences;
    }

    /**
     * Traite chaque gramme d'un corpus et est enregistré dans CALCULATED_CORPUS
     * pour ne pas à devoir être recalculé pour le même corpus et même clavier
     */
    private final class FileGram {
        private final Map<String, Integer>[] gramsTab;
        private final String text;
        private static final Map<Map<String, Map<Character, List<String>>>, FileGram> CALCULATED_CORPUS = new HashMap<>();

        @SuppressWarnings("unchecked")
        private FileGram(String fileName) {
            gramsTab = new HashMap[3];
            for (int i = 0; i < 3; i++)
                gramsTab[i] = new HashMap<>();
            text = fileName;
        }

        /**
         * Remplit la table nbOccurrences du corpus de texte
         * 
         * @throws InvalidNameException       si le nom ne correspond à aucun fichier
         * @throws InvalidCharactersException s'il y a des caractères incompatibles
         *                                    avec le clavier fourni
         * @throws IOException                s'il y a un problème dans le
         *                                    BufferedReader
         */
        private void fileNbOccurrences() throws InvalidNameException, InvalidCharactersException, IOException {
            String content = generateContent();
            FileGram alreadyCalculated = isAlreadyCalculated(content);
            if (alreadyCalculated == null) {
                ArrayList<String> arrayList = listOfCharInText(content);
                Thread[] threads = new Thread[3];
                for (int i = 1; i <= 3; i++) {
                    final int index = i;
                    (threads[i - 1] = new Thread() {
                        @Override
                        public void run() {
                            long start = System.currentTimeMillis();
                            for (int j = 0; j <= arrayList.size() - index; j++) {
                                String tmp = "";
                                for (int k = 0; k < index; k++)
                                    tmp += (arrayList.get(j + k).equals("\"")) ? ("\"" + arrayList.get(j + k))
                                            : arrayList.get(j + k);
                                if (!gramsTab[index - 1].containsKey(tmp))
                                    gramsTab[index - 1].put(tmp, 1);
                                else
                                    gramsTab[index - 1].put(tmp, gramsTab[index - 1].get(tmp) + 1);
                            }
                            String s = "";
                            if (index > 1)
                                s = "s";
                            System.out.println(text + ": " + index + "-gramme" + s + " traité" + s + " ("
                                    + ((System.currentTimeMillis() - start) / 1_000.) + "s)");
                            System.out.flush();
                        }
                    }).start();
                }
                for (Thread thread : threads)
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                Map<String, Map<Character, List<String>>> tmp = new HashMap<>();
                tmp.put(content, allCharacters);
                CALCULATED_CORPUS.put(new HashMap<>(tmp), this);
            } else
                for (int i = 0; i < 3; i++)
                    gramsTab[i].putAll(alreadyCalculated.gramsTab[i]);
        }

        /**
         * Vérifie si un contenu a déjà été traité
         * 
         * @param content le contenu
         * @return null si le contenu n'a pas encore été traité sinon l'instance qui l'a
         *         traité
         */
        private FileGram isAlreadyCalculated(String content) {
            for (Map<String, Map<Character, List<String>>> tmp : CALCULATED_CORPUS.keySet())
                for (String text : tmp.keySet())
                    if (text.equals(content) && tmp.get(text).equals(allCharacters))
                        return CALCULATED_CORPUS.get(tmp);
            return null;
        }

        /**
         * Génère le contenu d'un fichier
         * 
         * @return le contenu du fichier
         * @throws InvalidNameException si le nom ne correspond à aucun fichier
         * @throws IOException          s'il y a un problème dans le BufferedReader
         */
        private String generateContent() throws InvalidNameException, IOException {
            File file = new File(text);
            if (!file.exists() || file.isDirectory())
                throw new InvalidNameException(text, Entry.TEXT);
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null)
                    content.append(line).append("\n");
            }
            return content.toString();
        }

        /**
         * Convertit les caractères en leur combinaison. Par exemple, î sera ^i
         * 
         * @param content le corpus
         * @return le corpus "converti"
         * @throws InvalidCharactersException si le corpus n'est pas compatible avec le
         *                                    clavier
         */
        private ArrayList<String> listOfCharInText(String content) throws InvalidCharactersException {
            ArrayList<String> arrayList = new ArrayList<>();
            Set<Character> invalidCharacters = new HashSet<>();
            int i = 0;
            while (i < content.length()) {
                char c = content.charAt(i);
                if (c == ' ' && i + 3 < content.length() && content.substring(i, i + 4).equals("    ")) {
                    c = '\t';
                    i += 4;
                } else
                    i++;
                List<String> list = allCharacters.get(c);
                if (list == null)
                    invalidCharacters.add(c);
                else
                    arrayList.addAll(list);
            }
            if (!invalidCharacters.isEmpty())
                throw new InvalidCharactersException(invalidCharacters);
            return arrayList;
        }
    }
}