package util;

import java.util.List;

public class ListSolver {
	public static <T> void set(List<T> list, int index, T element) {
		if (index < list.size()) {
			list.set(index, element);
		}
		else {
			int size = list.size();
			for (int i = 0; i < index + 1 - size; i++)
				list.add(null);
			list.set(index, element);
		}
	}
}
