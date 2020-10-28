package jp.techacademy.atsuya.nakayama.jumpactiongame

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.FitViewport
import java.util.*

class MenuScreen(private val mGame: JumpActionGame) : ScreenAdapter() {
    companion object {
        internal val GUI_WIDTH = 320f
        internal val GUI_HEIGHT = 480f

        val MENU_POSITION_Y_TIME = 240f
        val MENU_POSITION_Y_SCORE = 304f
        val MENU_POSITION_X = 64f

        val GAME_TYPE_TIME = 1
        val GAME_TYPE_SCORE = 2
    }

    private val mBg: Sprite
    private val mGuiCamera: OrthographicCamera
    private val mGuiViewPort: FitViewport

    private var mFont: BitmapFont

    private var mTouchPoint: Vector3
    private var gameType: Int = 0

    init {
        // 背景の準備
        val bgTexture = Texture("back.png")
        // TextureRegionで切り出す時の原点は左上
        mBg = Sprite(TextureRegion(bgTexture, 0, 0, 540, 810))
        mBg.setSize(GUI_WIDTH, GUI_HEIGHT)
        mBg.setPosition(0f, 0f)

        // GUI用のカメラを設定する
        mGuiCamera = OrthographicCamera()
        mGuiCamera.setToOrtho(false, GUI_WIDTH, GUI_HEIGHT)
        mGuiViewPort = FitViewport(GUI_WIDTH, GUI_HEIGHT, mGuiCamera)

        mTouchPoint = Vector3()
        mFont = BitmapFont(Gdx.files.internal("font.fnt"), Gdx.files.internal("font.png"), false)
    }

    override fun render(delta: Float) {
        // 描画する
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // カメラの座標をアップデート（計算）し、スプライトの表示に反映させる
        mGuiCamera.update()
        mGame.batch.projectionMatrix = mGuiCamera.combined
        // テキスト表示
        mGame.batch.begin()
        mBg.draw(mGame.batch)
        mFont.draw(mGame.batch, "JAMP ACTION GAME", MENU_POSITION_X - 50, GUI_HEIGHT - 64)
        mFont.draw(mGame.batch, "TIMEATTACK", MENU_POSITION_X, GUI_HEIGHT - MENU_POSITION_Y_TIME)
        mFont.draw(mGame.batch, "SCOREATTACK", MENU_POSITION_X, GUI_HEIGHT - MENU_POSITION_Y_SCORE)
        mGame.batch.end()

        if (Gdx.input.justTouched()) {
            mGuiViewPort.unproject(mTouchPoint.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat(), 0f))
            val time = Rectangle(0f, GUI_HEIGHT - MENU_POSITION_Y_TIME - 16f, GUI_WIDTH, 16f)
            val score = Rectangle(0f, GUI_HEIGHT - MENU_POSITION_Y_SCORE - 16f, GUI_WIDTH, 16f)

            if (time.contains(mTouchPoint.x, mTouchPoint.y)) {
                gameType = GAME_TYPE_TIME
            } else if (score.contains(mTouchPoint.x, mTouchPoint.y)) {
                gameType = GAME_TYPE_SCORE
            }

            if (gameType > 0) {
                mGame.screen = GameScreen(mGame, gameType)
            }
        }
    }
    override fun resize(width: Int, height: Int) {
        mGuiViewPort.update(width, height)
    }


}