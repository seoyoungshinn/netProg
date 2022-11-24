package Client;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

public class GameStart extends JFrame implements KeyListener, MouseListener, Runnable {

	private static final long serialVersionUID = 1L;

	public Thread thread;

	// keyboard 제어를 위한 변수
	boolean keyU = false;
	boolean keyD = false;
	boolean keyL = false;
	boolean keyR = false;

	// 상태변수
	boolean die = false;
	boolean bombAvailable = true;

	// myMove, yourMove에 쓸 StringData
	final String UP = "up";
	final String DOWN = "down";
	final String RIGHT = "right";
	final String LEFT = "left";

	// 초기화시 캐릭터는 앞을 쳐다보고 있음
	public String myMove;
	public String yourMove;

	// 좌표를 나타낼 변수들
	public int myX, myY, yourX, yourY;
	int bombX, bombY;
	int bx, by;
	int x, y;
	int yourBx, yourBy;

	// 기본 속도
	public int speed = 10;

	// map state
	final String BROWNBLOCK = "BROWNBLOCK";
	final String PINKBLOCK = "PINKBLOCK";
	final String ITEM = "ITEM";
	final String FREE = "FREE";
	final String BUMB = "BUMB";

	// 아이템 변수
//	private Vector<JLabel> item = new Vector<JLabel>();
//	private ArrayList<JLabel> itemlist = new ArrayList<JLabel>();
//	private ImageIcon[] item2 = { new ImageIcon("images/speed.png"), null, null, null };
	ImageIcon item3;
	JLabel itemLabel;

	private JLabel contentPane;

	GameScreen gamescreen; // Canvas객체

	int gScreenWidth = 615; // 게임 화면 너비
	int gScreenHeight = 645; // 게임 화면 높이

	Image mapBG = new ImageIcon("Images/mapbg1.png").getImage(); // 게임1배경

	boolean roof = true;// 스레드 루프 정보
	public MapInfo mapInfo = new MapInfo(15);

	WaitingFrame wf;
	String UserName;
	RoomFrame rf;
	int master;

	GameThread gt;
	ChatMsg movingInfo;

	// GameStart 생성자
	public GameStart(WaitingFrame wf, String UserName, RoomFrame rf) {

		gt = new GameThread(wf); // 게임 스레드 시작
		gt.start();

		// master 변수에 따라 방장 참여자 좌표 조정
		if (rf.master == 1) {
			myX = 500;
			myY = 500;
			yourX = 100;
			yourY = 100;
		} else if (rf.master == 2) {
			myX = 100;
			myY = 100;
			yourX = 500;
			yourY = 500;
		}

		this.wf = wf;
		this.rf = rf;
		this.UserName = UserName;

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, gScreenWidth, gScreenHeight); // 화면 크기 조절
		setLocationRelativeTo(null);
		contentPane = new JLabel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);

		setContentPane(contentPane);
		setResizable(true); // 윈도우 크기 변경 불가
		addKeyListener(this);// 키 입력 이벤트 리스너 활성화

		setVisible(true); // 보여라

		gamescreen = new GameScreen(this);// 화면 묘화를 위한 캔버스 객체
		gamescreen.setBounds(0, 0, gScreenWidth, gScreenHeight);
		add(gamescreen);// Canvas 객체를 프레임에 올린다

		this.setFocusable(true);
		this.requestFocus();

		mapSetting(mapInfo); // 맵의 값 설정
		initialize(); // 시작 전 초기화

		thread = new Thread(this); // GameStart 스레드 시작
		thread.run();

		// 디버깅 용
//		for (int i = 0; i < 15; i++) {
//			for (int j = 0; j < 15; j++) {
//				System.out.print(
//						"(" + mapInfo.map[i][j].x + "," + mapInfo.map[i][j].y + ")" + mapInfo.map[i][j].state + "   ");
//			}
//			System.out.println();
//		}
	}// End of GameStart(생성자)

	///////////////////////////////////////////////////////////////
	// initializing Function //
	///////////////////////////////////////////////////////////////

	public void mapSetting(MapInfo mapInfo) { // 맵 정보 초기화
		// 가생이 갈색 블록 추가
		for (int i = 0; i < 15; i++) {
			for (int j = 0; j < 15; j++) {
				if (i == 0 || i == 14 || j == 0 || j == 14)
					mapInfo.map[i][j].state = BROWNBLOCK;
			} // End of inner for
		} // End of outer for

		// 가운데 하트 핑크블록 추가
		mapInfo.map[1][3].state = PINKBLOCK;
		mapInfo.map[1][4].state = PINKBLOCK;
		mapInfo.map[1][5].state = PINKBLOCK;
		mapInfo.map[1][9].state = PINKBLOCK;
		mapInfo.map[1][10].state = PINKBLOCK;
		mapInfo.map[1][11].state = PINKBLOCK;

		mapInfo.map[2][2].state = PINKBLOCK;
		mapInfo.map[2][6].state = PINKBLOCK;
		mapInfo.map[2][8].state = PINKBLOCK;
		mapInfo.map[2][12].state = PINKBLOCK;

		mapInfo.map[3][1].state = PINKBLOCK;
		mapInfo.map[3][7].state = PINKBLOCK;
		mapInfo.map[3][13].state = PINKBLOCK;

		mapInfo.map[4][1].state = PINKBLOCK;
		mapInfo.map[4][7].state = PINKBLOCK;
		mapInfo.map[4][13].state = PINKBLOCK;

		mapInfo.map[5][1].state = PINKBLOCK;
		mapInfo.map[5][13].state = PINKBLOCK;

		mapInfo.map[6][1].state = PINKBLOCK;
		mapInfo.map[6][13].state = PINKBLOCK;

		mapInfo.map[7][1].state = PINKBLOCK;
		mapInfo.map[7][13].state = PINKBLOCK;

		mapInfo.map[8][2].state = PINKBLOCK;
		mapInfo.map[8][12].state = PINKBLOCK;

		mapInfo.map[9][3].state = PINKBLOCK;
		mapInfo.map[9][11].state = PINKBLOCK;

		mapInfo.map[10][4].state = PINKBLOCK;
		mapInfo.map[10][10].state = PINKBLOCK;

		mapInfo.map[11][5].state = PINKBLOCK;
		mapInfo.map[11][9].state = PINKBLOCK;

		mapInfo.map[12][6].state = PINKBLOCK;
		mapInfo.map[12][8].state = PINKBLOCK;

		mapInfo.map[13][7].state = PINKBLOCK;

	}// End of mapSetting(MapInfo mapInfo)

	public void initialize() { // 게임 초기화
		// 초기 캐릭터는 앞을 보고있음.
		myMove = DOWN;
		yourMove = DOWN;
	}// End of initialize()

	///////////////////////////////////////////////////////////////
	// 기능 함수들 모음 //
	///////////////////////////////////////////////////////////////

	public void colliderControl(MapInfo mapInfo) { // 충돌처리
		for (int i = 0; i < mapInfo.size; i++) {
			for (int j = 0; j < mapInfo.size; j++) {
				switch (mapInfo.map[i][j].state) {
				case BROWNBLOCK, PINKBLOCK:
					if (((myX >= mapInfo.map[i][j].x - 40) && (myX <= mapInfo.map[i][j].x))
							&& ((myY < mapInfo.map[i][j].y + 5) && (myY > mapInfo.map[i][j].y - 35))) {
						keyR = false;
						continue;
					} else if (((myX >= mapInfo.map[i][j].x) && (myX <= mapInfo.map[i][j].x + 40))
							&& ((myY < mapInfo.map[i][j].y + 5) && (myY > mapInfo.map[i][j].y - 35))) {
						keyL = false;
						continue;
					} else if (((myX > mapInfo.map[i][j].x - 10) && (myX < mapInfo.map[i][j].x + 30))
							&& ((myY > mapInfo.map[i][j].y) && (myY < mapInfo.map[i][j].y + 40))) {
						keyU = false;
						continue;
					} else if (((myX >= mapInfo.map[i][j].x - 10) && (myX <= mapInfo.map[i][j].x + 30))
							&& ((myY + 46 >= mapInfo.map[i][j].y - 10) && (myY + 46 < mapInfo.map[i][j].y + 40))) {
						keyD = false;
						continue;
					}
					break;
				case ITEM:
					break;
				case BUMB:
					if (((myX >= mapInfo.map[i][j].x - 40) && (myX <= mapInfo.map[i][j].x))
							&& ((myY < mapInfo.map[i][j].y + 5) && (myY > mapInfo.map[i][j].y - 35))) {
						keyR = false;
						continue;
					} else if (((myX >= mapInfo.map[i][j].x) && (myX <= mapInfo.map[i][j].x + 40))
							&& ((myY < mapInfo.map[i][j].y + 5) && (myY > mapInfo.map[i][j].y - 35))) {
						keyL = false;
						continue;
					} else if (((myX > mapInfo.map[i][j].x - 10) && (myX < mapInfo.map[i][j].x + 30))
							&& ((myY > mapInfo.map[i][j].y) && (myY < mapInfo.map[i][j].y + 40))) {
						keyU = false;
						continue;
					} else if (((myX >= mapInfo.map[i][j].x - 10) && (myX <= mapInfo.map[i][j].x + 30))
							&& ((myY + 46 >= mapInfo.map[i][j].y - 10) && (myY + 46 < mapInfo.map[i][j].y + 40))) {
						keyD = false;
						continue;
					}
					break;
				}// end of switch
			} // end of inner for
		} // end of outer for
	}// End of colliderControl(MapInfo mapInfo) //충돌처리함수 끝

	// 폭탄 설치 스레드
	Runnable bombThread = new Runnable() {
		@Override
		public void run() {
			if (bombAvailable) {
				dropBomb();
				bombAvailable = false;
				try {
					Thread.sleep(3000);
					bombAvailable = true;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	};// End of Runnable bombThread = new Runnable()

	public void dropBomb() {
		this.x = myX;
		this.y = myY;
		movingInfo = new ChatMsg(UserName, "602", ""); // 602번으로 좌표를 보낸다
		movingInfo.update(myX, myY, myMove);
		gt.send(movingInfo);

		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				System.out.println("설치한 물풍선 스레드 돌아가는중입니다");
				ImageIcon bubble = new ImageIcon("images/bomb.png");
				JLabel bu = new JLabel(bubble);
				x /= 40;
				y /= 40;
				x *= 40;
				y *= 40;

				bu.setSize(40, 40);
				bu.setLocation(x + 16, y + 5);
				contentPane.add(bu);
				bu.setVisible(true);

				bx = x + 16;
				by = y + 5;
				try {
					Thread.sleep(2000);
//	               bu.setVisible(false);

					ImageIcon bup = new ImageIcon("images/bup.png");
					JLabel bupp = new JLabel(bup);
					ImageIcon bright = new ImageIcon("images/bright.png");
					JLabel br = new JLabel(bright);
					ImageIcon bdown = new ImageIcon("images/bdown.png");
					JLabel bd = new JLabel(bdown);
					ImageIcon bleft = new ImageIcon("images/bleft.png");
					JLabel bl = new JLabel(bleft);

					bupp.setSize(40, 40);
					bupp.setLocation(bu.getLocation().x, bu.getLocation().y - 40);
					bupp.setVisible(true);
					contentPane.add(bupp);
					br.setSize(40, 40);
					br.setLocation(bu.getLocation().x + 40, bu.getLocation().y);
					br.setVisible(true);
					contentPane.add(br);
					bd.setSize(40, 40);
					bd.setLocation(bu.getLocation().x, bu.getLocation().y + 40);
					bd.setVisible(true);
					contentPane.add(bd);
					bl.setSize(40, 40);
					bl.setLocation(bu.getLocation().x - 40, bu.getLocation().y);
					bl.setVisible(true);
					contentPane.add(bl);
					bu.setIcon(new ImageIcon("images/bcenter.png"));
					Thread.sleep(1000);
					bupp.setVisible(false);
					br.setVisible(false);
					bd.setVisible(false);
					bl.setVisible(false);
					bu.setVisible(false);

//	               checkLocation();
					bx = bu.getLocation().x;
					by = bu.getLocation().y;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		};
		new Thread(runnable).start();
	} // End of dropBomb()

	Runnable enemyBomb = new Runnable() {

		@Override
		public void run() {
			try {
				Thread.sleep(2000);
//				enemyCheckLocation(enemyBx, enemyBy);

			} catch (Exception e) {
				// TODO: handle exception
			}

		}
	};

	public void dropBomb(int imgX, int imgY) {
		this.x = imgX;
		this.y = imgY;
		
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				System.out.println("상대방 물풍선 스레드 돌아가는중입니다");
				ImageIcon bubble = new ImageIcon("images/bomb.png");
				JLabel bu = new JLabel(bubble);
				x /= 40;
				y /= 40;
				x *= 40;
				y *= 40;

				bu.setSize(40, 40);
				bu.setLocation(x + 16, y + 5);
				bu.setVisible(true);
				contentPane.add(bu);
				yourBx = x + 16;
				yourBy = y + 5;
				try {
					Thread.sleep(2000);
					bu.setVisible(false);

					ImageIcon bcenter = new ImageIcon("images/bcenter.png");
					JLabel bc = new JLabel(bcenter);
					ImageIcon bup = new ImageIcon("images/bup.png");
					JLabel bupp = new JLabel(bup);
					ImageIcon bright = new ImageIcon("images/bright.png");
					JLabel br = new JLabel(bright);
					ImageIcon bdown = new ImageIcon("images/bdown.png");
					JLabel bd = new JLabel(bdown);
					ImageIcon bleft = new ImageIcon("images/bleft.png");
					JLabel bl = new JLabel(bleft);

					bc.setSize(40, 40);
					bc.setLocation(yourBx, yourBy);
					bc.setVisible(true);
					contentPane.add(bc);
					bupp.setSize(40, 40);
					bupp.setLocation(yourBx, yourBy - 40);
					bupp.setVisible(true);
					contentPane.add(bupp);
					br.setSize(40, 40);
					br.setLocation(yourBx + 40, yourBy);
					br.setVisible(true);
					contentPane.add(br);
					bd.setSize(40, 40);
					bd.setLocation(yourBx, yourBy + 40);
					bd.setVisible(true);
					contentPane.add(bd);
					bl.setSize(40, 40);
					bl.setLocation(yourBx - 40, yourBy);
					bl.setVisible(true);
					contentPane.add(bl);
					Thread.sleep(1000);
					bc.setVisible(false);
					bupp.setVisible(false);
					br.setVisible(false);
					bd.setVisible(false);
					bl.setVisible(false);

//	               checkLocation();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		new Thread(runnable).start();
	}

	///////////////////////////////////////////////////////////////
	// KeyBoard 이벤트 처리 + process //
	///////////////////////////////////////////////////////////////

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		// System.out.println(e.getKeyCode());
		switch (e.getKeyCode()) {
		case KeyEvent.VK_RIGHT:
			keyR = true;
//			System.out.println("Right");
			break;
		case KeyEvent.VK_LEFT:
			keyL = true;
//			System.out.println("Left");
			break;
		case KeyEvent.VK_UP:
			keyU = true;
//			System.out.println("Up");
			break;
		case KeyEvent.VK_DOWN:
			keyD = true;
//			System.out.println("Down");
			break;
		case KeyEvent.VK_SPACE:
			System.out.println("클릭!");
			new Thread(bombThread).start();
		}// End of switch
	}// End of KeyPressed(KeyEvent e)

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		switch (e.getKeyCode()) {
		case KeyEvent.VK_RIGHT:
//			System.out.println("Right False");
//			System.out.println("(" + myX + "," + myY + ")");
			keyR = false;
			break;
		case KeyEvent.VK_LEFT:
//			System.out.println("Left False");
//			System.out.println("(" + myX + "," + myY + ")");
			keyL = false;
			break;
		case KeyEvent.VK_UP:
//			System.out.println("UP False");
//			System.out.println("(" + myX + "," + myY + ")");
			keyU = false;
			break;
		case KeyEvent.VK_DOWN:
//			System.out.println("Down False");
//			System.out.println("(" + myX + "," + myY + ")");
			keyD = false;
			break;
		}// End of switch
	}// End of KeyRealesed(KeyEvent e)

	// 키에 따른 이동 함수
	public void keyProcess() {
		movingInfo = new ChatMsg(UserName, "601", ""); // 601번으로 좌표를 보낸다
		// 방향과 좌표를 서버로 보낸다
		if (keyU == true) {
			myMove = UP;
			myY -= speed;
			if (myY < 0) {
				myY = 0;
			}
			movingInfo.update(myX, myY, myMove);
			gt.send(movingInfo);
		}
		if (keyD == true) {
			myMove = DOWN;
			myY += speed;
			if (myY > 550) {
				myY = 550;
			}
			movingInfo.update(myX, myY, myMove);
			gt.send(movingInfo);
		}
		if (keyL == true) {
			myMove = LEFT;
			myX -= speed;
			if (myX < 16) {
				myX = 16;
			}
			movingInfo.update(myX, myY, myMove);
			gt.send(movingInfo);
		}
		if (keyR == true) {
			myMove = RIGHT;
			myX += speed;
			if (myX > 580) {
				myX = 580;
			}
			movingInfo.update(myX, myY, myMove);
			gt.send(movingInfo);
		}
	}// End of keyProcess()

	///////////////////////////////////////////////////////////////
	// OnClickListener(focus) //
	///////////////////////////////////////////////////////////////

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	public void mousePressed(MouseEvent e) {
	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseEntered(MouseEvent e) {

	}

	public void mouseExited(MouseEvent e) {
	}

	///////////////////////////////////////////////////////////////
	// GameCanvas //
	///////////////////////////////////////////////////////////////
	
	class GameScreen extends Canvas {

		private static final long serialVersionUID = 1L;

		GameStart main;

		Image dblbuff; // 더블버퍼링용 백버퍼
		Graphics gc; // 더블버퍼링용 그래픽 컨텍스트

		Image bg; // 배경화면

		// 내 캐릭터 -> 배찌로 고정
		Image bazziUp = new ImageIcon("images/bazzi_back.png").getImage();
		Image bazziDown = new ImageIcon("images/bazzi_front.png").getImage();
		Image bazziLeft = new ImageIcon("images/bazzi_left.png").getImage();
		Image bazziRight = new ImageIcon("images/bazzi_right.png").getImage();

		// 상대 캐릭터 -> 우니로 고정
		Image uniUp = new ImageIcon("images/woonie_back.png").getImage();
		Image uniDown = new ImageIcon("images/woonie_front.png").getImage();
		Image uniLeft = new ImageIcon("images/woonie_left.png").getImage();
		Image uniRight = new ImageIcon("images/woonie_right.png").getImage();

		// 박스
		Image iconBoxBrown = new ImageIcon("images/cookie.png").getImage();
		Image iconBoxPink = new ImageIcon("images/cookie2.png").getImage();
		Image iconItemSpeed = new ImageIcon("images/speed.png").getImage();
		Image iconBomb = new ImageIcon("images/bomb.png").getImage();

		//////////////////////////////////////////////////////////////////////////////////////////

		GameScreen(GameStart main) {
			this.main = main;
		}// End of GameScreen(생성자)

		public void paint(Graphics g) {
			if (gc == null) {
				dblbuff = createImage(main.gScreenWidth, main.gScreenHeight);
				// 더블 버퍼링용 오프스크린 버퍼 생성. 필히 paint 함수 내에서 해줘야 한다. 그렇지 않으면 null이 반환된다.
				if (dblbuff == null)
					System.out.println("오프스크린 버퍼 생성 실패");
				else
					gc = dblbuff.getGraphics();// 오프스크린 버퍼에 그리기 위한 그래픽 컨텍스트 획득
				return;
			}
			update(g);
		}

		public void update(Graphics g) { // 화면 깜박거림을 줄이기 위해, paint에서 화면을 바로 묘화하지 않고 update 메소드를 호출하게 한다.
			if (gc == null)
				return;
			dblpaint(); // 오프스크린 버퍼에 그리기
			g.drawImage(dblbuff, 0, 0, this); // 오프스크린 버퍼를 메인화면에 그린다.
		}

		public void dblpaint() {
			// 실제 그리는 동작은 이 함수에서 모두 행한다.
			Draw_BG(); // 맵 배경화면 (핑크) 그리기
			Draw_Blocks(); // 블록 그리기
			Draw_myChracter(); // 내 캐릭터 (배찌)그리기
			Draw_yourChracter(); // 상대 캐릭터(우니) 그리기
		}

		public void Draw_BG() {
			gc.drawImage(mapBG, 0, 0, this);
		}

		public void Draw_Blocks() {

			// 테두리 (15*15)
//			for (int i = 0; i <= 560; i += 40) {
//				gc.drawImage(boxBrown, i, 0, this);
//			}
//			for (int i = 40; i <= 520; i += 40) {
//				gc.drawImage(boxBrown, 0, i, this);
//				gc.drawImage(boxBrown, 560, i, this);
//			}
//			for (int i = 0; i <= 560; i += 40) {
//				gc.drawImage(boxBrown, i, 560, this);
//			}

			for (int i = 0; i < 15; i++) {
				for (int j = 0; j < 15; j++) {
					switch (mapInfo.map[i][j].state) {
					case BROWNBLOCK:
						gc.drawImage(iconBoxBrown, mapInfo.map[i][j].x, mapInfo.map[i][j].y, this);
						break;
					case PINKBLOCK:
						gc.drawImage(iconBoxPink, mapInfo.map[i][j].x, mapInfo.map[i][j].y, this);
						break;
					case ITEM:
						gc.drawImage(iconItemSpeed, mapInfo.map[i][j].x, mapInfo.map[i][j].y, this);
						break;
					case BUMB:
						gc.drawImage(iconBomb, mapInfo.map[i][j].x, mapInfo.map[i][j].y, this);
						break;
					case FREE:
						gc.drawImage(null, mapInfo.map[i][j].x, mapInfo.map[i][j].y, this);
						break;

					}// End of switch
				} // End of inner for
			} // End of outer for
		}// End of Draw_Blocks()

		// 내 캐릭터를 그린다
		public void Draw_myChracter() {
			switch (myMove) {
			case UP:
				gc.drawImage(bazziUp, myX, myY, this);
				break;
			case DOWN:
				gc.drawImage(bazziDown, myX, myY, this);
				break;
			case LEFT:
				gc.drawImage(bazziLeft, myX, myY, this);
				break;
			case RIGHT:
				gc.drawImage(bazziRight, myX, myY, this);
				break;
			}// End of switch
		}// End of Draw_myCharcter()

		// 상대 캐릭터를 그린다
		public void Draw_yourChracter() {
			switch (yourMove) {
			case UP:
				gc.drawImage(uniUp, yourX, yourY, this);
				break;
			case DOWN:
				gc.drawImage(uniDown, yourX, yourY, this);
				break;
			case LEFT:
				gc.drawImage(uniLeft, yourX, yourY, this);
				break;
			case RIGHT:
				gc.drawImage(uniRight, yourX, yourY, this);
				break;
			}// End of switch
		}// End of Draw_myCharcter()
	}// End of GameCanvas(class)
		///////////////////////////////////////////////////////////////
		// GameCanvas 끝 //
		///////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////////
	// 메인 스레드, 메인 함수 //
	///////////////////////////////////////////////////////////////
	@Override
	public void run() {// 메인 스레드
		while (roof) {
			gamescreen.repaint(); // 화면 리페인트
			colliderControl(mapInfo); // 충돌 처리
			keyProcess(); // 키보드 처리
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	} // End of run()

	public class GameThread extends Thread {
		WaitingFrame waitingFrame; // WaitingFrame 접근용
		private Socket socket; // 연 결소켓
		private ObjectInputStream ois;
		private ObjectOutputStream oos;

		GameThread(WaitingFrame waitingFrame) {
			this.waitingFrame = waitingFrame;
			this.socket = waitingFrame.getSocket();
			this.ois = waitingFrame.getOis();
			this.oos = waitingFrame.getOos();
		}

		// 서버에 객체를 보낸다
		public void send(ChatMsg msg) {
			waitingFrame.SendObject(msg);
		}

		public void run() {
			while (true) {
				try {
					Object obcm = null;
					ChatMsg cm;
					try {
						obcm = ois.readObject(); // 서버가 보낸 값을 읽는다
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						break;
					}
					if (obcm == null) {
						break;
					}
					if (obcm instanceof ChatMsg) {
						cm = (ChatMsg) obcm;
					} else
						continue;
					switch (cm.code) {
					case "601": // 좌표를 받아서 상대의 좌표 갱신
						yourMove = cm.move;
						yourX = cm.x;
						yourY = cm.y;
						break;
					case "602": // 물풍선 좌표 받고 설치
						System.out.println("물풍선 좌표 잘 받았읍니다~" + cm.x + cm.y);
						yourBx = cm.x;
						yourBy = cm.y;
						dropBomb(yourBx, yourBy);
						new Thread(enemyBomb).start();
						break;
					}
				} catch (IOException e) {
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
}
