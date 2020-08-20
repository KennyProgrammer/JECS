package kenny.jecs.collection;

import java.util.List;
import java.util.ListIterator;

public class ReversedIteratorList<T> implements Iterable<T> 
{
  private List<T> list;
  
  public ReversedIteratorList(List<T> list)
  {
    this.list = list;
  }
  
  /**
   * Iterator provided to clients of this class.
   */
  @Override
  public ListIterator<T> iterator() 
  {
	    /*
	     * Every time an iterator is requested we define a new ListIterator that will be used to
	     * iterate the list in the reverse order.
	     */
	    final ListIterator<T> iterator = list.listIterator(list.size());
	    
	    // The iterator returned to the caller will
	    // work based on the ListIterator
	    return new ListIterator<T>(){
	
	      // hasNext() and next() methods call in fact
	      // the reverse operations in ListIterator
	      
	    @Override
	    public boolean hasNext(){
	        return iterator.hasPrevious();
	    }
	
	    @Override
	    public T next(){
	        return iterator.previous();
	    }
	
	    @Override
	    public void remove() {
	    	iterator.remove();
	    }
	
		@Override
		public boolean hasPrevious() {
			return iterator.hasNext();
		}
	
		@Override
		public T previous() {
			return iterator.next();
		}
	
		@Override
		public int nextIndex() {
			return previousIndex();
		}
	
		@Override
		public int previousIndex() {
			return nextIndex();
		}
	
		@Override
		public void set(T e) {
			iterator.set(e);
		}
	
		@Override
		public void add(T e) {
			iterator.add(e);
		}
	 };
   }
}