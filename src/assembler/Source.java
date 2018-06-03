package assembler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.Vector;

public class Source {
	//Check Flag
	private boolean sourceError = false;
	private boolean[] operationError;
	private boolean[] operandError;			
	private boolean[] locationError;
	private Vector<Boolean> X = new Vector<Boolean>();				
	//Builded
	private Vector<Integer> location = new Vector<Integer>();	
	private Vector<String> object = new Vector<String>();		
	//Source File
	private Vector<String> label = new Vector<String>();		
	private Vector<String> operation = new Vector<String>();	
	private Vector<String> operand = new Vector<String>();		
	private Vector<String> comment = new Vector<String>();		
	//Assist Building
	private OpCode op = new OpCode();							
	
	public Source() {
		super();
		readSrcFile();	
		checkCode();	
		if(!sourceError){	
			buildLocation();
			buildObjectcode();
		}
	}
	
	private void readSrcFile(){
		Scanner scanner = null;
		String line = new String();
		String[] part;
		try {scanner = new Scanner(new File("src/Data/SRCFILE.txt"));}
		catch (FileNotFoundException e) {e.printStackTrace();}		
		
		while(scanner.hasNextLine()){
			int i = 0;
			part = new String[4];
			line = scanner.nextLine();
			try{
				StringTokenizer splitLine = new StringTokenizer(line, " ");		
				while(splitLine.hasMoreTokens()){part[i++] = splitLine.nextToken();}
				for(;i < 4; i++)	part[i] = " ";
			}
			catch(Exception e){
				sourceError = true;
				break;
			}
			
			try{
				if(op.getOpcode(part[0]) != -1){
					label.add(new String(part[3]).toUpperCase());
					operation.add(new String(part[0]).toUpperCase());
					operand.add(new String(part[1]).toUpperCase());
					comment.add(new String(part[2]).toUpperCase());
				}
				else{
					label.add(new String(part[0]).toUpperCase());
					operation.add(new String(part[1]).toUpperCase());
					operand.add(new String(part[2]).toUpperCase());
					comment.add(new String(part[3]).toUpperCase());
				}
			}
			catch(Exception e){
				sourceError = true;
				label.add(new String(" "));
				operation.add(new String(" "));
				operand.add(new String(" "));
				comment.add(new String(" "));
			}
		}
	}
	
	private void checkCode(){
		
		if(sourceError)	return;
		operationError = new boolean[size()];
		operandError = new boolean[size()];
		//START
		if(!operation.get(0).equals("START") || !isInteger(operand.get(0),true))	
			sourceError = true;	
		X.add(false);
		for(int i = 1; i < size()-1; i++){
			if(op.getOpcode(operation.get(i)) == -1){
				operationError[i] = true;
			}
			else	operationError[i] = false;
			
			StringTokenizer splitOperand = new StringTokenizer(operand.get(i), ",");
			operand.set(i, splitOperand.nextToken());	
			if(splitOperand.hasMoreTokens()){		
				if(splitOperand.nextToken().toUpperCase().equals("X"))	X.add(true);
				else{
					X.add(false);														
					operandError[i] = true;
				}
			}
			else	X.add(false);
			if(operation.get(i).equals("BYTE")) {
				if(operand.get(i).charAt(0) == 'X'){								//�Yoperand�� X'   '
					char[] temp = new char[operand.get(i).length()-3];					//�r����μȦs��
					operand.get(i).getChars(2, operand.get(i).length()-1, temp, 0);		//�Noperand���h X'' �T��char
					if(!isInteger(new String(temp),true))	operandError[i] = true;		//�ˬd�Ȧs�������r��O�_��16�i��Ʀr
				}
				else if(operand.get(i).charAt(0) != 'C')	operandError[i] = true;	//�Yoperand�� C'   '�A���t��r�h���N���i
			}
			else if(operation.get(i).equals("WORD")){	//�YOp code�� WORD
				if(!isInteger(operand.get(i),false))	
					operandError[i] = true;
			}
			else if(operation.get(i).equals("RESW")){	//�YOp code�� RESW
				if(!isInteger(operand.get(i),false))	
					operandError[i] = true;
			}
			else if(operation.get(i).equals("RESB")){	//�YOp code�� RESB
				if(!isInteger(operand.get(i),false))	
					operandError[i] = true;
			}
			else if(operation.get(i).equals("RSUB")){	//�YOp code�� RSUB
				if(!operand.get(i).equals(" "))			
					operandError[i] = true;
			}
			else {//��l��operand�������ҡA�ˬd�Ӽ��ҬO�_�Q�w�q�L
				if (operand.get(i).charAt(0) == '#'){
				}
				else if(operand.get(i).charAt(0) == '@'){
				}
				else if(findLabel(operand.get(i)) == size())
					operandError[i] = true;
			}
		}
		//�ˬdEND�X�k��
		if(!operation.get(size()-1).equals("END") || ( findLabel(operand.get(size()-1)) == size() && !operand.get(size()-1).equals(" ")))
			sourceError = true;	//OP code������END �B operand �������i�Q�w�q�L�����ҡj�άO�i�ťաj
		X.add(false);
	}
	
	private void buildLocation(){
		int org = 0;
		locationError = new boolean[size()];
		location.add(Integer.valueOf(operand.get(0),16));
		for(int i = 1; i < size(); i++){
			if(locationError[i-1] || operationError[i-1] || operandError[i-1]){
				locationError[i] = true;
				location.add(0);
				continue;
			}
			
			if(operation.get(i).equals("ORG")){
				if(operand.get(i).equals(" ")){
					location.add(org);
					org = 0;
				}
				else{
					if(operation.get(i-1).equals("RESW") || operation.get(i-1).equals("RESB"))
						org = location.get(i-1) + op.getFormat(operation.get(i-1)) * Integer.valueOf(operand.get(i-1));
					else if(operation.get(i-1).equals("BYTE")){
						if(operand.get(i-1).charAt(0) == 'C')
							org = location.get(i-1) + (operand.get(i-1).length()-3);
						else if(operand.get(i-1).charAt(0) == 'X')
							org = location.get(i-1) + (operand.get(i-1).length()-3) / 2;
					}
					else
						org = location.get(i-1) + op.getFormat(operation.get(i-1));
					location.add(location.get(findLabel(operand.get(i))));
				}
			}
			else if(operation.get(i-1).equals("RESW") || operation.get(i-1).equals("RESB")){
				location.add(location.get(i-1) + op.getFormat(operation.get(i-1)) * Integer.valueOf(operand.get(i-1)));
			}
			//�YOp code��BYTE
			else if(operation.get(i-1).equals("BYTE")){
				if(operand.get(i-1).charAt(0) == 'C') 		//�Y��C�Ҧ�
					location.add(location.get(i-1) + (operand.get(i-1).length()-3) );	//C��1�Ӧr1Byte�A��} = �e�@��} + '���t��char�Ӽ�'
				else if(operand.get(i-1).charAt(0) == 'X')	//�Y��X�Ҧ�
					location.add(location.get(i-1) + (operand.get(i-1).length()-3) / 2);//X��2�Ӧr1Byte�A��} = �e�@��} + '���[��char�Ӽ�'���H2
			}
			else	//��l����}�h�̷Ӹ�Op code��format�A�[�W�e�@��}(�q�`��3)
				location.add(location.get(i-1) + op.getFormat(operation.get(i-1)));
		}
	}
	
	private void buildObjectcode(){
		int OP, x, flag;
		char[] temp;
		for(int i = 0; i < size(); i++){
			if(operationError[i] || operandError[i]){
				object.add("000000");					//�H000000�N���L�k�ͦ�����}
				continue;
			}
			OP = 0; x = 0;								//��l��OPcode�P X�X��
			OP = op.getOpcode(operation.get(i));
			if(X.get(i))	x = 1;						//�p�G��X,�Nx�ܼƳ]��1
			if(OP == 0xAA)	object.add("FFFFFF");		//�Y���������O�A�Nobject code �w�]��FFFFFF (�L�X�ɧP�_��)
			else if(operation.get(i).equals("WORD")){	//�YOp code��WORD�A�Noperand�����ର16�i��r��
				flag = Integer.valueOf(operand.get(i));
				object.add(String.format("%06X", flag));
			}
			else if(operation.get(i).equals("BYTE")){	//�YOp code��BYTE�A�A�i�@�B�P�_��X��C
				if(operand.get(i).charAt(0) == 'X'){
					temp = new char[operand.get(i).length()-3];
					operand.get(i).getChars(2, operand.get(i).length()-1, temp, 0);	//�N��޸������Ʀr���ΥX��
					object.add(new String(temp));									//�Ӧr��Y��object code
				}
				else if(operand.get(i).charAt(0) == 'C'){
					flag = 0;
					temp = new char[operand.get(i).length()-3];
					operand.get(i).getChars(2, operand.get(i).length()-1, temp, 0);	//�N��޸������Ʀr���ΥX��
					for(int j = 0; j < temp.length; j++)
						flag = flag * 0x100 + (int) new String(temp).charAt(j);		//�Nflag���������A�[�W�C��char��ASCII code
					object.add(String.format("%X", flag));							//�Nflag�H16�i��覡�ন�r��
				}
			}
			else if(operation.get(i).equals("RSUB"))	//�YOp code��RSUB�A�e�@���}�����[�WX���ন�r��
				object.add(String.format("%06X", OP * 0x10000 + x * 0x8000));
			else {//��l��operand�������ҡA�e�@��r��
				if (operand.get(i).charAt(0) == '#'){
					if(isNumeric(operand.get(i).substring(1,operand.get(i).length())) == true){//�P�_'#'�᭱�O�_���ƭ�
						flag = Integer.valueOf(operand.get(i).substring(1,operand.get(i).length()));
						object.add(String.format("%06X", (OP+1) * 0x10000 + flag));
					}else{
						if(location.get(findLabel(operand.get(i).substring(1,operand.get(i).length()))) - location.get(i+1) < 2047 &&
						   location.get(findLabel(operand.get(i).substring(1,operand.get(i).length()))) - location.get(i+1) > -2048){//pc�d��:-2048~2047
								if(location.get(findLabel(operand.get(i).substring(1,operand.get(i).length()))) < location.get(i+1)){
									object.add(String.format("%06X", (OP+2) * 0x10000 + x * 0x8000 + 2 * 0x100 + location.get(findLabel(operand.get(i).substring(1,operand.get(i).length()))) - location.get(i+1)));
								}else{
									object.add(String.format("%06X", (OP+1) * 0x10000 + x * 0x8000 + 2 * 0x100 + location.get(findLabel(operand.get(i).substring(1,operand.get(i).length()))) - location.get(i+1)));
								}
						}else{
							
						}
					}
				}
				else if(operand.get(i).charAt(0) == '@'){
					if(location.get(findLabel(operand.get(i).substring(1,operand.get(i).length()))) - location.get(i+1) < 2047 &&
					   location.get(findLabel(operand.get(i).substring(1,operand.get(i).length()))) - location.get(i+1) > -2048){//pc�d��:-2048~2047
						if(location.get(findLabel(operand.get(i).substring(1,operand.get(i).length()))) < location.get(i+1)){
							object.add(String.format("%06X", (OP+3) * 0x10000 + x * 0x8000 + 2 * 0x100 + location.get(findLabel(operand.get(i).substring(1,operand.get(i).length()))) - location.get(i+1)));
						}
						else{
							object.add(String.format("%06X", (OP+2) * 0x10000 + x * 0x8000 + 2 * 0x100 + location.get(findLabel(operand.get(i).substring(1,operand.get(i).length()))) - location.get(i+1)));
						}
					}
				}
				else{
//					if(location.get(findLabel(operand.get(i))) - location.get(i+1) < 2047 &&
//					   location.get(findLabel(operand.get(i))) - location.get(i+1) > -2048){//pc�d��:-2048~2047
						if(location.get(findLabel(operand.get(i))) < location.get(i+1)){
							object.add(String.format("%06X", (OP+4) * 0x10000 + x * 0x8000 + 2 * 0x1000 +location.get(findLabel(operand.get(i))) - location.get(i+1)));
						}
						else{
							object.add(String.format("%06X", (OP+3) * 0x10000 + x * 0x8000 + 2 * 0x1000 + location.get(findLabel(operand.get(i))) - location.get(i+1)));
						}
//					}
				}
				//System.out.println(Integer.toHexString(location.get(findLabel(operand.get(3)))-location.get(3+1)));
			}
		}
	}
	private boolean isInteger(String s, boolean hex) {
		try {Integer.parseInt(s); }	
		catch (NumberFormatException ex) {
			if(hex){
				try{Long.parseLong(s, 16);}	
				catch(NumberFormatException e){return false;}
			}
			else	return false;
		}
	    return true;
	}
	
	public int size(){
		return label.size();
	}
	
	public int findLabel(String l){
		int i;
		for(i = 0; i < size(); i++)	
			if(label.get(i).equals(l))	break;
		return i;
	}
	
	public Boolean getX(int index) {
		return X.get(index);
	}

	public int getLocation(int index) {
		return location.get(index);
	}
	
	public String getObject(int index) {
		return object.get(index);
	}
	
	public String getLabel(int index) {
		String temp = label.get(index);
		return temp;
	}
	public String getOperation(int index) {
		String temp = new String(operation.get(index));
		return temp;
	}
	public String getOperand(int index) {
		String temp = new String(operand.get(index));
		return temp;
	}
	public String getComment(int index) {
		String temp = new String(comment.get(index));
		return temp;
	}

	public boolean getSourceError() {
		return sourceError;
	}

	public boolean getOperationError(int index) {
		return operationError[index];
	}

	public boolean getOperandError(int index) {
		return operandError[index];
	}
	public static boolean isNumeric(String str){  
		  for (int i = str.length();--i>=0;){    
		   if (!Character.isDigit(str.charAt(i))){  
		    return false;  
		   }  
		  }  
		  return true;  
		}
	
	
}