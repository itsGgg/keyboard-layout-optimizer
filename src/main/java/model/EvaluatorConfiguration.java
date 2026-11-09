package model;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import model.exceptions.InvalidExtensionException;
import model.exceptions.InvalidFileException.Entry;
import model.exceptions.InvalidNameException;

/**
 * Classe permettant de transformer un fichier JSON en une répresentation de
 * clavier nécessaire pour le calcul du score. Elle permet aussi de transformer
 * un fichier .json en une List<Double> qui contient des constantes permettant
 * le calcul du score.
 * 
 */
public class EvaluatorConfiguration {

    /**
     * Cette méthode prend un nom de fichier en paramètres qui répresente une
     * disposition de clavier et le transforme dans un HashMap<String, Key>
     * 
     * @param filename le nom du fichier
     * @return les données du json
     * @throws InvalidExtensionException si l'extension n'est pas valide
     * @throws IOException
     * @throws InvalidNameException      si le nom ne correspond à aucun fichier
     */
    public HashMap<String, Key> jsonToHashMapConverter(String filename)
            throws InvalidExtensionException, IOException, InvalidNameException {
        // vérification que le fichier se termine en .json
        if (filename.length() <= 5 || !filename.endsWith(".json")) {
            throw new InvalidExtensionException(filename, Entry.LAYOUT);
        }

        File file = new File(filename);
        if (!file.exists())
            throw new InvalidNameException(filename, Entry.LAYOUT);

        ObjectMapper objectMapper = new ObjectMapper();

        TypeReference<HashMap<String, List<Integer>>> typeRef = new TypeReference<>() {
        };
        // stocke temporairement string-> coord,finger,doigt pour pouvoir créer hashmap:
        // string->key
        HashMap<String, List<Integer>> map = objectMapper.readValue(file, typeRef);

        HashMap<String, Key> result = new HashMap<>();
        for (Map.Entry<String, List<Integer>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<Integer> coordinates = entry.getValue();

            int fingerValue = coordinates.get(2);
            int handValue = coordinates.get(3);
            Finger finger = Finger.values()[fingerValue];
            Hand hand = Hand.values()[handValue];
            Key keyValue = new Key(hand, finger, new Point(coordinates.get(0), coordinates.get(1)));
            result.put(key, keyValue);
        }
        return result;
    }

    /**
     * Méthode permettant de transformer un fichier JSON contenant une liste de
     * constantes en une List<Double>
     * 
     * @param filename le nom du fichier
     * @return la liste des constantes
     * @throws IOException
     * @throws InvalidExtensionException si l'extension n'est pas valide
     * @throws InvalidNameException      si le nom ne correspond à aucun fichier
     */
    public List<Double> jsonToListConverter(String filename)
            throws IOException, InvalidExtensionException, InvalidNameException {
        if (filename.length() <= 5 || !filename.endsWith(".json")) {
            throw new InvalidExtensionException(filename, Entry.CONSTANT);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<List<Double>> typeRef = new TypeReference<>() {
        };

        File file = new File(filename);
        if (!file.exists()) {
            throw new InvalidNameException(filename, Entry.CONSTANT);
        }

        List<Double> result = objectMapper.readValue(file, typeRef);

        return Collections.unmodifiableList(result);
    }
}