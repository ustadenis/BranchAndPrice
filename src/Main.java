
public class Main {

	public static int maxWorkTime;
	public static String path;
	
	public static void main(String[] args) {
		maxWorkTime = Integer.parseInt("1000");
		path =  "C:\\Users\\ustad\\Desktop\\color.txt";
		
		ATSPParser atspParser = new ATSPParser(path);
		atspParser.print();
		
		Heuristic heuristic = new Heuristic(atspParser.getCostMatrix());
		heuristic.getHeuristic();
		heuristic.printVert();
		
		int colorCount = heuristic.getColorCount();
		
		System.out.println(colorCount);
		
		BranchAndPrice bnp = new BranchAndPrice(atspParser.getCostMatrix().clone(), heuristic.getHeuristic().clone(), colorCount);
		bnp.printWidthVert();
		bnp.useSolver();
	}
}
