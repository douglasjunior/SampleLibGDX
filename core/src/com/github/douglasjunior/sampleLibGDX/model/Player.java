package com.github.douglasjunior.sampleLibGDX.model;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by douglas on 02/07/15.
 */
public class Player {

    private final World world;
    private float width = 2f;
    private float depth = 4.6f;
    private float rotation = 0.0f;
    private float rotationModifier;

    private float speed = 3.0f;
    private Vector2 velocity = new Vector2(0.0f, 0.0f);
    private Vector2 centerPos = new Vector2(0.0f, 0.0f);

    private Vector2 hitboxFrontRight = new Vector2(0.0f, 0.0f);
    private Vector2 hitboxBackRight = new Vector2(0.0f, 0.0f);
    private Vector2 hitboxBackLeft = new Vector2(0.0f, 0.0f);
    private Vector2 hitboxFrontLeft = new Vector2(0.0f, 0.0f);

    public Player(World world) {
        this.world = world;
    }

    public void moveForward(float delta) {
        velocity.set(-(float) (Math.sin(Math.toRadians(getRotation())) * speed), (float) (Math.cos(Math.toRadians(getRotation())) * speed));
        rotationModifier = 0;
        tryMove(delta);
    }

    public void moveBackward(float delta) {
        velocity.set((float) (Math.sin(Math.toRadians(getRotation())) * speed), -(float) (Math.cos(Math.toRadians(getRotation())) * speed));
        rotationModifier = 0;
        tryMove(delta);
    }

    public void strafeLeft(float delta) {
        velocity.set(-(float) (Math.cos(Math.toRadians(getRotation())) * speed), -(float) (Math.sin(Math.toRadians(getRotation())) * speed));
        rotationModifier = 0;
        tryMove(delta);
    }

    public void strafeRight(float delta) {
        velocity.set((float) (Math.cos(Math.toRadians(getRotation())) * speed), (float) (Math.sin(Math.toRadians(getRotation())) * speed));
        rotationModifier = 0;
        tryMove(delta);
    }

    public void turnLeft(float delta) {
        velocity.set(0.0f, 0.0f);
        rotationModifier = speed * 50 * delta;
        tryMove(delta);
    }

    public void turnRight(float delta) {
        velocity.set(0.0f, 0.0f);
        rotationModifier = speed * -50 * delta;
        tryMove(delta);
    }

    private void tryMove(float delta) {
        centerPos.add(velocity.x * delta, velocity.y * delta);
        setRotation(getRotation() + rotationModifier);
        updateBounds();
    }

    private void updateBounds() {
        float cosTheta = (float) (Math.cos(Math.toRadians(getRotation())));
        float sinTheta = (float) (Math.sin(Math.toRadians(getRotation())));
        hitboxFrontRight.x = (float) (centerPos.x + (width * 0.3 * cosTheta) - (depth * 0.3 * sinTheta));
        hitboxFrontRight.y = (float) (centerPos.y + (width * 0.3 * sinTheta) + (depth * 0.3 * cosTheta));
        hitboxBackRight.x = (float) (centerPos.x + (width * 0.3 * cosTheta) - (-depth * 0.3 * sinTheta));
        hitboxBackRight.y = (float) (centerPos.y + (width * 0.3 * sinTheta) + (-depth * 0.3 * cosTheta));
        hitboxBackLeft.x = (float) (centerPos.x + (-width * 0.3 * cosTheta) - (-depth * 0.3 * sinTheta));
        hitboxBackLeft.y = (float) (centerPos.y + (-width * 0.3 * sinTheta) + (-depth * 0.3 * cosTheta));
        hitboxFrontLeft.x = (float) (centerPos.x + (-width * 0.3 * cosTheta) - (depth * 0.3 * sinTheta));
        hitboxFrontLeft.y = (float) (centerPos.y + (-width * 0.3 * sinTheta) + (depth * 0.3 * cosTheta));
    }

    public World getWorld() {
        return world;
    }

    public Vector2 getCenterPos() {
        return centerPos;
    }

    public float getRotation() {
        return rotation;
    }

    public float getWidth() {
        return width;
    }

    public float getDepth() {
        return depth;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public Vector2 getHitboxFrontRight() {
        return hitboxFrontRight;
    }

    public Vector2 getHitboxBackRight() {
        return hitboxBackRight;
    }

    public Vector2 getHitboxBackLeft() {
        return hitboxBackLeft;
    }

    public Vector2 getHitboxFrontLeft() {
        return hitboxFrontLeft;
    }
}
