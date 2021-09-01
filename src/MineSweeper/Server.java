package MineSweeper;

import javax.swing.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;

public class Server extends JFrame implements Runnable, Serializable {
    private JTextArea ta;
    public Server() {
        ta = new JTextArea(10,10);
        JScrollPane sp = new JScrollPane(ta);
        this.add(sp);
        this.setTitle("MineServer");
        this.setSize(400,200);
        Thread t = new Thread(this);
        t.start();
    }
    @Override
    public void run() {
        try {
            ServerSocket ss = new ServerSocket(8000);
            ta.append("Server started at "
                    + new Date() + '\n');
            while(true){
                Socket socket = ss.accept();
                ta.append("New Game!\n");
                new Thread(new CreateThreadForClient(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    class CreateThreadForClient implements Runnable{
        private Socket socket;
        private Connection conn;
        private PreparedStatement selectLatestId, selectForOpen, selectForRank;
        private PreparedStatement insertForMines, insertForRank;
        private ResultSet rs;
        public CreateThreadForClient(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            try{
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection("jdbc:sqlite:data.db");
                String sqlForMines = "INSERT INTO Bomb VALUES (NULL, ?);";
                String sqlForRankInsert = "INSERT INTO User VALUES(NULL, ?);";
                String sqlForMaxId = "SELECT MAX(id) AS id FROM Bomb";
                String sqlForOpen = "SELECT status FROM Bomb WHERE id = ?;";
                String sqlForRankSelect = "SELECT info FROM User;";
                while (true){
                    ObjectInputStream fromClient = new ObjectInputStream(socket.getInputStream());
                    ObjectOutputStream toClient = new ObjectOutputStream(socket.getOutputStream());
                    insertForMines = conn.prepareStatement(sqlForMines);
                    insertForRank = conn.prepareStatement(sqlForRankInsert);
                    selectLatestId = conn.prepareStatement(sqlForMaxId);
                    selectForOpen = conn.prepareStatement(sqlForOpen);
                    selectForRank = conn.prepareStatement(sqlForRankSelect);
                    //insert into database
                    ArrayList<Object> temp = (ArrayList<Object>)fromClient.readObject();
                    if (temp.size() == 9 && temp.get(8).equals("save")){
                        temp.remove(8);
                        Object o = (Object)temp;
                        insertForMines.setBytes(1, Client.transferToByte(o));
                        insertForMines.executeUpdate();
                        ta.append("Object received from client\n");
                        rs = selectLatestId.executeQuery();
                        while (rs.next()){
                            Integer id = rs.getInt(1);
                            toClient.writeObject(id);
                            toClient.flush();
                        }
                        rs.close();
                        insertForMines.close();
                    }
                    else if(temp.size() == 2 && temp.get(1).equals("Id for select")){
                        temp.remove(1);
                        selectForOpen.setInt(1, (int)temp.get(0));
                        rs = selectForOpen.executeQuery();
                        if(rs.next()){
                            byte[] buffer = rs.getBytes(1);
                            ArrayList<Object> status = Client.transferToObject(buffer);
                            toClient.writeObject(status);
                        }
                        else{
                            toClient.writeObject("Nothing to show");
                        }
                        toClient.flush();
                        rs.close();
                        selectForOpen.close();
                    }
                    //rank
                    else if(temp.size() == 3 && temp.get(2).equals("top")) {
                        temp.remove(2);
                        insertForRank.setBytes(1, Client.transferToByte((Object) temp));
                        insertForRank.executeUpdate();

                        ta.append("Object received from client\n");
                        insertForRank.close();
                    }
                    else if(temp.size() == 1 && temp.get(0).equals("Open rank list")){
                        rs = selectForRank.executeQuery();
                        ArrayList<Object> resList = new ArrayList<>();
                        while(rs.next()){
                            byte[] buffer = rs.getBytes(1);
                            ArrayList<Object> status = Client.transferToObject(buffer);
                            resList.add(status);
                        }
                        toClient.writeObject(resList);
                        toClient.flush();
                        rs.close();
                        selectForRank.close();
                    }
                }
            } catch ( SQLException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException ignored){

            }

        }
    }
    public static void main(String[] args){
        Server ms = new Server();
        ms.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ms.setVisible(true);
    }
}
