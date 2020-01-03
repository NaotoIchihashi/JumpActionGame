package jp.techacademy.naoto.ichihashi.jumpactiongame

import com.badlogic.gdx.graphics.Texture

class Enemy(texture: Texture, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int): GameObject(texture,srcX,srcY,srcWidth,srcHeight) {
    companion object{
        //横幅、高さ
        val ENEMY_WIDTH = 2.0f
        val ENEMY_HEIGHT = 1.3f
    }

    init{
        setSize(ENEMY_WIDTH, ENEMY_HEIGHT)
    }
}