package analysis;

public class OchiaiHeuristic implements Heuristic {

	public double eval(int cef, int cnf, int cep, int cnp) {
		double suspiciousness = 0.0d;

		if (cef > 0) {
			suspiciousness = (cef)
					/ (double) Math.sqrt((cef + cnf) * (cef + cep));
		}
		return suspiciousness;
	}

	public HeuristicEnum getEnum() {
		return HeuristicEnum.OCHIAI;
	}
}
