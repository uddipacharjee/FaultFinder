package p3;

import static org.junit.Assert.*;

import org.junit.Test;

public class MaxTest {

	@Test
	public void test1() {
		int expected = 3;
		int actual = Max.max(new int[] { 1, 2, 3 });
		assertEquals(expected, actual);
	}

	@Test
	public void test2() {
		int expected = 6;
		int actual = Max.max(new int[] { 5, 5, 6 });
		assertEquals(expected, actual);
	}

	@Test
	public void test3() {
		int expected = 10;
		int actual = Max.max(new int[] { 2, 1, 10 });
		assertEquals(expected, actual);
	}

	@Test
	public void test4() {
		int expected = 4;
		int actual = Max.max(new int[] { 4, 3, 2 });
		assertEquals(expected, actual);
	}
	
	@Test
	public void test5() {
		int expected = 4;
		int actual = Max.max(new int[] { 4 });
		assertEquals(expected, actual);
	}
}
