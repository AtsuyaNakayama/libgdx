package jp.techacademy.atsuya.nakayama.jumpactiongame

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite

class Enemy(type: Int, texture: Texture, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int)
    : GameObject(texture, srcX, srcY, srcWidth, srcHeight) {

    companion object {
        // 横幅、高さ
        val ENEMY_WIDTH = 0.5f
        val ENEMY_HIGHT = 0.5f

        // タイプ（通常と動くタイプ）
        val ENEMY_TYPE_STATIC = 0
        val ENEMY_TYPE_MOVING = 1

        // 速度
        val ENEMY_VELOCITY = 1.5f
    }

    var mType: Int

    init {
        setSize(ENEMY_WIDTH, ENEMY_HIGHT)
        mType = type
        if (mType == ENEMY_TYPE_MOVING) {
            velocity.x = ENEMY_VELOCITY
        }
    }

    // 座標を更新する
    fun update(deltaTime: Float) {
        x += velocity.x * deltaTime

        if (x < ENEMY_WIDTH / 2) {
            velocity.x = -velocity.x
            x = ENEMY_WIDTH / 2
        }
        if (x > GameScreen.WORLD_WIDTH - ENEMY_WIDTH / 2) {
            velocity.x = -velocity.x
            x = GameScreen.WORLD_WIDTH - ENEMY_WIDTH / 2
        }
    }
}