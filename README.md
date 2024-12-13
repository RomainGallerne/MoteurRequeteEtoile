# Plan Expe :

- Une nouvelle JVM par expe
- Chauffer la JVM avec 20% des requêtes
- Permutation des query avant interrogation
- Même requêtes dans le même ordre
- Tps de réponse (utilisateur et réel)
- Tester sur plusieurs hardwares

-----------------------------------------------------------------
## Pré-requis:

### :part_alternation_mark: Installation de maven:
Executer la commande: `sudo apt-get -y install maven`

### :coffee: Installation de java:
Installer une version de JAVA qui est au minimum la version [21](https://www.oracle.com/fr/java/technologies/javase/jdk21-archive-downloads.html)

Un détail de la procédure sur linux est disponible [ici](https://linuxconfig.org/how-to-install-and-switch-java-versions-on-ubuntu-linux)


-----------------------------------------------------------------

 - Commande pour créer le JAR: `mvn clean install`

NB: Le Jar utilisable est celui avec la mention "jar-with-dependencies"

Il se trouve dans le dossier target

## Comment recompiler le JAR:

- Supprimer le répertoire target
- Executer la commande: `mvn clean install`

:point_right: Le JAR qui nous intéresse est celui qui possède la notion `jar-with-dependencies`

-----------------------------------------------------------------

 - Commande pour executer le JAR:
```bash 
java -Xms512m -Xmx2g -jar qengine_RGRPO.jar
```

-----------------------------------------------------------------

# Infos Hardware: 

 - Commande pour connaitre ses infos: `sudo lshw -short`

 ## Richard: 
    - 32Go RAM 
    - 13th Gen Intel(R) Core(TM) i7-13700H
    - GeForce RTX 4050

## Romain:
    - 16GiB RAM
    - Intel(R) Core(TM) Ultra 5 135H
