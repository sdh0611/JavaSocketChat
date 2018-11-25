/*
 * IRC 스타일의 채팅 클라이언트
 * 컴퓨터 그룹 작업
 *
 */
package es.uniovi.UO217138;
import java.net.Socket;
import java.io.IOException;

/*
 * Class NetworkIn
 *
 * 네트워크에서 들어오는 데이터를 처리하는 클래스
 * Message 클래스의 응답으로 변환함.
 * 응답 버퍼에 입력하셈.
 */
public class NetworkIn extends Thread {
	private ChatIRC netParent;
	private BufferFifo bufferResponses;
	private Socket socket;
	private BinaryProtocolConverter protocolConverter;

	/*
	 * NetworkIn 클래스 생성자
	 */
	public NetworkIn(BufferFifo bufferResponses, Socket netInterface, ChatIRC principal) {
		this.netParent = principal;
		this.bufferResponses = bufferResponses;
		this.socket = netInterface;

		try {
			this.protocolConverter = new BinaryProtocolConverter(this.socket.getInputStream());
		} catch (IOException e) {
			System.err.println("네트워크 입력 스트림을 가져 오는 데 실패했습니다 : "+e.getMessage());
			e.printStackTrace();
		}
	}

	/*
	 * 스레드 메서드.
	 */
	public void run() {
		Message inputMsg; // 네트워크에서 얻은 메시지

		while (this.netParent.execution) {
			inputMsg = new Message(); // 메시지를 정리하고 유효하지 않은 항목이 있기 전에 메시지가 반복되지 않도록 하셈.

			try {
				// 네트워크에서 패킷을 가져 옴.
				inputMsg = this.protocolConverter.getMessage();
			} catch (IOException e) {
				// 닫는 과정에서 오류무시
				if (this.netParent.execution) {
					System.err.println("네트워크에서 메시지를 가져 오는 중 오류가 발생했습니다 : "+e.getMessage());
					e.printStackTrace();
				}
			}
			try {
				// 답변 버퍼에 개체를 입력합니다 (유효한 경우).
				if (inputMsg.isValid()) {
					this.bufferResponses.put(inputMsg);
				}
			}
			catch (InterruptedException e) {
				// 닫는 과정에서 오류무시
				if (this.netParent.execution) {
					System.err.println("응답 버퍼에서 메시지를로드하는 중 오류가 발생했습니다 : "+e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

}
