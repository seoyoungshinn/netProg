package netProg;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Vector;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import java.awt.MediaTracker;

//import netProg.Cookie;


public class GameStart extends JFrame implements KeyListener,MouseListener,Runnable{

	//keyboard 제어를 위한 변수
	   boolean keyU = false;
	   boolean keyD = false;
	   boolean keyL = false;
	   boolean keyR = false;
	     
	// 상태변수
	   boolean die = false;
	   int bombAvailable =3;		//초기 최대 물풍선 개수는 1
	 //  boolean isBombExplode = false;
	   private int speed = 40;  //80이 기본, 40이면 빠름
	
	//myMove, yourMove에 쓸 StringData
	   final String UP = "up";
	   final String DOWN = "down";
	   final String RIGHT = "right";
	   final String LEFT = "left";
	   
	//초기화시 캐릭터는 앞을 쳐다보고 있음
	   String myMove = DOWN;
	   String yourMove = DOWN;
	
	// 좌표 변수들
	   int myX = 500, myY = 500;
	   int yourX = 100, yourY = 100;   
	   int bombX, bombY;	   
	   int bx, by;
	   
	  //map state
	   final String BROWNBLOCK = "BROWNBLOCK";
	   final String PINKBLOCK = "PINKBLOCK";
	   final String ITEM = "ITEM";
	   final String FREE = "FREE";
	   final String BOMB = "BOMB";
	   final String BUP = "BUP";
	   final String BDOWN = "BDOWN";
	   final String BLEFT = "BLEFT";
	   final String BRIGHT = "BRIGHT";
	   final String BCENTER = "BCENTER";
	   final String ITEMSPEED = "ITEMSPEED";				//속력을 빠르게
	   final String ITEMSTRONGBOMB = "ITEMSTRONGBOMB";		//물풍선 길이를 길게
	   final String ITEMPLUSBOMB = "ITEMPLUSBOMB";			//물풍선 개수 증가
	   
	   //8개중 3개만아이템 (나올확률 1/8)
	   private String[] itemArray = {ITEMSPEED, ITEMSPEED, ITEMSPEED, ITEMSPEED, ITEMSPEED, ITEMSPEED, FREE,FREE};
	
	// 아이템 변수
	   private Vector<JLabel> item = new Vector<JLabel>();
	   private ArrayList<JLabel> itemlist = new ArrayList<JLabel>();	   
	   private ImageIcon[] item2 = { new ImageIcon("images/speed.png"),null,null, null };
	   ImageIcon item3;
	   JLabel itemLabel;
	   
	   private JLabel contentPane;
	   
	   GameScreen gamescreen; //Canvas객체
	   
	   int gScreenWidth=615;//게임 화면 너비
	   int gScreenHeight=645;//게임 화면 높이
	   Image mapBG = new ImageIcon("Images/mapbg1.png").getImage();  //게임1배경
	   

	   boolean roof=true;//스레드 루프 정보
	   public MapInfo mapInfo;
	   Random random = new Random();
	   
	   
	   
	   
	   
	   ///////////////////////////////////////////////////////////////
	   //							생성자							//
	   ///////////////////////////////////////////////////////////////	   
	   public GameStart() {
			 setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		     setBounds(100, 100, gScreenWidth, gScreenHeight);
		     setLocationRelativeTo(null);
		    // contentPane = new JLabel(new ImageIcon("Images/mapbg1.png"));
		     contentPane = new JLabel();
		     contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		     contentPane.setLayout(null);

		     setContentPane(contentPane);
		     setResizable(true); //윈도우 크기 변경 
		     setVisible(true);		     
		     addKeyListener(this);//키 입력 이벤트 리스너 활성화
		     
		     gamescreen=new GameScreen(this);//화면 묘화를 위한 캔버스 객체
		     gamescreen.setBounds(0,0,gScreenWidth,gScreenHeight);
			 add(gamescreen);//Canvas 객체를 프레임에 올린다

	 
			 this.requestFocus();

			 mapInfo =  new MapInfo(15);	
			 mapSetting(mapInfo);
			 initialize();
			 

			 printMap(); //디버깅용

	   	}//End of GameStart(생성자)
	   
//////////////////////디버깅용///////////////////////////////   
	   public void printMap() {
		   for (int i = 0; i<15; i++) {
			   for (int j =0; j<15; j++) {
				 //좌표와 상태출력
				 //  System.out.printf("(%3d,%3d) %10s\t", mapInfo.map[i][j].x,mapInfo.map[i][j].y,mapInfo.map[i][j].state);
			   
				 //상태만출력  
				   //System.out.printf("%10s\t", mapInfo.map[i][j].state); 
				   
				 //벽여부만 출력
				   System.out.printf("%3s\t", mapInfo.map[i][j].wasWall); 
			   }
			   System.out.println();
		   } 
		   System.out.println();
		   System.out.println();
	   }
//////////////////////디버깅용///////////////////////////////	   
	   
	   ///////////////////////////////////////////////////////////////
	   //					initializing Function					//
	   ///////////////////////////////////////////////////////////////
	   
	   public void mapSetting(MapInfo mapInfo) { //맵 정보 초기화
		   //가생이 갈색 블록 추가
		   for (int j = 0; j<15; j++) {
			   for (int i =0; i<15; i++) {
				   if (i == 0 || i == 14 || j ==0 || j ==14) {
					   mapInfo.map[i][j].state = BROWNBLOCK;
				   	   mapInfo.map[i][j].wasWall = true;
				   }//End of if
			   }//End of inner for
		   }//End of outer for
		   
		   //가운데 하트 핑크블록 추가
		   mapInfo.map[1][3].state = PINKBLOCK;		   mapInfo.map[1][4].state = PINKBLOCK;		   mapInfo.map[1][5].state = PINKBLOCK;		   mapInfo.map[1][9].state = PINKBLOCK;		   mapInfo.map[1][10].state = PINKBLOCK;		   mapInfo.map[1][11].state = PINKBLOCK;
		   mapInfo.map[2][2].state = PINKBLOCK;		   mapInfo.map[2][6].state = PINKBLOCK;		   mapInfo.map[2][8].state = PINKBLOCK;		   mapInfo.map[2][12].state = PINKBLOCK;
		   mapInfo.map[3][1].state = PINKBLOCK;		   mapInfo.map[3][7].state = PINKBLOCK;		   mapInfo.map[3][13].state = PINKBLOCK;
		   mapInfo.map[4][1].state = PINKBLOCK;		   mapInfo.map[4][7].state = PINKBLOCK;		   mapInfo.map[4][13].state = PINKBLOCK;
		   mapInfo.map[5][1].state = PINKBLOCK;		   mapInfo.map[5][13].state = PINKBLOCK;
		   mapInfo.map[6][1].state = PINKBLOCK;		   mapInfo.map[6][13].state = PINKBLOCK;
		   mapInfo.map[7][1].state = PINKBLOCK;		   mapInfo.map[7][13].state = PINKBLOCK;
		   mapInfo.map[8][2].state = PINKBLOCK;		   mapInfo.map[8][12].state = PINKBLOCK;
		   mapInfo.map[9][3].state = PINKBLOCK;		   mapInfo.map[9][11].state = PINKBLOCK;
		   mapInfo.map[10][4].state = PINKBLOCK;		   mapInfo.map[10][10].state = PINKBLOCK;
		   mapInfo.map[11][5].state = PINKBLOCK;		   mapInfo.map[11][9].state = PINKBLOCK;
		   mapInfo.map[12][6].state = PINKBLOCK;		   mapInfo.map[12][8].state = PINKBLOCK;
		   mapInfo.map[13][7].state = PINKBLOCK;
		   //핑크하트도 모두 벽처리 		   
		   mapInfo.map[1][3].wasWall = true;		   mapInfo.map[1][4].wasWall = true;		   mapInfo.map[1][5].wasWall = true;		   mapInfo.map[1][9].wasWall = true;		   mapInfo.map[1][10].wasWall = true;		   mapInfo.map[1][11].wasWall = true;
		   mapInfo.map[2][2].wasWall = true;		   mapInfo.map[2][6].wasWall = true;		   mapInfo.map[2][8].wasWall = true;		   mapInfo.map[2][12].wasWall = true;
		   mapInfo.map[3][1].wasWall = true;		   mapInfo.map[3][7].wasWall = true;		   mapInfo.map[3][13].wasWall = true;
		   mapInfo.map[4][1].wasWall = true;		   mapInfo.map[4][7].wasWall = true;		   mapInfo.map[4][13].wasWall = true;
		   mapInfo.map[5][1].wasWall = true;		   mapInfo.map[5][13].wasWall = true;
		   mapInfo.map[6][1].wasWall = true;		   mapInfo.map[6][13].wasWall = true;
		   mapInfo.map[7][1].wasWall = true;		   mapInfo.map[7][13].wasWall = true;
		   mapInfo.map[8][2].wasWall = true;		   mapInfo.map[8][12].wasWall = true;
		   mapInfo.map[9][3].wasWall = true;		   mapInfo.map[9][11].wasWall = true;
		   mapInfo.map[10][4].wasWall = true;		   mapInfo.map[10][10].wasWall = true;
		   mapInfo.map[11][5].wasWall = true;		   mapInfo.map[11][9].wasWall = true;
		   mapInfo.map[12][6].wasWall = true;		   mapInfo.map[12][8].wasWall = true;
		   mapInfo.map[13][7].wasWall = true;
	   }//End of mapSetting(MapInfo mapInfo)
	   
	   
	   
	   public void initialize() {	//게임 초기화		   
		   //초기 캐릭터는 앞을 보고있음.
		   myMove = DOWN;
		   yourMove = DOWN;
		   gamescreen.repaint();
	   }//End of initialize()

	   
	   
   ///////////////////////////////////////////////////////////////
   //						기능 함수들 모음							//
   ///////////////////////////////////////////////////////////////
	   
	   public void colliderControl(MapInfo mapInfo ){	//충돌처리	   
		   for (int i =0; i<mapInfo.size; i++) {
			   for (int j =0; j<mapInfo.size; j++) {
				   switch(mapInfo.map[i][j].state) {
					   	case BROWNBLOCK,PINKBLOCK:
						   if (((myX >= mapInfo.map[i][j].x-40) && (myX<= mapInfo.map[i][j].x)) && ((myY < mapInfo.map[i][j].y+5) && (myY> mapInfo.map[i][j].y-35))) {
							   keyR = false;
						   		continue;
						   }else if (((myX >= mapInfo.map[i][j].x) && (myX<= mapInfo.map[i][j].x+40)) && ((myY < mapInfo.map[i][j].y+5) && (myY> mapInfo.map[i][j].y-35))) {
							   keyL = false;
						   		continue;
						   }else if (((myX > mapInfo.map[i][j].x-10) && (myX< mapInfo.map[i][j].x+30)) && ((myY > mapInfo.map[i][j].y) && (myY< mapInfo.map[i][j].y+40))) {
							   keyU = false;
						   		continue;
						   }else if (((myX >= mapInfo.map[i][j].x-10) && (myX<= mapInfo.map[i][j].x+30)) && ((myY+46 >= mapInfo.map[i][j].y-10) && (myY+46< mapInfo.map[i][j].y+40))) {
							   keyD = false;
						   		continue;
						   }
					   		break;
					   	case ITEM:
					   		break;
					   	case BOMB:   
						   if (((myX >= mapInfo.map[i][j].x-40) && (myX<= mapInfo.map[i][j].x)) && ((myY < mapInfo.map[i][j].y+5) && (myY> mapInfo.map[i][j].y-35))) {
							   keyR = false;
						   		continue;
						   }else if (((myX >= mapInfo.map[i][j].x) && (myX<= mapInfo.map[i][j].x+40)) && ((myY < mapInfo.map[i][j].y+5) && (myY> mapInfo.map[i][j].y-35))) {
							   keyL = false;
						   		continue;
						   }else if (((myX > mapInfo.map[i][j].x-10) && (myX< mapInfo.map[i][j].x+30)) && ((myY > mapInfo.map[i][j].y) && (myY< mapInfo.map[i][j].y+40))) {
							   keyU = false;
						   		continue;
						   }else if (((myX >= mapInfo.map[i][j].x-10) && (myX<= mapInfo.map[i][j].x+30)) && ((myY+46 >= mapInfo.map[i][j].y-10) && (myY+46< mapInfo.map[i][j].y+40))) {
							   keyD = false;
						   		continue;
						   }
						   break;  
					}//end of switch		 
			   }//end of inner for
		   }//end of outer for
	   }//End of colliderControl(MapInfo mapInfo)	//충돌처리함수 끝
	   
	   
	   
	   
	   public class BombThread implements Runnable {
		   int bombX, bombY;
		   public BombThread(int myX, int myY) {
			   this.bombX = myX;
			   this.bombY = myY;
			   

		    	  System.out.println("1 >>>"+bombX+bombY);
		    	  bombX/=40;
		    	  bombY/=40;
		    	  if (myY !=0) bombY+=1;
		    	  
		    	  System.out.println("배열좌표>>>"+bombX+","+bombY);
		       // store parameter for later user
		   }//End of 생성자

		   @Override
	         public void run() {
	            if(bombAvailable>0) {
	               dropBomb(bombX, bombY);
	                 try {
	                	 Thread.sleep(3000);
	                	 explodeBomb(bombX,bombY);
	                	 Thread.sleep(500);
	                	 freeBomb(bombX,bombY);
	                 } catch (InterruptedException e) {
	                  // TODO Auto-generated catch block
	                  e.printStackTrace();
	                 }
	            }//End of if	            
	         }//End of run()
		}//End of BombThread
	   
	   public void randomState(MapInfo mapinfo) {
		   
	   }
	   
	   
	   
	   
	      public void freeBomb(int bombX, int bombY) {
	    	  //이렇게하면 아이템 줄 수 없음 왜냐면 이전상태가 모두 bomb에 관한 상태로 바뀌기 때문에 벽이었는지 확인 불가함
	    	  //클래스에 이전상태를 저장하는 배열을 만들어 관리하던가 해야함
	    	  System.out.println("FreeBomb");
	    	  printMap(); 
	    	  mapInfo.map[bombY][bombX].state = FREE;
	    	  if (bombY-1 >=0) {
	    		  if (mapInfo.map[bombY-1][bombX].wasWall==true) {	  //이전 상태가 벽 상태였으면,  		  
	    		 // if (mapInfo.map[bombY-1][bombX].state != FREE) {	    		  
	    			  //여기다 랜덤으로 state를 FREE 또는 아이템 1,2로 설정하는 함수 
	    			  mapInfo.map[bombY-1][bombX].wasWall=false; //벽 취소
	    			  mapInfo.map[bombY-1][bombX].state =itemArray[random.nextInt(8)];	    			  
	    	  		}	else {
	    		  mapInfo.map[bombY-1][bombX].state = FREE;
	    	  		}
	    	  }
	    	  if(bombY+1<15) {
	    		  if (mapInfo.map[bombY+1][bombX].wasWall==true) {	  //이전 상태가 벽 상태였으면,  
	    			  // if (mapInfo.map[bombY-1][bombX].state != FREE) {	    		  
	 	    			  //여기다 랜덤으로 state를 FREE 또는 아이템 1,2로 설정하는 함수 
	    			  mapInfo.map[bombY+1][bombX].wasWall=false; //벽 취소
	 	    			  mapInfo.map[bombY+1][bombX].state =itemArray[random.nextInt(8)];	    			  
	 	    	  		}	else {
	 	    		  mapInfo.map[bombY+1][bombX].state = FREE;
	 	    	  		}
	    	  }
	    		
	    	  if(bombX+1 <15) {
	    		  if (mapInfo.map[bombY][bombX+1].wasWall==true) {	  //이전 상태가 벽 상태였으면,  
	    			  // if (mapInfo.map[bombY-1][bombX].state != FREE) {	    		  
	 	    			  //여기다 랜덤으로 state를 FREE 또는 아이템 1,2로 설정하는 함수 
	    			  mapInfo.map[bombY][bombX+1].wasWall=false; //벽 취소
	 	    			  mapInfo.map[bombY][bombX+1].state =itemArray[random.nextInt(8)];	    			  
	 	    	  		}	else {
	 	    		  mapInfo.map[bombY][bombX+1].state = FREE;
	 	    	  		}
	    	  }
	    		 
	    	  if(bombX-1 >=0) {
	    		  if (mapInfo.map[bombY][bombX-1].wasWall==true) {	  //이전 상태가 벽 상태였으면,  
	    			  // if (mapInfo.map[bombY-1][bombX].state != FREE) {	    		  
	 	    			  //여기다 랜덤으로 state를 FREE 또는 아이템 1,2로 설정하는 함수 
	    			  	mapInfo.map[bombY][bombX-1].wasWall=false; //벽 취소
	 	    			  mapInfo.map[bombY][bombX-1].state =itemArray[random.nextInt(8)];	    			  
	 	    	  		}	else {
	 	    		  mapInfo.map[bombY][bombX-1].state = FREE;
	 	    	  		}    
	    	  }
	    		  
	    	  bombAvailable+=1;  
	    	  printMap();
	      }
	      
	      
	      public void explodeBomb(int bombX, int bombY) {
	    	  System.out.println("explodeBomb");
	    	  printMap();
	    	  mapInfo.map[bombY][bombX].state = BCENTER;
	    	  if (bombY-1 >=0)
	    		  mapInfo.map[bombY-1][bombX].state = BUP;
	    	  if(bombY+1<15)
	    		  mapInfo.map[bombY+1][bombX].state = BDOWN;
	    	  if(bombX+1 <15)
	    		  mapInfo.map[bombY][bombX+1].state = BRIGHT;
	    	  if(bombX-1 >=0)
	    		  mapInfo.map[bombY][bombX-1].state = BLEFT;    	  
	      }
	      
	      
	      public void dropBomb(int bombX, int bombY) {

	    	  System.out.println("배열좌표>>>"+bombX+","+bombY);
	    	  mapInfo.map[bombY][bombX].state = BOMB;
	    	  printMap(); 

	    	  System.out.println(">>>"+bombX+","+bombY);
	    	  bombAvailable-=1;
	      }//End of dropBomb()
		



	   
   ///////////////////////////////////////////////////////////////
   //					KeyBoard 이벤트 처리 + process				//
   ///////////////////////////////////////////////////////////////
	@Override
	public void keyTyped(KeyEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		 //System.out.println(e.getKeyCode());
        switch (e.getKeyCode()) {
	        case KeyEvent.VK_RIGHT:
	        	keyR = true;         
	        	System.out.println("Right");
	           break;
	        case KeyEvent.VK_LEFT:
	        	keyL = true;
	        	System.out.println("Left");
	           break;
	        case KeyEvent.VK_UP:
	        	keyU = true;
	        	System.out.println("Up");
	        	break;
	        case KeyEvent.VK_DOWN:
	        	keyD = true;
	        	System.out.println("Down");
	        	break;
	        case KeyEvent.VK_SPACE:
	        	Runnable BombThread = new BombThread(myX,myY);
	        	new Thread(BombThread).start();
	        	
	        	//new Thread(bombThread).start();
        }//End of switch
	}//End of KeyPressed(KeyEvent e)
	


	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		switch (e.getKeyCode()) {
	        case KeyEvent.VK_RIGHT:
	        	 System.out.println("Right False");
	        	 System.out.println("("+myX+","+myY+")");
	             keyR = false;
	           break;
	        case KeyEvent.VK_LEFT:
	        	 System.out.println("Left False");
	        	 System.out.println("("+myX+","+myY+")");
	             keyL = false;
	           break;
	        case KeyEvent.VK_UP:
	        	 System.out.println("UP False");
	        	 System.out.println("("+myX+","+myY+")");
	             keyU = false;
	           break;
	        case KeyEvent.VK_DOWN:
	        	 System.out.println("Down False");
	        	 System.out.println("("+myX+","+myY+")");
	             keyD = false;
	           break;
        }//End of switch
	}//End of KeyRealesed(KeyEvent e)
	
	
	
	//테스트를 위해 우니도 배찌와 같은방향으로 보게 설정해놓았음
	public void keyProcess() {
	//	System.out.println("KeyProcess()"+myMove);
	    if (keyU == true) {
	    	System.out.println("myMove"+myMove);
	       myMove = UP;  // bazziCurrent("images/bazzi_back.png");
	       yourMove = UP;
	       myY -= 10;
	      // gt.send(username + ":" + "MOVE:" + move);
	       if (myY < 0) {
	          myY = 0;
	       }
	    }
	    if (keyD == true) {
	       myMove = DOWN; //bazziCurrent("images/bazzi_front.png");
	       myY += 10;
	       yourMove = DOWN;
	    //   gt.send(username + ":" + "MOVE:" + move);
	       if (myY > 550) {
	          myY = 550;
	       }
	    }
	    if (keyL == true) {
	       myMove = LEFT; //bazziCurrent("images/bazzi_left.png");
	       myX -= 10;
	       yourMove = LEFT;
	   //    gt.send(username + ":" + "MOVE:" + move);//
	       if (myX < 16) {
	          myX = 16;
	       }
	    }
	    if (keyR == true) {
	       myMove = RIGHT;
	       myX += 10;
	       yourMove = RIGHT;
	    //   gt.send(username + ":" + "MOVE:" + move);
	       if (myX > 580) {
	          myX = 580;
	       }
	    }
	 }//End of keyProcess()
	
	
   ///////////////////////////////////////////////////////////////
   //					OnClickListener(focus)					//
   ///////////////////////////////////////////////////////////////

	//이게안됨 ㅠㅠ
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
		contentPane.requestFocus(); 
	}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	
	

		   
	
	
	
   ///////////////////////////////////////////////////////////////
   //						GameCanvas							//
   ///////////////////////////////////////////////////////////////

	   class GameScreen extends Canvas{
		   	GameStart main;
		   
			Image dblbuff;//더블버퍼링용 백버퍼
			Graphics gc;//더블버퍼링용 그래픽 컨텍스트

			Image bg; //배경화면
			
			 //내 캐릭터 -> 배찌로 고정
			Image bazziUp = new ImageIcon("images/bazzi_back.png").getImage();
			Image bazziDown = new ImageIcon("images/bazzi_front.png").getImage();
			Image bazziLeft = new ImageIcon("images/bazzi_left.png").getImage();
			Image bazziRight = new ImageIcon("images/bazzi_right.png").getImage();
			   
			 //상대 캐릭터 -> 우니로 고정
			Image uniUp = new ImageIcon("images/woonie_back.png").getImage();
			Image uniDown = new ImageIcon("images/woonie_front.png").getImage();
			Image uniLeft = new ImageIcon("images/woonie_left.png").getImage();
			Image uniRight = new ImageIcon("images/woonie_right.png").getImage();
			 
			 //박스
			Image iconBoxBrown = new ImageIcon("images/cookie.png").getImage();
			Image iconBoxPink = new ImageIcon("images/cookie2.png").getImage();
			Image iconItemSpeed = new ImageIcon("images/speed.png").getImage();
			Image iconBomb = new ImageIcon("images/bomb.png").getImage();
			
			//물풍선
			Image iconBumb = new ImageIcon("images/bomb.png").getImage();
			Image iconBup = new ImageIcon("images/bup.png").getImage();
			Image iconBdown = new ImageIcon("images/bdown.png").getImage();
			Image iconBleft = new ImageIcon("images/bleft.png").getImage();
			Image iconBright = new ImageIcon("images/bright.png").getImage();
			Image iconBcenter = new ImageIcon("images/bcenter.png").getImage();
			
	//////////////////////////////////////////////////////////////////////////////////////////
			
			GameScreen (GameStart main){
				this.main = main;
			}//End of GameScreen(생성자)
			
			
			
			public void paint(Graphics g){
				if(gc==null) {
					dblbuff=createImage(main.gScreenWidth,main.gScreenHeight);//더블 버퍼링용 오프스크린 버퍼 생성. 필히 paint 함수 내에서 해 줘야 한다. 그렇지 않으면 null이 반환된다.
					if(dblbuff==null) System.out.println("오프스크린 버퍼 생성 실패");
					else gc=dblbuff.getGraphics();//오프스크린 버퍼에 그리기 위한 그래픽 컨텍스트 획득
					return;
				}
				update(g);
			}//End of paint(Graphics g)
			
			
			
			public void update(Graphics g){//화면 깜박거림을 줄이기 위해, paint에서 화면을 바로 묘화하지 않고 update 메소드를 호출하게 한다.
				//cnt=main.cnt;
				//gamecnt=main.gamecnt;
				if(gc==null) return;
				dblpaint();//오프스크린 버퍼에 그리기
				g.drawImage(dblbuff,0,0,this);//오프스크린 버퍼를 메인화면에 그린다.
			}//End of update(Graphics g)
			
			
			
			public void dblpaint(){  
				//실제 그리는 동작은 이 함수에서 모두 행한다.				
				Draw_BG (); // 맵 배경화면 (핑크) 그리기
				Draw_Blocks(); //블록 그리기
				Draw_myChracter(); //내 캐릭터 (배찌)그리기
				Draw_yourChracter(); //상대 캐릭터(우니) 그리기
				//DrawBomb();	//물풍선그리기
				//ExplodeBomb();  //물풍선 폭발 그리기
			}//End of dblpaint()

			
			
			public void Draw_BG() {
				gc.drawImage(mapBG,0,0,this);
			}//End of Draw_BG()
			
			
			
			public void Draw_Blocks() {				
				/*
				 * //테두리 (15*15) for (int i = 0; i<=560; i+=40) {
				 * gc.drawImage(boxBrown,i,0,this); } for (int i = 40; i<=520; i+=40) {
				 * gc.drawImage(boxBrown,0,i,this); gc.drawImage(boxBrown,560,i,this); } for
				 * (int i = 0; i<=560; i+=40) { gc.drawImage(boxBrown,i,560,this); }
				 */							
			   for (int i = 0; i<15; i++) {
				   for (int j =0; j<15; j++) {
					   switch(mapInfo.map[i][j].state) {
						   case BROWNBLOCK :
							   gc.drawImage(iconBoxBrown,mapInfo.map[i][j].x,mapInfo.map[i][j].y,this);
							   break;
						   case PINKBLOCK :
							   gc.drawImage(iconBoxPink,mapInfo.map[i][j].x,mapInfo.map[i][j].y,this);
							   break;
						   case ITEM :
							   gc.drawImage(iconItemSpeed,mapInfo.map[i][j].x,mapInfo.map[i][j].y,this);
							   break;
						   case BOMB :
							   gc.drawImage(iconBomb,mapInfo.map[i][j].x,mapInfo.map[i][j].y,this);
							   break;
						   case BUP :
							   gc.drawImage(iconBup,mapInfo.map[i][j].x,mapInfo.map[i][j].y,this);
							   break;
						   case BDOWN :
							   gc.drawImage(iconBdown,mapInfo.map[i][j].x,mapInfo.map[i][j].y,this);
							   break;
						   case BLEFT :
							   gc.drawImage(iconBleft,mapInfo.map[i][j].x,mapInfo.map[i][j].y,this);
							   break;
						   case BRIGHT :
							   gc.drawImage(iconBright,mapInfo.map[i][j].x,mapInfo.map[i][j].y,this);
							   break;  
						   case BCENTER :
							   gc.drawImage(iconBcenter,mapInfo.map[i][j].x,mapInfo.map[i][j].y,this);
							   break;  
						   case ITEMSPEED:
							   gc.drawImage( new ImageIcon("images/speed.png").getImage(),mapInfo.map[i][j].x,mapInfo.map[i][j].y,this);
							   break;
						   case ITEMSTRONGBOMB:
							   gc.drawImage( new ImageIcon("images/speed.png").getImage(),mapInfo.map[i][j].x,mapInfo.map[i][j].y,this);
							   break;
						   case ITEMPLUSBOMB:
							   gc.drawImage( new ImageIcon("images/speed.png").getImage(),mapInfo.map[i][j].x,mapInfo.map[i][j].y,this);
							   break;
						   case FREE :
							   gc.drawImage(null,mapInfo.map[i][j].x,mapInfo.map[i][j].y,this);
							   break;
							   							  
					   }//End of switch					 				   
				   }//End of inner for
			   }//End of outer for
			}//End of Draw_Blocks()
			
			
			
			public void Draw_myChracter() {
				switch(myMove) {
					case UP:
						gc.drawImage(bazziUp, myX, myY,this);
						break;
					case DOWN:
						gc.drawImage(bazziDown, myX, myY,this);
						break;
					case LEFT:
						gc.drawImage(bazziLeft, myX, myY,this);
						break;
					case RIGHT:
						gc.drawImage(bazziRight, myX, myY,this);
						break;
				}//End of switch	
			}//End of Draw_myCharcter()
			
			public void Draw_yourChracter() {
				switch(yourMove) {
					case UP:
						gc.drawImage(uniUp, yourX, yourY,this);
						break;
					case DOWN:
						gc.drawImage(uniDown, yourX, yourY,this);
						break;
					case LEFT:
						gc.drawImage(uniLeft, yourX, yourY,this);
						break;
					case RIGHT:
						gc.drawImage(uniRight, yourX, yourY,this);
						break;
					//gc.drawImage(uni, yourX, yourY,this);
				}//End of switch	
			}//End of Draw_myCharcter()
			
					
			/*  배경그리는걸로 합침
			 * public void DrawBomb() { //안터진 물풍선 그리기 if (bombAvailable == false) {
			 * gc.drawImage(iconBumb, bombX, bombY,this); } else { gc.drawImage(null, bombX,
			 * bombY,this); } }
			 */	
			
			/*  배경그리는걸로 합침
			 * public void ExplodeBomb() { //터지는 물풍선 그리기 if (isBombExplode == true) {
			 * gc.drawImage(iconBcenter, bombX, bombY,this); gc.drawImage(iconBup, bombX,
			 * bombY-40,this); gc.drawImage(iconBdown, bombX, bombY+40,this);
			 * gc.drawImage(iconBleft, bombX-40, bombY,this); gc.drawImage(iconBright,
			 * bombX+40, bombY,this); } else { gc.drawImage(null, bombX, bombY,this); } }
			 */
			
			
	   }//End of GameCanvas(class)
   ///////////////////////////////////////////////////////////////
   //						GameCanvas 끝						//
   ///////////////////////////////////////////////////////////////

	   
	   
   ///////////////////////////////////////////////////////////////
   //						메인 스레드, 메인 함수					//
   ///////////////////////////////////////////////////////////////
			@Override
			public void run() {//메인 스레드
				// TODO Auto-generated method stub
				
				while(roof) {
					gamescreen.repaint();//화면 리페인트
					colliderControl(mapInfo); //충돌처리
					keyProcess();	//키보드 처리
					
				try {
					Thread.sleep(speed);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			} //End of run()
			
		   public static void main(String[] args) {
			   new GameStart().run();
		   }	
} //End of Class
