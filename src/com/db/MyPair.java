package com.db;

public class MyPair<K, V> {
	
	private K k;
	private V v;
	
	public MyPair(K k, V v) {
		this.k = k;
		this.v = v;
	}

	public final K getK() {
		return k;
	}

	public final void setK(K k) {
		this.k = k;
	}

	public final V getV() {
		return v;
	}

	public final void setV(V v) {
		this.v = v;
	}
}
