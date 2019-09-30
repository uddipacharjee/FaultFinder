package analysis;
public interface Heuristic {
	
	HeuristicEnum getEnum();
	
	double eval(int cef, int cnf, int cep, int cnp);
}
