package model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

import com.fasterxml.jackson.databind.exc.MismatchedInputException;

import model.exceptions.InvalidCharactersException;
import model.exceptions.InvalidConstantsException;
import model.exceptions.InvalidExtensionException;
import model.exceptions.InvalidFileException.Entry;
import model.exceptions.InvalidNameException;
import model.exceptions.UnconvertibleException;

public final class Run extends Thread {
    @Override
    public void run() {
        Scanner sc = new Scanner(System.in);
        boolean rest = true;
        while (rest) {
            String[] listFilesName = enterFilesName(sc);
            String keyboard = entryKeyboard(sc);
            String keyboardLayout = enterKeyboardLayout(sc);
            String constants = enterConstants(sc);
            System.out.println();
            Thread[] threads = createThreads(listFilesName, keyboard, keyboardLayout, constants);
            if (threads != null)
                for (Thread thread : threads)
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            if (Gram.getCount() > 0) {
                String s = "";
                if (Gram.getCount() > 1)
                    s = "s";
                System.out.println(
                        Gram.getCount() + " fichier" + s + " sur " + listFilesName.length + " traité" + s + ".");
            } else
                System.out.println("Aucun fichier traité.");
            Gram.reboot();
            rest = restart(sc);
        }
        sc.close();
    }

    private static String[] enterFilesName(Scanner sc) {
        String filesName = "";
        while (filesName.isBlank()) {
            System.out.print("Nom de fichier(s): ");
            filesName = sc.nextLine();
        }
        return filesName.split(" ");
    }

    // pour importer la disposition(plan géom) d'un json (voir AZLayout)
    private static String enterKeyboardLayout(Scanner sc) {
        String filename = "";
        while (filename.isBlank()) {
            System.out.print("Nom du fichier JSON répresentant la disposition de clavier: ");
            filename = sc.nextLine();
        }
        return filename;
    }

    private static String enterConstants(Scanner sc) {
        String filename = "";
        while (filename.isBlank()) {
            System.out.print("Fichier JSON contenant les constantes nécessaires pour le calcul de divers mouvements: ");
            filename = sc.nextLine();
        }
        return filename;
    }

    private static String entryKeyboard(Scanner sc) {
        String keyboard = "";
        while (keyboard.isBlank()) {
            System.out.print("Clavier: ");
            keyboard = sc.nextLine();
        }
        return keyboard;
    }

    private static Thread[] createThreads(String[] listFilesName, String keyboard, String layoutName,
            String constants) {
        Gram gram;
        try {
            gram = new Gram(keyboard, listFilesName.length);
        } catch (InvalidNameException | NullPointerException e1) {
            System.out.println(keyboard + " n'est pas un fichier valide pour le clavier.");
            return null;
        } catch (InvalidExtensionException e2) {
            System.out.println(keyboard + " n'est pas en format JSON pour le clavier.");
            return null;
        }
        int index = 0;
        Thread[] threads = new Thread[listFilesName.length];
        for (String name : listFilesName) {
            final int i = index;
            (threads[index++] = new Thread() {
                @Override
                public void run() {
                    long start = System.currentTimeMillis();
                    String result;
                    try {
                        for (int j = 0; j < i; j++)
                            if (listFilesName[j].equals(name)) {
                                threads[j].join();
                                break;
                            }
                        gram.createFileGram(i, name);
                        result = "Fichier " + name + " traité";
                        Gram.increment();
                    } catch (InvalidNameException e1) {
                        result = e1.getInvalidName() + " n'est pas un fichier valide pour le fichier texte";
                    } catch (InvalidCharactersException e2) {
                        result = e2.error(name);
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                    System.out.println(result + " (" + ((System.currentTimeMillis() - start) / 1_000.) + "s)\n");
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
        try {
            if (Gram.getCount() > 0) {
                gram.addOccurrences();
                gram.fillCSV();
                Evaluator evaluator = new Evaluator(new EvaluatorConfiguration(), layoutName, constants);
                HashMap<String, Integer> nbOc = new HashMap<>(gram.getNbOccurrences());
                System.out.println("Score: " + evaluator.evaluateGrams(nbOc));
            }
        } catch (InvalidNameException e1) {
            System.out.print(e1.getInvalidName() + " n'est pas un fichier valide pour ");
            if (e1.getEntry() == Entry.LAYOUT)
                System.out.println("la disposition de clavier.");
            else
                System.out.println("les constantes.");
        } catch (UnconvertibleException e2) {
            System.out.println(keyboard + " et " + layoutName + " ne sont pas compatibles.");
        } catch (InvalidExtensionException e3) {
            System.out.print(e3.getInvalidName() + " n'est pas en format JSON pour ");
            if (e3.getEntry() == Entry.LAYOUT)
                System.out.println("la disposition de clavier.");
            else
                System.out.println("les constantes.");
        } catch (MismatchedInputException e5) {
            System.out.println(layoutName + " n'est pas fichier valide pour la disposition du clavier.");
        } catch (InvalidConstantsException e6) {
            System.out.println("Le fichier contenant les constantes doit avoir exactement 15 constantes.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return threads;
    }

    private static boolean restart(Scanner sc) {
        String entry = "";
        while (true) {
            System.out.println("\nEntrez 'r' si vous voulez continuer à générer des fichiers.");
            System.out.println("Entrer 'q' pour quitter.");
            entry = sc.nextLine();
            if (entry.equals("r")) {
                try {
                    clean();
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
                return true;
            } else if (entry.equals("q"))
                return false;
            else {
                if (!entry.isBlank())
                    System.out.println("Entrée non valide.");
                entry = "";
            }
        }
    }

    private static void clean() throws InterruptedException, IOException {
        ProcessBuilder p = (System.getProperty("os.name").toLowerCase().contains("win"))
                ? new ProcessBuilder("cmd", "/c", "cls")
                : new ProcessBuilder("/bin/bash", "-c", "clear");
        p.inheritIO().start().waitFor();
    }
}
