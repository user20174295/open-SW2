package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Client { //�Ѹ��� Ŭ���̾�Ʈ�� ����� �� �ְ� �����ִ� Ŭ����
	
	Socket socket;
	
	public Client(Socket socket) { //���� �ʱ�ȭ�� ���� 
		this.socket = socket; //���� �ʱ�ȭ
		receive();
	}
	
	// Ŭ���̾�Ʈ�κ��� �޼����� ���� �޴� �޼ҵ��Դϴ�.
	public void receive() {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {
					while(true) {
						InputStream in = socket.getInputStream();
						//��� ������ ���� �ޱ� ���� inputstream��ü 
						byte[] buffer = new byte[512];
						//�ѹ��� 512����Ʈ ��ŭ ���ް���
						int length = in.read(buffer);
						//buffer�� ���޹ޱ��� length�� ���İ�
						while(length == -1) throw new IOException();
						//���࿡ �о���϶� ������ �߻��ϸ� ioexception�߻�
						System.out.println("[�޼��� ���� ����]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						//�ּ�������, thread�� ������ �̸����� ǥ��
						String message = new String(buffer, 0, length, "UTF-8");
						// �ѱ������Ҽ� �ְ� ���ڵ����Ѽ� ���۸� ���� ���� ������ message�� String���� ���� ����
						for(Client client : Main.clients) {
							client.send(message);
						}
						//���� �޼����� �ٸ� Ŭ���̾�Ʈ �鿡�Ե� ������
					}
				} catch(Exception e) {//���� �κ�
					try {
						System.out.println("[�޼��� ���� ����]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						//���κа� ���� ������ �߻��� �ּҰ��� thread�� ���� �̸����� ���
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
			
		};
		Main.threadPool.submit(thread);//�����Լ��� �ִ� threadPool�� ����
	}
	
	// Ŭ���̾�Ʈ���� �޼����� �����ϴ� �޼ҵ��Դϴ�.
	public void send(String message) {
		Runnable thread = new Runnable() {
			@Override
			public void run() {
				try {//�̰� ������ ���̹Ƿ� inputstream�� �ƴ� outputstream�� ���
					OutputStream out = socket.getOutputStream();
					byte[] buffer = message.getBytes("UTF-8");
					out.write(buffer); //���ۿ� ��䳻���� �������� Ŭ���̾�Ʈ�� ����
					out.flush();//���������� �� ���� �����ߴٴ°� �˼��ְ�����
				} catch(Exception e) {
					try {//������ �޼��� ����
						System.out.println("[�޼��� �۽� ����]"
								+ socket.getRemoteSocketAddress()
								+ ": " + Thread.currentThread().getName());
						//�����޼����� �ּ�, thread�� ���� �̸� ����
						Main.clients.remove(Client.this);
						//������ �߻��ߴٸ� �����Լ��� ���� client������ ����
						//�������� Ŭ�󿡼��� �̹� �������ϱ� ���������� ����
						socket.close();
						//������ ���� Ŭ���ξ�Ʈ�� ������ ����
					} catch(Exception e2) {
						e2.printStackTrace();
					}
				}
			}
			
		};
		Main.threadPool.submit(thread);//thread�� �����Լ��� threadPool�� ����
	}
}
