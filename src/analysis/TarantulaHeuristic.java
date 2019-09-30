package analysis;

public class TarantulaHeuristic implements Heuristic {

	public double eval(int cef, int cnf, int cep, int cnp) {
		double suspiciousness = 0.0d;

		if (cef > 0) {
			if (cep > 0) {
				suspiciousness = ((double) cef / (cef + cnf))
						/ (((double) cef / (cef + cnf)) + ((double) cep / (cep + cnp)));
			} else {
				suspiciousness = (double) 1;
			}
		}
		return suspiciousness;
	}

	public HeuristicEnum getEnum() {
		return HeuristicEnum.TARANTULA;
	}

}
