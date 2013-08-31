package org.lyricue.android;

public class PlaylistItem {
	public String title="";
	public String type="";
	public Long id=(long)0;
	public Long data=(long)0;
	
	public PlaylistItem(Long id, String title, String type, Long data){
		this.title=title;
		this.id=id;
		this.type=type;
		this.data=data;
	}
}
