package qengine.benchmark;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class ExecutionTimer {

    private final ThreadMXBean threadMXBean;
    private long startRealTime;
    private long startCpuTime;
    private long startUserTime;

    public ExecutionTimer() {
        threadMXBean = ManagementFactory.getThreadMXBean();
        if (!threadMXBean.isThreadCpuTimeSupported()) {
            throw new UnsupportedOperationException("La mesure du temps CPU n'est pas supportée sur cette JVM.");
        }
    }

    /**
     * Démarre le chronométrage.
     */
    public void start() {
        startRealTime = System.nanoTime();
        startCpuTime = threadMXBean.getCurrentThreadCpuTime();
        startUserTime = threadMXBean.getCurrentThreadUserTime();
    }

    /**
     * Arrête le chronométrage et retourne un rapport des temps écoulés.
     *
     * @return un rapport contenant les temps réel, CPU et utilisateur en millisecondes.
     */
    public TimerReport stop() {
        long endRealTime = System.nanoTime();
        long endCpuTime = threadMXBean.getCurrentThreadCpuTime();
        long endUserTime = threadMXBean.getCurrentThreadUserTime();

        return new TimerReport(
                (endRealTime - startRealTime) / 1_000_000, // Temps réel en ms
                (endCpuTime - startCpuTime) / 1_000_000,   // Temps CPU en ms
                (endUserTime - startUserTime) / 1_000_000  // Temps utilisateur en ms
        );
    }

    /**
     * Classe interne pour représenter un rapport de temps.
     */
    public static class TimerReport {
        private final long realTime;
        private final long cpuTime;
        private final long userTime;

        public TimerReport(long realTime, long cpuTime, long userTime) {
            this.realTime = realTime;
            this.cpuTime = cpuTime;
            this.userTime = userTime;
        }

        public long getRealTime() {
            return realTime;
        }

        public long getCpuTime() {
            return cpuTime;
        }

        public long getUserTime() {
            return userTime;
        }

        @Override
        public String toString() {
            return String.format(
                    "Temps réel : %d ms, Temps CPU : %d ms, Temps utilisateur : %d ms",
                    realTime, cpuTime, userTime
            );
        }
    }
}

