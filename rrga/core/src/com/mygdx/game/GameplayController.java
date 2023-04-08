package com.mygdx.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.audio.Music;
import com.mygdx.game.model.hazard.BirdHazard;
import com.mygdx.game.model.hazard.HazardModel;
import com.mygdx.game.model.hazard.LightningHazard;
import com.mygdx.game.model.PlayerModel;
import com.mygdx.game.model.UmbrellaModel;
import com.mygdx.game.model.WindModel;
import com.mygdx.game.utility.assets.AssetDirectory;
import com.mygdx.game.utility.obstacle.BoxObstacle;
import com.mygdx.game.utility.obstacle.Obstacle;
import com.mygdx.game.utility.util.PooledList;
import com.mygdx.game.utility.util.ScreenListener;

import java.util.Iterator;

public class GameplayController implements ContactListener {
    /**
     * The amount of time for a physics engine step.
     */
    public static final float WORLD_STEP = 1 / 60.0f;

    /**
     * Number of velocity iterations for the constrain solvers
     */
    public static final int WORLD_VELOC = 6;

    /**
     * Number of position iterations for the constrain solvers s
     */
    public static final int WORLD_POSIT = 2;

    /**
     * Width of the game world in Box2d units
     */
    protected static final float DEFAULT_WIDTH = 32.0f;

    /**
     * Height of the game world in Box2d units
     */
    protected static final float DEFAULT_HEIGHT = 18.0f;

    /**
     * the iframes effect duration
     */
    public static final int NUM_I_FRAMES = 120;

    /**
     * All the objects in the world.
     */
    protected PooledList<Obstacle> objects = new PooledList<Obstacle>();

    /**
     * Queue for adding objects
     */
    protected PooledList<Obstacle> addQueue = new PooledList<Obstacle>();

    /**
     * The Box2D world
     */
    protected World world;

    /**
     * The boundary of the world
     */
    protected Rectangle bounds;

    /**
     * The world draw scale (world coordinates to screen coordinates)
     */
    protected Vector2 scale;

    /**
     * Current Width of the game world in Box2d units
     */
    private float physicsWidth;

    /**
     * Current Height of the game world in Box2d units
     */
    private float physicsHeight;

    /**
     * Current Width of the canvas in Box2d units
     */
    private float displayWidth;

    /**
     * Current Height of the canvas in Box2d units
     */
    private float displayHeight;

    /**
     * scaling factors for drag force
     */
    private Vector2 dragScale;

    /**
     * whether the player has won
     */
    private boolean completed;

    /**
     * whether the player has lost
     */
    private boolean failed;

    /**
     * Countdown active for winning or losing
     */
    private int countdown;

    /**
     * the delay after game is lost before we transition to new screen.
     */
    private static final int LOSE_COUNTDOWN_TIMER = 40;

    /**
     * the delay after game is won before we transition to new screen.
     */
    private static final int WIN_COUNTDOWN_TIMER = 20;

    /**
     * Texture asset for background image
     */
    private TextureRegion backgroundTexture;
    /** Background music */
    private Music backgroundMusic;


    // <=============================== Physics objects for the game BEGINS here ===============================>
    /** Physics constants for global */
    private JsonValue globalConstants;
    
    /** Physics constants for current level */
    private JsonValue levelConstants;

    /**
     * Reference to the character avatar
     */
    private PlayerModel avatar;

    /**
     * Reference to the umbrella
     */
    private UmbrellaModel umbrella;

    /**
     * Reference to the goalDoor (for collision detection)
     */
    private BoxObstacle goalDoor;

    /**
     * Mark set to handle more sophisticated collision callbacks
     */
    protected ObjectSet<Fixture> sensorFixtures;

    /**
     * The set of all wind fixtures that umbrella in contact with
     */
    protected ObjectSet<Fixture> contactWindFix = new ObjectSet<>();

    /**
     * The set of all wind bodies that umbrella in contact with
     */
    protected ObjectSet<WindModel> contactWindBod = new ObjectSet<>();

    /**
     * The set of all birds currently in the level
     */
    private ObjectSet<BirdHazard> birds = new ObjectSet<>();

    /**
     * The set of all lightning currently in the level
     */
    private ObjectSet<LightningHazard> lightnings = new ObjectSet<>();

    // <=============================== Physics objects for the game ENDS here ===============================>

    /**
     * cache for vector computations
     */
    Vector2 cache = new Vector2();

    /**
     * cache for wind force computations
     */
    Vector2 windForce = new Vector2();

    //THESE ARE USED FOR MAKING THE UMBRELLA FOLLOW THE MOUSE POINTER

    /**
     * difference in initial position between umbrella and player
     */
    private final Vector2 diff = new Vector2();

    /**
     * center of the screen in canvas coordinates
     */
    private Vector2 center = new Vector2();

    /**
     * the upward-pointing unit vector
     */
    private final Vector2 up = new Vector2(0, 1);

    /**
     * current mouse position
     * <br>
     * should not be updated except when making the umbrella follow the mouse
     */
    private final Vector2 mousePos = new Vector2();

    /**
     * umbrella's last valid angle
     */
    private float lastValidAng;

    /**
     * whether the mouse angle is allowed
     */
    private boolean angInBounds = true;

    /**
     * The level container for GameplayController
     */
    private LevelContainer levelContainer;

    /**
     * Currently selected level
     */
    private int currentLevel = 0;

    // TODO: ====================== BEGIN CURRENTLY UNUSED FIELDS =============================

    /**
     * Listener that will update the player mode when we are done.
     */
    private ScreenListener listener;

    /**
     * JSON value storing all level data
     */
    private JsonValue levels;

    /**
     * The default value of gravity (going down)
     */
    protected static final float DEFAULT_GRAVITY = -4.9f;

    private long jumpId = -1;
    private long fireId = -1;
    private long plopId = -1;

    /**
     * The default sound volume
     */
    private float volume;
    // TODO: ====================== (END) CURRENTLY UNUSED FIELDS =============================


    /**
     * Creates and initialize a new instance of the platformer game
     * <p>
     * The game has default gravity and other settings
     */
    public GameplayController(Rectangle bounds, Vector2 gravity, int level) {
        world = new World(gravity, false);
        this.bounds = new Rectangle(bounds);
        this.scale = new Vector2(1, 1);
        this.dragScale = new Vector2(1, 1);

        world.setContactListener(this);
        sensorFixtures = new ObjectSet<Fixture>();

        // Set current level
        currentLevel = level;

        // Initialize level container
        levelContainer = new LevelContainer(world, this.bounds, this.scale, level);
    }

    /**
     * Gather the assets for this controller.
     * <br>
     * This method extracts the asset variables from the given asset directory. It
     * should only be called after the asset directory is completed.
     *
     * @param directory Reference to global asset manager.
     */
    public void gatherAssets(AssetDirectory directory) {
        // Setting up Constant/Asset Path for different levels
        String constantPath = "global:constants";
        String levelPath = "level" + this.currentLevel + ":constants";

        globalConstants = directory.getEntry(constantPath, JsonValue.class);
        levelConstants = directory.getEntry(levelPath, JsonValue.class);


        // Level container gather assets
        levelContainer.gatherAssets(directory);
        backgroundTexture = new TextureRegion(directory.getEntry( "game:background", Texture.class ));
        backgroundMusic = directory.getEntry("music:level0", Music.class);

        // Constants for Window/World scale
        physicsWidth = levelConstants.get("world").getFloat("max_width", DEFAULT_WIDTH);
        physicsHeight = levelConstants.get("world").getFloat("max_height", DEFAULT_HEIGHT);
        displayWidth = levelConstants.get("world").getFloat("width", DEFAULT_WIDTH);
        displayHeight = levelConstants.get("world").getFloat("height", DEFAULT_HEIGHT);
        dragScale.x = globalConstants.get("player").getFloat("drag_x", 1);
        dragScale.y = globalConstants.get("player").getFloat("drag_y", 1);
    }

    /**
     * Resets the status of the game so that we can play again.
     * <p>
     * This method disposes of the world and creates a new one.
     */
    public void reset() {
        Vector2 gravity = new Vector2(world.getGravity());
        objects = levelContainer.getObjects();

        for (Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }

        // TODO: (review) remove, objects after deactivating physics is still the same set of objects.
        levelContainer.setObjects(objects);

        world.dispose();
        world = new World(gravity, false);
        world.setContactListener(this);

        // game status reset
        failed = false;
        completed = false;

        // empty LevelContainer and update its world.
        levelContainer.reset();
        levelContainer.setWorld(world);

        // Populate LevelContainer w/ same level
        levelContainer.populateLevel();
        // TODO: (review) remove next line, world is still the same object.
        world = levelContainer.getWorld();
        goalDoor = levelContainer.getGoalDoor();

        // Calculate Diff for Umbrella Position
        avatar = levelContainer.getAvatar();
        umbrella = levelContainer.getUmbrella();

        diff.x = umbrella.getX()-avatar.getX();
        diff.y = umbrella.getY()-avatar.getY();

        backgroundMusic.stop();
        backgroundMusic.play();
        backgroundMusic.setLooping(true);
    }


    /**
     * The core gameplay loop of this world.
     * <p>
     * This method contains the specific update code for this mini-game. It does
     * not handle collisions, as those are managed by the parent class WorldController.
     * This method is called after input is read, but before collisions are resolved.
     * The very last thing that it should do is apply forces to the appropriate objects.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void update(InputController input, float dt) {
        // Get objects from level container
        this.avatar = levelContainer.getAvatar();
        this.umbrella = levelContainer.getUmbrella();
        this.birds = levelContainer.getBirds();
        this.lightnings = levelContainer.getLightenings();
        this.world = levelContainer.getWorld();
        this.objects = levelContainer.getObjects();

        // Process actions in object model

        // player dies if falling through void
        if (!failed && avatar.getPosition().y <= -0.01f) {
            avatar.setHealth(0);
            avatar.setiFrames(NUM_I_FRAMES);
            setFailed();
            return;
        }

        // decrement countdown towards rendering victory/fail screen
        if (countdown > 0) {
            countdown--;
        }

        // Check for whether the player toggled the umbrella being open/closed
        if (input.didToggle()) {
            umbrella.setOpen(!umbrella.isOpen());
            if (umbrella.isOpen()) {
                umbrella.useOpenedTexture();
                //TODO: apply some force to the player so the floatiness comes back
            } else {
                umbrella.useClosedTexture();
                Body body = avatar.getBody();
                body.setLinearVelocity(body.getLinearVelocity().x * umbrella.getClosedMomentum(), body.getLinearVelocity().y);
            }
        }
        //make player face forward in air
        if (avatar.isGrounded()) avatar.useSideTexture();
        else avatar.useFrontTexture();

        //umbrella points towards mouse pointer
        center.x = Gdx.graphics.getWidth() / 2f;
        center.y = Gdx.graphics.getHeight() / 2f;
        mousePos.x = input.getMousePos().x;
        mousePos.y = input.getMousePos().y;
        //convert from screen coordinates to canvas coordinates
        mousePos.y = 2 * center.y - mousePos.y;
        //convert to player coordinates
        mousePos.sub(center);
        //normalize manually because Vector2.nor() is less accurate
        float l = mousePos.len();
        mousePos.x /= l;
        mousePos.y /= l;
        //compute new angle
        float mouseAng = (float) Math.acos(mousePos.dot(up));
        if (input.getMousePos().x > center.x) mouseAng *= -1;
        angInBounds = mouseAng <= (float) Math.PI / 2 && mouseAng >= -(float) Math.PI / 2;
        if (angInBounds) {
            umbrella.setAngle(mouseAng);
            lastValidAng = mouseAng;
        } else if (lastValidAng >= 0) {
            umbrella.setAngle((float) Math.PI / 2);
            mousePos.x = -1;
            mousePos.y = 0;
        } else {
            umbrella.setAngle(-(float) Math.PI / 2);
            mousePos.x = 1;
            mousePos.y = 0;
        }

        //average the force of touched winds
        boolean touching_wind = contactWindFix.size > 0;
        float ang = umbrella.getRotation();
        float umbrellaX = (float) Math.cos(ang);
        float umbrellaY = (float) Math.sin(ang);
        int count = 0;
        for (Fixture w : contactWindFix) {
            WindModel bod = (WindModel) w.getBody().getUserData();
            float f = bod.getWindForce(ang);
            if (!contactWindBod.contains(bod) && umbrella.isOpen()) {
                count++;
                windForce.add(umbrellaX * f, umbrellaY * f);
                contactWindBod.add(bod);
            }
        }
        if(count!=0){
            avatar.applyWindForce(windForce.x/count, windForce.y/count);
        }
        contactWindBod.clear();
        windForce.set(0,0);

        // Process actions in object model
        if (avatar.isGrounded()) {
            avatar.setMovement(input.getHorizontal() * avatar.getForce());
            avatar.applyWalkingForce();
        } else if (!touching_wind && umbrella.isOpen() && avatar.getVY() < 0) {
            // player must be falling through AIR
            // apply horizontal force based on rotation, and upward drag.
            float angle = umbrella.getRotation() % ((float) Math.PI * 2);
            if (angle < Math.PI) {
                avatar.applyDragForce(dragScale.x * (float) Math.sin(2 * angle));
            }
        }
        if (umbrella.isOpen() && avatar.getVY() < avatar.getMaxSpeedDownOpen()) {
            avatar.setVY(avatar.getMaxSpeedDownOpen());
        }
        if (!umbrella.isOpen() && avatar.getVY() < avatar.getMaxSpeedDownClosed()) {
            avatar.setVY(avatar.getMaxSpeedDownClosed());
        }

        //System.out.println(avatar.getVX() + ", " + avatar.getVY());

        // TODO: (design) enable this and put it in a conditional statement if we decide to still have an arrow key mode
//        umbrella.setTurning(input.getMouseMovement() * umbrella.getForce());
//        umbrella.applyForce();

        //move the birds
        for (BirdHazard bird : birds) {
            bird.move();
        }

        //update the lightnings
        for (LightningHazard light : lightnings) {
            light.strike();
        }
    }

    /**
     * Processes physics
     * <p>
     * Once the update phase is over, but before we draw, we are ready to handle
     * physics.  The primary method is the step() method in world.  This implementation
     * works for all applications and should not need to be overwritten.
     *
     * @param dt Number of seconds since last animation frame
     */
    public void postUpdate(float dt) {
        // Add any objects created by actions
        while (!levelContainer.addQueue.isEmpty()) {
            levelContainer.addObject(levelContainer.addQueue.poll());
        }

        // Turn the physics engine crank.
        world.step(WORLD_STEP, WORLD_VELOC, WORLD_POSIT);
        //make umbrella follow player position. since it is a static body, we update
        //its position after the world step so that it properly follows the player
        cache.x = avatar.getX() + mousePos.x * diff.len();
        cache.y = avatar.getY() + mousePos.y * diff.len();
        umbrella.setPosition(cache.x, cache.y);

        // Garbage collect the deleted objects.
        // Note how we use the linked list nodes to delete O(1) in place.
        // This is O(n) without copying.
        Iterator<PooledList<Obstacle>.Entry> iterator = objects.entryIterator();
        while (iterator.hasNext()) {
            PooledList<Obstacle>.Entry entry = iterator.next();
            Obstacle obj = entry.getValue();
            if (obj.isRemoved()) {
                obj.deactivatePhysics(world);
                entry.remove();
                if (obj.getClass() == BirdHazard.class) birds.remove((BirdHazard) obj);
            } else {
                // Note that update is called last!
                obj.update(dt);
            }
        }

        // Set objects from level container
        // TODO: (review) Delete the following, object properties are changed but references are not.
        levelContainer.setWorld(world);
        levelContainer.setAvatar(avatar);
        levelContainer.setObjects(objects);
        levelContainer.setUmbrella(umbrella);
        levelContainer.setLightnings(lightnings);
        levelContainer.setBirds(birds);
    }

    /**
     * Callback method for the start of a collision
     * <p>
     * This method is called when we first get a collision between two objects.  We use
     * this method to test if it is the "right" kind of collision.  In particular, we
     * use it to test if we made it to the win door.
     *
     * @param contact The two bodies that collided
     */
    public void beginContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        try {
            Obstacle bd1 = (Obstacle) body1.getUserData();
            Obstacle bd2 = (Obstacle) body2.getUserData();

            // See if we have landed on the ground.
            if ((avatar.getSensorName().equals(fd2) && bd1.getName().contains("platform")) ||
                    (avatar.getSensorName().equals(fd1) && bd2.getName().contains("platform"))) {
                avatar.setGrounded(true);
                sensorFixtures.add(avatar == bd1 ? fix2 : fix1); // Could have more than one ground
            }

            // See if umbrella touches wind
            if ((fd2 == "umbrellaSensor" && (bd1.getClass() == WindModel.class)) ||
                    (fd1 == "umbrellaSensor" && (bd2.getClass() == WindModel.class))) {
                Fixture windFix = (umbrella == bd2 ? fix1 : fix2);
                contactWindFix.add(windFix);
            }

            // Check for hazard collision
            // Is there any way to add fixture data to all fixtures in a polygon obstacle without changing the
            // implementation? If so, want to change to fd1 == "damage"
            if (((umbrella == bd2 || avatar == bd2) && (bd1 instanceof HazardModel && fd1 == null) ||
                    ((umbrella == bd1 || avatar == bd1) && (bd2 instanceof HazardModel && fd2 == null)))) {
                HazardModel h = (HazardModel) (bd1 instanceof HazardModel ? bd1 : bd2);
                int dam = h.getDamage();

                // player is only vulnerable to further damage and effects if the level is still ongoing
                boolean vulnerable = !failed && !completed;
                if (avatar.getiFrames() == 0 && vulnerable) {
                    if (avatar.getHealth() - dam > 0) {
                        Vector2 knockback = h.getKnockbackForce().scl(h.getKnockbackScl());
                        avatar.getBody().applyLinearImpulse(knockback, avatar.getPosition(), true);
                        avatar.setHealth(avatar.getHealth() - dam);
                        avatar.setiFrames(NUM_I_FRAMES);
                    } else {
                        avatar.setHealth(0);
                        // start iframes even when we die, otherwise player being damaged is not so apparent.
                        avatar.setiFrames(NUM_I_FRAMES);
                        setFailed();
                    }
                }
            }

            // check for bird sensor collision
            if ((avatar == bd1 && fd2 == "birdSensor") ||
                    (avatar == bd2 && fd1 == "birdSensor")) {
                BirdHazard bird = (BirdHazard) ("birdSensor" == fd1 ? bd1 : bd2);
                if (!bird.seesTarget) {
                    bird.seesTarget = true;
                    bird.setTargetDir(avatar.getX(), avatar.getY(), avatar.getVX(), avatar.getVY());
                }
            }

            // Check for win condition
            if ((bd1 == avatar && bd2 == goalDoor) ||
                    (bd1 == goalDoor && bd2 == avatar)) {
                // player wins
                if (!failed && !completed) {
                    setCompleted();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Callback method for the start of a collision
     * <p>
     * This method is called when two objects cease to touch.  The main use of this method
     * is to determine when the characer is NOT on the ground.  This is how we prevent
     * double jumping.
     */
    public void endContact(Contact contact) {
        Fixture fix1 = contact.getFixtureA();
        Fixture fix2 = contact.getFixtureB();

        Body body1 = fix1.getBody();
        Body body2 = fix2.getBody();

        Object fd1 = fix1.getUserData();
        Object fd2 = fix2.getUserData();

        Obstacle bd1 = (Obstacle) body1.getUserData();
        Obstacle bd2 = (Obstacle) body2.getUserData();

        if ((avatar.getSensorName().equals(fd2) && avatar != bd1) ||
                (avatar.getSensorName().equals(fd1) && avatar != bd2)) {
            sensorFixtures.remove(avatar == bd1 ? fix2 : fix1);
            if (sensorFixtures.size == 0) {
                avatar.setGrounded(false);
            }
        }

        if ((umbrella == bd2 && bd1.getName().contains("wind")) ||
                (umbrella == bd1 && bd2.getName().contains("wind"))) {
            Fixture windFix = (umbrella == bd2 ? fix1 : fix2);
            contactWindFix.remove(windFix);
        }
    }

    /**
     * Unused ContactListener method
     */
    public void postSolve(Contact contact, ContactImpulse impulse) {
    }

    /**
     * Unused ContactListener method
     */
    public void preSolve(Contact contact, Manifold oldManifold) {
    }

    /**
     * Called when the Screen is paused.
     * <p>
     * We need this method to stop all sounds when we pause.
     * Pausing happens when we switch game modes.
     */
    public void pause() {

    }

    /**
     * Sets current level of the game
     */
    public void setLevel(int level) {
        currentLevel = level;
    }

    /**
     * Returns true if the object is in bounds.
     * <p>
     * This assertion is useful for debugging the physics.
     *
     * @param obj The object to check.
     * @return true if the object is in bounds.
     */
    public boolean inBounds(Obstacle obj) {
        boolean horiz = (bounds.x <= obj.getX() && obj.getX() <= bounds.x + bounds.width);
        boolean vert = (bounds.y <= obj.getY() && obj.getY() <= bounds.y + bounds.height);
        return horiz && vert;
    }

    /**
     * Dispose of all (non-static) resources allocated to this mode.
     */
    public void dispose() {
        // deactivate physics from remaining live objects
        for (Obstacle obj : objects) {
            obj.deactivatePhysics(world);
        }
        // empty out of level container and world
        levelContainer.dispose();
        world.dispose();

        levelContainer = null;
        objects = null;
        addQueue = null;
        bounds = null;
        scale = null;
        world = null;
        dragScale = null;
    }

    /**
     * Gets all the objects in objects
     */
    public PooledList<Obstacle> getObjects() {
        return levelContainer.getObjects();
    }

    public void setScale(Vector2 scale) {
        this.scale = scale;
        levelContainer.setScale(scale);
    }

    /**
     * @return player x-coordinate on screen, -1 if no player is found.
     * coordinates are non-negative.
     */
    public float getPlayerScreenX() {

        return avatar != null ? avatar.getDrawScale().x * avatar.getX() : -1;
    }

    /**
     * @return player y-coordinate on screen, -1 if no player is found.
     * coordinates are non-negative.
     */
    public float getPlayerScreenY() {
        return avatar != null ? avatar.getDrawScale().y * avatar.getY() : -1;
    }

    /**
     * @return reference to player model
     */
    public PlayerModel getPlayer() {
        return avatar;
    }

    /**
     * set world bounds to be the given rectangle dimensions.
     * This should be followed with a reset of the game.
     *
     * @param rect the bounds
     */
    public void setBounds(Rectangle rect) {
        this.bounds.set(rect.x, rect.y, rect.getWidth(), rect.getHeight());
    }


    /**
     * player officially wins if they finished the level and
     * a small countdown is over.
     *
     * @return whether player finished level
     */
    public boolean isCompleted() {
        return completed && countdown <= 0;
    }

    /**
     * player officially fails if they failed the level and
     * a small countdown is over.
     *
     * @return whether player failed
     */
    public boolean isFailed() {
        return failed && countdown <= 0;
    }

    /**
     * set player level status to completed and start a countdown timer
     */
    private void setCompleted() {
        completed = true;
        countdown = WIN_COUNTDOWN_TIMER;
    }

    /**
     * set player level status to failed and start a countdown timer
     */
    private void setFailed() {
        failed = true;
        countdown = LOSE_COUNTDOWN_TIMER;
    }

    /**
     * Get Background Texture
     *
     * @return Background Texture
     */
    public TextureRegion getBackgroundTexture() {
        return this.backgroundTexture;
    }

    /**
     * Get physics dimensions of the world
     *
     * @return physicsDim - [physicsWidth, physicsHeight]
     */
    public float[] getPhysicsDims() {
        float[] physicsDim = new float[2];
        physicsDim[0] = this.physicsWidth;
        physicsDim[1] = this.physicsHeight;
        return physicsDim;
    }

    /**
     * Get physics dimensions of the world
     *
     * @return physicsDim - [physicsWidth, physicsHeight]
     */
    public float[] getDisplayDims() {
        float[] DisplayDim = new float[2];
        DisplayDim[0] = this.displayWidth;
        DisplayDim[1] = this.displayHeight;
        return DisplayDim;
    }
}
