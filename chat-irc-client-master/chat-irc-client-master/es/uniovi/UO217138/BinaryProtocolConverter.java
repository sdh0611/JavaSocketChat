package es.uniovi.UO217138;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class BinaryProtocolConverter {
	private DataInputStream input;
	private DataOutputStream output;
	
	//3가지의 오버로딩된 생성자 제공
	//용도에 따라서 읽기전용, 쓰기전용으로 사용 가능.
	/**
	 * 데이터 입력 모드의 생성자
	 * @param 입력 스트림에서
	 */
	public BinaryProtocolConverter(InputStream in) {
		this.input = new DataInputStream(in);
	}
	
	/**
	 * 데이터 출력 모드의 생성자
	 * @param 출력 스트림 출력 
	 */
	public BinaryProtocolConverter(OutputStream out) {
		this.output = new DataOutputStream(out);
	}
	
	/**
	 * 데이터 입력 / 종료 모드의 생성자
	 * @param in InputStream 진입
	 * @param out OutputStream 진입
	 */
	public BinaryProtocolConverter(InputStream in, OutputStream out) {
		this.input = new DataInputStream(in);
		this.output = new DataOutputStream(out);
	}
	
	/**
	 * InputStream로부터 short를 읽는 기능
	 * @return short read
	 * @throws IOException
	 */
	short readShort() throws IOException {
		return input.readShort();
	}
	
	/**
	 *InputStream로부터 1 바이트를 읽어 들이기위한 기능
	 * @return byte 읽기
	 * @throws IOException
	 */
	byte readByte() throws IOException {
		return input.readByte();
	}
	
	/**
	 * InputStream로부터 몇개의 바이트를 읽어들이는 기능
	 * @param size size 읽을 바이트 수
	 * @return Array 바이트 읽기
	 * @throws IOException
	 */
	byte[] readByteArray(int size) throws IOException {
		byte[] departure = new byte[size];
		
		for(int n = 0; n < size; n++) {
			departure[n]=this.readByte();
		}
		
		return departure;
	}
	
	/**
	 * short 형식의 변수를 2 바이트 배열로 변환하는 메소드.
	 * @param num Short Convert
	 * @return
	 */
	public byte[] short2bytes(short num) {
		return new byte[]{(byte)(num & 0x00FF),(byte)((num & 0xFF00)>>8)};
	}
	
	/**
	 * 네트워크에서 바이너리 메시지를 받아서 메시지 형식으로 변환하는 기능.
	 * @return Message 받은 메시지
	 * @throws IOException
	 */
	public Message getMessage() throws IOException {
		//salida -> output으로 변경(18.06.16 // 심대현)
		Message output = new Message();
		
		short sizeLoad;
		short numArgs;
		short sizeArg;
		byte[] argBytes;
		String[] args;
		
		// 패키지 유형 및 메시지 유형 읽기 (2 바이트 )
		output.setPacket(this.readByte());
		output.setType(this.readByte());
		
		// 크기 읽음
		sizeLoad = readShort();
		
		if (sizeLoad > 0) { 
			// 로드가 있으면 매개 변수의 수를 읽습니다.
			numArgs = readShort();
			args = new String[numArgs];
			
			// 접수 된 인수 처리
			for(int n=0; n<numArgs; n++) {
				// 인수의 크기 (바이트)
				sizeArg = this.readShort();
				
				if (sizeArg > 0){
					// 인수를 읽고 변환하십시오.
					argBytes = this.readByteArray((int)sizeArg);
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
		
		// 스토어 인수
		output.setArgs(args);
		
		return output;
	}
	
	/**
	 * Message 형의 객체를 바이너리로 변환 해 OutputStream에 보낸다.
	 * @param msg 프린트할 메시지
	 * @throws IOException
	 */
	public void sendMessage(Message msg) throws IOException {
		short sizeLoad = 0;		// 하중 크기
		short numArgs = 0;		// 메시지 인수의 수
		byte[][] argsBytes;		// Array 이진 형식의 인수가있는 배열
		String[] args;			// Array 텍스트 형식의 인수가있는 배열
		
		// 메시지의 인수 얻기
		args = msg.getArgs();
		numArgs = (short) args.length;
		
		// 이진 인수 배열 초기화
		argsBytes = new byte[args.length][];
		
		sizeLoad += 2; //인수의 수에는 이미 2 바이트의 코스트르 사용
		
		//인수 인코딩, 한글쓸려면 여기를 설정
		for (int n = 0; n < args.length; n++) {
			argsBytes[n] = args[n].getBytes("UTF-8");
			sizeLoad += (2+argsBytes[n].length);
		}
		
		//스트림에 글쓰기
		this.output.write(msg.getPacket());
		this.output.write(msg.getType());
		this.output.writeShort(sizeLoad);
		
		if (sizeLoad > 0) {
			// Args의 개수를 outputStream에 쓴다.
			this.output.writeShort(numArgs);
			
			for (int n = 0; n < numArgs; n++) {
				// 인수의 길이와 인수를 씁니다.
				this.output.writeShort((short) argsBytes[n].length);
				this.output.write(argsBytes[n]);
			}
		}
	}
}
