package jp.techacademy.naoto.ichihashi.jumpactiongame

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.Vector2

//texture：画像、srcX,srcY,srcWidth,srcHeight：TextureRegionの切り取り範囲設定
//Spriteを継承したのでGameObjectも同じ変数が必要
open class GameObject (texture: Texture, srcX: Int, srcY: Int, srcWidth: Int,srcHeight: Int): Sprite(texture,srcX,srcY,srcWidth,srcHeight){
    val velocity: Vector2

    init {
        //Vector2のインスタンス生成
        velocity = Vector2()
    }
}