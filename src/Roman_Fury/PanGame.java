package Roman_Fury;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.Toolkit;
import javax.imageio.ImageIO;
import java.io.IOException;
import javax.swing.JPanel;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;

//This is the main panel of the game
public final class PanGame extends JPanel implements Runnable {

    private int sunPosX, sunPosY, levelNum = 0, counter1, counter2, cloudNum1 = 230, 
            cloudNum2 = 400, cloudNum3 = 650, cloudNum4 = 1045;
    private BufferedImage background;
    private final BufferedImage sun, cloud, pause;
    private final Hero hero;
    private final Sorcerer1 sor1;
    private final Sorcerer2 sor2;
    private final Fireball1 fire1;
    private final Fireball2 fire2;
    private final Knight knight;
    private final Boss boss;
    private boolean isCol, isCol1, isCol2, isSorcerer = true, isKnight, isBoss;
    private final Main main = new Main();
    private final int DELTA = 9;
    private Thread thread;
    private Clip clipBreach;
    private AudioInputStream AISBreach, AISFireball, AISShield;
    private long before = 0;
    private final long DELAY = 480;

    public PanGame() throws Exception {
        //KeyBindings: http://stackoverflow.com/questions/15753551/java-keybindings-how-does-it-work
        //I used KeyBindings instead of KeyEvents because the game loses focus when the
        //start button is pressed with the latter.
        keySetup();
        setFocusable(true);
        setDoubleBuffered(true);
        background = ImageIO.read(getClass().getResourceAsStream("/background1.png"));
        sun = ImageIO.read(getClass().getResourceAsStream("/sun.png"));
        cloud = ImageIO.read(getClass().getResourceAsStream("/cloud.png"));
        pause = ImageIO.read(getClass().getResourceAsStream("/pause.png"));
        AISBreach = AudioSystem.getAudioInputStream(getClass().getResource("/TheBreach.wav"));
        clipBreach = AudioSystem.getClip();
        hero = new Hero();
        sor1 = new Sorcerer1();
        sor2 = new Sorcerer2();
        fire1 = new Fireball1();
        fire2 = new Fireball2();
        knight = new Knight();
        boss = new Boss();
    }
    
    public void keySetup() {
        InputMap im = this.getInputMap(JPanel.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), "RightArrow");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true), "rlRightArrow");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), "LeftArrow");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true), "rlLeftArrow");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0, false), "C");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0, true), "rlC");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0, false), "X");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, 0, true), "rlX");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0, false), "Z");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0, true), "rlZ");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0, false), "P");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0, true), "rlP");
        am.put("RightArrow", new KeyAction("RightArrow"));
        am.put("rlRightArrow", new KeyAction("rlRightArrow"));
        am.put("LeftArrow", new KeyAction("LeftArrow"));
        am.put("rlLeftArrow", new KeyAction("rlLeftArrow"));
        am.put("C", new KeyAction("C"));
        am.put("rlC", new KeyAction("rlC"));
        am.put("X", new KeyAction("X"));
        am.put("rlX", new KeyAction("rlX"));
        am.put("Z", new KeyAction("Z"));
        am.put("rlZ", new KeyAction("rlZ"));
        am.put("P", new KeyAction("P"));
        am.put("rlP", new KeyAction("rlP"));
    }

    @Override
    public void addNotify() {
        super.addNotify();
        thread = new Thread(this);
        thread.start();
    }

    //Threads run more consistentantly than timers.
    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void run() {
        long beforeTime, timeDiff, sleep;
        beforeTime = System.currentTimeMillis();
        while (true) {
            if (!hero.getPause()) {
                if (!hero.getAction() && !isCol2 && !isCol1 && !isCol) {
                    if (isBoss & hero.getX() > 750) {
                        hero.setX(hero.getX() - 1);
                    }
                    hero.move();
                }
                if (fire1.isVisible()) {
                    fire1.move();
                }
                if (fire2.isVisible()) {
                    fire2.move();
                }
                if (!isBoss) {
                    clipBreach.close();
                }
            }
            repaint();
            timeDiff = System.currentTimeMillis() - beforeTime;
            sleep = DELTA - timeDiff;
            if (sleep < 0) {
                sleep = 9;
            }
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                System.out.println("interrupted");
            }
            beforeTime = System.currentTimeMillis();
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (hero.getPause()) {
            g.drawImage(background, 0, 0, null);
            g.drawImage(pause, 0, 0, null);
            clipBreach.close();
            return;
        }
        g.drawImage(background, 0, 0, null);
        hero.Health(g);
        g.drawImage(sun, sunPosX, sunPosY, null);
        if (hero.getHealth() <= 0) {
            clipBreach.close();
        }
        g.drawImage(hero.getImage(), hero.getX(), hero.getY(), this);
        //Loads the sorcerer level
        if (isSorcerer) {
            int[] nCloudX = {cloudNum1, cloudNum2, cloudNum3, cloudNum4};
            int[] nCloudY = {40, 85, 5, 165};
            for (int i = 0; i < nCloudX.length; i++) {
                g.drawImage(cloud, nCloudX[i], nCloudY[i], null);
            }
            sunPosX = 1068;
            sunPosY = 55;
            if (sor1.getHealth() > 0) {
                sor1.Health(g);
                g.drawImage(sor1.getImage(), sor1.getX(), sor1.getY(), this);
            }
            if (sor2.getHealth() > 0) {
                sor2.Health(g);
                g.drawImage(sor2.getImage(), sor2.getX(), sor2.getY(), this);
            }
            if (sor1.getAttack()) {
                fire1.setVisible(true);
            }
            if (fire1.isVisible()) {
                g.drawImage(fire1.getImage(), fire1.getX(), fire1.getY(), this);
            }
            if (sor2.getAttack()) {
                fire2.setVisible(true);
            }
            if (fire2.isVisible()) {
                g.drawImage(fire2.getImage(), fire2.getX(), fire2.getY(), this);
            }
            if (sor1.getAttack() || sor2.getAttack()) {
                try {
                    Clip clipFireball = AudioSystem.getClip();
                    AISFireball = AudioSystem.getAudioInputStream(getClass().getResource("/Fireball.wav"));
                    clipFireball.open(AISFireball);
                    clipFireball.start();
                } catch (IOException | LineUnavailableException | UnsupportedAudioFileException ex) {
                }
            }
            checkCollisionsSor();
        }
        //Loads the knight level
        if (isKnight) {
            int[] nCloudX = {cloudNum1, cloudNum2, cloudNum3, cloudNum4};
            int[] nCloudY = {40, 85, 5, 130};
            for (int i = 0; i < nCloudX.length; i++) {
                g.drawImage(cloud, nCloudX[i], nCloudY[i], null);
            }
            knight.KnightHealth(g);
            sunPosX = 600;
            sunPosY = 5;
            if (knight.getHealth() > 0) {
                g.drawImage(knight.getImage(), knight.getX(), knight.getY(), this);
            }
            checkCollisionsKnight();
        }
        //Loads the boss level
        if (isBoss) {
            int[] nCloudX = {cloudNum1, cloudNum2, cloudNum3, cloudNum4};
            int[] nCloudY = {40, 85, 2, 130};
            for (int i = 0; i < nCloudX.length; i++) {
                g.drawImage(cloud, nCloudX[i], nCloudY[i], null);
            }
            if (hero.getX() < 619) {
                boss.Beam(g);
            } else if (hero.getX() > 700) {
                boss.Blast();
            }
            boss.Health(g);
            sunPosX = 50;
            sunPosY = 100;
            if (boss.getHealth() > 0) {
                g.drawImage(boss.getImage(), boss.getX(), boss.getY(), this);
            }
            checkCollisionsBoss();
        }
        Toolkit.getDefaultToolkit().sync();
        g.dispose();
        Clouds();
    }

    //Moves clouds
    public void Clouds() {
        if (counter1 < 20) {
            counter2 = 0;
        } else if (counter1 > 20) {
            counter2 = 1;
            counter1 = 0;
            cloudNum1 += counter2;
            cloudNum2 += counter2;
            cloudNum3 += counter2;
            cloudNum4 += counter2;
        }
        counter1++;
        if (cloudNum1 > 1280) {
            cloudNum1 = -120;
        }
        if (cloudNum2 > 1280) {
            cloudNum2 = -120;
        }
        if (cloudNum3 > 1280) {
            cloudNum3 = -120;
        }
        if (cloudNum4 > 1280) {
            cloudNum4 = -120;
        }
    }

    public void reset() {
        hero.Restart();
        hero.setX(580);
        hero.setState(1);
        hero.setRight(true);
        sor1.Restart();
        sor1.setAttack(1299);
        sor1.setChange();
        sor2.Restart();
        sor2.setAttack(500);
        sor2.setChange();
        knight.Restart();
        boss.Restart();
        fire1.Restart();
        fire2.Restart();
        isSorcerer = true;
        isKnight = isBoss = isCol1 = isCol2 = isCol = false;
        try {
            background = ImageIO.read(getClass().getResourceAsStream("/background1.png"));
        } catch (IOException e) {
            System.out.println("IOException!");
        }
        cloudNum1 = 230;
        cloudNum2 = 400;
        cloudNum3 = 650;
        cloudNum4 = 1045;
        levelNum = 0;
    }

    public void ReBreach() {
        try {
            AISBreach = AudioSystem.getAudioInputStream(getClass().getResource("/TheBreach.wav"));
            clipBreach = AudioSystem.getClip();
            clipBreach.open(AISBreach);
            clipBreach.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException ex) {
        }
    }

    //http://zetcode.com/tutorials/javagamestutorial/collision/
    public void checkCollisionsSor() {
        Rectangle RecHero = hero.getBounds(), RecFireball1 = fire1.getBounds(),
                RecFireball2 = fire2.getBounds(), RecSor1 = sor1.getBounds(),
                RecSor2 = sor2.getBounds();
        //checks collision of fireballs and hero
        if (fire1.isVisible() && RecHero.intersects(RecFireball1)) {
            fire1.setVisible(false);
            fire1.setX(1120);
            hero.setHitRight(true);
            sor1.setAttack(1100);
        }
        if (fire2.isVisible() && RecHero.intersects(RecFireball2)) {
            fire2.setVisible(false);
            fire2.setX(115);
            hero.setHitLeft(true);
            sor2.setAttack(1100);
        }
        //checks collision of the sorcerers and hero
        if (RecHero.intersects(RecSor1)) {
            if (hero.getRight() && sor1.getHealth() > 0) {
                isCol1 = true;
                if (hero.getWeak()) {
                    sor1.setHealth(10);
                    hero.setWeak(false);
                } else if (hero.getStrong()) {
                    sor1.setHealth(30);
                    hero.setStrong(false);
                }
            } else {
                isCol1 = false;
            }
        }
        if (RecHero.intersects(RecSor2)) {
            if (!hero.getRight() && sor2.getHealth() > 0) {
                isCol2 = true;
                if (hero.getWeak()) {
                    sor2.setHealth(10);
                    hero.setWeak(false);
                } else if (hero.getStrong()) {
                    sor2.setHealth(30);
                    hero.setStrong(false);
                }
            } else {
                isCol2 = false;
            }
        }
        if (!RecHero.intersects(RecSor1)) {
            isCol1 = false;
        }
        if (!RecHero.intersects(RecSor2)) {
            isCol2 = false;
        }
        //Switch to kngiht level
        if (sor1.getHealth() <= 0 && sor2.getHealth() <= 0 && levelNum == 0) {
            isSorcerer = false;
            isKnight = true;
            try {
                background = ImageIO.read(getClass().getResourceAsStream("/background2.png"));
            } catch (IOException e) {
                System.out.println("IOException!");
            }
            levelNum++;
            hero.setX(300);
            hero.setRight(true);
            hero.setState(1);
            isCol1 = isCol2 = false;
            cloudNum1 = 450;
            cloudNum2 = 1000;
            cloudNum3 = 200;
            cloudNum4 = 400;
        }
    }

    public void checkCollisionsKnight() {
        Rectangle RecHero = hero.getBounds(), RecKnight = knight.getBounds();
        //checks collision of the knight and hero
        if (RecHero.intersects(RecKnight)) {
            if (hero.getRight() && knight.getHealth() > 0) {
                isCol = true;
                if (hero.getWeak() && !knight.getBlock() && knight.getState() == 1) {
                    knight.setHealth(10);
                    hero.setWeak(false);
                } else if (hero.getStrong() && !knight.getBlock() && knight.getState() == 1) {
                    knight.setHealth(30);
                    hero.setStrong(false);
                }
                if (hero.getWeak() || hero.getStrong() && knight.getBlock()) {
                    try {
                        long now = System.currentTimeMillis();
                        if (now - before > DELAY) {
                            Clip clipShield = AudioSystem.getClip();
                            AISShield = AudioSystem.getAudioInputStream(getClass().getResource("/Shield.wav"));
                            clipShield.open(AISShield);
                            clipShield.start();
                        }
                        before = now;
                    } catch (IOException | LineUnavailableException | UnsupportedAudioFileException ex) {
                    }
                }
            } else if (!hero.getRight()) {
                isCol = false;
            }
            if (knight.getAttack()) {
                knight.setAttack(false);
                hero.setHit(true);
            }
        }
        if (!RecHero.intersects(RecKnight)) {
            isCol = false;
        }
        //Switch to boss level
        if (knight.getHealth() <= 0 && levelNum == 1) {
            isKnight = false;
            isBoss = true;
            try {
                background = ImageIO.read(getClass().getResourceAsStream("/background3.png"));
                ReBreach();
            } catch (IOException e) {
                System.out.println("Exception!");
            }
            levelNum++;
            hero.setX(50);
            hero.setRight(true);
            hero.setState(1);
            isCol = false;
            cloudNum1 = 700;
            cloudNum2 = 720;
            cloudNum3 = 1100;
            cloudNum4 = 100;
        }
    }

    public void checkCollisionsBoss() {
        Rectangle RecHero = hero.getBounds(), RecBoss = boss.getBounds(),
                RecBeam = boss.recBeam.getBounds();
        //checks collision of the boss and hero
        if (RecHero.intersects(RecBoss) && boss.getHealth() > 0) {
            if (hero.getRight()) {
                isCol = true;
                if (hero.getWeak()) {
                    boss.setHealth(5);
                    hero.setWeak(false);
                } else if (hero.getStrong()) {
                    boss.setHealth(15);
                    hero.setStrong(false);
                }
            } else if (!hero.getRight()) {
                isCol = false;
            }
            if (boss.getBlast() && hero.getX() > 700) {
                hero.setBlast(true);
                boss.setBlast(false);
            }
        } else if (RecHero.intersects(RecBeam) && boss.getHealth() > 0 && boss.getDelay() == 1 && hero.getX() < 619) {
            if (hero.getRight()) {
                isCol = true;
            } else if (!hero.getRight()) {
                isCol = false;
            }
            if (boss.getBeam()) {
                boss.setBeam(false);
                boss.setGrow(false);
                hero.setBeam(true);
            }
        } else if (!RecHero.intersects(RecBeam)) {
            boss.setBeam(true);
            boss.setGrow(true);
            isCol = false;
        }
        if (boss.getDelay() != 1) {
            isCol = false;
        }
        //You win!
        if (boss.getHealth() <= 0) {
            clipBreach.close();
            hero.setState(1);
            hero.noAction();
            isCol = false;
            main.Win();
        }
    }

    public class KeyAction extends AbstractAction {

        private final String cmd;

        public KeyAction(String cmd) {
            this.cmd = cmd;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (cmd.equalsIgnoreCase("LeftArrow")) {
                hero.keyPressed("left");
            } else if (cmd.equalsIgnoreCase("rlLeftArrow")) {
                hero.keyReleased("rlleft");
            } else if (cmd.equalsIgnoreCase("RightArrow")) {
                hero.keyPressed("right");
            } else if (cmd.equalsIgnoreCase("rlRightArrow")) {
                hero.keyReleased("rlright");
            } else if (cmd.equalsIgnoreCase("C")) {
                hero.keyPressed("C");
            } else if (cmd.equalsIgnoreCase("rlC")) {
                hero.keyReleased("rlC");
            } else if (cmd.equalsIgnoreCase("X")) {
                hero.keyPressed("X");
            } else if (cmd.equalsIgnoreCase("rlX")) {
                hero.keyReleased("rlX");
            } else if (cmd.equalsIgnoreCase("Z")) {
                hero.keyPressed("Z");
            } else if (cmd.equalsIgnoreCase("rlZ")) {
                hero.keyReleased("rlZ");
            } else if (cmd.equalsIgnoreCase("P")) {
                hero.keyPressed("P");
            } else if (cmd.equalsIgnoreCase("rlP")) {
                hero.keyReleased("rlP");
            }
        }
    }
}
