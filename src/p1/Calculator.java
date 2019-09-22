package p1;

public class Calculator {
	public int add(int x,int y) {
		int sum=0;
		sum=x+y;
		return sum;
	}
	public int difference(int x,int y) {
		int diff=0;
		diff=Math.abs(x-y);
		return diff;
	}
}
