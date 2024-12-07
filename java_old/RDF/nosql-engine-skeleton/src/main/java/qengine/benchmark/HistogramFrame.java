package main.java.qengine.benchmark;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

import static java.lang.Math.max;

public class HistogramFrame extends JFrame {

    private final Map<Integer, Integer> data;

    /**
     * Constructeur de la fenêtre d'histogramme.
     *
     * @param data une map où la clé représente la taille des sous-ensembles
     *             et la valeur représente le nombre de sous-ensembles.
     */
    public HistogramFrame(Map<Integer, Integer> data) {
        this.data = data;
        setTitle("Histogramme des tailles de sous-ensembles");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        drawHistogram(g);
    }

    /**
     * Dessine l'histogramme dans la fenêtre.
     *
     * @param g l'objet Graphics pour dessiner.
     */
    private void drawHistogram(Graphics g) {
        int padding = 50; // Marge autour de l'histogramme
        int width = getWidth() - 2 * padding;
        int height = getHeight() - 2 * padding;

        // Obtenir les valeurs maximales pour normaliser les données
        int maxKey = data.keySet().stream().max(Integer::compare).orElse(1);
        int maxValue = data.values().stream().max(Integer::compare).orElse(1);

        // Taille des barres
        int barWidth = width / data.size();
        int startX = padding; // Position initiale pour les barres

        // Dessiner les axes X et Y
        g.setColor(Color.BLACK);
        // Axe Y
        g.drawLine(padding, padding, padding, height + padding);
        // Axe X
        g.drawLine(padding, height + padding, getWidth() - padding, height + padding);

        // Dessiner les barres
        for (Map.Entry<Integer, Integer> entry : data.entrySet()) {
            int key = entry.getKey();
            int value = entry.getValue();

            // Calculer la hauteur de la barre (normalisée)
            int barHeight = (int) ((double) value / maxValue * height);

            // Position Y de la barre
            int topBarY = height + padding - barHeight;

            // Dessiner la barre
            g.setColor(Color.BLUE);
            g.fillRect(startX, topBarY, barWidth - 5, barHeight);

            // Ajouter des étiquettes
            g.setColor(Color.BLACK);
            // Clé (taille) sous la barre
            g.drawString(String.valueOf(key), startX, height + padding + 20);
            // Valeur (nombre) au-dessus de la barre
            g.drawString(String.valueOf(value), startX, topBarY - 5);

            // Avancer la position X
            startX += barWidth;
        }
    }

}
