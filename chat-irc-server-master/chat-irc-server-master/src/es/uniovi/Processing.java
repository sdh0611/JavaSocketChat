package es.uniovi;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/*
 * 이 클래스는 GlobalObject로 부터 가져온 메시지의 처리를 수행합니다.
 * 입력 버퍼 내부. 또한 응답 메시지를 생성합니다.
 * 출력 버퍼에 넣습니다.
 */

/*
 * NOTE(18.06.17, 심대현):
 * Operator기능을 추가함에 따라
 * Processing 클래스에도 Operator기능과 관련된 메소드 몇 개 추가해줬습니다.
 */
 
public class Processing extends Thread{

	private GlobalObject global;
	private BufferMessages bufferInput;
	private BufferMessages bufferOutput;	
	
	public Processing(GlobalObject global) {
		
		this.global=global;
		this.bufferInput=global.getBufferInput();
		this.bufferOutput=global.getBufferOutput();
		
	}

	/**
	 * 어떤 유형인지 살펴보고 메소드를 호출 할 때,
	 * 분석 할 때까지 기다리십시오.
	 * 분석에 대응
	 */
	public void run(){

		Message msg = null;
		
		while(this.global.isRunning()) {
						
			try {
				// GlobalObject의 입력버퍼로 부터 메시지를 가져옵니다.
				msg = this.bufferInput.get();
			} catch (InterruptedException e) {
				System.err.println("메시지를 읽는 동안 오류가 감지되었습니다.");	
				e.printStackTrace();				
			}
			
			// 읽어온 메시지의 타입에 따라 처리합니다.
			typeProcessing(msg);
		}


	}
	
	/**
	 * 메시지 유형에 따라 함수를 호출하십시오.
	 * 같은 경우들에 해당합니다.
	 * @param msg 분석된 메시지
	 */
	
	private void typeProcessing(Message msg) {
		if (msg.isValid()) {
			// 명령이 전송되지 않으면 오류가 발생합니다.
			if (msg.getPacket() != Message.PKT_CMD) {
				processingUNKNOW(msg);
				return;
			}
			
			switch(msg.getType()) {
				case Message.TYPE_MSG:
					processingMSG(msg);
					break;
				case Message.TYPE_JOIN:
					processingJOIN(msg);
					break;
				case Message.TYPE_LEAVE:
					processingLEAVE(msg);
					break;
				case Message.TYPE_NICK:
					processingNICK(msg);
					break;
				case Message.TYPE_LIST:
					processingLIST(msg);
					break;
				case Message.TYPE_WHO:
					processingWHO(msg);
					break;
				case Message.TYPE_QUIT:
					processingQUIT(msg);
					break;
				case Message.TYPE_CALL:
					processingCALL(msg);
					break;
				case Message.TYPE_KICK:
					processingKICK(msg);
					break;
				case Message.TYPE_BAN:
					processingBAN(msg);
					break;
				case Message.TYPE_BAN_RELEASE:
					processingBanRelease(msg);
					break;
				case Message.TYPE_BAN_LIST:
					processingBanList(msg);
					break;
				case Message.TYPE_SET_PRIVACY:
					processingSetPrivacy(msg);
					break;
				case Message.TYPE_SUB_OPERATOR:
					processingSubOperator(msg);
					break;
				case Message.TYPE_SUB_OP_LIST:
					processingSubOpList(msg);
					break;
				case Message.TYPE_DE_SUB_OP:
					processingDeSubOp(msg);
					break;
				default:
					processingUNKNOW(msg);
					break;
			}
		}
	}


	/**
	 * 처리 불가능한 유형의 메시지 처리
	 */
	private void processingUNKNOW(Message msg) {
		constructMessage(Message.TYPE_MISC, Message.PKT_ERR, new String[]{"입력 한 명령이 프로토콜에 포함되어 있지 않습니다."}, msg.getUser());
	}


	/**
	 * 해당 사용자의 QUIT메시지 처리
	 */
	private void processingQUIT(Message msg) {
		/* QUIT - OK 유형의 메시지 만 생성되어 원본으로 전송됩니다.*/
		if(msg.getUser().getSocket().isConnected())
			constructMessage(Message.TYPE_QUIT, Message.PKT_OK, new String[]{msg.getUser().getNick()}, msg.getUser());
		
		System.out.println("INFO : Call QUIT " + msg.getUser().getNick());
		/* QUIT-INF 유형의 메시지를 그것이 있던 방의 모든 사용자에게 보냈습니다 (단 한 번만).*/
		ArrayList<User> users_sended = new ArrayList<User>();
		/*
		 * NOTE(18.06.17, 심대현):
		 * Room클래스 추가에 따라 변경된 부분입니다. 
		 */
		//모든 방 순회
//		for (String key: this.global.getRoomUsers().keySet()) {								
//			ArrayList<User> users = this.global.getRoomUsers().get(key);
		for (Room room : this.global.getRoomList()) {								
			ArrayList<User> users = room.getUserList();
		
			if (users.contains(msg.getUser())) {
				for (int i = 0; i < users.size(); i++) {
					if (users.get(i) != msg.getUser() && !users_sended.contains(users.get(i))){ 
						users_sended.add(users.get(i));
						constructMessage(Message.TYPE_QUIT, Message.PKT_INF, new String[] {msg.getUser().getNick()} , users.get(i));
					}
				}
				
			}
		}
		this.global.deleteUser(msg.getUser());
		
		if (this.global.getHasPanel()) {
			//우리는 TREE에서 사용자를 제거합니다.
			this.global.getPanel().delUser(msg.getUser().getNick());
		}
	}

	/**
	 * 
	 *  방의 모든 참가자를 검색하여 처리하는 기능
	 * 원점에 WHO 유형 메시지를 생성합니다.
	 * 
	 */
	private void processingWHO(Message msg) {
		String chain = "";
		String[] args = msg.getArgs(); /* WHO 유형이므로 메시지 객체의 매개 변수를 가져옵니다. 메시지 객체의 매개 변수는 1이 될 것이고 방의 이름이 포함됩니다*/
	
		/*
		 * NOTE(18.06.17, 심대현):
		 * Room클래스 추가에 따라 변경된 부분입니다. 
		 */
//		if (this.global.getRoomUsers().containsKey(args[0])) { /* 그 이름의 방이 있는지 확인합니다. */
//			
//			/* 존재하는 경우, 우리는 방의 모든 사용자를 수집하고 그들의 닉네임들을 연결합니다 */
//			ArrayList<User> users = this.global.getRoomUsers().get(args[0]); 

		if (this.global.isRoom(args[0])) { /* 그 이름의 방이 있는지 확인합니다. */			
			/* 존재하는 경우, 우리는 방의 모든 사용자를 수집하고 그들의 닉네임들을 연결합니다 */
			ArrayList<User> users = this.global.findRoom(args[0]).getUserList(); 				
			
			for (int i = 0; i < users.size(); i++) {
				chain += users.get(i).getNick() + ";" ;	
			}
			
			/* client에 대한 응답 메시지가 생성됩니다. */
			constructMessage(Message.TYPE_WHO, Message.PKT_OK, new String[]{  args[0], chain.substring(0 , chain.length() - 1) }, msg.getUser());
		} else {
			/* 그렇지 않은 경우 오류 메시지가 생성됩니다.*/ 
			constructMessage(Message.TYPE_WHO, Message.PKT_ERR, new String[]{" 요청한 채팅방은 현재 존재하지 않습니다." }, msg.getUser());
		}
	}

	private void processingLIST(Message msg) {
		constructMessage(Message.TYPE_LIST, Message.PKT_OK, this.global.listRooms() , msg.getUser());
	}

	/**
	 * 
	 * 닉네임 변경이 가능한지 여부를 결정하는 기능
	 * 필요한 경우 필요한 조치를 취하십시오.
	 * 모든 메시지를 다른 사람에게 보내려면
	 * 사용자
	 * 
	 */
	private void processingNICK(Message msg){
		String[] args = msg.getArgs(); /* 메시지 객체의 매개 변수를 얻습니다. NICK 유형이기 때문에 메시지 객체의 매개 변수는 1 뿐이며 새 닉을 포함하게됩니다. */

		if (this.global.getNickUsers().containsKey(args[0])) { 
			// 닉네임이 이미 사용 중인지 확인한 다음 닉네임이 사용중인 경우 오류 메시지를 생성합니다.
			constructMessage(Message.TYPE_NICK, Message.PKT_ERR, new String[] {  "이미 사용중인 닉네임입니다. 다시 시도하십시오. " }, msg.getUser());
		} else {
			/* 그렇지 않으면 알림을 변경하여 닉 변경을 진행합니다. */
			String nick_old = msg.getUser().getNick();
			
			// 먼저 전역 변수에서 닉 변경을하십시오.
			this.global.modifyUserNick(msg.getUser(), args[0]);
			
			// 사용자에게 이름이 변경되었음을 알립니다.
			constructMessage(Message.TYPE_NICK, Message.PKT_OK,  new String[] { nick_old, msg.getUser().getNick() }, msg.getUser());

			/* 한편으로는 모든 참가자들에게 INFO 명령을 내리고 방을 사용자와 공유하도록 경고하는 것 */
			
			if (this.global.getHasPanel()) {
				//우리는 TREE의 모든 방에서 사용자를 삭제합니다.
				this.global.getPanel().delUser(nick_old);
			}
			/*
			 * NOTE(18.06.17, 심대현):
			 * Room클래스 추가에 따라 변경된 부분입니다. 
			 */
//			for (String key: this.global.getRoomUsers().keySet()) {
//				ArrayList<User> users = this.global.getRoomUsers().get(key);

			for (Room room : this.global.getRoomList()) {								
				ArrayList<User> users = room.getUserList();
				
				if (users.contains(msg.getUser())) {
					for (int i = 0; i < users.size(); i++) {
						if (users.get(i) != msg.getUser()) 
							constructMessage(Message.TYPE_NICK, Message.PKT_INF, new String[] {  nick_old, msg.getUser().getNick()} , users.get(i));
					}
					
					if (this.global.getHasPanel()) {
						String roomName = room.getRoomName();
						//  TREE에 사용자를 다시 소개합니다
						// 새 닉네임
						if(!this.global.getPanel().isRoom(roomName)) {
							//그것이 존재하지 않으면 우리는 그것을 창조한다.
							this.global.getPanel().newRoom(roomName);
						}						
						this.global.getPanel().newUser(roomName, msg.getUser().getNick());
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * 방에서 나가는 기능
	 * @param msg는 나가기를 원하는 사용자 및 방의 내용을 표시합니다.
	 * 
	 */
	private void processingLEAVE(Message msg) {
		String room = msg.getArgs()[0];
	
		/*
		 * NOTE(18.06.17, 심대현):
		 * Room클래스 추가에 따라 변경된 부분입니다. 
		 */
		if (this.global.isRoom(room)) {
			if (this.global.findRoom(room).getUserList().contains(msg.getUser())) {
				ArrayList<User> users = this.global.findRoom(room).getUserList();
				
				this.global.removeUsertoRoom(msg.getUser(), room);
				constructMessage(Message.TYPE_LEAVE, Message.PKT_OK,  new String[] { msg.getUser().getNick(), room }, msg.getUser());
				
				for (int i = 0; i < users.size(); i++) {					
					if(!global.findRoom(room).getOperator().equals(msg.getUser()))
						constructMessage(Message.TYPE_LEAVE, Message.PKT_INF, new String[] { msg.getUser().getNick(), room, global.findRoom(room).getOperator().getNick() }
							, users.get(i));
					else {
						constructMessage(Message.TYPE_LEAVE, Message.PKT_INF, new String[] { msg.getUser().getNick(), room }, users.get(i));
					}
				}

			}

			if (this.global.getHasPanel()) {
				//TREE의 방에서 꺼냅니다.
				this.global.getPanel().delUser(room, msg.getUser().getNick());
			}
		}
		else {
			constructMessage(Message.TYPE_LEAVE, Message.PKT_ERR,  new String[] { "ERROR: 현재이 방에 있지 않으므로 퇴장 할 수 없습니다." } , msg.getUser());
		}
	}
		
	
	

	/**
	 * 
	 * 방에 합류하는 기능
	 * * @ param msg 메시지를 입력하고 싶은 사용자와 방의 내용
	 */
	private void processingJOIN(Message msg){
		
		String room = null;
		String password = null;
		
		System.out.println("INFO : processingJOIN " + msg.getUser().getNick());
		
		try {
			room = msg.getArgs()[0];
			password = msg.getArgs()[1];
		} catch (IndexOutOfBoundsException e) {
			System.err.println("JOIN 메시지 room argument에서 에러 발생");
			e.printStackTrace();
		}
		/*
		 * NOTE(18.06.18):
		 * Ban기능 추가에 따라서 추가된 부분입니다.
		 * 방이 존재하는지 확인하고, 있다면 Ban리스트와 user의 ip를 비교함.
		 */		
		if(global.isRoom(room)) {
			if( global.findRoom(room).getBanList().contains( msg.getUser().getSocket().getInetAddress().toString().substring(1) ) ) {
				System.out.println("INFO : ["+ room + "]의 BanList에 존재하는 사용자(" + msg.getUser().getNick()+")");
				constructMessage(Message.TYPE_JOIN, Message.PKT_ERR, new String[] {  "ERROR: 사용자가 BanList에 등록되어 있습니다." } , msg.getUser());
				return;
			}
		
			if( global.findRoom(room).getRoomIsPrivate() ) {
				if( !global.findRoom(room).isEqualPassword(password) ) {
					System.out.println("INFO : ["+ room + "] Password Error from " + msg.getUser().getNick()+" password : " + password);
					constructMessage(Message.TYPE_JOIN, Message.PKT_ERR, new String[] {  "ERROR: Password가 틀렸습니다..\nUsage : /JOIN roomName password" } , msg.getUser());
					return;
				}
			}
			
		}				
		
		if (this.global.userInRoom(msg.getUser(), room)) {
			System.out.println("INFO : ["+ room + "]에 이미 존재하는 사용자(" + msg.getUser().getNick() + ")");
			constructMessage(Message.TYPE_JOIN, Message.PKT_ERR, new String[] {  "ERROR: 사용자가 이미 이 방에 있습니다." } , msg.getUser());
		} else {
			this.global.addUsertoRoom(msg.getUser(), room);
			
			if (this.global.getHasPanel()) {
				//우리는 TREE에 사용자를 소개합니다.
				//먼저 방이 있는지 확인합니다.
				if(!this.global.getPanel().isRoom(room)) {
					//그것이 존재하지 않으면 우리는 그것을 창조한다.
					this.global.getPanel().newRoom(room);
				}
			
				this.global.getPanel().newUser(room, msg.getUser().getNick());
			}
	
//			ArrayList<User> users = this.global.getRoomUsers().get(room);

			ArrayList<User> users = this.global.findRoom(room).getUserList();
			
			for (int i = 0; i < users.size(); i++) {
				if (users.get(i).getNick() == (msg.getUser().getNick())) { 
					constructMessage(Message.TYPE_JOIN, Message.PKT_OK, new String[] {  msg.getUser().getNick(), room } , users.get(i));
	                i++;
				} else {
					constructMessage(Message.TYPE_JOIN, Message.PKT_INF,  new String[] { msg.getUser().getNick(), room } , users.get(i));
				}
			}
		}
	}

	/*
	 * 메시지를 보내는 기능
	 * @param msg 모든 내용의 msg
	 */
	private void processingMSG(Message msg) {
		String [] args = msg.getArgs(); /* 우리는 방과 메시지의 매개 변수를 얻습니다. */
//		ArrayList<User> users = this.global.getRoomUsers().get(args[0]);
		ArrayList<User> users = this.global.findRoom(args[0]).getUserList();
		
//		if (this.global.getRoomUsers().containsKey(args[0]) && users.contains(msg.getUser())) { /* 우리는 방이 존재하고 그것 안에 있는지 확인합니다.*/
//			ArrayList<User> salas = this.global.getRoomUsers().get(args[0]); /* 우리는 그 방의 모든 사용자를 얻는다. */

		/*
		 * NOTE(18.06.17, 심대현):
		 * Room 클래스 추가에 따라 수정된 부분입니다.
		 */
		if (this.global.isRoom(args[0]) && users.contains(msg.getUser())) { /* 우리는 방이 존재하고 그것 안에 있는지 확인합니다.*/
			ArrayList<User> salas = this.global.findRoom(args[0]).getUserList(); /* 우리는 그 방의 모든 사용자를 얻는다. */
			
			for (int i = 0; i < salas.size(); i++) { /* 각각에 대해 사용자 정의 메시지를 생성합니다 */
				constructMessage(Message.TYPE_MSG, Message.PKT_INF, new String[] { msg.getUser().getNick(), args[0], args[1] }, salas.get(i));
			}
		} else{ /* 사용자가 방에 없다면 오류 메시지를 보냅니다. */
			constructMessage(Message.TYPE_MSG, Message.PKT_ERR, new String[] {"ERROR: 당신(User)은 방에 존재하지 않습니다. " + args[0] }, msg.getUser());
		}

	}
	/**
	 * 
	 * 메시지를 작성하고 전송하는 기능
	 * 
	 * @param type 명령 유형
	 * @param pkt 패키지
	 * @param args 메시지 인수
	 * @param user 사용자
	 */
	
	private void constructMessage(Byte type, Byte pkt, String[] args, User user){
		Message msg = new Message();
		msg.setType(type);
		msg.setPacket(pkt);
		msg.setArgs(args);
		msg.setUser(user);
		
		try {
			/* 마지막으로 출력 버퍼에 씁니다.*/
			this.bufferOutput.put(msg);
		} catch(InterruptedException e) {
			System.err.println("ERROR: 메시지를 보내는 중 오류가 발생했습니다. "+ msg.getUser().getCompleteInfo());
			e.printStackTrace();
		}
	}
	
	private void processingCALL(Message msg) {
		
		System.out.println("taemin");
		String nick = msg.getUser().getNick();
		String room = null;
		String text = null;
		
		try {
			room = msg.getArgs()[0];
			text = msg.getArgs()[1];
		} catch (IndexOutOfBoundsException e) {
			System.err.println("Error CALL");
			e.printStackTrace();
		}
		HashMap<String,User> users = this.global.getNickUsers();
		Iterator<String> keyIterator = users.keySet().iterator();
		while(keyIterator.hasNext()) {
			String key = keyIterator.next();
			System.out.println(key);
			constructMessage(Message.TYPE_CALL, Message.PKT_INF, new String[] {  msg.getUser().getNick(), room, text  } , users.get(key));
			
		}		
		

	}

	//여기서부터 Operator와 관련된 기능들.

	/*
	 * NOTE(18.06.17, 심대현):
	 * KICK기능 구현을 위한 메소드.
	 * 대상 user에게 강제로 leave메시지를 수신하도록 합니다.
	 * 
	 * BUG REPORT(18.06.18):
	 * 오류메시지 보내는거만 손봐주자.
	 */
	private void processingKICK(Message msg) {
						
		String roomName = msg.getArgs()[1];
		String kickedUserName = msg.getArgs()[0];
		Room room = global.findRoom(roomName);
		User kickedUser = room.getUserByNick(kickedUserName);		
		
		
		//메시지 송신자가 해당 방의 Operator 혹은 SubOperator가 아닌 경우 오류메시지 전송 후 종료
		if( ( !room.getOperator().equals(msg.getUser()) && ( !room.getSubOperators().contains(msg.getUser()) ) ) ) {
			constructMessage(Message.TYPE_KICK, Message.PKT_ERR, new String[] {"ERROR: 권한이 없습니다. (KICK)", roomName }, msg.getUser());
			return;
		}
				
		if(kickedUser == null) {
			constructMessage(Message.TYPE_KICK, Message.PKT_ERR, new String[] {"ERROR: 대상이 방에 존재하지 않습니다. " 
					+ kickedUserName }, msg.getUser());
			return;
		}
		
		System.out.println("INFO : Processing Kick from " + msg.getUser().getNick() + " to " + msg.getArgs()[0]);
		
		//Kick 대상 유저에게 메시지 전송
		constructMessage(Message.TYPE_KICK, Message.PKT_OK, new String[] {kickedUserName, roomName},kickedUser);
		this.global.removeUsertoRoom(kickedUser, roomName);
		
		//방에 남아있는 user들에게 메시지 전송.
		for(User user : global.findRoom(roomName).getUserList()) {
			if(user.getNick().equals(kickedUserName)) {
				constructMessage(Message.TYPE_KICK, Message.PKT_OK, new String[] {kickedUserName, roomName}, user);
				this.global.removeUsertoRoom(kickedUser, roomName);
			}
			else
				constructMessage(Message.TYPE_KICK, Message.PKT_INF, new String[] {kickedUserName, roomName}, user);
			
			if(global.findRoom(roomName).getUserList().isEmpty())
				break;
		}
	
		
	}

	//나중에 Ban해제기능 + BanList 출력기능 추가할 것.
	private void processingBAN(Message msg) {
					
		try {
			String banUserNick = msg.getArgs()[0];
			String roomName = msg.getArgs()[1];
			Room room = global.findRoom(roomName);
			
			//메시지 송신자가 해당 방의 Operator 혹은 SubOperator가 아닌 경우 오류메시지 전송 후 종료
			if( ( !room.getOperator().equals(msg.getUser()) && ( !room.getSubOperators().contains(msg.getUser()) ) ) ) {
				constructMessage(Message.TYPE_BAN, Message.PKT_ERR, new String[] {"ERROR: 권한이 없습니다. (BAN)", roomName }, msg.getUser());
				return;
			}
			
			if(!global.getNickUsers().containsKey(banUserNick)) {
				constructMessage(Message.TYPE_BAN, Message.PKT_ERR, new String[] {"ERROR: 대상이 서버에 존재하지 않습니다. " 
						+ banUserNick, roomName }, msg.getUser());
				return;
			}			
	
			User banedUser = global.getUserByNick(banUserNick);
			
			/*
			 * NOTE(18.06.18, 심대현):
			 * 대상 유저를 BanList에 등록합니다.
			 * InetAddress를 toString()으로 변환할 때		
			 */			 
			String addr = banedUser.getSocket().getInetAddress().toString().substring(1);
			room.addAddrInIpBanList(addr);
			
			//Ban 대상 유저에게 메시지 전송 후 방에서 제거
			constructMessage(Message.TYPE_BAN, Message.PKT_OK, new String[] {banUserNick, roomName},banedUser);
			this.global.removeUsertoRoom(banedUser, roomName);
						//방에 있는 user들에게 메시지 전송.
			for(User user : global.findRoom(roomName).getUserList()) {
				constructMessage(Message.TYPE_BAN, Message.PKT_INF, new String[] {banUserNick, roomName}, user);
			}
			System.out.println("INFO : Ban(" + banedUser.getSocket().getInetAddress().toString()+ ")");
			
		}catch(IndexOutOfBoundsException e) {
			System.err.println("ERROR: 메시지를 보내는 중 오류가 발생했습니다. "+ msg.getUser().getCompleteInfo());
			e.printStackTrace();
		}
				
		
	}
	
	private void processingBanRelease(Message msg) {
		try {
			String address = msg.getArgs()[0];
			String roomName = msg.getArgs()[1];
			Room room = global.findRoom(roomName);
			//메시지 송신자가 해당 방의 Operator혹은 SubOperator가 아닌 경우 오류메시지 전송 후 종료
			if( ( !room.getOperator().equals(msg.getUser()) && ( !room.getSubOperators().contains(msg.getUser()) ) )) {
				constructMessage(Message.TYPE_BAN_RELEASE, Message.PKT_ERR, new String[] {"ERROR: 권한이 없습니다. (BAN_RELEASE)", roomName }, msg.getUser());
				return;
			}
		
			if(!room.getBanList().contains(address)) {
				constructMessage(Message.TYPE_BAN_RELEASE, Message.PKT_ERR, new String[] {"ERROR: 대상이 BanList에 존재하지 않습니다. " 
						+ address, roomName }, msg.getUser());
				return;
			}			
		
			//대상 IP를 BanList에서 제거합니다.
			room.deleteAddrInIpBanList(address);

			//방에 있는 user들에게 메시지 전송.
			for(User user : room.getUserList()) {
				constructMessage(Message.TYPE_BAN_RELEASE, Message.PKT_INF, new String[] {address, room.getRoomName()}, user);
			}
		}catch(IndexOutOfBoundsException e) {
			System.err.println("ERROR: 메시지를 보내는 중 오류가 발생했습니다. "+ msg.getUser().getCompleteInfo());
			e.printStackTrace();
		}
	}
	
	private void processingBanList(Message msg) {
		
		try {
			String roomName = msg.getArgs()[0];
			Room room = global.findRoom(roomName);
		
			//메시지 송신자가 해당 방의 Operator혹은 SubOperator가 아닌 경우 오류메시지 전송 후 종료
			if( ( !room.getOperator().equals(msg.getUser()) && ( !room.getSubOperators().contains(msg.getUser()) ) ) ) {
				constructMessage(Message.TYPE_BAN_LIST, Message.PKT_ERR, new String[] {"ERROR: 권한이 없습니다. (BAN_LIST)", roomName }, msg.getUser());
				return;
			}
			
			if( room.getBanList().isEmpty() ) {
				constructMessage(Message.TYPE_BAN_LIST, Message.PKT_ERR, new String[] {"ERROR: Ban List가 비어있습니다.. (BAN_LIST)", roomName }, msg.getUser());
				return;
			}
		
			//BanList의 크기만큼 String 배열 생성
			int listSize = room.getBanList().size();
			String list[] = new String[listSize+1];		
		
			int count = 0;	//list 순회를 위한 변수
		
			//BanList 초기화
			for(String addr : room.getBanList()) {
				list[count++] = addr;
			}
		
			list[listSize] = room.getRoomName();
				
			//요청한 user에게 List 전송	
			constructMessage(Message.TYPE_BAN_LIST, Message.PKT_OK, list, msg.getUser());
		}catch(IndexOutOfBoundsException e) {
			System.err.println("ERROR: 메시지를 보내는 중 오류가 발생했습니다. "+ msg.getUser().getCompleteInfo());
			e.printStackTrace();
		}
	}
	
	private void processingSetPrivacy(Message msg) {
		
		try {
			String password = msg.getArgs()[0];
			String roomName = msg.getArgs()[1];
			
			System.out.println("Password : " + password);
			
			//메시지 송신자가 해당 방의 Operator가 아닌 경우 오류메시지 전송 후 종료
			if( !global.findRoom(roomName).getOperator().equals(msg.getUser()) ) {
				constructMessage(Message.TYPE_SET_PRIVACY, Message.PKT_ERR, new String[] {"ERROR: 권한이 없습니다. (SET_PRIVACY)", roomName }, msg.getUser());
				return;
			}
						
			/*
			 * 방의 Private여부와 password 설정
			 * 만약 password가 null이면 Private해제
			 */
			Room room = global.findRoom(roomName);	
			Boolean isPrivate = true;
			
			if(password.equals("null")) {
				room.setRoomPassword(null);
				isPrivate = false;
			}else {
				room.setRoomPassword(password);	
			}			
			room.setRoomIsPrivate(isPrivate);
			
			//방에 있는 user들에게 메시지 전송.
			for(User user : global.findRoom(roomName).getUserList()) {
				if(user == msg.getUser())
					constructMessage(Message.TYPE_SET_PRIVACY, Message.PKT_OK, new String[] {password, roomName, isPrivate.toString()}, user);
				else
					constructMessage(Message.TYPE_SET_PRIVACY, Message.PKT_INF, new String[] {password, roomName, isPrivate.toString()}, user);
			}
						
		}catch(IndexOutOfBoundsException e) {
			System.err.println("ERROR: 메시지를 보내는 중 오류가 발생했습니다. "+ msg.getUser().getCompleteInfo());
			e.printStackTrace();
		}
		
		
		
	}

	private void processingSubOperator(Message msg) {
		
		try {
			String nickName = msg.getArgs()[0];
			String roomName = msg.getArgs()[1];
			Room room = global.findRoom(roomName);
			
			//메시지 송신자가 해당 방의 Operator가 아닌 경우 오류메시지 전송 후 종료
			if( !global.findRoom(roomName).getOperator().equals(msg.getUser()) ) {
				constructMessage(Message.TYPE_SUB_OPERATOR, Message.PKT_ERR, new String[] {"ERROR: 권한이 없습니다. (SUB_OPERATOR)", roomName }, msg.getUser());
				return;
			}
			
			User subOperator = room.getUserByNick(nickName);
			
			if(subOperator == null) {
				constructMessage(Message.TYPE_SUB_OPERATOR, Message.PKT_ERR, new String[] {"ERROR: 존재하지 않는 사용자입니다.. (SUB_OPERATOR)", roomName }, msg.getUser());
				return;
			}
			
			//방의 SubOperator 목록에 user 추가
			room.addSubOperators(subOperator);
			
			//새로운 SubOperator에게 결과를 알림.
			constructMessage(Message.TYPE_SUB_OPERATOR, Message.PKT_OK, new String[] { nickName, roomName}, subOperator);
			
			//방 인원들에게 결과를 알림.
			for(User user : room.getUserList()) {
				constructMessage(Message.TYPE_SUB_OPERATOR, Message.PKT_INF, new String[] {nickName, roomName}, user);
			}
			
			
		}catch(IndexOutOfBoundsException e) {
			System.err.println("ERROR: 메시지를 보내는 중 오류가 발생했습니다. "+ msg.getUser().getCompleteInfo());
			e.printStackTrace();
		}
		
		
	}
	
	//SubOperatorList를 클라이언트에 송신하는 메소드.
	private void processingSubOpList(Message msg) {
		
		try {
			String roomName = msg.getArgs()[0];
			Room room = global.findRoom(roomName);
		
			//메시지 송신자가 해당 방의 Operator혹은 SubOperator가 아닌 경우 오류메시지 전송 후 종료
			if( ( !room.getOperator().equals(msg.getUser()) && ( !room.getSubOperators().contains(msg.getUser()) ) ) ) {
				constructMessage(Message.TYPE_SUB_OP_LIST, Message.PKT_ERR, new String[] {"ERROR: 권한이 없습니다. (SUB_OP_LIST)", roomName }, msg.getUser());
				return;
			}
			
			if( room.getSubOperators().isEmpty() ) {
				constructMessage(Message.TYPE_SUB_OP_LIST, Message.PKT_ERR, new String[] {"ERROR: SubOperator List가 비어있습니다.. (SUB_OP_LIST)", roomName }, msg.getUser());
				return;
			}
		
			//BanList의 크기만큼 String 배열 생성
			int listSize = room.getSubOperators().size();
			String list[] = new String[listSize+1];		
		
			int count = 0;	//list 순회를 위한 변수
		
			//BanList 초기화
			for(User subOp : room.getSubOperators()) {
				list[count++] = subOp.getNick();
			}
					
			list[listSize] = room.getRoomName();
		
			for(String str : list) {
				System.out.println(str);
			}
			
			//요청한 user에게 List 전송	
			constructMessage(Message.TYPE_SUB_OP_LIST, Message.PKT_OK, list, msg.getUser());
		}catch(IndexOutOfBoundsException e) {
			System.err.println("ERROR: 메시지를 보내는 중 오류가 발생했습니다. "+ msg.getUser().getCompleteInfo());
			e.printStackTrace();
		}
	}
		
	//특정 SubOperator의 권한을 해제하기 위한 메소드.
	//Operator가 아니면 사용할 수 없습니다.
	private void processingDeSubOp(Message msg) {
		try {
			String nickName = msg.getArgs()[0];
			String roomName = msg.getArgs()[1];
			Room room = global.findRoom(roomName);
			//메시지 송신자가 해당 방의 Operator가 아닌 경우 오류메시지 전송 후 종료
			if( !room.getOperator().equals(msg.getUser()) ) {
				constructMessage(Message.TYPE_DE_SUB_OP, Message.PKT_ERR, new String[] {"ERROR: 권한이 없습니다. (DE_SUB_OP)", roomName }, msg.getUser());
				return;
			}
		
			if( !room.getSubOperators().contains(room.getUserByNick(nickName)) ) {
				constructMessage(Message.TYPE_DE_SUB_OP, Message.PKT_ERR, new String[] {"ERROR: 대상이 SubOperator List에 존재하지 않습니다. " 
						+ nickName, roomName }, msg.getUser());
				return;
			}			
		
			//대상 IP를 BanList에서 제거합니다.
			room.deleteSubOperators(room.getUserByNick(nickName));

			//방에 있는 user들에게 메시지 전송.
			for(User user : room.getUserList()) {
				constructMessage(Message.TYPE_DE_SUB_OP, Message.PKT_INF, new String[] {nickName, room.getRoomName()}, user);
			}
		}catch(IndexOutOfBoundsException e) {
			System.err.println("ERROR: 메시지를 보내는 중 오류가 발생했습니다. "+ msg.getUser().getCompleteInfo());
			e.printStackTrace();
		}
	}
	
	
}





