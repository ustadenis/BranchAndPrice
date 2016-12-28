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
	ArrayList<Float> mPi;
	ArrayList<Integer> maxIndependentVerts;
	
	private CLP mModel;
	private CLPVariable[] mCLPVar;
	private ArrayList<CLPConstraint> mClpConstraints;
	
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
				System.out.print(mWidthVert.get(i).get(j) + "\t");
			}
			System.out.println();
		}
	}
	
	public void start() {
		mCLPVar = new CLPVariable[mnColorCount];
		mClpConstraints = new ArrayList<>();
		
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
			
			mClpConstraints.add(exp.geq(1));
		}
		
		mModel.printModel();
		
		double sumPi = runSolver();
	}
	
	private double runSolver() {
		HashMap<CLPVariable, Double> objFunc = new HashMap<CLPVariable, Double>();
		for (CLPVariable var : mCLPVar) {
			objFunc.put(var, 1.0);
		}
		
		mPi = new ArrayList<>();
		int i = 0;
		for (CLPConstraint constraints : mClpConstraints) {
			mModel.addObjective(objFunc, 0);
			mModel.minimize();
			
			float pi = (float) mModel.getDualSolution(constraints);
			
			mPi.add(pi);
			System.out.print("i" + (i + 1) + " = " + pi + "\t");
			i++;
		}
		System.out.println();
		
		double sumPi = sumPi();
		System.out.println("Sum pi: " + sumPi);
		
		if (sumPi > 1){
			//add new constraint to solver with edges from sumPi*
			//run solver and get dual solutions.
			//find sumPi again
			
			CLPExpression exp = mModel.createExpression();
			
			for(int k : maxIndependentVerts) {
				for(int j = 0; j < mnColorCount; j++) {
					if(mWidthVert.get(j).indexOf(k) >= 0) {
						exp.add(mCLPVar[j]);
					}
				}
			}
			
			mClpConstraints.add(exp.geq(1));
			
			runSolver();
		} else {	  
			int[] result = cliquer();
			//sumPi <=1,run cliquer, get weight
			//if (weight > 1) {
			//get edges, add as constraint, run solver 
			//else if (weight <=1 ){
			//solver.getPrimalSolution() , returns you xi, color number 
			//round this number to  up, if solution >= f* (euristic best) -> return 
			//else (if xi is integer) -> FOUND SOLUTION HERE, return this value and get vertixes 
			//else (xi not integer) {
			//traverse tree. 1st branch - find two edges( i dont remember what exactly... mb read article?),
			//merge them into one 
			//2nd branch - connect those edges with path (?? i dont remember how REBRO is in english, bro) 
			//run from the euristic part until better solution is found 
		}
		
		return sumPi;
	}
	
	private double sumPi() {
		double sumPi = 0;
		
		findMaxIndependentVerts();
		
		for (Integer vertex : maxIndependentVerts) {
			sumPi += mPi.get(vertex);
		}
		return sumPi;
	}
	
	private void findMaxIndependentVerts() {
		maxIndependentVerts = new ArrayList<Integer>();
		int index = 0;
		int minDegree = Integer.MAX_VALUE;
		int j = 0;
		for(int[] i : mHeuristicVert) {
			if(minDegree > i[Heuristic.VERT_DEGREE]) {
				minDegree = i[Heuristic.VERT_DEGREE];
				index = j;
			}
			j++;
		}
		maxIndependentVerts.add(mHeuristicVert[index][Heuristic.VERT_NUMBER]);
		for(int i = 0; i < mpCostMatrix.length; i++) {
			if(mpCostMatrix[i][mHeuristicVert[index][Heuristic.VERT_NUMBER]] > 0) {
				maxIndependentVerts.add(i);
			}
		}
	}
	
	private int[] cliquer() {
		initInvertedCostMatrix();
		return invokeCliquer();
	}

	private int[] invokeCliquer() {
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
			weights[i] = (float) mPi.get(i);
		}
		int[] clique = CliquerWrapper.getInstance().findClique(edges, weights);
        System.out.println(Arrays.toString(clique));
        
        return clique;
	}
	
	private void initInvertedCostMatrix() {
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
	
	public void printVert() {
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
