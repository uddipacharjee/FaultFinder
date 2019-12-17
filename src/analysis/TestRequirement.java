package analysis; 

public class TestRequirement implements Comparable<TestRequirement> {

	private final String className;
	private final Integer lineNumber;
	private int cef = 0;
	private int cep = 0;
	private int cnp = 0;
	private int cnf = 0;
	private Double suspiciousness; 

	public TestRequirement(String className, Integer lineNumber) {
		super();
		this.className = className;
		this.lineNumber = lineNumber;
	}

	public String getClassName() {
		return className;
	}

	public Integer getLineNumber() {
		return lineNumber;
	}

	public void increase_cef(){
		cef++;
	}
	
	public int getCef() {
		return cef;
	}

	public void increase_cep(){
		cep++;
	}

	public int getCep() {
		return cep;
	}
	public void increase_cnp() {
		cnp++;
	}
	public int getCnp() {
		return cnp;
	}
	public void increase_cnf() {
		cnf++;
	}
	public int getCnf() {
		return cnf;
	}
	public double getSuspiciousness() {
		return suspiciousness;
	}

	public void setSuspiciousness(double suspiciousness) {
		this.suspiciousness = suspiciousness;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());
		result = prime * result
				+ ((lineNumber == null) ? 0 : lineNumber.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestRequirement other = (TestRequirement) obj;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (lineNumber == null) {
			if (other.lineNumber != null)
				return false;
		} else if (!lineNumber.equals(other.lineNumber))
			return false;
		return true;
	}

	public int compareTo(TestRequirement o) {
		return this.suspiciousness.compareTo(o.getSuspiciousness());
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		//return "lineNumber: "+lineNumber+" cef: "+cef+" cep: "+cep+" cnf: "+cnf+" cnp: "+cnp+" suscpiciousness: "+suspiciousness+" \n";
		return lineNumber+" "+suspiciousness+"\n";
	}
}
