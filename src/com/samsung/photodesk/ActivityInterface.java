package com.samsung.photodesk;

import com.samsung.photodesk.loader.ThreadPool;

/**
 * <p>Interface for default activity</p>
 *
 */
public interface ActivityInterface {
	/**
	 * Get themeID
	 * @return themeID
	 */
    abstract int getThemeId();
    
    /**
     * Get ThreadPool
     * @return ThreadPool
     */
    abstract ThreadPool getThreadPool();
    
    /**
     * Get ActionBar
     * @return ActionBar
     */
    abstract PhotoDeskActionBar getPhotoDeskActionBar();
}
