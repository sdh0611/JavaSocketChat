
package es.uniovi.UO217138;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/*
 * Class ChatIRC
 * 이 클래스는 클라이언트의 기본 클래스로 사용되며 초기 화면과
 * 접속 데이터에 관한 사용자의 응답을 기다린다.
 */
public class ChatIRC extends Thread {
	public final static String version = "1.0";
	
	public String server;
	public Integer port;
	public Boolean DEBUG = false;
	public Boolean execution = true;
	
	public String nick = new String();
	
	public Interface mainWindow;
	public final ChatIRC mainObject;
	public HashMap<String, ArrayList<String>> room2Users;
	
	// 액세스 가능한 메시지 I / O 스레드
	public UserOut userOut;
	public UserIn userIn;
	
	//네트워크 스레드 접근 불가
	private NetworkOut netOut;
	private NetworkIn netIn;
	
	private Socket socket; 
	
	//메시지 버퍼. Private

	private BufferFifo bufferResponses;
	private BufferFifo bufferCommands; 
	
	public static void main(String[] args) {
		new ChatIRC();
		return;
	}
	
	public ChatIRC() {
		//시작 창에 있는 이벤트에서 액세스 할 수 있또록 
		//최종  개체에 현재 개체 지정
		this.mainObject = this;
		
		// 환영 창 표시
		createWelcomeScreen();
	}
	
	/*
	 *응용 프로그램 시작의 집행자.
	 *시작 윈도우에서 주 스레드를 시작할 때 호출됨.
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		// 중간 버퍼 만들기
		this.bufferResponses = new BufferFifo(); // Buffer queue : 네트워크에서 온 응답을 저장하는 버퍼
		this.bufferCommands = new BufferFifo(); // Buffer queue : 사용자 명령을 저장하는 버퍼
				
		// 메시지 I / O 스레드 만들기
		// GUI에서 액세스 가능
		this.userOut = new UserOut(this.bufferResponses, this);
		this.userIn = new UserIn(this.bufferCommands, this);
		
		this.userIn.setDaemon(true);
		this.userOut.setDaemon(true);
		
		// room당 user를 관리하는 Hashmap 멤버
		this.room2Users = new HashMap<String, ArrayList<String>>();
		//메인GUI
		this.mainWindow = new Interface(this);
		
		serverLogPrintln("INFO: RUN ChatIRC v"+ChatIRC.version);
		serverLogPrintln("");
		
		// 네트워크 인터페이스 생성 및 스레드 처리
		try {
			serverLogPrint("INFO: Connect to a "+this.server+":"+this.port+"...");
			socket = new Socket(this.server, this.port);
			
			this.netOut = new NetworkOut(this.bufferCommands, socket, this);
			this.netIn = new NetworkIn(this.bufferResponses, socket, this);
			
			this.netOut.setDaemon(true);

			// 스레드 시작
			this.netIn.start();
			this.netOut.start();
			
			// HELLO 메시지 캡쳐용
			Message msgHello = new Message();
			
			try {
				msgHello = this.bufferResponses.get();
			} catch(InterruptedException e){
				serverLogPrint("Error! ");
				serverLogPrintln("Check the console for more information about the console.");
				e.printStackTrace();
			}
			
			if (msgHello.getPacket() == Message.PKT_OK && msgHello.getType() == Message.TYPE_HELLO) {
				serverLogPrintln("SUCESS!"); // 올바른 연결

				
				//필요한 나머지 스레드를 시작하십시오.
				this.userOut.start();
				this.userIn.start();
				
				serverLogPrintln("SERVER: "+msgHello.getArgs()[0]);
				
				// 사용자 이름 (닉네임)의 초기 설정
				serverLogPrintln("INFO: You are about to change your nickname. '"+this.nick+"'.");
				
				Message msgNick = new Message();
				msgNick.setPacket(Message.PKT_CMD);
				msgNick.setType(Message.TYPE_NICK);
				msgNick.setArgs(new String[]{this.nick});
				
				// 사용 가능한 객실에 대한 정보 요청
				serverLogPrintln("INFO: Requesting info about available rooms.");
				
				Message msgList = new Message();
				msgList.setPacket(Message.PKT_CMD);
				msgList.setType(Message.TYPE_LIST);
				msgList.setArgs(new String[]{});
				
				try {
					// 메시지를 명령 버퍼에 넣습니다.
					this.bufferCommands.put(msgNick);
					this.bufferCommands.put(msgList);
				} catch(InterruptedException e) {
					serverLogPrintln("ERROR");
					serverLogPrintln("Check the console for more information about it.");
					e.printStackTrace();
				}
			}
			else {
				serverLogPrintln("ERROR:The expected HELLO command was not received.");
			}
		} catch (IOException e) {
			serverLogPrintln("\nERROR: Error creating the socket: "+e.getMessage());
			serverLogPrintln("Check the console for more information about it.");
			e.printStackTrace();
		}
	}
	
	/*
	 * 실행 중 스레드를 닫고 프로세스를 종료하는 기능
	 */
	public void closeThreads() {
		// 사이클을 끝내는 스레드를 중지하십시오.
		synchronized (this.execution){
			this.execution = false;
		}
		
		this.netIn.interrupt();
		this.netOut.interrupt();
		this.userIn.interrupt();
		this.userOut.interrupt();
		
		// 소켓이 닫힙니다.
		try {
			this.socket.close();
		} catch (IOException e) {
			System.err.println("Error closing the socket.");
			e.printStackTrace();
		}
		
		// End of execution :)
	}
	
	/*
	 * Funcion para 서버 콘솔에서 텍스트를 인쇄하십시오.
	 */
	public void serverLogPrint(String text) {
		final JTextArea txtServerLog = this.mainWindow.txtServer;
		final String[] textFinal = new String[1];
		
		textFinal[0] = text;
		
		SwingUtilities.invokeLater(new Runnable() { 
			public void run() {
				txtServerLog.append(textFinal[0]);
			}
		});
	}
	
	/*
	 * 서버 콘솔에서 텍스트를 인쇄하는 기능
	 * 라인의 점프로 끝남
	 */
	public void serverLogPrintln(String text) {
		final JTextArea txtServerLog = this.mainWindow.txtServer;
		final String[] textFinal = new String[1];
		
		textFinal[0] = text+"\n";
		
		SwingUtilities.invokeLater(new Runnable() { 
			public void run() {
				txtServerLog.append(textFinal[0]);
			}
		});
	}

	/*
	 * 이 함수는 연결에 대한 데이터가 요청되고 연결을 위해 수집되는 입력창을 작성
	 * 해당 데이터가 있으면 기본 스레드가 시작
	 */
	private void createWelcomeScreen() {
		// 작업에서 액세스 할 개체 끝내기
		final JFrame welcomeScreen;
		final JTextField txtServer;
		final JTextField txtNick;
		final JSpinner slcPort;
		JLabel lblServidor;
		JLabel lblNick;
		final JButton btnConnect;
		JButton btnAbout;
		JButton btnExit;
		
		// 기본 창 만들기
		welcomeScreen = new JFrame("ChatIRC version "+ChatIRC.version);
		welcomeScreen.setResizable(false);
		welcomeScreen.setSize(new Dimension(398, 187));
		welcomeScreen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		welcomeScreen.getContentPane().setLayout(null);
		
		// 제목 태그
		JLabel lblChatircVersion = new JLabel("ChatIRC version "+ChatIRC.version);
		lblChatircVersion.setFont(new Font("Verdana", Font.BOLD, 16));
		lblChatircVersion.setHorizontalAlignment(SwingConstants.CENTER);
		lblChatircVersion.setBounds(12, 12, 372, 25);
		welcomeScreen.getContentPane().add(lblChatircVersion);
		
		// 서버 필드 레이블 (주소 및 포트)
		lblServidor = new JLabel("Server");
		lblServidor.setHorizontalAlignment(SwingConstants.RIGHT);
		lblServidor.setFont(new Font("Verdana", Font.PLAIN, 14));
		lblServidor.setBounds(12, 53, 70, 15);
		welcomeScreen.getContentPane().add(lblServidor);
		
		// 닉네임 설정
		lblNick = new JLabel("Nick");
		lblNick.setHorizontalAlignment(SwingConstants.RIGHT);
		lblServidor.setFont(new Font("Verdana", Font.PLAIN, 14));
		lblNick.setBounds(12, 88, 70, 15);
		welcomeScreen.getContentPane().add(lblNick);
		
		// 서버의 텍스트 필드
		txtServer = new JTextField();
		txtServer.setText("chat.oscardearriba.com");
		txtServer.setColumns(10);
		txtServer.setBounds(100, 49, 203, 25);
		welcomeScreen.getContentPane().add(txtServer);
		
		// 포트 선택
		slcPort = new JSpinner();
		slcPort.setModel(new SpinnerNumberModel(7777, 1, 65535, 1));
		slcPort.setBounds(304, 49, 70, 25);
		welcomeScreen.getContentPane().add(slcPort);
		
		// 닉의 텍스트 필드
		txtNick = new JTextField();
		txtNick.setText("User");
		txtNick.setColumns(10);
		txtNick.setBounds(100, 83, 274, 25);
		
		welcomeScreen.getContentPane().add(txtNick);
		
		// 연결 버튼
		btnConnect = new JButton("Connect to >");
		btnConnect.setBounds(267, 120, 117, 25);
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 데이터 확인하기
				if (txtServer.getText().length() == 0){
					JOptionPane.showMessageDialog(welcomeScreen, "You must enter the server to connect to.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				} else if (txtNick.getText().length() == 0){
					JOptionPane.showMessageDialog(welcomeScreen, "A nick must be entered to connect", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				
				// 정보를 수집하고 필요한 객체에 수정 한 후 나머지 프로그램을 시작
				mainObject.server = txtServer.getText();
				mainObject.port = (Integer)slcPort.getValue();
				mainObject.nick = txtNick.getText();
				
				// 창 닫기
				welcomeScreen.setVisible(false);
				welcomeScreen.dispose();
				
				mainObject.start();
			}
		});
		welcomeScreen.getContentPane().add(btnConnect);
		
		// 앱에 대한 정보 버튼
		btnAbout = new JButton("Information");
		btnAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 매우 중요한 정보 표시
				JOptionPane.showMessageDialog(welcomeScreen, "2018/Kim/Kim/Sim/Lee.\n\n");
			}
		});
		btnAbout.setBounds(141, 120, 117, 25);
		welcomeScreen.getContentPane().add(btnAbout);
		
		// 나가기 버튼
		btnExit = new JButton("Exit");
		btnExit.setBounds(12, 120, 117, 25);
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// 버튼을 누를 때 창을 닫으므로 더 이상 실행중인 스레드가 없으므로 실행을 종료합니다.
				welcomeScreen.setVisible(false);
				welcomeScreen.dispose();
			}
		});
		welcomeScreen.getContentPane().add(btnExit);
		
		// 일부 입력란에 Enter 키를 누른 경우 데이터를 보내보십시오.
		txtServer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Enter 키를 누르고 연결 버튼을 클릭하여 연결을 계속 진행합니다.
				btnConnect.doClick();
			}
		});
		txtNick.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Enter 키를 누르고 연결 버튼을 클릭하여 연결을 계속 진행합니다.
				btnConnect.doClick();
			}
		});
		
		// 마지막으로 창을 표시하십시오.
		welcomeScreen.setVisible(true);
	}
}
