package es.uniovi;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;

/*
 * NOTE(18.06.17, 심대현):
 * 사실 GlobalObject의 경우엔 전역적으로 하나만 존재함을 보장해야하는건데
 * 싱글톤으로 빼내던가 정적 클래스로 만들까요 ?
 */

/**
 * 서로 다른 스레드가 공유하는 전역 변수를 관리하는 클래스로, 
 * 원자 적이어야하는 연산의 동기화를 보장합니다.
 * 주요 기능: 
 * 필드들에 대한 접근자와 설정자
 * User 및 Room 관리
 * QUIT 시뮬레이션.
 * @author
 *
 */
public class GlobalObject {
	private Boolean running;
	private Boolean debug;
	private Boolean hasPanel;
	private ArrayList<Room> listRoom;	//방의 관리를 위한 ArrayList
	private HashMap<String,User> nickUsers;	// 접속한 전체 유저들의 별명을 관리하기 위한 해시맵.
	private HashMap<String,ArrayList<User> > roomUsers; // 방에 있는 User들을 관리하기 위한 해시맵.
	private HashMap<String, ArrayList<InetAddress>> roomBanList; // 방의 Banlist 관리를 위한 해시맵
	private BufferMessages bufferInput;		// User들로 부터 들어온 메시지를 보관하기 위한 BlockingQueue 래퍼클래스.
	private BufferMessages bufferOutput;		// User에게 출력하기 위한 메시지들을 보관하는 BlockingQueue 래퍼클래스.
	private Panel panel;
	
	private static final String version = "1.0";
	private static final String compilationDate = "2018-06-01";
	
	public GlobalObject() {
		this.running = true;
		this.listRoom = new ArrayList<Room>();
		this.nickUsers = new HashMap<String,User>();
		this.roomUsers = new HashMap<String,ArrayList<User> >();
		this.roomBanList = new HashMap<String, ArrayList<InetAddress>>();
		this.bufferInput = new BufferMessages();
		this.bufferOutput = new BufferMessages();
		this.panel = new Panel();
	}
	
	public final HashMap<String, ArrayList<InetAddress>> getBanListConstant(){
		return roomBanList;
	}
	
	//방을 찾아서 Room객체의 참조를 반환해주는 메소드입니다.
	public final Room findRoom(String roomName) {
		
		for(Room room : listRoom) 
			if(room.getRoomName().equals(roomName))
				return room;
		
		
		return null;
	}
	
	//특정 IP를 해당 Room의 Banlist에 추가하기 위한 메소드입니다.
	public synchronized Boolean insertRoomBanList(String roomName, String addr) {
		
		Room room = findRoom(roomName);
		
		if( room == null )
			return false;
		
		room.addAddrInIpBanList(addr);
		
		return true;
		
	}
	
	//특정 IP를 해당 room의 Banlist에서 삭제하기 위한 메소드입니다.
	public synchronized Boolean deleteRoomBanList(String roomName, String addr) {
		
		Room room = findRoom(roomName);
		
		if( room == null )
			return false;
		
		room.deleteAddrInIpBanList(addr);
		
		return true;
		
	}
	
	public final ArrayList<String> getRoomBanList(String roomName){
		
		Room room = findRoom(roomName);
		
		if( room == null )
			return null;
		
		return room.getBanList();
		
	}
	
	
	/**
	 *컴파일의 버전 번호를 얻습니다.
	 * @return String 버전 번호
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * 편집 날짜 가져 오기
	 * @return String 컴파일 날짜
	 */
	public String getCompilationdate() {
		return compilationDate;
	}

	/**
	 * thread에 실행을 계속할지 어떨지를 나타내는 메소드
	 * @return Boolean 프로세스가 계속 진행 중인지 여부를 나타내는 값
	 */
	public Boolean isRunning() {
		return running;
	}

	/**
	 * 응용 프로그램의 실행 상태를 변경하는 동기화 된 메서드
	 * @param Boolean 계속 실행해야하는지 여부를 나타내는 값
	 */
	public synchronized void setRunning(Boolean running) {
		this.running = running;
	}

	/**
	 * 디버그 모드의 활성화 상태를 가져옵니다.
	 * @return Boolean 디버그 모드가 활성 상태인지 여부를 나타내는
	 */
	public Boolean getDebug() {
		return debug;
	}

	/**
	 * 디버그 모드의 활성화 상태 수정
	 * @param Boolean 디버그 모드의 활성화 여부
	 */
	public void setDebug(Boolean debug) {
		this.debug = debug;
	}
	
	/*
	 * 사용자 작업 기능
	 */
	
	/**
	 * @return hasPanel
	 */
	public Boolean getHasPanel() {
		return hasPanel;
	}

	/**
	 * @param hasPanel the hasPanel to set
	 */
	public void setHasPanel(Boolean hasPanel) {
		this.hasPanel = hasPanel;
	}

	/**
	 * 사용되지 않는 사용자 인터페이스 사용 안 함
	 * @param user 
	 */
	public synchronized void addUser(User user) {
		nickUsers.put(user.getNick(), user);
	}
	
	/**
	 * 새로운 클라이언트가 표시된 방에 우리를 삽입하는 비공개 방이 존재하지 않으면 새로운 방이 생성됩니다.
	 * @param user 포함시킬 사용자
	 * @param room 수정할 방
	 */	
	public synchronized void addUsertoRoom(User user, String roomName) {
		Room room = findRoom(roomName);
			
		if(room == null) {
			System.out.println("INFO : 방 생성 : " + roomName);
			listRoom.add(new Room(user, roomName));
		}else {
			room.addUser(user);
		}
	}
	
	/**
	 * 방 사용자를 삭제하는 기능
	 * @param  user 지우는 사용자
	 * @param room 수정할 방
	 */
	public synchronized void removeUsertoRoom(User user, String roomName) {
		Room room = findRoom(roomName);
		
		if(room == null)
			return;
			
		room.deleteUser(user);
		
		if(room.isEmpty()) {
			System.out.println("INFO : [" + roomName + "] 제거");
			listRoom.remove(room);
		}
	}
		
	/*
	 * 존재하는 Room들의 List 반환.
	 */
	public synchronized String[] listRooms() {
		String[] chain = new String[1];
		chain[0] = "";
		
		for(Room room : listRoom) {
			chain[0] += room.getRoomName() + ";";			
		}
		
		if (chain[0].length() > 0) {
			chain[0].substring(0, chain[0].length() - 1);
		}
		
		return chain;
	}
	
	/**
	 *방의 존재를 확인하십시오.
	 * @return True when there are no rooms 
	 */	
	public synchronized boolean noRooms() {
		return listRoom.isEmpty();
	}
	
	/**
	 * 사용자가 사용하는 용도로 사용
	 * @param user 삭제할 사용자
	 */
	
	public synchronized void deleteUser(User user) {
		
		for(Room room : new ArrayList<>(listRoom)) {
			if(room.isUserInRoom(user)) {
				room.deleteUser(user);
				if(room.isEmpty()) {
					listRoom.remove(room);
					System.out.println("INFO : 방 제거 : " + room.getRoomName());
				}
			}						
		}
		
		nickUsers.remove(user.getNick());
		
		user.setConnected(false);
	}
	
	/*
	 * 방에 user가 있는지 여부 반환.
	 */	
	public synchronized boolean userInRoom(User user, String roomName){
		
		Room room = findRoom(roomName);
		
		if(room == null)
			return false;
					
		return room.isUserInRoom(user);
	}
	
	/**
	 * 방이 비어 있는지 확인하십시오.
	 * @param room 우리가 확인하려고하는 방이다.
	 * @return 방이 비어있으면 true 아니면 false
	 */
	
//	public synchronized boolean emptyRoom(String room){
//		
//		
//		if (roomUsers.containsKey(room)) {
//			
//			return roomUsers.get(room).isEmpty();
//			
//		}
//		return false;
//	}

	public synchronized boolean emptyRoom(String roomName){
			
		Room room = findRoom(roomName);
		
		if(room == null)
			return false;
	
		return room.isEmpty();
	}
	
	/**
	 * 방의 존재를 확인하십시오
	 * @param 확인할 방 이름
	 * @return  존재하는 경우는 true, 아니면 false
	 */
	public synchronized boolean isRoom(String roomName){
				
		if(findRoom(roomName) == null)
			return false;
				
		return true;
		
	}
	
	/**
	 *기존 사용자의 닉네임 
	 * @param user 닉네임이 변경된 사용자
	 * @param nick 할당 할 새 닉
	 */	
	public synchronized void modifyUserNick(User user, String nick) {
		String oldNick = user.getNick();
		user.setNick(nick);
		
		for(Room room : listRoom) {
			if(room.isUserInRoom(user)) {
				room.modifyUserNickName(user, nick);
			}			
		}
		
		nickUsers.put(user.getNick(), user);
		nickUsers.remove(oldNick);
	}
	
	/**
	 * 당신의 별명에서 사용자를 얻으십시오.
	 * @param nickname
	 * @return user 사용자가 발견했습니다.
	 */
	public User getUserByNick(String nick) {
		return nickUsers.get(nick);
	}
	
	/**
	 * 데이터 입력 버퍼를 가져옵니다.
	 * @return bufferInput
	 */
	
	public BufferMessages getBufferInput() {
		return bufferInput;
	}
	
	/**
	 * 데이터 출력 버퍼 얻기
	 * @return bufferOutput
	 */
	public BufferMessages getBufferOutput() {
		return bufferOutput;
	}


	public void setBufferOutput(BufferMessages bufferOutput) {
		this.bufferOutput = bufferOutput;
	}
	
	/**
	 * 사용자 해시 맵 얻기
	 * @return nickUsers
	 */	
	public HashMap<String, User> getNickUsers() {
		return nickUsers;
	}

	public void setNickUsers(HashMap<String, User> nickUsers) {
		this.nickUsers = nickUsers;
	}

	/**
	 *  방당 사용자 해시 맵 가져 오기
	 * @return roomUsers
	 */	
	public final ArrayList<Room> getRoomList(){
		return listRoom;				
	}

	public void setRoomUsers(HashMap<String, ArrayList<User>> roomUsers) {
		this.roomUsers = roomUsers;
	}
	
	/**
	 * 
	 * @return 표현 패널을 반환합니다.
	 */
	public synchronized Panel getPanel(){
		return panel;
	}
	
	/**
	 * 입력 버퍼에 QUIT 유형의 메시지를 입력하십시오.
	 * @param user 종료 한 사용자입니다.
	 */	
	public synchronized void simulateQUIT(User user){
		Message msgExit=new Message();
		msgExit.setUser(user);
		msgExit.setType(Message.TYPE_QUIT);
		msgExit.setPacket(Message.PKT_CMD);
		try {
			getBufferInput().put(msgExit);
		} catch (InterruptedException e1) {
			System.err.println("QUIT를 버퍼에 입력 할 수 없습니다.");
			e1.printStackTrace();
		}
	}
}
