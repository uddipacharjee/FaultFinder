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
	public int max(int a,int b,int c) {
		int maximum=a;
		if(b>maximum) {
			maximum=b;
		}
		if(c>maximum) {
			maximum=c;
		}
		return maximum;
	}
	
}
