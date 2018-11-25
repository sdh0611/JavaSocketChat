package es.uniovi;
import java.io.IOException;
import java.io.DataOutputStream;

/**
 *출력 버퍼에서 패킷을 가져 와서 해당 수신자에게 보내는 네트워크 출력 클래스, 메시지에 포함 된 User 클래스의 객체
 */
public class NetworkOut extends Thread {
	private GlobalObject global;
	private BufferMessages bufferOutput;

	/**
	 *NetworkOut 클래스의 생성자
	 * @param bufferOutput 출력 메시지 버퍼
	 * @param global Variable GlobalObject 전역 변수
	 */
	public NetworkOut(GlobalObject global) {
		this.global = global;
		this.bufferOutput = global.getBufferOutput();
	}
	
	/**
	 * 스레드의 연속 실행 방법
	 */
	public void run() {
		Message outputMsg;
		
		// 버퍼에서 메시지를 가져 와서 네트워크를 통해 꺼내는 무한 루프 실행.
		while (this.global.isRunning()) {
			outputMsg = new Message(); // 새 메시지로 이전 메시지 정리
			
			// bufferOutput로부터 메세지를 취득하려고합니다. 
			try {
				outputMsg = this.bufferOutput.get();
			} catch (InterruptedException e) {
				System.err.println("ERROR: 출력 버퍼에서 네트워크로 메시지를 가져 오는 중 오류가 발생했습니다.");
				e.printStackTrace();
			}
			
			
			try {
				if (outputMsg.isValid()) {	// 메시지가 유효한지 확인하십시오.
					if (this.global.getDebug()) {
						System.out.println("DEBUG:전송 된 메시지의 추적 "+outputMsg.getUser().getCompleteInfo()+" :");
						outputMsg.showInfo();
					}
					sendMessage(outputMsg);	//메시지 전송.
				}
				else {
					System.err.println("ERROR: 보내는 메시지가 유효하지 않습니다.");
					outputMsg.showInfo();
				}
			} catch(IOException e){
				//연결된 사용자에게 I / O 오류가 발생하면 다시 부팅하고 연결을 끊습니다.
				if (outputMsg.getUser().getConnected()) {
					System.err.println("ERROR: 발신 메시지를 사용자에게 네트워크로 보내는 중 오류가 발생했습니다. "+outputMsg.getUser().getCompleteInfo()+".");
					e.printStackTrace();
					System.out.println("INFO: 그것은 사용자의 제거로 진행됩니다. "+outputMsg.getUser().getCompleteInfo());
					
					//QUIT의 전송이 시뮬레이션됩니다.
					this.global.simulateQUIT(outputMsg.getUser());
				}
			}
		}
	}
	
	/**
	 * 바이너리로 메시지를 인코딩하고 포함 된 사용자의 소켓을 통해 메시지를 보내는 기능
	 * @param msg 보낼 Message
	 * @throws IOException
	 */
	private void sendMessage(Message msg) throws IOException {
		DataOutputStream output;// 
		short sizeLoad = 0;		// 
		short numArgs = 0;		// 
		byte[][] argsBytes;		// 
		String[] args;			//
		
		if (!msg.isValid()) {
			// 여기에 도착하기 전에 메시지가 확인되었다고 가정합니다..
			return;
		}
		
		// 사용자로부터 DataOutputStream 얻기
		output = new DataOutputStream(msg.getUser().getSocket().getOutputStream());
		
		// 메시지 인수와 크기를 얻는다.
		args = msg.getArgs();
		numArgs = (short) args.length;
		
		// 이진 인수 배열 초기화
		argsBytes = new byte[args.length][];
		
		sizeLoad += 2; // 인수의 수에 대한 2 바이트의 코스트
		// 인수 인코딩
		for (int n = 0; n < args.length; n++) {
			argsBytes[n] = args[n].getBytes("UTF-8");
			sizeLoad += (2+argsBytes[n].length);
		}
		
		// 특정 사용자의 outputStream에 쓰기
		output.write(msg.getPacket());
		output.write(msg.getType());
		output.writeShort(sizeLoad);
		
		if (sizeLoad > 0) {
			//인수가 있으면 숫자를 씁니다.
			output.writeShort(numArgs);
			
			for (int n = 0; n < numArgs; n++) {
				// 인수의 길이와 인수 자체를 작성하십시오.
				output.writeShort((short) argsBytes[n].length);
				output.write(argsBytes[n]);
			}
		}
	}
}