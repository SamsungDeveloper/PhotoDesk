package com.samsung.photodesk.editor;

import android.os.Parcel;
import android.os.Parcelable;

/**
 *	<p>SAMM file information class.</p>
 *  Managing for SAMM file path , background music file path , voice record file path. 
 */
public class AnimationData implements Parcelable{
	private String mAnimationPath;
	private String mMidiPath;
	private String mVoicePath;
	
	
	public AnimationData(String aniPath, String midiPath, String voicePath){
		this.mAnimationPath = aniPath;
		this.mMidiPath = midiPath;
		this.mVoicePath = voicePath;
		
	}
	public AnimationData(){}
	
	/**
	 * <p>Get animation file path</p>
	 * @return		animation file path
	 */
	public String getAnimationPath() {
		return mAnimationPath;
	}
	
	/**
	 * <p>Get animation background music path</p>
	 * @return	animation background music path
	 */
	public String getMidiPath() {
		return mMidiPath;
	}
	
	/**
	 * <p>Get animation voice file path</p>
	 * @return	animation voice file path
	 */
	public String getVoicePath() {
		return mVoicePath;
	}

	 @Override
	public int describeContents() {
        return 0;
    }
    @Override
	public void writeToParcel(Parcel dest, int flags) {
       dest.writeString(mAnimationPath);
       dest.writeString(mMidiPath);
       dest.writeString(mVoicePath);
    }
    
    public static final Parcelable.Creator<AnimationData> CREATOR = new Creator<AnimationData>() {
        @Override
		public AnimationData createFromParcel(Parcel source) {
        	
        	AnimationData data = new AnimationData();
        	
        	data.mAnimationPath = source.readString();
        	data.mMidiPath = source.readString();
        	data.mVoicePath = source.readString();
        	
            return data;
        }
        @Override
		public AnimationData[] newArray(int size) {
            return new AnimationData[size];
        }
    };
}
