package assembler;

import java.util.Vector;

public class Card {
	private String H = new String();
	private Vector<String> T = new Vector<String>();
	private String E = new String();
	Card(Source source){
		super();
		if(source.getSourceError())	return;					//�Y���Y���榡���~�A�����ͥd��
		int count = 0;										//�d�����׭p�ƾ�
		int location = source.getLocation(0);				//�d���_�l��}�A��l�Ƭ�START��}
		String content = new String();						//�d�����e�w�İ�
		//����H�d��
		String programName,programStart,programLength;
		programName = source.getLabel(0);
		programStart = String.format(" %06X", source.getLocation(0));
		programLength = String.format(" %06X", source.getLocation(source.size()-1) - source.getLocation(0));
		H = programName + programStart + programLength;
		//����T�d��
		for(int i = 0; i < source.size()-1; i++){
			//�P�_�O�_�ݭn���d��  �i�[�W���qobject code�|�j��30Bytes�j ��   �i����object code���������O�j
			if(count +  (source.getObject(i).length() / 2) > 30 || source.getObject(i) == "FFFFFF"){
				if(content.equals("")){	//�Y�S��T�d�����e�|����X�A����Ū���U�@��object code
					location = source.getLocation(i+1);
					continue;
				}
				content = String.format("%06X %02X ", location, count) + content;	//�N�w�İϤ��e�e���[�W�d���_�l��}�B�d������
				T.add(content);										//�N�w�İϿ�X��T�d���V�q
				count = 0;	content = new String();					//��l�ƭp�ƾ��P�w�İ�
				if(source.getObject(i) == "FFFFFF"){
					location = source.getLocation(i+1);				//�Y���欰�������O�A�d���_�l��}�]���U�@��᪽��Ū���U�@��
					continue;
				}
				location = source.getLocation(i);					//�N�d���_�l��}�]�������}
			}
			content += String.format(" %s", source.getObject(i));	//�N����object code�[�i�w�İ�
			count += source.getObject(i).length() / 2;				//�d�����׳]��object code���װ��G (2�r1Byte)
		}
		if(!content.equals("")){	//���ͳ̤@�iT�d��
			content = String.format("%06X ", location) + String.format("%02X", count) + content;
			T.add(content);
		}
		//����E�d��
		E = String.format("%06X", source.getLocation(source.findLabel(source.getOperand(source.size()-1))));
	}
	
	public int SizeOfT(){
		return T.size();
	}
	
	public String getH() {
		return H;
	}
	
	public String getT(int index) {
		return T.get(index);
	}
	
	public String getE() {
		return E;
	}
}