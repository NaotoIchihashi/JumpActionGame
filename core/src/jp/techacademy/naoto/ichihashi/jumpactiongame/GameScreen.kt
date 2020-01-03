package jp.techacademy.naoto.ichihashi.jumpactiongame

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.ScreenAdapter
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.FitViewport
import sun.rmi.runtime.Log
import java.util.*
import kotlin.collections.ArrayList

class GameScreen(private val mGame:JumpActionGame) :ScreenAdapter(){
    //カメラのサイズを指定する定数プロパティ
    companion object{
        val CAMERA_WIDTH = 10f
        val CAMERA_HEIGHT =15f
        val WORLD_WIDTH = 10f
        val WORLD_HEIGHT = 15 * 20 //20画面分登れば終了
        val GUI_WIDTH = 320f
        val GUI_HEIGHT = 480f

        val GAME_STATE_READY = 0
        val GAME_STATE_PLAYING = 1
        val GAME_STATE_GAMEOVER = 2

        val GRAVITY = -12
    }

    private val mBg: Sprite
    private val mCamera: OrthographicCamera
    private val mGuiCamera: OrthographicCamera
    private val mViewPort: FitViewport
    private val mGuiViewport: FitViewport

    private var mRandom: Random
    private var mSteps: ArrayList<Step> //複数配置するためArrayList
    private var mStars: ArrayList<Star> //複数配置するためArrayList
    private lateinit var mUfo: Ufo
    private lateinit var mPlayer: Player
    private var mEnemys:ArrayList<Enemy>

    private var mGameState: Int
    private var mHeightSoFar: Float = 0f
    private var mTouchPoint: Vector3
    private var mFont: BitmapFont
    private var mScore: Int
    private var mHighScore: Int
    private var mPref: Preferences
    //checkCollision()のEnemy接触判定時に読み込むと　W/SoundPool: sample 1 not READY　発生
    //読み込みが間に合っていないようなので、ゲーム開始に音データ取得
    val sound = Gdx.audio.newSound(Gdx.files.internal("bomb1.mp3"))


    //イニシャライザ（コンストラクター）
    init{
        //背景の準備
        val bgTexture = Texture("back.png")
        //TextureRegionで切り出す時の原点は左上
        //引数を与えてSpriteクラスのインスタンス生成
        mBg = Sprite(TextureRegion(bgTexture,0,0,540,810))
        //SpriteクラスのsetPositionメソッドで描画位置を指定
        mBg.setSize(CAMERA_WIDTH, CAMERA_HEIGHT)
        mBg.setPosition(0f,0f)

        //OrthographicCameraとFitViewportのインスタンス生成
        //OrthographicCameraは更にsetToOrthoメソッド呼び出し
        //CameraとViewportのサイズを同一値に固定し、縦横比を固定する設定となる
        mCamera = OrthographicCamera()
        mCamera.setToOrtho(false, CAMERA_WIDTH, CAMERA_HEIGHT)
        mViewPort = FitViewport(CAMERA_WIDTH, CAMERA_HEIGHT,mCamera)

        //GUI用のCameraを設定する
        mGuiCamera = OrthographicCamera()
        mGuiCamera.setToOrtho(false, GUI_WIDTH, GUI_HEIGHT)
        mGuiViewport = FitViewport(GUI_WIDTH, GUI_HEIGHT,mGuiCamera)

        //プロパティの初期化
        mRandom = Random()
        mSteps = ArrayList<Step>()
        mStars = ArrayList<Star>()
        mGameState = GAME_STATE_READY
        mTouchPoint = Vector3()
        mEnemys = ArrayList<Enemy>()

        mFont = BitmapFont(Gdx.files.internal("font.fnt"),Gdx.files.internal("font.png"),false)
        mFont.data.setScale(0.8f)
        mScore = 0
        mHighScore = 0

        mPref = Gdx.app.getPreferences("jp.techacademy.naoto.ichihashi.jumpactiongame")
        mHighScore = mPref.getInteger("HIGHSCORE",0)

        createStage()
    }

    override fun render(delta: Float) {
        //それぞれの状態をアップデートする
        update(delta)

        Gdx.gl.glClearColor(0f,0f,0f,1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        //Playerのy座標がCameraの中心のy座標を超えたら
        //Cameraの中心座標をPlayerの座標に変更する
        if(mPlayer.y > mCamera.position.y){
            mCamera.position.y = mPlayer.y
        }

        //mCameraの座標を計算しSpriteの表示反映させる
        mCamera.update()
        mGame.batch.projectionMatrix = mCamera.combined

        //SpriteBatchクラスのbegin()、end()メソッドで囲み
        //Spriteクラスのdrawメソッドを呼び出すこと画面を描画
        mGame.batch.begin()

        //setPositionメソッドで背景画面の原点を左下に設定
        mBg.setPosition(mCamera.position.x - CAMERA_WIDTH / 2 , mCamera.position.y - CAMERA_HEIGHT / 2)
        mBg.draw(mGame.batch)

        //Stepクラスの描画。ArrayListで保持しているので繰り返し処理
        for (i in 0 until mSteps.size){
            mSteps[i].draw(mGame.batch)
        }

        //Starクラスの描画。ArrayListで保持しているので繰り返し処理
        for (i in 0 until mStars.size){
            mStars[i].draw(mGame.batch)
        }

        //UFOクラスの描画
        mUfo.draw(mGame.batch)

        //Playerクラスの描画
        mPlayer.draw(mGame.batch)

        //Enemyクラスの描画
        for(i in 0 until mEnemys.size) {
            mEnemys[i].draw(mGame.batch)
        }

        mGame.batch.end()

        mGuiCamera.update()
        mGame.batch.projectionMatrix = mGuiCamera.combined
        mGame.batch.begin()
        mFont.draw(mGame.batch,"HighScore: $mHighScore",16f, GUI_HEIGHT - 15)
        mFont.draw(mGame.batch,"Score: $mScore",16f, GUI_HEIGHT -35)
        mGame.batch.end()
    }

    override fun resize(width: Int, height: Int) {
        mViewPort.update(width,height)
        mGuiViewport.update(width,height)
    }

    private fun createStage(){
        //テクスチャの準備
        val stepTexture = Texture("step.png")
        val starTexture = Texture("star.png")
        val playerTexture = Texture("uma.png")
        val ufoTexture = Texture("ufo.png")
        //ひとまず敵はufoにする
        val enemyTexture = Texture("ufo.png")

        //StepクラスとStarクラスをゴールの高さまで配置していくための変数y
        var y = 0f

        val maxJumpHeight = Player.PLAYER_JUMP_VELOCITY * Player.PLAYER_JUMP_VELOCITY / ( 2 * - GRAVITY)
        //y座標の下から処理実行
        while (y < WORLD_HEIGHT - 5){

            //Stepクラスの設定
            //mRandom(0.0-1.0)が>0.8ならtypeをStepクラスのSTEP_TYPE_MOVINGに設定
            val type = if(mRandom.nextFloat() > 0.8f) Step.STEP_TYPE_MOVING else Step.STEP_TYPE_STATIC
            //mRandomによってStepが画面からはみ出ない範囲でx座標を変化させる
            val x = mRandom.nextFloat() * (WORLD_WIDTH - Step.STEP_WIDTH)
            val step = Step(type,stepTexture,0,0,144,36)
            step.setPosition(x,y)
            mSteps.add(step)

            //Starクラスの設定
            if(mRandom.nextFloat() > 0.6f){
                val star = Star(starTexture,0,0,72,72)
                //mRandomを使って出現位置をランダムに変える
                //ユーザがStarを取り易いようにstepの近くに出現させる設定
                star.setPosition(step.getX() + mRandom.nextFloat(),step.getY() + Star.STAR_HEIGHT + mRandom.nextFloat() * 3)
                mStars.add(star)
            }

            //Enemyクラスの設定
            if(mRandom.nextFloat() > 0.6f){
                val enemy = Enemy(enemyTexture,0,0,120,74)
                //mRandomを使って出現位置をランダムに変える
                //あまり下にいると不自然なのでy>5以上の範囲に出現させる
                //また、Starと違ってランダム配置とする(stepの位置に依存させない)
                if(y>3){
                enemy.setPosition(mRandom.nextFloat()*10,y)
                //Ufoと区別するため小さめにする
                enemy.setSize(1.2f,0.8f)
                mEnemys.add(enemy)
                }
            }

            y += (maxJumpHeight - 0.5f)
            y -= mRandom.nextFloat() * (maxJumpHeight / 3)
        }

        //Playerを配置
        mPlayer = Player(playerTexture,0,0,72,72)
        mPlayer.setPosition(WORLD_WIDTH / 2 - mPlayer.getWidth() / 2 , Step.STEP_HEIGHT)

        //ゴールのUFOを配置
        mUfo = Ufo(ufoTexture,0,0,120,74)
        mUfo.setPosition(WORLD_WIDTH / 2 - Ufo.UFO_WIDTH / 2 , y)

    }

    private fun update(delta:Float){
        when(mGameState){
            GAME_STATE_READY ->
                updateReady()
            GAME_STATE_PLAYING ->
                updatePlaying(delta)
            GAME_STATE_GAMEOVER ->
                updateGameOver()
        }
    }

    private fun updateReady(){
        if(Gdx.input.justTouched()){
            mGameState = GAME_STATE_PLAYING
        }
    }

    private fun updatePlaying(delta:Float){
        var accel = 0f
        if(Gdx.input.isTouched){
            //Gdx.input.(x,y)　でタッチされた座標を取得してsetメソッドを使って
            // mTouchPoint(Vector3クラス)に保持
            //それを引数としてOrthographicCameraクラスのunprojectメソッドを呼び出し、
            //Camera用の座標に変換
            mGuiViewport.unproject(mTouchPoint.set(Gdx.input.x.toFloat(),Gdx.input.y.toFloat(),0f))
            //Rectangle(開始のx,y,変化量のx,y)　引数
            val left = Rectangle(0f,0f, GUI_WIDTH / 2 , GUI_HEIGHT)
            val right = Rectangle(GUI_WIDTH / 2 , 0f , GUI_WIDTH /2 , GUI_HEIGHT)
            if(left.contains(mTouchPoint.x,mTouchPoint.y)){
                accel = 5.0f
            }
            if(right.contains(mTouchPoint.x,mTouchPoint.y)){
                accel = -5.0f
            }
        }

        //Stepの更新
        for(i in 0 until mSteps.size){
            mSteps[i].update(delta)
        }

        //Playerの更新
        if(mPlayer.y <= 0.5f){
            mPlayer.hitStep()
        }

        mPlayer.update(delta,accel)
        mHeightSoFar = Math.max(mPlayer.y,mHeightSoFar)

        checkCollision()

        checkGameOver()
    }

    private fun updateGameOver(){
        if (Gdx.input.justTouched()){
            mGame.screen = ResultScreen(mGame,mScore)
        }
    }

    private fun checkGameOver(){
        if(mHeightSoFar - CAMERA_HEIGHT / 2 > mPlayer.y){
            Gdx.app.log("AAA","GAMEOVER")
            mGameState = GAME_STATE_GAMEOVER
        }
    }

    private fun checkCollision(){
        //UFOとの当たり判定
        if(mPlayer.boundingRectangle.overlaps(mUfo.boundingRectangle)){
            mGameState = GAME_STATE_GAMEOVER
            sound.play(1.0f)
            sound.dispose()
            //ゲームが終了するのでreturnでcheckCollision()を抜ける
            return
        }

        //Enemyとの当たり判定
        for(i in 0 until mEnemys.size) {
            val enemy = mEnemys[i]
            if (mPlayer.boundingRectangle.overlaps(enemy.boundingRectangle)) {
                Gdx.app.log("AAA", "Touched Enemy")
                //音声データ再生および音声データを破棄する
                sound.play(1.0f)
                sound.dispose()
                //ゲームが終了するのでreturnでcheckCollision()を抜ける
                mGameState = GAME_STATE_GAMEOVER
                return
            }
        }

        //各Starとの当たり判定
        for(i in 0 until mStars.size){
            val star = mStars[i]
            //STAR_NONEになっていたら処理を終えて次のStar判定ループへ移行
            if(star.mState == Star.STAR_NONE){
                continue
            }

            //Starに触れるとmScoreをカウントアップ
            if(mPlayer.boundingRectangle.overlaps(star.boundingRectangle)){
                star.get()
                mScore++
                //mHighScoreを更新するとPreferencesに保存
                if(mScore > mHighScore){
                    mHighScore = mScore
                    mPref.putInteger("HIGHSCORE",mHighScore)
                    mPref.flush()
                }
                //Starの繰り返し処理を終了
                break
            }
        }

        //Stepとの当たり判定。上昇中はStepとの当たり判定を確認しない
        //mPlayerの速度が0より大きければStepの当たり判定が不要になるのでcheckCollision()を抜ける
        if(mPlayer.velocity.y > 0){
            return
        }

        for(i in 0 until mSteps.size){
            val step = mSteps[i]

            if(step.mState == Step.STEP_STATE_VANISH){
                //mStateがすでにSTEP_STATE_VANISHなら次のStep判定ループへ移行
                continue
            }
            if(mPlayer.y > step.y){
                if(mPlayer.boundingRectangle.overlaps(step.boundingRectangle)){
                    mPlayer.hitStep()
                    if(mRandom.nextFloat() > 0.5f){
                        step.vanish()
                    }
                    //Stepの繰り返し処理を終了
                    break
                }
            }
        }

    }



}