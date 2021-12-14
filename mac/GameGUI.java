import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Image;
import java.awt.Point;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JFrame;

import javax.imageio.ImageIO;

import java.util.Random;

/**
 * A Game board on which to place and move players.
 * 
 * @author PLTW
 * @version 1.0
 */
public class GameGUI extends JComponent
{
  static final long serialVersionUID = 141L; // problem 1.4.1

  //private static final int WIDTH = 510;
  private static final int GRID_W = 29;
  private static final int GRID_H = 16;
  public static int WIDTH = 60*GRID_W+14;
  public static int HEIGHT = 60*GRID_H+36;
  private static final int SPACE_SIZE = 60;
  public static final int START_LOC_X = 15;
  public static final int START_LOC_Y = 15;
  public static final int START_LOC_X2 = 15;
  public static final int START_LOC_Y2 = (GRID_H-1)*60+15;
  
  public static int pSize = 40;

  // initial placement of player
  public int x = START_LOC_X; 
  public int y = START_LOC_Y;
  public int x2 = START_LOC_X2;
  public int y2 = START_LOC_Y2;

  // grid image to show in background
  private Image bgImage;

  // player image and info
  private Image player;
  public Point playerLoc;
  private int playerSteps;
  private Image player2;
  public Point player2Loc;
  private int player2Steps;

  // walls, prizes, traps
  private int totalWalls;
  private Rectangle[] walls;
  private int totalPrizes;
  private Rectangle[] prizes;
  private int totalTraps;
  private Rectangle[] traps;
  private Image wallImage;
  private Image wall2Image;
  private Image prizeImage;
  private Image trapImage;
  private Image trappedPrizeImage;

  private Image frankImage;
  private Image overlayImage;

  public int imageMode = 1;

  // scores, sometimes awarded as (negative) penalties
  private int prizeVal = 10;
  private int trapVal = -5;
  private int endVal = 10;
  private int offGridVal = 5; // penalty only
  private int hitWallVal = 5;  // penalty only

  private static final int texts = 4;
  private String[] text = new String[texts];
  private int[] textx = new int[texts];
  private int[] texty = new int[texts];
  private int[] textS = new int[texts];

  public boolean overlay = false;

  // game frame
  public JFrame frame;

  /**
   * Constructor for the GameGUI class.
   * Creates a frame with a background image and a player that will move around the board.
   */
  public GameGUI()
  {
    try {
      
      bgImage = ImageIO.read(EscapeRoom.class.getResource("grid.png"));
    } catch (Exception e) {
      System.err.println("Could not open file GRID.png");
    }      
    try {
      prizeImage = ImageIO.read(EscapeRoom.class.getResource("coin.png"));
    } catch (Exception e) {
      System.err.println("Could not open file COIN.png");
    }
    try {
      trapImage = ImageIO.read(EscapeRoom.class.getResource("steven.png"));
    } catch (Exception e) {
      System.err.println("Could not open file STEVEN.png");
    }
    try {
      wallImage = ImageIO.read(EscapeRoom.class.getResource("wall.png"));
    } catch (Exception e) {
      System.err.println("Could not open file WALL.png");
    }
    try {
      wall2Image = ImageIO.read(EscapeRoom.class.getResource("otherWall.png"));
    } catch (Exception e) {
      System.err.println("Could not open file OTHERWALL.png");
    }
  
    // player image, student can customize this image by changing file on disk
    try {
      player = ImageIO.read(EscapeRoom.class.getResource("dukeLarge.png"));
    } catch (Exception e) {
     System.err.println("Could not open file DUKELARGE.png");
    }
    try {
      player2 = ImageIO.read(EscapeRoom.class.getResource("dukeSmall.png"));
    } catch (Exception e) {
     System.err.println("Could not open file DUKESMALL.png");
    }
    try {
      overlayImage = ImageIO.read(EscapeRoom.class.getResource("overlay.png"));
    } catch (Exception e) {
     System.err.println("Could not open file OVERLAY.png");
    }
    try {
      trappedPrizeImage = ImageIO.read(EscapeRoom.class.getResource("trappedCoin.png"));
    } catch (Exception e) {
     System.err.println("Could not open file TRAPPEDCOIN.png");
    }
    try {
      frankImage = ImageIO.read(EscapeRoom.class.getResource("frank.png"));
    } catch (Exception e) {
     System.err.println("Could not open file FRANK.png");
    }
    
    // save player location
    playerLoc = new Point(x,y);
    player2Loc = new Point(x2,y2);

    // create the game frame
    frame = new JFrame("7-11");
    frame.setSize(WIDTH, HEIGHT);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.add(this);
    frame.setVisible(true);
    frame.setResizable(false);

    // set default config
    totalWalls = 100;
    Random rand = new Random();
    totalPrizes = rand.nextInt(20) + 10;
    totalTraps = rand.nextInt(20) + 10;
  }
 /**
  * After a GameGUI object is created, this method adds the walls, prizes, and traps to the gameboard.
  * Note that traps and prizes may occupy the same location.
  */
  public void createBoard()
  {
    traps = new Rectangle[totalTraps];
    createTraps();
    
    prizes = new Rectangle[totalPrizes];
    createPrizes();

    walls = new Rectangle[totalWalls];
    createWalls();
  }

  /**
   * Increment/decrement the player location by the amount designated.
   * This method checks for bumping into walls and going off the grid,
   * both of which result in a penalty.
   * <P>
   * precondition: amount to move is not larger than the board, otherwise player may appear to disappear
   * postcondition: increases number of steps even if the player did not actually move (e.g. bumping into a wall)
   * <P>
   * @param incrx amount to move player in x direction
   * @param incry amount to move player in y direction
   * @return penalty score for hitting a wall or potentially going off the grid, 0 otherwise
   */
  public int movePlayer(int incrx, int incry,int plIn)
  {
      int newX = x;
      int newY = y;
      if (plIn == 1) {
        newX = x + (incrx*(pSize/40));
        newY = y + (incry*(pSize/40));
        // increment regardless of whether player really moves
        playerSteps++;
      } else if (plIn == 2) {
        newX = x2 + (incrx*(pSize/40));
        newY = y2 + (incry*(pSize/40));
        // increment regardless of whether player really moves
        player2Steps++;
      }

      // check if off grid horizontally and vertically
      if ( (newX < 0 || newX > SPACE_SIZE*GRID_W) || (newY < 0 || newY > SPACE_SIZE*GRID_H) )
      {
        //System.out.println ("OFF THE GRID!");
        return -offGridVal;
      }

      int xI = 0;
      int yI = 0;
      if (plIn == 1) {
        xI = x;
        yI = y;
      } else if (plIn == 2) {
        xI = x2;
        yI = y2;
      }
      // determine if a wall is in the way
      for (Rectangle r: walls)
      {
        // this rect. location
        int startX =  (int)r.getX();
        int endX  =  (int)r.getX() + (int)r.getWidth();
        int startY =  (int)r.getY();
        int endY = (int) r.getY() + (int)r.getHeight();

        // (Note: the following if statements could be written as huge conditional but who wants to look at that!?)
        // moving RIGHT, check to the right
        if (((incrx*(pSize/40)) > 0) && (xI <= startX) && (startX <= newX) && (yI >= startY) && (yI <= endY))
        {
          //System.out.println("A WALL IS IN THE WAY");
          return -hitWallVal;
        }
        // moving LEFT, check to the left
        else if (((incrx*(pSize/40)) < 0) && (xI >= startX) && (startX >= newX) && (yI >= startY) && (yI <= endY))
        {
          //System.out.println("A WALL IS IN THE WAY");
          return -hitWallVal;
        }
        // moving DOWN check below
        else if (((incry*(pSize/40)) > 0) && (yI <= startY && startY <= newY && xI >= startX && xI <= endX))
        {
          //System.out.println("A WALL IS IN THE WAY");
          return -hitWallVal;
        }
        // moving UP check above
        else if (((incry*(pSize/40)) < 0) && (yI >= startY) && (startY >= newY) && (xI >= startX) && (xI <= endX))
        {
          //System.out.println("A WALL IS IN THE WAY");
          return -hitWallVal;
        }     
      }

      // all is well, move player
      if (plIn == 1) {
        x += (incrx*(pSize/40));
        y += (incry*(pSize/40));
      } else if (plIn == 2) {
        x2 += (incrx*(pSize/40));
        y2 += (incry*(pSize/40));
      }
      repaint();   
      return 0;   
  }
  public void toEnd(int plIn) {
    if (plIn == 1) {
      x = (GRID_W-1)*SPACE_SIZE+15;
    } else if (plIn == 2) {
      x2 = (GRID_W-1)*SPACE_SIZE+15;
    }
    repaint();
  }

  /**
   * Check the space adjacent to the player for a trap. The adjacent location is one space away from the player, 
   * designated by newx, newy.
   * <P>
   * precondition: newx and newy must be the amount a player regularly moves, otherwise an existing trap may go undetected
   * <P>
   * @param newx a location indicating the space to the right or left of the player
   * @param newy a location indicating the space above or below the player
   * @return true if the new location has a trap that has not been sprung, false otherwise
   */
  public boolean isTrap(int newx, int newy,int plIn) {
    double px = 0;
    double py = 0;
    if (plIn == 1) {
      px = playerLoc.getX() + newx;
      py = playerLoc.getY() + newy;
    } else if (plIn == 2) {
      px = player2Loc.getX() + newx;
      py = player2Loc.getY() + newy;
    }

    for (Rectangle r: traps) {
      // DEBUG: System.out.println("trapx:" + r.getX() + " trapy:" + r.getY() + "\npx: " + px + " py:" + py);
      // zero size traps have already been sprung, ignore
      if (r.getWidth() > 0 && r.width != 28) {
        // if new location of player has a trap, return true
        if (r.contains(px, py)) {
          //System.out.println("A TRAP IS AHEAD");
          return true;
        }
        
        if (r.contains(px, py)) {
          r.setSize(29,29);
        } else if (r.contains(px+SPACE_SIZE, py)) {
          r.setSize(29,29);
        } else if (r.contains(px, py-SPACE_SIZE)) {
          r.setSize(29,29);
        } else if (r.contains(px, py+SPACE_SIZE)) {
          r.setSize(29,29);
        }
      }
    }
    // there is no trap where player wants to go
    return false;
  }

  /**
   * Spring the trap. Traps can only be sprung once and attempts to spring
   * a sprung task results in a penalty.
   * <P>
   * precondition: newx and newy must be the amount a player regularly moves, otherwise an existing trap may go unsprung
   * <P>
   * @param newx a location indicating the space to the right or left of the player
   * @param newy a location indicating the space above or below the player
   * @return a positive score if a trap is sprung, otherwise a negative penalty for trying to spring a non-existent trap
   */
  public int springTrap(int newx, int newy, int plIn)
  {
    double px = 0;
    double py = 0;
    if (plIn == 1) {
      px = playerLoc.getX() + newx;
      py = playerLoc.getY() + newy;
    } else if (plIn == 1) {
      px = player2Loc.getX() + newx;
      py = player2Loc.getY() + newy;
    }

    // check all traps, some of which may be already sprung
    for (Rectangle r: traps)
    {
      // DEBUG: System.out.println("trapx:" + r.getX() + " trapy:" + r.getY() + "\npx: " + px + " py:" + py);
      if (r.contains(px, py))
      {
        // zero size traps indicate it has been sprung, cannot spring again, so ignore
        if (r.getWidth() > 0)
        {
          r.setSize(0,0);
          //repaint();
          //System.out.println("TRAP IS SPRUNG!");
          return trapVal;
        }
      }
    }
    return 0;
  }

  /**
   * Pickup a prize and score points. If no prize is in that location, this results in a penalty.
   * <P>
   * @return positive score if a location had a prize to be picked up, otherwise a negative penalty
   */
  public int pickupPrize(int plIn)
  {
    double px = 0;
    double py = 0;
    if (plIn == 1) {
      px = playerLoc.getX();
      py = playerLoc.getY();
    } else if (plIn == 2) {
      px = player2Loc.getX();
      py = player2Loc.getY();
    }

    for (Rectangle p: prizes)
    {
      // DEBUG: System.out.println("prizex:" + p.getX() + " prizey:" + p.getY() + "\npx: " + px + " py:" + py);
      // if location has a prize, pick it up
      if (p.getWidth() > 0 && p.contains(px, py))
      {
        if (p.width != 16) {
          //System.out.println("YOU PICKED UP A PRIZE!");
          p.setSize(0,0);
          repaint();
          return prizeVal;
        } else {
          //System.out.println("THAT PRIZE WAS TRAPPED!");
          p.setSize(0,0);
          return (int)(prizeVal*-2.5);
        }
      }
    }
    //System.out.println("OOPS, NO PRIZE HERE");
    return -prizeVal;
  }

  /**
   * Return the numbers of steps the player has taken.
   * <P>
   * @return the number of steps
   */
  public int getSteps(int plIn)
  {
    if (plIn == 1) {
      return playerSteps;
    } else if (plIn == 2) {
      return player2Steps;
    } else {
      return 0;
    }
  }
  
  /**
   * Set the designated number of prizes in the game.  This can be used to customize the gameboard configuration.
   * <P>
   * precondition p must be a positive, non-zero integer
   * <P>
   * @param p number of prizes to create
   */
  public void setPrizes(int p) 
  {
    totalPrizes = p;
  }
  
  /**
   * Set the designated number of traps in the game. This can be used to customize the gameboard configuration.
   * <P>
   * precondition t must be a positive, non-zero integer
   * <P>
   * @param t number of traps to create
   */
  public void setTraps(int t) 
  {
    totalTraps = t;
  }
  
  /**
   * Set the designated number of walls in the game. This can be used to customize the gameboard configuration.
   * <P>
   * precondition t must be a positive, non-zero integer
   * <P>
   * @param w number of walls to create
   */
  public void setWalls(int w) 
  {
    totalWalls = w;
  }

  /**
   * Reset the board to replay existing game. The method can be called at any time but results in a penalty if called
   * before the player reaches the far right wall.
   * <P>
   * @return positive score for reaching the far right wall, penalty otherwise
   */
  public int replay()
  {

    int win = playerAtEnd();
  
    // resize prizes and traps to "reactivate" them
    for (Rectangle p: prizes)
      p.setSize(SPACE_SIZE/3, SPACE_SIZE/3);
    for (Rectangle t: traps)
      t.setSize(SPACE_SIZE/3, SPACE_SIZE/3);

    // move player to start of board
    x = START_LOC_X;
    y = START_LOC_Y;
    playerSteps = 0;
    x2 = START_LOC_X2;
    y2 = START_LOC_Y2;
    player2Steps = 0;
    repaint();
    return win;
  }

 /**
  * End the game, checking if the player made it to the far right wall.
  * <P>
  * @return positive score for reaching the far right wall, penalty otherwise
  */
  public int endGame() 
  {
    int win = playerAtEnd();
    return win;
  }

  /*------------------- public methods not to be called as part of API -------------------*/

  /** 
   * For internal use and should not be called directly: Users graphics buffer to paint board elements.
   */
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D)g;

    // draw grid
    //x=68
    //y=32
    if (imageMode == 1) {
      g.drawImage(bgImage, 0, 0, 60*8,60*5,null);
      g.drawImage(bgImage, 60*8, 0, 60*8,60*5,null);
      g.drawImage(bgImage, 60*16, 0, 60*8,60*5,null);
      g.drawImage(bgImage, 60*24, 0, 60*8,60*5,null);
  
      g.drawImage(bgImage, 0, 60*5, 60*8,60*5,null);
      g.drawImage(bgImage, 60*8, 60*5, 60*8,60*5,null);
      g.drawImage(bgImage, 60*16, 60*5, 60*8,60*5,null);
      g.drawImage(bgImage, 60*24, 60*5, 60*8,60*5,null);
  
      g.drawImage(bgImage, 0, 60*10, 60*8,60*5,null);
      g.drawImage(bgImage, 60*8, 60*10, 60*8,60*5,null);
      g.drawImage(bgImage, 60*16, 60*10, 60*8,60*5,null);
      g.drawImage(bgImage, 60*24, 60*10, 60*8,60*5,null);
  
      g.drawImage(bgImage, 0, 60*15, 60*8,60*5,null);
      g.drawImage(bgImage, 60*8, 60*15, 60*8,60*5,null);
      g.drawImage(bgImage, 60*16, 60*15, 60*8,60*5,null);
      g.drawImage(bgImage, 60*24, 60*15, 60*8,60*5,null);
    } else if (imageMode == 2) {
      g.drawImage(frankImage, 0, 0, 60*8,60*5,null);
      g.drawImage(frankImage, 60*8, 0, 60*8,60*5,null);
      g.drawImage(frankImage, 60*16, 0, 60*8,60*5,null);
      g.drawImage(frankImage, 60*24, 0, 60*8,60*5,null);
  
      g.drawImage(frankImage, 0, 60*5, 60*8,60*5,null);
      g.drawImage(frankImage, 60*8, 60*5, 60*8,60*5,null);
      g.drawImage(frankImage, 60*16, 60*5, 60*8,60*5,null);
      g.drawImage(frankImage, 60*24, 60*5, 60*8,60*5,null);
  
      g.drawImage(frankImage, 0, 60*10, 60*8,60*5,null);
      g.drawImage(frankImage, 60*8, 60*10, 60*8,60*5,null);
      g.drawImage(frankImage, 60*16, 60*10, 60*8,60*5,null);
      g.drawImage(frankImage, 60*24, 60*10, 60*8,60*5,null);
  
      g.drawImage(frankImage, 0, 60*15, 60*8,60*5,null);
      g.drawImage(frankImage, 60*8, 60*15, 60*8,60*5,null);
      g.drawImage(frankImage, 60*16, 60*15, 60*8,60*5,null);
      g.drawImage(frankImage, 60*24, 60*15, 60*8,60*5,null);
    }

    // add (invisible) traps
    for (Rectangle t : traps)
    {
      if (t.getWidth() == 0 || t.getWidth() == 29) 
      {
        int x = (int)t.getX();
        int y = (int)t.getY();
        if (imageMode == 1) {
          g.drawImage(trapImage, x, y,50,50, null);
        } else if (imageMode == 2) {
          g.drawImage(frankImage, x, y,50,50, null);
        }
      }
    }

    // add prizes
    for (Rectangle p : prizes)
    {
      // picked up prizes are 0 size so don't render
      if (p.getWidth() > 0)
      {
        int px = (int)p.getX();
        int py = (int)p.getY();
        if (imageMode == 1) {
          if (p.width == 16) {
            g.drawImage(trappedPrizeImage, px, py, 40,40,null);
          } else {
            g.drawImage(prizeImage, px, py, 40,40,null);
          }
        } else if (imageMode == 2) {
          g.drawImage(frankImage, px, py, 40,40,null);
        }
      }
    }

    // add walls
    for (Rectangle r : walls) 
    {
      if (r.getWidth() == 14) 
      {
        int x = (int)r.getX();
        int y = (int)r.getY();
        if (imageMode == 1){
          g.drawImage(wallImage, x, y, (int)r.getWidth(),(int)r.getHeight(),null);
        } else if (imageMode == 2){
          g.drawImage(frankImage, x, y, (int)r.getWidth(),(int)r.getHeight(),null);
        }
      } else if (r.getHeight() == 14) {
        int x = (int)r.getX();
        int y = (int)r.getY();
        if (imageMode == 1){
          g.drawImage(wall2Image, x, y, (int)r.getWidth(),(int)r.getHeight(),null);
        } else if (imageMode == 2){
          g.drawImage(frankImage, x, y, (int)r.getWidth(),(int)r.getHeight(),null);
        }
      }
      //g2.setPaint(Color.BLACK);
      //g2.fill(r);
    }

    // draw player, saving its location
    if (imageMode == 1) {
      g.drawImage(player, x, y, pSize,pSize, null);
      playerLoc.setLocation(x,y);
      g.drawImage(player2, x2, y2, pSize,pSize, null);
      player2Loc.setLocation(x2,y2);
    } else if (imageMode == 2) {
      g.drawImage(frankImage, x, y, pSize,pSize, null);
      playerLoc.setLocation(x,y);
      g.drawImage(frankImage, x2, y2, pSize,pSize, null);
      player2Loc.setLocation(x2,y2);
    }


    if (overlay) {
      g.drawImage(overlayImage, 0, 0, WIDTH, HEIGHT, null);
    }

    //render text on screen
    for (int i = 0; i < texts; i++) {
      if (text[i] != null && text[i] != "") {
        Font font = new Font("Arial", Font.PLAIN, textS[i]);
        g2.setFont(font);
        g2.drawString(text[i],textx[i],texty[i]);
      }
    }
  }

  /*------------------- private methods -------------------*/

  /*
   * Add randomly placed prizes to be picked up.
   * Note:  prizes and traps may occupy the same location, with traps hiding prizes
   */
  private void createPrizes()
  {
    int s = SPACE_SIZE; 
    Random rand = new Random();
     for (int numPrizes = 0; numPrizes < totalPrizes; numPrizes++)
     {
      int h = rand.nextInt(GRID_H);
      int w = rand.nextInt(GRID_W);
      while ( (w == (START_LOC_X-15)/s && h == (START_LOC_Y-15)/s) || (w == (START_LOC_X2-15)/s && h == (START_LOC_Y2-15)/s)) {
        h = rand.nextInt(GRID_H);
        w = rand.nextInt(GRID_W);
      }
      boolean trappedPrize = false;
      for ( Rectangle trap : traps) {
        if (trap.x == w*s+5 && trap.y == h*s+5) {
          trappedPrize = true;
          trap.width = 28;
          trap.height = 28;
          break;
        }
      }
      Rectangle r;
      if (!trappedPrize) {
        r = new Rectangle((w*s + 15),(h*s + 15), 15, 15);
      } else {
        r = new Rectangle((w*s + 15),(h*s + 15), 16, 16);
      }
      prizes[numPrizes] = r;
     }
  }

  /*
   * Add randomly placed traps to the board. They will be painted white and appear invisible.
   * Note:  prizes and traps may occupy the same location, with traps hiding prizes
   */
  private void createTraps()
  {
    int s = SPACE_SIZE; 
    Random rand = new Random();
     for (int numTraps = 0; numTraps < totalTraps; numTraps++)
     {
      int h = rand.nextInt(GRID_H);
      int w = rand.nextInt(GRID_W);
      while ( (w == (START_LOC_X-5)/60 && h == (START_LOC_Y-5)/60) || (w == (START_LOC_X2-5)/60 && h == (START_LOC_Y2-5)/60)) {
        h = rand.nextInt(GRID_H);
        w = rand.nextInt(GRID_W);
      }

      Rectangle r;
      r = new Rectangle((w*s + 5),(h*s + 5), 30, 30);
      traps[numTraps] = r;
     }
  }

  /*
   * Add walls to the board in random locations 
   */
  private void createWalls()
  {
     int s = SPACE_SIZE;

     Random rand = new Random();
     for (int numWalls = 0; numWalls < totalWalls; numWalls++)
     {
      int h = rand.nextInt(GRID_H);
      int w = rand.nextInt(GRID_W);

      Rectangle r;
       if (rand.nextInt(2) == 0) 
       {
         // vertical wall
         r = new Rectangle((w*s + s - 7),h*s, 14,s);
       }
       else
       {
         /// horizontal
         r = new Rectangle(w*s-4,(h*s + s - 7), s+7, 14);
       }
       walls[numWalls] = r;
     }
  }

  /**
   * Checks if player as at the far right of the board 
   * @return positive score for reaching the far right wall, penalty otherwise
   */
  public int playerAtEnd()
  {
    int score;

    double px = playerLoc.getX();
    double px2 = player2Loc.getX();
    if (px > (GRID_W-2)*SPACE_SIZE && px2 > (GRID_W-2)*SPACE_SIZE)
    {
      //System.out.println("YOU MADE IT!");
      score = endVal;
    }
    else
    {
      //System.out.println("OOPS, YOU QUIT TOO SOON!");
      score = -endVal;
    }
    return score;
  }
  public int switchImages() {
    if (imageMode == 2) { imageMode = 1;
    } else if (imageMode == 1) { imageMode = 2; }
    repaint();
    return imageMode;
  }
  public int switchImages(Thread thr) {
    if (imageMode == 2) { imageMode = 1;
    } else if (imageMode == 1) { imageMode = 2; }
    repaint();
    thr.start();
    return imageMode;
  }
  public void DUKELARGE() {
    if (pSize == 40) {
      pSize = 120;
    } else {
      pSize = 40;
    }
    repaint();
  }
  public void moveRand() {
    Random rand = new Random();
    x = rand.nextInt(GRID_W)*60+15;
    y = rand.nextInt(GRID_H)*60+15;
    x2 = rand.nextInt(GRID_W)*60+15;
    y2 = rand.nextInt(GRID_H)*60+15;
    repaint();
  }
  public void text(int index, String text, int x, int y, int size) {
    this.text[index-1] = text;
    textx[index-1] = x;
    texty[index-1] = y;
    textS[index-1] = size;
  }
  public void repaintScreen() {
    repaint();
  }public void repaintScreen(Thread thr) {
    repaint();
    thr.start();
  }
}