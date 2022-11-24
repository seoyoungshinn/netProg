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

	
	Thread thread;
	
	//keyboard ��� ���� ����
	   boolean keyU = false;
	   boolean keyD = false;
	   boolean keyL = false;
	   boolean keyR = false;
	     
	// ���º���
	   boolean die = false;
	   int bombAvailable =3;		//�ʱ� �ִ� ��ǳ�� ������ 1
	   int maxBomb = 10;
	 //  boolean isBombExplode = false;
	   private int speed = 40;  //80�� �⺻, 40�̸� ����, �׽�Ʈ���̶� 30����
	   private int MaxSpeed = 30;
	
	//myMove, yourMove�� �� StringData
	   final String UP = "up";
	   final String DOWN = "down";
	   final String RIGHT = "right";
	   final String LEFT = "left";
	   
	//�ʱ�ȭ�� ĳ���ʹ� ���� �Ĵٺ��� ����
	   String myMove = DOWN;
	   String yourMove = DOWN;
	
	// ��ǥ ������
	   int myX = 500, myY = 500;
	   int yourX = 100, yourY = 100;   
	   int bombX, bombY;	   
	   int bx, by;
	   
	  //map state
	   final String BROWNBLOCK = "BROWNBLOCK";
	   final String PINKBLOCK = "PINKBLOCK";
	  // final String ITEM = "ITEM";
	   final String FREE = "FREE";
	   final String BOMB = "BOMB";
	   final String BUP = "BUP";
	   final String BDOWN = "BDOWN";
	   final String BLEFT = "BLEFT";
	   final String BRIGHT = "BRIGHT";
	   final String BCENTER = "BCENTER";
	   final String ITEMSPEED = "ITEMSPEED";				//�ӷ��� ������
	   final String ITEMSTRONGBOMB = "ITEMSTRONGBOMB";		//��ǳ�� ���̸� ���
	   final String ITEMPLUSBOMB = "ITEMPLUSBOMB";			//��ǳ�� ���� ����
	   
	   //8���� 3���������� (����Ȯ�� 1/8)
	   private String[] itemArray = {ITEMPLUSBOMB, ITEMPLUSBOMB, ITEMPLUSBOMB, ITEMSPEED, FREE, FREE, FREE,FREE};
	
	// ������ ����
//	   private Vector<JLabel> item = new Vector<JLabel>();
//	   private ArrayList<JLabel> itemlist = new ArrayList<JLabel>();	   
//	   private ImageIcon[] item2 = { new ImageIcon("images/speed.png"),null,null, null };
	   ImageIcon item3;
	   JLabel itemLabel;
	   
	   private JLabel contentPane;
	   
	   GameScreen gamescreen; //Canvas��ü
	   
	   int gScreenWidth=615;//���� ȭ�� �ʺ�
	   int gScreenHeight=645;//���� ȭ�� ����
	   Image mapBG = new ImageIcon("Images/mapbg1.png").getImage();  //����1���
	   

	   boolean roof=true;//������ ���� ����
	   public MapInfo mapInfo;
	   Random random = new Random();
	   
	   
	   
	   
	   
	   ///////////////////////////////////////////////////////////////
	   //							������							//
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
		     setResizable(true); //������ ũ�� ���� 
		     setVisible(true);		     
		     addKeyListener(this);//Ű �Է� �̺�Ʈ ������ Ȱ��ȭ
		     
		     gamescreen=new GameScreen(this);//ȭ�� ��ȭ�� ���� ĵ���� ��ü
		     gamescreen.setBounds(0,0,gScreenWidth,gScreenHeight);
			 add(gamescreen);//Canvas ��ü�� �����ӿ� �ø���

	 
			 this.requestFocus();

			 mapInfo =  new MapInfo(15);	
			 mapSetting(mapInfo);
			 initialize();
			 
			 thread = new Thread(this);
			 thread.start();
			 

			 printMap(); //������

	   	}//End of GameStart(������)
	   
//////////////////////������///////////////////////////////   
	   public void printMap() {
		   for (int i = 0; i<15; i++) {
			   for (int j =0; j<15; j++) {
				 //��ǥ�� �������
				 //  System.out.printf("(%3d,%3d) %10s\t", mapInfo.map[i][j].x,mapInfo.map[i][j].y,mapInfo.map[i][j].state);
			   
				 //���¸����  
				   System.out.printf("%10s\t", mapInfo.map[i][j].state); 
				   
				 //�����θ� ���
				 //  System.out.printf("%3s\t", mapInfo.map[i][j].wasWall); 
			   }
			   System.out.println();
		   } 
		   System.out.println();
		   System.out.println();
	   }
//////////////////////������///////////////////////////////	   
	   
	   ///////////////////////////////////////////////////////////////
	   //					initializing Function					//
	   ///////////////////////////////////////////////////////////////
	   
	   public void mapSetting(MapInfo mapInfo) { //�� ���� �ʱ�ȭ
		   //������ ���� ��� �߰�
		   for (int j = 0; j<15; j++) {
			   for (int i =0; i<15; i++) {
				   if (i == 0 || i == 14 || j ==0 || j ==14) {
					   mapInfo.map[i][j].state = BROWNBLOCK;
				   	   mapInfo.map[i][j].wasWall = true;
				   }//End of if
			   }//End of inner for
		   }//End of outer for
		   
		   //��� ��Ʈ ��ũ��� �߰�
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
		   //��ũ��Ʈ�� ��� ��ó�� 		   
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
	   
	   
	   
	   public void initialize() {	//���� �ʱ�ȭ		   
		   //�ʱ� ĳ���ʹ� ���� ��������.
		   myMove = DOWN;
		   yourMove = DOWN;
		   gamescreen.repaint();
	   }//End of initialize()

	   
	   
   ///////////////////////////////////////////////////////////////
   //						��� �Լ��� ����							//
   ///////////////////////////////////////////////////////////////
	   
	   public void colliderControl(MapInfo mapInfo ){	//�浹ó��	   
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
					   		
					   		
					   	case ITEMSPEED:
					   		if (((myX >= mapInfo.map[i][j].x-40) && (myX<= mapInfo.map[i][j].x)) && ((myY < mapInfo.map[i][j].y+5) && (myY> mapInfo.map[i][j].y-35))) {
								   mapInfo.map[i][j].state =FREE;
								   itemSpeedUp();
							   		continue;
							   }else if (((myX >= mapInfo.map[i][j].x) && (myX<= mapInfo.map[i][j].x+40)) && ((myY < mapInfo.map[i][j].y+5) && (myY> mapInfo.map[i][j].y-35))) {
								   mapInfo.map[i][j].state =FREE;
								   itemSpeedUp();
							   		continue;
							   }else if (((myX > mapInfo.map[i][j].x-10) && (myX< mapInfo.map[i][j].x+30)) && ((myY > mapInfo.map[i][j].y) && (myY< mapInfo.map[i][j].y+40))) {
								   mapInfo.map[i][j].state =FREE;
								   itemSpeedUp();
							   		continue;
							   }else if (((myX >= mapInfo.map[i][j].x-10) && (myX<= mapInfo.map[i][j].x+30)) && ((myY+46 >= mapInfo.map[i][j].y-10) && (myY+46< mapInfo.map[i][j].y+40))) {
								   mapInfo.map[i][j].state =FREE;
								   itemSpeedUp();
							   		continue;
							   }
					   		break;
					   	
					   	case ITEMPLUSBOMB:
					   		if (((myX >= mapInfo.map[i][j].x-40) && (myX<= mapInfo.map[i][j].x)) && ((myY < mapInfo.map[i][j].y+5) && (myY> mapInfo.map[i][j].y-35))) {
								   mapInfo.map[i][j].state =FREE;
								   itemPlusBomb();
							   		continue;
							   }else if (((myX >= mapInfo.map[i][j].x) && (myX<= mapInfo.map[i][j].x+40)) && ((myY < mapInfo.map[i][j].y+5) && (myY> mapInfo.map[i][j].y-35))) {
								   mapInfo.map[i][j].state =FREE;
								   itemPlusBomb();
							   		continue;
							   }else if (((myX > mapInfo.map[i][j].x-10) && (myX< mapInfo.map[i][j].x+30)) && ((myY > mapInfo.map[i][j].y) && (myY< mapInfo.map[i][j].y+40))) {
								   mapInfo.map[i][j].state =FREE;
								   itemPlusBomb();
							   		continue;
							   }else if (((myX >= mapInfo.map[i][j].x-10) && (myX<= mapInfo.map[i][j].x+30)) && ((myY+46 >= mapInfo.map[i][j].y-10) && (myY+46< mapInfo.map[i][j].y+40))) {
								   mapInfo.map[i][j].state =FREE;
								   itemPlusBomb();
							   		continue;
							   }
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
	   }//End of colliderControl(MapInfo mapInfo)	//�浹ó���Լ� ��
	   
	   
	   
	   public void itemSpeedUp() { //ĳ���� ������ �ӵ� ����
		   //�ٵ� �̰� �ٽ�¥���� �� �� ���� �ֳĸ� ������ ���ٿ��� ���� ������ ������ ��뵵 �����Ҽ����־
		   //������ ������ repaint ������Ʈ ����(�Ƹ�)�ε� �׽�Ʈ �غ��� �ȵŸ� Ű���μ����� ���������� �ٲ����
		   //�ϴ� 1�ο��϶��� �Ǳ� ��
		   this.speed-=20;
		   if(speed<MaxSpeed)
		   speed = MaxSpeed;
	   }
	   
	   
	   public void itemPlusBomb() {	//�� ���� ������ ��ǳ�� ���� ���� (�ִ� 10)
		   this.bombAvailable+=1;
		   if(bombAvailable>maxBomb)
			   bombAvailable = maxBomb;
	   }
	   
	   
	   public class BombThread implements Runnable {
		   int bombX, bombY;
		   public BombThread(int myX, int myY) {
			  this.bombX = myX;
			  this.bombY = myY;
	    	  System.out.println("1 >>>"+bombX+bombY);
	    	  bombX/=40;
	    	  bombY/=40;
	    	  if (myY !=0) bombY+=1;
	    	  
	    	  System.out.println("�迭��ǥ>>>"+bombX+","+bombY);
		       // store parameter for later user
		   }//End of ������

		   @Override
	         public void run() {
	            if(bombAvailable>0) {
	               dropBomb(bombX, bombY);
	                 try {
	                	 Thread.sleep(3000);
	                	 explodeBomb(bombX,bombY);
	                	 Thread.sleep(400);
	                	 freeBomb(bombX,bombY);
	                 } catch (InterruptedException e) {
	                  // TODO Auto-generated catch block
	                  e.printStackTrace();
	                 }
	            }//End of if	            
	         }//End of run()
		}//End of BombThread
	   
		
	   
	   
	   
	   
	      public void freeBomb(int bombX, int bombY) {
	    	  int randomNum;
	    	  System.out.println("FreeBomb");
	    	  mapInfo.map[bombY][bombX].state = FREE;
	    	  if (bombY-1 >=0) {
	    		  if (mapInfo.map[bombY-1][bombX].wasWall==true) {	  //���� ���°� �� ���¿�����,  		  
	    			  //����� �������� state�� FREE �Ǵ� ������ 1,2�� �����ϴ� �Լ� 
	    			  randomNum = random.nextInt(8);
	    			  mapInfo.map[bombY-1][bombX].wasWall=false; //�� ���
	    			  mapInfo.map[bombY-1][bombX].state =itemArray[randomNum];	    			  
	    	  		}	else {
	    	  				mapInfo.map[bombY-1][bombX].state = FREE;
	    	  				}
	    	  }
	    	  if(bombY+1<15) {
	    		  if (mapInfo.map[bombY+1][bombX].wasWall==true) {	  //���� ���°� �� ���¿�����,  
	    			  randomNum = random.nextInt(8);
	    			  mapInfo.map[bombY+1][bombX].wasWall=false; //�� ���
	    			  mapInfo.map[bombY+1][bombX].state =itemArray[randomNum];	    			  
	 	    	  		}	else {
	 	    	  				mapInfo.map[bombY+1][bombX].state = FREE;
	 	    	  				}
	    	  }
	    		
	    	  if(bombX+1 <15) {
	    		  if (mapInfo.map[bombY][bombX+1].wasWall==true) {	  //���� ���°� �� ���¿�����,  
	    			  randomNum = random.nextInt(8);
	    			  mapInfo.map[bombY][bombX+1].wasWall=false; //�� ���
	    			  mapInfo.map[bombY][bombX+1].state =itemArray[randomNum];	    			  
	 	    	  		}	else {
				 	    		  mapInfo.map[bombY][bombX+1].state = FREE;
				 	    	  	}
	    	  }
	    		 
	    	  if(bombX-1 >=0) {
	    		  if (mapInfo.map[bombY][bombX-1].wasWall==true) {	  //���� ���°� �� ���¿�����,  
	    			  // if (mapInfo.map[bombY-1][bombX].state != FREE) {	    		  
	    			  randomNum = random.nextInt(8);	
	    			  mapInfo.map[bombY][bombX-1].wasWall=false; //�� ���
	    			  mapInfo.map[bombY][bombX-1].state =itemArray[randomNum];	    			  
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

	    	  System.out.println("�迭��ǥ>>>"+bombX+","+bombY);
	    	  mapInfo.map[bombY][bombX].state = BOMB;
	    	  printMap(); 

	    	  System.out.println(">>>"+bombX+","+bombY);
	    	  bombAvailable-=1;
	      }//End of dropBomb()
		



	   
   ///////////////////////////////////////////////////////////////
   //					KeyBoard �̺�Ʈ ó�� + process				//
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
	
	
	
	//�׽�Ʈ�� ���� ��ϵ� ����� ������������ ���� �����س�����
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

	//�̰Ծȵ� �Ф�
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
		   
			Image dblbuff;//������۸��� �����
			Graphics gc;//������۸��� �׷��� ���ؽ�Ʈ

			Image bg; //���ȭ��
			
			 //�� ĳ���� -> ����� ����
			Image bazziUp = new ImageIcon("images/bazzi_back.png").getImage();
			Image bazziDown = new ImageIcon("images/bazzi_front.png").getImage();
			Image bazziLeft = new ImageIcon("images/bazzi_left.png").getImage();
			Image bazziRight = new ImageIcon("images/bazzi_right.png").getImage();
			   
			 //��� ĳ���� -> ��Ϸ� ����
			Image uniUp = new ImageIcon("images/woonie_back.png").getImage();
			Image uniDown = new ImageIcon("images/woonie_front.png").getImage();
			Image uniLeft = new ImageIcon("images/woonie_left.png").getImage();
			Image uniRight = new ImageIcon("images/woonie_right.png").getImage();
			 
			 //�ڽ�
			Image iconBoxBrown = new ImageIcon("images/cookie.png").getImage();
			Image iconBoxPink = new ImageIcon("images/cookie2.png").getImage();
			Image iconItemSpeed = new ImageIcon("images/speed.png").getImage();
			Image iconBomb = new ImageIcon("images/bomb.png").getImage();
			
			//��ǳ��
			Image iconBumb = new ImageIcon("images/bomb.png").getImage();
			Image iconBup = new ImageIcon("images/bup.png").getImage();
			Image iconBdown = new ImageIcon("images/bdown.png").getImage();
			Image iconBleft = new ImageIcon("images/bleft.png").getImage();
			Image iconBright = new ImageIcon("images/bright.png").getImage();
			Image iconBcenter = new ImageIcon("images/bcenter.png").getImage();
			
	//////////////////////////////////////////////////////////////////////////////////////////
			
			GameScreen (GameStart main){
				this.main = main;
			}//End of GameScreen(������)
			
			
			
			public void paint(Graphics g){
				if(gc==null) {
					dblbuff=createImage(main.gScreenWidth,main.gScreenHeight);//���� ���۸��� ������ũ�� ���� ����. ���� paint �Լ� ������ �� ��� �Ѵ�. �׷��� ������ null�� ��ȯ�ȴ�.
					if(dblbuff==null) System.out.println("������ũ�� ���� ���� ����");
					else gc=dblbuff.getGraphics();//������ũ�� ���ۿ� �׸��� ���� �׷��� ���ؽ�Ʈ ȹ��
					return;
				}
				update(g);
			}//End of paint(Graphics g)
			
			
			
			public void update(Graphics g){//ȭ�� ���ڰŸ��� ���̱� ����, paint���� ȭ���� �ٷ� ��ȭ���� �ʰ� update �޼ҵ带 ȣ���ϰ� �Ѵ�.
				//cnt=main.cnt;
				//gamecnt=main.gamecnt;
				if(gc==null) return;
				dblpaint();//������ũ�� ���ۿ� �׸���
				g.drawImage(dblbuff,0,0,this);//������ũ�� ���۸� ����ȭ�鿡 �׸���.
			}//End of update(Graphics g)
			
			
			
			public void dblpaint(){  
				//���� �׸��� ������ �� �Լ����� ��� ���Ѵ�.				
				Draw_BG (); // �� ���ȭ�� (��ũ) �׸���
				Draw_Blocks(); //��� �׸���
				Draw_myChracter(); //�� ĳ���� (����)�׸���
				Draw_yourChracter(); //��� ĳ����(���) �׸���
				//DrawBomb();	//��ǳ���׸���
				//ExplodeBomb();  //��ǳ�� ���� �׸���
			}//End of dblpaint()

			
			
			public void Draw_BG() {
				gc.drawImage(mapBG,0,0,this);
			}//End of Draw_BG()
			
			
			
			public void Draw_Blocks() {				
				/*
				 * //�׵θ� (15*15) for (int i = 0; i<=560; i+=40) {
				 * gc.drawImage(boxBrown,i,0,this); } for (int i = 40; i<=520; i+=40) {
				 * gc.drawImage(boxBrown,0,i,this); gc.drawImage(boxBrown,560,i,this); } for
				 * (int i = 0; i<=560; i+=40) { gc.drawImage(boxBrown,i,560,this); }
				 */							
			   for (int i = 0; i<15; i++) {
				   for (int j =0; j<15; j++) {
					   switch(mapInfo.map[i][j].state) {
					   
					   //WALL
						   case BROWNBLOCK :
							   gc.drawImage(iconBoxBrown,mapInfo.map[i][j].x,mapInfo.map[i][j].y,this);
							   break;
						   case PINKBLOCK :
							   gc.drawImage(iconBoxPink,mapInfo.map[i][j].x,mapInfo.map[i][j].y,this);
							   break;
							   
						//BOMB	   
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
							   
						//ITEM
						   case ITEMSPEED:
							   gc.drawImage( new ImageIcon("images/speed.png").getImage(),mapInfo.map[i][j].x,mapInfo.map[i][j].y,this);
							   break;
						   case ITEMSTRONGBOMB:
							   gc.drawImage( new ImageIcon("images/speed.png").getImage(),mapInfo.map[i][j].x,mapInfo.map[i][j].y,this);
							   break;
						   case ITEMPLUSBOMB:							   
							   gc.drawImage( new ImageIcon("images/plusBomb2.png").getImage(),mapInfo.map[i][j].x,mapInfo.map[i][j].y,this);
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
			
					
			/*  ���׸��°ɷ� ��ħ
			 * public void DrawBomb() { //������ ��ǳ�� �׸��� if (bombAvailable == false) {
			 * gc.drawImage(iconBumb, bombX, bombY,this); } else { gc.drawImage(null, bombX,
			 * bombY,this); } }
			 */	
			
			/*  ���׸��°ɷ� ��ħ
			 * public void ExplodeBomb() { //������ ��ǳ�� �׸��� if (isBombExplode == true) {
			 * gc.drawImage(iconBcenter, bombX, bombY,this); gc.drawImage(iconBup, bombX,
			 * bombY-40,this); gc.drawImage(iconBdown, bombX, bombY+40,this);
			 * gc.drawImage(iconBleft, bombX-40, bombY,this); gc.drawImage(iconBright,
			 * bombX+40, bombY,this); } else { gc.drawImage(null, bombX, bombY,this); } }
			 */
			
			
	   }//End of GameCanvas(class)
   ///////////////////////////////////////////////////////////////
   //						GameCanvas ��						//
   ///////////////////////////////////////////////////////////////

	   
	   
   ///////////////////////////////////////////////////////////////
   //						���� ������, ���� �Լ�					//
   ///////////////////////////////////////////////////////////////
			@Override
			public void run() {//���� ������
				// TODO Auto-generated method stub
				
				while(roof) {
					gamescreen.repaint();//ȭ�� ������Ʈ
					colliderControl(mapInfo); //�浹ó��
					keyProcess();	//Ű���� ó��
					
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
