package qengine.benchmark;

import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.TreeMap;

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

        int maxValue = data.values().stream().max(Integer::compare).orElse(1);
        Map<Integer, Integer> sortedData = new TreeMap<>(data);

        int barWidth = width / data.size();
        int startX = padding; // Position initiale pour les barres

        g.setColor(Color.BLACK);
        g.drawLine(padding, padding, padding, height + padding);
        g.drawLine(padding, height + padding, getWidth() - padding, height + padding);

        for (Map.Entry<Integer, Integer> entry : sortedData.entrySet()) {
            int key = entry.getKey();
            int value = entry.getValue();
            int barHeight = (int) ((double) value / maxValue * height);
            int topBarY = height + padding - barHeight;

            g.setColor(Color.BLUE);
            g.fillRect(startX, topBarY, barWidth - 5, barHeight);
            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(key), startX, height + padding + 20);
            g.drawString(String.valueOf(value), startX, topBarY - 5);

            startX += barWidth;
        }
    }

}
