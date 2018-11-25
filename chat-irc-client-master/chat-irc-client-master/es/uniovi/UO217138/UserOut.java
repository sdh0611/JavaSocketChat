package es.uniovi.UO217138;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.UnsupportedAudioFileException;

//import es.uniovi.Message;

public class UserOut extends Thread {
	private ChatIRC netParent;
	private BufferFifo bufferResponses;
	
	/*
	 * UserOut 생성자
	 */
	public UserOut (BufferFifo bufferResponses, ChatIRC principal) {
		this.netParent = principal;
		this.bufferResponses = bufferResponses;
	}
	
	/*
	 * 스레드 실행 함수
	 */
	public void run() {
		Message message;
		
		while(this.netParent.execution) {
			//의미가 없는 것 같아서 일단 주석처리했습니다.(18.06.17, 심대현)
			//message = new Message();
			
			try {
				// 버퍼 응답을 얻으려고 시도
				message = this.bufferResponses.get();
			
				if (message.isValid()) {
					switch(message.getType()) {
						case Message.TYPE_MSG:
							processMsg(message);
							break;				
						case Message.TYPE_JOIN:
							processJoin(message);
							break;							
						case Message.TYPE_LEAVE:
							processLeave(message);
							break;							
						case Message.TYPE_NICK:
							processNick(message);
							break;							
						case Message.TYPE_QUIT:
							processQuit(message);
							break;							
						case Message.TYPE_LIST:
							processList(message);
							break;							
						case Message.TYPE_WHO:
							processWho(message);							
							break;							
						case Message.TYPE_HELLO:
							processHello(message);
							break;							
						case Message.TYPE_MISC:
							processMisc(message);
							break;							
						case Message.TYPE_CALL:
							processCALL(message);
							break;
						case Message.TYPE_KICK:
							processKick(message);
							break;
						case Message.TYPE_BAN:
							processBan(message);
							break;
						case Message.TYPE_BAN_RELEASE:
							processBanRelease(message);
							break;
						case Message.TYPE_BAN_LIST:
							processBanList(message);
							break;
						case Message.TYPE_SET_PRIVACY:
							processSetPrivacy(message);
							break;
						case Message.TYPE_SUB_OPERATOR:
							processSubOperator(message);
							break;
						case Message.TYPE_SUB_OP_LIST:
							processSubOpList(message);
							break;
						case Message.TYPE_DE_SUB_OP:
							processDeSubOp(message);
							break;
					}
				}
			} catch(InterruptedException e) {
				// 닫는 과정에서 오류를 무시
				if (this.netParent.execution) {
					System.err.println("입력 대기열에서 패키지를 읽는 중 오류가 발생했습니다.");
					e.printStackTrace();
				}
			}
		}
	}
	
	
	private void processCALL(Message message){
		String[] args = message.getArgs();
		String sendName = args[0];
		String roomName = args[1];
		String recvName = args[2];
		
		System.out.println("taemine된다고");
		
		if(netParent.nick.equals(recvName)){
			File fileName = new File("sound.wav");
			netParent.serverLogPrintln("CALL : "+sendName+"님께서 '"+roomName+"'으로 당신을 호출했습니다.");
			try{
				AudioInputStream ais = AudioSystem.getAudioInputStream(fileName);
	            Clip clip = AudioSystem.getClip();
	            clip.stop();
	            clip.open(ais);
	            clip.start();
			}catch(UnsupportedAudioFileException e){
				e.printStackTrace();
				return;
			} catch(Exception e){
				e.printStackTrace();
			}
			


		}
		
	}
	/**
	 * message 유형 메시지를 처리하여 사용자방으로 보냅니다.
	 * @param message
	 */
	private void processMsg( Message message) {
		String[] args = message.getArgs();
		
		if (message.getPacket() == Message.PKT_INF) {
			// MSG INFO 서버 메시지 : 다른 사용자가 보낸 메시지
			netParent.mainWindow.print2Room(args[1],args[0]+">"+args[2]);
		}else if (message.getPacket() == Message.PKT_ERR) {
			// 서버 오류	
			netParent.serverLogPrintln("ERROR: 메시지를 보내는 중 오류가 발생했습니다 - "+args[0]);
		}
	}
	
	/**
	 * JOIN 메시지 처리
	 * @param message
	 */
	private void processJoin (Message message) {
		String[] args = message.getArgs();
		
		//INFO
		if (message.getPacket() == Message.PKT_INF) {
			// 
			// JOIN INF를 받으면 다른 사용자가 방을 가입했습니다.
			netParent.mainWindow.print2Room(args[1], ""+ args[0]+"님이 입장하셨습니다.");
			
			if (netParent.room2Users.get(args[1]) != null) {
				// 사용자 목록 업데이트
				synchronized (netParent.room2Users) {
					netParent.room2Users.get(args[1]).add(args[0]);
				}
				// UI 업데이트
				netParent.mainWindow.setUsersRoom(args[1], netParent.room2Users.get(args[1]));
			} else {
				netParent.serverLogPrintln("ERROR: 연결되지 않은 방에서 알림을받습니다.");
			}
			
		}else if (message.getPacket() == Message.PKT_OK) {
			this.netParent.serverLogPrintln("INFO: JOIN명령을 완료했습니다. '"+args[1]+"'에 입장하셨습니다.");
			// JOIN OK를 받으면 클라이언트가 방에 들어갑니다..
			netParent.mainWindow.createRoom(args[1]);
			
			// UI에 공간 만들기
			netParent.userIn.sendList();

		}else if (message.getPacket() == Message.PKT_ERR) {
			// 서버 오류	
			netParent.serverLogPrintln("ERROR: 방에 입장하는 중 오류..\n"+ args[0]);
		}
	}
	
	/**
	 * Leave type message processing
	 * @param message
	 */
	private void processLeave (Message message) {
		String[] args = message.getArgs();
		
		//package type leave room
		if (message.getPacket() == Message.PKT_INF) {
//			System.out.println("LEAVE : PKT_INF");
			// Notify in the room that the user has left
			netParent.mainWindow.print2Room(args[1], "INFO: 사용자 "+ args[0]+" 가 방을 떠났습니다..");
			
			if(args.length > 2)
				netParent.mainWindow.print2Room(args[1], "INFO : 새로운 Operator는 <" + args[2] + "> 입니다.");
			// Update the list of users of the room
			synchronized (netParent.room2Users) {
				netParent.room2Users.get(args[1]).remove(args[0]);
				netParent.mainWindow.setUsersRoom(args[1], netParent.room2Users.get(args[1]));
			}
			
		}else if (message.getPacket() == Message.PKT_OK) {
//			System.out.println("LEAVE : PKT_OK");
			// Notify in the console
			netParent.serverLogPrintln("INFO: "+args[1]+"을 떠났습니다.");
			
			// Remove the eyelash from the main window
			netParent.mainWindow.removeRoom(args[1]);
			
			// Remove user data from that room
			synchronized(this.netParent.room2Users) {
				this.netParent.room2Users.remove(args[1]);
			}
			
			// Reload the list of rooms.
			netParent.userIn.sendList();
			
		}else if (message.getPacket() == Message.PKT_ERR) {
			// server error	
			netParent.serverLogPrintln("ERROR: 방을 떠났습니다. - "+args[0]);
		}
	}
	
	/**
	 * Process NICK type messages from the server
	 * @param message
	 */
	private void processNick (Message message) {
		String[] args = message.getArgs();
		
		if (message.getPacket() == Message.PKT_INF) {
			// 메시지 유형 NICK INF : 닉을 변경 한 방의 사용자에게 알립니다.
			this.netParent.serverLogPrintln("INFO: 사용자 "+ args[0]+" 는  "+args[1]+"로 닉네임을 변경했습니다");
			
			synchronized(this.netParent.room2Users) {
				Object[] rooms = this.netParent.room2Users.keySet().toArray();
				for (int i = 0; i < rooms.length; i++) {
					String key = (String)rooms[i];
					
					// 사용자가이 방에 있는지 확인
					if (this.netParent.room2Users.get(key).indexOf(args[0]) != -1) {
						this.netParent.room2Users.get(key).remove(args[0]);
						this.netParent.room2Users.get(key).add(args[1]);
						// UI 업데이트
						this.netParent.mainWindow.setUsersRoom(key, this.netParent.room2Users.get(key));
						this.netParent.mainWindow.print2Room(key, "INFO: 사용자 "+ args[0]+" 는  "+args[1]+"로 닉네임을 변경했습니다");
					}
				}
			}
		}else if (message.getPacket() == Message.PKT_OK) {
			// 닉을 올바르게 변경했습니다.
			this.netParent.serverLogPrintln("INFO: "+ args[0]+"이  "+args[1]+"으로 바뀌었습니다.");
			
			// 현재 닉 정보 업데이트
			synchronized(this.netParent.nick) {
				this.netParent.nick = args[1];
			}
			
			// 모든 객실이 모두 업데이트되어 있기 때문에 업데이트해야합니다.
			synchronized(this.netParent.room2Users) {
				Object[] rooms = this.netParent.room2Users.keySet().toArray();
				for (int i = 0; i < rooms.length; i++) {
					String key = (String) rooms[i];
					// 사용자가이 방에 있는지 확인하십시오.
					this.netParent.room2Users.get(key).remove(args[0]);
					this.netParent.room2Users.get(key).add(args[1]);
					// UI 업데이트
					this.netParent.mainWindow.setUsersRoom(key, this.netParent.room2Users.get(key));
					this.netParent.mainWindow.print2Room(key, "INFO: "+ args[0]+"이  "+args[1]+"으로 바뀌었습니다.");
				}
			}
		}else if (message.getPacket() == Message.PKT_ERR) {
			// 서버 오류	
			netParent.serverLogPrintln("ERROR: 별명 변경 오류 - "+args[0]);
		}
	}
	
	/**
	 * QUIT 유형의 메시지 처리
	 * @param message
	 */
	private void processQuit (Message message) {
		String[] args = message.getArgs();
		
		//QUIT INF 유형
		if (message.getPacket() == Message.PKT_INF) {
			netParent.serverLogPrintln("INFO: 사용자 "+ args[0]+" 연결이 끊어져있다.");
			
			// 모든 객실이 모두 업데이트되어 있기 때문에 업데이트해야합니다.
			synchronized(this.netParent.room2Users) {
				Object[] rooms = this.netParent.room2Users.keySet().toArray();
				for (int i = 0; i < rooms.length; i++) {
					String key = (String) rooms[i];
					
					if (this.netParent.room2Users.get(key).indexOf(args[0]) != -1) {
						// 사용자가이 방에 있는지 확인.
						this.netParent.room2Users.get(key).remove(args[0]);
						// UI 업데이트
						this.netParent.mainWindow.setUsersRoom(key, this.netParent.room2Users.get(key));
						this.netParent.mainWindow.print2Room(key, "INFO: 사용자 "+ args[0]+" 의 연결이 끊어졌습니다.");
					}
				}
			}
			//서버 메시지
		}else if (message.getPacket() == Message.PKT_OK) {
			// 올바른 연결 해제 주 스레드 알리기
			this.netParent.closeThreads();
		}else if (message.getPacket() == Message.PKT_ERR) {
			netParent.serverLogPrintln("ERROR: 연결 해제 실패 - "+args[0]);
			// 서버 오류
		}
	}
	
	/**
	 * LIST 유형 메시지 처리
	 * @param message
	 */
	private void processList (Message message) {
		String[] args = message.getArgs();
		
		//
		if (message.getPacket() == Message.PKT_OK) {
			final String[] rooms = args[0].split(";");
			int num_salas = rooms.length;
			
			if (rooms[0].length() == 0) {
				num_salas--;
			}
			
			// 가능한 경우 목록을 업데이트하십시오.
			this.netParent.mainWindow.updateRoomList(rooms);
			
			// 콘솔에 정보 표시
			this.netParent.serverLogPrintln("INFO: LIST정보는 총 "+num_salas+" 입니다.");
			
		}else if (message.getPacket() == Message.PKT_ERR) {
			this.netParent.serverLogPrintln("ERROR: 현재 방list을 요청하는 중 오류가 발생했습니다. - "+args[0]);
			//서버 오류	
		}
	}
	
	/**
	 * WHO 유형 메시지 처리
	 * @param message
	 */
	private void processWho (Message message) {
		String[] args = message.getArgs();
		
		//
		if (message.getPacket() == Message.PKT_OK) {
			String[] users = args[1].split(";");
			ArrayList<String> usersList = new ArrayList<String>();
			usersList.addAll(Arrays.asList(users));
			
			// 받은 목록 저장
			synchronized (this.netParent.mainObject.room2Users) {
				this.netParent.mainObject.room2Users.put(args[0], usersList);
			}
			
			this.netParent.mainWindow.setUsersRoom(args[0], usersList);
			//서버 회신
		}else if (message.getPacket() == Message.PKT_ERR) {
			netParent.serverLogPrintln("ERROR: 방의 사용자를 확보하지 못했습니다. - "+args[0]);
			// 서버오류	
		}
	}
	
	/**
	 * HELLO 유형 메시지 처리
	 * @param message
	 */
	private void processHello (Message message) {
		String[] args = message.getArgs();
		
		//서버 환영 메시지 유형의 패키지
		if (message.getPacket() == Message.PKT_OK) {
			netParent.serverLogPrintln("SERVER: "+args[0]);
			//서버 회신
		}
	}
	
	/**
	 * MISC 유형의 프로세스 메시지
	 * @param message
	 */
	private void processMisc(Message message) {
		String[] args = message.getArgs();
		
		//다른 유형의 패키지 유형
		if (message.getPacket() == Message.PKT_ERR) {
			netParent.serverLogPrintln("ERROR0: "+args[0]);
			// 서버 오류
		}
	}
	
	
	private void processKick(Message msg) {
		
		String[] args = msg.getArgs();
		
		//package type leave room
		if (msg.getPacket() == Message.PKT_INF) {
			// Notify in the room that the user has left
			netParent.mainWindow.print2Room(args[1], "INFO: 사용자 "+ args[0]+" 가 퇴장당했습니다..");
			
			// Update the list of users of the room
			synchronized (netParent.room2Users) {
				netParent.room2Users.get(args[1]).remove(args[0]);
				netParent.mainWindow.setUsersRoom(args[1], netParent.room2Users.get(args[1]));
			}
//			System.out.println("PKT_INF");
		}else if (msg.getPacket() == Message.PKT_OK) {
			// Notify in the console
			netParent.serverLogPrintln("INFO: "+args[1]+"에서 퇴장당했습니다.");
			
			// Remove the eyelash from the main window
			netParent.mainWindow.removeRoom(args[1]);
			
			// Remove user data from that room
			synchronized(this.netParent.room2Users) {
				this.netParent.room2Users.remove(args[1]);
			}
			
			// Reload the list of rooms.
			netParent.userIn.sendList();
			
//			System.out.println(msg.getArgs()[0] + " PKT_OK");
			
		}else if (msg.getPacket() == Message.PKT_ERR) {
			// server error	
			netParent.mainWindow.print2Room(args[1], args[0]);
//			netParent.serverLogPrintln(args[0]);
//			System.out.println(msg.getArgs()[0] + " PKT_ERR");
		}
		
//		System.out.println(msg.getArgs()[0] + " processKick");
	}

	private void processBan(Message msg) {
				
		String[] args = msg.getArgs();
		
		//package type leave room
		if (msg.getPacket() == Message.PKT_INF) {
			// Notify in the room that the user has left
			netParent.mainWindow.print2Room(args[1], "INFO: 사용자 "+ args[0]+" 가 IP Ban을 당했습니다..");
			
			// Update the list of users of the room
			synchronized (netParent.room2Users) {
				netParent.room2Users.get(args[1]).remove(args[0]);
				netParent.mainWindow.setUsersRoom(args[1], netParent.room2Users.get(args[1]));
			}
//			System.out.println("PKT_INF");
		}else if (msg.getPacket() == Message.PKT_OK) {
			// Notify in the console
			netParent.serverLogPrintln("INFO: "+args[1]+"에서 IP Ban을 당했습니다.");
			
			// Remove the eyelash from the main window
			netParent.mainWindow.removeRoom(args[1]);
			
			// Remove user data from that room
			synchronized(this.netParent.room2Users) {
				this.netParent.room2Users.remove(args[1]);
			}
			
			// Reload the list of rooms.
			netParent.userIn.sendList();
			
		}else if (msg.getPacket() == Message.PKT_ERR) {
			// server error	
			netParent.mainWindow.print2Room(args[1], args[0]);

		}
		
	}

	private void processBanRelease(Message msg) {
		
		String[] args = msg.getArgs();
		
		//package type leave room
		if (msg.getPacket() == Message.PKT_INF) {
			// Notify in the room that the user has left
			netParent.mainWindow.print2Room(args[1], "INFO: IP :  "+ args[0]+" 가 IP BanList에서 제외되었습니다..");
					
		}else if (msg.getPacket() == Message.PKT_ERR) {
			// server error	
			netParent.mainWindow.print2Room(args[1], args[0]);

		}
		
	}
	
	private void processBanList(Message msg) {
		
		String[] args = msg.getArgs();

		if (msg.getPacket() == Message.PKT_OK) {
			// Notify in the console
			netParent.mainWindow.print2Room(args[args.length-1], "-----------BAN LIST-----------");
			for(int i=0; i<args.length-1; ++i) {
				netParent.mainWindow.print2Room(args[args.length-1], args[i]);
			}

		}else if (msg.getPacket() == Message.PKT_ERR) {
			// server error	
			netParent.mainWindow.print2Room(args[1], args[0]);

		}
		
	}
	
	private void processSetPrivacy(Message msg) {
		
		String[] args = msg.getArgs();

		
		if (msg.getPacket() == Message.PKT_INF) {
			// Notify in the room that the user has left
			if(args[2].equals("true"))
				netParent.mainWindow.print2Room(args[1], "INFO: 방 Password가 설정되었습니다. Password : "+ args[0]);
			else
				netParent.mainWindow.print2Room(args[1], "INFO: 방 Password가 해제되었습니다.");

		}else if (msg.getPacket() == Message.PKT_OK) {
			// Notify in the console
			if(args[2].equals("true"))
				netParent.mainWindow.print2Room(args[1], "INFO: Password 설정이 완료되었습니다. Password : " + args[0]);
			else
				netParent.mainWindow.print2Room(args[1], "INFO: Password 해제가 완료되었습니다. Password : " + args[0]);
			
		}else if (msg.getPacket() == Message.PKT_ERR) {
			// server error	
			netParent.mainWindow.print2Room(args[1], args[0]);
			
		}
		
	}
	
	//특정 User가 SubOperator가 되었음을 알립니다.
	private void processSubOperator(Message msg) {
		
		String args[] = msg.getArgs();
		
		if(msg.getPacket() == Message.PKT_INF) {
			netParent.mainWindow.print2Room(args[1], "INFO : " + args[0] + "가 SubOperator가 되었습니다.");
		}
		else if(msg.getPacket() == Message.PKT_OK) {
			netParent.mainWindow.print2Room(args[1], "INFO : SubOperator가 되었습니다 !");
		}else if(msg.getPacket() == Message.PKT_ERR) {
			netParent.mainWindow.print2Room(args[1], args[0]);
		}
		
		
	}
	
	//SubOperator들의 목록을 출력해줍니다.
	private void processSubOpList(Message msg) {
		
		String[] args = msg.getArgs();

		if (msg.getPacket() == Message.PKT_OK) {
			// Notify in the console
			netParent.mainWindow.print2Room(args[args.length-1], "-----------SUB OPERATOR LIST-----------");
			for(int i=0; i<args.length-1; ++i) {
				netParent.mainWindow.print2Room(args[args.length-1], args[i]);
			}

		}else if (msg.getPacket() == Message.PKT_ERR) {
			// server error	
			netParent.mainWindow.print2Room(args[1], args[0]);

		}
		
	}
	
	//특정 SubOperator의 권한이 해제되었음을 알립니다.
	private void processDeSubOp(Message msg) {
		
		String[] args = msg.getArgs();
		
		if (msg.getPacket() == Message.PKT_INF) {
			netParent.mainWindow.print2Room(args[1], "INFO: "+ args[0]+"가 SubOperator에서 제외되었습니다..");
					
		}
		else if(msg.getPacket() == Message.PKT_OK){
			netParent.mainWindow.print2Room(args[1], "INFO : SubOperator가 해제되었습니다..");
		}
		else if (msg.getPacket() == Message.PKT_ERR) {
			// server error	
			netParent.mainWindow.print2Room(args[1], args[0]);

		}
		
	}
	
	
	
}
