package com.vodiytechnologies.rtcmsclient;

import java.util.Hashtable;

import android.content.Context;
import android.graphics.Typeface;

public class TypefaceProvider {
	public static final String TYPEFACE_FOLDER = "fonts";
	public static final String TYPEFACE_EXTENSION = ".ttf";
	
	private static Hashtable<String, Typeface> aTypeFaces = new Hashtable<String, Typeface>(4);
	
	public static Typeface getTypeFace(Context context, String fileName) {
		Typeface tempTypeface = aTypeFaces.get(fileName);
		
		if (tempTypeface == null) {
			String fontPath = new StringBuilder(TYPEFACE_FOLDER).append('/').append(fileName).append(TYPEFACE_EXTENSION).toString();
			tempTypeface = Typeface.createFromAsset(context.getAssets(), fontPath);
			aTypeFaces.put(fileName, tempTypeface);
		}
		return tempTypeface;
	}
}
