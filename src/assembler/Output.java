package assembler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Output {
	Output(Source p, Card c){
		buildTable(p);
		buildCard(c);
	}
	
	private void buildTable(Source program){
		BufferedWriter output = null;
		try {
            output = new BufferedWriter(new FileWriter(new File("src/Data/table.txt")));	//�}��table.txt
            if(program.getSourceError())
            	output.write("\t*********ERROR*********\r\n");
            for(int i = 0; i < program.size(); i++){
            	if(program.getSourceError())		//�Y�榡���~�A�@�泣���L
    				break;
    			if(program.getObject(i) == "FFFFFF")//�Y���������O�A���LObject code
    				output.write(String.format("%04X %-8s %-6s %-18s      \t%-31s\r\n", program.getLocation(i), program.getLabel(i), program.getOperation(i), program.getOperand(i), program.getComment(i)));
    			else if(program.getX(i))			//�Y��X�A�L�XObject,X
    				output.write(String.format("%04X %-8s %-6s %-18s %-6s\t%-31s\r\n", program.getLocation(i), program.getLabel(i), program.getOperation(i), program.getOperand(i) + ",X", program.getObject(i), program.getComment(i)));
    			else								//��l������L�X
    				output.write(String.format("%04X %-8s %-6s %-18s %-6s\t%-31s\r\n", program.getLocation(i), program.getLabel(i), program.getOperation(i), program.getOperand(i), program.getObject(i), program.getComment(i)));
    			
    			if(program.getOperationError(i))	//�p�GOP code�Q�е�Error�A�L�X���ܰT��
    				output.write("\t*********���w�q��operation*********\r\n");
    			if(program.getOperandError(i))		//�p�G�������Q�е�Error�A�L�X���ܰT��
    				output.write("\t**********���w�q��operand**********\r\n");
            }
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
          if ( output != null ) {
            try {
				output.close();			//����
			} catch (IOException e) {
				e.printStackTrace();
			}
          }
        }
		System.out.println("table.txt Create Successful");
	}
	
	private void buildCard(Card card){
		BufferedWriter output = null;
		try {
            output = new BufferedWriter(new FileWriter(new File("src/Data/card.txt")));	//�}��card.txt��
            output.write(String.format("H " + card.getH() + "\r\n"));		//�L�XH�d��
    		for(int i = 0; i < card.SizeOfT(); i++)							//�L�XT�d��
    			output.write(String.format("T "	 + card.getT(i) + "\r\n"));
    		output.write(String.format("E " + card.getE() + "\r\n"));		//�L�XE�d��
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
          if ( output != null ) {
            try {
				output.close();												//����
			} catch (IOException e) {
				e.printStackTrace();
			}
          }
        }
		System.out.println("card.txt Create Successful");
	}
}
