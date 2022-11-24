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
	public JLabel contentPane2; // ���ȭ�� JLabel
	private JTextField txtInput2; // �� ä��â
	private String UserName; // ���� ���� �̸�
	public JButton startGame; // ���� ���� ��ư
	public JButton exitRoom; // ���� ������ ��ư
	private JButton btnSend2; // �� ä�� ������ ��ư
	public JLabel waitGame; // �濡 ������ ������� ��� �ȳ�
	private JTextPane chatArea; // �� ä��â

	public JLabel player1; // �÷��̾� 1 ����
	public JLabel player2; // �÷��̾� 2 ����
	public JLabel player1name; // �÷��̾� 1 �̸� ǥ��
	public JLabel player2name; // �÷��̾� 2 �̸� ǥ��

	private String RoomName; // �� �̸�
	
	static WaitingFrame wf;
	static RoomFrame rf;
	
	int master;

	static int status = 0; // ���¿� ���� ���ӽ��� ���� ǥ��

	void setRoomName(String RoomName) {
		this.RoomName = RoomName;
	}

	String getRoomName() {
		return this.RoomName;
	}

	public RoomFrame(String username, int master, String ip_addr, String port_no, WaitingFrame wf) {
		RoomFrame.wf = wf;
		this.master = master; // ����, ������ ���� ����
		this.UserName = username; // �Ű������� ���� �̸� ���
		setResizable(false); // ������ �缳�� �Ұ�
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(0, 0, 400, 400); // ȭ�� ũ�� ����
		contentPane2 = new JLabel(new ImageIcon("images/���ȭ��2.png"));
		setContentPane(contentPane2); 
		contentPane2.setLayout(null);
		
		chatArea = new JTextPane(); // �� ä��â
		chatArea.setEditable(true);
		chatArea.setFont(new Font("����", Font.PLAIN, 12));

		player1 = new JLabel(new ImageIcon("images/bazzi_front.png"));
		// �÷��̾� 1�� ������ �׻� ���ͼ� �׳� ���
		player1.setBounds(110, 50, 48, 56);
		contentPane2.add(player1);

		// �÷��̾� 2�� ������ ����� ���⸸ �ϰ� ����� �ʴ´� (������ ������ ����)
		player2 = new JLabel(new ImageIcon("images/woonie_front.png"));
		player2.setBounds(242, 50, 48, 56);
		setVisible(true); // ������

		// �Ű������� ���� ������ ���� 1 �̸� ����
		if (master == 1) {
			player1name = new JLabel(UserName); // �� �̸� �״�� ���
			player1name.setForeground(Color.white);
			player1name.setHorizontalAlignment(JLabel.CENTER);
			player1name.setBounds(110, 110, 48, 30);
			contentPane2.add(player1name);

			// ���� ���� ��ư
			startGame = new JButton(new ImageIcon("images/start.png"));
			startGame.addActionListener(new ActionListener() { // ���� ��ư �̺�Ʈ
				public void actionPerformed(ActionEvent e) {
					// ��밡 ���� ���¸�
					if (status == 0)
						wf.SendMessage3("��밡 �����ϴ�..");
					// ��밡 �ִ� ���¸�
					else if (status == 1) {
						wf.SendMessage3("���ӽ��� !!");
						ChatMsg start = new ChatMsg("rf.getRoomName()", "600", "game start");
						wf.SendObject(start);
					}
				}
			});
			startGame.setBounds(110, 150, 180, 50);
			contentPane2.add(startGame);

			// ���� ������ ��ư
			exitRoom = new JButton("test");
			exitRoom.addActionListener(new ActionListener() { // ���� ��ư �̺�Ʈ
				public void actionPerformed(ActionEvent e) {
					ChatMsg out = new ChatMsg(UserName,"510", getRoomName());
					wf.SendObject(out);
				}
			});
			exitRoom.setBounds(320, 150, 50, 50);
			contentPane2.add(exitRoom);

			// �Űܺ����� ���� �����Ͱ��� 2�� ������
		} else if (master == 2) {
			// �����ڿ��� ����� �� ���
			waitGame = new JLabel("����� �Դϴ�..");
			waitGame.setFont(new Font("���� ���", Font.BOLD, 14));
			waitGame.setHorizontalAlignment(JLabel.CENTER);
			waitGame.setOpaque(true);
			waitGame.setBackground(new Color(255,153,000));
			waitGame.setForeground(Color.white);
			Border border = BorderFactory.createLineBorder(Color.BLACK, 1);
			waitGame.setBorder(border);
			waitGame.setBounds(110, 150, 180, 50);
			contentPane2.add(waitGame);

			// �������� �̸��� �÷��̾� 1�� ���
			player1name = new JLabel(UserName);
			player1name.setForeground(Color.white);
			player1name.setHorizontalAlignment(JLabel.CENTER);
			player1name.setBounds(110, 110, 48, 30);
			contentPane2.add(player1name);
		}

		// �� ä��â
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(44, 210, 300, 110);
		contentPane2.add(scrollPane);
		scrollPane.setViewportView(chatArea); 

		// ä�� �Է�â
		txtInput2 = new JTextField();
		txtInput2.setBounds(44, 325, 230, 35);
		contentPane2.add(txtInput2);
		txtInput2.setColumns(10);

		// ä�� ������ ��ư
		btnSend2 = new JButton("������");
		btnSend2.setBackground(new Color(255,153,000));
		btnSend2.setForeground(Color.white);
		btnSend2.setFont(new Font("���� ���", Font.BOLD, 9));
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

	// �� �ȿ��� ä�� ���� �� �׼�
	class TextSendActionInRoom implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Send button�� �����ų� �޽��� �Է��ϰ� Enter key ġ��
			if (e.getSource() == btnSend2 || e.getSource() == txtInput2) {
				String msg = null;
				msg = txtInput2.getText();
				wf.SendMessage2(msg);
				txtInput2.setText(""); // �޽����� ������ �ۼ�â�� ����
				txtInput2.requestFocus(); // �޼����� ������ Ŀ���� �ٽ� �ؽ�Ʈ �ʵ�� ��ġ��Ų��
				if (msg.contains("/exit")) // ����ó��
					System.exit(0);
			}
		}
	}
	
	public void AppendText2(String msg) {  // ä��â�� ���
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
