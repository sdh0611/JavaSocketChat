package es.uniovi.UO217138;

/*
 * Class UserIn
 *사용자의 키보드 입력 처리를 담당합니다.
 * 전송할 Message 유형 객체로 변환합니다.
 * 명령 버퍼를 통해 네트워크 출력 스레드로 전송
 */

/*
 * NOTE(18.06.17, 심대현):
 * Operator기능 추가에 따라
 * 관련 기능들을 구현한 메소드를 추가했습니다.
 */

public class UserIn extends Thread {
	private ChatIRC netParent;
	private BufferFifo bufferCommands;
	private BufferFifo bufferHilo;

	/*
	 * UserIn 생성자
	 */
	public UserIn(BufferFifo bufferCommands, ChatIRC principal) {
		this.netParent = principal;
		this.bufferCommands = bufferCommands;
		this.bufferHilo = new BufferFifo(100); // 네트워크 출력 버퍼의 5 배
	}

	/*
	 * 스레드 실행 함수
	 */
	public void run() {
		Message msg;

		while(this.netParent.execution) {
			try {
				msg = this.bufferHilo.get();
				// 명령 버퍼에 메시지를 입력
				this.bufferCommands.put(msg);
			} catch (InterruptedException e) {
				if (this.netParent.execution) {
					System.err.println("이탈 메시지를 처리하지 못했습니다.");
					e.printStackTrace();
				}
			}
		}
	}

	public void sendNick(String newNick) {
		Message msgOut = new Message();

		if (newNick.length() == 0) {
			this.netParent.serverLogPrintln("ERROR: 닉네임을 변경하려면 먼저 새 닉네임을 입력해야합니다.");
			return;
		}

		this.netParent.serverLogPrintln("INFO : NICK 명령을 완료했습니다. 새로운 닉네임: "+newNick);

		msgOut.setType(Message.TYPE_NICK);
		msgOut.setPacket(Message.PKT_CMD);
		msgOut.setArgs(new String[]{newNick});

		insertMessage(msgOut);
	}

	public void sendJoin(String room, String password) {
		Message msgOut = new Message();

		if (room.length() == 0) {
			this.netParent.serverLogPrintln("ERROR: JOIN 명령을 보내는 데 필요한 데이터를받지 못했습니다.");
			return;
		}

		synchronized (this.netParent.mainWindow.room2Panel) {
			if (this.netParent.mainWindow.room2Panel.containsKey(room)){
				this.netParent.serverLogPrintln("ERROR: 이미있는 방에 입장할 수 없습니다.");
				return;
			}
		}

		

		msgOut.setType(Message.TYPE_JOIN);
		msgOut.setPacket(Message.PKT_CMD);
		msgOut.setArgs(new String[]{room, password});

		insertMessage(msgOut);
	}
	
	public void sendCall(String msg, String room) {
		Message msgOut = new Message();
		
		if (msg.length() == 0) {
			this.netParent.mainWindow.print2Room(room,"ERROR: 상대방 이름을 써주세요!");
			return;
		}
		
		if (room.equals("Log Servidor")) {
			this.netParent.serverLogPrintln("ERROR: 대기실에서는 상대방을 호출 할 수 없습니다.");
			return;
		}
		
		if (room.length() == 0) {
			this.netParent.serverLogPrintln("ERROR: MSG 명령을 보내는 데 필요한 데이터를받지 못했습니다.");
			return;
		}
		
		// 메시지 작성
		msgOut.setType(Message.TYPE_CALL);
		msgOut.setPacket(Message.PKT_CMD);
		msgOut.setArgs(new String[]{room, msg});
				
		insertMessage(msgOut);
	}

	public void sendLeave(String room) {
		Message msgOut = new Message();

		if (room.length() == 0) {
			this.netParent.serverLogPrintln("ERROR: LEAVE 명령을 보내는 데 필요한 데이터를받지 못했습니다.");
			return;
		}

		synchronized (this.netParent.mainWindow.room2Panel) {
			if (this.netParent.mainWindow.room2Panel.containsKey(room) == false){
				this.netParent.serverLogPrintln("ERROR: 올바르지 않은 방에서 나올 수 없습니다.");
				return;
			}
		}

		msgOut.setType(Message.TYPE_LEAVE);
		msgOut.setPacket(Message.PKT_CMD);
		msgOut.setArgs(new String[]{room});

		this.netParent.serverLogPrintln("INFO: '"+room+"'에서 나왔습니다.");
		this.netParent.mainWindow.print2Room(room,"INFO: '"+room+"'에서 나왔습니다.");

		insertMessage(msgOut);
	}

	public void sendList() {
		Message msgOut = new Message();

		msgOut.setType(Message.TYPE_LIST);
		msgOut.setPacket(Message.PKT_CMD);

		this.netParent.serverLogPrintln("INFO: 방 정보 LIST를 보냈습니다.");

		insertMessage(msgOut);
	}

	public void sendWho(String room) {
		Message msgOut = new Message();

		if (room.length() == 0) {
			this.netParent.serverLogPrintln("ERROR: WHO 명령을 보내는 데 필요한 데이터를받지 못했습니다.");
			return;
		}

		this.netParent.serverLogPrintln("INFO: "+room+"'에서 WHO 명령어를 사용했습니다.");
		this.netParent.mainWindow.print2Room(room,"INFO: "+room+"'에서 WHO 명령어를 사용했습니다.");

		msgOut.setType(Message.TYPE_WHO);
		msgOut.setPacket(Message.PKT_CMD);
		msgOut.setArgs(new String[]{room});

		insertMessage(msgOut);
	}

	public void sendQuit() {
		Message msgOut = new Message();

		msgOut.setType(Message.TYPE_QUIT);
		msgOut.setPacket(Message.PKT_CMD);

		this.netParent.serverLogPrintln("INFO: QUIT명령 보냈습니다.");

		insertMessage(msgOut);
	}

	public void sendMessage(String msg, String room) {
		Message msgOut = new Message();

		if (msg.length() == 0) {
			this.netParent.mainWindow.print2Room(room,"ERROR: 메시지의 내용을 적어주세요.");
			return;
		}

		if (room.length() == 0) {
			this.netParent.serverLogPrintln("ERROR: MSG 명령을 보내는 데 필요한 데이터를받지 못했습니다.");
			return;
		}

		// 메시지 작성
		msgOut.setType(Message.TYPE_MSG);
		msgOut.setPacket(Message.PKT_CMD);
		msgOut.setArgs(new String[]{room, msg});

		insertMessage(msgOut);
	}

	public void sendKick(String nickName, String room) {
		Message msgOut = new Message();
		
		if( (nickName.length() == 0) || (room.length() == 0) ) {
			this.netParent.serverLogPrintln("ERROR: KICK 명령을 보내는 데 필요한 데이터를 받지 못했습니다.");
			return;
		}
		
		msgOut.setType(Message.TYPE_KICK);
		msgOut.setPacket(Message.PKT_CMD);
		msgOut.setArgs(new String[] {nickName, room});
		
		this.netParent.serverLogPrintln("INFO: " + room + " 내의 User(" + nickName + ")에 대한 KICK명령을 요청하였습니다.");
		
		insertMessage(msgOut);
	}
	
	public void sendBan(String nickName, String room) {
		
		Message msgOut = new Message();
		
		if( (nickName.length() == 0) || (room.length() == 0) ) {
			this.netParent.serverLogPrintln("ERROR: BAN 명령을 보내는 데 필요한 데이터를 받지 못했습니다.");
			return;
		}
		
		msgOut.setType(Message.TYPE_BAN);
		msgOut.setPacket(Message.PKT_CMD);
		msgOut.setArgs(new String[] {nickName, room});
		
		this.netParent.serverLogPrintln("INFO: " + room + " 내의 User(" + nickName + ")에 대한 BAN명령을 요청하였습니다.");
		
		insertMessage(msgOut);
	}
	
	public void sendBanRelease(String address, String room) {
		
		Message msgOut = new Message();
		
		if( (address.length() == 0) || (room.length() == 0) ) {
			this.netParent.serverLogPrintln("ERROR: BAN_RELEASE 명령을 보내는 데 필요한 데이터를 받지 못했습니다.");
			return;
		}
		
		msgOut.setType(Message.TYPE_BAN_RELEASE);
		msgOut.setPacket(Message.PKT_CMD);
		msgOut.setArgs(new String[] {address, room});
		
		this.netParent.serverLogPrintln("INFO: " + room + " 내의 User(" + address + ")에 대한 Ban Release명령을 요청하였습니다.");
		netParent.mainWindow.print2Room(room, "INFO: " + room + " 내의 User(" + address + ")에 대한 Ban Release명령을 요청하였습니다.");
		
		insertMessage(msgOut);
	}
	
	public void sendBanList(String room) {
		
		Message msgOut = new Message();
		
		if( room.length() == 0 ) {
			this.netParent.serverLogPrintln("ERROR: BAN_LIST 명령을 보내는 데 필요한 데이터를 받지 못했습니다.");
			return;
		}
		
		msgOut.setType(Message.TYPE_BAN_LIST);
		msgOut.setPacket(Message.PKT_CMD);
		msgOut.setArgs(new String[] {room});
		
		this.netParent.serverLogPrintln("INFO: " + room + " 내의 Ban List를 요청하였습니다.");
		
		insertMessage(msgOut);
	}
	
	public void sendSetPrivacy(String password, String room) {
		
		Message msgOut = new Message();		
		
		if( (password.length() == 0) || (room.length() == 0) ) {
			this.netParent.serverLogPrintln("ERROR: BAN 명령을 보내는 데 필요한 데이터를 받지 못했습니다.");
			return;
		}
		
		msgOut.setType(Message.TYPE_SET_PRIVACY);
		msgOut.setPacket(Message.PKT_CMD);
		msgOut.setArgs(new String[] {password, room});
		
		this.netParent.serverLogPrintln("INFO: " + room + " 에 대한 PRIVACY 설정 명령을 요청하였습니다.(PASSWORD : " + password + ")");
		
		insertMessage(msgOut);
	}
	
	//SET_PRIVACY 해제를 위한 sendSetPrivacy 오버로딩 메소드
	public void sendSetPrivacy(String room) {
		
		Message msgOut = new Message();		
		
		if( room.length() == 0 ) {
			this.netParent.serverLogPrintln("ERROR: BAN 명령을 보내는 데 필요한 데이터를 받지 못했습니다.");
			return;
		}
		
		msgOut.setType(Message.TYPE_SET_PRIVACY);
		msgOut.setPacket(Message.PKT_CMD);
		msgOut.setArgs(new String[] { "null", room});
		
		this.netParent.serverLogPrintln("INFO: " + room + " 에 대한 PRIVACY 해제 명령을 요청하였습니다.");
		
		insertMessage(msgOut);
	}
		
	
	//SubOperator 설정을 위한 메시지를 보내는 메소드
	public void sendSubOperator(String nickName, String room) {
		
		Message msgOut = new Message();
		
		if( (nickName.length() == 0) || (room.length() == 0) ) {
			this.netParent.serverLogPrintln("ERROR: SET_RIGHTS 명령을 보내는 데 필요한 데이터를 받지 못했습니다.");
			return;
		}
		
		msgOut.setType(Message.TYPE_SUB_OPERATOR);
		msgOut.setPacket(Message.PKT_CMD);
		msgOut.setArgs(new String[] {nickName, room});
		
		this.netParent.serverLogPrintln("INFO: " + room + " 내의 User(" + nickName + ")에 대한 권한부여 명령을 요청하였습니다.");
		
		insertMessage(msgOut);
	}
	
	public void sendSubOpList(String room) {
		
		Message msgOut = new Message();
		
		if( room.length() == 0 ) {
			this.netParent.serverLogPrintln("ERROR: SUB_OP_LIST 명령을 보내는 데 필요한 데이터를 받지 못했습니다.");
			return;
		}
		
		msgOut.setType(Message.TYPE_SUB_OP_LIST);
		msgOut.setPacket(Message.PKT_CMD);
		msgOut.setArgs(new String[] {room});
		
		this.netParent.serverLogPrintln("INFO: " + room + " 내의 SUB_OP_List를 요청하였습니다.");
		
		insertMessage(msgOut);
	}
	
	public void sendDeSubOp(String nickName, String room) {
		
		Message msgOut = new Message();
		
		if( (nickName.length() == 0) || (room.length() == 0) ) {
			this.netParent.serverLogPrintln("ERROR: BAN_RELEASE 명령을 보내는 데 필요한 데이터를 받지 못했습니다.");
			return;
		}
		
		msgOut.setType(Message.TYPE_DE_SUB_OP);
		msgOut.setPacket(Message.PKT_CMD);
		msgOut.setArgs(new String[] {nickName, room});
		
		this.netParent.serverLogPrintln("INFO: " + room + " 내의 User(" + nickName + ")에 대한 De_Sub_Op명령을 요청하였습니다.");
		netParent.mainWindow.print2Room(room, "INFO: " + room + " 내의 User(" + nickName + ")에 대한 De_Sub_Op명령을 요청하였습니다.");
		
		insertMessage(msgOut);
	}
	
	private void insertMessage(Message msg) {
		try {
			// 스레드 버퍼에 메시지를 입력..
			this.bufferHilo.put(msg);
		} catch(InterruptedException e) {
			System.err.println("패킷을 내부 스레드 버퍼로 보내는 중 오류가 발생했습니다.: "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	
	
	
}
