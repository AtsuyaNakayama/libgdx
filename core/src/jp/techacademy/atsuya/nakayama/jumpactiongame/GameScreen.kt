package jp.techacademy.atsuya.nakayama.jumpactiongame


import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import java.util.*

class GameScreen(private val mGame: JumpActionGame, private val gameType: Int) : ScreenAdapter() {
    companion object {
        val CAMERA_WIDTH = 10f
        val CAMERA_HEIGHT = 15f
        val WORLD_WIDTH = 10f
        val WORLD_HEIGHT = 15 * 10    // 20画面分登れば終了
        val GUI_WIDTH = 320f
        val GUI_HEIGHT = 480f

        val GAME_STATE_READY = 0
        val GAME_STATE_PLAYING = 1
        val GAME_STATE_GAMEOVER = 2

        val GAME_TYPE_TIME = 1
        val GAME_TYPE_SCORE = 2

        // 重力
        val GRAVITY = -12
    }

    private val mBg: Sprite
    private val mCamera: OrthographicCamera
    private val mGuiCamera: OrthographicCamera
    private val mViewPort: FitViewport
    private val mGuiViewPort: FitViewport


    private var mRandom: Random
    private var mSteps: ArrayList<Step>
    private var mStars: ArrayList<Star>
    private var mEnemy: ArrayList<Enemy>
    private lateinit var mUfo: Ufo
    private lateinit var mPlayer: Player

    private var mGameState: Int
    private var mHeightSoFar: Float = 0f
    private var mTouchPoint: Vector3
    private var mFont: BitmapFont

    private var mScore: Int
    private var mHighScore: Int

    private var mTime: Long
    private var mStartTime: Long
    private var mFirstTime: Long

    private var mNewScore: Int
    private var mPrefs: Preferences

    private val bgm: Sound
    private val hitSe: Sound
    private val goalSe: Sound
    private var id: Long

    init {
        // 背景の準備
        val bgTexture = Texture("back.png")
        // TextureRegionで切り出すときの原点は左上
        mBg = Sprite(TextureRegion(bgTexture, 0, 0, 540, 810))
        mBg.setSize(CAMERA_WIDTH, CAMERA_HEIGHT)
        mBg.setPosition(0f, 0f)

        // カメラ、ViewPortを生成、設定する
        mCamera = OrthographicCamera()
        mCamera.setToOrtho(false, CAMERA_WIDTH, CAMERA_HEIGHT)
        mViewPort = FitViewport(CAMERA_WIDTH, CAMERA_HEIGHT, mCamera)

        // GUI用のカメラを設定する
        mGuiCamera = OrthographicCamera()
        mGuiCamera.setToOrtho(false, GUI_WIDTH, GUI_HEIGHT)
        mGuiViewPort = FitViewport(GUI_WIDTH, GUI_HEIGHT, mGuiCamera)

        // プロパティの初期化
        mRandom = Random()
        mSteps = ArrayList<Step>()
        mStars = ArrayList<Star>()
        mEnemy = ArrayList<Enemy>()
        mGameState = GAME_STATE_READY
        mTouchPoint = Vector3()

        mFont = BitmapFont(Gdx.files.internal("font.fnt"), Gdx.files.internal("font.png"), false)
        mFont.data.setScale(0.8f)
        mScore = 0
        mHighScore = 0
        mStartTime = 0L
        mTime = 0L
        mFirstTime = 99999
        mNewScore = 0


        // ハイスコアをPreferencesから取得する

        mPrefs = Gdx.app.getPreferences("jp.techacademy.atsuya.nakayama.jumpactiongame")
        mHighScore = mPrefs.getInteger("HIGHSCORE1", 1)
        mFirstTime = mPrefs.getLong("FIRESTTIME1", 99999)

        bgm = Gdx.audio.newSound(Gdx.files.internal("sound/bgm.mp3"))
        hitSe = Gdx.audio.newSound(Gdx.files.internal("sound/enemy.mp3"))
        goalSe = Gdx.audio.newSound(Gdx.files.internal("sound/ufo.mp3"))
        id = 0L

        createStage()
    }

    override fun render(delta: Float) {
        // それぞれの状態をアップデートする
        update(delta)

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // カメラの中心を超えたらカメラを上に移動させる
        if (mPlayer.y > mCamera.position.y) {
            mCamera.position.y = mPlayer.y
        }

        // カメラの座標をアップデート（計算）し、スプライトの表示に反映させる
        mCamera.update()
        mGame.batch.projectionMatrix = mCamera.combined

        mGame.batch.begin()

        // 背景
        // 原点は左下
        mBg.setPosition(mCamera.position.x - CAMERA_WIDTH / 2, mCamera.position.y - CAMERA_HEIGHT / 2)
        mBg.draw(mGame.batch)

        // Step
        for (i in 0 until mSteps.size) {
            mSteps[i].draw(mGame.batch)
        }

        // Star
        for (i in 0 until mStars.size) {
            mStars[i].draw(mGame.batch)
        }

        // Enemy
        for (i in 0 until mEnemy.size) {
            mEnemy[i].draw(mGame.batch)
        }

        // UFO
        mUfo.draw(mGame.batch)

        //Player
        mPlayer.draw(mGame.batch)

        mGame.batch.end()

        // スコア表示
        mGuiCamera.update()
        mGame.batch.projectionMatrix = mGuiCamera.combined
        mGame.batch.begin()
        if (gameType == GAME_TYPE_TIME) {
            mFont.draw(mGame.batch, "FirstClearTime: $mFirstTime", 16f, GUI_HEIGHT - 15)
            mFont.draw(mGame.batch, "Time: $mTime", 16f, GUI_HEIGHT - 35)
        } else if (gameType == GAME_TYPE_SCORE) {
            mFont.draw(mGame.batch, "HighScore: $mHighScore", 16f, GUI_HEIGHT - 15)
            mFont.draw(mGame.batch, "Score: $mScore", 16f, GUI_HEIGHT - 35)
        }
        mGame.batch.end()
    }

    override fun resize(width: Int, height: Int) {
        mViewPort.update(width, height)
        mGuiViewPort.update(width, height)
    }

    // ステージを作成する
    private fun createStage() {


        // テクスチャの準備
        val stepTexture = Texture("step.png")
        val starTexture = Texture("star.png")
        val enemyTexture = Texture("enemy.png")
        val playerTexture = Texture("uma.png")
        val ufoTexture = Texture("ufo.png")

        // StepとStar,Enemyをゴールの高さまで配置しておく
        var y = 0f

        val maxJumpHeight = Player.PLAYER_JUMP_VELOCITY * Player.PLAYER_JUMP_VELOCITY / (2 * -GRAVITY)
        while (y < WORLD_HEIGHT - 5) {
            val type = if (mRandom.nextFloat() > 0.8f) Step.STEP_TYPE_MOVING else Step.STEP_TYPE_STATIC
            val x = mRandom.nextFloat() * (WORLD_WIDTH - Step.STEP_WIDTH)

            val step = Step(type, stepTexture, 0, 0, 144, 36)
            step.setPosition(x, y)
            mSteps.add(step)

            if (mRandom.nextFloat() > 0.5f) {
                val star = Star(starTexture, 0, 0, 72, 72)
                star.setPosition(step.x + mRandom.nextFloat(), step.y + Star.STAR_HEIGHT + mRandom.nextFloat() * 3)
                mStars.add(star)
            }

            if (mRandom.nextFloat() > 0.8f) {
                val enemy = Enemy(type, enemyTexture, 0, 0, 72, 72)
                enemy.setPosition(step.x + mRandom.nextFloat(), step.y + Star.STAR_HEIGHT + mRandom.nextFloat() * 3)
                mEnemy.add(enemy)
            }

            y += (maxJumpHeight - 0.5f)
            y -= (mRandom.nextFloat() * (maxJumpHeight / 3))
        }

        // Playerを配置
        mPlayer = Player(playerTexture, 0, 0, 72, 72)
        mPlayer.setPosition(WORLD_WIDTH / 2 - mPlayer.width / 2, Step.STEP_HEIGHT)

        // ゴールのUFOを配置
        mUfo = Ufo(ufoTexture, 0, 0, 140, 74)
        mUfo.setPosition(WORLD_WIDTH / 2 - Ufo.UFO_WIDTH / 2, y)

    }

    // それぞれのオブジェクトの状態をアップデートする
    private fun update(delta: Float) {
        when (mGameState) {
            GAME_STATE_READY ->
                updateReady()
            GAME_STATE_PLAYING ->
                updatePlaying(delta)
            GAME_STATE_GAMEOVER ->
                updateGameOver()
        }
    }

    private fun updateReady() {
        if (Gdx.input.justTouched()) {
            mStartTime = System.currentTimeMillis()

            mGameState = GAME_STATE_PLAYING
        }
    }

    private fun updatePlaying(delta: Float) {
        var accel = 0f

        mTime = (System.currentTimeMillis() - mStartTime) / 1000

        if (Gdx.input.isTouched) {
            mGuiViewPort.unproject(mTouchPoint.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
            val left = Rectangle(0f, 0f, GUI_WIDTH / 2, GUI_HEIGHT)
            val right = Rectangle(GUI_WIDTH / 2, 0f, GUI_WIDTH / 2, GUI_HEIGHT)
            if (left.contains(mTouchPoint.x, mTouchPoint.y)) {
                accel = 5.0f
            }
            if (right.contains(mTouchPoint.x, mTouchPoint.y)) {
                accel = -5.0f
            }
        }

        // Step
        for (i in 0 until mSteps.size) {
            mSteps[i].update(delta)
        }

        // Player
        if (mPlayer.y <= 0.5f) {
            mPlayer.hitStep()
        }
        mPlayer.update(delta, accel)
        mHeightSoFar = Math.max(mPlayer.y, mHeightSoFar)

        // 当たり判定を行う
        checkCollision()

        // ゲームオーバーか判断する
        checkGameOver()
    }

    private fun updateGameOver() {
        bgm.dispose()
        hitSe.dispose()
        goalSe.dispose()
        if (Gdx.input.justTouched()) {
            if (gameType == GAME_TYPE_SCORE) {
                mGame.screen = ResultScreen(mGame, mScore, mNewScore, gameType)
            } else {
                mGame.screen = ResultScreen(mGame, mTime.toInt(), mNewScore, gameType)
            }
        }
    }

    private fun checkCollision() {
        // UFO(ゴールとの当たり判定)
        if (mPlayer.boundingRectangle.overlaps(mUfo.boundingRectangle)) {
            if (mTime < mStartTime && gameType == GAME_TYPE_TIME) {
                mStartTime = mTime
                mNewScore = 1
                // ハイスコアをPreferenceに保存する
                mPrefs.putLong("FIRESTTIME", mStartTime)
                mPrefs.flush()
            }

            goalSe.play()
            mGameState = GAME_STATE_GAMEOVER
            return
        }

        // Enemyとの当たり判定
        for (i in 0 until mEnemy.size) {
            val enemy = mEnemy[i]

            if (mPlayer.boundingRectangle.overlaps(enemy.boundingRectangle)) {
                hitSe.play()
                mGameState = GAME_STATE_GAMEOVER
                break
            }

        }

        if (mGameState == GAME_STATE_GAMEOVER) {
            return
        }

        // Starとの当たり判定
        for (i in 0 until mStars.size) {
            val star = mStars[i]

            if (star.mState == Star.STAR_NONE) {
                continue
            }

            if (mPlayer.boundingRectangle.overlaps(star.boundingRectangle)) {
                star.get()
                mScore++
                if (mScore > mHighScore && gameType == GAME_TYPE_SCORE) {
                    mHighScore = mScore
                    mNewScore = 1
                    // ハイスコアをPreferenceに保存する
                    mPrefs.putInteger("HIGHSCORE1", mHighScore)
                    mPrefs.flush()
                }
                break
            }

        }

        // Stepとの当たり判定
        // 上昇中はStepとの当たり判定を確認しない
        if (mPlayer.velocity.y > 0) {
            return
        }

        for (i in 0 until mSteps.size) {
            val step = mSteps[i]

            if (step.mState == Step.STEP_STATE_VANISH) {
                continue
            }

            if (mPlayer.y > step.y) {
                if (mPlayer.boundingRectangle.overlaps(step.boundingRectangle)) {
                    mPlayer.hitStep()
                    if (mRandom.nextFloat() > 1.0f) {
                        step.vanish()
                    }
                    break
                }
            }
        }
    }

    private fun checkGameOver() {
        if (mHeightSoFar - CAMERA_HEIGHT / 2 > mPlayer.y) {
            mGameState = GAME_STATE_GAMEOVER
        }
    }
}
