package com.github.douglasjunior.sampleLibGDX.util;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import static com.github.douglasjunior.sampleLibGDX.util.Constants.PX_PER_M;

/**
 * Created by douglas on 01/07/15.
 */
public final class BodyFactory {

    public static Body createBody(World world, float x, float y, float width, float height, boolean isStatic) {
        BodyDef bDef = new BodyDef();
        bDef.type = isStatic ? BodyDef.BodyType.StaticBody : BodyDef.BodyType.DynamicBody;
        bDef.position.set(x / PX_PER_M, y / PX_PER_M);
        bDef.fixedRotation = false;
        Body body = world.createBody(bDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(width / 2f / PX_PER_M, height / 2f / PX_PER_M);
        body.createFixture(shape, 1);
        shape.dispose();

        return body;
    }
}
