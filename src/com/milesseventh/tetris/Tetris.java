package com.milesseventh.tetris;

import java.util.ArrayList;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Tetris extends ApplicationAdapter {
	public static final int GLASS_W = 12, GLASS_H = 22;//Only GLASS_H - 2 cells are rendered
    public static final int MARGIN = 1;//In cells
    public static final int SIDEBAR_W = MARGIN + 4;//In cells
    public static final int SCREEN_W = GLASS_W + SIDEBAR_W;//In cells
    public static final int SCREEN_H = GLASS_H - 2;//In cells
    public static final int GUI_MULTIPLIER = 32;
    public static final float MOVE_DELAY = .35f, MOVE_STEP = .07f;//In secs
    private static float SPEED = 2f; 
    private static final float FLASH_SPEED = 0.05f;//Frequency
    private static final Color GRID_COL = Color.GREEN;
    private static final Color TM_INACTIVE_COL = GRID_COL;
    private static final Color TM_ACTIVE_COL = Color.WHITE;
    private static final Color TM_GHOST_COL = Color.GRAY;
    private static final Color FLASH_COL = Color.GREEN.cpy();
    private static final Color TEXT_COL = Color.WHITE;
    private static final Color SHOUT_COL = Color.WHITE.cpy();
    
    public boolean[][] glass = new boolean[GLASS_W][GLASS_H];
    public Tetramino fallingTetramino;
    public int linesCleared = 0, selectedEntry = 0;
    private boolean isPermaDragging;
	private Viewport viewport;
    private Camera camera, guiCamera;
    private ShapeRenderer sr; 
    private BitmapFont font, shoutFont;
    private Batch batch;
    private float timer = 1 / SPEED, moveTimer = MOVE_DELAY, dragDownTimer = MOVE_STEP;
    private IPU inputProcessor = new IPU(this);
    private float[] flashes = new float[SCREEN_H];
    private boolean isPaused = true;
    public boolean strangeModeIsOn = true, traditionalModeIsOn = true, ghostPieceIsOn = false;
    private GlyphLayout glay = new GlyphLayout();
    private ArrayList<Phrase> phrases= new ArrayList<Phrase>();
    
	private MenuEntry.MEAction menu_ng = new MenuEntry.MEAction(){
		@Override
		public void run(MenuEntry _me, boolean _inc){
			reset();
			isPaused = false;
		}
	};
	private MenuEntry.MEAction menu_con = new MenuEntry.MEAction(){
		@Override
		public void run(MenuEntry _me, boolean _inc){
			isPaused = false;
		}
	};
	private MenuEntry.MEAction menu_stm = new MenuEntry.MEAction(){
		@Override
		public void run(MenuEntry _me, boolean _inc){
			strangeModeIsOn = !strangeModeIsOn;
			_me.setTitle("Strange mode is " + (strangeModeIsOn?"on":"off"));
		}
	};
	private MenuEntry.MEAction menu_gp = new MenuEntry.MEAction(){
		@Override
		public void run(MenuEntry _me, boolean _inc){
			ghostPieceIsOn = !ghostPieceIsOn;
			_me.setTitle("Ghost piece is " + (ghostPieceIsOn?"on":"off"));
		}
	};
	private MenuEntry.MEAction menu_tm = new MenuEntry.MEAction(){
		@Override
		public void run(MenuEntry _me, boolean _inc){
			traditionalModeIsOn = !traditionalModeIsOn;
			_me.setTitle("Growing speed is " + (traditionalModeIsOn?"on":"off"));
		}
	};
	private MenuEntry.MEAction menu_spd = new MenuEntry.MEAction(){
		@Override
		public void run(MenuEntry _me, boolean _inc){
			SPEED += _inc?.5f:-.5f;
			if (SPEED < .5f)
				SPEED = .5f;
			_me.setTitle("< Speed: " + SPEED + " Hz >");
		}
	};
	private MenuEntry.MEAction menu_ex = new MenuEntry.MEAction(){
		@Override
		public void run(MenuEntry _me, boolean _inc){
			Gdx.app.exit();
		}
	};
	public final MenuEntry[] MENU = new MenuEntry[]{
			new MenuEntry("New game", menu_ng),
			new MenuEntry("Continue", menu_con),
			new MenuEntry("Strange mode is " + (strangeModeIsOn?"on":"off"), menu_stm),
			new MenuEntry("Ghost piece is " + (ghostPieceIsOn?"on":"off"), menu_gp),
			new MenuEntry("Growing speed is " + (traditionalModeIsOn?"on":"off"), menu_tm),
			new MenuEntry("< Speed: " + SPEED + " Hz >", menu_spd),
			new MenuEntry("Exit", menu_ex)
		};
	
	@Override
	public void create () {
		Gdx.input.setInputProcessor(inputProcessor);
		Gdx.input.setCatchBackKey(true);
		Gdx.input.setCatchMenuKey(true);
		
		FreeTypeFontGenerator ftfg = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Prototype.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 28;
		parameter.characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz:0123456789.<>!?";
		font = ftfg.generateFont(parameter);
		parameter.size = 50;
		shoutFont = ftfg.generateFont(parameter);
		font.setColor(TEXT_COL);
		ftfg.dispose();
		
        camera = new OrthographicCamera(SCREEN_W, SCREEN_H);
        viewport = new FitViewport(SCREEN_W, SCREEN_H/*- 2 + 2*/, camera);
		sr = new ShapeRenderer(); 
		sr.setProjectionMatrix(camera.combined);
		sr.translate(-SCREEN_W / 2.0f, -SCREEN_H / 2.0f, 0);
		
		guiCamera = new OrthographicCamera(SCREEN_W * GUI_MULTIPLIER, SCREEN_H * GUI_MULTIPLIER);
        batch = new SpriteBatch();
		batch.setProjectionMatrix(guiCamera.combined);
        
        fallingTetramino = new Tetramino(this);
        
        for (int _lick = 0; _lick < SCREEN_H; _lick++)
        	flashes[_lick] = 0;
        
        for (int _tos = 0; _tos < MENU.length; _tos++){
        	glay.setText(font, MENU[_tos].getTitle());
        	MENU[_tos].setPositionOffset(glay.width / 2, glay.height / 2);
        }
	}
	
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void render(){
		Gdx.gl.glClearColor(0, 0, 0, 0);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		camera.update();
		if (isPaused)
			render_menu();
		else
			render_game();
    }
    
	public void render_game() {
		update(Gdx.graphics.getDeltaTime());
        Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		

		sr.setColor(GRID_COL);
		sr.begin(ShapeType.Line);
		//Glass grid
		drawGrid(sr, 0, 0, GLASS_W, GLASS_H);
		//Nextmino grid
		drawGrid(sr, GLASS_W + MARGIN, SCREEN_H - 4 - MARGIN, 4, 4);
		sr.end();
		
		sr.setColor(TM_ACTIVE_COL);
		sr.begin(ShapeType.Filled);
		//Ghost piece
		if (ghostPieceIsOn)
			for (int xx = 0; xx < fallingTetramino.getSideSize(); xx++)
				for (int yy = 0; yy < fallingTetramino.getSideSize(); yy++)
					if (fallingTetramino.arr[xx][yy])
						sr.rect(fallingTetramino.getX() + xx, fallingTetramino.getGhostY() + yy, 1, 1,
								TM_GHOST_COL, TM_GHOST_COL, TM_GHOST_COL, TM_GHOST_COL);

		//Active tetramino
		for (int xx = 0; xx < fallingTetramino.getSideSize(); xx++)
			for (int yy = 0; yy < fallingTetramino.getSideSize(); yy++)
				if (fallingTetramino.arr[xx][yy])
					sr.rect(fallingTetramino.getX() + xx, fallingTetramino.getY() + yy, 1, 1);
		
		//Next tetramino
		short _size = Tetramino.getSideSize(Tetramino.getNextTetramino());
		boolean[][] _t = Tetramino.getNextTetraminoAsArray();
		for (int xx = 0; xx < _size; xx++)
			for (int yy = 0; yy < _size; yy++)
				if (_t[xx][yy]){
					try {
						sr.rect(GLASS_W + MARGIN + xx, SCREEN_H - 4 - MARGIN + yy, 1, 1);
					} catch (IndexOutOfBoundsException _ex){}
				}
		
		sr.setColor(TM_INACTIVE_COL);
		//Inactive tetraminos
		for (int xx = 0; xx < GLASS_W; xx++)
			for (int yy = 0; yy < SCREEN_H; yy++)
				if (glass[xx][yy])
					sr.rect(xx, yy, 1, 1);
		
		//Flashes
		for (int _spark = 0; _spark < SCREEN_H; _spark++)
			if (flashes[_spark] > 0f){
				FLASH_COL.a = flashes[_spark];
				sr.setColor(FLASH_COL);
				sr.rect(0, _spark, GLASS_W, 1);
				flashes[_spark] -= FLASH_SPEED;
			}
		sr.end();
		
		//Text
		batch.begin();
		font.draw(batch, "  Lines:" + linesCleared + (traditionalModeIsOn?("\n  Speed:" + SPEED + "Hz"):""), 3.2f * GUI_MULTIPLIER, 0);
		if (phrases.size() > 0){
			SHOUT_COL.a = phrases.get(0).getTransparency() / (float)Phrase.MAX_TRANSPARENCY;
			shoutFont.setColor(SHOUT_COL);
			glay.setText(shoutFont, phrases.get(0).getText());
			shoutFont.draw(batch, phrases.get(0).getText(), GUI_MULTIPLIER * (-SCREEN_W / 2 + GLASS_W / 2) - glay.width / 2f, GUI_MULTIPLIER * SCREEN_H * .25f);
			if (phrases.get(0).tryHide())
				phrases.remove(0);
		}
		batch.end();
	}

	public void render_menu() {
		int _displayH = Gdx.graphics.getHeight();
		float _partHeight = 1 / (float)MENU.length;

		if (Gdx.input.isKeyJustPressed(Keys.UP)){
			selectedEntry--;
			if (selectedEntry < 0)
				selectedEntry = MENU.length - 1;
		} else if (Gdx.input.isKeyJustPressed(Keys.DOWN)){
			selectedEntry++;
			if (selectedEntry == MENU.length)
				selectedEntry = 0;
		}
		
		if (Gdx.input.isKeyJustPressed(Keys.ENTER))
			MENU[selectedEntry].call(true);
		
		batch.begin();
		for (int tearsOfSorrow = 0; tearsOfSorrow < MENU.length; tearsOfSorrow++){
			if (Gdx.input.justTouched()){
				if (Gdx.input.getY() / (float)_displayH < (tearsOfSorrow + 1) / (float)MENU.length &&
					Gdx.input.getY() / (float)_displayH > tearsOfSorrow / (float)MENU.length){
					MENU[tearsOfSorrow].call(Gdx.input.getX() / (float)Gdx.graphics.getWidth() > 0.5f);
				}
			}
			if (Gdx.app.getType() == com.badlogic.gdx.Application.ApplicationType.Desktop)
				if (tearsOfSorrow == selectedEntry)
					font.setColor(Color.GREEN);
				else
					font.setColor(Color.WHITE);
			font.draw(batch, MENU[tearsOfSorrow].getTitle(), -MENU[tearsOfSorrow].getPositionOffset(true), 
					GUI_MULTIPLIER * SCREEN_H * (.5f - _partHeight * (tearsOfSorrow + .5f) + MENU[tearsOfSorrow].getPositionOffset(false) / (float)Gdx.graphics.getHeight()));
		}
		batch.end();
	}
	
	private void drawGrid(ShapeRenderer _sr, int _x, int _y, int _w, int _h){
		for (int xx = _x; xx <= _x + _w; xx++)
			_sr.line(xx, _y, xx, _y + _h);
		for (int yy = _y; yy <= _y + _h; yy++)
			_sr.line(_x, yy, _x + _w, yy);
	}
	
	private void update(float _dt){
		//Gravity
		timer -= _dt;
		if (timer <= 0){
			resetGravityTimer();

    		if (!isPermaDragging)
    			fallingTetramino.dragDown();
		}
		
		//Long move
		switch (inputProcessor.getMovingState()){
		case L:
			moveTimer -= _dt;
			if (moveTimer <= 0){
				fallingTetramino.move(false);
				moveTimer += MOVE_STEP;
			}
			break;
		case R:
			moveTimer -= _dt;
			if (moveTimer <= 0){
				fallingTetramino.move(true);
				moveTimer += MOVE_STEP;
			}
			break;
		case N:
			moveTimer = MOVE_DELAY;
			break;
		}
		
		//Long dragdown
		if (isPermaDragging){
			dragDownTimer -= _dt;
			if (dragDownTimer <= 0){
				dragDownTimer += MOVE_STEP;
				fallingTetramino.dragDown();
			}
		}
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		sr.dispose();
		font.dispose();
	}

	public void toggleDragDown(boolean _onoff){
		isPermaDragging = _onoff;
	}
	
	public void dodgeToGlass(Tetramino __, boolean _eog){
		short _size = __.getSideSize();
		int preLinesCleared = linesCleared, deltaLinesCleared;
		
		for (int yy = 0; yy < _size; yy++){
			for (int xx = 0; xx < _size; xx++)
				if (__.arr[xx][yy])
					glass[__.getX() + xx][__.getY() + yy] = true;
			if (__.getY() + yy >= 0)
				checkLine(__.getY() + yy);
		}
		
		if (_eog){
			//End of the game!
			pause(true);
			return;
		}
		
		dropLines();
		deltaLinesCleared = linesCleared - preLinesCleared;
		switch (deltaLinesCleared){
		case(2):
		case(3):
			shout("" + deltaLinesCleared + "x Combo!");
			break;
		case(4):
			shout("Tetris!");
			break;
		case(5):
			shout("P... Pentis?!");
			break;
		}
		toggleDragDown(false);
		fallingTetramino = new Tetramino(this);
	}
	
	public void checkLine(int _yy){
		for (int xx = 0; xx < GLASS_W; xx++)
			if (!glass[xx][_yy])
				return;
		deleteLine(_yy);
	}
	
	public void pause(boolean _onoff){
		isPaused = _onoff;
	}
	
	public boolean isPaused(){
		return isPaused;
	}
	
	private void deleteLine(int _yy){
		for (int xx = 0; xx < GLASS_W; xx++)
			glass[xx][_yy] = false;
		linesCleared++;
		flashes[_yy] = 1;
		if (traditionalModeIsOn && linesCleared % 10 == 0)
			SPEED += .5f;
	}
	
	private void dropLines(){
		boolean [][] _t = new boolean[GLASS_W][GLASS_H];
		int _yoff = 0;
		for (int yy = 0; yy < GLASS_H; yy++){
			boolean _lineIsEmpty = true;
			for (int xx = 0; xx < GLASS_W; xx++)
				if (glass[xx][yy]){
					_lineIsEmpty = false;
					break;
				}
			if (!_lineIsEmpty){
				for (int xx = 0; xx < GLASS_W; xx++)
					_t[xx][_yoff] = glass[xx][yy];
				_yoff++;
			}
		}
		glass = _t;
	}
	
	public void shout(String _text){
		phrases.add(new Phrase(_text));
	}
	
	public void reset(){
		//Clean the glass
		for (int xx = 0; xx < GLASS_W; xx++)
			for (int yy = 0; yy < GLASS_H; yy++)
				glass[xx][yy] = false;
		//Reset score
		linesCleared = 0;
		//Reset flashes
		for (int _cum = 0; _cum < SCREEN_H; _cum++)
			flashes[_cum] = 0;
		//Reset falling tetramino
        fallingTetramino = new Tetramino(this);
        //Reset bag
        Tetramino.bagReset();
	}
	
	public void resetGravityTimer(){
		timer = 1 / SPEED;
	}
}
