package es.uniovi;
import java.util.Date;

/*
 * NOTE(18.06.17, 심대현):
 * User에 Operator만의 기능을 추가함에 따라
 * Message 클래스에도 그에 따른 메시지 상수를 추가해줬습니다.
 */


/**
 * 고객에게주고받는 데이터를 전달하는 내부 메시지 데이터의 클래스. 
 * 처리 할 / 발신 대상 사용자에 대한 정보 및 처리 / 전송할 다양한 매개 변수가 있습니다.
 */
public class Message {
	/*보내거나 받을 수 있는 메시지 유형*/
	public static final byte TYPE_MISC	= 0x00;
	public static final byte TYPE_MSG 	= 0x01;
	public static final byte TYPE_JOIN 	= 0x02;
	public static final byte TYPE_LEAVE = 0x03;
	public static final byte TYPE_NICK 	= 0x04;
	public static final byte TYPE_QUIT 	= 0x05;
	public static final byte TYPE_LIST 	= 0x10;
	public static final byte TYPE_WHO 	= 0x11;
	public static final byte TYPE_HELLO	= 0x20;
	public static final byte TYPE_CALL = 0x21;
	
	
	//Operator가 쓸 수 있는 Message유형 추가(18.06.17, 심대현)
	public static final byte TYPE_KICK = 0x30;
	public static final byte TYPE_BAN = 0x31;
	public static final byte TYPE_BAN_RELEASE = 0x32;
	public static final byte TYPE_BAN_LIST = 0x33;
	public static final byte TYPE_SET_PRIVACY = 0x34;
	public static final byte TYPE_SUB_OPERATOR= 0x35;
	public static final byte TYPE_SUB_OP_LIST = 0x36;
	public static final byte TYPE_DE_SUB_OP = 0x37;
	
	/* 가능한 패킷 유형 */
	public static final byte PKT_CMD 	= 0x00;
	public static final byte PKT_INF 	= 0x01;
	public static final byte PKT_OK 	= 0x02;
	public static final byte PKT_ERR 	= 0x03;
	
	
	private byte type;
	private byte packet;
	private String[] args = new String[0]; //대상 User에게 보낼 String
	private Date timeStamp;
	
	private User user;		//메시지 송신자

	/**
	 *	생성 시간을 나타내는 클래스의 생성자
	 */
	public Message(){
		// 메시지의 시간을 찍어낼 타임 스탬프 초기화
		this.timeStamp = new Date();
	}
	
	/**
	 *패키지 유형 가져 오기 
	 * @return  바이트 형태의 패키지 유형
	 */
	public byte getType() {
		return this.type;
	}
	
	/**
	 * 메시지 유형의 문자 설명을 가져옵니다.
	 * @return String 패키지 유형 정의
	 */
	public String getTypeLiteral() {
		switch(this.type){
			case TYPE_MISC:
				return "MISC";
			case TYPE_MSG:
				return "MSG";
			case TYPE_JOIN:
				return "JOIN";
			case TYPE_LEAVE:
				return "LEAVE";
			case TYPE_NICK:
				return "NICK";
			case TYPE_LIST:
				return "LIST";
			case TYPE_WHO:
				return "WHO";
			case TYPE_QUIT:
				return "QUIT";
			case TYPE_HELLO:
				return "HELLO";
			case TYPE_KICK:
				return "KICK";
			case TYPE_BAN:
				return "BAN";
			case TYPE_BAN_RELEASE:
				return "BAN_RELEASE";
			case TYPE_BAN_LIST:
				return "BAN_LIST";
			case TYPE_SET_PRIVACY:
				return "SET_PRIVACY";
			case TYPE_SUB_OPERATOR:
				return "SUB_OPERATOR";
			case TYPE_SUB_OP_LIST:
				return "SUB_OP_LIST";
			case TYPE_DE_SUB_OP:
				return "DE_SUB_OP";
			default:
				return "UNKNOW";
		}
	}

	
	/**
	 * 저장된 메시지 유형 설정
	 * @param  type 클래스의 변수를 사용하는 패키지 유형
	 */
	public void setType(byte type) {
		boolean validType = true;
		
		//Type을 설정하기 전 정의된 메시지타입인지 검사함.
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
		
		//유효하면 해당 타입으로 설정
		if (validType) {
			this.type = type;
		}
	}
	
	/**
	 * get Packet Type 
	 * @return Packet Type
	 */
	public byte getPacket() {
		return this.packet;
	}
	
	/**
	 * 저장된 패킷 유형 얻기
	 * @return String 형식의 패킷 유형
	 */
	public String getPacketLiteral(){
		switch(this.packet){
		case PKT_CMD:
			return "CMD";
		case PKT_INF:
			return "INF";
		case PKT_OK:
			return "OK";
		case PKT_ERR:
			return "ERR";
		default:
			return "UNKNOW";
		}
	}
	
	/**
	 * 저장되는 패킷 유형 설정
	 * @param 클래스 변수 형식의 패킷 유형
	 */
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
		
		// 유효한 유형인지 확인하십시오.
		if (validPacket == true) {
			this.packet = packet;
		}
	}
	
	/**
	 * 저장된 인수 배열 가져 오기
	 * @return Array 인수를 구성하는 문자열
	 */
	public String[] getArgs() {
		return this.args;
	}
	
	/**
	 * 저장된 인수 수정
	 * @param args Array 인수가있는 문자열
	 */
	public void setArgs(String[] args) {
		this.args = args;
	}
	
	/**
	 * 메시지가 완전하고 유효한지 확인하십시오.
	 * @return Boolean 메시지의 유효성을 나타내는  값
	 */
	
	public boolean isValid() {
		//
		boolean valid = true;
		
		/*사용 가능한 패킷 유형 중 하나인지 확인하십시오.*/
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
		
		//사용자가 설정되어 있는지 확인하십시오.
		if (this.user == null) {
			valid = false;
		}else if (!this.user.isValid()) {
			// 사용자가 유효하지 않은 경우 false
			valid = false;
		}
		
		return valid;
	}
	
	/**
	 * 메시지 작성 타임 스탬프 가져 오기
	 * @return Date TimeStamp
	 */
	public Date getTimeStamp() {
		return this.timeStamp;
	}
	
	/**
	 * 메시지의 사용자 가져 오기
	 * @return user 메시지의 사용자
	 */
	public User getUser() {
		return user;
	}

	/**
	 * 저장된 메시지의 사용자 설정
	 * @param user
	 */
	public void setUser(User user) {
		this.user = user;
	}
	
	/**
	 * 오류 및 디버그를 위해 메시지를 디버깅하는 방법
	 */
	public void showInfo() {
		System.out.println("[MESSAGE]");
		System.out.println(" TimeStamp: "+this.getTimeStamp());
		System.out.println(" ----------------------------------------");
		
		System.out.print(" 패킷 유형 : ");
		System.out.println(this.getPacketLiteral());
			
		System.out.print(" 메시지 유형 : ");
		System.out.println(this.getTypeLiteral());
		
		String[] args = this.getArgs();
		System.out.println("인수의 개수 : "+args.length);
		
		for (int n = 0; n < args.length; n++) {
			System.out.println(" - Argument "+(n+1)+": "+args[n]);
		}
		
		System.out.println("[/MESSAGE]");
	}
}
