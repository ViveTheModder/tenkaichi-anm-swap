package cmd;
//Tenkaichi Animation Pack Object by ViveTheModder
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class AnmPak 
{
	public static final int NUM_ANMS = 440;
	private int[] positions = new int[NUM_ANMS+1];
	private RandomAccessFile pak;
	private String fileName="";
	
	public AnmPak(File f)
	{
		try
		{
			pak = new RandomAccessFile(f,"rw");
			fileName = f.getName();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	public boolean isValidAnmPak() throws IOException
	{
		pak.seek(0);
		int numPakContents = LittleEndian.getInt(pak.readInt());
		if (numPakContents<0) //prevent negative seek offset
		{
			numPakContents = LittleEndian.getInt(numPakContents); //reverse byte order
			cmd.Main.wiiMode=true;
		}
		else cmd.Main.wiiMode=false;
		
		pak.seek(1764);
		int expectedFileSize = LittleEndian.getInt(pak.readInt());
		int actualFileSize = (int)pak.length();
		if (numPakContents==440 && expectedFileSize==actualFileSize && fileName.toLowerCase().contains("anm")) return true;
		return false;
	}
	public String getFileName() 
	{
		return fileName;
	}
	public byte[] getAnmContents(int first, int last) throws IOException
	{
		int size = positions[last+1]-positions[first];
		byte[] anmContents = new byte[size];
		pak.seek(positions[first]);
		pak.read(anmContents);
		return anmContents;
	}
	public int[] getOldAnmPositions(int first, int last) throws IOException
	{
		int[] anmPositions = new int[last-first+1];
		pak.seek((first+1)*4);
		for (int i=0; i<anmPositions.length; i++) anmPositions[i]=LittleEndian.getInt(pak.readInt());
		return anmPositions;
	}
	public void setPositions() throws IOException
	{
		pak.seek(4);
		for (int i=0; i<positions.length; i++) positions[i]=LittleEndian.getInt(pak.readInt());
	}
	public void overwriteAnmPak(byte[] anmContents, int[] anmPos, int[] otherAnmPos, int first, int last) throws IOException
	{
		int next=last+1, selLength = positions[next]-positions[first];
		int difference = selLength-anmContents.length;
		if (difference!=0)
		{
			//initial index fix from next entry up until the file's last entry (the file size)
			pak.seek((next+1)*4);
			for (int i=next; i<=NUM_ANMS; i++) pak.writeInt(LittleEndian.getInt(positions[i]-difference));
			pak.seek(positions[next]);
			int fullFileSize = (int)pak.length();
			int restOfFileSize = fullFileSize - positions[next];
			byte[] restOfFile = new byte[restOfFileSize];
			pak.read(restOfFile); //copy the rest of the file contents before overwriting
			pak.seek(positions[first]);
			//actual overwriting process
			pak.write(anmContents);
			pak.write(restOfFile);
			pak.setLength(fullFileSize-difference);
		}
		else
		{
			pak.seek(positions[first]);
			pak.write(anmContents);
		}
		//last index fix from first selection entry to last selection entry
		pak.seek((first+1)*4);
		int posDiff=0;
		for (int i=0; i<otherAnmPos.length; i++)
		{
			if (i==0) posDiff = otherAnmPos[i]-anmPos[i];
			pak.writeInt(LittleEndian.getInt(otherAnmPos[i]-posDiff));
		}
		pak.close();
	}
}
