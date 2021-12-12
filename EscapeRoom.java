/*
* Problem 1: Escape Room
* 
* V1.0
* 10/10/2019
* Copyright(c) 2019 PLTW to present. All rights reserved
*/
import javax.swing.*;

import java.awt.Dimension;
import java.awt.event.*;

//audio imports and stuf
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Random;

import javax.sound.sampled.Clip;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Create an escape room game where the player must navigate
 * to the other side of the screen in the fewest steps, while
 * avoiding obstacles and collecting prizes.
 */

public class EscapeRoom implements ActionListener
{
  // describe the game with brief welcome message
  // Allow game commands:
  //    right, left, up, down: if you try to go off grid or bump into wall, score decreases
  //    jump over 1 space: you cannot jump over walls
  //    if you land on a trap, spring a trap to increase score: you must first check if there is a trap, if none exists, penalty
  //    pick up prize: score increases, if there is no prize, penalty
  //    quit: reach the far right wall, score increase, game ends, if game ended without reaching far right wall, penalty
  //    replay: shows number of player steps and resets the board, you or another player can play the same board
  //    switch: switches from one player to the other
  // Note that you must adjust the score with any method that returns a score
  // Optional: create a custom image for your player use the file player.png on disk

  private static GameGUI game;
  private static EscapeRoom esc;
  private static int player = 1;
  // size of move
  private static final int m = 60;
  // individual player moves
  private static int px = 0;
  private static int py = 0;
  private static int score = 0;
  private static JFrame frame;
  private static boolean[] musicIds = new boolean[5];
  private static boolean mute = false;
  private static boolean isStroke = false;
  private static int strokeType = 0;
  private static Thread onEnter;
  private static boolean onEnterNull = true;
  private static JButton[] buttons;
  private static KeyListener listener;
  
  public static void main(String[] args)
  {
    // welcome message
    System.out.println("Welcome to EscapeRoom!");
    System.out.println("Get to the other side of the room, avoiding walls and invisible traps,");
    System.out.println("pick up all the prizes.\n");

    game = new GameGUI();
    game.createBoard();
    esc = new EscapeRoom();
    esc.Setup();
    listener = esc.new MyKeyListener();
    game.frame.addKeyListener(listener);
    game.frame.setFocusable(true);
    
    //play bg music
    if (!mute) {
      loopAudioT2("music.wav","music2.wav",0);
      musicIds[0] = true;
    }
    
    //display text on screen
    game.text(1,"score: " + score,29*60-10*21,16*60-10,40);
    game.text(2,"player: " + player,29*60-11*19,16*60-50,40);
  }
  public void Setup() {
    frame = new JFrame("Menu");
    ButtonData[] buttonsData = {
      new ButtonData("right", 200, 100, 50, 50),
      new ButtonData("jumpright", 250, 100, 100, 50),
      new ButtonData("left", 100, 100, 50, 50),
      new ButtonData("jumpleft", 0, 100, 100, 50),
      new ButtonData("up", 150, 50, 50, 50),
      new ButtonData("jumpup", 125, 0, 100, 50),
      new ButtonData("down", 150, 150, 50, 50),
      new ButtonData("jumpdown", 125, 200, 100, 50),
      new ButtonData("pickup", 400, 0, 100, 50),
      new ButtonData("quit", 400, 50, 100, 50),
      new ButtonData("replay", 400, 100, 100, 50),
      new ButtonData("switch", 400, 150, 100, 50),
      new ButtonData("mute", 400, 200, 100, 50),
      new ButtonData("konami", 0, 250, 100, 50),
      new ButtonData("frank", 100, 250, 100, 50),
      new ButtonData("stroke", 200, 250, 100, 50)
    };
    buttons = new JButton[buttonsData.length];
    for(int i = 0; i < buttons.length; i++) {
      buttons[i] = new JButton(buttonsData[i].name);
      buttons[i].setBounds(buttonsData[i].x, buttonsData[i].y, buttonsData[i].width, buttonsData[i].height);
      buttons[i].addActionListener((ActionListener) this);
    }
    frame.getContentPane().setLayout(null);
    for(int i = 0; i < buttons.length; i++) {
      frame.getContentPane().add(buttons[i]);
    }
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(500+15,250+25+10);

    frame.addKeyListener(listener);
    frame.setFocusable(true);
    frame.setResizable(true);
    frame.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent componentEvent) {
        if (!isStroke) {
          Dimension dimension = frame.getSize();
          if (dimension.width != 500+15 || dimension.height < 250+25+10 || dimension.height > 250+25+10+50) {
            int newWidth = 500+15;
            int newHeight = dimension.height;
            if (dimension.height < 250+25+10) {
              newHeight = 250+25+10;
            } else if (dimension.height > 250+25+10+50) {
              newHeight = 250+25+10+50;
            }
            try {
              Thread.sleep(125);
              frame.setSize(newWidth,newHeight);
            } catch (Exception e) { }
          }
        }
      }
    });
    frame.setVisible(true);
  }
  public class MyKeyListener implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) {}
		@Override
		public void keyPressed(KeyEvent e) {
      if (KeyEvent.getKeyText(e.getKeyCode()).equals("Semicolon")) {
        if (frame.getSize().height == 250+25+10) {
          frame.setSize(500+15,250+25+10+50);
        } else {
          frame.setSize(500+15,250+25+10);
        }
      } else if (KeyEvent.getKeyText(e.getKeyCode()).equals("Enter")) {
        if (!onEnterNull) {
          onEnter.start();
          onEnterNull = true;
        }
      } else {
        //println(KeyEvent.getKeyText(e.getKeyCode()));
      }
		}
		@Override
		public void keyReleased(KeyEvent e) {}
	}
  public void actionPerformed(ActionEvent event) {

    String buttonName = event.getActionCommand();
    if (buttonName == "right") {
      px=m;
    } else if (buttonName == "left") {
      px=-m;
    } else if (buttonName == "up") {
      py=-m;
    } else if (buttonName == "down") {
      py=m;
    } else if (buttonName == "jumpright") {
      px=m*2;
    } else if (buttonName == "jumpleft") {
      px=-m*2;
    } else if (buttonName == "jumpup") {
      py=-m*2;
    } else if (buttonName == "jumpdown") {
      py=m*2;
    } else if (buttonName == "pickup") {
      int temp = game.pickupPrize(player);
    if (temp > 0) { playAudio("coin2.wav"); }
      score += temp;
      game.text(1,"score: " + score,29*60-10*21,16*60-10,40);
      game.repaintScreen();
    } else if (buttonName == "quit") {
      int temp = game.endGame();
      score += temp;
      frame.setVisible(false);
      frame.dispose();

      game.overlay = true;
      game.text(1,"score: " + score,29*60-10*21,16*60-40,40);
      game.text(2,"steps=" + (game.getSteps(1) + game.getSteps(2)),29*60-10*21,16*60-10,40);
      if (temp > 0) {
        game.text(3,"YOU MADE IT!",29*(int)(60/2)-(14/2*(int)(21/40.0*75)),16*(int)(60/2)-(int)(10/40.0*75),75);
        game.text(4,"Press enter to exit.",29*(int)(60/2)-(18/2*(int)(21/40.0*75)),16*(int)(60/2)+(int)(40/40.0*75),75);
      } else {
        game.text(3,"OOPS, YOU QUIT TOO SOON!",29*(int)(60/2)-(28/2*(int)(21/40.0*75)),16*(int)(60/2)-(int)(10/40.0*75),75);
        game.text(4,"Press enter to exit.",29*(int)(60/2)-(18/2*(int)(21/40.0*75)),16*(int)(60/2)+(int)(40/40.0*75),75);
      }
      game.repaintScreen();
      frame.setVisible(false);
      frame.dispose();
      musicIds[0] = false;
      onEnterNull = false;
      onEnter = new Thread() {
        public void run() {
          game.frame.dispose();
        }
      };
    } else if (buttonName == "replay") {
      int steps = game.getSteps(1) + game.getSteps(2);
      int temp = game.playerAtEnd();
      score += temp;

      game.overlay = true;
      game.text(1,"score: " + score,29*60-10*21,16*60-40,40);
      game.text(2,"steps=" + steps,29*60-10*21,16*60-10,40);
      if (temp > 0) {
        game.text(3,"YOU MADE IT!",29*(int)(60/2)-(14/2*(int)(21/40.0*75)),16*(int)(60/2)-(int)(10/40.0*75),75);
        game.text(4,"Press enter to continue.",29*(int)(60/2)-(22/2*(int)(21/40.0*75)),16*(int)(60/2)+(int)(40/40.0*75),75);
      } else {
        game.text(3,"OOPS, YOU QUIT TOO SOON!",29*(int)(60/2)-(28/2*(int)(21/40.0*75)),16*(int)(60/2)-(int)(10/40.0*75),75);
        game.text(4,"Press enter to continue.",29*(int)(60/2)-(22/2*(int)(21/40.0*75)),16*(int)(60/2)+(int)(40/40.0*75),75);
      }
      game.repaintScreen();

      frame.setVisible(false);
      frame.dispose();
      onEnterNull = false;
      onEnter = new Thread() {
        public void run() {
          game.replay();
          score = 0;
          player = 1;

          game.overlay = false;
          game.text(1,"score: " + score,29*60-11*19,16*60-10,40);
          game.text(2,"",0,0,0);
          game.text(3,"",0,0,0);
          game.text(4,"",0,0,0);
          game.repaintScreen();

          esc.Setup();
        }
      };
    } else if (buttonName == "switch") {
      if (player == 1) { player = 2; } else if (player == 2) { player = 1; }
      game.text(2,"player: " + player,29*60-11*19,16*60-50,40);
      game.repaintScreen();
    } else if (buttonName == "mute") {
      if (mute) {
        mute = false;
        musicIds[0] = true;
        loopAudioT2("music.wav","music2.wav",0);
      } else {
        mute = true;
        musicIds[0] = false;
      }
    } else if (buttonName == "konami") {
      playAudio("konami.wav");
      game.toEnd(player);
    } else if (buttonName == "frank") {
      if (game.imageMode == 1) {
        game.switchImages();
        playAudio("frank.wav");
        musicIds[0] = false;
      } else {
        playAudio("frank.wav");
        game.switchImages();
        if (!mute && musicIds[0] != true) {
          loopAudioT2("music.wav","music2.wav",0);
          musicIds[0] = true;
        }
      }
    } else if (buttonName == "stroke") {
      stroke();
    } else if (buttonName == "stop") {
      stopStroke();
    }
    if (px != 0 || py != 0) {
      int scoreChange = 0;
      if (game.isTrap(px,py,player)) {
        scoreChange += game.springTrap(px,py,player);
        if (scoreChange != 0) {
          playAudio("oogaBooga.wav");
        }
      }
      scoreChange += game.movePlayer(px,py,player);
      if (scoreChange != 0) {
        score += scoreChange;
        game.text(1,"score: " + score,29*60-10*21,16*60-10,40);
        game.repaintScreen();
      }
      px=0;
      py=0;
    }
  }
  public static void strokeActions(boolean thing) {
    playAudio("frank.wav");
    game.switchImages(new Thread(){
      public void run() {
        game.DUKELARGE();
        playAudio("dUKElARGE.wav", new Thread(){
          public void run() {
            game.moveRand();
            if(thing) {
              Random rand = new Random();
              game.frame.setSize((int)((double)GameGUI.WIDTH*(rand.nextDouble()*1.5+0.5)),(int)((double)GameGUI.HEIGHT*(rand.nextDouble()*1.5+0.5)));
            }
            if (isStroke) {
              if (thing) {
                strokeActions(thing);
              } else {
                try {
                  Thread.sleep(10);
                  strokeActions(thing);
                } catch (InterruptedException e) { }
              }
            }
          }
        });
      }
    });
  }
  public static void stroke() {
    isStroke = true;
    strokeType = 1;

    frame.setSize(120,85);
    for (int i = 0; i < buttons.length; i++) {
      frame.remove(buttons[i]);
    }
    buttons[1] = new JButton("stop");
    buttons[1].setBounds(0, 0, 120, 50);
    buttons[1].addActionListener((ActionListener) esc);
    frame.add(buttons[1]);

    game.text(1,"",0,0,0);
    game.text(2,"",0,0,0);
    game.text(3,"Are you proud of yourself? Is this what you wanted? You deserver this.",29*(int)(60/2)-(70/2*(int)(19/40.0*75)),16*(int)(60/2)-(int)(10/40.0*75),75);
    game.text(4,"",0,0,0);
    new Thread() {
      public void run() {
        strokeActions(true);
      }
    }.start();
  }
  public static void stroke2() {
    isStroke = true;
    strokeType = 2;

    frame.setVisible(false);
    frame.dispose();
    game.text(1,"",0,0,0);
    game.text(2,"",0,0,0);
    game.text(3,"That slurpie was poisoned, you will recover in 3 seconds",29*(int)(60/2)-(44/2*(int)(19/40.0*75)),16*(int)(60/2)-(int)(10/40.0*75),60);
    game.text(4,"",0,0,0);
    game.repaint();

    new Thread() {
      public void run() {
        strokeActions(false);
      }
    }.start();
  }
  public static void stopStroke() {
    isStroke = false;
    strokeType = 0;
    try {
      Thread.sleep(125);
      game.frame.setSize(GameGUI.WIDTH,GameGUI.HEIGHT);
      if (game.imageMode == 2) {
        game.switchImages();
      }
      GameGUI.pSize = 40;
      game.x = GameGUI.START_LOC_X;
      game.y = GameGUI.START_LOC_Y;
      game.x2 = GameGUI.START_LOC_X2;
      game.y2 = GameGUI.START_LOC_Y2;
      game.text(1,"score: " + score,29*60-10*21,16*60-40,40);
      game.text(2,"steps=" + (game.getSteps(1) + game.getSteps(2)),29*60-10*21,16*60-10,40);
      if (game.playerAtEnd() > 0) {
        game.text(3,"YOU MADE IT!",29*(int)(60/2)-(14/2*(int)(21/40.0*75)),16*(int)(60/2)-(int)(10/40.0*75),75);
      } else {
        game.text(3,"OOPS, YOU QUIT TOO SOON!",29*(int)(60/2)-(28/2*(int)(21/40.0*75)),16*(int)(60/2)-(int)(10/40.0*75),75);
      }
      game.text(4,"Press enter to continue.",29*(int)(60/2)-(22/2*(int)(21/40.0*75)),16*(int)(60/2)+(int)(40/40.0*75),75);
      game.repaintScreen(new Thread(){
        public void run() {
          musicIds[0] = false;
          frame.setVisible(false);
          frame.dispose();
          onEnterNull = false;
          onEnter = new Thread() {
            public void run() {
              game.frame.dispose();
            }
          };
        }
      });
    } catch (Exception e) { }
  }
  public static void stopStroke2(int x, int y, int x2, int y2) {
    isStroke = false;
    if (strokeType == 2) {
      strokeType = 0;
      try {
        Thread.sleep(250);
        game.frame.setSize(GameGUI.WIDTH,GameGUI.HEIGHT);
        if (game.imageMode == 2) {
          game.switchImages();
        }
        GameGUI.pSize = 40;
        game.x = x;
        game.y = y;
        game.x2 = x2;
        game.y2 = y2;
        game.text(1,"score: " + score,29*60-10*21,16*60-10,40);
        game.text(2,"player: " + player,29*60-11*19,16*60-50,40);
        game.text(3,"",0,0,0);
        game.text(4,"",0,0,0);
        game.repaintScreen();
        esc.Setup();
      } catch (Exception e) { }
    } else {
      stopStroke();
    }
  }
  public static void playAudio(String filename) {
    new Thread() {
        public void run() {
          try {
            InputStream audioSrc = getClass().getResourceAsStream(filename);
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            while (!clip.isRunning())
              Thread.sleep(10);
            while (clip.isRunning())
              Thread.sleep(10);
            clip.close();
          } catch (Exception e) { e.printStackTrace(); }
        }
    }.start();
  }
  public static void playAudio(String filename,Thread thread) {
    new Thread() {
        public void run() {
          try {
            InputStream audioSrc = getClass().getResourceAsStream(filename);
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);
            clip.start();
            while (!clip.isRunning())
              Thread.sleep(10);
            thread.start();
            while (clip.isRunning())
              Thread.sleep(10);
            clip.close();
          } catch (Exception e) { e.printStackTrace(); }
        }
    }.start();
  }
  public static void loopAudio(String filename) {
    new Thread() {
      public void run() {
        while(true) {
          try {
            InputStream audioSrc = getClass().getResourceAsStream(filename);
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
            Clip clip = AudioSystem.getClip();
    
            clip.open(audioStream);
            clip.start();
            while (!clip.isRunning())
                Thread.sleep(10);
            while (clip.isRunning())
                Thread.sleep(10);
            clip.close();
          } catch (Exception e) { e.printStackTrace(); }
        }
      }
    }.start();
  }
  public static void loopAudioT2(String filename, String filename2, int id) {
    new Thread() {
      public void run() {
        try {
          InputStream audioSrc = getClass().getResourceAsStream(filename);
          InputStream bufferedIn = new BufferedInputStream(audioSrc);
          AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
          Clip clip = AudioSystem.getClip();
  
          clip.open(audioStream);
          clip.start();
          while (!clip.isRunning() && musicIds[id])
              Thread.sleep(10);
          while (clip.isRunning() && musicIds[id])
              Thread.sleep(10);
          clip.close();
        } catch (Exception e) { e.printStackTrace(); }
        while(musicIds[id]) {
          try {
            InputStream audioSrc = getClass().getResourceAsStream(filename2);
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
            Clip clip = AudioSystem.getClip();
    
            clip.open(audioStream);
            clip.start();
            while (!clip.isRunning() && musicIds[id])
                Thread.sleep(10);
            while (clip.isRunning() && musicIds[id])
                Thread.sleep(10);
            clip.close();
          } catch (Exception e) { e.printStackTrace(); }
        }
      }
    }.start();
  }
}