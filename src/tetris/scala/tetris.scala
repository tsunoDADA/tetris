/*
プログラムの実行手順：
1. ターミナル / コマンドプロンプトを開く
2. build.sbt が置かれた場所で sbt と入力し、return を押す
3. project tetris と入力し、return を押す
4. run と入力し、return を押す
5. コンパイルが成功したら、tetris.A を選択（1 と入力）し、return を押す
6. ゲーム画面を閉じたら、手動で java を終了する
7. プログラムを変更後、もう一度実行したいときは run と入力し、return を押す
*/

package tetris

import scala.util.Random

import sgeometry.Pos
import sdraw.{World, Color, Transparent, HSB}

import tetris.{ShapeLib => S}

// テトリスを動かすための関数
case class TetrisWorld(piece: ((Int, Int), S.Shape), pile: S.Shape) extends World() {

  // マウスクリックは無視
  def click(p: sgeometry.Pos): World = this

  // ブロックの描画
  def drawRect(x: Int, y: Int, w: Int, h: Int, c: Color): Boolean = {
    canvas.drawRect(Pos(A.BlockSize * x, A.BlockSize * y), A.BlockSize * w, A.BlockSize * h, c)
  }

  // shape の描画（与えられた位置）
  def drawShape(pos: (Int, Int), shape: S.Shape): Boolean = {
    val pos_colors = shape.zipWithIndex.flatMap(row_i => {
      val (row, i) = row_i
      row.zipWithIndex.map(box_j => {
        val (color, j) = box_j
        (j, i, color)
      })
    })

    val (x, y) = pos
    pos_colors.forall(pos_color => {
      val (dx, dy, color) = pos_color
      drawRect(x + dx, y + dy, 1, 1, color)
    })
  }

  // shape の描画（原点）
  def drawShape00(shape: S.Shape): Boolean = drawShape((0, 0), shape)

  // ゲーム画面の描画
  val CanvasColor = HSB(0, 0, 0.1f)

  def draw(): Boolean = {
    val (pos, shape) = piece
    canvas.drawRect(Pos(0, 0), canvas.width, canvas.height, CanvasColor) &&
    drawShape00(pile) &&
    drawShape(pos, shape)
  }

  // 1, 4, 7. tick
  // 目的：時間の経過に応じて世界を更新する
  def tick(): World = {
    /*　課題１
    val ((x,y),s) = piece
    TetrisWorld(((x,y+1),S), pile)
    */

    /*　課題４
    val ((x,y),s) = piece
    val world = TetrisWorld(((x,y+1),s), pile)
    if(collision(world)) TetrisWorld(piece,pile)  else world
    */

    val ((x,y),s) = piece
    val world = TetrisWorld(((x,y+1),s), pile)
    if(collision(world)) {
      val nextPiece = A.newPiece()
      val ((a,b),t) = nextPiece
      val nextPile = eraseRows(S.combine(S.shiftSE(s,x,y),pile))
      if(!S.overlap(S.shiftSE(t,a,b),nextPile)) TetrisWorld(nextPiece, nextPile)
      //endOfWorld("Game Over")←これの使い方がわからずエラーが出るので初期化することにしました。
      else TetrisWorld(nextPiece, S.empty(A.WellHeight,A.WellWidth))
    }
    else world
  }

  // 2, 5. keyEvent
  // 目的：キー入力に従って世界を更新する
  def keyEvent(key: String): World = {
    /*　課題２
    val ((x,y),s) = piece
    key match {
      case "RIGHT" => TetrisWorld(((x+1,y),s), pile)
      case "LEFT"  => TetrisWorld(((x-1,y),s), pile)
      case "UP"    => TetrisWorld(((x,y),S.rotate(s)), pile)
    }
    */

    val ((x,y),s) = piece
    val world = {
      key match {
      case "RIGHT" => TetrisWorld(((x+1,y),s), pile)
      case "LEFT"  => TetrisWorld(((x-1,y),s), pile)
      case "UP"    => TetrisWorld(((x,y),S.rotate(s)), pile)
      }
    }
    if(collision(world)) TetrisWorld(piece,pile)
    else world
  }

  // 3. collision
  // 目的：受け取った世界で衝突が起きているかを判定する
  def collision(world: TetrisWorld): Boolean = {
    val ((x,y), s) = world.piece
    val (r,c) = S.size(s)
    if(x+c-1 >= A.WellWidth || x <= -1 || y+r-1 >= A.WellHeight || S.overlap(S.shiftSE(s,x,y),world.pile)) true
    else false
  }

  // 6. eraseRows
  // 目的：pile を受け取ったら、揃った行を削除する
  def eraseRows(pile: S.Shape): S.Shape = {
    //空白が無い行を消す
    def choice(p:S.Shape):S.Shape = {
      //空白の要素があればtrueを返す関数
      def judge (r:S.Row):Boolean = {
        r match{
          case Nil   => false
          case x::xs => if(x==Transparent) true  else judge(xs)
        }
      }
      p.filter(judge(_))
    }
    val n = A.WellHeight - choice(pile).length
    S.shiftSE(choice(pile),0,n)
  }
}

// ゲームの実行
object A extends App {
  // ゲームウィンドウとブロックのサイズ
  val WellWidth = 10
  val WellHeight = 10
  val BlockSize = 30

  // 新しいテトロミノの作成
  val r = new Random()

  def newPiece(): ((Int, Int), S.Shape) = {
    val pos = (WellWidth / 2 - 1, 0)
    (pos,
     List.fill(r.nextInt(4))(0).foldLeft(S.random())((shape, _) => shape))
  }

  // 最初のテトロミノ
  val piece = newPiece()

  // ゲームの初期値
  val world = TetrisWorld(piece, List.fill(WellHeight)(List.fill(WellWidth)(Transparent)))

  // ゲームの開始
  world.bigBang(BlockSize * WellWidth, BlockSize * WellHeight, 1)
}
