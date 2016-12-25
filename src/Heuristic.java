
public class Heuristic {
	
	public static final int VERT_VECTOR_SIZE = 3;
	public static final int VERT_NUMBER = 0;
	public static final int VERT_DEGREE = 1;
	public static final int VERT_COLOR = 2;

	private int[][] mVert;
	private int[][] mpCostMatrix;
	private int mnColorCount;
	
	public Heuristic(int[][] pCostMatrix) {
		mpCostMatrix = pCostMatrix.clone();
		
		mVert = new int[mpCostMatrix.length][mpCostMatrix.length];
		
		for (int i = 0; i < mpCostMatrix.length; i++) {
			for (int j = 0; j < mpCostMatrix.length; j++) {
				mVert[i][j] = 0;
			}
		}

		for (int i = 0; i < mpCostMatrix.length; i++)
		{
			mVert[i][0] = i;
			for (int j = 0; j < mpCostMatrix.length; j++)
			{
				mVert[i][1] += mpCostMatrix[i][j];
			}
			mVert[i][2] = -1;
		}
	}
	
	public int[][] getHeuristic() {
		sortVert();

		coloring();

		return mVert;
	}
	
	public void printVert() {
		for (int i = 0; i < mpCostMatrix.length; i++)
		{
			for (int j = 0; j < mVert[i].length; j++)
			{
				if (j == VERT_NUMBER)
				{
					System.out.print(mVert[i][j] + 1 + "\t");
				}
				else
				{
					System.out.print(mVert[i][j] + "\t");
				}
			}

			System.out.println();
		}
	}
	
	public int getColorCount() {
		return mnColorCount;
	}
	
	private void sortVert() {
		for (int i = 0; i < mVert.length; i++)
		{
			for (int j = i; j < mVert.length; j++)
			{
				if (mVert[i][VERT_DEGREE] < mVert[j][VERT_DEGREE])
				{
					int[] tmp = mVert[i];
					mVert[i] = mVert[j];
					mVert[j] = tmp;
				}
			}
		}
	}
	
	private void coloring() {
		int color = 0;
		while (!isAllColoring())
		{
			for (int i = 0; i < mVert.length; i++)
			{
				int k = mVert[i][VERT_NUMBER] + 1;
				if (mVert[i][VERT_COLOR] < 0 && needDraw(i, color))
				{
					mVert[i][VERT_COLOR] = color;
				}
			}
			color++;
		}
		mnColorCount = color;
	}
	
	private boolean needDraw(int i, int color) {
		for (int j = 0; j < mVert.length; j++)
		{
			if (mVert[j][VERT_COLOR] == color)
			{
				if (mpCostMatrix[mVert[i][VERT_NUMBER]][mVert[j][VERT_NUMBER]] != 0)
				{
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean isAllColoring() {
		for (int i = 0; i < mVert.length; i++)
		{
			if (mVert[i][VERT_COLOR] < 0)
			{
				return false;
			}
		}

		return true;
	}
	
}
