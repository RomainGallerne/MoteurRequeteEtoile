#!/bin/bash

# Répertoire contenant les fichiers .queryset
QUERY_DIR="testsuite/queries"

# Fichier de sortie
MERGED_FILE="merged.queryset"

# Mode verbose désactivé par défaut
VERBOSE=false

# Traitement des arguments
for arg in "$@"; do
  case $arg in
    --verbose)
      VERBOSE=true
      shift
      ;;
    *)
      echo "Option inconnue : $arg"
      echo "Usage : $0 [--verbose]"
      exit 1
      ;;
  esac
done

# Vérification que le répertoire existe
if [[ ! -d "$QUERY_DIR" ]]; then
  echo "Le répertoire $QUERY_DIR n'existe pas. Vérifiez le chemin."
  exit 1
fi

# Vérification qu'il y a des fichiers .queryset
if [[ -z $(ls "$QUERY_DIR"/*.queryset 2>/dev/null) ]]; then
  echo "Aucun fichier .queryset trouvé dans $QUERY_DIR."
  exit 1
fi

# Supprimer le fichier de sortie existant s'il existe
if [[ -f "$MERGED_FILE" ]]; then
  echo "Le fichier $MERGED_FILE existe déjà. Suppression en cours..."
  rm "$MERGED_FILE"
fi

# Recréer un fichier vide
touch "$MERGED_FILE"

# Mélanger les fichiers .queryset
echo "Mélange des fichiers .queryset en cours..."
find "$QUERY_DIR" -type f -name "*.queryset" | shuf | while read -r file; do
  if $VERBOSE; then
    echo "# Début de $file" >> "$MERGED_FILE"
  fi
  cat "$file" >> "$MERGED_FILE"
  if $VERBOSE; then
    echo -e "\n# Fin de $file\n" >> "$MERGED_FILE"
  fi
done

echo "Les fichiers .queryset ont été mélangés et fusionnés dans $MERGED_FILE."

