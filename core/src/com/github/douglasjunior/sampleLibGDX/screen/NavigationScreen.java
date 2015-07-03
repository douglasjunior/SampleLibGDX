package com.github.douglasjunior.sampleLibGDX.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
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

    private ShapeRenderer debugShapes;
    private int startX;
    private int startY;
    private int endX;
    private int endY;

    private float lapsedTime = 0;

    private Array<Vector2> historyPoints = new Array<>();
    private volatile Array<float[]> historyToDraw = new Array<>();
    private Thread calculePointsToDraw;


    public NavigationScreen(MainApplication app) {
        super(app);
        this.world = new World(app);
    }

    @Override
    public void show() {
        debugShapes = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();
        playerImage = new TextureRegion(new Texture(Gdx.files.internal("Images/car.png")));
        camera = new OrthographicCamera();
        fontCamera = new OrthographicCamera();

        //region calcule path to draw
        calculePointsToDraw = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!calculePointsToDraw.isInterrupted() && calculePointsToDraw.isAlive()) {
                    if (historyPoints.size > 0) {
                        Array<float[]> history = new Array<>();
                        for (int i = 1; i < historyPoints.size; i++) {
                            Vector2 currentPoint = historyPoints.get(i);
                            Vector2 lastPoint = historyPoints.get(i - 1);
                            if (insideOfScreen(currentPoint) || insideOfScreen(lastPoint)) {
                                history.add(new float[]{currentPoint.x, currentPoint.y, lastPoint.x, lastPoint.y});
                            }
                        }
                        historyToDraw = history;
                    }
                    try {
                        Thread.sleep((long) (MAX_LAPSED_TIME * 1000));
                    } catch (InterruptedException e) {
                    }
                }
            }
        });
        calculePointsToDraw.start();
        //endregion
    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0f, .25f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

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
        world.update(delta);

        //region capture path positions
        lapsedTime += delta;
        if (lapsedTime >= MAX_LAPSED_TIME) {
            lapsedTime = 0;
            Vector2 point = world.getPlayer().getCenterPos().cpy().scl(PX_PER_M);
            if (historyPoints.size == 0 || !point.equals(historyPoints.get(historyPoints.size - 1))) {
                historyPoints.add(point);
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

        batch.setProjectionMatrix(camera.combined);
        float playerX = (world.getPlayer().getCenterPos().x * PX_PER_M) - ((world.getPlayer().getWidth() * PX_PER_M) / 2);
        float playerY = (world.getPlayer().getCenterPos().y * PX_PER_M) - ((world.getPlayer().getDepth() * PX_PER_M) / 2);
        float playerOrigX = (world.getPlayer().getWidth() * PX_PER_M) / 2;
        float playerOrigY = (world.getPlayer().getDepth() * PX_PER_M) / 2;
        float playerWidth = world.getPlayer().getWidth() * PX_PER_M;
        float playerHeight = world.getPlayer().getDepth() * PX_PER_M;
        batch.draw(playerImage, playerX, playerY, playerOrigX, playerOrigY, playerWidth, playerHeight, 1, 1, world.getPlayer().getRotation());

        //region debug info
        batch.setProjectionMatrix(fontCamera.combined);
        int position = 10;
        font.draw(batch, "Rotation: " + world.getPlayer().getRotation(), 10, (Gdx.graphics.getHeight() - (position += 15)));
        font.draw(batch, "PlayerCenter: " + world.getPlayer().getCenterPos().x + " " + world.getPlayer().getCenterPos().y, 10, (Gdx.graphics.getHeight() - (position += 15)));
        font.draw(batch, "StartXY: " + startX + " " + startY, 10, (Gdx.graphics.getHeight() - (position += 15)));
        font.draw(batch, "EndXY: " + endX + " " + endY, 10, (Gdx.graphics.getHeight() - (position += 15)));
        font.draw(batch, "LapsedTime: " + lapsedTime, 10, (Gdx.graphics.getHeight() - (position += 15)));
        font.draw(batch, "HistoryPoints: " + historyPoints.size, 10, (Gdx.graphics.getHeight() - (position += 15)));
        font.draw(batch, "InsideOfScreen: " + historyToDraw.size, 10, (Gdx.graphics.getHeight() - (position += 15)));
        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, (Gdx.graphics.getHeight() - (position += 15)));
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
        debugShapes.dispose();
        playerImage.getTexture().dispose();
        batch.dispose();
        font.dispose();
    }

    private int gridSize = 200;

    private void renderMap() {
        debugShapes.setProjectionMatrix(camera.combined);
        debugShapes.setColor(0.25f, 0.25f, 0.85f, 1.0f);
        debugShapes.begin(ShapeRenderer.ShapeType.Filled);

        //region draw the map
        float size = Math.max(camera.viewportWidth, camera.viewportHeight);

        startX = (int) ((world.getPlayer().getCenterPos().x * PX_PER_M - size / 2f) / gridSize) * gridSize - gridSize * 2;
        endX = (int) (startX + size + gridSize * 3);

        startY = (int) ((world.getPlayer().getCenterPos().y * PX_PER_M - size / 2f) / gridSize) * gridSize - gridSize * 2;
        endY = (int) (startY + size + gridSize * 3);

        for (int y = startY; y <= endY; y += gridSize) {
            debugShapes.rectLine(startX, y, endX, y, 2);
        }

        for (int x = startX; x <= endX; x += gridSize) {
            debugShapes.rectLine(x, startY, x, endY, 2);
        }
        //endregion

        //region draw the path
        if (historyPoints.size > 0) {
            debugShapes.setColor(0.85f, 0.85f, 0.25f, 0.1f);

            for (int i = 1; i < historyToDraw.size; i++) {
                float[] point = historyToDraw.get(i);
                debugShapes.rectLine(point[0], point[1], point[2], point[3], 100);
            }
        }
        //endregion

        debugShapes.end();
    }

    private boolean insideOfScreen(Vector2 point) {
        return point.x >= startX && point.x <= endX && point.y >= startY && point.y <= endY;
    }


}
