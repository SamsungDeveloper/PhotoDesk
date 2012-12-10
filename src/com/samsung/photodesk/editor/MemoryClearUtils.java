package com.samsung.photodesk.editor;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import com.samsung.samm.common.SObjectImage;
import com.samsung.spensdk.SCanvasView;

/**
 * <p>Memory clear util</p>
 * clear memory that ImageEditor used in.
 */
public class MemoryClearUtils {
	
	private MemoryClearUtils(){};
 
    public static void recursiveRecycle(View parent) {
    	if (parent == null)	return;
    
    	parent.setBackgroundDrawable(null);

        if (parent instanceof ViewGroup) {
            ViewGroup group = (ViewGroup)parent;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                recursiveRecycle(group.getChildAt(i));
            }
 
            if (!(parent instanceof AdapterView)) {
                group.removeAllViews();
            }
 
        }
        
        if (parent instanceof SCanvasView) {
        	SCanvasView sCanvasView = (SCanvasView)parent;
        	sCanvasView.setBackgroundImage(null);
        	
        	int SAMMDataNum = sCanvasView.getSAMMObjectNum();
        	for (int i = 0; i < SAMMDataNum; i++) {
        		if (sCanvasView.getSAMMObject(i) instanceof SObjectImage) {
        			((SObjectImage)sCanvasView.getSAMMObject(i)).getImageBitmap().recycle();
        		}
        	}
        }
        
        if (parent instanceof ImageView) {
            ((ImageView)parent).setImageDrawable(null);
        }
        parent = null;
        
        
        
        return;
    }
}
