package Server;
import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.awt.event.ActionEvent;
import javax.swing.SwingConstants;

public class JavaGameServer extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	JTextArea textArea;
	private JTextField txtPortNumber;
	private ServerSocket socket; // ��������
	private Socket client_socket; // accept() ���� ������ client ����
	private Vector<UserService> UserVec = new Vector<UserService>(); // ����� ��ü ����ڸ� ������ ����
	private Vector<UserService> WaitVec = new Vector<UserService>(); // ���� ����� ������ ����
	private Vector<GameRoom> RoomVec = new Vector<GameRoom>(); // �� ��� ������ ����
	private static final int BUF_LEN = 128; // Windows ó�� BUF_LEN �� ����

	// ����
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JavaGameServer frame = new JavaGameServer();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	// ���� ������
	public JavaGameServer() {
		new DefaultListModel<String>();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 338, 440);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 10, 300, 298);
		contentPane.add(scrollPane);

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		JLabel lblNewLabel = new JLabel("Port Number");
		lblNewLabel.setBounds(13, 318, 87, 26);
		contentPane.add(lblNewLabel);

		txtPortNumber = new JTextField();
		txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
		txtPortNumber.setText("30000");
		txtPortNumber.setBounds(112, 318, 199, 26);
		contentPane.add(txtPortNumber);
		txtPortNumber.setColumns(10);

		JButton btnServerStart = new JButton("Server Start");
		btnServerStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
				} catch (NumberFormatException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				AppendText("Chat Server Running..");
				btnServerStart.setText("Chat Server Running..");
				btnServerStart.setEnabled(false); // ������ ���̻� �����Ű�� �� �ϰ� ���´�
				txtPortNumber.setEnabled(false); // ���̻� ��Ʈ��ȣ ������ �ϰ� ���´�
				AcceptServer accept_server = new AcceptServer();
				accept_server.start();
			}
		});
		btnServerStart.setBounds(12, 356, 300, 35);
		contentPane.add(btnServerStart);
	}

	// ���ο� ������ accept() �ϰ� user thread�� ���� �����Ѵ�.
	class AcceptServer extends Thread {
		public void run() {
			while (true) { // ����� ������ ����ؼ� �ޱ� ���� while��
				try {
					AppendText("Waiting new clients ...");
					client_socket = socket.accept(); // accept�� �Ͼ�� �������� ���� �����
					AppendText("���ο� ������ from " + client_socket);
					// User �� �ϳ��� Thread ����
					UserService new_user = new UserService(client_socket, socket);
					UserVec.add(new_user); // ���ο� ������ �迭�� �߰�
					WaitVec.add(new_user); // ������� ���� ���Ϳ� ������ �߰�
					new_user.start(); // ���� ��ü�� ������ ����

					AppendText("���� ������ �� " + UserVec.size());
				} catch (IOException e) {
					AppendText("accept() error");
				}
			}
		}
	}

	// ����â�� ���
	public void AppendText(String str) {
		textArea.append(str + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	// ����â�� ä�� ��ü ���
	public void AppendObject(ChatMsg msg) {
		textArea.append("code = " + msg.code + "\n");
		textArea.append("id = " + msg.UserName + "\n");
		textArea.append("data = " + msg.data + "\n");
		textArea.append("x = " + msg.x);
		textArea.append("    y = " + msg.y + "\n");
		textArea.append("move = " + msg.move + "\n" + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	// User �� �����Ǵ� Thread
	// Read One ���� ��� -> Write All
	class UserService extends Thread {

		private ObjectInputStream ois;
		private ObjectOutputStream oos;
		private Socket client_socket;

		public String UserName = ""; // �̸� ����
		public String UserStatus; // ���� ����
		public String RoomNameList = ""; // �� ����Ʈ�� ���� �� �� ���ڿ�

		GameRoom myRoom; // ������ �� ��ü�� ����

		public UserService(Socket client_socket, ServerSocket socket) {
			this.client_socket = client_socket;

			try {
				oos = new ObjectOutputStream(client_socket.getOutputStream());
				oos.flush();
				ois = new ObjectInputStream(client_socket.getInputStream());
			} catch (Exception e) {
				AppendText("userService error");
			}
		}

		public void Login() {
			AppendText("���ο� ������ " + UserName + " ����.");
			WriteOne("Welcome to Java chat server\n");
			WriteOne(UserName + "�� ȯ���մϴ�.\n"); // ����� ����ڿ��� ���������� �˸�
			String msg = "[" + UserName + "]���� ���� �Ͽ����ϴ�.\n";
			WriteOthers(msg); // ���� user_vc�� ���� ������ user�� ���Ե��� �ʾҴ�.
			RoomNameList = "";
			for (int i = 0; i < RoomVec.size(); i++) {
				RoomNameList += (RoomVec.elementAt(i).RoomName + "$");
			}
			ChatMsg cm = new ChatMsg(UserName, "504", "�� ��� ����", RoomNameList);
			WriteOneObject(cm); // ���� ���� ������� �� ����Ʈ ���ſ� �ʿ��� �� ����
		}

		public void Logout() {
			String msg = "[" + UserName + "]���� ���� �Ͽ����ϴ�.\n";
			UserVec.removeElement(this); // Logout�� ���� ��ü�� ���Ϳ��� �����
			WaitVec.removeElement(this);
			WriteAll(msg); // ���� ������ �ٸ� User�鿡�� ����
			AppendText("����� " + "[" + UserName + "] ����. ���� ������ �� " + UserVec.size());
			SendUserListAll(); // ���� ��� ����
		}

		// ��� User�鿡�� ���. ������ UserService Thread�� WriteONe() �� ȣ���Ѵ�.
		public void WriteAll(String str) {
			for (int i = 0; i < UserVec.size(); i++) {
				UserService user = (UserService) UserVec.elementAt(i);
				if (user.UserStatus == "O")
					user.WriteOne(str);
			}
		}

		// ��� User�鿡�� Object�� ���. ä�� message�� image object�� ���� �� �ִ�
		public void WriteAllObject(Object ob) {
			for (int i = 0; i < UserVec.size(); i++) {
				UserService user = (UserService) UserVec.elementAt(i);
				if (user.UserStatus == "O")
					user.WriteOneObject(ob);
			}
		}

		// ���� �����鿡�� Object �߼�
		public void WriteRoomObject(Object ob) {
			for (int i = 0; i < myRoom.InRoomUser.size(); i++) {
				UserService user = (UserService) myRoom.InRoomUser.elementAt(i);
				user.WriteOneObject(ob);
			}
		}

		// ����� �����ڿ��� ������ �̸��� �˷��ش�
		public void InformElseName(Object ob) {
			UserService user = (UserService) myRoom.InRoomUser.elementAt(0);
			user.WriteOneObject(ob); // ���忡�� ������ �̸� ����
			String owner = user.UserName;
			user = (UserService) myRoom.InRoomUser.elementAt(1);
			ChatMsg player2m = new ChatMsg(user.UserName, "507", owner);
			user.WriteOneObject(player2m); // �����ڿ��� ���� �̸� ����
		}

		// ���� ������ User�鿡�� ���. ������ UserService Thread�� WriteONe() �� ȣ���Ѵ�.
		public void WriteOthers(String str) {
			for (int i = 0; i < UserVec.size(); i++) {
				UserService user = (UserService) UserVec.elementAt(i);
				if (user != this && user.UserStatus == "O")
					user.WriteOne(str);
			}
		}

		// Windows ó�� message ������ ������ �κ��� NULL �� ����� ���� �Լ�
		public byte[] MakePacket(String msg) {
			byte[] packet = new byte[BUF_LEN];
			byte[] bb = null;
			int i;
			for (i = 0; i < BUF_LEN; i++)
				packet[i] = 0;
			try {
				bb = msg.getBytes("euc-kr");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (i = 0; i < bb.length; i++)
				packet[i] = bb[i];
			return packet;
		}

		// UserService Thread�� ����ϴ� Client ���� 1:1 ����
		public void WriteOne(String msg) {
			try {
				ChatMsg obcm = new ChatMsg("SERVER", "200", msg);
				oos.writeObject(obcm);
			} catch (IOException e) {
				AppendText("dos.writeObject() error");
				try {
					ois.close();
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Logout(); // �������� ���� ��ü�� ���Ϳ��� �����
			}
		}

		// ��ο��� ����� ����� ������
		public void SendUserListAll() {
			for (int i = 0; i < UserVec.size(); i++) {
				UserService user = (UserService) UserVec.elementAt(i);
				user.SendUserList();
			}
		}

		// ����� ����� ������
		public void SendUserList() {
			String users = "";
			// �ݸ����� ����� ����� ����
			for (int i = 0; i < WaitVec.size(); i++) {
				UserService user = (UserService) WaitVec.elementAt(i);
				users += (user.UserName + "\n");
			}
			try {
				ChatMsg obcm = new ChatMsg("SERVER", "300", users);
				oos.writeObject(obcm);
			} catch (IOException e) {
				AppendText("dos.writeObject() error");
				try {
					ois.close();
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Logout(); // �������� ���� ��ü�� ���Ϳ��� �����
			}
		}

		// ����ڵ鿡�� Object ����
		public void WriteWaitObject(Object ob) {
			for (int i = 0; i < WaitVec.size(); i++) {
				UserService user = (UserService) WaitVec.elementAt(i);
				user.WriteOneObject(ob);
			}
		}

		// �ӼӸ� ����
		public void WritePrivate(String msg) {
			try {
				ChatMsg obcm = new ChatMsg("�ӼӸ�", "200", msg);
				oos.writeObject(obcm);
			} catch (IOException e) {
				AppendText("dos.writeObject() error");
				try {
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				Logout(); // �������� ���� ��ü�� ���Ϳ��� �����
			}
		}

		// Object ����
		public void WriteOneObject(Object ob) {
			try {
				oos.writeObject(ob);
			} catch (IOException e) {
				AppendText("oos.writeObject(ob) error");
				try {
					ois.close();
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				Logout();
			}
		}

		public void run() {
			while (true) { // ����� ������ ����ؼ� �ޱ� ���� while��
				try {
					Object obcm = null;
					String msg = null;
					ChatMsg cm = null;
					if (socket == null)
						break;
					try {
						obcm = ois.readObject();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						return;
					}
					if (obcm == null)
						break;
					if (obcm instanceof ChatMsg) {
						cm = (ChatMsg) obcm;
						AppendObject(cm);
					} else
						continue;
					// �α���
					if (cm.code.matches("100")) {
						UserName = cm.UserName;
						UserStatus = "O"; // Online ����
						Login();
						// ���� ����Ʈ ����
						SendUserListAll();
					}

					// 200 ä�� �޽��� ����
					else if (cm.code.matches("200")) {
						msg = String.format("[%s] %s", cm.UserName, cm.data);
						AppendText(msg); // server ȭ�鿡 ���
						String[] args = msg.split(" "); // �ܾ���� �и��Ѵ�.
						if (args.length == 1) { // Enter key �� ���� ��� Wakeup ó���� �Ѵ�.
							UserStatus = "O";
						} else if (args[1].matches("/exit")) {
							Logout();
							break;
						} else if (args[1].matches("/list")) {
							WriteOne("User list\n");
							WriteOne("Name\tStatus\n");
							WriteOne("-----------------------------\n");
							for (int i = 0; i < UserVec.size(); i++) {
								UserService user = (UserService) UserVec.elementAt(i);
								WriteOne(user.UserName + "\t" + user.UserStatus + "\n");
							}
							WriteOne("-----------------------------\n");
						} else if (args[1].matches("/sleep")) {
							UserStatus = "S";
						} else if (args[1].matches("/wakeup")) {
							UserStatus = "O";
						} else if (args[1].matches("/to")) { // �ӼӸ�
							for (int i = 0; i < UserVec.size(); i++) {
								UserService user = (UserService) UserVec.elementAt(i);
								if (user.UserName.matches(args[2]) && user.UserStatus.matches("O")) {
									String msg2 = "";
									for (int j = 3; j < args.length; j++) {// ���� message �κ�
										msg2 += args[j];
										if (j < args.length - 1)
											msg2 += " ";
									}
									// /to ����.. [�ӼӸ�] [user1] Hello user2..
									user.WritePrivate(args[0] + " " + msg2 + "\n");
									// user.WriteOne("[�ӼӸ�] " + args[0] + " " + msg2 + "\n");
									break;
								}
							}
						} else { // �Ϲ� ä�� �޽���
							UserStatus = "O";
							// WriteAll(msg + "\n"); // Write All
							WriteAllObject(cm);
						}
					} else if (cm.code.matches("400")) {
						Logout();
					}

					// 500 �� ����
					else if (cm.code.matches("500")) {
						myRoom = new GameRoom(); // �� ��ü ���� �� myRoom�� �ʱ�ȭ
						myRoom.setRoomName(cm.data); // �� �̸� ����
						myRoom.count++; // �� �ο��� ����
						RoomVec.add(myRoom); // room ���Ϳ� ��� ���� myRoom �߰�
						myRoom.InRoomUser.add(this); // myRoom�� �����ο� ���Ϳ� Ŭ���̾�Ʈ �߰�
						WaitVec.remove(this); // ���� �ο����� Ŭ���̾�Ʈ ����
						SendUserListAll(); // ���� �ο� ����Ʈ ����
						cm.code = "504";
						RoomNameList = "";
						for (int i = 0; i < RoomVec.size(); i++) {
							RoomNameList += (RoomVec.elementAt(i).RoomName + "$");
						}
						cm.data = myRoom.RoomName;
						cm.dm = RoomNameList;
						WriteAllObject(cm);
					}

					// 501 ������
					else if (cm.code.matches("501")) {
						for (int i = 0; i < RoomVec.size(); i++) { // ������ ���� ������ŭ �ݺ�
							GameRoom r = RoomVec.get(i); // �ݺ����� ���鼭 i��° �� ��ü ����
							if (r.RoomName.equals(cm.data)) { // �� ������ ������
								if (r.count < 2) { // �� �ο����� 2���� ���� �� ����
									myRoom = RoomVec.get(i); // myRoom�� �� ������ �´� i��° room�� �ʱ�ȭ
									myRoom.count++; // ���� �ο��� �ϳ� �߰�
									WaitVec.remove(this); // ���� ���� �ο����� Ŭ���̾�Ʈ ����
									myRoom.InRoomUser.add(this); // myRoom�� ���� �ο��� Ŭ���̾�Ʈ �߰�
									// �� ���� �� ó��
									cm.code = "506";
									cm.data = "�濡 ����";
									InformElseName(cm); // ���忡�� ������ �̸��� ����
									SendUserListAll(); // ����� ��� ����
								} else { // �� �ο����� 2�� �̻��̹Ƿ� ���� ����
									cm.code = "505";
									cm.data = "������ �� �ο��� �� á���ϴ�.";
									WriteOneObject(cm);
								}
							}
						}
					}
					// 503 �� ä�� �޽��� ����
					else if (cm.code.matches("503")) {
						msg = String.format("[%s] %s", cm.UserName, cm.data);
						AppendObject(cm);
						// �� �� �ȿ� ������Ը� ������.
						WriteOneObject(cm);
					}

					// 508 �� ���� ����
					else if (cm.code.matches("508")) {
						msg = String.format("[%s] %s", cm.UserName, cm.data);
						AppendObject(cm);
						// �� �ȿ� ������Ը� ������.
						WriteRoomObject(cm);
					}

					// �� ����
					else if (cm.code.matches("510")) { // ������
						AppendObject(cm);
						for (int i = 0; i < RoomVec.size(); i++) {
							GameRoom r = RoomVec.get(i); // �ݺ����� ���鼭 i��° �� ��ü ����
							if (r.RoomName.equals(cm.data)) {
								AppendText("�� �ο�" + r.count);
								if (r.count == 2) { // �濡 ȥ���϶� ������
									for (int j = 0; j < r.InRoomUser.size(); j++) {
										ChatMsg out = new ChatMsg(UserName, "509", "back");
										WriteRoomObject(out);
										WaitVec.add(r.InRoomUser.get(j));
									}
									RoomVec.removeElement(r); // �� ����

									RoomNameList = "";
									for (int j = 0; j < RoomVec.size(); j++) {
										RoomNameList += (RoomVec.elementAt(j).RoomName + "$");
									}
									System.out.println("������ ó�� �� list" + RoomNameList);
									cm.code = "504";
									cm.data = myRoom.RoomName;
									cm.dm = RoomNameList;
									// �� ��� ����
									WriteAllObject(cm);
									SendUserListAll(); // ���� �ο� ����
								}
							}
						}
					}

					// 600 ���� ����
					else if (cm.code.matches("600")) {
						if (myRoom.count == 2) {
							for (int i = 0; i < myRoom.InRoomUser.size(); i++) { // myRoom�� �ο�����ŭ �ݺ�
								ChatMsg start = new ChatMsg(myRoom.InRoomUser.get(i).UserName, "600", myRoom.RoomName);
								UserService user = myRoom.InRoomUser.get(i); // ���� ���� �鿡�� ���� ��ȣ
								user.WriteOneObject(start);
								AppendText(user.UserName + "���� ����");
							}
						}
					}

					// ��ǥ�� �޾Ƽ� ��뿡�� �����Ѵ�
					else if (cm.code.matches("601")) {
						AppendText("��ȣ Ȯ��");
						for (int i = 0; i < myRoom.InRoomUser.size(); i++) {
							if (!(myRoom.InRoomUser.get(i).UserName.equals(UserName))) {
								AppendText(UserName + "��" + myRoom.InRoomUser.get(i).UserName + "���� ���´�");
								AppendText(cm.code + cm.move + cm.x + cm.y);
								UserService user = myRoom.InRoomUser.get(i); // ���� ���� �鿡�� ���� ��ȣ
								user.WriteOneObject(cm);
							}
						}
					}

					// 602�� ��ǳ�� ��ǥ ����
					else if (cm.code.matches("602")) {
						for (int i = 0; i < myRoom.InRoomUser.size(); i++) {
							if (!(myRoom.InRoomUser.get(i).UserName.equals(UserName))) {
								AppendText(UserName + "��" + myRoom.InRoomUser.get(i).UserName + "���� ���´�");
								AppendText(cm.code + cm.move + cm.x + cm.y);
								UserService user = myRoom.InRoomUser.get(i); // ���� ���� �鿡�� ���� ��ȣ
								user.WriteOneObject(cm);
							}
						}
					}

					// 400 ����
					else if (cm.code.matches("400")) { // logout message ó��
						Logout();
						break;
					} else { // 300, 500, ... ��Ÿ object�� ��� ����Ѵ�.
						WriteAllObject(cm);
					}

				} catch (IOException e) {
					AppendText("server) ois.readObject() error");
					try {
						RoomVec.removeElement(myRoom);
						UserVec.removeElement(this);
						RoomNameList = "";
						for (int j = 0; j < RoomVec.size(); j++) {
							RoomNameList += (RoomVec.elementAt(j).RoomName + "$");
						}
						ChatMsg cm = new ChatMsg(UserName, "504", "�� ��� ����", RoomNameList);
						// �� ��� ����
						if (myRoom != null) {
							if (myRoom.count == 2) {
								for (int j = 0; j < myRoom.InRoomUser.size(); j++) {
									UserService user = myRoom.InRoomUser.get(j);
									if (!(UserName.equals(user.UserName))) {
										ChatMsg error = new ChatMsg(user.UserName, "400", "����");
										WaitVec.add(user); // ������� ���� ���Ϳ� ������ �߰�
										SendUserListAll(); // ���� ��� ����
										user.WriteOneObject(error);
									}
								}
							}
						}
						WriteAllObject(cm);
//						System.out.println("������ ó�� �� list" + RoomNameList);
//						System.out.println("���� ��" + RoomVec.size());
//						System.out.println("���� ���" + WaitVec.size());
//						System.out.println("���� �ο�" + UserVec.size());
						ois.close();
						oos.close();
						client_socket.close();
						Logout(); // �������� ���� ��ü�� ���Ϳ��� �����
						break;
					} catch (Exception ee) {
						break;
					} // catch�� ��
				} // �ٱ� catch����
			} // while
		} // run
	}

	// ���� �� Ŭ����
	public class GameRoom {
		public String RoomName; // ���� �̸�
		Vector<UserService> InRoomUser; // �濡 ������ ������ ������ vector
		int count = 0; // ���� ���� ī��Ʈ

		GameRoom() { // ���ӹ� ������
			InRoomUser = new Vector<>();
		}

		// �� �̸� ����
		void setRoomName(String username) {
			this.RoomName = username;
		}

		// �� �̸� ���
		String getRoomName() {
			return this.RoomName;
		}
	}
}
