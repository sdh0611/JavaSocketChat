package es.uniovi;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;

/*
 * NOTE(18.06.17, 심대현):
 * GlobalObject 클래스에서 방과 관련된 기능들을 묶어놓은 클래스입니다.
 * 기존대로하면 사실상 방 비밀번호 설정등의 기능 구현이 매우 복잡해지기에
 * 그냥 따로 빼놓는 것이 나을 것 같아 빼놨습니다.
 */


public class Room {
	
	//최대 방 인원수 제한
	public static final int 					MAX = 10;
	
	private ArrayList<String>			 	listIpBan;				//IP Ban리스트
	private ArrayList<User>			 	listUser;					//방에 있는 User 리스트
	private ArrayList<User>				listSubOperators;	//SubOperator 리스트. SubOperator들은 Kick과 Ban관련 기능 수행 가능.
	private String								roomName;			
	private String 								password;
	private Boolean							isPrivate;
	private User									operator;
	private int									count;
	
	
	public Room(User user, String _roomName){
		listIpBan = new ArrayList<String>();
		listUser = new ArrayList<User>();
		listSubOperators = new ArrayList<User>();
		roomName = _roomName;
		password = null;
		isPrivate = false;
		operator = user;
		count = 1;
		
		listUser.add(user);
		System.out.println("INFO : [" + roomName +"] 의 Op는" + operator.getNick() + ", 인원수 : " + count);
	}
	
	public Room(User user, String _roomName, String _password) {
		listIpBan = new ArrayList<String>();
		listUser = new ArrayList<User>();
		listSubOperators = new ArrayList<User>();
		roomName = _roomName;
		password = _password;
		isPrivate = true;
		operator = user;
		count = 1;
		
		listUser.add(user);
	}
	
	public final User getOperator() {
		return operator;
	}
	
	public void setOperator(User user) {
		operator = user;
	}
	
	public final ArrayList<User> getSubOperators(){
		return listSubOperators;
	}
	
	public boolean addSubOperators(User user) {
		if(listUser.contains(user)) {
			listSubOperators.add(user);
			return true;
		}
		
		return false;
	}
	
	public boolean deleteSubOperators(User user) {
		if(!listSubOperators.contains(user)) {
			return false;
		}
		listSubOperators.remove(user);
		
		return true;
	}
	
	public final int getRoomCount() {
		return count;
	}
	
	//race condition 방지를 위한 동기화 메소드 작성
	private synchronized Boolean increaseRoomCount() {
		if(count < MAX) {
			++count;
			return true;
		}
		else
			return false;
	}
	
	private synchronized Boolean decreaseRoomCount() {
		if(count > 0) {
			--count;
			return true;
		}
		else {
			return false;	
		}
	}
		
	public final String getRoomName() {
		return roomName;
	}
	
	public void setRoomName(String _roomName) {
		roomName = _roomName;		
	}	
	
	public final synchronized ArrayList<User> getUserList(){
		return listUser;
	}
			
	public Boolean addUser(User user) {
		
		if(count < MAX) {
			listUser.add(user);
			increaseRoomCount(); //동기화된 작업
			System.out.println("INFO : ["+roomName+"]의 인원은 " + count);
			return true;
		}
				
		return false;
		
	}
	
	public Boolean deleteUser(User user) {
		
		if(!listUser.contains(user))
			return false;
				
		listUser.remove(user);
		decreaseRoomCount(); // 동기화된 작업
		System.out.println("INFO : ["+roomName+"]의 인원은 " + count);
	
		if(count > 0) {
			if(user.equals(operator)) {
				if(listSubOperators.isEmpty())
					operator = listUser.get(0);
				else {
					operator = listSubOperators.get(0);
					listSubOperators.remove(0);
				}
				System.out.println("INFO : 새로운 Operator는 " + operator.getNick());
					
			}
		}
		
		return true;
	}
	
	public Boolean isUserInRoom(User user) {
		if(listUser.contains(user))
			return true;
		
		return false;
	}
	
	public User getUserByNick(String nickName) {
		
		for(User temp : listUser) 
			if(temp.getNick().equals(nickName))
				return temp;
		
		
		return null;
	}
	
	public Boolean modifyUserNickName(User user, String _newNick) {
		
		if(!listUser.contains(user))
			return false;
		
		listUser.get(listUser.indexOf(user)).setNick(_newNick);
		
		return true;
	}
	

	public void setRoomIsPrivate(Boolean _isPrivate) {		
		isPrivate = _isPrivate;		
		
		if(isPrivate) {
			System.out.println("INFO : [" + roomName + "]에서 비밀번호 설정(" + password + ")");
		}else
			System.out.println("INFO : [" + roomName + "]에서 비밀번호 해제");
		
	}
	
	public final Boolean getRoomIsPrivate() {
		return isPrivate;
	}
	
		
	public void setRoomPassword(String _password) {
		
		password = _password;
	
	}
	
	public Boolean isEqualPassword(String _password) {
		
		if(password.equals(_password))		
			return true;
		
		return false;
	}
	
	public void addAddrInIpBanList(String addr) {
		if(listIpBan.contains(addr))
			return;
		
		listIpBan.add(addr);
	}
	
	public final ArrayList<String> getBanList(){
		return listIpBan;
	}
	
	public void deleteAddrInIpBanList(String addr) {

		listIpBan.remove(addr);
	
	}
	
	public void deleteAddrInIpBanList(int index) {

		if( (index < listIpBan.size()) && (index >= 0) )
			listIpBan.remove(index);
	
	}
	
	public final Boolean isEmpty() {
		if(count > 0)
			return false;
		
		return true;
	}
	
	
}
