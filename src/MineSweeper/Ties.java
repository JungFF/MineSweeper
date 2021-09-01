package MineSweeper;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.Serializable;

public class Ties implements Serializable {
	private static final long serialVersionUID = 1L;
	public static final Image bomb1 = Toolkit.getDefaultToolkit().getImage("Resource/9.png");
	public static final Image bomb2 =  Toolkit.getDefaultToolkit().getImage("Resource/9.png");
	private int numberW = 15;
	private int numberH = 15;
	private transient Client xzc;
	public static final Image twoBomb =  Toolkit.getDefaultToolkit().getImage("Resource/2.png");
	public static final Image threeBomb =  Toolkit.getDefaultToolkit().getImage("Resource/3.png");
	public static final Image fourBomb =  Toolkit.getDefaultToolkit().getImage("Resource/4.png");
	private int x;
	private int y;
	private int numberOfBlocked;
	private int numberOfUnblocked = 0;
	public static final Image zeroBomb =  Toolkit.getDefaultToolkit().getImage("Resource/0.png");
	public static final Image eightBomb =  Toolkit.getDefaultToolkit().getImage("Resource/8.png");
	public static final Image flag1 =  Toolkit.getDefaultToolkit().getImage("Resource/11.png");
	public static final Image flag2 =  Toolkit.getDefaultToolkit().getImage("Resource/12.png");
	public static final Image oneBomb =  Toolkit.getDefaultToolkit().getImage("Resource/1.png");
	public static final Image fiveBomb =  Toolkit.getDefaultToolkit().getImage("Resource/5.png");
	public static final Image sixBomb =  Toolkit.getDefaultToolkit().getImage("Resource/6.png");
	public static final Image severnBomb =  Toolkit.getDefaultToolkit().getImage("Resource/7.png");
	public static final Image normal =  Toolkit.getDefaultToolkit().getImage("Resource/10.png");

	public Ties() {
		super();
	}

	public Ties(int x, int y, int numberOfBlocked, Client xzc) {
		super();
		this.x = x;
		this.y = y;
		this.numberOfBlocked = numberOfBlocked;
		this.xzc = xzc;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getNumberOfBlocked() {
		return numberOfBlocked;
	}

	public void setNumberOfBlocked(int numberOfBlocked) {
		this.numberOfBlocked = numberOfBlocked;
	}

	public int getNumberOfUnblocked() {
		return numberOfUnblocked;
	}

	public void setNumberOfUnblocked(int numberOfUnblocked) {
		this.numberOfUnblocked = numberOfUnblocked;
	}
	public void draw(Graphics g) {
		switch (numberOfBlocked) {
		case 0:
			g.drawImage(zeroBomb, x, y, numberW, numberH, xzc);
			break;
		case 1:
			g.drawImage(oneBomb, x, y, numberW, numberH, xzc);
			break;
		case 2:
			g.drawImage(twoBomb, x, y, numberW, numberH, xzc);
			break;
		case 3:
			g.drawImage(threeBomb, x, y, numberW, numberH, xzc);
			break;
		case 4:
			g.drawImage(fourBomb, x, y, numberW, numberH, xzc);
			break;
		case 5:
			g.drawImage(fiveBomb, x, y, numberW, numberH, xzc);
			break;
		case 6:
			g.drawImage(sixBomb, x, y, numberW, numberH, xzc);
			break;
		case 7:
			g.drawImage(severnBomb, x, y, numberW, numberH, xzc);
			break;
		case 8:
			g.drawImage(eightBomb, x, y, numberW, numberH, xzc);
			break;
		case 9:
			g.drawImage(bomb1, x, y, numberW, numberH, xzc);
			break;
		case 10:
			g.drawImage(bomb2, x, y, numberW, numberH, xzc);
			break;
		case 11:
			g.drawImage(flag1, x, y, numberW, numberH, xzc);
			break;
		case 12:
			g.drawImage(flag2, x, y, numberW, numberH, xzc);
			break;
		case 13:
			g.drawImage(normal, x, y, numberW, numberH, xzc);
			break;
		}
	}
	
	public Rectangle getRec() {
		return new Rectangle(x, y, numberW, numberH);
	}
}
