package io.github.seguiJordiSpaceInvaders;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    SpriteBatch spriteBatch;
    FitViewport viewport;
    Texture hiScoreTexture;
    Texture playerTexture;
    Texture shotTexture;
    Sprite playerSprite;
    Array<Sprite> shootSprites;

    Vector2 touchPos;
    float playerTargetX;
    float playerHealth;
    float playerMaxHealth;
    float moveSpeed;
    float shotSpeed;
    float shootCooldown;

    boolean isTimePaused;


    @Override
    public void create() {
        playerTexture = new Texture("player.png");
        shotTexture = new Texture("shot.png");
        spriteBatch = new SpriteBatch();

        viewport = new FitViewport(8, 10);
        //resize(480, 640);

        playerSprite = new Sprite(playerTexture); // Initialize the sprite based on the texture
        playerSprite.setSize(1, 1);
        playerSprite.setPosition((viewport.getWorldWidth() - playerSprite.getWidth()) / 2f, 0); // Centre = 3.5f, 4.5f

        shootSprites = new Array<>();
        touchPos = new Vector2();
        playerTargetX = playerSprite.getX();

        moveSpeed = 4f;
        shotSpeed = 6f;
        isTimePaused = false;
        shootCooldown = 0;

        playerMaxHealth = 3;
        playerHealth = playerMaxHealth;
    }

    @Override
    public void render() {
        input();
        logic();
        draw();
    }

    private void input(){
        float delta = Gdx.graphics.getDeltaTime();
        // Keyboard Movement
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            playerSprite.translateX(moveSpeed * delta); // pasito a la dreta
            playerTargetX = playerSprite.getX();
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            playerSprite.translateX(-moveSpeed * delta); // pasito a la esquerra
            playerTargetX = playerSprite.getX();
        }

        // Touch / Mouse Movement
        else if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos); // transforma a coordenades del món
            float halfWidth = playerSprite.getWidth() / 2;
            playerTargetX = touchPos.x - halfWidth;
        }
    }

    private void logic(){
        float delta = Gdx.graphics.getDeltaTime();
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float playerWidth = playerSprite.getWidth();
        float playerHeight = playerSprite.getHeight();

        playerSprite.setX(MathUtils.clamp(playerSprite.getX(), 0, worldWidth - playerWidth));

        shootCooldown+= delta;
        if (Gdx.input.isTouched()){
            if (Math.abs(playerTargetX - playerSprite.getX()) > 0.01f) {
                float direction = Math.signum(playerTargetX - playerSprite.getX()); // +1 o -1
                float moveAmount = shotSpeed * delta;

                // Si ens passem, parem exactament a la destinació
                if (Math.abs(playerTargetX - playerSprite.getX()) < moveAmount) {
                    playerSprite.setX(playerTargetX);
                } else {
                    playerSprite.setX(playerSprite.getX() + direction * moveAmount);
                }
            }
            if (shootCooldown > 0.4f){
                shootCooldown=0;
                Shoot();
            }
        }

        for (int i = shootSprites.size -1; i >= 0; i--){
            Sprite shotSprite = shootSprites.get(i);
            float shotWidth = shotSprite.getWidth();
            float shotHeight = shotSprite.getHeight();
            // shotRectangle.set(shotSprite.getX(),shotSprite.getY(), shotWidth, shotHeight);
            shotSprite.translateY(shotSpeed*delta);
            if (shotSprite.getY() > viewport.getWorldHeight()) shootSprites.removeIndex(i);
            // else if (shotRectangle.overlaps(enemyRectangle))
        }
    }

    // Spawnea tots els enemics i reseteja les vides / puntuació
    private void StartGame(){

    }

    private void Shoot(){
        float dropWidth = 0.5f;
        float dropHeight = 0.5f;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        Sprite shotSprite = new Sprite(shotTexture);
        shotSprite.setSize(dropWidth, dropHeight);
        shotSprite.setX(playerSprite.getX());
        shotSprite.setY(playerSprite.getY() + playerSprite.getHeight());
        shootSprites.add(shotSprite);
    }
    private void draw(){
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();// Aquí es comença a dibuixar les coses que apareixen per pantalla.

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        playerSprite.draw(spriteBatch);
        for (Sprite shotSprite : shootSprites) {
            shotSprite.draw(spriteBatch);
        }
        spriteBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
}
