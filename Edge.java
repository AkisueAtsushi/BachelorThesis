/******************************************************************************//*																																						*//*													���̏���ێ�����N���X													*//* 																																						*//******************************************************************************/
import java.awt.geom.*;
import java.awt.Point;

public class Edge extends PartOfEdgeMaker
{
	private int[] end = new int[2];	//�ӂ̒[�@0 = �n�_�@1 = �I�_

	private Point endXY;

	//�l�p�`�Ɉ͂܂ꂽ���ǂ����̏���ێ�
	private boolean surrounded = false;

	//�R���X�g���N�^
		//num1,num2 = ���_�̎��ʔԍ�
		//ud,lr = �W�J�}�̏㉺�^���E��ʂ�����
		//sX,sY = �n�_�̓W�J�}���xy���W
		//eX,eY = �I�_�̓W�J�}���xy���W
		// w, h = �W�J�}�̕��A����
	Edge(int num1,int num2,
			 int   ud,int   lr,
			 int   sX,int   sY,
			 int   eX,int   eY,
			 int    w,int    h)
	{
		//PartOfEdgeMaker�N���X�̃R���X�g���N�^���g�p
		super(sX,sY,eX,eY,ud,lr,w,h);

		end[0] = num1;	//�n�_�̒��_�̎��ʔԍ�
		end[1] = num2;	//�I�_�̒��_�̎��ʔԍ�
	}

	//�ӂ̒[�_�̔ԍ�(���_�̎��ʔԍ�)��Ԃ�
	public int[] getEnd()
	{
		return end;
	}

	//�͂܂�Ă��邩�ǂ����̏������
	public void setSurround(boolean state)
	{
		surrounded = state;
	}

	//�͂܂�Ă��邩�ǂ����̏����o��
	public boolean getSurround()
	{
		return surrounded;
	}
}
