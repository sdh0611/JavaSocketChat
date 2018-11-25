package es.uniovi;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 네트워크 입출력 쓰레드와 다른 서버 처리 쓰레드 간의 메시지를 처리.
 * BlockingQueue의 래퍼 클래스.
 */
public class BufferMessages {
	private ArrayBlockingQueue<Message> buffer;
	private static final int DEFAULT_SIZE = 200;
	
	/**
	 * DEFAULT SIZE 크기를 적용하는 기본 생성자입니다.
	 */
	public BufferMessages() {
		this.buffer = new ArrayBlockingQueue<Message>(DEFAULT_SIZE);
	}
	
	/**
	 * 1보다 작은 경우 DEFAULT_SIZE의 값을 적용하는 크기를 지정하는 생성자입니다.
	 */
	public BufferMessages(Integer size) {
		if (size <= 0) {
			this.buffer = new ArrayBlockingQueue<Message>(DEFAULT_SIZE);
		} else {
			this.buffer = new ArrayBlockingQueue<Message>(size);
		}
	}

	/**
	 *버퍼에서 메시지 가져 오기
	 * @return Message 획득 된 메시지
	 * @throws InterruptedException
	 */
	public Message get() throws InterruptedException {
		return this.buffer.take();
	}

	/**
	 * 버퍼에 메시지를 넣음.
	 * @param message 입력하는 Message 오브젝트
	 * @throws InterruptedException
	 *
	 */
	public void put(Message msg) throws InterruptedException {
		this.buffer.put(msg);
	}
	
	/**
	 * 버퍼 내의 요소 수를 가져옵니다.
	 * @return Integer 버퍼의 요소 수
	 */
	public Integer numElements() {
		return this.buffer.size();
	}

	/**
	 * 버퍼가 비어 있는지 확인하십시오.
	 * @return Boolean 버퍼가 비어 있거나없는 경우
	 */
	public boolean empty() {
		return (this.numElements() == 0);
	}

}
