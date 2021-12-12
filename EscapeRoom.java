/*
* Problem 1: Escape Room
* 
* V1.0
* 10/10/2019
* Copyright(c) 2019 PLTW to present. All rights reserved
*/
import javax.swing.*;
import java.awt.event.*;
/**
 * Create an escape room game where the player must navigate
 * to the other side of the screen in the fewest steps, while
 * avoiding obstacles and collecting prizes.
 */

//audio imports and stuf
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.BufferedInputStream;
import java.io.InputStream;
import javax.sound.sampled.Clip;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class EscapeRoom implements ActionListener
{
      // describe the game with brief welcome message
      // determine the size (length and width) a player must move to stay within the grid markings
      // Allow game commands:
      //    right, left, up, down: if you try to go off grid or bump into wall, score decreases
      //    jump over 1 space: you cannot jump over walls
      //    if you land on a trap, spring a trap to increase score: you must first check if there is a trap, if none exists, penalty
      //    pick up prize: score increases, if there is no prize, penalty
      //    help: display all possible commands
      //    end: reach the far right wall, score increase, game ends, if game ended without reaching far right wall, penalty
      //    replay: shows number of player steps and resets the board, you or another player can play the same board
      // Note that you must adjust the score with any method that returns a score
      // Optional: create a custom image for your player use the file player.png on disk

  public static GameGUI game;
  public static EscapeRoom esc;
  public static int player = 1;
  // size of move
  public static final int m = 60;
  // individual player moves
  public static int px = 0;
  public static int py = 0;
  public static int score = 0;
  public static JFrame frame;
  public static boolean[] musicIds = new boolean[5];
  public static boolean mute = false;
  public static boolean isStroke = false;
  public static Thread onEnter;
  public static boolean onEnterNull = true;

  public static void println(String output) {
    System.out.println(output);
  }
  public static void print(String output) {
    System.out.print(output);
  }
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
    KeyListener listener = esc.new MyKeyListener();
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
    JButton[] buttons = new JButton[buttonsData.length];
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

    KeyListener listener = new MyKeyListener();
    frame.addKeyListener(listener);
    frame.setFocusable(true);
    frame.setResizable(false);
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
    if (temp > 0) { playAudio("coin.wav"); }
      score += temp;
      game.text(1,"score: " + score,29*60-10*21,16*60-10,40);
      game.repaintScreen();
    } else if (buttonName == "quit") {
      int temp = game.endGame();
      score += temp;
      frame.setVisible(false);
      frame.dispose();

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
      int temp = game.replay();
      score += temp;
      player = 1;
      game.text(1,"score: " + score,29*60-10*21,16*60-40,40);
      game.text(2,"steps=" + (game.getSteps(1) + game.getSteps(2)),29*60-10*21,16*60-10,40);
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
          score = 0;
          game.text(1,"score: " + score,29*60-11*19,16*60-10,40);
          game.text(2,"",0,0,0);
          game.text(3,"",0,0,0);
          game.text(4,"",0,0,0);
          game.repaintScreen();
          esc.Setup();
        }
      };
    } else if (buttonName == "switch") {
      if (player == 1) { player = 2; println("player 2's turn."); } else if (player == 2) { player = 1; println("player 1's turn."); }
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
        if (!mute) {
          loopAudioT2("music.wav","music2.wav",0);
          musicIds[0] = true;
        }
      }
    } else if (buttonName == "stroke") {
      stroke();
    }
    if (px != 0 || py != 0) {
      int scoreChange = 0;
      if (game.isTrap(px,py,player)) {
        scoreChange += game.springTrap(px,py,player);
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
  public static void stroke() {
    if (!isStroke) {
      isStroke = true;
      game.text(3,"Are you proud of yourself? Is this what you wanted? You deserver this.",29*(int)(60/2)-(70/2*(int)(19/40.0*75)),16*(int)(60/2)-(int)(10/40.0*75),75);
      Thread thread = new Thread() {
        public void run() {
          while (isStroke) {
            game.switchImages();
            playAudio("frank.wav");
            game.DUKELARGE();
            playAudio("dUKElARGE.wav");
            game.moveRand();
          }
        }
      };
      thread.start();
    } else {
      isStroke = false;
      
      game.text(1,"score: " + score,29*60-10*21,16*60-40,40);
      game.text(2,"steps=" + (game.getSteps(1) + game.getSteps(2)),29*60-10*21,16*60-10,40);
      game.text(3,"OOPS, YOU QUIT TOO SOON!",29*(int)(60/2)-(28/2*(int)(21/40.0*75)),16*(int)(60/2)-(int)(10/40.0*75),75);
      game.text(4,"Press enter to continue.",29*(int)(60/2)-(22/2*(int)(21/40.0*75)),16*(int)(60/2)+(int)(40/40.0*75),75);
      game.repaintScreen();
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