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

package net.reduls.sanmoku.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public final class Misc {
    public static InputStream openDictionaryData(String string) {
        return Misc.class.getResourceAsStream("/net/reduls/sanmoku/dicdata/" + string);
    }

    public static DataInputStream openDictionaryDataAsDIS(String string) {
        return new DataInputStream(new BufferedInputStream(Misc.openDictionaryData(string), 80960));
    }

    public static BufferedReader openDictionaryDataAsBR(String string) {
        try {
            return new BufferedReader(new InputStreamReader(Misc.openDictionaryData(string), "UTF-8"), 80960);
        }
        catch (IOException iOException) {
            throw new AssertionError((Object)iOException.getMessage());
        }
    }

    public static String readLine(BufferedReader bufferedReader) {
        try {
            return bufferedReader.readLine();
        }
        catch (IOException iOException) {
            throw new AssertionError((Object)iOException.getMessage());
        }
    }

    public static void close(Closeable closeable) {
        try {
            closeable.close();
        }
        catch (IOException iOException) {
            throw new AssertionError((Object)iOException.getMessage());
        }
    }

    public static long readLong(DataInput dataInput) {
        try {
            return dataInput.readLong();
        }
        catch (IOException iOException) {
            throw new AssertionError((Object)iOException.getMessage());
        }
    }

    public static int readInt(DataInput dataInput) {
        try {
            return dataInput.readInt();
        }
        catch (IOException iOException) {
            throw new AssertionError((Object)iOException.getMessage());
        }
    }

    public static short readShort(DataInput dataInput) {
        try {
            return dataInput.readShort();
        }
        catch (IOException iOException) {
            throw new AssertionError((Object)iOException.getMessage());
        }
    }

    public static byte readByte(DataInput dataInput) {
        try {
            return dataInput.readByte();
        }
        catch (IOException iOException) {
            throw new AssertionError((Object)iOException.getMessage());
        }
    }

    public static char readChar(DataInput dataInput) {
        try {
            return dataInput.readChar();
        }
        catch (IOException iOException) {
            throw new AssertionError((Object)iOException.getMessage());
        }
    }

    public static byte[] readBytesFromFile(String string, int n) {
        DataInputStream dataInputStream = Misc.openDictionaryDataAsDIS(string);
        int n2 = Misc.readInt(dataInputStream);
        byte[] arrby = new byte[n2 * n];
        try {
            dataInputStream.readFully(arrby, 0, arrby.length);
        }
        catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        return arrby;
    }

    public static byte[] readBytesFromFile(String string, int n, int n2) {
        DataInputStream dataInputStream = Misc.openDictionaryDataAsDIS(string);
        byte[] arrby = new byte[n * n2];
        try {
            dataInputStream.readFully(arrby, 0, arrby.length);
        }
        catch (Exception exception) {
            throw new RuntimeException(exception);
        }
        return arrby;
    }

    public static int readIntFromFile(String string) {
        DataInputStream dataInputStream = Misc.openDictionaryDataAsDIS(string);
        int n = Misc.readInt(dataInputStream);
        Misc.close(dataInputStream);
        return n;
    }
}

