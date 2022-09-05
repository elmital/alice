
package org.alicebot.ab;

public class Timer {
    private long startTimeMillis;

    public Timer() {
        this.start();
    }

    public void start() {
        this.startTimeMillis = System.currentTimeMillis();
    }

    public long elapsedTimeMillis() {
        return System.currentTimeMillis() - this.startTimeMillis + 1L;
    }

    public long elapsedRestartMs() {
        long ms = System.currentTimeMillis() - this.startTimeMillis + 1L;
        this.start();
        return ms;
    }

    public float elapsedTimeSecs() {
        return (float)this.elapsedTimeMillis() / 1000.0f;
    }

    public float elapsedTimeMins() {
        return this.elapsedTimeSecs() / 60.0f;
    }
}

