package jp.techacademy.naoto.ichihashi.jumpactiongame

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Game
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import sun.rmi.runtime.Log

//別Package(com.badlogic.gdx)のGameクラスにアクセスするためにimport宣言が必要(Alt+Enter)でOK
class JumpActionGame(val mRequestHandler: ActivityRequestHandler) : Game() {
    lateinit var batch: SpriteBatch

    override fun create() {
        //SpriteBatchクラスのインスタンス生成
        batch = SpriteBatch()
        //GameクラスのsetScreenメソッドにGameScreenクラスを引数として与えて実行
        this.setScreen(GameScreen(this))
    }
}

