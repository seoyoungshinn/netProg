package Client;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.swing.JOptionPane;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class WaitingFrame extends JFrame implements MouseListener {

	private static final long serialVersionUID = 1L;
	int width = 800, height = 800; // 창 크기 지정

	private JLabel contentPane; // 배경 화면 등록 용
	private JTextField txtInput; // 채팅창 채팅 입력 칸
	public String UserName; // 유저 이름
	private JButton btnSend; // 채팅창 보내기 버튼

	private Socket socket; // 연결소켓
	private ObjectInputStream ois;
	private ObjectOutputStream oos;

	// 연결에 필요한 것들 가져오기
	Socket getSocket() {
		return this.socket;
	}

	ObjectInputStream getOis() {
		return this.ois;
	}

	ObjectOutputStream getOos() {
		return this.oos;
	}

	private JLabel lblUserName; // 유저 이름 표시
	private JTextPane textArea; // 채팅창
	private JTextPane textArea2; // 대기유저 목록창

	private JButton btnRoom; // 방 생성 버튼
	private JButton btnExit; // 종료 버튼

	private JScrollPane scrollPane2; // 방 목록 표시용
	private DefaultListModel<String> demoList = new DefaultListModel<String>(); // 디폴트 리스트 모델, 방 목록 표시용
	private JList<String> roomJList = new JList<String>(demoList); // 방 목록에 쓸 리스트

	private String ip_addr; // 아이피 주소
	private String port_no; // 포트 넘버

	static WaitingFrame wf; // WaitingFrame 접근용 객체
	private RoomFrame myRoom; // 나중에 프레임 변환에 사용할 룸 프레임 객체
	static GameStart gs; // GameStart 접근용 객체

	ChatMsg movingInfo; // 좌표 전달용 ChatMsg

	// 대기방 생성자
	public WaitingFrame(String username, String ip_addr, String port_no) {
		WaitingFrame wf = this; // 자기 자신
		this.ip_addr = ip_addr;
		this.port_no = port_no;

		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, width, height);
		contentPane = new JLabel(new ImageIcon("images/배경화면.png"));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		// 방 목록 값 조절
		roomJList.setModel(demoList);
		roomJList.setFont(new Font("맑은 고딕", Font.BOLD, 14));
		roomJList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		roomJList.addMouseListener(this);

		// 방 목록 용 스크롤
		scrollPane2 = new JScrollPane(roomJList);
		scrollPane2.setBounds(25, 45, 350, 280);
		contentPane.add(scrollPane2);

		// 방 생성 버튼
		btnRoom = new JButton("방 생성");
		btnRoom.setBackground(new Color(255, 153, 000));
		btnRoom.setForeground(Color.white);
		btnRoom.setFont(new Font("맑은 고딕", Font.BOLD, 14));
		btnRoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String roomName = String.format("%s의 방", UserName);
				ChatMsg msg = new ChatMsg(UserName, "500", roomName);
				SendObject(msg);
				myRoom = new RoomFrame(username, 1, ip_addr, port_no, wf);
				myRoom.setRoomName(roomName);
				setVisible(false); // WaitingFrame
			}
		});
		btnRoom.setBounds(25, 360, 160, 40);
		contentPane.add(btnRoom);

		// 종료 버튼
		btnExit = new JButton("종료");
		btnExit.setBackground(new Color(255, 153, 000));
		btnExit.setForeground(Color.white);
		btnExit.setFont(new Font("맑은 고딕", Font.BOLD, 14));
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ChatMsg msg = new ChatMsg(UserName, "400", "Bye");
				SendObject(msg);
				System.exit(0);
			}
		});
		btnExit.setBounds(215, 360, 160, 40);
		contentPane.add(btnExit);

		// 접속자 목록
		JScrollPane scrollPane3 = new JScrollPane();
		scrollPane3.setBounds(25, 455, 350, 285);
		contentPane.add(scrollPane3);

		// 접속자 목록 표시용
		textArea2 = new JTextPane();
		textArea2.insertIcon(new ImageIcon("images/배경화면"));
		textArea2.setEditable(true);
		textArea2.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
		scrollPane3.setViewportView(textArea2);

		// 채팅창
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(405, 45, 350, 650);
		contentPane.add(scrollPane);

		// 채팅창 표시용
		textArea = new JTextPane();
		textArea.setEditable(true);
		textArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
		scrollPane.setViewportView(textArea);

		// 채팅 입력창
		txtInput = new JTextField();
		txtInput.setBounds(470, 705, 210, 40);
		contentPane.add(txtInput);
		txtInput.setColumns(10);

		// 채팅 보내기 버튼
		btnSend = new JButton("보내기");
		btnSend.setBackground(new Color(255, 153, 000));
		btnSend.setForeground(Color.white);
		btnSend.setFont(new Font("맑은 고딕", Font.BOLD, 12));
		btnSend.setBounds(685, 705, 70, 40);
		contentPane.add(btnSend);

		// 이름 표시용 JLabel
		lblUserName = new JLabel("Name");
		lblUserName.setOpaque(true);
		lblUserName.setForeground(Color.WHITE);
		lblUserName.setBackground(new Color(255, 153, 000));
		lblUserName.setFont(new Font("맑은 고딕", Font.BOLD, 14));
		lblUserName.setHorizontalAlignment(SwingConstants.CENTER);
		lblUserName.setBounds(405, 705, 60, 40);
		contentPane.add(lblUserName);

		// 접속 안내 문구
		AppendText("User " + username + " connecting " + ip_addr + " " + port_no);
		UserName = username;
		lblUserName.setText(username);
		setVisible(true); // 보여라

		// 주고 받고
		try {
			socket = new Socket(ip_addr, Integer.parseInt(port_no)); // 소켓 연결

			oos = new ObjectOutputStream(socket.getOutputStream()); // output 스트림
			oos.flush();
			ois = new ObjectInputStream(socket.getInputStream()); // input 스트림

			ChatMsg obcm = new ChatMsg(UserName, "100", "Hello"); // WaitingFrame 생성시 로그인을 서버에 알린다
			SendObject(obcm);

			new ListenNetwork(wf).start(); // 네트워크 스레드 실행

			TextSendAction action = new TextSendAction(); // 채팅 보낼 때 쓸 이벤트
			btnSend.addActionListener(action);
			txtInput.addActionListener(action);
			txtInput.requestFocus(); // 텍스트 입력 포커스

		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
			AppendText("connect error");
		}

	}

	// 룸 목록 클릭 시 이벤트
	public void mouseClicked(MouseEvent click) {
		if (click.getClickCount() == 2) { // 두번누르면
			String name = (String) roomJList.getSelectedValue(); // 선택한 방 이름
			ChatMsg obcm = new ChatMsg(UserName, "501", name);
			SendObject(obcm);
		}
	}

	// 서버로부터 받은거 채팅창에 작성
	public void AppendText(String msg) {
		msg = msg.trim();
		StyledDocument doc = textArea.getStyledDocument();
		SimpleAttributeSet left = new SimpleAttributeSet();
		StyleConstants.setAlignment(left, StyleConstants.ALIGN_LEFT);
		StyleConstants.setForeground(left, Color.BLACK);
		// 다른 사람 채팅은 왼쪽에
		doc.setParagraphAttributes(doc.getLength(), 1, left, false);
		try {
			doc.insertString(doc.getLength(), msg + "\n", left);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		int len = textArea.getDocument().getLength();
		textArea.setCaretPosition(len);
	}

	// 자기가 쓴건 오른쪽에
	public void AppendTextR(String msg) {
		msg = msg.trim();

		StyledDocument doc = textArea.getStyledDocument();
		SimpleAttributeSet right = new SimpleAttributeSet();
		StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);
		StyleConstants.setForeground(right, Color.BLUE);

		doc.setParagraphAttributes(doc.getLength(), 1, right, false);
		try {
			doc.insertString(doc.getLength(), msg + "\n", right);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int len = textArea.getDocument().getLength();
		textArea.setCaretPosition(len);
	}

	// 유저리스트를 유저 목록 칸에 적는다
	public void AppendUser(String msg) {
		msg = msg.trim();

		textArea2.setText("");
		StyledDocument doc = textArea2.getStyledDocument();
		SimpleAttributeSet right = new SimpleAttributeSet();
		try {
			doc.insertString(doc.getLength(), msg, right);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		int len = textArea2.getDocument().getLength();
		textArea2.setCaretPosition(len);
	}

	// 서버로 부터 받아서 코드에 따라 처리
	class ListenNetwork extends Thread {
		private WaitingFrame wf;

		public ListenNetwork(WaitingFrame wf) {
			this.wf = wf;
		}

		public void run() {
			while (true) {
				try {
					Object obcm = null;
					String msg = null;
					ChatMsg cm;
					try {
						obcm = ois.readObject();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						break;
					}
					if (obcm == null) {
						break;
					}

					if (obcm instanceof ChatMsg) {
						cm = (ChatMsg) obcm;
						msg = String.format("[%s]\n%s", cm.UserName, cm.data);
					} else
						continue;
					switch (cm.code) {
					case "200": // 일반 채팅
						if (cm.UserName.equals(UserName))
							AppendTextR(msg); // 유저가 쓴거면 오른쪽에
						else
							AppendText(msg);
						break;
					case "300": // 참여자 목록
						AppendUser(cm.data);
						break;
					case "400": // 방에서 퇴장
						JOptionPane.showMessageDialog(contentPane, "상대방이 나갔습니다."); // 저장 알림창
						myRoom.setVisible(false);
						wf.setVisible(true);
						break;
					case "503": // 방끼리 채팅
						msg = String.format("[%s] %s", cm.UserName, cm.data);
						myRoom.AppendText2(msg);
						break;
					case "504": // 방 목록 갱신
						demoList.clear();
						String[] RoomNameList = cm.dm.split(("\\$"));
						for (String RoomName : RoomNameList) {
							demoList.addElement(RoomName);
						}
						break;
					case "505": // 사람이 꽉차서 입장 불가
						JOptionPane.showMessageDialog(contentPane, cm.data); // 저장 알림창
						break;
					case "506": // 상대방이 들어오면 방장의 화면 갱신
						RoomFrame.status = 1; // 상태를 1로 -> 게임시작 가능
						// 상대방의 이름 표시
						myRoom.player2name = new JLabel(cm.UserName);
						myRoom.player2name.setForeground(Color.white);
						myRoom.player2name.setHorizontalAlignment(JLabel.CENTER);
						myRoom.player2name.setBounds(242, 110, 48, 30);
						myRoom.contentPane2.add(myRoom.player2name);
						// 상대방의 사진 표시
						myRoom.player2 = new JLabel(new ImageIcon("images/woonie_front.png"));
						myRoom.player2.setBounds(242, 50, 48, 56);
						myRoom.contentPane2.add(myRoom.player2);
						myRoom.repaint(); // 추가하고 다시 그려서 갱신한다
						myRoom.AppendText2("상대가 입장하였습니다.");
						break;
					case "507": // 방에 참여 했을 때(다른 사람 방에 들어갔을 때) 화면 갱신
						setVisible(false);
						myRoom = new RoomFrame(UserName, 2, ip_addr, port_no, wf);
						// 방장의 이름을 상대 이름에 등록
						myRoom.player2name = new JLabel(cm.data);
						myRoom.player2name.setForeground(Color.white);
						myRoom.player2name.setHorizontalAlignment(JLabel.CENTER);
						String RoomName = String.format("%s의 방", cm.data);
						myRoom.setRoomName(RoomName);
						myRoom.player2name.setBounds(242, 110, 48, 30);
						myRoom.contentPane2.add(myRoom.player2name);
						// 상대 사진 등록
						myRoom.player2 = new JLabel(new ImageIcon("images/woonie_front.png"));
						myRoom.player2.setBounds(242, 50, 48, 56);
						myRoom.contentPane2.add(myRoom.player2);
						myRoom.repaint(); // 다시 그려라
						myRoom.AppendText2("방에 입장하였습니다.");
						break;
					case "508": // 방 공지 전달
						msg = String.format("%s", cm.data);
						myRoom.AppendText2(msg);
						break;
					case "509":
						myRoom.setVisible(false);
						wf.setVisible(true);
						break;
					case "600": // 시작
						myRoom.setVisible(false); // 방 화면을 지우고
						gs = new GameStart(wf, UserName, myRoom); // GameStart 생성 후 화면 전환
						break;
					}
				} catch (IOException e) {
					AppendText("ois.readObject() error");
					try {
						ois.close();
						oos.close();
						socket.close();
						break;
					} catch (Exception ee) {
						break;
					}
				}
			}
		}
	}

	// keyboard enter key 치면 서버로 전송
	class TextSendAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Send button을 누르거나 메시지 입력하고 Enter key 치면
			if (e.getSource() == btnSend || e.getSource() == txtInput) {
				String msg = null;
				msg = txtInput.getText();
				SendMessage(msg);
				txtInput.setText(""); // 메시지를 보내고 작성창을 비운다
				txtInput.requestFocus(); // 메세지를 보내고 커서를 다시 텍스트 필드로 위치시킨다
				if (msg.contains("/exit")) // 종료처리
					System.exit(0);
			}

		}
	}

	// 서버에 network으로 전송
	public void SendMessage(String msg) {
		try {
			ChatMsg obcm = new ChatMsg(UserName, "200", msg); // 채팅 전달
			oos.writeObject(obcm);
		} catch (IOException e) {
			AppendText("oos.writeObject() error");
			try {
				ois.close();
				oos.close();
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(0);
			}
		}
	}

	// 서버에 Object로 보내기
	public void SendObject(ChatMsg ob) { // 서버로 메세지를 보내는 메소드
		try {
			oos.writeObject(ob);

		} catch (IOException e) {
			AppendText("SendObject Error");
		}
	}

	// 방 끼리 채팅에 사용
	public void SendMessage2(String msg) {
		try {
			ChatMsg obcm = new ChatMsg(UserName, "503", msg);
			oos.writeObject(obcm);
		} catch (IOException e) {
			myRoom.AppendText2("oos.writeObject() error");
			try {
				ois.close();
				oos.close();
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(0);
			}
		}
	}

	// 방 끼리 알림 공지용
	public void SendMessage3(String msg) {
		try {

			ChatMsg obcm = new ChatMsg(UserName, "508", msg);
			oos.writeObject(obcm);
		} catch (IOException e) {
			myRoom.AppendText2("oos.writeObject() error");
			try {
				ois.close();
				oos.close();
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(0);
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}
}
