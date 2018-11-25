package es.uniovi.UO217138;

import javax.swing.JFrame;
import java.awt.BorderLayout;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JTree;
import javax.swing.JTabbedPane;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.RowSpec;

import sun.util.locale.StringTokenIterator;

import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Interface {
	private ChatIRC mainThread;
	private JFrame window;
	private JTree roomLists;
	public JTextArea txtServer;
	public HashMap<String, JPanel> room2Panel;
	public HashMap<String, JTextArea> room2TextArea;
	public HashMap<String, JTree> room2TreeUsers;
	private final JTabbedPane panelTab = new JTabbedPane(JTabbedPane.TOP);

	/**
	 *창을 만듭니다.
	 */
	public Interface(ChatIRC mainThread) {
		this.room2Panel = new HashMap<String, JPanel>();
		this.room2TextArea = new HashMap<String, JTextArea>();
		this.room2TreeUsers = new HashMap<String, JTree>();
		this.mainThread = mainThread;
		initialize();
	}

	/**
	 *프레임의 내용을 초기화하십시오.
	 */
	private void initialize() {
		// 처리하는 익명 클래스에서 사용할 최종 변수
		final UserIn userIn = this.mainThread.userIn;
		
		// 창 디자인
		window = new JFrame("ChatIRC - "+this.mainThread.server+":"+this.mainThread.port);
		window.setBounds(100, 100, 715, 537);
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.getContentPane().setLayout(new BorderLayout(0, 0));
		
		window.addWindowListener(new WindowAdapter() {                                                               
			 
			 
            //This is the function that will be invoked when the user clicks on the close button (the X)
            //At the end of this function , there will be no window on the screen , because we set in the 
            //previous line the frame's defautl close operation to DisposeOnClose               

            @Override                              
            public void windowClosing(WindowEvent e) {
                int confirmed = JOptionPane.showConfirmDialog(null,
                        "Are you sure you want to end the chat?", "Confirm", JOptionPane.YES_NO_OPTION);
                if (confirmed == JOptionPane.YES_OPTION) {
                    window.dispose();
                }
            }

            /*
            This is the function that is invoked when the window is closed (i.e , immediatly after the previous 
            function "windowClosing" exits). 
                            If the frame's default closing operation was "EXIT_ON_CLOSE" , this function wouldn't run.
            */
            @Override
            public void windowClosed(WindowEvent e) {
            	// 나머지 스레드의 실행을 마치려면 QUIT를 보냅니다.
                mainThread.userIn.sendQuit();
            }
        });
		
		// 탭이있는 중간 지역
		window.getContentPane().add(panelTab, BorderLayout.CENTER);
		
		// 서비스 정보 및 객실 목록이있는 서버 화면
		JPanel panelServer = new JPanel();
		panelTab.addTab("Log Servidor", null, panelServer, null);
		panelServer.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("default:grow"),
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("150px"),},
			new RowSpec[] {
				RowSpec.decode("default:grow"),}));
		
		//서버 로그 창에 대한 스크롤 창
		JScrollPane scrollServer = new JScrollPane();
		scrollServer.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollServer.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		panelServer.add(scrollServer, "1, 1, fill, fill");
		
		// 서버 로그 텍스트 영역
		this.txtServer = new JTextArea();
		txtServer.setLineWrap(true);
		//txtServer.setFont(new Font("Verdana", Font.PLAIN, 14));
		this.txtServer.setEditable(false);
		scrollServer.setViewportView(this.txtServer);
		
		// 객실 목록
		this.roomLists = new JTree();
		this.roomLists.setModel(new DefaultTreeModel(
			new DefaultMutableTreeNode("Salas") {
				{
				}
			}
		));
		this.roomLists.setRootVisible(false);
		// 방을 두 번 클릭하면 입력해야합니다.
		this.roomLists.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && !e.isConsumed()) {
					e.consume(); // 이벤트 지우기
					
					// 선택된 노드와 그 방을 가져옵니다.
					TreePath selPath = roomLists.getPathForLocation(e.getX(), e.getY());
				
					if (selPath != null) {
						DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)selPath.getLastPathComponent();
						final String roomName = (String) selectedNode.getUserObject();
						
						// 그 방에 JOIN 요청을 보냅니다.
						SwingUtilities.invokeLater(new Runnable() { 
							public void run() {
								userIn.sendJoin(roomName, "null");
							}
						});
					}
				}
			}
		});
		panelServer.add(this.roomLists, "3, 1, fill, fill");
		
		// 애플리케이션의 하단 패널
		JPanel panelInferior = new JPanel();
		window.getContentPane().add(panelInferior, BorderLayout.SOUTH);
		panelInferior.setLayout(new BorderLayout(5, 5));
		
		// 창의 가장자리에 붙어 있지 않도록 여백
		Border current = panelInferior.getBorder();
		Border empty = new EmptyBorder(0, 6, 6, 6);
		if (current == null) {
			panelInferior.setBorder(empty);
		} else {
			panelInferior.setBorder(new CompoundBorder(empty, current));
		}
	
		//스크롤이있는 명령 패널 작성
		final JComboBox comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"/MSG","/JOIN","/LEAVE","/NICK","/QUIT", 
				"/LIST","/WHO","/CALL", "/KICK", "/BAN", "/BAN_RELEASE","/BAN_LIST","/SET_PRIVACY", "/SUB_OPERATOR"
				, "/SUB_OP_LIST", "/DE_SUB_OP"}));
		panelInferior.add(comboBox, BorderLayout.WEST);

		// 텍스트 입력 필드 생성
		final JTextField msg = new JTextField();
		panelInferior.add(msg, BorderLayout.CENTER);
		msg.setColumns(10);
				
		// 버튼 생성 보내기
		final JButton btnSend = new JButton("send >");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//콤보 상자에 표시된 내용을 얻고 명령에 저장합니다.

				final String comando=(String)comboBox.getSelectedItem();
				// 작성된 텍스트도 저장됩니다.
				final String text = msg.getText();
				
				final String room = panelTab.getTitleAt(panelTab.getSelectedIndex());
				
				if (room.equals("Log Servidor") && (comando.toUpperCase().equals("/WHO") || comando.toUpperCase().equals("/MSG") 
						|| comando.toUpperCase().equals("/LEAVE") || comando.toUpperCase().equals("/KICK") || comando.toUpperCase().equals("/BAN") 
						|| comando.toUpperCase().equals("/BAN_RELEASE") || comando.toUpperCase().equals("/BAN_LIST")
						|| comando.toUpperCase().equals("/SET_PRIVACY") || comando.toUpperCase().equals("/SUB_OPERATOR"))){
					// / LEAVE, / WHO 및 / MSG 명령은 서버 로그에서 호출 할 수없는 경우 해당 서버가있는 방에 따라 다릅니다
					mainThread.serverLogPrintln("ERROR: This command is not available from the server console.");
					return;
				}
				
				// UserIn에게 요청 보내기
				SwingUtilities.invokeLater(new Runnable() { 
					public void run() {
						// 명령을 비교하여 원하는 것을 알 수 있습니다.
						if (comando.toUpperCase().equals("/NICK")) {
							if (text.length() > 0) {
								mainThread.userIn.sendNick(text);
							}
						}
						else if (comando.toUpperCase().equals("/LEAVE")) {
							mainThread.userIn.sendLeave(room);
						}
						else if (comando.toUpperCase().equals("/LIST")) {
							mainThread.userIn.sendList();
						}
						else if (comando.toUpperCase().equals("/WHO")) {
							mainThread.userIn.sendWho(room);
						}
						else if (comando.toUpperCase().equals("/CALL")) {
						mainThread.userIn.sendCall(text, room);
						}
						else if (comando.toUpperCase().equals("/JOIN")) {
							if (text.length() > 0) {
								StringTokenizer str = new StringTokenizer(text, " ");
								String password = null;
								String room = str.nextToken();
						
								if(str.countTokens() < 1) {
									mainThread.userIn.sendJoin(room, "null");
								}else {
									password = str.nextToken();
									System.out.println(room + ", " + password);
									mainThread.userIn.sendJoin(room, password);
								}
																
							}
						}
						else if (comando.toUpperCase().equals("/QUIT")) {
							int confirmed = JOptionPane.showConfirmDialog(null,
			                        "Are you sure you want to end the chat?", "Confirm", JOptionPane.YES_NO_OPTION);
			                if (confirmed == JOptionPane.YES_OPTION) {
			                    window.dispose();
			                }
						}						
						else if(comando.toUpperCase().equals("/MSG")){
							if (text.length() > 0) {
								mainThread.userIn.sendMessage(text, room);
							}
						}
						else if(comando.toUpperCase().equals("/KICK")){
							if (text.length() > 0) {
								mainThread.userIn.sendKick(text, room);
							}
						}
						else if(comando.toUpperCase().equals("/BAN")){
							if (text.length() > 0) {
								mainThread.userIn.sendBan(text, room);
							}
						}
						else if(comando.toUpperCase().equals("/BAN_RELEASE")){
							if (text.length() > 0) {
								mainThread.userIn.sendBanRelease(text, room);
							}
						}
						else if(comando.toUpperCase().equals("/BAN_LIST")){
							mainThread.userIn.sendBanList(room);							
						}
						else if(comando.toUpperCase().equals("/SET_PRIVACY")){
							if (text.length() > 0) {
								mainThread.userIn.sendSetPrivacy(text, room);
							}
							else {
								mainThread.userIn.sendSetPrivacy(room);
							}
						}
						else if(comando.toUpperCase().equals("/SUB_OPERATOR")){
							if (text.length() > 0) {
								mainThread.userIn.sendSubOperator(text, room);
							}					
						}
						else if(comando.toUpperCase().equals("/SUB_OP_LIST")){
							mainThread.userIn.sendSubOpList(room);									
						}
						else if(comando.toUpperCase().equals("/DE_SUB_OP")){
							if (text.length() > 0) {
								mainThread.userIn.sendDeSubOp(text, room);
							}					
						}
					}
				});
					
				//우리는 콤보 상자를 / MSG에 다시 넣고 쓰는 공간을 비워 둡니다
				comboBox.setSelectedIndex(0);
				msg.setText("");
			}
		});
		panelInferior.add(btnSend, BorderLayout.EAST);
		
		// 텍스트 필드에서 Enter 키를 눌러 기본 동작
		msg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//쓰기 입력란에 enter 키를 누르면 버튼으로 보내기를 시뮬레이션합니다.
				btnSend.doClick();
			}
		});
		
		// 창 표시

		this.window.setVisible(true);
	}

	public void createRoom(String room) {
		final String[] roomName = new String[]{room};
		final JPanel panelRoom = new JPanel();
		
		SwingUtilities.invokeLater(new Runnable() { 
			public void run() {
				// 새 룸의 패널 레이아웃 구성
				panelRoom.setLayout(new FormLayout(new ColumnSpec[] {
						ColumnSpec.decode("default:grow"),
						FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
						ColumnSpec.decode("150px"),},
						new RowSpec[] {
						RowSpec.decode("default:grow"),
				}));
		
				// 방의 JTextArea를위한 스크롤 판

				JScrollPane scrollRoom = new JScrollPane();
				scrollRoom.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
				scrollRoom.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				//방의 패널을 부착하십시오.
				panelRoom.add(scrollRoom, "1, 1, fill, fill");
		
				// 방의 텍스트 영역

				JTextArea txtRoom = new JTextArea();
				txtRoom.setLineWrap(true);
				txtRoom.setEditable(false);
				scrollRoom.setViewportView(txtRoom);
		
				// 회의실 사용자 목록
				JTree roomUsers = new JTree();
				roomUsers.setModel(new DefaultTreeModel(
					new DefaultMutableTreeNode("Usuarios") {
						{
						}
					}
				));
				roomUsers.setRootVisible(false);
				// 실내 패널에 부착
				panelRoom.add(roomUsers, "3, 1, fill, fill");
				
				// 공유 HashMaps에 대한 참조 추가
				room2Panel.put(roomName[0], panelRoom);
				room2TextArea.put(roomName[0], txtRoom);
				room2TreeUsers.put(roomName[0], roomUsers);
		
				// 패널이 GUI에 추가됩니다.
				panelTab.addTab(roomName[0], null, panelRoom, null);
				
				// 공유 HashMaps 일치에 대한 참조 추가
				mainThread.mainWindow.print2Room(roomName[0], "INFO: You entered the room. "+roomName[0]);
				mainThread.serverLogPrintln("INFO: You entered the room. "+roomName[0]);
				
				// 방의 사용자를 보려면 WHO 메시지를 보냅니다.
				mainThread.userIn.sendWho(roomName[0]);
			}
		});
	}
	
	public void removeRoom(String room) {
		final String[] roomName = new String[]{room};
		// 공유 HashMaps 일치에 대한 참조 추가
		
		SwingUtilities.invokeLater(new Runnable() { 
			public void run() {
				JPanel panel = room2Panel.get(roomName[0]);
				// UI 패널을 제거하고 요소를 제거하십시오.
				panelTab.remove(panel);
				
				room2Panel.remove(roomName[0]);
				room2TextArea.remove(roomName[0]);
				room2TreeUsers.remove(roomName[0]);
			}
		});
	}
	
	public void print2Room(String room, String text) {
		final String[] args = new String[]{room, text};
		
		if (room2TextArea.get(room) != null) {
			SwingUtilities.invokeLater(new Runnable() { 
				public void run() {
					// 방이 나중에 수정 / 생성 할 수있는 공유 객체에 있으므로 방이로드됩니다.
					JTextArea txtRoom = room2TextArea.get(args[0]);
					txtRoom.append(args[1]+"\n");
				}
			});
		} else {
			this.mainThread.serverLogPrintln("ERROR: Try writing in a non-existent room.");
		}
	}
	
	public void updateRoomList(String[] rooms) {
		final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Rooms");
		
		for (int i = 0; i < rooms.length; i++) {
			if (rooms[i].length()>0)
				rootNode.add(new DefaultMutableTreeNode(rooms[i]));
		}
		
		SwingUtilities.invokeLater(new Runnable() { 
			public void run() {
				roomLists.setModel(new DefaultTreeModel(rootNode));
			}
		});
	}
	
	public void setUsersRoom(String room, ArrayList<String> users) {
		final DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Users");
		final String[] arg = new String[]{room};
		Object[] usersArray = users.toArray();
		
		for (int i = 0; i < usersArray.length; i++) {
			if (((String)usersArray[i]).length() > 0) {
				rootNode.add(new DefaultMutableTreeNode((String)usersArray[i]));
			}
		}
		
		SwingUtilities.invokeLater(new Runnable() { 
			public void run() {
				JTree usersRoom = room2TreeUsers.get(arg[0]);
				usersRoom.setModel(new DefaultTreeModel(rootNode));
			}
		});
	}
	
	public void closeWindow() {
		SwingUtilities.invokeLater(new Runnable() { 
			public void run() {
				window.setVisible(false);
				window.dispose();
			}
		});
	}
}
