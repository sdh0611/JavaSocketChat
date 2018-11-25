package es.uniovi;

import java.util.HashMap;

import javax.swing.tree.DefaultMutableTreeNode;

/*
 * NOTE(18.06.17, 심대현): 
 * 유저 관리하는 자료구조에 굳이 DefaultMutableTree를 쓴 이유를 잘 모르겠습니다
 * 서버의 Panel 클래스때문에 DefaultMutableTree를 쓴 것 같은데..
 */

/**
 *방 유형 노드와 내부에있는 사용자를 저장하는 데 도움이되는 클래스
 */

public class RoomNode{
	
	private DefaultMutableTreeNode room;
	private HashMap<String, DefaultMutableTreeNode> users;	 // 방당 유저를 저장할 때 Tree형태로 유저를 관리.
	
	public RoomNode(DefaultMutableTreeNode room){
		this.room=room;
		this.users=new HashMap<String, DefaultMutableTreeNode>();
	}
	
	/**
	 * 참조하는 방의 DefaultMutableTreeNode를 설정합니다.
	 * @param room 설정하고 싶은 DefaultMutableTreeNode의 값
	 */
	public synchronized void setRoom(DefaultMutableTreeNode room){
		this.room=room;		
	}
	
	/**
	 * 
	 * @return 참조하고있는 객체의 방에 대응하는 DefaultMutableTreeNode를 돌려줍니다.
	 */
	public synchronized DefaultMutableTreeNode getRoom(){
		return this.room;
	}
	
	/**
	 * 방에 사용자를 넣습니다.
	 * @param user 소개하는 사용자의 사용자 이름
	 */
	public synchronized void setUser(String user){
		users.put(user, new DefaultMutableTreeNode(user));
	}
	
	/**
	 * 지정된 사용자의 이름에 해당하는 DefaultMutableTreeNode를 반환합니다.
	 * @param user 우리가 원하는 사용자의 이름
	 * @return  사용자의 DefaultMutableTreeNode를 리턴한다.
	 */    	
	public synchronized DefaultMutableTreeNode getUser(String user){
		return users.get(user);
	}
	
	/**
	 * 사용자가 방에 있는지 확인하십시오.
	 * @param user 확인할 사용자
	 * @return 이 경우 true를 반환하고 그렇지 않으면 false를 반환합니다.
	 */
	public synchronized boolean isUser(String user){
		return users.containsKey(user);
	}
	
	/**
	 * 방에서 사용자 삭제
	 * @param user 삭제할 사용자의 이름
	 */
	public synchronized void delUser(String user){
		users.remove(user);
	}
	
	/** 
	 * @return 방에 사용자가 없으면 true를 반환하고 or false
	 */
	public synchronized boolean isEmpty(){
		return users.isEmpty();
	}
}
