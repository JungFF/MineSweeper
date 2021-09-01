package MineSweeper;

import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Random;

public class NewMouseListener extends MouseAdapter {
	private Client xzc;
	private int countOfCol;
	private boolean isFirstClick;
	private ArrayList<Ties> tiesList = new ArrayList<Ties>();
	boolean[] noBee;
	private int countOfRow;
	public NewMouseListener() {
		super();
	}

	public NewMouseListener(Client xzc) {
		super();
		this.xzc = xzc;
		countOfCol = xzc.getColCount();
		countOfRow = xzc.getRowCount();
		noBee = new boolean[countOfCol * countOfRow];
		tiesList = xzc.getBombList();
		this.isFirstClick=xzc.isFirstTouch();
	}
	private void tiesCheck() {
		for (Ties ties : tiesList) {
			boolean edgeU = false, edgeD = false;
			int num = tiesList.indexOf(ties);
			if ((num + 1) % (countOfCol) != 0)
				edgeU = true;
			if (num % (countOfCol) != 0)
				edgeD = true;
			if (ties.getNumberOfUnblocked() != 9) {
				if (decide(num - 1) && edgeD)
					ties.setNumberOfUnblocked(ties.getNumberOfUnblocked() + 1);
				if (decide(num + 1) && edgeU)
					ties.setNumberOfUnblocked(ties.getNumberOfUnblocked() + 1);
				if (decide(num - countOfCol))
					ties.setNumberOfUnblocked(ties.getNumberOfUnblocked() + 1);
				if (decide(num + countOfCol))
					ties.setNumberOfUnblocked(ties.getNumberOfUnblocked() + 1);
				if (decide(num - countOfCol + 1) && edgeU)
					ties.setNumberOfUnblocked(ties.getNumberOfUnblocked() + 1);
				if (decide(num - countOfCol - 1) && edgeD)
					ties.setNumberOfUnblocked(ties.getNumberOfUnblocked() + 1);
				if (decide(num + countOfCol + 1) && edgeU)
					ties.setNumberOfUnblocked(ties.getNumberOfUnblocked() + 1);
				if (decide(num + countOfCol - 1) && edgeD)
					ties.setNumberOfUnblocked(ties.getNumberOfUnblocked() + 1);
			}
		}
	}



	public void mouseReleased(MouseEvent e) {
		if (xzc.getStateOfGame().equals("lose")) {
			return;
		}
		int x = e.getX();
		int y = e.getY();
		Rectangle rec = new Rectangle(x, y, 1, 1);
		if (e.getButton() == MouseEvent.BUTTON1) {
			for (Ties ties : tiesList) {
				if (ties.getNumberOfBlocked() != 11){
					if (rec.intersects(ties.getRec())) {
						if (ties.getNumberOfUnblocked() == 9) {
							xzc.setStateOfGame("lose");
						} else {
							if (ties.getNumberOfUnblocked() == 0) {
								augumentLimit(tiesList.indexOf(ties));
							}
							ties.setNumberOfBlocked(ties.getNumberOfUnblocked());
						}

					}
				}
			}
		}
		if (e.getButton() == MouseEvent.BUTTON3) {
				for (Ties ties : tiesList) {
					if (rec.intersects(ties.getRec())) {
						if(ties.getNumberOfBlocked()!= ties.getNumberOfUnblocked()){
							if(xzc.getRemainedMine() > 0){
								if(ties.getNumberOfBlocked()==13){
									ties.setNumberOfBlocked(11);
								}
								else if(ties.getNumberOfBlocked()==11){
									ties.setNumberOfBlocked(13);
								}
							}
							else{
								if(ties.getNumberOfBlocked() == 11){
									ties.setNumberOfBlocked(13);
								}
							}

						}
					}


			}
		}

	}

	private void augumentLimit(int index) {
		if (noBee[index])
			return;
		noBee[index] = true;
		boolean edgeU = false, edgeD = false;
		if ((index + 1) % (countOfCol) != 0)
			edgeU = true;
		if (index % (countOfCol) != 0)
			edgeD = true;
		if (judgeLimit(index - 1) && edgeD) {
			Ties ties = tiesList.get(index - 1);
			NowSetNoBee(ties, index - 1);
		}

		if (judgeLimit(index + 1) && edgeU) {
			Ties ties = tiesList.get(index + 1);
			NowSetNoBee(ties, index + 1);
		}

		if (judgeLimit(index - countOfCol)) {
			Ties ties = tiesList.get(index - countOfCol);
			NowSetNoBee(ties, index - countOfCol);
		}

		if (judgeLimit(index + countOfCol)) {
			Ties ties = tiesList.get(index + countOfCol);
			NowSetNoBee(ties, index + countOfCol);
		}

		if (judgeLimit(index - countOfCol + 1) && edgeU) {
			Ties ties = tiesList.get(index - countOfCol + 1);
			NowSetNoBee(ties, index - countOfCol + 1);
		}

		if (judgeLimit(index - countOfCol - 1) && edgeD) {
			Ties ties = tiesList.get(index - countOfCol - 1);
			NowSetNoBee(ties, index - countOfCol - 1);
		}

		if (judgeLimit(index + countOfCol + 1) && edgeU) {
			Ties ties = tiesList.get(index + countOfCol + 1);
			NowSetNoBee(ties, index + countOfCol + 1);
		}

		if (judgeLimit(index + countOfCol - 1) && edgeD) {
			Ties ties = tiesList.get(index + countOfCol - 1);
			NowSetNoBee(ties, index + countOfCol - 1);
		}

	}

	private boolean judgeLimit(int i) {
		if (i >= 0 && i < tiesList.size())
			return true;
		return false;
	}


	public void NowSetNoBee(Ties ties, int index) {
			if (ties.getNumberOfBlocked() == ties.getNumberOfUnblocked() && ties.getNumberOfBlocked() != 0)
				return;
			if (ties.getNumberOfUnblocked() >= 0 && ties.getNumberOfUnblocked() <= 8 && ties.getNumberOfUnblocked() != 9) {
				ties.setNumberOfBlocked(ties.getNumberOfUnblocked());
				if (ties.getNumberOfBlocked() == 0)
					augumentLimit(index);
			} else {
				augumentLimit(index);
			}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (xzc.getStateOfGame().equals("lose")) {
			return;
		}
		if (e.getButton() == MouseEvent.BUTTON1 || e.getButton() == MouseEvent.BUTTON3) {
			if (isFirstClick) {
				isFirstClick = false;
				xzc.setFirstTouch(false);
				initTies(e);
				tiesCheck();
			}
		}
	}

	private void initTies(MouseEvent e) {
		int x = e.getX();
		int y = e.getY();
		Rectangle rec = new Rectangle(x, y, 1, 1);
		Ties tiesTemp =new Ties();
		int what=0;
		for (Ties ties : tiesList) {
			if(rec.intersects(ties.getRec())){
				what= ties.getNumberOfUnblocked();
				tiesTemp = ties;
				ties.setNumberOfUnblocked(9);
				break;
			}
		}
		Random r = new Random();
		for (int i = 0; i < xzc.getMineNum(); i++) {
			while (true) {
				int index = r.nextInt(tiesList.size());
				if (tiesList.get(index).getNumberOfUnblocked() != 9) {
					tiesList.get(index).setNumberOfUnblocked(9);
					break;
				}
			}
		}
		tiesTemp.setNumberOfUnblocked(what);
	}
	private boolean decide(int x) {
		if (x >= 0 && x < tiesList.size()) {
			if (tiesList.get(x).getNumberOfUnblocked() == 9)
				return true;
		}
		return false;
	}
}
