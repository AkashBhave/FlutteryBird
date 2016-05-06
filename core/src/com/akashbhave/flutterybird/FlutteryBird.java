package com.akashbhave.flutterybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Timer;

import java.util.Random;

public class FlutteryBird extends ApplicationAdapter {
    // SpriteBatch manages a sprite (image, background, player, etc.)
    SpriteBatch batch;
    Texture background;

    Texture playButton;
    Texture gameOver;
    Sound flapSound;
    Sound hitSound;
    Sound pointSound;

    /* TUBE VARIABLES */
    Texture topTube;
    Texture bottomTube;
    float tubeGap = 350f; // Distance between the two tubes
    float maxTubeOffset;
    Random randomGenerator;
    float tubeVelocity = 4f;
    int numberOfTubes = 4; // 4 needed to show there are infinite tubes
    float[] tubeX = new float[numberOfTubes]; // Position of tubes along the x-axis
    float[] tubeOffset = new float[numberOfTubes];
    float distanceBtwnTubes;
    Rectangle[] topTubeRectangles;
    Rectangle[] bottomTubeRectangles;
    int scoringTube = 0;

    /* BIRD VARIABLES */
    Texture[] birds;
    int flapState = 0;
    boolean flapUp = true; // If bird is flapping up or down
    float flapDelay = 0.1f;
    float birdY = 0; // How high the bird is
    float velocity = 0;
    float gravity = 0.8f;
    Circle birdCircle; // Overlap that will help with collision detection
    boolean firstTouched = true;

    Preferences userPrefs;
    int score = 0;
    int highScore;
    BitmapFont scoreFont; // The font used for score
    BitmapFont gameOverScoreFont;

    int gameState = 0;// Keeps track of the state of the game

    @Override
    public void create() {
        batch = new SpriteBatch();
        background = new Texture("bg.png");
        gameOver = new Texture("gameOver.png");

        playButton = new Texture("play.png");
        flapSound = Gdx.audio.newSound(Gdx.files.internal("flapSound.mp3"));
        hitSound = Gdx.audio.newSound(Gdx.files.internal("hitSound.mp3"));
        pointSound = Gdx.audio.newSound(Gdx.files.internal("pointSound.mp3"));

        topTube = new Texture("topTube.png");
        bottomTube = new Texture("bottomTube.png");
        maxTubeOffset = Gdx.graphics.getHeight() / 2 - tubeGap / 2 - 100;
        randomGenerator = new Random();
        distanceBtwnTubes = (float) (Gdx.graphics.getWidth() * 3 / 4);
        topTubeRectangles = new Rectangle[numberOfTubes];
        bottomTubeRectangles = new Rectangle[numberOfTubes];

        for (int i = 0; i < numberOfTubes; i++) {
            tubeOffset[i] = (randomGenerator.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - tubeGap - 200);
            tubeX[i] = ((float) Gdx.graphics.getWidth() / 2 - topTube.getWidth() / 2) + Gdx.graphics.getWidth() + i * distanceBtwnTubes;

            topTubeRectangles[i] = new Rectangle();
            bottomTubeRectangles[i] = new Rectangle();
        }

        birds = new Texture[3];
        birds[0] = new Texture("bird1.png");
        birds[1] = new Texture("bird2.png");
        birds[2] = new Texture("bird3.png");
        birdCircle = new Circle();

        // The font used for the score while playing
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("04B_19.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 100;
        parameter.characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789.!'()>?: ";
        scoreFont = generator.generateFont(parameter);
        scoreFont.setColor(Color.WHITE);
        // Font used to show score and high score in game over screen
        parameter.size = 70;
        gameOverScoreFont = generator.generateFont(parameter);
        gameOverScoreFont.setColor(251, 121, 88, 1);
        generator.dispose();

        // Retrieves the high score
        userPrefs = Gdx.app.getPreferences("userPrefs");
        highScore = userPrefs.getInteger("highScore");
        Gdx.graphics.setContinuousRendering(false);

        birdY = Gdx.graphics.getHeight() / 2 - birds[flapState].getHeight() / 2;
        startFlapping();

        // Senses if the screen is touched
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (gameState == 0) {
                    gameState = 1; // Starts the game
                    Gdx.graphics.setContinuousRendering(true);
                } else if (gameState == 1) {
                    Gdx.graphics.setContinuousRendering(true);
                }
                return super.touchDown(screenX, screenY, pointer, button);
            }
        });
    }


    @Override
    public void render() {
        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        if (gameState == 1) {

            if (tubeX[scoringTube] < Gdx.graphics.getWidth() / 2) {
                score++;
                pointSound.play(0.75f);
                if (scoringTube < numberOfTubes - 1) {
                    scoringTube++;
                } else {
                    scoringTube = 0;
                }
            }

            if (Gdx.input.justTouched()) {
                if(firstTouched) {
                    flapState = 0;
                    velocity = -25;
                    flapSound.play(1.0f);
                    firstTouched = false;
                } else {
                    if (birdY > topTubeRectangles[scoringTube].getY() + topTubeRectangles[scoringTube].getHeight()) {
                        Gdx.app.log("Bird", "Above Tube");
                        velocity = 0;
                    } else {
                        flapState = 0;
                        velocity = -25;
                    }
                    flapSound.play(1.0f);
                }
            } else {
                velocity++;
                flapState = 1;
            }

            // Adds the tubes
            for (int i = 0; i < numberOfTubes; i++) {
                // Loops the four tubes over again
                if (tubeX[i] < -topTube.getWidth()) {
                    tubeOffset[i] = (randomGenerator.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - tubeGap - 200);
                    tubeX[i] += numberOfTubes * distanceBtwnTubes;
                } else {
                    tubeX[i] -= tubeVelocity;
                }

                batch.draw(topTube,
                        tubeX[i],
                        Gdx.graphics.getHeight() / 2 + tubeGap / 2 + tubeOffset[i]);
                batch.draw(bottomTube,
                        tubeX[i],
                        Gdx.graphics.getHeight() / 2 - tubeGap / 2 - bottomTube.getHeight() + tubeOffset[i]);

                topTubeRectangles[i] = new Rectangle(tubeX[i],
                        Gdx.graphics.getHeight() / 2 + tubeGap / 2 + tubeOffset[i],
                        topTube.getWidth(),
                        topTube.getHeight());
                bottomTubeRectangles[i] = new Rectangle(tubeX[i],
                        Gdx.graphics.getHeight() / 2 - tubeGap / 2 - bottomTube.getHeight() + tubeOffset[i],
                        bottomTube.getWidth(),
                        bottomTube.getHeight());
            }

            // Starts the 'gravity' of the game
            if (birdY > 0 || velocity < 0) {
                velocity = velocity + gravity;
                birdY -= velocity;
            } else {
                gameState = 2;
                hitSound.play(1.0f);
            }

            birdCircle.set(Gdx.graphics.getWidth() / 2, birdY + birds[flapState].getHeight() / 2, birds[flapState].getHeight() / 2);
            for (int i = 0; i < numberOfTubes; i++) {
                // The actual detection
                if (Intersector.overlaps(birdCircle, topTubeRectangles[i]) || Intersector.overlaps(birdCircle, bottomTubeRectangles[i])) {
                    hitSound.play(1.0f);
                    gameState = 2;
                }
            }
        } else if (gameState == 2) {
            // Sees if there is a new high score
            if (score > highScore) {
                userPrefs.putInteger("highScore", score);
                highScore = score;
                userPrefs.flush();
            } else {

            }

            batch.draw(gameOver, Gdx.graphics.getWidth() / 2 - gameOver.getWidth() / 2, Gdx.graphics.getHeight() / 2 - gameOver.getHeight() / 2);
            batch.draw(playButton, Gdx.graphics.getWidth() / 2 - playButton.getWidth() / 2 + 100, Gdx.graphics.getHeight() / 2 - playButton.getHeight() / 2 - 25);

            // Checks to see if score is a double digit, makes UI adjustments
            int scoreXPos;
            int highScoreXPos;
            if(score < 9) {
                scoreXPos = 166;
                if(highScore < 9) {
                    highScoreXPos = 166;
                } else {
                    highScoreXPos = 178;
                }
            } else {
                scoreXPos = 178;
                if(highScore < 9) {
                    highScoreXPos = 166;
                } else {
                    highScoreXPos = 178;
                }
            }
            gameOverScoreFont.draw(batch, String.valueOf(score), Gdx.graphics.getWidth() / 2 - scoreXPos, Gdx.graphics.getHeight() / 2 - 2);
            gameOverScoreFont.draw(batch, String.valueOf(highScore), Gdx.graphics.getWidth() / 2 - highScoreXPos, Gdx.graphics.getHeight() / 2 - 127);


            if (Gdx.input.justTouched()) {
                Vector2 region = new Vector2(Gdx.input.getX(), Gdx.input.getY());
                Rectangle textureBounds = new Rectangle(Gdx.graphics.getWidth() / 2 - playButton.getWidth() / 2,
                        Gdx.graphics.getHeight() / 2 - playButton.getHeight() / 2 - 50,
                        playButton.getWidth(),
                        playButton.getHeight());
                if (textureBounds.contains(region.x, region.y)) {
                    Gdx.app.log("Restart", "Tapped");
                    gameState = 0;
                    firstTouched = true;
                    birdY = Gdx.graphics.getHeight() / 2 - birds[flapState].getHeight() / 2;
                    for (int i = 0; i < numberOfTubes; i++) {
                        tubeOffset[i] = (randomGenerator.nextFloat() - 0.5f) * (Gdx.graphics.getHeight() - tubeGap - 200);
                        tubeX[i] = ((float) Gdx.graphics.getWidth() / 2 - topTube.getWidth() / 2) + Gdx.graphics.getWidth() + i * distanceBtwnTubes;

                        topTubeRectangles[i] = new Rectangle();
                        bottomTubeRectangles[i] = new Rectangle();
                    }
                    score = 0;
                    scoringTube = 0;
                    velocity = 0;
                    startFlapping();
                }
            }
        }

        if (gameState == 1 || gameState == 0) {
            if (gameState == 1) {
                // Displays the score
                scoreFont.draw(batch, String.valueOf(score), 50, Gdx.graphics.getHeight() - 50);
            }
            batch.draw(birds[flapState],
                    (Gdx.graphics.getWidth() / 2) - birds[flapState].getWidth() / 2,
                    birdY);
        }

        batch.end();

    }


    public void startFlapping() {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                // Tells the render method that we are going to start displaying sprites
                batch.begin();
                batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight()); // Displays it on the screen
                // Controls the bird speed
                if (flapState == 0) {
                    flapState = 1;
                    flapUp = true;
                } else if (flapState == 1) {
                    if (flapUp) {
                        flapState = 2;
                        flapUp = true;
                    } else {
                        flapState = 0;
                        flapUp = false;
                    }
                } else if (flapState == 2) {
                    flapState = 1;
                    flapUp = false;
                }
                batch.draw(birds[flapState],
                        (Gdx.graphics.getWidth() / 2) - birds[flapState].getWidth() / 2,
                        birdY);
                batch.end();
                if (gameState == 0) {
                    startFlapping();
                } else {
                    return;
                }
            }
        }, flapDelay);

    }

}
