package arraylist.entity.withlock;
import java.util.*;
import top.anonymous.anno.Perm;
import java.util.concurrent.locks.ReentrantReadWriteLock;
public abstract class AbstractList<E> extends AbstractCollection<E> implements List<E> {
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	protected AbstractList() {
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public boolean add(E e) {
		add(size(), e);
		return true;
	}
	abstract public E get(int index);
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public E set(int index, E element) {
		throw new UnsupportedOperationException();
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public E remove(int index) {
		throw new UnsupportedOperationException();
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public int indexOf(Object o) {
		ListIterator<E> e = listIterator();
		if (o == null) {
			while (e.hasNext())
				if (e.next() == null)
					return e.previousIndex();
		} else {
			while (e.hasNext())
				if (o.equals(e.next()))
					return e.previousIndex();
		}
		return -1;
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public int lastIndexOf(Object o) {
		ListIterator<E> e = listIterator(size());
		if (o == null) {
			while (e.hasPrevious())
				if (e.previous() == null)
					return e.nextIndex();
		} else {
			while (e.hasPrevious())
				if (o.equals(e.previous()))
					return e.nextIndex();
		}
		return -1;
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public void clear() {
		removeRange(0, size());
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public boolean addAll(int index, Collection<? extends E> c) {
		boolean modified = false;
		Iterator<? extends E> e = c.iterator();
		while (e.hasNext()) {
			add(index++, e.next());
			modified = true;
		}
		return modified;
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public Iterator<E> iterator() {
		return new Itr();
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public ListIterator<E> listIterator() {
		return listIterator(0);
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public ListIterator<E> listIterator(final int index) {
		if (index < 0 || index > size())
			throw new IndexOutOfBoundsException("Index: " + index);
		return new ListItr(index);
	}
	private class Itr implements Iterator<E> {
		public ReentrantReadWriteLock cursor_expectedModCount_lastRet_modCountLock = new ReentrantReadWriteLock();
		int cursor = 0;
		int lastRet = -1;
		int expectedModCount = modCount;
		@Perm(requires = "pure(cursor) in alive", ensures = "pure(cursor) in alive")
		public boolean hasNext() {
			try {
				cursor_expectedModCount_lastRet_modCountLock.readLock().lock();
				return cursor != size();
			} finally {
				cursor_expectedModCount_lastRet_modCountLock.readLock().unlock();
			}
		}
		@Perm(requires = "share(cursor) * share(lastRet) in alive", ensures = "share(cursor) * share(lastRet) in alive")
		public E next() {
			try {
				cursor_expectedModCount_lastRet_modCountLock.writeLock().lock();
				checkForComodification();
				try {
					E next = get(cursor);
					lastRet = cursor++;
					return next;
				} catch (IndexOutOfBoundsException e) {
					checkForComodification();
					throw new NoSuchElementException();
				}
			} finally {
				cursor_expectedModCount_lastRet_modCountLock.writeLock().unlock();
			}
		}
		@Perm(requires = "share(lastRet) * share(cursor) * share(expectedModCount) * pure(modCount) in alive", ensures = "share(lastRet) * share(cursor) * share(expectedModCount) * pure(modCount) in alive")
		public void remove() {
			try {
				cursor_expectedModCount_lastRet_modCountLock.writeLock().lock();
				if (lastRet == -1)
					throw new IllegalStateException();
				checkForComodification();
				try {
					remove(lastRet);
					if (lastRet < cursor)
						cursor--;
					lastRet = -1;
					expectedModCount = modCount;
				} catch (IndexOutOfBoundsException e) {
					throw new ConcurrentModificationException();
				}
			} finally {
				cursor_expectedModCount_lastRet_modCountLock.writeLock().unlock();
			}
		}
		@Perm(requires = "pure(modCount) * pure(expectedModCount) in alive", ensures = "pure(modCount) * pure(expectedModCount) in alive")
		final void checkForComodification() {
			try {
				cursor_expectedModCount_lastRet_modCountLock.readLock().lock();
				if (modCount != expectedModCount)
					throw new ConcurrentModificationException();
			} finally {
				cursor_expectedModCount_lastRet_modCountLock.readLock().unlock();
			}
		}
	}
	private class ListItr extends Itr implements ListIterator<E> {
		public ReentrantReadWriteLock cursor_expectedModCount_lastRet_modCountLock = new ReentrantReadWriteLock();
		@Perm(requires = "unique(cursor) in alive", ensures = "unique(cursor) in alive")
		ListItr(int index) {
			cursor_expectedModCount_lastRet_modCountLock.writeLock().lock();
			cursor = index;
			cursor_expectedModCount_lastRet_modCountLock.writeLock().unlock();
		}
		@Perm(requires = "pure(cursor) in alive", ensures = "pure(cursor) in alive")
		public boolean hasPrevious() {
			try {
				cursor_expectedModCount_lastRet_modCountLock.readLock().lock();
				return cursor != 0;
			} finally {
				cursor_expectedModCount_lastRet_modCountLock.readLock().unlock();
			}
		}
		@Perm(requires = "share(cursor) * share(lastRet) in alive", ensures = "share(cursor) * share(lastRet) in alive")
		public E previous() {
			checkForComodification();
			try {
				cursor_expectedModCount_lastRet_modCountLock.writeLock().lock();
				int i = cursor - 1;
				E previous = get(i);
				lastRet = cursor = i;
				cursor_expectedModCount_lastRet_modCountLock.writeLock().unlock();
				return previous;
			} catch (IndexOutOfBoundsException e) {
				checkForComodification();
				throw new NoSuchElementException();
			}
		}
		@Perm(requires = "pure(cursor) in alive", ensures = "pure(cursor) in alive")
		public int nextIndex() {
			try {
				cursor_expectedModCount_lastRet_modCountLock.readLock().lock();
				return cursor;
			} finally {
				cursor_expectedModCount_lastRet_modCountLock.readLock().unlock();
			}
		}
		@Perm(requires = "pure(cursor) in alive", ensures = "pure(cursor) in alive")
		public int previousIndex() {
			try {
				cursor_expectedModCount_lastRet_modCountLock.readLock().lock();
				return cursor - 1;
			} finally {
				cursor_expectedModCount_lastRet_modCountLock.readLock().unlock();
			}
		}
		@Perm(requires = "pure(lastRet) * share(expectedModCount) * pure(modCount) in alive", ensures = "pure(lastRet) * share(expectedModCount) * pure(modCount) in alive")
		public void set(E e) {
			try {
				cursor_expectedModCount_lastRet_modCountLock.readLock().lock();
				cursor_expectedModCount_lastRet_modCountLock.writeLock().lock();
				if (lastRet == -1)
					throw new IllegalStateException();
				checkForComodification();
				try {
					AbstractList.this.set(lastRet, e);
					expectedModCount = modCount;
				} catch (IndexOutOfBoundsException ex) {
					throw new ConcurrentModificationException();
				}
			} finally {
				cursor_expectedModCount_lastRet_modCountLock.writeLock().unlock();
				cursor_expectedModCount_lastRet_modCountLock.readLock().unlock();
			}
		}
		@Perm(requires = "share(cursor) * share(lastRet) * share(expectedModCount) * pure(modCount) in alive", ensures = "share(cursor) * share(lastRet) * share(expectedModCount) * pure(modCount) in alive")
		public void add(E e) {
			checkForComodification();
			try {
				cursor_expectedModCount_lastRet_modCountLock.writeLock().lock();
				AbstractList.this.add(cursor++, e);
				lastRet = -1;
				expectedModCount = modCount;
				cursor_expectedModCount_lastRet_modCountLock.writeLock().unlock();
			} catch (IndexOutOfBoundsException ex) {
				throw new ConcurrentModificationException();
			}
		}
	}
	public List<E> subList(int fromIndex, int toIndex) {
		return (this instanceof RandomAccess
				? new RandomAccessSubList<E>(this, fromIndex, toIndex)
				: new SubList<E>(this, fromIndex, toIndex));
	}
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof List))
			return false;
		ListIterator<E> e1 = listIterator();
		ListIterator e2 = ((List) o).listIterator();
		while (e1.hasNext() && e2.hasNext()) {
			E o1 = e1.next();
			Object o2 = e2.next();
			if (!(o1 == null ? o2 == null : o1.equals(o2)))
				return false;
		}
		return !(e1.hasNext() || e2.hasNext());
	}
	public int hashCode() {
		int hashCode = 1;
		Iterator<E> i = iterator();
		while (i.hasNext()) {
			E obj = i.next();
			hashCode = 31 * hashCode + (obj == null ? 0 : obj.hashCode());
		}
		return hashCode;
	}
	protected void removeRange(int fromIndex, int toIndex) {
		ListIterator<E> it = listIterator(fromIndex);
		for (int i = 0, n = toIndex - fromIndex; i < n; i++) {
			it.next();
			it.remove();
		}
	}
	protected transient int modCount = 0;
}
class SubList<E> extends AbstractList<E> {
	public ReentrantReadWriteLock expectedModCount_l_modCount_offset_sizeLock = new ReentrantReadWriteLock();
	private AbstractList<E> l;
	private int offset;
	private int size;
	private int expectedModCount;
	@Perm(requires = "unique(l) * none(l) * unique(offset) * unique(size) * unique(expectedModCount) * none(modCount) in alive", ensures = "unique(l) * none(l) * unique(offset) * unique(size) * unique(expectedModCount) * none(modCount) in alive")
	SubList(AbstractList<E> list, int fromIndex, int toIndex) {
		if (fromIndex < 0)
			throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
		if (toIndex > list.size())
			throw new IndexOutOfBoundsException("toIndex = " + toIndex);
		if (fromIndex > toIndex)
			throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
		expectedModCount_l_modCount_offset_sizeLock.writeLock().lock();
		l = list;
		offset = fromIndex;
		size = toIndex - fromIndex;
		expectedModCount = l.modCount;
		expectedModCount_l_modCount_offset_sizeLock.writeLock().unlock();
	}
	@Perm(requires = "immutable(l) * pure(offset) in alive", ensures = "immutable(l) * pure(offset) in alive")
	public E set(int index, E element) {
		rangeCheck(index);
		checkForComodification();
		try {
			expectedModCount_l_modCount_offset_sizeLock.readLock().lock();
			return l.set(index + offset, element);
		} finally {
			expectedModCount_l_modCount_offset_sizeLock.readLock().unlock();
		}
	}
	@Perm(requires = "immutable(l) * pure(offset) in alive", ensures = "immutable(l) * pure(offset) in alive")
	public E get(int index) {
		rangeCheck(index);
		checkForComodification();
		try {
			expectedModCount_l_modCount_offset_sizeLock.readLock().lock();
			return l.get(index + offset);
		} finally {
			expectedModCount_l_modCount_offset_sizeLock.readLock().unlock();
		}
	}
	@Perm(requires = "pure(size) in alive", ensures = "pure(size) in alive")
	public int size() {
		checkForComodification();
		try {
			expectedModCount_l_modCount_offset_sizeLock.readLock().lock();
			return size;
		} finally {
			expectedModCount_l_modCount_offset_sizeLock.readLock().unlock();
		}
	}
	@Perm(requires = "share(size) * immutable(l) * pure(offset) * share(expectedModCount) * share(modCount) in alive", ensures = "share(size) * immutable(l) * pure(offset) * share(expectedModCount) * share(modCount) in alive")
	public void add(int index, E element) {
		try {
			expectedModCount_l_modCount_offset_sizeLock.writeLock().lock();
			if (index < 0 || index > size)
				throw new IndexOutOfBoundsException();
			checkForComodification();
			l.add(index + offset, element);
			expectedModCount = l.modCount;
			size++;
			modCount++;
		} finally {
			expectedModCount_l_modCount_offset_sizeLock.writeLock().unlock();
		}
	}
	@Perm(requires = "immutable(l) * pure(offset) * share(expectedModCount) * share(modCount) * share(size) in alive", ensures = "immutable(l) * pure(offset) * share(expectedModCount) * share(modCount) * share(size) in alive")
	public E remove(int index) {
		expectedModCount_l_modCount_offset_sizeLock.writeLock().lock();
		rangeCheck(index);
		checkForComodification();
		E result = l.remove(index + offset);
		expectedModCount = l.modCount;
		size--;
		modCount++;
		expectedModCount_l_modCount_offset_sizeLock.writeLock().unlock();
		return result;
	}
	@Perm(requires = "immutable(l) * pure(offset) * share(expectedModCount) * share(modCount) * share(size) in alive", ensures = "immutable(l) * pure(offset) * share(expectedModCount) * share(modCount) * share(size) in alive")
	protected void removeRange(int fromIndex, int toIndex) {
		expectedModCount_l_modCount_offset_sizeLock.writeLock().lock();
		checkForComodification();
		l.removeRange(fromIndex + offset, toIndex + offset);
		expectedModCount = l.modCount;
		size -= (toIndex - fromIndex);
		modCount++;
		expectedModCount_l_modCount_offset_sizeLock.writeLock().unlock();
	}
	@Perm(requires = "pure(size) in alive", ensures = "pure(size) in alive")
	public boolean addAll(Collection<? extends E> c) {
		try {
			expectedModCount_l_modCount_offset_sizeLock.readLock().lock();
			return addAll(size, c);
		} finally {
			expectedModCount_l_modCount_offset_sizeLock.readLock().unlock();
		}
	}
	@Perm(requires = "share(size) * immutable(l) * pure(offset) * share(expectedModCount) * share(modCount) in alive", ensures = "share(size) * immutable(l) * pure(offset) * share(expectedModCount) * share(modCount) in alive")
	public boolean addAll(int index, Collection<? extends E> c) {
		try {
			expectedModCount_l_modCount_offset_sizeLock.writeLock().lock();
			if (index < 0 || index > size)
				throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
			int cSize = c.size();
			if (cSize == 0)
				return false;
			checkForComodification();
			l.addAll(offset + index, c);
			expectedModCount = l.modCount;
			size += cSize;
			modCount++;
		} finally {
			expectedModCount_l_modCount_offset_sizeLock.writeLock().unlock();
		}
		return true;
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public Iterator<E> iterator() {
		return listIterator();
	}
	@Perm(requires = "pure(size) * immutable(l) * pure(offset) in alive", ensures = "pure(size) * immutable(l) * pure(offset) in alive")
	public ListIterator<E> listIterator(final int index) {
		checkForComodification();
		try {
			expectedModCount_l_modCount_offset_sizeLock.readLock().lock();
			if (index < 0 || index > size)
				throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
			return new ListIterator<E>() {
				private ListIterator<E> i = l.listIterator(index + offset);
				public boolean hasNext() {
					return nextIndex() < size;
				}
				public E next() {
					if (hasNext())
						return i.next();
					else
						throw new NoSuchElementException();
				}
				public boolean hasPrevious() {
					return previousIndex() >= 0;
				}
				public E previous() {
					if (hasPrevious())
						return i.previous();
					else
						throw new NoSuchElementException();
				}
				public int nextIndex() {
					return i.nextIndex() - offset;
				}
				public int previousIndex() {
					return i.previousIndex() - offset;
				}
				public void remove() {
					i.remove();
					expectedModCount = l.modCount;
					size--;
					modCount++;
				}
				public void set(E e) {
					i.set(e);
				}
				public void add(E e) {
					i.add(e);
					expectedModCount = l.modCount;
					size++;
					modCount++;
				}
			};
		} finally {
			expectedModCount_l_modCount_offset_sizeLock.readLock().unlock();
		}
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public List<E> subList(int fromIndex, int toIndex) {
		return new SubList<E>(this, fromIndex, toIndex);
	}
	@Perm(requires = "pure(size) in alive", ensures = "pure(size) in alive")
	private void rangeCheck(int index) {
		try {
			expectedModCount_l_modCount_offset_sizeLock.readLock().lock();
			if (index < 0 || index >= size)
				throw new IndexOutOfBoundsException("Index: " + index + ",Size: " + size);
		} finally {
			expectedModCount_l_modCount_offset_sizeLock.readLock().unlock();
		}
	}
	@Perm(requires = "immutable(l) * pure(modCount) * pure(expectedModCount) in alive", ensures = "immutable(l) * pure(modCount) * pure(expectedModCount) in alive")
	private void checkForComodification() {
		try {
			expectedModCount_l_modCount_offset_sizeLock.readLock().lock();
			if (l.modCount != expectedModCount)
				throw new ConcurrentModificationException();
		} finally {
			expectedModCount_l_modCount_offset_sizeLock.readLock().unlock();
		}
	}
}
class RandomAccessSubList<E> extends SubList<E> implements RandomAccess {
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	RandomAccessSubList(AbstractList<E> list, int fromIndex, int toIndex) {
		super(list, fromIndex, toIndex);
	}
	@Perm(requires = "no permission in alive", ensures = "no permission in alive")
	public List<E> subList(int fromIndex, int toIndex) {
		return new RandomAccessSubList<E>(this, fromIndex, toIndex);
	}
}
