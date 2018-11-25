package es.uniovi;
import java.net.InetAddress;
import java.net.Socket;

/*	NOTE (18.06.17, 심대현) : 
 * 방 주인인지 여부를 저장하는 Boolean형 isOperator 변수 추가
 * RoomNode객체에서 해당 유저에게 옵션부여할 수 있도록 기능을 추가함.
 * User 클래스를 상속받는 Operator 클래스를 만들까 하다가 그냥 필드랑 메소드만 추가하기로함.
 * 
 */


public class User {
	private Socket socket;
	private String nick;
//	private String ipAddress;		//IP밴 구현을 위해 새로 추가한 String 필드(18.06.17, 심대현)
	private Boolean connected;
	private Boolean isOperator;	//방의 Operator인지 저장하는 멤버 추가(18.06.17, 심대현) -> 이거 없애는게 나을듯

	
	/**
	 * 연결된 사용자를 생성하는 생성자
	 * @param nick사용자의 별명
	 * @param socket 연결된 소켓
	 */
	public User(String nick, Socket socket) {
		this.nick = nick;
		this.socket = socket;
//		this.ipAddress = socket.getInetAddress().toString();
		this.connected = true;
		this.isOperator = false;
	}
	
	//생성할 때 Operator 여부를 설정할 수 있는 오버로딩 생성자 제공(18.06.17, 심대현)
	public User(String nick, Socket socket, Boolean isOperator) {
		this.nick = nick;
		this.socket = socket;
		//this.ipAddress = socket.getInetAddress().toString();
		this.connected = true;
		this.isOperator = isOperator;
	}

	//User의 ip주소를 반환하는 접근자 메소드 추가(18.06.17, 심대현)
/*	public String getUserIP() {
		return ipAddress;
	}	
*/
	/**
	 * 사용자의 연결 상태 변경
	 * @param state 사용자 연결 상태
	 */
	public void setConnected(Boolean state) {
		this.connected = state;
	}
	
	/**
	 * 사용자의 연결 상태 얻기
	 * @return 연결 상태
	 */
	public Boolean getConnected() {
		return connected;
	}
	/**
	 * 사용자의 소켓 가져 오기
	 * @return 사용자가 사용하고 있는 소켓
	 */
	
	public Socket getSocket() {
		return socket;
	}
	
	/**
	 * 그것은 전체 서버에 대해 고유 한 방식으로 연결된 사용자 및 소켓의 정보를 얻습니다.
	 * @return  정보가 있는 스트링
	 */
	public String getCompleteInfo() {
		return this.getNick()+"@"+this.getSocket().getInetAddress().getHostAddress()+":"+this.getSocket().getPort();
	}
	
	/**
	 * 사용자의 닉을 얻음
	 * @return String 닉네임
	 */
	public String getNick() {
		return nick;
	}
	
	/**
	 * 사용자의 닉네임을 변경하는 방법. 낮은 수준의 변경이며 다른 개체는 수정되지 않습니다.
	 * 동기를 보장하려면 GlobalObject 클래스에서 호출해야합니다.
	 * @param nick 새 별명
	 */
	public synchronized void setNick(String nick) {
		if (nick.length() > 0) {
			this.nick = nick;
		}
	}
	
	/**
	 * 사용자가 유효하다는 매우 낮은 수준의 확인 모든 가능성을 검사하지는 않지만 사용되는 수준에서는 충분합니다.
	 * @return boolean 유효성을 나타내는 값
	 */
	public Boolean isValid() {
		// 별명에 글자가 있고 소켓이 닫히지 않으면 유효합니다.
		if ((this.nick.length() > 0) && (this.socket.isClosed() == false)) {
			return true;
		}
		
		return false;
	}
	
	/* NOTE(18.06.17, 심대현)
	 * Operator기능을 사용하기 전 해당 User가 operator가 맞는지 검사하기 위한 public 메소드.
	 * 이 아래부터는 Operator의 기능을 구현하기 위해 추가한 메소드들입니다.
	 */
	public Boolean operatorFunction() {
		if(isOperator) {
			
			return true;
		}
		else
			return false;		
		
	}
	
	//Operator 여부를 설정하는 set메소드
	public void setIsOperator(Boolean state) {
		isOperator = state;
	}
	
	//Operator 여부를 반환하는 get메소드
	public Boolean getIsOperator() {
		return isOperator;
	}	
	
	private void setRoomPrivacy() {
		
		
	}
	
	private void assignRightsToOther(User other) {
		
		
	}
	
	private void kickOther(User user) {
		
		
	}
	
	private void banUserIP(User user) {
		
		
	}
	
	
}
