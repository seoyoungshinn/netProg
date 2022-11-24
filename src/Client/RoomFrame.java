package Client;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

class RoomFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	public JLabel contentPane2; // 배경화면 JLabel
	private JTextField txtInput2; // 방 채팅창
	private String UserName; // 방의 유저 이름
	public JButton startGame; // 게임 시작 버튼
	public JButton exitRoom; // 게임 나가기 버튼
	private JButton btnSend2; // 방 채팅 보내기 버튼
	public JLabel waitGame; // 방에 참여한 사람에게 대기 안내
	private JTextPane chatArea; // 룸 채팅창

	public JLabel player1; // 플레이어 1 사진
	public JLabel player2; // 플레이어 2 사진
	public JLabel player1name; // 플레이어 1 이름 표시
	public JLabel player2name; // 플레이어 2 이름 표시

	private String RoomName; // 방 이름
	
	static WaitingFrame wf;
	static RoomFrame rf;
	
	int master;

	static int status = 0; // 상태에 따른 게임시작 여부 표현

	void setRoomName(String RoomName) {
		this.RoomName = RoomName;
	}

	String getRoomName() {
		return this.RoomName;
	}

	public RoomFrame(String username, int master, String ip_addr, String port_no, WaitingFrame wf) {
		RoomFrame.wf = wf;
		this.master = master; // 방장, 참여자 구분 변수
		this.UserName = username; // 매개변수로 받은 이름 등록
		setResizable(false); // 사이즈 재설정 불가
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, 400, 400); // 화면 크기 조정
		contentPane2 = new JLabel(new ImageIcon("images/배경화면2.png"));
		setContentPane(contentPane2); 
		contentPane2.setLayout(null);
		
		chatArea = new JTextPane(); // 방 채팅창
		chatArea.setEditable(true);
		chatArea.setFont(new Font("굴림", Font.PLAIN, 12));

		player1 = new JLabel(new ImageIcon("images/bazzi_front.png"));
		// 플레이어 1의 사진은 항상 나와서 그냥 출력
		player1.setBounds(110, 50, 48, 56);
		contentPane2.add(player1);

		// 플레이어 2의 사진은 만들어 놓기만 하고 띄우진 않는다 (상대방이 들어오면 띄운다)
		player2 = new JLabel(new ImageIcon("images/woonie_front.png"));
		player2.setBounds(242, 50, 48, 56);
		setVisible(true); // 보여라

		// 매개변수로 받은 마스터 값이 1 이면 방장
		if (master == 1) {
			player1name = new JLabel(UserName); // 방 이름 그대로 사용
			player1name.setForeground(Color.white);
			player1name.setHorizontalAlignment(JLabel.CENTER);
			player1name.setBounds(110, 110, 48, 30);
			contentPane2.add(player1name);

			// 게임 시작 버튼
			startGame = new JButton(new ImageIcon("images/start.png"));
			startGame.addActionListener(new ActionListener() { // 시작 버튼 이벤트
				public void actionPerformed(ActionEvent e) {
					// 상대가 없는 상태면
					if (status == 0)
						wf.SendMessage3("상대가 없습니다..");
					// 상대가 있는 상태면
					else if (status == 1) {
						wf.SendMessage3("게임시작 !!");
						ChatMsg start = new ChatMsg("rf.getRoomName()", "600", "game start");
						wf.SendObject(start);
					}
				}
			});
			startGame.setBounds(110, 150, 180, 50);
			contentPane2.add(startGame);

			// 게임 나가기 버튼
			exitRoom = new JButton("test");
			exitRoom.addActionListener(new ActionListener() { // 시작 버튼 이벤트
				public void actionPerformed(ActionEvent e) {
					ChatMsg out = new ChatMsg(UserName,"510", getRoomName());
					wf.SendObject(out);
				}
			});
			exitRoom.setBounds(320, 150, 50, 50);
			contentPane2.add(exitRoom);

			// 매겨변수로 받은 마스터값이 2면 참여자
		} else if (master == 2) {
			// 참여자에겐 대기중 라벨 띄움
			waitGame = new JLabel("대기중 입니다..");
			waitGame.setFont(new Font("맑은 고딕", Font.BOLD, 14));
			waitGame.setHorizontalAlignment(JLabel.CENTER);
			waitGame.setOpaque(true);
			waitGame.setBackground(new Color(255,153,000));
			waitGame.setForeground(Color.white);
			Border border = BorderFactory.createLineBorder(Color.BLACK, 1);
			waitGame.setBorder(border);
			waitGame.setBounds(110, 150, 180, 50);
			contentPane2.add(waitGame);

			// 참여자의 이름을 플레이어 1에 등록
			player1name = new JLabel(UserName);
			player1name.setForeground(Color.white);
			player1name.setHorizontalAlignment(JLabel.CENTER);
			player1name.setBounds(110, 110, 48, 30);
			contentPane2.add(player1name);
		}

		// 방 채팅창
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(44, 210, 300, 110);
		contentPane2.add(scrollPane);
		scrollPane.setViewportView(chatArea); 

		// 채팅 입력창
		txtInput2 = new JTextField();
		txtInput2.setBounds(44, 325, 230, 35);
		contentPane2.add(txtInput2);
		txtInput2.setColumns(10);

		// 채팅 보내기 버튼
		btnSend2 = new JButton("보내기");
		btnSend2.setBackground(new Color(255,153,000));
		btnSend2.setForeground(Color.white);
		btnSend2.setFont(new Font("맑은 고딕", Font.BOLD, 9));
		btnSend2.setBounds(280, 325, 64, 35);
		contentPane2.add(btnSend2);

		try {
			TextSendActionInRoom action = new TextSendActionInRoom();
			btnSend2.addActionListener(action);
			txtInput2.addActionListener(action);
			txtInput2.requestFocus();
		} catch (NumberFormatException e) {
			e.printStackTrace();
			wf.AppendText("connect error");
		}
	}

	// 방 안에서 채팅 보낼 때 액션
	class TextSendActionInRoom implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Send button을 누르거나 메시지 입력하고 Enter key 치면
			if (e.getSource() == btnSend2 || e.getSource() == txtInput2) {
				String msg = null;
				msg = txtInput2.getText();
				wf.SendMessage2(msg);
				txtInput2.setText(""); // 메시지를 보내고 작성창을 비운다
				txtInput2.requestFocus(); // 메세지를 보내고 커서를 다시 텍스트 필드로 위치시킨다
				if (msg.contains("/exit")) // 종료처리
					System.exit(0);
			}
		}
	}
	
	public void AppendText2(String msg) {  // 채팅창에 출력
		StyledDocument doc = chatArea.getStyledDocument();
		SimpleAttributeSet left = new SimpleAttributeSet();
		StyleConstants.setAlignment(left, StyleConstants.ALIGN_LEFT);
		StyleConstants.setForeground(left, Color.BLACK);
		
	    doc.setParagraphAttributes(doc.getLength(), 1, left, false);
		try {
			doc.insertString(doc.getLength(), msg+"\n", left );
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		int len = chatArea.getDocument().getLength();
		chatArea.setCaretPosition(len);
	}	
}
