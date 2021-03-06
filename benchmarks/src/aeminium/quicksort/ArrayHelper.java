package aeminium.quicksort.withlock;
import java.util.Random;
import top.anonymous.anno.Perm;
import java.util.concurrent.locks.ReentrantReadWriteLock;
public class ArrayHelper {
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public static boolean checkArray(long[] c) {
		boolean st = true;
		for (int i = 0; i < c.length - 1; i++) {
			st = st && (c[i] <= c[i + 1]);
		}
		return st;
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public static long[] generateRandomArray(long[] ar, int size) {
		Random r = new Random();
		r.setSeed(1234567890);
		ar = new long[size];
		for (int i = 0; i < size; i++) {
			ar[i] = r.nextLong();
		}
		return ar;
	}
}
