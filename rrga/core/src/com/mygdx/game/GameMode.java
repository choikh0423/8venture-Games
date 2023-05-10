package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.model.MovingPlatformModel;
import com.mygdx.game.model.PlayerModel;
import com.mygdx.game.utility.util.*;
import com.mygdx.game.utility.assets.AssetDirectory;
import com.mygdx.game.utility.obstacle.Obstacle;
import com.mygdx.game.utility.util.ScreenListener;

public class GameMode implements Screen {
    /** Texture asset for background image */
    private TextureRegion backgroundTexture;

    /** Texture asset for SKY parallax layer A*/
    private TextureRegion skyLayerTextureA;

    /** Texture asset for SKY parallax layer B*/
    private TextureRegion skyLayerTextureB;

    /** Texture asset for SKY parallax layer C*/
    private TextureRegion skyLayerTextureC;
    /** Texture for cursor */
    private TextureRegion cursorTexture;
    private final float cursorScl = .33f;

    //TODO: Want to move this to constant.json later
    /** Horizontal Parallax Constant A*/
    private float horizontalA = 0.5f;
    /** Horizontal Parallax Constant B*/
    private float horizontalB = 0.7f;
    /** Horizontal Parallax Constant C*/
    private float horizontalC = 0.9f;

    /** Vertical Parallax Constant A*/
    private float verticalA = 0.5f;
    /** Vertical Parallax Constant B*/
    private float verticalB = 0.75f;
    /** Vertical Parallax Constant C*/
    private float verticalC = 1.0f;

    /** The font for giving messages to the player */
    protected BitmapFont displayFont;

    /** Exit code for quitting the game */
    public static final int EXIT_QUIT = 0;

    /** Exit code for pausing the game */
    public static final int EXIT_PAUSE = 1;

    /** Exit code for victory screen */
    public static final int EXIT_VICTORY = 2;

    /** Exit code for fail screen */
    public static final int EXIT_FAIL = 3;

    /** Current Width of the game world in Box2d units */
    private float physicsWidth;

    /** Current Height of the game world in Box2d units */
    private float physicsHeight;

    /** Current Width of the canvas in Box2d units */
    private float displayWidth;

    /** Current Height of the canvas in Box2d units */
    private float displayHeight;

    /** Width of the game world in Box2d units */
    protected static final float DEFAULT_WIDTH  = 32.0f;
    /** Height of the game world in Box2d units */
    protected static final float DEFAULT_HEIGHT = 18.0f;
    /** Placeholder gravity value */
    protected static final float DEFAULT_GRAVITY = -4.9f;

    /** Reference to the game canvas */
    protected GameCanvas canvas;
    /** Reads input from keyboard or game pad (CONTROLLER CLASS) */
    private InputController inputController;
    /** Constructs the game models and handle basic gameplay (CONTROLLER CLASS) */
    private GameplayController gameplayController;
    /** Listener that will update the player mode when we are done */
    private ScreenListener listener;

    /** The boundary of the world */
    protected Rectangle bounds;

    /** The world scale */
    protected Vector2 scale;

    private boolean debug;

    private LevelParser parser;

    private int currentLevel;

    private BitmapFont debugFont;

    /** reference to asset manager to get level JSON files. */
    private AssetDirectory directory;

    /** temporary vector cache */
    private Vector2 cache;

    /** level in development */
    private JsonValue sampleLevel;

    public static final float standardZoom = 1.0f;

    /** maximum camera zoom scale factor */
    private static final float maximumZoom = 1.25f;

    /** interpolation coefficient for zooming */
    private float zoomAlpha = 0;

    /** how quickly to change zoom scale */
    private static final float zoomAlphaDelta = 0.05f;

    /** the current zoom factor */
    private float zoomScl = 1;

    /**
     * Returns true if debug mode is active.
     *
     * If true, all objects will display their physics bodies.
     *
     * @return true if debug mode is active.
     */
    public boolean isDebug( ) {
        return debug;
    }

    /**
     * Sets whether debug mode is active.
     *
     * If true, all objects will display their physics bodies.
     *
     * @param value whether debug mode is active.
     */
    public void setDebug(boolean value) {
        debug = value;
    }

    /**
     * Returns the canvas associated with this controller
     *
     * The canvas is shared across all controllers
     *
     * @return the canvas associated with this controller
     */
    public GameCanvas getCanvas() {
        return canvas;
    }

    /**
     * Sets the canvas associated with this controller
     *
     * The canvas is shared across all controllers.  Setting this value will compute
     * the drawing scale from the canvas size.
     *
     * @param canvas the canvas associated with this controller
     */
    public void setCanvas(GameCanvas canvas) {
        this.canvas = canvas;
        this.scale.x = canvas.getWidth()/displayWidth;
        this.scale.y = canvas.getHeight()/displayHeight;
        gameplayController.setScale(this.scale);
    }

    public Music getMusic(){return gameplayController.getMusic();}

    /**
     * Creates a new game world with the default values.
     *
     * The game world is scaled so that the screen coordinates do not agree
     * with the Box2d coordinates.  The bounds are in terms of the Box2d
     * world, not the screen.
     */
    protected GameMode() {
        this(new Rectangle(0,0,DEFAULT_WIDTH,DEFAULT_HEIGHT),
                new Vector2(0,DEFAULT_GRAVITY));
    }

    /**
     * Creates a new game world
     *
     * The game world is scaled so that the screen coordinates do not agree
     * with the Box2d coordinates.  The bounds are in terms of the Box2d
     * world, not the screen.
     *
     * @param width      The width in Box2d coordinates
     * @param height    The height in Box2d coordinates
     * @param gravity    The downward gravity
     */
    protected GameMode(float width, float height, float gravity) {
        this(new Rectangle(0,0,width,height), new Vector2(0,gravity));
    }

    /**
     * Creates a new game world
     *
     * The game world is scaled so that the screen coordinates do not agree
     * with the Box2d coordinates.  The bounds are in terms of the Box2d
     * world, not the screen.
     *
     * @param bounds    The game bounds in Box2d coordinates
     * @param gravity    The gravitational force on this Box2d world
     */
    protected GameMode(Rectangle bounds, Vector2 gravity) {
        debug  = false;
        this.bounds = bounds;
        this.scale = new Vector2(1,1);
        this.currentLevel = 1;

        // Create the controllers.
        inputController = new InputController();
        gameplayController = new GameplayController(bounds, gravity, 0);
        cache = new Vector2(1,1);
    }

    /**
     * Dispose of all (non-static) resources allocated to this mode.
     */
    public void dispose() {
        // Dispose Controllers
        gameplayController.dispose();
        inputController = null;
        gameplayController = null;

        bounds = null;
        scale  = null;
        canvas = null;
        parser = null;
        cache = null;

        // GameMode does not own the directory, so it does not unload assets
        directory = null;
    }


    /**
     * Gather the assets for this controller.
     *
     * This method extracts the asset variables from the given asset directory. It
     * should only be called after the asset directory is completed.
     *
     * @param directory    Reference to global asset manager.
     */
    public void gatherAssets(AssetDirectory directory) {
        this.directory = directory;

        JsonValue worldData = directory.getEntry("global:constants", JsonValue.class).get("world");
        displayWidth = worldData.get("width").asFloat();
        displayHeight = worldData.get("height").asFloat();

        gameplayController.gatherAssets(directory);

        backgroundTexture = new TextureRegion(directory.getEntry("game:background", Texture.class));
        skyLayerTextureA =  new TextureRegion(directory.getEntry("game:skylayerA", Texture.class));
        skyLayerTextureB =  new TextureRegion(directory.getEntry("game:skylayerB", Texture.class));
        skyLayerTextureC =  new TextureRegion(directory.getEntry("game:skylayerC", Texture.class));

        cursorTexture = new TextureRegion(directory.getEntry("game:cursor_ingame", Texture.class));

        debugFont = directory.getEntry("shared:minecraft", BitmapFont.class);

        // instantiate level parser for loading levels
        parser = new LevelParser(directory);
        // pass parser reference to level container to lessen the traffic on GameMode -> Gameplay -> Container.
        gameplayController.getLevelContainer().setParser(parser);
    }

    public int resetCounter = -1;
    private Vector2 camPos = new Vector2();
    /**
     * Resets the status of the game so that we can play again.
     *
     * This method disposes of the world and creates a new one.
     */
    public void reset() {
        // level may have changed, parse data
        // this is instantaneous if the level has been parsed in previous reset.

        // TODO: REMOVE FOR SUBMISSION
        // this ignores all levels, always runs the given file
        if (sampleLevel != null){
            parser.parseLevel(sampleLevel);
        }
        else {
            parser.parseLevel(directory.getEntry("tiled:level"+currentLevel, JsonValue.class));
        }

        physicsWidth = parser.getWorldSize().x;
        physicsHeight = parser.getWorldSize().y;
        this.bounds.set(0,0, physicsWidth, physicsHeight);
        gameplayController.setBounds(this.bounds);
        gameplayController.reset();

        resetCounter++;
    }

    Preferences unlocked = Gdx.app.getPreferences("unlocked");
    /**
     * Returns whether to process the update loop
     *
     * At the start of the update loop, we check if it is time
     * to switch to a new game mode.  If not, the update proceeds
     * normally.
     *
     * @param dt    Number of seconds since last animation frame
     *
     * @return whether to process the update loop
     */
    public boolean preUpdate(float dt) {
        if (listener == null) {
            return true;
        }

        if (gameplayController.isCompleted()) {
            listener.exitScreen(this, EXIT_VICTORY);
            unlocked.putBoolean((currentLevel+1)+"unlocked", true);
            unlocked.flush();
            return false;
        } else if (gameplayController.isFailed()) {
            listener.exitScreen(this, EXIT_FAIL);
            return false;
        }

        inputController.readInput(bounds, scale);

        // Toggle debug
        if (inputController.didDebug()) {
            debug = !debug;
        }

        // Handle resets
        if (inputController.didReset()) {
            reset();
            return true;
        }

        // TODO: TEMPORARY NEXT LEVEL
        if (inputController.didNext()) {
            setNextLevel();
            reset();
            return true;
        }

        // Now it is time to maybe switch screens.
        if (inputController.didExit()) {
            listener.exitScreen(this, EXIT_QUIT);
            return false;
        }

        // Pause button pressed, no changes to internal state of game world
        if (inputController.didPause()) {
            listener.exitScreen(this, EXIT_PAUSE);
            return false;
        }

        return true;
    }

    /**
     * The core gameplay loop of this world.
     *
     * This method contains the specific update code. It does not handle collisions,
     * as those are managed by gameplay controller through Box2D.
     * This method is called after input is read, in which case a decision has been
     * made about updating the game state. This method should be followed by a call
     * to draw the elements of the world.
     *
     * @param dt    Number of seconds since last animation frame
     */
    public void update(float dt) {
        //contain cursor
        Gdx.input.setCursorCatched(true);
        int x = Gdx.input.getX();
        int y = Gdx.input.getY();
        if(Gdx.input.getY() < cursorTexture.getRegionHeight()/2f * cursorScl){
            y = (int) (cursorTexture.getRegionHeight()/2f * cursorScl);
        }
        if(Gdx.input.getY() > Gdx.graphics.getHeight() - (cursorTexture.getRegionHeight()/2f * cursorScl)){
            y = Gdx.graphics.getHeight() - (int) (cursorTexture.getRegionHeight()/2f * cursorScl);
        }
        if(Gdx.input.getX() < cursorTexture.getRegionWidth()/2f * cursorScl){
            x = (int) (cursorTexture.getRegionWidth()/2f * cursorScl);;
        }
        if(Gdx.input.getX() > Gdx.graphics.getWidth() - (cursorTexture.getRegionWidth()/2f * cursorScl)){
            x = Gdx.graphics.getWidth() - (int) (cursorTexture.getRegionWidth()/2f * cursorScl);
        }
        Gdx.input.setCursorPosition(x,y);

        if (inputController.didZoom() && gameplayController.getPlayer().isGrounded() && !gameplayController.getPlayer().isMoving() && gameplayController.getPlayer().getLinearVelocity().epsilonEquals(0,0)){
            zoomAlpha += zoomAlphaDelta;
        }
        else {
            zoomAlpha -= zoomAlphaDelta;
        }

        if (gameplayController.getPlayer().getiFrames()>0) zoomAlpha = 0;

        // constraint zoomAlpha into [0,1] range
        if (zoomAlpha < 0){ zoomAlpha = 0; }
        else if (zoomAlpha > 1){ zoomAlpha = 1; }

        zoomScl = standardZoom * (1 - zoomAlpha) + (zoomAlpha) * (maximumZoom);
        canvas.setDynamicCameraZoom(zoomScl);

        gameplayController.update(inputController, dt);
        gameplayController.postUpdate(dt);
    };

    public boolean showGoal = true;
    /**
     * Draw the physics objects to the canvas
     *
     * The method draws all objects in the order that they were added.
     * Heads-up display (HUD) content is drawn on top of these physics objects.
     *
     * @param dt    Number of seconds since last animation frame
     */
    public void draw(float dt, boolean cursor) {
        canvas.clear();

        // focus camera on player
        float px = gameplayController.getPlayerScreenX();
        float py = gameplayController.getPlayerScreenY();
        float gx = gameplayController.getLevelContainer().getShowGoal().getX();
        float gy = gameplayController.getLevelContainer().getShowGoal().getY();

        canvas.setCameraDynamic();
        Vector2 scl = gameplayController.getPlayer().getDrawScale();

        //camera starts at the goal door then moves to the player the
        //first time we reset the level (i.e. when loading in)
        if (gameplayController.getLevelContainer().getShowGoal().getPatrol() == MovingPlatformModel.MoveBehavior.REVERSE || resetCounter > 0) showGoal = false;
        if (showGoal){
            camPos.set(gx*scl.x, gy*scl.y);
        } else {
            camPos.set(px,py);
        }
        canvas.translateCameraToPoint(camPos.x,camPos.y);
        canvas.begin();

        float sclY = canvas.getWidth() * zoomScl/backgroundTexture.getRegionWidth();
        float sclX = canvas.getHeight() * zoomScl /backgroundTexture.getRegionHeight();

        // center a background on player
        // TODO: replace with repeating background? - Currently the background is drawn according to camera scale.
        canvas.draw(backgroundTexture, Color.WHITE, backgroundTexture.getRegionWidth()/2f,
                backgroundTexture.getRegionHeight()/2f, camPos.x,camPos.y, 0, sclX, sclY);

        float worldHeight = physicsHeight * scale.y;

        // Parallax Drawing
        //TODO: REMOVE THIS COMMENT FOR PARALLAX IMPLEMENTATION (IT NEEDS FURTHER SCALING AND CHANGE OF ASSET)
        if (currentLevel == 2) {
            canvas.drawWrapped(skyLayerTextureA, -px * horizontalA, -py * verticalA, px, py, worldHeight, zoomScl, sclX, sclY);
            canvas.drawWrapped(skyLayerTextureB, -px * horizontalB, -py * verticalB, px, py, worldHeight, zoomScl, sclX, sclY);
            canvas.drawWrapped(skyLayerTextureC, -px * horizontalC, -py * verticalC, px, py, worldHeight, zoomScl, sclX, sclY);
        }
        PlayerModel avatar = gameplayController.getPlayer();

        // draw all game objects + stickers + tile layers, these objects are "dynamic"
        // a change in player's position should yield a different perspective.
        float ax = camPos.x/scl.x;
        float ay = camPos.y/scl.y;
        int objCount = 0;
        int tileCount = 0;
        for(Drawable drawable : gameplayController.getDrawables()) {
            if (drawable instanceof TiledLayer){
                TiledLayer tiledLayer = (TiledLayer) drawable;
                tiledLayer.draw(canvas, ax, ay, displayWidth/2 * zoomScl, displayHeight/2 * zoomScl);
                tileCount += tiledLayer.lastDrawn();
            }
            else if (drawable instanceof PlayerModel){
                drawable.draw(canvas);
                gameplayController.getLevelContainer().getUmbrella().draw(canvas);
                objCount++;
            }
            else {
                cache.set(drawable.getBoxCorner());
                float bx = cache.x;
                float by = cache.y;
                cache.set(drawable.getDimensions());
                float width = cache.x;
                float height = cache.y;
                if (bx > ax + zoomScl * displayWidth/2f || bx + width < ax - zoomScl * displayWidth/2f
                    || by < ay - zoomScl * displayHeight/2f || by - height > ay + zoomScl * displayHeight/2f ){
                    continue;
                }
                drawable.draw(canvas);
                objCount++;
            }
        }

        canvas.end();

        if (debug) {
            canvas.beginDebug();
            // Drawables do not include platforms
//            for(Drawable drawable : gameplayController.getDrawables()) {
//                cache.set(drawable.getBoxCorner());
//                float bx = cache.x;
//                float by = cache.y;
//                cache.set(drawable.getDimensions());
//                float width = cache.x;
//                float height = cache.y;
//                if (bx > ax + zoomScl * displayWidth/2f || bx + width < ax - zoomScl * displayWidth/2f
//                        || by < ay - zoomScl * displayHeight/2f || by - height > ay + zoomScl * displayHeight/2f ){
//                    continue;
//                }
//                drawable.drawDebug(canvas);
//            }
            for (Obstacle o : gameplayController.getObjects()){
                o.drawDebug(canvas);
            }
            canvas.endDebug();
        }

        // Draw all HUD content
        canvas.setCameraHUD();
        PlayerModel p = gameplayController.getPlayer();
        canvas.begin();
        p.drawInfo(canvas);

        //draw cursor
        if(cursor) {
            int mx = Gdx.input.getX();
            int my = Gdx.graphics.getHeight() - Gdx.input.getY();
            canvas.draw(cursorTexture, Color.WHITE, cursorTexture.getRegionWidth() / 2f, cursorTexture.getRegionHeight() / 2f,
                    mx, my, 0, cursorScl, cursorScl);
        }

        // debug information on screen to track FPS, etc
        if (debug){
            debugFont.setColor(Color.BLACK);
            int fps = (int) (1/dt);
            String s = fps >= 59 ? "GOOD" : fps >= 57 ? "MEDIOCRE" : "BAD";
            Color c = fps >= 58 ? Color.GREEN : fps >= 56 ? Color.YELLOW : Color.RED;
            canvas.drawText("FPS:" + fps, debugFont, 0.1f*canvas.getWidth(), 0.95f*canvas.getHeight());
            debugFont.setColor(c);
            canvas.drawText("FPS status: " + s, debugFont, 0.1f * canvas.getWidth(), 0.9f * canvas.getHeight());
            debugFont.setColor(Color.BLACK);
            canvas.drawText("X:" + p.getX(), debugFont, 0.1f*canvas.getWidth(), 0.85f*canvas.getHeight());
            canvas.drawText("Y:" + p.getY(), debugFont, 0.1f*canvas.getWidth(), 0.8f*canvas.getHeight());
            canvas.drawText("VX:" + p.getVX(), debugFont, 0.1f*canvas.getWidth(), 0.75f*canvas.getHeight());
            canvas.drawText("VY:" + p.getVY(), debugFont, 0.1f*canvas.getWidth(), 0.7f*canvas.getHeight());
            canvas.drawText("HP:" + p.getHealth(), debugFont, 0.1f*canvas.getWidth(), 0.65f*canvas.getHeight());
            cache.set(inputController.getMousePos());
            canvas.drawText("MouseScreenX:" + cache.x, debugFont, 0.1f*canvas.getWidth(), 0.6f*canvas.getHeight());
            canvas.drawText("MouseScreenY:" + cache.y, debugFont, 0.1f*canvas.getWidth(), 0.55f*canvas.getHeight());
            canvas.drawText("MouseX:" + ((cache.x - canvas.getWidth()/2f)/scale.x + p.getX()),
                    debugFont, 0.1f*canvas.getWidth(), 0.50f*canvas.getHeight());
            canvas.drawText("MouseY:" + ((canvas.getHeight()/2f - cache.y)/scale.y + p.getY()),
                    debugFont, 0.1f*canvas.getWidth(), 0.45f*canvas.getHeight());
            canvas.drawText("MouseAng:" + gameplayController.getLevelContainer().getUmbrella().getAngle(),
                    debugFont, 0.1f*canvas.getWidth(), 0.4f*canvas.getHeight());
            canvas.drawText("Objects Drawn:" + (objCount + 1),
                    debugFont, 0.1f*canvas.getWidth(), 0.35f*canvas.getHeight());
            canvas.drawText("Tiles Drawn:" + tileCount,
                    debugFont, 0.1f*canvas.getWidth(), 0.30f*canvas.getHeight());
            canvas.drawText("Grounded:" + avatar.isGrounded(),
                    debugFont, 0.1f*canvas.getWidth(), 0.25f*canvas.getHeight());
        }
        canvas.end();
    }




    /**
     * Method to ensure that a sound asset is only played once.
     *
     * Every time you play a sound asset, it makes a new instance of that sound.
     * If you play the sounds to close together, you will have overlapping copies.
     * To prevent that, you must stop the sound before you play it again.  That
     * is the purpose of this method.  It stops the current instance playing (if
     * any) and then returns the id of the new instance for tracking.
     *
     * @param sound        The sound asset to play
     * @param soundId    The previously playing sound instance
     *
     * @return the new sound instance for this asset.
     */
    public long playSound(Sound sound, long soundId) {
        return playSound( sound, soundId, 1.0f );
    }


    /**
     * Method to ensure that a sound asset is only played once.
     *
     * Every time you play a sound asset, it makes a new instance of that sound.
     * If you play the sounds to close together, you will have overlapping copies.
     * To prevent that, you must stop the sound before you play it again.  That
     * is the purpose of this method.  It stops the current instance playing (if
     * any) and then returns the id of the new instance for tracking.
     *
     * @param sound        The sound asset to play
     * @param soundId    The previously playing sound instance
     * @param volume    The sound volume
     *
     * @return the new sound instance for this asset.
     */
    public long playSound(Sound sound, long soundId, float volume) {
        if (soundId != -1) {
            sound.stop( soundId );
        }
        return sound.play(volume);
    }

    /**
     * Called when the Screen is resized.
     *
     * This can happen at any point during a non-paused state but will never happen
     * before a call to show().
     *
     * @param width  The new width in pixels
     * @param height The new height in pixels
     */
    public void resize(int width, int height) {
        // IGNORE FOR NOW
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
            if (preUpdate(delta)) {
                update(delta); // This is the one that must be defined.
            }
            draw(delta, true);
    }

    /**
     * Called when transitioning to other modes
     *
     */
    public void pause() {
        gameplayController.pause();
    }
    /**
     * Called when the Screen is resumed from a paused state.
     *
     * This is usually when it regains focus.
     */
    public void resume() {
        // does nothing
    }

    /**
     * Called when this screen becomes the current screen for a Game.
     */
    public void show() {
        // Useless if called in outside animation loop
    }

    /**
     * Called when this screen is no longer the current screen for a Game.
     */
    public void hide() {
        // Useless if called in outside animation loop
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
     * Sets current level of the game
     */
    public void setLevel(int level){
        currentLevel = level;
        resetShowGoal();
        resetCounter--;
        gameplayController.resetCounter--;
    }

    /**
     * Sets current level of the game
     */
    public void setNextLevel(){
        if (currentLevel < 3) {
            currentLevel += 1;
        } else {
            currentLevel = 1;
        }
        resetShowGoal();
        resetCounter--;
        gameplayController.resetCounter--;
    }

    public void resetShowGoal(){
        resetCounter = 0;
        showGoal = true;
        gameplayController.resetCounter = 0;
        gameplayController.showGoal = true;
    }

    /**
     * Sets current level of the game
     */
    public void setVolume(float sfxVolume, float musicVolume){
        gameplayController.setVolume(sfxVolume, musicVolume);
    }

    /**
     * Sets secondary control mode
     */
    public void setSecondaryControlMode(boolean toggleOn){
        inputController.setSecondaryControlMode(toggleOn);
    }
    /**
     * temporary override of levels with sample level
     * @param sampleLevel tiled json
     */
    public void setSampleLevel(JsonValue sampleLevel){ this.sampleLevel = sampleLevel;}

}
