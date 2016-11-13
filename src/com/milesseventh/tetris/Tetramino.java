package com.milesseventh.tetris;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class Tetramino {
	static public enum TetraType{
		I, O, T, S, Z, L, J, X
	}
	private static Random r = new Random();
	
	public TetraType type;
	public boolean[][] arr;
	
	private int x = Tetris.GLASS_W / 2 - 1, y = Tetris.GLASS_H - 1;//RO
	@SuppressWarnings("unchecked")
	public static ArrayList<TetraType> bagSample = new ArrayList<TetraType>(Arrays.asList(new TetraType[] {
										TetraType.I, TetraType.O, TetraType.T, TetraType.S,  
										TetraType.Z, TetraType.L, TetraType.J
										})), bag = (ArrayList<TetraType>) bagSample.clone();
	private static TetraType nextTetramino = extractNextTetramino();
	private static boolean[][] nextTetraminoAsArray;
	private boolean collisionOnTheRight, stuck = true;
	private Tetris host;
	private int ghostPieceY;
	
	public Tetramino(Tetris _host){
		host = _host;
		if (host.strangeModeIsOn && ((float)host.linesCleared / 500) > r.nextFloat()){
			type = TetraType.X;
			host.shout("Strange piece!");
		} else
			type = extractNextTetramino();
		arr = getArray(type);
		y -= getSideSize();
		calculateGhostPieceY();
	}
	
	public void rotateCW(){
		short _size = getSideSize();
		boolean[][] _out = new boolean[_size][_size];
		for (int xx = 0; xx < _size; xx++)
			for (int yy = 0; yy < _size; yy++)
				_out[xx][yy] = arr[_size - 1 - yy][xx];

		if (!isLegal(arr, x, y - 1, _size))
			host.resetGravityTimer();
		if (isLegal(_out, x, y, _size)){
			arr = _out;
			calculateGhostPieceY();
		} else {
			boolean _t = collisionOnTheRight;
			for(short _shake = 1; _shake <= 3; _shake++)
				if (isLegal(_out, x + (_t?-_shake:_shake), y, _size)){
					x += (_t?-1:1);
					arr = _out;
					calculateGhostPieceY();
					break;
				}
		}
	}
	
	public void move(boolean _toright){
		int _off = (_toright?1:-1);
		if (isLegal(arr, x + _off, y, getSideSize())){
			x += _off;
			calculateGhostPieceY();
		}
	}
	
	private void calculateGhostPieceY(){
		ghostPieceY = y;
		do {
			ghostPieceY--;
		} while (isLegal(arr, x, ghostPieceY, getSideSize()));
		ghostPieceY++;
	}
	
	public void dragDown(){
		if (isLegal(arr, x, y - 1, getSideSize())){
			y--;
			stuck = false;
		} else
			host.dodgeToGlass(this, stuck);
	}
	
	private boolean isLegal(boolean[][] _in, int _x, int _y, short _size){
		boolean _glassblock;
		for (int xx = 0; xx < _size; xx++)
			for (int yy = 0; yy < _size; yy++){
				try{
					_glassblock = host.glass[_x + xx][_y + yy];
				} catch (IndexOutOfBoundsException _ex) {
					_glassblock = true;
				}
				if (_in[xx][yy] && _glassblock){
					if (xx > 1)
						collisionOnTheRight = true;
					else
						collisionOnTheRight = false;
					return false;
				}
			}
		return true;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public int getGhostY(){
		return ghostPieceY;
	}
	
	private static TetraType extractNextTetramino(){
		TetraType _t = nextTetramino;
		if (bag.size() == 0)
			bagReset();
		nextTetramino = bag.get(r.nextInt(bag.size()));
		bag.remove(nextTetramino);
		nextTetraminoAsArray = getArray(nextTetramino);
		if (_t == null)
			return nextTetramino;
		else
			return _t;
	}
	
	@SuppressWarnings("unchecked")
	public static void bagReset(){
		bag = (ArrayList<TetraType>) bagSample.clone();
	}
	
	public static boolean[][] getArray(TetraType _type){
		switch(_type){
		case I:
			return new boolean[][]{
				{false, false, false, false},
				{true, true, true, true},
				{false, false, false, false},
				{false, false, false, false}
			};
		case O:
			return new boolean[][]{
				{false, false, false, false},
				{false, true, true, false},
				{false, true, true, false},
				{false, false, false, false}
			};
		case T:
			return new boolean[][]{
				{false, true, false},
				{false, true, true},
				{false, true, false}
			};
		case S:
			return new boolean[][]{
				{false, true, false},
				{false, true, true},
				{false, false, true}
			};
		case Z:
			return new boolean[][]{
				{false, false, true},
				{false, true, true},
				{false, true, false}
			};
		case J:
			return new boolean[][]{
				{false, true, true},
				{false, true, false},
				{false, true, false}
			};
		case L:
			return new boolean[][]{
				{false, true, false},
				{false, true, false},
				{false, true, true}
			};
		case X:
			switch (r.nextInt(10)){
			case (0):
				return new boolean[][]{
					{true, false, false},
					{true, false, false},
					{true, true, true}
				};
			case (1):
				return new boolean[][]{
					{true, true, true},
					{true, false, false},
					{true, true, true}
				};
			case (2):
				return new boolean[][]{
					{true, false, true},
					{false, true, false},
					{true, false, true}
				};
			case (3):
				return new boolean[][]{
					{false, false, true},
					{true, true, true},
					{true, false, false}
				};
			case (4):
				return new boolean[][]{
					{true, false, false},
					{true, true, true},
					{false, false, true}
				};
			case (5):
				return new boolean[][]{
					{true, false, true},
					{true, true, true},
					{true, false, true}
				};
			case (6):
				return new boolean[][]{
					{true, true, true},
					{true, false, true},
					{true, true, true}
				};
			case (7):
				return new boolean[][]{
					{false, true, false},
					{false, true, false},
					{true, true, true}
				};
			case (8):
				return new boolean[][]{
					{true, false, true},
					{false, false, false},
					{true, false, true}
				};
			case (9):
				return new boolean[][]{
					{r.nextBoolean(), r.nextBoolean(), r.nextBoolean()},
					{r.nextBoolean(), true, r.nextBoolean()},
					{r.nextBoolean(), r.nextBoolean(), r.nextBoolean()}
				};
			}
		}
		return null;
	}
	
	public static short getSideSize(TetraType _type){
		switch(_type){
		case I:
		case O:
			return 4;
		default:
			return 3;
		}
	}
	
	static public TetraType getNextTetramino(){
		return nextTetramino;
	}
	
	static public boolean[][] getNextTetraminoAsArray(){
		return nextTetraminoAsArray;
	}
	
	public short getSideSize(){
		return getSideSize(type);
	}
}
