/**
 *   Copyright (c) Rich Hickey. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Common Public License 1.0 (http://opensource.org/licenses/cpl.php)
 *   which can be found in the file CPL.TXT at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

package clojure.lang;

import java.util.List;
import java.util.ListIterator;
import java.util.LinkedList;

public class PersistentList extends ASeq implements IPersistentList, IReduce{

private final Object _first;
private final IPersistentList _rest;
private final int _count;

public static IFn creator = new RestFn(0){
	protected Object doInvoke(Object args) throws Exception{
		if(args instanceof ArraySeq)
			{
			Object[] argsarray = (Object[]) ((ArraySeq) args).array;
			IPersistentList ret = EMPTY;
			for(int i = argsarray.length - 1; i >= 0; --i)
				ret = (IPersistentList) ret.cons(argsarray[i]);
			return ret;
			}
		LinkedList list = new LinkedList();
		for(ISeq s = RT.seq(args); s != null; s = s.rest())
			list.add(s.first());
		 return create(list);
	}
};

final public static EmptyList EMPTY = new EmptyList(null);

public PersistentList(Object first){
	this._first = first;
	this._rest = null;

	this._count = 1;
}

PersistentList(IPersistentMap meta, Object _first, IPersistentList _rest, int _count){
	super(meta);
	this._first = _first;
	this._rest = _rest;
	this._count = _count;
}

public static IPersistentList create(List init){
	IPersistentList ret = EMPTY;
	for(ListIterator i = init.listIterator(init.size()); i.hasPrevious();)
		{
		ret = (IPersistentList) ret.cons(i.previous());
		}
	return ret;
}

public Object first(){
	return _first;
}

public ISeq rest(){
	if(_count == 1)
		return null;
	return (ISeq) _rest;
}

public Object peek(){
	return first();
}

public IPersistentList pop(){
	if(_rest == null)
		return EMPTY.withMeta(_meta);
	return _rest;
}

public int count(){
	return _count;
}

public PersistentList cons(Object o){
	return new PersistentList(meta(), o, this, _count + 1);
}

public IPersistentCollection empty(){
	return EMPTY.withMeta(meta());	
}

public PersistentList withMeta(IPersistentMap meta){
	if(meta != _meta)
		return new PersistentList(meta, _first, _rest, _count);
	return this;
}

public Object reduce(IFn f) throws Exception{
	Object ret = first();
	for(ISeq s = rest(); s != null; s = s.rest())
		ret = f.invoke(ret, s.first());
	return ret;
}

public Object reduce(IFn f, Object start) throws Exception{
	Object ret = f.invoke(start, first());
	for(ISeq s = rest(); s != null; s = s.rest())
		ret = f.invoke(ret, s.first());
	return ret;
}

static class EmptyList extends Obj implements IPersistentList{

	EmptyList(IPersistentMap meta){
		super(meta);
	}

	public PersistentList cons(Object o){
		return new PersistentList(meta(), o, null, 1);
	}

	public IPersistentCollection empty(){
		return this;
	}

	public EmptyList withMeta(IPersistentMap meta){
		if(meta != meta())
			return new EmptyList(meta);
		return this;
	}

	public Object peek(){
		return null;
	}

	public IPersistentList pop(){
		throw new IllegalStateException("Can't pop empty list");
	}

	public int count(){
		return 0;
	}

	public ISeq seq(){
		return null;
	}
}

}
