package com.milesseventh.tetris;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;

public class IPU implements InputProcessor {
	public enum MovingState{L, N, R}
	private MovingState movingState = MovingState.N;
	private Tetris host;
	
	public IPU (Tetris _host){
		host = _host;
	}
	
	@Override
	public boolean keyDown(int keycode) {
		if (host.isPaused())
			return false;
		switch(keycode){
		case Keys.LEFT:
		case Keys.A:
			host.fallingTetramino.move(false);
			movingState = MovingState.L;
			return true;
		case Keys.RIGHT:
		case Keys.D:
			host.fallingTetramino.move(true);
			movingState = MovingState.R;
			return true;
		case Keys.UP:
		case Keys.W:
			host.fallingTetramino.rotateCW();
			return true;
		case Keys.DOWN:
		case Keys.S:
			host.toggleDragDown(true);
			return true;
		case Keys.BACK:
		case Keys.MENU:
		case Keys.ESCAPE:
			host.pause(!host.isPaused());
			return false;
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		if (host.isPaused())
			return false;
		switch(keycode){
		case Keys.DOWN:
		case Keys.S:
			host.toggleDragDown(false);
			return true;
		case Keys.LEFT:
		case Keys.A:
		case Keys.RIGHT:
		case Keys.D:
			movingState = MovingState.N;
			return true;
		default:
			return false;
		}
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		float _x = screenX / (float)Gdx.graphics.getWidth();
		float _y = screenY / (float)Gdx.graphics.getHeight();

		if (_x < 0.5f){
			if (_y > 0.5f)
				keyDown(Keys.LEFT);
			else
				keyDown(Keys.UP);
		} else {
			if (_y > 0.5f)
				keyDown(Keys.RIGHT);
			else
				keyDown(Keys.DOWN);
		}
		
		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		float _x = screenX / (float)Gdx.graphics.getWidth();
		float _y = screenY / (float)Gdx.graphics.getHeight();
		
		if (!host.isPaused())
			if (_x < 0.5f){
				if (_y > 0.5f)
					keyUp(Keys.LEFT);
				else
					keyUp(Keys.UP);
			} else {
				if (_y > 0.5f)
					keyUp(Keys.RIGHT);
				else
					keyUp(Keys.DOWN);
			}
		
		return true;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public MovingState getMovingState(){
		return movingState;
	}
}