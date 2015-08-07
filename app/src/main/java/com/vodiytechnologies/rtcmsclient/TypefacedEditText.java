package com.vodiytechnologies.rtcmsclient;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;;

public class TypefacedEditText extends EditText{
	public TypefacedEditText(Context context, AttributeSet attrs) {
		super(context,  attrs);
	
		if (isInEditMode()) {
			return;
		}
	
		if (attrs != null) {
			TypedArray styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.TypefacedEditText);
			String fontName = styledAttrs.getString(R.styleable.TypefacedEditText_fontNameForEditText);
			
			if (fontName != null) {
			  	//Typeface typeface = Typeface.createFromAsset(context.getAssets(), fontName);
				Typeface typeface = TypefaceProvider.getTypeFace(context, fontName);
			  	setTypeface(typeface);
			}
			styledAttrs.recycle();
		}
	}
}