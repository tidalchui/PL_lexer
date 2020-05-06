package lexer;

import java.io.*;


public class ParseWords { 
	private BufferedReader reader;
	private StringBuffer buffer;

	private static boolean isNumber(char c){//是否为数字
		if (c >= '0' && c <= '9')
			return true;
		return false;
	}

	private static boolean isLetter(char c){//是否为字母
		if (((c >= 'A' && c <= 'Z' )||(c >= 'a' && c <= 'z'))|| c == '_')
			return true;
		return false;
	}
 
	private static Integer search(String key){//搜索关键字所对应的编码
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
		if (key == '*'){//多行注释 /* */
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
		}else if (key == '/'){//单行注释 //
			while((key = (char) reader.read()) != '\r');
		}else{//除 /
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
			}else
				matched = matched + key;
		}
		SymbolTable.wordItemList.add(new WordItem(matched,133 , line));
		return true;
	}
	
	private String transformation(String num , int op) {//进制转换函数
		String back = num;
		try {
			if (op == 8 || op == 2 || op == 16) {//八进制转十进制
				back = Integer.valueOf(num,op).toString();
			}else if(op != 10){
				System.out.println("不支持进制类型");
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("进制错误:line:"+line+" key:"+num+" 转"+op+"时出错");
			ErrorWriter.addError("转译错误"+" 转"+op+"时出错", line, ""+num);
		}
		return back;
	}
	
	private void rules() {//规则描述
		SymbolTable.initLoadSortCodeList();
		char key = 0;
		try {
			key = (char) reader.read();
			while(true){
				if (key == '\uFFFF')//判断是否读取文档结束
					break;
				else if (key == '/'){//判断是否为 /  /* */  //
					isNotes(key);
				}
				else if (key == ':'){//是否为 : 或者 :=
					key = (char) reader.read();
					if (key == '='){
						SymbolTable.wordItemList.add(new WordItem(":=", search(":="), line));
						key = (char) reader.read();
					}else{
						SymbolTable.wordItemList.add(new WordItem(":", search(":"), line));
					}
				}else if (key == '\n'){//是否为 换行 \n
					line ++;
					key = (char) reader.read();
				}
				else if(key == '"'){
					//识别字符串
					if (!matchString()) {
						System.out.println("转译错误:line:"+line+" key:"+key);
						ErrorWriter.addError("转译错误", line, ""+key);
					}
					key = (char) reader.read();
				}
				else if (key == '\r' || key == ' ' || key == '\t' || key == '.'){//跳过tab、空格
					key = (char) reader.read();
				}else if (key == '+' || key == '-' || key == '*' || key == ',' || key == ';' || key == '='|| key == '#' || key == '(' || key == ')'){//运算符
					String word = key + "";
					SymbolTable.wordItemList.add(new WordItem(word, search(word), line));
					key = (char) reader.read();
				}else if(key == '>' || key == '<' ){//关系符
					String word = ""+key;
					key = (char) reader.read();
					if (key == '=' || (word+key).equals("<>") ) {
						SymbolTable.wordItemList.add(new WordItem(word+key, search(word+key), line));
						key = (char) reader.read();
					}
					else
						SymbolTable.wordItemList.add(new WordItem(word, search(word), line));
				}
				else if (isLetter(key)){//标识符判断
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
						SymbolTable.wordItemList.add(new WordItem(buffer.toString(), search("标识符"), line));
					}
					buffer.setLength(0);
				}
				else if ( isNumber(key) ){
					buffer.append(key);
					key = (char) reader.read();
					int x = 0;
					while ( (isNumber(key)) || key == '.' || ( key <= 'F' && key >= 'A' ) || ( key <= 'f' && key >= 'a' ) ){
						if(key == '.')
							x++;
						buffer.append(key);
						key = (char) reader.read();
					}
					if (x == 0 ) {//整数
						String num = buffer.toString();
						if(num.length() > 2){
							int binary = 2; 
							String op = num.substring(0, 2);
							if(op.charAt(0) == '0') {
								if( op.equals("0x") || op.equals("0X") )
									binary = 16;
								else if( op.equals("00") ) {
									num = num.substring(2);
									for(int i=0 ; i < num.length() ;i++) {//自适应判断
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
								}
								num = transformation(num, binary);
							}
						}
						long number = Long.parseLong(num);
						if (number >= Integer.MIN_VALUE && number <= Integer.MAX_VALUE){
							SymbolTable.wordItemList.add(new WordItem(num, search("整数"), line));
						}else {
							System.out.println("数字太大越界:line:"+line+" number:"+buffer.toString());
							ErrorWriter.addError("数字太大越界", line, ""+buffer.toString());
						}
						buffer.setLength(0);
					}else if(x == 1){//浮点数
						SymbolTable.wordItemList.add(new WordItem(buffer.toString(), search("浮点数"), line));
						buffer.setLength(0);
					}else {
						System.out.println("小数点错误:line:"+line+" number:"+buffer.toString());
						ErrorWriter.addError("小数点错误", line, ""+buffer.toString());
		                buffer.setLength(0);
						key = nextLine();
					}
				}else{
					System.out.println("非法字符:line:"+line+" key:"+key);
					ErrorWriter.addError("非法字符", line, ""+key);
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
				System.out.println();
				line = wordItem.line;
			}
			lineOut = lineOut + wordItem;
			System.out.print(wordItem);
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
		FileWriter fw = null;
		File output=new File("./PascalCode/SymbolTable.txt");
		fw = new FileWriter(output, false);
		PrintWriter pw = new PrintWriter(fw);
		pw.println("名字\t\t\t\t"+"属性\t\t\t\t"+"值");
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
}