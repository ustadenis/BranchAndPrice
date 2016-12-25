import java.awt.List;
import java.util.ArrayList;

import com.quantego.clp.CLP;
import com.quantego.clp.CLPConstraint;
import com.quantego.clp.CLP.ALGORITHM;
import com.quantego.clp.CLPExpression;
import com.quantego.clp.CLPVariable;

public class BranchAndPrice {
	
	private int[][] mpCostMatrix;
	private int mnColorCount;
	private int[][] mVert;
	private ArrayList<ArrayList<Integer>> mWidthVert;
	
	private CLP mModel;
	private CLPVariable[] mCLPVar;
	private CLPConstraint[] mClpConstraints;
	
	public BranchAndPrice(int[][] costMatrix, int[][] vert, int colorCount) {
		mpCostMatrix = costMatrix;
		mnColorCount = colorCount;
		mVert = vert;
		
		mModel = new CLP().algorithm(ALGORITHM.DUAL).presolve(true);
		
		getWidthVariables();
	}
	
	public void printWidthVert() {
		for (int i = 0; i < mWidthVert.size(); i++)
		{
			for (int j = 0; j < mWidthVert.get(i).size(); j++)
			{
				System.out.print(mWidthVert.get(i).get(j) + 1 + "\t");
			}
			System.out.println();
		}
	}
	
	public void useSolver() {
		mCLPVar = new CLPVariable[mnColorCount];
		mClpConstraints = new CLPConstraint[mVert.length];
		
		for(int i = 0; i < mnColorCount; i++) {
			mCLPVar[i] = mModel.addVariable().lb(0);
		}
		
		for(int i = 0; i < mVert.length; i++) {
			CLPExpression exp = mModel.createExpression();
			
			for(int j = 0; j < mnColorCount; j++) {
				if(mWidthVert.get(j).indexOf(i) >= 0) {
					exp.add(mCLPVar[j]);
				}
			}
			
			mClpConstraints[i] = exp.geq(1);
		}
		
		mModel.printModel();
		
		for(int i = 0; i < mnColorCount; i++) {
			System.out.print(mModel.getSolution(mCLPVar[i]) + "\t");
		}
		
		System.out.println();
		
		mModel.solve();
		
		for(int i = 0; i < mnColorCount; i++) {
			System.out.print(mModel.getSolution(mCLPVar[i]) + "\t");
		}
		
		System.out.println();
		
		for(int i = 0; i < mVert.length; i++) {
			System.out.print(mModel.getDualSolution(mClpConstraints[i]) + "\t");
		}
	}
	
	private void getWidthVariables() {
		
		mWidthVert = new ArrayList<ArrayList<Integer>>();
		
		for(int i = 0; i < mnColorCount; i++) {
			mWidthVert.add(new ArrayList<Integer>());
		}
		
		for(int i = 0; i < mnColorCount; i++) {
			for(int j = 0; j < mVert.length; j++) {
				if(mVert[j][Heuristic.VERT_COLOR] == i) {
					mWidthVert.get(i).add(mVert[j][Heuristic.VERT_NUMBER]);
				} else if(hasntVert(j, i)) {
					mWidthVert.get(i).add(mVert[j][Heuristic.VERT_NUMBER]);
				}
			}
		}
	}
	
	private boolean hasntVert(int i, int color) {
		for (int j = 0; j < mVert.length; j++)
		{
			if (mVert[j][Heuristic.VERT_COLOR] == color)
			{
				if (mpCostMatrix[mVert[i][Heuristic.VERT_NUMBER]][mVert[j][Heuristic.VERT_NUMBER]] != 0)
				{
					return false;
				}
			}
		}
		for (int j = 0; j < mWidthVert.get(color).size(); j++)
		{
			if (mpCostMatrix[mWidthVert.get(color).get(j)][mVert[i][Heuristic.VERT_NUMBER]] != 0)
			{
				return false;
			}
		}
		
		return true;
	}
}
