import java.awt.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.quantego.clp.CLP;
import com.quantego.clp.CLPConstraint;
import com.quantego.clp.CLP.ALGORITHM;
import com.quantego.clp.CLPExpression;
import com.quantego.clp.CLPVariable;

public class BranchAndPrice {
	
	private int[][] mpCostMatrix;
	private int[][] mpInvertedCostMatrix;
	private int mnColorCount;
	private int[][] mHeuristicVert;
	private int[][] mVert;
	private ArrayList<ArrayList<Integer>> mWidthVert;
	double mPi[];
	
	private CLP mModel;
	private CLPVariable[] mCLPVar;
	private CLPConstraint[] mClpConstraints;
	
	public BranchAndPrice(int[][] costMatrix, int[][] vert, int colorCount) {
		mpCostMatrix = costMatrix;
		mnColorCount = colorCount;
		mHeuristicVert = vert;
		int[][] mVert = new int[mHeuristicVert.length][3];
		
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
		mClpConstraints = new CLPConstraint[mHeuristicVert.length];
		
		for(int i = 0; i < mnColorCount; i++) {
			mCLPVar[i] = mModel.addVariable().lb(0);
		}
		
		for(int i = 0; i < mHeuristicVert.length; i++) {
			CLPExpression exp = mModel.createExpression();
			
			for(int j = 0; j < mnColorCount; j++) {
				if(mWidthVert.get(j).indexOf(i) >= 0) {
					exp.add(mCLPVar[j]);
				}
			}
			
			mClpConstraints[i] = exp.geq(1);
		}
		
		mModel.printModel();
		
		HashMap<CLPVariable, Double> objFunc = new HashMap<CLPVariable, Double>();
		for (CLPVariable var : mCLPVar) {
			objFunc.put(var, 1.0);
		}
		int i = 0;
		
		mPi = new double[mHeuristicVert.length];
		for (CLPConstraint constr : mClpConstraints) {
			mModel.addObjective(objFunc, 0);
			mModel.minimize();
			double piStar = mModel.getDualSolution(constr);
			mPi[i] = piStar;
			System.out.print("i" + (i + 1) + " = "
					+ piStar + "\t");
		}
		System.out.println();
		
		double sumPi = getSumPiWithStar();
		System.out.println("Sum pi with star: " + sumPi);
		
		if (sumPi > 1) {
			runCliquer();
		}
		//if not good enough : traverse tree(); 
	}
	
	private double getSumPiWithStar() {
		double sumPiWithStar = 0;
		ArrayList<Integer> maxIndependentSet = new ArrayList<>();
		
		int index = 0;
		int maxLength = 0;
		for(int i = 0; i < mWidthVert.size(); i++) {
			if(maxLength < mWidthVert.get(i).size()) {
				maxLength = mWidthVert.get(i).size();
				index = i;
			}
		}
		
		for (Integer vertex : mWidthVert.get(index)) {
			sumPiWithStar += mPi[vertex];
		}
		return sumPiWithStar;
	}
	
	private void runCliquer() {
		initInvertedAdjacencyMatrix();
		invokeCliquer();
	}

	private void invokeCliquer() {
		ArrayList<Edge> edges = new ArrayList<Edge>();
		for (int i = 0; i < mHeuristicVert.length; i++) {
			for (int j = i + 1; j < mHeuristicVert.length; j++) {
				if (mpInvertedCostMatrix[i][j] > 0) {
					edges.add(new Edge(i, j));
				}
			}
		}
		
		float[] weights = new float[mHeuristicVert.length];
		for (int i = 0; i < mHeuristicVert.length; i++) {
			weights[i] = (float) mPi[i];
		}
		int[] clique = CliquerWrapper.getInstance().findClique(edges, weights);
        System.out.println(Arrays.toString(clique));
	}
	
	private void initInvertedAdjacencyMatrix() {
		mpInvertedCostMatrix = mpCostMatrix.clone();
		for (int i = 0; i < mHeuristicVert.length; i++) {
			for (int j = 0; j < mHeuristicVert.length; j++) {
				if (i == j) {
					continue;
				}
				if(mpInvertedCostMatrix[i][j] > 0) {
					mpInvertedCostMatrix[i][j] = 0;
				} else {
					mpInvertedCostMatrix[i][j] = 1;
				}
			}
		}
	}
	
	private void printVert() {
		for (int i = 0; i < mHeuristicVert.length; i++)
		{
			for (int j = 0; j < mHeuristicVert[i].length; j++)
			{
				if (j == Heuristic.VERT_NUMBER)
				{
					System.out.print(mHeuristicVert[i][j] + 1 + "\t");
				}
				else
				{
					System.out.print(mHeuristicVert[i][j] + "\t");
				}
			}

			System.out.println();
		}
	}
	
	private void getWidthVariables() {
		
		mWidthVert = new ArrayList<ArrayList<Integer>>();
		
		for(int i = 0; i < mnColorCount; i++) {
			mWidthVert.add(new ArrayList<Integer>());
		}
		
		for(int i = 0; i < mnColorCount; i++) {
			for(int j = 0; j < mHeuristicVert.length; j++) {
				if(mHeuristicVert[j][Heuristic.VERT_COLOR] == i) {
					mWidthVert.get(i).add(mHeuristicVert[j][Heuristic.VERT_NUMBER]);
				} else if(hasntVert(j, i)) {
					mWidthVert.get(i).add(mHeuristicVert[j][Heuristic.VERT_NUMBER]);
				}
			}
		}
	}
	
	private boolean hasntVert(int i, int color) {
		for (int j = 0; j < mHeuristicVert.length; j++)
		{
			if (mHeuristicVert[j][Heuristic.VERT_COLOR] == color)
			{
				if (mpCostMatrix[mHeuristicVert[i][Heuristic.VERT_NUMBER]][mHeuristicVert[j][Heuristic.VERT_NUMBER]] != 0)
				{
					return false;
				}
			}
		}
		for (int j = 0; j < mWidthVert.get(color).size(); j++)
		{
			if (mpCostMatrix[mWidthVert.get(color).get(j)][mHeuristicVert[i][Heuristic.VERT_NUMBER]] != 0)
			{
				return false;
			}
		}
		
		return true;
	}
}
