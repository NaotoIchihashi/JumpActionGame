package jp.techacademy.naoto.ichihashi.jumpactiongame

import com.badlogic.gdx.graphics.Texture

class Star(texture: Texture, srcX: Int, srcY: Int, srcWidth: Int, srcHeight: Int): GameObject(texture,srcX,srcY,srcWidth,srcHeight) {

    companion object{
        val STAR_WIDTH = 0.8f
        val STAR_HEIGHT = 0.8f

        //状態
        val STAR_EXIST = 0
        val STAR_NONE = 1
    }
    var mState:Int = 0

    init{
        setSize(STAR_WIDTH, STAR_HEIGHT)
        mState = STAR_EXIST
    }

    fun get(){
        mState = STAR_NONE
        setAlpha(0f)
    }
}