
## Projet : Analyseur de texte et optimisateur de disposition clavier

## Membres du projet :
- [@itsGgg](https://github.com/itsGgg)
- Z. Henry

## Introduction
Ce projet consiste en deux logiciels partageant certaines structures de données :
1. un analyseur de fréquence de suites de caractères dans un corpus de textes donné
2. un évaluateur de disposition de clavier

## Compilation du projet et son exécution
Pour compiler, vérifiez que Gradle est bien installé sur votre machine sinon réalisez les étapes d'installation selon votre OS que vous pouvez consulter sur le site :
https://docs.gradle.org/current/userguide/installation.html .

*Nous vous conseillons la version gradle 8.12*
Vérifiez que vous êtes à la racine de l'arborescence du projet puis réalisez les commandes suivantes dans un terminal:
- `gradle build`
- *gradle run --console=plain* (préferablement à gradle run pour un affichage sans barre de progression)

Une fois cela fait le programme est lancé et veuillez suivre les instructions demandés. Pour plus d'informations sur les fichiers demandés veuillez voir la section "**Fichiers requis**"

## Fichiers requis
Une fois le programme executé, vous aurez besoin de : 

- Un/Des fichier(s) qui constitue(nt) votre corpus sur lequel l'évaluateur testera votre disposition. Des fichiers de test pour le programme sont disponibles dans le répertoire `corpus/`.
- Un fichier `.json` qui sera utilisé pour transformer les n-grammes en combinaisons de touches. (par exemple `azertyKeyCombinations.json` ou `qwerty.json`).
- Un fichier `.json` répresentant les disposition de clavier de votre choix. (`azertyLayout.json` ou `qwertyLayout.json`).
- Un fichier `.json` qui contient des constantes qui permettront de calculer le score. (`constantes.json`)

Des exemples de ces fichiers de configuration sont disponibles dans le répertoire `config/` (sous-dossiers `azerty/` et `qwerty/` selon la disposition ; `constantes.json` à la racine de `config/`).

## Configuration fichiers .json 
- Fichier `.json` pour transformer n-grammes en combinaisons de touches : 
    - Veuillez suivre ce format : 
        "n-gramme":["combinaisons","touches","realisés","pour","obtenir","cet","ngramme"],
        - par exemple, en azerty "%" s'obtient par la combinaison des touches "Shift" et "ù". Dans le fichier `.json` cela doit ajouté de la manière **"%": ["Shift","ù"]** 
    
- Fichier `.json` répresentant la disposition de clavier de votre choix.
    - Veuillez suivre ce format : 
    "touche" : [position x de la touche sur le clavier, position y, numéro de doigt qui doit l'utiliser, numéro de la main qui doit l'utiliser]
    - position x : colonne sur laquelle se trouve votre touche
    - position y : ligne sur laquelle se trouve votre touche 
        - par exemple la touche "²" sur azerty a une position [0,0]   
    - numéro du doigt : 
        - 0 pour le pouce
        - 1 pour l'index
        - 2 pour le majeur
        - 3 pour l'annulaire
        - 4 pour l'auriculaire
    - numéro de la main :
        - 0 pour la main gauche
        - 1 pour la main droite

- Un fichier `constantes.json` est présent qui contient des constantes par défaut. Ces constantes influencent le score total de votre disposition.
Précisement ce fichier `.json` contient des données de type *double* dans l'ordre : 
- 1ère valeur = une valeur neutre nécessaire au bon fonctionnement du programme : "1";
- à partir de la 2ème valeur jusqu'à la 6ème valeur on a les poids des doigts dans le calcul du score des unigrammes, dans l'ordre :
    - 2éme valeur = le doigt appuyant cette touche est un pouce : "1"
    - 3ème valeur = le doigt appuyant cette touche est un index : "0.8"
    - 4ème valeur = le doigt appuyant cette touche est un majeur : "0.9"
    - 5ème valeur = le doigt appuyant cette touche est un annulaire : "1.1"
    - 6ème valeur = le doigt appuyant cette touche est un auriculaire : "1.3"
Ces valeurs sont utilisées pour calculer une constante dédiée à un unigramme. Précisement, elles sont utilisées dans le calcul par rapport à la position de repos du doigt concerné. Ainsi si le doigt appuyant la touche correspondant à l'unigramme répresente sa position de repos, on renvoie la 7ème valeur ; sinon la constante rénvoyée sera le produit de la distance * le poids du doigt appuyant la touche. 
Ces différents poids répresentent la facilité d'utilisation d'un tel doigt sur une touche.

- 7ème valeur = valeur correspondant à un unigramme dont la touche se trouve à la position de repos du doigt : "0.7"
- 8ème valeur = valeur influençant la distance dans le calcul des unigrammes si celle-ci est supérieure à 1 : "1.3"
Cette valeur a un rôle de punition, une valeur > 1 est nécessaire.
- à partir de la 9ème valeur jusqu'à la 15ème valeur incluse on a les constantes influençant les scores des divers mouvements des bigrammes :
    - la 9ème valeur = valeur répresentant un mouvement alternant les 2 mains : "-0.3"
    - la 10ème valeur = valeur répresentant un roulement de l'éxterieur vers l'intérieur : "-0.4"
    - la 11ème valeur = valeur répresentant le roulement de l'intérieur vers l'éxterieur : "-0.2"
    La 9ème,10ème,11ème valeurs ont un rôle de compensation d'une disposition. Une valeur négative >=-1 et < 0 permet de répresenter cela. D'autres valeurs négatives peuvent être utilisés dans la cohérence avec le reste des valeurs.
    - la 12ème valeur = valeur répresentant un roulement qui n'est pas pur, c'est à dire un roulement pouvant être aussi un LSB/Ciseaux/SFB : "0.5"
    Cette valeur a un rôle de punition pour un bigramme qui est de plus LSB/SFB/Ciseaux. Elle sera par conséquent rajouté à la constante finale. 
    - la 13ème valeur = valeur répresentant un mouvement LSB : "1.9"
    - la 14ème valeur = valeur répresentant un mouvement SFB : "1.4"
    - la 15ème valeur = valeur répresentant un mouvement en Ciseaux : "1.7"
- à partir du la 16ème valeur jusqu'à la 18ème incluse on a les constantes influençant les scores des divers mouvements des trigrammes : 
    - la 16ème valeur = valeur répresentant une mauvaise redirection, c.à.d une redirection n'utilisant pas d'index : "2"
    - la 17ème valeur = valeur répresentant une redirection (qui n'en est pas une mauvaise) : "1.8"
    - la 18ème valeur = valeur répresentant skipgramme : "1.7"

-  à partir de la 19ème valeur jusqu'à la 21ème incluse, on a les valeurs des divers poids des n-grammes lors du calcul du score total : 
    - la 19ème valeur = valeur répresentant le poids des unigrammes dans le score total : "1"
    - la 20ème valeur = valeur répresentant le poids des bigrammes dans dans le score total : "0.8"
    - la 21ème valeur = valeur répresentant le poids des trigrammes dans le score totale : "0.6"

- les 3 derniers (22 à 24) valeurs répresentent les poids d'une distance à l'équilibre (répartition main gauche - droite) plus ou moins élévé de votre disposition sur un texte. Précisement, on rajoutera au score total la distance à l'équilibre obtenue sur un corpus * un de ces poids selon le cas :
    - la 22ème valeur = valeur répresentant une distance à l'équilibre égale à 0 ou dite parfaite : "-0.7"
    - la 23ème valeur = valeur répresentant une distance à l'équilibre <=2 : "2"
    - la 24ème valeur = valeur répresentant une distance à l'équilibre > 0.2 et <=0.5 : "10" 

Nous vous conseillons d'utiliser le fichier `constantes.json` par défaut si vous n'avez pas encore configuré votre fichier `.json` personnel des constantes. Toutesfois si vous voulez configurer votre propre fichier de constantes, veuillez bien suivre ce format au dessus.

Enfin, pour avoir un exemple, veuillez consulter ces fichiers :
- `config/azerty/azertyKeyCombinations.json` / `config/qwerty/qwerty.json` pour la configuration des ngramme en combinaisons de touches
- `config/azerty/azertyLayout.json` / `config/qwerty/qwertyLayout.json` pour la configuration de votre disposition de clavier sous forme .json
- `config/constantes.json` pour la configuration des constantes

## Tests JUnit5
Des tests ont été realisés dans 
/src/test/java/model 
grâce au framework **JUnit5**. Ces tests peuvent être initialisés avec la commande :
- `gradle test`

## Dépendances 
Ce projet utilise les bibliothéques suivantes :
* **Jackson** : permet la manipulation des fichiers .json :  
- com.fasterxml.jackson.core:jackson-core:2.18.2
- com.fasterxml.jackson.core:jackson-databind:2.18.2
- com.fasterxml.jackson.core:jackson-annotations:2.18.2

* **JUnit5** : Framework des tests unitaires.
- org.junit.jupiter:junit-jupiter-api:5.11.4
- org.junit.jupiter:junit-jupiter-engine:5.11.4
- org.junit.vintage:junit-vintage-engine:5.11.4
