package info.joriki.io;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ReverseDataInputStream extends FilterInputStream implements DataInput
{
    DataInputStream dis = new DataInputStream(this);

    public ReverseDataInputStream(InputStream in) {
        super(in);
    }

    public byte readByte() throws IOException
    {
        return this.dis.readByte();
    }

    public char readChar() throws IOException
    {
        char result = (char) this.dis.readUnsignedByte();
        result = (char) (result | this.dis.readUnsignedByte() << 8);
        return result;
    }

    public double readDouble() throws IOException
    {
        long l = readLong(); 
        return Double.longBitsToDouble(l);
    }

    public float readFloat() throws IOException
    {
        return Float.intBitsToFloat(readInt());
    }

    public int readInt() throws IOException
    {
        int result = this.dis.readUnsignedByte() << 0;
        result |= this.dis.readUnsignedByte() << 8;
        result |= this.dis.readUnsignedByte() << 16;
        result |= this.dis.readUnsignedByte() << 24;
        return result;
    }

    public int readUnsignedByte() throws IOException
    {
        return this.dis.readUnsignedByte();
    }

    public int readUnsignedShort() throws IOException
    {
        return readShort() & 0xFFFF;
    }

    public long readLong() throws IOException
    {
        return (long) readInt() & 0xFFFFFFFFl | (long) readInt() << 32l;
    }

    public short readShort() throws IOException
    {
        short result = (short) this.dis.readUnsignedByte();
        result = (short) (result | this.dis.readUnsignedByte() << 8);
        return result;
    }

    public boolean readBoolean() throws IOException
    {
        return this.dis.readBoolean();
    }

    public int skipBytes(int n) throws IOException
    {
        return this.dis.skipBytes(n);
    }

    public void readFully(byte[] b) throws IOException
    {
        this.dis.readFully(b);
    }

    public void readFully(byte[] b, int off, int len) throws IOException
    {
        this.dis.readFully(b, off, len);
    }

    public String readLine() throws IOException
    {
        return new BufferedReader(new InputStreamReader(this.in)).readLine();
    }

    public String readUTF() throws IOException
    {
        return this.dis.readUTF();
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: info.joriki.io.ReverseDataInputStream JD-Core Version: 0.6.0
 */