/******************************************************************************//*																																						*//*														二次元閉曲面を再現															*//*																																						*//******************************************************************************/
import java.awt.Color;
import java.awt.Label;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.geom.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLCanvas;

/*import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLJPanel;*/

import javax.media.opengl.glu.GLU;

import com.sun.opengl.util.Animator;
import com.sun.opengl.util.GLUT;
import com.sun.opengl.util.BufferUtil;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.IntBuffer;
import java.nio.FloatBuffer;

import java.util.ArrayList;

public class AnchorRing implements GLEventListener
{
	//Field//////////////////////////////////////////////////////////////////////
	//ウィンドウの大きさ
	private int WIDTH = 700;
	private int HEIGHT = 700;

	//カメラの位置、x,yは0
	private float CAMERA = -20.0f;

  // ライト
  private float[] LPOSITION = { -10.0f, 0.0f, 10.0f, 0.0f };
  private float[] LSPECULAR = { 0.5f, 0.5f, 0.5f, 1.0f };
  private float[] LDIFFUSE = { 0.5f, 0.5f, 0.5f, 1.0f };
  private float[] LAMBIENT = { 0.8f, 0.8f, 0.8f, 0.5f };

  // 物体の反射率
  private float[] MSPECULAR = { 0.0f, 0.0f, 0.0f, 1.0f };
  private float[] MDIFFUSE = { 0.0f, 0.0f, 0.0f, 1.0f };
  private float[] MAMBIENT = { 1.0f, 0.0f, 0.0f, 1.0f };
  private float MSHININESS = 10.0f;

  private float[][] COLORS = {{ 1.0f, 0.0f, 0.0f, 1.0f },
                              { 0.0f, 1.0f, 0.0f, 1.0f },
                              { 0.0f, 0.0f, 0.3f, 1.0f },
                              { 0.0f, 0.0f, 0.3f, 1.0f },
                              { 1.0f, 0.0f, 1.0f, 1.0f },
                              { 0.0f, 1.0f, 1.0f, 1.0f }};
	private GL gl;
	private GLU glu;
	private GLUT glut;
	private Animator animator;

	//物体の回転
	private int prevMouseX;
	private int prevMouseY;
	private float angleX = 0.0f;
	private float angleY = 0.0f;
	private float distanceX = 0.0f;
	private float distanceY = 0.0f;

	//トーラスに関する変数群
	private double inRadius = 1.0;		//トーラスの内径（チューブの太さ）
	private double outRadius = 2.5;		//トーラスの外経（浮き輪の大きさ）
	private int nsides	= 25;					//チューブの輪の分割数
	private int rings = 50;						//浮き輪の輪の分割数

	//点の表示
	private double xzAngle = 0.0;
	private double xyAngle = 0.0;

	private static DevelopmentChart d;

	//辺の描画に関して
	int division = 50;	//線の描画を分割する数

	//三次元座標取得関係
	private boolean picked = false;
	private int pickX;
	private int pickY;
	private double[] oxyz = new double[3];
	private static final int BUFSIZE = 512;
	//Field end//////////////////////////////////////////////////////////////////

	//コンストラクタ
	public AnchorRing() {
		Frame frame = new Frame("Anchor Ring  - Graph Maker On Torus - ");

		//フレームの大きさセット
		frame.setSize(WIDTH, HEIGHT);

		// 3Dを描画するコンポーネント
		GLCanvas canvas = new GLCanvas();
		canvas.addGLEventListener(this);
		frame.add(canvas);

		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				prevMouseX = e.getX();
				prevMouseY = e.getY();

				//ダブルクリック
				if(e.getClickCount() >= 2)
				{
					picked = true;
					pickX = e.getX();
					pickY = e.getY();
				}
			}
		});

    canvas.addMouseMotionListener(new MouseMotionAdapter() {
    	public void mouseDragged(MouseEvent e) {
      	int x = e.getX();
        int y = e.getY();

        if (e.isShiftDown()) {
        	// 移動量の算出
          float diffX = (float)(x - prevMouseX)/10.0f;
          float diffY = (float)(prevMouseY - y)/10.0f;

          // 移動量の更新
          distanceX += diffX;
          distanceY += diffY;
        } else {	    
        	Dimension size = e.getComponent().getSize();

          // 回転量の算出
          // ウィンドウの端から端までで、360度回転するようにする
          float thetaY = 360.0f * ((float)(x-prevMouseX)/size.width);
          float thetaX = 360.0f * ((float)(prevMouseY-y)/size.height);

          // 角度の更新
          angleX -= thetaX;
          angleY += thetaY;
        }

        // 現在のマウスの位置を保存
        prevMouseX = x;
        prevMouseY = y;
      }
    });

		animator = new Animator(canvas);

		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e){
				animator.stop();
				System.exit(0);
			}
		});

		frame.setVisible(true);
		animator.start();
	}
	//コンストラクタここまで/////////////////////

	public void init(GLAutoDrawable drawable) {

		gl = drawable.getGL();
		glu = new GLU();
    glut = new GLUT();

		gl.glDepthFunc(GL.GL_LESS);
    gl.glDepthRange(0.0, 10.0);

    gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		//gl.glEnable(GL.GL_LINE_SMOOTH);	//線のアンチ
		//gl.glEnable(GL.GL_POLYGON_SMOOTH);	//ポリゴンのアンチ
		gl.glEnable(GL.GL_BLEND);
    gl.glEnable(GL.GL_LIGHTING);
    gl.glEnable(GL.GL_LIGHT0);
    gl.glEnable(GL.GL_DEPTH_TEST);
		gl.glBlendFunc(GL.GL_SRC_ALPHA,GL.GL_ONE_MINUS_SRC_ALPHA);
		gl.glHint(GL.GL_LINE_SMOOTH_HINT,GL.GL_DONT_CARE);

    gl.glEnable(GL.GL_CULL_FACE);

    gl.glEnable(GL.GL_NORMALIZE);

    gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, LPOSITION, 0);
    gl.glLightfv(GL.GL_LIGHT0, GL.GL_SPECULAR, LSPECULAR, 0);
    gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, LDIFFUSE, 0);
    gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, LAMBIENT, 0);

     gl.glMaterialfv(GL.GL_FRONT, GL.GL_SPECULAR, MSPECULAR, 0);
     gl.glMaterialfv(GL.GL_FRONT, GL.GL_DIFFUSE, MDIFFUSE, 0);
     gl.glMaterialf(GL.GL_FRONT, GL.GL_SHININESS, MSHININESS);
  }

  public void reshape(GLAutoDrawable drawable,
                      int x, int y,
                      int width, int height) {
  	float aspect = (float)height / (float)width;

    gl.glViewport(0, 0, width, height);

    gl.glMatrixMode(GL.GL_PROJECTION);
    gl.glLoadIdentity();
		//glu.gluPerspective(60.0, aspect, 1.0, 100.0);
    gl.glFrustum(-1.0, 1.0, -aspect, aspect,
                 5.0, 40.0);

    gl.glMatrixMode(GL.GL_MODELVIEW);
    gl.glLoadIdentity();
    gl.glTranslatef(0.0f, 0.0f, CAMERA);
  }

  public void display(GLAutoDrawable drawable) {
    gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

		gl.glPushMatrix();

    	//マウスの移動量に応じて移動
    	gl.glTranslatef(distanceX, distanceY, 0.0f);

    	// マウスの移動量に応じて回転
    	gl.glRotatef(angleX, 1.0f, 0.0f, 0.0f);
    	gl.glRotatef(angleY, 0.0f, 1.0f, 0.0f);
				DrawObject(GL.GL_RENDER);
		gl.glPopMatrix();

  	/*if(picked){
			pickup(pickX,pickY);
			pick(pickX,pickY);
			picked = false;
			return;
		}*/

		gl.glFlush();
  }

	//オブジェクトの描画
	private void DrawObject(int mode){

		Vertex v;	//頂点情報を受け取る
		Edge e;		//辺の情報を受け取る

		if(d != null)
		{
			//頂点を描画
			for(int i=0; i<d.getVertexNumbers(); i++)
			{
				v = d.getVertexInfo(i);

				xzAngle = v.getY() - d.getTorusEdge().getY();
				xyAngle =	v.getX() - d.getTorusEdge().getX();

				//辺描画の起点の頂点
				if(v.getMouseClick())
			 		gl.glMaterialfv(GL.GL_FRONT,GL.GL_AMBIENT, COLORS[1],0);
				//辺描画中ではなくマウスがオンした頂点
				else if(v.getMouseState() && d.getIsDraw() == false)
			 		gl.glMaterialfv(GL.GL_FRONT,GL.GL_AMBIENT, COLORS[5],0);
				//辺描画中にマウスがオンした頂点
				else if(v.getMouseState() && d.getIsDraw())
			 		gl.glMaterialfv(GL.GL_FRONT,GL.GL_AMBIENT, COLORS[1],0);
				//それ以外
				else
			 		gl.glMaterialfv(GL.GL_FRONT,GL.GL_AMBIENT, COLORS[3],0);

				gl.glPushMatrix();

					//頂点の位置を適切な場所に移動
			    gl.glTranslated((float)(Math.cos(Math.toRadians(xyAngle))*(Math.cos(Math.toRadians(xzAngle))+outRadius)),
													(float)(Math.sin(Math.toRadians(xyAngle))*(Math.cos(Math.toRadians(xzAngle))+outRadius)),
													(float)Math.sin(Math.toRadians(xzAngle)));

					//頂点を描く
					glut.glutSolidSphere(0.065,10,10);
				gl.glPopMatrix();
			}

			//線を描画
			gl.glLineWidth(3.0f);	//線の太さ

			for(int i=0; i<d.getEdgeNumbers(); i++)
			{
				e = d.getEdgeInfo(i);		//辺の情報を一つづつ取得
				drawLine(e, d.getTorusEdge());
			}

			//描画中の辺の描画
			if(d.getIsDraw())
			{
				PartOfEdgeMaker poet = d.getIsDrawInfo();
				drawLine(poet, d.getTorusEdge());
			}

			//展開図の切れ目を表示する
			if(d.getIsChangeEdge())
			{
		 		gl.glMaterialfv(GL.GL_FRONT,GL.GL_AMBIENT, COLORS[5],0);
				drawEdge(d.getTorusEdge().getX(),d.getTorusEdge().getY());
			}
		}

    //トーラスを描画
		if(mode == GL.GL_SELECT){
			gl.glPushName(1);
		}
			float[] materialCube = {1.0f,0.7f,0.9f,0.8f};
			gl.glDepthMask(false);
			//gl.glBlendFunc(GL.GL_SRC_ALPHA,GL.GL_DST_ALPHA);
			gl.glBlendFunc(GL.GL_SRC_ALPHA,GL.GL_ONE_MINUS_SRC_ALPHA);

			gl.glLineWidth(0.5f);	//線の太さ
	 		gl.glMaterialfv(GL.GL_FRONT,GL.GL_AMBIENT_AND_DIFFUSE, materialCube,0);
    	glut.glutSolidTorus(inRadius-0.01,outRadius-0.01,nsides,rings);
		gl.glPopName();

		gl.glFlush();
		gl.glDepthMask(true);

		//マウスが展開図上に乗っているならば、マウスのトーラス上の位置を表示
		/*if(d.getIsMouseOnChart())
		{
	 		gl.glMaterialfv(GL.GL_FRONT,GL.GL_AMBIENT, COLORS[4],0);
			drawEdge(d.getMouseOnTorus().getX(),d.getMouseOnTorus().getY());
		}*/
	}
	//DrawObject関数ここまで/////////////////////////////////////////////////////

	//辺の描画/////////////////////////////////
	private void drawLine(PartOfEdgeMaker poet, Point2D.Double torusEdge)
	{
		//辺の描画に関して
		double[][] points = new double[2][2];	//両端のxy座標を保持

		for(int i=0; i<poet.getDivideNumber(); i++)
		{
			points[0][0] = poet.getDivideStartAngle(i).getY() - torusEdge.getY();
			points[0][1] = poet.getDivideStartAngle(i).getX() - torusEdge.getX();
			points[1][0] = poet.getDivideEndAngle(i).getY() - torusEdge.getY();
			points[1][1] = poet.getDivideEndAngle(i).getX() - torusEdge.getX();

			double xzAngle = (points[1][0] - points[0][0])/division;
			double xyAngle = (points[1][1] - points[0][1])/division;

			gl.glMaterialfv(GL.GL_FRONT,GL.GL_AMBIENT, COLORS[5],0);

			gl.glPushMatrix();
				gl.glBegin(GL.GL_LINE_STRIP);
					for(int j=0; j<=division; j++)
					{
		    		gl.glVertex3d((float)(Math.cos(Math.toRadians(points[0][1]+(xyAngle*j)))*(Math.cos(Math.toRadians(points[0][0]+(xzAngle*j)))+outRadius)),
													(float)(Math.sin(Math.toRadians(points[0][1]+(xyAngle*j)))*(Math.cos(Math.toRadians(points[0][0]+(xzAngle*j)))+outRadius)),
													(float)Math.sin(Math.toRadians(points[0][0]+(xzAngle*j))));
					}
				gl.glEnd();
			gl.glPopMatrix();
		}
	}
	//辺の描画ここまで/////////////////////////////////

	//境界線の描画/////////////////////////////
	private void drawEdge(double xy, double xz)
	{
		xyAngle = -xy;
		xzAngle = -xz;

		gl.glLineWidth(3.0f);

		gl.glPushMatrix();
			gl.glTranslated(0,0,Math.sin(Math.toRadians(xzAngle)));
	   	gl.glBegin(GL.GL_LINE_STRIP);
				for(int i=0; i<=division; i++)
	   			gl.glVertex3d((double)(Math.cos(Math.toRadians(360.0/division*i))*
														(outRadius + Math.cos(Math.toRadians(xzAngle)))),
												(double)(Math.sin(Math.toRadians(360.0/division*i))*
														(outRadius + Math.cos(Math.toRadians(xzAngle)))),
												0);
	   	gl.glEnd();
		gl.glPopMatrix();

		gl.glPushMatrix();
			gl.glRotated(xyAngle, 0.0, 0.0, 1.0);

			gl.glPushMatrix();
				gl.glTranslated(outRadius,0,0);
		   	gl.glBegin(GL.GL_LINE_STRIP);
					for(int i=0; i<=division; i++)
		   			gl.glVertex3d((double)(Math.cos(Math.toRadians(360.0/division*i))*
																																	 inRadius),0,
													(double)(Math.sin(Math.toRadians(360.0/division*i))*
																																		inRadius));
		   	gl.glEnd();
			gl.glPopMatrix();
		gl.glPopMatrix();
	}
	//境界線の描画ここまで/////////////////////////////

	public void displayChanged(GLAutoDrawable drawable,
                             boolean modeChanged,
                             boolean deviceChanged) {}

	public static void main(String[] args)
	{
		new AnchorRing();
		d = new DevelopmentChart();
	}
}

			//座標軸
	 		/*gl.glMaterialfv(GL.GL_FRONT,GL.GL_AMBIENT, COLORS[0],0);
	    gl.glBegin(GL.GL_LINES);
	    	gl.glVertex3d(1.5,0,0);
				gl.glVertex3d(-1.5,0,0);
	    gl.glEnd();

	 		gl.glMaterialfv(GL.GL_FRONT,GL.GL_AMBIENT, COLORS[3],0);
	    gl.glBegin(GL.GL_LINES);
	    	gl.glVertex3d(0,-1.5,0);
				gl.glVertex3d(0,1.5,0);
	    gl.glEnd();

	 		gl.glMaterialfv(GL.GL_FRONT,GL.GL_AMBIENT, COLORS[2],0);
	    gl.glBegin(GL.GL_LINES);
	    	gl.glVertex3d(0,0,-1.0);
				gl.glVertex3d(0,0,1.0);
	    gl.glEnd();*/

/*
	//オブジェクトのピッキング
	private void pickup(int x, int y)
	{
		int[] selectBuf = new int[BUFSIZE];
		IntBuffer selectBuffer = BufferUtil.newIntBuffer(BUFSIZE);
		int hits;
		int[] viewport = new int[4];
		float current_aspect;
		gl.glSelectBuffer(BUFSIZE, selectBuffer);

		gl.glRenderMode(GL.GL_SELECT);

		gl.glInitNames();
		gl.glPushName(-1);

		gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPushMatrix();
			gl.glLoadIdentity();
			gl.glGetIntegerv(GL.GL_VIEWPORT, viewport,0);
			glu.gluPickMatrix((double)x,(double)viewport[3]-y, 3.0, 3.0, viewport, 0);
			current_aspect = (float)viewport[2]/(float)viewport[3];
    	gl.glFrustum(-1.0f, 1.0f, -current_aspect, current_aspect,
                 5.0f, 40.0f);
			gl.glMatrixMode( GL.GL_MODELVIEW);
			DrawObject(GL.GL_SELECT);
			gl.glMatrixMode(GL.GL_PROJECTION);
		gl.glPopMatrix();

		gl.glMatrixMode(GL.GL_MODELVIEW);
		hits = gl.glRenderMode(GL.GL_RENDER);
		selectBuffer.get(selectBuf);
		processHits(hits, selectBuf);
	}


 	private void processHits(int hits, int buffer[])
  {
    int names, ptr = 0;
    System.out.println("hits = " + hits);
    // ptr = (GLuint *) buffer;
    for (int i = 0; i < hits; i++)
    { // for each hit
      names = buffer[ptr];
      System.out.println(" number of names for hit = " + names);
      ptr++;
      System.out.println(" z1 is " + buffer[ptr]);
      ptr++;
      System.out.println(" z2 is " + buffer[ptr]);
      ptr++;
      System.out.print("\n   the name is ");
      for (int j = 0; j < names; j++)
      { // for each name
        System.out.println("" + buffer[ptr]);
        ptr++;
      }
      System.out.println();
    }
  }

	//三次元座標を取得
	private void pick(int x,int y) {

  	double modelMatrix[] = new double[16];
  	double projMatrix[] = new double[16];
  	int viewPort[] = new int[4];

  	// 現在の視点・視界の情報を取得
  	gl.glGetIntegerv(GL.GL_VIEWPORT, viewPort,0);

  	// depth buffer の値を取得
  	// zバッファの値を入れる配列
 		float z = 0;
  	gl.glReadPixels(x,viewPort[3]-y,1,1,
                    GL.GL_DEPTH_COMPONENT, GL.GL_FLOAT, FloatBuffer.wrap(z));

  	gl.glGetIntegerv(GL.GL_VIEWPORT, viewPort,0);
  	gl.glGetDoublev(GL.GL_MODELVIEW_MATRIX, modelMatrix,0);
  	gl.glGetDoublev(GL.GL_PROJECTION_MATRIX, projMatrix,0);

		//for(int i=0; i<4; i++)
			System.out.println(z);

		System.out.println("x = "+ x);
		System.out.println("y = "+ y);
  	// 3次元座標を取得
  	if(glu.gluUnProject((double)x,(double)viewPort[3]-y,0.8422,modelMatrix,0,projMatrix,0,viewPort,0,oxyz,0)){
  		System.out.println("frontX="+ oxyz[0]);
  		System.out.println("frontY="+ oxyz[1]);
  		System.out.println("frontZ="+ oxyz[2]);
		}
	}
	//三次元座標取得ここまで
*/