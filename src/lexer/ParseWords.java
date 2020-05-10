package lexer;

import java.io.*;
import java.nio.charset.StandardCharsets;


public class ParseWords { 
	private BufferedReader reader;
	private StringBuffer buffer;

	private static boolean isNumber(char c){//�Ƿ�Ϊ����
		if (c >= '0' && c <= '9')
			return true;
		return false;
	}

	private static boolean isLetter(char c){//�Ƿ�Ϊ��ĸ
		if (((c >= 'A' && c <= 'Z' )||(c >= 'a' && c <= 'z'))|| c == '_')
			return true;
		return false;
	}
 
	private static Integer search(String key){//�����ؼ�������Ӧ�ı���
		String bigWord = key.toUpperCase();
		for (SortCode sortCode : SymbolTable.sortCodeList){
			if (sortCode.word.equals(bigWord)){
				return sortCode.encode;
			}
		}
		return -1;
	}
	private int line;
	private char nextLine() throws IOException {
		char key;
		while( (key = (char) reader.read())!='\n') {}
		return key;
	}
	private void isNotes(char key) throws IOException {
		key = (char) reader.read();
		if (key == '*'){//����ע�� /* */
			while(true){
				while((key = (char) reader.read()) != '*'){
					if (key == '\n'){
						line ++;
					}
				}
				key = (char) reader.read();
				if (key == '/'){
					key = (char) reader.read();
					break;
				}
			}
		}else if (key == '/'){//����ע�� //
			while((key = (char) reader.read()) != '\r');
		}else{//�� /
			SymbolTable.wordItemList.add(new WordItem("/", search("/"), line));
		}
	}
	
	private boolean matchString() throws IOException {
		char key;
		String matched = "";
		while(true) {
			key = (char) reader.read();
			if(key == '"') {
				break;
			}else if(key == '\\'){
				key = (char) reader.read();
				if(key == '"' || key == '\\')
					matched = matched + key;
				else
					return false;
			}else if(key == '\n') {
				ErrorWriter.errorList.add(new ErrorType("�ַ�������", line, matched));
				nextLine();
				line++;
				return true;
			}
			else
				matched = matched + key;
		}
		SymbolTable.wordItemList.add(new WordItem(matched,search("�ַ���") , line));
		return true;
	}
	
	private String transformation(String num , int op) throws IOException {//����ת������
		String back = num;
		try {
			if (op == 8 || op == 2) {//�˽���תʮ����
				back = Integer.valueOf(num,op).toString();
			}else if (op == 16) {
				back = Integer.valueOf(num,op).toString();
			}
			else if(op != 10){
				System.out.println("��֧�ֽ�������");
			}
		} catch (Exception e) {
			// TODO: handle exception
			nextLine();
			buffer.setLength(0);
			ErrorWriter.addError("����ת������"+" ת"+op+"����ʱ����", line, ""+num);
			line++;
			return "";
		}
		return back;
	}
	
	private void rules() {//��������
		SymbolTable.initLoadSortCodeList();
		char key = 0;
		try {
			key = (char) reader.read();
			while(true){
				if (key == '\uFFFF')//�ж��Ƿ��ȡ�ĵ�����
					break;
				else if (key == '/'){//�ж��Ƿ�Ϊ /  /* */  //
					isNotes(key);
				}
				else if (key == ':'){//�Ƿ�Ϊ : ���� :=
					key = (char) reader.read();
					if (key == '='){
						SymbolTable.wordItemList.add(new WordItem(":=", search(":="), line));
						key = (char) reader.read();
					}else{
						SymbolTable.wordItemList.add(new WordItem(":", search(":"), line));
					}
				}else if (key == '\n'){//�Ƿ�Ϊ ���� \n
					line ++;
					key = (char) reader.read();
				}
				else if(key == '"'){
					//ʶ���ַ���
					if (!matchString()) {
						ErrorWriter.addError("ת�����", line, ""+key);
					}
					key = (char) reader.read();
				}
				else if (key == '\r' || key == ' ' || key == '\t' || key == '.'){//����tab���ո�
					key = (char) reader.read();
				}else if (key == '+' || key == '-' || key == '*' || key == ',' || key == ';' || key == '='|| key == '#' || key == '(' || key == ')'){//�����
					String word = key + "";
					SymbolTable.wordItemList.add(new WordItem(word, search(word), line));
					key = (char) reader.read();
				}else if(key == '>' || key == '<' ){//��ϵ��
					String word = ""+key;
					key = (char) reader.read();
					if (key == '=' || (word+key).equals("<>") ) {
						SymbolTable.wordItemList.add(new WordItem(word+key, search(word+key), line));
						key = (char) reader.read();
					}
					else
						SymbolTable.wordItemList.add(new WordItem(word, search(word), line));
				}
				else if (isLetter(key)){//��ʶ���ж�
					buffer.append(key);
					key = (char) reader.read();
					while ( isLetter(key) || isNumber(key) ){
						buffer.append(key);
						key = (char) reader.read();
					}
					int encode = search(buffer.toString());
					if (encode != -1){
						SymbolTable.wordItemList.add(new WordItem(buffer.toString(), encode, line));
					}else{
						SymbolTable.wordItemList.add(new WordItem(buffer.toString(), search("��ʶ��"), line));
					}
					buffer.setLength(0);
				}
				else if ( isNumber(key) ){
					buffer.append(key);
					key = (char) reader.read();
					int x = 0;
					while ( (isNumber(key)) || key == '.' || ( key <= 'F' && key >= 'A' ) || ( key <= 'f' && key >= 'a' ) || key == 'x' ||key == 'X' ){
						if(key == '.')
							x++;
						buffer.append(key);
						key = (char) reader.read();
					}
					if (x == 0 ) {//����
						String num = buffer.toString();
						if(num.length() >= 2){
							int binary = 2; 
							String op = num.substring(0, 2);
							if(op.charAt(0) == '0') {
								if( op.equals("0x") || op.equals("0X") ) {
									num = num.substring(2);
									binary = 16;
								}else if( op.equals("00") ) {
									num = num.substring(2);
									for(int i=0 ; i < num.length() ;i++) {//����Ӧ�ж�
										if( num.charAt(i) > '1' )
											binary = 8;
										if(num.charAt(i) > '7')
											binary = 10;
										if(num.charAt(i) >= 'A' && num.charAt(i) <= 'F')
										{
											binary = 16;
											break;
										}
									}
								}else {
									binary = 8;
								}
								num = transformation(num, binary);
							}
						}
						long number = 0;
						try {
							if (!num.equals(""))
								number = Long.parseLong(num);
							else {
								line--;
								number = Long.parseLong("A");
							}
						} catch (Exception e) {
							// TODO: handle exception
							ErrorWriter.errorList.add(new ErrorType("���ָ�ʽ����", line,"num"));
							nextLine();
							line++;
							buffer.setLength(0);
							key = (char) reader.read();
							continue;
						}
						if (number >= Integer.MIN_VALUE && number <= Integer.MAX_VALUE){
							SymbolTable.wordItemList.add(new WordItem(num, search("����"), line));
						}else {
							ErrorWriter.addError("����̫��Խ��", line, ""+buffer.toString());
						}
						buffer.setLength(0);
					}else if(x == 1){//������
						SymbolTable.wordItemList.add(new WordItem(buffer.toString(), search("������"), line));
						buffer.setLength(0);
					}else {
						//System.out.println("С�������:line:"+line+" number:"+buffer.toString());
						ErrorWriter.addError("С�������", line, ""+buffer.toString());
		                buffer.setLength(0);
						key = nextLine();
					}
				}else{
					//System.out.println("�Ƿ��ַ�:line:"+line+" key:"+key);
					ErrorWriter.addError("�Ƿ��ַ�", line, ""+key);
					key = nextLine();
	            }
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public static void writeFile(String x,String x2) {
        try {
            File writeName = new File(x2);
            writeName.createNewFile(); 
            try (FileOutputStream writerStream = new FileOutputStream(writeName); 
            		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(writerStream, "UTF-8")); ) 
            {
                writer.write(x);
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	private void outputFile() throws IOException {
		int line = 1;
		FileWriter fw = null;
		File output=new File("./PascalCode/lexer_output.pas");
		fw = new FileWriter(output, false);
		PrintWriter pw = new PrintWriter(fw);
		String lineOut = "";
		for (WordItem wordItem : SymbolTable.wordItemList){
			if(line != wordItem.line) {
				pw.println(lineOut);
				lineOut = "";
				pw.flush();
				//System.out.println();
				line = wordItem.line;
			}
			lineOut = lineOut + wordItem;
			//System.out.print(wordItem);
		}
		pw.println(lineOut);
		pw.flush();
		for(ErrorType errorType : ErrorWriter.errorList) {
			pw.println(errorType);
			pw.flush();
		}
		fw.flush();
		pw.close();
		fw.close();
	}
	private void outputSymbolTable() throws IOException {
		File output=new File("./PascalCode/SymbolTable.txt");
		BufferedWriter fw = new BufferedWriter (new OutputStreamWriter (new FileOutputStream (output,true), StandardCharsets.UTF_8));
		PrintWriter pw = new PrintWriter(fw);
		pw.println("����\t\t\t\t"+"����\t\t\t\t"+"ֵ");
		pw.flush();
		for(int i = 0 ; i < SymbolTable.wordItemList.size() ; i++) {
			WordItem wordItem = SymbolTable.wordItemList.get(i);
			SortCode sortCode = SymbolTable.getSortCodeByEncode(wordItem.encode);
			if(wordItem.encode<=131)
				pw.println(wordItem.key+"\t\t\t\t"+sortCode.symbol);
			else
				pw.println(sortCode.word+"\t\t\t\t"+sortCode.symbol+"\t\t\t\t"+wordItem.key);
			pw.flush();
		}
	}
	public void doWork(File file) throws IOException {
		reader = new BufferedReader(new FileReader(file));
		buffer = new StringBuffer();
		line = 1;
		rules();
		reader.close();
		outputFile();
		outputSymbolTable();
	}

	public Object doWorkByStr(String str) throws IOException {
		reader = new BufferedReader(new StringReader(str));
		buffer = new StringBuffer();
		line = 1;
		rules();
		reader.close();
		//outputFile();
		//outputSymbolTable();
		return SymbolTable.wordItemList;
	}
}