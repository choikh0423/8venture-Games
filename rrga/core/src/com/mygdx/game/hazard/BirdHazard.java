package com.mygdx.game.hazard;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.JsonValue;
import com.mygdx.game.GameCanvas;

import java.util.Arrays;
import java.util.Collections;

public class BirdHazard extends HazardModel{

    /** Attack speed of a bird */
    private final int ATTACK_SPEED = 15;

    /** Identifier to allow us to track the sensor in ContactListener */
    private final String sensorName;

    /** The shape of this bird's sensor */
    private CircleShape sensorShape;

    /** A list of points which represent this bird's flight path.
     * Invariant: length >=2 and length is even.
     */
    private float[] path;

    /** The index of the birds current targeted x-coordinate in path
     * Invariant: currentPath index is even or 0
     */
    private int currentPathIndex;

    /** Move speed of this bird*/
    private int moveSpeed;

    /** The coordinates this bird is currently moving to */
    private Vector2 move= new Vector2();

    /** If patrol is true, bird will go back and forth along its path.
     * If false, bird will disappear after reaching last point on path */
    private boolean patrol;

    /** Whether this bird sees its target.
     * If true, moves in a straight line towards initial sighting position.
     * If false, moves along its path.
     */
    public boolean seesTarget;

    /** Direction of the target */
    private Vector2 targetDir = new Vector2();

    /**
     * Returns the name of this bird's sensor
     *
     * @return the name of this bird's sensor
     */
    public String getSensorName() {
        return sensorName;
    }

    /** Sets the direction of the target using the targets x and y coordinates */
    public void setTargetDir(float tx, float ty) {
        float moveX = tx - getX();
        float moveY = ty - getY();
        move.set(moveX, moveY);
        move.nor();
        move.scl(ATTACK_SPEED);
        targetDir.set(move);
    }

    public BirdHazard(JsonValue data) {
        super(data);
        path = data.get("path").asFloatArray();
        moveSpeed = data.getInt("movespeed");
        //add patrol parameter to JSON?
        currentPathIndex = 0;
        sensorName = "birdSensor";
        seesTarget = false;
    }

    public boolean activatePhysics(World world) {
        // create the box from our superclass
        if (!super.activatePhysics(world)) {
            return false;
        }

        //create sensor
        Vector2 sensorCenter = new Vector2(0, 0);
        FixtureDef sensorDef = new FixtureDef();
        sensorDef.density = 0;
        sensorDef.isSensor = true;
        sensorShape = new CircleShape();
        sensorShape.setRadius(7);
        //change radius to variable?
        sensorDef.shape = sensorShape;
        Fixture sensorFixture = body.createFixture( sensorDef );
        sensorFixture.setUserData(getSensorName());
        return true;
    }

    public void move(){
        System.out.println(Arrays.toString(path));
        if(!seesTarget) {
            float pathX = path[currentPathIndex];
            float pathY = path[currentPathIndex + 1];
            float moveX = pathX - getX();
            float moveY = pathY - getY();
            //if at next point in path
            if (moveX < .001 && moveY < .001) {
                //if at end of path
                if(currentPathIndex == path.length - 2){
                    //if patrol
                    if(patrol){
                        for (int i = 0; i < path.length / 2; i+=2) {
                            float temp1 = path[i];
                            float temp2 = path[i+1];
                            path[i] = path[path.length - i - 1];
                            path[i+1] = path[path.length - i];
                            path[path.length - i - 1] = temp1;
                            path[path.length - i] = temp2;
                        }
                        currentPathIndex = 0;
                    }
                    //if not patrol
                    else{
                        markRemoved(true);
                    }
                }
                //if not at end of path
                else{
                    currentPathIndex += 2;
                }
            }
            //if not yet at next point in path
            else {
                move.set(moveX, moveY);
                move.nor();
                move.scl(moveSpeed);
                setX(getX() + (move.x / 100));
                setY(getY() + (move.y / 100));
            }
        }
        else{
            //move in direction of targetCoords until offscreen
            setX(getX() + (targetDir.x / 100));
            setY(getY() + (targetDir.y / 100));
        }
    }

    /**
     * Draws the outline of the physics body.
     *
     * This method can be helpful for understanding issues with collisions.
     *
     * @param canvas Drawing context
     */
    public void drawDebug(GameCanvas canvas) {
        super.drawDebug(canvas);
        canvas.drawPhysics(sensorShape, Color.RED,getX(),getY(),drawScale.x,drawScale.y);
    }


}
