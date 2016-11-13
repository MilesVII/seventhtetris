package com.milesseventh.tetris;

public class Phrase {
	private String text;
	public static final int HIDE_STEP = 2, MAX_TRANSPARENCY = 100;
	private int transparency = MAX_TRANSPARENCY;
	public Phrase(String _text){
		text = _text;
	}
	
	public boolean tryHide(){
		transparency -= HIDE_STEP;
		return (transparency == 0);
	}

	public String getText(){
		return text;
	}
	
	public float getTransparency(){
		return transparency;
	}
}
