/*
 * Copyright 2002 Felix Pahl. All rights reserved.
 * Use is subject to license terms.
 */

package info.joriki.util;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Collection;
import java.util.HashMap;

/** A MultiSet keeps count of the multiplicity of its elements.
 *  If an object is added which <code>equals</code> an element already
 *  contained, the latter's multiplicity is incremented.
 *  There is a choice in the meaning of the <code>iterator</code> and
 *  <code>containsAll</code> methods : Is the iterator to return every
 *  element once or according to its multiplicity? Should
 *  <code>containsAll</code> called with another multiset as an
 *  argument take multiplicities into account or not?
 *  <BR>
 *  These questions are decided by requiring a) that all methods
 *  should behave like the ones in <code>AbstractCollection</code> and be
 *  reimplemented for efficiency only, and b) that <code>addAll</code>
 *  when called with another multiset should take multiplicities
 *  into account. This means that the iterator must take multiplicity
 *  into account, and that <code>containsAll</code> must not.
 *  The alternatives are provided in methods of their own:
 *  The iterator returned by <code>simpleIterator</code> returns
 *  every element exactly once, and <code>greaterOrEqual</code> is a
 *  version of <code>containsAll</code> that takes multiplicities
 *  into account, i.e. it returns true if and only if each element
 *  of the argument is contained with at least the same multiplicity
 *  in <code>this</code>.
 *  <BR>
 *  The current implementation doesn't check for integer overflows
 *  in element counts and the <code>size</code> method.
 */

public class MultiSet<T> extends AbstractCollection<T>
{
  final protected Map<T,Count> map;

  public MultiSet ()
  {
    map = new HashMap<T,Count> ();
  }

  public MultiSet (Collection<T> collection)
  {
    this ();
    if (collection instanceof MultiSet)
      for (Map.Entry<T,Count> entry : ((MultiSet<T>) collection).map.entrySet ())
        map.put (entry.getKey (),(Count) (entry.getValue ()).clone ());
    else
      addAll (collection);
  }

  public boolean add (T o)
  {
    Count count = map.get (o);
    if (count == null)
      map.put (o,count = new Count ());
    count.increment ();
    return true;
  }

  public boolean addAll (Collection<? extends T> collection)
  {
    if (!(collection instanceof MultiSet))
      return super.addAll (collection);

    Iterator<Map.Entry<T,Count>> entries = ((MultiSet<T>) collection).entryIterator ();
    while (entries.hasNext ()) {
      Map.Entry<T,Count> entry = entries.next ();
      T key = entry.getKey ();
      Count count = map.get (key);
      if (count == null)
        map.put (key,count = new Count ());
      count.add (entry.getValue ());
    }
    return !collection.isEmpty ();
  }

  public void clear ()
  {
    map.clear ();
  }

  public boolean contains (Object o)
  {
    return map.containsKey (o);
  }

  public boolean containsAll (Collection collection)
  {
    return collection instanceof MultiSet ?
      map.keySet ().containsAll (((MultiSet) collection).map.keySet ()) :
      super.containsAll (collection);
  }

  public boolean isEmpty ()
  {
    return map.isEmpty ();
  }

  public Iterator<T> iterator ()
  {
    return multipleIterator ();
  }

  public boolean remove (Object o)
  {
    Count count = map.get (o);
    if (count == null)
      return false;
    if (count.count == 1)
      map.remove (o);
    else
      count.decrement ();
    return true;
  }

  public boolean removeAll (Collection collection)
  {
    if (!(collection instanceof MultiSet))
      return super.removeAll (collection);

    boolean changed = false;
    Iterator entries = ((MultiSet) collection).entryIterator ();
    while (entries.hasNext ()) {
      Map.Entry entry = (Map.Entry) entries.next ();
      Object key = entry.getKey ();
      Count count = map.get (key);
      if (count != null) {
        changed = true;
        int nremove = ((Count) entry.getValue ()).count;
        if (count.count <= nremove)
          map.remove (key);
        else
          count.count -= nremove;
      }
    }
    return changed;
  }

  public boolean retainAll (Collection collection)
  {
    boolean changed = false;
    Iterator keys = map.keySet ().iterator ();
    while (keys.hasNext ())
      {
        Object key = keys.next ();
        if (!collection.contains (key))
          {
            keys.remove ();
            changed = true;
          }
      }
    return changed;
  }

  public int size ()
  {
    Iterator values = map.values ().iterator ();
    int size = 0;
    while (values.hasNext ())
      size += ((Count) values.next ()).count;
    return size;
  }

  public boolean greaterOrEqual (MultiSet multiSet)
  {
    Iterator entries = multiSet.entryIterator ();
    while (entries.hasNext ())
      {
        Map.Entry entry = (Map.Entry) entries.next ();
        Count count = map.get (entry.getKey ());
        if (count == null || count.count < ((Count) entry.getValue ()).count)
          return false;
      }
    return true;
  }

  protected Entry<T,Count> mostFrequentEntry ()
  {
	return extremeEntry (1);
  }
  
  protected Entry<T,Count> leastFrequentEntry ()
  {
	return extremeEntry (-1);
  }
  
  private Entry<T,Count> extremeEntry (int sign) {
    int max = -sign * Integer.MAX_VALUE;
    Entry<T,Count> maxEntry = null;
    Iterator<Entry<T,Count>> entries = entryIterator ();
    while (entries.hasNext ())
      {
        Entry<T,Count> entry = entries.next ();
        int count = entry.getValue ().count;
        if (sign * count > sign * max)
          {
            max = count;
            maxEntry = entry;
          }
      }
    return maxEntry;
  }

  public T mostFrequentElement ()
  {
    return mostFrequentEntry ().getKey ();
  }

  public T leastFrequentElement ()
  {
    return leastFrequentEntry ().getKey ();
  }

  public int highestFrequency ()
  {
    return mostFrequentEntry ().getValue ().count;
  }

  public int lowestFrequency ()
  {
    return leastFrequentEntry ().getValue ().count;
  }

  public Set<T> toSet ()
  {
    return map.keySet ();
  }
  
  public Iterator<Map.Entry<T,Count>> entryIterator ()
  {
    return map.entrySet ().iterator ();
  }

  public Iterator simpleIterator ()
  {
    return toSet ().iterator ();
  }

  public Iterator<T> multipleIterator ()
  {
    return new Iterator<T> () {
        Iterator<Map.Entry<T,Count>> entries = entryIterator ();
        Map.Entry<T,Count> current;
        T object;
        Count count;
        int left = 0;
        boolean removeAllowed = false;

        final private void getNextEntry ()
        {
          current = entries.next ();
          object = current.getKey ();
          count = current.getValue ();
          left = count.count;
        }

        public boolean hasNext ()
        {
          if (left == 0)
            {
              if (!entries.hasNext ())
                return false;
              getNextEntry ();
            }
          return true;
        }

        public T next ()
        {
          if (left == 0)
            getNextEntry ();
          left--;
          removeAllowed = true;
          return object;
        }

        public void remove ()
        {
          if (!removeAllowed)
            throw new IllegalStateException ();
          if (--count.count == 0)
            entries.remove ();
          removeAllowed = false;
        }
      };
  }
  
  public Count getCount (T t) {
    return map.get (t);
  }
}
