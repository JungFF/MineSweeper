package MineSweeper;

import javafx.util.Pair;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import javax.swing.*;

public class Client extends JFrame {
	private JMenuBar menuBar;
	private JMenu menuForFile, menuForRank;
	private JMenuItem newItem, openItem, saveItem, exitItem, rankItem;
	//The number of mines
	private int mineNum= 40;
	//remaining time
	private int time= 1000;
	private int remainedMine;
	private int normalTie;
	private MineMap map;
	private  int imgWidth = 15;
	private  int imgHeight = 15;
	private int rowCount = 0;
	private int colCount = 0;
	private String stateOfGame = "start";
	private boolean firstTouch = true;
	private ArrayList<Ties> tiesList = new ArrayList<Ties>();
	private int count = 0;
	private Refresh thread;
	private Socket socket;
    private String name;
    private int id;
    private boolean winOrNot = false;
	public Client() {
		createMenu();
		this.initializeClient();
		createList();
		map = new MineMap();
		add(map);
		map.addMouseListener(new NewMouseListener(this));
		thread = new Refresh();
		thread.start();
		this.connectToServer();
	}
	public Client(ArrayList<Object> status){
		createMenu();
		this.initializeClient();
		createList((ArrayList<Ties>) status.get(7));
		map = new MineMap();
		this.add(map);
		count = (int)status.get(1);
		time = (int) status.get(2);
		remainedMine = (int) status.get(3);
		normalTie = (int) status.get(4);
		stateOfGame = (String) status.get(5);
		firstTouch = (boolean) status.get(6);
		map.addMouseListener(new NewMouseListener(this));
		thread = new Refresh();
		thread.start();
	    this.connectToServer();
	}
	public static byte[] transferToByte(Object o){
		try {
			ByteArrayOutputStream arrayOs = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(arrayOs);
			oos.writeObject(o);
			oos.flush();
			byte[] buffer = arrayOs.toByteArray();
			ByteArrayInputStream arrayIs = new ByteArrayInputStream(buffer);
			return buffer;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	private void initializeClient(){
		this.setTitle("Minesweeper");
		this.setSize(280, 340);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setResizable(false);
		this.setVisible(true);
	}
	private void connectToServer(){
		try {
			socket = new Socket("localhost", 8000);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(Client.this ,e.getMessage());
			System.exit(0);
		}
	}



	public static ArrayList<Object> transferToObject(byte[] data) {
		try {
			ByteArrayInputStream arrayIs = new ByteArrayInputStream(data);
			ObjectInputStream ois = new ObjectInputStream(arrayIs);
			ArrayList<Object> temp = (ArrayList<Object>) ois.readObject();
			return temp;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void createMenu() {
		menuForFile=new JMenu("File");
		menuForRank = new JMenu("History");
		menuBar=new JMenuBar();
		newItem=new JMenuItem("New");
		openItem=new JMenuItem("Open");
		saveItem=new JMenuItem("Save");
		exitItem=new JMenuItem("Exit");
		rankItem = new JMenuItem("Rank");
		newItem.addActionListener(new ActionListenerForNew());
		openItem.addActionListener(new ActionListenerForOpen());
        saveItem.addActionListener(new ActionListenerForSave());
		exitItem.addActionListener(e->{
			System.exit(1);
		});
        rankItem.addActionListener(new ActionListenerForRank());
		menuForFile.add(newItem);
		menuForFile.add(openItem);
		menuForFile.add(saveItem);
		menuForFile.add(exitItem);
		menuForRank.add(rankItem);
		menuBar.add(menuForFile);
		menuBar.add(menuForRank);
		this.setJMenuBar(menuBar);
	}
	public class ActionListenerForNew implements ActionListener{
		@Override
		public void actionPerformed(ActionEvent e) {
			Client.this.dispose();
			new Client();
		}
	}
	public class ActionListenerForOpen implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				PanelOpen po = new PanelOpen();
				po.setVisible(true);
				if(po.id.getText().equals("")) {
					JOptionPane.showMessageDialog(Client.this, "Id can not be empty!");
				}
				else{
					socket.sendUrgentData(0xFF);
					ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
					ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());
					ArrayList<Object> temp = new ArrayList<>();
					int num = Integer.parseInt(po.id.getText());
					temp.add(num);
					temp.add("Id for select");
					toServer.writeObject(temp);
					toServer.flush();
					ArrayList<Object> status = new ArrayList<>();
					Object oFromServer = fromServer.readObject();
					if(oFromServer instanceof String){
						JOptionPane.showMessageDialog(Client.this, "Can not find anything in the database!");
					}
					else{
						status = (ArrayList<Object>) oFromServer;
						dispose();
						new Client(status);
					}

				}
			} catch (IOException | ClassNotFoundException ioException) {
				ioException.printStackTrace();
			} catch (NumberFormatException e1){
				JOptionPane.showMessageDialog(Client.this, "Your number format is wrong");
				System.exit(1);
			}

		}
	}
	public class ActionListenerForSave implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				if (Client.this.stateOfGame.equals("lose") || Client.this.stateOfGame.equals("win")){
					JOptionPane.showMessageDialog(Client.this, "Game over, can not save!");
				}
				else{
					socket.sendUrgentData(0xFF);
					ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
					ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());
					thread.flagForSleep = true;
					DialogForSave df = new DialogForSave();
					df.setVisible(true);
					Client.this.name = df.name.getText();
					thread.interrupt();

					ArrayList<Object> elementList = Client.this.saveCurrentElements();
					toServer.writeObject(elementList);
					toServer.flush();

					Integer id = (Integer) fromServer.readObject();
					Client.this.id = id;
					JOptionPane.showMessageDialog(Client.this, "Save successfully! Your id is: " + id);
				}

			} catch (IOException | ClassNotFoundException exception){
				JOptionPane.showMessageDialog(Client.this, "Connection Error! Please make sure" +
						"server is open");
			}
		}
	}
	public class ActionListenerForRank implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			ShowHistory sh = new ShowHistory();
			try {
				socket.sendUrgentData(0xFF);
				ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());
				thread.flagForSleep = true;
				ArrayList<Object> tempForSend = new ArrayList<>();
				tempForSend.add("Open rank list");
				toServer.writeObject(tempForSend);
				ArrayList<Object> realResList = (ArrayList<Object>)fromServer.readObject();

				Pair<String, Integer> pair = null;
				List<Pair<String,Integer>> resList = Collections.synchronizedList(new ArrayList<>());
				for(Object o : realResList){
					ArrayList<Object> ele = (ArrayList<Object>) o;
					pair = new Pair<String,Integer>((String) ele.get(0), (Integer) ele.get(1));

					if(resList.size() == 0){
						resList.add(pair);
					}
					else{
						boolean insertFlag = false;
						for(int i=0; i<resList.size(); i++){
							if(resList.get(i).getValue() <= (Integer)ele.get(1)){
								Integer index = resList.indexOf(resList.get(i));
								resList.add(index, pair);
								insertFlag = true;
								break;
							}
						}
						if(!insertFlag){
							resList.add(pair);
						}
					}
				}
				sh.ta.append("Name\tRemaining time\n");
				if(resList.size()<5){
					//System.out.println(resList.size());
					for(int i = 0; i < resList.size(); i++){
						Pair<String, Integer> r = resList.get(i);
						sh.ta.append(r.getKey().toString() + "\t"+(Integer.parseInt(r.getValue().toString()))+"\n");
					}
				}
				else{
					for(int i=0; i<5; i++){
						System.out.println(resList.size());
						Pair<String, Integer> r = resList.get(i);
						sh.ta.append(r.getKey().toString() + "\t"+(Integer.parseInt(r.getValue().toString()))+"\n");
					}
				}
				sh.setVisible(true);
			} catch (IOException | ClassNotFoundException ioException) {
				JOptionPane.showMessageDialog(Client.this, ioException.getMessage());
				System.exit(1);
			}

		}
	}
	public boolean isFirstTouch() {
		return firstTouch;
	}
	public void setFirstTouch(boolean firstClick) {
		this.firstTouch = firstClick;
	}
	public int getImgWidth() {
		return imgWidth;
	}

	public void setImgWidth(int imgWidth) {
		this.imgWidth = imgWidth;
	}

	public int getImgHeight() {
		return imgHeight;
	}

	public void setImgHeight(int imgHeight) {
		this.imgHeight = imgHeight;
	}

	public MineMap getMyPanel() {
		return this.map;
	}

	public void setMyPanel(MineMap map) {
		this.map = map;
	}
	public String getStateOfGame() {
		return stateOfGame;
	}

	public void setStateOfGame(String stateOfGame) {
		this.stateOfGame = stateOfGame;
	}

	public ArrayList<Ties> getBombList() {
		return tiesList;
	}

	public int getRowCount() {
		return rowCount;
	}

	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	public int getColCount() {
		return colCount;
	}

	public void setColCount(int colCount) {
		this.colCount = colCount;
	}

	public int getMineNum() {
		return mineNum;
	}
	public int getRemainedMine(){
		return remainedMine;
	}

	private ArrayList<Object> saveCurrentElements(){
		ArrayList<Object> elementSet = new ArrayList<>();
		elementSet.add(name);
		elementSet.add(count);
		elementSet.add(time);
		elementSet.add(remainedMine);
		elementSet.add(normalTie);
		elementSet.add(stateOfGame);
		elementSet.add(firstTouch);
		elementSet.add(tiesList);
		elementSet.add("save");
		return elementSet;
	}


	private void createList() {
		for (int i = imgWidth; i < this.getWidth() - 2 * imgWidth; i += imgWidth) {
			for (int j = imgWidth; j < this.getHeight() - 6 * imgWidth; j += imgHeight) {
				rowCount = Math.max(rowCount, i / imgWidth);
				colCount = Math.max(colCount, j / imgWidth);
				Ties ties = new Ties(i, j, 13, this);
				tiesList.add(ties);
			}
		}
	}


	public class MineMap extends JPanel {
		private static final long serialVersionUID = 1L;
		public void paint(Graphics g) {
			super.paintComponent(g);

			remainedMine =mineNum;
			normalTie =0;
			for (Ties ties : tiesList) {
				ties.draw(g);
				if(ties.getNumberOfBlocked()==11 && remainedMine > 0)
					remainedMine--;
				if(ties.getNumberOfBlocked()>=0&& ties.getNumberOfBlocked()<=8)
					normalTie++;
			}


			if (stateOfGame.equals("lose")) {
				for (Ties ties : tiesList) {
					if (ties.getNumberOfUnblocked() == 9) {
						if (ties.getNumberOfBlocked() == 11){
							ties.setNumberOfBlocked(11);
						}else {
							ties.setNumberOfBlocked(ties.getNumberOfUnblocked());
							Font font = new Font("Times New Roman", Font.BOLD, 30);
							g.setFont(font);
							g.setColor(Color.black);
							g.drawString("Game Lost", this.getWidth() / 4, this.getHeight() / 2);
							//thread.continueOrNot = true;
						}
					}
					if (ties.getNumberOfBlocked() == 11 && ties.getNumberOfUnblocked() != 9){
						ties.setNumberOfBlocked(12);
					}
				}
			}

			if(!stateOfGame.equals("lose")&& normalTie +mineNum== colCount * rowCount)
			{
				stateOfGame ="win";
				Font font = new Font("Times New Roman", Font.BOLD, 30);
				g.setFont(font);
				g.setColor(Color.black);
				g.drawString("You win!", this.getWidth() / 4, this.getHeight() / 2);
				thread.continueOrNot = true;
				Client.this.winOrNot = true;
			}
			Font font = new Font("Times New Roman", Font.PLAIN, 15);
			g.setFont(font);
			g.setColor(Color.black);
			g.drawString(String.valueOf(remainedMine), 5, this.getHeight());
			g.drawString("Time Remaining:" + time,80,12);
		}

	}

	public class Refresh extends Thread {
		private volatile boolean continueOrNot = false;
		private volatile boolean flagForSleep = false;
		public boolean getContinueOrNot()
		{
			return this.continueOrNot;
		}
		public void setContinueOrNot(boolean continueOrNot){
			this.continueOrNot = continueOrNot;
		}
		public void run() {
			while (!continueOrNot) {
				repaint();
				if (stateOfGame.equals("start")) {
					if(!firstTouch){
						count+=100;
						if(count==1000){
							count=0;
							time--;
							if (time == 0){
								setStateOfGame("lose");
								repaint();
							}
						}
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				if (winOrNot){
					AfterWinSaveName asm = new AfterWinSaveName();
					asm.setVisible(true);
					try {
						socket.sendUrgentData(0xFF);
						ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
						ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream());
						ArrayList<Object> tempForSend = new ArrayList<>();
						//After win, if user types nothing in the textField, the program will store
						//"Anonymous" as his name.
						if (asm.name.getText().trim().equals("")){
							tempForSend.add("Anonymous");
						}
						else{
							tempForSend.add(asm.name.getText());
						}
						tempForSend.add(Client.this.time);
						tempForSend.add("top");
						toServer.writeObject(tempForSend);
						toServer.flush();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;
				}
				if (flagForSleep){
					try {
						Thread.sleep(66666666);
					} catch (InterruptedException e) {
						flagForSleep = false;
					}
				}
			}
		}
	}

	public class DialogForSave extends JDialog {
		private JLabel hint;
		//A panel on top
		private JPanel topPanel;
		//A panel down
		private JPanel downPanel;
		//A textField for name
		private JTextField name;
		private JButton confirm;
		public DialogForSave() {
			this.giveElementContent();
			this.addElement();
		}
		private void giveElementContent(){
			hint = new JLabel("Please enter the name of user: ");
			hint.setFont(new java.awt.Font("Dialog", Font.BOLD, 10));
			hint.setForeground(Color.RED);
			name = new JTextField(10);
			confirm = new JButton("Confirm");
			confirm.setForeground(Color.BLUE);
			topPanel = new JPanel();
			downPanel = new JPanel();

			confirm.addActionListener(e->{
				this.dispose();
			});
		}
		private void addElement(){
			topPanel.add(hint);
			topPanel.add(name);
			downPanel.add(confirm);
			this.add(topPanel, BorderLayout.NORTH);
			this.add(downPanel, BorderLayout.SOUTH);
			this.setSize(500, 100);
			this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
			this.setUndecorated(true);
			this.setLocationRelativeTo(null);
		}
	}
	private void createList(ArrayList<Ties> tiesList) {
		for (int i = imgWidth; i < this.getWidth() - 2 * imgWidth; i += imgWidth) {
			for (int j = imgWidth; j < this.getHeight() - 6 * imgWidth; j += imgHeight) {
				rowCount = Math.max(rowCount, i / imgWidth);
				colCount = Math.max(colCount, j / imgWidth);
				this.tiesList = tiesList;
			}
		}
	}
	public class PanelOpen extends JDialog{
		private JLabel hint;
		private JTextField id;
		private JButton confirm;
		private JPanel topPanel;
		private JPanel downPanel;
		public PanelOpen() {
			this.giveContentToElement();
			this.addElement();
		}
		private void giveContentToElement(){
			hint = new JLabel("Enter the id of record(Must enter number): ");
			hint.setFont(new Font("Dialog", Font.BOLD, 10));
			hint.setForeground(Color.RED);
			id = new JTextField(10);
			confirm = new JButton("Confirm");
			confirm.setForeground(Color.BLUE);
			topPanel = new JPanel();
			downPanel = new JPanel();
			//Can only enter number in textfield
			id.addKeyListener(new KeyListener() {
				@Override
				public void keyTyped(KeyEvent e) {
					int keyChar=e.getKeyChar();
					if (keyChar>=KeyEvent.VK_0 && keyChar<=KeyEvent.VK_9) {
					} else {
						e.consume();
					}
				}

				@Override
				public void keyPressed(KeyEvent e) {

				}

				@Override
				public void keyReleased(KeyEvent e) {

				}
			});
			confirm.addActionListener(e->{
				this.dispose();
			});
		}
		private void addElement(){
			topPanel.add(hint);
			topPanel.add(id);
			downPanel.add(confirm);
			this.add(topPanel, BorderLayout.NORTH);
			this.add(downPanel, BorderLayout.SOUTH);
			this.setSize(500, 100);
			this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
			this.setUndecorated(true);
			this.setLocationRelativeTo(Client.this);
		}

	}
	public class AfterWinSaveName extends JDialog{
		private JLabel hint;
		private JTextField name;
		//Confirm save
		private JButton confirm;
		private JPanel topPanel;
		private JPanel downPanel;
		public AfterWinSaveName() {
			this.giveElementContent();
			this.addElement();
		}
		private void giveElementContent(){
			hint = new JLabel("Congrulations! Please enter your name: ");
			name = new JTextField(10);
			confirm = new JButton("Confirm");
			topPanel = new JPanel();
			downPanel = new JPanel();
			confirm.addActionListener(e->{
				this.dispose();
			});
		}
		private void addElement(){
			topPanel.add(hint);
			topPanel.add(name);
			downPanel.add(confirm);
			this.add(topPanel, BorderLayout.NORTH);
			this.add(downPanel, BorderLayout.SOUTH);
			this.setSize(500, 100);
			this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
			this.setUndecorated(true);
			this.setLocationRelativeTo(Client.this);
		}
	}
	public class ShowHistory extends JDialog{
		//Hint the user
		private JLabel hint;
		private JTextArea ta;
		private JButton ok;
		private JPanel topPanel;
		private JPanel downPanel;
		public ShowHistory() {
			hint = new JLabel("Top 5 is: ");
			ok = new JButton("Ok");
			ta = new JTextArea(13, 10);
			topPanel = new JPanel();
			downPanel = new JPanel();

			ok.addActionListener(e->{
				Client.this.thread.interrupt();
				this.dispose();
			});

			topPanel.add(hint);
			topPanel.add(ta);
			downPanel.add(ok);
			this.add(topPanel, BorderLayout.NORTH);
			this.add(downPanel, BorderLayout.SOUTH);
			this.setSize(400, 300);
			this.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
			this.setUndecorated(true);
			this.setLocationRelativeTo(null);
		}

	}
	public static void main(String[] args) {
		Client mineClient = new Client();
		mineClient.setVisible(true);
	}
}
