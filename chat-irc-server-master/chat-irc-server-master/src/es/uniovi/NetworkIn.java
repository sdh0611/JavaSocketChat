package es.uniovi;
import java.net.Socket;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * 각 사용자 소켓을 독립적으로 청취하고 입력 버퍼에 메시지를 추가 할 수 있도록 메시지를 내부 형식으로 변환하는 클래스.
 * 유저당 하나씩 생성됩니다.
 */
public class NetworkIn extends Thread {
	private User user;
	private DataInputStream inputStream;
	private Socket socket;
	private GlobalObject global;
	private Boolean threadRunning;	// 스레드가 필요할 때 독립적으로 닫히도록하는 변수

	
	/**
	 * 자체 실행 NetworkIn 클래스의 생성자
	 * @param user
	 * @param Global
	 */
	public NetworkIn(User user, GlobalObject global) {
		// 클래스 변수 지정
		this.user = user;
		this.global = global;
		this.threadRunning = true;
		
		
		// 	소켓 및 InputStream을 가져옴.
		try {
			this.socket = this.user.getSocket();
			
			if (this.socket.isClosed()==true || this.socket.isConnected() == false) {
				System.err.println("ERROR: You have created a listener for a socket that is not open.");
			} else {
				this.inputStream = new DataInputStream(this.socket.getInputStream());
			}
		} catch (IOException e) {
			System.err.println("ERROR: While Creating listener "+this.user.getCompleteInfo());
		}
		
		this.start();
	}
	
	public void run() {
		Message msg;
		System.out.println("INFO: Created listener for the client "+this.user.getCompleteInfo());
		
		/*
		 * NOTE(18.06.01, 심대현):
		 * 해당 루틴의 역할은 메시지를 해당 user의 inputStream으로 부터 읽어온 후, 
		 * 메시지의 유효성을 확인한 후에 GlobalObject의 입력버퍼큐로 메시지를 넣습니다.
		 */
		while(this.global.isRunning() && this.threadRunning) {
			//NOTE(18.06.17, 심대현) : 의미가 없는 것 같아서 일단 주석처리했습니다.
			//msg = new Message();
			
			try {
				//Message객체를 가져옵니다.
				msg = readMessage();
			} catch (IOException e) {
				// 사용자가 연결되어 있고 오류가 발생하면 오류를 표시하고 연결을 종료합니다.
				if (this.user.getConnected()){
					// 소켓이 닫힌경우 사용자 데이터를 삭제하고 스레드의 실행을 취소합니다.
					System.out.println("INFO: The connection suddenly disconnected. "+this.user.getCompleteInfo()+". It is ready to eliminate it from the system.");
					//	QUIT 메시지가 사용자로부터 수신되었음을 시뮬레이션합니다.
					global.simulateQUIT(this.user);
				}				
				// 어떤 경우이든 연결 여부와 관계없이 네트워크 스레드를 닫아야함								
				this.threadRunning = false;	// 쓰레드 실행여부 변경.				
				
				return;		// 스레드 종료
			}
			
			//  읽은 메시지가 완전하고 유효한지 확인하십시오.
			if (msg.isValid()) {
				// 디버그가 활성화되어 있으면 정보를 표시합니다
				if (this.global.getDebug()) {
					System.out.println("DEBUG: Trace of the message received from "+this.user.getCompleteInfo()+" :");
					msg.showInfo();
				}
				
				try {
					// 입력 버퍼에 메시지를 넣음.
					global.getBufferInput().put(msg);
				} catch (InterruptedException e) {
					System.err.println("ERROR: Error entering the message: ");
					msg.showInfo(); // 오류 정보 표시
					e.printStackTrace();
				}
			} else {	// 허용되지 않은 메시지인 경우
				System.err.println("ERROR: Received invalid message from the user "+this.user.getCompleteInfo());
				msg.showInfo();
			}
		}
	}
	
	private byte[] readByteArray(int size) throws IOException {
		byte[] outPut = new byte[size];
		
		for(int n = 0; n < size; n++) {
			outPut[n]=this.inputStream.readByte();
		}
		
		return outPut;
	}
	
	private Message readMessage() throws IOException {
		Message msg;		// 만들 객체
		short sizeLoad;		// 하중 크기
		short numArgs;		// 인수의 수
		short sizeArg;		// 각 인수의 크기
		byte[] argBytes;	// 각 인수의 바이트 배열
		String[] args;		// 이미 변환 된 인수의 배열
		
		msg = new Message(); // 개체 만들기
		
		// 패킷 유형 및 메시지 유형 읽기
		msg.setPacket(this.inputStream.readByte());
		msg.setType(this.inputStream.readByte());
		
		// 메시지 크기를 읽습니다.
		sizeLoad = this.inputStream.readShort();
		
		if (sizeLoad > 0) { 
			//로드가 있으면 매개 변수의 수를 읽습니다
			numArgs = this.inputStream.readShort();
			args = new String[numArgs];
			
			// 접수 된 인수 처리
			for(int n=0; n<numArgs; n++) {
				// 인수의 크기 (바이트)
				sizeArg = this.inputStream.readShort();
				
				if (sizeArg > 0){
					// 인수를 읽고 변환하십시오..
					argBytes = readByteArray((int)sizeArg);
					args[n] = new String(argBytes, "UTF-8");
				}
				else {
					args[n] = new String();
				}
			}
		}
		else {
			args = new String[0];
		}
		
		// 인수를 저장하고 사용자 추가
		msg.setArgs(args);
		msg.setUser(this.user);
		
		return msg;
	}
}
