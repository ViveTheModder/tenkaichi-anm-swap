package cmd;
//Tenkaichi ANM Swap (CMD) by ViveTheModder
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main 
{
	public static boolean wiiMode;
	public static void swap(AnmPak pak1, AnmPak pak2, int[] anmIds) throws IOException
	{
		pak1.setPositions(); pak2.setPositions();
		int anmSelFirst1=anmIds[0], anmSelFirst2=anmIds[2];
		int anmSelLast1=anmIds[1], anmSelLast2=anmIds[3];
		
		int[] oldAnmPos1 = pak1.getOldAnmPositions(anmSelFirst1, anmSelLast1);
		int[] oldAnmPos2 = pak2.getOldAnmPositions(anmSelFirst2, anmSelLast2);
		byte[] anmContents1 = pak1.getAnmContents(anmSelFirst1, anmSelLast1);
		byte[] anmContents2 = pak2.getAnmContents(anmSelFirst2, anmSelLast2);
		
		pak1.overwriteAnmPak(anmContents2, oldAnmPos1, oldAnmPos2, anmSelFirst1, anmSelLast1);
		pak2.overwriteAnmPak(anmContents1, oldAnmPos2, oldAnmPos1, anmSelFirst2, anmSelLast2);
	}
	public static AnmPak[] getAnmPaks() throws IOException
	{
		AnmPak[] paks = new AnmPak[2];
		Scanner sc = new Scanner(System.in);
		for (int i=0; i<2; i++)
		{
			while (paks[i]==null)
			{
				System.out.println("Enter a valid path to an animation pack (ANM.PAK):");
				String path = sc.nextLine();
				File tmpFile = new File(path);
				if (tmpFile.isFile())
				{
					AnmPak tmpPak = new AnmPak(tmpFile);
					if (tmpPak.isValidAnmPak()) paks[i]=tmpPak;
					else System.out.println("Invalid PAK file. Try again!");
				}
				else System.out.println("Provided path does NOT point to any file. Try again!");
			}
		}
		sc.close();
		return paks;
	}
	public static void main(String[] args)
	{
		try
		{
			AnmPak[] paks=null;
			int[] anmIds = {-1,-1,-1,-1};
			String helpMsg = "Valid usage: java -jar tenkaichi-anm-swap.jar [arg1] [arg2] [arg3] [arg4]\n"
			+ "* arg1 -> ANM ID (Selection Start for ANM PAK 1),\n* arg2 -> ANM ID (Selection End for ANM PAK 1),\n"
			+ "* arg3 -> ANM ID (Selection Start for ANM PAK 2),\n* arg4 -> ANM ID (Selection End for ANM PAK 2).";
			
			if (args.length>=4)
			{
				for (int i=0; i<4; i++)
				{
					if (args[i].matches("\\d+"))
					{
						int id = Integer.parseInt(args[i]);
						if (id<AnmPak.NUM_ANMS) anmIds[i]=id;
						else 
						{
							System.out.println("Argument "+(i+1)+" is an invalid animation ID that is greater than 440!");
							System.exit(1);
						}
					}
					else 
					{
						System.out.println("Argument "+(i+1)+" is not a valid animation ID! Enter a number between 0 and 439.");
						System.exit(2);
					}
				}
				int numSelAnms1 = anmIds[1]-anmIds[0]+1, numSelAnms2 = anmIds[3]-anmIds[2]+1;
				if (numSelAnms1>=2 || numSelAnms2>=2)
				{
					if (numSelAnms1!=numSelAnms2) 
					{
						System.out.println("ANM selection sizes do NOT match (Sel. 1: "+numSelAnms1+", Sel. 2: "+numSelAnms2+").");
						System.exit(3);
					}
					else 
					{
						paks=getAnmPaks();
						long start = System.currentTimeMillis();
						swap(paks[0],paks[1],anmIds);
						long end = System.currentTimeMillis();
						double time = (end-start)/1000.0;
						System.out.println("Animation Swap done in "+time+" s!");
					}
				}
				else
				{
					System.out.println("At least one ANM selection is below 2 animations.");
					System.exit(4);
				}
			}
			else if (args.length>0)
			{
				String msg = "Not enough arguments provided!\n";
				if (args[0].equals("-h")) msg="";
				System.out.println(msg+helpMsg);
			}
			else gui.Main.main(args);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}