package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client { //한명의 클라이언트와 통신할 수 있게 도와주는 클래스
	
	Socket socket;
	
	public Client(Socket socket) { //변수 초기화를 위한 
		this.socket = socket; //변수 초기화
		receive();
	}
	
	// 클라이언트로부터 메세지를 전달 받는 메소드입니다.
	public void receive() {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					while(true) {
						InputStream in = socket.getInputStream();
						//어떠한 내용을 전달 받기 위한 inputstream객체 
						byte[] buffer = new byte[512];
						//한번에 512바이트 만큼 전달가능
						int length = in.read(buffer);
						//buffer에 전달받기전 length를 거쳐감
						while(length == -1) throw new IOException();
						//만약에 읽어들일때 오류가 발생하면 ioexception발생
						System.out.println("[메세지 수신 성공]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						//주소정보와, thread의 고유한 이름값을 표시
						String message = new String(buffer, 0, length, "UTF-8");
						// 한글포함할수 있게 인코딩시켜서 버퍼를 통해 들어온 정보를 message란 String값을 통해 전달
						for(Client client : Main.clients) {
							client.send(message);
						}
						//받은 메세지를 다른 클라이언트 들에게도 보내줌
					}
				} catch(Exception e) {//오류 부분
					try {
						System.out.println("[메세지 수신 오류]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						//윗부분과 같이 오류가 발생시 주소값과 thread의 고유 이름값을 출력
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
			
		};
		Main.threadPool.submit(thread);//메인함수에 있는 threadPool에 접속
	}
	
	// 클라이언트에게 메세지를 전송하는 메소드입니다.
	public void send(String message) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {//이건 보내는 것이므로 inputstream이 아닌 outputstream을 사용
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer); //버퍼에 담긴내용을 서버에서 클라이언트로 전송
					out.flush();//성공적으로 위 과정 수행했다는걸 알수있게해줌
				} catch(Exception e) {
					try {//오류시 메세지 송출
						System.out.println("[메세지 송신 오류]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						//오류메세지와 주소, thread의 고유 이름 전송
						Main.clients.remove(Client.this);
						//오류가 발생했다면 메인함수의 현재 client정보를 지움
						//오류나서 클라에서도 이미 끊겻으니까 서버에서도 끊음
						socket.close();
						//오류가 생긴 클라인언트의 소켓을 닫음
					} catch(Exception e2) {
						e2.printStackTrace();
					}
				}
			}
			
		};
		Main.threadPool.submit(thread);//thread를 메인함수의 threadPool에 연결
	}
}
