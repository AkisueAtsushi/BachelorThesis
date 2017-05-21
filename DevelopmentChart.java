import java.awt.*;
import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.*;
import java.awt.Font;
import java.awt.geom.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Panel;
import java.awt.Point;

import javax.swing.*;
import javax.swing.JPanel;
import javax.swing.JLabel;

import java.util.ArrayList;

//展開図を表示するクラス///////////////////////////////////////////////////////
public class DevelopmentChart extends JFrame
{
	//フィールド/////////////////////////////////////////////////////////////////

	//展開図を表現するパネル
	private panel p;
	private panel fp;

	//ベース
	private JPanel base;

	//頂点、辺の数を表示
	private jlabel ver;
	private jlabel edg;

	//フェイクの展開図の大きさ
	private int D_WIDTH;
	private int D_HEIGHT;

	//フェイクと本物の展開図との幅
	private int DIFF = Vertex.getDiameter();

	//展開図の境目を繋ぐ為の処理
	private Robot r;

	//点の情報群
	private ArrayList<Vertex> points = new ArrayList<Vertex>(0);

	//辺の情報群
	private ArrayList<Edge> edges = new ArrayList<Edge>(0);

	//トータルで何個目の頂点かを判別するための変数（識別番号）
	private int count = 0;

	//マウスがオンになっている頂点のアレイリスト上の順番を保持
	private int mOn = -1;

	//マウスが四角形描画可能かどうか true = 可能 false = 描画不可
	private boolean isDrawRect = true;

	//四角形描画中かどうかを判定
	private boolean isRect = false;

	//四角形が描画が完成されている状況を保持
	private boolean madeRect = false;

	//四角形の始点
	private int prevX;
	private int prevY;

	//四角形の終点
	private int currentX;
	private int currentY;

	//辺を描画中かどうかを判定(始点となる頂点の"識別番号"を保持)
	private int isDrawLine = -1;

	//描画中の辺
	private PartOfEdgeMaker poem;

	//描画中の辺が展開図の切れ目を通った回数を保持
	private int throughUD;	//上下の切れ目
	private int throughLR;	//左右の切れ目

	//展開図の切れ目の位置を保持する
	private double edgeOfXZplane = 0.0;
	private double edgeOfXYplane = 0.0;

	//展開図にマウスが乗っているかどうかの状況を保持
	private boolean isMouseOnChart = false;

	//マウスが乗っかっているトーラス上の位置
	private double mXZ_Plane = 0.0;
	private double mXY_Plane = 0.0;

	//マウスが乗っかっている展開図上の位置
	private int mX = 0;
	private int mY = 0;

	//ひとつ前のマウスの位置
	private int preX;
	private int preY;

	//境界線が移動中かどうかを判断
	private boolean isChangeEdge = false;

	//頂点が境界線を通過できるかどうかを判定
	private boolean canThrough = false;

	//境界線上のどの部分に本当の頂点が乗っているかの情報を保持
	private boolean onUp = false, onDown = false, onLeft = false, onRight = false;
	//選択できる頂点グラフを増やせるかどうか判断
	private boolean addSelect = false;

	//マウスがドラッグ中か、ムーブ中か判定　true = drag, false = move
	private boolean moveOrdrag = false;

	//フィールドここまで/////////////////////////////////////////////////////////

	//コンストラクタ
	DevelopmentChart()
	{
		super("Net of Torus");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setBounds(700, 0, 500, 485);

		//レイアウト無効化
		setLayout(null);

		//リサイズ無効
		setResizable(false);

		//パネルを取り付け
		p = new panel(true);
		p.setBounds(30,100,430,300);
		p.setOpaque(true);
		p.addMouseListener(new myListener());
		p.addMouseMotionListener(new myMotionListener());
		p.addKeyListener(new myKeyListener());
		p.setFocusable(true);
		p.requestFocus();
		getContentPane().add(p);

		//フェイクのパネル
		fp = new panel(false);
		fp.addMouseListener(new myListener());
		fp.addMouseMotionListener(new myMotionListener());
		fp.setOpaque(true);
		getContentPane().add(fp);

		changeFakePanel();

		//Torus
		JLabel torus = new JLabel()
		{
			public void paintComponent(Graphics g)
			{
				Graphics2D g2 = (Graphics2D)g;

				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
														RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

				g2.drawString("Torus",0, 30);
			}
		};
		torus.setBounds(30,5,90,35);
		torus.setFont(new Font("Serif", Font.BOLD, 30));
		getContentPane().add(torus);

		//Development Chart
		JLabel chart = new JLabel()
		{
			public void paintComponent(Graphics g)
			{
				Graphics2D g2 = (Graphics2D)g;

				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
														RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

				g2.drawString("-Graph maker on Surface-",0,15);
			}
		};
		chart.setBounds(30,38,200,20);
		chart.setFont(new Font("Serif", Font.BOLD, 15));
		getContentPane().add(chart);

		//コマンドの説明
		JLabel tutorial = new JLabel()
		{
			public void paintComponent(Graphics g)
			{
				Graphics2D g2 = (Graphics2D)g;

				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
														RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

				graphTutorial(g2);
			}

			//チュートリアル表示
			public void graphTutorial(Graphics2D g2)
			{
				//頂点がひとつもないとき
				if(points.size() == 0 && !madeRect && !isRect)
					g2.drawString("Click : make Vertex", 0, 10);

				//辺の描画中×、頂点上マウスオン×、ドラッグ中×、四角形完成×
				else if(mOn == -1 && isDrawLine == -1 &&
								moveOrdrag == false && !madeRect)
				{
					g2.drawString("Click : make Vertex", 0, 10);
					g2.drawString("Drag : select Vertex and Edge", 125, 10);
					g2.drawString("←↑↓→：Shift position of edge of developed Torus."
																																				,0,30);
				}

				//辺の描画中×、頂点上マウスオン○、ドラッグ中×
				else if(mOn != -1 && isDrawLine == -1 && moveOrdrag == false)
				{
					g2.drawString("Click : draw Edge", 0, 10);
					g2.drawString("Drag : move Vertex", 125, 10);
					g2.drawString("Double Click : remove this Vertex", 0, 30);
				}

				//頂点をドラッグ中で、境界線通過不可のとき
				else if(mOn != -1 && isDrawLine == -1 &&
																						moveOrdrag == true && !canThrough)
					g2.drawString("+Shift : move Continuously", 0, 10);

				//頂点をドラッグ中で、境界線通過可能のとき
				else if(mOn != -1 && isDrawLine == -1 &&
																						moveOrdrag == true && canThrough)
					g2.drawString("-Shift : stop to the edge", 0, 10);

				//辺の描画中で、マウスが始点以外の頂点にオンしていない
				else if(mOn == -1 && isDrawLine != -1)
					g2.drawString("Click : set Edge + make Vertex",0, 10);

				//辺の描画中で、マウスが始点以外の頂点にオンしている
				else if(mOn != -1 && isDrawLine != -1 &&
								points.get(mOn).getVertexNumber() != isDrawLine)
					g2.drawString("Click : set Edge", 0, 10);

				//辺の描画中で、マウスが始点の頂点にオンしている
				else if(mOn != -1 && isDrawLine != -1 &&
								points.get(mOn).getVertexNumber() == isDrawLine)
					g2.drawString("Click : quit to draw Edge", 0, 10);

				//エリア選択中
				else if(isRect && !madeRect)
					g2.drawString("Release : select Vertex and Edge", 0, 10);

				//四角形を描画後
				else if(madeRect && points.size() != 0 && !addSelect)
				{
					g2.drawString("Drag +Shift : select more Vertex and Edge", 0, 10);
					g2.drawString("Press 'delete' : remove selected Vertex and Edge",
																																0, 30);
					g2.drawString("Click : quit selection", 260, 10);
				}

				//さらに追加して四角形を描画可能なとき（Shiftを押しているとき）
				else if(madeRect && points.size() != 0 && addSelect)
					g2.drawString("Drag : select more Vertex and Edge", 0, 10);

				//何もグラフが描かれていない状況で四角形を描画した場合
				else if(madeRect && points.size() == 0)
					g2.drawString("Click : quit selection", 0, 10);
			}
		};
		tutorial.setBounds(30,410,430,60);
		tutorial.setFont(new Font("Serif", Font.PLAIN, 14));
		getContentPane().add(tutorial);

		//頂点数表示
		ver = new jlabel()
		{
			public void paintComponent(Graphics g)
			{
				Graphics2D g2 = (Graphics2D)g;

				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
														RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

				g2.drawString("Vertex : " + i, 0, 28);
			}
		};
		ver.setBounds(30,53,100,40);
		ver.setFont(new Font("Serif", Font.BOLD, 20));
		getContentPane().add(ver);

		//辺数表示
		edg = new jlabel()
		{
			public void paintComponent(Graphics g)
			{
				Graphics2D g2 = (Graphics2D)g;

				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
														RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

				g2.drawString("Edge : " + i, 0, 28);
			}
		};
		edg.setBounds(135,53,100,40);
		edg.setFont(new Font("Serif", Font.BOLD, 20));
		getContentPane().add(edg);

		//ベース
		base = new JPanel();
		base.setBounds(0,0,getWidth(),getHeight());
		base.setBackground(Color.WHITE);
		getContentPane().add(base);
	}

	//フェイクのパネルの大きさを変更する
	public void changeFakePanel()
	{
		DIFF = Vertex.getDiameter();

		//展開図の大きさを指定
		D_WIDTH = p.getWidth() + (DIFF*2);
		D_HEIGHT = p.getHeight() + (DIFF*2);

		fp.setBounds(30-DIFF,100-DIFF,D_WIDTH,D_HEIGHT);
	}

	//頂点の数を返す
	public int getVertexNumbers()
	{
		return points.size();
	}

	//辺の数を返す
	public int getEdgeNumbers()
	{
		return edges.size();
	}

	//各頂点の情報を返す
	public Vertex getVertexInfo(int i)
	{
		if(i>=points.size()) return null;
		return points.get(i);
	}

	//各辺の情報を返す
	public Edge getEdgeInfo(int i)
	{
		if(i>=edges.size()) return null;
		return edges.get(i);
	}

	//辺の描画状況を返す
	public boolean getIsDraw()
	{
		if(isDrawLine == -1)	return false;
		else	return true;
	}

	//描画中の辺の情報を返す
	public PartOfEdgeMaker getIsDrawInfo()
	{
		return poem;
	}

	//マウスが展開図の中にあるかどうかの情報を返す
	public boolean getIsMouseOnChart()
	{
		return isMouseOnChart;
	}

	//マウスが乗っている位置を返す
	public Point2D.Double getMouseOnTorus()
	{
		return new Point2D.Double(mXY_Plane, mXZ_Plane);
	}

	//境界線の移動中かどうかの状況を出力 true = 移動中
	public boolean getIsChangeEdge()
	{
		return isChangeEdge;
	}

	//境界線の角度情報を出力
	public Point2D.Double getTorusEdge()
	{
		return new Point2D.Double(edgeOfXYplane,edgeOfXZplane);
	}

	//マウスモーションイベント/////////////////////////////////////////////
	public class myMotionListener extends MouseMotionAdapter
	{
		//ドラッグにて頂点を反対側に移動 true = 可能 false = 不可能
		private boolean isMoving = false;

		//コンストラクタ
		myMotionListener()
		{
			try{ r = new Robot(); }
			catch(AWTException e)
			{
				e.printStackTrace();
				return;
			}
		}

		//マウスドラッグ///////////////////////////////////////////////////////////
		public void mouseDragged(MouseEvent e)
		{
			//マウスがドラッグしている状態へ
			moveOrdrag = true;

			//マウスの位置情報を入力
			mXZ_Plane = (double)p.getWidth()/360.0*e.getY() + edgeOfXZplane;
			mXY_Plane = (double)p.getHeight()/360.0*e.getX() + edgeOfXYplane;

			//マウスのひとつ前の位置を入力
			preX = mX; preY = mY;

			mX = e.getX(); mY = e.getY();

			//マウスが頂点の上に乗っていて、辺の描画中ではないなら
			if(mOn != -1 && points.get(mOn).getMouseState() && isDrawLine == -1)
			{
				//四角形に囲まれていない状況に設定
				for(int i=0; i<edges.size(); i++)
					edges.get(i).setSurround(false);
				for(int i=0;i<points.size(); i++)
					points.get(i).setSurround(false);

				Point pos = p.getLocationOnScreen();

				int diffX = e.getX()-points.get(mOn).getXYi('x');
				int diffY = e.getY()-points.get(mOn).getXYi('y');

				//カーソルの位置を決定（境界線を通過できる場合）
				if(canThrough)
				{
					if(e.getX() <= 0 || e.getX() >= p.getWidth() ||
						 e.getY() <= 0 || e.getY() >= p.getHeight())
					{
						isMoving = true;
						if(e.getX() <= 0)
						{
							onLeft = true;	r.mouseMove(pos.x+p.getWidth()-1,pos.y+e.getY());
						}
						else if(e.getX() >= p.getWidth())
						{
							onRight = true;	r.mouseMove(pos.x+1, pos.y+e.getY());
						}

						if(e.getY() <= 0)
						{
							onUp = true;	r.mouseMove(pos.x+e.getX(), pos.y+p.getHeight()-1);
						}
						else if(e.getY() >= p.getHeight())
						{
							onDown = true;	r.mouseMove(pos.x+e.getX(), pos.y+1);
						}
					}
				}

				//カーソルの位置を決定（境界線を通過できない場合)
				else
				{
					isMoving = false;

					if(e.getX() < 20 && mX < preX)
					{
						r.mouseMove(pos.x, pos.y+e.getY());
						diffX = -points.get(mOn).getXYi('x');
					}

					else if(e.getX() > p.getWidth()-20 && mX > preX)
					{
						r.mouseMove(pos.x+p.getWidth(), pos.y+e.getY());
						diffX = p.getWidth() - points.get(mOn).getXYi('x');
					}

					if(e.getY() < 20 && mY < preY)
					{
						r.mouseMove(pos.x+e.getX(), pos.y);
						diffY = -points.get(mOn).getXYi('y');
					}

					else if(e.getY() > p.getHeight()-20 && mY > preY)
					{
						r.mouseMove(pos.x+e.getX(), pos.y+p.getHeight());
						diffY = p.getHeight()-points.get(mOn).getXYi('y');
					}

					if(e.getX() <= 20 && e.getY() <= 20 && (mY < preY || mX < preX))
						r.mouseMove(pos.x, pos.y);
					else if(e.getX() <= 20 && e.getY() >= p.getHeight()-20 &&
																											(mY > preY || mX < preX))
						r.mouseMove(pos.x, pos.y+p.getHeight());
					else if(e.getX() >= p.getWidth()-20 && e.getY() <= 20 &&
																											(mY < preY || mX > preX))
						r.mouseMove(pos.x+p.getWidth(), pos.y);
					else if(e.getX() >= p.getWidth()-20 && e.getY() >= p.getHeight()-20 &&																											(mY > preY || mX > preX))
						r.mouseMove(pos.x+p.getWidth(), pos.y+p.getHeight());
				}

				moveGraph(mOn, diffX, diffY);

				if(!canThrough)
					moveOnEdgeGraph(e.getX(), e.getY());
			}
			//マウスが頂点の上にのっている場合の処理ここまで

			else if(isDrawRect)	//四角形の描画可能なら
			{
				isRect = true;
				currentX = e.getX();	currentY = e.getY();
			}

			//辺の描画中なら
			if(isDrawLine != -1)
			{
				p.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
				fp.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
				poem.setVirtualPosition(e.getX(),e.getY(),p.getWidth(),p.getHeight());
				poem.pointCalculate(p.getWidth(),p.getHeight());
			}

			repaint();
		}
		//マウスドラッグここまで///////////////////////////////////////////////////

		//マウスが展開図の切れ目に近づいたら、境界上のグラフを移動する
		public void moveOnEdgeGraph(int x, int y)
		{
			if(x < 30)	onRight = true;
			else if(x > p.getWidth() - 30) onLeft = true;
			if(y < 30)	onDown = true;
			else if(y > p.getHeight() - 30) onUp = true;

			if(onUp == true || onDown == true || onRight == true || onLeft == true)
			{
				isMoving = false; canThrough = true;
				for(int m=0; m < points.size(); m++)
				{
					if(onUp && points.get(m).getXYi('y') == 0)
						moveGraph(m, 0, 0);
					else if(onDown && points.get(m).getXYi('y') == p.getHeight())
						moveGraph(m, 0, 0);
					if(onLeft && points.get(m).getXYi('x') == 0)
						moveGraph(m, 0, 0);
					else if(onRight && points.get(m).getXYi('x') == p.getWidth())
						moveGraph(m, 0, 0);
				}
				onUp = onDown = onRight = onLeft = false;	canThrough = false;
			}
		}

		//境界線に近い頂点を境界線上に移動させる
		public void moveToEdge()
		{
			int marge = 4; boolean keepCanThrough = canThrough;

			canThrough = false;
			for(int m=0; m < points.size(); m++)
			{
				if(points.get(m).getXYi('y') <= marge)
					moveGraph(m, 0, -marge);
				else if(points.get(m).getXYi('y') >= p.getHeight() - marge)
					moveGraph(m, 0, marge);
				if(points.get(m).getXYi('x') <= marge)
					moveGraph(m, -marge, 0);
				else if(points.get(m).getXYi('x') >= p.getWidth() - marge)
					moveGraph(m, marge, 0);
			}
			canThrough = keepCanThrough;
		}

		//マウスムーブ/////////////////////////////////////////////////////////////
		public void mouseMoved(MouseEvent e)
		{
			//マウスがドラッグしていない状態へ
			moveOrdrag = false;

			p.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
			fp.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));

			//マウスのトーラス上の位置情報を入力
			mXZ_Plane = (double)p.getWidth()/360.0*e.getY() + edgeOfXZplane;
			mXY_Plane = (double)p.getHeight()/360.0*e.getX() + edgeOfXYplane;

			//マウスのひとつ前の位置を入力
			preX = mX; preY = mY;

			//マウスの展開図上の位置情報を入力
			mX = e.getX(); mY = e.getY();

			//境界線上のグラフを移動させる
			moveOnEdgeGraph(e.getX(), e.getY());

			//境界線上にグラフを移動させる
			moveToEdge();

			//辺の描画中なら
			if(isDrawLine != -1)
			{
				poem.setVirtualPosition(e.getX(),e.getY(),p.getWidth(),p.getHeight());
				poem.pointCalculate(p.getWidth(),p.getHeight());
			}

			if(!madeRect)
				if(checkIsMouseOn(e.getX(), e.getY()))
					mOn = -1;

			repaint();
		}
		//マウスムーブここまで/////////////////////////////////////////////////////

		//マウスが頂点に吸着するか調べる chackIsAbsortion関数(引数=頂点の座標)
		public boolean checkIsAbsortion(double x, double y, Vertex v)
		{
			Point pos = p.getLocationOnScreen();
			Point2D.Double cen = new Point2D.Double(x, y);
			Point2D.Double pre = new Point2D.Double(preX, preY);
			Point2D.Double cur = new Point2D.Double(mX, mY);

			if(x == 0) x++;
			else if(x == p.getWidth()) x--;

			if(y == 0) y++;
			else if(y == p.getHeight()) y--;

			if(cen.distance(cur) <= cen.distance(pre) ||
					v.isContainSteadyRange(mX, mY))
			{
				r.mouseMove(pos.x+(int)x, pos.y+(int)y);	return true;
			}

			else return false;
		}

		//マウスが頂点の上に乗っているかどうか調べる checkIsMouseOn関数
		public boolean checkIsMouseOn(int x, int y)
		{
			boolean isOn = false;

			//全ての頂点を探索
			for(int i=0; i<points.size(); i++)
			{
				Vertex v = points.get(i);

				v.setMouseState(false);	//初期化
			}

			//全ての頂点を探索
			for(int i=0; i<points.size(); i++)
			{
				Vertex v = points.get(i);

				//実際に頂点がある場所にカーソルが乗っている
				if(v.isContain((double)x,(double)y))	isOn = true;

				//頂点のレンジに入っている
				else if(v.isContainRange((double)x, (double)y))
					if(checkIsAbsortion((double)v.getXYi('x'),(double)v.getXYi('y'), v))
						isOn = true;

				//マウスが頂点の上に乗っているなら
				if(isOn)
				{
					mOn = i;
					points.get(mOn).setMouseState(true); //マウスがOnであることを知らせる
					p.setCursor(new Cursor(Cursor.HAND_CURSOR));
					fp.setCursor(new Cursor(Cursor.HAND_CURSOR));
					return false;
				}
			}
			//全ての頂点探索ここまで

			return true;
		}

		//頂点,辺を移動する関数　moveGraph関数/////////////////////////////////////
			// i = 移動する頂点のアレイリスト上の番号
			//mx = 頂点のx座標の移動量
			//my = 頂点のy座標の移動量
		public void moveGraph(int i,int mx, int my)
		{
			Vertex v = points.get(i);
			int number = points.get(i).getVertexNumber();

			//頂点の表示位置を変更
			v.setXY(v.getXYi('x')+mx, v.getXYi('y')+my, p.getWidth(), p.getHeight());

			//展開図の切れ目を繋ぐ処理
			boolean throughUp = false, throughDown = false,
							throughLeft = false, throughRight = false,
							isThrough = true;

			//境界線の通過が可能なら
			int m = 1;	if(isMoving == false)	m = 0;

			//頂点の表示位置を変更（境界線を通過可能な場合）
			if(canThrough)
			{
				if(v.getXYi('y') <= 0 && onUp == true)
				{
					throughUp = true;
					v.setXY(-p.getWidth(),p.getHeight()-m,p.getWidth(), p.getHeight());
				}

				else if(v.getXYi('y') >= p.getHeight() && onDown == true)
				{
					throughDown = true;
					v.setXY(-p.getWidth(), 0+m, p.getWidth(), p.getHeight());
				}

				if(v.getXYi('x') <= 0 && onLeft == true)
				{
					throughLeft = true;
					v.setXY(p.getWidth()-m,-p.getHeight(),p.getWidth(), p.getHeight());
				}

				else if(v.getXYi('x') >= p.getWidth() && onRight == true)
				{
					throughRight = true;
					v.setXY(0+m, -p.getHeight(), p.getWidth(), p.getHeight());
				}

				else isThrough = false;
			}

			else
			{
				if(v.getXYi('y') < 0)
					v.setXY(v.getXYi('x'), 0, p.getWidth(), p.getHeight());

				else if(v.getXYi('y') > p.getHeight())
					v.setXY(v.getXYi('x'), p.getHeight(), p.getWidth(), p.getHeight());

				if(v.getXYi('x') < 0)
					v.setXY(0, v.getXYi('y'), p.getWidth(), p.getHeight());

				else if(v.getXYi('x') > p.getWidth())
					v.setXY(p.getWidth(), v.getXYi('y'), p.getWidth(), p.getHeight());
			}

			//マウスが乗っかった頂点に繋がった辺とその終点の頂点を取得///////////

			//マウスが乗っかった頂点に繋がった辺のアレイリスト上の番号を保持
			ArrayList<Integer> edgeNumber = new ArrayList<Integer>(0);

			//展開図にある辺の数だけ検索
			for(int j=0; j<edges.size(); j++)
			{
				int[] end = edges.get(j).getEnd();

				//マウスが乗った頂点が辺の始点なら
				if(end[0] == number)
				{
					//終点・始点を逆にする
					edges.add(j, new Edge(end[1],end[0],
														-(edges.get(j).getUpDownThroughTimes()),
														-(edges.get(j).getLeftRightThroughTimes()),
														edges.get(j).getEndXY().x,
														edges.get(j).getEndXY().y,
														edges.get(j).getStartX(),
														edges.get(j).getStartY(),
														p.getWidth(), p.getHeight()
														));
					edges.remove(j+1);
					j--;
				}

				//マウスが乗った頂点が辺の終点なら
				else if(end[1] == number)
					edgeNumber.add(j);
			}
			//マウスが乗っかった頂点に繋がった辺とその終点の頂点を取得ここまで///

			//頂点に付随した辺の諸々の情報を変更/////////////////////////////////
			for(int j=0; j<edgeNumber.size();j++)
			{
				Edge aE = edges.get(edgeNumber.get(j));

				if(throughUp)
				{
					//同じ境界線にある辺を逆側に移動する
					if(aE.getStartY() == 0 && aE.getVirtualY() == 0 &&
						 aE.getUpDownThroughTimes() == 0)
						edges.get(edgeNumber.get(j)).setStartY(p.getHeight());

					//(既に逆側に移動して)反対側の境界線上の辺でないなら
					else if(!(aE.getStartY() == p.getHeight() &&
										aE.getVirtualY() == p.getHeight() &&
										aE.getUpDownThroughTimes() == 0))
						edges.get(edgeNumber.get(j)).setUpDownThroughTimes(-1);
				}

				else if(throughDown)
				{
					//同じ境界線にある辺、頂点を逆側に移動する
					if(aE.getStartY() == p.getHeight() &&
						 aE.getVirtualY() == p.getHeight() &&
						 aE.getUpDownThroughTimes() == 0)
						edges.get(edgeNumber.get(j)).setStartY(0);

					//(既に逆側に移動して)反対側の境界線上の辺でないなら
					else if(!(aE.getStartY() == 0 && aE.getVirtualY() == 0 &&
									aE.getUpDownThroughTimes() == 0))
						edges.get(edgeNumber.get(j)).setUpDownThroughTimes(1);
				}

				else if(throughLeft)
				{
					//同じ境界線にある辺、頂点を逆側に移動する
					if(aE.getStartX() == 0 && aE.getVirtualX() == 0 &&
						 aE.getLeftRightThroughTimes() == 0)
						edges.get(edgeNumber.get(j)).setStartX(p.getWidth());

					//(既に逆側に移動して)反対側の境界線上の辺でないなら
					else if(!(aE.getStartX() == p.getWidth() &&
										aE.getVirtualX() == p.getWidth() &&
										aE.getLeftRightThroughTimes() == 0))
						edges.get(edgeNumber.get(j)).setLeftRightThroughTimes(-1);
				}

				else if(throughRight)
				{
					//同じ境界線にある辺、頂点を逆側に移動する
					if(aE.getStartX() == p.getWidth() &&
						 aE.getVirtualX() == p.getWidth() &&
						 aE.getLeftRightThroughTimes() == 0)
						edges.get(edgeNumber.get(j)).setStartX(0);

					//(既に逆側に移動して)反対側の境界線上の辺でないなら
					else if(!(aE.getStartX() == 0 && aE.getVirtualX() == 0 &&
										aE.getLeftRightThroughTimes() == 0))
						edges.get(edgeNumber.get(j)).setLeftRightThroughTimes(1);
				}

				//仮想上のxy座標を再設定
				edges.get(edgeNumber.get(j)).setVirtualPosition(
																								v.getXYi('x'), v.getXYi('y'),
																								p.getWidth(), p.getHeight());

				//頂点に付随した辺のパーツを作成
				edges.get(edgeNumber.get(j)).pointCalculate(p.getWidth(),
																										p.getHeight());
			}
			//頂点に付随した辺の諸々の情報を変更ここまで/////////////////////////
		}
		//頂点,辺を移動する関数　moveGraph関数 ここまで////////////////////////////
	}
	//マウスモーションイベントここまで///////////////////////////////////////////

	//マウスイベント/////////////////////////////////////////////////////////////
  public class myListener extends MouseAdapter implements MouseMotionListener
	{
		//辺描画中の起点の頂点のアレイリスト上の順番を保持
		private int clicked = -1;

		//コンストラクタ
		myListener(){

			try{ r = new Robot(); }
			catch(AWTException e)
			{
				e.printStackTrace();
				return;
			}
		}

		//mouseEntered関数/////////////////////////////////////////////////////////
		public void mouseEntered(MouseEvent e)
		{
			isMouseOnChart = true;
		}
		//mouseEntered関数ここまで/////////////////////////////////////////////////

		//マウスが離れるとき(辺の描画中、展開図の切れ目を通った回数を求める)///////
		public void mouseExited(MouseEvent e)
		{
			//線の描画中かつマウスが頂点に乗ってなければ
			if(isDrawLine != -1 && mOn == -1)
			{
				//展開図の切れ目を繋ぐ処理
				Point pos = p.getLocationOnScreen();

				if(e.getX() <= 0)
				{
					throughLR--;
					poem.setLeftRightThroughTimes(-1);
					r.mouseMove(pos.x+p.getWidth(),pos.y+e.getY());
				}

				if(e.getX() >= p.getWidth()-1)
				{
					throughLR++;
					poem.setLeftRightThroughTimes(1);
					r.mouseMove(pos.x,pos.y+e.getY());
				}

				if(e.getY() <= 0)
				{
					throughUD--;
					poem.setUpDownThroughTimes(-1);
					r.mouseMove(pos.x+e.getX(),pos.y+p.getHeight());
				}

				if(e.getY() >= p.getHeight()-1)
				{
					throughUD++;
					poem.setUpDownThroughTimes(1);
					r.mouseMove(pos.x+e.getX(),pos.y);
				}

				poem.setVirtualPosition(e.getX(), e.getY(),p.getWidth(),p.getHeight());
				poem.pointCalculate(p.getWidth(),p.getHeight());
			}

			else	isMouseOnChart = false;

			repaint();
		}
		//mouseExitedここまで//////////////////////////////////////////////////////

		//マウスプレス/////////////////////////////////////////////////////////////
		public void mousePressed(MouseEvent e)
		{
			//念のため
			p.requestFocus();

			int count1 = 0;
			prevX = e.getX();
			prevY = e.getY();

			if(addSelect == false)
			{
				//四角形に囲まれていない状況に設定
				for(int i=0; i<edges.size(); i++)
					edges.get(i).setSurround(false);
				for(int i=0;i<points.size(); i++)
					points.get(i).setSurround(false);
			}

			if(isDrawRect == true)
			{
				//マウスが頂点の上に乗っているなら四角形描画はしない
				if(mOn != -1 && points.get(mOn).getMouseState())
				{
					isRect = false;
					isDrawRect = false;
					count1++;
				}
			}

			if(count1 == 0) isDrawRect = true;
			if(isDrawLine != -1) isDrawRect = false;
		}
		//マウスプレスここまで/////////////////////////////////////////////////////

		//マウスリリース
		public void mouseReleased(MouseEvent e)
		{
			//境界線を越えてしまった頂点をもとにもどす
			for(int i=0; i < points.size(); i++)
			{
				Vertex v = points.get(i);

				if(v.getXYi('x') < 0)
					v.setXY(0, v.getXYi('y'), p.getWidth(), p.getHeight());
				else if (v.getXYi('x') > p.getWidth())
					v.setXY(p.getWidth(), v.getXYi('y'), p.getWidth(), p.getHeight());

				if(v.getXYi('y') < 0)
					v.setXY(v.getXYi('x'), 0, p.getWidth(), p.getHeight());
				else if(v.getXYi('y') > p.getHeight())
					v.setXY(v.getXYi('x'), p.getHeight(), p.getWidth(), p.getHeight());
			}

			//四角形の描画中なら
			if(isDrawRect == true)
			{
				currentX = e.getX();
				currentY = e.getY();

				//四角形ができているかどうか判定
				if(prevX == currentX && prevY == currentY)	isRect = false;

				//四角形に含まれている辺、頂点を判別
				if(isRect)
					if(checkSurrounded(prevX,prevY,currentX,currentY))
						madeRect = true;
			}

			isDrawRect = true;
			repaint();
		}
		//マウスリリースここまで

		//mouseClicked関数/////////////////////////////////////////////////////////
		public void mouseClicked(MouseEvent e)
		{
			int num = -1;	//クリックされた頂点の識別番号

			//四角形ができていて、Shiftキーが押されていない
			if(madeRect && !addSelect){	isRect = false; madeRect = false; }

			else if(madeRect && addSelect){ isRect = false;}

			//四角形ができていないなら
			else if(!isRect)
			{
				//マウスが頂点に乗っているかどうかチェック
				if(mOn != -1 && points.get(mOn).getMouseState())
					num = points.get(mOn).getVertexNumber();//頂点の識別番号

				//マウスが頂点に乗っていないなら頂点を新規作成
				if(num == -1)
				{
					points.add(new Vertex(p.getWidth(), p.getHeight(),
																e.getX(), e.getY(), count));
					count++;
					ver.changeNumber(points.size());

					p.setCursor(new Cursor(Cursor.HAND_CURSOR));
					fp.setCursor(new Cursor(Cursor.HAND_CURSOR));

					mOn = points.size()-1;
					points.get(mOn).setMouseState(true);

					//辺の描画中なら辺を新規作成、辺描画終了
					if(isDrawLine != -1)
					{
						edges.add(new Edge(isDrawLine,
															 points.get(points.size()-1).getVertexNumber(),
															 throughUD,throughLR,
															 points.get(clicked).getXYi('x'),
															 points.get(clicked).getXYi('y'),
															 e.getX(),e.getY(),
															 p.getWidth(),p.getHeight()));
						edg.changeNumber(edges.size());
						throughUD = 0; throughLR = 0;
						points.get(clicked).setMouseClick(false);
						isDrawLine = -1;
						clicked = -1;
					}
				}

				//マウスが頂点の上に乗っている
				else
				{
					//ダブルクリックで頂点削除////////////////////
					if(e.getClickCount() >= 2 && isDrawLine != -1)
					{
						//削除する頂点から出ている辺も削除
						for(int i=0; i<edges.size(); i++)
						{
							int[] rend = edges.get(i).getEnd();

							if(rend[0] == num || rend[1] == num)
							{
								edges.remove(i);
								i--;
							}
							if(edges.size() == 0)
									break;
						}

						points.remove(mOn);
						ver.changeNumber(points.size());
						edg.changeNumber(edges.size());
						p.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
						mOn = -1;
						isDrawLine = -1;
					}
					//削除ここまで///////////////////////////////

					//マウスが頂点の上に乗っていて且つダブルクリックでない
					else
					{
						//辺の描画中でないなら
						if(isDrawLine == -1)
						{
							points.get(mOn).setMouseClick(true);//頂点上クリックを伝達
							isDrawLine = num;
							clicked = mOn;
							poem = new PartOfEdgeMaker(points.get(mOn).getXYi('x'),
																				 points.get(mOn).getXYi('y'),
																				 points.get(mOn).getXYi('x'),
																				 points.get(mOn).getXYi('y'),
																				 0,0,
																				 p.getWidth(),p.getHeight());
						}

						//辺の描画中なら
						else if(isDrawLine != -1)
						{
							//辺描画の始点と同じでないなら辺を作成
							if(isDrawLine != num)
							{
								points.get(mOn).setMouseState(true);
								edges.add(new Edge(isDrawLine,num,
																	 throughUD,throughLR,
																	 points.get(clicked).getXYi('x'),
																	 points.get(clicked).getXYi('y'),
																	 points.get(mOn).getXYi('x'),
																	 points.get(mOn).getXYi('y'),
																	 p.getWidth(),p.getHeight()));
								edg.changeNumber(edges.size());
								throughUD = 0; throughLR = 0;
							}

							//情報を初期状態に戻す
							points.get(clicked).setMouseClick(false);
							isDrawLine = -1;
							clicked = -1;
						}
					}
				}
				//マウスが頂点に乗っている時の処理ここまで
			}
			repaint();
		}
		//マウスクリック関数ここまで///////////////////////////////////////////////

		//四角形に囲まれているかどうかをチェック///////////////////
		public boolean checkSurrounded(int x1, int y1, int x2, int y2)
		{
			boolean inVertex = false, inEdge = false;

			int topx, bottomx, topy, bottomy;
			int width, height;

			if(x1 > x2)
			{
				topx = x2; bottomx = x1;
			}
			else
			{
				topx = x1; bottomx = x2;
			}

			if(y1 > y2)
			{
				topy = y2; bottomy = y1;
			}
			else
			{
				topy = y1; bottomy = y2;
			}

			width = bottomx - topx; height = bottomy - topy;
			Rectangle checkR = new Rectangle(topx,topy,width,height);

			//頂点のチェック
			for(int i=0; i<points.size(); i++)
			{
				Vertex v = points.get(i);
				int inx = v.getXYi('x');
				int iny = v.getXYi('y');

				if(checkR.contains(inx,iny))
					v.setSurround(true);

				if(v.getIsOnBound() == Vertex.isUDbound)
					if(checkR.contains(inx,0) || checkR.contains(inx,p.getHeight()))
						v.setSurround(true);

				if(v.getIsOnBound() == Vertex.isLRbound)
					if(checkR.contains(0,iny) || checkR.contains(p.getWidth(),iny))
						v.setSurround(true);

				if(v.getIsOnBound() == Vertex.isCrossbound)
					if(checkR.contains(0,0) || 
						 checkR.contains(p.getWidth(),p.getHeight()) ||
						 checkR.contains(0,p.getHeight()) ||
						 checkR.contains(p.getWidth(), 0))
						v.setSurround(true);

				if(v.getSurround())
				{
					inVertex = true;
					for(int j=0; j<edges.size(); j++)
					{
						int[] temp = edges.get(j).getEnd();
						if(points.get(i).getVertexNumber() == temp[0] ||
							 points.get(i).getVertexNumber() == temp[1])
							edges.get(j).setSurround(true);
					}
				}
			}

			//辺のチェック
			for(int i=0; i<edges.size(); i++)
			{
				for(int j=0; j<edges.get(i).getDivideNumber(); j++)
				{
					boolean surround = true;

					//辺の各パーツの両端の頂点のxy座標を取得
					int ex1, ey1, ex2, ey2;

					ex1 = edges.get(i).getDivideStartX(j);
					ey1 = edges.get(i).getDivideStartY(j);
					ex2 = edges.get(i).getDivideEndX(j);
					ey2 = edges.get(i).getDivideEndY(j);

					if(ex1 > ex2)
					{
						if(topx > ex1 || bottomx < ex2)
							surround = false;
					}

					else
					{
						if(topx > ex2 || bottomx < ex1)
							surround = false;
					}

					if(ey1 > ey2)
					{
						if(topy > ey1 || bottomy < ey2)
							surround = false;
					}

					else
					{
						if(topy > ey2 || bottomy < ey1)
							surround = false;
					}

					//判定　top~ = 四角形 e~ 辺の領域
					//始点終点が範囲に含まれている
					if(topx < ex1 && topy < ey1 && bottomx > ex2 && bottomy > ey2)
						edges.get(i).setSurround(true);

					else if(surround)
					{
						double tx, ty, bx, by;

						//傾き、切片を保持
						if((ex2 != ex1) && (ey2 != ey1))
						{
							double gradient = (double)(ey2-ey1)/(double)(ex2-ex1);
							double intercept = (double)(ey2) - (gradient*(double)(ex2));

							tx = ((double)(topy) - intercept)/gradient;
							bx = ((double)(bottomy) - intercept)/gradient;

							ty = gradient*(double)(topx) + intercept;
							by = gradient*(double)(bottomx) + intercept;
						}

						else
						{
							if(ex2 == ex1)
							{
								tx = ex1;  bx = ex2; ty = by = 0;
							}
							else
							{
								ty = ey1;  by = ey2; tx = bx = 0;
							}
						}

						if((ty > topy && ty < bottomy && by < bottomy && by > topy) ||
							 (tx > topx && tx < bottomx && bx < bottomx && bx > topx))
							edges.get(i).setSurround(true);
					}

					//その線が四角形に囲われていたらブレイク
					if(edges.get(i).getSurround())
					{
						inEdge = true;	break;
					}
				}
			}

			//四角形が十分小さくて、囲ったものがない場合は削除
			if(width < 10 && height < 10 && !inVertex && !inEdge)
			{
				isRect = false;	isDrawRect = true;	return false;
			}
			else return true;
		}
		//四角形に囲まれているかどうかをチェックここまで///////////
	}
	//マウスイベントここまで/////////////////////////////////////////////////////

	//キーリスナー///////////////////////////////////////////////////////////////
	public class myKeyListener extends KeyAdapter
	{
		//コンストラクタ
		myKeyListener(){}

		//キープレス
		public void keyPressed(KeyEvent e)
		{
			int keycode = e.getKeyCode();

			//トーラスの表示を変える（使用しない？）
			//int mod = e.getModifiersEx();

			//if(keycode == 'T')
			//	if((mod & InputEvent.CTRL_DOWN_MASK) != 0)
			//		System.out.println("Come");
			//トーラスの表示を変える（使用しない？）

			//境界線を通過させる
			if(keycode == KeyEvent.VK_SHIFT)
			{
				canThrough = true;
				addSelect = true;
			}

			if(keycode == KeyEvent.VK_DELETE)
			{
				//デリートが押されていたら囲まれた頂点、辺は削除
				if(madeRect)
				{
					for(int i=0; i<points.size(); i++)
					{
						if(points.get(i).getSurround())
						{
							points.remove(i);
							i = -1;
						}
					}

					for(int i=0; i<edges.size(); i++)
					{
						if(edges.get(i).getSurround())
						{
							edges.remove(i);
							i = -1;
						}
					}

					mOn = -1;
					ver.changeNumber(points.size());
					edg.changeNumber(edges.size());
				}
				madeRect = false; isRect = false;

				//オールクリアされていたら識別番号を0に戻す
				if(points.size() == 0)	count = 0;
			}

			if((keycode == KeyEvent.VK_UP 	|| keycode == KeyEvent.VK_DOWN ||
				 keycode == KeyEvent.VK_RIGHT || keycode == KeyEvent.VK_LEFT) &&
				 !isRect && !madeRect && isDrawLine == -1 && !moveOrdrag)
			{
				isChangeEdge = true;
				int diffx = 0, diffy = 0, diff = 4;
				double diffv = 360.0/p.getHeight() * diff;
				double diffs = 360.0/p.getWidth() * diff;

				new myMotionListener().checkIsMouseOn(mX, mY);

				//「↑」が押されたとき
				if(keycode == KeyEvent.VK_UP)
				{
					edgeOfXZplane -= diffv;
					if(edgeOfXZplane < 0.0)	edgeOfXZplane += 360.0;
					diffx = 0; diffy = -diff;
				}

				//「↓」が押されたとき
				else if(keycode == KeyEvent.VK_DOWN)
				{
					edgeOfXZplane += diffv;
					if(edgeOfXZplane >= 360.0)	edgeOfXZplane -= 360.0;
					diffx = 0; diffy = diff;
				}

				//「←」が押されたとき
				else if(keycode == KeyEvent.VK_LEFT)
				{
					edgeOfXYplane -= diffs;
					if(edgeOfXYplane < 0.0)	edgeOfXYplane = 360.0;
					diffx = -diff; diffy = 0;
				}

				//「→」が押されたとき
				else if(keycode == KeyEvent.VK_RIGHT)
				{
					edgeOfXYplane += diffs;
					if(edgeOfXYplane >= 360.0)	edgeOfXYplane = 0.0;
					diffx = diff; diffy = 0;
				}

				canThrough = true; onUp = onDown = onLeft = onRight = true;
				for(int i=0; i<points.size(); i++)
					new myMotionListener().moveGraph(i, diffx, diffy);
				canThrough = false; onUp = onDown = onLeft = onRight = false;
			}

			repaint();
		}

		//キーリリース
		public void keyReleased(KeyEvent e)
		{
			isChangeEdge = false;	canThrough = false; addSelect = false;
			repaint();
		}

		//キータイプ
		public void keyTyped(KeyEvent e)
		{}
	}
	//キーリスナーここまで/////////////////////////////////////////////////

	//JPanelクラスを拡張/////////////////////////////////////////////////////////
	public class panel extends JPanel{

		private boolean drawBound;

		//コンストラクタ
		panel(boolean drawB){
			drawBound = drawB;
			setBackground(Color.WHITE);
		}

		//パネルに描画
		public void paintComponent(Graphics g)
		{
			int onMouse = -1;		//マウスが乗っている頂点のアレイリスト上の番号を保持
			int click = -1;			//クリックされた頂点のアレイリスト上の番号を保持

			super.paintComponent(g);	//描画処理を呼び出し

			Graphics2D g2 = (Graphics2D)g;

			if(drawBound == false)	g2.translate(DIFF,DIFF);

			//アンチエイリアス処理（画像と文字）
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
													RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
													RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			//境界線を描画
			g2.setColor(Color.BLUE);
			g2.drawLine(0,0,p.getWidth(),0);
			g2.drawLine(0,0,0,p.getHeight());
			g2.drawLine(0, p.getHeight(), p.getWidth(), p.getHeight());
			g2.drawLine(p.getWidth(), 0, p.getWidth(), p.getHeight());
			g2.setColor(Color.BLACK);

			//辺描画
			for(int i=0; i<edges.size(); i++)
			{
				//四角形に囲まれているかどうかで実線か破線かを決定
				if(edges.get(i).getSurround())
				{
					float[] dash = {2.0f, 1.5f};
					g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
														BasicStroke.JOIN_MITER, 1, dash, 0));
				}

				//直線を描画
				for(int j=0; j<edges.get(i).getDivideNumber(); j++)
				{
					g2.drawLine(edges.get(i).getDivideStartX(j),
											edges.get(i).getDivideStartY(j),
											edges.get(i).getDivideEndX(j),
											edges.get(i).getDivideEndY(j));

					//境界線上の場合、フェイクの線を描く
					if(edges.get(i).getDivideStartX(j) == 0 &&
						 edges.get(i).getDivideEndX(j) == 0)
						g2.drawLine(p.getWidth(), edges.get(i).getDivideStartY(j),
												p.getWidth(), edges.get(i).getDivideEndY(j));

					else if(edges.get(i).getDivideStartX(j) == p.getWidth() &&
									edges.get(i).getDivideEndX(j) == p.getWidth())
						g2.drawLine(0, edges.get(i).getDivideStartY(j),
												0, edges.get(i).getDivideEndY(j));

					if(edges.get(i).getDivideStartY(j) == 0 &&
						 edges.get(i).getDivideEndY(j) == 0)
						g2.drawLine(edges.get(i).getDivideStartX(j), p.getHeight(),
												edges.get(i).getDivideEndX(j), p.getHeight());

					else if(edges.get(i).getDivideStartY(j) == p.getHeight() &&
									edges.get(i).getDivideEndY(j) == p.getHeight())
						g2.drawLine(edges.get(i).getDivideStartX(j), 0,
												edges.get(i).getDivideEndX(j), 0);
				}

				//実線描画に戻す
				g2.setStroke(new BasicStroke());
			}

			//描画中の辺
			if(isDrawLine != -1)
			{
				float[] dash = {5.0f, 2.0f};
				g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
													BasicStroke.JOIN_MITER, 1, dash, 0));

				for(int i=0; i<poem.getDivideNumber(); i++)
					g2.drawLine(poem.getDivideStartX(i),poem.getDivideStartY(i),
											poem.getDivideEndX(i),poem.getDivideEndY(i));

				//実線描画に戻す
				g2.setStroke(new BasicStroke());
			}

			//頂点描画

			//描画する頂点の直径と半径
			int dia = Vertex.getDiameter();	int rad = dia/2;

			for(int i=0;i<points.size();i++)
			{
				Vertex v = points.get(i);

				//辺の描画中ではなく、マウスが乗っかっている頂点（白抜きの円で表示）
				if(v.getMouseState() && isDrawLine == -1 && !v.getSurround())
					g2.drawOval(v.getXYi('x')-rad, v.getXYi('y')-rad, dia, dia);

				//辺の描画中の始点の頂点とマウスが乗っかっている頂点（四角で表示）
				else if((v.getMouseState() && isDrawLine != -1) || v.getMouseClick())
					g2.fillRect(v.getXYi('x')-rad, v.getXYi('y')-rad,dia,dia);

				//四角形の中に入っている点（点線で表示）
				else if(v.getSurround())
				{
					float[] dash = {2.0f, 1.5f};
					g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
														BasicStroke.JOIN_MITER, 1, dash, 0));
					g2.drawOval(v.getXYi('x')-rad, v.getXYi('y')-rad, dia, dia);
				}

				//何も関連がない頂点
				else	g2.fillOval(v.getXYi('x')-rad, v.getXYi('y')-rad, dia, dia);

				//境界線上に乗っている場合
				if(v.getIsOnBound() == Vertex.isUDbound ||
					 v.getIsOnBound() == Vertex.isCrossbound)
				{
					//辺の描画中ではなく、マウスが乗っかっている頂点（白抜きの円で表示）
					if(v.getMouseState() && isDrawLine == -1 || v.getSurround())
						g2.drawOval(v.getXYi('x')-rad,
												Math.abs(v.getXYi('y')-p.getHeight())-rad, dia, dia);

					//辺の描画中の始点の頂点とマウスが乗っかっている頂点（四角で表示）
					else if((v.getMouseState() && isDrawLine != -1) || v.getMouseClick())
						g2.fillRect(v.getXYi('x')-rad,
												Math.abs(v.getXYi('y')-p.getHeight())-rad, dia,dia);

					//何も関連がない頂点
					else g2.fillOval(v.getXYi('x')-rad,
													Math.abs(v.getXYi('y')-p.getHeight())-rad, dia, dia);
				}

				if(v.getIsOnBound() == Vertex.isLRbound ||
					 v.getIsOnBound() == Vertex.isCrossbound)
				{
					//辺の描画中ではなく、マウスが乗っかっている頂点（白抜きの円で表示）
					if(v.getMouseState() && isDrawLine == -1 || v.getSurround())
						g2.drawOval(Math.abs(v.getXYi('x')-p.getWidth())-rad,
												v.getXYi('y')-rad, dia, dia);

					//辺の描画中の始点の頂点とマウスが乗っかっている頂点（四角で表示）
					else if((v.getMouseState() && isDrawLine != -1) || v.getMouseClick())
						g2.fillRect(Math.abs(v.getXYi('x')-p.getWidth())-rad,
												v.getXYi('y')-rad, dia, dia);

					//何も関連がない頂点
					else g2.fillOval(Math.abs(v.getXYi('x')-p.getWidth())-rad,
											v.getXYi('y')-rad, dia, dia);
				}

				if(v.getIsOnBound() == Vertex.isCrossbound)
				{
					//辺の描画中ではなく、マウスが乗っかっている頂点（白抜きの円で表示）
					if(v.getMouseState() && isDrawLine == -1 || v.getSurround())
						g2.drawOval(Math.abs(v.getXYi('x')-p.getWidth())-rad,
												Math.abs(v.getXYi('y')-p.getHeight())-rad, dia, dia);

					//辺の描画中の始点の頂点とマウスが乗っかっている頂点（四角で表示）
					else if((v.getMouseState() && isDrawLine != -1) || v.getMouseClick())
						g2.fillRect(Math.abs(v.getXYi('x')-p.getWidth())-rad,
												Math.abs(v.getXYi('y')-p.getHeight())-rad, dia, dia);

					else g2.fillOval(Math.abs(v.getXYi('x')-p.getWidth())-rad,
													Math.abs(v.getXYi('y')-p.getHeight())-rad, dia, dia);
				}

				//描画を実線に戻す
				g2.setStroke(new BasicStroke());
			}

			//アルファ値
			AlphaComposite composite = AlphaComposite.getInstance(
																 AlphaComposite.SRC_OVER, 0.3f);

			//四角形描画
			if(isRect || madeRect)
			{
				// アルファ値を半透明に
				g2.setComposite(composite);
				g2.setColor(Color.PINK);

				int pX,pY,wX,hY;

				if(prevX < currentX && prevY < currentY)
				{
					g2.fillRect(prevX,prevY,currentX - prevX,currentY - prevY);
					pX = prevX; pY = prevY; wX = currentX - prevX; hY = currentY - prevY;
				}

				else if(prevX < currentX && prevY > currentY)
				{
					g2.fillRect(prevX,currentY,currentX - prevX,prevY - currentY);
					pX = prevX; pY = currentY;
					wX = currentX - prevX; hY = prevY - currentY;
				}

				else if(prevX > currentX && prevY < currentY)
				{
					g2.fillRect(currentX,prevY,prevX - currentX,currentY - prevY);
					pX = currentX; pY = prevY;
					wX = prevX - currentX; hY = currentY - prevY;
				}

				else
				{
					g2.fillRect(currentX,currentY,prevX - currentX,prevY - currentY);
					pX = currentX; pY = currentY;
					wX = prevX - currentX; hY = prevY - currentY;
				}

				//アルファ値を不透明にリセット
				composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
				g2.setComposite(composite);

				g2.drawRect(pX,pY,wX,hY);
			}
		}
		//パネル描画ここまで
	}
	//JPanelクラスを拡張/////////////////////////////////////////////////////////
}
//展開図を表示するクラス///////////////////////////////////////////////////////

/*
			//デバッグ
			if(e.getKeyChar() == 'v' || e.getKeyChar() == 'i')
			{
				if(points.size() == 0)
				{
					System.out.println("No Vertex");
					System.out.println();
				}

				else
				{
					System.out.println("Vertex Info");

					for(int i=0; i<points.size();i++)
					{
						Vertex v = points.get(i);
						System.out.println("Serial Number = " + v.getVertexNumber());
						System.out.println("mapX = " + v.getXYi('x'));
						System.out.println("mapY = " + v.getXYi('y'));
						//System.out.println("XYangle = " + v.getX());
						//System.out.println("XZangle = " + v.getY());
						System.out.println("Bound = " + v.getIsOnBound());
						System.out.println();
					}
				}
			}

			if(e.getKeyChar() == 'e' || e.getKeyChar() == 'i')
			{

				if(edges.size() == 0)
				{
					System.out.println("No Edge");
					System.out.println();
				}

				else
				{
					Edge ed;
					System.out.println("Edge Info");

					for(int i=0; i<edges.size(); i++)
					{
						ed = edges.get(i);
						System.out.println("Number = " + i);
						System.out.println("startX = " + ed.getStartX());
						System.out.println("startY = " + ed.getStartY());
						System.out.println("virtualX = " + ed.getVirtualX());
						System.out.println("virtualY = " + ed.getVirtualY());
						System.out.println("UD Through = " + ed.getUpDownThroughTimes());
						System.out.println("LR Through = " + ed.getLeftRightThroughTimes());
						System.out.println("---------------");
						if(ed.getDivideNumber() == 1)
							System.out.println("Edge is not divided.");
						else
						{
							System.out.println("Edge is divided in "
																							 + ed.getDivideNumber());
							System.out.println();

							for(int j=0; j<ed.getDivideNumber(); j++)
							{
								System.out.println("Division Number : " + j);
								System.out.println("divide startX = " + ed.getDivideStartX(j));
								System.out.println("divide startY = " + ed.getDivideStartY(j));
								System.out.println("divide endX = " + ed.getDivideEndX(j));
								System.out.println("divide endY = " + ed.getDivideEndY(j));
								//System.out.println("divide startXYangle = "
								//												 + ed.getDivideStartAngle(j).getX());
								//System.out.println("divide startXZangle = "
								//												 + ed.getDivideStartAngle(j).getY());
								//System.out.println("divide endXYangle = "
								//												 + ed.getDivideEndAngle(j).getX());
								//System.out.println("divide endXZangle = "
								//												 + ed.getDivideEndAngle(j).getY());
								System.out.println();
							}
						}
					}
				}
			}

			if(e.getKeyChar() == 'p' || e.getKeyChar() == 'i')
			{
				System.out.println("Panel Size");
				System.out.println("Width = " + p.getWidth());
				System.out.println("Height = " + p.getHeight());
				System.out.println();
			}

			if(e.getKeyChar() == 'a' || e.getKeyChar() == 'i')
			{
				System.out.println("境界線角度情報");
				System.out.println("edgeXYplane = " + edgeOfXYplane);
				System.out.println("edgeXZplane = " + edgeOfXZplane);
				System.out.println();
			}
			//デバッグここまで
*/