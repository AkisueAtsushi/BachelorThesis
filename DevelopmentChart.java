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

//�W�J�}��\������N���X///////////////////////////////////////////////////////
public class DevelopmentChart extends JFrame
{
	//�t�B�[���h/////////////////////////////////////////////////////////////////

	//�W�J�}��\������p�l��
	private panel p;
	private panel fp;

	//�x�[�X
	private JPanel base;

	//���_�A�ӂ̐���\��
	private jlabel ver;
	private jlabel edg;

	//�t�F�C�N�̓W�J�}�̑傫��
	private int D_WIDTH;
	private int D_HEIGHT;

	//�t�F�C�N�Ɩ{���̓W�J�}�Ƃ̕�
	private int DIFF = Vertex.getDiameter();

	//�W�J�}�̋��ڂ��q���ׂ̏���
	private Robot r;

	//�_�̏��Q
	private ArrayList<Vertex> points = new ArrayList<Vertex>(0);

	//�ӂ̏��Q
	private ArrayList<Edge> edges = new ArrayList<Edge>(0);

	//�g�[�^���ŉ��ڂ̒��_���𔻕ʂ��邽�߂̕ϐ��i���ʔԍ��j
	private int count = 0;

	//�}�E�X���I���ɂȂ��Ă��钸�_�̃A���C���X�g��̏��Ԃ�ێ�
	private int mOn = -1;

	//�}�E�X���l�p�`�`��\���ǂ��� true = �\ false = �`��s��
	private boolean isDrawRect = true;

	//�l�p�`�`�撆���ǂ����𔻒�
	private boolean isRect = false;

	//�l�p�`���`�悪��������Ă���󋵂�ێ�
	private boolean madeRect = false;

	//�l�p�`�̎n�_
	private int prevX;
	private int prevY;

	//�l�p�`�̏I�_
	private int currentX;
	private int currentY;

	//�ӂ�`�撆���ǂ����𔻒�(�n�_�ƂȂ钸�_��"���ʔԍ�"��ێ�)
	private int isDrawLine = -1;

	//�`�撆�̕�
	private PartOfEdgeMaker poem;

	//�`�撆�̕ӂ��W�J�}�̐؂�ڂ�ʂ����񐔂�ێ�
	private int throughUD;	//�㉺�̐؂��
	private int throughLR;	//���E�̐؂��

	//�W�J�}�̐؂�ڂ̈ʒu��ێ�����
	private double edgeOfXZplane = 0.0;
	private double edgeOfXYplane = 0.0;

	//�W�J�}�Ƀ}�E�X������Ă��邩�ǂ����̏󋵂�ێ�
	private boolean isMouseOnChart = false;

	//�}�E�X����������Ă���g�[���X��̈ʒu
	private double mXZ_Plane = 0.0;
	private double mXY_Plane = 0.0;

	//�}�E�X����������Ă���W�J�}��̈ʒu
	private int mX = 0;
	private int mY = 0;

	//�ЂƂO�̃}�E�X�̈ʒu
	private int preX;
	private int preY;

	//���E�����ړ������ǂ����𔻒f
	private boolean isChangeEdge = false;

	//���_�����E����ʉ߂ł��邩�ǂ����𔻒�
	private boolean canThrough = false;

	//���E����̂ǂ̕����ɖ{���̒��_������Ă��邩�̏���ێ�
	private boolean onUp = false, onDown = false, onLeft = false, onRight = false;
	//�I���ł��钸�_�O���t�𑝂₹�邩�ǂ������f
	private boolean addSelect = false;

	//�}�E�X���h���b�O�����A���[�u��������@true = drag, false = move
	private boolean moveOrdrag = false;

	//�t�B�[���h�����܂�/////////////////////////////////////////////////////////

	//�R���X�g���N�^
	DevelopmentChart()
	{
		super("Net of Torus");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		setBounds(700, 0, 500, 485);

		//���C�A�E�g������
		setLayout(null);

		//���T�C�Y����
		setResizable(false);

		//�p�l�������t��
		p = new panel(true);
		p.setBounds(30,100,430,300);
		p.setOpaque(true);
		p.addMouseListener(new myListener());
		p.addMouseMotionListener(new myMotionListener());
		p.addKeyListener(new myKeyListener());
		p.setFocusable(true);
		p.requestFocus();
		getContentPane().add(p);

		//�t�F�C�N�̃p�l��
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

		//�R�}���h�̐���
		JLabel tutorial = new JLabel()
		{
			public void paintComponent(Graphics g)
			{
				Graphics2D g2 = (Graphics2D)g;

				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
														RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

				graphTutorial(g2);
			}

			//�`���[�g���A���\��
			public void graphTutorial(Graphics2D g2)
			{
				//���_���ЂƂ��Ȃ��Ƃ�
				if(points.size() == 0 && !madeRect && !isRect)
					g2.drawString("Click : make Vertex", 0, 10);

				//�ӂ̕`�撆�~�A���_��}�E�X�I���~�A�h���b�O���~�A�l�p�`�����~
				else if(mOn == -1 && isDrawLine == -1 &&
								moveOrdrag == false && !madeRect)
				{
					g2.drawString("Click : make Vertex", 0, 10);
					g2.drawString("Drag : select Vertex and Edge", 125, 10);
					g2.drawString("���������FShift position of edge of developed Torus."
																																				,0,30);
				}

				//�ӂ̕`�撆�~�A���_��}�E�X�I�����A�h���b�O���~
				else if(mOn != -1 && isDrawLine == -1 && moveOrdrag == false)
				{
					g2.drawString("Click : draw Edge", 0, 10);
					g2.drawString("Drag : move Vertex", 125, 10);
					g2.drawString("Double Click : remove this Vertex", 0, 30);
				}

				//���_���h���b�O���ŁA���E���ʉߕs�̂Ƃ�
				else if(mOn != -1 && isDrawLine == -1 &&
																						moveOrdrag == true && !canThrough)
					g2.drawString("+Shift : move Continuously", 0, 10);

				//���_���h���b�O���ŁA���E���ʉ߉\�̂Ƃ�
				else if(mOn != -1 && isDrawLine == -1 &&
																						moveOrdrag == true && canThrough)
					g2.drawString("-Shift : stop to the edge", 0, 10);

				//�ӂ̕`�撆�ŁA�}�E�X���n�_�ȊO�̒��_�ɃI�����Ă��Ȃ�
				else if(mOn == -1 && isDrawLine != -1)
					g2.drawString("Click : set Edge + make Vertex",0, 10);

				//�ӂ̕`�撆�ŁA�}�E�X���n�_�ȊO�̒��_�ɃI�����Ă���
				else if(mOn != -1 && isDrawLine != -1 &&
								points.get(mOn).getVertexNumber() != isDrawLine)
					g2.drawString("Click : set Edge", 0, 10);

				//�ӂ̕`�撆�ŁA�}�E�X���n�_�̒��_�ɃI�����Ă���
				else if(mOn != -1 && isDrawLine != -1 &&
								points.get(mOn).getVertexNumber() == isDrawLine)
					g2.drawString("Click : quit to draw Edge", 0, 10);

				//�G���A�I��
				else if(isRect && !madeRect)
					g2.drawString("Release : select Vertex and Edge", 0, 10);

				//�l�p�`��`���
				else if(madeRect && points.size() != 0 && !addSelect)
				{
					g2.drawString("Drag +Shift : select more Vertex and Edge", 0, 10);
					g2.drawString("Press 'delete' : remove selected Vertex and Edge",
																																0, 30);
					g2.drawString("Click : quit selection", 260, 10);
				}

				//����ɒǉ����Ďl�p�`��`��\�ȂƂ��iShift�������Ă���Ƃ��j
				else if(madeRect && points.size() != 0 && addSelect)
					g2.drawString("Drag : select more Vertex and Edge", 0, 10);

				//�����O���t���`����Ă��Ȃ��󋵂Ŏl�p�`��`�悵���ꍇ
				else if(madeRect && points.size() == 0)
					g2.drawString("Click : quit selection", 0, 10);
			}
		};
		tutorial.setBounds(30,410,430,60);
		tutorial.setFont(new Font("Serif", Font.PLAIN, 14));
		getContentPane().add(tutorial);

		//���_���\��
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

		//�Ӑ��\��
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

		//�x�[�X
		base = new JPanel();
		base.setBounds(0,0,getWidth(),getHeight());
		base.setBackground(Color.WHITE);
		getContentPane().add(base);
	}

	//�t�F�C�N�̃p�l���̑傫����ύX����
	public void changeFakePanel()
	{
		DIFF = Vertex.getDiameter();

		//�W�J�}�̑傫�����w��
		D_WIDTH = p.getWidth() + (DIFF*2);
		D_HEIGHT = p.getHeight() + (DIFF*2);

		fp.setBounds(30-DIFF,100-DIFF,D_WIDTH,D_HEIGHT);
	}

	//���_�̐���Ԃ�
	public int getVertexNumbers()
	{
		return points.size();
	}

	//�ӂ̐���Ԃ�
	public int getEdgeNumbers()
	{
		return edges.size();
	}

	//�e���_�̏���Ԃ�
	public Vertex getVertexInfo(int i)
	{
		if(i>=points.size()) return null;
		return points.get(i);
	}

	//�e�ӂ̏���Ԃ�
	public Edge getEdgeInfo(int i)
	{
		if(i>=edges.size()) return null;
		return edges.get(i);
	}

	//�ӂ̕`��󋵂�Ԃ�
	public boolean getIsDraw()
	{
		if(isDrawLine == -1)	return false;
		else	return true;
	}

	//�`�撆�̕ӂ̏���Ԃ�
	public PartOfEdgeMaker getIsDrawInfo()
	{
		return poem;
	}

	//�}�E�X���W�J�}�̒��ɂ��邩�ǂ����̏���Ԃ�
	public boolean getIsMouseOnChart()
	{
		return isMouseOnChart;
	}

	//�}�E�X������Ă���ʒu��Ԃ�
	public Point2D.Double getMouseOnTorus()
	{
		return new Point2D.Double(mXY_Plane, mXZ_Plane);
	}

	//���E���̈ړ������ǂ����̏󋵂��o�� true = �ړ���
	public boolean getIsChangeEdge()
	{
		return isChangeEdge;
	}

	//���E���̊p�x�����o��
	public Point2D.Double getTorusEdge()
	{
		return new Point2D.Double(edgeOfXYplane,edgeOfXZplane);
	}

	//�}�E�X���[�V�����C�x���g/////////////////////////////////////////////
	public class myMotionListener extends MouseMotionAdapter
	{
		//�h���b�O�ɂĒ��_�𔽑Α��Ɉړ� true = �\ false = �s�\
		private boolean isMoving = false;

		//�R���X�g���N�^
		myMotionListener()
		{
			try{ r = new Robot(); }
			catch(AWTException e)
			{
				e.printStackTrace();
				return;
			}
		}

		//�}�E�X�h���b�O///////////////////////////////////////////////////////////
		public void mouseDragged(MouseEvent e)
		{
			//�}�E�X���h���b�O���Ă����Ԃ�
			moveOrdrag = true;

			//�}�E�X�̈ʒu�������
			mXZ_Plane = (double)p.getWidth()/360.0*e.getY() + edgeOfXZplane;
			mXY_Plane = (double)p.getHeight()/360.0*e.getX() + edgeOfXYplane;

			//�}�E�X�̂ЂƂO�̈ʒu�����
			preX = mX; preY = mY;

			mX = e.getX(); mY = e.getY();

			//�}�E�X�����_�̏�ɏ���Ă��āA�ӂ̕`�撆�ł͂Ȃ��Ȃ�
			if(mOn != -1 && points.get(mOn).getMouseState() && isDrawLine == -1)
			{
				//�l�p�`�Ɉ͂܂�Ă��Ȃ��󋵂ɐݒ�
				for(int i=0; i<edges.size(); i++)
					edges.get(i).setSurround(false);
				for(int i=0;i<points.size(); i++)
					points.get(i).setSurround(false);

				Point pos = p.getLocationOnScreen();

				int diffX = e.getX()-points.get(mOn).getXYi('x');
				int diffY = e.getY()-points.get(mOn).getXYi('y');

				//�J�[�\���̈ʒu������i���E����ʉ߂ł���ꍇ�j
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

				//�J�[�\���̈ʒu������i���E����ʉ߂ł��Ȃ��ꍇ)
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
			//�}�E�X�����_�̏�ɂ̂��Ă���ꍇ�̏��������܂�

			else if(isDrawRect)	//�l�p�`�̕`��\�Ȃ�
			{
				isRect = true;
				currentX = e.getX();	currentY = e.getY();
			}

			//�ӂ̕`�撆�Ȃ�
			if(isDrawLine != -1)
			{
				p.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
				fp.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
				poem.setVirtualPosition(e.getX(),e.getY(),p.getWidth(),p.getHeight());
				poem.pointCalculate(p.getWidth(),p.getHeight());
			}

			repaint();
		}
		//�}�E�X�h���b�O�����܂�///////////////////////////////////////////////////

		//�}�E�X���W�J�}�̐؂�ڂɋ߂Â�����A���E��̃O���t���ړ�����
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

		//���E���ɋ߂����_�����E����Ɉړ�������
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

		//�}�E�X���[�u/////////////////////////////////////////////////////////////
		public void mouseMoved(MouseEvent e)
		{
			//�}�E�X���h���b�O���Ă��Ȃ���Ԃ�
			moveOrdrag = false;

			p.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
			fp.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));

			//�}�E�X�̃g�[���X��̈ʒu�������
			mXZ_Plane = (double)p.getWidth()/360.0*e.getY() + edgeOfXZplane;
			mXY_Plane = (double)p.getHeight()/360.0*e.getX() + edgeOfXYplane;

			//�}�E�X�̂ЂƂO�̈ʒu�����
			preX = mX; preY = mY;

			//�}�E�X�̓W�J�}��̈ʒu�������
			mX = e.getX(); mY = e.getY();

			//���E����̃O���t���ړ�������
			moveOnEdgeGraph(e.getX(), e.getY());

			//���E����ɃO���t���ړ�������
			moveToEdge();

			//�ӂ̕`�撆�Ȃ�
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
		//�}�E�X���[�u�����܂�/////////////////////////////////////////////////////

		//�}�E�X�����_�ɋz�����邩���ׂ� chackIsAbsortion�֐�(����=���_�̍��W)
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

		//�}�E�X�����_�̏�ɏ���Ă��邩�ǂ������ׂ� checkIsMouseOn�֐�
		public boolean checkIsMouseOn(int x, int y)
		{
			boolean isOn = false;

			//�S�Ă̒��_��T��
			for(int i=0; i<points.size(); i++)
			{
				Vertex v = points.get(i);

				v.setMouseState(false);	//������
			}

			//�S�Ă̒��_��T��
			for(int i=0; i<points.size(); i++)
			{
				Vertex v = points.get(i);

				//���ۂɒ��_������ꏊ�ɃJ�[�\��������Ă���
				if(v.isContain((double)x,(double)y))	isOn = true;

				//���_�̃����W�ɓ����Ă���
				else if(v.isContainRange((double)x, (double)y))
					if(checkIsAbsortion((double)v.getXYi('x'),(double)v.getXYi('y'), v))
						isOn = true;

				//�}�E�X�����_�̏�ɏ���Ă���Ȃ�
				if(isOn)
				{
					mOn = i;
					points.get(mOn).setMouseState(true); //�}�E�X��On�ł��邱�Ƃ�m�点��
					p.setCursor(new Cursor(Cursor.HAND_CURSOR));
					fp.setCursor(new Cursor(Cursor.HAND_CURSOR));
					return false;
				}
			}
			//�S�Ă̒��_�T�������܂�

			return true;
		}

		//���_,�ӂ��ړ�����֐��@moveGraph�֐�/////////////////////////////////////
			// i = �ړ����钸�_�̃A���C���X�g��̔ԍ�
			//mx = ���_��x���W�̈ړ���
			//my = ���_��y���W�̈ړ���
		public void moveGraph(int i,int mx, int my)
		{
			Vertex v = points.get(i);
			int number = points.get(i).getVertexNumber();

			//���_�̕\���ʒu��ύX
			v.setXY(v.getXYi('x')+mx, v.getXYi('y')+my, p.getWidth(), p.getHeight());

			//�W�J�}�̐؂�ڂ��q������
			boolean throughUp = false, throughDown = false,
							throughLeft = false, throughRight = false,
							isThrough = true;

			//���E���̒ʉ߂��\�Ȃ�
			int m = 1;	if(isMoving == false)	m = 0;

			//���_�̕\���ʒu��ύX�i���E����ʉ߉\�ȏꍇ�j
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

			//�}�E�X��������������_�Ɍq�������ӂƂ��̏I�_�̒��_���擾///////////

			//�}�E�X��������������_�Ɍq�������ӂ̃A���C���X�g��̔ԍ���ێ�
			ArrayList<Integer> edgeNumber = new ArrayList<Integer>(0);

			//�W�J�}�ɂ���ӂ̐���������
			for(int j=0; j<edges.size(); j++)
			{
				int[] end = edges.get(j).getEnd();

				//�}�E�X����������_���ӂ̎n�_�Ȃ�
				if(end[0] == number)
				{
					//�I�_�E�n�_���t�ɂ���
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

				//�}�E�X����������_���ӂ̏I�_�Ȃ�
				else if(end[1] == number)
					edgeNumber.add(j);
			}
			//�}�E�X��������������_�Ɍq�������ӂƂ��̏I�_�̒��_���擾�����܂�///

			//���_�ɕt�������ӂ̏��X�̏���ύX/////////////////////////////////
			for(int j=0; j<edgeNumber.size();j++)
			{
				Edge aE = edges.get(edgeNumber.get(j));

				if(throughUp)
				{
					//�������E���ɂ���ӂ��t���Ɉړ�����
					if(aE.getStartY() == 0 && aE.getVirtualY() == 0 &&
						 aE.getUpDownThroughTimes() == 0)
						edges.get(edgeNumber.get(j)).setStartY(p.getHeight());

					//(���ɋt���Ɉړ�����)���Α��̋��E����̕ӂłȂ��Ȃ�
					else if(!(aE.getStartY() == p.getHeight() &&
										aE.getVirtualY() == p.getHeight() &&
										aE.getUpDownThroughTimes() == 0))
						edges.get(edgeNumber.get(j)).setUpDownThroughTimes(-1);
				}

				else if(throughDown)
				{
					//�������E���ɂ���ӁA���_���t���Ɉړ�����
					if(aE.getStartY() == p.getHeight() &&
						 aE.getVirtualY() == p.getHeight() &&
						 aE.getUpDownThroughTimes() == 0)
						edges.get(edgeNumber.get(j)).setStartY(0);

					//(���ɋt���Ɉړ�����)���Α��̋��E����̕ӂłȂ��Ȃ�
					else if(!(aE.getStartY() == 0 && aE.getVirtualY() == 0 &&
									aE.getUpDownThroughTimes() == 0))
						edges.get(edgeNumber.get(j)).setUpDownThroughTimes(1);
				}

				else if(throughLeft)
				{
					//�������E���ɂ���ӁA���_���t���Ɉړ�����
					if(aE.getStartX() == 0 && aE.getVirtualX() == 0 &&
						 aE.getLeftRightThroughTimes() == 0)
						edges.get(edgeNumber.get(j)).setStartX(p.getWidth());

					//(���ɋt���Ɉړ�����)���Α��̋��E����̕ӂłȂ��Ȃ�
					else if(!(aE.getStartX() == p.getWidth() &&
										aE.getVirtualX() == p.getWidth() &&
										aE.getLeftRightThroughTimes() == 0))
						edges.get(edgeNumber.get(j)).setLeftRightThroughTimes(-1);
				}

				else if(throughRight)
				{
					//�������E���ɂ���ӁA���_���t���Ɉړ�����
					if(aE.getStartX() == p.getWidth() &&
						 aE.getVirtualX() == p.getWidth() &&
						 aE.getLeftRightThroughTimes() == 0)
						edges.get(edgeNumber.get(j)).setStartX(0);

					//(���ɋt���Ɉړ�����)���Α��̋��E����̕ӂłȂ��Ȃ�
					else if(!(aE.getStartX() == 0 && aE.getVirtualX() == 0 &&
										aE.getLeftRightThroughTimes() == 0))
						edges.get(edgeNumber.get(j)).setLeftRightThroughTimes(1);
				}

				//���z���xy���W���Đݒ�
				edges.get(edgeNumber.get(j)).setVirtualPosition(
																								v.getXYi('x'), v.getXYi('y'),
																								p.getWidth(), p.getHeight());

				//���_�ɕt�������ӂ̃p�[�c���쐬
				edges.get(edgeNumber.get(j)).pointCalculate(p.getWidth(),
																										p.getHeight());
			}
			//���_�ɕt�������ӂ̏��X�̏���ύX�����܂�/////////////////////////
		}
		//���_,�ӂ��ړ�����֐��@moveGraph�֐� �����܂�////////////////////////////
	}
	//�}�E�X���[�V�����C�x���g�����܂�///////////////////////////////////////////

	//�}�E�X�C�x���g/////////////////////////////////////////////////////////////
  public class myListener extends MouseAdapter implements MouseMotionListener
	{
		//�ӕ`�撆�̋N�_�̒��_�̃A���C���X�g��̏��Ԃ�ێ�
		private int clicked = -1;

		//�R���X�g���N�^
		myListener(){

			try{ r = new Robot(); }
			catch(AWTException e)
			{
				e.printStackTrace();
				return;
			}
		}

		//mouseEntered�֐�/////////////////////////////////////////////////////////
		public void mouseEntered(MouseEvent e)
		{
			isMouseOnChart = true;
		}
		//mouseEntered�֐������܂�/////////////////////////////////////////////////

		//�}�E�X�������Ƃ�(�ӂ̕`�撆�A�W�J�}�̐؂�ڂ�ʂ����񐔂����߂�)///////
		public void mouseExited(MouseEvent e)
		{
			//���̕`�撆���}�E�X�����_�ɏ���ĂȂ����
			if(isDrawLine != -1 && mOn == -1)
			{
				//�W�J�}�̐؂�ڂ��q������
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
		//mouseExited�����܂�//////////////////////////////////////////////////////

		//�}�E�X�v���X/////////////////////////////////////////////////////////////
		public void mousePressed(MouseEvent e)
		{
			//�O�̂���
			p.requestFocus();

			int count1 = 0;
			prevX = e.getX();
			prevY = e.getY();

			if(addSelect == false)
			{
				//�l�p�`�Ɉ͂܂�Ă��Ȃ��󋵂ɐݒ�
				for(int i=0; i<edges.size(); i++)
					edges.get(i).setSurround(false);
				for(int i=0;i<points.size(); i++)
					points.get(i).setSurround(false);
			}

			if(isDrawRect == true)
			{
				//�}�E�X�����_�̏�ɏ���Ă���Ȃ�l�p�`�`��͂��Ȃ�
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
		//�}�E�X�v���X�����܂�/////////////////////////////////////////////////////

		//�}�E�X�����[�X
		public void mouseReleased(MouseEvent e)
		{
			//���E�����z���Ă��܂������_�����Ƃɂ��ǂ�
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

			//�l�p�`�̕`�撆�Ȃ�
			if(isDrawRect == true)
			{
				currentX = e.getX();
				currentY = e.getY();

				//�l�p�`���ł��Ă��邩�ǂ�������
				if(prevX == currentX && prevY == currentY)	isRect = false;

				//�l�p�`�Ɋ܂܂�Ă���ӁA���_�𔻕�
				if(isRect)
					if(checkSurrounded(prevX,prevY,currentX,currentY))
						madeRect = true;
			}

			isDrawRect = true;
			repaint();
		}
		//�}�E�X�����[�X�����܂�

		//mouseClicked�֐�/////////////////////////////////////////////////////////
		public void mouseClicked(MouseEvent e)
		{
			int num = -1;	//�N���b�N���ꂽ���_�̎��ʔԍ�

			//�l�p�`���ł��Ă��āAShift�L�[��������Ă��Ȃ�
			if(madeRect && !addSelect){	isRect = false; madeRect = false; }

			else if(madeRect && addSelect){ isRect = false;}

			//�l�p�`���ł��Ă��Ȃ��Ȃ�
			else if(!isRect)
			{
				//�}�E�X�����_�ɏ���Ă��邩�ǂ����`�F�b�N
				if(mOn != -1 && points.get(mOn).getMouseState())
					num = points.get(mOn).getVertexNumber();//���_�̎��ʔԍ�

				//�}�E�X�����_�ɏ���Ă��Ȃ��Ȃ璸�_��V�K�쐬
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

					//�ӂ̕`�撆�Ȃ�ӂ�V�K�쐬�A�ӕ`��I��
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

				//�}�E�X�����_�̏�ɏ���Ă���
				else
				{
					//�_�u���N���b�N�Œ��_�폜////////////////////
					if(e.getClickCount() >= 2 && isDrawLine != -1)
					{
						//�폜���钸�_����o�Ă���ӂ��폜
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
					//�폜�����܂�///////////////////////////////

					//�}�E�X�����_�̏�ɏ���Ă��Ċ��_�u���N���b�N�łȂ�
					else
					{
						//�ӂ̕`�撆�łȂ��Ȃ�
						if(isDrawLine == -1)
						{
							points.get(mOn).setMouseClick(true);//���_��N���b�N��`�B
							isDrawLine = num;
							clicked = mOn;
							poem = new PartOfEdgeMaker(points.get(mOn).getXYi('x'),
																				 points.get(mOn).getXYi('y'),
																				 points.get(mOn).getXYi('x'),
																				 points.get(mOn).getXYi('y'),
																				 0,0,
																				 p.getWidth(),p.getHeight());
						}

						//�ӂ̕`�撆�Ȃ�
						else if(isDrawLine != -1)
						{
							//�ӕ`��̎n�_�Ɠ����łȂ��Ȃ�ӂ��쐬
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

							//����������Ԃɖ߂�
							points.get(clicked).setMouseClick(false);
							isDrawLine = -1;
							clicked = -1;
						}
					}
				}
				//�}�E�X�����_�ɏ���Ă��鎞�̏��������܂�
			}
			repaint();
		}
		//�}�E�X�N���b�N�֐������܂�///////////////////////////////////////////////

		//�l�p�`�Ɉ͂܂�Ă��邩�ǂ������`�F�b�N///////////////////
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

			//���_�̃`�F�b�N
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

			//�ӂ̃`�F�b�N
			for(int i=0; i<edges.size(); i++)
			{
				for(int j=0; j<edges.get(i).getDivideNumber(); j++)
				{
					boolean surround = true;

					//�ӂ̊e�p�[�c�̗��[�̒��_��xy���W���擾
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

					//����@top~ = �l�p�` e~ �ӂ̗̈�
					//�n�_�I�_���͈͂Ɋ܂܂�Ă���
					if(topx < ex1 && topy < ey1 && bottomx > ex2 && bottomy > ey2)
						edges.get(i).setSurround(true);

					else if(surround)
					{
						double tx, ty, bx, by;

						//�X���A�ؕЂ�ێ�
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

					//���̐����l�p�`�Ɉ͂��Ă�����u���C�N
					if(edges.get(i).getSurround())
					{
						inEdge = true;	break;
					}
				}
			}

			//�l�p�`���\���������āA�͂������̂��Ȃ��ꍇ�͍폜
			if(width < 10 && height < 10 && !inVertex && !inEdge)
			{
				isRect = false;	isDrawRect = true;	return false;
			}
			else return true;
		}
		//�l�p�`�Ɉ͂܂�Ă��邩�ǂ������`�F�b�N�����܂�///////////
	}
	//�}�E�X�C�x���g�����܂�/////////////////////////////////////////////////////

	//�L�[���X�i�[///////////////////////////////////////////////////////////////
	public class myKeyListener extends KeyAdapter
	{
		//�R���X�g���N�^
		myKeyListener(){}

		//�L�[�v���X
		public void keyPressed(KeyEvent e)
		{
			int keycode = e.getKeyCode();

			//�g�[���X�̕\����ς���i�g�p���Ȃ��H�j
			//int mod = e.getModifiersEx();

			//if(keycode == 'T')
			//	if((mod & InputEvent.CTRL_DOWN_MASK) != 0)
			//		System.out.println("Come");
			//�g�[���X�̕\����ς���i�g�p���Ȃ��H�j

			//���E����ʉ߂�����
			if(keycode == KeyEvent.VK_SHIFT)
			{
				canThrough = true;
				addSelect = true;
			}

			if(keycode == KeyEvent.VK_DELETE)
			{
				//�f���[�g��������Ă�����͂܂ꂽ���_�A�ӂ͍폜
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

				//�I�[���N���A����Ă����环�ʔԍ���0�ɖ߂�
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

				//�u���v�������ꂽ�Ƃ�
				if(keycode == KeyEvent.VK_UP)
				{
					edgeOfXZplane -= diffv;
					if(edgeOfXZplane < 0.0)	edgeOfXZplane += 360.0;
					diffx = 0; diffy = -diff;
				}

				//�u���v�������ꂽ�Ƃ�
				else if(keycode == KeyEvent.VK_DOWN)
				{
					edgeOfXZplane += diffv;
					if(edgeOfXZplane >= 360.0)	edgeOfXZplane -= 360.0;
					diffx = 0; diffy = diff;
				}

				//�u���v�������ꂽ�Ƃ�
				else if(keycode == KeyEvent.VK_LEFT)
				{
					edgeOfXYplane -= diffs;
					if(edgeOfXYplane < 0.0)	edgeOfXYplane = 360.0;
					diffx = -diff; diffy = 0;
				}

				//�u���v�������ꂽ�Ƃ�
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

		//�L�[�����[�X
		public void keyReleased(KeyEvent e)
		{
			isChangeEdge = false;	canThrough = false; addSelect = false;
			repaint();
		}

		//�L�[�^�C�v
		public void keyTyped(KeyEvent e)
		{}
	}
	//�L�[���X�i�[�����܂�/////////////////////////////////////////////////

	//JPanel�N���X���g��/////////////////////////////////////////////////////////
	public class panel extends JPanel{

		private boolean drawBound;

		//�R���X�g���N�^
		panel(boolean drawB){
			drawBound = drawB;
			setBackground(Color.WHITE);
		}

		//�p�l���ɕ`��
		public void paintComponent(Graphics g)
		{
			int onMouse = -1;		//�}�E�X������Ă��钸�_�̃A���C���X�g��̔ԍ���ێ�
			int click = -1;			//�N���b�N���ꂽ���_�̃A���C���X�g��̔ԍ���ێ�

			super.paintComponent(g);	//�`�揈�����Ăяo��

			Graphics2D g2 = (Graphics2D)g;

			if(drawBound == false)	g2.translate(DIFF,DIFF);

			//�A���`�G�C���A�X�����i�摜�ƕ����j
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
													RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
													RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			//���E����`��
			g2.setColor(Color.BLUE);
			g2.drawLine(0,0,p.getWidth(),0);
			g2.drawLine(0,0,0,p.getHeight());
			g2.drawLine(0, p.getHeight(), p.getWidth(), p.getHeight());
			g2.drawLine(p.getWidth(), 0, p.getWidth(), p.getHeight());
			g2.setColor(Color.BLACK);

			//�ӕ`��
			for(int i=0; i<edges.size(); i++)
			{
				//�l�p�`�Ɉ͂܂�Ă��邩�ǂ����Ŏ������j����������
				if(edges.get(i).getSurround())
				{
					float[] dash = {2.0f, 1.5f};
					g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
														BasicStroke.JOIN_MITER, 1, dash, 0));
				}

				//������`��
				for(int j=0; j<edges.get(i).getDivideNumber(); j++)
				{
					g2.drawLine(edges.get(i).getDivideStartX(j),
											edges.get(i).getDivideStartY(j),
											edges.get(i).getDivideEndX(j),
											edges.get(i).getDivideEndY(j));

					//���E����̏ꍇ�A�t�F�C�N�̐���`��
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

				//�����`��ɖ߂�
				g2.setStroke(new BasicStroke());
			}

			//�`�撆�̕�
			if(isDrawLine != -1)
			{
				float[] dash = {5.0f, 2.0f};
				g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
													BasicStroke.JOIN_MITER, 1, dash, 0));

				for(int i=0; i<poem.getDivideNumber(); i++)
					g2.drawLine(poem.getDivideStartX(i),poem.getDivideStartY(i),
											poem.getDivideEndX(i),poem.getDivideEndY(i));

				//�����`��ɖ߂�
				g2.setStroke(new BasicStroke());
			}

			//���_�`��

			//�`�悷�钸�_�̒��a�Ɣ��a
			int dia = Vertex.getDiameter();	int rad = dia/2;

			for(int i=0;i<points.size();i++)
			{
				Vertex v = points.get(i);

				//�ӂ̕`�撆�ł͂Ȃ��A�}�E�X����������Ă��钸�_�i�������̉~�ŕ\���j
				if(v.getMouseState() && isDrawLine == -1 && !v.getSurround())
					g2.drawOval(v.getXYi('x')-rad, v.getXYi('y')-rad, dia, dia);

				//�ӂ̕`�撆�̎n�_�̒��_�ƃ}�E�X����������Ă��钸�_�i�l�p�ŕ\���j
				else if((v.getMouseState() && isDrawLine != -1) || v.getMouseClick())
					g2.fillRect(v.getXYi('x')-rad, v.getXYi('y')-rad,dia,dia);

				//�l�p�`�̒��ɓ����Ă���_�i�_���ŕ\���j
				else if(v.getSurround())
				{
					float[] dash = {2.0f, 1.5f};
					g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT,
														BasicStroke.JOIN_MITER, 1, dash, 0));
					g2.drawOval(v.getXYi('x')-rad, v.getXYi('y')-rad, dia, dia);
				}

				//�����֘A���Ȃ����_
				else	g2.fillOval(v.getXYi('x')-rad, v.getXYi('y')-rad, dia, dia);

				//���E����ɏ���Ă���ꍇ
				if(v.getIsOnBound() == Vertex.isUDbound ||
					 v.getIsOnBound() == Vertex.isCrossbound)
				{
					//�ӂ̕`�撆�ł͂Ȃ��A�}�E�X����������Ă��钸�_�i�������̉~�ŕ\���j
					if(v.getMouseState() && isDrawLine == -1 || v.getSurround())
						g2.drawOval(v.getXYi('x')-rad,
												Math.abs(v.getXYi('y')-p.getHeight())-rad, dia, dia);

					//�ӂ̕`�撆�̎n�_�̒��_�ƃ}�E�X����������Ă��钸�_�i�l�p�ŕ\���j
					else if((v.getMouseState() && isDrawLine != -1) || v.getMouseClick())
						g2.fillRect(v.getXYi('x')-rad,
												Math.abs(v.getXYi('y')-p.getHeight())-rad, dia,dia);

					//�����֘A���Ȃ����_
					else g2.fillOval(v.getXYi('x')-rad,
													Math.abs(v.getXYi('y')-p.getHeight())-rad, dia, dia);
				}

				if(v.getIsOnBound() == Vertex.isLRbound ||
					 v.getIsOnBound() == Vertex.isCrossbound)
				{
					//�ӂ̕`�撆�ł͂Ȃ��A�}�E�X����������Ă��钸�_�i�������̉~�ŕ\���j
					if(v.getMouseState() && isDrawLine == -1 || v.getSurround())
						g2.drawOval(Math.abs(v.getXYi('x')-p.getWidth())-rad,
												v.getXYi('y')-rad, dia, dia);

					//�ӂ̕`�撆�̎n�_�̒��_�ƃ}�E�X����������Ă��钸�_�i�l�p�ŕ\���j
					else if((v.getMouseState() && isDrawLine != -1) || v.getMouseClick())
						g2.fillRect(Math.abs(v.getXYi('x')-p.getWidth())-rad,
												v.getXYi('y')-rad, dia, dia);

					//�����֘A���Ȃ����_
					else g2.fillOval(Math.abs(v.getXYi('x')-p.getWidth())-rad,
											v.getXYi('y')-rad, dia, dia);
				}

				if(v.getIsOnBound() == Vertex.isCrossbound)
				{
					//�ӂ̕`�撆�ł͂Ȃ��A�}�E�X����������Ă��钸�_�i�������̉~�ŕ\���j
					if(v.getMouseState() && isDrawLine == -1 || v.getSurround())
						g2.drawOval(Math.abs(v.getXYi('x')-p.getWidth())-rad,
												Math.abs(v.getXYi('y')-p.getHeight())-rad, dia, dia);

					//�ӂ̕`�撆�̎n�_�̒��_�ƃ}�E�X����������Ă��钸�_�i�l�p�ŕ\���j
					else if((v.getMouseState() && isDrawLine != -1) || v.getMouseClick())
						g2.fillRect(Math.abs(v.getXYi('x')-p.getWidth())-rad,
												Math.abs(v.getXYi('y')-p.getHeight())-rad, dia, dia);

					else g2.fillOval(Math.abs(v.getXYi('x')-p.getWidth())-rad,
													Math.abs(v.getXYi('y')-p.getHeight())-rad, dia, dia);
				}

				//�`��������ɖ߂�
				g2.setStroke(new BasicStroke());
			}

			//�A���t�@�l
			AlphaComposite composite = AlphaComposite.getInstance(
																 AlphaComposite.SRC_OVER, 0.3f);

			//�l�p�`�`��
			if(isRect || madeRect)
			{
				// �A���t�@�l�𔼓�����
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

				//�A���t�@�l��s�����Ƀ��Z�b�g
				composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f);
				g2.setComposite(composite);

				g2.drawRect(pX,pY,wX,hY);
			}
		}
		//�p�l���`�悱���܂�
	}
	//JPanel�N���X���g��/////////////////////////////////////////////////////////
}
//�W�J�}��\������N���X///////////////////////////////////////////////////////

/*
			//�f�o�b�O
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
				System.out.println("���E���p�x���");
				System.out.println("edgeXYplane = " + edgeOfXYplane);
				System.out.println("edgeXZplane = " + edgeOfXZplane);
				System.out.println();
			}
			//�f�o�b�O�����܂�
*/