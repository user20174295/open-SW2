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

//�⺻ ���̽��� �ϳ��� ���� ���α׷��� �ϳ��� ���� ��⸸�� �۵���Ŵ
public class Main extends Application {
	
	public static ExecutorService threadPool; //threadPool�� ���� Ŭ���̾�Ʈ���� ���ӽ� ȿ������ ������ ������Ȱ
	//executorservice���̺귯���� �������� �����带 ȿ�������� �����ϰ� ���ִ� ���̺귯��
	//������ ���� ���ϸ� ����
	
	public static Vector<Client> clients = new Vector<Client>();
	
	
	
	ServerSocket serverSocket;
	
	//������ �������Ѽ� Ŭ���̾�Ʈ�� ������ ��ٸ��� �޼ҵ��Դϴ�.
	public void startServer(String IP, int port) {
		try {
			serverSocket = new ServerSocket(); //��������� ���� ���� ����
			serverSocket.bind(new InetSocketAddress(IP, port));
			//bind�� ���� ������ ����ϴ� ��ǻ�Ͱ� �ڽ��� IP�ּҿ� port��ȣ�� Ư�� Ŭ���̾�Ʈ�� ������ ��ٸ�
		} catch(Exception e) {//������ �߻��ϴ� ���
			e.printStackTrace();
			if(!serverSocket.isClosed()) {//���� ������ ���� ��찡 �ƴ϶��
				stopServer();//�ݾƹ���
			}
			return;
		}
		//������ �߻����� �ʰ� ������ ������ �� ��� Ư�� Ŭ�� ��ٸ��� ���¶��
		//Ŭ���̾�Ʈ�� ������ ������ ��� ��ٸ��� ������ �Դϴ�.
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				while(true) {
					try {//����ؼ� ���ο� Ŭ���̾�Ʈ�� ���� �� �� �ֵ��� ����
						Socket socket = serverSocket.accept();
						clients.add(new Client(socket)); //Ŭ���̾�Ʈ �߰�
						System.out.println("[Ŭ���̾�Ʈ ����]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						//������ Ŭ���̾�Ʈ�� ���� ���
					} catch(Exception e) {//���� �߻���
						if(!serverSocket.isClosed()) {//�������Ϲ����̴ϱ�
							stopServer();//���� ���߰�
						}
						break;//break�� ����
					}
				}
			}
		};
		threadPool = Executors.newCachedThreadPool(); //threadpool�ʱ�ȭ
		threadPool.submit(thread);//������ thread�� pool�� �־���
	}
	
	//������ �۵��� ����Ű�ô� �޼ҵ��Դϴ�.
	public void stopServer() {
		try {
			//���� �۵����� ��� ��Ĺ �ݱ�
			Iterator<Client> iterator = clients.iterator();
			//iterator�� ����ؼ� ��� Ŭ���̾�Ʈ�� ���������� �����Ҽ� �ְ� ��
			while(iterator.hasNext()) {//Ư��Ŭ���̾�Ʈ ������
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
				//Ư�� Ŭ���̾�Ʈ ������ �������
			}
			//���� ���� �ݱ�
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}//���� ������ �ΰ��� �ƴϰ� ������ �����ִٸ� �ݾ���
			//������ Ǯ �����ϱ�
			if(threadPool != null && !threadPool.isShutdown()) {
				threadPool.shutdown();
			}//������Ǯ���� �ݾƹ���
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	//UI�� �����ϰ�, ���������� ���α׷��� ���۽�Ű�� �޼ҵ��Դϴ�.
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();//�һ���
		root.setPadding(new Insets(5));//padding�� �ֱ�
		
		TextArea textArea = new TextArea();
		textArea.setEditable(false);
		textArea.setFont(new Font("���", 15));
		root.setCenter(textArea);
		
		Button toggleButton = new Button("�����ϱ�");//����ġ ��Ȱ�� �ϴ� ��ư
		toggleButton.setMaxWidth(Double.MAX_VALUE);
		BorderPane.setMargin(toggleButton, new Insets(1,0,0,0));
		root.setBottom(toggleButton);
		
		String IP = "127.0.0.1";//�ڱ� �ڽ��� ��ǻ�� IP�� �ϴ� ���
		//�ǿ���� �ƴ����� �ϴ� �ڱⲨ��
		int port = 9876;
		
		toggleButton.setOnAction(event ->{
			if(toggleButton.getText().equals("�����ϱ�")) {
				startServer(IP, port); //������ ��� ���� ����
				Platform.runLater(()->{ //runlater�Լ��� �̿��ؼ� gui��Ҹ� ����ϰ��� �Ҷ� �̰� ���ؼ� ����ؾ��� 
					String message = String.format("[���� ����]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("�����ϱ�");
				});
			} else {
				stopServer();
				Platform.runLater(()->{ //runlater�Լ��� �̿��ؼ� gui��Ҹ� ����ϰ��� �Ҷ� �̰� ���ؼ� ����ؾ��� 
					String message = String.format("[���� ����]\n", IP, port);
					textArea.appendText(message);
					toggleButton.setText("�����ϱ�");
				});
			}
		});
		
		Scene scene = new Scene(root, 400, 400);//sceneũ������
		primaryStage.setTitle("[ ä�� ���� ]");
		primaryStage.setOnCloseRequest(event -> stopServer());
		primaryStage.setScene(scene);
		primaryStage.show();
		
	}
	
	//���α׷��� ������ �Դϴ�.
	public static void main(String[] args) {
		launch(args);
	}
}
