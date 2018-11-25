
package es.uniovi.UO217138;
import java.util.Date;

/*
 * 응용 프로그램의 다른 스레드간에 메시지를 교환하는 데 사용됩니다.
 */
public class Message {
	/* 보내거나받을 수있는 메시지의 유형 */
	public static final byte TYPE_MISC	= 0x00;
	public static final byte TYPE_MSG 	= 0x01;
	public static final byte TYPE_JOIN 	= 0x02;
	public static final byte TYPE_LEAVE = 0x03;
	public static final byte TYPE_NICK 	= 0x04;
	public static final byte TYPE_QUIT 	= 0x05;
	public static final byte TYPE_LIST 	= 0x10;
	public static final byte TYPE_WHO 	= 0x11;
	public static final byte TYPE_HELLO	= 0x20;
	public static final byte TYPE_CALL  = 0x15;
	
	//Operator가 쓸 수 있는 Message유형 추가(18.06.17, 심대현)
	public static final byte TYPE_KICK = 0x30;
	public static final byte TYPE_BAN = 0x31;
	public static final byte TYPE_BAN_RELEASE = 0x32;
	public static final byte TYPE_BAN_LIST = 0x33;
	public static final byte TYPE_SET_PRIVACY = 0x34;
	public static final byte TYPE_SUB_OPERATOR= 0x35;
	public static final byte TYPE_SUB_OP_LIST = 0x36;
	public static final byte TYPE_DE_SUB_OP = 0x37;
	
	/* 가능한 패키지의 종류 */
	public static final byte PKT_CMD 	= 0x00;
	public static final byte PKT_INF 	= 0x01;
	public static final byte PKT_OK 	= 0x02;
	public static final byte PKT_ERR 	= 0x03;
	
	private byte type;
	private byte packet;
	private String[] args = new String[0];
	private Date timeStamp;
	
	/*
	 *클래스 생성자
	 *객체 생성의 타임 스탬프 생성
	 */
	public Message(){
		// 메시지의 타임 스탬프 생성
		this.timeStamp = new Date();
	}
	
	/*
	 * 교환되는 메시지의 각 변수의 게터와 세터
	 * 
	 */
	public byte getType() {
		return this.type;
	}
	
	public void setType(byte type) {
		boolean validType = true;
		
		/* 사용 가능한 메시지 유형 중 하나인지 확인하십시오 */
		switch(type){
			case TYPE_MISC:
				break;
			case TYPE_MSG:
				break;
			case TYPE_JOIN:
				break;
			case TYPE_LEAVE:
				break;
			case TYPE_NICK:
				break;
			case TYPE_LIST:
				break;
			case TYPE_WHO:
				break;
			case TYPE_QUIT:
				break;
			case TYPE_HELLO:
				break;
			case TYPE_CALL:
				break;
			case TYPE_KICK:
				break;
			case TYPE_BAN:
				break;
			case TYPE_BAN_RELEASE:
				break;
			case TYPE_BAN_LIST:
				break;
			case TYPE_SET_PRIVACY:
				break;
			case TYPE_SUB_OPERATOR:
				break;
			case TYPE_SUB_OP_LIST:
				break;
			case TYPE_DE_SUB_OP:
				break;
			default:
				validType = false;
				break;
		}
		
		if (validType) {
			this.type = type;
		}
	}
	
	public byte getPacket() {
		return this.packet;
	}
	
	public void setPacket(byte packet) {
		boolean validPacket = true;
		
		/* 사용 가능한 패키지 유형 중 하나인지 확인하십시오. */
		switch(packet){
			case PKT_CMD:
				break;
			case PKT_INF:
				break;
			case PKT_OK:
				break;
			case PKT_ERR:
				break;
			default:
				validPacket = false;
				break;
		}
		
		if (validPacket == true) {
			this.packet = packet;
		}
	}
	
	public String[] getArgs() {
		return this.args;
	}
	
	public void setArgs(String[] args) {
		this.args = args;
	}
	
	public boolean isValid() {
		boolean valid = true;
		
		/* 사용 가능한 패키지 유형 중 하나인지 확인하십시오.. */
		switch(this.packet){
			case PKT_CMD:
				break;
			case PKT_INF:
				break;
			case PKT_OK:
				break;
			case PKT_ERR:
				break;
			default:
				valid = false;
				break;
		}
		
		if (valid == false) {
			return valid;
		}
		
		/*사용 가능한 메시지 유형 중 하나인지 확인하십시오. */
		switch(this.type){
			case TYPE_MISC:
				break;
			case TYPE_MSG:
				break;
			case TYPE_JOIN:
				break;
			case TYPE_LEAVE:
				break;
			case TYPE_NICK:
				break;
			case TYPE_LIST:
				break;
			case TYPE_WHO:
				break;
			case TYPE_QUIT:
				break;
			case TYPE_HELLO:
				break;
			case TYPE_CALL:
				break;
			case TYPE_KICK:
				break;
			case TYPE_BAN:
				break;
			case TYPE_BAN_RELEASE:
				break;
			case TYPE_BAN_LIST:
				break;
			case TYPE_SET_PRIVACY:
				break;
			case TYPE_SUB_OPERATOR:
				break;
			case TYPE_SUB_OP_LIST:
				break;
			case TYPE_DE_SUB_OP:
				break;
			default:
				valid = false;
				break;
		}
		
		return valid;
	}
	
	public Date getTimeStamp() {
		return this.timeStamp;
	}
	
	public void showDebug() {
		System.out.println("\n[DEBUG - Message]");
		System.out.println(" Package with timestamp: "+this.getTimeStamp());
		System.out.println(" ----------------------------------------");
		System.out.print(" Type of package: ");
		
		switch(this.packet){
		case PKT_CMD:
			System.out.println("Command");
			break;
		case PKT_INF:
			System.out.println("Info");
			break;
		case PKT_OK:
			System.out.println("Okey");
			break;
		case PKT_ERR:
			System.out.println("Error");
			break;
		default:
			System.out.println("Unknown");
			break;
		}
		
		System.out.print(" Type of message: ");
		
		switch(this.type){
			case TYPE_MISC:
				System.out.println("Misc");
				break;
			case TYPE_MSG:
				System.out.println("MSG");
				break;
			case TYPE_JOIN:
				System.out.println("JOIN");
				break;
			case TYPE_LEAVE:
				System.out.println("LEAVE");
				break;
			case TYPE_NICK:
				System.out.println("NICK");
				break;
			case TYPE_LIST:
				System.out.println("LIST");
				break;
			case TYPE_WHO:
				System.out.println("WHO");
				break;
			case TYPE_QUIT:
				System.out.println("QUIT");
				break;
			case TYPE_HELLO:
				System.out.println("HELLO");
				break;
			case TYPE_CALL:
				System.out.println("CALL");
				break;
			case TYPE_KICK:
				System.out.println("KICK");
				break;
			case TYPE_BAN:
				System.out.println("BAN");
				break;
			case TYPE_BAN_RELEASE:
				System.out.println("BAN_RELEASE");
				break;
			case TYPE_BAN_LIST:
				System.out.println("BAN_LIST");
				break;
			case TYPE_SET_PRIVACY:
				System.out.println("SET_PRIVACY");
				break;
			case TYPE_SUB_OPERATOR:
				System.out.println("SUB_OPERATOR");
				break;
			case TYPE_SUB_OP_LIST:
				System.out.println("SUB_OP_LIST");
			case TYPE_DE_SUB_OP:
				System.out.println("DE_SUB_OP");
			default:
				System.out.println("Unknown");
				break;
		}
		
		String[] args = this.getArgs();
		System.out.println(" Number of arguments: "+args.length);
		
		for (int n = 0; n < args.length; n++) {
			System.out.println(" - Argument "+(n+1)+": "+args[n]);
		}
		
		System.out.println("[/DEBUG - Message]\n");
	}
}
