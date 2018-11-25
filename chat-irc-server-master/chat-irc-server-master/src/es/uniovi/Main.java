package es.uniovi;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main extends Thread {
	public Integer port;
	public Boolean debug;
	public boolean panel;
	private GlobalObject global;
	private ServerSocket socketPrincipal;
	private NetworkOut netOut;
	private Processing process;
	
	/**
	 * Main 클래스의 생성자. 서버를 시작하는 데 필요한 모든 객체가 만들어집니다.
	 * 또한 다른 스레드가 생성되지만 아직 풀어 놓지는 않습니다.
	 * @param integer Portnum
	 * @param debug 디버그 모드가 활성화되었는지 여부를 나타내는 부울입니다.
	 * @param panel 
	 */
	public Main(Integer port, Boolean debug,Boolean panel) {
		
		this.port = port;
		this.debug = debug;
		this.global = new GlobalObject();
		
		this.global.setDebug(this.debug);
		//기본적으로 인터페이스가 없습니다.
		this.panel=panel;
		this.global.setHasPanel(panel);
		
		// 필요한 스레드를 시작하십시오.
		
		this.netOut = new NetworkOut(this.global);
		this.process = new Processing(this.global);
		// TODO:여기서 처리 스레드의 객체가 생성됩니다.
	}
	
	public void run() {

		Socket socketAccepted;		
		Message msgWelcome;
		Integer usersAccepted=0;
		
		// 	컴파일 버전 및 날짜가 포함 된 헤더 메시지
		System.out.println("ChatIRC Server version."+this.global.getVersion()+" ("+this.global.getCompilationdate()+")");
		System.out.println("--------------------------------------");
		
		if (this.global.getDebug()) { 
			System.out.println("INFO: Server Mode DEBUG.");
		}
		
		
		System.out.println("INFO: Server Port Number : "+this.port+"...");
		
		try {
			socketPrincipal = new ServerSocket(this.port);
		} catch (IOException e) {
			// 	오류가 발생할 경우 관리자에게 알리고 실행을 종료하십시오.
			System.err.println("ERROR: 포트에서 소켓 열기를 시도하는 중 오류가 발생했습니다. : Port("+this.port+")");
			e.printStackTrace();
			return;
		}
		
		// 	응용 프로그램의 다른 초기 스레드를 시작
		this.netOut.start();
		this.process.start();
		
		//panel 우리는 지시가있는 경우에만 인터페이스를 사용합니다.
		if(this.global.getHasPanel()) {
			global.getPanel().build();
		}
		
		/*
		 * NOTE(18.06.01, 심대현):
		 * 해당 루틴의 역할은 새로 들어온 클라이언트를 GlobalObject가 관리하고 있는
		 * User관리용 해시맵에 정보를 추가하고, 환영메시지를 출력하는 것. 
		 */
		while(this.global.isRunning()) {
			try {
				socketAccepted = socketPrincipal.accept();
				usersAccepted++;
				
				// 사용자 생성 및 등록
				User user = new User("익명 "+usersAccepted, socketAccepted);
				this.global.addUser(user);
				
				/*
				 * 특정 사용자의 읽기 스레드 생성
				 * 생성됨과 동시에 해당 스레드가 바로 시작됩니다.
				 */				
				new NetworkIn(user, this.global);
				
				System.out.println("INFO: 사용자에게 보낸 환영 메시지 :"+user.getCompleteInfo());
				
				// 	환영 메시지 작성
				msgWelcome = new Message();
				msgWelcome.setType(Message.TYPE_HELLO);
				msgWelcome.setPacket(Message.PKT_OK);
				msgWelcome.setArgs(new String[]{"Welcome to ChatIRC Server version."+this.global.getVersion()+" ("+this.global.getCompilationdate()+")"});
				msgWelcome.setUser(user);
				
				try {
					//Global 출력버퍼에 환영 메시지를 넣음.
					global.getBufferOutput().put(msgWelcome);
				} catch(InterruptedException e) {
					System.err.println("ERROR: 환영 메시지를 보내는 중에 오류가 발생했습니다. "+user.getCompleteInfo());
					e.printStackTrace();
				}
				
			} catch (IOException e) {
				// 	오류가 발생할 경우 관리자에게 알리고 루프를 다시 실행하십시오.
				System.err.println("ERROR: 주 소켓에서 요청을 수락하지 못했습니다.");
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Integer port;
		Boolean debug = false;
		Boolean panel=false;
		Main main;
		
		// 	매개 변수가 충분하지 않거나 도움말이 요청되면 메시지를 표시하십시오.
		if (args.length < 1 || args[0].equals("-h") || args[0].equals("--help")) {
			System.err.println("Usage : <puerto> [-d | --debug] [-p | --panel]");
			return;
		}
		
		// 	네트워크 포트 읽기
		port = new Integer(args[0]);
		
		//포트가 지원되는 값 사이에 있는지 확인하십시오.
		if (port < 1 || port > 65535) {
			System.err.println("Error: Out of Port Range 0 y 65535");
			return;
		}
		
		// 	디버그 또는 패널 모드가 요청 된 경우 플래그를 활성화하십시오.
		if (args.length > 1) {
			for(int i=1;i<args.length;i++){
				if((args[i].equals("-d") || args[i].equals("--debug")))
					debug = true;
				else if((args[i].equals("-p") || args[i].equals("--panel")))
					panel = true;
			}
		}
		
		// M주 스레드 초기화
		main = new Main(port, debug, panel);
		main.start();
		
		return;
	}

}
