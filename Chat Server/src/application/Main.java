package application;
	
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

//기본 베이스를 하나의 서버 프로그램은 하나의 서버 모듈만을 작동시킴
public class Main extends Application {
	
	public static ExecutorService threadPool; //threadPool은 여러 클라이언트들이 접속시 효과적이 스레드 관리역활
	//executorservice라이브러리는 여러개의 스레드를 효과적으로 관리하게 해주는 라이브러리
	//서버의 성능 저하를 방지
	
	public static Vector<Client> clients = new Vector<Client>();
	
	
	
	ServerSocket serverSocket;
	
	//서버를 구동시켜서 클라이언트의 연결을 기다리는 메소드입니다.
	public void startServer(String IP, int port) {
		try {
			serverSocket = new ServerSocket(); //서버실행시 서버 소켓 생성
			serverSocket.bind(new InetSocketAddress(IP, port));
			//bind를 통한 서버를 담당하는 컴퓨터가 자신의 IP주소와 port번호로 특정 클라이언트의 접속을 기다림
		} catch(Exception e) {//오류가 발생하는 경우
			e.printStackTrace();
			if(!serverSocket.isClosed()) {//서버 소켓이 닫힌 경우가 아니라면
				stopServer();//닫아버림
			}
			return;
		}
		//오류가 발생하지 않고 서버가 소켓을 잘 열어서 특정 클라 기다리는 상태라면
		//클라이언트가 접속할 때까지 계속 기다리는 쓰레드 입니다.
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {//계속해서 새로운 클라이언트가 접속 할 수 있도록 만듬
						Socket socket = serverSocket.accept();
						clients.add(new Client(socket)); //클라이언트 추가
						System.out.println("[클라이언트 접속]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						//접속한 클라이언트의 정보 출력
					} catch(Exception e) {//오류 발생시
						if(!serverSocket.isClosed()) {//서버소켓문제이니까
							stopServer();//서버 멈추고
						}
						break;//break로 나감
					}
				}
			}
		};
		threadPool = Executors.newCachedThreadPool(); //threadpool초기화
		threadPool.submit(thread);//현재의 thread를 pool에 넣어줌
	}
	
	//서버의 작동을 중지키시는 메소드입니다.
	public void stopServer() {
		try {
			//현재 작동죽인 모든 소캣 닫기
			Iterator<Client> iterator = clients.iterator();
			//iterator을 사용해서 모든 클라이언트에 개별적으로 접근할수 있게 함
			while(iterator.hasNext()) {//특정클라이언트 접속후
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
				//특정 클라이언트 접속을 끊어버림
			}
			//서버 소켓 닫기
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}//서버 소켓이 널값이 아니고 소켓이 열려있다면 닫아줌
			//쓰레드 풀 종료하기
			if(threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdown();
			}//쓰레드풀역시 닫아버림
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	//UI를 생성하고, 실질적으로 프로그램을 동작시키는 메소드입니다.
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();//팬생성
		root.setPadding(new Insets(5));//padding값 주기
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("고딕", 15));
		root.setCenter(textArea);
		
		Button toggleButton = new Button("시작하기");//스위치 역활을 하는 버튼
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1,0,0,0));
		root.setBottom(toggleButton);
		
		String IP = "127.0.0.1";//자기 자신의 컴퓨터 IP만 일단 허용
		//실운용은 아님으로 일단 자기꺼만
		int port = 9876;
		
		toggleButton.setOnAction(event ->{
			if(toggleButton.getText().equals("시작하기")) {
				startServer(IP, port); //눌럿을 경우 서버 시작
				Platform.runLater(()->{ //runlater함수를 이용해서 gui요소를 출력하고자 할때 이걸 통해서 출력해야함 
					String message = String.format("[서버 시작]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("종료하기");
				});
			} else {
				stopServer();
				Platform.runLater(()->{ //runlater함수를 이용해서 gui요소를 출력하고자 할때 이걸 통해서 출력해야함 
					String message = String.format("[서버 종료]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("시작하기");
				});
			}
		});
		
		Scene scene = new Scene(root, 400, 400);//scene크기조절
		primaryStage.setTitle("[ 채팅 서버 ]");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
		
	}
	
	//프로그램의 진입점 입니다.
	public static void main(String[] args) {
		launch(args);
	}
}
