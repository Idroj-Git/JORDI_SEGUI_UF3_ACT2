package io.github.seguiJordiSpaceInvaders;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    SpriteBatch spriteBatch;
    FitViewport viewport;

    Texture hiScoreTexture;
    Texture playerTexture;
    Texture shotTexture;
    Texture enemy1Texture;
    Texture enemyShotTexture;
    Texture heartTexture;
    Texture pauseTexture;
    Texture resumeTexture;

    Sprite playerSprite;
    Array<Sprite> shootSprites;
    Array<Sprite> enemySprites;
    Array<Sprite> enemyShotSprites;
    Array<Sprite> heartSprites;
    Sprite pauseSprite;
    Sprite resumeSprite;

    Rectangle playerRectangle;
    Rectangle shotRectangle;
    Rectangle enemyRectangle;
    Rectangle enemyShotRectangle;
    Rectangle pauseRectangle;
    Rectangle resumeRectangle;

    Vector2 touchPos;

    float playerTargetX;
    int playerHealth;
    int playerMaxHealth;
    float moveSpeed;
    float shotSpeed;
    float shootCooldown;
    float sfxVolume;
    float leftLimit;
    float rightLimit;
    float moveTimer;
    float enemyShootCooldown;
    int currentScore;
    int highScore;
    int enemyScoreAmount;

    boolean isGamePaused;
    boolean enemyHitLimit;
    boolean areEnemyMovingRight;
    boolean enemyDescension;

    Music gameMusic;
    Music loseMusic;
    Sound hurtSound;
    Sound laserSound;
    Sound explosionSound;

    BitmapFont bitmapFont;
    String highScoreText;
    String currentScoreText;

    boolean isGameOver = false;
    Sprite restartSprite;
    Rectangle restartRectangle;

    @Override
    public void create() {
        playerTexture = new Texture("player.png");
        shotTexture = new Texture("shot.png");
        spriteBatch = new SpriteBatch();
        enemy1Texture = new Texture("whiteEnemy.png");
        enemyShotTexture = new Texture("enemy_shot.png");
        heartTexture = new Texture("heart.png");
        pauseTexture = new Texture("pause.png");
        resumeTexture = new Texture("resume.png");

        viewport = new FitViewport(9, 16);
        //resize(480, 640);

        playerSprite = new Sprite(playerTexture); // Initialize the sprite based on the texture
        playerSprite.setSize(1, 1);
        playerSprite.setPosition((viewport.getWorldWidth() - playerSprite.getWidth()) / 2f, 1); // Centre = 3.5f, 4.5f

        shootSprites = new Array<>();
        enemySprites = new Array<>();
        enemyShotSprites = new Array<>();
        heartSprites = new Array<>();
        pauseSprite = new Sprite(pauseTexture);
        pauseSprite.setSize(1f, 1f);
        pauseSprite.setPosition(3.5f, 13.2f); // Més o menys sota la vida
        resumeSprite = new Sprite(resumeTexture);
        resumeSprite.setSize(1f,1f);
        resumeSprite.setPosition(viewport.getWorldWidth()/2 - 0.5f, viewport.getWorldHeight()/2 - 0.5f); // Més o menys el centre

        touchPos = new Vector2();
        playerTargetX = playerSprite.getX();

        playerRectangle = new Rectangle();
        shotRectangle = new Rectangle();
        enemyRectangle = new Rectangle();
        enemyShotRectangle = new Rectangle();
        pauseRectangle = new Rectangle(pauseSprite.getX(), pauseSprite.getY(), pauseSprite.getWidth(), pauseSprite.getHeight());
        resumeRectangle = new Rectangle(resumeSprite.getX(), resumeSprite.getY(), resumeSprite.getWidth(), resumeSprite.getHeight());

        moveSpeed = 4f;
        shotSpeed = 6f;
        isGamePaused = false;
        enemyHitLimit = false;
        areEnemyMovingRight = false;
        enemyDescension = false;
        leftLimit = 1f;
        rightLimit = 7f;
        shootCooldown = 0;
        currentScore = 0;
        highScore = 0;
        moveTimer = 0;
        enemyShootCooldown = 0;
        enemyScoreAmount = 500;

        playerMaxHealth = 3;
        playerHealth = playerMaxHealth;

        gameMusic = Gdx.audio.newMusic(Gdx.files.internal("Tetris.mp3"));
        gameMusic.setLooping(true);
        gameMusic.setVolume(0.3f);
        gameMusic.play();

        loseMusic = Gdx.audio.newMusic(Gdx.files.internal("loseMusic.mp3"));
        loseMusic.setLooping(true);
        loseMusic.setVolume(0.3f);

        laserSound = Gdx.audio.newSound(Gdx.files.internal("laserShoot.wav"));
        explosionSound = Gdx.audio.newSound(Gdx.files.internal("explosion.wav"));
        hurtSound = Gdx.audio.newSound(Gdx.files.internal("hitHurt.wav"));
        sfxVolume = 0.3f;

        // Me cagun la puta la que he hagut de liar per posar un puto text de merda...
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
            Gdx.files.internal("space_invaders.ttf")   // Ruta del teu fitxer TTF dins assets/fonts/
        );
        FreeTypeFontGenerator.FreeTypeFontParameter parameter =
            new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 36;       // mida en píxels que vols per a l'alçada de cada línia
        parameter.color = Color.WHITE;
        parameter.genMipMaps = true;

        bitmapFont = generator.generateFont(parameter);
        bitmapFont.getData().setScale(0.01f);
        bitmapFont.setUseIntegerPositions(false);
        generator.dispose();
        //bitmapFont = new BitmapFont();
        //bitmapFont.setColor(Color.WHITE); // Color del text.
        //bitmapFont.getData().setScale(0.01f);
        highScoreText = "HighScore\n\t" + highScore;
        currentScoreText = "CurrentScore\n\t" + currentScore;

        restartSprite = new Sprite(resumeTexture);
        restartSprite.setSize(1.5f, 1.5f);
        restartSprite.setPosition(viewport.getWorldWidth()/2 - 0.75f, viewport.getWorldHeight()/2 - 1.5f);
        restartRectangle = new Rectangle(
            restartSprite.getX(),
            restartSprite.getY(),
            restartSprite.getWidth(),
            restartSprite.getHeight()
        );

        StartGame();
    }

    @Override
    public void render() {
        input();
        if (!isGamePaused && !isGameOver)
            logic();
        draw();
    }

    private void input(){
        float delta = Gdx.graphics.getDeltaTime();
        if (Gdx.input.justTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(touchPos);

            if (isGameOver && restartRectangle.contains(touchPos)) {
                StartGame();
                isGameOver = false;
                loseMusic.stop();
                gameMusic.play();
                return;
            }

            if (!isGamePaused && pauseRectangle.contains(touchPos)) {
                isGamePaused = true;
                gameMusic.pause();
                return;
            }

            if (isGamePaused && resumeRectangle.contains(touchPos)) {
                isGamePaused = false;
                gameMusic.play();
                return;
            }
        }
        if (!isGamePaused){
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
    }

    private void logic(){
        float delta = Gdx.graphics.getDeltaTime();
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float playerWidth = playerSprite.getWidth();
        float playerHeight = playerSprite.getHeight();

        playerSprite.setX(MathUtils.clamp(playerSprite.getX(), 0, worldWidth - playerWidth));
        playerRectangle.set(playerSprite.getX(), playerSprite.getY(), playerWidth, playerHeight);

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

        // PIUM PIUM DISPARAU
        for (int i = shootSprites.size -1; i >= 0; i--){
            Sprite shotSprite = shootSprites.get(i);
            float shotWidth = shotSprite.getWidth();
            float shotHeight = shotSprite.getHeight();
            shotRectangle.set(shotSprite.getX(),shotSprite.getY(), shotWidth, shotHeight);

            shotSprite.translateY(shotSpeed*delta);
            if (shotSprite.getY() > viewport.getWorldHeight() - 4) {
                shootSprites.removeIndex(i);
                continue;
            }

            for (int z = enemySprites.size -1; z >= 0; z--){
                enemyRectangle.set(enemySprites.get(z).getX(), enemySprites.get(z).getY(), enemySprites.get(z).getWidth(), enemySprites.get(z).getHeight() -1f);
                if (shotRectangle.overlaps(enemyRectangle)){
                    // ENEMY HIT
                    shootSprites.removeIndex(i);
                    enemySprites.removeIndex(z);

                    currentScore += enemyScoreAmount;
                    currentScoreText = "CurrentScore\n\t" + currentScore;
                    if (currentScore > highScore){
                        highScore += enemyScoreAmount;
                        highScoreText = "HighScore\n" + highScore;
                    }
                    explosionSound.play(sfxVolume);
                    if (enemySprites.size <= 0){
                        SpawnEnemyWave();
                        //StartGame();
                        // SEGÜENT FASE
                    }
                    return; // Acaba de comprovar el for.
                }
            }
        }

        enemyHitLimit = false;
        moveTimer += delta;
        if (moveTimer > 0.8f){
            moveTimer = 0;
            // Moviment lateral uniforme dels enemics
            for (int i = enemySprites.size-1; i >= 0; i--){
                if (!enemyDescension){
                    if (areEnemyMovingRight){
                        enemySprites.get(i).setPosition(enemySprites.get(i).getX() + 0.5f, enemySprites.get(i).getY());
                    } else {
                        enemySprites.get(i).setPosition(enemySprites.get(i).getX() - 0.5f, enemySprites.get(i).getY());
                    }
                    if (enemySprites.get(i).getX() > rightLimit || enemySprites.get(i).getX() < leftLimit){
                        enemyHitLimit = true;
                    }
                }
                else{
                    enemySprites.get(i).setPosition(enemySprites.get(i).getX(), enemySprites.get(i).getY() - 1f);
                }
                enemyRectangle.setPosition(enemySprites.get(i).getX(), enemySprites.get(i).getY());
                if (enemyRectangle.overlaps(playerRectangle) || enemySprites.get(i).getY() <= 2){
                    hurtSound.play(sfxVolume);
                    playerHealth--;
                    UpdateHealth();
                    enemySprites.removeIndex(i);
                }
            }
            if (enemyHitLimit && !enemyDescension){
                areEnemyMovingRight = !areEnemyMovingRight;
                enemyDescension = true;
            } else if (enemyDescension){
                enemyDescension = false;
                if (areEnemyMovingRight) SpawnEnemyWave();
            }
        }

        // Dispar enemics
        enemyShootCooldown += delta;
        if (enemyShootCooldown >= 1)
            if (MathUtils.random() >= 0.2){
                EnemyShoot(MathUtils.random(0, enemySprites.size - 1));
                enemyShootCooldown = 0;
            }
        // Moviment del dispar
        for (int i = enemyShotSprites.size -1; i >= 0; i--){
            Sprite enemyShotSprite = enemyShotSprites.get(i);
            float shotWidth = enemyShotSprite.getWidth();
            float shotHeight = enemyShotSprite.getHeight();
            enemyShotRectangle.set(enemyShotSprite.getX(),enemyShotSprite.getY(), shotWidth, shotHeight);

            enemyShotSprite.translateY(-shotSpeed*delta);
            if (enemyShotSprite.getY() < 0.5) { // Passa per sota del player = desapareix.
                enemyShotSprites.removeIndex(i);
                continue;
            }
            if (enemyShotRectangle.overlaps(playerRectangle)){
                playerHealth--;
                UpdateHealth();
                enemyShotSprites.removeIndex(i);
                hurtSound.play(sfxVolume);
            }
        }
    }

    // Spawnea tots els enemics i reseteja les vides / puntuació
    private void StartGame(){
        playerHealth = playerMaxHealth;
        UpdateHealth();
        currentScore = 0;
        enemySprites.clear();
        shootSprites.clear();
        enemyShotSprites.clear();
        SpawnEnemyWave();
    }

    private void Shoot(){
        float dropWidth = 0.5f;
        float dropHeight = 0.5f;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        Sprite shotSprite = new Sprite(shotTexture);
        shotSprite.setSize(dropWidth, dropHeight);
        shotSprite.setX(playerSprite.getX() + 0.25f);
        shotSprite.setY(playerSprite.getY() + playerSprite.getHeight());
        shootSprites.add(shotSprite);

        laserSound.play(sfxVolume);
    }

    private void EnemyShoot(int enemyIndex){
        float shotWidth = 0.5f;
        float shotHeight = 0.5f;

        Sprite shotSprite = new Sprite(enemyShotTexture);
        shotSprite.setSize(shotWidth, shotHeight);
        shotSprite.setX(enemySprites.get(enemyIndex).getX() + 0.25f);
        shotSprite.setY(enemySprites.get(enemyIndex).getY());
        enemyShotSprites.add(shotSprite);

        laserSound.play(sfxVolume);
    }

    private void SpawnEnemyWave(Array<Vector2> positions){
        int enemyAmount = positions.size -1;

        for (Vector2 position : positions){
            SpawnEnemy(position);
        }
    }

    private void SpawnEnemyWave(){
        float enemyHeight = 12;
        SpawnEnemyWave(new Array<Vector2>(new Vector2[]{
            new Vector2(1.5f,enemyHeight),
            new Vector2(2.5f, enemyHeight),
            new Vector2(3.5f, enemyHeight),
            new Vector2(4.5f, enemyHeight),
            new Vector2(5.5f, enemyHeight),
            new Vector2(6.5f, enemyHeight)
        }));
    }

    private void SpawnEnemy(Vector2 position){
        float enemyWidth = 1f;
        float enemyHeight = 1f;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        Sprite enemySprite = new Sprite(enemy1Texture);
        enemySprite.setSize(enemyWidth, enemyHeight);
        enemySprite.setPosition(position.x, position.y);
        enemySprites.add(enemySprite);
    }


    private void UpdateHealth(){
        float healthHeight = 14.5f;
        heartSprites.clear();

        if (playerHealth <= 0 && !isGameOver) {
            isGameOver = true;
            gameMusic.stop();
            loseMusic.play();
        }

        if (playerHealth >= 1){
            Sprite heartSprite = new Sprite(heartTexture);
            heartSprite.setSize(0.5f, 0.5f);
            heartSprite.setPosition(3.2f, healthHeight);
            heartSprites.add(heartSprite);
        }

        if (playerHealth >= 2){
            Sprite heartSprite = new Sprite(heartTexture);
            heartSprite.setSize(0.5f, 0.5f);
            heartSprite.setPosition(3.7f, healthHeight);
            heartSprites.add(heartSprite);
        }

        if (playerHealth == 3){
            Sprite heartSprite = new Sprite(heartTexture);
            heartSprite.setSize(0.5f, 0.5f);
            heartSprite.setPosition(4.2f, healthHeight);
            heartSprites.add(heartSprite);
        }
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
        spriteBatch.setColor(Color.RED); // NO FUNCIONA PERQUE EL IMG ES NEGRE CANVIAR A BLANC!
        for (Sprite enemySprite : enemySprites){
            enemySprite.draw(spriteBatch);
        }
        spriteBatch.setColor(Color.WHITE);
        for (Sprite heartSprite : heartSprites){
            heartSprite.draw(spriteBatch);
        }
        for (Sprite enemyShotSprite : enemyShotSprites){
            enemyShotSprite.draw(spriteBatch);
        }

        if (!isGamePaused) pauseSprite.draw(spriteBatch);

        if (isGamePaused){
            // Overlay fosc
            spriteBatch.setColor(0, 0, 0, 0.7f);
            spriteBatch.draw(new Texture(Gdx.files.internal("white.png")), 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
            spriteBatch.setColor(Color.WHITE);

            // Botó Resume
            resumeSprite.draw(spriteBatch);

            // Text PAUSED
            bitmapFont.draw(spriteBatch, "PAUSED",
                viewport.getWorldWidth()/2f - .8f,
                viewport.getWorldHeight()/2f + 1f);
        }
        if (isGameOver) {
            spriteBatch.setColor(0, 0, 0, 0.9f);
            spriteBatch.draw(new Texture(Gdx.files.internal("white.png")), 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
            spriteBatch.setColor(Color.WHITE);

            bitmapFont.getData().setScale(0.02f); // Doblem la mida per al títol
            bitmapFont.draw(spriteBatch, "YOU LOST",
                viewport.getWorldWidth()/2 - 1.5f,
                viewport.getWorldHeight()/2 + 1f);
            bitmapFont.getData().setScale(0.01f);
            restartSprite.draw(spriteBatch);
        }
        bitmapFont.draw(spriteBatch, currentScoreText, 5f, 15f); // Dreta a dalt
        bitmapFont.draw(spriteBatch, highScoreText, 0.5f, 15f); // Esquerra a dalt
        spriteBatch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        viewport.apply(true);
    }

    @Override
    public void dispose() {
        playerTexture.dispose();
        shotTexture.dispose();
        enemy1Texture.dispose();
        laserSound.dispose();
        explosionSound.dispose();
        hurtSound.dispose();
        gameMusic.dispose();

        bitmapFont.dispose();
        spriteBatch.dispose();
        pauseTexture.dispose();
        resumeTexture.dispose();
    }
}
