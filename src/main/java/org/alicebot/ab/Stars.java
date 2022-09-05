
package org.alicebot.ab;

import java.util.ArrayList;

public class Stars
extends ArrayList<String> {
    public String star(int i) {
        if (i < this.size()) {
            return (String)this.get(i);
        }
        return null;
    }
}

