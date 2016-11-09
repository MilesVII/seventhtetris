package com.milesseventh.tetris;

public class MenuEntry {
	public interface MEAction {
		public void run(MenuEntry _me, boolean _inc);
	}
	private String title;
	private MEAction action;
	private float x, y;
	
	public MenuEntry(String _title, MEAction _action){
		title = _title;
		action = _action;
	}
	
	public void call(boolean _inc){
		action.run(this, _inc);
	}

	public String getTitle(){
		return title;
	}
	
	public void setTitle(String _title){
		title = _title;
	}

	public void setPositionOffset(float _x, float _y){
		x = _x; y = _y; 
	}
	
	public float getPositionOffset(boolean _isX){
		return _isX?x:y;
	}
}
