
package org.alicebot.ab;

import org.alicebot.ab.MagicNumbers;
import org.alicebot.ab.MagicStrings;

public class History<T> {
    private Object[] history;
    private String name;

    public History() {
        this("unknown");
    }

    public History(String name) {
        this.name = name;
        this.history = new Object[MagicNumbers.max_history];
    }

    public void add(T item) {
        for (int i = MagicNumbers.max_history - 1; i > 0; --i) {
            this.history[i] = this.history[i - 1];
        }
        this.history[0] = item;
    }

    public T get(int index) {
        if (index < MagicNumbers.max_history) {
            if (this.history[index] == null) {
                return null;
            }
            return (T)this.history[index];
        }
        return null;
    }

    public String getString(int index) {
        if (index < MagicNumbers.max_history) {
            if (this.history[index] == null) {
                return MagicStrings.unknown_history_item;
            }
            return (String)this.history[index];
        }
        return null;
    }

    public void printHistory() {
        int i = 0;
        while (this.get(i) != null) {
            System.out.println(this.name + "History " + (i + 1) + " = " + this.get(i));
            System.out.println(String.valueOf(this.get(i).getClass()).contains("History"));
            if (String.valueOf(this.get(i).getClass()).contains("History")) {
                ((History)this.get(i)).printHistory();
            }
            ++i;
        }
    }
}

