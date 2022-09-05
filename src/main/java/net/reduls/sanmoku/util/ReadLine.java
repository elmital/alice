
package net.reduls.sanmoku.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ReadLine {
    private final BufferedReader br;

    public ReadLine(InputStream inputStream) throws IOException {
        this.br = new BufferedReader(new InputStreamReader(inputStream));
    }

    public void close() {
        try {
            this.br.close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
    }

    public String read() throws IOException {
        return this.br.readLine();
    }
}

