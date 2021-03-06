package Roman_Fury;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.io.IOException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

//This class loads all of the attributes for the hero
public class Hero {

    private static int dX, positionX, nImgNum = 1, nDelay = 1, nState, nImage, nHealth = 100;
    private static final int POSITIONY = 505;
    public static BufferedImage bImage, portrait;
    private final static BufferedImage[][] imageArray = new BufferedImage[7][7];
    public static boolean isAction, isRight = true, isMoving, isBlock, isStrong, isWeak, 
            pause, isHitRight, isHitLeft, isHit, isPush, isPush2, isBeam,isBlast;
    private Rectangle recHealth;
    private final Main main = new Main();
    private long lastAtkTime, lastBlockTime;
    private final long ZDELAY = 1500, XDELAY = 600, CDELAY = 500;
    private AudioInputStream AISHurt, AISPause, AISStrong, AISWeak, AISShield;

    public Hero() throws Exception {
        //First bracket is for the state of the hero and the second is for the image
        //1 - right at rest, 2 left at rest, 3 right moving, 4 left moving,
        //5 action right, 6 action left
        nState = nImage = 1;
        imageArray[1][1] = ImageIO.read(getClass().getResourceAsStream("/heroright.png"));
        imageArray[2][1] = ImageIO.read(getClass().getResourceAsStream("/heroleft.png"));
        for (int i = 1; i < imageArray.length; i++) {
            imageArray[3][i] = ImageIO.read(getClass().getResourceAsStream("/herorightwalk" + i + ".png"));
            imageArray[4][i] = ImageIO.read(getClass().getResourceAsStream("/heroleftwalk" + i + ".png"));
        }
        for (int i = 1; i < 4; i++) {
            imageArray[5][i] = ImageIO.read(getClass().getResourceAsStream("/herorightaction" + i + ".png"));
            imageArray[6][i] = ImageIO.read(getClass().getResourceAsStream("/heroleftaction" + i + ".png"));
        }
        portrait = ImageIO.read(getClass().getResourceAsStream("/heroportrait.png"));
        isHitRight = isHitLeft = false;
        AISHurt = AudioSystem.getAudioInputStream(getClass().getResource("/Hurt.wav"));
        lastAtkTime = lastBlockTime = System.currentTimeMillis();
        positionX = 580;
    }

    public int getX() {
        Push();
        return positionX;
    }

    public int getY() {
        return POSITIONY;
    }

    public int getHealth() {
        return nHealth;
    }

    public int getState() {
        return nState;
    }

    public boolean getAction() {
        return isAction;
    }

    public boolean getStrong() {
        return isStrong;
    }

    public boolean getWeak() {
        return isWeak;
    }

    public boolean getPause() {
        return pause;
    }

    public boolean getRight() {
        return isRight;
    }

    public boolean getBlock() {
        return isBlock;
    }

    public boolean getPush() {
        return isPush;
    }

    public BufferedImage getImage() {
        if (isMoving) {
            Count();
        }
        if (isAction) {
            nState = 5;
        }
        main.Death();
        if (isRight) {
            bImage = imageArray[nState][nImage];
        } else {
            bImage = imageArray[nState + 1][nImage];
        }
        return bImage;
    }

    //I have so many ifs because I didn't to change the bounds of the hero to fit
    //the current image better.
    public Rectangle getBounds() {
        if (isRight && !isAction) {
            return new Rectangle(positionX + 28, POSITIONY, bImage.getWidth() - 70, bImage.getHeight());
        } else if (!isRight && !isAction) {
            return new Rectangle(positionX + 42, POSITIONY, bImage.getWidth() - 73, bImage.getHeight());
        } else if (isRight && isBlock) {
            return new Rectangle(positionX + 35, POSITIONY, bImage.getWidth() - 59, bImage.getHeight());
        } else if (!isRight && isBlock) {
            return new Rectangle(positionX + 22, POSITIONY, bImage.getWidth() - 58, bImage.getHeight());
        } else {
            return new Rectangle(positionX + 25, POSITIONY, bImage.getWidth() - 25, bImage.getHeight());
        }
    }

    public void setX(int x) {
        positionX = x;
    }

    public void setRight(boolean b) {
        isRight = b;
    }

    public void setHitRight(boolean Hit) {
        isHitRight = Hit;
    }

    public void setHitLeft(boolean Hit) {
        isHitLeft = Hit;
    }

    public void setHit(boolean Hit) {
        isHit = Hit;
    }

    public void setWeak(boolean quick) {
        isWeak = quick;
    }

    public void setStrong(boolean strong) {
        isStrong = strong;
    }

    public void setPush(boolean push) {
        isPush = push;
    }

    public void setBeam(boolean beam) {
        isBeam = beam;
    }

    public void setBlast(boolean blast) {
        isBlast = blast;
    }

    public void setState(int state) {
        nState = nImage = state;
    }

    public void noAction() {
        isAction = false;
    }

    public void Restart() {
        nHealth = 100;
        dX = 0;
        isMoving = false;
    }

    public void move() {
        positionX += dX;
    }

    public void Health(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        int health = nHealth;
        //Allows you to block attacks and if you're blocking the opposite way you'll get hurt
        if (isRight) {
            if (isHitRight && !isBlock) {
                nHealth -= 10;
            }
            if (isHitLeft) {
                nHealth -= 10;
            } else if (isHit) {
                if (!isBlock) {
                    nHealth -= 20;
                }
                isPush = true;
            }
            if (isBeam && !isBlock) {
                nHealth -= 10;
            } else if (isBlast) {
                if (!isBlock) {
                    nHealth -= 10;
                }
                isPush2 = true;
            }
        } else if (!isRight) {
            if (isHitRight) {
                nHealth -= 10;
            }
            if (isHitLeft && !isBlock) {
                nHealth -= 10;
            } else if (isHit) {
                nHealth -= 20;
                isPush = true;
            }
            if (isBeam) {
                nHealth -= 10;
            } else if (isBlast) {
                nHealth -= 10;
                isPush2 = true;
            }
        }
        if (health != nHealth) {
            try {
                Clip clipHurt = AudioSystem.getClip();
                AISHurt = AudioSystem.getAudioInputStream(getClass().getResource("/Hurt.wav"));
                clipHurt.open(AISHurt);
                clipHurt.start();
            } catch (IOException | LineUnavailableException | UnsupportedAudioFileException ex) {
            }
        } else if (isBlock && isBeam || isBlast || isHitLeft || isHitRight || isHit) {
            try {
                Clip clipShield = AudioSystem.getClip();
                AISShield = AudioSystem.getAudioInputStream(getClass().getResource("/Shield.wav"));
                clipShield.open(AISShield);
                clipShield.start();
            } catch (IOException | LineUnavailableException | UnsupportedAudioFileException ex) {
            }
        }
        isHitRight = isHitLeft = isHit = isBeam = isBlast = false;
        recHealth = new Rectangle(50, 15, nHealth, 20);
        g.drawImage(portrait, 0, 0, null);
        g.setColor(Color.red);
        g2.fill(recHealth);
        g.setColor(Color.black);
        g2.draw(recHealth);
    }

    private void Animation() {
        nState = 3;
        nImage = nImgNum;
    }

    //This method is part of the animation.  It's the counter that cycles
    //through the animation images.
    private void Count() {
        nDelay++;
        if (nDelay >= 24) {
            nImgNum++;
            if (nImgNum >= 7) {
                nImgNum = 1;
            }
            nDelay = 1;
        }
        Animation();
    }

    //This pushes back the hero when certain enemies do certain attacks.
    private void Push() {
        if (isPush && positionX > 910) {
            positionX -= 1;
        } else {
            isPush = false;
        }
        if (isPush2 && positionX > 620) {
            positionX -= 1;
        } else {
            isPush2 = false;
        }
    }

    //http://zetcode.com/tutorials/javagamestutorial/movingsprites/
    public void keyPressed(String s) {
        if (!isAction) {
            switch (s) {
                case "left":
                    dX = -1;
                    if (positionX < -10) {
                        dX = 0;
                    }
                    isRight = false;
                    isMoving = true;
                    break;
                case "right":
                    dX = 1;
                    if (positionX > 1200) {
                        dX = 0;
                    }
                    isRight = true;
                    isMoving = true;
                    break;
                case "C": {
                    long timeNow = System.currentTimeMillis();
                    if (timeNow - lastBlockTime < CDELAY) {
                        return;
                    }
                    dX = 0;
                    isAction = true;
                    isMoving = false;
                    isBlock = true;
                    nImage = 1;
                    lastBlockTime = timeNow;
                    break;
                }
                case "X": {
                    long timeNow = System.currentTimeMillis();
                    if (timeNow - lastAtkTime < XDELAY) {
                        return;
                    }
                    try {
                        Clip clipWeak = AudioSystem.getClip();
                        AISWeak = AudioSystem.getAudioInputStream(getClass().getResource("/Weak.wav"));
                        clipWeak.open(AISWeak);
                        clipWeak.start();
                    } catch (LineUnavailableException | UnsupportedAudioFileException | IOException ex) {
                    }
                    dX = 0;
                    isAction = true;
                    isMoving = false;
                    isWeak = true;
                    nImage = 2;
                    lastAtkTime = timeNow;
                    break;
                }
                case "Z": {
                    long timeNow = System.currentTimeMillis();
                    if (timeNow - lastAtkTime < ZDELAY) {
                        return;
                    }
                    try {
                        Clip clipStrong = AudioSystem.getClip();
                        AISStrong = AudioSystem.getAudioInputStream(getClass().getResource("/Strong.wav"));
                        clipStrong.open(AISStrong);
                        clipStrong.start();
                    } catch (IOException | LineUnavailableException | UnsupportedAudioFileException ex) {
                    }
                    dX = 0;
                    isAction = true;
                    isMoving = false;
                    isStrong = true;
                    nImage = 3;
                    lastAtkTime = timeNow;
                    break;
                }
                case "P":
                    pause = !pause;
                    try {
                        Clip clipPause = AudioSystem.getClip();
                        if (pause) {
                            AISPause = AudioSystem.getAudioInputStream(getClass().getResource("/Pause.wav"));
                        } else {
                            AISPause = AudioSystem.getAudioInputStream(getClass().getResource("/Unpause.wav"));
                            main.ReBreach();
                        }
                        clipPause.open(AISPause);
                        clipPause.start();
                    } catch (IOException | LineUnavailableException | UnsupportedAudioFileException ex) {
                    }
                    break;
            }
        }
    }

    public void keyReleased(String s) {
        if (s.equals("rlright") || s.equals("rlleft") && !isAction) {
            dX = 0;
            isMoving = false;
            nState = nImage = 1;
        } else if (s.equals("rlC") && !isWeak && !isStrong) {
            isAction = isBlock = false;
            nState = nImage = 1;
        } else if (s.equals("rlX") && !isBlock && !isStrong) {
            isAction = isWeak = false;
            nState = nImage = 1;
        } else if (s.equals("rlZ") && !isBlock && !isWeak) {
            isAction = isStrong = false;
            nState = nImage = 1;
        }
        nImgNum = (int) (Math.random() * 6 + 1);
        nDelay = 0;
    }
}
