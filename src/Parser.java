import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
	
	private int mEdgeCount;
	private int mVertCount;
	private int[][] mCostMatrix;
	
	public Parser(String filename) {
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			
			String tmp = "1";
			
			while (tmp != "")
			{
				tmp = br.readLine();
				
				if(tmp == null) { 
					return;
				}
				
				switch (tmp.charAt(0))
				{
				case 'c':
				{
					continue;
				}
				case 'p':
				{
					String line = tmp;

					int n = line.indexOf(" ");
					if (n != 0 && n < line.length())
					{
						line = line.substring(n + 1, line.length());
					}

					n = line.indexOf(" ");
					if (n != 0 && n < line.length())
					{
						line = line.substring(n + 1, line.length());
					}

					n = line.indexOf(" ");
					if (n != 0 && n < line.length())
					{
						String c = line.substring(0, n);
						mVertCount = Integer.parseInt(c);
						line = line.substring(n + 1, line.length());
					}
					mEdgeCount = Integer.parseInt(line);

					if (mVertCount > 0)
					{
						mCostMatrix = new int[mVertCount][mVertCount];
						for(int i = 0; i < mVertCount; i++) {
							for(int j = 0; j < mVertCount; j++) {
								mCostMatrix[i][j] = 0;
							}
						}
					}

					break;
				}
				case 'e':
				{
					String line = tmp;
					
					int v1 = 0;
					int v2 = 0;

					int n = line.indexOf(" ");
					if (n != 0 && n < line.length())
					{
						line = line.substring(n + 1, line.length());
					}

					n = line.indexOf(" ");
					if (n != 0 && n < line.length())
					{
						String c = line.substring(0, n);
						v1 = Integer.parseInt(c);
						line = line.substring(n + 1, line.length());
					}
					v2 = Integer.parseInt(line);

					mCostMatrix[v1 - 1][v2 - 1] = 1;
					mCostMatrix[v2 - 1][v1 - 1] = 1;

					break;
				}
				default:

					break;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void print() {
		for(int i = 0; i < mVertCount; i++) {
			for(int j = 0; j < mVertCount; j++) {
				System.out.print(mCostMatrix[i][j]);
			}
			System.out.println("");
		}
	}

	public int getEdgeCount() {
		return mEdgeCount;
	}

	public int getVertCount() {
		return mVertCount;
	}

	public int[][] getCostMatrix() {
		return mCostMatrix;
	}
}
