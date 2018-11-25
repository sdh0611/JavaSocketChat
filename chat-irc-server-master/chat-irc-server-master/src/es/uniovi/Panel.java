package es.uniovi;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.WindowConstants;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.Font;


/**
 * JTree의 간단한 사용 예제 클래스
 *
 *
 */
public class Panel
{
	private JTextField textField;
	private DefaultMutableTreeNode main;
	private DefaultTreeModel model;
	private HashMap<String, RoomNode> roomNodes;
	JTree tree;
	/**
	 * JTree를 사용하는 간단한 예제
	 *
	 * @param args 명령 행 인수, 무시됩니다.
	 * @wbp.parser.entryPoint
	 */
	public  void build()
	{
		// Tree construction
		roomNodes=new HashMap<String, RoomNode>();
		main = new DefaultMutableTreeNode("Rooms"); // 
		model = new DefaultTreeModel(main);
		tree = new JTree(model); // model=model
		//컴파일하는 곳을 확인합니다.
		String path = null;
		try {
			path = new java.io.File(".").getCanonicalPath();//우리는 현재 경로를 얻는다.
		} catch (IOException e) {
			System.err.println("현재 경로를 가져 오는 중 오류가 발생했습니다.");
			e.printStackTrace();
		}
		//우리가 src 안에 있다면
		if(path.endsWith("src"))
			path="./";
		//우리가 src 안에 있지 않으면
		else
			path="./src/";

		DefaultTreeCellRenderer render = new DefaultTreeCellRenderer();
		render.setLeafIcon(new ImageIcon(path+"images/user.png"));
		render.setOpenIcon(new ImageIcon(path+"images/room.png"));
		render.setClosedIcon(new ImageIcon(path+"images/room.png"));
		tree.setCellRenderer(render);
		tree.setRootVisible(false);
		// 창 구성 및 시각화
		JFrame v = new JFrame();
		v.getContentPane().setLayout(new BorderLayout(0, 0));

		JLabel lblTitle = new JLabel("객실 및 사용자:"); 
		lblTitle.setFont(new Font("Dialog", Font.BOLD, 20));
		v.getContentPane().add(lblTitle, BorderLayout.NORTH);
		JScrollPane scroll = new JScrollPane(tree);
		v.getContentPane().add(scroll, BorderLayout.CENTER);

		v.setSize(new Dimension(250, 600));
		v.setVisible(true);
		v.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	/**
	 * tree에 새 방을 입력하십시오.
	 * @param room 새 방 이름
	 */
	public synchronized void newRoom(String room){

		DefaultMutableTreeNode root1 = new DefaultMutableTreeNode(room);
		roomNodes.put(room, new RoomNode(root1));
		model.insertNodeInto(root1, main, 0);

		showTree();
	}
	/**
	 * 지정된 방에 새 사용자를 입력하십시오.
	 * @param room 입장 할 방 이름
	 * 사용자
	 * @param user 입력 할 사용자 이름
	 */
	public  synchronized void newUser(String room,String user){
		roomNodes.get(room).setUser(user);
		roomNodes.get(room).getRoom().add(roomNodes.get(room).getUser(user));
		model.reload(main);

		showTree();
	}
	/**
	 * TREE에서 방을 꺼내십시오.
	 * @param room 우리가 제거하고자하는 방
	 */

	public  synchronized void delRoom(String room){

		model.removeNodeFromParent(roomNodes.get(room).getRoom());
		roomNodes.remove(room);
		model.reload(main);

		showTree();
	}
	/**
	 *모든 방에서 사용자 삭제
	 * 비어있는 것이 있으면 제거하십시오.
	 * @param user 제거 할 사용자의 이름
	 */
	public  synchronized void delUser(String user){
		for (String key: roomNodes.keySet()){
			delUser(key, user);
		}

		showTree();

	}

	/**
	 * 표시된 방에서 사용자를 제거하십시오. 방이 비어 있으면 제거하십시오.
	 * @param  room 어느 방의 이름
	 * 사용자 제거
	 * @param  user 삭제할 사용자의 이름
	 */
	public  synchronized void delUser(String room, String user){
		if(roomNodes.get(room).isUser(user)){
			roomNodes.get(room).getRoom().remove(roomNodes.get(room).getUser(user));
			model.reload(main);
			roomNodes.get(room).delUser(user);

		}
		if(roomNodes.get(room).isEmpty())
			delRoom(room);

		showTree();
	}
	/**
	 * 방의 존재를 확인합니다.
	 * @param room 점검 할 방의 이름
	 * @return  존재하는 경우는 true를 돌려 주어, 그렇지 않은 경우는 false를 돌려줍니다.
	 */
	public synchronized boolean isRoom(String room){
		return roomNodes.containsKey(room);
	}

	/**
	 * 트리를 자동으로 확장하십시오.
	 */

	public void showTree(){

		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
	}

}