#!/bin/bash

# Définir les chemins des fichiers JAR
JAR_FILES=(
    "qengine_500k.jar"
    "qengine_500k_unif.jar"
    "qengine_2M.jar"
    "qengine_2M_unif.jar"
)

# Boucler à travers chaque fichier JAR et l'exécuter avec java
for JAR in "${JAR_FILES[@]}"; do
    echo "$JAR 4g"
    java -jar "$JAR" -Xms512m -Xmx4g
    echo "$JAR 8g"
    java -jar "$JAR" -Xms512m -Xmx8g

    # Vérifiez si l'exécution a réussi
    if [ $? -ne 0 ]; then
        echo "Erreur lors de l'exécution de $JAR. Arrêt du script."
        exit 1
    fi
done

echo "FIN."
