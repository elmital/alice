
/* Program AB Reference AIML 2.0 implementation
        Copyright (C) 2013 ALICE A.I. Foundation
        Contact: info@alicebot.org
        This library is free software; you can redistribute it and/or
        modify it under the terms of the GNU Library General Public
        License as published by the Free Software Foundation; either
        version 2 of the License, or (at your option) any later version.
        This library is distributed in the hope that it will be useful,
        but WITHOUT ANY WARRANTY; without even the implied warranty of
        MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
        Library General Public License for more details.
        You should have received a copy of the GNU Library General Public
        License along with this library; if not, write to the
        Free Software Foundation, Inc., 51 Franklin St, Fifth Floor,
        Boston, MA  02110-1301, USA.
*/

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

