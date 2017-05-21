/******************************************************************************//*																																						*//*													線の情報を保持するクラス													*//* 																																						*//******************************************************************************/
import java.awt.geom.*;
import java.awt.Point;

public class Edge extends PartOfEdgeMaker
{
	private int[] end = new int[2];	//辺の端　0 = 始点　1 = 終点

	private Point endXY;

	//四角形に囲まれたかどうかの情報を保持
	private boolean surrounded = false;

	//コンストラクタ
		//num1,num2 = 頂点の識別番号
		//ud,lr = 展開図の上下／左右を通った回数
		//sX,sY = 始点の展開図上のxy座標
		//eX,eY = 終点の展開図上のxy座標
		// w, h = 展開図の幅、高さ
	Edge(int num1,int num2,
			 int   ud,int   lr,
			 int   sX,int   sY,
			 int   eX,int   eY,
			 int    w,int    h)
	{
		//PartOfEdgeMakerクラスのコンストラクタを使用
		super(sX,sY,eX,eY,ud,lr,w,h);

		end[0] = num1;	//始点の頂点の識別番号
		end[1] = num2;	//終点の頂点の識別番号
	}

	//辺の端点の番号(頂点の識別番号)を返す
	public int[] getEnd()
	{
		return end;
	}

	//囲まれているかどうかの情報を入力
	public void setSurround(boolean state)
	{
		surrounded = state;
	}

	//囲まれているかどうかの情報を出力
	public boolean getSurround()
	{
		return surrounded;
	}
}
