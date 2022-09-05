/*
 * The MIT License
 *
 * Copyright (c) 2011 Takeru Ohta <phjgt308@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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

