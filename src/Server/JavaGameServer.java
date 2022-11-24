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
	private ServerSocket socket; // 서버소켓
	private Socket client_socket; // accept() 에서 생성된 client 소켓
	private Vector<UserService> UserVec = new Vector<UserService>(); // 연결된 전체 사용자를 저장할 벡터
	private Vector<UserService> WaitVec = new Vector<UserService>(); // 대기방 사용자 저장할 벡터
	private Vector<GameRoom> RoomVec = new Vector<GameRoom>(); // 방 목록 저장할 벡터
	private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의

	// 메인
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

	// 서버 생성자
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
				btnServerStart.setEnabled(false); // 서버를 더이상 실행시키지 못 하게 막는다
				txtPortNumber.setEnabled(false); // 더이상 포트번호 수정못 하게 막는다
				AcceptServer accept_server = new AcceptServer();
				accept_server.start();
			}
		});
		btnServerStart.setBounds(12, 356, 300, 35);
		contentPane.add(btnServerStart);
	}

	// 새로운 참가자 accept() 하고 user thread를 새로 생성한다.
	class AcceptServer extends Thread {
		public void run() {
			while (true) { // 사용자 접속을 계속해서 받기 위해 while문
				try {
					AppendText("Waiting new clients ...");
					client_socket = socket.accept(); // accept가 일어나기 전까지는 무한 대기중
					AppendText("새로운 참가자 from " + client_socket);
					// User 당 하나씩 Thread 생성
					UserService new_user = new UserService(client_socket, socket);
					UserVec.add(new_user); // 새로운 참가자 배열에 추가
					WaitVec.add(new_user); // 대기중인 유저 벡터에 참가자 추가
					new_user.start(); // 만든 객체의 스레드 실행

					AppendText("현재 참가자 수 " + UserVec.size());
				} catch (IOException e) {
					AppendText("accept() error");
				}
			}
		}
	}

	// 서버창에 출력
	public void AppendText(String str) {
		textArea.append(str + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	// 서버창에 채팅 객체 출력
	public void AppendObject(ChatMsg msg) {
		textArea.append("code = " + msg.code + "\n");
		textArea.append("id = " + msg.UserName + "\n");
		textArea.append("data = " + msg.data + "\n");
		textArea.append("x = " + msg.x);
		textArea.append("    y = " + msg.y + "\n");
		textArea.append("move = " + msg.move + "\n" + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	// User 당 생성되는 Thread
	// Read One 에서 대기 -> Write All
	class UserService extends Thread {

		private ObjectInputStream ois;
		private ObjectOutputStream oos;
		private Socket client_socket;

		public String UserName = ""; // 이름 저장
		public String UserStatus; // 유저 상태
		public String RoomNameList = ""; // 방 리스트를 보낼 때 쓸 문자열

		GameRoom myRoom; // 입장한 방 객체를 저장

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
			AppendText("새로운 참가자 " + UserName + " 입장.");
			WriteOne("Welcome to Java chat server\n");
			WriteOne(UserName + "님 환영합니다.\n"); // 연결된 사용자에게 정상접속을 알림
			String msg = "[" + UserName + "]님이 입장 하였습니다.\n";
			WriteOthers(msg); // 아직 user_vc에 새로 입장한 user는 포함되지 않았다.
			RoomNameList = "";
			for (int i = 0; i < RoomVec.size(); i++) {
				RoomNameList += (RoomVec.elementAt(i).RoomName + "$");
			}
			ChatMsg cm = new ChatMsg(UserName, "504", "방 목록 갱신", RoomNameList);
			WriteOneObject(cm); // 새로 들어온 사람에게 방 리스트 갱신에 필요한 값 전달
		}

		public void Logout() {
			String msg = "[" + UserName + "]님이 퇴장 하였습니다.\n";
			UserVec.removeElement(this); // Logout한 현재 객체를 벡터에서 지운다
			WaitVec.removeElement(this);
			WriteAll(msg); // 나를 제외한 다른 User들에게 전송
			AppendText("사용자 " + "[" + UserName + "] 퇴장. 현재 참가자 수 " + UserVec.size());
			SendUserListAll(); // 유저 목록 갱신
		}

		// 모든 User들에게 방송. 각각의 UserService Thread의 WriteONe() 을 호출한다.
		public void WriteAll(String str) {
			for (int i = 0; i < UserVec.size(); i++) {
				UserService user = (UserService) UserVec.elementAt(i);
				if (user.UserStatus == "O")
					user.WriteOne(str);
			}
		}

		// 모든 User들에게 Object를 방송. 채팅 message와 image object를 보낼 수 있다
		public void WriteAllObject(Object ob) {
			for (int i = 0; i < UserVec.size(); i++) {
				UserService user = (UserService) UserVec.elementAt(i);
				if (user.UserStatus == "O")
					user.WriteOneObject(ob);
			}
		}

		// 방의 유저들에게 Object 발송
		public void WriteRoomObject(Object ob) {
			for (int i = 0; i < myRoom.InRoomUser.size(); i++) {
				UserService user = (UserService) myRoom.InRoomUser.elementAt(i);
				user.WriteOneObject(ob);
			}
		}

		// 방장과 참여자에게 서로의 이름을 알려준다
		public void InformElseName(Object ob) {
			UserService user = (UserService) myRoom.InRoomUser.elementAt(0);
			user.WriteOneObject(ob); // 방장에게 참여자 이름 보냄
			String owner = user.UserName;
			user = (UserService) myRoom.InRoomUser.elementAt(1);
			ChatMsg player2m = new ChatMsg(user.UserName, "507", owner);
			user.WriteOneObject(player2m); // 참여자에게 방장 이름 보냄
		}

		// 나를 제외한 User들에게 방송. 각각의 UserService Thread의 WriteONe() 을 호출한다.
		public void WriteOthers(String str) {
			for (int i = 0; i < UserVec.size(); i++) {
				UserService user = (UserService) UserVec.elementAt(i);
				if (user != this && user.UserStatus == "O")
					user.WriteOne(str);
			}
		}

		// Windows 처럼 message 제외한 나머지 부분은 NULL 로 만들기 위한 함수
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

		// UserService Thread가 담당하는 Client 에게 1:1 전송
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
				Logout(); // 에러가난 현재 객체를 벡터에서 지운다
			}
		}

		// 모두에게 대기자 명단을 보낸다
		public void SendUserListAll() {
			for (int i = 0; i < UserVec.size(); i++) {
				UserService user = (UserService) UserVec.elementAt(i);
				user.SendUserList();
			}
		}

		// 대기자 명단을 보낸다
		public void SendUserList() {
			String users = "";
			// 반목문으로 대기자 명단을 만듬
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
				Logout(); // 에러가난 현재 객체를 벡터에서 지운다
			}
		}

		// 대기자들에게 Object 보냄
		public void WriteWaitObject(Object ob) {
			for (int i = 0; i < WaitVec.size(); i++) {
				UserService user = (UserService) WaitVec.elementAt(i);
				user.WriteOneObject(ob);
			}
		}

		// 귓속말 전송
		public void WritePrivate(String msg) {
			try {
				ChatMsg obcm = new ChatMsg("귓속말", "200", msg);
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
				Logout(); // 에러가난 현재 객체를 벡터에서 지운다
			}
		}

		// Object 보냄
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
			while (true) { // 사용자 접속을 계속해서 받기 위해 while문
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
					// 로그인
					if (cm.code.matches("100")) {
						UserName = cm.UserName;
						UserStatus = "O"; // Online 상태
						Login();
						// 유저 리스트 갱신
						SendUserListAll();
					}

					// 200 채팅 메시지 관련
					else if (cm.code.matches("200")) {
						msg = String.format("[%s] %s", cm.UserName, cm.data);
						AppendText(msg); // server 화면에 출력
						String[] args = msg.split(" "); // 단어들을 분리한다.
						if (args.length == 1) { // Enter key 만 들어온 경우 Wakeup 처리만 한다.
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
						} else if (args[1].matches("/to")) { // 귓속말
							for (int i = 0; i < UserVec.size(); i++) {
								UserService user = (UserService) UserVec.elementAt(i);
								if (user.UserName.matches(args[2]) && user.UserStatus.matches("O")) {
									String msg2 = "";
									for (int j = 3; j < args.length; j++) {// 실제 message 부분
										msg2 += args[j];
										if (j < args.length - 1)
											msg2 += " ";
									}
									// /to 빼고.. [귓속말] [user1] Hello user2..
									user.WritePrivate(args[0] + " " + msg2 + "\n");
									// user.WriteOne("[귓속말] " + args[0] + " " + msg2 + "\n");
									break;
								}
							}
						} else { // 일반 채팅 메시지
							UserStatus = "O";
							// WriteAll(msg + "\n"); // Write All
							WriteAllObject(cm);
						}
					} else if (cm.code.matches("400")) {
						Logout();
					}

					// 500 방 생성
					else if (cm.code.matches("500")) {
						myRoom = new GameRoom(); // 방 객체 생성 후 myRoom에 초기화
						myRoom.setRoomName(cm.data); // 방 이름 설정
						myRoom.count++; // 방 인원수 증가
						RoomVec.add(myRoom); // room 벡터에 방금 만든 myRoom 추가
						myRoom.InRoomUser.add(this); // myRoom의 접속인원 벡터에 클라이언트 추가
						WaitVec.remove(this); // 대기실 인원에서 클라이언트 삭제
						SendUserListAll(); // 대기방 인원 리스트 갱신
						cm.code = "504";
						RoomNameList = "";
						for (int i = 0; i < RoomVec.size(); i++) {
							RoomNameList += (RoomVec.elementAt(i).RoomName + "$");
						}
						cm.data = myRoom.RoomName;
						cm.dm = RoomNameList;
						WriteAllObject(cm);
					}

					// 501 방입장
					else if (cm.code.matches("501")) {
						for (int i = 0; i < RoomVec.size(); i++) { // 생성된 방의 개수만큼 반복
							GameRoom r = RoomVec.get(i); // 반복문을 돌면서 i번째 방 객체 얻어옴
							if (r.RoomName.equals(cm.data)) { // 방 제목이 같으면
								if (r.count < 2) { // 방 인원수가 2명보다 적을 때 입장
									myRoom = RoomVec.get(i); // myRoom에 두 조건이 맞는 i번째 room을 초기화
									myRoom.count++; // 방의 인원수 하나 추가
									WaitVec.remove(this); // 대기실 접속 인원에서 클라이언트 삭제
									myRoom.InRoomUser.add(this); // myRoom의 접속 인원에 클라이언트 추가
									// 방 입장 시 처리
									cm.code = "506";
									cm.data = "방에 입장";
									InformElseName(cm); // 방장에게 참여자 이름을 보냄
									SendUserListAll(); // 대기자 명단 갱신
								} else { // 방 인원수가 2명 이상이므로 입장 실패
									cm.code = "505";
									cm.data = "선택한 방 인원이 꽉 찼습니다.";
									WriteOneObject(cm);
								}
							}
						}
					}
					// 503 방 채팅 메시지 관련
					else if (cm.code.matches("503")) {
						msg = String.format("[%s] %s", cm.UserName, cm.data);
						AppendObject(cm);
						// 이 방 안에 사람에게만 보낸다.
						WriteOneObject(cm);
					}

					// 508 방 공지 관련
					else if (cm.code.matches("508")) {
						msg = String.format("[%s] %s", cm.UserName, cm.data);
						AppendObject(cm);
						// 방 안에 사람에게만 보낸다.
						WriteRoomObject(cm);
					}

					// 방 종료
					else if (cm.code.matches("510")) { // 나가기
						AppendObject(cm);
						for (int i = 0; i < RoomVec.size(); i++) {
							GameRoom r = RoomVec.get(i); // 반복문을 돌면서 i번째 방 객체 얻어옴
							if (r.RoomName.equals(cm.data)) {
								AppendText("방 인원" + r.count);
								if (r.count == 2) { // 방에 혼자일때 나가면
									for (int j = 0; j < r.InRoomUser.size(); j++) {
										ChatMsg out = new ChatMsg(UserName, "509", "back");
										WriteRoomObject(out);
										WaitVec.add(r.InRoomUser.get(j));
									}
									RoomVec.removeElement(r); // 방 삭제

									RoomNameList = "";
									for (int j = 0; j < RoomVec.size(); j++) {
										RoomNameList += (RoomVec.elementAt(j).RoomName + "$");
									}
									System.out.println("나가기 처리 후 list" + RoomNameList);
									cm.code = "504";
									cm.data = myRoom.RoomName;
									cm.dm = RoomNameList;
									// 방 목록 갱신
									WriteAllObject(cm);
									SendUserListAll(); // 대기방 인원 갱신
								}
							}
						}
					}

					// 600 게임 시작
					else if (cm.code.matches("600")) {
						if (myRoom.count == 2) {
							for (int i = 0; i < myRoom.InRoomUser.size(); i++) { // myRoom의 인원수만큼 반복
								ChatMsg start = new ChatMsg(myRoom.InRoomUser.get(i).UserName, "600", myRoom.RoomName);
								UserService user = myRoom.InRoomUser.get(i); // 방의 유저 들에게 시작 신호
								user.WriteOneObject(start);
								AppendText(user.UserName + "에게 보냄");
							}
						}
					}

					// 좌표를 받아서 상대에게 전달한다
					else if (cm.code.matches("601")) {
						AppendText("신호 확인");
						for (int i = 0; i < myRoom.InRoomUser.size(); i++) {
							if (!(myRoom.InRoomUser.get(i).UserName.equals(UserName))) {
								AppendText(UserName + "가" + myRoom.InRoomUser.get(i).UserName + "에게 보냈다");
								AppendText(cm.code + cm.move + cm.x + cm.y);
								UserService user = myRoom.InRoomUser.get(i); // 방의 유저 들에게 시작 신호
								user.WriteOneObject(cm);
							}
						}
					}

					// 602번 물풍선 좌표 전달
					else if (cm.code.matches("602")) {
						for (int i = 0; i < myRoom.InRoomUser.size(); i++) {
							if (!(myRoom.InRoomUser.get(i).UserName.equals(UserName))) {
								AppendText(UserName + "가" + myRoom.InRoomUser.get(i).UserName + "에게 보냈다");
								AppendText(cm.code + cm.move + cm.x + cm.y);
								UserService user = myRoom.InRoomUser.get(i); // 방의 유저 들에게 시작 신호
								user.WriteOneObject(cm);
							}
						}
					}

					// 400 종료
					else if (cm.code.matches("400")) { // logout message 처리
						Logout();
						break;
					} else { // 300, 500, ... 기타 object는 모두 방송한다.
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
						ChatMsg cm = new ChatMsg(UserName, "504", "방 목록 갱신", RoomNameList);
						// 방 목록 갱신
						if (myRoom != null) {
							if (myRoom.count == 2) {
								for (int j = 0; j < myRoom.InRoomUser.size(); j++) {
									UserService user = myRoom.InRoomUser.get(j);
									if (!(UserName.equals(user.UserName))) {
										ChatMsg error = new ChatMsg(user.UserName, "400", "에러");
										WaitVec.add(user); // 대기중인 유저 벡터에 참가자 추가
										SendUserListAll(); // 유저 목록 갱신
										user.WriteOneObject(error);
									}
								}
							}
						}
						WriteAllObject(cm);
//						System.out.println("나가기 처리 후 list" + RoomNameList);
//						System.out.println("남은 방" + RoomVec.size());
//						System.out.println("남은 대기" + WaitVec.size());
//						System.out.println("남은 인원" + UserVec.size());
						ois.close();
						oos.close();
						client_socket.close();
						Logout(); // 에러가난 현재 객체를 벡터에서 지운다
						break;
					} catch (Exception ee) {
						break;
					} // catch문 끝
				} // 바깥 catch문끝
			} // while
		} // run
	}

	// 게임 방 클래스
	public class GameRoom {
		public String RoomName; // 방의 이름
		Vector<UserService> InRoomUser; // 방에 접속한 유저를 저장할 vector
		int count = 0; // 접속 유저 카운트

		GameRoom() { // 게임방 생성자
			InRoomUser = new Vector<>();
		}

		// 방 이름 설정
		void setRoomName(String username) {
			this.RoomName = username;
		}

		// 방 이름 얻기
		String getRoomName() {
			return this.RoomName;
		}
	}
}
