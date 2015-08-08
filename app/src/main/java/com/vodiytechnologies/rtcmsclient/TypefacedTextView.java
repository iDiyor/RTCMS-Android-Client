package com.vodiytechnologies.rtcmsclient;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class TypefacedTextView extends TextView {
	public TypefacedTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		if (isInEditMode()) {
			return;
		}
		
		if (attrs != null) {
			TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.TypefacedTextView);
			String fontName = styledAttrs.getString(R.styleable.TypefacedTextView_fontNameForTextView);
			
			if (fontName != null) {
				//Typeface typeface = Typeface.createFromAsset(context.getAssets(), fontName);
				Typeface typeface = TypefaceProvider.getTypeFace(context, fontName);
				setTypeface(typeface);
			}
			styledAttrs.recycle();
		}
	}
}
