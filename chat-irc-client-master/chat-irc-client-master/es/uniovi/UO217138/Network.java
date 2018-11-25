package es.uniovi.UO217138;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 채팅 클라이언트 내의 네트워크를 나타냄.
 */
public class Network extends Thread {
	private static final int MAX_USERS = 10;
	private static final int MAX_WAITING_TIME_SECS = 20;
	private static final String[] ROOM = new String[] { "test", "empty" };
	private ArrayBlockingQueue<String> msgQueue;
	private static final int MAX_MESSAGES = 20;
	private boolean closed;

	/**
	 * 네트워크 형식의 개체를 만듬.
	 */
	public Network() {
		msgQueue = new ArrayBlockingQueue<String>(MAX_MESSAGES);
		setName("Network Thread");
		closed = false;
		start();
	}

	/**
	 * 네트워크 닫기
	 * 
	 * @throws IllegalStateException
	 * 				네트워크가 이미 닫힌 경우.
	 */
	public void close() throws IllegalStateException {
		testClosed();
		interrupt();
		closed = true;
	}

	/**
	 * 보류중인 메시지가없는 경우 네트워크에서 다음 메시지를 수신하여 스레드를 차단합니다.
	 * 
	 * @return 받은 메시지
	 * @throws IllegalStateException
	 *             네트워크가 이미 닫힌 경우.
	 * @throws InterruptedException
	 *             메시지를 기다리는 동안 스레드가 차단 된 경우
	 */
	public String recv() throws IllegalStateException, InterruptedException {
		testClosed();
		return msgQueue.take();
	}

	/**
	 * 메시지를 보낼 공간이 없으면 스레드를 차단하는 메시지를 네트워크로 보냅니다.
	 * 
	 * @param msg
	 *            보낼 메시지
	 * @throws IllegalStateException
	 *             네트워크가 닫힌 경우.
	 * @throws InterruptedException
	 *             메시지를 기다리는 동안 스레드가 차단 된 경우
	 */
	public void send(String msg) throws IllegalStateException,
			InterruptedException {
		testClosed();
		msgQueue.put(msg);
	}

	/**
	 * 네트워크가 닫혀 있으면 예외를 발생
	 * 
	 * @throws IllegalStateException
	 *             네트워크가 닫힌 경우.
	 */
	private void testClosed() throws IllegalStateException {
		if (closed)
			throw new IllegalStateException("The network is already closed");
	}

	/**
	 * 임의의 메시지 생성 스레드.
	 * 
	 * @throws IllegalStateException
	 *             네트워크가 닫힌 경우.
	 */
	@Override
	public void run() throws IllegalStateException {
		int i = 0;
		testClosed();
		Random random = new Random();
		try {
			while (true) {
				// 임의시간 sleep
				sleep(random.nextInt(MAX_WAITING_TIME_SECS) * 1000);
				// 사용자 닉네임 생성
				int n = random.nextInt(MAX_USERS) + 1;
				String nick = "User" + n;
				String room = ROOM[n % 2];
				String message = "Message number " + i;
				String msg = "/MSG;"+nick + ";" + room + ";" + message;
				// put
				msgQueue.put(msg);
				i = i + 1;
			}
		} catch (InterruptedException ex) {
		}
	}
}