
public class Main {

	public static int maxWorkTime;
	public static String path;
	
	public static void main(String[] args) {
		if(args.length == 0) {
			maxWorkTime = Integer.parseInt("1000");
			path =  "C:\\Users\\ustad\\Desktop\\color.txt";
		} else if(args.length == 2) {
			path = args[0];
			maxWorkTime = Integer.parseInt(args[1]);
		} else {
			System.out.println("Invalid arguments.");
			
			return;
		}
		
		Parser atspParser = new Parser(path);
		atspParser.print();
		
		Heuristic heuristic = new Heuristic(atspParser.getCostMatrix());
		heuristic.getHeuristic();
		heuristic.printVert();
		
		int colorCount = heuristic.getColorCount();
		
		System.out.println(colorCount);
		
		BranchAndPrice bnp = new BranchAndPrice(atspParser.getCostMatrix().clone(), heuristic.getHeuristic().clone(), colorCount);
		bnp.printWidthVert();
		bnp.start();
	}
}
