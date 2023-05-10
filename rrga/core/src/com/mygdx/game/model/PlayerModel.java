/*
 * PlayerModel.java
 *
 * You SHOULD NOT need to modify this file.  However, you may learn valuable lessons
 * for the rest of the lab by looking at it.
 *
 * Author: Walker M. White
 * Based on original PhysicsDemo Lab by Don Holden, 2007
 * Updated asset version, 2/6/2021
 */
package com.mygdx.game.model;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.mygdx.game.GameCanvas;
import com.mygdx.game.utility.obstacle.CapsuleObstacle;
import com.mygdx.game.utility.util.Drawable;

/**
 * Player avatar for the plaform game.
 * <p>
 * Note that this class returns to static loading.  That is because there are
 * no other subclasses that we might loop through.
 */
public class PlayerModel extends CapsuleObstacle implements Drawable {

	/** The initializing data (to avoid magic numbers) */
	private final JsonValue data;

	/** The factor to multiply by the input */
	private final float force;
	/** The amount to slow the character down */
	private final float damping;
	/** The maximum character horizontal speed in the air */
	private final float maxspeed_x_air_wind;
	private final float maxspeed_x_air_drag;
	/** The maximum character horizontal speed on the ground */
	private final float maxspeed_x_ground;
	/** The maximum character vertical speed */
	private final float maxspeed_up;
	private final float maxspeed_down_open;
	private final float maxspeed_down_closed;
	/** Identifier to allow us to track the sensor in ContactListener */
	private final String sensorName;

	/** The current horizontal movement of the character */
	private float   movement;
	/** Which direction is the character facing */
	private boolean faceRight;
	/** How long until we can jump again */
	private int jumpCooldown;
	/** Whether we are actively jumping */
	private boolean isJumping;
	/** Whether our feet are on the ground */
	private boolean isGrounded;
	/** Whether we are zooming out or not */
	private boolean isZooming;
	/** The physics shape of this object */
	private PolygonShape sensorShape;
	/** The size of the player in physics units (up to scaling by shrink factor) */
	private float[] size;
	/** Player Mass */
	private float FINAL_MASS = 1.25f;
	/** Max player hp */
	private int MAX_HEALTH;
	/** Player hp */
	private int health;
	public BitmapFont healthFont;

	/** draw depth */
	private int depth;

	/** Cache for getters */
	private final Vector2 temp = new Vector2();

	/** Another vector cache */
	private final Vector2 temp2 = new Vector2();

	/** Cache for internal force calculations */
	private final Vector2 forceCache = new Vector2();

	/**
	 * The number i-frames the player currently has
	 */
	private int iFrames;

	/** health point texture */
	private TextureRegion[] hpTexture;
	/** Boost texture */
	private TextureRegion[] boostTexture;

	/** The player's front view texture (this is the main texture for air) */
	private TextureRegion frontTexture;

	/** The player's side view texture (this is the main texture for platform) */
	private TextureRegion sideTexture;

	/** The number of updates before the texture is switched when I-Frames are active */
	private int iFrameCountdown = 7;

	/** When the player is hit, whether or not the all white texture is drawn.
	 * Swaps between all white and regular */
	private boolean drawIFrameTexture = true;

	/** The magnitude of the lighter force */
	private float lighterForce;
	private final float maxLighterFuel;
	private float lighterFuel;
	private float lighterChangeRate;

	// <=============================== Animation objects start here ===============================>
	/** Player walk animation*/
	private Animation<TextureRegion> walkAnimation;

	/** Player walk animation elapsed time */
	float walkElapsedTime;

	/** Player fall animation*/
	private Animation<TextureRegion> fallAnimation;

	/** Player fall animation elapsed time */
	private float fallElapsedTime;

	/** Player idle animation texture */
	private Animation<TextureRegion> idleAnimation;

	/** Player idle animation elapsed time */
	private float idleElapsedTime;

	/** Player look animation texture */
	private Animation<TextureRegion> lookAnimation;

	/** Player look animation elapsed time */
	private float lookElapsedTime;

	/**
	 * Returns left/right movement of this character.
	 *
	 * This is the result of input times player force.
	 *
	 * @return left/right movement of this character.
	 */
	public float getMovement() {
		return movement;
	}

	/**
	 * Sets left/right movement of this character.
	 *
	 * This is the result of input times player force.
	 *
	 * @param value left/right movement of this character.
	 */
	public void setMovement(float value) {
		movement = value;
		// Change facing if appropriate
		if (movement < 0) {
			faceRight = false;
		} else if (movement > 0) {
			faceRight = true;
		}
	}

	/**
	 * Returns true if the player is actively jumping.
	 *
	 * @return true if the player is actively jumping.
	 */
	public boolean isJumping() {
		return isJumping && isGrounded && jumpCooldown <= 0;
	}

	/**
	 * Sets whether the player is actively jumping.
	 *
	 * @param value whether the player is actively jumping.
	 */
	public void setJumping(boolean value) {
		isJumping = value;
	}

	/**
	 * Returns true if the player is on the ground.
	 *
	 * @return true if the player is on the ground.
	 */
	public boolean isGrounded() {
		return isGrounded;
	}

	/**
	 * Sets whether the player is on the ground.
	 *
	 * @param value whether the player is on the ground.
	 */
	public void setGrounded(boolean value) {
		isGrounded = value;
	}

	/**
	 * Returns true if the player is zooming out.
	 *
	 * @return true if the player is zooming out.
	 */
	public boolean isZooming() {
		return isZooming;
	}

	/**
	 * Sets whether the player is zooming out.
	 *
	 * @param value whether the player is zooming out.
	 */
	public void setZooming(boolean value) {
		isZooming = value;
	}

	/**
	 *
	 * */
	public boolean isMoving() {
		return this.movement != 0f;
	}
	/**
	 * Returns how much force to apply to get the player moving
	 *
	 * Multiply this by the input to get the movement value.
	 *
	 * @return how much force to apply to get the player moving
	 */
	public float getForce() {
		return force;
	}

	/**
	 * Returns ow hard the brakes are applied to get a player to stop moving
	 *
	 * @return ow hard the brakes are applied to get a player to stop moving
	 */
	public float getDamping() {
		return damping;
	}

	/**
	 * Returns the upper limit on player left-right movement.
	 *
	 * This does NOT apply to vertical movement.
	 *
	 * @return the upper limit on player left-right movement.
	 */
	public float getMaxSpeedXAirWind() {
		return maxspeed_x_air_wind;
	}

	public float getMaxSpeedXAirDrag() {
		return maxspeed_x_air_drag;
	}

	public float getMaxSpeedXGround() {
		return maxspeed_x_ground;
	}

	/**
	 * Returns the upper limit on player upwards movement.
	 *
	 * This does NOT apply to horizontal movement.
	 *
	 * @return the upper limit on player upwards movement.
	 */
	public float getMaxSpeedUp() {
		return maxspeed_up;
	}
	public float getMaxSpeedDownOpen() {
		return maxspeed_down_open;
	}
	public float getMaxSpeedDownClosed() {
		return maxspeed_down_closed;
	}

	/**
	 * Returns the name of the ground sensor
	 *
	 * This is used by ContactListener
	 *
	 * @return the name of the ground sensor
	 */
	public String getSensorName() {
		return sensorName;
	}

	/**
	 * Returns true if this character is facing right
	 *
	 * @return true if this character is facing right
	 */
	public boolean isFacingRight() {
		return faceRight;
	}

	/**
	 * Returns the player's max hp. Should only be used when initializing the player
	 *
	 * @return the max hp
	 */
	public int getMaxHealth(){return MAX_HEALTH;}

	/**
	 * Sets the player's max hp. Does not allow max health to be less than 1;
	 * we want the player to have hp!
	 *
	 * @param hp the new max hp value
	 */
	public void setMaxHealth(int hp){
		if (hp >= 1) MAX_HEALTH = hp;
		else MAX_HEALTH = 1;
		if (getHealth() > MAX_HEALTH) setHealth(MAX_HEALTH);
	}

	/**
	 * Returns the player's current hp
	 *
	 * @return the current hp
	 */
	public int getHealth(){return health;}

	/**
	 * Sets the player's current hp. If this value is above the maximum,
	 * sets it to the maximum. If this value is below 0, sets it to 0.
	 *
	 * @param hp the new hp value
	 */
	public void setHealth(int hp){
		if (hp < 0) health = 0;
		else if (hp > MAX_HEALTH) health = MAX_HEALTH;
		else health = hp;
	}

	/**
	 * Returns the player's i-frames
	 *
	 * @return the current i-frames
	 */
	public int getiFrames(){return iFrames;}

	/**
	 * Sets the player's i-frames
	 *
	 * @param f the new i-frames value
	 */
	public void setiFrames(int f){
		iFrames = f;
	}

	public boolean canBoost(){
		return lighterFuel == maxLighterFuel;
	}


	/**
	 * sets the player's HP texture.
	 * @param texture the HP texture
	 */
	public void setHpTexture(Texture texture){
		TextureRegion[][] tempTexture = TextureRegion.split(texture, 304, 200);
		hpTexture = new TextureRegion[4];

		// Ordering Texture Tile
		int count = 0;
		for (int i = 1; i > -1; i--) {
			for (int j = 1; j > -1; j--){
				hpTexture[count] = tempTexture[i][j];
				count ++;
			}
		}
	}

	/**
	 * sets the player's boost texture.
	 * @param texture the boost texture
	 */
	public void setBoostTexture(Texture texture){
		TextureRegion[][] tempTexture = TextureRegion.split(texture, texture.getWidth()/5, texture.getHeight()/2);
		boostTexture = new TextureRegion[10];

		// Ordering Texture Tile
		int count = 0;
		for (int i = 1; i > -1; i--){
			for (int j = 4; j > -1; j--) {
				boostTexture[count] = tempTexture[i][j];
				count ++;
			}
		}
	}

	/**
	 * sets the player's in-air texture.
	 * @param texture front view texture
	 */
	public void setFrontTexture(TextureRegion texture){
		this.frontTexture = texture;
	}

	/**
	 * sets the player's platform/ground texture.
	 * @param texture side view texture
	 */
	public void setSideTexture(TextureRegion texture){this.sideTexture = texture;}

	/**
	 * Sets player walk animation
	 * NOTE: iterator is specific to current filmstrip - need to change value if tile dimension changes on filmstrip
	 * */
	public void setWalkAnimation(Texture texture) {
		//TODO maybe find a way to do this without constants?
		TextureRegion[][] tempFrames = TextureRegion.split(texture, 252, 352);
		TextureRegion[] frames = new TextureRegion[8];

		// Placing animation frames in order
		int index = 0;
		for (int i=0; i<tempFrames.length; i++) {
			for (int j=0; j<tempFrames[0].length; j++) {
				frames[index] = tempFrames[i][j];
				index++;
			}
		}

		// Adjust walk animation speed here
		this.walkAnimation = new Animation<>(1f/12f, frames);
	}

	/**
	 * Sets player falling animation
	 * NOTE: iterator is specific to current filmstrip - need to change value if tile dimension changes on filmstrip
	 * */
	public void setFallingAnimation(Texture texture) {
		//TODO maybe find a way to do this without constants?
		TextureRegion[][] tempFrames = TextureRegion.split(texture, 252, 352);
		TextureRegion[] frames = new TextureRegion[4];

		// Placing animation frames in order
		int index = 0;
		for (int i=0; i<tempFrames.length; i++) {
			for (int j=0; j<tempFrames[0].length; j++) {
				frames[index] = tempFrames[i][j];
				index++;
			}
		}

		// Adjust fall animation speed here
		this.fallAnimation = new Animation<>(1f/12f, frames);
	}

	/**
	 * Sets player idle animation
	 * NOTE: iterator is specific to current filmstrip - need to change value if tile dimension changes on filmstrip
	 * */
	public void setIdleAnimation(Texture texture){
		TextureRegion[][] tempFrames = TextureRegion.split(texture, 252, 352);
		TextureRegion[] frames = new TextureRegion[15];

		// Placing animation frames in order
		int index = 0;
		for (int i=0; i<tempFrames.length; i++) {
			for (int j=0; j<tempFrames[0].length; j++) {
				frames[index] = tempFrames[i][j];
				index++;
			}
		}

		// Adjust idle animation speed here
		idleAnimation = new Animation<>(1f/15f, frames);
	}

	/**
	 * Sets player look animation
	 * NOTE: iterator is specific to current filmstrip - need to change value if tile dimension changes on filmstrip
	 * */
	public void setLookAnimation(Texture texture){
		TextureRegion[][] tempFrames = TextureRegion.split(texture, 252, 352);
		TextureRegion[] frames = new TextureRegion[32];

		// Placing animation frames in order
		int index = 0;
		for (int i=0; i<tempFrames.length; i++) {
			for (int j=0; j<tempFrames[0].length; j++) {
				frames[index] = tempFrames[i][j];
				index++;
			}
		}

		// Adjust idle animation speed here
		lookAnimation = new Animation<>(1f/12f, frames);
	}

	/**
	 * sets the texture to be frontal view for drawing purposes.
	 *
	 * No update occurs if the current texture is already the front view texture.
	 */
	public void useFrontTexture(){
		if (texture != frontTexture){
			setTexture(frontTexture);
		}
	}

	/**
	 * sets the texture to be side view for drawing purposes.
	 *
	 * No update occurs if the current texture is already the side view texture
	 */
	public void useSideTexture(){
		if (texture != sideTexture){
			setTexture(sideTexture);
		}
	}

	/**
	 * Creates a new player avatar with the given physics data
	 *
	 * The size is expressed in physics units NOT pixels.  In order for
	 * drawing to work properly, you MUST set the drawScale. The drawScale
	 * converts the physics units to pixels.
	 *
	 * @param data  	The physics constants for this player
	 * @param width		The object width in physics units
	 * @param height	The object width in physics units
	 */
	public PlayerModel(JsonValue data, Vector2 pos, float width, float height, int maxHp, int drawDepth) {
		// The shrink factors fit the image to a tigher hitbox
		super(	pos.x,
				pos.y,
				width*data.get("shrink").getFloat( 0 ),
				height*data.get("shrink").getFloat( 1 ));

		float density = FINAL_MASS / (width *data.get("shrink").getFloat( 0 )
				* height * data.get("shrink").getFloat( 1 ));

		setDensity(density);
		setFriction(data.getFloat("friction", 0));  /// HE WILL STICK TO WALLS IF YOU FORGET
		setFixedRotation(true);

		maxspeed_x_ground = data.getFloat("maxspeed_x_ground", 0);
		maxspeed_x_air_wind = data.getFloat("maxspeed_x_air_wind", 0);
		maxspeed_x_air_drag = data.getFloat("maxspeed_x_air_drag", 0);
		maxspeed_up = data.getFloat("maxspeed_up", 0);
		maxspeed_down_open = data.getFloat("maxspeed_down_open", 0);
		maxspeed_down_closed = data.getFloat("maxspeed_down_closed", 0);
		damping = data.getFloat("damping", 0);
		force = data.getFloat("force", 0);
		size = data.get("size").asFloatArray();
		sensorName = "PlayerGroundSensor";
		lighterForce = data.getFloat("lighter_force");
		maxLighterFuel = data.getFloat("lighter_fuel");
		lighterFuel = maxLighterFuel;
		lighterChangeRate = data.getFloat("lighter_change_rate");
		this.data = data;

		// Gameplay attributes
		isGrounded = false;
		isJumping = false;
		faceRight = true;
		setMaxHealth(maxHp);
		setHealth(getMaxHealth());
		jumpCooldown = 0;
		setName("player");
		iFrames = 0;

		walkElapsedTime = 0f;
		depth = drawDepth;
	}

	/**
	 * Creates the physics Body(s) for this object, adding them to the world.
	 *
	 * This method overrides the base method to keep your ship from spinning.
	 *
	 * @param world Box2D world to store body
	 *
	 * @return true if object allocation succeeded
	 */
	public boolean activatePhysics(World world) {
		// create the box from our superclass
		if (!super.activatePhysics(world)) {
			return false;
		}

		// Ground Sensor
		// -------------
		// We only allow the player to jump when he's on the ground.
		// Double jumping is not allowed.
		//
		// To determine whether or not the player is on the ground,
		// we create a thin sensor under his feet, which reports
		// collisions with the world but has no collision response.
		Vector2 sensorCenter = new Vector2(0, -getHeight() / 2);
		FixtureDef sensorDef = new FixtureDef();
		sensorDef.density = data.getFloat("density",0);
		sensorDef.isSensor = true;
		sensorShape = new PolygonShape();
		JsonValue sensorjv = data.get("sensor");
		sensorShape.setAsBox(sensorjv.getFloat("shrink",0)*getWidth()/2.0f,
				sensorjv.getFloat("height",0), sensorCenter, 0.0f);
		sensorDef.shape = sensorShape;

		// Ground sensor to represent our feet
		Fixture sensorFixture = body.createFixture( sensorDef );
		sensorFixture.setUserData(getSensorName());

		return true;
	}


	/**
	 * Applies force to the body of this player as given by movement.
	 *
	 * This method should be called after the movement attribute is set.
	 */
	public void applyWalkingForce() {
		if (!isActive()) {
			return;
		}

		// Don't want to be moving. Damp out player motion
		if (getMovement() == 0f) {
			forceCache.set(-getDamping()*getVX(),0);
			body.applyForce(forceCache,getPosition(),true);
		}

		if (Math.abs(getVX()) >= getMaxSpeedXGround()) {
			setVX(Math.signum(getVX())*getMaxSpeedXGround());
		}{
			forceCache.set(getMovement(),0);
			body.applyForce(forceCache,getPosition(),true);
		}
	}

	/**
	 * Applies wind force to the body of this player.
	 *
	 * Horizontal component of force is ignored if player has reached horizontal maximum speed.
	 *
	 */
	public void applyWindForce(float fx, float fy) {
		if (!isActive()) {
			return;
		}
		float x = 0;
		float y = 0;
		//determine x of wind force
		//if force in same direction as currently moving and at max horiz speed, clamp
		if (Math.signum(fx) == Math.signum(getVX()) && Math.abs(getVX()) >= getMaxSpeedXAirWind()) {
			setVX(Math.signum(getVX()) * getMaxSpeedXAirWind());
		} else {
			x = fx;
		}
		//determine y of wind force
		if (Math.abs(getVY()) >= getMaxSpeedUp()) {
			setVY(Math.signum(getVY()) * getMaxSpeedUp());
		} else {
			y = fy;
		}
		forceCache.set(x, y);

		//Damp out velcoity not in direction of wind velocity
		//wind dir
		temp.set(x, y);
		temp.nor();
		//vel
		temp2.set(getVX(), getY());
		temp2.nor();
		boolean applyX = (Math.signum(temp.x) == Math.signum(temp2.x) && Math.abs(temp2.x) > Math.abs(temp.x))
				|| Math.signum(temp.x) != Math.signum(temp2.x);
		boolean applyY = (Math.signum(temp.y) == Math.signum(temp2.y) && Math.abs(temp2.y) > Math.abs(temp.y))
				|| Math.signum(temp.y) != Math.signum(temp2.y);
		float dampscl = 85;
		if (applyX) {
			forceCache.add((temp.x - temp2.x) * dampscl, 0);
		}
		if (applyY){
			//forceCache.add(0, (temp.y - temp2.y) * dampscl);
		}
		body.applyForce(forceCache,getPosition(),true);
	}

	/**
	 * Applies drag force to the body of this player.
	 */
	public void applyDragForce(float fx) {
		if (!isActive()) {
			return;
		}
		if ((Math.signum(fx) == Math.signum(getVX()) && Math.abs(getVX()) < getMaxSpeedXAirDrag())
				|| Math.signum(fx) == -Math.signum(getVX()) || getVX() == 0){
			forceCache.set(fx, 0);
			float scl = Math.signum(fx) == -Math.signum(getVX()) ? Math.abs(getVX()) + 1 : .6f;
			forceCache.scl(scl);
			body.applyForce(forceCache, getPosition(), true);
		}
	}
  
  /**
     * Applies lighter force to the body of this player.
     */
    public boolean applyLighterForce(float umbAng) {
        if (lighterFuel == maxLighterFuel) {
            lighterFuel = 0;
            float umbrellaX = (float) Math.cos(umbAng);
            float umbrellaY = (float) Math.sin(umbAng);
            float yscl = 1.05f;

            //determine X
            if (Math.signum(umbrellaX) == Math.signum(getVX()) && Math.abs(getVX()) >= getMaxSpeedXAirWind()) {
                forceCache.x = getMaxSpeedXAirWind();
            } else {
                if (Math.signum(umbrellaX) == Math.signum(getVX()) && Math.abs(getVX()) > Math.abs(umbrellaX * lighterForce)) {
                    forceCache.x = getVX();
                } else forceCache.x = umbrellaX * lighterForce;
            }

            //determine Y
            if (Math.signum(umbrellaY) == Math.signum(getVY()) && getVY() >= getMaxSpeedUp()) {
				forceCache.y = getMaxSpeedUp();
			}else if (Math.signum(umbrellaY) == Math.signum(getVY()) && getVY() <= getMaxSpeedDownOpen()){
				forceCache.y = getMaxSpeedDownOpen();
			} else {
                if (Math.signum(umbrellaY) == Math.signum(getVY()) && Math.abs(getVY()) > Math.abs(umbrellaY * lighterForce)) {
                    forceCache.y = getVY();
                } else forceCache.y = umbrellaY * lighterForce * yscl;
            }
            body.setLinearVelocity(forceCache);
            return true;
        }
        return false;
    }

	public void dampAirHoriz(){
		if(Math.abs(getVX()) > getMaxSpeedXAirDrag()/1.5f){
			forceCache.set(-getDamping()*getVX(),0);
			body.applyForce(forceCache,getPosition(),true);
		}
	}

	public void refillLighter(){
		if(lighterFuel != maxLighterFuel){
			if(lighterFuel + lighterChangeRate > maxLighterFuel) lighterFuel = maxLighterFuel;
			else lighterFuel += lighterChangeRate;
		}
	}

	/**
	 * Updates the object's physics state (NOT GAME LOGIC).
	 *
	 * We use this method to reset cooldowns.
	 *
	 * @param dt	Number of seconds since last animation frame
	 */
	public void update(float dt) {
		// Apply cooldowns
		if(iFrames!=0) iFrames--;

		super.update(dt);
	}

	/**
	 * auxillary method to simplify draw method below.
	 */
	private void drawAux(GameCanvas canvas, Color tint){
		// mirror left or right (if player is facing left, this should be -1)
		float effect = faceRight ? -1.0f : 1.0f;
		TextureRegion t;
		if (isGrounded() && isMoving()) {
			// Reset other animation elapsed time
			fallElapsedTime = 0;
			idleElapsedTime = 0;
			lookElapsedTime = 0;

			// Walk animation
			walkElapsedTime += Gdx.graphics.getDeltaTime();
			t = walkAnimation.getKeyFrame(walkElapsedTime, true);
		} else if (isGrounded() && !isMoving()) {
			// Reset other animation elapsed time
			walkElapsedTime = 0f;
			fallElapsedTime = 0;

			if (isZooming() && getLinearVelocity().epsilonEquals(0, 0)) {
				idleElapsedTime = 0;
				//look animation
				lookElapsedTime += Gdx.graphics.getDeltaTime();
				t = lookAnimation.getKeyFrame(lookElapsedTime, true);
			} else {
				lookElapsedTime = 0;
				//idle animation
				idleElapsedTime += Gdx.graphics.getDeltaTime();
				t = idleAnimation.getKeyFrame(idleElapsedTime, true);
			}
		} else {
			// Reset other animation elapsed time
			walkElapsedTime = 0f;
			idleElapsedTime = 0;
			lookElapsedTime = 0;

			//falling animation
			fallElapsedTime += Gdx.graphics.getDeltaTime();
			t = fallAnimation.getKeyFrame(fallElapsedTime, true);
		}
		canvas.draw(t, tint, t.getRegionWidth()/2f, t.getRegionHeight()/2f,
				getX() * drawScale.x, getY() * drawScale.y, getAngle(),
				effect * size[0]/ t.getRegionWidth() * drawScale.x, size[1]/t.getRegionHeight() * drawScale.y);
	}
      
    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        if (iFrames > 0) {
            if (iFrameCountdown == 0) {
                iFrameCountdown = 7;
                drawIFrameTexture = !drawIFrameTexture;
            }
            if (drawIFrameTexture) {
                drawAux(canvas, Color.BLACK);
                iFrameCountdown--;
            } else {
                drawAux(canvas, Color.WHITE);
                iFrameCountdown--;
            }
        } else {
            drawAux(canvas, Color.WHITE);
        }
    }

    /**
     * Draws player HP information on screen and
     * TODO: possibly other status information
     *
     * @param canvas the game canvas
     */

    public void drawInfo(GameCanvas canvas) {
        // draw health info
        if (hpTexture == null) {
            return;
        }
        float height = hpTexture[health].getRegionHeight();
        float width = hpTexture[health].getRegionWidth();

        // TODO: HP Texture is manually scaled at the moment
        canvas.draw(hpTexture[health], Color.WHITE, width / 2f, height / 2f, drawScale.x,
                canvas.getHeight() - drawScale.y, 0, 0.3f, 0.3f);

        // TODO: Boost Texture is manually scaled at the moment
        int boost_capac = (int) (lighterFuel / maxLighterFuel * 9);
        canvas.draw(boostTexture[boost_capac], Color.WHITE, boostTexture[health].getRegionWidth() / 2f,
                boostTexture[health].getRegionHeight() / 2f, drawScale.x, canvas.getHeight() - drawScale.y * 2,
                0, 0.3f, 0.3f);

    }

    /**
     * Draws the outline of the physics body.
     * <p>
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        canvas.drawPhysics(sensorShape, Color.RED, getX(), getY(), getAngle(), drawScale.x, drawScale.y);
    }

    @Override
    public Vector2 getDimensions() {
        return temp.set(size[0], size[1]);
    }

    @Override
    public int getDepth() {
        return this.depth;
    }

    @Override
    public Vector2 getBoxCorner() {
        return temp.set(getX() - size[0] / 2f, getY() + size[1] / 2f);
    }
}