package com.samsung.photodesk.data;

import java.util.ArrayList;

/**
 * Items used in the content view
 *
 */
public class ContentItem  {

	private ArrayList<MediaItem> mItems = new ArrayList<MediaItem>();
	
	private ContentItem () {}
	
	public static ContentItem getInstance() {
		return SingletonHolder.instance;
	}
	
	/**         
	  * SingletonHolder is loaded on the first execution of Singleton.getInstance()
	  * or the first access to SingletonHolder.INSTANCE, not before.         
	  **/        
	private static class SingletonHolder {
		public static final ContentItem instance = new ContentItem();         
	} 

	/**
	 * Additional items.
	 * @param items - add items
	 */
	public void add(ArrayList<MediaItem> items) {
		mItems.clear();
		mItems.addAll(items);
	}

	/**
	 * Get the number of items.
	 * @return number of items
	 */
	public int getCount() {
		return mItems.size();
	}

	/**
	 * clear
	 */
	public void clear() {
		mItems.clear();
	}

	/**
	 * Get items.
	 * @return items
	 */
	public ArrayList<MediaItem> getItem() {
		return mItems;
	}

	/**
	 * Get item.
	 * @param index the index of the MediaItem to return. 
	 * @return the media item at the specified index.
	 */
	public MediaItem get(int index) {
		return mItems.get(index);
	}

	/**
	 * Replaces the element at the specified location in this ArrayList with the specified MediaItem.
	 * @param index the index at which to put the specified MediaItem.
	 * @param mediaItem the MediaItem to add.
	 */
	public void set(int index, MediaItem mediaItem) {
		if (index >= mItems.size()) return;
		mItems.set(index, mediaItem);
	}

	/**
	 * Removes the MediaItem at the specified location from this list.
	 * @param index the index of the MediaItem to remove. 
	 */
	public void remove(int index) {
		if (index >= mItems.size()) return;
		mItems.remove(index);
	}

	/**
	 * Inserts the specified object into this ArrayList at the specified location. 
	 * @param index the index at which to insert the MediaItem.
	 * @param mediaItem the MediaItem to add.
	 */
	public void add(int index, MediaItem mediaItem) {
		if (index >= mItems.size()) {
			mItems.add(mediaItem);
		} else {
			mItems.add(index, mediaItem);
		}
	}
}
