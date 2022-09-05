
package org.alicebot.ab;

import java.util.Set;
import org.alicebot.ab.ParseState;
import org.w3c.dom.Node;

public interface AIMLProcessorExtension {
    public Set<String> extensionTagSet();

    public String recursEval(Node var1, ParseState var2);
}

