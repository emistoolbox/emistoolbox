package info.joriki.io;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ReverseDataOutputStream extends FilterOutputStream implements DataOutput
{
    DataOutputStream dos = new DataOutputStream(this);

    public ReverseDataOutputStream(OutputStream out) {
        super(out);
    }

    public final void writeChar(int v) throws IOException
    {
        this.out.write(v >>> 0);
        this.out.write(v >>> 8);
    }

    public final void writeShort(int v) throws IOException
    {
        this.out.write(v >>> 0);
        this.out.write(v >>> 8);
    }

    public final void writeInt(int v) throws IOException
    {
        this.out.write(v >>> 0);
        this.out.write(v >>> 8);
        this.out.write(v >>> 16);
        this.out.write(v >>> 24);
    }

    public final void writeLong(long v) throws IOException
    {
        writeInt((int) v);
        writeInt((int) (v >>> 32));
    }

    public final void writeFloat(float v) throws IOException
    {
        writeInt(Float.floatToIntBits(v));
    }

    public final void writeDouble(double v) throws IOException
    {
        writeLong(Double.doubleToLongBits(v));
    }

    public final void writeBoolean(boolean v) throws IOException
    {
        this.dos.writeBoolean(v);
    }

    public final void writeByte(int v) throws IOException
    {
        this.dos.writeByte(v);
    }

    public final void writeBytes(String v) throws IOException
    {
        this.dos.writeBytes(v);
    }

    public final void writeUTF(String v) throws IOException
    {
        this.dos.writeUTF(v);
    }

    public final void writeChars(String v) throws IOException
    {
        int len = v.length();
        for (int i = 0; i < len; i++)
            writeChar(v.charAt(i));
    }
}

/*
 * Location: D:\work\emistoolbox\source\core\resources\WEB-INF\classes\
 * Qualified Name: info.joriki.io.ReverseDataOutputStream JD-Core Version: 0.6.0
 */