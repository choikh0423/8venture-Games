package com.mygdx.game.model.hazard;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;

public class BirdHazard extends HazardModel {

    /**
     * Attack speed of this bird
     */
    private final int attackSpeed;

    /**
     * Radius of a bird's sensor
     */
    private final int sensorRadius;

    /**
     * Identifier to allow us to track the sensor in ContactListener
     */
    private final String sensorName;

    /**
     * The shape of this bird's sensor
     */
    private CircleShape sensorShape;

    /**
     * A list of points which represent this bird's flight path.
     * Invariant: length >=2 and length is even.
     */
    private float[] path;

    /**
     * The index of the birds current targeted x-coordinate in path
     * Invariant: currentPath index is even or 0
     */
    private int currentPathIndex;

    /**
     * If loop is true, bird will go from last point in path to first.
     * If loop is false, bird will turn around after last point and reverse its path
     */
    private boolean loop;

    /**
     * The color of this bird. Determines the bird's behavior.
     * Red: Stationary, then attacks.
     * Blue: Patrols, doesn't attacks.
     * Brown: Patrols, then attacks.
     * Invariant: Must be one of "red", "blue", or "brown"
     */
    private String color;

    /**
     * Move speed of this bird
     */
    private int moveSpeed;

    /**
     * The coordinates this bird is currently moving to
     */
    private Vector2 move = new Vector2();

    /**
     * Which direction is the bird facing
     */
    private boolean faceRight;

    /**
     * Whether this bird sees its target.
     * If true, moves in a straight line towards initial sighting position.
     * If false, moves along its path.
     */
    public boolean seesTarget;

    /**
     * Direction of the target
     */
    private Vector2 targetDir = new Vector2();

    /**
     * Direction of the birds movement
     */
    private Vector2 moveDir = new Vector2();

    /**
     * Returns the name of this bird's sensor
     *
     * @return the name of this bird's sensor
     */
    public String getSensorName() {
        return sensorName;
    }

    /**
     * Sets the direction of the target using the targets x and y coordinates
     */
    public void setTargetDir(float tx, float ty, float tvx, float tvy) {
        //Right now using euler's method to determine target direction
        //In the future might want to switch to tracking player's location up to a certain point
        //and incrementally adjusting direction.
        float timestep = (float) sensorRadius / attackSpeed;
        float moveX = tx - getX() + (tvx * timestep);
        float moveY = ty - getY() + (tvy * timestep);
        move.set(moveX, moveY);
        move.nor();
        move.scl(attackSpeed);
        targetDir.set(move);
    }

    public BirdHazard(float[] shape, JsonValue birdData, int birdDamage, int birdSensorRadius, int birdAttackSpeed, float birdKnockback) {
        super(shape, birdData.get("path").asFloatArray()[0], birdData.get("path").asFloatArray()[1],
                birdDamage, birdKnockback);
        path = birdData.get("path").asFloatArray();
        moveSpeed = birdData.getInt("movespeed");
        loop = birdData.getBoolean("loop");
        color = birdData.getString("color");
        attackSpeed = birdAttackSpeed;
        sensorRadius = birdSensorRadius;
        currentPathIndex = 0;
        sensorName = "birdSensor";
        seesTarget = false;
        faceRight = true;
        fixture.isSensor = true;
    }

    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }

        //create sensor if attacker
        if(color.equals("red") || color.equals("brown")) {
            FixtureDef sensorDef = new FixtureDef();
            sensorDef.density = 0;
            sensorDef.isSensor = true;
            sensorShape = new CircleShape();
            sensorShape.setRadius(sensorRadius);
            sensorDef.shape = sensorShape;
            Fixture sensorFixture = body.createFixture(sensorDef);
            sensorFixture.setUserData(getSensorName());
        }
        return true;
    }

    public void move() {
        //if target not seen
        if (!seesTarget) {
            if(color.equals("blue") || color.equals("brown")) {
                float pathX = path[currentPathIndex];
                float pathY = path[currentPathIndex + 1];
                float moveX = pathX - getX();
                float moveY = pathY - getY();
                //if bird's path is > 1 point
                if (path.length > 2) {
                    //if at next point in path
                    if (Math.abs(moveX) < .001 && Math.abs(moveY) < .001) {
                        //if at end of path
                        if (currentPathIndex == path.length - 2) {
                            if (!loop) {
                                for (int i = 0; i < path.length / 2; i += 2) {
                                    float temp1 = path[i];
                                    float temp2 = path[i + 1];
                                    path[i] = path[path.length - i - 2];
                                    path[i + 1] = path[path.length - i - 1];
                                    path[path.length - i - 2] = temp1;
                                    path[path.length - i - 1] = temp2;
                                }
                            }
                            currentPathIndex = 0;
                        }
                        //else not at end of path
                        else {
                            currentPathIndex += 2;
                        }
                    }
                    //else not yet at next point in path
                    else {
                        move.set(moveX, moveY);
                        move.nor();
                        move.scl(moveSpeed);
                        if (Math.abs((move.x / 100)) > Math.abs(moveX)) setX(pathX);
                        else setX(getX() + (move.x / 100));
                        if (Math.abs((move.y / 100)) > Math.abs(moveY)) setY(pathY);
                        else setY(getY() + (move.y / 100));
                        if (move.x > 0) faceRight = true;
                        else faceRight = false;
                    }
                }
                //else path is 1 point
                //no movement
                moveDir.set(moveX, moveY);
            }
        }
        //else target is seen
        else {
            //move in direction of targetCoords until offscreen
            setX(getX() + (targetDir.x / 100));
            setY(getY() + (targetDir.y / 100));
            moveDir.set(targetDir);
            // targetDir is the direction of target relative to bird's location
            if (targetDir.x > 0) faceRight = true;
            else faceRight = false;
            //Need some way to delete when offscreen, should be handled by gamecontroller
        }
    }

    @Override
    public Vector2 getKnockbackForce() {
        return new Vector2(moveDir.x, moveDir.y).nor();
    }

    /**
     * Draws the physics object.
     *
     * @param canvas Drawing context
     */
    public void draw(GameCanvas canvas) {
        // TODO: birds should also be mirrored when facing opposite directions
        float effect = faceRight ? -1.0f : 1.0f;
        canvas.draw(texture, Color.WHITE, origin.x, origin.y,
                getX() * drawScale.x, getY() * drawScale.y, getAngle(),
                effect * .15f, .15f);
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
        if(color.equals("red") || color.equals("brown")) {
            canvas.drawPhysics(sensorShape, Color.RED, getX(), getY(), drawScale.x, drawScale.y);
        }
    }
}
