package com.idcompany.taxidriverapp;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

public class TypefacedButton extends Button{
	
	public TypefacedButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		if (isInEditMode()) {
			return;
		}
		
		if (attrs != null) {
			TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.TypefacedButton);
			String fontName = styledAttrs.getString(R.styleable.TypefacedButton_fontNameForButton);
						
			if (fontName != null) {
				//Typeface typeface = Typeface.createFromAsset(context.getAssets(), fontName);
				Typeface typeface = TypefaceProvider.getTypeFace(context, fontName);
				setTypeface(typeface);
			}
			styledAttrs.recycle();
		}
	}
}
