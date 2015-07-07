package com.github.douglasjunior.sampleLibGDX.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.GeometryUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.github.douglasjunior.sampleLibGDX.MainApplication;
import com.github.douglasjunior.sampleLibGDX.model.World;

import static com.github.douglasjunior.sampleLibGDX.util.Constants.PX_PER_M;

/**
 * Created by douglas on 02/07/15.
 */
public class NavigationScreen extends AbstractScreen {

    private static final float MAX_LAPSED_TIME = 0.25f;

    private TextureRegion playerImage;
    private OrthographicCamera camera;
    private World world;
    public SpriteBatch batch;
    public BitmapFont font;
    public OrthographicCamera fontCamera;
    private ShapeRenderer shapeRenderer;

    private float lapsedTime = 0;

    private Thread calculePointsToDraw;

    private final Array<Vector2> historyPoints = new Array<>();
    private volatile Array<float[]> wayToDraw = new Array<>();

    private final float pathWidth = 5; // in meters

    private final int gridSize = 200;
    private final Vector2 screenStart = new Vector2();
    private final Vector2 screenEnd = new Vector2();
    private boolean pathFilled = true;
    private boolean capturePath = true;
    private float areaTotal = 0;
    private float distanceTotal = 0;

    public NavigationScreen(MainApplication app) {
        super(app);
        this.world = new World(app);
    }

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        playerImage = new TextureRegion(new Texture(Gdx.files.internal("Images/car.png")));
        camera = new OrthographicCamera();
        fontCamera = new OrthographicCamera();

        //region calcule path to draw
        calculePointsToDraw = new Thread(new Runnable() {

            @Override
            public void run() {
                Vector2 tmp = new Vector2();
                final Vector2 screenStart = new Vector2();
                final Vector2 screenEnd = new Vector2();
                final float halfWidth = pathWidth * 0.5f; // in meters

                while (!calculePointsToDraw.isInterrupted() && calculePointsToDraw.isAlive()) {

                    calculeScreenDimensions(screenStart, screenEnd, world.getPlayer().getCenterPos().cpy().scl(PX_PER_M));
                    Array<float[]> way = new Array<>();
                    float area = 0;
                    float distance = 0;

                    // Filters only the points that are covered within the screen.
                    for (int i = 1; i < historyPoints.size; i++) {
                        Vector2 lastPoint = historyPoints.get(i - 1);
                        Vector2 currentPoint = historyPoints.get(i);
                        if (lastPoint != null && currentPoint != null) {
                            distance += lastPoint.dst(currentPoint);

                            // Converts the points covered in coordinates that make the outline of the way.
                            Vector2 t = tmp.set(currentPoint.y - lastPoint.y, lastPoint.x - currentPoint.x).nor();

                            float tx = t.x * halfWidth;
                            float ty = t.y * halfWidth;

                            float[] pathStep = {(currentPoint.x + tx), (currentPoint.y + ty),
                                    (currentPoint.x - tx), (currentPoint.y - ty)};

                            if (way.size > 0 && way.get(way.size - 1) != null) {
                                float[] lastStep = way.get(way.size - 1);
                                area += GeometryUtils.triangleArea(pathStep[0], pathStep[1], pathStep[2], pathStep[3], lastStep[0], lastStep[1]);
                                area += GeometryUtils.triangleArea(lastStep[2], lastStep[3], pathStep[2], pathStep[3], lastStep[0], lastStep[1]);
                            }

                            if (insideOfScreen(currentPoint.cpy().scl(PX_PER_M), screenStart, screenEnd) || insideOfScreen(lastPoint.cpy().scl(PX_PER_M), screenStart, screenEnd)) {
                                way.add(pathStep);
                            }
                        } else {
                            way.add(null);
                        }
                    }
                    wayToDraw = way;
                    areaTotal = area;
                    distanceTotal = distance;
                    try {
                        Thread.sleep((long) (MAX_LAPSED_TIME * 1000));
                    } catch (InterruptedException e) {
                    }
                }
            }
        }

        );
        calculePointsToDraw.start();
        //endregion
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0f, .25f, 0f, 1f);
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_STENCIL_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glStencilMask(0xFF);
        Gdx.gl.glClearStencil(0x0);

        camera.position.set(world.getPlayer().getCenterPos().x * PX_PER_M, world.getPlayer().getCenterPos().y * PX_PER_M, 0);
        camera.rotate(-world.getPlayer().getRotation());
        camera.update();
        camera.rotate(world.getPlayer().getRotation());

        renderMap();
        renderPlayerArea(delta);
    }

    /**
     * Update objects
     *
     * @param delta
     */
    private void update(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
            pathFilled = !pathFilled;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            capturePath = !capturePath;
        }
        world.update(delta);

        //region capture path positions
        lapsedTime += delta;
        if (lapsedTime >= MAX_LAPSED_TIME) {
            lapsedTime = 0;
            if (capturePath) {
                Vector2 point = world.getPlayer().getCenterPos().cpy();
                if (historyPoints.size == 0 || !point.equals(historyPoints.get(historyPoints.size - 1))) {
                    historyPoints.add(point);
                }
            } else {
                // add a null value to cut the path
                if (historyPoints.size > 0 && historyPoints.get(historyPoints.size - 1) != null) {
                    historyPoints.add(null);
                }
            }
        }
        //endregion
    }

    /**
     * Render the player
     *
     * @param delta
     */
    private void renderPlayerArea(float delta) {
        batch.begin();

        //region vehicle
        batch.setProjectionMatrix(camera.combined);
        float playerX = (world.getPlayer().getCenterPos().x * PX_PER_M) - ((world.getPlayer().getWidth() * PX_PER_M) / 2);
        float playerY = (world.getPlayer().getCenterPos().y * PX_PER_M) - ((world.getPlayer().getDepth() * PX_PER_M) / 2);
        float playerOrigX = (world.getPlayer().getWidth() * PX_PER_M) / 2;
        float playerOrigY = (world.getPlayer().getDepth() * PX_PER_M) / 2;
        float playerWidth = world.getPlayer().getWidth() * PX_PER_M;
        float playerHeight = world.getPlayer().getDepth() * PX_PER_M;
        batch.draw(playerImage, playerX, playerY, playerOrigX, playerOrigY, playerWidth, playerHeight, 1, 1, world.getPlayer().getRotation());
        //endregion

        //region debug info
        batch.setProjectionMatrix(fontCamera.combined);
        int position = 10;
        font.draw(batch, "Rotation: " + world.getPlayer().getRotation(), 10, (Gdx.graphics.getHeight() - (position += 15)));
        font.draw(batch, "PlayerCenter: " + world.getPlayer().getCenterPos().cpy().scl(PX_PER_M), 10, (Gdx.graphics.getHeight() - (position += 15)));
        font.draw(batch, "StartXY: " + screenStart, 10, (Gdx.graphics.getHeight() - (position += 15)));
        font.draw(batch, "EndXY: " + screenEnd, 10, (Gdx.graphics.getHeight() - (position += 15)));
        font.draw(batch, "LapsedTime: " + lapsedTime, 10, (Gdx.graphics.getHeight() - (position += 15)));
        font.draw(batch, "HistoryPoints: " + historyPoints.size, 10, (Gdx.graphics.getHeight() - (position += 15)));
        font.draw(batch, "InsideOfScreen: " + wayToDraw.size, 10, (Gdx.graphics.getHeight() - (position += 15)));
        font.draw(batch, "Dist/Area: " + distanceTotal + " / " + areaTotal, 10, (Gdx.graphics.getHeight() - (position += 15)));
        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, (Gdx.graphics.getHeight() - (position += 15)));
        font.draw(batch, "WayFilled: " + pathFilled, 10, (Gdx.graphics.getHeight() - (position += 15)));
        //endregion

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
        camera.update();
        fontCamera.setToOrtho(false, width, height);
        fontCamera.position.set(width / 2f, height / 2f, 0);
        fontCamera.update();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        calculePointsToDraw.interrupt();
        shapeRenderer.dispose();
        playerImage.getTexture().dispose();
        batch.dispose();
        font.dispose();
    }

    private void renderMap() {
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(pathFilled ? ShapeRenderer.ShapeType.Filled : ShapeRenderer.ShapeType.Line);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        Gdx.gl.glEnable(GL20.GL_STENCIL_TEST);

        Array<float[]> wayToDraw = this.wayToDraw;

        //region draw the path
        Gdx.gl.glStencilFunc(Gdx.gl.GL_EQUAL, 0, 0xFF);
        Gdx.gl.glStencilOp(Gdx.gl.GL_REPLACE, Gdx.gl.GL_REPLACE, Gdx.gl.GL_REPLACE);
        shapeRenderer.setColor(new Color(1f, 1f, 0f, 1f)); // AMARELO
        renderPath(wayToDraw);
        shapeRenderer.flush();
        //endregion

        //region draw the path
        Gdx.gl.glStencilFunc(GL20.GL_EQUAL, 1, 0xFF);
        Gdx.gl.glStencilOp(Gdx.gl.GL_REPLACE, Gdx.gl.GL_REPLACE, Gdx.gl.GL_REPLACE);
        shapeRenderer.setColor(new Color(1f, 0f, 0f, 1f)); // VERMELHO
        renderPath(wayToDraw);
        shapeRenderer.flush();
        //endregion

        Gdx.gl.glDisable(GL20.GL_STENCIL_TEST);

        //region draw the map
        calculeScreenDimensions(screenStart, screenEnd, world.getPlayer().getCenterPos().cpy().scl(PX_PER_M));

        shapeRenderer.setColor(0.25f, 0.25f, 0.85f, 1.0f);

        for (int y = (int) screenStart.y; y <= screenEnd.y; y += gridSize) {
            shapeRenderer.rectLine(screenStart.x, y, screenEnd.x, y, 2);
        }

        for (int x = (int) screenStart.x; x <= screenEnd.x; x += gridSize) {
            shapeRenderer.rectLine(x, screenStart.y, x, screenEnd.y, 2);
        }
        //endregion

        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void renderPath(Array<float[]> wayToDraw) {
        if (wayToDraw.size > 1) {
            for (int i = 1; i < wayToDraw.size; i++) {
                float[] lastCoord = wayToDraw.get(i - 1);
                float[] currentCoord = wayToDraw.get(i);
                if (lastCoord != null && currentCoord != null) {
                    shapeRenderer.triangle( //
                            lastCoord[0] * PX_PER_M, lastCoord[1] * PX_PER_M, //
                            lastCoord[2] * PX_PER_M, lastCoord[3] * PX_PER_M, //
                            currentCoord[0] * PX_PER_M, currentCoord[1] * PX_PER_M //
                    );
                    shapeRenderer.triangle( //
                            currentCoord[2] * PX_PER_M, currentCoord[3] * PX_PER_M, //
                            lastCoord[2] * PX_PER_M, lastCoord[3] * PX_PER_M, //
                            currentCoord[0] * PX_PER_M, currentCoord[1] * PX_PER_M //
                    );
                }
            }
        }
    }

    private void calculeScreenDimensions(Vector2 screenStart, Vector2 screenEnd, Vector3 center) {
        calculeScreenDimensions(screenStart, screenEnd, center.x, center.y);
    }

    private void calculeScreenDimensions(Vector2 screenStart, Vector2 screenEnd, Vector2 center) {
        calculeScreenDimensions(screenStart, screenEnd, center.x, center.y);
    }

    private void calculeScreenDimensions(Vector2 screenStart, Vector2 screenEnd, float centerX, float centerY) {
        float size = Math.max(camera.viewportWidth, camera.viewportHeight);

        screenStart.x = (int) ((centerX - size / 2f) / gridSize) * gridSize - gridSize * 2;
        screenEnd.x = (int) (screenStart.x + size + gridSize * 3);

        screenStart.y = (int) ((centerY - size / 2f) / gridSize) * gridSize - gridSize * 2;
        screenEnd.y = (int) (screenStart.y + size + gridSize * 3);
    }

    private boolean insideOfScreen(Vector2 point, Vector2 screenStart, Vector2 screenEnd) {
        return insideOfScreen(point.x, point.y, screenStart.x, screenStart.y, screenEnd.x, screenEnd.y);
    }

    private boolean insideOfScreen(Vector3 point, Vector2 screenStart, Vector2 screenEnd) {
        return insideOfScreen(point.x, point.y, screenStart.x, screenStart.y, screenEnd.x, screenEnd.y);
    }

    private boolean insideOfScreen(float x, float y, float startX, float startY, float endX, float endY) {
        return x >= startX && x <= endX && y >= startY && y <= endY;
    }

}
