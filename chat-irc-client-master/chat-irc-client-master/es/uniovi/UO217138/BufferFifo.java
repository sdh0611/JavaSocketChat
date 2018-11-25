
package es.uniovi.UO217138;

import java.util.concurrent.ArrayBlockingQueue;

/*
 * Class BufferFifo
 * Brief : 생성자에서 큐 크기 초기화
 * 			맨 처음 요소를 반환하는 get()
 * 			맨 마지막에 요소를 큐에 넣는 put()
 * 			요소 개수 반환하는 numElements(), 비어있는지 검사하는 empty()
 * 이 클래스는 네트워크와 사용자 I/O 사이의 중간요소로 사용됨
 * 지연 또는 양 당사자간의 동기화로 인한 다른 영향을 피할 수 있음
 * 처음에는 인스턴스만 있고, ArrayBlockingQueue클래스의 객체와 함께 작동하지만 
 * 필요한 경우 원형 버퍼가 있는 Product-Consumer 모델은 부분 및 동기화를 사용하여
 * 구현할 수 있음
 */
public class BufferFifo {
	//Thread-Safe한 Message형 BlockingQueue 배열 생성
	private ArrayBlockingQueue<Message> buffer;
	private static final int DEFAULT_SIZE = 20;
	
	/*
	 * BufferFifo 클래스의 기본 생성자
	 * DEFAULT_SIZE값으로 초기화
	 */
	public BufferFifo() {
		this.buffer = new ArrayBlockingQueue<Message>(DEFAULT_SIZE);
	}
	
	/*
	 * BufferFifo 클래스의 생성자
	 * 
	 * 비슷한 크기로 전달한 경우 수신 된 크기를 사용하십시오.
	 * 값이 0보다 작 으면 기본값을 사용하십시오.
	 */
	public BufferFifo(Integer size) {
		if (size <= 0) {
			this.buffer = new ArrayBlockingQueue<Message>(DEFAULT_SIZE);
		} else {
			this.buffer = new ArrayBlockingQueue<Message>(size);
		}
	}

	/*
	 * Function get();
	 * 버퍼로부터 메세지를 취득하여 메세지가 취득될  까지 Thread를 정지 OR
	 * 호출원에게 돌려주어지는 에외를 위해 중단
	 */
	public Message get() throws InterruptedException {
		//take() : 큐가 비어있으면 대기, 채워져 있는 경우 반환.
		return this.buffer.take();
	}

	/*
	 * Function put(); -> 큐가 꽉 찼는지 검사함.
	 * 버퍼에 메시지를 입력하여 버퍼가 만성적으로 액세스하거나 
	 * 예외로 인해 중단 될 때 까지 스레드를 차단
	 */
	public void put(Message msg) throws InterruptedException {
		this.buffer.put(msg);
	}
	
	/*
	 * Function numElements();
	 * 버퍼 내의 요소 수 반환
	 */
	public Integer numElements() {
		return this.buffer.size();
	}

	/*
	 * Function empty();
	 * 버퍼가 있는지 여부를 나타내는 부울 값을 반환.
	 * 비어 있거나 일부 요소가 있습니다.
	 */
	public boolean empty() {
		return (this.numElements() == 0);
	}

}
