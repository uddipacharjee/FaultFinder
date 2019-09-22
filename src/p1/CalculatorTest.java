package p1;

import static org.junit.Assert.*;

import org.junit.Test;

public class CalculatorTest {
	Calculator c=new Calculator();
	@Test
	public void testAdd() {
		assertEquals(2, c.add(1, 1));
	}
	@Test
	public void testDiff() {
		assertEquals(3, c.difference(4, 1));
	}
	

}
