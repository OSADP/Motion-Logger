package com.example.utils;

import java.util.ArrayList;
import java.util.List;

public class Predicate {
	
	public static Object predicateParams;
	
	public static <T> List<T> filter(List<T> target, IPredicate<T> predicate) {
		List<T> result = new ArrayList<T>();
	    for (T element : target) {
	        if (predicate.apply(element)) {
	            result.add(element);
	        }
	    }
	    return result;
	}

	public static <T> T select(List<T> target, IPredicate<T> predicate) {
	    T result = null;
	    for (T element : target) {
	        if (!predicate.apply(element))
	            continue;
	        result = element;
	        break;
	    }
	    return result;
	}

	public static <T> T select(List<T> target, IPredicate<T> predicate, T defaultValue) {
	    T result = defaultValue;
	    for (T element : target) {
	        if (!predicate.apply(element))
	            continue;
	        result = element;
	        break;
	    }
	    return result;
	}
}
