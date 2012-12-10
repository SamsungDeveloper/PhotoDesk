package com.samsung.photodesk.data;

/**
 *  In the gallery items, including basic functions and abstract classes
 *
 */
abstract public class MediaObject {
	public static final int IMAGE = 0;
	public static final int VIDEO = 1;
	public static final int FOLDER = 2;

	/**
	 * Get the path.
	 * @return the path.
	 */
	abstract public String getPath();
	
	/**
	 * Get the type
	 * @return type - IMAGE, VIDEO, FOLDER
	 */
	abstract public int getType();
	
	/**
	 * Check the state is selected.
	 * @return selected is true Otherwise false
	 */
	abstract public boolean isSelected();
	
	/**
	 * Set the selected
	 * @param selected
	 */
	abstract public void setSelected(boolean selected);
	
	/**
	 * Get bucket id.
	 * @return the bucket id
	 */
	abstract public long getId();
	
	/**
	 * Get the name showing.
	 * @return display name
	 */
	abstract public String getDisplayName();
	
	/**
	 * Protection settings. 
	 * @param protect Protection whether
	 */
	abstract public void setProtected(boolean protect);
	
	/**
	 * change protected status
	 */
	abstract public void changeProtectedStatus();
	
	/**
	 * Check the state is protected.
	 * @return protected is true Otherwise false
	 */
	abstract public boolean isProtected();
}
