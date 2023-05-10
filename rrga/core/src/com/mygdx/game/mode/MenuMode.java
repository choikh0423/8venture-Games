package com.mygdx.game.mode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.GameMode;
import com.mygdx.game.screen.MenuScreen;
import com.mygdx.game.utility.assets.AssetDirectory;
import com.mygdx.game.utility.util.MySlider;
import com.mygdx.game.utility.util.ScreenListener;
import org.w3c.dom.Text;

import java.awt.*;

public class MenuMode extends MenuScreen {
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The Screen to draw underneath the pause screen*/
    private GameMode gameScreen;

    /** Reference to GameCanvas created by the root */
    protected GameCanvas canvas;

    /** Background texture */
    private TextureRegion backgroundTexture;
    /** Level Selector Background texture */
    private TextureRegion backgroundTexture2;


    /** Types of button by shape */
    public enum ButtonShape {
        RECTANGLE,
        CIRCLE
    }
    /** exit button*/
    private MenuButton exitButton;
    /** start button */
    private MenuButton startButton;
    /** settings button */
    private MenuButton settingsButton;
    /** level select button */
    private MenuButton levelSelectButton;
    /** level 1 button */
    private MenuButton levelButton1;
    /** level 2 button */
    private MenuButton levelButton2;
    /** level 3 button */
    private MenuButton levelButton3;
    /** back button */
    private MenuButton backButton;
    /** reset button */
    private MenuButton resetButton;


    /** The current state of the level select button */
    private int selectPressState;
    /** The current state of the settings button */
    private int settingsPressState;
    /** The current state of the exit button */
    private int exitPressState;
    /** The current state of the exit button */
    private int startPressState;
    /** The current state of the exit button */
    private int levelPressState1;
    /** The current state of the exit button */
    private int levelPressState2;
    /** The current state of the exit button */
    private int levelPressState3;
    /** The current state of the umbrella toggle button */
    private int togglePressState;
    private int resetLevelPressState;
    private int resetSettingsPressState;

    /** Background music */
    private Music backgroundMusic;
    public Music getMusic(){return backgroundMusic;}
    /** Music volume */
    private float musicVolume;
    /** SFX volume */
    private float sfxVolume;


    /** exit code to terminate game */
    public static final int EXIT_QUIT = 0;
    /** exit code to play game */
    public static final int EXIT_PLAY = 1;
    /** exit code to pause menu */
    public static final int EXIT_PAUSE = 2;
    public static final int EXIT_CONFIRM = 3;
    /** current assigned exit code of mode (valid exits are non-negative) */
    private int currentExitCode;
    /** Tracker for checking which screen the menu screen is showing
     *  1: main menu
     *  2: level selector
     *  3: settings
     * */
    private int screenMode;
    public int getScreenMode(){return screenMode;}
    public void setScreenMode(int mode){screenMode = mode;}

    /** Standard window size (for scaling) */
    private static int STANDARD_WIDTH  = 800;
    /** Standard window height (for scaling) */
    private static int STANDARD_HEIGHT = 700;
    /** Scaling factor for when the player changes the resolution. */
    private float scale;
    /** The height of the canvas window (necessary since sprite origin != screen origin) */
    private int heightY;
    /** Height of the button */
    private static float BUTTON_SCALE  = 1.2f;
    /** The width of the button */
    private int buttonWidth;
    /** Ratio of the button width to the screen */
    private static float BUTTON_WIDTH_RATIO  = 0.66f;


    /** Music volume slider bar texture */
    private TextureRegion musicSliderBar;
    /** Music volume slider knob texture */
    private TextureRegion musicSliderKnob;

    /** The x-coordinate of the center of the music slider */
    private int musicSliderX;
    /** The y-coordinate of the center of the music slider */
    private int musicSliderY;
    /** Ratio of the music slider width to the screen */
    private static float MUSIC_X_RATIO = 0.5f;
    /** Ratio of the music slider height to the screen */
    private static float MUSIC_Y_RATIO = 0.5f;

    /** SFX volume slider bar texture */
    private TextureRegion sfxSliderBar;
    /** SFX volume slider knob texture */
    private TextureRegion sfxSliderKnob;

    /** The x-coordinate of the center of the sfx slider */
    private int sfxSliderX;
    /** The y-coordinate of the center of the sfx slider */
    private int sfxSliderY;
    /** Ratio of the sfx slider width to the screen */
    private static float SFX_X_RATIO = 0.5f;
    /** Ratio of the sfx slider height to the screen */
    private static float SFX_Y_RATIO = 0.35f;
    private float SLIDER_SCL_X = 1;
    private float SLIDER_SCL_Y = 1;
    /** Touch range constant */
    private static float TOUCH_AREA_RATIO = 0.95f;
    /** The music slider */
    private MySlider musicSlider;
    /** The sfx slider */
    private MySlider sfxSlider;
    private TextureRegion musicTag;
    private static float MUSIC_TAG_X_RATIO = .2f;
    private static float MUSIC_TAG_Y_RATIO = .50f;
    private int musicTagX;
    private int musicTagY;
    private TextureRegion sfxTag;
    private static float SFX_TAG_X_RATIO = .2f;
    private static float SFX_TAG_Y_RATIO = .35f;
    private int sfxTagX;
    private int sfxTagY;
    private float TAG_SCL = 1;

    private TextureRegion settingTag;
    private static float SETTING_TAG_X_RATIO = .5f;
    private static float SETTING_TAG_Y_RATIO = .77f;
    private int settingTagX;
    private int settingTagY;

    private TextureRegion toggleTag;
    private static float TOGGLE_TAG_X_RATIO = .3f;
    private static float TOGGLE_TAG_Y_RATIO = .2f;
    private int toggleTagX;
    private int toggleTagY;


    /** toggle button texture */
    private TextureRegion toggleToggle;
    private static float TOGGLE_BUTTON_X_RATIO = .65f;
    private static float TOGGLE_BUTTON_Y_RATIO = .19f;
    private int toggleButtonX;
    private int toggleButtonY;

    /** toggle check texture */
    private TextureRegion toggleHold;

    private boolean toggleOn;

    /** current selected level */
    private int currentLevel;

    /** texture for the cursor */
    private TextureRegion cursorTexture;
    /** pixmap for the cursor */
    private Cursor newCursor;
    /** number of levels in the game. NEED TO CHANGE THIS AS WE ADD MORE LEVELS */
    public static final int LEVEL_COUNT = 3;

    /** preferences object to store user settings */
    Preferences settings = Gdx.app.getPreferences("settings");
    /** preferences object to store which levels the user has unlocked */
    Preferences unlocked = Gdx.app.getPreferences("unlocked");
    /** a list containing whether each level is unlocked.
     * size is 30 to allow for room for more levels.
     * 0th element is whether to unlock all levels at start of game
     * (true for developers and final submission, false for publicly distributed version)*/
    private boolean[] levelUnlocked = new boolean[LEVEL_COUNT+1];

    public boolean cameForPauseSettings = false;
    private static Music menuMusic;

    public MenuMode(GameCanvas canvas) {
        //TODO: CHANGE TO FALSE FOR PUBLIC RELEASE (or to test unlocking of levels)
        levelUnlocked[0] = true;

        this.canvas = canvas;
        currentExitCode = Integer.MIN_VALUE;
        this.screenMode = 1;

        // TODO: All the ratios are hard coded - these can be extracted to JSON
        this.exitButton = new MenuButton(ButtonShape.CIRCLE, 0.05f, 0.93f, 0.05f * 3.14f);
        this.startButton = new MenuButton(ButtonShape.RECTANGLE, 0.37f, 0.2f, 0.05f * 3.14f);
        this.settingsButton = new MenuButton(ButtonShape.RECTANGLE, 0.95f, 0.07f, 0);
        this.levelSelectButton = new MenuButton(ButtonShape.RECTANGLE, 0.63f, 0.2f, -0.05f * 3.14f);
        this.backButton = new MenuButton(ButtonShape.CIRCLE, 0.05f, 0.93f, 0);
        resetButton = new MenuButton(ButtonShape.RECTANGLE, 0.85f,0.1f,0);

        this.levelButton1 = new MenuButton(ButtonShape.CIRCLE, 0.25f, 0.5f, 0);
        this.levelButton2 = new MenuButton(ButtonShape.CIRCLE, 0.5f, 0.5f, 0);
        this.levelButton3 = new MenuButton(ButtonShape.CIRCLE, 0.75f, 0.5f, 0);
    }

    public void gatherAssets(AssetDirectory directory) {

        backgroundTexture = new TextureRegion(directory.getEntry( "menu:background", Texture.class ));
        backgroundTexture2 = new TextureRegion(directory.getEntry( "menu:background2", Texture.class ));

        cursorTexture = new TextureRegion(directory.getEntry( "menu:cursor_menu", Texture.class ));
        Pixmap pm = new Pixmap(Gdx.files.internal("game/goal.png"));
        newCursor = Gdx.graphics.newCursor(pm, 0, 0);
        pm.dispose();


        // TODO: To reduce global variables, made temporary texture region variables, Let me know if this is too much of a bad practice
        // MENU COMPONENTS
        TextureRegion exitTexture = new TextureRegion(directory.getEntry("menu:exit_button", Texture.class));
        TextureRegion startTexture = new TextureRegion(directory.getEntry("menu:start_button", Texture.class));
        TextureRegion settingsTexture = new TextureRegion(directory.getEntry("menu:settings_button", Texture.class));
        TextureRegion levelSelectTexture = new TextureRegion(directory.getEntry("menu:level_select_button", Texture.class));
        TextureRegion backButtonTexture = new TextureRegion(directory.getEntry("menu:back_button", Texture.class));

        exitButton.setTexture(exitTexture);
        startButton.setTexture(startTexture);
        settingsButton.setTexture(settingsTexture);
        levelSelectButton.setTexture(levelSelectTexture);
        backButton.setTexture(backButtonTexture);
        resetButton.setTexture(new TextureRegion(directory.getEntry("menu:reset_button", Texture.class)));

        // LEVEL SELECT COMPONENTS
        TextureRegion levelButtonTexture1 = new TextureRegion(directory.getEntry("menu:level1_button", Texture.class));
        TextureRegion levelButtonTexture2 = new TextureRegion(directory.getEntry("menu:level2_button", Texture.class));
        // TODO: Change this to level 3 button
        TextureRegion levelButtonTexture3 = new TextureRegion(directory.getEntry("menu:level3_button", Texture.class));

        levelButton1.setTexture(levelButtonTexture1);
        levelButton2.setTexture(levelButtonTexture2);
        levelButton3.setTexture(levelButtonTexture3);

        // SETTINGS COMPONENT
        musicTag = new TextureRegion(directory.getEntry("menu:music_tag", Texture.class));
        sfxTag = new TextureRegion(directory.getEntry("menu:sfx_tag", Texture.class));
        settingTag = new TextureRegion(directory.getEntry("menu:setting_tag", Texture.class));
        toggleTag = new TextureRegion(directory.getEntry("menu:toggle_tag", Texture.class));
        toggleToggle = new TextureRegion(directory.getEntry("menu:toggle_toggle", Texture.class));
        toggleHold = new TextureRegion(directory.getEntry("menu:toggle_hold", Texture.class));

        // TODO: Scale slider bars
        musicSliderBar = new TextureRegion(directory.getEntry("menu:sliderBar", Texture.class));
        musicSliderKnob = new TextureRegion(directory.getEntry("menu:sliderKnob", Texture.class));
        musicSlider = new MySlider(musicSliderBar, musicSliderKnob, 20, musicSliderX, musicSliderY, SLIDER_SCL_X, SLIDER_SCL_Y);

        sfxSliderBar = new TextureRegion(directory.getEntry("menu:sliderBar", Texture.class));
        sfxSliderKnob = new TextureRegion(directory.getEntry("menu:sliderKnob", Texture.class));
        sfxSlider = new MySlider(sfxSliderBar, sfxSliderKnob, 20, sfxSliderX, sfxSliderY, SLIDER_SCL_X, SLIDER_SCL_Y);

        backgroundMusic = directory.getEntry("music:menu", Music.class);
        menuMusic = directory.getEntry("music:menu", Music.class);

        //load in user settings
        musicVolume = settings.getFloat("musicVolume", 0.5f);
        musicSlider.ratio = musicVolume;
        sfxVolume = settings.getFloat("sfxVolume", 0.5f);
        sfxSlider.ratio = sfxVolume;
        toggleOn = settings.getBoolean("toggle", false);

        //load in whether player has unlocked each level
        for (int i = 0; i <= LEVEL_COUNT; i++){
            //level 1 always starts unlocked
            if (i==1) levelUnlocked[i] = true;
            //if we have unlocked all levels, unlock all levels
            else if (levelUnlocked[0]) levelUnlocked[i] = true;
            //otherwise load in whether player has unlocked each level
            else levelUnlocked[i] = unlocked.getBoolean(i+"unlocked", false);
        }
    }

    public boolean touchDown(int screenX, int screenY, int pointer, int button) {

        // Setup
        screenY = heightY-screenY;

        if (screenMode == 1) {
            // Checks which button was clicked
            boolean selectPressed = checkClicked2(screenX, screenY, levelSelectButton);
            boolean settingsPressed = checkCircleClicked2(screenX, screenY, settingsButton, BUTTON_SCALE);
            boolean exitPressed = checkCircleClicked2(screenX, screenY, exitButton, BUTTON_SCALE);
            boolean startPressed = checkClicked2(screenX, screenY, startButton);

            if (selectPressed) {
                selectPressState = 1;
            } else if (settingsPressed) {
                settingsPressState = 1;
            } else if (exitPressed) {
                exitPressState = 1;
            } else if (startPressed) {
                startPressState = 1;
            }
        } else if (screenMode == 2) {
            // Checks which button was clicked in Level Selector Screen
            boolean exitPressed = checkCircleClicked2(screenX, screenY, exitButton, BUTTON_SCALE);
            boolean levelPressed1 = checkCircleClicked2(screenX, screenY, levelButton1, BUTTON_SCALE);
            boolean levelPressed2 = checkCircleClicked2(screenX, screenY, levelButton2, BUTTON_SCALE);
            boolean levelPressed3 = checkCircleClicked2(screenX, screenY, levelButton3, BUTTON_SCALE);
            boolean resetPressed = checkClicked2(screenX, screenY, resetButton);

            if (levelPressed1) {
                levelPressState1 = 1;
            } else if (levelPressed2) {
                levelPressState2 = 1;
            } else if (levelPressed3) {
                levelPressState3 = 1;
            } else if (exitPressed) {
                exitPressState = 1;
            } else if (resetPressed){
                resetLevelPressState = 1;
            }
        } else if (screenMode == 3) {
            // Checks which button was clicked in Settings Screen
            boolean exitPressed = checkCircleClicked2(screenX, screenY, exitButton, BUTTON_SCALE);
            boolean musicKnobPressed = checkCircleClicked(screenX, screenY, musicSlider.getKnobX(), musicSlider.getKnobY(), musicSliderKnob, musicSlider.sx);
            boolean sfxKnobPressed = checkCircleClicked(screenX, screenY, sfxSlider.getKnobX(), sfxSlider.getKnobY(), sfxSliderKnob, sfxSlider.sx);
            boolean togglePressed = checkClicked(screenX, screenY, toggleButtonX, toggleButtonY, toggleToggle, 0) || checkClicked(screenX, screenY, toggleButtonX, toggleButtonY, toggleHold, 0);
            boolean resetPressed = checkClicked2(screenX, screenY, resetButton);

            if (exitPressed) {
                exitPressState = 1;
            } else if(musicKnobPressed){
                musicSlider.knobFollow = true;
            } else if(sfxKnobPressed){
                sfxSlider.knobFollow = true;
            } else if (togglePressed) {
                togglePressState = 1;
            } else if (resetPressed){
                resetSettingsPressState = 1;
            }
        }
        return false;
    }

    /**
     * Checks if click was in bound for rectangular buttons
     *
     * @return boolean for whether button is pressed
     */
    private boolean checkClicked(int screenX, int screenY, int buttonX, int buttonY, TextureRegion button, float angle) {

        // TODO: TEMPORARY touch range to make it smaller than button
        float buttonTX = buttonX * (float)Math.cos(angle) + buttonY * (float)Math.sin(angle);
        float buttonTY = -buttonX * (float)Math.sin(angle) + buttonY * (float)Math.cos(angle);
        float screenTX = screenX * (float)Math.cos(angle) + screenY * (float)Math.sin(angle);
        float screenTY = -screenX * (float)Math.sin(angle) + screenY * (float)Math.cos(angle);

        boolean buttonPressedX = buttonTX - TOUCH_AREA_RATIO*BUTTON_SCALE*scale*button.getRegionWidth()/2 <= screenTX &&
                screenTX <= buttonTX + TOUCH_AREA_RATIO*BUTTON_SCALE*scale*button.getRegionWidth()/2;
        boolean buttonPressedY = buttonTY - BUTTON_SCALE*scale*button.getRegionHeight()/2 <= screenTY &&
                screenTY <= buttonTY + BUTTON_SCALE*scale*button.getRegionHeight()/2;

        return buttonPressedX && buttonPressedY;
    }

    /**
     * Checks if click was in bound for rectangular buttons
     *
     * @return boolean for whether button is pressed
     */
    private boolean checkClicked2(int screenX, int screenY,  MenuButton button) {

        // TODO: TEMPORARY touch range to make it smaller than button
        // Gets positional data of button
        float buttonX = button.getX();
        float buttonY = button.getY();
        float angle = button.getAngle();

        // Gives linear translation for tilted buttons
        float buttonTX = buttonX * (float)Math.cos(angle) + buttonY * (float)Math.sin(angle);
        float buttonTY = -buttonX * (float)Math.sin(angle) + buttonY * (float)Math.cos(angle);
        float screenTX = screenX * (float)Math.cos(angle) + screenY * (float)Math.sin(angle);
        float screenTY = -screenX * (float)Math.sin(angle) + screenY * (float)Math.cos(angle);

        // Checks if appropriate area was clicked
        boolean buttonPressedX = buttonTX - TOUCH_AREA_RATIO*BUTTON_SCALE*scale*button.getRegionWidth()/2 <= screenTX &&
                screenTX <= buttonTX + TOUCH_AREA_RATIO*BUTTON_SCALE*scale*button.getRegionWidth()/2;
        boolean buttonPressedY = buttonTY - BUTTON_SCALE*scale*button.getRegionHeight()/2 <= screenTY &&
                screenTY <= buttonTY + BUTTON_SCALE*scale*button.getRegionHeight()/2;

        return buttonPressedX && buttonPressedY;
    }

    /**
     * Checks if click was in bound for circular buttons
     *
     * @return boolean for whether button is pressed
     */
    private boolean checkCircleClicked(float screenX, float screenY, float buttonX, float buttonY, TextureRegion button, float scl) {

        float radius = scl*scale*button.getRegionWidth()/2.0f;
        float dist = (screenX-buttonX)*(screenX-buttonX)+(screenY-buttonY)*(screenY-buttonY);

        // Checks if space inside the circle has been clicked
        return dist < radius*radius;
    }

    /**
     * Checks if click was in bound for circular buttons
     *
     * @return boolean for whether button is pressed
     */
    private boolean checkCircleClicked2(float screenX, float screenY, MenuButton button, float scl) {

        float buttonX = button.getX();
        float buttonY = button.getY();
        float radius = scl*scale*button.getRegionWidth()/2.0f;
        float dist = (screenX-buttonX)*(screenX-buttonX)+(screenY-buttonY)*(screenY-buttonY);

        // Checks if space inside the circle has been clicked
        return dist < radius*radius;
    }
    
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (screenMode == 1) {
            if (selectPressState == 1) {
                selectPressState = 2;
                screenMode = 2;
                return false;
            } else if (settingsPressState == 1) {
                settingsPressState = 2;
                screenMode = 3;
                return false;
            } else if (exitPressState == 1) {
                // Main Menu: Exit Game
                exitPressState = 2;
                currentExitCode = EXIT_QUIT;
                listener.exitScreen(this, currentExitCode);
                currentExitCode = Integer.MIN_VALUE;
            } else if (startPressState == 1) {
                currentLevel = 1;
                startPressState = 2;
                currentExitCode = EXIT_PLAY;
                listener.exitScreen(this, currentExitCode);
                currentExitCode = Integer.MIN_VALUE;
            }
        } else if (screenMode == 2) {
            if (exitPressState == 1) {
                // Level Selector: Back to main screen
                screenMode = 1;
                exitPressState = 2;
            } else if (resetLevelPressState==1){
                //TODO: popup
                resetLevelPressState = 2;
                currentExitCode = EXIT_CONFIRM;
                listener.exitScreen(this, currentExitCode);
                currentExitCode = Integer.MIN_VALUE;
            } else if (levelPressState1 == 1) {
                // TODO: TEMPORARY NEED CHANGE - Level Selector needs to be a list of levels
                currentLevel = 1;
                levelPressState1 = 2;
                currentExitCode = EXIT_PLAY;
                listener.exitScreen(this, currentExitCode);
                currentExitCode = Integer.MIN_VALUE;
            } else if (levelPressState2 == 1) {
                if (levelUnlocked[2]){
                    currentLevel = 2;
                    currentExitCode = EXIT_PLAY;
                    listener.exitScreen(this, currentExitCode);
                    currentExitCode = Integer.MIN_VALUE;
                }
                levelPressState2 = 2;
            } else if (levelPressState3 == 1) {
                if (levelUnlocked[3]) {
                    currentLevel = 3;
                    currentExitCode = EXIT_PLAY;
                    listener.exitScreen(this, currentExitCode);
                    currentExitCode = Integer.MIN_VALUE;
                }
                levelPressState3 = 2;
            }
        } else if (screenMode == 3) {
            if (exitPressState == 1) {
                // Settings: Back to main screen
                exitPressState = 2;
                if (cameForPauseSettings) {
                    listener.exitScreen(this, EXIT_PAUSE);
                } else screenMode = 1;
            } else if (togglePressState == 1) {
                toggleOn = !toggleOn;
                togglePressState = 2;
            } else if (resetSettingsPressState == 1) {
                resetSettingsPressState = 2;
                currentExitCode = EXIT_CONFIRM;
                listener.exitScreen(this, currentExitCode);
                currentExitCode = Integer.MIN_VALUE;
            }
            if(musicSlider.knobFollow) musicSlider.knobFollow = false;
            if(sfxSlider.knobFollow) sfxSlider.knobFollow = false;
            //save user settings
            settings.putFloat("musicVolume", musicVolume);
            settings.putFloat("sfxVolume", sfxVolume);
            settings.putBoolean("toggle", toggleOn);
            settings.flush();
        }
        return true;
    }

    public boolean touchDragged(int screenX, int screenY, int pointer){
        if(screenMode == 3){
            if(musicSlider.knobFollow){
                musicSlider.updateKnob(screenX, screenY);
            }
            if(sfxSlider.knobFollow){
                sfxSlider.updateKnob(screenX, screenY);
            }
        }
        return true;
    }

    /**
     * Sets the ScreenListener for this mode
     *
     * The ScreenListener will respond to requests to quit.
     */
    public void setScreenListener(ScreenListener listener) {
        this.listener = listener;
    }
    /**
     * Draw the status of this player mode.
     *
     * We prefer to separate update and draw from one another as separate methods, instead
     * of using the single render() method that LibGDX does.  We will talk about why we
     * prefer this in lecture.
     */
    public void draw() {
        canvas.begin();
        if (screenMode == 1) {
            canvas.draw(backgroundTexture, Color.WHITE, 0, 0, (float) canvas.getWidth(), (float) canvas.getHeight());
        } else if (screenMode == 2 || screenMode == 3) {
            canvas.draw(backgroundTexture2, Color.WHITE, 0, 0, (float) canvas.getWidth(), (float) canvas.getHeight());
        }

        if (screenMode == 1) {
            // Draw Level Select Button
            levelSelectButton.draw(canvas, selectPressState, BUTTON_SCALE, Color.WHITE);
            // Draw Settings Button
            settingsButton.draw(canvas, settingsPressState, BUTTON_SCALE, Color.WHITE);
            // Draw Exit Button
            exitButton.draw(canvas, exitPressState, BUTTON_SCALE, Color.WHITE);
            // Draw Start Button
            startButton.draw(canvas, startPressState, BUTTON_SCALE, Color.WHITE);
        } else if (screenMode == 2){
            // Draw Back Button
            backButton.draw(canvas, exitPressState, BUTTON_SCALE, Color.WHITE);
            // Temporary Implementation - Will change to iterables once we get proper textures
            levelButton1.draw(canvas, levelPressState1, BUTTON_SCALE, Color.WHITE);
            levelButton2.draw(canvas, levelPressState2, BUTTON_SCALE, levelUnlocked[2] ? Color.WHITE : Color.LIGHT_GRAY);
            levelButton3.draw(canvas, levelPressState3, BUTTON_SCALE, levelUnlocked[3] ? Color.WHITE : Color.LIGHT_GRAY);
            resetButton.draw(canvas, resetLevelPressState, BUTTON_SCALE, Color.WHITE);

        } else if (screenMode == 3) {
            // Draw Back Button
            backButton.draw(canvas, exitPressState, BUTTON_SCALE, Color.WHITE);

            // Draw sliders
            musicSlider.draw(canvas);
            sfxSlider.draw(canvas);

            canvas.draw(settingTag, Color.WHITE, settingTag.getRegionWidth()/2f, settingTag.getRegionHeight()/2f,
                    settingTagX, settingTagY, 0 , TAG_SCL * scale, TAG_SCL * scale);
            canvas.draw(musicTag, Color.WHITE, musicTag.getRegionWidth()/2f, musicTag.getRegionHeight()/2f,
                    musicTagX, musicTagY, 0,  TAG_SCL * scale, TAG_SCL * scale);
            canvas.draw(sfxTag, Color.WHITE, sfxTag.getRegionWidth()/2f, sfxTag.getRegionHeight()/2f,
                    sfxTagX, sfxTagY, 0 , TAG_SCL * scale, TAG_SCL * scale);
            canvas.draw(toggleTag, Color.WHITE, toggleTag.getRegionWidth()/2f, toggleTag.getRegionHeight()/2f,
                    toggleTagX, toggleTagY, 0 , TAG_SCL * scale, TAG_SCL * scale);


            if (toggleOn) {
                canvas.draw(toggleHold, Color.WHITE, toggleHold.getRegionWidth()/2f, toggleHold.getRegionHeight()/2f,
                        toggleButtonX, toggleButtonY, 0 , TAG_SCL * scale, TAG_SCL * scale);
            } else {
                canvas.draw(toggleToggle, Color.WHITE, toggleToggle.getRegionWidth()/2f, toggleToggle.getRegionHeight()/2f,
                        toggleButtonX, toggleButtonY, 0 , TAG_SCL * scale, TAG_SCL * scale);
            }
            resetButton.draw(canvas, resetSettingsPressState, BUTTON_SCALE, Color.WHITE);
        }

        //draw cursor
        int mx = Gdx.input.getX();
        int my = Gdx.graphics.getHeight() - Gdx.input.getY();
        if(mx<Gdx.graphics.getWidth() && mx>0 && my<Gdx.graphics.getHeight() && my>0) {
            canvas.draw(cursorTexture, Color.WHITE, 0, cursorTexture.getRegionHeight(),
                    mx, my, 0, .4f, .4f);
        }

        canvas.end();
    }

    /**
     * Called when the Screen should render itself.
     *
     * We defer to the other methods update() and draw().  However, it is VERY important
     * that we only quit AFTER a draw.
     *
     * @param delta Number of seconds since last animation frame
     */
    public void render(float delta) {
        //DOESN'T WORK. IDK WHY
        //Gdx.graphics.setCursor(newCursor);
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.None);
        // TODO: Move this if necessary
        musicVolume = musicSlider.ratio;
        sfxVolume = sfxSlider.ratio;
        backgroundMusic.play();
        backgroundMusic.setVolume(musicVolume);
        backgroundMusic.setLooping(true);
        draw();
    }

    @Override
    public void resize(int width, int height) {
        // Scaling code from Professor White's code
        float sx = ((float)width)/STANDARD_WIDTH;
        float sy = ((float)height)/STANDARD_HEIGHT;
        scale = (sx < sy ? sx : sy);

        exitButton.setPos(width, height, scale);
        startButton.setPos(width, height, scale);
        settingsButton.setPos(width, height, scale);
        levelSelectButton.setPos(width, height, scale);
        backButton.setPos(width, height, scale);
        resetButton.setPos(width, height, scale);

        levelButton1.setPos(width, height, scale);
        levelButton2.setPos(width, height, scale);
        levelButton3.setPos(width, height, scale);

        this.buttonWidth = (int)(BUTTON_WIDTH_RATIO*width);
        heightY = height;

        musicTagX = (int)(MUSIC_TAG_X_RATIO * width);
        musicTagY = (int)(MUSIC_TAG_Y_RATIO * height);
        sfxTagX = (int)(SFX_TAG_X_RATIO * width);
        sfxTagY = (int)(SFX_TAG_Y_RATIO * height);
        settingTagY = (int)(SETTING_TAG_Y_RATIO * height);
        settingTagX = (int)(SETTING_TAG_X_RATIO * width);
        toggleTagY = (int)(TOGGLE_TAG_Y_RATIO * height);
        toggleTagX = (int)(TOGGLE_TAG_X_RATIO * width);
        toggleButtonY = (int)(TOGGLE_BUTTON_Y_RATIO * height);
        toggleButtonX = (int)(TOGGLE_BUTTON_X_RATIO * width);

        musicSlider.setY(MUSIC_Y_RATIO * height);
        musicSlider.setX(MUSIC_X_RATIO * width);
        sfxSlider.setY(SFX_Y_RATIO * height);
        sfxSlider.setX(SFX_X_RATIO * width);
    }
    /** Returns current level selected */
    public int getCurrentLevel() {
        return currentLevel;
    }
    /** Returns sfx volume */
    public float getSfxVolume() {
        return sfxVolume;
    }
    /** Returns music volume */
    public float getMusicVolume() {
        return musicVolume;
    }
    /** Returns control toggle on/off boolean */
    public boolean getControlToggle() {
        return toggleOn;
    }


    @Override
    public void dispose() {
        //TODO: Need legitimate disposing
        listener = null;
        if (backgroundMusic != null){
            backgroundMusic.stop();
        }
        // NEED TO ADD
    }

    public void pause() {
        if (backgroundMusic != null){
            backgroundMusic.stop();
        }
    }

    public void setMusic(Music music){
        if (!music.equals(backgroundMusic)){
            backgroundMusic.stop();
            backgroundMusic=music;
        }
    }

    /** Reset is for transitioning from other mode to current mode*/
    public void reset() {
        musicVolume = settings.getFloat("musicVolume", 0.5f);
        sfxVolume = settings.getFloat("sfxVolume", 0.5f);
        toggleOn = settings.getBoolean("toggle", false);
        musicSlider.ratio = musicVolume;
        sfxSlider.ratio = sfxVolume;
        if (!cameForPauseSettings)
            backgroundMusic = menuMusic;
        backgroundMusic.play();
        backgroundMusic.setVolume(musicVolume);
        backgroundMusic.setLooping(true);

        this.screenMode = 1;

        if (!levelUnlocked[0]){
            for(int i = 2; i <= LEVEL_COUNT; i++){
                levelUnlocked[i] = unlocked.getBoolean(i+"unlocked", levelUnlocked[i]);
            }
        }
    }
}
