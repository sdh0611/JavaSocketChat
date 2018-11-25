
package es.uniovi.UO217138;
import java.net.Socket;
import java.io.IOException;

/*
 * Class NetworkOut
 * 
 * 커멘드 · 버퍼의 데이터를 처리하는 클래스
 * 메시지 형식으로 표시되므로 네트워크로 보낼 수 있습니다.
 */
public class NetworkOut extends Thread {
	private ChatIRC netParent;
	private BufferFifo bufferCommands;
	private Socket socket;
	private BinaryProtocolConverter protocolConverter;

	/*
	 * NetworkOut 클래스 생성자
	 */
	public NetworkOut(BufferFifo bufferCommands, Socket netInterface, ChatIRC principal) {
		this.netParent = principal;
		this.bufferCommands = bufferCommands;
		this.socket = netInterface;
		
		try {
			this.protocolConverter = new BinaryProtocolConverter(this.socket.getOutputStream());
		} catch(IOException e) {
			System.err.println("출력 스트림을 가져 오는 중에 오류가 발생했습니다 : "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	/*
	 * 스레드로서의 연속 실행 방법
	 */
	public void run() {
		Message outputMsg;
		
		// 버퍼에서 메시지를 가져 와서 네트워크를 통해 꺼내는 무한 루프 실행.
		while (this.netParent.execution) {
			outputMsg = new Message(); // 새 메시지로 이전 메시지 정리
			
			// 버퍼에서 메시지를 가져 옴.
			try {
				outputMsg = this.bufferCommands.get();
			} catch (InterruptedException e) {
				// 닫는 과정에서 오류를 무시
				if (this.netParent.execution) {
					System.err.println("명령 버퍼에서 메시지를 가져 오는 중 오류가 발생했습니다 : "+e.getMessage());
					e.printStackTrace();
				}
			}
			
			// 유효한지 확인하고 보냄
			try {
				if (outputMsg.isValid()) {
					this.protocolConverter.sendMessage(outputMsg);
				}
			} catch(IOException e){
				// 닫는 과정에서 오류를 무시
				if (this.netParent.execution) {
					System.err.println("네트워크에 메시지를 보내는 중 오류가 발생했습니다 : "+e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}
	
}
