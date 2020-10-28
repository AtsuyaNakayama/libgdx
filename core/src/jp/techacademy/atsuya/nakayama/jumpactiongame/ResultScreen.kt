package jp.techacademy.atsuya.nakayama.jumpactiongame

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.FitViewport

class ResultScreen(private val mGame: JumpActionGame, private val mScore: Int, private val mNewScore: Int, private val gameType: Int) : ScreenAdapter() {
    companion object {
        internal val GUI_WIDTH = 320f
        internal val GUI_HEIGHT = 480f

        val GAME_TYPE_TIME = 1
        val GAME_TYPE_SCORE = 2
    }

    private var mBg: Sprite
    private var mGuiCamera: OrthographicCamera
    private var mGuiViewPort: FitViewport
    private var mFont: BitmapFont
    init {
        if (mGame.mRequestHandler != null) {
            mGame.mRequestHandler.showAds(true)
        }

        // 背景の準備
        val bgTexture = Texture("resultback.png")
        mBg = Sprite(TextureRegion(bgTexture, 0, 0, 540, 810))
        mBg.setSize(GUI_WIDTH, GUI_HEIGHT)
        mBg.setPosition(0f, 0f)

        // GUI用のカメラを設定する
        mGuiCamera = OrthographicCamera()
        mGuiCamera.setToOrtho(false, GUI_WIDTH, GUI_HEIGHT)
        mGuiViewPort = FitViewport(GUI_WIDTH, GUI_HEIGHT, mGuiCamera)

        // フォント
        mFont = BitmapFont(Gdx.files.internal("font.fnt"), Gdx.files.internal("font.png"), false)
    }

    override fun render(delta: Float) {
        // 描画する
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // カメラの座標をアップデート（計算）し、スプライトの表示に反映させる
        mGuiCamera.update()
        mGame.batch.projectionMatrix = mGuiCamera.combined

        mGame.batch.begin()
        mBg.draw(mGame.batch)
        if (gameType == GAME_TYPE_TIME) {
            mFont.draw(mGame.batch, "Tiem: $mScore second", 0f, GUI_HEIGHT / 2 + 40, GUI_WIDTH, Align.center, false)
        } else if (gameType == GAME_TYPE_SCORE) {
            mFont.draw(mGame.batch, "Score: $mScore", 0f, GUI_HEIGHT / 2 + 40, GUI_WIDTH, Align.center, false)
        }
        if (mNewScore == 1) {
            mFont.draw(mGame.batch, "NEW RECORD! $mScore", 0f, GUI_HEIGHT / 2 + 10, GUI_WIDTH, Align.center, false)
        }
        mFont.draw(mGame.batch, "back to menu?", 0f, GUI_HEIGHT / 2 - 40, GUI_WIDTH, Align.center, false)
        mGame.batch.end()

        if (Gdx.input.justTouched()) {
            if (mGame.mRequestHandler != null) {
                mGame.mRequestHandler.showAds(false)
            }

            mGame.screen = MenuScreen(mGame)

        }
    }
}